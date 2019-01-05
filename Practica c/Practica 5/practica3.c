#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int main (int argc,char * argv[]){
	if(argc > 2){
		FILE * arch1=fopen(argv[1],"r");
		FILE * arch2=fopen(argv[2],"r");
		char * cadena1=(char *)malloc(1024*sizeof(char));
		char * cadena2=(char *)malloc(1024*sizeof(char));
		while((!feof(arch1))&&(!feof(arch2))){
			fgets(cadena1,1024,arch1);
			fgets(cadena1,1024,arch2);
			if(strcmp(cadena1,cadena2)){
				fputs(cadena1,stdout);
				fputs(cadena2,stdout);
			}
		}
	}
	else
		printf("no se insertaron la cantidad necesarias de archivos");
	return 0;
}
