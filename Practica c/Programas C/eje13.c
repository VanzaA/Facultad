#include <stdio.h>
#define min(a,b) (a<b)?a:b
#define max(a,b) (a>b)?a:b

int main(){
  printf("min: %d \n",min(3,4));
  printf("max: %d \n",max(3,4));
  return 0;

}
