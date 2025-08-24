package com.fmd;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.antlr.v4.runtime.tree.ParseTree;

public class VariableVisitorTest {

    @Test
    void testDeclaracionVariableCorrecta() {
        String codigo = "var x: int;";
        ParseTree tree = TestUtils.getParseTree(codigo, p -> p.variableDeclaration());

        VariableVisitor visitor = new VariableVisitor();
        visitor.visit(tree);

        assertTrue(visitor.getTablaVariables().containsKey("x"));
        assertEquals("int", visitor.getTablaVariables().get("x"));
    }

    @Test
    void testConstanteSinInicializacion() {
        String codigo = "const y: float;";
        ParseTree tree = TestUtils.getParseTree(codigo, p -> p.constantDeclaration());

        VariableVisitor visitor = new VariableVisitor();

        // Capturar salida de error
        java.io.ByteArrayOutputStream outContent = new java.io.ByteArrayOutputStream();
        System.setErr(new java.io.PrintStream(outContent));

        visitor.visit(tree);

        String errores = outContent.toString();
        assertTrue(errores.contains("debe inicializarse"));
    }

    @Test
    void testAsignacionTipoInvalido() {
        String codigo = "var x: int; x = true;";
        ParseTree tree = TestUtils.getParseTree(codigo, p -> p.program());

        VariableVisitor visitor = new VariableVisitor();

        // Capturar errores
        java.io.ByteArrayOutputStream outContent = new java.io.ByteArrayOutputStream();
        System.setErr(new java.io.PrintStream(outContent));

        visitor.visit(tree);

        String errores = outContent.toString();
        assertTrue(errores.contains("incompatibilidad"));
    }
}
