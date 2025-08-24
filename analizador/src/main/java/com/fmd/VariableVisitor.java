package com.fmd;

import java.util.HashMap;
import java.util.Map;

public class VariableVisitor extends CompiscriptBaseVisitor<String> {

    // -----------------------
    // Clase para manejar scopes
    // -----------------------
    public static class Entorno {
        private final Map<String, String> variables = new HashMap<>();
        private final Entorno padre;

        public Entorno(Entorno padre) {
            this.padre = padre;
        }

        public Entorno getPadre() {
            return padre;
        }

        public boolean existeLocal(String nombre) {
            return variables.containsKey(nombre);
        }

        public boolean existeGlobal(String nombre) {
            if (variables.containsKey(nombre)) return true;
            return padre != null && padre.existeGlobal(nombre);
        }

        public void agregar(String nombre, String tipo) {
            variables.put(nombre, tipo);
        }

        public String obtener(String nombre) {
            if (variables.containsKey(nombre)) return variables.get(nombre);
            if (padre != null) return padre.obtener(nombre);
            return null; // no declarado
        }

        public Map<String, String> getVariables() {
            return variables;
        }
    }

    private Entorno entornoActual = new Entorno(null);

    // -----------------------
    // Constantes
    // -----------------------
    @Override
    public String visitConstantDeclaration(CompiscriptParser.ConstantDeclarationContext ctx) {
        String nombre = ctx.Identifier().getText();
        String tipo = ctx.typeAnnotation() != null ? ctx.typeAnnotation().getText() : "desconocido";

        if (ctx.expression() == null) {
            System.err.println("Error semántico: la constante '" + nombre + "' debe inicializarse.");
        }

        if (entornoActual.existeLocal(nombre)) {
            System.err.println("Error semántico: la constante '" + nombre + "' ya está declarada en este scope.");
        } else {
            entornoActual.agregar(nombre, tipo);
        }

        return tipo;
    }

    // -----------------------
    // Variables normales
    // -----------------------
    @Override
    public String visitVariableDeclaration(CompiscriptParser.VariableDeclarationContext ctx) {
        String nombre = ctx.Identifier().getText();
        String tipo = ctx.typeAnnotation() != null ? ctx.typeAnnotation().getText() : "desconocido";

        if (entornoActual.existeLocal(nombre)) {
            System.err.println("Error semántico: variable '" + nombre + "' ya declarada en este scope.");
        } else {
            entornoActual.agregar(nombre, tipo);
        }

        return tipo;
    }

    // -----------------------
    // Asignaciones
    // -----------------------
    @Override
    public String visitAssignment(CompiscriptParser.AssignmentContext ctx) {
        String nombreVar = ctx.Identifier().getText();

        if (!entornoActual.existeGlobal(nombreVar)) {
            System.err.println("Error semántico: variable '" + nombreVar + "' no declarada.");
            return "desconocido";
        }

        String tipoDeclarado = entornoActual.obtener(nombreVar);
        String tipoExpr = visit(ctx.expression(0));

        if (!tipoDeclarado.equals(tipoExpr)) {
            System.err.println("Error semántico: no se puede asignar valor de tipo '"
                    + tipoExpr + "' a variable '" + nombreVar + "' de tipo '" + tipoDeclarado + "'.");
        }

        return tipoExpr;
    }

    // -----------------------
    // Literales e identificadores
    // -----------------------
    @Override
    public String visitIdentifierExpr(CompiscriptParser.IdentifierExprContext ctx) {
        String nombre = ctx.Identifier().getText();
        String tipo = entornoActual.obtener(nombre);
        if (tipo == null) {
            System.err.println("Error semántico: variable '" + nombre + "' no declarada.");
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

    // -----------------------
    // Entradas y salidas de bloques
    // -----------------------
    public void entrarScope() {
        entornoActual = new Entorno(entornoActual);
    }

    public void salirScope() {
        if (entornoActual.getPadre() != null) {
            entornoActual = entornoActual.getPadre();
        }
    }

    public Map<String, String> getTablaVariables() {
        return entornoActual.getVariables();
    }
}
