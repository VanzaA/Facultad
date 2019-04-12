#include<stdio.h>
#include <sys/time.h>
#define cant 1000000000 

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
  double x=39916801, y=719;
  double timetick, timeend;
  double resultado;
  long long int i;

  /*Suma*/
  printf("Suma...\n");
  timetick = dwalltime();
	for(i=0;i<cant;i++){
		resultado = x+y;
	}
  timeend = dwalltime();
  printf(" Tiempo total en segundos %.10lf \n", (timeend - timetick));
  printf(" Tiempo promedio en segundos %.10lf \n", (timeend - timetick)/cant);
  
  /*Resta*/
  printf("Resta...\n");
  timetick = dwalltime();
	for(i=0;i<cant;i++){
		resultado = x-y;
	}
  timeend = dwalltime();
  printf(" Tiempo total en segundos %.10lf \n", (timeend - timetick));
  printf(" Tiempo promedio en segundos %.10lf \n", (timeend - timetick)/cant);
  
  /*Producto*/
  printf("Producto...\n");
  timetick = dwalltime();
	for(i=0;i<cant;i++){
		resultado = x*y;
	}
  timeend = dwalltime();
  printf(" Tiempo total en segundos %.10lf \n", (timeend - timetick));
  printf(" Tiempo promedio en segundos %.10lf \n", (timeend - timetick)/cant);
  
  /*Division*/
  printf("Division...\n");
  timetick = dwalltime();
	for(i=0;i<cant;i++){
		resultado = x/y;
	}
  timeend = dwalltime();
  printf(" Tiempo total en segundos %.10lf \n", (timeend - timetick));
  printf(" Tiempo promedio en segundos %.10lf \n", (timeend - timetick)/cant);

  return(0);

}
