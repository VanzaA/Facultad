#include <stdio.h>
int main(int argc, char **argv){
  unsigned i;
  for (i = 10; i >= 0; i--){
    printf("Valor de i = %2u\n", i);
  }
  return 0;
}
