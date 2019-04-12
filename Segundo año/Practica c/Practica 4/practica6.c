#include <stdio.h>
#include <string.h>

struct a {
  int dia;
  char mes[15];
  int anio;
}fecha;
struct a cambio_fecha (struct a fecha){
  fecha.dia=1;
  strcpy(fecha.mes,"enero");
  fecha.anio=1970;
  return fecha;
}

int main(int argc,char * argv[]){
  fecha=cambio_fecha(fecha);
  printf("fecha = dia: %d mes: %s anio: %d\n",fecha.dia,fecha.mes,fecha.anio);
  return 0;
}
