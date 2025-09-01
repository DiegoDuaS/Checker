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

@DisplayName("Tests para Operaciones Lógicas")
public class LogicalVisitorTest {

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
    @DisplayName("Operaciones lógicas válidas con boolean")
    void testValidLogicalOperations() {
        String code = """
            let a: boolean = true;
            let b: boolean = false;
            let result1: boolean = a && b;
            let result2: boolean = a || b;
            let result3: boolean = !a;
            let result4: boolean = !b;
            """;

        List<SemanticError> errors = analyzeCode(code);
        if (!errors.isEmpty()) {
            printErrors(errors);
        }
        assertTrue(errors.isEmpty(), "No debería haber errores en operaciones lógicas válidas");
    }

    @Test
    @DisplayName("Operaciones lógicas con literales boolean")
    void testValidLogicalLiterals() {
        String code = """
            let result1: boolean = true && false;
            let result2: boolean = false || false;
            let result3: boolean = true || false;
            let result4: boolean = !true;
            let result5: boolean = !false;
            """;

        List<SemanticError> errors = analyzeCode(code);
        if (!errors.isEmpty()) {
            printErrors(errors);
        }
        assertTrue(errors.isEmpty(), "No debería haber errores con literales boolean");
    }

    @Test
    @DisplayName("Operaciones lógicas complejas válidas")
    void testValidComplexLogicalOperations() {
        String code = """
            let a: boolean = true;
            let b: boolean = false;
            let complex1: boolean = (a && b) || (!a && !b);
            let complex2: boolean = !((a || b) && (a || !b));
            let complex3: boolean = a && (b || true);
            let complex4: boolean = (a || b) && (b || !a);
            """;

        List<SemanticError> errors = analyzeCode(code);
        if (!errors.isEmpty()) {
            printErrors(errors);
        }
        assertTrue(errors.isEmpty(), "No debería haber errores en operaciones lógicas complejas válidas");
    }

    @Test
    @DisplayName("Uso válido en estructuras de control")
    void testValidLogicalInControlStructures() {
        String code = """
            let a: boolean = true;
            let b: boolean = false;
            
            if (a && b) {
                print("Both are true");
            }
            
            if (a || b) {
                print("At least one is true");
            }
            """;

        List<SemanticError> errors = analyzeCode(code);
        if (!errors.isEmpty()) {
            printErrors(errors);
        }

        // Filtrar solo errores relacionados con operaciones lógicas, no otros errores posibles
        long logicalErrors = errors.stream()
                .filter(error ->
                        error.getMensaje().toLowerCase().contains("operando") ||
                                error.getMensaje().contains("&&") ||
                                error.getMensaje().contains("||") ||
                                error.getMensaje().contains("!"))
                .count();

        assertEquals(0, logicalErrors, "No debería haber errores lógicos en estructuras de control válidas");
    }

    // ========================================
    // TESTS INVÁLIDOS - DEBEN GENERAR ERRORES
    // ========================================

    @Test
    @DisplayName("Error: integer && integer")
    void testIntegerAndInteger() {
        String code = """
            let x: integer = 5;
            let y: integer = 10;
            let error: boolean = x && y;
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        assertFalse(errors.isEmpty(), "Debería generar error por integer && integer");

        boolean foundError = errors.stream().anyMatch(error ->
                (error.getMensaje().toLowerCase().contains("operando") ||
                        error.getMensaje().toLowerCase().contains("debe ser boolean") ||
                        error.getMensaje().contains("&&")) &&
                        error.getMensaje().contains("integer"));
        assertTrue(foundError, "Debería encontrar error específico de operando && inválido. " +
                "Errores: " + errors.stream().map(SemanticError::getMensaje).toList());
    }

    @Test
    @DisplayName("Error: boolean && integer")
    void testBooleanAndInteger() {
        String code = """
            let a: boolean = true;
            let x: integer = 5;
            let error: boolean = a && x;
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        assertFalse(errors.isEmpty(), "Debería generar error por boolean && integer");

        boolean foundError = errors.stream().anyMatch(error ->
                error.getMensaje().contains("&&") ||
                        (error.getMensaje().toLowerCase().contains("operando") && error.getMensaje().contains("integer")));
        assertTrue(foundError, "Debería encontrar error específico de operando derecho");
    }

    @Test
    @DisplayName("Error: string && boolean")
    void testStringAndBoolean() {
        String code = """
            let name: string = "test";
            let a: boolean = true;
            let error: boolean = name && a;
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        assertFalse(errors.isEmpty(), "Debería generar error por string && boolean");

        boolean foundError = errors.stream().anyMatch(error ->
                error.getMensaje().contains("&&") ||
                        (error.getMensaje().toLowerCase().contains("operando") && error.getMensaje().contains("string")));
        assertTrue(foundError, "Debería encontrar error específico de operando izquierdo");
    }

    @Test
    @DisplayName("Error: integer || integer")
    void testIntegerOrInteger() {
        String code = """
            let x: integer = 5;
            let y: integer = 10;
            let error: boolean = x || y;
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        assertFalse(errors.isEmpty(), "Debería generar error por integer || integer");

        boolean foundError = errors.stream().anyMatch(error ->
                (error.getMensaje().toLowerCase().contains("operando") ||
                        error.getMensaje().toLowerCase().contains("debe ser boolean") ||
                        error.getMensaje().contains("||")) &&
                        error.getMensaje().contains("integer"));
        assertTrue(foundError, "Debería encontrar error específico de operando || inválido");
    }

    @Test
    @DisplayName("Error: string || boolean")
    void testStringOrBoolean() {
        String code = """
            let name: string = "test";
            let b: boolean = false;
            let error: boolean = name || b;
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        assertFalse(errors.isEmpty(), "Debería generar error por string || boolean");

        boolean foundError = errors.stream().anyMatch(error ->
                error.getMensaje().contains("||") ||
                        (error.getMensaje().toLowerCase().contains("operando") && error.getMensaje().contains("string")));
        assertTrue(foundError, "Debería encontrar error específico de operando izquierdo");
    }

    @Test
    @DisplayName("Error: integer || boolean")
    void testIntegerOrBoolean() {
        String code = """
            let x: integer = 5;
            let a: boolean = true;
            let error: boolean = x || a;
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        assertFalse(errors.isEmpty(), "Debería generar error por integer || boolean");

        boolean foundError = errors.stream().anyMatch(error ->
                error.getMensaje().contains("||") ||
                        (error.getMensaje().toLowerCase().contains("operando") && error.getMensaje().contains("integer")));
        assertTrue(foundError, "Debería encontrar error específico de operando izquierdo");
    }

    @Test
    @DisplayName("Error: !integer")
    void testNotInteger() {
        String code = """
            let x: integer = 5;
            let error: boolean = !x;
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        assertFalse(errors.isEmpty(), "Debería generar error por !integer");

        boolean foundError = errors.stream().anyMatch(error ->
                error.getMensaje().contains("!") && error.getMensaje().contains("integer"));
        assertTrue(foundError, "Debería encontrar error específico de operador ! con integer");
    }

    @Test
    @DisplayName("Error: !string")
    void testNotString() {
        String code = """
            let name: string = "test";
            let error: boolean = !name;
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        assertFalse(errors.isEmpty(), "Debería generar error por !string");

        boolean foundError = errors.stream().anyMatch(error ->
                error.getMensaje().contains("!") && error.getMensaje().contains("string"));
        assertTrue(foundError, "Debería encontrar error específico de operador ! con string");
    }

    @Test
    @DisplayName("Error: !literal_integer")
    void testNotLiteralInteger() {
        String code = """
            let error: boolean = !5;
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        assertFalse(errors.isEmpty(), "Debería generar error por !5");

        boolean foundError = errors.stream().anyMatch(error ->
                error.getMensaje().contains("!") && error.getMensaje().contains("integer"));
        assertTrue(foundError, "Debería encontrar error específico de operador ! con literal integer");
    }

    // ========================================
    // TESTS DE OPERACIONES MIXTAS CON ERRORES SIMPLIFICADOS
    // ========================================

    @Test
    @DisplayName("Error: Operaciones lógicas mixtas con tipos incorrectos - separadas")
    void testMixedLogicalOperationsWithErrors() {
        // Test 1: (x && y) donde x,y son integer
        String code1 = """
            let x: integer = 5;
            let y: integer = 10;
            let error1: boolean = x && y;
            """;

        List<SemanticError> errors1 = analyzeCode(code1);
        printErrors(errors1);
        assertFalse(errors1.isEmpty(), "Debería generar error por integer && integer");

        // Test 2: !integer
        String code2 = """
            let x: integer = 5;
            let error2: boolean = !x;
            """;

        List<SemanticError> errors2 = analyzeCode(code2);
        printErrors(errors2);
        assertFalse(errors2.isEmpty(), "Debería generar error por !integer");
    }

    @Test
    @DisplayName("Error: Uso incorrecto en estructuras de control")
    void testInvalidLogicalInControlStructures() {
        String code = """
            let x: integer = 5;
            
            if (x) {
                print("This should not work");
            }
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        assertFalse(errors.isEmpty(), "Debería generar errores por uso incorrecto en control structures");

        // Verificar errores de condiciones o de tipos
        boolean hasConditionError = errors.stream().anyMatch(error ->
                error.getMensaje().toLowerCase().contains("condición") ||
                        error.getMensaje().toLowerCase().contains("debe ser boolean") ||
                        error.getMensaje().contains("integer"));
        assertTrue(hasConditionError, "Debería encontrar error de condición o tipo inválido");
    }

    // ========================================
    // TESTS DE CASOS EDGE SIMPLIFICADOS
    // ========================================

    @Test
    @DisplayName("Múltiples operadores NOT con errores")
    void testMultipleNotOperatorsWithErrors() {
        String code = """
            let x: integer = 5;
            let name: string = "test";
            let error1: boolean = !x;
            let error2: boolean = !name;
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        assertFalse(errors.isEmpty(), "Debería generar múltiples errores con operador NOT");

        long notErrors = errors.stream()
                .filter(error -> error.getMensaje().contains("!"))
                .count();
        assertTrue(notErrors >= 1, "Debería encontrar al menos 1 error de operador NOT");
    }

    @Test
    @DisplayName("Cadenas de operadores lógicos con errores")
    void testLogicalOperatorChainsWithErrors() {
        String code = """
            let a: boolean = true;
            let x: integer = 5;
            let error: boolean = a && x;
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        assertFalse(errors.isEmpty(), "Debería generar errores en cadenas de operadores lógicos");

        boolean hasLogicalError = errors.stream().anyMatch(error ->
                error.getMensaje().contains("&&") ||
                        (error.getMensaje().toLowerCase().contains("operando") && error.getMensaje().contains("integer")));
        assertTrue(hasLogicalError, "Debería encontrar error en operador lógico");
    }

    @Test
    @DisplayName("Test simple de compatibilidad de tipos lógicos")
    void testSimpleLogicalTypeCompatibility() {
        String code = """
            let num: integer = 5;
            let flag: boolean = true;
            let result: boolean = num && flag;
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        assertFalse(errors.isEmpty(), "Debería generar al menos un error por incompatibilidad de tipos lógicos");
    }

    @Test
    @DisplayName("Test simple de operador NOT inválido")
    void testSimpleInvalidNotOperator() {
        String code = """
            let text: string = "hello";
            let result: boolean = !text;
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        assertFalse(errors.isEmpty(), "Debería generar error por operador NOT con tipo inválido");
    }
}