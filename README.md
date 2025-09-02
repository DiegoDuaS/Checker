# Analizador Semántico Compiscript

Un analizador semántico completo para el lenguaje de programación **Compiscript**, desarrollado en Java usando ANTLR4 y Spring Boot. Este proyecto implementa análisis léxico, sintáctico y semántico con generación visual de árboles AST y un IDE integrado.

## Características Principales

- **Analizador Sintáctico** usando ANTLR4 con gramática completa
- **Análisis Semántico** robusto con verificación de reglas semánticas
- **Sistema de Tipos** completo con validaciones exhaustivas
- **Tabla de Símbolos** con manejo de entornos y ámbitos
- **IDE Integrado** para escribir y compilar código
- **API REST** para compilación en línea
- **Generación Visual de AST** usando Graphviz
- **Batería de Tests** comprehensiva para validar todas las reglas

## Reglas Semánticas Implementadas

### Sistema de Tipos
- **Operaciones Aritméticas** (`+`, `-`, `*`, `/`): Verificación de tipos `integer`
- **Operaciones Lógicas** (`&&`, `||`, `!`): Validación de operandos `boolean`
- **Comparaciones** (`==`, `!=`, `<`, `<=`, `>`, `>=`): Compatibilidad de tipos
- **Asignaciones**: Concordancia de tipos entre valor y variable declarada
- **Constantes**: Inicialización obligatoria de `const` en declaración
- **Concatenación**: Reglas especiales para `string + integer` en contexto `print()`

### Manejo de Ámbito
- **Resolución de Nombres**: Variables y funciones según ámbito local/global
- **Variables No Declaradas**: Error por uso de identificadores no declarados
- **Redeclaración**: Prohibición de identificadores duplicados en mismo ámbito
- **Bloques Anidados**: Control de acceso correcto en estructuras anidadas
- **Entornos**: Creación de nuevos entornos para funciones, clases y bloques

### Funciones y Procedimientos
- **Validación de Argumentos**: Número y tipo de parámetros en llamadas
- **Tipo de Retorno**: Concordancia con tipo declarado de la función
- **Recursión**: Soporte completo para funciones recursivas
- **Funciones Anidadas**: Soporte para closures y captura de variables
- **Declaraciones Múltiples**: Detección de funciones duplicadas

### Control de Flujo
- **Condiciones Boolean**: Validación en `if`, `while`, `do-while`, `for`, `switch`
- **Break/Continue**: Uso válido solo dentro de bucles
- **Return**: Verificación de uso dentro de funciones únicamente
- **Foreach**: Validación de tipos iterables y variables de iteración

### Clases y Objetos (POO)
- **Dot Notation**: Validación de existencia de atributos y métodos
- **Constructores**: Verificación de llamadas correctas con `new`
- **Herencia**: Soporte completo con validación de superclases
- **This**: Manejo correcto de referencia al objeto actual
- **Inicialización de Miembros**: Control de estado de inicialización

### Estructuras de Datos
- **Arreglos**: Verificación de tipos de elementos y dimensiones
- **Índices**: Validación de acceso con tipos `integer`
- **Arreglos Multidimensionales**: Soporte completo para matrices n-dimensionales
- **Foreach**: Iteración válida sobre estructuras de datos

### Validaciones Generales
- **Código Muerto**: Detección de instrucciones después de `return`/`break`
- **Expresiones Válidas**: Verificación de sentido semántico
- **Declaraciones Duplicadas**: Control de variables y parámetros duplicados

## Arquitectura del Proyecto

```
analizador/
├── src/main/
│   ├── antlr4/
│   │   └── Compiscript.g4              # Gramática ANTLR4 completa
│   ├── java/com/fmd/
│   │   ├── AnalizadorApplication.java  # API REST principal + IDE
│   │   ├── SemanticVisitor.java        # Coordinador de análisis semántico
│   │   ├── VariableVisitor.java        # Variables y aritmética
│   │   ├── ComparisonVisitor.java      # Operaciones de comparación
│   │   ├── LogicalVisitor.java         # Operaciones lógicas
│   │   ├── FunctionsVisitor.java       # Análisis de funciones
│   │   ├── ClassesListener.java        # Análisis de clases y POO
│   │   └── modules/
│   │       ├── Symbol.java             # Representación de símbolos
│   │       └── SemanticError.java      # Sistema de manejo de errores
│   └── additions/
│       └── AstTreeGenerator.py         # Generador visual de AST
├── src/test/java/com/fmd/
│   ├── VariableVisitorTest.java        # Tests operaciones aritméticas
│   ├── LogicalVisitorTest.java         # Tests operaciones lógicas
│   ├── ComparisonVisitorTest.java      # Tests comparaciones
│   ├── FunctionSemanticTest.java       # Tests funciones
│   ├── ClassesTest.java                # Tests POO
│   ├── ControlFlowTest.java            # Tests estructuras de control
│   └── DataStructuresTest.java         # Tests arreglos y estructuras
└── README.md
```

## Tecnologías Utilizadas

- **Java 17** - Lenguaje principal
- **Spring Boot 3.2.5** - Framework web y API REST
- **ANTLR4 4.13.1** - Generador de analizadores léxico/sintáctico
- **Graphviz + Python** - Visualización de AST
- **Maven 3.6+** - Gestión de dependencias y build
- **JUnit 5** - Framework de testing

## Instalación y Configuración

### Prerrequisitos
- **JDK 17** o superior
- **Maven 3.6+**
- **Python 3.x** con `pip install graphviz`
- **Graphviz** instalado en el sistema

### Configuración e Instalación

1. **Clonar repositorio**
   ```bash
   git clone <repository-url>
   cd analizador
   ```

2. **Instalar dependencias Python**
   ```bash
   pip install graphviz
   ```

3. **Compilar proyecto**
   ```bash
   mvn clean compile
   ```

4. **Ejecutar tests completos**
   ```bash
   mvn test
   ```

5. **Iniciar aplicación (API + IDE)**
   ```bash
   mvn spring-boot:run
   ```

La aplicación estará disponible en: **http://localhost:8080**

## Uso del Sistema

### API REST

#### Endpoint de Compilación
```http
POST /compilar
Content-Type: application/json

{
    "codigo": "let x: integer = 10; print(x + 5);"
}
```

#### Respuesta Exitosa:
```json
{
    "errors": [],
    "symbols": [
        {
            "name": "x",
            "type": "integer", 
            "kind": "VARIABLE",
            "line": 1,
            "column": 4
        }
    ],
    "astImage": "data:image/png;base64,iVBORw0KGgoAAAANS..."
}
```

#### Respuesta con Errores:
```json
{
    "errors": [
        {
            "mensaje": "Operación '+' no válida entre tipos: 'string' y 'integer'",
            "linea": 2,
            "columna": 15
        }
    ],
    "symbols": [...],
    "astImage": "..."
}
```

### IDE Integrado
https://github.com/DiegoDuaS/ANTLR-IDE.git

El IDE está disponible en la interfaz web e incluye:

- Editor de código con syntax highlighting
- Compilación en tiempo real
- Visualización de errores semánticos
- Tabla de símbolos interactiva
- Árbol AST visual

### Línea de Comandos
```bash
# Compilar archivo específico
mvn exec:java -Dexec.args="src/main/java/com/fmd/program.cps"

# Usar archivo de ejemplo por defecto
mvn exec:java
```

## Sintaxis del Lenguaje Compiscript

### Tipos de Datos
```javascript
let numero: integer = 42;
let texto: string = "Hola Compiscript";
let bandera: boolean = true;
let lista: integer[] = [1, 2, 3, 4, 5];
let matriz: integer[][] = [[1, 2], [3, 4]];
```

### Variables y Constantes
```javascript
// Variables mutables
let variable: integer = 10;
var flexible: string = "mutable";

// Constantes inmutables (inicialización obligatoria)
const PI: integer = 314;
const NOMBRE: string = "Compiscript";
```

### Funciones
```javascript
// Función con tipo de retorno
function sumar(a: integer, b: integer): integer {
    return a + b;
}

// Función recursiva
function factorial(n: integer): integer {
    if (n <= 1) { return 1; }
    return n * factorial(n - 1);
}

// Función void (sin tipo de retorno)
function saludar(nombre: string) {
    print("Hola " + nombre);
}

// Funciones anidadas (closures)
function crearContador(): integer {
    let count: integer = 0;
    function incrementar(): integer {
        count = count + 1;
        return count;
    }
    return incrementar();
}
```

### Clases y Herencia
```javascript
class Animal {
    let nombre: string;
    let edad: integer;
    
    function constructor(nombre: string, edad: integer) {
        this.nombre = nombre;
        this.edad = edad;
    }
    
    function hablar(): string {
        return this.nombre + " hace un sonido";
    }
    
    function getEdad(): integer {
        return this.edad;
    }
}

class Perro : Animal {
    let raza: string;
    
    function constructor(nombre: string, edad: integer, raza: string) {
        this.nombre = nombre;
        this.edad = edad;
        this.raza = raza;
    }
    
    function hablar(): string {
        return this.nombre + " ladra: Woof!";
    }
}

// Uso de clases
let mascota: Perro = new Perro("Rex", 3, "Pastor Alemán");
print(mascota.hablar());
print("Edad: " + mascota.getEdad());
```

### Estructuras de Control
```javascript
// Condicionales
if (edad >= 18) {
    print("Mayor de edad");
} else {
    print("Menor de edad");
}

// Bucles
let i: integer = 0;
while (i < 10) {
    print("Iteración: " + i);
    i = i + 1;
}

do {
    print("Al menos una vez");
    i = i - 1;
} while (i > 5);

// For tradicional
for (let j: integer = 0; j < 5; j = j + 1) {
    if (j == 3) { continue; }
    if (j == 4) { break; }
    print("j = " + j);
}

// Foreach para arreglos
let numeros: integer[] = [1, 2, 3, 4, 5];
foreach (num in numeros) {
    print("Número: " + num);
}

// Switch-case
switch (opcion) {
    case 1:
        print("Opción uno");
        break;
    case 2:
        print("Opción dos");
        break;
    default:
        print("Opción desconocida");
}

// Try-catch
try {
    let resultado: integer = division(10, 0);
} catch (error) {
    print("Error capturado: " + error);
}
```

### Estructuras de Datos
```javascript
// Arreglos unidimensionales
let enteros: integer[] = [1, 2, 3, 4, 5];
let cadenas: string[] = ["hola", "mundo", "compiscript"];
let booleanos: boolean[] = [true, false, true];

// Acceso y modificación
print(enteros[0]);        // Imprime: 1
enteros[1] = 10;          // Modifica elemento
print(enteros[1]);        // Imprime: 10

// Arreglos multidimensionales  
let matriz: integer[][] = [[1, 2, 3], [4, 5, 6]];
let cubo: integer[][][] = [[[1, 2], [3, 4]], [[5, 6], [7, 8]]];

// Acceso multidimensional
print(matriz[0][1]);      // Imprime: 2
print(cubo[1][0][1]);     // Imprime: 6
```

##  Sistema de Testing

El proyecto incluye una batería completa de tests organizados por funcionalidad:

### Tests Implementados

- **VariableVisitorTest**: Operaciones aritméticas y tipos
- **LogicalVisitorTest**: Operaciones lógicas (&&, ||, !)
- **ComparisonVisitorTest**: Comparaciones (==, !=, <, >, etc.)
- **FunctionSemanticTest**: Funciones, parámetros, recursión, closures
- **ClassesTest**: POO, herencia, constructores, métodos
- **ControlFlowTest**: Estructuras de control y flujo de ejecución
- **DataStructuresTest**: Arreglos, matrices, foreach

### Ejecutar Tests
```bash
# Todos los tests
mvn test

# Tests específicos por categoría
mvn test -Dtest=VariableVisitorTest
mvn test -Dtest=LogicalVisitorTest  
mvn test -Dtest=FunctionSemanticTest
mvn test -Dtest=ClassesTest

# Tests con reporte detallado
mvn test -Dtest="*Test" --batch-mode
```

### Cobertura de Tests
Los tests cubren tanto casos exitosos como casos de error, validando:

- ✅ **Casos válidos**: Código semánticamente correcto
- ❌ **Casos inválidos**: Detección de errores semánticos específicos
- 🔍 **Casos edge**: Situaciones límite y complejas

## Tabla de Símbolos

### Características Implementadas

- **Gestión de Entornos**: Ámbitos anidados con jerarquía padre-hijo
- **Información por Símbolo**: Nombre, tipo, kind, línea, columna, mutabilidad
- **Tipos de Símbolos**: Variables, constantes, funciones, clases
- **Metadatos Avanzados**: Parámetros, tipos de retorno, miembros de clase
- **Scope Resolution**: Búsqueda en ámbito local → global

### Estructura de Símbolos
```java
Symbol {
    String name;           // Nombre del identificador  
    Kind kind;            // VARIABLE, CONSTANT, FUNCTION, CLASS
    String type;          // Tipo de dato
    int line, column;     // Ubicación en código fuente
    boolean mutable;      // Mutabilidad (const vs let/var)
    List<Symbol> params;  // Parámetros (si es función)
    Map<String,Symbol> members; // Miembros (si es clase)
    // ... más metadatos
}
```

## Sistema de Errores Semánticos

### Tipos de Errores Detectados
```bash
# Errores de tipos
[ERROR SEMÁNTICO] Operación '+' no válida entre tipos: 'integer' y 'string' (línea 5, columna 12)

# Errores de ámbito  
[ERROR SEMÁNTICO] Variable 'x' no declarada en este scope (línea 8, columna 4)

# Errores de funciones
[ERROR SEMÁNTICO] Función 'calcular' espera 2 argumentos, pero recibe 1 (línea 10, columna 8)

# Errores de POO
[ERROR SEMÁNTICO] Método 'correr' no existe en la clase 'Animal' (línea 15, columna 12)

# Errores de control de flujo
[ERROR SEMÁNTICO] 'break' solo puede usarse dentro de bucles (línea 20, columna 5)
```

## Generación de AST Visual

El sistema genera automáticamente diagramas del Árbol Sintáctico Abstracto:

### Características

- Formato PNG de alta resolución
- Codificación Base64 para integración web
- Layout jerárquico con Graphviz
- Nodos etiquetados con información semántica

### Ejemplo de Uso
```python
# Ejecutado automáticamente por el sistema
python additions/AstTreeGenerator.py
```


## 📚 Documentación Técnica

### Archivos de Documentación

- **README.md**: Guía completa (este archivo)
- **Compiscript.g4**: Gramática ANTLR4 comentada
- **program.cps**: Ejemplos de código Compiscript
- **Tests**: Documentación por casos de uso

### Arquitectura de Implementación

- **Patrón Visitor**: Para recorrido de AST
- **Tabla de Símbolos**: Implementación de entornos anidados
- **Sistema de Tipos**: Validación exhaustiva
- **API REST**: Endpoints documentados


## 👥 Equipo de Desarrollo

- Fabiola Contreras 22787
- Diego Duarte 22075
- María José Villafuerte 22129
