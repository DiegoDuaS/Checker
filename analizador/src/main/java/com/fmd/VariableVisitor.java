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

        if (ctx.expression() != null) {
            String tipoExpresion = visit(ctx.expression());

            // Inferir tipo si no hay anotación
            if (tipo == null || "desconocido".equals(tipo)) {
                tipo = tipoExpresion;
            } else {
                // Validar compatibilidad de tipos si hay anotación explícita
                if (!tipo.equals(tipoExpresion) && !"desconocido".equals(tipoExpresion)) {
                    semanticVisitor.agregarError(
                            "No se puede inicializar constante '" + nombre + "' de tipo '" + tipo +
                                    "' con expresión de tipo '" + tipoExpresion + "'",
                            ctx.start.getLine(),
                            ctx.start.getCharPositionInLine());
                }
            }
        }

        if (tipo == null) {
            tipo = "desconocido";
        }

        Symbol sym = new Symbol(nombre, Symbol.Kind.CONSTANT, tipo, ctx, ctx.start.getLine(),
                ctx.start.getCharPositionInLine(), false);
        semanticVisitor.getEntornoActual().agregar(sym);
        return tipo;
    }

    @Override
    public String visitVariableDeclaration(CompiscriptParser.VariableDeclarationContext ctx) {
        String nombre = ctx.Identifier().getText();
        String tipo = ctx.typeAnnotation() != null ? ctx.typeAnnotation().type().getText() : null;

        // Verificar si estamos dentro de una clase
        Symbol currentClass = semanticVisitor.getCurrentClass();
        if (currentClass != null) {
            if (currentClass.getMembers().containsKey(nombre)) {
                semanticVisitor.agregarError(
                        "Miembro '" + nombre + "' ya declarado en la clase '" + currentClass.getName() + "'",
                        ctx.start.getLine(),
                        ctx.start.getCharPositionInLine());
                return tipo != null ? tipo : "desconocido";
            }
        } else {
            if (semanticVisitor.getEntornoActual().existeLocal(nombre)) {
                semanticVisitor.agregarError(
                        "Variable '" + nombre + "' ya declarada en este scope",
                        ctx.start.getLine(),
                        ctx.start.getCharPositionInLine());
                return tipo != null ? tipo : "desconocido";
            }
        }

        // Inferir tipo si hay inicializador
        if (ctx.initializer() != null && ctx.initializer().expression() != null) {
            String tipoInicializador = semanticVisitor.getExpressionType(ctx.initializer().expression());
            if (tipo == null) {
                tipo = tipoInicializador;
            } else if (!tipo.equals(tipoInicializador) && !"desconocido".equals(tipoInicializador)) {
                semanticVisitor.agregarError(
                        "No se puede inicializar variable '" + nombre + "' de tipo '" + tipo +
                                "' con expresión de tipo '" + tipoInicializador + "'",
                        ctx.start.getLine(),
                        ctx.start.getCharPositionInLine());
            }
        }

        if (tipo == null) tipo = "desconocido";

        Symbol sym = new Symbol(nombre, Symbol.Kind.VARIABLE, tipo, ctx, ctx.start.getLine(),
                ctx.start.getCharPositionInLine(), true);

        // Agregar al entorno o como miembro de clase
        if (currentClass != null) {
            currentClass.addMember(sym);
        } else {
            semanticVisitor.getEntornoActual().agregar(sym);
        }

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
            semanticVisitor.agregarError(
                    "Variable '" + nombre + "' no declarada en este scope",
                    ctx.start.getLine(),
                    ctx.start.getCharPositionInLine());
            return "ERROR";
        }

        if (sym.getEnclosingClassName() != null) {
            semanticVisitor.agregarError(
                    "No se puede acceder al miembro '" + nombre + "' sin un objeto de tipo '" + sym.getEnclosingClassName() + "'",
                    ctx.start.getLine(),
                    ctx.start.getCharPositionInLine());
            return "ERROR";
        }

        return sym.getType();
    }
    /**
     * Maneja operaciones aditivas: + y -
     * Valida que ambos operandos sean integer
     */
    @Override
    public String visitAdditiveExpr(CompiscriptParser.AdditiveExprContext ctx) {
        // Obtener el primer operando (siempre existe)
        String tipoIzq = visit(ctx.multiplicativeExpr(0));

        // Si solo hay un operando, retornamos su tipo (no hay operación)
        if (ctx.multiplicativeExpr().size() == 1) {
            return tipoIzq;
        }

        // Procesar todas las operaciones de suma/resta en cadena
        for (int i = 1; i < ctx.multiplicativeExpr().size(); i++) {
            String tipoDer = visit(ctx.multiplicativeExpr(i));
            String operador = ctx.getChild(2 * i - 1).getText(); // +, -

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

            // Si ambos son integer, el resultado es integer
            if ("integer".equals(tipoIzq) && "integer".equals(tipoDer)) {
                tipoIzq = "integer";
            } else {
                tipoIzq = "desconocido"; // Tipo inválido por el error
            }
        }

        return tipoIzq;
    }

    /**
     * Maneja operaciones multiplicativas: *, / y %
     * Valida que ambos operandos sean integer
     */
    @Override
    public String visitMultiplicativeExpr(CompiscriptParser.MultiplicativeExprContext ctx) {
        // Obtener el primer operando (siempre existe)
        String tipoIzq = visit(ctx.unaryExpr(0));

        // Si solo hay un operando, retornamos su tipo (no hay operación)
        if (ctx.unaryExpr().size() == 1) {
            return tipoIzq;
        }

        // Procesar todas las operaciones de multiplicación/división en cadena
        for (int i = 1; i < ctx.unaryExpr().size(); i++) {
            String tipoDer = visit(ctx.unaryExpr(i));
            String operador = ctx.getChild(2 * i - 1).getText(); // *, /, %

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

            // Validación especial para división por cero (opcional)
            if ("/".equals(operador) && "0".equals(ctx.unaryExpr(i).getText())) {
                semanticVisitor.agregarError(
                        "División por cero detectada",
                        ctx.start.getLine(),
                        ctx.start.getCharPositionInLine()
                );
            }

            // Si ambos son integer, el resultado es integer
            if ("integer".equals(tipoIzq) && "integer".equals(tipoDer)) {
                tipoIzq = "integer";
            } else {
                tipoIzq = "desconocido"; // Tipo inválido por el error
            }
        }

        return tipoIzq;
    }

    /**
     * Maneja expresiones unarias: -expr y !expr
     */
    @Override
    public String visitUnaryExpr(CompiscriptParser.UnaryExprContext ctx) {
        // Si no hay operador unario, es solo primaryExpr
        if (ctx.getChildCount() == 1) {
            return visit(ctx.primaryExpr());
        }

        // Hay operador unario (- o !)
        String operador = ctx.getChild(0).getText(); // - o !
        String tipoOperando = visit(ctx.unaryExpr());

        if ("-".equals(operador)) {
            // Operador negación numérica: debe ser integer
            if (!"integer".equals(tipoOperando)) {
                semanticVisitor.agregarError(
                        "Operador '-' unario requiere operando integer, encontrado: " + tipoOperando,
                        ctx.start.getLine(),
                        ctx.start.getCharPositionInLine()
                );
            }
            return "integer";
        } else if ("!".equals(operador)) {
            // Operador negación lógica: debe ser boolean
            if (!"boolean".equals(tipoOperando)) {
                semanticVisitor.agregarError(
                        "Operador '!' requiere operando boolean, encontrado: " + tipoOperando,
                        ctx.start.getLine(),
                        ctx.start.getCharPositionInLine()
                );
            }
            return "boolean";
        }

        return tipoOperando;
    }

    @Override
    public String visitLiteralExpr(CompiscriptParser.LiteralExprContext ctx) {
        if (ctx.Literal() != null) {
            String lit = ctx.Literal().getText();
            if (lit.matches("[0-9]+")) {
                return "integer";
            }
            if (lit.startsWith("\"") && lit.endsWith("\"")) {
                return "string";
            }
        }
        String texto = ctx.getText();
        if ("true".equals(texto) || "false".equals(texto)) {
            return "boolean";
        }
        if ("null".equals(texto)) {
            return "null";
        }
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

    // Maneja la creación de nuevas instancias: new Dog("Rex")
    @Override
    public String visitNewExpr(CompiscriptParser.NewExprContext ctx) {
        String className = ctx.Identifier().getText();
        Symbol classSym = semanticVisitor.getEntornoActual().obtener(className);
        if (classSym == null || classSym.getKind() != Symbol.Kind.CLASS) {
            semanticVisitor.agregarError(
                    "Clase '" + className + "' no existe",
                    ctx.start.getLine(),
                    ctx.start.getCharPositionInLine()
            );
            return "desconocido";
        }
        return className;
    }

    // Maneja llamadas a métodos y acceso a propiedades
    @Override
    public String visitPropertyAccessExpr(CompiscriptParser.PropertyAccessExprContext ctx) {
        // Evaluamos la izquierda: el objeto
        String leftTipo = (String) visit(ctx.getParent()); // tipo del objeto
        Symbol classSym = semanticVisitor.getEntornoActual().obtener(leftTipo);

        if (classSym == null || classSym.getKind() != Symbol.Kind.CLASS) {
            semanticVisitor.agregarError(
                    "Tipo '" + leftTipo + "' no es una clase válida",
                    ctx.start.getLine(),
                    ctx.start.getCharPositionInLine()
            );
            return null;
        }

        String propName = ctx.Identifier().getText();
        Symbol propSym = null;

        // Buscar la propiedad/método en la clase
        for (Symbol s : semanticVisitor.getEntornoActual().getAllSymbols().values()) {
            if (s.getEnclosingClassName() != null && s.getEnclosingClassName().equals(classSym.getName())
                    && s.getName().equals(propName)) {
                propSym = s;
                break;
            }
        }

        if (propSym == null) {
            semanticVisitor.agregarError("'" + ctx.Identifier().getText() + "' no existe",
                    ctx.start.getLine(), ctx.start.getCharPositionInLine());
            return "desconocido";
        }

        semanticVisitor.setLastSymbol(propSym);

        return propSym.getType();
    }

    // Métodos para manejar la jerarquía de expresiones
    @Override
    public String visitExprNoAssign(CompiscriptParser.ExprNoAssignContext ctx) {
        return visit(ctx.conditionalExpr());
    }


    @Override
    public String visitTernaryExpr(CompiscriptParser.TernaryExprContext ctx) {
        return visit(ctx.logicalOrExpr()); // Procesar la expresión principal
    }
    @Override
    public String visitLogicalOrExpr(CompiscriptParser.LogicalOrExprContext ctx) {
        return visit(ctx.logicalAndExpr(0));
    }

    @Override
    public String visitLogicalAndExpr(CompiscriptParser.LogicalAndExprContext ctx) {
        return visit(ctx.equalityExpr(0));
    }

    @Override
    public String visitEqualityExpr(CompiscriptParser.EqualityExprContext ctx) {
        return visit(ctx.relationalExpr(0));
    }

    @Override
    public String visitRelationalExpr(CompiscriptParser.RelationalExprContext ctx) {
        return visit(ctx.additiveExpr(0));
    }

    @Override
    public String visitPrimaryExpr(CompiscriptParser.PrimaryExprContext ctx) {
        if (ctx.literalExpr() != null) {
            return visit(ctx.literalExpr());
        }
        if (ctx.leftHandSide() != null) {
            return visit(ctx.leftHandSide());
        }
        if (ctx.expression() != null) {
            return visit(ctx.expression());
        }
        return "desconocido";
    }


}
