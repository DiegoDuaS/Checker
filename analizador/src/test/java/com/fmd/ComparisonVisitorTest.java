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

@DisplayName("Tests para Operaciones de Comparación")
public class ComparisonVisitorTest {

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
    // TESTS VÁLIDOS DE IGUALDAD - NO DEBEN GENERAR ERRORES
    // ========================================

    @Test
    @DisplayName("Comparaciones de igualdad válidas - mismo tipo")
    void testValidEqualityComparisons() {
        String code = """
            let num1: integer = 10;
            let num2: integer = 20;
            let num3: integer = 10;
            let text1: string = "hello";
            let text2: string = "world";
            let text3: string = "hello";
            let flag1: boolean = true;
            let flag2: boolean = false;
            let flag3: boolean = true;
            
            let eq1: boolean = num1 == num3;
            let eq2: boolean = num1 == num2;
            let eq3: boolean = text1 == text3;
            let eq4: boolean = text1 == text2;
            let eq5: boolean = flag1 == flag3;
            let eq6: boolean = flag1 == flag2;
            """;

        List<SemanticError> errors = analyzeCode(code);
        if (!errors.isEmpty()) {
            printErrors(errors);
        }
        assertTrue(errors.isEmpty(), "No debería haber errores en comparaciones de igualdad válidas");
    }

    @Test
    @DisplayName("Comparaciones de desigualdad válidas - mismo tipo")
    void testValidInequalityComparisons() {
        String code = """
            let num1: integer = 10;
            let num2: integer = 20;
            let num3: integer = 10;
            let text1: string = "hello";
            let text2: string = "world";
            let text3: string = "hello";
            let flag1: boolean = true;
            let flag3: boolean = true;
            
            let neq1: boolean = num1 != num2;
            let neq2: boolean = num1 != num3;
            let neq3: boolean = text1 != text2;
            let neq4: boolean = text1 != text3;
            let neq5: boolean = flag1 != flag3;
            """;

        List<SemanticError> errors = analyzeCode(code);
        if (!errors.isEmpty()) {
            printErrors(errors);
        }
        assertTrue(errors.isEmpty(), "No debería haber errores en comparaciones de desigualdad válidas");
    }

    @Test
    @DisplayName("Comparaciones válidas con null (usando literales)")
    void testValidNullComparisons() {
        String code = """
            let num1: integer = 10;
            let text1: string = "hello";
            let flag1: boolean = true;
            
            let null2: boolean = num1 == null;
            let null3: boolean = text1 == null;
            let null4: boolean = flag1 == null;
            let null5: boolean = num1 != null;
            let null6: boolean = text1 != null;
            """;

        List<SemanticError> errors = analyzeCode(code);
        if (!errors.isEmpty()) {
            printErrors(errors);
        }
        assertTrue(errors.isEmpty(), "No debería haber errores en comparaciones con null");
    }

    // ========================================
    // TESTS VÁLIDOS RELACIONALES - NO DEBEN GENERAR ERRORES
    // ========================================

    @Test
    @DisplayName("Comparaciones relacionales válidas con integers")
    void testValidRelationalComparisonsIntegers() {
        String code = """
            let num1: integer = 10;
            let num2: integer = 20;
            let num3: integer = 10;
            
            let rel1: boolean = num1 < num2;
            let rel2: boolean = num2 > num1;
            let rel3: boolean = num1 <= num3;
            let rel4: boolean = num2 >= num1;
            let rel5: boolean = num1 <= num2;
            let rel6: boolean = num2 >= num3;
            """;

        List<SemanticError> errors = analyzeCode(code);
        if (!errors.isEmpty()) {
            printErrors(errors);
        }
        assertTrue(errors.isEmpty(), "No debería haber errores en comparaciones relacionales con integers");
    }

    @Test
    @DisplayName("Comparaciones relacionales válidas con strings")
    void testValidRelationalComparisonsStrings() {
        String code = """
            let text1: string = "hello";
            let text2: string = "world";
            let text3: string = "hello";
            
            let strRel1: boolean = text1 < text2;
            let strRel2: boolean = text2 > text1;
            let strRel3: boolean = text1 <= text3;
            let strRel4: boolean = text1 >= text3;
            """;

        List<SemanticError> errors = analyzeCode(code);
        if (!errors.isEmpty()) {
            printErrors(errors);
        }
        assertTrue(errors.isEmpty(), "No debería haber errores en comparaciones relacionales con strings");
    }

    @Test
    @DisplayName("Comparaciones con literales válidas")
    void testValidLiteralComparisons() {
        String code = """
            let num1: integer = 10;
            let text1: string = "hello";
            
            let lit1: boolean = 5 < 10;
            let lit2: boolean = "abc" <= "def";
            let lit3: boolean = num1 == 10;
            let lit4: boolean = text1 != "goodbye";
            let lit5: boolean = 15 > num1;
            """;

        List<SemanticError> errors = analyzeCode(code);
        if (!errors.isEmpty()) {
            printErrors(errors);
        }
        assertTrue(errors.isEmpty(), "No debería haber errores en comparaciones con literales");
    }

    // ========================================
    // TESTS INVÁLIDOS DE IGUALDAD - DEBEN GENERAR ERRORES
    // ========================================

    @Test
    @DisplayName("Error: Comparación de igualdad entre integer y string")
    void testEqualityIntegerString() {
        String code = """
            let num1: integer = 10;
            let text1: string = "hello";
            let error: boolean = num1 == text1;
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        assertFalse(errors.isEmpty(), "Debería generar error por integer == string");

        boolean foundError = errors.stream().anyMatch(error ->
                error.getMensaje().toLowerCase().contains("comparar") ||
                        error.getMensaje().contains("==") ||
                        (error.getMensaje().contains("integer") && error.getMensaje().contains("string")));
        assertTrue(foundError, "Debería encontrar error específico de tipos incompatibles en igualdad. " +
                "Errores: " + errors.stream().map(SemanticError::getMensaje).toList());
    }

    @Test
    @DisplayName("Error: Comparación de igualdad entre boolean y integer")
    void testEqualityBooleanInteger() {
        String code = """
            let flag1: boolean = true;
            let num1: integer = 10;
            let error: boolean = flag1 == num1;
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        assertFalse(errors.isEmpty(), "Debería generar error por boolean == integer");

        boolean foundError = errors.stream().anyMatch(error ->
                error.getMensaje().toLowerCase().contains("comparar") ||
                        error.getMensaje().contains("==") ||
                        (error.getMensaje().contains("boolean") && error.getMensaje().contains("integer")));
        assertTrue(foundError, "Debería encontrar error específico de tipos incompatibles en igualdad");
    }

    @Test
    @DisplayName("Error: Comparación de desigualdad entre string y boolean")
    void testInequalityStringBoolean() {
        String code = """
            let text1: string = "hello";
            let flag1: boolean = true;
            let error: boolean = text1 != flag1;
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        assertFalse(errors.isEmpty(), "Debería generar error por string != boolean");

        boolean foundError = errors.stream().anyMatch(error ->
                error.getMensaje().toLowerCase().contains("comparar") ||
                        error.getMensaje().contains("!=") ||
                        (error.getMensaje().contains("string") && error.getMensaje().contains("boolean")));
        assertTrue(foundError, "Debería encontrar error específico de tipos incompatibles en desigualdad");
    }

    // ========================================
    // TESTS INVÁLIDOS RELACIONALES - DEBEN GENERAR ERRORES
    // ========================================

    @Test
    @DisplayName("Error: Comparación relacional entre integer y string")
    void testRelationalIntegerString() {
        String code = """
            let num1: integer = 10;
            let text1: string = "hello";
            let error: boolean = num1 < text1;
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        assertFalse(errors.isEmpty(), "Debería generar error por integer < string");

        boolean foundError = errors.stream().anyMatch(error ->
                error.getMensaje().contains("<") ||
                        error.getMensaje().toLowerCase().contains("relacional") ||
                        (error.getMensaje().contains("integer") && error.getMensaje().contains("string")));
        assertTrue(foundError, "Debería encontrar error específico de operación relacional inválida");
    }

    @Test
    @DisplayName("Error: Comparación relacional entre boolean y boolean")
    void testRelationalBooleanBoolean() {
        String code = """
            let flag1: boolean = true;
            let flag2: boolean = false;
            let error: boolean = flag1 > flag2;
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        assertFalse(errors.isEmpty(), "Debería generar error por boolean > boolean");

        boolean foundError = errors.stream().anyMatch(error ->
                error.getMensaje().contains(">") ||
                        error.getMensaje().toLowerCase().contains("ordenable") ||
                        error.getMensaje().toLowerCase().contains("relacional") ||
                        error.getMensaje().contains("boolean"));
        assertTrue(foundError, "Debería encontrar error específico de tipo no ordenable");
    }

    @Test
    @DisplayName("Error: Comparaciones relacionales con tipos no ordenables")
    void testNonOrderableTypes() {
        String code = """
            let flag1: boolean = true;
            let flag2: boolean = false;
            
            let err1: boolean = flag1 <= flag2;
            let err2: boolean = flag1 >= flag2;
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        assertFalse(errors.isEmpty(), "Debería generar errores por tipos no ordenables");

        long orderableErrors = errors.stream()
                .filter(error ->
                        error.getMensaje().toLowerCase().contains("ordenable") ||
                                error.getMensaje().toLowerCase().contains("relacional") ||
                                error.getMensaje().contains("<=") ||
                                error.getMensaje().contains(">="))
                .count();
        assertTrue(orderableErrors >= 1, "Debería encontrar al menos un error de tipos no ordenables");
    }

    @Test
    @DisplayName("Error: Comparaciones relacionales mixtas")
    void testMixedRelationalErrors() {
        String code = """
            let num1: integer = 10;
            let text1: string = "hello";
            let flag1: boolean = true;
            
            let err1: boolean = text1 <= num1;
            let err2: boolean = num1 >= flag1;
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        assertFalse(errors.isEmpty(), "Debería generar múltiples errores por comparaciones mixtas");

        boolean hasRelationalErrors = errors.stream().anyMatch(error ->
                error.getMensaje().toLowerCase().contains("relacional") ||
                        error.getMensaje().contains("<=") ||
                        error.getMensaje().contains(">=") ||
                        error.getMensaje().toLowerCase().contains("tipos"));
        assertTrue(hasRelationalErrors, "Debería encontrar errores de operaciones relacionales inválidas");
    }

    // ========================================
    // TESTS DE CASOS EDGE Y COMPLEJOS SIMPLIFICADOS
    // ========================================

    @Test
    @DisplayName("Expresiones complejas válidas e inválidas por separado")
    void testMixedValidInvalidComplexExpressions() {
        // Primero probamos la expresión válida por separado
        String validCode = """
            let num1: integer = 10;
            let num2: integer = 20;
            let text1: string = "hello";
            let text3: string = "hello";
            
            let valid: boolean = (num1 < num2) && (text1 == text3);
            """;

        List<SemanticError> validErrors = analyzeCode(validCode);
        if (!validErrors.isEmpty()) {
            printErrors(validErrors);
        }
        assertTrue(validErrors.isEmpty(), "La expresión válida no debería generar errores");

        // Ahora probamos expresiones inválidas por separado
        String invalidCode1 = """
            let num1: integer = 10;
            let text1: string = "hello";
            let invalid: boolean = num1 == text1;
            """;

        List<SemanticError> invalidErrors1 = analyzeCode(invalidCode1);
        printErrors(invalidErrors1);

        assertFalse(invalidErrors1.isEmpty(), "La expresión inválida debería generar errores");

        boolean hasComparisonError = invalidErrors1.stream().anyMatch(error ->
                error.getMensaje().toLowerCase().contains("comparar") ||
                        error.getMensaje().contains("==") ||
                        error.getMensaje().toLowerCase().contains("tipos"));

        assertTrue(hasComparisonError, "Debería encontrar error de comparación inválida. " +
                "Errores: " + invalidErrors1.stream().map(SemanticError::getMensaje).toList());
    }

    @Test
    @DisplayName("Error: Uso incorrecto en estructuras de control")
    void testInvalidComparisonsInControlStructures() {
        String code = """
            let num1: integer = 10;
            let text1: string = "hello";
            
            if (num1 < text1) {
                print("This should not work");
            }
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        assertFalse(errors.isEmpty(), "Debería generar errores por comparaciones inválidas en estructuras de control");

        boolean hasComparisonError = errors.stream().anyMatch(error ->
                error.getMensaje().contains("<") ||
                        error.getMensaje().toLowerCase().contains("relacional") ||
                        error.getMensaje().toLowerCase().contains("tipos"));
        assertTrue(hasComparisonError, "Debería encontrar errores de comparaciones inválidas");
    }

    @Test
    @DisplayName("Test simple de incompatibilidad de tipos en igualdad")
    void testSimpleTypeIncompatibility() {
        String code = """
            let num: integer = 5;
            let text: string = "test";
            let result: boolean = num == text;
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        assertFalse(errors.isEmpty(), "Debería generar al menos un error por incompatibilidad de tipos");
    }

    @Test
    @DisplayName("Test simple de operación relacional inválida")
    void testSimpleInvalidRelational() {
        String code = """
            let flag1: boolean = true;
            let flag2: boolean = false;
            let result: boolean = flag1 < flag2;
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        assertFalse(errors.isEmpty(), "Debería generar error por operación relacional con tipos no ordenables");
    }
}