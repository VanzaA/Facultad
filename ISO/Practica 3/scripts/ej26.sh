#!/bin/bash
if [ $# -eq 0 ]
then
	echo "no se ingresaron parametros"
	exit 1
else
	noex=0
	for (( i=1; i<=$#; i=`expr i + 2` ))
	do
		if [ -f $("$i") ]
		then
			echo "$($i) es un archvo"
		elif [ -d $("$i") ]
		then
			echo $("$i")"es un directorio"
		else
			noex++
		fi
	done
	echo "la cantidad de archivos no existentes son: " $noex
fi
