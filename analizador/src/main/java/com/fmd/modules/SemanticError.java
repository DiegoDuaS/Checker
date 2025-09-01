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

    // ========================================
    // MÉTODOS HELPER PARA OPERACIONES
    // ========================================

    // Operaciones aritméticas
    public static String getArithmeticErrorMessage(String operator, String leftType, String rightType) {
        return "Operación '" + operator + "' no válida entre tipos: '" + leftType + "' y '" + rightType + "'";
    }

    public static String getUnaryArithmeticErrorMessage(String operator, String operandType) {
        return "Operador '" + operator + "' unario requiere operando integer, encontrado: " + operandType;
    }

    public static String getDivisionByZeroMessage() {
        return "División por cero detectada";
    }

    // Operaciones lógicas
    public static String getLogicalErrorMessage(String operator, String operandType, boolean isLeft) {
        String position = isLeft ? "izquierdo" : "derecho";
        return "Operando " + position + " de '" + operator + "' debe ser boolean, encontrado: " + operandType;
    }

    public static String getUnaryLogicalErrorMessage(String operator, String operandType) {
        return "Operador '" + operator + "' requiere operando boolean, encontrado: " + operandType;
    }

    // Operaciones de comparación
    public static String getComparisonErrorMessage(String operator, String leftType, String rightType) {
        return "No se pueden comparar tipos incompatibles: '" + leftType + "' " + operator + " '" + rightType + "'";
    }

    public static String getRelationalErrorMessage(String operator, String leftType, String rightType) {
        return "Operación relacional '" + operator + "' no válida entre tipos: '" + leftType + "' y '" + rightType + "'";
    }

    public static String getNonOrderableTypeMessage(String operator, String type) {
        return "Operación relacional '" + operator + "' no soportada para tipo: '" + type + "'";
    }

    // Getters originales
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