# Subsistemas de entrada/salida

## interfaz de I/O - Metas
- Generalidad:
    - Es deseable manejar todos los dispositivos de I/O de una manera uniforme, estandarizada
    - Ocultar la mayoría de los detalles del dispositivo en las rutinas de niveles más “bajos” para que los procesos vean a los dispositivos, en términos de operaciones comunes como: read, write, open, close, lock, unlock
- Eficiencia
    - Los dispositivos de I/O pueden resultar extremadamente lentos respecto a la memoria y la CPU
    - El uso de la multiprogramación permite que un procesos espere por la finalización de su I/O mientras que otro proceso se ejecuta
 
 ### Aspectos de los dispositivos de I/O 
- Unidad de Transferencia
    - Dispositivos por bloques (discos):
        - Operaciones: Read, Write, Seek
    - Dispositivos por Caracter (keyboards,
mouse, serial ports)
        - Operaciones: get, put
- Formas de Acceso
    - Secuencial o Aleatorio

- Tipo de acceso
    - Acceso Compartido: Disco Rígido
    - Acceso Exclusivo: Impresora
    - Read only: CDROM
    - Write only: Pantalla
    - Read/Write: Disco

### Servicios
- Planificación: organización de los requerimientos a los dispositivos
    - Ej: Planificación de requerimientos a disco para minimizar tiempos
- Buffering – Almacenamiento de los datos en memoria mientras se transfieren
    - Solucionar problemas de velocidad entre los dispositivos
    - Solucionar problemas de tamaño y/o forma de los datos entre los dispositivos
- Caching – Mantener en memoria copia de los datos de reciente acceso para mejorar performance
- Spooling – Administrar la cola de requerimientos de un dispositivo
    - Algunos dispositivos de acceso exclusivo, nopueden atender distintos requerimientos al mismo tiempo: Por ej. Impresora
    - Spooling es un mecanismo para coordinar el acceso concurrente al dispositivo
- Reserva de Dispositivos: Acceso exclusivo
- Manejo de Errores:
    - El S.O. debe administrar errores ocurridos(lectura de un disco, dispositivo no disponible, errores de escritura)
    - La mayoría retorna un número de error o código cuando la I/O falla.
    - Logs de errores
- Formas de realizar I/O
    - Bloqueante: El proceso se suspende hasta  que el requerimiento de I/O se completa
        - Fácil de usar y entender
        - No es suficiente bajo algunas necesidades
    - No Bloqueante: El requerimiento de I/O retorna en cuanto es posible
    - Ejemplo: Interfaz de usuario que recibe input desde el teclado/mouse y se muestra en el screen.
    - Ejemplo: Aplicación de video que lee frames desde un archivo mientras va mostrandolo en pantalla.

### Subsistema de I/O - Estructuras de Datos
- El Kernel mantiene la información de estado de cada dispositivo o componente
    - Archivos abiertos
    - Conexiones de red
    - Etc.
- Hay varias estructuras complejas que representan buffers, utilización de la memoria, disco, etc.

#### Desde el Requerimiento de I/O hasta el Hardware
- Consideremos la lectura sobre un archivo en un disco:
    - Determinar el dispositivo que almacena los datos
        - Traducir el nombre del archivo en la representación del dispositivo.
    - Traducir requerimiento abstracto en bloques de disco (Filesystem)
    - Realizar la lectura física de los datos (bloques) en la memoria
    - Marcar los datos como disponibles al proceso que realizo el requerimiento
        - Desbloquearlo
    - Retornar el control al proceso

### Drivers
- Contienen el código dependiente del
dispositivo
- Manejan un tipo dispositivo
- Traducen los requerimientos abstractos en
los comandos para el dispositivo
    - Escribe sobre los registros del controlador
    - Acceso a la memoria mapeada
    - Encola requerimientos
- Comúnmente las interrupciones de los
dispositivos están asociadas a una función
del driver

- Interfaz entre el SO y el HARD
- Forman parte del espacio de memoria del
Kernel
    - En general se cargan como módulos
- Los fabricantes de HW implementan el driver en función de una API especificada por el SO
    - open(), close(), read(), write(), etc
- Para agregar nuevo HW sólo basta indicar el driver correspondiente sin necesidad de
cambios en el Kernel

##### Ejemplo en Linux
- Linux distingue 3 tipos de dispositivos
    - Carácter: I/O programa o por interrupciones
    - Bloque: DMA
    - Red: Ports de comunicaciones
- Los Drivers se implementan como módulos
    - Se cargan dinámicamente
- Debe tener al menos estas operaciones:
    - init_module: Para instalarlo
    - cleanup_module: Para desinstalarlo

- Operaciones que debe contener para I/
O
    - open: abre el dispositivo
    - release: cerrar el dispositivo
    - read: leer bytes del dispositivo
    - write: escribir bytes en el dispositivo
    - ioctl: orden de control sobre el dispositivo

Otras operaciones menos comunes
- llseek: posicionar el puntero de lectura/escritura
- flush: volcar los búferes al dispositivo
- poll: preguntar si se puede leer o escribir
- mmap: mapear el dispositivo en memoria
- fsync: sincronizar el dispositivo
- fasync: notificación de operación asíncrona
- lock: reservar el dispositivo

Por convención, los nombres de las operaciones comienzan con el nombre del dispositivo
> por ejemplo para /dev/ptr: 

int ptr_open(struct inode *inode, struct file *filp)

###Performance
- I/O es uno de los factores que mas
afectan a la performance del sistema:
    - Utiliza mucho la CPU para executar losdrivers y el codigo del subsistema de I/O
    - Provoca Context switches ante las
    interrupciones y bloqueos de los procesos
    - Utiliza el bus de mem. en copia de datos:
        - Aplicaciones (espacio usuario) – Kernel
        - Kernel (memoria fisica) - Controladora
