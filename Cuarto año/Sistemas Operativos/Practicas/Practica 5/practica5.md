Parte 1: Conceptos teóricos

## 1 Defina virtualización. Investigue cuál fue la primer implementación que se realizó.

la virtualización es la creación a través de software de una versión virtual de algún recurso tecnológico, como puede ser una plataforma de hardware, un sistema operativo, un dispositivo de almacenamiento u otros recursos de red

El sistema pionero que utilizó este concepto fue la CP-40, la primera versión (1967) de la CP/CMS de IBM (1967-1972) y el precursor de la familia VM de IBM (de 1972 en adelante). Con la arquitectura VM, la mayor parte de usuarios controlan un sistema operativo monousuario relativamente simple llamado CMS que se ejecuta en la máquina virtual VM.


## 2 ¿Qué diferencia existe entre virtualización y emulación?

Un emulador es un programa de software que simula la funcionalidad de otro programa o un componente de hardware. Dado que implementa funcionalidad por software, proporciona una gran flexibilidad y la capacidad de recopilar información muy detallada acerca de la ejecución.

Con la virtualización, el programa huésped se ejecuta realmente en el hardware subyacente. El software de virtualización (VMM, Virtual Machine Monitor) sólo media en los accesos de las diferentes máquinas virtuales al hardware real. Así, éstas son independientes, y pueden ejecutar programas a velocidad casi nativa.

## 3 Investigue el concepto de hypervisor y responda:
## (a) ¿Qué es un hypervisor?
    
El hipervisor, también llamado monitor de máquina virtual (VMM), es el núcleo central de algunas de las tecnologías de virtualización de hardware más populares y eficaces, entre las cuales se encuentran las de Microsoft: Microsoft Virtual PC, Windows Virtual PC, Microsoft Windows Server e Hyper-V.

Los hipervisores son aplicaciones que presentan a los sistemas operativos virtualizados (sistemas invitados) una plataforma operativa virtual (hardware virtual), a la vez que ocultan a dicho sistema operativo virtualizado las características físicas reales del equipo sobre el que operan.

Los hipervisores también son los encargados de monitorizar la ejecución de los sistemas operativos invitados.

Con el uso de hipervisores es posible conseguir que múltiples sistemas operativos compitan por el acceso simultáneo a los recursos hardware de una máquina virtual de manera eficaz y sin conflictos.

## (b) ¿Qué beneficios traen los  hypervisors? ¿Cómo se clasifican?

### Hipervisor de tipo 1
Se denomina también virtualización en modo nativo y se caracteriza porque este software se instala directamente sobre el equipo haciendo las funciones tanto de sistema operativo (SO) como de virtualización.

Este método de virtualización lo utilizan sobre todo las empresas que pueden disponer de uno o varios servidores dedicados en exclusiva a la virtualización de sistemas.

Algunos de los hipervisores de tipo 1 más conocidos son los siguientes:
- VMware ESXi 
- VMware ESX Server 
- Xen 
- Citrix XenServer 
- Microsoft Hyper-V Server .    

## Hipervisor de tipo 2

El software de virtualización de tipo 2, o alojado (hosted), se caracteriza porque debe ser instalado en un equipo que cuente con un SO previo (como Debian GNU/Linux, Fedora, Microsoft Windows, Mac OS X, etc.).

Para un usuario doméstico, este es el método de virtualización apropiado y es el ideal para probar software (sistemas operativos y aplicaciones) sin riesgo de afectar a nada de lo que haya en la máquina anfitriona.

Obviamente, esta forma de virtualización es menos eficiente que la anterior, pero se puede seguir utilizando el equipo físico con otras aplicaciones: editor de texto, hoja de cálculo, navegador u otros servicios.

Algunos de los hipervisores de tipo 2 más utilizados son los siguientes:\
Oracle:

- Oracle VM VirtualBox

VMware:
- Workstation
- Server 
- Player 
- QEMU 

Microsoft:
- Virtual PC 
- Virtual Server 
#
## (c) Indique por qué un hypervisor de tipo 1 no podría correr en una arquitectura sin tecnología de virtualización. ¿Y un hypervisor de tipo 2 en hardware sin tecnología de virtualización?

#

## 4 Investigue el concepto de paravirtualización y responda:
## (a) ¿Qué es la paravirtualización?
La paravirtualización es una técnica de programación informática que permite virtualizar por software sistemas operativos. El programa paravirtualizador presenta una interfaz de manejo de máquinas virtuales. Cada máquina virtual se comporta como un computador independiente, por lo que permite usar un sistema operativo o varios por computador emulado.

## (b) ¿Sería posible utilizar paravirtualización en sistemas operativos como Windows o iOS? ¿Por qué?
#
## (c) Mencione algún sistema que implemente paravirtualización.

La paravirtualización es una expansión de una tecnología que ha existido durante años en el sistema operativo IBM conocido como VM. Xen, un proyecto de software de código abierto, incorpora paravirtualización.

## (d) Defina VMI.

VMI, o la interfaz de máquina virtual, es una especificación extensible claramente definida para la comunicación del sistema operativo con el hipervisor. VMI ofrece un gran rendimiento sin necesidad de que
los desarrolladores del kernel deben tener en cuenta los conceptos que solo son relevantes para el hipervisor. Como resultado, puede seguir el ritmo de los lanzamientos rápidos de
El kernel de Linux y una nueva versión del kernel pueden ser trivialmente paravirtualizados. Con VMI, un único binario del kernel de Linux puede ejecutarse en una máquina nativa y en uno o más hipervisores.

## (e) ¿Qué beneficios trae con respecto al resto de los modos de virtualización?

## (f) Investigue si VMI podría correr sobre hypervisors de tipo 1 ó 2, y justifique por qué.
#
## 5 Investigue sobre containers en el ámbito de la virtualización y responda:
## (a)¿Qué son?

Un contenedor es una unidad estándar de software que empaqueta el código y todas sus dependencias para que la aplicación se ejecute de forma rápida y confiable de un entorno informático a otro.

## (b) ¿Dependen del hardware subyacente?


## (c) ¿Qué lo diferencia por sobre el resto de las tecnologías estudiadas?

Los contenedores y las máquinas virtuales tienen beneficios similares de aislamiento de recursos y asignación, pero funcionan de manera diferente porque los contenedores virtualizan el sistema operativo en lugar del hardware. Los contenedores son más portátiles y eficientes.

## (d) Investigue qué funcionalidades son necesarias para poder implementar containers.