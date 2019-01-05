#include <string.h>
#include <stdio.h>

int main(int argc, char * argv[] ){
  char s[]={"hola"};
  printf("%c",s[strlen(s)-1]);
  return 0;
}
