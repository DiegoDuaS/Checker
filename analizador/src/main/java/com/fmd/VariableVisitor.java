package com.fmd;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fmd.modules.Symbol;

public class VariableVisitor extends CompiscriptBaseVisitor<String> {
    private final SemanticVisitor semanticVisitor;

    private Entorno entornoActual;
    private final Entorno raiz;

    public VariableVisitor(SemanticVisitor sv) {
        this.semanticVisitor = sv;
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
    public String visitConstantDeclaration(CompiscriptParser.ConstantDeclarationContext ctx) {
        String nombre = ctx.Identifier().getText();
        String tipo = ctx.typeAnnotation() != null ? ctx.typeAnnotation().getText() : "desconocido";

        if (ctx.expression() == null) {
            semanticVisitor.agregarError(
                    "La constante '" + nombre + "' debe inicializarse",
                    ctx.start.getLine(),
                    ctx.start.getCharPositionInLine());
        }

        if (entornoActual.existeLocal(nombre)) {
            semanticVisitor.agregarError(
                    "Constante '" + nombre + "' ya declarada en este scope",
                    ctx.start.getLine(),
                    ctx.start.getCharPositionInLine());
            return tipo;
        }

        if (tipo == null && ctx.expression() != null) {
            tipo = visit(ctx.expression());
        }
        if (tipo == null)
            tipo = "desconocido";

        Symbol sym = new Symbol(nombre, Symbol.Kind.CONSTANT, tipo, ctx, ctx.start.getLine(),
                ctx.start.getCharPositionInLine(), false);
        entornoActual.agregar(sym);
        return tipo;
    }

    @Override
    public String visitVariableDeclaration(CompiscriptParser.VariableDeclarationContext ctx) {
        String nombre = ctx.Identifier().getText();
        String tipo = ctx.typeAnnotation() != null ? ctx.typeAnnotation().getText() : "desconocido";

        if (entornoActual.existeLocal(nombre)) {
            semanticVisitor.agregarError(
                    "Variable '" + nombre + "' ya declarada en este scope",
                    ctx.start.getLine(),
                    ctx.start.getCharPositionInLine());
            return tipo;
        }

        // inferir tipo desde initializer si no hay anotación
        if (tipo == null && ctx.initializer() != null)
            tipo = visit(ctx.initializer().expression());

        if (tipo == null)
            tipo = "desconocido";

        Symbol sym = new Symbol(nombre, Symbol.Kind.VARIABLE, tipo, ctx, ctx.start.getLine(),
                ctx.start.getCharPositionInLine(), true);
        entornoActual.agregar(sym);
        return tipo;
    }

    @Override
    public String visitAssignment(CompiscriptParser.AssignmentContext ctx) {
        String nombreVar = ctx.Identifier().getText();
        Symbol sym = entornoActual.obtener(nombreVar);

        if (sym == null) {
            semanticVisitor.agregarError("Variable '" + nombreVar + "' no declarada",
                    ctx.start.getLine(), ctx.start.getCharPositionInLine());
            return "desconocido";
        }
        if (!sym.isMutable()) {
            semanticVisitor.agregarError("No se puede asignar a la constante '" + nombreVar + "'",
                    ctx.start.getLine(), ctx.start.getCharPositionInLine());
            return sym.getType();
        }

        String tipoExpr = visit(ctx.expression(0));
        // Chequeo simple de igualdad de cadenas de tipo; aquí podrías añadir coerciones
        if (!sym.getType().equals(tipoExpr) && !"desconocido".equals(tipoExpr)) {
            semanticVisitor.agregarError(
                    "No se puede asignar valor de tipo '" + tipoExpr + "' a variable '" + nombreVar + "' de tipo '"
                            + sym.getType() + "'",
                    ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }
        return tipoExpr;
    }

    @Override
    public String visitIdentifierExpr(CompiscriptParser.IdentifierExprContext ctx) {
        String nombre = ctx.Identifier().getText();
        Symbol sym = entornoActual.obtener(nombre);
        if (sym == null) {
            semanticVisitor.agregarError("Variable '" + nombre + "' no declarada",
                    ctx.start.getLine(), ctx.start.getCharPositionInLine());
            return "desconocido";
        }
        return sym.getType();
    }

    @Override
    public String visitLiteralExpr(CompiscriptParser.LiteralExprContext ctx) {
        if (ctx.Literal() != null) {
            String lit = ctx.Literal().getText();
            if (lit.matches("[0-9]+"))
                return "int";
            if (lit.startsWith("\""))
                return "string";
        }
        String texto = ctx.getText();
        if (texto.equals("true") || texto.equals("false"))
            return "boolean";
        if (texto.equals("null"))
            return "null";
        return "desconocido";
    }

    public void entrarScope() {
        entornoActual = new Entorno(entornoActual);
    }

    public void salirScope() {
        if (entornoActual.getPadre() != null) {
            entornoActual = entornoActual.getPadre();
        }
    }

    // Exportar tabla como Map<String, Symbol>
    public Map<String, Symbol> getAllSymbols() {
        return raiz.getAllSymbols();
    }

    // Compatibilidad: si quieres seguir devolviendo Map<String,String>
    public Map<String, String> getTablaVariables() {
        Map<String, String> res = new LinkedHashMap<>();
        for (Map.Entry<String, Symbol> e : getAllSymbols().entrySet()) {
            res.put(e.getKey(), e.getValue().getType());
        }
        return res;
    }
}
