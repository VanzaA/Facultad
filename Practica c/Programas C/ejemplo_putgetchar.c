#include <stdio.h>
int main (void){

int c;
  printf("\n Ingrese un caracter");

  c = getchar();

 while (c!= EOF){
  putchar(c);
  c = getchar();
 }
 return 0;

}
