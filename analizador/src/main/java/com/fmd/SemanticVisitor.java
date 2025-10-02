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

import com.fmd.CompiscriptLexer;
import com.fmd.CompiscriptParser;
import com.fmd.CompiscriptBaseVisitor;

public class SemanticVisitor extends CompiscriptBaseVisitor<Object> {
    private final List<SemanticError> errores = new ArrayList<>();

    private Entorno entornoActual;
    private final Entorno raiz;
    private Symbol lastSymbol;
    private boolean dentroDeContextoPrint = false;
    private int loopDepth = 0;
    private int switchDepth = 0;

    private final VariableVisitor variableVisitor = new VariableVisitor(this);
    private final FunctionsVisitor functionsVisitor = new FunctionsVisitor(this);
    private final ClassesListener classesListener = new ClassesListener(this);
    private final LogicalVisitor logicalVisitor = new LogicalVisitor(this);
    private final ComparisonVisitor comparisonVisitor = new ComparisonVisitor(this);

    public SemanticVisitor() {
        this.entornoActual = new Entorno(null);
        this.raiz = this.entornoActual; // root/global
    }

    public boolean isDentroDeContextoPrint() {
        return dentroDeContextoPrint;
    }

    public void setDentroDeContextoPrint(boolean value) {
        this.dentroDeContextoPrint = value;
    }

    public static class Entorno {
        private final Map<String, Symbol> symbols = new HashMap<>();
        private final Entorno padre;
        private final List<Entorno> hijos = new ArrayList<>(); // <- hijos para scopes anidados

        public Entorno(Entorno padre) {
            this.padre = padre;
            if (padre != null) {
                padre.agregarHijo(this); // agregar este entorno al padre
            }
        }

        public Entorno getPadre() {
            return padre;
        }

        public List<Entorno> getHijos() {
            return hijos;
        }

        private void agregarHijo(Entorno hijo) {
            hijos.add(hijo);
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

        /** imprime recursivamente los símbolos por scope */
        public void imprimirScopes(String prefijo) {
            System.out.println(prefijo + "Scope (symbols: " + symbols.size() + ")");
            symbols.values().forEach(sym -> System.out.println(prefijo + "  " + sym));
            for (Entorno hijo : hijos) {
                hijo.imprimirScopes(prefijo + "    "); // recursión con sangría
            }
        }

        /**
         * Devuelve un mapa con todos los símbolos accesibles desde este scope:
         * incluye los símbolos de los ancestros (root -> ... -> this) y
         * además los de todos los hijos de forma recursiva.
         */
        public Map<String, Symbol> getAllScopesSymbols() {
            LinkedHashMap<String, Symbol> result = new LinkedHashMap<>();

            // 1. Ancestros: root → ... → this
            if (padre != null) {
                result.putAll(padre.getAllSymbols());
            }
            result.putAll(this.symbols);

            // 2. Hijos: recursivamente
            for (Entorno hijo : hijos) {
                result.putAll(hijo.getAllScopesSymbols());
            }

            return result;
        }

    }


    @Override
    public Void visitProgram(CompiscriptParser.ProgramContext ctx) {
        for (CompiscriptParser.StatementContext stmt : ctx.statement()) {
            visit(stmt);
        }
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
            String tipoCond = comparisonVisitor.visit(ctx.expression());
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
        loopDepth++;
        if (ctx.expression() != null) {
            String tipoCond = comparisonVisitor.visit(ctx.expression());
            if (!"boolean".equals(tipoCond)) {
                agregarError(
                        "Condición del while debe ser boolean, encontrada: " + tipoCond,
                        ctx.start.getLine(),
                        ctx.start.getCharPositionInLine());
            }
        }
        visit(ctx.block());
        loopDepth--;
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
    public Void visitTryCatchStatement(CompiscriptParser.TryCatchStatementContext ctx) {
        // Abrir scope para el bloque try
        entrarScope();
        visit(ctx.block(0)); // Bloque try
        salirScope();

        // Si hay catch
        if (ctx.block().size() > 1) {
            entrarScope();
            // Declarar la variable de excepción en el scope del catch
            if (ctx.Identifier() != null) {
                String exName = ctx.Identifier().getText();
                Symbol exSym = new Symbol(
                        exName,
                        Symbol.Kind.VARIABLE,
                        "string", // o tipo específico si tu gramática lo define
                        ctx,
                        ctx.start.getLine(),
                        ctx.start.getCharPositionInLine(),
                        true
                );
                entornoActual.agregar(exSym);
            }
            visit(ctx.block(1)); // Bloque catch
            salirScope();
        }

        return null;
    }

    @Override
    public Void visitForStatement(CompiscriptParser.ForStatementContext ctx) {
        loopDepth++;
        entrarScope();

        // Inicialización: puede ser declaración o asignación
        if (ctx.variableDeclaration() != null) {
            variableVisitor.visitVariableDeclaration(ctx.variableDeclaration());
        } else if (ctx.assignment() != null) {
            variableVisitor.visitAssignment(ctx.assignment());
        }

        // Condición
        if (ctx.expression(0) != null) {
            String tipoCond = comparisonVisitor.visit(ctx.expression(0));
            if (!"boolean".equals(tipoCond)) {
                agregarError(
                        "Condición del for debe ser boolean, encontrada: " + tipoCond,
                        ctx.start.getLine(),
                        ctx.start.getCharPositionInLine()
                );
            }
        }

        // Incremento (solo evaluar tipo)
        if (ctx.expression(1) != null) {
            variableVisitor.visit(ctx.expression(1));
        }

        // Bloque del for
        visit(ctx.block());

        salirScope();
        loopDepth--;
        return null;
    }

    @Override
    public Void visitForeachStatement(CompiscriptParser.ForeachStatementContext ctx) {
        loopDepth++;
        entrarScope();

        String iterName = ctx.Identifier().getText();
        String iterableType = variableVisitor.visit(ctx.expression()); // tipo del iterable

        // Verificar que sea tipo arreglo
        if (!iterableType.endsWith("[]")) {
            agregarError(
                    "No se puede iterar sobre '" + iterName + "' de tipo '" + iterableType + "'",
                    ctx.start.getLine(),
                    ctx.start.getCharPositionInLine()
            );
        } else {
            // Declarar la variable iteradora solo si es válido
            String elementType = iterableType.substring(0, iterableType.length() - 2); // quitar []
            Symbol iterSym = new Symbol(
                    iterName,
                    Symbol.Kind.VARIABLE,
                    elementType,
                    ctx,
                    ctx.start.getLine(),
                    ctx.start.getCharPositionInLine(),
                    true
            );
            entornoActual.agregar(iterSym);
        }

        // Bloque del foreach
        visit(ctx.block());

        salirScope();
        loopDepth--;
        return null;
    }

    @Override
    public Void visitSwitchStatement(CompiscriptParser.SwitchStatementContext ctx) {
        String switchType = variableVisitor.visit(ctx.expression());

        for (CompiscriptParser.SwitchCaseContext caseCtx : ctx.switchCase()) {
            visitSwitchCase(caseCtx, switchType);
        }

        if (ctx.defaultCase() != null) {
            visitDefaultCase(ctx.defaultCase());
        }

        return null;
    }

    public Void visitSwitchCase(CompiscriptParser.SwitchCaseContext ctx, String switchType) {

        String caseType = variableVisitor.visit(ctx.expression());
        if (!switchType.equals(caseType)) {
            agregarError(
                    "Tipo del case '" + caseType + "' no coincide con tipo del switch '" + switchType + "'",
                    ctx.start.getLine(),
                    ctx.start.getCharPositionInLine()
            );
        }
        switchDepth++;
        // Visitar statements del case
        for (CompiscriptParser.StatementContext stmt : ctx.statement()) {
            visit(stmt);
        }
        switchDepth--;
        return null;
    }

    public Void visitDefaultCase(CompiscriptParser.DefaultCaseContext ctx) {switchDepth++;
        for (CompiscriptParser.StatementContext stmt : ctx.statement()) {
            visit(stmt);
        }
        switchDepth--;
        return null;
    }

    @Override
    public Void visitDoWhileStatement(CompiscriptParser.DoWhileStatementContext ctx) {
        loopDepth++;
        // Ejecutar bloque
        visit(ctx.block());

        // Evaluar condición
        if (ctx.expression() != null) {
            String tipoCond = comparisonVisitor.visit(ctx.expression());
            if (!"boolean".equals(tipoCond)) {
                agregarError(
                        "Condición del do-while debe ser boolean, encontrada: " + tipoCond,
                        ctx.start.getLine(),
                        ctx.start.getCharPositionInLine()
                );
            }
        }
        loopDepth--;
        return null;
    }

    // break statement
    @Override
    public Void visitBreakStatement(CompiscriptParser.BreakStatementContext ctx) {
        if (loopDepth == 0 && switchDepth == 0) {
            agregarError("'break' fuera de un ciclo o switch", ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }
        return null;
    }

    // continue statement
    @Override
    public Void visitContinueStatement(CompiscriptParser.ContinueStatementContext ctx) {
        if (loopDepth == 0) {
            agregarError("'continue' fuera de un ciclo", ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }
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

    public LogicalVisitor getLogicalVisitor() {
        return logicalVisitor;
    }

    public ComparisonVisitor getComparisonVisitor() {
        return comparisonVisitor;
    }

    // Delegación de expresiones lógicas al LogicalVisitor
    @Override
    public Void visitLogicalOrExpr(CompiscriptParser.LogicalOrExprContext ctx) {
        logicalVisitor.visit(ctx);
        return null;
    }

    @Override
    public Void visitLogicalAndExpr(CompiscriptParser.LogicalAndExprContext ctx) {
        logicalVisitor.visit(ctx);
        return null;
    }

    @Override
    public Void visitEqualityExpr(CompiscriptParser.EqualityExprContext ctx) {
        comparisonVisitor.visit(ctx);
        return null;
    }

    @Override
    public Void visitRelationalExpr(CompiscriptParser.RelationalExprContext ctx) {
        comparisonVisitor.visit(ctx);
        return null;
    }

    // Delegación de expresiones aritméticas al VariableVisitor
    @Override
    public Void visitAdditiveExpr(CompiscriptParser.AdditiveExprContext ctx) {
        variableVisitor.visit(ctx);
        return null;
    }

    @Override
    public Void visitMultiplicativeExpr(CompiscriptParser.MultiplicativeExprContext ctx) {
        variableVisitor.visit(ctx);
        return null;
    }

    @Override
    public Void visitUnaryExpr(CompiscriptParser.UnaryExprContext ctx) {
        variableVisitor.visit(ctx);
        return null;
    }

    @Override
    public Void visitLiteralExpr(CompiscriptParser.LiteralExprContext ctx) {
        variableVisitor.visit(ctx);
        return null;
    }

    @Override
    public Void visitIdentifierExpr(CompiscriptParser.IdentifierExprContext ctx) {
        variableVisitor.visit(ctx);
        return null;
    }

    @Override
    public Void visitExpressionStatement(CompiscriptParser.ExpressionStatementContext ctx) {
        if (ctx.expression() != null) {
            comparisonVisitor.visit(ctx.expression()); // Usar comparisonVisitor para expresiones completas
        }
        return null;
    }

    @Override
    public Void visitPrintStatement(CompiscriptParser.PrintStatementContext ctx) {
        if (ctx.expression() != null) {
            setDentroDeContextoPrint(true);
            comparisonVisitor.visit(ctx.expression()); // Usar comparisonVisitor para expresiones complet
            setDentroDeContextoPrint(false);

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
        return classesListener.getCurrentClass();
    }

    public String getExpressionType(CompiscriptParser.ExpressionContext ctx) {
        if (ctx == null) return "desconocido";

        // Aquí puedes delegar al VariableVisitor y que retorne String
        return variableVisitor.visit(ctx);
    }

}