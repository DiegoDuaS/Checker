// Pruebas para operaciones de comparación (==, !=, <, >, <=, >=)

// ===== DECLARACIONES DE VARIABLES PARA PRUEBAS =====
let num1: integer = 10;
let num2: integer = 20;
let num3: integer = 10;
let text1: string = "hello";
let text2: string = "world";
let text3: string = "hello";
let flag1: boolean = true;
let flag2: boolean = false;
let flag3: boolean = true;
let nullValue: null = null;

// ===== PRUEBAS VÁLIDAS - OPERACIONES DE IGUALDAD =====
print("=== Pruebas válidas de igualdad ===");

// Comparaciones de igualdad válidas - mismo tipo
let eq1: boolean = num1 == num3;          // 10 == 10 = true
let eq2: boolean = num1 == num2;          // 10 == 20 = false
let eq3: boolean = text1 == text3;        // "hello" == "hello" = true
let eq4: boolean = text1 == text2;        // "hello" == "world" = false
let eq5: boolean = flag1 == flag3;        // true == true = true
let eq6: boolean = flag1 == flag2;        // true == false = false

// Comparaciones de desigualdad válidas - mismo tipo
let neq1: boolean = num1 != num2;         // 10 != 20 = true
let neq2: boolean = num1 != num3;         // 10 != 10 = false
let neq3: boolean = text1 != text2;       // "hello" != "world" = true
let neq4: boolean = text1 != text3;       // "hello" != "hello" = false
let neq6: boolean = flag1 != flag3;       // true != true = false

// Comparaciones con null (válidas con cualquier tipo)
let null1: boolean = nullValue == null;   // null == null = true
let null2: boolean = num1 == null;        // integer == null = false
let null3: boolean = text1 == null;       // string == null = false
let null4: boolean = flag1 == null;       // boolean == null = false

// ===== PRUEBAS VÁLIDAS - OPERACIONES RELACIONALES =====
print("=== Pruebas válidas relacionales ===");

// Comparaciones relacionales con integers - válidas
let rel1: boolean = num1 < num2;          // 10 < 20 = true
let rel2: boolean = num2 > num1;          // 20 > 10 = true
let rel3: boolean = num1 <= num3;         // 10 <= 10 = true
let rel4: boolean = num2 >= num1;         // 20 >= 10 = true
let rel5: boolean = num1 <= num2;         // 10 <= 20 = true
let rel6: boolean = num2 >= num3;         // 20 >= 10 = true

// Comparaciones relacionales con strings - válidas
let strRel1: boolean = text1 < text2;     // "hello" < "world" = true
let strRel2: boolean = text2 > text1;     // "world" > "hello" = true
let strRel3: boolean = text1 <= text3;    // "hello" <= "hello" = true
let strRel4: boolean = text1 >= text3;    // "hello" >= "hello" = true

// Comparaciones con literales - válidas
let lit1: boolean = 5 < 10;               // literal integer comparison
let lit2: boolean = "abc" <= "def";       // literal string comparison
let lit3: boolean = num1 == 10;           // variable vs literal
let lit4: boolean = text1 != "goodbye";   // string variable vs literal

// Uso en estructuras de control - válidas
if (num1 < num2) {
    print("num1 is less than num2");
}

if (text1 == "hello") {
    print("text1 equals hello");
}

while (num1 <= 15) {
    print("num1 is still <= 15");
    num1 = num1 + 1;
}


// ===== PRUEBAS INVÁLIDAS - DEBEN GENERAR ERRORES =====
print("=== Estas líneas deben generar errores ===");

// ERROR: Comparación de igualdad entre tipos incompatibles
let err1: boolean = num1 == text1;        // ERROR: integer == string
let err2: boolean = flag1 == num1;        // ERROR: boolean == integer
let err3: boolean = text1 == flag1;       // ERROR: string == boolean
let err4: boolean = num1 != flag1;        // ERROR: integer != boolean
let err5: boolean = text1 != num2;        // ERROR: string != integer

// ERROR: Operaciones relacionales entre tipos incompatibles
let err6: boolean = num1 < text1;         // ERROR: integer < string
let err7: boolean = flag1 > flag2;        // ERROR: boolean > boolean (no ordenable)
let err8: boolean = text1 <= num1;        // ERROR: string <= integer
let err9: boolean = num1 >= flag1;        // ERROR: integer >= boolean
let err10: boolean = flag1 < flag2;       // ERROR: boolean < boolean (no ordenable)

// ERROR: Operaciones relacionales con tipos no ordenables
let err11: boolean = flag1 <= flag2;      // ERROR: boolean no es ordenable
let err12: boolean = flag1 >= flag3;      // ERROR: boolean no es ordenable
let err13: boolean = nullValue < num1;    // ERROR: null no es ordenable
let err14: boolean = nullValue > text1;   // ERROR: null no es ordenable

// ERROR: Comparaciones relacionales con null
let err15: boolean = nullValue <= null;   // ERROR: null no es ordenable
let err16: boolean = nullValue >= null;   // ERROR: null no es ordenable

// ERROR: Uso incorrecto en estructuras de control
if (num1 < text1) {                      // ERROR: tipos incompatibles
    print("This should not work");
}

if (flag1 > flag2) {                     // ERROR: boolean no ordenable
    print("This should not work either");
}

while (text1 <= num1) {                  // ERROR: tipos incompatibles
    print("This will not work");
    break;
}

// ERROR: Comparaciones complejas con tipos incorrectos
let err17: boolean = (num1 < text1) == flag1;     // ERROR en subconsulta
let err18: boolean = (flag1 <= flag2) != true;    // ERROR en subconsulta
let err19: boolean = num1 == text1 && flag1;      // ERROR: integer == string

// Expresiones más complejas - algunas válidas, algunas inválidas
let complex1: boolean = (num1 < num2) && (text1 == text3);  // Válida
let complex2: boolean = (num1 == text1) || (flag1 == flag2); // ERROR en primera parte
let complex3: boolean = (flag1 > flag2) && (num1 < num2);   // ERROR en primera parte

print("=== Fin de las pruebas de comparación ===");

