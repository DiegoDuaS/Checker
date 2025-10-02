package com.fmd;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.tree.ParseTree;

import com.fmd.modules.Symbol;
import com.fmd.CompiscriptLexer;
import com.fmd.CompiscriptParser;
import com.fmd.CompiscriptBaseVisitor;

public class FunctionsVisitor extends CompiscriptBaseVisitor<Object> {
    private final SemanticVisitor semanticVisitor;
    private Symbol currentFunction;

    public FunctionsVisitor(SemanticVisitor semanticVisitor) {
        this.semanticVisitor = semanticVisitor;
    }

    // Funciones recursivas y Detección de múltiples declaraciones
    @Override
    public Symbol visitFunctionDeclaration(CompiscriptParser.FunctionDeclarationContext ctx) {
        String functionName = ctx.Identifier().getText();

        if (semanticVisitor.getEntornoActual().existeLocal(functionName)) {
            semanticVisitor.agregarError("Función '" + functionName + "' ya fue declarada en este ámbito",
                    ctx.start.getLine(), ctx.start.getCharPositionInLine());
            return null;
        }

        String tipo = ctx.type() != null ? ctx.type().getText() : "void";
        Symbol function = new Symbol(functionName, Symbol.Kind.FUNCTION, tipo, ctx,
                ctx.start.getLine(), ctx.start.getCharPositionInLine(), false);

        if (currentFunction != null) {
            function.setEnclosingFunctionName(currentFunction.getName());
            function.setNested(true);
        }

        semanticVisitor.getEntornoActual().agregar(function);

        Symbol previousFunction = currentFunction;
        currentFunction = function;
        semanticVisitor.entrarScope();

        if (ctx.parameters() != null) {
            for (CompiscriptParser.ParameterContext param : ctx.parameters().parameter()) {
                String paramName = param.Identifier().getText();
                String paramType = param.type() != null ? getTypeFromContext(param.type()) : "OBJECT";

                Symbol newParameter = new Symbol(paramName, Symbol.Kind.VARIABLE, paramType, param,
                        param.start.getLine(), param.start.getCharPositionInLine(), false);
                function.addParameter(newParameter);
                semanticVisitor.getEntornoActual().agregar(newParameter);
            }
        }

        if (function.isNested()) {
            Set<String> capturedVars = findCapturedVariables(ctx.block());
            function.setCapturedVariables(capturedVars);

            for (String varName : capturedVars) {
                if (!semanticVisitor.getEntornoActual().getPadre().existeGlobal(varName)) {
                    semanticVisitor.agregarError("Variable capturada '" + varName +
                            "' no existe en ámbitos externos", ctx.start.getLine(), ctx.start.getCharPositionInLine());
                }
            }
        }

        visitBlock(ctx.block());
        function.setMembers(semanticVisitor.getEntornoActual().getSymbolsLocal());

        currentFunction = previousFunction;
        semanticVisitor.salirScope();

        return function;
    }

    // Validación de tipo de retorno
    @Override
    public String visitReturnStatement(CompiscriptParser.ReturnStatementContext ctx) {
        if (currentFunction == null) {
            semanticVisitor.agregarError("return fuera de una función", ctx.start.getLine(),
                    ctx.start.getCharPositionInLine());
            return "ERROR";
        }

        String expectedReturnType = currentFunction.getType();
        String actualReturnType;

        if (ctx.expression() != null) {
            actualReturnType = semanticVisitor.getVariableVisitor().visit(ctx.expression()); // Obtener tipo de la
                                                                                             // expresión
        } else {
            actualReturnType = "void";
        }

        if (actualReturnType == null) {
            actualReturnType = "void";
        }

        if (!actualReturnType.equals(expectedReturnType)) {
            semanticVisitor.agregarError("Tipo de retorno incorrecto: esperado " + expectedReturnType +
                    ", encontrado " + actualReturnType, ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        return actualReturnType;
    }

    @Override
    public String visitCallExpr(CompiscriptParser.CallExprContext ctx) {
        // Obtener base y método
        String[] parts = getFunctionParts(ctx);
        String baseName = parts[0]; // null o "unknown" si no hay objeto
        String methodName = parts[1]; // nombre de la función o método

        // ============================
        // CASO 1: objeto.funcion()
        // ============================
        if (baseName != null && methodName != null) {
            Symbol baseSym = semanticVisitor.getEntornoActual().obtener(baseName);

            if (baseSym == null) {
                semanticVisitor.agregarError(
                        "Variable u objeto '" + baseName + "' no declarado",
                        ctx.start.getLine(), ctx.start.getCharPositionInLine());
                return "ERROR";
            }

            if (baseSym.getKind() != Symbol.Kind.VARIABLE) {
                semanticVisitor.agregarError(
                        "'" + baseName + "' no es un objeto para llamar métodos",
                        ctx.start.getLine(), ctx.start.getCharPositionInLine());
                return "ERROR";
            }

            // El objeto existe, obtenemos su clase
            String classType = baseSym.getType();
            Symbol classSym = semanticVisitor.getEntornoActual().obtener(classType);

            if (classSym == null || classSym.getKind() != Symbol.Kind.CLASS) {
                semanticVisitor.agregarError(
                        "Clase '" + classType + "' no existe",
                        ctx.start.getLine(), ctx.start.getCharPositionInLine());
                return "ERROR";
            }

            // Buscar el método en la clase y superclases
            Symbol methodSym = null;
            Symbol currentClassSym = classSym;
            while (currentClassSym != null && methodSym == null) {
                methodSym = currentClassSym.getMembers().values().stream()
                        .filter(m -> m.getName().equals(methodName) && m.getKind() == Symbol.Kind.FUNCTION)
                        .findFirst().orElse(null);

                if (methodSym == null && currentClassSym.getSuperClass() != null) {
                    currentClassSym = semanticVisitor.getEntornoActual().obtener(currentClassSym.getSuperClass());
                } else {
                    currentClassSym = null;
                }
            }

            if (methodSym == null) {
                semanticVisitor.agregarError(
                        "Método '" + methodName + "' no existe en la clase '" + classType + "' ni en sus superclases",
                        ctx.start.getLine(), ctx.start.getCharPositionInLine());
                return "ERROR";
            }

            // Validar argumentos
            int expectedArgs = methodSym.getParameterCount();
            int actualArgs = ctx.arguments() != null ? ctx.arguments().expression().size() : 0;

            if (expectedArgs != actualArgs) {
                semanticVisitor.agregarError(
                        "Método '" + methodName + "' espera " + expectedArgs +
                                " argumentos, pero recibe " + actualArgs,
                        ctx.start.getLine(), ctx.start.getCharPositionInLine());
                return "ERROR";
            }

            return methodSym.getType();
        }

        // ============================
        // CASO 2: funcion()
        // ============================
        if (baseName == null && methodName != null) {
            Symbol funcSym = semanticVisitor.getEntornoActual().obtener(methodName);

            // 1. ¿Es función global?
            if (funcSym != null && funcSym.getKind() == Symbol.Kind.FUNCTION) {
                int expectedArgs = funcSym.getParameterCount();
                int actualArgs = ctx.arguments() != null ? ctx.arguments().expression().size() : 0;

                if (expectedArgs != actualArgs) {
                    semanticVisitor.agregarError(
                            "Función '" + funcSym.getName() + "' espera " + expectedArgs +
                                    " argumentos, pero recibe " + actualArgs,
                            ctx.start.getLine(), ctx.start.getCharPositionInLine());
                    return "ERROR";
                }

                // Validar tipos de argumentos
                if (actualArgs > 0) {
                    List<Symbol> functionParams = funcSym.getParams();
                    List<CompiscriptParser.ExpressionContext> args = ctx.arguments().expression();
                    for (int i = 0; i < actualArgs; i++) {
                        String actualType = semanticVisitor.getVariableVisitor().visit(args.get(i));
                        String expectedType = functionParams.get(i).getType();

                        if (!typesCompatible(actualType, expectedType)) {
                            semanticVisitor.agregarError("Argumento " + (i + 1) + " en función '" +
                                    funcSym.getName() + "': esperado " + expectedType + ", encontrado " + actualType,
                                    ctx.start.getLine(), ctx.start.getCharPositionInLine());
                        }
                    }
                }

                return funcSym.getType();
            }

            // 2. ¿Es método de alguna clase?
            boolean metodoDeClase = false;
            for (Symbol s : semanticVisitor.getEntornoActual().getAllSymbols().values()) {
                if (s.getKind() == Symbol.Kind.CLASS) {
                    for (Symbol m : s.getMembers().values()) {
                        if (m.getKind() == Symbol.Kind.FUNCTION && m.getName().equals(methodName)) {
                            metodoDeClase = true;
                        }
                    }
                }
            }

            if (metodoDeClase) {
                semanticVisitor.agregarError(
                        "Método '" + methodName + "' no se puede llamar sin su clase",
                        ctx.start.getLine(), ctx.start.getCharPositionInLine());
                return "ERROR";
            }

            // 3. ¿Es variable?
            if (funcSym != null && funcSym.getKind() == Symbol.Kind.VARIABLE) {
                semanticVisitor.agregarError(
                        "La variable '" + methodName + "' no es una función",
                        ctx.start.getLine(), ctx.start.getCharPositionInLine());
                return "ERROR";
            }

            // 4. Si no existe en ningún lado
            semanticVisitor.agregarError(
                    "Función o método '" + methodName + "' no está definido",
                    ctx.start.getLine(), ctx.start.getCharPositionInLine());
            return "ERROR";
        }

        return "ERROR";
    }

    @Override
    public String visitBlock(CompiscriptParser.BlockContext ctx) {
        for (CompiscriptParser.StatementContext stmt : ctx.statement()) {
            semanticVisitor.visit(stmt);
        }
        return null;
    }

    // Método auxiliar para compatibilidad de tipos
    private boolean typesCompatible(String actualType, String expectedType) {
        if (actualType == null) {
            actualType = "void";
        }
        return actualType.equals(expectedType) || "ERROR".equals(actualType);
    }

    // Método auxiliar para obtener el nombre de la función
    // Método auxiliar para obtener base y nombre de función
    private String[] getFunctionParts(CompiscriptParser.CallExprContext ctx) {
        ParseTree parent = ctx.getParent();

        // Caso típico de objeto.metodo()
        while (parent != null) {
            if (parent instanceof CompiscriptParser.LeftHandSideContext) {
                CompiscriptParser.LeftHandSideContext lhs = (CompiscriptParser.LeftHandSideContext) parent;

                if (lhs.primaryAtom() instanceof CompiscriptParser.IdentifierExprContext) {
                    String baseName = ((CompiscriptParser.IdentifierExprContext) lhs.primaryAtom()).Identifier()
                            .getText();
                    String methodName = null;

                    if (lhs.suffixOp() != null) {
                        for (CompiscriptParser.SuffixOpContext suffix : lhs.suffixOp()) {
                            if (suffix instanceof CompiscriptParser.PropertyAccessExprContext) {
                                CompiscriptParser.PropertyAccessExprContext prop = (CompiscriptParser.PropertyAccessExprContext) suffix;
                                if (prop.Identifier() != null) {
                                    methodName = prop.Identifier().getText();
                                    break; // primer PropertyAccess encontrado
                                }
                            }
                        }
                    }

                    // Si no hay suffix, significa que es una llamada directa a función global o de
                    // clase
                    if (methodName == null) {
                        methodName = baseName;
                        baseName = null;
                    }

                    return new String[] { baseName, methodName };
                }
            }
            parent = parent.getParent();
        }

        return new String[] { null, "unknown" };
    }

    // Método auxiliar para obtener tipo desde contexto
    private String getTypeFromContext(CompiscriptParser.TypeContext typeCtx) {
        if (typeCtx == null)
            return "OBJECT";

        String baseType = typeCtx.baseType().getText();

        // Contar dimensiones del array
        int arrayDimensions = typeCtx.getChildCount() - 1; // -1 porque el primer hijo es baseType

        StringBuilder result = new StringBuilder(baseType);
        for (int i = 0; i < arrayDimensions; i++) {
            result.append("[]");
        }

        return result.toString();
    }

    // Metodo para encontrar variables capturadas (closure)
    private Set<String> findCapturedVariables(CompiscriptParser.BlockContext block) {
        VariableCaptureAnalyzer analyzer = new VariableCaptureAnalyzer();
        analyzer.visit(block);
        return analyzer.getCapturedVariables();
    }

    // Clase auxiliar para analizar variables capturadas
    private class VariableCaptureAnalyzer extends CompiscriptBaseVisitor<Void> {
        private Set<String> capturedVariables = new HashSet<>();

        @Override
        public Void visitIdentifierExpr(CompiscriptParser.IdentifierExprContext ctx) {
            String varName = ctx.Identifier().getText();

            // Si la variable no está en el ámbito actual pero existe en ámbitos externos
            if (!semanticVisitor.getEntornoActual().existeLocal(varName) &&
                    semanticVisitor.getEntornoActual().getPadre() != null &&
                    semanticVisitor.getEntornoActual().getPadre().existeGlobal(varName)) {
                capturedVariables.add(varName);
            }

            return null;
        }

        public Set<String> getCapturedVariables() {
            return capturedVariables;
        }
    }
}