package com.fmd;

import java.nio.file.Files;
import java.nio.file.Path;

import com.fmd.modules.Symbol;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import com.fmd.modules.SemanticError;

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

        // 4. Invocar la regla inicial
        ParseTree tree = parser.program();

        // 5. Opcional: ver árbol sintáctico
        System.out.println(tree.toStringTree(parser));

        // 6. Análisis semántico
        SemanticVisitor visitor = new SemanticVisitor();
        visitor.visit(tree);

        // 7. Mostrar errores
        if (visitor.getErrores().isEmpty()) {
            System.out.println("Análisis semántico completado sin errores.");
        } else {
            System.out.println("Se encontraron errores semánticos:");
            for (SemanticError err : visitor.getErrores()) {
                System.out.println(err);
            }
        }

        // 8. Mostrar tabla de simbolos
        System.out.println("\n Esta es la tabla de símbolos:");
        for (String sym : visitor.getAllSymbols().keySet()) {
            Symbol info = visitor.getAllSymbols().get(sym);
            System.out.println(sym + ": " + info.getType() );
        }
    }
}
