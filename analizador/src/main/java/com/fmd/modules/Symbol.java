package com.fmd.modules;

import com.fmd.SemanticVisitor;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.*;

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

    private Set<String> CapturedVariables = new HashSet<>();
    private String superClass;
    private String enclosingClassName;
    private Map<String, Symbol> members = new HashMap<>();
    private boolean initialized = false;
    private boolean constructor = false;

    // PARA GENERACIÓN DE CÓDIGO INTERMEDIO
    private String tacAddress;   // Nombre en TAC: variable, temporal o etiqueta
    private int offset;          // Desplazamiento en el frame o en el objeto
    private int size;            // Tamaño en bytes (sirve para calcular offsets)

    // Para arrays/structs
    private List<Integer> dimensions; // Si es array: [10], [10,20]...
    private int elementSize;          // Tamaño de cada elemento

    // Para funciones
    private int paramCount;      // Cantidad de parámetros
    private int localVarSize;    // Tamaño total de locales (para reservar stack)

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

    public void setSuperClass(String superClass) {
        this.superClass = superClass;
    }

    public String getSuperClass() {
        return superClass;
    }

    public void setEnclosingClassName(String name) {
        this.enclosingClassName = name;
    }

    public String getEnclosingClassName() {
        return enclosingClassName;
    }

    public Map<String, Symbol> getMembers() {
        return members;
    }

    public void setMembers(Map<String, Symbol> symbols) {
        members = symbols;
    }

    public void addMember(Symbol s) {
        members.put(s.getName(), s);
    }

    public boolean isInitialized() { return initialized; }

    public void setInitialized(boolean value) { initialized = value; }

    public boolean isConstructor() {
        return constructor;
    }

    public void setConstructor(boolean value) {
        this.constructor = value;
    }

    public String getTacAddress() {
        return tacAddress;
    }

    public void setTacAddress(String tacAddress) {
        this.tacAddress = tacAddress;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<Integer> getDimensions() {
        return dimensions;
    }

    public void setDimensions(List<Integer> dimensions) {
        this.dimensions = dimensions;
    }

    public int getElementSize() {
        return elementSize;
    }

    public void setElementSize(int elementSize) {
        this.elementSize = elementSize;
    }

    public int getParamCount() {
        return paramCount;
    }

    public void setParamCount(int paramCount) {
        this.paramCount = paramCount;
    }

    public int getLocalVarSize() {
        return localVarSize;
    }

    public void setLocalVarSize(int localVarSize) {
        this.localVarSize = localVarSize;
    }


    @Override
    public String toString() {
        return kind + " " + name + ":" + type + " (line " + line + ":" + column + ")";
    }
}
