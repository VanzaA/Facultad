#include <stdio.h>
#include <string.h>
#include <stdlib.h>

struct alumno{
  char * nombre;
  char * apellido;
  char * nacimiento;
  char * legajo;
  char * tipo_de_documento;
  int numero_de_documento;
};

char * alocar (char *s){
  int c,j=1000;
  s=malloc(j);
  int i=0;
  while((c=getchar())!='\n'){
    if(i+2>j){
      j=j*2;
      s=realloc(s,j);
    }
    s[i]=c;
    i++;
  }
  s[i]='\0';
  s=realloc(s,i+2);
  return s;
}/*
char * alocar (char *s){
  int c;
  s= malloc(1);
  int i=0;
  while((c=getchar())!=){
    s=realloc(s,strlen(s)+1);
    s[i]=c;
    i++;
  }
  s[i]='\0';
  return s;
}
}*/

struct alumno inicializar(struct alumno a){
  printf("inserte nombre\n");
  a.nombre=alocar(a.nombre);
  printf("inserte apellido\n");
  a.apellido=alocar(a.apellido);
  printf("inserte fecha de nacimiento\n");
  a.nacimiento=alocar(a.nacimiento);
  printf("inserte numero de legajo\n");
  a.legajo=alocar(a.legajo);
  printf("inserte tipo de documento\n");
  a.tipo_de_documento=alocar(a.tipo_de_documento);
  printf("inserte numero de documento\n");
  scanf("%d",&a.numero_de_documento);
  return a;
}

int main(int argc,char* argv[]){
  struct alumno b,a;
  a=inicializar(a);
  printf("nombre=%s\napellido=%s\nnacimiento=%s\nlegajo=%s\ntipo_de_documento=%s\nnumero_de_documento%d\n",a.nombre,a.apellido,a.nacimiento,a.legajo,a.tipo_de_documento,a.numero_de_documento);
  printf("---------------------------------------------------------------------------------\n---------------------------------------------------------------------------------\n");
  b=a;
  strcpy(b.nombre,"Maria");
  printf(" nombre=%s\n apellido=%s\n nacimiento=%s\n legajo=%s\n tipo_de_documento=%s\n numero_de_documento=%d\n",b.nombre,b.apellido,b.nacimiento,b.legajo,b.tipo_de_documento,b.numero_de_documento);
  return 0;
}
