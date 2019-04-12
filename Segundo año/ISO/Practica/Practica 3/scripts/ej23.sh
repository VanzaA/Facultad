#! /bin/bash
num=(10 3 5 7 9 3 5 4)
imp=0
for ((i = 0; i < ${#num[@]}; i++))
do
        n=`expr ${num[$i]} % 2`
        if [ $n == 0 ]
        then
                echo ${num[$i]}
        else
                let imp++
        fi
done
echo "la cantidad de impares es" $imp


