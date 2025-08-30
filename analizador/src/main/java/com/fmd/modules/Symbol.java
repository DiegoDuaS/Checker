package com.fmd.modules;

import org.antlr.v4.runtime.ParserRuleContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
    private List<Symbol> params = new ArrayList<>(); // para funciones
    private boolean mutable; // para variables/constantes
    private boolean nested;
    private String EnclosingFunctionName;
    private Set<String> CapturedVariables;

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

    public int getParameterCount() {
        return params.size();
    }

    public void addParameter(Symbol newParam) {
        params.add(newParam);
    }

    public String getEnclosingFunctionName() {
        return EnclosingFunctionName;
    }

    public void setEnclosingFunctionName(String enclosingFunctionName) {
        EnclosingFunctionName = enclosingFunctionName;
    }

    public boolean isNested() {
        return nested;
    }

    public void setNested(boolean nested) {
        this.nested = nested;
    }

    public Set<String> getCapturedVariables() {
        return CapturedVariables;
    }

    public void setCapturedVariables(Set<String> capturedVariables) {
        CapturedVariables = capturedVariables;
    }

    @Override
    public String toString() {
        return kind + " " + name + ":" + type + " (line " + line + ":" + column + ")";
    }
}
