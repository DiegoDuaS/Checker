package com.fmd;

import java.util.HashMap;
import java.util.Map;

public class VariableVisitor extends CompiscriptBaseVisitor<String> {

    private final SemanticVisitor semanticVisitor;

    public VariableVisitor(SemanticVisitor sv) {
        this.semanticVisitor = sv;
    }

    public static class Entorno {
        private final Map<String, String> variables = new HashMap<>();
        private final Entorno padre;

        public Entorno(Entorno padre) {
            this.padre = padre;
        }

        public Entorno getPadre() { return padre; }

        public boolean existeLocal(String nombre) { return variables.containsKey(nombre); }

        public boolean existeGlobal(String nombre) {
            if (variables.containsKey(nombre)) return true;
            return padre != null && padre.existeGlobal(nombre);
        }

        public void agregar(String nombre, String tipo) { variables.put(nombre, tipo); }

        public String obtener(String nombre) {
            if (variables.containsKey(nombre)) return variables.get(nombre);
            if (padre != null) return padre.obtener(nombre);
            return null;
        }

        public Map<String, String> getVariables() { return variables; }
    }

    private Entorno entornoActual = new Entorno(null);

    @Override
    public String visitConstantDeclaration(CompiscriptParser.ConstantDeclarationContext ctx) {
        String nombre = ctx.Identifier().getText();
        String tipo = ctx.typeAnnotation() != null ? ctx.typeAnnotation().getText() : "desconocido";

        if (ctx.expression() == null) {
            semanticVisitor.agregarError(
                "La constante '" + nombre + "' debe inicializarse",
                ctx.start.getLine(),
                ctx.start.getCharPositionInLine()
            );
        }

        if (entornoActual.existeLocal(nombre)) {
            semanticVisitor.agregarError(
                "Constante '" + nombre + "' ya declarada en este scope",
                ctx.start.getLine(),
                ctx.start.getCharPositionInLine()
            );
        } else {
            entornoActual.agregar(nombre, tipo);
        }

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
                ctx.start.getCharPositionInLine()
            );
        } else {
            entornoActual.agregar(nombre, tipo);
        }

        return tipo;
    }

    @Override
    public String visitAssignment(CompiscriptParser.AssignmentContext ctx) {
        String nombreVar = ctx.Identifier().getText();

        if (!entornoActual.existeGlobal(nombreVar)) {
            semanticVisitor.agregarError(
                "Variable '" + nombreVar + "' no declarada",
                ctx.start.getLine(),
                ctx.start.getCharPositionInLine()
            );
            return "desconocido";
        }

        String tipoDeclarado = entornoActual.obtener(nombreVar);
        String tipoExpr = visit(ctx.expression(0));

        if (!tipoDeclarado.equals(tipoExpr)) {
            semanticVisitor.agregarError(
                "No se puede asignar valor de tipo '" + tipoExpr + "' a variable '" + nombreVar + "' de tipo '" + tipoDeclarado + "'",
                ctx.start.getLine(),
                ctx.start.getCharPositionInLine()
            );
        }

        return tipoExpr;
    }

    @Override
    public String visitIdentifierExpr(CompiscriptParser.IdentifierExprContext ctx) {
        String nombre = ctx.Identifier().getText();
        String tipo = entornoActual.obtener(nombre);
        if (tipo == null) {
            semanticVisitor.agregarError(
                "Variable '" + nombre + "' no declarada",
                ctx.start.getLine(),
                ctx.start.getCharPositionInLine()
            );
            return "desconocido";
        }
        return tipo;
    }

    @Override
    public String visitLiteralExpr(CompiscriptParser.LiteralExprContext ctx) {
        if (ctx.Literal() != null) {
            String lit = ctx.Literal().getText();
            if (lit.matches("[0-9]+")) return "int";
            if (lit.startsWith("\"")) return "string";
        }
        String texto = ctx.getText();
        if (texto.equals("true") || texto.equals("false")) return "boolean";
        if (texto.equals("null")) return "null";
        return "desconocido";
    }

    public void entrarScope() { entornoActual = new Entorno(entornoActual); }

    public void salirScope() { 
        if (entornoActual.getPadre() != null) {
            entornoActual = entornoActual.getPadre();
        }
    }

    public Map<String, String> getTablaVariables() { return entornoActual.getVariables(); }
}
