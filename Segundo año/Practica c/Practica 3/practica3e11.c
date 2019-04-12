#include <stdio.h>
#include <string.h>

char* encripto(char *dest, const char *src){
  int i;
  for (i = 0; i < strlen(src); i++){
    dest[i] = ~src[i];
  }
  dest[strlen(src)] = '\0';
  return dest;
}

int main(){
  char src[255];
  char dest[255];
  int i;
  printf("Ingrese un string\n");
  fgets(src,254,stdin);
  printf("El complemento a1 de [ %s ] es [ %s ] \n",src,encripto(dest,src));
  for (i = 0; i < strlen(dest); i++){
    src[i] = ~dest[i];
  }
  printf("El complemento a1 de [ %s ] es [ %s ] \n",encripto(dest,src),src);
  return 0;
}
