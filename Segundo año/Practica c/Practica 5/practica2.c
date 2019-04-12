#include <stdio.h>

int main(int argc, char * argv[]){
	if(argc > 2){
		int i,c;
		FILE * out= fopen(argv[argc-1], "w");
		for(i = 1 ; i < argc-1; i++){
			FILE * arch = fopen(argv[i],"r");
			while((c=fgetc(arch))!= EOF)
				fputc(c,out);
			fclose(arch);
		}
		fclose(out);
	}
	else
		printf("no se inserto la cantidad necesaria de archivos");
		return 0;
}
