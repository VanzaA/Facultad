# Sistema Operativo

## Definicion
Un sistema operativo es el software principal o conjunto de programas de un sistema informático que gestiona los recursos de hardware y provee servicios a los programas de aplicación de software, ejecutándose en modo privilegiado respecto de los restantes (aunque puede que parte de él se ejecute en espacio de usuario).

En ciertos textos, el sistema operativo es llamado indistintamente como núcleo o kernel, pero debe tenerse en cuenta que la diferencia entre kernel y sistema operativo solo es aplicable si el núcleo es monolítico, lo cual fue muy común entre los primeros sistemas. En caso contrario, es incorrecto llamar al sistema operativo núcleo.

> existen 2 perspectivas

### Perspectiva de arriba hacia abajo
- Abstracción con respecto a la arquitectura

- Arquitectura, conjunto de instrucciones, organización de memoria, E/S, estructura de bus)

- El SO “oculta” el HW y presenta a los programas abstracciones más simples de manejar.
- Los programas de aplicación son los clientes del SO.


- Comparación, uso de escritorio y uso de comandos de texto

- Comodidad, “amigabilidad” (friendliness)

### Perspectiva de abajo hacia arriba

- Visión del SO como un administrador de recursos

- Administra los recursos de HW de uno o más procesos

- Provee un conjunto de servicios a los usuarios del sistema

- Maneja la memoria secundaria y dispositivos de I/O.

- Ejecución simultánea de procesos

- Multiplexación en tiempo (CPU) y en espacio (memoria)

## Objetivos de los S.O.
-  **Comodidad**
    -  Hacer mas fácil el uso de la PC
-  **Eficiencia**
    -  Hacer un uso más eficiente de los
recursos del sistema
-  **Evolución**
    -  Permitir la introducción de nuevas funciones al sistema sin interferir con funciones anteriores

## Componentes de un SO
- Kernel
- Shell
    - GUI / CUI o CLI
- Herramientas
    - Editores, Compiladores, Librerías, etc

### Kernel (Núcleo)
- “Porción de código” que se encuentra en memoria principal que se encarga de la administración de los recursos.
- Implementa servicios esenciales:
    - Manejo de memoria
    - Administración de procesos
    - Comunicación y Concurrencia
    - Gestión de la E/S
### Shell

El shell o intérprete de órdenes o intérprete de comandos es el prog    rama informático que provee una interfaz de usuario para acceder a los servicios del sistema operativo.
Dependiendo del tipo de interfaz que empleen, los shells pueden ser:

- De líneas texto (CLI, Command-Line Interface, interfaz de línea de comandos),
- Gráficos (GUI, Graphical User Interface,    interfaz gráfica de usuario),
- De lenguaje natural (NUI, Natural User Interface, interfaz natural de usuario).

Los shell son necesarios para invocar o ejecutar los distintos programas disponibles en la computadora.

## Servicios de un SO
- Administración y planificación del procesador
    - Imparcialidad, “justicia” en la ejecución (Fairness)
    - Que no haya bloqueos
    - Manejo de Prioridades
- Administración de Memoria
    - Memoria física vs memoria virtual. Jerarquías de memoria
    - Protección de programas que compiten o se ejecutan concurrentemente
- Administración del almacenamiento– Sistema de archivos
    - Acceso a medios de almacenamiento externos
-  Administración de dispositivos
    - Ocultamiento de dependencias de HW
    - Administración de accesos simultáneos
- Detección de errores y respuestas
    - Errores de HW internos y Externos
        - Errores de Memoria
        - Errores de Dispositivos
    - Errores de SW
        - Errores Aritméticos
        - Acceso no permitido a direcciones de memoria
    - Incapacidad del SO para conceder una solicitud de una aplicación
- Interacción del Usuario (Shell)
- Contabilidad
    - Recoger estadísticas del uso
    - Monitorear parámetros de rendimiento
    - Anticipar necesidades de mejoras futuras
    - Dar elementos si es necesario facturartiempo de procesamiento

## Problemas que un SO debe evitar
- Que un proceso se apropie de la CPU
- Que un proceso intente ejecutar   instrucciones de E/S por ejemplo.
- Que un proceso intente acceder a una posición de memoria fuera de su espacio declarado.
     - Proteger los espacios de direcciones

 Para ello, el SO entre otras debe:

- Gestionar el uso de la CPU
- Detectar intentos de ejecución de instrucciones de E/S ilegales
- Detectar accesos ilegales a memoria
- Proteger el vector de interrupciones

### Apoyo del Hardware
- Modos de Ejecución
- Interrupción de Clock
- Protección de la Memoria
    - Memoria Lógica Vs Física

#### Modos de ejecución
- El bit en la CPU indica el modo actual Las instrucciones privilegiadas deben ejecutarse en **modo Supervisor o Kernel**
    - necesitan acceder a estructuras del **kernel**, o ejecutar código que no es del proceso
- En **modo Usuario**, el proceso puede acceder sólo a su espacio de direcciones, es decir a las direcciones “propias”.

- El **Kernel** del SO se ejecuta en **modo supervisor**
- El resto del SO y los programas de usuario se ejecutan en **modo usuario** (subconjunto de instrucciones permitidas)

>Tener en cuenta que...

- Cuando se arranque el sistema, arranca con el bit en **modo supervisor**.
- Cada vez que comienza a ejecutarse un
proceso de usuario, este bit se **DEBE
PONER en modo usuario**.
    - Mediante una Instrucción especial.
- Cuando hay un trap o una interrupción, el
bit de modo se pone en **modo Kernel**.
    - Única forma de pasar a **Modo Kernel**

>Cómo actúa...
- Cuando el proceso de usuario intenta por sí mismo ejecutar instrucciones que pueden causar problemas (las llamadas instrucciones privilegiadas) esto el HW lo detecta como una operación ilegal y produce un trap al SO.

>Resumiendo...
- Modo kernel:
    - Modo privilegiado
    - Manejo estricto de pautas de confabilidad/seguridad
    - Manejo de:
        - CPU, memoria, operaciones de entrada/salida
        - Administración multiprocesador, diagnosticos, testing
        - Estructura del Filesystem
        - Comunicaciones: interfaz de red
- Modo user:
    - Más fexible
    - Funciones de Mantenimiento más simples, debugging
        - Compiler, assembler, interpreter, linker/loader
        - File system management, telecommunication, network management
        - Editors, spreadsheets, user applications

#### Protección de la memoria
- Delimitar el espacio de direcciones del proceso
- Poner limites a las direcciones que puede utilizar un proceso
    - Por ejemplo: Uso de un registro base y un registro límite
    - El kernel carga estos registros por medio de instrucciones privilegiadas. Esta acción sólo puede realizarse en modo Kernel
- La memoria principal aloja al SO y a los
procesos de usuario
    - El kernel debe proteger para que los procesos de usuario no puedan acceder a donde no les corresponde
    - El kernel debe proteger el espacio de direcciones de un proceso del acceso de otros procesos.
#### Protección de la E/S
- Las instrucciones de E/S se definen como privilegiadas.
- Deben ejecutarse en **Modo Kernel**
    - Se deberían gestionar en el kerneldel sistema operativo

#### Protección de la CPU
- Uso de interrupción por clock para evitar que un proceso se apropie de la CPU
- Se implementa normalmente a través de un clock y un contador.
- El kernel le da valor al contador que se decrementa con cada tick de reloj y al llegar a cero puede expulsar al proceso para ejecutar otro.
- Las instrucciones que modifican el funcionamiento del reloj son privilegiadas.
- Se le asigna al contador el valor que se quiere que se ejecute un proceso.

## System Calls

- Es la forma en que los programas de usuario acceden a los servicios del SO.

- Los parámetros asociados a las llamadas pueden pasarse de varias maneras: por registros, bloques o tablas en memoria ó la pila.

    > count=read(fle, bufer, nbytes);

- Se ejecutan en modo kernel o supervisor

### Categorias
Categorías de system calls:
- Control de Procesos
- Manejo de archivos
- Manejo de dispositivos
- Mantenimiento de información del sistema
- Comunicaciones

>Ejemplo - System Call Linux

- Para activar iniciar la system call se indica:
    - el número de syscall que se quiere
ejecutar
    - los parámetros de esa syscall
- Luego se emite una interrupción para pasar a modo Kernel y gestionar la systemcall
- El manejador de interrupciones evalúa la system call deseada y la ejecuta