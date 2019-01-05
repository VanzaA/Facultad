#!/bin/bash
if [ $# -ne 1 ]
then
	echo "no se mandaron bien los parametros"
	exit 1
else
	while true
	do
		echo "buscando usuario"

		if [ "$1" == "$(who |cut -d" " -f1|grep $1)" ]
		then
			echo "se encontro el usuario logeado"
			break
		fi
		sleep 10
	done
fi
