# Introduccion a Threads

## Concurrencia y Paralelismo
Es común dividir un programa en diferentes “tareas” que, independientemente o colaborativamente, solucionan el problema.
Tambien es común contar con un pool de procesadores para ejecutar
nuestros programas.

> Analicemos estas situaciones
- Procesador de texto: ingreso de caracteres, auto-guardado, análisis ortográfico/gramatical
- Aplicaciones que muestran una animación, o un gráfico a medida que se ingresan datos
- Acceso simultáneo a diferentes fuentes de E/S
- Tendencia de los procesadores actuales a
contar con varios núcleos (multiprocesadores)

## Primeros SO – Procesos
- Programa en Ejecución
- Unidad de asignación de los recursos
- Conceptos relacionados con proceso:
    - Espacio de direcciones
    - Punteros a los recursos asignados (stacks, archivos, etc.)
    - Estructuras asociadas: PCB, tablas
- Único hilo de ejecución por proceso

## SO Actuales - Threads
Unidad básica de utilización de CPU

Proceso:
- Espacio de direcciones
- Unidad de propiedad de recursos
- Conjunto de threads (eventualmente uno)

Thread:
- Unidad de trabajo (hilo de ejecución)
- Contexto del procesador
- Stacks de Usuario y Kernel
- Variables propias
- Acceso a la memoria y recursos del PROCESO

## Procesos e Hilos
>Porqué dividir una aplicación en threads?
- Respuestas percibidas por los usuarios,
paralelismo/ejecución en background
    - Ejemplo: El servicio de impresión de Word ejecuta en background y nos permite seguir editando
- Aprovechar las ventajas de múltiples procesadores
    - Con n CPUs pueden ejecutarse n threads al mismo tiempo
- Características complejas
    - Sincronización
    - Escalabilidad: una cantidad de threads por proceso excesiva implica más cambios de contexto entre hilos del mismo proceso...)

### Threads - Ventajas
- Sincronización de Procesos
- Mejorar tiempos de Respuesta
- Compartir Recursos
- Economía
- Analicemos uso de RPC, o servidor de archivos

> Algunos conceptos

Hyper Threading

- Permite al software programado para ejecutar múltiples hilos (multi-threaded) procesar los hilos en paralelo dentro de un único procesador .
- Simular dos procesadores lógicos dentro de un único procesador físico
    - Duplica solo algunas “secciones” de un procesador
        - Registros de Control (MMU, Interrupciones, Estado,etc)
        - Registros de Proposito General (AX, BX, PC, Stack, etc.)
- Resultado: mejoría en el uso del procesador (entre 20 y 30%)

Sistemas Dual-core: una CPU con dos cores por procesador físico. Un circuito integrado tiene 2 procesadores completos. Los 2 procesadores combinan cache y controlador.

Sistemas Dual-processor (DP): tiene 2 procesadores físicos en el mismo chasis. Pueden estar en la misma motherboard o no. Cache y controlador independientes.

En ambos casos, las APIC (Advanced Programmable Interrupt Controllers) están separadas por procesador. De esta manera proveen administración de interrupciones x
procesador.

### Estructura de un hilo
Cada hilo dentro de un proceso contará con:
- un estado de ejecución
- un contexto de procesador
- una pila en modo usuario y otra en modo supervisor
- Almacenamiento para variables locales
- Acceso a memoria y recursos del proceso (archivos abiertos, señales, además de la parte de código y datos) que compartirá con el resto de los hilos.

La estructura de un hilo está constituida por:
- program counter
- un conjunto de registros
- un espacio de stack

### Análisis en hilos de:
- Context switch
- Creación
- Destrucción
- Planificación
- Protección

### Estados de un Thread
- Ejecución, Listo y Bloqueado
- Planificación: sobre los Threads
- Eventos sobre procesos afectan todos sus Threads

# Introduccion a IPC

## Concurrencia y Paralelismo
Es común dividir un programa en diferentes “tareas” que, independientemente o colaborativamente, solucionan el
problema, tambien es común contar con un conjunto de procesadores para ejecutar nuestras soluciones de Forma Paralela

Necesidades:
- Comunicar Procesos
    - Compartir Información entre Procesos
- Sincronizar Procesos
    - Acceso a información compartida

### Definición - Condición de carrera
El resultado final depende del orden en
que se ejecuten los procesos.

Ejemplo:
- Dos procesos P1 y P2 comparten la
variable b y c.
- Están inicializadas b=1, c=2
- P1 ejecuta b=b+c
- P2 ejecuta c=b+c
- El valor final dependera del orden de
ejecución

### Definición - Sección Crítica
Sección de código en un proceso que
accede a recursos compartidos con otros
procesos y que no puede ser ejecutada
mientras otro proceso esté en su sección
crítica
- Se protegen datos, no código
- El SO también presenta secciones
criticas.

Condiciones:
- Exclusión Mutua: Dos procesos no
pueden estar simultáneamente dentro de
sus SC.
- No se pueden hacer suposiciones en
cuanto a velocidades o cantidad de CPUs
- Ningún proceso que se ejecute fuera de su
SC puede bloquear otros procesos
- Espera Limitada: Ningún proceso tiene
que esperar “por siempre” para entrar en
su SC.

## iPC
Inter-Process Communication, mecanismo para comunicar y sincronizar procesos.

Consiste de:
- Semáforos
- Sistema de mensajes
- Memoria Compartida

### Semáforos
Es una herramienta de sincronización. Sirve para solucionar el problema de la
sección crítica y problemas de
sincronización.
Es una variable entera inicializada en un valor no negativo

Dos operaciones:
- wait (también llamadas down o p)
Decrementa el valor. El proceso no puede continuar ante un valor negativo, se bloquea.
- signal (también llamadas up o v) Incrementa el valor. Desbloqueo de un proceso que espere en el semaforo

Operaciones atómicas
- Cuando un proceso modifica su valor, otros procesos no pueden modificarlo simultáneamente.

>Esquema general de implementación

- Wait(S)
    - While S ≤ 0 do
    - no op;
    - S:=S-1

- Signal(S)
    - S:=S+1

### Pasaje de Mensajes
Dos primitivas básicas: send y receive. Se establece un link de comunicación entre dos o mas procesos.

La comunicación puede ser:
- unidireccional o bidireccional
simétrico o asimétrico.
- Directa o indirecta
- Sincrónica o asincrónica

Los mensajes de medida fija o variable.

#### Comunicación directa:
- Cada proceso que quiere comunicarse con otro deberá explícitamente indicar quien recibe o manda la comunicación
- Send (P, mensaje) Envía un mensaje al proceso P
- Receive (Q, mensaje) Recibe un mensaje desde el proceso Q

#### Comunicación Directa – Naming Asimétrico
- Send (P,message) Envía un mensaje a P
- Receive (id, message) Recibe un mensaje desde cualquier proceso. Id identifica el nombre del proceso con el que se ha establecido la comunicación.

#### Comunicación Indirecta
- usa un mailbox o port: Un mailbox puede verse como un objetodonde se ponen y sacan mensajes
- Cada mailbox tiene una identificación única

Send (A, mensaje) Envía un mensaje al 
mailbox A

Receive (A, mensaje) Recibe un mensaje desde el mailbox A

El sistema operativo debe proveer los mecanismo para que un proceso pueda:
- Crear un nuevo mailbox
- Compartir un mailbox
- Enviar y recibir mensajes a través del mailbox
- Destruir un mailbox

> Capacidad del Link: ¿Cuántos mensajes puede mantener el link?
- Cero: no puede haber mensajes esperando. Es lo que se llama Rendezvous: el emisor debe esperar que el receptor reciba el mensaje para poder mandar otro. Hay sincronismo.
- Capacidad limitada: la cola tiene una longitud
finita.
- Capacidad ilimitada: tiene una longitud “infinita”. El emisor nunca espera.
#
### Volviendo a pasaje de mensajes

Emisor y receptor pueden ser bloqueantes o no bloqueantes.

Caso receptor:
- Si el mensaje ya se mandó, lo recibe.
- Si no hay mensajes: o se bloquea o continua sin recepción

Caso emisor:
- Si hay un proceso esperando o hay capacidad en el link, enviá
- Si no hay un proceso esperando o el link se lleno: o se bloquea o continua su ejecución sin enviar

### Memoria Compartida
Tradicionalmente cada proceso cuenta con
su espacio de direcciones (Direcciones Virtuales). Un proceso NO puede acceder al espacio de otro (Protección de la memoria).
Los procesos siguen “viendo” un espacio
virtual
- Cada región compartida puede estar en diferente lugar del Espacio de Direcciones de cada
proceso.

La técnica permite a dos o mas
procesos compartir un segmento de
memoria, Permite la transferencia de datos entre procesos (Comunicación). Se requieren mecanismos de Sincronización (Semáforos).
