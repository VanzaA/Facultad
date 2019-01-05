#include <stdio.h>
int main(){
 int x,y,res;
 printf("\n Ingrese dos valores enteros separados por -, luego presione enter:");
 res = scanf("%d-%d",&x,&y);
 printf("\n Se leyeron: \n\t1\t=>\t%d\n\t2\t=>\t%d\n El resultado del scanf fue: %d\n", x, y, res);
 return 0;
}
