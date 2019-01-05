#include <stdio.h>
#include <string.h>
void invertir(char s[],int i){
  if (i>=0){
    printf("%c",s[i]);
    invertir(s,i-1);

  }
}
int main(){
  char s[50] ;
  printf("Ingrese un string: \n");
  scanf("%s",&*s);
  invertir(s,strlen(s));
  printf("\n");

}
