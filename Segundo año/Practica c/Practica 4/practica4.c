#include <stdio.h>
#include <stdlib.h>

void asignar(int * n){
  n=(int *)malloc(sizeof(int));
}

int main(int argc, char * argv[]){
  int * a=NULL;
  asignar(a);
  printf("%lu \n",sizeof(a));
  return 0;
}
