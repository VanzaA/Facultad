#include<stdio.h>
#include <sys/time.h>
#define cant 10

/**********Para calcular tiempo*************************************/
double dwalltime()
{
        double sec;
        struct timeval tv;

        gettimeofday(&tv,NULL);
        sec = tv.tv_sec + tv.tv_usec/1000000.0;
        return sec;
}
/****************************************************************/

unsigned long long fibRecursivo(unsigned long long  n){
  if (n<= 2){
   return 1;
  }else{
   return fibRecursivo(n-1) + fibRecursivo(n - 2);
  }
}

unsigned long long fibIterativo(int n){
unsigned long long j = 0;
unsigned long long i = 1;
unsigned long long k;
unsigned long long t;

	for(k=1;k<=n;k++){
		t = i + j;
		i = j;
		j = t;
	}
 	return j;
}

int main(int argc, char *argv[]){

  double timetick, timeend;
  unsigned long long n = atoi(argv[1]);
  unsigned long long resultado;
  int i;

  /*Fibonacci iterativo*/
  printf("Calculando Fibonacci iterativo para n = %llu...\n",n);
  timetick = dwalltime();
	for(i=0;i<cant;i++){
		resultado = fibIterativo(n);
	}
  timeend = dwalltime();
  printf(" Resultado = %llu\n",resultado); 
  printf(" Tiempo en segundos %.10lf \n", (timeend - timetick)/cant);
  
  /*Fibonacci recursivo*/
  printf("Calculando Fibonacci recursivo para n = %llu...\n",n);
  timetick = dwalltime();
	for(i=0;i<cant;i++){
		resultado = fibRecursivo(n); 
	}
  timeend = dwalltime();
  printf(" Resultado = %llu\n",resultado); 
  printf(" Tiempo en segundos %.10lf \n", (timeend - timetick)/cant);


	
  return(0);

}
