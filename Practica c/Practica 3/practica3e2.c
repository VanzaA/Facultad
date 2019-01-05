#include <stdio.h>

int main(){

  char *arr1 = "Hola mundo";
  char arr2[20];
  printf(" %lu \n", sizeof(arr1));
  printf(" %lu \n", sizeof(arr2));


  char arr3[ ][15] = {"uno", "dos", "tres"};
  char arr4[5][15];
  char *arr5[ ] = {"uno", "dos", "tres"};
  char *arr6[4];
  printf(" %lu \n", sizeof(arr3));
  printf(" %lu \n", sizeof(arr4));
  printf(" %lu \n", sizeof(arr5));
  printf(" %lu \n", sizeof(arr6));

  return 0;

}
