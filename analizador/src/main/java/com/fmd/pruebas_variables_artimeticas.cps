// Archivo de prueba para operaciones aritméticas
// test_arithmetic.cps

// Variables de prueba
let a: integer = 10;
let b: integer = 5;
let c: integer = 3;
let texto: string = "hello";
let bandera: boolean = true;

// ========================================
// ✅ CASOS VÁLIDOS (NO deberían generar errores)
// ========================================

// Operaciones básicas válidas
let suma_valida: integer = a + b;              // 10 + 5
let resta_valida: integer = a - b;             // 10 - 5  
let mult_valida: integer = a * c;              // 10 * 3
let div_valida: integer = a / b;               // 10 / 5

// Operaciones con literales
let literal_suma: integer = 100 + 50;          // Literales integer
let literal_mult: integer = 25 * 4;            // Literales integer

// Operaciones complejas válidas
let compleja1: integer = (a + b) * c;          // (15) * 3
let compleja2: integer = a + (b * c);          // 10 + (15)
let compleja3: integer = a - b + c;            // Operadores en cadena
let compleja4: integer = a * b / c;            // Mult y div en cadena

// ========================================
// ❌ CASOS INVÁLIDOS (DEBERÍAN generar errores)
// ========================================

// Error: integer + string
let error1: integer = a + texto;

// Error: integer * boolean  
let error2: integer = b * bandera;

// Error: integer / string
let error3: integer = a / texto;

// Error: string - integer
let error4: integer = texto - a;

// Error: boolean + integer
let error5: integer = bandera + a;

// Error: operaciones mixtas con tipos incorrectos
let error6: integer = (a + texto) * b;         // Error en subexpresión
let error7: integer = a * (bandera + b);       // Error en subexpresión
let error8: integer = a + b * texto;           // Error en operando derecho

print("Pruebas completadas.");