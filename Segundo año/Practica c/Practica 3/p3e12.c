#include <stdio.h>
#include <strings.h>
#include <stdlib.h>
#include <string.h>
#include "invertir.h"

int es_palindromo(char *word, int size){

  char * temp=calloc(sizeof(char),size); //ASIGNA EN MEMORIA Y SE GUARDA EN TEMPORAL

  strcpy(temp,word); //COPIA EL STRING ORIGINAL EN TEMPORAL PARA QUE SE COMPARE EN LA FUNCION INVERTIR 

  invertir(temp);


  return strcmp(temp,word);

}


int main(){

  int suma,i,linea;

  int c;

  char* A = NULL;

  i = suma = linea = 0;

  A=malloc(sizeof(char)); //ASIGNACION DE MEMORIA PARA LOS CARACTERES

  c = getchar();

  while (c != EOF) {   //MIENTRAS NO SEA FIN DE ARCHIVO

    while ((c!=' ')&&(c!= EOF)&&(c!='\n')&&(c!='\0')){  //SI ENTRA SABENOS QUE ES PALABRA 

       if(i>strlen(A)){   //SI DA VERDADERO ES QUE EL ARREGLO NECESITA MAS MEMORIA

          A=(char *) realloc(A,(i*2));  

       }

       A[i]=c; //GUARDA PRIMERO EN LA POSICION 0

       i++;    //DESPUES INCREMENTA PARA LA SIGUIENTE POSICION

     c=getchar();

    }

    if (c == ' ' || c == '\n'){

     A[i]='\0'; //SI ENTRA ES PORQUE DEBEMOS INGRESAR EL FINAL DEL STRING, ENTONCES SABEMOS DONDE TERMINA EL STRING

    }

   

    A = (char *)realloc(A,i); //REDIMENSIONAMOS NUESTRO BLOQUE DE MEMORIA 

    if(es_palindromo(A,i) == 0) {

       suma++;

    }

    i=0;


    if (c=='\n') {

      linea++;

      printf("La suma de las palabras palindromas en la linea %d es de %d \n",linea,suma);

      suma=0;

    }

    c = getchar();

  }

  free(A);

  

  return EXIT_SUCCESS;

}
