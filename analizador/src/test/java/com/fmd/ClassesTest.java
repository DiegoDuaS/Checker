package com.fmd;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fmd.modules.SemanticError;

public class ClassesTest {

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

    @Test
    void testClassDeclaration() {
        String code = """
            class Perro {
                var nombre: string = "Fido";
                const edad: integer = 5;
            }
        """;

        List<SemanticError> errors = analyzeCode(code);
        assertTrue(errors.isEmpty(), "No debería haber errores semánticos");
    }

    @Test
    void testDuplicateMemberError() {
        String code = """
            class Perro {
                var nombre: string = "Fido";
                var nombre: string = "Max";
            }
        """;

        List<SemanticError> errors = analyzeCode(code);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getMensaje().contains("ya declarado en la clase"));
    }

    @Test
    void testConstantMustBeInitialized() {
        String code = """
            class Perro {
                const edad: integer;
            }
        """;

        List<SemanticError> errors = analyzeCode(code);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getMensaje().contains("debe inicializarse"));
    }

    @Test
    void testAccessClassMember() {
        String code = """
            class Perro {
                var nombre: string = "Fido";
            }
            var dog: Perro = new Perro();
            print(dog.nombre);
        """;

        List<SemanticError> errors = analyzeCode(code);
        assertTrue(errors.isEmpty(), "Acceso a miembro debería ser correcto");
    }

    @Test
    void testAccessMemberWithoutInstance() {
        String code = """
            class Perro {
                var nombre: string = "Fido";
            }
            print(nombre);
        """;

        List<SemanticError> errors = analyzeCode(code);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getMensaje().contains("sin un objeto de tipo 'Perro'"));
    }

    @Test
    void testClassConstructorDeclaration() {
        String code = """
        class Animal {
            let name: string = "hugo";
            let casa: string;

            function constructor(name: string) {
                this.name = name;
            }

            function speak(): string {
                return this.name + " makes a sound.";
            }
        }
    """;

        List<SemanticError> errors = analyzeCode(code);
        assertTrue(errors.isEmpty(), "Declaración de clase y miembros no debería tener errores");
    }

    @Test
    void testConstructorInitialization() {
        String code = """
        class Animal {
            let name: string = "hugo";
            let casa: string;

            function constructor(name: string) {
                this.name = name;
            }
        }

        var pet: Animal = new Animal("Luis");
    """;

        List<SemanticError> errors = analyzeCode(code);
        errors.forEach(e -> System.out.println(e.getMensaje()));
        assertTrue(errors.isEmpty(), "Constructor debe inicializar correctamente 'name'");
    }

    @Test
    void testTypeMismatchInConstructor() {
        String code = """
        class Animal {
            let name: string;
            function constructor(name: string) {
                this.name = name;
            }
        }

        var pet: Animal = new Animal(5); // error de tipo
    """;

        List<SemanticError> errors = analyzeCode(code);
        assertEquals(1, errors.size(), "Constructor con tipo incompatible debería generar error");
        assertTrue(errors.get(0).getMensaje().contains("esperado:"));
    }

    @Test
    void testNewWithoutConstructor() {
        String code = """
    class Animal {
        let name: string = "hugo";
        let casa: string;
    }

    var pet: Animal = new Animal("Luis"); // Error: no hay constructor
    """;

        List<SemanticError> errors = analyzeCode(code);
        errors.forEach(e -> System.out.println(e.getMensaje()));

        // Debe haber al menos un error
        assertFalse(errors.isEmpty(), "Se esperaba error por llamar constructor inexistente");

        // Comprobamos que el mensaje contenga algo sobre constructor
        boolean hasConstructorError = errors.stream()
                .anyMatch(e -> e.getMensaje().contains("no tiene constructor definido"));
        assertTrue(hasConstructorError, "El error debe indicar que la clase no tiene constructor definido");
    }

    @Test
    void testMethodCallSpeak() {
        String code = """
    class Animal {
        let name: string = "hugo";

        function speak(): string {
            return this.name + " makes a sound.";
        }
    }

    var pet: Animal = new Animal();
    print(pet.speak());
    """;

        List<SemanticError> errors = analyzeCode(code);
        errors.forEach(e -> System.out.println(e.getMensaje()));

        // Debe estar libre de errores
        assertTrue(errors.isEmpty(), "Llamada al método 'speak' debe ser válida");
    }

    @Test
    void testMethodCallWithoutObject() {
        String code = """
    class Animal {
        let name: string = "hugo";

        function speak(): string {
            return this.name + " makes a sound.";
        }
    }

    print(pet.speak()); // Error: 'pet' no está declarado
    """;

        List<SemanticError> errors = analyzeCode(code);
        errors.forEach(e -> System.out.println(e.getMensaje()));

        // Debe haber al menos un error por variable no declarada
        assertFalse(errors.isEmpty(), "Se esperaba error por llamar método de variable inexistente");

        // Comprobamos que el mensaje contenga algo sobre variable no declarada
        boolean hasUndeclaredVarError = errors.stream()
                .anyMatch(e -> e.getMensaje().contains("no declarado"));
        assertTrue(hasUndeclaredVarError, "El error debe indicar que la variable no está declarada");
    }

    @Test
    void testMethodCallWithoutObjectReference() {
        String code = """
    class Animal {
        let name: string = "hugo";

        function speak(): string {
            return this.name + " makes a sound.";
        }
    }

    var pet: Animal = new Animal();
    print(speak()); // Error: método llamado sin objeto
    """;

        List<SemanticError> errors = analyzeCode(code);
        errors.forEach(e -> System.out.println(e.getMensaje()));

        // Debe haber al menos un error por llamada de método sin objeto
        assertFalse(errors.isEmpty(), "Se esperaba error por llamar método sin referencia a objeto");

        // Comprobamos que el mensaje contenga algo sobre contexto inválido o inexistente
        boolean hasInvalidCallError = errors.stream()
                .anyMatch(e -> e.getMensaje().contains("no se puede llamar sin su clase") || e.getMensaje().contains("inválido"));
        assertTrue(hasInvalidCallError, "El error debe indicar que el método no puede llamarse sin un objeto");
    }

    @Test
    void testHerenciaMetodo() {
        String code = """
    class Animal {
        let name: string = "hugo";
        let casa: string;

        function constructor(name: string) {
            this.name = name;
        }

        function speak(): string {
            return this.name + " makes a sound.";
        }
    }

    class Dog : Animal {
        let owner: string;
    }

    let dog: Dog = new Dog("Hugo");
    print(dog.speak());
    """;

        List<SemanticError> errors = analyzeCode(code);
        errors.forEach(e -> System.out.println(e.getMensaje()));

        // La llamada a speak() debe ser válida, porque Dog hereda de Animal
        assertTrue(errors.isEmpty(), "Método heredado 'speak' debe ser accesible desde Dog");
    }






}

