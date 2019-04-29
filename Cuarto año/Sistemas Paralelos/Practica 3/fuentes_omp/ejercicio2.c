//Ejercicio 2
#include<stdio.h>
#include<stdlib.h>
#include<omp.h>
#include<math.h>


int main(int argc,char*argv[]){
 double x,scale;
 int i;
 //int numThreads = atoi(argv[2]);
 int N=atoi(argv[1]);
 //omp_set_num_threads(numThreads);
 scale=2.78;
 x=0.0;

//este algoritmo no necesita ser paralelo :P
// #pragma omp parallel for private
 for(i=1;i<=N;i++){
	x = x + sqrt(i*scale) + 2*x;
 }

 printf("\n Resultado: %f \n",x);

 return(0);
}

