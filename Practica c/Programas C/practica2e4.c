#include <stdio.h>
int main(){

 int lineas, suma=0;
 char c;
 lineas=0;
 suma=0;
 c=getchar();
 while (c != EOF){
  if(c=='\n')
   lineas++;
  putchar(c);
  suma++;
  c=getchar();
 }

 printf("Ha ingresado %d caracteres\n",suma/2);
 printf("SUMA 2: %d \n",suma);
 printf("Lineas: %d \n",lineas);
 return 0;
}
