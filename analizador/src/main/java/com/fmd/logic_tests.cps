// Pruebas para operaciones lógicas (&&, ||, !)

// ===== DECLARACIONES DE VARIABLES PARA PRUEBAS =====
let a: boolean = true;
let b: boolean = false;
let x: integer = 5;
let y: integer = 10;
let name: string = "test";

// ===== PRUEBAS VÁLIDAS - OPERACIONES LÓGICAS =====
print("=== Pruebas válidas ===");

// Operador AND (&&) - casos válidos
let result1: boolean = a && b;          // true && false = false
let result2: boolean = true && true;    // true && true = true
let result3: boolean = false && false;  // false && false = false

// Operador OR (||) - casos válidos
let result4: boolean = a || b;          // true || false = true
let result5: boolean = false || false;  // false || false = false
let result6: boolean = true || false;   // true || false = true

// Operador NOT (!) - casos válidos
let result7: boolean = !a;              // !true = false
let result8: boolean = !b;              // !false = true
let result9: boolean = !true;           // !true = false

// Combinaciones complejas válidas
let complex1: boolean = (a && b) || (!a && !b);    // Combinación válida
let complex2: boolean = !((a || b) && (a || !b));  // Negación de expresión compleja
let complex3: boolean = a && (b || true);          // AND y OR combinados

// ===== PRUEBAS VÁLIDAS - OPERACIONES DE COMPARACIÓN =====

// Operadores relacionales (retornan boolean)
let comp1: boolean = x < y;             // 5 < 10 = true
let comp2: boolean = x > y;             // 5 > 10 = false
let comp3: boolean = x <= 5;            // 5 <= 5 = true
let comp4: boolean = y >= 10;           // 10 >= 10 = true

// Operadores de igualdad (retornan boolean)
let eq1: boolean = x == 5;              // 5 == 5 = true
let eq2: boolean = y != x;              // 10 != 5 = true
let eq3: boolean = a == true;           // true == true = true
let eq4: boolean = name == "test";      // "test" == "test" = true

// Uso en estructuras de control (válidas)
if (a && b) {
    print("Both are true");
}

if (x < y || a) {
    print("x is less than y or a is true");
}

while (a && !b) {
    print("Loop while a is true and b is false");
    a = false; // Para evitar bucle infinito
}

// ===== PRUEBAS INVÁLIDAS - DEBEN GENERAR ERRORES =====
print("=== Estas líneas deben generar errores ===");

// ERROR: Operandos de && deben ser boolean
let error1: boolean = x && y;           // ERROR: integer && integer
let error2: boolean = a && x;           // ERROR: boolean && integer
let error3: boolean = name && a;        // ERROR: string && boolean

// ERROR: Operandos de || deben ser boolean
let error4: boolean = x || y;           // ERROR: integer || integer
let error5: boolean = name || b;        // ERROR: string || boolean
let error6: boolean = x || a;           // ERROR: integer || boolean

// ERROR: Operando de ! debe ser boolean
let error7: boolean = !x;               // ERROR: !integer
let error8: boolean = !name;            // ERROR: !string
let error9: boolean = !5;               // ERROR: !integer literal

// ERROR: Operandos de comparación relacional deben ser integer
let error10: boolean = a < b;           // ERROR: boolean < boolean
let error11: boolean = name > "other";  // ERROR: string > string
let error12: boolean = a <= true;       // ERROR: boolean <= boolean

// ERROR: Comparación de igualdad de tipos diferentes
let error13: boolean = x == a;          // ERROR: integer == boolean
let error14: boolean = name == x;       // ERROR: string == integer
let error15: boolean = a != y;          // ERROR: boolean != integer

// ERROR: Expresiones complejas con tipos incorrectos
let error16: boolean = (x && y) || a;   // ERROR: (integer && integer) || boolean
let error17: boolean = !(x + y);        // ERROR: !(integer) - ! necesita boolean
let error18: boolean = a && (x < name); // ERROR: x < name es inválido

// ERROR: Uso incorrecto en estructuras de control
if (x) {                               // ERROR: condición debe ser boolean
    print("This should not work");
}

if (name || x) {                       // ERROR: string || integer
    print("This should not work either");
}

while (x + y) {                        // ERROR: condición debe ser boolean, no integer
    print("This will not work");
    break;
}

print("=== Fin de las pruebas ===");