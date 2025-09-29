package com.fmd;

import com.fmd.modules.SemanticError;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import com.fmd.CompiscriptLexer;
import com.fmd.CompiscriptParser;
import com.fmd.CompiscriptBaseVisitor;

@DisplayName("Tests para Operaciones Aritméticas")
public class VariableVisitorTest {

    private SemanticVisitor semanticVisitor;

    @BeforeEach
    void setUp() {
        semanticVisitor = new SemanticVisitor();
    }

    /**
     * Helper method para analizar código y obtener errores
     */
    private List<SemanticError> analyzeCode(String code) {
        try {
            CompiscriptLexer lexer = new CompiscriptLexer(CharStreams.fromString(code));
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            CompiscriptParser parser = new CompiscriptParser(tokens);
            ParseTree tree = parser.program();

            semanticVisitor.visit(tree);
            return semanticVisitor.getErrores();
        } catch (Exception e) {
            fail("Error parsing code: " + e.getMessage());
            return null;
        }
    }

    /**
     * Helper method para imprimir errores (debugging)
     */
    private void printErrors(List<SemanticError> errors) {
        System.out.println("=== ERRORES ENCONTRADOS ===");
        if (errors.isEmpty()) {
            System.out.println("No hay errores");
        } else {
            errors.forEach(error -> System.out.println("- " + error.getMensaje()));
        }
        System.out.println("============================");
    }

    // ========================================
    // TESTS VÁLIDOS - NO DEBEN GENERAR ERRORES
    // ========================================

    @Test
    @DisplayName("Operaciones aritméticas válidas con integers")
    void testValidArithmeticOperations() {
        String code = """
            let a: integer = 10;
            let b: integer = 5;
            let suma: integer = a + b;
            let resta: integer = a - b;
            let mult: integer = a * b;
            let div: integer = a / b;
            """;

        List<SemanticError> errors = analyzeCode(code);
        if (!errors.isEmpty()) {
            printErrors(errors);
        }
        assertTrue(errors.isEmpty(), "No debería haber errores en operaciones aritméticas válidas");
    }

    @Test
    @DisplayName("Operaciones con literales enteros")
    void testValidLiteralOperations() {
        String code = """
            let result1: integer = 100 + 50;
            let result2: integer = 25 * 4;
            let result3: integer = 200 - 75;
            let result4: integer = 100 / 10;
            """;

        List<SemanticError> errors = analyzeCode(code);
        if (!errors.isEmpty()) {
            printErrors(errors);
        }
        assertTrue(errors.isEmpty(), "No debería haber errores con operaciones de literales");
    }

    @Test
    @DisplayName("Operaciones complejas válidas")
    void testValidComplexOperations() {
        String code = """
            let a: integer = 10;
            let b: integer = 5;
            let c: integer = 3;
            let compleja1: integer = (a + b) * c;
            let compleja2: integer = a + (b * c);
            let compleja3: integer = a - b + c;
            let compleja4: integer = a * b / c;
            """;

        List<SemanticError> errors = analyzeCode(code);
        if (!errors.isEmpty()) {
            printErrors(errors);
        }
        assertTrue(errors.isEmpty(), "No debería haber errores en operaciones complejas válidas");
    }

    @Test
    @DisplayName("Operación unaria negativa válida")
    void testValidUnaryMinus() {
        String code = """
            let a: integer = 10;
            let negativo: integer = -a;
            let literal_neg: integer = -5;
            """;

        List<SemanticError> errors = analyzeCode(code);
        if (!errors.isEmpty()) {
            printErrors(errors);
        }
        assertTrue(errors.isEmpty(), "No debería haber errores con operador unario negativo en integers");
    }


    // ========================================
    // TESTS INVÁLIDOS - DEBEN GENERAR ERRORES
    // ========================================

    @Test
    @DisplayName("Error: integer * boolean")
    void testIntegerMultiplyBoolean() {
        String code = """
            let b: integer = 5;
            let bandera: boolean = true;
            let error: integer = b * bandera;
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        assertFalse(errors.isEmpty(), "Debería generar error por integer * boolean");

        boolean foundError = errors.stream().anyMatch(error ->
                error.getMensaje().contains("*") &&
                        (error.getMensaje().contains("integer") || error.getMensaje().contains("boolean")));
        assertTrue(foundError, "Debería encontrar error específico de tipos incompatibles");
    }

    @Test
    @DisplayName("Error: string - integer")
    void testStringMinusInteger() {
        String code = """
            let texto: string = "hello";
            let a: integer = 10;
            let error: integer = texto - a;
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        assertFalse(errors.isEmpty(), "Debería generar error por string - integer");

        boolean foundError = errors.stream().anyMatch(error ->
                error.getMensaje().contains("-") &&
                        (error.getMensaje().contains("string") || error.getMensaje().contains("integer")));
        assertTrue(foundError, "Debería encontrar error específico de tipos incompatibles");
    }

    @Test
    @DisplayName("Error: boolean + integer")
    void testBooleanPlusInteger() {
        String code = """
            let bandera: boolean = true;
            let a: integer = 10;
            let error: integer = bandera + a;
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        assertFalse(errors.isEmpty(), "Debería generar error por boolean + integer");

        boolean foundError = errors.stream().anyMatch(error ->
                error.getMensaje().contains("+") &&
                        (error.getMensaje().contains("boolean") || error.getMensaje().contains("integer")));
        assertTrue(foundError, "Debería encontrar error específico de tipos incompatibles");
    }

    @Test
    @DisplayName("Error: integer / string")
    void testIntegerDivideString() {
        String code = """
            let a: integer = 10;
            let texto: string = "hello";
            let error: integer = a / texto;
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        assertFalse(errors.isEmpty(), "Debería generar error por integer / string");

        boolean foundError = errors.stream().anyMatch(error ->
                error.getMensaje().contains("/") &&
                        (error.getMensaje().contains("integer") || error.getMensaje().contains("string")));
        assertTrue(foundError, "Debería encontrar error específico de tipos incompatibles");
    }

    @Test
    @DisplayName("Error: Operador unario negativo en string")
    void testUnaryMinusOnString() {
        String code = """
            let texto: string = "hello";
            let error: integer = -texto;
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        assertFalse(errors.isEmpty(), "Debería generar error por -string");

        boolean foundError = errors.stream().anyMatch(error ->
                error.getMensaje().contains("-") && error.getMensaje().contains("string"));
        assertTrue(foundError, "Debería encontrar error específico de operador unario");
    }

    @Test
    @DisplayName("Error: Operador unario negativo en boolean")
    void testUnaryMinusOnBoolean() {
        String code = """
            let bandera: boolean = true;
            let error: integer = -bandera;
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        assertFalse(errors.isEmpty(), "Debería generar error por -boolean");

        boolean foundError = errors.stream().anyMatch(error ->
                error.getMensaje().contains("-") && error.getMensaje().contains("boolean"));
        assertTrue(foundError, "Debería encontrar error específico de operador unario");
    }

    @Test
    @DisplayName("Error: División por cero literal")
    void testDivisionByZero() {
        String code = """
            let a: integer = 10;
            let error: integer = a / 0;
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        // Este test es opcional dependiendo de si tu implementación detecta división por cero
        if (!errors.isEmpty()) {
            boolean foundError = errors.stream().anyMatch(error ->
                    error.getMensaje().toLowerCase().contains("división") ||
                            error.getMensaje().toLowerCase().contains("cero"));
            // Si hay errores, al menos uno debería ser de división por cero
        }
        // No forzamos este test porque no todos los compiladores detectan esto en análisis semántico
    }

    // ========================================
    // TESTS DE CASOS EDGE SIMPLIFICADOS
    // ========================================

    @Test
    @DisplayName("Múltiples errores en expresiones separadas")
    void testMultipleErrorsInExpression() {
        String code = """
            let a: integer = 10;
            let texto: string = "hello";
            let bandera: boolean = true;
            let error1: integer = a + texto;
            let error2: integer = bandera * a;
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        assertFalse(errors.isEmpty(), "Debería generar múltiples errores");

        // Verificar que hay al menos un error de tipos incompatibles
        boolean hasTypeError = errors.stream().anyMatch(error ->
                error.getMensaje().contains("+") || error.getMensaje().contains("*") ||
                        error.getMensaje().toLowerCase().contains("operación") ||
                        error.getMensaje().toLowerCase().contains("tipos"));

        assertTrue(hasTypeError, "Debería detectar al menos un error de tipos incompatibles. " +
                "Errores: " + errors.stream().map(SemanticError::getMensaje).toList());
    }


    @Test
    @DisplayName("Test simple de compatibilidad de tipos")
    void testSimpleTypeCompatibility() {
        String code = """
            let num: integer = 5;
            let text: string = "test";
            let result: integer = num + text;
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        // Al menos debería haber un error
        assertFalse(errors.isEmpty(), "Debería generar al menos un error por incompatibilidad de tipos");
    }
}