#include <stdio.h>
int main(){
  int x = 1300;
  char buffer[4];
  printf("x = %d\n", x);
  printf("Ingresa por teclado: \"hola\"\n");
  gets(buffer);
  printf("x = %d\n", x);
return 0;
}
