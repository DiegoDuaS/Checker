package com.fmd;

import com.fmd.modules.Symbol;

import com.fmd.CompiscriptParser;
import com.fmd.CompiscriptBaseListener;

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
        semanticVisitor.entrarScope();
        currentClass = clase;
    }

    @Override
    public void exitClassDeclaration(CompiscriptParser.ClassDeclarationContext ctx) {
        semanticVisitor.salirScope();
        currentClass = null;
    }

    @Override
    public void enterVariableDeclaration(CompiscriptParser.VariableDeclarationContext ctx) {
        String varName = ctx.Identifier().getText();
        boolean initialized = false;

        // Inferir tipo si hay typeAnnotation
        String type = ctx.typeAnnotation() != null ? ctx.typeAnnotation().type().getText() : null;

        // Verificar duplicados
        if (currentClass != null) {
            if (currentClass.getMembers().containsKey(varName)) {
                semanticVisitor.agregarError(
                        "Miembro '" + varName + "' ya declarado en la clase '" + currentClass.getName() + "'",
                        ctx.start.getLine(),
                        ctx.start.getCharPositionInLine()
                );
                return;
            }
        } else {
            if (semanticVisitor.getEntornoActual().existeLocal(varName)) {
                semanticVisitor.agregarError(
                        "Variable '" + varName + "' ya declarada en este scope",
                        ctx.start.getLine(),
                        ctx.start.getCharPositionInLine()
                );
                return;
            }
        }

        // Inferir tipo desde el inicializador si existe
        if (ctx.initializer() != null && ctx.initializer().expression() != null) {
            String tipoInicializador = semanticVisitor.getExpressionType(ctx.initializer().expression());
            initialized = true;

            if (type == null) {
                type = tipoInicializador;
            } else if (!type.equals(tipoInicializador) && !"desconocido".equals(tipoInicializador)) {
                semanticVisitor.agregarError(
                        "No se puede inicializar variable '" + varName + "' de tipo '" + type +
                                "' con expresión de tipo '" + tipoInicializador + "'",
                        ctx.start.getLine(),
                        ctx.start.getCharPositionInLine()
                );
            }
        }

        if (type == null) type = "desconocido";

        // Crear símbolo
        Symbol varSym = new Symbol(
                varName,
                Symbol.Kind.VARIABLE,
                type,
                ctx,
                ctx.start.getLine(),
                ctx.start.getCharPositionInLine(),
                true
        );

        // Registrar como miembro de clase o en el entorno
        if (currentClass != null) {
            currentClass.addMember(varSym);
            varSym.setEnclosingClassName(currentClass.getName());
            if (initialized) {
                varSym.setInitialized(true);
            }

        }
            semanticVisitor.getEntornoActual().agregar(varSym);

    }




    @Override
    public void enterFunctionDeclaration(CompiscriptParser.FunctionDeclarationContext ctx) {
        String funcName = ctx.Identifier().getText();
        boolean hayError = false;

        boolean isConstructor = currentClass != null && funcName.equals("constructor");

        // Tipo de retorno
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

        // Registrar parámetros
        if (ctx.parameters() != null) {
            for (CompiscriptParser.ParameterContext paramCtx : ctx.parameters().parameter()) {
                String paramName = paramCtx.Identifier().getText();
                String paramType = paramCtx.type() != null ? paramCtx.type().getText() : "desconocido";
                Symbol paramSym = new Symbol(paramName, Symbol.Kind.VARIABLE, paramType, paramCtx,
                        paramCtx.start.getLine(), paramCtx.start.getCharPositionInLine(), true);
                funcSym.addParameter(paramSym);
            }
        }

        // 🔹 Paso 2: Recorrer cuerpo del constructor
        if (isConstructor && !hayError) {
            for (CompiscriptParser.StatementContext stmt : ctx.block().statement()) {
                if (stmt.assignment() != null) {
                    CompiscriptParser.AssignmentContext assignCtx = stmt.assignment();

                    // Solo permitimos asignaciones this.<miembro> = <parametro>
                    if (assignCtx.expression().size() == 2 && assignCtx.getText().contains("this.")) {
                        String left = assignCtx.getText().split("=")[0].trim();
                        String right = assignCtx.expression(1).getText();

                        if (!left.startsWith("this.")) {
                            semanticVisitor.agregarError(
                                    "En constructor solo se permiten asignaciones a miembros de la clase",
                                    assignCtx.start.getLine(),
                                    assignCtx.start.getCharPositionInLine()
                            );
                            continue;
                        }

                        String memberName = left.substring(5);
                        Symbol memberSym = currentClass.getMembers().get(memberName);
                        Symbol paramSym = funcSym.getParams().stream()
                                .filter(p -> p.getName().equals(right))
                                .findFirst()
                                .orElse(null);

                        if (memberSym == null) {
                            semanticVisitor.agregarError(
                                    "Miembro '" + memberName + "' no existe en la clase",
                                    assignCtx.start.getLine(),
                                    assignCtx.start.getCharPositionInLine()
                            );
                            continue;
                        }

                        if (paramSym == null) {
                            semanticVisitor.agregarError(
                                    "Asignación inválida: '" + right + "' no es parámetro del constructor",
                                    assignCtx.start.getLine(),
                                    assignCtx.start.getCharPositionInLine()
                            );
                            continue;
                        }

                        // Verificar tipo
                        if (!memberSym.getType().equals(paramSym.getType()) && !"desconocido".equals(paramSym.getType())) {
                            semanticVisitor.agregarError(
                                    "Tipo del parámetro '" + right + "' (" + paramSym.getType() +
                                            ") no coincide con tipo del miembro '" + memberName + "' (" + memberSym.getType() + ")",
                                    assignCtx.start.getLine(),
                                    assignCtx.start.getCharPositionInLine()
                            );
                        } else {
                            memberSym.setInitialized(true);
                        }

                    } else {
                        semanticVisitor.agregarError(
                                "En constructor solo se permiten asignaciones de miembros (this.<miembro> = <parametro>)",
                                assignCtx.start.getLine(),
                                assignCtx.start.getCharPositionInLine()
                        );
                    }
                } else {
                    semanticVisitor.agregarError(
                            "En constructor solo se permiten asignaciones",
                            stmt.start.getLine(),
                            stmt.start.getCharPositionInLine()
                    );
                }
            }
        }

        if (!isConstructor || (isConstructor && !hayError)) {
            if (currentClass != null) {
                funcSym.setEnclosingClassName(currentClass.getName());
                if (isConstructor) {
                    funcSym.setConstructor(true);
                }
                currentClass.getMembers().put(funcName, funcSym);
            }
            semanticVisitor.getEntornoActual().agregar(funcSym);
        }

    }

    @Override
    public void enterConstantDeclaration(CompiscriptParser.ConstantDeclarationContext ctx) {
        String constName = ctx.Identifier().getText();
        String type = ctx.typeAnnotation() != null ? ctx.typeAnnotation().type().getText() : null;

        // Verificar duplicados
        if (currentClass != null) {
            if (currentClass.getMembers().containsKey(constName)) {
                semanticVisitor.agregarError(
                        "Constante '" + constName + "' ya declarada en esta clase",
                        ctx.start.getLine(),
                        ctx.start.getCharPositionInLine()
                );
                return;
            }
        } else {
            if (semanticVisitor.getEntornoActual().existeLocal(constName)) {
                semanticVisitor.agregarError(
                        "Constante '" + constName + "' ya declarada en este scope",
                        ctx.start.getLine(),
                        ctx.start.getCharPositionInLine()
                );
                return;
            }
        }

        // Debe inicializarse obligatoriamente
        if (ctx.expression() == null) {
            semanticVisitor.agregarError(
                    "La constante '" + constName + "' debe inicializarse",
                    ctx.start.getLine(),
                    ctx.start.getCharPositionInLine()
            );
            return;
        }

        // Obtener tipo de la expresión
        String tipoExpresion = semanticVisitor.getExpressionType(ctx.expression());

        // Inferir tipo si no está declarado
        if (type == null) {
            type = tipoExpresion;
        } else if (!type.equals(tipoExpresion) && !"desconocido".equals(tipoExpresion)) {
            semanticVisitor.agregarError(
                    "No se puede inicializar constante '" + constName + "' de tipo '" + type +
                            "' con expresión de tipo '" + tipoExpresion + "'",
                    ctx.start.getLine(),
                    ctx.start.getCharPositionInLine()
            );
        }

        // Crear símbolo como CONSTANTE y siempre inicializada
        Symbol constSym = new Symbol(
                constName,
                Symbol.Kind.CONSTANT,
                type,
                ctx,
                ctx.start.getLine(),
                ctx.start.getCharPositionInLine(),
                true
        );

        // Registrar como miembro de clase o en el entorno
        if (currentClass != null) {
            currentClass.addMember(constSym);
            constSym.setEnclosingClassName(currentClass.getName());
        }

        semanticVisitor.getEntornoActual().agregar(constSym);
    }



    public void enterAssignment(CompiscriptParser.AssignmentContext ctx) {
        String nombreVar = ctx.Identifier().getText();

        Symbol sym;
        if (nombreVar.startsWith("this.")) {
            if (currentClass == null) {
                semanticVisitor.agregarError(
                        "Uso de 'this' fuera de una clase",
                        ctx.start.getLine(), ctx.start.getCharPositionInLine()
                );
                return;
            }
            String memberName = nombreVar.substring(5);
            sym = currentClass.getMembers().get(memberName);
            if (sym == null) {
                semanticVisitor.agregarError(
                        "Miembro '" + memberName + "' no existe en la clase '" + currentClass.getName() + "'",
                        ctx.start.getLine(), ctx.start.getCharPositionInLine()
                );
                return;
            }
        } else {
            sym = semanticVisitor.getEntornoActual().obtener(nombreVar);
            if (sym == null) {
                semanticVisitor.agregarError(
                        "Variable '" + nombreVar + "' no declarada",
                        ctx.start.getLine(), ctx.start.getCharPositionInLine()
                );
                return;
            }
        }

        // Revisar mutabilidad
        if (!sym.isMutable()) {
            semanticVisitor.agregarError(
                    "No se puede asignar a la constante '" + nombreVar + "'",
                    ctx.start.getLine(), ctx.start.getCharPositionInLine()
            );
        }

        // Obtener tipo de la expresión usando tu visitor de tipos
        String tipoExpr = semanticVisitor.getExpressionType(ctx.expression(0));

        // Chequeo de tipos
        if (!sym.getType().equals(tipoExpr) && !"desconocido".equals(tipoExpr)) {
            semanticVisitor.agregarError(
                    "No se puede asignar valor de tipo '" + tipoExpr + "' a variable '" + nombreVar + "' de tipo '" + sym.getType() + "'",
                    ctx.start.getLine(), ctx.start.getCharPositionInLine()
            );
        }

        // Marcar miembro como inicializado
        if (nombreVar.startsWith("this.")) {
            sym.setInitialized(true);
        }
    }



}
