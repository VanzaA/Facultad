#include <stdio.h>

int cal (int a, int b)
{
   int c = 0 ;
   if ((a < b) && (a != 0)) {
     c = b / a; 
   }
   else
    if ((b <= a) && (b != 0)){
      c = a / b;
    } 
   else 
     printf("no escribas 0 mogolico XDXDXDX \n" );

   return c;   
}

int main (){
   int n,m;
   printf("inserte un numero para la division\n" );
   scanf("%i",&n);
   printf("Ingrese otro numero para la divison \n");
   scanf("%i",&m);
   printf("el resultado es: %d \n", cal(n,m));
}
