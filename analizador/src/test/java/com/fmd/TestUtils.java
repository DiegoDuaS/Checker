package com.fmd;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import com.fmd.CompiscriptBaseVisitor;
import com.fmd.CompiscriptParser;
import com.fmd.CompiscriptLexer;

public class TestUtils {

    public static ParseTree getParseTree(String input,
            java.util.function.Function<CompiscriptParser, ParseTree> startRule) {
        CharStream cs = CharStreams.fromString(input);
        CompiscriptLexer lexer = new CompiscriptLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CompiscriptParser parser = new CompiscriptParser(tokens);

        // startRule.apply(parser) permite elegir qué regla raíz usar
        return startRule.apply(parser);
    }
}
