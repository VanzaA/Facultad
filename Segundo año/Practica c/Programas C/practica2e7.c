#include <stdio.h>
int main(){

 int x;
 printf("Ingrese un numero,corta con 0 \n");
 scanf("%d",&x);
 while (x != 0){
  printf("Usted ingresó %d\n a %10d a \n a %-10d a \n",x,x,x);
  
  printf("Ingrese un numero, corta con 0 \n");
  scanf("%d",&x); 
 }
 return 0;
}
