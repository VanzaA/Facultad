#include <stdio.h>
#include "invertir.h"

void invertir(char* cadena){

     char *i, *f, temp;

     f = cadena;
     while (*f) // Recorremos toda la cadena hasta el final
         f++;

     f--; // Apuntamos al último carácter de cadena

     i = cadena;
     while (i < f) {
           temp = *f;
           *f = *i;
           *i = temp;
           i++;
           f--;
     }
}


/*int main(void) {

    char s[] ={'B','A','R','B','A','R','A','\0'} ;

    inversa(s);

    printf("La cadena inversa queda %s \n", s);

    return 0;
}*/
