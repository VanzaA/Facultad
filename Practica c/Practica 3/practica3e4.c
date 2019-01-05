#include <stdio.h>
int main(){
  int temp,i;
  int v[]={78,15,46,32,108,300,205};
  for(i=0; i<7; i++){
     printf(" %d -",v[i]);
  }
  printf("\n");
    for (i = 0; i < 6; i++){
      for (int j = i + 1; j < 7; j++){
        if (v[j] < v[i]){
          temp = v[j];
          v[j] = v[i];
          v[i] = temp;
        }
      }
    }
  for(i=0; i<7;i++){
    printf(" %d -",v[i]);
  }
  putchar('\n');
  return 0;
}
