#include "estructura.h"
#include <stdlib.h>
#include <stdio.h>

char * alocar (char * s){
	int c,i=0,j=1000;
	s=malloc(j);
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
}

alumno_t inicializar (alumno_t a){
	printf("inserte nombre\n");
	a.nombre=alocar(a.nombre);
	printf("inserte apellido\n");
	a.apellido=alocar(a.apellido);
	printf("inserte tipo de documento\n");
	a.tipo_de_documento=alocar(a.tipo_de_documento);
  	printf("inserte numero de documento\n");
	a.numero_de_documento=alocar(a.numero_de_documento);
	return a;
}
