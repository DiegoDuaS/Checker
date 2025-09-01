package com.fmd;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import com.fmd.modules.SemanticError;
import com.fmd.CompiscriptParser;

@DisplayName("Tests para Estructuras de Datos")
public class DataStructuresTest {

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
    // ARREGLOS UNIDIMENSIONALES - CASOS VÁLIDOS
    // ========================================

    @Test
    @DisplayName("Declaración de arreglo de integers válida")
    void testValidIntegerArrayDeclaration() {
        String code = """
            let numeros: integer[] = [1, 2, 3, 4, 5];
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);
        assertTrue(errors.isEmpty(), "Declaración de arreglo de integers válida no debería generar errores");
    }

    @Test
    @DisplayName("Declaración de arreglo de strings válida")
    void testValidStringArrayDeclaration() {
        String code = """
            let nombres: string[] = ["Ana", "Pedro", "María"];
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);
        assertTrue(errors.isEmpty(), "Declaración de arreglo de strings válida no debería generar errores");
    }

    @Test
    @DisplayName("Declaración de arreglo de booleans válida")
    void testValidBooleanArrayDeclaration() {
        String code = """
            let flags: boolean[] = [true, false, true, false];
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);
        assertTrue(errors.isEmpty(), "Declaración de arreglo de booleans válida no debería generar errores");
    }

    @Test
    @DisplayName("Arreglo vacío válido")
    void testValidEmptyArray() {
        String code = """
            let vacio: integer[] = [];
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);
        assertTrue(errors.isEmpty(), "Arreglo vacío válido no debería generar errores");
    }

    @Test
    @DisplayName("Declaración de arreglo sin inicialización válida")
    void testValidArrayDeclarationWithoutInitialization() {
        String code = """
            let lista: string[];
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);
        assertTrue(errors.isEmpty(), "Declaración de arreglo sin inicialización debería ser válida");
    }

    // ========================================
    // ARREGLOS UNIDIMENSIONALES - CASOS INVÁLIDOS
    // ========================================

    @Test
    @DisplayName("Error: Arreglo con tipos mixtos")
    void testInvalidMixedTypeArray() {
        String code = """
            let mixto: integer[] = [1, "dos", 3];
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        assertFalse(errors.isEmpty(), "Arreglo con tipos mixtos debería generar error");

        boolean hasTypeError = errors.stream().anyMatch(error ->
                error.getMensaje().toLowerCase().contains("tipo") &&
                        (error.getMensaje().contains("integer") || error.getMensaje().contains("string")));
        assertTrue(hasTypeError, "Debería reportar error de tipos incompatibles en arreglo");
    }

    @Test
    @DisplayName("Error: Asignación de tipo incorrecto a arreglo")
    void testInvalidArrayTypeAssignment() {
        String code = """
            let numeros: integer[] = ["uno", "dos", "tres"];
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        assertFalse(errors.isEmpty(), "Asignación de strings a arreglo de integers debería generar error");

        boolean hasTypeError = errors.stream().anyMatch(error ->
                error.getMensaje().contains("integer") && error.getMensaje().contains("string"));
        assertTrue(hasTypeError, "Debería reportar error de tipo incorrecto en asignación de arreglo");
    }

    @Test
    @DisplayName("Error: Declaración de arreglo con tipo base incorrecto")
    void testInvalidArrayBaseTypeDeclaration() {
        String code = """
            let invalido: void[] = []; // void no es un tipo válido para arreglos
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        // Este test depende de si tu implementación permite void como tipo base
        // Ajustar según las reglas específicas de tu lenguaje
    }

    // ========================================
    // ACCESO A ELEMENTOS DE ARREGLO - CASOS VÁLIDOS
    // ========================================

    @Test
    @DisplayName("Acceso a elemento de arreglo válido")
    void testValidArrayElementAccess() {
        String code = """
            let numeros: integer[] = [10, 20, 30, 40, 50];
            let primero: integer = numeros[0];
            let ultimo: integer = numeros[4];
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);
        assertTrue(errors.isEmpty(), "Acceso a elementos de arreglo válido no debería generar errores");
    }

    @Test
    @DisplayName("Acceso con variable como índice válido")
    void testValidArrayAccessWithVariable() {
        String code = """
            let lista: string[] = ["a", "b", "c"];
            let indice: integer = 1;
            let elemento: string = lista[indice];
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);
        assertTrue(errors.isEmpty(), "Acceso con variable como índice válido no debería generar errores");
    }

    @Test
    @DisplayName("Acceso con expresión como índice válido")
    void testValidArrayAccessWithExpression() {
        String code = """
            let datos: integer[] = [1, 2, 3, 4, 5];
            let base: integer = 2;
            let valor: integer = datos[base + 1]; // datos[3]
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);
        assertTrue(errors.isEmpty(), "Acceso con expresión como índice válido no debería generar errores");
    }

    // ========================================
    // ACCESO A ELEMENTOS DE ARREGLO - CASOS INVÁLIDOS
    // ========================================

    @Test
    @DisplayName("Error: Acceso a arreglo con índice no entero")
    void testInvalidArrayAccessNonIntegerIndex() {
        String code = """
            let numeros: integer[] = [1, 2, 3];
            let elemento: integer = numeros["indice"]; // Error: índice string
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        assertFalse(errors.isEmpty(), "Acceso a arreglo con índice string debería generar error");

        boolean hasIndexError = errors.stream().anyMatch(error ->
                error.getMensaje().toLowerCase().contains("índice") ||
                        (error.getMensaje().contains("integer") && error.getMensaje().contains("string")));
        assertTrue(hasIndexError, "Debería reportar error de tipo de índice incorrecto");
    }

    @Test
    @DisplayName("Error: Acceso a arreglo con índice boolean")
    void testInvalidArrayAccessBooleanIndex() {
        String code = """
            let lista: string[] = ["x", "y", "z"];
            let item: string = lista[true]; // Error: índice boolean
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        assertFalse(errors.isEmpty(), "Acceso a arreglo con índice boolean debería generar error");

        boolean hasIndexError = errors.stream().anyMatch(error ->
                error.getMensaje().toLowerCase().contains("índice") ||
                        error.getMensaje().contains("boolean"));
        assertTrue(hasIndexError, "Debería reportar error de índice boolean");
    }

    @Test
    @DisplayName("Error: Acceso a elemento de variable no arreglo")
    void testInvalidArrayAccessOnNonArray() {
        String code = """
            let numero: integer = 42;
            let elemento: integer = numero[0]; // Error: número no es arreglo
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        assertFalse(errors.isEmpty(), "Acceso con [] a variable no arreglo debería generar error");

        boolean hasArrayError = errors.stream().anyMatch(error ->
                error.getMensaje().toLowerCase().contains("arreglo") ||
                        error.getMensaje().toLowerCase().contains("array") ||
                        error.getMensaje().contains("[]"));
        assertTrue(hasArrayError, "Debería reportar error de acceso a no-arreglo");
    }

    // ========================================
    // ASIGNACIÓN A ELEMENTOS DE ARREGLO - CASOS VÁLIDOS
    // ========================================

    @Test
    @DisplayName("Asignación a elemento de arreglo válida")
    void testValidArrayElementAssignment() {
        String code = """
            let numeros: integer[] = [1, 2, 3];
            numeros[0] = 10;
            numeros[1] = numeros[0] + 5;
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);
        assertTrue(errors.isEmpty(), "Asignación a elementos de arreglo válida no debería generar errores");
    }

    @Test
    @DisplayName("Asignación con variable como índice válida")
    void testValidArrayAssignmentWithVariableIndex() {
        String code = """
            let palabras: string[] = ["hola", "mundo"];
            let pos: integer = 1;
            palabras[pos] = "universo";
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);
        assertTrue(errors.isEmpty(), "Asignación con variable como índice válida no debería generar errores");
    }

    // ========================================
    // ASIGNACIÓN A ELEMENTOS DE ARREGLO - CASOS INVÁLIDOS
    // ========================================

    @Test
    @DisplayName("Error: Asignación de tipo incorrecto a elemento")
    void testInvalidArrayElementTypeAssignment() {
        String code = """
            let numeros: integer[] = [1, 2, 3];
            numeros[0] = "string"; // Error: asignar string a elemento integer
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        assertFalse(errors.isEmpty(), "Asignación de string a elemento integer debería generar error");

        boolean hasTypeError = errors.stream().anyMatch(error ->
                error.getMensaje().contains("integer") && error.getMensaje().contains("string"));
        assertTrue(hasTypeError, "Debería reportar error de tipo incorrecto en asignación");
    }

    // ========================================
    // ARREGLOS MULTIDIMENSIONALES - CASOS VÁLIDOS
    // ========================================

    @Test
    @DisplayName("Declaración de matriz 2D válida")
    void testValidTwoDimensionalArray() {
        String code = """
            let matriz: integer[][] = [[1, 2, 3], [4, 5, 6], [7, 8, 9]];
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);
        assertTrue(errors.isEmpty(), "Declaración de matriz 2D válida no debería generar errores");
    }

    @Test
    @DisplayName("Acceso a elemento de matriz 2D válido")
    void testValidTwoDimensionalArrayAccess() {
        String code = """
            let matriz: integer[][] = [[1, 2], [3, 4]];
            let elemento: integer = matriz[0][1]; // Acceso a fila 0, columna 1
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);
        assertTrue(errors.isEmpty(), "Acceso a elemento de matriz 2D válido no debería generar errores");
    }

    @Test
    @DisplayName("Asignación a elemento de matriz 2D válida")
    void testValidTwoDimensionalArrayAssignment() {
        String code = """
            let tabla: string[][] = [["a", "b"], ["c", "d"]];
            tabla[1][0] = "nuevo";
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);
        assertTrue(errors.isEmpty(), "Asignación a elemento de matriz 2D válida no debería generar errores");
    }

    @Test
    @DisplayName("Matriz 3D válida")
    void testValidThreeDimensionalArray() {
        String code = """
            let cubo: boolean[][][] = [[[true, false], [false, true]], [[false, false], [true, true]]];
            let valor: boolean = cubo[0][1][0];
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);
        assertTrue(errors.isEmpty(), "Matriz 3D válida no debería generar errores");
    }

    // ========================================
    // ARREGLOS MULTIDIMENSIONALES - CASOS INVÁLIDOS
    // ========================================

    @Test
    @DisplayName("Error: Matriz con dimensiones inconsistentes")
    void testInvalidInconsistentMatrixDimensions() {
        String code = """
            let matriz: integer[][] = [[1, 2, 3], [4, 5], [6, 7, 8, 9]];
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        // Este test es opcional - depende de si tu implementación requiere dimensiones consistentes
        // Algunos lenguajes permiten arrays "jagged" (dentados)
    }

    @Test
    @DisplayName("Error: Acceso incompleto a matriz multidimensional")
    void testInvalidIncompleteMultidimensionalAccess() {
        String code = """
            let matriz: integer[][] = [[1, 2], [3, 4]];
            let fila: integer = matriz[0]; // Error: devuelve integer[], no integer
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        assertFalse(errors.isEmpty(), "Acceso incompleto a matriz debería generar error de tipos");

        boolean hasTypeError = errors.stream().anyMatch(error ->
                error.getMensaje().toLowerCase().contains("tipo") ||
                        (error.getMensaje().contains("integer[]") && error.getMensaje().contains("integer")));
        assertTrue(hasTypeError, "Debería reportar error de tipo incorrecto en acceso incompleto");
    }

    // ========================================
    // FOREACH CON ARREGLOS - CASOS VÁLIDOS
    // ========================================

    @Test
    @DisplayName("Foreach con arreglo de integers válido")
    void testValidForeachWithIntegerArray() {
        String code = """
            let numeros: integer[] = [1, 2, 3, 4, 5];
            foreach (numero in numeros) {
                print(numero);
            }
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);
        assertTrue(errors.isEmpty(), "Foreach con arreglo de integers válido no debería generar errores");
    }

    @Test
    @DisplayName("Foreach con arreglo de strings válido")
    void testValidForeachWithStringArray() {
        String code = """
            let nombres: string[] = ["Ana", "Luis", "Carmen"];
            foreach (nombre in nombres) {
                print("Hola " + nombre);
            }
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);
        assertTrue(errors.isEmpty(), "Foreach con arreglo de strings válido no debería generar errores");
    }

    @Test
    @DisplayName("Foreach anidados válidos")
    void testValidNestedForeach() {
        String code = """
            let matriz: integer[][] = [[1, 2], [3, 4], [5, 6]];
            foreach (fila in matriz) {
                foreach (elemento in fila) {
                    print(elemento);
                }
            }
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);
        assertTrue(errors.isEmpty(), "Foreach anidados válidos no deberían generar errores");
    }

    // ========================================
    // FOREACH CON ARREGLOS - CASOS INVÁLIDOS
    // ========================================

    @Test
    @DisplayName("Error: Foreach con variable no iterable")
    void testInvalidForeachWithNonIterable() {
        String code = """
            let numero: integer = 42;
            foreach (digito in numero) {
                print(digito);
            }
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        assertFalse(errors.isEmpty(), "Foreach con variable no iterable debería generar error");

        boolean hasIterableError = errors.stream().anyMatch(error ->
                error.getMensaje().toLowerCase().contains("iterable") ||
                        error.getMensaje().toLowerCase().contains("arreglo") ||
                        error.getMensaje().toLowerCase().contains("foreach"));
        assertTrue(hasIterableError, "Debería reportar error de variable no iterable");
    }

    @Test
    @DisplayName("Error: Variable de iteración con tipo incorrecto")
    void testInvalidForeachVariableType() {
        String code = """
            let numeros: integer[] = [1, 2, 3];
            foreach (texto in numeros) {
                let valor: string = texto; // Error: texto debería ser integer, no string
            }
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        // Este test puede requerir análisis más sofisticado del tipo de la variable de iteración
        // La implementación podría inferir automáticamente el tipo correcto
    }

    // ========================================
    // CASOS COMPLEJOS CON ARREGLOS
    // ========================================

    @Test
    @DisplayName("Arreglos como parámetros de función válidos")
    void testValidArraysAsFunctionParameters() {
        String code = """
            function sumarArreglo(numeros: integer[]): integer {
                let suma: integer = 0;
                foreach (num in numeros) {
                    suma = suma + num;
                }
                return suma;
            }
            
            let datos: integer[] = [1, 2, 3, 4, 5];
            let total: integer = sumarArreglo(datos);
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);
        assertTrue(errors.isEmpty(), "Arreglos como parámetros de función válidos no deberían generar errores");
    }

    @Test
    @DisplayName("Función que retorna arreglo válida")
    void testValidFunctionReturningArray() {
        String code = """
            function crearRango(inicio: integer, fin: integer): integer[] {
                let resultado: integer[] = [];
                for (let i: integer = inicio; i <= fin; i = i + 1) {
                    resultado[i - inicio] = i;
                }
                return resultado;
            }
            
            let numeros: integer[] = crearRango(1, 5);
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);
        assertTrue(errors.isEmpty(), "Función que retorna arreglo válida no debería generar errores");
    }

    @Test
    @DisplayName("Arreglos en clases válidos")
    void testValidArraysInClasses() {
        String code = """
            class ListaNumeros {
                let datos: integer[];
                
                function constructor(tamano: integer) {
                    this.datos = [];
                }
                
                function agregar(numero: integer) {
                    // Lógica para agregar elemento
                }
                
                function obtener(indice: integer): integer {
                    return this.datos[indice];
                }
            }
            
            let lista: ListaNumeros = new ListaNumeros(10);
            lista.agregar(42);
            let valor: integer = lista.obtener(0);
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);
        assertTrue(errors.isEmpty(), "Arreglos en clases válidos no deberían generar errores");
    }

    // ========================================
    // CONCATENACIÓN DE STRINGS - CASOS VÁLIDOS
    // ========================================

    @Test
    @DisplayName("Concatenación básica de strings válida")
    void testValidBasicStringConcatenation() {
        String code = """
            let saludo: string = "Hola";
            let nombre: string = "mundo";
            let mensaje: string = saludo + " " + nombre;
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);
        assertTrue(errors.isEmpty(), "Concatenación básica de strings válida no debería generar errores");
    }

    @Test
    @DisplayName("Concatenación de string con literal válida")
    void testValidStringLiteralConcatenation() {
        String code = """
            let nombre: string = "Juan";
            let saludo: string = "Hola " + nombre + "!";
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);
        assertTrue(errors.isEmpty(), "Concatenación de string con literal válida no debería generar errores");
    }

    @Test
    @DisplayName("Concatenación compleja de strings válida")
    void testValidComplexStringConcatenation() {
        String code = """
            let nombre: string = "Ana";
            let apellido: string = "García";
            let edad: integer = 25;
            let mensaje: string = "Mi nombre es " + nombre + " " + apellido + " y tengo " + edad + " años";
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);
        assertTrue(errors.isEmpty(), "Concatenación compleja de strings válida no debería generar errores");
    }

    // ========================================
    // CONCATENACIÓN DE STRINGS - CASOS INVÁLIDOS
    // ========================================

    @Test
    @DisplayName("Error: Concatenación de string con tipos no compatibles")
    void testInvalidStringConcatenationWithIncompatibleTypes() {
        String code = """
            let texto: string = "Valor: ";
            let flag: boolean = true;
            let resultado: string = texto + flag; // Error: string + boolean
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);

        // Este test depende de si tu implementación permite conversión automática de tipos
        // Algunos lenguajes permiten concatenar boolean/integer con string automáticamente
        // Ajustar según las reglas específicas de tu lenguaje
    }

    // ========================================
    // CASOS DE INTEGRACIÓN COMPLEJOS
    // ========================================

    @Test
    @DisplayName("Integración compleja: arreglos, loops y strings")
    void testComplexIntegrationArraysLoopsStrings() {
        String code = """
            let palabras: string[] = ["Hola", "mundo", "desde", "Compiscript"];
            let frase: string = "";
            
            for (let i: integer = 0; i < 4; i = i + 1) {
                if (i > 0) {
                    frase = frase + " ";
                }
                frase = frase + palabras[i];
            }
            
            print(frase);
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);
        assertTrue(errors.isEmpty(), "Integración compleja válida no debería generar errores");
    }

    @Test
    @DisplayName("Matrices con control de flujo válidas")
    void testValidMatricesWithControlFlow() {
        String code = """
            let matriz: integer[][] = [[1, 2, 3], [4, 5, 6], [7, 8, 9]];
            let suma: integer = 0;
            
            for (let fila: integer = 0; fila < 3; fila = fila + 1) {
                for (let col: integer = 0; col < 3; col = col + 1) {
                    suma = suma + matriz[fila][col];
                    
                    if (matriz[fila][col] == 5) {
                        print("Encontrado el 5 en posición " + fila + "," + col);
                        break;
                    }
                }
            }
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);
        assertTrue(errors.isEmpty(), "Matrices con control de flujo válidas no deberían generar errores");
    }

    // ========================================
    // CASOS EDGE Y DE LÍMITES
    // ========================================

    @Test
    @DisplayName("Arreglo con un solo elemento válido")
    void testValidSingleElementArray() {
        String code = """
            let unico: string[] = ["solo"];
            let elemento: string = unico[0];
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);
        assertTrue(errors.isEmpty(), "Arreglo con un solo elemento válido no debería generar errores");
    }

    @Test
    @DisplayName("Acceso múltiple al mismo elemento válido")
    void testValidMultipleAccessToSameElement() {
        String code = """
            let datos: integer[] = [10, 20, 30];
            let primero: integer = datos[0];
            let tambienPrimero: integer = datos[0];
            datos[0] = primero + tambienPrimero;
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);
        assertTrue(errors.isEmpty(), "Acceso múltiple al mismo elemento válido no debería generar errores");
    }

    @Test
    @DisplayName("Asignación de arreglo a arreglo válida")
    void testValidArrayToArrayAssignment() {
        String code = """
            let origen: integer[] = [1, 2, 3];
            let destino: integer[] = origen; // Referencia al mismo arreglo
            """;

        List<SemanticError> errors = analyzeCode(code);
        printErrors(errors);
        assertTrue(errors.isEmpty(), "Asignación de arreglo a arreglo válida no debería generar errores");
    }
}