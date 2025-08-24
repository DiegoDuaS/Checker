package com.fmd;

import java.util.HashMap;
import java.util.Map;

public class VariableVisitor extends CompiscriptBaseVisitor<String> {

    // Tabla de símbolos: nombre de variable → tipo declarado
    private final Map<String, String> tablaVariables = new HashMap<>();

    // -----------------------------------------
    // Constantes: inicialización obligatoria
    // -----------------------------------------
    @Override
    public String visitConstantDeclaration(CompiscriptParser.ConstantDeclarationContext ctx) {
        String nombre = ctx.Identifier().getText();
        String tipo = ctx.typeAnnotation() != null ? ctx.typeAnnotation().getText() : "desconocido";

        if (ctx.expression() == null) {
            System.err.println("Error semántico: la constante '" + nombre + "' debe inicializarse.");
        }

        if (tablaVariables.containsKey(nombre)) {
            System.err.println("Error semántico: la constante '" + nombre + "' ya está declarada.");
        } else {
            tablaVariables.put(nombre, tipo);
        }

        return tipo;
    }

    // -----------------------------------------
    // Variables normales (let/var)
    // -----------------------------------------
    @Override
    public String visitVariableDeclaration(CompiscriptParser.VariableDeclarationContext ctx) {
        String nombre = ctx.Identifier().getText();
        String tipo = ctx.typeAnnotation() != null ? ctx.typeAnnotation().getText() : "desconocido";

        if (tablaVariables.containsKey(nombre)) {
            System.err.println("Error semántico: variable '" + nombre + "' ya declarada.");
        } else {
            tablaVariables.put(nombre, tipo);
        }

        return tipo;
    }

    // -----------------------------------------
    // Asignaciones: verificar tipos
    // -----------------------------------------
    @Override
    public String visitAssignment(CompiscriptParser.AssignmentContext ctx) {
        String nombreVar = ctx.Identifier().getText();

        if (!tablaVariables.containsKey(nombreVar)) {
            System.err.println("Error semántico: variable '" + nombreVar + "' no declarada.");
            return "desconocido";
        }

        String tipoDeclarado = tablaVariables.get(nombreVar);
        String tipoExpr = visit(ctx.expression(0)); // delega al visitor correcto

        if (!tipoDeclarado.equals(tipoExpr)) {
            System.err.println("Error semántico: no se puede asignar valor de tipo '"
                    + tipoExpr + "' a variable '" + nombreVar + "' de tipo '" + tipoDeclarado + "'.");
        }

        return tipoExpr;
    }

    // -----------------------------------------
    // Identificadores → buscar en tabla
    // -----------------------------------------
    @Override
    public String visitIdentifierExpr(CompiscriptParser.IdentifierExprContext ctx) {
        String nombre = ctx.Identifier().getText();
        return tablaVariables.getOrDefault(nombre, "desconocido");
    }

    // -----------------------------------------
    // Literales → deducir tipo
    // -----------------------------------------
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

    // -----------------------------------------
    // Para expresiones complejas: delegar al visitor
    // -----------------------------------------
    @Override
    public String visitExprNoAssign(CompiscriptParser.ExprNoAssignContext ctx) {
        return visit(ctx.conditionalExpr());
    }

    @Override
    public String visitAssignExpr(CompiscriptParser.AssignExprContext ctx) {
        return visit(ctx.assignmentExpr()); // delega recursivamente
    }

    // -----------------------------------------
    // Getter de tabla de variables
    // -----------------------------------------
    public Map<String, String> getTablaVariables() {
        return tablaVariables;
    }
}