#include <stdio.h>
int factorial (int num){
 if (num <= 1)
  return 1;
 else
  return (num * factorial(num-1));
 
}
int main(){

 int x,f;
 
 printf("Ingrese un numero: \n");
 scanf("%d",&x);
 if (x != 0)
  f = factorial(x);
 printf("El factorial de %d es: %d\n",x,f);

 return 0;

}
