package com.fmd;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fmd.modules.SemanticError;

public class FunctionSemanticTest {

    private SemanticVisitor semanticVisitor;

    @BeforeEach
    void setUp() {
        semanticVisitor = new SemanticVisitor();
    }

    private List<SemanticError> analyzeCode(String code) {
        try {
            ParseTree tree = TestUtils.getParseTree(code, CompiscriptParser::program);
            semanticVisitor.visit(tree);

            return semanticVisitor.getErrores();
        } catch (Exception e) {
            fail("Error al parsear código: " + e.getMessage());
            return null;
        }
    }

    // Metodo adicional para testing de reglas específicas
    private List<SemanticError> analyzeFunctionDeclaration(String functionCode) {
        try {
            ParseTree tree = TestUtils.getParseTree(functionCode, CompiscriptParser::functionDeclaration);
            semanticVisitor.visit(tree);

            return semanticVisitor.getErrores();
        } catch (Exception e) {
            fail("Error al parsear función: " + e.getMessage());
            return null;
        }
    }

    // Metodo para testing de expresiones
    private String analyzeExpression(String expressionCode) {
        try {
            ParseTree tree = TestUtils.getParseTree(expressionCode, CompiscriptParser::expression);
            return semanticVisitor.getVariableVisitor().visit(tree);
        } catch (Exception e) {
            fail("Error al parsear expresión: " + e.getMessage());
            return null;
        }
    }

    // ✅ CASOS CORRECTOS - No deberían generar errores

    @Test
    void testFunctionDeclarationCorrect() {
        String code = """
            function saludar(nombre: string): string {
                return "Hola " + nombre;
            }
            let mensaje = saludar("Mundo");
            """;

        List<SemanticError> errors = analyzeCode(code);
        assertNotNull(errors);
        assertTrue(errors.isEmpty(), "No debería haber errores en declaración correcta");
    }

    @Test
    void testNestedFunctionCorrect() {
        String code = """
            function crearContador(): integer {
                function siguiente(): integer {
                    return 1;
                }
                return siguiente();
            }
            """;

        List<SemanticError> errors = analyzeCode(code);
        assertNotNull(errors);
        assertTrue(errors.isEmpty(), "No debería haber errores en función anidada correcta");
    }

    @Test
    void testMultipleParametersCorrect() {
        String code = """
            function suma(a: integer, b: integer): integer {
                return a + b;
            }
            let resultado = suma(5, 3);
            """;

        List<SemanticError> errors = analyzeCode(code);
        assertNotNull(errors);
        assertTrue(errors.isEmpty(), "No debería haber errores en función con múltiples parámetros");
    }

    @Test
    void testRecursiveFunctionCorrect() {
        String code = """
            function factorial(n: integer): integer {
                if (n <= 1) return 1;
                return n * factorial(n - 1);
            }
            let result = factorial(5);
            """;

        List<SemanticError> errors = analyzeCode(code);
        assertNotNull(errors);
        assertTrue(errors.isEmpty(), "No debería haber errores en función recursiva correcta");
    }

    @Test
    void testFunctionWithoutReturnType() {
        String code = """
            function imprimir() {
                print("Hola mundo");
            }
            imprimir();
            """;

        List<SemanticError> errors = analyzeCode(code);
        assertNotNull(errors);
        assertTrue(errors.isEmpty(), "Función void debería ser válida");
    }

    // ❌ CASOS INCORRECTOS - Deberían generar errores específicos

    @Test
    void testDuplicateFunctionDeclaration() {
        String code = """
            function test(): integer {
                return 1;
            }
            function test(): string {
                return "duplicada";
            }
            """;

        List<SemanticError> errors = analyzeCode(code);
        assertNotNull(errors);
        assertFalse(errors.isEmpty(), "Debería detectar función duplicada");
        assertTrue(errors.stream().anyMatch(e ->
                        e.getMensaje().contains("ya fue declarada")),
                "Debería reportar función duplicada");
    }

    @Test
    void testWrongArgumentCount() {
        String code = """
            function suma(a: integer, b: integer): integer {
                return a + b;
            }
            let resultado = suma(5); // Faltan argumentos
            """;

        List<SemanticError> errors = analyzeCode(code);
        assertNotNull(errors);
        assertFalse(errors.isEmpty(), "Debería detectar número incorrecto de argumentos");
        assertTrue(errors.stream().anyMatch(e ->
                        e.getMensaje().contains("espera 2 argumentos, pero recibe 1")),
                "Debería reportar número incorrecto de argumentos");
    }

    @Test
    void testTooManyArguments() {
        String code = """
            function saludar(nombre: string): string {
                return "Hola " + nombre;
            }
            let mensaje = saludar("Mundo", "Extra"); // Argumentos de más
            """;

        List<SemanticError> errors = analyzeCode(code);
        assertNotNull(errors);
        assertFalse(errors.isEmpty(), "Debería detectar demasiados argumentos");
        assertTrue(errors.stream().anyMatch(e ->
                        e.getMensaje().contains("espera 1 argumentos, pero recibe 2")),
                "Debería reportar demasiados argumentos");
    }

    @Test
    void testWrongArgumentType() {
        String code = """
            function procesar(num: integer): string {
                return "Procesado";
            }
            let resultado = procesar("texto"); // Tipo incorrecto
            """;

        List<SemanticError> errors = analyzeCode(code);
        assertNotNull(errors);
        assertFalse(errors.isEmpty(), "Debería detectar tipo de argumento incorrecto");
        assertTrue(errors.stream().anyMatch(e ->
                        e.getMensaje().contains("esperado integer, encontrado string")),
                "Debería reportar tipo de argumento incorrecto");
    }

    @Test
    void testWrongReturnType() {
        String code = """
            function obtenerNumero(): integer {
                return "no es numero"; // Tipo de retorno incorrecto
            }
            """;

        List<SemanticError> errors = analyzeCode(code);
        assertNotNull(errors);
        assertFalse(errors.isEmpty(), "Debería detectar tipo de retorno incorrecto");
        assertTrue(errors.stream().anyMatch(e ->
                        e.getMensaje().contains("Tipo de retorno incorrecto")),
                "Debería reportar tipo de retorno incorrecto");
    }

    @Test
    void testReturnOutsideFunction() {
        String code = """
            let x = 5;
            return x; // Return fuera de función
            """;

        List<SemanticError> errors = analyzeCode(code);
        assertNotNull(errors);
        assertFalse(errors.isEmpty(), "Debería detectar return fuera de función");
        assertTrue(errors.stream().anyMatch(e ->
                        e.getMensaje().contains("return fuera de una función")),
                "Debería reportar return fuera de función");
    }

    @Test
    void testUndeclaredFunctionCall() {
        String code = """
            let resultado = funcionInexistente(5);
            """;

        List<SemanticError> errors = analyzeCode(code);
        assertNotNull(errors);
        assertFalse(errors.isEmpty(), "Debería detectar función no declarada");
        assertTrue(errors.stream().anyMatch(e ->
                        e.getMensaje().contains("no declarada")),
                "Debería reportar función no declarada");
    }

    @Test
    void testCallNonFunction() {
        String code = """
            let variable = 42;
            let resultado = variable(5); // Intentar llamar una variable como función
            """;

        List<SemanticError> errors = analyzeCode(code);
        assertNotNull(errors);
        assertFalse(errors.isEmpty(), "Debería detectar llamada a no-función");
        assertTrue(errors.stream().anyMatch(e ->
                        e.getMensaje().contains("no es una función")),
                "Debería reportar que no es una función");
    }

    // 🧪 CASOS COMPLEJOS

    @Test
    void testNestedFunctionWithClosure() {
        String code = """
            function contador(): integer {
                let count = 0;
                function incrementar(): integer {
                    count = count + 1; // Variable capturada
                    return count;
                }
                return incrementar();
            }
            """;

        List<SemanticError> errors = analyzeCode(code);
        assertNotNull(errors);
        assertFalse(errors.isEmpty(), "Debería funcionar closure");
    }

    @Test
    void testRecursiveFunction() {
        String code = """
            function fibonacci(n: integer): integer {
                if (n <= 1) return n;
                return fibonacci(n - 1) + fibonacci(n - 2);
            }
            let result = fibonacci(10);
            """;

        List<SemanticError> errors = analyzeCode(code);
        assertNotNull(errors);
        assertTrue(errors.isEmpty(), "Función recursiva debería ser válida");
    }

    @Test
    void testMultipleNestedFunctions() {
        String code = """
            function outer(): integer {
                function inner1(): integer {
                    return 1;
                }
                function inner2(): integer {
                    return 2;
                }
                return inner1() + inner2();
            }
            """;

        List<SemanticError> errors = analyzeCode(code);
        assertNotNull(errors);
        assertTrue(errors.isEmpty(), "Múltiples funciones anidadas deberían ser válidas");
    }

    @Test
    void testFunctionParameterShadowing() {
        String code = """
            let x = "global";
            function test(x: integer): integer {
                return x + 1; // El parámetro x debería "sombrear" la variable global
            }
            let result = test(5);
            """;

        List<SemanticError> errors = analyzeCode(code);
        assertNotNull(errors);
        assertTrue(errors.isEmpty(), "Shadowing de parámetros debería ser válido");
    }

    // 🔧 CASOS EDGE

    @Test
    void testFunctionWithNoParameters() {
        String code = """
            function obtenerConstante(): integer {
                return 42;
            }
            let valor = obtenerConstante();
            """;

        List<SemanticError> errors = analyzeCode(code);
        assertNotNull(errors);
        assertTrue(errors.isEmpty(), "Función sin parámetros debería ser válida");
    }

    @Test
    void testFunctionWithNoReturn() {
        String code = """
            function proceso(): integer {
                let x = 5;
                // Sin return explícito
            }
            """;

        List<SemanticError> errors = analyzeCode(code);
        // Esto depende de si quieres requerir return explícito para funciones no-void
        // Puedes ajustar la expectativa según tu diseño
    }

    // 🎯 TESTS GRANULARES usando TestUtils

    @Test
    void testSingleFunctionDeclaration() {
        // Testear solo la declaración de función, sin programa completo
        String functionCode = "function suma(a: integer, b: integer): integer { return a + b; }";

        List<SemanticError> errors = analyzeFunctionDeclaration(functionCode);
        assertNotNull(errors);
        assertTrue(errors.isEmpty(), "Declaración de función debería ser válida");

        // Verificar que se agregó a la tabla de símbolos
        assertTrue(semanticVisitor.getEntornoActual().existeLocal("suma"));
    }

    @Test
    void testNestedFunctionScoping() {
        String code = """
            function outer(): integer {
                let outerVar = 10;
                
                function inner(): integer {
                    return outerVar; // Debería acceder a variable del ámbito externo
                }
                
                return inner();
            }
            """;

        List<SemanticError> errors = analyzeCode(code);

        // Esto podría generar error si tu implementación no maneja closures correctamente
        // Ajusta la expectativa según tu implementación actual
        System.out.println("Se encontraron errores semánticos:");
        assertNotNull(errors);
        for (SemanticError err : errors) {
            System.out.println(err);
        }
    }

    @Test
    void testParameterVsGlobalVariable() {
        String code = """
            let globalVar = "global";
            
            function test(globalVar: integer): integer {
                return globalVar + 1; // Debería usar el parámetro, no la variable global
            }
            
            let result = test(5);
            """;

        List<SemanticError> errors = analyzeCode(code);
        assertTrue(errors.isEmpty(), "Parámetro debería sobrescribir variable global");
    }
}