# Cache de disco

## Disk Cache
Buffers en memoria principal para
almacenamiento temporario de
bloques de disco.

Objetivo: 
- MINIMIZAR LA FRECUENCIA
DE ACCESO AL DISCO

> Algunas observaciones

Cuando un proceso quiere acceder a un bloque de la cache hay dos alternativas:
- Se copia el bloque al espacio de
direcciones del usuario
- Se trabaja como memoria compartida
(no se copia permitiendo acceso a
varios procesos)

### Estrategia de reemplazo
- Cuando se necesita un buffer para cargar un nuevo bloque, se elige el que hace más tiempo que no es referenciado.
- Es una lista de bloques, donde el último es el más recientemente usado (LRU, Least Recently Used)
- Cuando un bloque se referencia o entra en la cache queda al final de la lista
- No se mueven los bloques en la memoria: se
asocian punteros.
- Otra alternativa: Least Frecuently Used. Se reemplaza el que tenga menor número de
referencias

# Buffer Cache Unix System V

## Objetivo y estructura
- Minimizar la frecuencia de acceso a
disco
- Es una estructura formada por buffers
- El kernel asigna un espacio en la memoria durante la inicialización para esta estructura.
- Un buffer tiene dos partes: el header y el lugar donde se almacena el bloque de disco traído a memoria

### El header
- Identifica por nro. de dispositivo y nro. de bloque
- Estado
- Punteros a:
    - 2 punteros para la hash queue
    - 2 punteros para la free list
    - 1 puntero al bloque en memoria

### Estados de los buffers
- Free o disponible
- Busy o no disponible (en uso por algún
proceso)
- Se está escribiendo o leyendo del
disco.
- Delayed Write (DW): buffers modificados en memoria, pero los cambios no han sido reflejados en el bloque original en disco.

#### Free List
- Organiza los buffers disponibles para ser
utilizados para cargar nuevos
bloque de disco. 
- No necesariamente los buffers están
vacios
- Se ordena según LRU
(least recent used)

#### Hash Queues
- Son colas para optimizar la búsqueda de un
buffer en particular
- Se organizan según una función de hash
usando (dispositivo,#bloque)

### Funcionamiento del buffer cache
Cuando un proceso quiere acceder a un archivo, utiliza su inodo para localizar los bloques de datos donde se encuentra éste.

El requerimiento llega al buffer cache quien evalúa si puede satifacer el requerimiento o si debe realizar la E/S.

Se pueden dar 5 escenarios:
1) El kernel encuentra el bloque en la hash queue y el buffer está
libre.
2) El kernel no encuentra el bloque en la hash queue y utiliza un
buffer libre.
3) Idem 2, pero el bloque libre esta marcado como DW.
4) El kernel no encuentra el bloque en la hash queue y la free list
está vacía.
5) El kernel encuentra el bloque en la hash queue pero está BUSY.

__Ver ejemplo de cada escenario a partir de la diapositiva 14 tema 6 bufferCache__

### Algoritmo de asignación
- Escenarios:
1) El kernel encuentra el bloque en la hash queue y el buffer está libre.
2) El kernel no encuentra el bloque en la hash queue y utiliza un buffer libre.
3) Idem 2, pero el bloque libre esta marcado como DW.
4) El kernel no encuentra el bloque en la hash queue y la free list está vacía.
5) El kernel encuentra el bloque en la hash queue pero está BUSY.