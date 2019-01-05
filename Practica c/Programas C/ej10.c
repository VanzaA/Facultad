#include <stdio.h>

int main()
{  
   char b;
   printf("Ingrese un caracter \n");
   scanf("%c",&b); 
   if ((b>='a')&&(b<='z'))
   {
     printf("La letra es minuscula \n");
   }
   else 
     if ((b>='A')&&(b<='Z'))
       printf("Su caracter es mayuscula \n");
     else 
       printf("Eso no es un caracter valido, tonto \n");

}
