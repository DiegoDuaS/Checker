package com.fmd;

import java.nio.file.Files;
import java.nio.file.Path;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

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
        SemanticVisitor semanticVisitor = new SemanticVisitor();
        semanticVisitor.visit(tree);
    }
}
