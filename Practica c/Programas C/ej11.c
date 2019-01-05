#include <stdio.h>

int main()
{  
   char b;
   printf("Ingrese un caracter \n");
   scanf("%c",&b); 
   if ((b>='a')&&(b<='z'))
   {
     b-=32;
     printf("su contrario es : %c\n",b);
   }
   else 
     if ((b>='A')&&(b<='Z')){
       b+=32;
       printf("Su contrario es: %c \n",b);
     }
    else 
       printf("Eso no es un caracter valido, tonto \n");

}


