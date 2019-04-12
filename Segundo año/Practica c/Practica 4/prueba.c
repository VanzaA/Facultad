#include <stdio.h>
void funcion(){
static int x = 0 ; // Se va i n c r em e n t a n d o
int y = 0 ; // S´o l o l l e g a h a s t a 1 y s e p i e r d e
x++; y++;
printf("x = %d , y = %d\n",x,y);
}
int main(){
  for(int i=0;i<10;i++)
    funcion();
  return 0;
}
