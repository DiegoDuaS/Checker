package com.fmd;

public class LogicalVisitor extends CompiscriptBaseVisitor<String> {
    private final SemanticVisitor semanticVisitor;

    public LogicalVisitor(SemanticVisitor semanticVisitor) {
        this.semanticVisitor = semanticVisitor;
    }

    /**
     * Maneja operaciones lógicas OR (||)
     * Valida que ambos operandos sean boolean
     */
    @Override
    public String visitLogicalOrExpr(CompiscriptParser.LogicalOrExprContext ctx) {
        // Obtener el primer operando (siempre existe)
        String tipoIzq = visit(ctx.logicalAndExpr(0));

        // Si solo hay un operando, retornamos su tipo (no hay operación)
        if (ctx.logicalAndExpr().size() == 1) {
            return tipoIzq;
        }

        // Procesar todas las operaciones OR en cadena
        for (int i = 1; i < ctx.logicalAndExpr().size(); i++) {
            String tipoDer = visit(ctx.logicalAndExpr(i));
            String operador = "||";

            // Validar que ambos operandos sean boolean
            if (!"boolean".equals(tipoIzq)) {
                semanticVisitor.agregarError(
                        "Operando izquierdo de '" + operador + "' debe ser boolean, encontrado: " + tipoIzq,
                        ctx.start.getLine(),
                        ctx.start.getCharPositionInLine()
                );
            }

            if (!"boolean".equals(tipoDer)) {
                semanticVisitor.agregarError(
                        "Operando derecho de '" + operador + "' debe ser boolean, encontrado: " + tipoDer,
                        ctx.start.getLine(),
                        ctx.start.getCharPositionInLine()
                );
            }

            // Si ambos son boolean, el resultado es boolean
            if ("boolean".equals(tipoIzq) && "boolean".equals(tipoDer)) {
                tipoIzq = "boolean";
            } else {
                tipoIzq = "desconocido"; // Tipo inválido por el error
            }
        }

        return tipoIzq;
    }

    /**
     * Maneja operaciones lógicas AND (&&)
     * Valida que ambos operandos sean boolean
     */
    @Override
    public String visitLogicalAndExpr(CompiscriptParser.LogicalAndExprContext ctx) {
        // Obtener el primer operando (siempre existe)
        String tipoIzq = visit(ctx.equalityExpr(0));

        // Si solo hay un operando, retornamos su tipo (no hay operación)
        if (ctx.equalityExpr().size() == 1) {
            return tipoIzq;
        }

        // Procesar todas las operaciones AND en cadena
        for (int i = 1; i < ctx.equalityExpr().size(); i++) {
            String tipoDer = visit(ctx.equalityExpr(i));
            String operador = "&&";

            // Validar que ambos operandos sean boolean
            if (!"boolean".equals(tipoIzq)) {
                semanticVisitor.agregarError(
                        "Operando izquierdo de '" + operador + "' debe ser boolean, encontrado: " + tipoIzq,
                        ctx.start.getLine(),
                        ctx.start.getCharPositionInLine()
                );
            }

            if (!"boolean".equals(tipoDer)) {
                semanticVisitor.agregarError(
                        "Operando derecho de '" + operador + "' debe ser boolean, encontrado: " + tipoDer,
                        ctx.start.getLine(),
                        ctx.start.getCharPositionInLine()
                );
            }

            // Si ambos son boolean, el resultado es boolean
            if ("boolean".equals(tipoIzq) && "boolean".equals(tipoDer)) {
                tipoIzq = "boolean";
            } else {
                tipoIzq = "desconocido"; // Tipo inválido por el error
            }
        }

        return tipoIzq;
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

            // Validar que ambos operandos sean del mismo tipo
            if (!tipoIzq.equals(tipoDer) && !"desconocido".equals(tipoIzq) && !"desconocido".equals(tipoDer)) {
                semanticVisitor.agregarError(
                        "No se pueden comparar tipos diferentes: '" + tipoIzq + "' " + operador + " '" + tipoDer + "'",
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
     * Valida que ambos operandos sean integer
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

            // Validar que ambos operandos sean integer
            if (!"integer".equals(tipoIzq)) {
                semanticVisitor.agregarError(
                        "Operando izquierdo de '" + operador + "' debe ser integer, encontrado: " + tipoIzq,
                        ctx.start.getLine(),
                        ctx.start.getCharPositionInLine()
                );
            }

            if (!"integer".equals(tipoDer)) {
                semanticVisitor.agregarError(
                        "Operando derecho de '" + operador + "' debe ser integer, encontrado: " + tipoDer,
                        ctx.start.getLine(),
                        ctx.start.getCharPositionInLine()
                );
            }

            // Las operaciones relacionales siempre retornan boolean
            tipoIzq = "boolean";
        }

        return tipoIzq;
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