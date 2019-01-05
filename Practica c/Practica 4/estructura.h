#ifndef ESTRUCTURA_H
#define ESTRUCTURA_H
struct alumno{
	char * nombre;
	char * apellido;
	char * tipo_de_documento;
	char * numero_de_documento;
};
typedef struct alumno alumno_t;

char * alocar(char *);

alumno_t inicializar(alumno_t);
#endif
