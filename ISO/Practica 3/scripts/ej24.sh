#!/bin/bash
arr1=(1 2 3 4 5)
arr2=(2 4 6 8 10)

for ((i=0; i<${#arr1[*]}; i++))
do
  result=$(expr ${arr1[$i]} + ${arr2[$i]})
  echo "La suma de los elementos de la posicion $i de los vectores es $result"
done
