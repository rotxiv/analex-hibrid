# Analizador Léxico Híbrido

Proyecto desarrollado para la asignatura de **Compiladores**. Implementa un
analizador léxico con una interfaz gráfica en Java/Swing capaz de reconocer
palabras reservadas, identificadores, números, literales y operadores de una
sintaxis inspirada tanto en Pascal como en Java.

## Descripción

El programa lee código fuente escrito en el área de texto, divide el contenido
en lexemas y presenta cada lexema junto con su token en una tabla. También
permite cargar programas desde archivos, analizar todo el contenido o avanzar
de un token a otro.

La implementación incluye:

- Una cinta (`Cinta`) que representa el código fuente y controla el cabezal.
- Un autómata léxico (`Analex`) que identifica cada lexema.
- Una clase (`Token`) que representa tokens y sus atributos.
- Una interfaz gráfica (`Principal`) para edición, carga y visualización.
- Archivos de ejemplo en sintaxis Java y Pascal.

## Funcionalidades

- Carga de archivos desde el sistema.
- Análisis léxico completo de un programa.
- Análisis paso a paso.
- Resaltado del lexema que está siendo analizado.
- Tabla con el nombre y los atributos de cada token.
- Limpieza del resultado o del editor completo.

## Requisitos

- Java Development Kit (JDK) 17 o superior.
- Visual Studio Code con la extensión **Extension Pack for Java**.

No se utilizan bibliotecas externas adicionales.

## Ejecutar el proyecto

### Desde Visual Studio Code

1. Abra la carpeta del proyecto.
2. Abra `src/App.java`.
3. Use **Run Java** o presione `F5`.
4. Seleccione la configuración `App` si Visual Studio Code lo solicita.

También puede usar la configuración incluida en `.vscode/launch.json`.

### Desde la terminal

Desde la carpeta raíz del proyecto:

```bat
javac -d bin src\App.java src\application\*.java src\data\*.java src\view\*.java
java -cp bin App
```

## Uso

1. Escriba el programa fuente en el área **Programa fuente**.
2. Para abrir un archivo existente, seleccione **Cargar archivo**.
3. Use **Analizar siguiente** para procesar un token a la vez.
4. Use **Analizar todo** para obtener la lista completa de tokens.
5. Use **Limpiar** para borrar únicamente el resultado o **Limpiar todo** para
   borrar también el contenido del editor.

Los lexemas reconocidos por el análisis aparecen resaltados en el área de
código. El resultado muestra una representación con el formato
`<TOKEN, ATRIBUTO>`.

## Elementos reconocidos

### Palabras reservadas

`PROGRAM`, `VAR`, `PROCEDURE`, `VOID`, `BEGIN`, `END`, `IF`, `THEN`, `ELSE`,
`FOR`, `TO`, `DO`, `DOWNTO`, `WHILE`, `CASE`, `OF`, `REPEAT`, `UNTIL`, `READ`,
`READLN`, `PRINT`, `PRINTLN`, `NOT`, `AND`, `OR`, `MOD`, `DIV`, `BOOLEAN` e
`INTEGER`.

Las palabras reservadas no distinguen mayúsculas de minúsculas.

### Identificadores y números

- Identificadores: comienzan con una letra y pueden contener letras o dígitos.
- Números: secuencias de dígitos dentro del rango admitido por `int`.
- Literales: cadenas encerradas entre comillas simples o dobles.

### Operadores y símbolos

| Categoría | Lexemas |
|---|---|
| Operadores relacionales | `=`, `<`, `>`, `<=`, `>=`, `<>`, `!=` |
| Operador de asignación | `:=` |
| Operadores aritméticos | `+`, `-`, `*`, `/`, `%` |
| Operadores lógicos | `&`, `\|`, `!` |
| Separadores | `,`, `:`, `;`, `(`, `)` |
| Bloques híbridos | `{`, `}` |

El analizador reconoce comentarios de línea (`//`) y comentarios de bloque
(`/* ... */`) sin incluirlos en la tabla de resultados.

## Ejemplos

### Sintaxis Pascal

Archivo: `external/pascal/EvaluarUno.pas`

```pascal
program Prueba;
var contador: integer;
begin
    contador := 1;
    while contador <= 3 do
    begin
        println('Iteracion');
        contador := contador + 1;
    end;
end
```

### Sintaxis híbrida Java/Pascal

Archivo: `external/java/EvaluarUno.java`

```java
void evaluar() {
    integer edad;
    edad := 20;

    if (edad >= 18) {
        println("Mayor de edad");
    } else {
        println("Menor de edad");
    }
}
```

## Estructura del proyecto

```text
analex-hibrid/
├── src/
│   ├── App.java
│   ├── application/
│   │   ├── Analex.java
│   │   ├── Cinta.java
│   │   └── Token.java
│   ├── data/
│   │   └── File.java
│   └── view/
│       └── Principal.java
├── external/
│   ├── java/
│   └── pascal/
├── bin/
├── lib/
├── .vscode/
└── README.md
```

- `src/application`: lógica del analizador léxico.
- `src/data`: carga de archivos.
- `src/view`: interfaz gráfica.
- `external`: programas de prueba en las sintaxis admitidas.
- `bin`: archivos Java compilados.
- `lib`: librerías externas opcionales en formato JAR.

## Alcance

Este proyecto corresponde a la etapa de análisis léxico. No realiza análisis
sintáctico ni genera código intermedio o código objeto.
