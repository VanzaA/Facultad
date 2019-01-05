#include <stdio.h>
char mayus(char c){
  
  if(c >= 'A' && c <= 'Z')
    return c;
  if(c >= 'a' && c <= 'z'){
    c = c - 32;
    return c;
  }
  return c;

}
int main(){

 char c,d;
 printf("Ingrese un caracter: \n");
 scanf("%c",&c);
 d = mayus(c);
 printf("%c\n",d);
 return 0;
}
