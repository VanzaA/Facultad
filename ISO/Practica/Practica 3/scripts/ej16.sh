#!/bin/bash
if [ $# -ne 1 ]
then
	echo "no se ingresaron la cantidad de parametros necesarios"
else
		for USUARIOS in $(cat /etc/passwd | cut -d":" -f1)
		do
		  var=$( sudo find /home -user ${USUARIOS} -name "*${1}" |wc -l )
			echo "$USUARIOS -|- $var " >> report.txt
			echo "creado"
		done
fi
