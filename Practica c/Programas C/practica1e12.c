#include <stdio.h>
int promedio(int a, int b){
 int prom=0;
 if (a>=0 && b>=0){
   prom = (a+b)/2;
 }
 return prom;

}

int main(){
 int x,y,prom;
 printf("Ingrese 2 numeros\n");
 scanf("%d",&x);
 scanf("%d",&y);
 prom = promedio(x,y);
 if (prom != 0)
   printf("El promedio entre %d y %d es: %d \n",x,y,prom);
 else
   printf("No se ha podido realizar el promedio\n");
 return 0;
}
