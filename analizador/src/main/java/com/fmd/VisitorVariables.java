package com.fmd;

import java.util.HashMap;
import java.util.Map;

public class VisitorVariables extends CompiscriptBaseVisitor<Void> {

    private Map<String, String> tablaVariables = new HashMap<>();

    @Override
    public Void visitVariableDeclaration(CompiscriptParser.VariableDeclarationContext ctx) {
        String nombre = ctx.ID().getText();
        String tipo = ctx.tipo().getText();
        if (tablaVariables.containsKey(nombre)) {
            System.err.println("Variable ya declarada: " + nombre);
        } else {
            tablaVariables.put(nombre, tipo);
        }
        return null;
    }

    // Ejemplo: visitar una asignación
    @Override
    public Void visitAssignment(CompiscriptParser.AssignmentContext ctx) {
        String nombreVar = ctx.ID().getText();
        if (!tablaVariables.containsKey(nombreVar)) {
            System.err.println("Error semántico: variable '" + nombreVar + "' no declarada.");
        }

        // Aquí podrías chequear tipos
        return null;
    }

    public Map<String, String> getTablaVariables() {
        return tablaVariables;
    }
}
