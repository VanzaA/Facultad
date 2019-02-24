# **Memoria**

## Administración de Memoria
- División Lógica de la Memoria Física para alojar múltiples procesos
  - Garantizando protección
  - Depende del mecanismo provisto por el HW
- Asignación eficientemente
  - Contener el mayor numero de procesos para garantizar el mayor uso de la CPU por los mismos

## Requisitos

### Reubicación
El programador no debe ocuparse de conocer donde será colocado el proceso en la Memoria RAM, mientras este se ejecuta puede ser sacado y traído a la memoria (swap) y, posiblemente, colocarse en diferentes direcciones. Las referencias a la memoria se deben“traducir” según ubicación actual del proceso.

### Protección
- Los procesos **NO** deben referenciar (acceder) a direcciones de memoria de
otros procesos, salvo que tengan permiso
    - El chequeo se debe realizar durante la
ejecución:
    - **NO** es posible anticipar todas las referencias amemoria que un proceso puede realizar.

### Compartición
- Permitir que varios procesos accedan a la
misma porción de memoria.
    - Ej: Rutinas comunes, librerías, espacios explícitamente compartidos, etc.
    - Permite un mejor uso – aprovechamiento - de la memoria RAM, evitando copias innecesarias (repetidas) de instrucciones

## Abstracción - Espacio de Direcciones
- Rango de direcciones (a memoria posibleque un proceso puede utilizar para
direccionar sus instrucciones y datos.
- El tamaño depende de la Arquitectura del
Procesador
    - 32 bits: 0 .. 2 32 - 1
    - 64 bits: 0 .. 2 64 – 1
- Es independiente de la ubicación “real” del proceso en la Memoria RAM

## Direcciones
- Lógicas
    - Referencia a una localidad de memoria independiente de la asignación actual de los datos en la memoria.
    - Representa una dirección en el “Espacio de Direcciones del Proceso”
- Físicas 
    - Referencia una localidad en la Memoria Física(RAM)
    - Dirección absoluta

__En caso de usar direcciones Lógicas, es necesaria algún tipo de conversión a direcciones Físicas.__

### Conversión de Direcciones
Una forma simple de hacer esto es utilizando registros auxiliares.
 
- Registro Base
    - Dirección de comienzo del Espacio de Direcciones del proceso en la RAM
- Registro Limite
    - Dirección final del proceso o medida del proceso - Tamaño de su Espacio de Direcciones 
- Ambos valores se fijan cuando el ED del proceso es
cargado a memoria.
- Varían entre procesos (Context Switch)

### Dir. Lógicas vs. Físicas
- Si la CPU trabaja con direcciones lógicas,
para acceder a memoria principal, se deben
transformar en direcciones físicas.
    - Resolución de direcciones (address-binding): transformar la dirección lógica en la dirección física correspondiente
- Resolución en momento de compilación (Archivos .com de DOS) y en tiempo de carga
    - Direcciones Lógicas y Físicas son idénticas
    - Para reubicar un proceso es necesario
recompilarlo o recargarlo
- Resolución en tiempo de ejecución
- Direcciones Lógicas y Físicas son diferentes
- Direcciones Lógicas son llamadas “Direcciones Virtuales”
- La reubicación se puede realizar fácilmente
- El mapeo entre “Virtuales” y “Físicas” es
realizado por hardware 
    - Memory Management Unit (MMU)

## Memory Management Unit (MMU)
- Dispositivo de Hardware que mapea direcciones virtuales a físicas
    - Es parte del Procesador
    - Re-programar el MMU es una operación  privilegiada
        -solo puede ser realizada en Kernel Mode
- El valor en el “registro de realocación” es sumado a cada dirección generada por el
proceso de usuario al momento de acceder
a la memoria.
    - Los procesos nunca usan direcciones físicas

__ver imagen diapositiva 15 tema 3 - memoria 1__

### Problemas del esquema
- El esquema de Registro Base + Limite
presenta problemas: Necesidad de almacenar el Espacio de Direcciones de forma continua en la Memoria Física
    - Fragmentación
    - Mantener “partes” del proceso que no son necesarias
- Solución
    - Paginación
    - Segmentación

#### Paginación
-  Memoria Física es dividida lógicamente en
pequeños trozos de igual tamaño -> Marcos
-   Memoria Lógica (espacio de direcciones) es dividida en trozos de igual tamaño que los marcos -> Paginas
- El SO debe mantener una tabla de paginas
por cada proceso, donde cada entrada
contiene (entre otras) el Marco en la que se coloca cada pagina.
- La dirección lógica se interpreta como:
    - un numero de pagina y un desplazamiento dentro de la misma.

#### Segmentación
- Esquema que soporta el “punto de vista de
un usuario”
- Un programa es una colección de
segmentos. Un segmento es una unidad
lógica como:
    - Programa Principal, Procedimientos y Funciones, variables locales y globales, stack, etc.
- Puede causar Fragmentación externa.
- Todos los segmentos de un programa    pueden no tener el mismo tamaño (código, datos, rutinas).
- Las direcciones Lógicas consisten en 2
partes:
    - Selector de Segmento
    - Desplazamiento dentro del segmento

#### Segmentación Paginada
- La paginación
        - Transparente al programador
        - Elimina Fragmentación externa.
- Segmentación
    - Es visible al programador
    - Facilita modularidad, estructuras de datos grandes y da mejor soporte a la compartición y protección
- Cada segmento es dividido en paginas de
tamaño fijo.

#### Memoria Virtual