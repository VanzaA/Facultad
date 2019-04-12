#include <stdio.h>


void cero(int * n){
  *n=0;
}
int main(int argc, char * argv[]){
  int n;
  cero(&n);
  printf("%d\n",n );
  return 0;
}
