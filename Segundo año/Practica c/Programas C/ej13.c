#include <stdio.h>

int main(){

  printf("inserte un numero para ver su factorial \n");
  int x,b;
  scanf("%i", &x);
  b=x;
  b--;
  while (b !=0){
   x=x*b;
   b-=1;
  }
  printf("su factorial es: %i \n",x);
} 
