#include "estructura.h"
#include <stdio.h>
#include <string.h>

int main(int argc, char * argv[]){
	alumno_t a,b;
	a=inicializar(a);
	b=a;
	printf("nombre=%s\napellido=%s\ntipo_de_documento=%s\nnumero_de_documento%s\n",a.nombre,a.apellido,a.tipo_de_documento,a.numero_de_documento);
	printf("---------------------------------------------------------------------------------\n---------------------------------------------------------------------------------\n");
	strcpy(b.nombre,"Maria");
	printf(" nombre=%s\n apellido=%s\n tipo_de_documento=%s\n numero_de_documento=%s\n",b.nombre,b.apellido,b.tipo_de_documento,b.numero_de_documento);
	return 0;
}
