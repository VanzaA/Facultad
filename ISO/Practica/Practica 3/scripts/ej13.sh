#!/bin/bash
echo "desea listar, ver quien esta conectado, o ver quien esta"
read opcion
case $opcion in
	"listar" )
		ls
	;;
	"donde" )
		pwd
	;;
	"who" )
		who
	;;
esac
