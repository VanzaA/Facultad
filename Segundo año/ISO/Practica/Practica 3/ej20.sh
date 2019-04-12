#!/bin/bash/
pila=()
print(){
	echo ${pila[*]} #imprime todos los elementos del arreglo
}

length(){
	echo ${#pila[@]} #imprime el largo del arreglo
}

pop(){
	i=`echo $(expr ${#pila[@]} - 1)`
	echo ${pila[$i]}
	unset pila[i]
}

push(){
	pila[${#pila[*]}]=$1 
}

select opcion in salir push pop print length
do
        case $opcion in
        "push")
                echo "Ingrese el numero a pushear"
                read num
                push $num
        ;;
        "pop")
                pop
        ;;
        "length")
                length
        ;;
        "print")
                echo "Elementos de la pila:"
                print
        ;;
        "salir" )
                exit 0
        ;;
        esac
done


