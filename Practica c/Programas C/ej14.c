#include <stdio.h>

int main ()
{
  int a,b;
  printf("Ponga un numero \n");  
  scanf("%i",&a);
  printf("Ponga otro numero \n");
  scanf("%i",&b);
  printf("El resultado es de: %d \n", (a<b)?a:b);
 
}



