#!/bin/bash
if [ $# -ne 3 ]
then
	echo "cantidad de parametros incorrecta"
else
	case $2 in
	"%")
		echo  $( expr $1 / $3)	
	;;
	"-")
		echo  `expr $1 - $3`
	;;
	"+")
		echo $(expr $1 + $3)
	;;
	"%")
		echo $(expr $1 \* $3)
	esac
fi
