package com.fmd.modules;

public class SemanticError {
    private final String mensaje;
    private final int linea;
    private final int columna;

    public SemanticError(String mensaje, int linea, int columna) {
        this.mensaje = mensaje;
        this.linea = linea;
        this.columna = columna;
    }

    public String getMensaje() {
        return mensaje;
    }

    public int getLinea() {
        return linea;
    }

    public int getColumna() {
        return columna;
    }

    @Override
    public String toString() {
        return "[ERROR SEMÁNTICO] " + mensaje + " (línea " + linea + ", columna " + columna + ")";
    }
}
