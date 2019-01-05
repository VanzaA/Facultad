#include <stdio.h>
int main(){
 char c;
 printf("Ingrese un caracter: \n");
 c= getchar();
 while (c != EOF){

  putchar(c);
  printf("%d\n",EOF);
  printf("Ingrese un caracter: \n");
  c=getchar();
 }
 return 0;


}
