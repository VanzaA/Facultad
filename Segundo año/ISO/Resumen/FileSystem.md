# Administración de Archivos

> Porque necesitamos archivos?

- Almacenar grandes cantidades de
datos
- Tener almacenamiento a largo plazo
- Permitir a distintos procesos acceder
al mismo conjunto de información

## Archivo
 Entidad abstracta con nombre, Espacio lógico continuo y direccionable. Provee a los programas de datos (entrada) y permite a los programas guardar datos (salida).El programa mismo es información que
debe guardarse

> Punto de vista del Usuario
- Que operaciones se pueden llevar a
cabo
- Como nombrar a un archivo
- Como asegurar la protección
- Como compartir archivos
- No tratar con aspectos físicos
- Etc.

> Punto de vista del Diseño
- Implementar archivos
- Implementar directorios
- Manejo del espacio en disco
- Manejo del espacio libre
- Eficiencia y mantenimiento

## Sistema de Manejo de Archivos
Conjunto de unidades de software que
proveen los servicios necesarios para la
utilización de archivos: crear, borrar, buscar, copiar, leer, escribir, Etc.

- Facilita el acceso a los archivos por
parte de las aplicaciones
- Permite la abstracción al programador,
en cuanto al acceso de bajo nivel (el
programador no desarrolla el soft de
administración de archivos)

> Objetivos del SO en cuanto a archivos
- Cumplir con la gestión de datos
- Cumplir con las solicitudes del usuario.
- Minimizar / eliminar la posibilidad de perder o destruir datos
    - Garantizar la integridad del contenido de los archivos
- Dar soporte de E/S a distintos dispositivos
- Brindar un conjunto de interfaces de E/S para tratamiento de archivos.

### Tipos de Archivos
**Archivos Regulares**
- Texto Plano
    - Source File
- Binarios
    - Object File
    - Executable File

 **Directorios**

- Archivos que mantienen la estructura en el
FileSystem

### Atributos de un Archivo
- Nombre
- Identificador
- Tipo
- Localización
- Tamaño
- Protección, Seguridad y Monitoreo
    - Owner, Permisos, Password
    - Momento en que el usuario lo modifico, creo, accedio por ultima vez
    - ACLs

__ver diapositiva 12 tema 5 fileSystem 1 para ejemplo de archivos y atributos__

## Directorios
Contiene información acerca de archivos y
directorios que están dentro de él. El directorio es, en si mismo, un archivo
- Interviene en la resolución entre el nombre y el archivo mismo.
- Operaciones en directorios: buscar un archivo, crear un archivo (entrada de directorio), borrar un archivo, listar el contenido, renombrar archivos, Etc.
- El uso de los directorios ayuda con:
    - La eficiencia: Localización rápida de archivos
    - Uso del mismo Nombre de archivo:
        - Diferentes usuarios pueden tener el mismo nombre de archivo
    - Agrupación: Agrupación lógica de archivos por propiedades/funciones:
        - Ejemplo: Programas Java, Juegos, Librerias, etc.

### Estructura de Directorios
- Los archivos pueden ubicarse
siguiendo un path desde el directorio
raíz y sus sucesivas referencias (**full pathname** del archivo o **PATH absoluto**)
- Distintos archivos pueden tener el
mismo nombre pero el **fullpathname** es
único
- El directorio actual se lo llama
“directorio de trabajo (working
directory)
- Dentro del directorio de trabajo, se pueden referenciar los archivos tanto por su **PATH absoluto** como por su **PATH relativo** indicando solamente la ruta al archivo desde el directorio de trabajo.

#### Identificación absoluta y relativa
Tanto archivos como directorios se pueden
identificar de manera:
- Absoluta. El nombre incluye todo el
camino del archivo.

    - /var/www/index.html
    - C:\windows\winhelp.exe
- Relativa. El nombre se calcula relativamente al directorio en el que se esté
    - si estoy en el directorio /var/spool/mail/
        - Entonces es: ../../www/index.html

## Compartir archivos
En un ambiente multiusuario se
necesita que varios usuarios puedan
compartir archivos. Debe ser realizado bajo un esquema de protección:
- Derechos de acceso
- Manejo de accesos simultáneos

### Protección
El propietario/administrador debe ser
capaz de controlar:
- Que se puede hacer
- Derechos de acceso
- Quien lo puede hacer

#### Derechos de acceso
Los directorios también tienenpermisos, los cuales pueden permitir el acceso al mismo para que el usuario pueda usar el archivo siempre y cuando tenga permisos.

-  Execution
    - El usuario puede ejecutar
- Reading
    - El usuario puede leer el archivo,
- Appending
    - El usuario puede agregar datos pero no modificar o borrar el contenido del archivo
- Updating
    - El usuario puede modificar, borrar y agregar datos. Incluye la creación de archivos, sobreescribirlo y remover datos
- Changing protection
    - El usuario puede modificar los derechos de acceso
- Deletion
    - El usuario puede borrar el archivo

Owners (propietarios): tiene todos los derechos. Pueder dar derechos a otros usuarios. se determinan clases:
- Usuario específico
- Grupos de usuarios
- Todos (archivos públicos)

__Ver ejemplo desde la diapositiva 25 tema 5 FileSystem 1__

## Metas del Sistema de Archivos
Brindar espacio en disco a los archivos
de usuario y del sistema. Mantener un registro del espacio libre, cantidad y ubicación del mismo dentro
del disco.

>Conceptos

Sector
- Unidad de almacenamiento utilizada en
los Discos Rígidos

 Bloque/Cluster
- Conjuntos de sectores consecutivos

File System
- Define la forma en que los datos son almacenados

FAT: File Allocation Table
- Contiene información sobre en que lugar
están alocados los distintos archivos

### Pre-asignación
Se necesita saber cuanto espacio va a
ocupar el archivo en el momento de su
creación, para eso se tiende a definir espacios mucho más grandes que lo necesario y permite la posibilidad de utilizar sectores contiguos
para almacenar los datos de un archivo
> Qué pasa cuando el archivo supera el
espacio asignado?

#### Asignación Dinámica
El espacio se solicita a medida que se
necesita, los bloques de datos pueden quedar
de manera no contigua

#### Asignación Continua
Conjunto continuo de bloques son utilizados,para eso se requiere una pre-asignación
- Se debe conocer el tamaño del archivo durante su creación

File Allocation Table (FAT) es simple
- Sólo una entrada que incluye Bloque de inicio y longitud
 
El archivo puede ser leído con una única operación pero puede existir fragmentación externa, para solucionar esto existe la  **Compactación**

Problemas de la técnica: encontrar bloques libres continuos en el disco y el Incremento del tamaño de un archivo

#### Asignación Encadenada
- Asignación en base a bloques individuales cada bloque tiene un puntero al próximo bloque del archivo 

- File allocation table
    - Única entrada por archivo: Bloque de inicio y tamaño del archivo
- No hay fragmentación externa
- Útil para acceso secuencial (no random)
- Los archivos pueden crecer bajo demanda
- No se requieren bloques contiguos
- Se pueden consolidar los bloques de un
mismo archivo para garantizar cercanía de
los bloques de un mismo archivo.

#### Asignación Indexada
- Asignación en base a bloques
individuales
- No se produce Fragmentación Externa
- El acceso “random” a un archivo es eficiente
- File Allocation Table
- Única entrada con la dirección del bloque
de índices (index node / i-node)

__Ver imagen de ejemplo de cada asignacion a partir de la diapositiva 7, tema 5, FileSystem 2__

### Gestión de Espacio Libre
- Control sobre cuáles de los bloques de
disco están disponibles.
- Alternativas
    - Tablas de bits
    - Bloques libres encadenados
    - Indexación

#### Tabla de bits
Tabla (vector) con 1 bit por cada bloque de disco
- Cada entrada:
    - 0 = bloque libre
    - 1 = bloque en uso

Ventaja
- Fácil encontrar un bloque o grupo de bloques libres.

Desventaja
- Tamaño del vector en memoria: tamaño disco bytes / tamaño bloque en sistema archivo, Eje: Disco 16 Gb con bloques de 512 bytes => 32 Mb.

##### Ejemplo en diapositiva 21, tema 5, fileSystem 2

#### Bloques Encadenados
- Se tiene un puntero al primer bloque libre.
- Cada bloque libre tiene un puntero al
siguiente bloque libre
- Ineficiente para la búsqueda de bloques
libres => Hay que realizar varias operaciones de E/S para obtener un grupo libre.
- Problemas con la pérdida de un enlace
- Difícil encontrar bloques libres consecutivos

##### Ejemplo en diapositiva 23, tema 5, fileSystem 2


#### indexación (o agrupamiento)
- Variante de “bloques libres
encadenados”, el primer bloque libre contiene las direcciones de N bloques libres.
- Las N-1 primeras direcciones son
bloques libres.
- La N-ésima dirección referencia otro
bloque con N direcciones de bloques
libres.

#### Recuento
- Variante de Indexación, esta estrategia considera las situaciones de que varios bloques contiguos pueden ser solicitados o liberados a la vez (en especial con asignación contigua).
- En lugar de tener N direcciones libres
(índice) se tiene:
    - La dirección del primer bloque libre
    - Los N bloques libres contiguos que le siguen.
        - (#bloque, N siguientes bloques libres)

## UNIX - Manejo de archivos
Tipos de Archivos:
- Archivo común
- Directorio
- Archivos especiales (dispositivos /dev/sda)
- Named pipes (comunicación entre
procesos)
- Links (comparten el i-nodo, solo dentro del mismo filesystem)
- Links simbólicos (para filesystems
diferentes)

## UNIX - Estructura del Volumen
- Boot Block: Código para bootear el S.O.
- Superblock: Atributos sobre el File System
    - Bloques/Clusters libres
- I-NODE Table: Tabla que contiene todos los I-NODOS
    - I-NODO: Estructura de control que contiene la información clave de un archivo
- Data Blocks: Bloques de datos de los
archivos

## Windows - File Systems Soportados
- CD-ROM File System (CDFS)
- Universal Disk Format (UDF)
- File Allocation Table
    - FAT12
    - FAT16
    - FAT32
- New Technology File System (NTFS)

### FAT
FAT (File Allocation Table) es un sistema de archivos utilizado originalmente por DOS y Windows 9x
> ¿Porqué Windows aun soporta FAT file systems?:

 -Por compatibilidad con otro SO en sistemas multiboot, para permitir upgrades desde versiones anteriores y para formato de dispositivos como diskettes

- Las distintas versiones de FAT se diferencian por un número que indica la cantidad de bits que se usan para identificar diferentes bloques o clusters:
    - FAT12
    - FAT16
    - FAT32

-   Se utiliza un mapa de bloques del sistema de archivos, llamado FAT.
- La FAT tiene tantas entradas como bloques.
- La FAT, su duplicado y el directorio raiz se almacenan en los primeros sectores de la partición

- Se utiliza un esquema de ASIGNACION
ENCADENADA, la única diferencia es que el puntero al proximo bloque está en la FAT y no en los bloques.
- Bloques libres y dañados tienen
codigos especiales

### NTFS
NTFS es el filesystem nativo de Windows, usa 64-bit para referenciar clusters, teoricamente permite tener volumenes de hasta 16 Exabytes (16 billones de GB)
> ¿Porqué usar NTFS en lugar de FAT?

 FAT es simple, mas rápido para ciertas operaciones, pero NTFS soporta:
-   Tamaños de archivo y de discos mayores
- Mejora performance en discos grandes
- Nombres de archivos de hasta 255 caracteres
- Atributos de seguridad
- Transaccional