#include <stdio.h>

int pares (int n){
  int i , b = 0 , c = 0;
  for(i = 0; i < n; i = i + 1 ){
    b= b + 2;
    c = c + b;
}
  return c;
}

int main(){
  int a = 0;
  printf("inserte la cantidad de numeros pares que desea sumar\n" );
  scanf("%i" , &a);
  printf("el resultado es: %i\n", pares(a));
}
