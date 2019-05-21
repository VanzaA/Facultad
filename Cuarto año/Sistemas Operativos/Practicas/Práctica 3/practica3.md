# Práctica 3

## Conceptos Generales

### 1. ¿Qué es una System Call?, ¿para que se utiliza?

Son llamados al kernel para ejecutar una función especı́fica que controla un dispositivo o ejecuta una instrucción privilegiada
- Su próposito es proveer una interfaz común para lograr portabilidad
- Su funcionalidad se ejecuta en modo Kernel pero en contexto del proceso

Las llamadas al sistema comúnmente usan una instrucción especial de la CPU que causa que el procesador transfiera el control a un código privilegiado (generalmente es el núcleo), previamente especificado. Esto permite al código privilegiado especificar donde va a ser conectado así como el estado del procesador.

Cuando una llamada al sistema es invocada, la ejecución del programa que invoca es interrumpida y sus datos son guardados, normalmente en su PCB (Bloque de Control de Proceso del inglés Process Control Block), para poder continuar ejecutándose luego. El procesador entonces comienza a ejecutar las instrucciones de código de bajo nivel de privilegio, para realizar la tarea requerida. Cuando esta finaliza, se retorna al proceso original, y continúa su ejecución. El retorno al proceso demandante no obligatoriamente es inmediato, depende del tiempo de ejecución de la llamada al sistema y del algoritmo de planificación de CPU.

### 2. ¿Para qué sirve la macro syscall?. Describa el propósito de cada uno de sus parámetros.

syscall() es una pequeña función de biblioteca que invoca la systemcall cuya interfaz de lenguaje assembler tiene el número especificado con los argumentos especificados. Emplear syscall() es útil, por ejemplo, cuando se invoca una llamada del sistema que no tiene una función de contenedor en la biblioteca de C.

syscall() guarda los registros de la CPU antes de realizar la llamada al sistema, restaura los registros al regresar de la llamada al sistema y almacena cualquier código de error devuelto por la llamada al sistema en errno (3) si ocurre un error.

Las constantes simbólicas para los números de llamadas al sistema se pueden encontrar en el archivo de encabezado <sys / syscall.h>.

syscall esta declarada en unistd.h.
Function: `long int syscall (long int sysno, …)`
syscall una system call generica.

sysno es el numero de la system call. cada tipo de system call esta identificada por un numero. Macros para todas las posibles system call estan definidas en sys/syscall.h

El resto de los argumentos son argumentos para la system call, en orden, y su significado depende del tipo de system call. cada tipo de system call tiene un numero definido de argumentos, entre 0 y 5. si envias mas argumentos que los que la system call recibe, estos van a ser ignorados.

### 3. ¿Para que sirven los siguientes archivos?

#### <kernel_code>/arch/x86/syscalls/syscall_32.tbla

```
# 32-bit system call numbers and entry vectors
#
# The format is:
# <number> <abi> <name> <entry point> <compat entry point>
```

#### <kernel_code>/arch/x86/syscalls/syscall_64.tbl

```
# 64-bit system call numbers and entry vectors
#
# The format is:
# <number> <abi> <name> <entry point>
```

### 4. ¿Para qué sirve la macro asmlinkage?

Este es un #define para hacer magia con gcc que le dice al compilador que la función no debe esperar encontrar ninguno de sus argumentos en los registros (una optimización común), sino solo en la pila de la CPU. la system_call consume su primer argumento, el número de llamada del sistema, y ​​permite hasta cuatro argumentos más que se pasan a la llamada del sistema real. system_call logra esta hazaña simplemente dejando sus otros argumentos (que se le pasaron en registros) en la pila. Todas las llamadas al sistema están marcadas con la etiqueta asmlinkage, por lo que todas buscan en la pila en busca de argumentos. Por supuesto, en el caso de sys_ni_syscall, esto no hace ninguna diferencia, porque sys_ni_syscall no acepta ningún argumento, pero es un problema para la mayoría de las otras llamadas del sistema. Y, debido a que verá un enlace de video frente a muchas otras funciones, pensé que debería saber de qué se trataba.

También se utiliza para permitir llamar a una función desde archivos de ensamblaje.

### 5. ¿Para qué sirve la herramienta strace?, ¿Cómo se usa?

strace es una utilidad de línea de comandos para comprobación de errores en el sistema operativo GNU/Linux. Permite monitorear las llamadas al sistema usadas por un determinado programa y todas las señales que éste recibe.​ Su funcionamiento es posible por una característica del núcleo linux llamada ptrace.

Su uso más común consiste en arrancarlo junto al programa al que se le efectúa el trazado, el cual imprime una lista de llamadas al sistema que dicho programa ejecuta. Es útil para averiguar la causa del fallo de un programa determinado porque informa de situaciones en las que por ejemplo, el programa está intentando acceder a un fichero que no existe o que no tiene permiso de lectura.

## Monitoreando System Calls

## Módulos y Drivers

### 1. ¿Cómo se denomina en Gnu/Linux a la porción de código que se agrega al kernel en tiempo de ejecución? ¿Es necesario reiniciar el sistema al cargarlo?. Si no se pudiera utilizar esto. ¿Cómo deberíamos hacer para proveer la misma funcionalidad en Gnu/Linux?

Los módulos del kernel son pedazos de código que han sido compilados sin estar incluídos en el kernel. Cuando se compila el kernel, se puede seleccionar que determinadas funcionalidades no sean incluidas en forma nativa en el kernel, sino como módulos, y luego estos pueden ser cargados en tiempo de ejecución.

Sin módulos el kernel seria 100% monolitico. Las funcionalidades implementadas en estas deberían entonces ser incluidas dentro del código del kernel.

### 2. ¿Qué es un driver? ¿para que se utiliza?

Dicho de manera muy simple, el driver o controlador de dispositivos es un pequeño software que conecta el sistema operativo directamente con los componentes del hardware de la PC. Por ejemplo, si tenemos una placa de vídeo instalada en la computadora, esta necesita entenderse con el sistema operativo para poder recibir las instrucciones y procesar todo correctamente; y precisamente esta es la función que cumple el controlador, un puente entre ambos. El driver le da instrucciones al sistema operativo sobre cómo debe funcionar determinado hardware y de que forma el sistema debe trabajar en conjunto para suministrarte los mejores resultados.

### 3. ¿Porque es necesario escribir drivers?

Hay varias razones para estar interesado en la escritura de drivers de Linux. La velocidad a la que el nuevo hardware se vuelve disponible (¡y se vuelve obsoleto!) Garantiza que los escritores de controladores estarán ocupados en el futuro inmediato. Es posible que las personas necesiten conocer los controladores para poder acceder a un dispositivo en particular que sea de su interés. Los proveedores de hardware, al hacer que un controlador de Linux esté disponible para sus productos, pueden agregar la amplia y creciente base de usuarios de Linux a sus mercados potenciales. Y la naturaleza de código abierto del sistema Linux significa que si el controlador del controlador lo desea, la fuente a un controlador se puede difundir rápidamente a millones de usuarios.

### 4. ¿Cuál es la relación entre modulo y driver en Gnu/Linux?

Un módulo del kernel es un código compilado que se puede insertar en el kernel en tiempo de ejecución, como con insmod o modprobe.

Un controlador es un poco de código que se ejecuta en el núcleo para comunicarse con algún dispositivo de hardware. Se "impulsa" el hardware. La mayoría de cada hardware en su computadora tiene un controlador asociado. Una gran parte de un núcleo en ejecución es el código del controlador.

Un controlador puede incorporarse estáticamente en el archivo del kernel en el disco. Un controlador también puede construirse como un módulo del kernel para que luego pueda cargarse dinámicamente.

La práctica estándar es construir controladores como módulos del kernel siempre que sea posible, en lugar de vincularlos estáticamente al kernel, ya que eso da más flexibilidad. Hay buenas razones para no hacerlo, sin embargo:

A veces, un controlador dado es absolutamente necesario para ayudar al arranque del sistema. Eso no sucede con la frecuencia que pueda imaginar, debido a la función initrd.

Los controladores construidos estáticamente pueden ser exactamente lo que usted desea en un sistema con un alcance estático, como un sistema integrado. Es decir, si sabe de antemano exactamente qué controladores siempre serán necesarios y que esto nunca cambiará, tiene una buena razón para no molestarse con los módulos dinámicos del kernel.

Si construye su kernel de forma estática y desactiva la función de carga dinámica del módulo de Linux, evita la modificación en tiempo de ejecución del código del kernel. Esto proporciona seguridad y estabilidad adicionales a expensas de la flexibilidad.

No todos los módulos del kernel son controladores. Por ejemplo, una característica relativamente reciente en el kernel de Linux es que puede cargar un programador de procesos diferente. Otro ejemplo es que los tipos de hardware más complejos a menudo tienen múltiples capas genéricas que se ubican entre el controlador de hardware de bajo nivel y la zona de usuario, como el controlador USB HID, que implementa un elemento particular de la pila USB, independientemente del hardware subyacente.

### 5. ¿Qué implicancias puede tener un bug en un driver o módulo?

“This design of modules makes it faster to load the Kernel (no need to load and initialize un-needed Modules), as well as developing Drivers (if you have a bug, just unload the Module, fix it, recompile and load it again).”

### 6. ¿Qué tipos de drivers existen en Gnu/Linux?

Podemos clasificar el hard en varios tipos:

* Dispositivos de acceso aleatorio(ej. discos).
* Dispositivos seriales(ej. Mouse, sonido,etc).

Acorde a esto los drivers se clasifican en:

* Drivers de bloques: son un grupo de bloques de datos persistentes. Leemos y escribimos de a bloques, generalmente de 1024 bytes.
* Drivers de carácter: Se accede de a 1 byte a la vez y 1 byte solo puede ser leıdo por  única vez.
* Drivers de red: tarjetas ethernet, WIFI, etc.

### 7. ¿Que hay en el directorio /dev? ¿qué tipos de archivo encontramos en esa ubicación?

/dev es la ubicación de archivos especiales o de dispositivo. Es un directorio muy interesante que destaca un aspecto importante del sistema de archivos de Linux: todo es un archivo o un directorio. Mire a través de este directorio y esperemos que vea hda1, hda2, etc. que representan las distintas particiones en la primera unidad maestra del sistema. /dev/cdrom y /dev/fd0 representan su unidad de CD-ROM y su unidad de disquete. Esto puede parecer extraño, pero tendrá sentido si compara las características de los archivos con las de su hardware. Ambos se pueden leer y escribir . Tome /dev/dsp, por ejemplo. Este archivo representa su dispositivo de altavoz. Cualquier dato escrito en este archivo será redirigido a su orador. Si prueba 'cat /boot/vmlinuz >/dev/dsp' (en un sistema configurado correctamente) debería escuchar un sonido en el altavoz. Ese es el sonido de tu núcleo! Se imprime un archivo enviado a /dev/lp0. Enviar datos y leer desde /dev/ttyS0 le permitirá comunicarse con un dispositivo conectado allí, por ejemplo, su módem.

La mayoría de los dispositivos son dispositivos de bloque o de caracteres; sin embargo existen otros tipos de dispositivos y pueden ser creados. En general, los "dispositivos de bloque" son dispositivos que almacenan o mantienen datos, los "dispositivos de caracteres" pueden considerarse como dispositivos que transmiten o transfieren datos. Por ejemplo, las unidades de disquete, las unidades de disco duro y las unidades de CD-ROM son dispositivos de bloque, mientras que los puertos serie, los ratones y los puertos de impresora paralelos son dispositivos de caracteres. Hay un tipo de esquema de nombres, pero en la gran mayoría de los casos son completamente ilógicos.

### 8. ¿Para qué sirven el archivos /lib/modules/<version>/modules.dep utilizado por el comando modprobe

modprobe examina el archivo /lib/modules/version/modules.dep para ver si se deben cargar otros módulos antes de que se cargue el módulo solicitado. Este archivo es creado por depmod -a y contiene dependencias de módulo. Por ejemplo, msdos.ko requiere que el módulo fat.ko ya esté cargado en el kernel. El módulo solicitado depende de otro módulo si el otro módulo define los símbolos (variables o funciones) que utiliza el módulo solicitado.