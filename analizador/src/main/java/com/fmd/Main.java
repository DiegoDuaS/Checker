package com.fmd;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import java.nio.file.*;

public class Main {
    public static void main(String[] args) throws Exception {
        // 1. Leer archivo de entrada
        String inputFile = args.length > 0 ? args[0] : "src\\main\\java\\com\\fmd\\program.cps";
        String code = Files.readString(Path.of(inputFile));

        // 2. Crear lexer
        CompiscriptLexer lexer = new CompiscriptLexer(CharStreams.fromString(code));
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // 3. Crear parser
        CompiscriptParser parser = new CompiscriptParser(tokens);

        // 4. Invocar la regla inicial de la gramática
        ParseTree tree = parser.program(); // cambia "program" por tu regla inicial

        // 5. Imprimir árbol sintáctico REEMPLAZAR POR UNA MEJOR VISUALIZACION
        System.out.println(tree.toStringTree(parser));

        // 6. Análisis semántico
        // SemanticVisitor visitor = new SemanticVisitor();
        // visitor.visit(tree);
    }
}
