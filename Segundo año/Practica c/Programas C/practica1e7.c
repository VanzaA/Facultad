#include <stdio.h>
double division (int a, int b){
 double res=0;
 if (a>0 && b>0){
  if (a>b) res = a/b;
  if (b>a) res = b/a;
  if (a==b) res = 1;
 }
 if (res == 0)
  printf("No se pudo realizar la división");
 return res;
}

int main(){

 double resultado;
 int x,y;
 printf("Ingrese dos valores enteros positivos distintos de 0\n");
 scanf("%d",&x);
 scanf("%d",&y);
 resultado = division(x,y);
 if (x > y)
   printf("El resultado de la division entre %d y %d es: %f \n",x,y,resultado);
 else
   printf("El resultado de la division entre %d y %d es: %f \n",y,x,resultado);

 return 0;
}
