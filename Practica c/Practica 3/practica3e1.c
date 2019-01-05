#include <stdio.h>
#include <string.h>
#include <strings.h>

void convertir(int i, char s[], int b){
  int dig;
  int p = 0;
  int v[100];
  int caracteres[] = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z','\0'};
  while (i != 0){ //divido el numero hasta que el cociente sea distinto de 0
    dig = i%b;
    v[p] = dig;
    p++;
    i = i/b;
  }
  int j;
  int c = 0;
  for (j = p-1; j>=0; j--){
    s[c] = caracteres[v[j]];
    c++;
  }
  s[c]='\0';
}


int main(){
  int i,b;
  printf("Ingrese un numero: \n");
  scanf("%d",&i);
  char s[50];
  printf("Ingrese la base 2..36\n");
  scanf("%d",&b);
  convertir(i,(char *)s,b);
  printf("%d en base %d es: ",i,b);
  for(int j = 0; j<=strlen(s); j++){
    printf("%c",s[j]);
  }
  printf("\n");
  return 0;

}
