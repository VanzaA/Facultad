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
- Si la CPU trabaja con direcciones lógicas, para acceder a memoria principal, se deben transformar en direcciones físicas.
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
- El valor en el “registro de realocación” es sumado a cada dirección generada por el proceso de usuario al momento de acceder a la memoria.
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

- El SO puede traer a memoria las “piezas”
de un proceso a medida que éste las
necesita.
- Definiremos como “Conjunto Residente”
a la porción del espacio de direcciones del
proceso que se encuentra en memoria.
    - Alguna bibliografía lo llama “Working Set”
- Con el apoyo del HW:
- Se detecta cuando se necesita una porción
del proceso que no está en su Conjunto
Residente

##### Ventajas
- Más procesos pueden ser mantenidos en
memoria.
- Sólo son cargadas algunas secciones de cadaproceso.
- Con más procesos en memoria principal es más probable que existan más procesos Ready
- Un proceso puede ser mas grande que la
memoria Principal
- El usuario no se debe preocupar por el tamaño de sus programas
- La limitación la impone el HW y el bus de
direcciones.

>¿Que se necesita para MV?
- El hardware debe soportar paginación por
demanda (y/o segmentación por demanda)
- Un dispositivo de memoria secundaria
(disco) que dé el apoyo para almacenar las
secciones del proceso que no están en
Memoria Principal (área de intercambio)
- El SO debe ser capaz de manejar el
movimiento de las páginas (o segmentos)
entre la memoria principal y la secundaria.

#### MV con Paginación
- Cada proceso tiene su tabla de páginas
- Cada entrada en la tabla referencia al frame
o marco en el que se encuentra la página en
la memoria principal
- Cada entrada en la tabla de páginas tiene
bits de control (entre otros):
- Bit V: Indica si la página está en memoria
- Bit M: Indica si la página fue modificada. Si se modificó, en algún momento, se deben reflejar los cambios en Memoria Secundaria

##### ejemplo
Una entrada válida tiene:
- Bit V = 1
- Page Frame Number (PFN) – Marco de memoria asociado
- Flags que describen su estado y protección

### Fallo de páginas (Page Fault)
Ocurre cuando el proceso intenta usar una
dirección que está en una página que no se
encuentra en la memoria principal. Bit V=0. La página no se encuentra en su conjunto
residente. El bit V es controlado por el HW
este detecta la situación y genera un trap al S.O el cual podrá colocar al proceso en estado de “Blocked” (espera) mientras gestiona que la página que se necesite se cargue.

El S.O. busca un “Frame o Marco
Libre” en la memoria y genera una
operación de E/S al disco para copiar
en dicho Frame la página del proceso
que se necesita utilizar. El SO puede asignarle la CPU a otro proceso mientras se completa la E/S.l a E/S se realizará y avisará mediante interrupción su finalización.

Cuando la operación de E/S finaliza, se
notifica al SO y este:
- Actualiza la tabla de páginas del proceso
- Coloca el Bit V en 1 en la página en cuestión
- Coloca la dirección base del Frame donde se colocó la página
- El proceso que generó el Fallo de Página
vuelve a estado de Ready (listo)
- Cuando el proceso se ejecute, se volverá a
ejecutar la instrucción que antes generó el
fallo de página

### Perfomance 

- Tasa de Page Faults 0 < p < 1
- Si p = 0 no hay page faults
- Si p = 1, cada a memoria genera un page fault
- Effective Access Time (EAT)
EAT = (1 – p) x memory access + p x (page_fault_overhead + (swap_page_out) + swap_page_in + restart_overhead)

### Tabla de Páginas
Cada proceso tiene su tabla de
páginas, el tamaño de la tabla de esta
depende del espacio de direcciones del
proceso. Puede alcanzar un tamaño
considerable.

Formas de organizar:
- Tabla de 1 nivel: Tabla única lineal
- Tabla de 2 niveles (o más, multinivel)
- Tabla invertida: Hashing
 
La forma de organizarla depende del
HW subyacente

>__ver ejemplo memoria-2 diapositiva 17__

####  Tabla invertida
 - Utilizada en Arquitecturas donde el espacio de direcciones es muy grande
    - Las tablas de paginas ocupan muchos niveles y la traducción es costosa
- Hay una entrada por cada frame. Hay una
sola tabla para todo el sistema
- Usada en PowerPC, UltraSPARC, y IA-64
- El número de página es transformado en un
valor de HASH
- El HASH se usa como índice de la tabla
invertida para encontrar el marco asociado
- Sólo se mantienen los PTEs de
páginas presentes en memoria
física
    - Tabla invertida organizada como tabla
hash en memoria principal
        - Se busca indexadamente por número de página virtual
        - Si está presente en tabla, se extrae el marco de página y sus protecciones
        - Si no está presente en tabla, corresponde a un fallo de página

### Tamaño de la Pagina
Pequeño
- Menor Fragmentación Interna.
- Más paginas requeridas por proceso  Tablas de páginas mas grandes.
- Más paginas pueden residir en memoria

Grande
- Mayor Fragmentación interna
- La memoria secundaria esta diseñada para
transferir grandes bloques de datos más
eficientemente -> Mas rápido mover páginas
hacia la memoria principal.

### Translation Lookaside Buffer 
- Cada referencia en el espacio virtual puede
causar 2 (o más) accesos a la memoria
física.
    - Uno (o más) para obtener la entrada en tabla de paginas
    - Uno para obtener los datos
- Para solucionar este problema, una memoria
cache de alta velocidad es usada para almacenar entradas de páginas
    - **TLB**

Contiene las entradas de la tabla de
páginas que fueron usadas mas
recientemente. Dada una dirección virtual, el procesador examina la TLB, si la entrada de la tabla de paginas se encuentra en la TLB (hit), es obtenido el frame y armada la dirección física, si la entrada no es encontrada en la TLB (miss), el número de página es usado como índice en la tabla de paginas del proceso.

Se controla si la pagina está en la memoria si no está, se genera un Page Fault

La TLB es actualizada para incluir la nueva
entrada. 
El cambio de contexto genera la invalidación
de las entradas de la TLB

#### Asignación de Marcos - Asignación Fija
Número fijo de marcos para cada proceso
- Asignación equitativa – Ejemplo: si tengo
100 frames y 5 procesos, 20 frames para
cada proceso
- Asignación Proporcional: Se asigna acorde
al tamaño del proceso.

#### Asignación Dinámica
- El número de marcos para cada proceso varía

### Reemplazo de páginas
> Qué sucede si ocurre un fallo de página y
todos los marcos están ocupados ,“Se
debe seleccionar una página víctima”  ¿Cual sería Reemplazo Optimo?
- Que la página a ser removida no sea referenciada en un futuro próximo
- La mayoría de los reemplazos predicen el
comportamiento futuro mirando el
comportamiento pasado.

#### Alcance del Reemplazo
Reemplazo Global
- El fallo de página de un proceso puede
reemplazar la página de cualquier proceso.
- El SO no controla la tasa de page-faults de
cada proceso
- Puede tomar frames de otro proceso
aumentando la cantidad de frames asignados
a él.
- Un proceso de alta prioridad podría tomar los frames de un proceso de menor prioridad.

Reemplazo Local
- El fallo de página de un proceso solo puede reemplazar sus propias páginas – De su
Conjunto Residente
- No cambia la cantidad de frames asignados
- El SO puede determinar cual es la tasa de
page-faults de cada proceso
- Un proceso puede tener frames asignados
que no usa, y no pueden ser usados por otros
procesos.

##### ejemplo de algoritmos de reemplazo

- OPTIMO
- FIFO
- LRU (Least Recently Used)
- 2da. Chance
- NRU (Non Recently Used)
    - Utiliza bits R y M
    - ~R ,~M > ~R, M > R, ~M > R, M

bit M: indica si la pagina fue modificada

bit R: indica si la pagina fue referenciada en un lapso de tiempo

## Thrashing (hiperpaginación)
Concepto: decimos que un
sistema está en thrashing cuando
pasa más tiempo paginando que
ejecutando procesos. Como consecuencia, hay una baja importante de performance en el
sistema.

### Ciclo del thrashing
1) El kernel monitorea el uso de la CPU.
2) Si hay baja utilización => aumenta el
grado de multiprogramación.
3) Si el algoritmo de reemplazo es global,
pueden sacarse frames a otros procesos.
4) Un proceso necesita más frames.
Comienzan los page-faults y robo de
frames a otros procesos.
5) Por swapping de páginas, y encolamiento
en dispositivos, baja el uso de la CPU.
6) Vuelve a 1).

### El scheduler de CPU y el thrashing
1) Cuando se decrementa el uso de la
CPU, el scheduler long term aumenta
el grado de multiprogramación.
2) El nuevo proceso inicia nuevos page-
faults, y por lo tanto, más actividad
de paginado.
3) Se decrementa el uso de la CPU
4) Vuelve a 1).

### Control del thrashing
- Se puede limitar el thrashing usando
algoritmos de reemplazo local. Con este algoritmo, si un proceso entra en thrashing no roba frames a otros procesos. Si bien perjudica la performance del sistema, es controlable.

> Conclusión sobre thrashing
- Si un proceso cuenta con todos los frames que necesita, no habría thrashing. Una manera de abordar esta problemática es utilizando la estrategia de Working Set, la cual se apoya en el modelo de localidad.Otra estrategia con el mismo espíritu es la del algoritmo PFF (Frecuencia de Fallos de Página)

### El modelo de localidad
- Cercanía de referencias o principio de
cercanía
- Las referencias a datos y programa dentro
de un proceso tienden a agruparse
- La localidad de un proceso en un momento
dado se da por el conjunto de páginas que
tiene en memoria en ese momento.
- En cortos períodos de tiempo, el proceso
necesitará pocas “piezas” del proceso (por
ejemplo, una página de instrucciones y otra
de datos...)
- Un programa se compone de varias
localidades.
- Ejemplo: Cada rutina será una nueva
localidad: se referencian sus
direcciones (cercanas) cuando se está
ejecutando.
- Para prevenir la hiperactividad, un
proceso debe tener en memoria sus
páginas más activas (menos page
faults).

#### El modelo de working set
- Se basa en el modelo de localidad.
- Ventana del working set (Δ): las ): las
referencias de memoria más recientes.
- Working set: es el conjunto de páginas
que tienen las más recientes Δ): las 
referencias a páginas.

##### ver ejemplo diapositiva 13 memoria 3

La selección del Δ 

- Δ  chico: no cubrirá la localidad
- Δ grande: puede tomar varias
localidades

##### ver diapositiva 15

#### Prevención del thrashing
- SO monitorea c/ proceso, dándole tantos
frames hasta su WSS
- Si quedan frames, puede iniciar otro
proceso.
- Si D crece, excediendo m, se elige un
proceso para suspender, reasignándose sus
frames...
Así, se mantiene alto el grado de
multiprogramación optimizando el uso de la
CPU.

Problema del modelo del WS
- Mantener un registro de los WSS
- La ventana es móvil

### Prevención del thrashing por PFF
PFF en lugar de calcular el WS de los
procesos, utiliza la tasa de fallos de
página para estimar si el proceso tiene
un conjunto residente que representa
adecuadamente al WS.
- PFF: frecuencia de page faults
- PFF alta => se necesitan más frames
- PFF baja => los procesos tienen muchas
frames asignados
- Establecer límites superior e inferior
de las PFF’s deseadas.
- Excede PFF máx. => le doy un frame
más.
- Por debajo del PFF mínimo => le saco
frame
- Puede llegar a suspender un proceso si
no hay más frames. Sus frames se
reasignan a procesos de alta PFF.

### Demonio de Paginación
- Proceso creado por el SO durante el arranque que apoya a la administración de la memoria. Se ejecuta cuando el sistema tiene una baja utilización o algún parámetro de la memoria lo indica:
    - Poca memoria libre
    - Mucha memoria modificada
- Tareas:
    - Limpiar páginas modificadas sincronizándolas con el swap
    - Reducir el tiempo de swap posterior ya que las páginas están“limpias”
    - Reducir el tiempo de transferencia al sincronizar varias páginas contiguas.
    - Mantener un cierto número de páginas libres en el sistema.
    - Demorar la liberación de una página hasta que haga falta realmente

##### ejemplo 
- En Linux  Proceso “kswapd”
- En Windows  Proceso “system”

### Memoria Compartida
- Gracias al uso de la tabla de páginas varios procesos pueden compartir un marco de
memoria; para ello ese marco debe estar
asociado a una página en la tabla de páginas de cada proceso. El número de página asociado al marco puede ser
diferente en cada proceso
- Código compartido
    - Los procesos comparten una copia de código (sólolectura) por ej. editores de texto, compiladores, etc
    - Los datos son privados a cada proceso y se encuentran en páginas no compartidas

#### Mapeo de Archivo en Memoria
- Técnica que permite a un proceso asociar
el contenido de un archivo a una región de
su espacio de direcciones virtuales. El contenido del archivo no se sube a
memoria hasta que se generan Page
Faults. El contenido de la pagina que genera el PF es obtenido desde el archivo asociado, no del área de intercambio
- Cuando el proceso termina o el archivo se libera, las páginas modificadas son escritas en el archivo correspondiente. Permite realizar E/S de una manera alternativa a usar operaciones directamente sobre el Sistema de Archivos
- Es utilizado comúnmente para asociar
librerías compartidas o DLLs

#### Copia en Escritura
- La copia en escritura (Copy-on-Write, COW)
permite a los procesos padre e hijo
compartir inicialmente las mismas páginas
de memoria, si uno de ellos modifica una página compartida la página es copiada
- COW permite crear procesos de forma más
eficiente debido a que sólo las páginas
modificadas son duplicadas

#### Área de Intercambio
- Sobre el Área utilizada
    - Área dedicada, separada del Sistema de Archivos (Por ejemplo, en Linux)
    -Un archivo dentro del Sistema de Archivos (Por ejemplo, Windows)
- Técnicas para la Administración:
    - Cada vez que se crea un proceso se reserva una zona del área de intercambio igual al tamaño de imagen del proceso. A cada proceso se le asigna la dirección en disco de su área de intercambio. La lectura se realiza sumando el número de página virtual a la dirección de comienzo del área asignada al proceso.
    - No se asigna nada inicialmente. A cada página se le asigna su espacio en disco cuando se va a intercambiar, y el espacio se libera cuando la página vuelve a memoria. Problema: se debe llevar contabilidad en memoria (página a página) de la localización de las páginas en disco.

    >  Cuando una página no esta en
memoria, sino en disco, como
podemos saber en que parte del área
de intercambio está?

- Respuesta : El PTE de dicha pagina tiene el bit
V=0 y todos los demás bits sin usar!

#### Área de Intercambio - Linux
- Permite definir un número
predefinido de áreas de
Swap
- swap_info es un arreglo
que contiene estas
estructuras

- Cada área es dividida en un número fijo de
slots según el tamaño de la página
- Cuando una página es llevada a disco,
Linux utiliza el PTE para almacenar 2
valores:
    - En número de área
    - El desplazamiento en el área (24 bits, lo que limita el tamaño máximo del área a 64 Gb)
