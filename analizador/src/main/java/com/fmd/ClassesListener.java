package com.fmd;

import com.fmd.modules.Symbol;

public class ClassesListener extends CompiscriptBaseListener {

    private final SemanticVisitor semanticVisitor;
    private Symbol currentClass; // clase actual

    public ClassesListener(SemanticVisitor visitor) {
        this.semanticVisitor = visitor;
    }

    @Override
    public void enterClassDeclaration(CompiscriptParser.ClassDeclarationContext ctx) {
        String className = ctx.Identifier(0).getText();
        String superName = ctx.Identifier().size() > 1 ? ctx.Identifier(1).getText() : null;

        Symbol clase = new Symbol(
                className,
                Symbol.Kind.CLASS,
                null,
                ctx,
                ctx.start.getLine(),
                ctx.start.getCharPositionInLine(),
                true
        );

        if (superName != null) {
            clase.setSuperClass(superName);
        }

        semanticVisitor.getEntornoActual().agregar(clase);
        currentClass = clase;

        System.out.println("*******SE ENTRO A UNA CLASE*************");
    }

    @Override
    public void exitClassDeclaration(CompiscriptParser.ClassDeclarationContext ctx) {
        currentClass = null;
        System.out.println("*******SE SALIO DE UNA CLASE*************");
    }

    @Override
    public void enterVariableDeclaration(CompiscriptParser.VariableDeclarationContext ctx) {
        String varName = ctx.Identifier().getText();

        // Revisar si tiene typeAnnotation
        String type = null;
        if (ctx.typeAnnotation() != null) {
            type = ctx.typeAnnotation().type().getText();
        }

        // Verificar si ya existe en la clase actual
        if (currentClass != null && currentClass.getMembers().containsKey(varName)) {
            semanticVisitor.agregarError(
                    "Variable '" + varName + "' ya declarada en la clase '" + currentClass.getName() + "'",
                    ctx.start.getLine(),
                    ctx.start.getCharPositionInLine()
            );
            return; // Salir sin agregar
        }

        // Verificar si ya existe en el entorno local (scope actual)
        if (semanticVisitor.getEntornoActual().existeLocal(varName)) {
            semanticVisitor.agregarError(
                    "Variable '" + varName + "' ya declarada en este scope",
                    ctx.start.getLine(),
                    ctx.start.getCharPositionInLine()
            );
            return; // Salir sin agregar
        }

        Symbol varSym = new Symbol(
                varName,
                Symbol.Kind.VARIABLE,
                type,
                ctx,
                ctx.start.getLine(),
                ctx.start.getCharPositionInLine(),
                true
        );

        // Si hay una clase actual, asociar la variable a sus miembros
        if (currentClass != null) {
            currentClass.getMembers().put(varName, varSym);
            varSym.setEnclosingClassName(currentClass.getName());
        }

        // Agregar al entorno
        semanticVisitor.getEntornoActual().agregar(varSym);
    }



    @Override
    public void enterFunctionDeclaration(CompiscriptParser.FunctionDeclarationContext ctx) {
        String funcName = ctx.Identifier().getText();

        // Obtener tipo de retorno si existe
        String returnType = ctx.type() != null ? ctx.type().getText() : "desconocido";

        // Crear símbolo de la función
        Symbol funcSym = new Symbol(
                funcName,
                Symbol.Kind.FUNCTION,
                returnType,
                ctx,
                ctx.start.getLine(),
                ctx.start.getCharPositionInLine(),
                true
        );

        // Asociar la función a la clase actual si existe
        if (currentClass != null) {
            funcSym.setEnclosingClassName(currentClass.getName());
            // Agregar la función a la tabla de miembros de la clase
            currentClass.getMembers().put(funcName, funcSym);
        }

        // Registrar parámetros de la función
        if (ctx.parameters() != null) {
            for (CompiscriptParser.ParameterContext paramCtx : ctx.parameters().parameter()) {
                String paramName = paramCtx.Identifier().getText();
                String paramType = paramCtx.type() != null ? paramCtx.type().getText() : "desconocido";
                Symbol paramSym = new Symbol(paramName, Symbol.Kind.VARIABLE, paramType, paramCtx,
                        paramCtx.start.getLine(), paramCtx.start.getCharPositionInLine(), true);
                funcSym.addParameter(paramSym);
            }
        }

        // Registrar función en el entorno
        semanticVisitor.getEntornoActual().agregar(funcSym);
        System.out.println("Función agregada al entorno global: " + funcSym.getName());
    }

    @Override
    public void enterConstantDeclaration(CompiscriptParser.ConstantDeclarationContext ctx) {
        String constName = ctx.Identifier().getText();
        String type = ctx.typeAnnotation() != null ? ctx.typeAnnotation().type().getText() : null;

        Symbol constSym = new Symbol(
                constName,
                Symbol.Kind.VARIABLE, // o CONSTANT si tienes un Kind para constantes
                type,
                ctx,
                ctx.start.getLine(),
                ctx.start.getCharPositionInLine(),
                true
        );

        semanticVisitor.getEntornoActual().agregar(constSym);
        System.out.println("Constante agregada: " + constSym.getName() + ", tipo: " + constSym.getType());
    }





}
