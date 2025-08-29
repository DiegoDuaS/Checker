package com.fmd.modules;

import org.antlr.v4.runtime.ParserRuleContext;
import java.util.ArrayList;
import java.util.List;

public class Symbol {
    public enum Kind {
        VARIABLE, CONSTANT, FUNCTION, CLASS
    }

    private final String name;
    private final Kind kind;
    private String type; // puede cambiar si se infiere luego
    private final ParserRuleContext declNode;
    private final int line;
    private final int column;
    private final boolean mutable;
    private final List<Symbol> params = new ArrayList<>(); // para funciones

    public Symbol(String name, Kind kind, String type, ParserRuleContext declNode, int line, int column,
            boolean mutable) {
        this.name = name;
        this.kind = kind;
        this.type = type;
        this.declNode = declNode;
        this.line = line;
        this.column = column;
        this.mutable = mutable;
    }

    // getters y setters básicos
    public String getName() {
        return name;
    }

    public Kind getKind() {
        return kind;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ParserRuleContext getDeclNode() {
        return declNode;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public boolean isMutable() {
        return mutable;
    }

    public List<Symbol> getParams() {
        return params;
    }

    @Override
    public String toString() {
        return kind + " " + name + ":" + type + " (line " + line + ":" + column + ")";
    }
}
