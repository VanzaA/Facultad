#include <stdio.h>
int es_digito (char c){
 int n=c;
 if (n>=0 && n<=9)
  return 1;
 else
  return 0;

}
int main(){

 int ok;
 char c;
 printf("Ingrese un caracter\n");
 scanf("%c",&c);
 ok=es_digito(c);
 if (ok == 0) printf ("No es un digito\n");
 else
  printf("Es un digito\n");

 return 0;
}
