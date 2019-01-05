#include <stdio.h>
int suma (int n){

 int i,sum=0;
 if (n==0) return 0;
 if (n==1) return 1;
 if (n>1){ 
  for(i=1; i <=n; i++)
    sum=sum+i;
 }

 if (n<0) printf("No se pudo realizar la sumatoria\n");
 return sum;

}

int main(){
 int num;
 int resultado;
 printf("Ingrese un numero: \n");
 scanf("%d",&num);
 resultado=suma(num);
 if (num>=0)
 printf("El resultado de la sumatoria de %d es: %d\n",num,resultado);
 if (num == 250)
 printf("La suma de los numeros pares entre 0 y %d es: %d\n",num,resultado/2);
 return 0;
}

