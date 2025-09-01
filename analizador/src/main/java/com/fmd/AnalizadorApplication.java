package com.fmd;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fmd.modules.Symbol;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

import com.fmd.modules.SemanticError;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@SpringBootApplication
public class AnalizadorApplication {
    public static void main(String[] args) {
        SpringApplication.run(AnalizadorApplication.class, args);
    }
}

@RestController
@RequestMapping("/compilar")
class AnalizadorController {

    @PostMapping
    public Map<String, Object> analizar(@RequestBody Map<String, String> body) throws Exception {
        String code = body.get("codigo");
        Map<String, Object> response = new HashMap<>();

        // 1. Crear lexer y parser
        CompiscriptLexer lexer = new CompiscriptLexer(CharStreams.fromString(code));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CompiscriptParser parser = new CompiscriptParser(tokens);

        // 2. Parsear
        ParseTree tree = parser.program();

        // 3. Semántico
        SemanticVisitor visitor = new SemanticVisitor();
        visitor.visit(tree);

        // 4. Guardar errores y símbolos
        List<SemanticError> errores = visitor.getErrores();
        List<Map<String, Object>> simbolos = visitor.getAllSymbols().values().stream()
                .map(sym -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", sym.getName());
                    map.put("type", sym.getType());
                    map.put("kind", sym.getKind());
                    map.put("line", sym.getLine());
                    map.put("column", sym.getColumn());
                    return map;
                })
                .toList();

        // 5. Ejecutar script de Python para generar imagen del árbol
        String treeString = tree.toStringTree(parser);
        String base64Img = generarImagen(treeString);

        response.put("errors", errores);
        response.put("symbols", simbolos);
        response.put("astImage", base64Img);

        return response;
    }

    public static String generarImagen(String treeString) {
        try {
            // Ejecuta un script Python que recibe el árbol y devuelve Base64
            ProcessBuilder pb = new ProcessBuilder("python", "additions\\AstTreeGenerator.py");
            Process p = pb.start();

            // Mandar el árbol al script
            p.getOutputStream().write(treeString.getBytes());
            p.getOutputStream().close();

            // Recibir errores
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String line;
            while ((line = errorReader.readLine()) != null) {
                System.err.println("[PYTHON ERROR] " + line);
            }

            // Esperar a que termine
            p.waitFor();

            // Leer el archivo generado
            byte[] bytes = Files.readAllBytes(Paths.get("ast_tree.png"));
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
