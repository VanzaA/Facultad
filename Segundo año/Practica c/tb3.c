#include <stdio.h>
#include <string.h>

void traductor (const char *a,const char *b){
  char leo;
  while((leo = getchar()) != EOF){
    int i;
    for (i = 0; i <= strlen(a); i++){
      if (leo == (a)[i])
        leo = ((b)[i]);
    } //fin del for
    putchar(leo);
  } //fin del while
}

void borrador(const char *a){
  char leo;
  while((leo = getchar()) != EOF){
    int i;
    for(i = 0; i <= strlen(a); i++){
      if (leo == (a)[i])
        leo = '\0';
      }// Fin del for
      putchar(leo);
    } // Fin del while
}

int main(int argc, const char *argv[]){
  if(argc == 1){
    printf("No se han ingresado parametros\n");
    return 0;
  }
  if (!(!strcmp(argv[1],"-t") || !strcmp(argv[1],"-b"))){
    return 1;
  }
  else if (!strcmp(argv[1],"-t")){  //SI  PUSE -t  -----------------------------------------------------
    if (argc != 4)
      return 2;
    else{
      if ((strlen(argv[2])) != (strlen(argv[3])))
        return 3;
      else{
        traductor(argv[2],argv[3]);
      }
    } //fin del else
  }
  else if(!strcmp(argv[1],"-b")){
    //SI PUSE -b  -------------------------------------------------------
    if(argc != 3)
      return 2;
    else{
        borrador(argv[2]);
    } // Fin del else
  }
  printf("Fin del programa\n");
  return 0;

}
