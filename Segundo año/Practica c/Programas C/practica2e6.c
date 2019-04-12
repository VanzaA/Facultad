#include <stdio.h>
int main(){

 int c;
 c=getchar();
 while (c != EOF){
 if (c == 92){
  printf("\\");
  printf("\\");
 }
 if (c == 9){
  while(c == 32)
   c=getchar();
  printf("\\t");
 }
 if (c == 10)
  printf("\\n");
 printf("%c",c);  
  c = getchar();
 }
 return 0;
}

