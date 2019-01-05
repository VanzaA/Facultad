#!/bin/bash
echo "MENU DE OPCIONES"
select opcion in $(ls) salir
do
	echo "ingrese la opcion a ejecutar"
	case $opcion in
		"salir" )
			exit 0
		;;
		*)
			bash $opcion
	esac
done
	
