# Procesos

## Definicion

Un Proceso puede informalmente entenderse como un programa en ejecución. Formalmente un proceso es "Una unidad de actividad que se caracteriza por la ejecución de una secuencia de instrucciones, un estado actual, y un conjunto de recursos del sistema asociados".​

Para entender mejor lo que es un proceso y la diferencia entre un programa y un proceso, A. S. Tanenbaum propone la analogía "Un científico computacional con mente culinaria hornea un pastel de cumpleaños para su hija; tiene la receta para un pastel de cumpleaños y una cocina bien equipada con todos los ingredientes necesarios, harina, huevo, azúcar, leche, etc." Situando cada parte de la analogía se puede decir que la receta representa el programa (el algoritmo), el científico computacional es el procesador y los ingredientes son las entradas del programa. El proceso es la actividad que consiste en que el científico computacional vaya leyendo la receta, obteniendo los ingredientes y horneando el pastel.

>Diferencias entre un programa y un proceso 

**Programa**
- Es estático
- No tiene program counter
- Existe desde que se edita hasta que se borra

**Proceso**
- Es dinámico
- Tiene program counter
- Su ciclo de vida comprende desdeque se lo “dispara” hasta que termina

### Componentes de un proceso
Proceso: Entidad de abstracción
Un proceso (para poder ejecutarse)
incluye como mínimo:
- Sección de Código (texto)
- Sección de Datos (variables globales)
- Stack(s) (datos temporarios: parámetros , variables temporales y direcciones de retorno)

#### Stack
- Un proceso cuenta con 1 o mas stacks
    - En general: modo Usuario y modo Kernel
- Se crean automáticamente y su medida se ajusta en run-time.
- Está formado por stack frames que son pushed (al llamar a una rutina) y popped (cuando se retorna de ella)
- El stack frame tiene los parámetros de la rutina(variables locales), y datos necesarios para recuperar el stack frame anterior (el contador de programa y el valor del stack pointer en el momento del llamado)


### Atributos de un proceso
- Identificación del proceso(**PID**), y del
proceso padre(**PPID**)
- Identificación del usuario que lo “disparó”
- Si hay estructura de grupos, grupo que lo disparó
- En ambientes multiusuario, desde que
terminal y quien lo ejecuto.

### Process Control Block (PCB)
- Estructura de datos asociada al proceso
(abstracción)
- Existe una por proceso.
- **Es lo primero que se crea cuando se crea un proceso y lo último que se borra cuando termina**
- Contiene la información asociada con cada proceso:
    - PID, PPID, etc
    - Valores de los registros de la CPU (PC, AC, etc)
    -Planificación (estado, prioridad, tiempo consumido, etc)
    - Ubicación (representación) en memoria
    - Accounting
    - Entrada salida (estado, pendientes, etc)

## Espacio de direccion de un proceso
- Es el conjunto de direcciones de
memoria que ocupa el proceso
    - stack, text y datos.
- No incluye su PCB o tablas asociadas
- Un proceso en **modo usuario** puede  acceder sólo a su espacio de direcciones;
- En **modo kernel**, se puede acceder a estructuras internas o a espacios de direcciones de otros procesos.

## El contexto de un proceso
- Incluye toda la información que el SO
necesita para administrar el proceso, y
la CPU para ejecutarlo correctamente.
- Son parte del contexto, los registros
de cpu, inclusive el contador de
programa, prioridad del proceso, si
tiene E/S pendientes, etc.

### Cambio de Contexto (Context Switch)
- Se produce cuando la CPU cambia de un
proceso a otro, se debe resguardar el contexto delproceso saliente, que pasa a espera y retornará después la CPU.
- Se debe cargar el contexto del nuevo
proceso y comenzar desde la instrucción
siguiente a la última ejecutada en dicho
contexto.
- Es tiempo no productivo de CPU
- El tiempo que consume depende del
soporte de HW.

## Sobre el Kernel del Sistema Operativo
- Es un conjunto de módulos de
software que se ejecuta en el procesador como cualquier otro proceso
> ¿El kernel es un proceso? Y de ser así ¿Quien lo controla?
Diferentes enfoques de diseño
__Enfoque 1 – El Kernel como entidad independiente__
- El Kernel se ejecuta fuera de todo
proceso
- Cuando un proceso es “interrumpido” o realiza una System Call, el contexto del proceso se salva y el control se pasa al Kernel del sistema operativo
- El Kernel tiene su propia región de
memoria
- El Kernel tiene su propio Stack
- Finalizada su actividad, le devuelve el
control al proceso (o a otro diferente)
- Importante:
    - **El Kernel NO es un proceso**
    - Se ejecuta como una entidad independiente en modo privilegiado

__Enfoque 2 – El Kernel “dentro” del Proceso__
- El “Código” del Kernel se encuentra dentro del espacio de direcciones de cada proceso.
- El Kernel se ejecuta en el MISMO contexto que algún proceso de usuario
- El Kernel se puede ver como una colección de rutinas que el proceso utiliza
- Dentro de un proceso se encuentra el código del programa (user) y el código de los módulos de SW del SO (kernel)
- Cada proceso tiene su propiostack (uno en modo usuario y otro en modo kernel)
- El proceso es el que se Ejecuta en Modo Usuario y el kernel del SO se ejecuta en Modo Kernel (cambio de modo)

- El código del Kernel es compartido por
todos los procesos
- En administración de memoria
veremos el “como”
- Cada interrupción (incluyendo las de System Call) es atendida en el contexto del proceso que se encontraba en ejecución
**en modo kernel**

## Estados de un proceso
En su ciclo de vida, un proceso pasa por
diferentes estados.
- Nuevo (new)
- Listo para ejecutar (ready)
- Ejecutándose (running)
- En espera (waiting)
- Terminado (terminated)

### Colas en la planificación de procesos
- Se enlazan las PCBs
- Ejemplos
    - De trabajos o procesos
        - Contiene todos los procesos en el sistema
    - De procesos listos
        - Residentes en memoria principal esperando para ejecutarse
    - De dispositivos
        - Esperando por un dispositivo de I/O
### Módulos de la planificación
- Son módulos (SW) del Kernel que
realizan distintas tareas asociadas a la
planificación, Se ejecutan ante determinados
eventos que así lo requieren:
    - Creación/Terminación de procesos
    - Eventos de Sincronización o de E/S
    - Finalización de lapso de tiempo
    - Etc

- Scheduler de long term medium y short term, su nombre proviene de la frecuencia de
ejecución.

- Otros módulos: Dispatcher y Loader.
- Pueden no existir como módulos separados de los schedulers vistos, pero la función debe cumplirse.
- Dispatcher: hace cambio de contexto, cambio de modo de ejecución...”despacha” el proceso elegido por el Short Term (es decir, “salta” a la instrucción a ejecutar).
- Loader: carga en memoria el proceso elegido por el long term.

#### Long term Scheduler
- Controla el grado de multiprogramación, es decir, la cantidad de procesos en memoria.
- Puede no existir este scheduler y absorber esta tarea el de short term

#### Medium Term Scheduler (swapping)
- Si es necesario, reduce el grado de
multiprogramación
- Saca temporalmente de memoria los
procesos que sea necesario para
mantener el equilibrio del sistema.
- Términos asociados: swap out (sacar
de memoria), swap in (volver a
memoria).

#### Short Term Scheduler
- Decide a cuál de los procesos en la cola de listos se elige para que use la CPU.
- Términos asociados: apropiativo, no
apropiativo, algoritmo de scheduling

### Sobre los estados

#### Nuevo (new)
- Un usuario “dispara” el proceso. Un
proceso es creado por otro proceso: su
proceso padre.
- En este estado se crean las estructuras asociadas, y el proceso queda en la cola de procesos, normalmente en espera de ser cargado en memoria
#### Listo para ejecutar (ready)
- Luego que el scheduller de largo plazo
eligió al proceso para cargarlo en
memoria, el proceso queda en estado
listo
- El proceso sólo necesita que se le
asigne CPU
- Está en la cola de procesos listos
(ready queue)
#### Ejecutándose (running)
- El scheduler de corto plazo lo eligió
para asignarle CPU
- Tendrá la CPU hasta que se termine el
período de tiempo asignado (quantum
o time slice), termine o hasta que
necesite realizar alguna operación de
E/S
#### En espera (waiting)
- El proceso necesita que se cumpla el
evento esperado para continuar.
- El evento puede ser la terminación de
una E/S solicitada, o la llegada de una
señal por parte de otro proceso.
- Sigue en memoria, pero no tiene la
CPU.
- Al cumplirse el evento, pasará al
estado de listo.

### Transiciones
- New-Ready: Por elección del scheduler
de largo plazo (carga en memoria)
- Ready-Running: Por elección del
scheduler de corto plazo (asignación
de CPU)
- Running-Waiting: el proceso “se pone
a dormir”, esperando por un evento.
- Waiting-Ready: Terminó la espera y
compite nuevamente por la CPU.

>Caso especial: running-ready
    Cuando el proceso termina su quantum
(franja de tiempo) sin haber
necesitado ser interrumpirlo por un
evento, pasa al estado de ready, para
competir por CPU, pues no está
esperando por ningún evento...

## Comportamiento de los procesos

Los Procesos alternan ráfagas de CPU y de
I/O.

- CPU-bound
    - Mayor parte del tiempo utilizando la CPU
- I/O-bound (I/O = E/S)
    - Mayor parte del tiempo esperando por I/O
- La velocidad de la CPU es mucho mas
rápida que la de los dispositivos de E/S
- Pensar: Necesidad de atender rápidamente
procesos I/O-bound para mantener el
dispositivo ocupado y aprovechar la CPU para
procesos CPU-bound

##Planificación
– Necesidad de determinar cual de todos los procesos que están listos para ejecutarse, se ejecutará a continuación en un ambiente
multiprogramado
 Algoritmo de Planificación
– Algoritmo utilizado para realizar la
planificación del sistema

### Algoritmos Apropiativos y No Apropiativos
- En los algoritmos Apropiativos (preemtive)
existen situaciones que hacen que el
proceso en ejecución sea expulsado de la
CPU
- En los algoritmos No Apropiativo
(nonpreemptive) los procesos se ejecutan
hasta que el mismo (por su propia cuenta)
abandone la CPU
    - Se bloquea por E/S o finaliza
    - No hay decisiones de planificación durante las interrupciones de reloj

### Categorías de los Algoritmos de Planificación
- Según el ambiente es posible requerir
algoritmos de planificación diferentes, con
diferentes metas:
    - Equidad: Otorgar una parte justa de la CPU a cada proceso
    - Balance: Mantener ocupadas todas las partes del sistema
- Ejemplos:
    - Procesos por lotes (batch)
    - Procesos Interactivos
    - Procesos en Tiempo Real

#### Procesos Batch
- No existen usuarios que esperen una
respuesta en una terminal.
- Se pueden utilizar algoritmos no
apropiativos
- Metas propias de este tipo de algoritmos:
    - Rendimiento: Maximizar el número de trabajos por hora
    - Tiempo de Retorno: Minimizar los tiempos entre el comienzo y la finalización
    - Uso de la CPU: Mantener la CPU ocupada la mayor cantidad de tiempo posible
- Ejemplos de Algoritmos:
    - FCFS – First Come First Served
    - SJF – Shortest Job First

### Procesos Interactivos
- No solo interacción con los usuarios
    - Un servidor, necesita de varios procesos para dar respuesta a diferentes requerimientos
- Son necesarios algoritmos apropiativos
para evitar que un proceso acapare la CPU
- Metas propias de este tipo de algoritmos:
    - Tiempo de Respuesta: Responder a peticiones con rapidez
    - Proporcionalidad: Cumplir con expectativas de los usuarios
        - Si el usuario le pone STOP al reproductor de música, que la música deje de ser reproducida en un tiempo considerablemente corto.

### Política Versus Mecanismo
- Existen situaciones en las que es
necesario que la planificación de uno o
varios procesos se comporte de
manera diferente
- El algoritmo de planificación debe
estar parametrizado, de manera que
los procesos/usuarios pueden indicar
los parámetros para modificar la
planificación

- El Kernel implementa el mecanismo
- El usuario/proceso/administrador utiliza los parámetros para determinar la Política
- Ejemplo:
    - Un algoritmo de planificación por prioridades yuna System Call que permite modificar laprioridad de un proceso (man nice)
    - Un proceso puede determinar las prioridades de los procesos que el crea, según la importancia de los mismos.

## Creación de procesos
- Un proceso es creado por otro proceso
- Un proceso padre tiene uno o más
procesos hijos.
- Se forma un árbol de procesos

### Actividades en la creación
- Crear la PCB
- Asignar PID (Process IDentification)
único
- Asignarle memoria para regiones
    - Stack, Text y Datos
- Crear estructuras de datos asociadas
    - Fork (copiar el contexto, regiones de
datos, text y stack)

### Relación entre procesos Padre e Hijo
> Con respecto a la Ejecución:
- El padre puede continuar ejecutándose
concurrentemente con su hijo
- El padre puede esperar a que el
proceso hijo (o los procesos hijos)
terminen para continuar la ejecución.
Con respecto al Espacio de Direcciones:
- El hijo es un duplicado del proceso
padre (caso Unix)
- Se crea el proceso y se le carga
adentro el programa (caso Windows)

### Creación de Procesos
- En UNIX:
    - system call fork() crea nuevo proceso
    - system call execve(), usada después del fork,carga un nuevo programa en el espacio dedirecciones.
- En Windows:
    - system call CreateProcess() crea un nuevo proceso y carga el programa para ejecución


### Terminación de procesos
- Ante un (exit), se retorna el control al
sistema operativo
- El proceso padre puede esperar recibir un
código de retorno (via wait)
- Proceso padre puede terminar la ejecución
de sus hijos (kill)
- La tarea asignada al hijo se terminó
- Cuando el padre termina su ejecución
- Habitualmente no se pemite a los hijos continuar, pero existe la opción.
- Terminación en cascada

### Procesos Cooperativos e Independientes
- Independiente: el proceso no afecta ni
puede ser afectado por la ejecución de
otros procesos. No comparte ningún
tipo de dato.
- Cooperativo: afecta o es afectado por
la ejecución de otros procesos en el
sistema.

#### Para qué sirven los procesos cooperativos?
- Para compartir información (por
ejemplo, un archivo)
- Para acelerar el cómputo (separar una
tarea en sub-tareas que cooperan
ejecutándose paralelamente)
- Para planificar tareas de manera tal
que se puedan ejecutar en paralelo.
