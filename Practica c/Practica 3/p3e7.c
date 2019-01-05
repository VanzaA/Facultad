#include <stdio.h>
#include <string.h>

int cuenta_palabras(char s[]){
    int c = 0,i = 0;
    while(s[i] == ' ')
        i++;
    while(s[i] != '\0'){
        if (s[i] == ' '){
            while((s[i] == ' ') && (s[i]!='\0'))
                i++;
            c++;
        }
        i++;
    }
  return c;
}

int main(){
  char cadena[256];
  printf("Ingrese un string: \n");
  fgets(cadena,255,stdin);
  printf("La cantidad de palabras del string ingresado es: %d \n",cuenta_palabras(cadena));
  return 0;
}
