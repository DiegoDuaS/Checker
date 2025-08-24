package com.fmd;

import java.util.HashMap;
import java.util.Map;

public class SemanticVisitor extends CompiscriptBaseVisitor<Void> {
    private VariableVisitor visitorVariables = new VariableVisitor();
    // private FunctionVisitor visitorFunciones = new FunctionVisitor();

    @Override
    public Void visitProgram(CompiscriptParser.ProgramContext ctx) {
        visitorVariables.visit(ctx);
        // visitorFunciones.visit(ctx);
        return null;
    }

}
