#!/bin/bash
echo "introduzca su nombre y apellido:"
read nombre apellido
echo "fecha y hora actual"
date
echo "Su apellido y nombre es:"
echo $apellido $nombre
echo "su usuario es:" `whoami`
echo "su directorio actual es:" `pwd`
echo `ls`
