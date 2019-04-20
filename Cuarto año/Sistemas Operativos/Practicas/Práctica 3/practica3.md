# Práctica 3

## Conceptos Generales

### 1. ¿Qué es una System Call?, ¿para que se utiliza?

A system call is a request for service that a program makes of the kernel. The service is generally something that only the kernel has the privilege to do, such as doing I/O. Programmers don’t normally need to be concerned with system calls because there are functions in the GNU C Library to do virtually everything that system calls do. These functions work by making system calls themselves. For example, there is a system call that changes the permissions of a file, but you don’t need to know about it because you can just use the GNU C Library’s chmod function.

### 2. ¿Para qué sirve la macro syscall?. Describa el propósito de cada uno de sus parámetros.

There are times when you want to make a system call explicitly, and for that, the GNU C Library provides the syscall function. syscall is harder to use and less portable than functions like chmod, but easier and more portable than coding the system call in assembler instructions.

syscall is most useful when you are working with a system call which is special to your system or is newer than the GNU C Library you are using. syscall is implemented in an entirely generic way; the function does not know anything about what a particular system call does or even if it is valid.

syscall is declared in unistd.h.

Function: `long int syscall (long int sysno, …)`

syscall performs a generic system call.

sysno is the system call number. Each kind of system call is identified by a number. Macros for all the possible system call numbers are defined in sys/syscall.h

The remaining arguments are the arguments for the system call, in order, and their meanings depend on the kind of system call. Each kind of system call has a definite number of arguments, from zero to five. If you code more arguments than the system call takes, the extra ones to the right are ignored.

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

The asmlinkage tag is one other thing that we should observe about this simple function. This is a #define for some gcc magic that tells the compiler that the function should not expect to find any of its arguments in registers (a common optimization), but only on the CPU's stack. Recall our earlier assertion that system_call consumes its first argument, the system call number, and allows up to four more arguments that are passed along to the real system call. system_call achieves this feat simply by leaving its other arguments (which were passed to it in registers) on the stack. All system calls are marked with the asmlinkage tag, so they all look to the stack for arguments. Of course, in sys_ni_syscall's case, this doesn't make any difference, because sys_ni_syscall doesn't take any arguments, but it's an issue for most other system calls. And, because you'll be seeing asmlinkage in front of many other functions, I thought you should know what it was about.

It is also used to allow calling a function from assembly files.

### 5. ¿Para qué sirve la herramienta strace?, ¿Cómo se usa?

strace es una utilidad de línea de comandos para comprobación de errores en el sistema operativo GNU/Linux. Permite monitorear las llamadas al sistema usadas por un determinado programa y todas las señales que éste recibe.​ Su funcionamiento es posible por una característica del núcleo linux llamada ptrace.

Su uso más común consiste en arrancarlo junto al programa al que se le efectúa el trazado, el cual imprime una lista de llamadas al sistema que dicho programa ejecuta. Es útil para averiguar la causa del fallo de un programa determinado porque informa de situaciones en las que por ejemplo, el programa está intentando acceder a un fichero que no existe o que no tiene permiso de lectura.

## Monitoreando System Calls

## Módulos y Drivers

### 1. ¿Cómo se denomina en Gnu/Linux a la porción de código que se agrega al kernel en tiempo de ejecución? ¿Es necesario reiniciar el sistema al cargarlo?. Si no se pudiera utilizar esto. ¿Cómo deberíamos hacer para proveer la misma funcionalidad en Gnu/Linux?

Los módulos del kernel son pedazos de código que han sido compilados sin estar incluídos en el kernel. Cuando se compila el kernel, se puede seleccionar que determinadas funcionalidades no sean incluidas en forma nativa en el kernel, sino como módulos, y luego estos pueden ser cargados en tiempo de ejecución.

Sin módulos el kernel seria 100% monolitico. Las funcionalidades implementadas en estas deberían entonces ser incluidas dentro del código del kernel.

### 2. ¿Qué es un driver? ¿para que se utiliza?

Device drivers take on a special role in the Linux kernel. They are distinct "black boxes" that make a particular piece of hardware respond to a well-defined internal programming interface; they hide completely the details of how the device works. User activities are performed by means of a set of standardized calls that are independent of the specific driver; mapping those calls to device-specific operations that act on real hardware is then the role of the device driver. This programming interface is such that drivers can be built separately from the rest of the kernel and "plugged in" at runtime when needed. This modularity makes Linux drivers easy to write, to the point that there are now hundreds of them available.

### 3. ¿Porque es necesario escribir drivers?

There are a number of reasons to be interested in the writing of Linux device drivers. The rate at which new hardware becomes available (and obsolete!) alone guarantees that driver writers will be busy for the foreseeable future. Individuals may need to know about drivers in order to gain access to a particular device that is of interest to them. Hardware vendors, by making a Linux driver available for their products, can add the large and growing Linux user base to their potential markets. And the open source nature of the Linux system means that if the driver writer wishes, the source to a driver can be quickly disseminated to millions of users.

### 4. ¿Cuál es la relación entre modulo y driver en Gnu/Linux?

A kernel module is a bit of compiled code that can be inserted into the kernel at run-time, such as with insmod or modprobe.

A driver is a bit of code that runs in the kernel to talk to some hardware device. It "drives" the hardware. Most every bit of hardware in your computer has an associated driver.¹ A large part of a running kernel is driver code.²

A driver may be built statically into the kernel file on disk.³ A driver may also be built as a kernel module so that it can be dynamically loaded later. (And then maybe unloaded.)

Standard practice is to build drivers as kernel modules where possible, rather than link them statically to the kernel, since that gives more flexibility. There are good reasons not to, however:

Sometimes a given driver is absolutely necessary to help the system boot up. That doesn't happen as often as you might imagine, due to the initrd feature.

Statically built drivers may be exactly what you want in a system that is statically scoped, such as an embedded system. That is to say, if you know in advance exactly which drivers will always be needed and that this will never change, you have a good reason not to bother with dynamic kernel modules.

If you build your kernel statically and disable Linux's dynamic module loading feature, you prevent run-time modification of the kernel code. This provides additional security and stability at the expense of flexibility.

Not all kernel modules are drivers. For example, a relatively recent feature in the Linux kernel is that you can load a different process scheduler. Another example is that the more complex types of hardware often have multiple generic layers that sit between the low-level hardware driver and userland, such as the USB HID driver, which implements a particular element of the USB stack, independent of the underlying hardware.

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

/dev is the location of special or device files. It is a very interesting directory that highlights one important aspect of the Linux filesystem - everything is a file or a directory. Look through this directory and you should hopefully see hda1, hda2 etc.... which represent the various partitions on the first master drive of the system. /dev/cdrom and /dev/fd0 represent your CD-ROM drive and your floppy drive. This may seem strange but it will make sense if you compare the characteristics of files to that of your hardware. Both can be read from and written to. Take /dev/dsp, for instance. This file represents your speaker device. Any data written to this file will be re-directed to your speaker. If you try 'cat /boot/vmlinuz > /dev/dsp' (on a properly configured system) you should hear some sound on the speaker. That's the sound of your kernel! A file sent to /dev/lp0 gets printed. Sending data to and reading from /dev/ttyS0 will allow you to communicate with a device attached there - for instance, your modem.

The majority of devices are either block or character devices; however other types of devices exist and can be created. In general, 'block devices' are devices that store or hold data, 'character devices' can be thought of as devices that transmit or transfer data. For example, diskette drives, hard drives and CD-ROM drives are all block devices while serial ports, mice and parallel printer ports are all character devices. There is a naming scheme of sorts but in the vast majority of cases these are completely illogical.

### 8. ¿Para qué sirven el archivos /lib/modules/<version>/modules.dep utilizado por el comando modprobe

modprobe looks through the file /lib/modules/version/modules.dep, to see if other modules must be loaded before the requested module may be loaded. This file is created by depmod -a and contains module dependencies. For example, msdos.ko requires the fat.ko module to be already loaded into the kernel. The requested module has a dependency on another module if the other module defines symbols	(variables or functions) that the requested module uses.