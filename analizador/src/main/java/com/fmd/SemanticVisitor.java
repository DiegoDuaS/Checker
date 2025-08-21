package com.fmd;

import java.util.HashMap;
import java.util.Map;

public class SemanticVisitor extends CompiscriptBaseVisitor<Void> {
    private VisitorVariables visitorVariables = new VisitorVariables();
    private VisitorFunciones visitorFunciones = new VisitorFunciones();

    @Override
    public Void visitPrograma(CompiscriptParser.ProgramaContext ctx) {
        visitorVariables.visit(ctx);
        visitorFunciones.visit(ctx);
        return null;
    }

}
