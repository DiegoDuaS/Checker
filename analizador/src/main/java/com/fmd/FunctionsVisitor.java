package com.fmd;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.tree.ParseTree;

import com.fmd.modules.Symbol;

public class FunctionsVisitor extends CompiscriptBaseVisitor<String> {
    private final SemanticVisitor semanticVisitor;
    private Symbol currentFunction;

    public FunctionsVisitor(SemanticVisitor semanticVisitor) {
        this.semanticVisitor = semanticVisitor;
    }

    // Funciones recursivas y Detección de múltiples declaraciones
    @Override
    public String visitFunctionDeclaration(CompiscriptParser.FunctionDeclarationContext ctx) {
        String functionName = ctx.Identifier().getText();

        // Reconocer si la función ya fue declarada en el ámbito actual
        if (semanticVisitor.getEntornoActual().existeLocal(functionName)) {
            semanticVisitor.agregarError("Función '" + functionName + "' ya fue declarada en este ámbito",
                    ctx.start.getLine(), ctx.start.getCharPositionInLine());
            return "ERROR";
        }

        String tipo = ctx.type() != null ? ctx.type().getText() : "void";

        // Crear función
        Symbol function = new Symbol(functionName, Symbol.Kind.FUNCTION, tipo, ctx, ctx.start.getLine(),
                ctx.start.getCharPositionInLine(), false);

        // Si estamos dentro de otra función, es una función anidada
        if (currentFunction != null) {
            function.setEnclosingFunctionName(currentFunction.getName());
            function.setNested(true);
        }

        // Agregar función ANTES de procesar el cuerpo (para recursión)
        semanticVisitor.getEntornoActual().agregar(function);

        // Nueva tabla para parámetros y variables locales
        semanticVisitor.entrarScope();
        Symbol previousFunction = currentFunction;
        currentFunction = function;

        // Procesar parámetros
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

        // Analizar variables capturadas si es función anidada
        if (function.isNested()) {
            Set<String> capturedVars = findCapturedVariables(ctx.block());
            function.setCapturedVariables(capturedVars);

            // Validar que las variables capturadas existen en ámbitos externos
            for (String varName : capturedVars) {
                if (!semanticVisitor.getEntornoActual().getPadre().existeGlobal(varName)) {
                    semanticVisitor.agregarError("Variable capturada '" + varName +
                            "' no existe en ámbitos externos", ctx.start.getLine(), ctx.start.getCharPositionInLine());
                }
            }
        }

        // Procesar cuerpo (aquí puede haber llamadas recursivas)
        semanticVisitor.visit(ctx.block());

        // Restaurar contexto anterior
        currentFunction = previousFunction;
        semanticVisitor.salirScope();

        return tipo;
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
            actualReturnType = semanticVisitor.getVariableVisitor().visit(ctx.expression()); // Obtener tipo de la expresión
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

    // Validación de argumentos en llamadas a funciones
    @Override
    public String visitCallExpr(CompiscriptParser.CallExprContext ctx) {
        // Obtener el nombre de la función del contexto padre
        String functionName = getFunctionNameFromContext(ctx);
        Symbol function = semanticVisitor.getEntornoActual().obtener(functionName);

        if (function == null) {
            semanticVisitor.agregarError("Función '" + functionName + "' no declarada", ctx.start.getLine(),
                    ctx.start.getCharPositionInLine());
            return "ERROR";
        }

        // Validar que sea realmente una función
        if (function.getKind() != Symbol.Kind.FUNCTION) {
            semanticVisitor.agregarError("'" + functionName + "' no es una función", ctx.start.getLine(),
                    ctx.start.getCharPositionInLine());
            return "ERROR";
        }

        // Validar número de argumentos
        int expectedArgs = function.getParameterCount();
        int actualArgs = ctx.arguments() != null ? ctx.arguments().expression().size() : 0;

        if (expectedArgs != actualArgs) {
            semanticVisitor.agregarError("Función '" + functionName + "' espera " +
                            expectedArgs + " argumentos, pero recibe " + actualArgs, ctx.start.getLine(),
                    ctx.start.getCharPositionInLine());
            return "ERROR";
        }

        // Validar tipos de argumentos
        if (ctx.arguments() != null) {
            List<Symbol> functionParams = function.getParams();
            List<CompiscriptParser.ExpressionContext> args = ctx.arguments().expression();
            for (int i = 0; i < args.size(); i++) {
                String actualType = semanticVisitor.getVariableVisitor().visit(args.get(i));
                String expectedType = functionParams.get(i).getType();

                if (!typesCompatible(actualType, expectedType)) {
                    semanticVisitor.agregarError("Argumento " + (i + 1) + " en función '" +
                                    functionName + "': esperado " + expectedType + ", encontrado " + actualType,
                            ctx.start.getLine(), ctx.start.getCharPositionInLine());
                }
            }
        }

        return function.getType();
    }

    // Método auxiliar para compatibilidad de tipos
    private boolean typesCompatible(String actualType, String expectedType) {
        if (actualType == null) {
            actualType = "void";
        }

        if (actualType.equals(expectedType))
            return true;
        if ("ERROR".equals(actualType))
            return true; // No reportar errores cascada

        return false;
    }

    // Método auxiliar para obtener el nombre de la función
    private String getFunctionNameFromContext(CompiscriptParser.CallExprContext ctx) {
        // Buscar en el contexto padre para encontrar el identificador
        ParseTree parent = ctx.getParent();
        while (parent != null) {
            if (parent instanceof CompiscriptParser.LeftHandSideContext) {
                CompiscriptParser.LeftHandSideContext lhs = (CompiscriptParser.LeftHandSideContext) parent;
                if (lhs.primaryAtom() instanceof CompiscriptParser.IdentifierExprContext) {
                    return ((CompiscriptParser.IdentifierExprContext) lhs.primaryAtom()).Identifier().getText();
                }
            }
            parent = parent.getParent();
        }
        return "unknown";
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

    // Método para encontrar variables capturadas (closure)
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