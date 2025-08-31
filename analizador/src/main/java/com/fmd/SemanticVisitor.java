package com.fmd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fmd.modules.SemanticError;
import com.fmd.modules.Symbol;
import org.antlr.v4.runtime.tree.ParseTree;

public class SemanticVisitor extends CompiscriptBaseVisitor<Void> {
    private final List<SemanticError> errores = new ArrayList<>();

    private Entorno entornoActual;
    private final Entorno raiz;
    private Symbol lastSymbol;
    private Symbol currentClass;

    private final VariableVisitor variableVisitor = new VariableVisitor(this);
    private final FunctionsVisitor functionsVisitor = new FunctionsVisitor(this);
    private final ClassesListener classesListener = new ClassesListener(this);

    public SemanticVisitor() {
        this.entornoActual = new Entorno(null);
        this.raiz = this.entornoActual; // root/global
    }

    public static class Entorno {
        private final Map<String, Symbol> symbols = new HashMap<>();
        private final Entorno padre;

        public Entorno(Entorno padre) {
            this.padre = padre;
        }

        public Entorno getPadre() {
            return padre;
        }

        public boolean existeLocal(String nombre) {
            return symbols.containsKey(nombre);
        }

        public boolean existeGlobal(String nombre) {
            if (symbols.containsKey(nombre))
                return true;
            return padre != null && padre.existeGlobal(nombre);
        }

        public void agregar(Symbol sym) {
            symbols.put(sym.getName(), sym);
        }

        public Symbol obtener(String nombre) {
            if (symbols.containsKey(nombre))
                return symbols.get(nombre);
            if (padre != null)
                return padre.obtener(nombre);
            return null;
        }

        /** devuelve solo los símbolos del entorno actual (no incluye padres) */
        public Map<String, Symbol> getSymbolsLocal() {
            return Collections.unmodifiableMap(symbols);
        }

        /** devuelve un mapa con la vista combinada de root->...->this (root primero) */
        public Map<String, Symbol> getAllSymbols() {
            LinkedHashMap<String, Symbol> result = new LinkedHashMap<>();
            if (padre != null)
                result.putAll(padre.getAllSymbols());
            result.putAll(this.symbols);
            return result;
        }
    }

    @Override
    public Void visitProgram(CompiscriptParser.ProgramContext ctx) {
        entrarScope();
        for (CompiscriptParser.StatementContext stmt : ctx.statement()) {
            visit(stmt);
        }
        salirScope();
        return null;
    }

    // General Statements
    @Override
    public Void visitBlock(CompiscriptParser.BlockContext ctx) {
        entrarScope();
        for (CompiscriptParser.StatementContext stmt : ctx.statement()) {
            visit(stmt);
        }
        salirScope();
        return null;
    }

    @Override
    public Void visitIfStatement(CompiscriptParser.IfStatementContext ctx) {
        if (ctx.expression() != null) {
            String tipoCond = variableVisitor.visit(ctx.expression());
            if (!"boolean".equals(tipoCond)) {
                agregarError(
                        "Condición del if debe ser boolean, encontrada: " + tipoCond,
                        ctx.start.getLine(),
                        ctx.start.getCharPositionInLine());
            }
        }
        visit(ctx.block(0));
        if (ctx.block().size() > 1) {
            visit(ctx.block(1));
        }
        return null;
    }

    @Override
    public Void visitWhileStatement(CompiscriptParser.WhileStatementContext ctx) {
        if (ctx.expression() != null) {
            String tipoCond = variableVisitor.visit(ctx.expression());
            if (!"boolean".equals(tipoCond)) {
                agregarError(
                        "Condición del while debe ser boolean, encontrada: " + tipoCond,
                        ctx.start.getLine(),
                        ctx.start.getCharPositionInLine());
            }
        }
        visit(ctx.block());
        return null;
    }

    // VARIABLES
    @Override
    public Void visitVariableDeclaration(CompiscriptParser.VariableDeclarationContext ctx) {
        variableVisitor.visitVariableDeclaration(ctx);
        return null;
    }

    @Override
    public Void visitConstantDeclaration(CompiscriptParser.ConstantDeclarationContext ctx) {
        variableVisitor.visitConstantDeclaration(ctx);
        return null;
    }

    @Override
    public Void visitAssignment(CompiscriptParser.AssignmentContext ctx) {
        variableVisitor.visitAssignment(ctx);
        return null;
    }

    // FUNCIONES
    @Override
    public Void visitFunctionDeclaration(CompiscriptParser.FunctionDeclarationContext ctx) {
        functionsVisitor.visitFunctionDeclaration(ctx);
        return null;
    }

    @Override
    public Void visitClassDeclaration(CompiscriptParser.ClassDeclarationContext ctx) {
        classesListener.enterClassDeclaration(ctx);

        for (CompiscriptParser.ClassMemberContext memberCtx : ctx.classMember()) {
            if (memberCtx.functionDeclaration() != null) {
                classesListener.enterFunctionDeclaration(memberCtx.functionDeclaration());
            } else if (memberCtx.variableDeclaration() != null) {
                classesListener.enterVariableDeclaration(memberCtx.variableDeclaration());
            } else if (memberCtx.constantDeclaration() != null) {
                classesListener.enterConstantDeclaration(memberCtx.constantDeclaration());
            }
        }


        classesListener.exitClassDeclaration(ctx);
        return null;
    }



    @Override
    public Void visitReturnStatement(CompiscriptParser.ReturnStatementContext ctx) {
        functionsVisitor.visitReturnStatement(ctx);
        return null;
    }

    // Manejo tabla de simbolos
    public void entrarScope() {
        entornoActual = new Entorno(entornoActual);
    }

    public void salirScope() {
        if (entornoActual.getPadre() != null) {
            entornoActual = entornoActual.getPadre();
        }
    }

    public Entorno getEntornoActual() {
        return entornoActual;
    }

    public Entorno getRaiz() {
        return raiz;
    }

    // Exportar tabla como Map<String, Symbol>
    public Map<String, Symbol> getAllSymbols() {
        return raiz.getAllSymbols();
    }

    // Manejo de errores
    public void agregarError(String mensaje, int linea, int columna) {
        errores.add(new SemanticError(mensaje, linea, columna));
    }

    public List<SemanticError> getErrores() {
        return errores;
    }

    // visitors
    public VariableVisitor getVariableVisitor() {
        return variableVisitor;
    }

    public FunctionsVisitor getFunctionsVisitor() {
        return functionsVisitor;
    }

    // Delegación de expresiones al VariableVisitor
    @Override
    public Void visitAdditiveExpr(CompiscriptParser.AdditiveExprContext ctx) {
        variableVisitor.visit(ctx); // Delegar al VariableVisitor
        return null;
    }

    @Override
    public Void visitMultiplicativeExpr(CompiscriptParser.MultiplicativeExprContext ctx) {
        variableVisitor.visit(ctx); // Delegar al VariableVisitor
        return null;
    }

    @Override
    public Void visitUnaryExpr(CompiscriptParser.UnaryExprContext ctx) {
        variableVisitor.visit(ctx); // Delegar al VariableVisitor
        return null;
    }

    @Override
    public Void visitLiteralExpr(CompiscriptParser.LiteralExprContext ctx) {
        variableVisitor.visit(ctx); // Delegar al VariableVisitor
        return null;
    }

    @Override
    public Void visitIdentifierExpr(CompiscriptParser.IdentifierExprContext ctx) {
        variableVisitor.visit(ctx); // Delegar al VariableVisitor
        return null;
    }

    @Override
    public Void visitExpressionStatement(CompiscriptParser.ExpressionStatementContext ctx) {
        if (ctx.expression() != null) {
            variableVisitor.visit(ctx.expression()); // Visitar la expresión
        }
        return null;
    }

    @Override
    public Void visitPrintStatement(CompiscriptParser.PrintStatementContext ctx) {
        if (ctx.expression() != null) {
            variableVisitor.visit(ctx.expression()); // Visitar la expresión del print
        }
        return null;
    }

    public void setLastSymbol(Symbol sym) {
        this.lastSymbol = sym;
    }

    public Symbol getLastSymbol() {
        return this.lastSymbol;
    }

    public Symbol getCurrentClass() {
        return currentClass;
    }

    public void setCurrentClass(Symbol cls) {
        this.currentClass = cls;
    }

    public String getExpressionType(CompiscriptParser.ExpressionContext ctx) {
        if (ctx == null) return "desconocido";

        // Aquí puedes delegar al VariableVisitor y que retorne String
        return variableVisitor.visit(ctx);
    }

}
