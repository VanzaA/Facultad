#!/bin/bash
if [ $# -eq 1 ]
then
	if [ -d $1 ] 
	then
		echo "es un directorio"
	elif [ -f $1 ]
	then
		echo "es un archivo"

	else
		echo "no existe"
		echo "creando directorio..."
		mkdir $1
		echo "directorio creado"
		echo "puto el que lee"
	fi
fi
