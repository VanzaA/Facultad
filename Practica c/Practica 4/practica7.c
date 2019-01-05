#include <stdio.h>

struct {
  int a;
  char b;
} asd;

int main(){
  printf("int a=%lu\n",sizeof(asd.a));
  printf("char b=%lu\n",sizeof(asd.b));
  printf("estructura=%lu\n",sizeof(asd));
  return 0;
}
