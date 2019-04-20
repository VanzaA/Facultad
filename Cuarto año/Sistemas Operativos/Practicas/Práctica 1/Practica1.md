# Práctica 1
**Repaso general
El objetivo de esta primera parte de la práctica es repasar los conceptos de shell scripting
aprendidos en la materia Introducción a los Sistemas Operativos. Se realizará un repaso
general sobre los comandos más comunes y su uso en scripts.**
## Preguntas de repaso

### 1. ¿Qué es la shell? ¿Para qué sirve?

el shell o intérprete de órdenes o intérprete de comandos es el programa informático que provee una interfaz de usuario para acceder a los servicios del sistema operativo.
Dependiendo del tipo de interfaz que empleen, los shells pueden ser:

- De líneas texto (CLI, Command-Line Interface, interfaz de línea de comandos),
- Gráficos (GUI, Graphical User Interface, interfaz gráfica de usuario),
- De lenguaje natural (NUI, Natural User Interface, interfaz natural de usuario).

Los shell son necesarios para invocar o ejecutar los distintos programas disponibles en la computadora.

### 2. ¿En qué espacio (de usuario o de kernel) se ejecuta?

en el espacio del usuario.

### 3. Si pensamos en el funcionamiento de una shell básica podríamos detallarlo secuencialmente de la siguiente manera:
- Esperar a que el usuario ingrese un comando
- Procesar la entrada del usuario y obtener el comando con sus parámetros
- Crear un nuevo proceso para ejecutar el comando, iniciarlo y esperar que retorne
- Presentar la salida (de haberla) al usuario
- Volver a empezar.

__Este tipo de comportamiento, típico de las shell interactivas, se conoce como REPL (Read- Eval-Print Loop, Ciclo de leer, interpretar e imprimir).__

__Analice cómo implementaría este ciclo básico de interpretación de scripts.__

Este tipo de comportamiento, típico de las shell interactivas, se conoce como REPL (ReadEval-Print Loop, Ciclo de leer, interpretar e imprimir). Analice cómo implementaría este ciclo básico de interpretación de scripts.

### 4. Investigue la system call fork:
1. ¿Qué es lo que realiza?
    
    Crea un nuevo proceso duplicando el proceso existente desde donde se llamo la funcion. El proceso existente de donde la función fue llamada se convierte en el proceso padre, y el proceso creado se convierte en el proceso hijo. Los procesos resultantes de este proceso son idénticos a su padre, salvo que tienen distinto número de proceso (PID). La operación fork crea un espacio de direcciones separado para el nuevo proceso hijo. Este tiene una copia exacta de todos los segmentos de memoria del proceso padre.
2. ¿Qué retorna?

    En caso de éxito, el PID del proceso hijo se devuelve al padre y 0 en el hijo. En
    falla, -1 se devuelve al padre,no se crea ningún proceso hijo y errno se configura adecuadamente.

3. ¿Para qué podrian servir los valores que retorna?

    A partir de los resultados se puede identificar cuál de los procesos es el padre y cuál es el hijo. También puede usarse para hacer frente a un error.

4. ¿Por qué invocaria a la misma al implementar una shell?

    Cuando se ejecuta un proceso desde una shell, la shell hace un fork() antes de ejecutar el proceso. Esto se produce así porque cuando alguien llama a una instrucción de la familia de exec esto no crea un nuevo proceso, sino que reemplaza la memoria e instrucciones del proceso actual con las del proceso que se quiere ejecutar. Así que cuando bash quiere ejecutar algo primero tiene que hacer un fork() y luego ejecutar. Si no lo hiciera así, se ejecutaría el proceso pero no podriamos acceder mas a la terminal bash.

    https://oskarth.com/unix01/


### 5. Investigue la system call exec:
1. ¿Para qué sirve?

    Exec provee una funcionalidad que permite correr archivos ejecutables dentro del contexto de un proceso existente. Exec reemplaza el programa actual en el proceso actual sin necesidad de forkear un nuevo proceso.


2. ¿Comó se comporta?


El comportamiento básico es conocido como overlay. No crea un nuevo proceso, por lo tanto el PID no cambia, pero el código, datos, heap, y el Stack del proceso son reemplazados por los del nuevo programa ejecutado. Al finalizar no se retorna al programa inicial.

3. ¿Cuáles son sus diferentes declaraciones POSIX?


### 6. Investigue la system call wait:

1. ¿Para qué sirve?

    esta llamada al sistema se usa para esperar los cambios de estado en un hijo del proceso de llamada, y Obtener información sobre el hijo cuyo estado ha cambiado. Un cambio de estado es considerado como: el hijo termino ;el hijo fue detenido por una señal; o el hijo fue reanudado por una señal. En el caso de un hijo terminado, realizar una espera permite al sistema liberar los recursos asociados con el hijo; Si no se realiza una espera, el hijo terminado permanece en un estado "zombie"

2. Sin ella, ¿qué sucedería, pensando en la implementación de la shell?

    Sin el wait no se podría esperar a que una línea de comando termine de ejecutarse para poder recibir la siguiente.

# Redes y Sistemas Operativos
**En esta sección se aprenderá cómo integrar conceptos del sistema operativo, como redirecciones y pipes, con una red TCP/IP. Para ello se utilizará principalmente la herramienta netcat, también conocida como nc. Investigue su funcionamiento y resuelva. Tip: man nc**

### 1. La herramienta netcat provee una forma sencilla de establecer una conexión TCP/IP. En una terminal levante una sesión de netcat en modo servidor, que escuche en la IP 127.0.0.1 (localhost) en un puerto a elección. En otra terminal conéctese, también vía netcat, al servidor recién levantado. Interactúe y experimente con ambas terminales.

- Cliente: nc -l 8999
- Servidor: nc localhost 8999

### 2. netcat también es bueno al momento de transmitir archivos sobre una red TCP/IP. Utili-zando dos terminales como se hizo en el ejercicio anterior, transmita el archivo /etc/passwd desde una sesión de netcat hacia la otra.
__Tip: recordar pipes y redirecciones.__

- Cliente: nc -l 8999 < /etc/passwd
- Servidor: nc localhost 8999

