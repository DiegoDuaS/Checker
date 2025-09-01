package com.fmd;
import com.fmd.modules.SemanticError;

public class LogicalVisitor extends CompiscriptBaseVisitor<String> {
    private final SemanticVisitor semanticVisitor;

    public LogicalVisitor(SemanticVisitor semanticVisitor) {
        this.semanticVisitor = semanticVisitor;
    }

// Solo mostrando los métodos que cambian para operaciones lógicas

    /**
     * Maneja operaciones lógicas OR (||)
     * Valida que ambos operandos sean boolean
     */
    @Override
    public String visitLogicalOrExpr(CompiscriptParser.LogicalOrExprContext ctx) {
        String tipoIzq = visit(ctx.logicalAndExpr(0));

        if (ctx.logicalAndExpr().size() == 1) {
            return tipoIzq;
        }

        for (int i = 1; i < ctx.logicalAndExpr().size(); i++) {
            String tipoDer = visit(ctx.logicalAndExpr(i));

            // Validar que ambos operandos sean boolean usando mensajes centralizados
            if (!"boolean".equals(tipoIzq)) {
                semanticVisitor.agregarError(
                        SemanticError.getLogicalErrorMessage("||", tipoIzq, true),
                        ctx.start.getLine(),
                        ctx.start.getCharPositionInLine()
                );
            }

            if (!"boolean".equals(tipoDer)) {
                semanticVisitor.agregarError(
                        SemanticError.getLogicalErrorMessage("||", tipoDer, false),
                        ctx.start.getLine(),
                        ctx.start.getCharPositionInLine()
                );
            }

            // Si ambos son boolean, el resultado es boolean
            if ("boolean".equals(tipoIzq) && "boolean".equals(tipoDer)) {
                tipoIzq = "boolean";
            } else {
                tipoIzq = "desconocido";
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
        String tipoIzq = visit(ctx.equalityExpr(0));

        if (ctx.equalityExpr().size() == 1) {
            return tipoIzq;
        }

        for (int i = 1; i < ctx.equalityExpr().size(); i++) {
            String tipoDer = visit(ctx.equalityExpr(i));

            // Validar que ambos operandos sean boolean usando mensajes centralizados
            if (!"boolean".equals(tipoIzq)) {
                semanticVisitor.agregarError(
                        SemanticError.getLogicalErrorMessage("&&", tipoIzq, true),
                        ctx.start.getLine(),
                        ctx.start.getCharPositionInLine()
                );
            }

            if (!"boolean".equals(tipoDer)) {
                semanticVisitor.agregarError(
                        SemanticError.getLogicalErrorMessage("&&", tipoDer, false),
                        ctx.start.getLine(),
                        ctx.start.getCharPositionInLine()
                );
            }

            // Si ambos son boolean, el resultado es boolean
            if ("boolean".equals(tipoIzq) && "boolean".equals(tipoDer)) {
                tipoIzq = "boolean";
            } else {
                tipoIzq = "desconocido";
            }
        }

        return tipoIzq;
    }

    // Delegación a ComparisonVisitor para operaciones de comparación
    @Override
    public String visitEqualityExpr(CompiscriptParser.EqualityExprContext ctx) {
        return semanticVisitor.getComparisonVisitor().visitEqualityExpr(ctx);
    }

    @Override
    public String visitRelationalExpr(CompiscriptParser.RelationalExprContext ctx) {
        return semanticVisitor.getComparisonVisitor().visitRelationalExpr(ctx);
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