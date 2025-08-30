package com.fmd;

import com.fmd.modules.Symbol;

public class VariableVisitor extends CompiscriptBaseVisitor<String> {
    private final SemanticVisitor semanticVisitor;

    public VariableVisitor(SemanticVisitor semanticVisitor) {
        this.semanticVisitor = semanticVisitor;
    }

    @Override
    public String visitConstantDeclaration(CompiscriptParser.ConstantDeclarationContext ctx) {
        String nombre = ctx.Identifier().getText();
        String tipo = ctx.typeAnnotation() != null ? ctx.typeAnnotation().type().getText() : "desconocido";

        if (ctx.expression() == null) {
            semanticVisitor.agregarError(
                    "La constante '" + nombre + "' debe inicializarse",
                    ctx.start.getLine(),
                    ctx.start.getCharPositionInLine());
        }

        if (semanticVisitor.getEntornoActual().existeLocal(nombre)) {
            semanticVisitor.agregarError(
                    "Constante '" + nombre + "' ya declarada en este scope",
                    ctx.start.getLine(),
                    ctx.start.getCharPositionInLine());
            return tipo;
        }

        if (tipo == null && ctx.expression() != null) {
            tipo = visit(ctx.expression());
        }
        if (tipo == null)
            tipo = "desconocido";

        Symbol sym = new Symbol(nombre, Symbol.Kind.CONSTANT, tipo, ctx, ctx.start.getLine(),
                ctx.start.getCharPositionInLine(), false);
        semanticVisitor.getEntornoActual().agregar(sym);
        return tipo;
    }

    @Override
    public String visitVariableDeclaration(CompiscriptParser.VariableDeclarationContext ctx) {
        String nombre = ctx.Identifier().getText();
        String tipo = ctx.typeAnnotation() != null ? ctx.typeAnnotation().type().getText() : null;

        if (semanticVisitor.getEntornoActual().existeLocal(nombre)) {
            semanticVisitor.agregarError(
                    "Variable '" + nombre + "' ya declarada en este scope",
                    ctx.start.getLine(),
                    ctx.start.getCharPositionInLine());
            return tipo;
        }

        // inferir tipo desde initializer si no hay anotación
        if (tipo == null && ctx.initializer() != null) {
            tipo = visit(ctx.initializer().expression());
        }

        if (tipo == null)
            tipo = "desconocido";

        Symbol sym = new Symbol(nombre, Symbol.Kind.VARIABLE, tipo, ctx, ctx.start.getLine(),
                ctx.start.getCharPositionInLine(), true);
        semanticVisitor.getEntornoActual().agregar(sym);
        return tipo;
    }

    @Override
    public String visitAssignment(CompiscriptParser.AssignmentContext ctx) {
        String nombreVar = ctx.Identifier().getText();
        Symbol sym = semanticVisitor.getEntornoActual().obtener(nombreVar);

        if (sym == null) {
            semanticVisitor.agregarError("Variable '" + nombreVar + "' no declarada",
                    ctx.start.getLine(), ctx.start.getCharPositionInLine());
            return "desconocido";
        }
        if (!sym.isMutable()) {
            semanticVisitor.agregarError("No se puede asignar a la constante '" + nombreVar + "'",
                    ctx.start.getLine(), ctx.start.getCharPositionInLine());
            return sym.getType();
        }

        String tipoExpr = visit(ctx.expression(0));
        // Chequeo simple de igualdad de cadenas de tipo; aquí podrías añadir coerciones
        if (!sym.getType().equals(tipoExpr) && !"desconocido".equals(tipoExpr)) {
            semanticVisitor.agregarError(
                    "No se puede asignar valor de tipo '" + tipoExpr + "' a variable '" + nombreVar + "' de tipo '"
                            + sym.getType() + "'",
                    ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }
        return tipoExpr;
    }

    @Override
    public String visitIdentifierExpr(CompiscriptParser.IdentifierExprContext ctx) {
        String nombre = ctx.Identifier().getText();
        Symbol sym = semanticVisitor.getEntornoActual().obtener(nombre);
        if (sym == null) {
            semanticVisitor.agregarError("Variable '" + nombre + "' no declarada",
                    ctx.start.getLine(), ctx.start.getCharPositionInLine());
            return "desconocido";
        }
        return sym.getType();
    }

    @Override
    public String visitLiteralExpr(CompiscriptParser.LiteralExprContext ctx) {
        if (ctx.Literal() != null) {
            String lit = ctx.Literal().getText();
            if (lit.matches("[0-9]+"))
                return "integer";
            if (lit.startsWith("\""))
                return "string";
        }
        String texto = ctx.getText();
        if (texto.equals("true") || texto.equals("false"))
            return "boolean";
        if (texto.equals("null"))
            return "null";
        return "desconocido";
    }

    @Override
    public String visitLeftHandSide(CompiscriptParser.LeftHandSideContext ctx) {
        // Si tiene sufixOp que es CallExpr
        for (CompiscriptParser.SuffixOpContext suffixOp : ctx.suffixOp()) {
            if (suffixOp instanceof CompiscriptParser.CallExprContext) {
                return semanticVisitor.getFunctionsVisitor().visitCallExpr((CompiscriptParser.CallExprContext) suffixOp);
            }
        }
        return visitChildren(ctx);
    }
}
