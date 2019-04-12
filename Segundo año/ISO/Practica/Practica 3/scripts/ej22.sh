#! /bin/bash
num=(10 3 5 7 9 3 5 4)
result=1
for ((i=0; i<${#num[@]}; i++))
do
	result=`expr ${num[$i]} \* $result`
done
echo "el resultado es " $result
