#include <stdio.h>

char test (char a){
  if ((a <= 9 ) && (a >= 0))
    printf("el caracter es un digito\n");
  else
    printf("el caracter no es un digito\n");
  return 0;
}

int main(){
  char x;
  printf("inserte un caracter para saber si es un digito\n");
  scanf("%c" , &x);
  test(x);
}

