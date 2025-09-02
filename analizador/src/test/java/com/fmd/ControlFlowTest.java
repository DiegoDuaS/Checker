package com.fmd;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import com.fmd.modules.SemanticError;
import com.fmd.CompiscriptParser;

@DisplayName("Tests para Control de Flujo")
public class ControlFlowTest {

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

    private void printErrors(List<SemanticError> errors) {
        if (!errors.isEmpty()) {
            System.out.println("=== ERRORES ENCONTRADOS ===");
            errors.forEach(error -> System.out.println("- " + error.getMensaje()));
            System.out.println("============================");
        }
    }

    // ========================================
    // IF/ELSE STATEMENTS - CASOS VÁLIDOS
    // ========================================

    @Test
    @DisplayName("If simple válido")
    void testValidSimpleIf() {
        String code = """
            let x: integer = 10;
            if (x > 5) {
                print("mayor que 5");
            }
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);
        assertTrue(errors.isEmpty(), "If simple con condición boolean válida no debería generar errores");
    }

    @Test
    @DisplayName("If-else válido")
    void testValidIfElse() {
        String code = """
            let x: integer = 3;
            if (x > 5) {
                print("mayor");
            } else {
                print("menor o igual");
            }
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);
        assertTrue(errors.isEmpty(), "If-else válido no debería generar errores");
    }

    @Test
    @DisplayName("If-else anidados válidos")
    void testValidNestedIfElse() {
        String code = """
            let score: integer = 85;
            if (score >= 90) {
                print("A");
            } else {
                if (score >= 80) {
                    print("B");
                } else {
                    print("C");
                }
            }
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);
        assertTrue(errors.isEmpty(), "If-else anidados válidos no deberían generar errores");
    }

    @Test
    @DisplayName("If con condiciones complejas válidas")
    void testValidComplexConditions() {
        String code = """
            let a: integer = 10;
            let b: integer = 20;
            let flag: boolean = true;
            
            if (a < b && flag) {
                print("condición compleja 1");
            }
            
            if ((a + b) > 25 || !flag) {
                print("condición compleja 2");
            }
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);
        assertTrue(errors.isEmpty(), "Condiciones complejas válidas no deberían generar errores");
    }

    // ========================================
    // IF/ELSE STATEMENTS - CASOS INVÁLIDOS
    // ========================================

    @Test
    @DisplayName("Error: If con condición no boolean")
    void testInvalidIfConditionInteger() {
        String code = """
            let x: integer = 10;
            if (x) {
                print("esto debería fallar");
            }
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        assertFalse(errors.isEmpty(), "If con condición integer debería generar error");

        boolean hasConditionError = errors.stream().anyMatch(error ->
                error.getMensaje().toLowerCase().contains("condición") ||
                        error.getMensaje().toLowerCase().contains("debe ser boolean") ||
                        error.getMensaje().contains("integer"));
        assertTrue(hasConditionError, "Debería reportar error de condición no boolean");
    }

    @Test
    @DisplayName("Error: If con condición string")
    void testInvalidIfConditionString() {
        String code = """
            let mensaje: string = "hola";
            if (mensaje) {
                print("esto debería fallar");
            }
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        assertFalse(errors.isEmpty(), "If con condición string debería generar error");

        boolean hasConditionError = errors.stream().anyMatch(error ->
                error.getMensaje().toLowerCase().contains("condición") ||
                        error.getMensaje().contains("string"));
        assertTrue(hasConditionError, "Debería reportar error de condición string");
    }

    // ========================================
    // WHILE LOOPS - CASOS VÁLIDOS
    // ========================================

    @Test
    @DisplayName("While loop válido")
    void testValidWhileLoop() {
        String code = """
            let i: integer = 0;
            while (i < 5) {
                i = i + 1;
            }
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);
        assertTrue(errors.isEmpty(), "While loop válido no debería generar errores");
    }

    @Test
    @DisplayName("While con condición compleja válida")
    void testValidComplexWhileCondition() {
        String code = """
            let x: integer = 10;
            let y: integer = 20;
            let continuar: boolean = true;
            
            while (x < y && continuar) {
                x = x + 1;
                if (x > 15) {
                    continuar = false;
                }
            }
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);
        assertTrue(errors.isEmpty(), "While con condición compleja válida no debería generar errores");
    }

    @Test
    @DisplayName("While anidados válidos")
    void testValidNestedWhile() {
        String code = """
            let i: integer = 0;
            let j: integer = 0;
            
            while (i < 3) {
                j = 0;
                while (j < 2) {
                    j = j + 1;
                }
                i = i + 1;
            }
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);
        assertTrue(errors.isEmpty(), "While anidados válidos no deberían generar errores");
    }

    // ========================================
    // WHILE LOOPS - CASOS INVÁLIDOS
    // ========================================

    @Test
    @DisplayName("Error: While con condición no boolean")
    void testInvalidWhileCondition() {
        String code = """
            let contador: integer = 5;
            while (contador) {
                contador = contador - 1;
            }
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        assertFalse(errors.isEmpty(), "While con condición integer debería generar error");

        boolean hasConditionError = errors.stream().anyMatch(error ->
                error.getMensaje().toLowerCase().contains("condición") ||
                        error.getMensaje().toLowerCase().contains("debe ser boolean"));
        assertTrue(hasConditionError, "Debería reportar error de condición no boolean en while");
    }

    // ========================================
    // DO-WHILE LOOPS - CASOS VÁLIDOS
    // ========================================

    @Test
    @DisplayName("Do-while válido")
    void testValidDoWhileLoop() {
        String code = """
            let x: integer = 0;
            do {
                x = x + 1;
            } while (x < 3);
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);
        assertTrue(errors.isEmpty(), "Do-while válido no debería generar errores");
    }

    @Test
    @DisplayName("Do-while con condición false (ejecuta al menos una vez)")
    void testValidDoWhileFalseCondition() {
        String code = """
            let ejecutado: boolean = false;
            do {
                ejecutado = true;
            } while (false);
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);
        assertTrue(errors.isEmpty(), "Do-while con condición false válida no debería generar errores");
    }

    // ========================================
    // DO-WHILE LOOPS - CASOS INVÁLIDOS
    // ========================================

    @Test
    @DisplayName("Error: Do-while con condición no boolean")
    void testInvalidDoWhileCondition() {
        String code = """
            let contador: integer = 3;
            do {
                contador = contador - 1;
            } while (contador);
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        assertFalse(errors.isEmpty(), "Do-while con condición integer debería generar error");

        boolean hasConditionError = errors.stream().anyMatch(error ->
                error.getMensaje().toLowerCase().contains("condición") ||
                        error.getMensaje().toLowerCase().contains("debe ser boolean"));
        assertTrue(hasConditionError, "Debería reportar error de condición no boolean en do-while");
    }

    // ========================================
    // FOR LOOPS - CASOS VÁLIDOS
    // ========================================

    @Test
    @DisplayName("For loop básico válido")
    void testValidBasicForLoop() {
        String code = """
            for (let i: integer = 0; i < 5; i = i + 1) {
                print(i);
            }
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);
        assertTrue(errors.isEmpty(), "For loop básico válido no debería generar errores");
    }

    @Test
    @DisplayName("For loop con variable externa válido")
    void testValidForLoopExternalVariable() {
        String code = """
            let contador: integer = 0;
            for (contador = 0; contador < 10; contador = contador + 2) {
                print(contador);
            }
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);
        assertTrue(errors.isEmpty(), "For con variable externa válido no debería generar errores");
    }

    @Test
    @DisplayName("For loops anidados válidos")
    void testValidNestedForLoops() {
        String code = """
            for (let i: integer = 0; i < 3; i = i + 1) {
                for (let j: integer = 0; j < 2; j = j + 1) {
                    print(i * j);
                }
            }
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);
        assertTrue(errors.isEmpty(), "For loops anidados válidos no deberían generar errores");
    }

    // ========================================
    // FOR LOOPS - CASOS INVÁLIDOS
    // ========================================

    @Test
    @DisplayName("Error: For con condición no boolean")
    void testInvalidForCondition() {
        String code = """
            for (let i: integer = 0; i; i = i + 1) {
                print(i);
            }
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        assertFalse(errors.isEmpty(), "For con condición integer debería generar error");

        boolean hasConditionError = errors.stream().anyMatch(error ->
                error.getMensaje().toLowerCase().contains("condición") ||
                        error.getMensaje().toLowerCase().contains("debe ser boolean"));
        assertTrue(hasConditionError, "Debería reportar error de condición no boolean en for");
    }

    @Test
    @DisplayName("Error: For con inicialización de tipo incorrecto")
    void testInvalidForInitialization() {
        String code = """
        for (let i: int = "0"; i != "5"; i = i + "1") {
            print(i);
        }
        """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        // Validar errores esperados
        List<String> expectedMessages = List.of(
                "No se puede inicializar variable 'i' de tipo 'int' con expresión de tipo 'string'",
                "No se pueden comparar tipos incompatibles: 'int' != 'string'",
                "Operación '+' no válida entre tipos: 'int' y 'string'",
                "No se puede inicializar variable 'i' de tipo 'int' con expresión de tipo 'desconocido'"
        );

        for (String msg : expectedMessages) {
            assertTrue(errors.stream().anyMatch(e -> e.getMensaje().equals(msg)),
                    "Se esperaba el error: " + msg);
        }

        assertEquals(expectedMessages.size(), errors.size(), "Debe generarse exactamente el número esperado de errores");
    }


    // ========================================
    // FOREACH LOOPS - CASOS VÁLIDOS
    // ========================================

    @Test
    @DisplayName("Foreach con arreglo válido")
    void testValidForeachArray() {
        String code = """
        let numeros: integer[] = [1, 2, 3, 4, 5];
        foreach (numero in numeros) {
            print(numero);
        }
        """;

        List<SemanticError> errors = analyzeCode(code);

        // Imprimir errores si los hay (útil para debug)
        printErrors(errors);

        // Validar que no se generen errores
        assertTrue(errors.isEmpty(), "Foreach con arreglo válido no debería generar errores");
    }

    @Test
    @DisplayName("Foreach con arreglo de strings válido")
    void testValidForeachStringArray() {
        String code = """
        let nombres: string[] = ["Ana", "Luis", "Pedro"];
        foreach (nombre in nombres) {
            print(nombre);
        }
        """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);
        assertTrue(errors.isEmpty(), "Foreach con arreglo de strings no debería generar errores");
    }

    @Test
    @DisplayName("Foreach accediendo variable externa")
    void testForeachWithOuterVariable() {
        String code = """
        let total: integer = 0;
        let numeros: integer[] = [1,2,3];
        foreach (n in numeros) {
            total = total + n;
        }
        print(total);
        """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);
        assertTrue(errors.isEmpty(), "Foreach debería poder modificar variables externas al bloque");
    }



    // ========================================
    // FOREACH LOOPS - CASOS INVALIDOS
    // ========================================

    @Test
    @DisplayName("Foreach con iterable inválido")
    void testInvalidForeachIterable() {
        String code = """
        let numero: integer = 5;
        foreach (n in numero) {
            print(n);
        }
        """;

        List<SemanticError> errors = analyzeCode(code);

        // Imprimir errores para debug
        printErrors(errors);

        // Validar que se haya detectado al menos un error
        assertFalse(errors.isEmpty(), "Foreach con tipo no iterable debería generar errores");

        // Validaciones específicas de los errores esperados
        assertTrue(errors.stream().anyMatch(e -> e.getMensaje().contains("No se puede iterar")),
                "Se esperaba error de tipo no iterable");
    }

    // ========================================
    // BREAK Y CONTINUE - CASOS VÁLIDOS
    // ========================================

    @Test
    @DisplayName("Break en while válido")
    void testValidBreakInWhile() {
        String code = """
            let i: integer = 0;
            while (true) {
                if (i >= 5) {
                    break;
                }
                i = i + 1;
            }
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);
        assertTrue(errors.isEmpty(), "Break en while válido no debería generar errores");
    }

    @Test
    @DisplayName("Continue en for válido")
    void testValidContinueInFor() {
        String code = """
            for (let i: integer = 0; i < 10; i = i + 1) {
                if (i == 5) {
                    continue;
                }
                print(i);
            }
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);
        assertTrue(errors.isEmpty(), "Continue en for válido no debería generar errores");
    }

    // ========================================
    // BREAK Y CONTINUE - CASOS INVÁLIDOS
    // ========================================

    @Test
    @DisplayName("Error: Break fuera de loop")
    void testInvalidBreakOutsideLoop() {
        String code = """
            let x: integer = 5;
            if (x > 3) {
                break; // Error: break fuera de loop
            }
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        assertFalse(errors.isEmpty(), "Break fuera de loop debería generar error");

        boolean hasBreakError = errors.stream().anyMatch(error ->
                error.getMensaje().toLowerCase().contains("break") &&
                        (error.getMensaje().toLowerCase().contains("fuera") ||
                                error.getMensaje().toLowerCase().contains("loop") ||
                                error.getMensaje().toLowerCase().contains("bucle")));
        assertTrue(hasBreakError, "Debería reportar error de break fuera de loop");
    }

    @Test
    @DisplayName("Error: Continue fuera de loop")
    void testInvalidContinueOutsideLoop() {
        String code = """
            let x: integer = 5;
            if (x > 3) {
                continue; // Error: continue fuera de loop
            }
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        assertFalse(errors.isEmpty(), "Continue fuera de loop debería generar error");

        boolean hasContinueError = errors.stream().anyMatch(error ->
                error.getMensaje().toLowerCase().contains("continue") &&
                        (error.getMensaje().toLowerCase().contains("fuera") ||
                                error.getMensaje().toLowerCase().contains("loop") ||
                                error.getMensaje().toLowerCase().contains("bucle")));
        assertTrue(hasContinueError, "Debería reportar error de continue fuera de loop");
    }

    // ========================================
    // SWITCH/CASE - CASOS VÁLIDOS
    // ========================================

    @Test
    @DisplayName("Switch básico válido")
    void testValidBasicSwitch() {
        String code = """
            let x: integer = 2;
            switch (x) {
                case 1:
                    print("uno");
                case 2:
                    print("dos");
                default:
                    print("otro");
            }
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);
        assertTrue(errors.isEmpty(), "Switch básico válido no debería generar errores");
    }

    @Test
    @DisplayName("Switch con break válido")
    void testValidSwitchWithBreak() {
        String code = """
            let valor: integer = 1;
            switch (valor) {
                case 1:
                    print("primero");
                    break;
                case 2:
                    print("segundo");
                    break;
                default:
                    print("default");
            }
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);
        assertTrue(errors.isEmpty(), "Switch con breaks válido no debería generar errores");
    }

    @Test
    @DisplayName("Switch con string válido")
    void testValidSwitchWithString() {
        String code = """
            let dia: string = "lunes";
            switch (dia) {
                case "lunes":
                    print("inicio de semana");
                case "viernes":
                    print("fin de semana");
                default:
                    print("día normal");
            }
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);
        assertTrue(errors.isEmpty(), "Switch con strings válido no debería generar errores");
    }

    // ========================================
    // SWITCH/CASE - CASOS INVÁLIDOS
    // ========================================

    @Test
    @DisplayName("Error: Switch con tipos de case inconsistentes")
    void testInvalidSwitchInconsistentCaseTypes() {
        String code = """
            let x: integer = 1;
            switch (x) {
                case 1:
                    print("entero");
                case "dos":  // Error: tipo inconsistente
                    print("string");
            }
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        assertFalse(errors.isEmpty(), "Switch con tipos de case inconsistentes debería generar error");

        boolean hasTypeError = errors.stream().anyMatch(error ->
                error.getMensaje().toLowerCase().contains("tipo") ||
                        error.getMensaje().toLowerCase().contains("case") ||
                        error.getMensaje().toLowerCase().contains("no coincide"));
        assertTrue(hasTypeError, "Debería reportar error de tipos inconsistentes en cases");
    }

    // ========================================
    // TRY/CATCH - CASOS VÁLIDOS
    // ========================================

    @Test
    @DisplayName("Try-catch básico válido")
    void testValidBasicTryCatch() {
        String code = """
            try {
                let riesgo: integer = 10 / 1;
            } catch (error) {
                print("Error capturado: " + error);
            }
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);
        assertTrue(errors.isEmpty(), "Try-catch básico válido no debería generar errores");
    }

    @Test
    @DisplayName("Try-catch con acceso a arreglo válido")
    void testValidTryCatchArrayAccess() {
        String code = """
            let lista: integer[] = [1, 2, 3];
            try {
                let elemento: integer = lista[100];
            } catch (err) {
                print("Índice fuera de rango");
            }
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        assertTrue(errors.isEmpty(), "Try-catch con acceso a arreglo válido no debería generar errores");
    }

    // ========================================
    // CASOS COMPLEJOS DE INTEGRACIÓN
    // ========================================

    @Test
    @DisplayName("Control de flujo mixto complejo válido")
    void testValidComplexControlFlow() {
        String code = """
            let numeros: integer[] = [1, 2, 3, 4, 5];
            let suma: integer = 0;
            
            for (let i: integer = 0; i < 5; i = i + 1) {
                if (numeros[i] > 3) {
                    continue;
                }
                
                suma = suma + numeros[i];
                
                if (suma > 10) {
                    break;
                }
            }
            
            if (suma > 0) {
                print("Suma final: " + suma);
            } else {
                print("Sin suma");
            }
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        assertTrue(errors.isEmpty(), "Control de flujo complejo válido no debería generar errores");
    }

    @Test
    @DisplayName("Loops anidados con break/continue válidos")
    void testValidNestedLoopsWithBreakContinue() {
        String code = """
            for (let i: integer = 0; i < 3; i = i + 1) {
                for (let j: integer = 0; j < 3; j = j + 1) {
                    if (i == j) {
                        continue; // Continue del loop interno
                    }
                    
                    if (i + j > 3) {
                        break; // Break del loop interno
                    }
                    
                    print(i * j);
                }
            }
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);
        assertTrue(errors.isEmpty(), "Loops anidados con break/continue válidos no deberían generar errores");
    }

    // ========================================
    // ÁMBITOS Y BLOQUES
    // ========================================

    @Test
    @DisplayName("Ámbitos en bloques de control válidos")
    void testValidControlBlockScoping() {
        String code = """
            let x: integer = 1;
            
            if (true) {
                let x: integer = 2; // Variable local al bloque if
                print(x); // Debería imprimir 2
            }
            
            print(x); // Debería imprimir 1
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);
        assertTrue(errors.isEmpty(), "Ámbitos en bloques de control válidos no deberían generar errores");
    }

    @Test
    @DisplayName("Variables de loop con ámbito correcto")
    void testValidLoopVariableScoping() {
        String code = """
            for (let i: integer = 0; i < 2; i = i + 1) {
                let temp: integer = i * 2;
                print(temp);
            }
            
            // i y temp no deberían ser accesibles aquí
            for (let i: integer = 10; i < 12; i = i + 1) {
                print(i); // Nueva variable i, diferente de la anterior
            }
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);
        assertTrue(errors.isEmpty(), "Variables de loop con ámbito correcto no deberían generar errores");
    }
}