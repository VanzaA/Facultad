#include <stdio.h>
int main(){
	int cont,c;
	cont=0;
	while((c = getchar()) != EOF){
		putchar(c);
		cont++;
	if(c == '\n'){
		cont=0;
	}
	else if (cont == 42){
		while(c!=' ' && c!=EOF && c!='\n'){
			c = getchar();
			putchar(c);
		}
		putchar('\n');
		cont=0;
	  }
       }
   return 0;
}
