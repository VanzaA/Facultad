#include<stdio.h>
#include <sys/time.h>
#define cant 100000000 

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

int main(int argc, char *argv[]){
 double x=2;
 
  double timetick, timeend;
  double resultado;
  long long int i;

  /*Calcula la funcion y hace los calculos*/
  printf("Funcion calculada...\n");
  timetick = dwalltime();
  	double fx = 2 * (( (x*x*x) + (3*x*x) + (3*x) + 2 ) / ( (x*x) + 1));
	for(i=0;i<cant;i++){
		resultado += fx - i;
	}
  timeend = dwalltime();
  printf(" Tiempo total en segundos %.10lf \n", (timeend - timetick));
  printf(" Tiempo promedio en segundos %.10lf \n", (timeend - timetick)/cant);
  
  /*Calcula la funcion cada vez que hace los calculos*/
  printf("Funcion calculada cada vez...\n");
  timetick = dwalltime();
	for(i=0;i<cant;i++){
 		resultado += 2 * ( ( (x*x*x) + (3*x*x) + (3*x) + 2 ) / ( (x*x) + 1) ) - i ;
	}
  timeend = dwalltime();
  printf(" Tiempo total en segundos %.10lf \n", (timeend - timetick));
  printf(" Tiempo promedio en segundos %.10lf \n", (timeend - timetick)/cant);

  return(0);

}
