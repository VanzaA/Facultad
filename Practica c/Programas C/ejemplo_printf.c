#include <stdio.h>
int main(){

 int i;
 printf("%8s %8s %8s\n", "^1", "^2", "^3");
 for (i=1; i<6; i++) 
  printf("%8d %8d %8d\n", i, i*i, i*i*i);
 return 0;

}
