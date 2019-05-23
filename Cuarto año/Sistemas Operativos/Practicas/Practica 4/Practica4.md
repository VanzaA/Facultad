# File Systems
1. ¿Qué es un file system?

- Un file system es una abstracción que permite la creación,
eliminación, modificación y búsqueda de archivos y su organización
en directorios
- También administra el control de acceso a los archivos y el
espacio en disco asignado a él
- File systems operan sobre bloques de datos (conjunto
consecutivo de sectores fı́sicos)
- Archivos son almacenados en una estructura jerárquica de
tipo árbol ( “/” en Linux, “C” en Windows)
- Define convenciones para el nombrado de los archivos
- File systems usados en discos, CDs, etc.; otros proveen acceso por la red (NFS, SMB, etc.); otros son virtuales (procfs, sysfs)

2. Describa las principales diferencias y similitudes entre los file systems: FAT, NTFS, Ext(2,3,4),
XFS y HFS+

## Ext2
- Second Extended Filesystem (Ext2). Introducido en 1994
- Primer sector de la partición no es administrado por Ext2
- Dividido en “block groups” de igual tamaño (excepto el
último). Estos reducen la fragmentación y aumentan la velocidad de acceso
- “Superblock” y “Group Descriptors Table” replicados en block
groups para backup
- Por cada bloque existe un “Group Descriptor”
- “Block Bitmap” e “Inode Bitmap” indican si un bloque de datos o inodo está libre u ocupado
- Tabla de inodos consiste de una serie de bloques consecutivos donde cada uno tiene un número predefinido de inodos. Todos los inodos de igual tamaño: 128 bytes (por default)
Cada archivo en el file system es representado por un inodo
(index node)
- Inodos contienen metadata de los archivos y punteros a los bloques de datos
- Metadata: permisos, owner, grupo, flags, tamaño, número bloques usados, tiempo acceso, cambio y modificación, etc.
- Nombre del archivo no se almacena en el inodo
- Atributos extendidos, como las ACLs, se almacenan en un bloque de datos (campo en el inodo llamado “i file acl))
- Datos se almacenan en bloques de 1024, 2048 ó 4096 bytes. Elegible al momento de generar el file system. No se puede
modificar

## Ext3
- Ext3, Third Extended FileSystem, es la evolucion de Ext2
- Introducido en 2001. Disponible desde la version de kernel
2.4.15
- Su principal mejora se basa en la incorporación del “journaling”, que permite reparar posibles inconsistencias en el file system
- Journaling: journal, ordered, writeback
- Es compatible con ext2
- Tamaño máximo de archivo 2TB. Tamaño máximo de File System: 32 TB (ambos igual que en ext2)
-  Cantidad máxima de subdirectorios: 32000

## Ext4
- Ext4, Fourth Extended FileSystem, es la evolucion de Ext2
- Introducido en 2006. Disponible desde la version de kernel 2.6.19
- Sistema de archivos de 64 bits (FS de 1EB, files de 16TB)
- Cantidad máxima de subdirectorios: 64000 (extendible)
- Tamaño inodo: 256 bytes (timestamps más precisos, ACLs).
- Uso de extents: descriptor que representa un rango contiguo de bloques fı́sicos
- Cada extent puede respresentar 2 15 bloques (128MB con bloques de 4KB, 4 extents por inodo)
- Para archivos más grandes se utiliza un árbol de extents
- Mejor alocación de bloques para disminuir la fragmentación e incrementar el throughput: “persistent preallocation”, “delay and multiple block allocation”

## XFS
- Desarrollado por SGI (Silicon Graphics Inc.) para IRIX (su versión de UNIX)
- En 2000, SGI lo liberó bajo una licencia de código abierto
- Incorporado a Linux desde la versión 2.4.25
- Red Hat 7.0 lo incluye como su FS default (CentOS desde la versión 7.2)
- File System de 64 bits (16 EB max. file system, 8EB max. file size)
- FS dividio en regiones llamadas “allocation groups”. Uso de extents. Inodo asignados dinámicamente.
- Journaling (primer FS de la familia UNIX en tenerlo)
- Mayor espacio para atributos extendidos (hasta 64KB)
- Contra: no es posible achicar un FS de este tipo

## FAT
- The File Allocation Table (FAT) es un sistema de archivos simple diseñado originalmente para discos pequeños y estructuras de carpetas simples.

- El sistema de archivos FAT recibe su nombre por su método de organización, la tabla de asignación de archivos(the file allocation table), que reside al principio del volumen. Para proteger el volumen, se guardan dos copias de la tabla, en caso de que una se dañe.

- Además, las tablas de asignación de archivos y la carpeta raíz deben almacenarse en una ubicación fija para que los archivos necesarios para iniciar el sistema puedan ubicarse correctamente.

- Un volumen formateado con el sistema de archivos FAT se asigna en grupos. El tamaño predeterminado del clúster está determinado por el tamaño del volumen. Para el sistema de archivos FAT, el número de grupo debe caber en 16 bits y debe ser una potencia de dos.

## NTFS

- NTFS (New Technology File System) es un sistema de archivos de registro en diario desarrollado por Microsoft. Es el sistema de archivos predeterminado para las versiones modernas de Windows. NTFS es compatible con algunas funciones avanzadas del sistema de archivos, como la compresión de archivos, el cifrado de archivos, las cuotas de disco, el cambio de tamaño, el Servicio de instantáneas de volumen, etc.

- En Linux, es más probable que encuentre NTFS en una partición de inicio de Windows en una configuración de inicio dual. Linux puede con seguridad NTFS y puede sobrescribir archivos existentes, pero no puede escribir nuevos archivos en una partición NTFS.

- NTFS admite nombres de archivos de hasta 255 caracteres, tamaños de archivos de hasta 16 EB y sistemas de archivos de hasta 16 EB.

## HFS+
- HFS Plus o HFS+ es un sistema de archivos desarrollado por Apple Inc. para reemplazar al HFS (Sistema jerárquico de archivos). También es el formato usado por el iPod al ser formateado desde un Mac. HFS Plus también es conocido como HFS Extended y Mac OS Extended. Durante el desarrollo, Apple se refirió a él con el nombre clave Sequoia.

- HFS Plus es una versión mejorada de HFS, soportando archivos mucho más grandes (Bloques direccionables de 32 bits en vez de 16) y usando Unicode (En vez de Mac OS Roman) para el nombre de los archivos, lo que además permitió nombres de archivo de hasta 255 letras.

- HFS Plus permite nombres de fichero de hasta 255 caracteres de longitud UTF-16, y archivos n-bifurcados similares a NTFS, aunque casi ningún software se aprovecha de bifurcaciones con excepción de la bifurcación de los datos y de la bifurcación del recurso. HFS Plus también utiliza tabla de asignación de 32 bits, en lugar de los 16 bits de HFS. Ésta era una limitación seria de HFS, significando que ningún disco podría apoyar más de 65.536 bloques de la asignación sobre de HFS.

- Cuando los discos eran pequeños, esto no tenía mayores problemas, pero cuando el tamaño de los discos comenzaron a acercarse a la marca de 1 GB, la cantidad de espacio mínima que requería cualquier archivo (un solo bloque de la asignación) llegó a ser excesivamente grande, perdiendo cantidades significativas de espacio de disco. Por ejemplo, en un disco de 1 GB, el tamaño de bloque de la asignación debajo de HFS es 16 KB, es decir un 1 archivo de un byte tomaría 16 KB de espacio de disco. Como HFS, HFS Plus emplea una estructura Árbol-B* para almacenar la mayoría de los metadatos del volumen.


3. En ext4, describa las siguientes características: extents, multiblock allocation, delay allocation y persistent pre-allocation (https://kernelnewbies.org/Ext4).

## Extents

Los sistemas de archivos derivados tradicionalmente de Unix como Ext3 utilizan un esquema de mapeo de bloques indirectos para realizar un seguimiento de cada bloque utilizado para los bloques correspondientes a los datos de un archivo. Esto es ineficiente para archivos grandes, especialmente en operaciones de eliminación y truncado de archivos grandes, porque el mapeo mantiene una entrada para cada bloque individual, y los archivos grandes tienen muchos bloques -> mapeos enormes, de lento manejo. Los sistemas de archivos modernos utilizan un enfoque diferente llamado "extensiones". Una extensión es básicamente un montón de bloques físicos contiguos. Básicamente dice "Los datos están en los siguientes n bloques". Por ejemplo, un archivo de 100 MB se puede asignar a una sola extensión de ese tamaño, en lugar de tener que crear la asignación indirecta para 25600 bloques (4 KB por bloque). Enormes archivos se dividen en varias extensiones. Las extensiones mejoran el rendimiento y también ayudan a reducir la fragmentación, ya que una medida fomenta diseños continuos en el disco.

## Multiblock Alocation

Cuando Ext3 necesita escribir nuevos datos en el disco, hay un asignador de bloques que decide qué bloques libres se utilizarán para escribir los datos. Pero el asignador de bloque Ext3 solo asigna un bloque (4KB) a la vez. Eso significa que si el sistema necesita escribir los datos de 100 MB mencionados en el punto anterior, deberá llamar al asignador de bloques 25600 veces (¡y solo fue de 100 MB!). No solo esto es ineficiente, no permite que el asignador de bloques optimice la política de asignación porque no sabe cuántos datos totales se están asignando, solo conoce un solo bloque. Ext4 usa un "asignador multibloque" (mballoc) que asigna muchos bloques en una sola llamada, en lugar de un solo bloque por llamada, evitando una gran cantidad de sobrecarga. Esto mejora el rendimiento, y es particularmente útil con la asignación retrasada y la extensión. Esta característica no afecta el formato del disco.

## Delay alocation

Delay alocation o "asignación retrasada" es una característica de rendimiento (no cambia el formato del disco) que se encuentra en algunos sistemas de archivos modernos como XFS, ZFS, btrfs o Reiser 4, y consiste en retrasar la asignación de bloques tanto como sea posible, contrariamente a lo que tradicionalmente, los sistemas de archivos (como Ext3, reiser3, etc.) sí: asignan los bloques tan pronto como sea posible. Por ejemplo, si un proceso escribe, el código del sistema de archivos asignará inmediatamente los bloques donde se ubicarán los datos, incluso si los datos no se escriben ahora en el disco y se mantendrán en la caché para algunos hora. Este enfoque tiene desventajas. Por ejemplo, cuando un proceso está escribiendo continuamente en un archivo que crece, las escrituras sucesivas asignan bloques para los datos, pero no saben si el archivo seguirá creciendo. Por otra parte, la asignación retrasada no asigna los bloques inmediatamente cuando el proceso escribe, sino que retrasa la asignación de los bloques mientras el archivo se mantiene en caché, hasta que realmente se va a escribir en el disco. Esto le da al asignador de bloque la oportunidad de optimizar la asignación en situaciones donde el sistema anterior no pudo. La asignación demorada juega muy bien con las dos características anteriores mencionadas, extensiones y asignación multibloque, porque en muchas cargas de trabajo, cuando el archivo se escribe finalmente en el disco, se asignará en extensiones cuya asignación de bloques se realice con el asignador mballoc. El rendimiento es mucho mejor y la fragmentación se mejora mucho en algunas cargas de trabajo.

## Persistent preallocation

Esta función, disponible en Ext3 en las últimas versiones del kernel, y emulada por glibc en los sistemas de archivos que no la admiten, permite a las aplicaciones preasignar espacio en el disco: las aplicaciones le dicen al sistema de archivos preasignar el espacio, y el sistema de archivos preasigna los bloques necesarios y Estructuras de datos, pero no hay datos hasta que la aplicación realmente necesite escribir los datos en el futuro. Esto es lo que hacen las aplicaciones P2P por sí mismas cuando "preasignan" el espacio necesario para una descarga que durará horas o días, pero se implementa de manera mucho más eficiente por el sistema de archivos y con una API genérica. Esto tiene varios usos: primero, para evitar que las aplicaciones (como las aplicaciones P2P) lo hagan de manera ineficiente rellenando un archivo con ceros. Segundo, para mejorar la fragmentación, ya que los bloques se asignarán de una vez, lo más contiguamente posible. Tercero, para garantizar que las aplicaciones siempre tengan el espacio que saben que necesitarán, lo cual es importante para las aplicaciones RT-ish, ya que sin la preasignación, el sistema de archivos podría llenarse en medio de una operación importante. La función está disponible a través de la interfaz libc posix_fallocate().

4. ¿Qué es el particionado? ¿Qué es el UUID? ¿Para qué se lo utiliza? (Hint: ver el comando blkid)
## particionado
Discos pueden ser utilizados completamente por un “file
system” o pueden ser “particionados”. Particiones son subdivisiones de un disco entero. SO laspresenta como un dispositivo de bloques (como si fuera el disco entero). Están definidas en un área especial del disco llamado “partition table”.Cada partición contiene un “file system” especı́fico o es una “raw partition”. Existen varios tipos de tablas de particiones. Dos más utilizadas:
- Master Boot Record (MBR)
- Globally Unified Identifier Partition Table (GPT)

## UUID

Los UUID son números largos de 128 bits representados por 32 dígitos hexadecimales y que se utilizan en el desarrollo de software para identificar de manera única la información sin más contexto. Se describen en RFC 4122, un ejemplo de UUID es:

>13152fae-d25a-4d78-b318-74397eb08184

Los UUID son probablemente más conocidos en Linux como identificador de dispositivos de bloque. El mundo de Windows conoce los UUID en forma de identificadores únicos globales de Microsoft, GUID, que se utilizan en el Modelo de objetos componentes de Microsoft.

Los UUID se generan en varias variantes: originalmente, la mayoría de ellos se derivaban del MAC de la computadora, más tarde se utilizaron sumas de nombres de hash

5. ¿Es necesario tener un file system para acceder a una partición?



6. ¿Qué es el área de swap en Linux? ¿Existe un área similar en Windows?

Swap es un espacio de intercambio que utiliza el disco duro, en lugar de la RAM para almacenar datos temporalmente.

Microsoft Windows usa un fichero de intercambio desde su versión 3.1 (1992), la primera en usar memoria virtual. Lo implementa mediante un fichero situado en el directorio raíz (C:\) o en el de sistema (C:\WINDOWS\), y tiene por nombre:

386SPART.PAR en Windows 3.1
WIN386.SWP en Windows 3.11, 95 y 98
pagefile.sys en Windows NT y sucesores
Este fichero tiene un tamaño variable (depende de la configuración) y no debe ser movido o borrado.

7. ¿Qué función cumple el directorio lost+found en Linux?

En los sistemas Unix, cada una de las particiones/sistema de ficheros cuenta con un directorio llamado /lost+found en el cual se almacenan ficheros y directorios (o restos de ellos…) recuperados tras una revisión del sistema de ficheros  a través de la herramienta fsck, todo ello provocado habitualmente por cuelgues del sistema, apagados forzados del equipo, cortes de corriente, etc.

8. En Linux, ¿dónde se almacena el nombre y los metadatos de los archivos?



9. Seleccione uno de sus file systems (una partición) y conteste usando el comando dumpe2fs:
- ¿Qué información describe el comando dumpe2fs?

dumpe2fs imprime el superbloque y el bloque de información del grupo para el sistema de archivos presente en el dispositivo.

- ¿Cuál es el tamaño de bloque del file system?

4096

- ¿Cuántos inodos en total contiene el file system? 

29525842


- ¿Cuántos archivos como máximo se pueden crear con el estado actual del file system?
- ¿Cuántos grupos de bloques existen?


32768
- ¿Cómo haría para incrementar la cantidad de inodos de un file system?


10. ¿Qué es el file system procfs? ¿Y el sysfs?

## Procfs

- ProcFS es un pseudo-filesystem montado comúnmente en el directorio /proc
- Provee una interface a las estructuras de datos del kernel
- Presenta información sobre procesos y otra información del sistema en una estructura jerárquica de “files”
- No existe en disco, el kernel lo create en memoria (generalmente tamaño 0 de los “files”)
- Mayorı́a de los “files” de solo lectura, aunque algunos pueden ser modificados (/proc/sys)
- echo 1 > /proc/sys/net/ipv4/ip forward (o con el comando sysctl)
- /proc/pid: dir. con información del proceso “pid”
- /proc/filesystems: lista los FS soportados por el kernel
- /proc/meminfo: información del uso de memoria fı́sica y swap

## SysFS
- Con el paso del tiempo, /proc se convirtió en un verdadero desorden
- En Linux 2.6 se implementó un nuevo sistema de archivos virtual llamado “Sysfs”
- SysFS exporta información sobre los dispositivos de hardware y sus controladors desde el kernel hacia el espacio del usuario
- También permite la configuración de parámetros
- SysFS se monta en /sys

11. Consultando el sysfs, ¿cuál es el tamaño del sector lógico de su disco? ¿Y el físco?

cat class/block/sda/queue/logical_block_size 
512

cat class/block/sda/queue/physical_block_size 
4096

12. Usando el directorio /proc, contestar:
- ¿Cuál es la versión de SO que tiene instalado?

vanza@Vanza:/proc$ cat version

Linux version 4.15.0-47-generic (buildd@lgw01-amd64-001) (gcc version 7.3.0 (Ubuntu 7.3.0-16ubuntu3)) #50-Ubuntu SMP Wed Mar 13 10:44:52 UTC 2019
- ¿Cuál es procesador de su máquina?
vanza@Vanza:/proc$ cat cpuinfo

model name	: Intel(R) Core(TM) i3-5015U CPU @ 2.10GHz

- ¿Cuánta memoria RAM disponible tiene?

    cat meminfo
    - MemTotal:        3950452 kB
    - MemFree:          341880 kB
    - MemAvailable:    1313308 kB


- ¿Qué archivo debería consultar si se quiere ver el mismo resultado que el comando lsmod?

cat modules

13. Usando el comando stat, contestar:

- ¿Cuándo fue la última vez que se modificó el archivo /etc/group?

    stat /etc/group
    - File: /etc/group
    - Size: 1010      	Blocks: 8          IO Block: 4096   regular file
    - Device: 801h/2049d	Inode: 3410618     Links: 1
    - Access: (0644/-rw-r--r--)  Uid: (    0/    root)   Gid: (    0/    root)
    - Access: 2019-05-22 16:55:06.299545629 -0300
    - Modify: 2019-05-22 16:55:06.199543090 -0300
    - Change: 2019-05-22 16:55:06.243544207 -0300
    - Birth: -

- ¿Cuál es la diferencia entre los datos Cambio (Change) y Modificación (Modify)?

Modificar: la última vez que se modificó el archivo (se modificó el contenido)

Cambio: la última vez que se cambiaron los metadatos del archivo (por ejemplo, permisos)

- ¿Cuál es el inodo que ocupa? ¿Cuántos bloques ocupa?

Inode: 3410618  -  Blocks: 8 

- ¿Qué número de inodo ocupa el directorio raíz?

    Inode: 2

- ¿Es posible conocer la fecha de creación de un file en Linux? ¿Por qué?



14. Los permisos por defecto de Linux al crear un archivo o directorio son 666 y 777 respectivamente. Cree un nuevo archivo y analice sus permisos. ¿Es así? ¿Por qué sucede esto?



15. ¿Qué es un link simbólico? ¿En qué se diferencia de un hard-link?

- Hard-Link

A cada archivo enlazado se le asigna el mismo valor de Inode que el original, por lo tanto, hacen referencia a la misma ubicación física del archivo. Los enlaces duros son más flexibles y permanecen vinculados incluso si los archivos originales o vinculados se mueven a través del sistema de archivos, aunque los enlaces duros no pueden cruzar diferentes sistemas de archivos.

El comando ls -l muestra todos los enlaces con la columna de enlaces muestra el número de enlaces.
Los enlaces tienen contenidos de archivos reales.

Al eliminar cualquier enlace, solo se reduce el número de enlaces, pero no afecta a otros enlaces.
No podemos crear un enlace fijo para un directorio para evitar bucles recursivos.
Si se elimina el archivo original, el enlace seguirá mostrando el contenido del archivo.
El comando para crear un enlace duro es:
> $ ln [nombre del archivo original] [nombre del enlace]
- Enlace simbolico

Un enlace flexible es similar a la función de acceso directo de archivo que se utiliza en los sistemas operativos de Windows. Cada archivo vinculado por software contiene un valor de Inode separado que apunta al archivo original. Al igual que los enlaces duros, cualquier cambio en los datos en cualquiera de los archivos se refleja en el otro. Los enlaces suaves pueden vincularse a través de diferentes sistemas de archivos, aunque si el archivo original se elimina o se mueve, el archivo vinculado no funcionará correctamente (llamado enlace pendiente).

El comando ls -l muestra todos los enlaces con el primer valor de columna l el enlace apunta al archivo original.
Soft Link contiene la ruta del archivo original y no el contenido.

La eliminación del enlace flexible no afecta a nada, pero al eliminar el archivo original, el enlace se convierte en un enlace "colgante" que apunta a un archivo no existente.

Un enlace suave puede enlazar a un directorio.

Enlace entre sistemas de archivos: si desea vincular archivos a través de los sistemas de archivos, solo puede usar enlaces simbólicos / enlaces simbólicos.
El comando para crear un enlace suave es:
>$ ln -s [nombre del archivo original] [nombre del enlace]


16. Si se tiene un archivo llamado prueba.txt y se le genera un link simbólico, ¿qué sucede con el link simbólico si se elimina el archivo prueba.txt? ¿Y si el link fuese hard-link?

el link simbolico pierde los datos mientras que el hardlink los mantiene

17. ¿Para qué sirven los permisos especiales en Linux? Analizar el Sticky-bit, SUID y SGID

Sticky bit

El Sticky bit se utiliza para permitir que cualquiera pueda escribir y modificar sobre un archivo o directorio, pero que solo su propietario o root pueda eliminarlo. Un ejemplo de uso es el directorio /tmp, que debe tener permisos para ser utilizado por cualquier proceso, pero solo el dueño o root puede eliminar los archivos que crea.

SUID

El bit SUID activo en un archivo significa que el que lo ejecute va a tener los mismos permisos que el que creó el archivo. Esto puede llegar a ser muy util en algunas situaciones pero hay que utilizarlo con cuidado, dado que puede generar grandes problemas de seguridad.

Para que sea efectivo el archivo debe tener permisos de ejecución.


SGID

El SGID es lo mismo que en el SUID, pero a nivel de grupo. Es decir, todo archivo que tenga activo el SGID, al ser ejecutado, tendrá los privilegios del grupo al que pertenece.

Esto es muy usado cuando queremos configurar un directorio colaborativo: si aplicamos este bit al directorio, cualquier archivo creado en dicho directorio, tendrá asignado el grupo al que pertenece el directorio.

18. ¿Cuáles son los permisos del archivo /etc/shadow? ¿Por qué puedo modificar mi password sino soy usuario root?



19. Crear un archivo en el directorio /tmp. Si abre otra consola y se loguea con un usuario distinto, ¿puede borrar ese archivo? ¿Por qué? (Hint.: ver permisos especiales en Linux)

20. ¿Qué es Advanced Format en los disco rígidos? ¿Qué es 512e?

Advanced Format (AF) es cualquier formato de sector de disco utilizado para almacenar datos en discos magnéticos en unidades de disco duro (HDD) que excedan los 512, 520 o 528 bytes por sector, como el 4096, 4112, 4160 y 4224 -byte (4 KB) sectores de una unidad de formato avanzado (AFD). Los sectores más grandes permiten la integración de algoritmos de corrección de errores más fuertes para mantener la integridad de los datos en densidades de almacenamiento más altas.

512e

Muchos componentes de hardware y software del equipo host asumen que el disco duro está configurado alrededor de los límites del sector de 512 bytes. Esto incluye una amplia gama de elementos que incluyen conjuntos de chips, sistemas operativos, motores de base de datos, herramientas de creación de imágenes y partición de discos duros, utilidades de respaldo y sistemas de archivos, así como una pequeña fracción de otras aplicaciones de software. Para mantener la compatibilidad con los componentes informáticos heredados, muchos proveedores de unidades de disco duro admiten tecnologías de formato avanzado en los medios de grabación junto con un firmware de conversión de 512 bytes. Los discos duros configurados con sectores físicos de 4096 bytes con firmware de 512 bytes se denominan unidades de emulación de formato avanzado 512e o 512.

21. ¿Por qué es recomendable alinear las particiones?

22. ¿Qué es el sistemas de archivo F2FS?

F2FS (Flash-Friendly File System) es un sistema de archivos creado por Kim Jaegeuk en Samsung para el núcleo Linux.

La motivación para crear F2FS fue construir un sistema de archivos que desde el principio tuviera en cuenta las características de los dispositivos de almacenamiento basados en memorias flash NAND, como las unidades de estado sólido (SSD) y las tarjetas eMMC y SD, los cuales han sido ampliamente usados en ordenadores, desde dispositivos móviles hasta servidores.

Samsung eligió un enfoque log-structured file system que se adaptara a las nuevas formas de almacenamiento. F2FS también soluciona algunos de los problemas conocidos de los log-structured file system antiguos, como el efecto bola de nieve (snowball effect), los árboles errantes y la alta sobrecarga de la limpieza.