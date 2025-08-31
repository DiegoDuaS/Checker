package com.fmd;

public class ComparisonVisitor extends CompiscriptBaseVisitor<String> {
    private final SemanticVisitor semanticVisitor;

    public ComparisonVisitor(SemanticVisitor semanticVisitor) {
        this.semanticVisitor = semanticVisitor;
    }

    /**
     * Maneja operaciones de igualdad (==, !=)
     * Valida que ambos operandos sean del mismo tipo
     */
    @Override
    public String visitEqualityExpr(CompiscriptParser.EqualityExprContext ctx) {
        // Obtener el primer operando (siempre existe)
        String tipoIzq = visit(ctx.relationalExpr(0));

        // Si solo hay un operando, retornamos su tipo (no hay operación)
        if (ctx.relationalExpr().size() == 1) {
            return tipoIzq;
        }

        // Procesar todas las operaciones de igualdad en cadena
        for (int i = 1; i < ctx.relationalExpr().size(); i++) {
            String tipoDer = visit(ctx.relationalExpr(i));
            String operador = ctx.getChild(2 * i - 1).getText(); // ==, !=

            // Validar compatibilidad de tipos para igualdad
            if (!sonTiposCompatiblesParaIgualdad(tipoIzq, tipoDer)) {
                semanticVisitor.agregarError(
                        "No se pueden comparar tipos incompatibles: '" + tipoIzq + "' " + operador + " '" + tipoDer + "'",
                        ctx.start.getLine(),
                        ctx.start.getCharPositionInLine()
                );
            }

            // Las operaciones de igualdad siempre retornan boolean
            tipoIzq = "boolean";
        }

        return tipoIzq;
    }

    /**
     * Maneja operaciones relacionales (<, >, <=, >=)
     * Valida que ambos operandos sean del mismo tipo y ordenables
     */
    @Override
    public String visitRelationalExpr(CompiscriptParser.RelationalExprContext ctx) {
        // Obtener el primer operando (siempre existe)
        String tipoIzq = visit(ctx.additiveExpr(0));

        // Si solo hay un operando, retornamos su tipo (no hay operación)
        if (ctx.additiveExpr().size() == 1) {
            return tipoIzq;
        }

        // Procesar todas las operaciones relacionales en cadena
        for (int i = 1; i < ctx.additiveExpr().size(); i++) {
            String tipoDer = visit(ctx.additiveExpr(i));
            String operador = ctx.getChild(2 * i - 1).getText(); // <, >, <=, >=

            // Validar que ambos operandos sean del mismo tipo y ordenables
            if (!sonTiposCompatiblesParaRelacional(tipoIzq, tipoDer)) {
                semanticVisitor.agregarError(
                        "Operación relacional '" + operador + "' no válida entre tipos: '" + tipoIzq + "' y '" + tipoDer + "'",
                        ctx.start.getLine(),
                        ctx.start.getCharPositionInLine()
                );
            } else if (!esTipoOrdenable(tipoIzq)) {
                semanticVisitor.agregarError(
                        "Operación relacional '" + operador + "' no soportada para tipo: '" + tipoIzq + "'",
                        ctx.start.getLine(),
                        ctx.start.getCharPositionInLine()
                );
            }

            // Las operaciones relacionales siempre retornan boolean
            tipoIzq = "boolean";
        }

        return tipoIzq;
    }

    /**
     * Verifica si dos tipos son compatibles para operaciones de igualdad (==, !=)
     * Los tipos null pueden compararse con cualquier tipo
     */
    private boolean sonTiposCompatiblesParaIgualdad(String tipo1, String tipo2) {
        // Ignorar errores previos
        if ("desconocido".equals(tipo1) || "desconocido".equals(tipo2)) {
            return true;
        }

        // null puede compararse con cualquier tipo
        if ("null".equals(tipo1) || "null".equals(tipo2)) {
            return true;
        }

        // Los tipos deben ser exactamente iguales
        return tipo1.equals(tipo2);
    }

    /**
     * Verifica si dos tipos son compatibles para operaciones relacionales (<, >, <=, >=)
     */
    private boolean sonTiposCompatiblesParaRelacional(String tipo1, String tipo2) {
        // Ignorar errores previos
        if ("desconocido".equals(tipo1) || "desconocido".equals(tipo2)) {
            return true;
        }

        // Para operaciones relacionales, los tipos deben ser exactamente iguales
        return tipo1.equals(tipo2);
    }

    /**
     * Verifica si un tipo soporta operaciones de orden (relacionales)
     */
    private boolean esTipoOrdenable(String tipo) {
        // Solo integer y string son ordenables por ahora
        return "integer".equals(tipo) || "string".equals(tipo);
    }

    // Delegación a VariableVisitor para operaciones aritméticas
    @Override
    public String visitAdditiveExpr(CompiscriptParser.AdditiveExprContext ctx) {
        return semanticVisitor.getVariableVisitor().visitAdditiveExpr(ctx);
    }

    @Override
    public String visitMultiplicativeExpr(CompiscriptParser.MultiplicativeExprContext ctx) {
        return semanticVisitor.getVariableVisitor().visitMultiplicativeExpr(ctx);
    }

    @Override
    public String visitUnaryExpr(CompiscriptParser.UnaryExprContext ctx) {
        return semanticVisitor.getVariableVisitor().visitUnaryExpr(ctx);
    }

    @Override
    public String visitPrimaryExpr(CompiscriptParser.PrimaryExprContext ctx) {
        return semanticVisitor.getVariableVisitor().visitPrimaryExpr(ctx);
    }

    @Override
    public String visitLiteralExpr(CompiscriptParser.LiteralExprContext ctx) {
        return semanticVisitor.getVariableVisitor().visitLiteralExpr(ctx);
    }

    @Override
    public String visitIdentifierExpr(CompiscriptParser.IdentifierExprContext ctx) {
        return semanticVisitor.getVariableVisitor().visitIdentifierExpr(ctx);
    }

    @Override
    public String visitLeftHandSide(CompiscriptParser.LeftHandSideContext ctx) {
        return semanticVisitor.getVariableVisitor().visitLeftHandSide(ctx);
    }
}