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
  double x=1000, y1=5, y2=0.2;
 
  double timetick, timeend;
  double resultado1, resultado2;
  long long int i;

  /*Division*/
  printf("Division...\n");
  timetick = dwalltime();
	for(i=0;i<cant;i++){
		resultado1 = x/y1;
	}
  timeend = dwalltime();
  printf(" Tiempo total en segundos %.10lf \n", (timeend - timetick));
  printf(" Tiempo promedio en segundos %.10lf \n", (timeend - timetick)/cant);
  
  /*Producto*/
  printf("Producto...\n");
  timetick = dwalltime();
	for(i=0;i<cant;i++){
		resultado2 = x*y2;
	}
  timeend = dwalltime();
  printf(" Tiempo total en segundos %.10lf \n", (timeend - timetick));
  printf(" Tiempo promedio en segundos %.10lf \n", (timeend - timetick)/cant);

  if (resultado1!=resultado2){
	printf("Error en resultado: %f <> %f \n",resultado1,resultado2);
  }else{
	printf("Resultado correcto\n");
  }
  return(0);

}
