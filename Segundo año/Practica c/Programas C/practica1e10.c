#include <stdio.h>
int es_Mom (char c){
 
 if ((c >= 'a') && (c <= 'z'))
  return 1;
 else if ((c >= 'A') && (c <= 'Z'))
  return 0;
 else
  return 2;

}

int main(){

 int ok;
 char c;
 printf("Ingrese un caracter\n");
 scanf("%c",&c);
 ok=es_Mom(c);
 if (ok == 0) printf ("Es mayuscula\n");
 else if (ok == 1)
  printf("Es minuscula\n");
 else
  printf("El caracter ingresado no es una letra\n");
 return 0;
}
