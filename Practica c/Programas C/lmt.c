#include <stdio.h>
int main(){

 int i,c;
 while ((c = getchar()) != EOF){
  putchar(c);
  i++;

  if (c == '\n'){
   i=0;
  } else if (i == 42){
     while (c != ' ' && c != '\n' && c != EOF){
       c = getchar();
       putchar(c);
     }
    putchar('\n');
    i = 0;
   }  
 }
 return 0;
}
