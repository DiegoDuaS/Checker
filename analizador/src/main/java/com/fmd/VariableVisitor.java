package com.fmd;
import com.fmd.modules.SemanticError;
import com.fmd.modules.Symbol;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import java.util.List;
import java.util.ArrayList;

import com.fmd.CompiscriptParser;
import com.fmd.CompiscriptBaseVisitor;


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
            String tipoExpresion = semanticVisitor.getLogicalVisitor().visit(ctx.expression());

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

        Symbol currentClass = semanticVisitor.getCurrentClass();

        // -------------------
        // Verificar duplicados
        // -------------------
        if (currentClass != null) {
            if (currentClass.getMembers().containsKey(nombre)) {
                semanticVisitor.agregarError(
                        "Miembro '" + nombre + "' ya declarado en la clase '" + currentClass.getName() + "'",
                        ctx.start.getLine(), ctx.start.getCharPositionInLine());
                return tipo != null ? tipo : "desconocido";
            }
        } else {
            if (semanticVisitor.getEntornoActual().existeLocal(nombre)) {
                semanticVisitor.agregarError(
                        "Variable '" + nombre + "' ya declarada en este scope",
                        ctx.start.getLine(), ctx.start.getCharPositionInLine());
                return tipo != null ? tipo : "desconocido";
            }
        }

        // -------------------
        // Inferir tipo desde inicializador
        // -------------------
        if (ctx.initializer() != null && ctx.initializer().expression() != null) {
            CompiscriptParser.ExpressionContext exprCtx = ctx.initializer().expression();
            String tipoInicializador = semanticVisitor.getLogicalVisitor().visit(ctx.initializer().expression());
            // Recorrer el árbol recursivamente para detectar 'new Clase(...)'
            detectNewExpr(exprCtx);
            if (tipo == null) {
                tipo = tipoInicializador;
            } else if (!tipo.equals(tipoInicializador) && !"desconocido".equals(tipoInicializador)) {
                semanticVisitor.agregarError(
                        "No se puede inicializar variable '" + nombre + "' de tipo '" + tipo +
                                "' con expresión de tipo '" + tipoInicializador + "'",
                        ctx.start.getLine(), ctx.start.getCharPositionInLine());
            }
        }

        if (tipo == null) tipo = "desconocido";

        Symbol sym = new Symbol(nombre, Symbol.Kind.VARIABLE, tipo, ctx,
                ctx.start.getLine(), ctx.start.getCharPositionInLine(), true);

        // Agregar al entorno o como miembro de clase
        if (currentClass != null) {
            currentClass.addMember(sym);
        } else {
            semanticVisitor.getEntornoActual().agregar(sym);
        }

        return tipo;
    }

    // -------------------
    // Verifica 'new Clase(...)' y tipos de parámetros usando getParams()
    // -------------------
    private void detectNewExpr(ParseTree node) {
        if (node instanceof CompiscriptParser.NewExprContext newCtx) {
            String claseNueva = newCtx.Identifier().getText();
            Symbol claseSym = semanticVisitor.getEntornoActual().obtener(claseNueva);

            if (claseSym == null || claseSym.getKind() != Symbol.Kind.CLASS) {
                semanticVisitor.agregarError(
                        "Clase '" + claseNueva + "' no existe",
                        newCtx.start.getLine(), newCtx.start.getCharPositionInLine()
                );
            } else {
                // Buscar constructor
                Symbol constructorSym = buscarConstructor(claseSym);
                int actualArgs = newCtx.arguments() != null ? newCtx.arguments().expression().size() : 0;

                if (constructorSym == null) {
                    if (actualArgs > 0) {
                        semanticVisitor.agregarError(
                                "Clase '" + claseNueva + "' no tiene constructor definido, no puede recibir argumentos",
                                newCtx.start.getLine(), newCtx.start.getCharPositionInLine()
                        );
                    }
                } else {
                    int expectedArgs = constructorSym.getParameterCount();

                    if (expectedArgs != actualArgs) {
                        semanticVisitor.agregarError(
                                "Constructor de '" + claseNueva + "' espera " + expectedArgs +
                                        " argumentos, pero recibe " + actualArgs,
                                newCtx.start.getLine(), newCtx.start.getCharPositionInLine()
                        );
                    } else {
                        // Validar tipos de argumentos usando getParams()
                        List<Symbol> params = constructorSym.getParams();
                        for (int i = 0; i < expectedArgs; i++) {
                            Symbol paramSym = params.get(i);
                            CompiscriptParser.ExpressionContext argExpr = newCtx.arguments().expression(i);
                            String tipoArg = semanticVisitor.getExpressionType(argExpr);

                            if (!tipoArg.equals(paramSym.getType()) && !"desconocido".equals(tipoArg)) {
                                semanticVisitor.agregarError(
                                        "Tipo del argumento " + (i + 1) + " de '" + claseNueva +
                                                "' esperado: '" + paramSym.getType() +
                                                "', recibido: '" + tipoArg + "'",
                                        argExpr.start.getLine(),
                                        argExpr.start.getCharPositionInLine()
                                );
                            }
                        }
                    }
                }
            }
        }

        // Recorrer hijos
        for (int i = 0; i < node.getChildCount(); i++) {
            detectNewExpr(node.getChild(i));
        }
    }

    /**
     * Busca un constructor en la clase o en su cadena de herencia.
     */
    private Symbol buscarConstructor(Symbol claseSym) {
        Symbol constructorSym = null;
        while (claseSym != null) {
            for (Symbol miembro : claseSym.getMembers().values()) {
                if (miembro.isConstructor()) {
                    constructorSym = miembro;
                    return constructorSym;
                }
            }

            // Subir a la superclase
            String superClassName = claseSym.getSuperClass();
            if (superClassName != null) {
                claseSym = semanticVisitor.getEntornoActual().obtener(superClassName);
            } else {
                claseSym = null;
            }
        }

        return constructorSym;
    }


    @Override
    public String visitAssignment(CompiscriptParser.AssignmentContext ctx) {
        Symbol sym;
        String nombreVar;
        if (ctx.getChild(0).getText().equals("this")) {
            // caso: this.name = ...
            nombreVar = ctx.getChild(0).getText() + "." + ctx.getChild(2).getText();
        } else {
            // variable normal
            nombreVar = ctx.Identifier().getText(); // o ctx.getChild(0).getText()
        }
        // Revisar si se está usando 'this'
        if (nombreVar.startsWith("this.")) {
            Symbol currentClass = semanticVisitor.getCurrentClass();
            if (currentClass == null) {
                semanticVisitor.agregarError(
                        "Uso de 'this' fuera de una clase",
                        ctx.start.getLine(), ctx.start.getCharPositionInLine()
                );
                return "ERROR";
            }
            String memberName = nombreVar.substring(5);
            sym = currentClass.getMembers().get(memberName);
            if (sym == null) {
                semanticVisitor.agregarError(
                        "Miembro '" + memberName + "' no existe en la clase '" + currentClass.getName() + "'",
                        ctx.start.getLine(), ctx.start.getCharPositionInLine()
                );
                return "ERROR";
            }
        } else {
            sym = semanticVisitor.getEntornoActual().obtener(nombreVar);
            if (sym == null) {
                semanticVisitor.agregarError(
                        "Variable '" + nombreVar + "' no declarada",
                        ctx.start.getLine(), ctx.start.getCharPositionInLine()
                );
                return "ERROR";
            }
        }

        // Revisar mutabilidad
        if (!sym.isMutable()) {
            semanticVisitor.agregarError(
                    "No se puede asignar a la constante '" + nombreVar + "'",
                    ctx.start.getLine(), ctx.start.getCharPositionInLine()
            );
        }

        // Obtener tipo de la expresión
         String tipoExpr = semanticVisitor.getLogicalVisitor().visit(ctx.expression(0));

        // Chequeo de tipos
        if (!sym.getType().equals(tipoExpr) && !"desconocido".equals(tipoExpr) && !"null".equals(tipoExpr)) {

            semanticVisitor.agregarError(
                    "No se puede asignar valor de tipo '" + tipoExpr + "' a variable '" + nombreVar + "' de tipo '" + sym.getType() + "'",
                    ctx.start.getLine(), ctx.start.getCharPositionInLine()
            );
        }

        // Marcar miembro como inicializado
        if (nombreVar.startsWith("this.")) {
            sym.setInitialized(true);
        }

        return sym.getType();
    }

    @Override
    public String visitIdentifierExpr(CompiscriptParser.IdentifierExprContext ctx) {
        if (ctx.Identifier() == null) {
            // Por ejemplo, puede ser un literal, u otra expresión
            return visitChildren(ctx); // o manejarlo según corresponda
        }

        String nombre = ctx.Identifier().getText();
        Symbol sym = semanticVisitor.getEntornoActual().obtener(nombre);

        if (sym == null) {
            semanticVisitor.agregarError(
                    "Variable '" + nombre + "' no declarada en este scope",
                    ctx.start.getLine(),
                    ctx.start.getCharPositionInLine()
            );
            return "ERROR";
        }

        if (sym.getEnclosingClassName() != null) {
            semanticVisitor.agregarError(
                    "No se puede acceder al miembro '" + nombre + "' sin un objeto de tipo '"
                            + sym.getEnclosingClassName() + "'",
                    ctx.start.getLine(),
                    ctx.start.getCharPositionInLine()
            );
            return "ERROR";
        }

        return sym.getType();
    }

// Solo mostrando los métodos que cambian para operaciones aritméticas

    /**
     * Maneja operaciones aditivas: + y -
     * Valida que ambos operandos sean integer
     */
    @Override
    public String visitAdditiveExpr(CompiscriptParser.AdditiveExprContext ctx) {
        String tipoIzq = visit(ctx.multiplicativeExpr(0));

        if (ctx.multiplicativeExpr().size() == 1) {
            return tipoIzq;
        }

        for (int i = 1; i < ctx.multiplicativeExpr().size(); i++) {
            String tipoDer = visit(ctx.multiplicativeExpr(i));
            String operador = ctx.getChild(2 * i - 1).getText(); // +, -

            // Validar que ambos operandos sean integer usando mensaje centralizado
            if (!"integer".equals(tipoIzq) || !"integer".equals(tipoDer)) {
                semanticVisitor.agregarError(
                        SemanticError.getArithmeticErrorMessage(operador, tipoIzq, tipoDer),
                        ctx.start.getLine(),
                        ctx.start.getCharPositionInLine()
                );
                tipoIzq = "desconocido";
            } else {
                tipoIzq = "integer";
            }
        }

        return tipoIzq;
    }

    /**
     * Maneja operaciones multiplicativas: *, / y %
     */
    @Override
    public String visitMultiplicativeExpr(CompiscriptParser.MultiplicativeExprContext ctx) {
        String tipoIzq = visit(ctx.unaryExpr(0));

        if (ctx.unaryExpr().size() == 1) {
            return tipoIzq;
        }

        for (int i = 1; i < ctx.unaryExpr().size(); i++) {
            String tipoDer = visit(ctx.unaryExpr(i));
            String operador = ctx.getChild(2 * i - 1).getText(); // *, /, %

            // Validación especial para división por cero usando mensaje centralizado
            if ("/".equals(operador) && "0".equals(ctx.unaryExpr(i).getText())) {
                semanticVisitor.agregarError(
                        SemanticError.getDivisionByZeroMessage(),
                        ctx.start.getLine(),
                        ctx.start.getCharPositionInLine()
                );
            }

            // Validar que ambos operandos sean integer usando mensaje centralizado
            if (!"integer".equals(tipoIzq) || !"integer".equals(tipoDer)) {
                semanticVisitor.agregarError(
                        SemanticError.getArithmeticErrorMessage(operador, tipoIzq, tipoDer),
                        ctx.start.getLine(),
                        ctx.start.getCharPositionInLine()
                );
                tipoIzq = "desconocido";
            } else {
                tipoIzq = "integer";
            }
        }

        return tipoIzq;
    }

    /**
     * Maneja expresiones unarias: -expr y !expr
     */
    @Override
    public String visitUnaryExpr(CompiscriptParser.UnaryExprContext ctx) {
        if (ctx.getChildCount() == 1) {
            return visit(ctx.primaryExpr());
        }

        String operador = ctx.getChild(0).getText(); // - o !
        String tipoOperando = visit(ctx.unaryExpr());

        if ("-".equals(operador)) {
            // Operador negación numérica: debe ser integer
            if (!"integer".equals(tipoOperando)) {
                semanticVisitor.agregarError(
                        SemanticError.getUnaryArithmeticErrorMessage("-", tipoOperando),
                        ctx.start.getLine(),
                        ctx.start.getCharPositionInLine()
                );
            }
            return "integer";
        } else if ("!".equals(operador)) {
            // Operador negación lógica: debe ser boolean
            if (!"boolean".equals(tipoOperando)) {
                semanticVisitor.agregarError(
                        SemanticError.getUnaryLogicalErrorMessage("!", tipoOperando),
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
        // Procesar cada suffixOp en orden
        String currentType = visitPrimaryAtom(ctx.primaryAtom());

        if (ctx.suffixOp() != null) {
            for (CompiscriptParser.SuffixOpContext suffixOp : ctx.suffixOp()) {
                if (suffixOp instanceof CompiscriptParser.CallExprContext) {
                    // Es una llamada a función
                    CompiscriptParser.CallExprContext callCtx = (CompiscriptParser.CallExprContext) suffixOp;
                    currentType = semanticVisitor.getFunctionsVisitor().visitCallExpr(callCtx);

                } else if (suffixOp instanceof CompiscriptParser.IndexExprContext) {
                    // Es acceso a array - ejemplo: arr[0]
                    if (currentType.endsWith("[]")) {
                        // Remover una dimensión del array
                        currentType = currentType.substring(0, currentType.length() - 2);
                    } else {
                        semanticVisitor.agregarError("Intento de indexar tipo no-array: " + currentType,
                                suffixOp.start.getLine(), suffixOp.start.getCharPositionInLine());
                        currentType = "ERROR";
                    }

                } else if (suffixOp instanceof CompiscriptParser.PropertyAccessExprContext) {
                    currentType = visitPropertyAccessExpr((CompiscriptParser.PropertyAccessExprContext) suffixOp);
                }
            }
        }

        return currentType;
    }

    public String visitPrimaryAtom(CompiscriptParser.PrimaryAtomContext ctx) {
        if (ctx instanceof CompiscriptParser.IdentifierExprContext) {
            // Es un identificador simple
            String identifier = ((CompiscriptParser.IdentifierExprContext) ctx).Identifier().getText();
            Symbol symbol = semanticVisitor.getEntornoActual().obtener(identifier);

            if (symbol == null) {
                semanticVisitor.agregarError("Variable '" + identifier + "' no declarada",
                        ctx.start.getLine(), ctx.start.getCharPositionInLine());
                return "ERROR";
            }

            return symbol.getType();

        } else if (ctx instanceof CompiscriptParser.NewExprContext) {
            // Es una construcción de objeto - ejemplo: new MiClase()
            String className = ((CompiscriptParser.NewExprContext) ctx).Identifier().getText();
            return className; // Retorna el tipo de la clase

        } else if (ctx instanceof CompiscriptParser.ThisExprContext) {
            // Es 'this' - depende del contexto de clase actual
            return "this"; // O el tipo de la clase actual si estás dentro de una
        }

        return "OBJECT"; // Fallback
    }

    // Maneja la creación de nuevas instancias
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
        ParserRuleContext parent = ctx.getParent();
        Symbol classSym = null;

        if (parent instanceof CompiscriptParser.LeftHandSideContext leftCtx) {
            String leftTipo = visitPrimaryAtom(leftCtx.primaryAtom());
            classSym = semanticVisitor.getEntornoActual().obtener(leftTipo);

            if (classSym == null || classSym.getKind() != Symbol.Kind.CLASS) {
                semanticVisitor.agregarError(
                        "Tipo '" + leftTipo + "' no es una clase válida",
                        ctx.start.getLine(),
                        ctx.start.getCharPositionInLine()
                );
                return null;
            }
        }

        if (classSym == null || classSym.getKind() != Symbol.Kind.CLASS) {
            semanticVisitor.agregarError(
                    "No se encontró una clase válida",
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
        return semanticVisitor.getLogicalVisitor().visit(ctx.conditionalExpr());
    }


    @Override
    public String visitTernaryExpr(CompiscriptParser.TernaryExprContext ctx) {
        return semanticVisitor.getLogicalVisitor().visit(ctx.logicalOrExpr());
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
            return semanticVisitor.getLogicalVisitor().visit(ctx.expression());
        }
        return "desconocido";
    }

    @Override
    public String visitPropertyAssignExpr(CompiscriptParser.PropertyAssignExprContext ctx) {
        // Izquierda: base y miembro
        String baseName = ctx.leftHandSide().getText();
        String memberName = ctx.Identifier().getText();

        System.out.println("DEBUG >> PropertyAssignExpr: " + baseName + "." + memberName);

        // Verificar que el objeto existe
        Symbol baseSym = semanticVisitor.getEntornoActual().obtener(baseName);
        if (baseSym == null) {
            semanticVisitor.agregarError(
                    "Objeto '" + baseName + "' no declarado",
                    ctx.start.getLine(), ctx.start.getCharPositionInLine()
            );
            return "ERROR";
        }

        if (baseSym.getKind() != Symbol.Kind.VARIABLE) {
            semanticVisitor.agregarError(
                    "'" + baseName + "' no es un objeto",
                    ctx.start.getLine(), ctx.start.getCharPositionInLine()
            );
            return "ERROR";
        }

        // Verificar que la clase tiene ese miembro
        String classType = baseSym.getType();
        Symbol classSym = semanticVisitor.getEntornoActual().obtener(classType);
        if (classSym == null || classSym.getKind() != Symbol.Kind.CLASS) {
            semanticVisitor.agregarError(
                    "Clase '" + classType + "' no existe",
                    ctx.start.getLine(), ctx.start.getCharPositionInLine()
            );
            return "ERROR";
        }

        Symbol memberSym = classSym.getMembers().get(memberName);
        if (memberSym == null) {
            semanticVisitor.agregarError(
                    "Miembro '" + memberName + "' no existe en la clase '" + classType + "'",
                    ctx.start.getLine(), ctx.start.getCharPositionInLine()
            );
            return "ERROR";
        }

        // Derecha: evaluar expresión sin llamar a visitPropertyAssignExpr recursivamente
        String rightType = visit(ctx.assignmentExpr()); // esto está bien mientras ctx.assignmentExpr() no sea otro PropertyAssignExpr anidado directamente

        // Validar tipos
        if (!memberSym.getType().equals(rightType) && !"desconocido".equals(rightType)) {
            semanticVisitor.agregarError(
                    "Tipo de '" + memberName + "' (" + memberSym.getType() + ") no coincide con expresión (" + rightType + ")",
                    ctx.start.getLine(), ctx.start.getCharPositionInLine()
            );
        } else {
            memberSym.setInitialized(true);
        }

        return memberSym.getType();
    }



}
