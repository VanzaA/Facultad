#!/bin/bash
if [  $# -ne 3 ]
then
	echo "no se mandaron bien los parametros"
	exit 1
else
	case $2 in
		"-a" )
			for FILE in $(ls $1)
			do
			 	 mv $1/$FILE $1/$FILE$3
			done
		;;
		"-b" )
			for FILE in $(ls $1)
                        do
                           	 mv $1/$FILE $1/$3$FILE
                        done
		esac
fi
exit 0
