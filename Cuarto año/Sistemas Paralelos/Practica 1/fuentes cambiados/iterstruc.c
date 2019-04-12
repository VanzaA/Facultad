#include<stdio.h>
#include<stdlib.h>

double dwalltime();

int main(int argc, char* argv[]){
int N=512;
int i=0,j=0,k=0;
double *A,*B,*C;
double timetick;

 N=atoi(argv[1]); 
 A=(double*)malloc(sizeof(double)*N*N); 
 B=(double*)malloc(sizeof(double)*N*N); 
 C=(double*)malloc(sizeof(double)*N*N); 

 printf("Incializando matrices...\n");
 for(i=0;i<N;i++){
 	for(j=0;j<N;j++){
		A[i*N+j]=1;
		B[i*N+j]=1;
	}
  }   
  
  printf("Calculando While...\n");
  timetick = dwalltime();
		i=0;
		while(i<N){
			j=0;
			while(j<N){
				k=0;
				while(k<N){
					C[i*N+j] += A[i*N+k]*B[k+j*N];
					k++;
				}	
				j++;	
			}
			i++;
		}   
  printf("Tiempo While en segundos %f \n", dwalltime() - timetick);
  
  printf("Calculando For...\n ");
  timetick = dwalltime();
		for(i=0;i<N;i++){
			for(j=0;j<N;j++){
				for(k=0;k<N;k++){
					C[i*N+j] += A[i*N+k]*B[k+j*N];
				}	
			}
		}   
  printf("Tiempo For en segundos %f \n", dwalltime() - timetick);
 
 
  free(A);
  free(B);
  free(C);
}

/*****************************************************************/

#include <sys/time.h>

double dwalltime()
{
	double sec;
	struct timeval tv;

	gettimeofday(&tv,NULL);
	sec = tv.tv_sec + tv.tv_usec/1000000.0;
	return sec;
}
