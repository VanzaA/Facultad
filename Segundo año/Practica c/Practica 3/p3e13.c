#include <stdio.h>
#include <stdlib.h>

int main(int agrc, char *agrv[]){
	int i;
	printf("HOLA: \n");
	printf("%i\n", agrc);
	for(i=0;i<agrc;i++){
		printf(" %s \n",agrv[i]);
	}
	return 0;

}
