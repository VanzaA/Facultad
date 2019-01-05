# /bin/bash
arreglo=()
if [ $# -eq 0 ]
then
	echo "no se ingresaron la cantidad de parametros"
	exit 1
fi
usuarios=`cat /etc/passwd |cut -d":" -f1,5 |grep "users" |cut -d":" -f1`
n=0
for i in $usuarios
do
arreglo[$n]=$i
done
case $1 in
	"-b")
  		if [ $2 -lt ${#arreglo[*]} ]
    		then
    			echo ${arreglo[$2]}
    		else
    			echo "no se encuentra el dato"
    		fi
	;;
	"-l")
	  	echo "La cantidad de usuarios es "${#arreglo[*]}
	;;
	"-i")
	    	echo "Usuarios: "${arreglo[*]}
  	;;
esac
exit 0
