#include <stdio.h>

int main(int argc, char *argv[]){
	if (argc > 1){
		int i,c;
		for(i=1; i<argc; i++){
			FILE * arch= fopen(argv[i],"r");
			while((c=fgetc(arch))!= EOF)
				fputc(c,stdout);
			fclose(arch);
		}
	}
	else
		printf("no se mandaron archivos\n");
	return 0;
}
