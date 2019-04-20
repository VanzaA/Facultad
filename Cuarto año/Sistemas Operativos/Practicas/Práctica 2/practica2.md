# Práctica 2

## Conceptos teóricos

### 1. ¿Qué es el kernel de GNU/Linux? ¿Cuáles son sus funciones principales dentro del Sistema Operativo?

El Kernel es un programa que ejecuta programas y gestiona dispositivos de hardware. Es el encargado de que el software y el hardware puedan trabajar juntos. Sus principales funciones son:

-  Administración de memoria principal.
-  Administración de uso de la CPU.

Se dice que el Kernel Linux es un núcleo monolítico híbrido por dos motivos:

-  Los drivers y el código del Kernel se ejecutan en modo privilegiado.
-  Lo que lo hace híbrido es la posibilidad de cargar y descargar funcionalidad a través de módulos.

### 2. Indique una breve reseña histórica acerca de la evolución del kernel de GNU/Linux

-  En 1991 Linus Torvalds inicia la programación del kernel Linux basado en Minix[2] (Clon de Unix desarrollado por Tanembaum en 1987 con el fin de crear un SO de uso didáctico).
-  El 5 de octubre de 1991, se anuncia la primera versión “oficial” de Linux (0.02).
-  En 1992, con la release de la versión 0.12, se decide cambiar a una licencia GNU.
-  En marzo de 1994 Torvalds considera que todos los componentes del kernel estaban suficientemente maduros y lanza la versión 1.0.
-  En el año 1995 Linux se porta a arquitecturas DEC Alpha y Sun SPARC. Con el correr de los años se portó a otra decena de arquitecturas.
-  En mayo de 1996 se decide adoptar a Tux como mascota oficial de Linux.
-  En julio de 1996 se lanza la versión 2.0 y se define un sistema de nomenclatura. Se desarrolló hasta febrero de 2004 y terminó con la version 2.0.40. Esta versión comenzó a brindar soporte a sistemas multiprocesadores.
-  En 2001 se lanza la versión 2.4 y se deja de desarrollar a fines del 2010 con la 2.4.37.11. La versión 2.4 fue la que catapultó a GNU/Linux como un sistema operativo estable y robusto.
-  A fines del año 2003 se lanza la versión 2.6. Esta versión ha tenido muchas mejoras para el kernel dentro de las que se destacan soporte de threads, mejoras en la planificación y soporte de nuevo hardware.
-  El 3 de agosto de 2011 se lanza la versión 2.6.39.4 anunciándose la misma desde meses previos como la última en su revisión.
-  El 17 Julio de 2011 se lanza la versión 3.0. (No agrega mayores cambios. La decisión del cambio son los 20 años del SO y no superar los 40 números de revisión)
-  El 12 de Abril de 2015 se lanza la versión 4.0 (Una de sus principales mejoras es la posibilidad de aplicar parches y actualizaciones sin necesidad de reiniciar el SO)

### 3. Explique brevemente la arquitectura del kernel de GNU/Linux teniendo en cuenta: tipo de kernel, módulos, portabilidad, etc.

Linux es un núcleo monolítico híbrido. Los controladores de dispositivos y las extensiones del núcleo normalmente se ejecutan en un espacio privilegiado conocido como anillo 0 (ring 0), con acceso irrestricto al hardware, aunque algunos se ejecutan en espacio de usuario. A diferencia de los núcleos monolíticos tradicionales, los controladores de dispositivos y las extensiones al núcleo se pueden cargar y descargar fácilmente como módulos, mientras el sistema continúa funcionando sin interrupciones.

Aún cuando Linus Torvalds no ideó originalmente Linux como un núcleo portable, ha evolucionado en esa dirección. Linux es ahora de hecho, uno de los núcleos más ampliamente portados, y funciona en sistemas muy diversos.

### 4. ¿Cuáles son los cambios que se introdujeron en el kernel a partir de la versión 3.0? ¿ Cuál fue la razón por la cual se cambió de la versión 2 a la 3? ¿Y la razón para el cambio de la versión 3 a la 4?

    Version 3.0 was released on 22 July 2011.[23] On 30 May 2011, Torvalds announced that the big change was "NOTHING. Absolutely nothing." and asked, "...let's make sure we really make the next release not just an all new shiny number, but a good kernel too."[116] After the expected 6–7 weeks of the development process, it would be released near the 20th anniversary of Linux.

    El 12 de Abril de 2015 se lanza la versión 4.0 (Una de sus principales mejoras es la posibilidad de aplicar parches y actualizaciones sin necesidad de reiniciar el SO)

    The numbering change from 2.6.39 to 3.0, and from 3.19 to 4.0, involved no meaningful technical differentiation. The major version number was increased to avoid large minor numbers.

### 5. ¿Cómo se define el versionado de los kernels de GNU/Linux?

Linux kernel development has used three different version numbering schemes.

The first scheme was used in the run-up to version 1.0. The first version of the kernel was 0.01. This was followed by 0.02, 0.03, 0.10, 0.11, 0.12 (the first GPL version), 0.95, 0.96, 0.97, 0.98, 0.99 and then 1.0.[350] From 0.95 on there were many patch releases between versions.

After the 1.0 release and prior to version 2.6, the number was composed as "a.b.c", where the number "a" denoted the kernel version, the number "b" denoted the major revision of the kernel, and the number "c" indicated the minor revision of the kernel. 

In 2004, after version 2.6.0 was released, the kernel developers held several discussions regarding the release and version scheme and ultimately Linus Torvalds and others decided that a much shorter "time-based" release cycle would be beneficial. For about seven years, the first two numbers remained "2.6", and the third number was incremented with each new release, which rolled out after two to three months. A fourth number was sometimes added to account for bug and security fixes (only) to the kernel version. The even-odd system of alternation between stable and unstable was gone. Instead, development pre-releases are titled release candidates, which is indicated by appending the suffix '-rc' to the kernel version, followed by an ordinal number.

The first use of the fourth number occurred when a grave error, which required immediate fixing, was encountered in 2.6.8's NFS code. However, there were not enough other changes to legitimize the release of a new minor revision (which would have been 2.6.9). So, 2.6.8.1 was released, with the only change being the fix of that error. With 2.6.11, this was adopted as the new official versioning policy. Later it became customary to continuously back-port major bug-fixes and security patches to released kernels and indicate that by updating the fourth number.

On 29 May 2011, Linus Torvalds announced that the kernel version would be bumped to 3.0 for the release following 2.6.39, due to the minor version number getting too large and to commemorate the 20th anniversary of Linux. It continued the time-based release practice introduced with 2.6.0, but using the second number; for example, 3.1 would follow 3.0 after a few months. An additional number (now the third number) would be added on when necessary to designate security and bug fixes, as for example with 3.0.18; the Linux community refers to this as "x.y.z" versioning. The major version number was also later raised to 4, for the release following version 3.19.

### 6. ¿Cuáles son las razones por las cuáles los usuarios de GNU/Linux recompilan sus kernels?

-  Soportar nuevos dispositivos como, por ejemplo, una placa de video.
-  Agregar mayor funcionalidad (soporte para algún hardware específico).
-  Optimizar funcionamiento de acuerdo al sistema en el que corre.
-  Adaptarlo al sistema donde corre (quitar soporte de hardware no utilizado).
-  Corrección de bugs (problemas de seguridad o errores de programación).

### 7. ¿Cuáles son las distintas opciones para realizar la configuración de opciones de compilación de un kernel? Cite diferencias, necesidades (paquetes adicionales de software que se pueden requerir), pro y contras de cada una de ellas.

El kernel Linux se configura mediante el archivo .config. Este archivo, que reside en la raíz del directorio del kernel, contiene las instrucciones de qué es lo que el kernel debe compilar.

Existen tres interfaces que permiten generar este archivo:

-  make config: modo texto y secuencial. Tedioso.
-  make xconfig: interfaz gráfica utilizando un sistema de ventanas. No todos los sistemas tienen instalado X.
-  make menuconfig: este modo utiliza ncurses, una librería que permite generar una interfaz con paneles desde la terminal. Generalmente es el más utilizado.

Las herramientas mencionadas permiten:

-  Crear un archivo .config con las directivas de compilación
-  Configurar un kernel desde cero es una tarea tediosa y propensa a errores (kernels que no arranquen). Estas herramientas automatizan el proceso por nosotros.

### 8. Nombre al menos 5 opciones de las más importantes que encontrará al momento de realizar la configuración de un kernel para su posterior compilación.

-  Dar soporte a distintos sistemas de archivos
-  Dar soporte a Dispositivos loopback
-  ...

### 9. Indique que tarea realiza cada uno de los siguientes comandos durante la tarea de configuración/compilación del kernel

-  `make menuconfig`: permite configurar la compilacion del kernel editando el archivo .config, nos ofrece una interfaz gráfica en consola, haciendo uso de la libreria ncurses.

-  `make clean`: borra todos los objetos o archivos previamente compilados.

-  `make`: ejecuta las directivas definidas en los Makefiles. El parámetro -j seguido de un número se utiliza para indicar la cantidad de threads concurrentes queremos establecer para hacer este proceso. 

-  `make modules`: compila todos los módulos necesarios para satisfacer las opciones que hayan sido seleccionadas como módulo. Generalmente esta tarea se encuentra incluida en la compilación del kernel con el comando make.

-  `make modules_install`: Los módulos compilados deben residir en el directorio /lib/modules/version-del-kernel. Al igual que en el paso anterior, el archivo Makefile tiene una regla para instalar los módulos. El parámetro modules install es una regla del Makefile que ubica los módulos del kernel recién compilado en el directorio correspondiente.

-  `make install`: Se utiliza para instalar el kernel y otros archivos en el directorio /boot.

### 10. Una vez que el kernel fue compilado, ¿dónde queda ubicada su imagen? ¿dónde debería ser reubicada? ¿Existe algún comando que realice esta copia en forma automática?

Al terminar el proceso de compilación, la imagen del kernel quedará ubicada en directorio-del-código/arch/arquitectura/boot/.

Por convención, es recomendable almacenar en el directorio /boot la imagen compilada del kernel junto con su .config.

Por convención el código fuente del kernel se guarda en /usr/src. Sin embargo, como dicho directorio generalmente no tiene permisos de escritura para usuarios no privilegiados, el archivo se debe descomprimir en un directorio donde tengamos permisos, como el $HOME del usuario actual. Generalmente se crea un enlace simbólico llamado linux apuntando al directorio del código fuente que actualmente se está configurando.

### 11. ¿A qué hace referencia el archivo initramfs? ¿Cuál es su funcionalidad? ¿Bajo qué condiciones puede no ser necesario?

Es un archivo cpio comprimido normalmente en formato gzip que contiene un pequeño sistema de archivos que se cargará en la memoria RAM en el proceso de arranque del núcleo. El kernel lo montará, como una pequeña raíz, pues la necesita para completar algunas tareas relacionadas con módulos y controladores de dispositivos antes de poder arrancar el verdadero sistema de archivos raíz instalado en el disco duro e invocar al proceso init. Más antiguo que initramfs es initrd “disco RAM inicial”, aún ampliamente usado, y cuya función es la misma a pesar de ciertas diferencias técnicas y de funcionamiento.

Recordemos que al compilar el kernel (make menuconfig) decidimos qué controladores se integrarán dentro de kernel mismo (en el bzImage) y cuales irán fuera en forma de módulos, que puedan ser invocados desde éste si son necesarios. Pues estos módulos se integrarán precisamente en la imagen initramfs (o initrd) para que estén a disposición del kernel en el proceso de arranque del núcleo.

La creación tradicional del ramdisk initrd se hacía mediante el comando mkinitrd pero hoy en día generalmente ha sido sustituido por la creación de un initramfs mediante el comando mkinitramfs.

For most distributions, kernel modules are the biggest reason to have an initramfs. In a general distribution, there are many unknowns such as file system types and disk layouts. In a way, this is the opposite of LFS where the system capabilities and layout are known and a custom kernel is normally built. In this situation, an initramfs is rarely needed.

### 12. ¿Cuál es la razón por la que una vez compilado el nuevo kernel, es necesario reconfigurar el gestor de arranque que tengamos instalado?

Luego de instalar el kernel, para que el gestor de arranque lo reconozca simplemente deberemos ejecutar, como usuario privilegiado, el siguiente comando:  update-grub2.

### 13. ¿Qué es un módulo del kernel? ¿Cuáles son los comandos principales para el manejo de módulos del kernel?

Un módulo del kernel es un fragmento de código que puede cargarse/descargarse en el mapa de memoria del SO (Kernel) bajo demanda. 

-  Permiten extender la funcionalidad del Kernel en “caliente” (sin necesidad de reiniciar el sistema). 
-  Todo su código se ejecuta en modo Kernel (privilegiado).
-  Cualquier error en el módulo, puede colgar el SO. 
-  Permiten que el kernel se desarrolle bajo un diseño más modular. 
-  Los módulos disponibles se ubican en /lib/modules/version del kernel. 
-  Con el comando lsmod es posible ver que módulos están cargados.

### 14. ¿Qué es un parche del kernel? ¿Cuáles son las razones principales por las cuáles se deberían aplicar parches en el kernel? ¿A través de qué comando se realiza la aplicación de parches en el kernel?

Es un mecanismo que permite aplicar actualizaciones NO incrementales sobre la version base. Se basa en archivos diff (archivos de diferencia), que indican qué agregar y qué quitar. Se aplican sobre la versión base. Permiten agregar funcionalidad (nuevos drivers, correcciones menores, etc.). A veces puede resultar más sencillo descargar el archivo de diferencia y aplicarlo en vez de descargar todo el código de la nueva versión.