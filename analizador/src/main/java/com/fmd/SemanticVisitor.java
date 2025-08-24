package com.fmd;

import java.util.Map;

public class SemanticVisitor extends CompiscriptBaseVisitor<Void> {

    private final VariableVisitor variableVisitor = new VariableVisitor();

    @Override
    public Void visitProgram(CompiscriptParser.ProgramContext ctx) {
        variableVisitor.entrarScope();
        for (CompiscriptParser.StatementContext stmt : ctx.statement()) {
            visit(stmt);
        }
        variableVisitor.salirScope();
        return null;
    }

    @Override
    public Void visitBlock(CompiscriptParser.BlockContext ctx) {
        variableVisitor.entrarScope();
        for (CompiscriptParser.StatementContext stmt : ctx.statement()) {
            visit(stmt);
        }
        variableVisitor.salirScope();
        return null;
    }

    @Override
    public Void visitIfStatement(CompiscriptParser.IfStatementContext ctx) {
        if (ctx.expression() != null) {
            String tipoCond = variableVisitor.visit(ctx.expression());
            if (!tipoCond.equals("boolean")) {
                System.err.println("Error semántico: condición del if debe ser boolean, encontrada: " + tipoCond);
            }
        }

        visit(ctx.block(0));

        if (ctx.block().size() > 1) {
            visit(ctx.block(1));
        }

        return null;
    }

    @Override
    public Void visitWhileStatement(CompiscriptParser.WhileStatementContext ctx) {
        if (ctx.expression() != null) {
            String tipoCond = variableVisitor.visit(ctx.expression());
            if (!tipoCond.equals("boolean")) {
                System.err.println("Error semántico: condición del while debe ser boolean, encontrada: " + tipoCond);
            }
        }

        visit(ctx.block());
        return null;
    }


    @Override
    public Void visitVariableDeclaration(CompiscriptParser.VariableDeclarationContext ctx) {
        variableVisitor.visitVariableDeclaration(ctx);
        return null;
    }

    @Override
    public Void visitConstantDeclaration(CompiscriptParser.ConstantDeclarationContext ctx) {
        variableVisitor.visitConstantDeclaration(ctx);
        return null;
    }

    @Override
    public Void visitAssignment(CompiscriptParser.AssignmentContext ctx) {
        variableVisitor.visitAssignment(ctx);
        return null;
    }

    public Map<String, String> getTablaVariables() {
        return variableVisitor.getTablaVariables();
    }
}
