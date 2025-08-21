package com.fmd;

import java.util.HashMap;
import java.util.Map;

public class VisitorFunciones extends CompiscriptBaseVisitor<Void> {
    /*
     * 🟠 Soporte para funciones recursivas — verificación de que pueden llamarse a
     * sí mismas.
     * 🟠 Soporte para funciones anidadas y closures — debe capturar variables del
     * entorno donde se definen.
     * 🟠 Detección de múltiples declaraciones de funciones con el mismo nombre (si
     * no se soporta sobrecarga).
     */
    private Map<String, String> tablaFunciones = new HashMap<>();

    @Override
    public Void visitDeclaracionFuncion(CompiscriptParser.DeclaracionFuncionContext ctx) {
        String nombre = ctx.ID().getText();
        tablaFunciones.put(nombre, "funcion");
        return null;
    }

    public Map<String, String> getTablaFunciones() {
        return tablaFunciones;
    }
}
