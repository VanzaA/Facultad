#include <stdio.h>
int main(int argc, char **argv){
  char nombre[20];
  int edad;
  int error;
// Lee e imprime 5 nombres seguidos de su edad
  for (int i = 0; i < 5; i++){
    printf("Ingrese el nombre y la edad: ");
    if ((error = scanf(" %s %d", nombre, &edad)) != 2){
      printf("------> Ocurrio un error, scanf retorno: %d\n", error);
// Si falla descartamos el intento
      i--;
     }
    else{
      printf("Ingreso el nombre: %s con edad: %d\n", nombre, edad);
     }
   }
}
