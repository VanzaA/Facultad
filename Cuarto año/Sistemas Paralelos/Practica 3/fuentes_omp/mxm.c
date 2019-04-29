#include<stdio.h>
#include<stdlib.h>
#include<omp.h>


/* Time in seconds from some point in the past */
double dwalltime();


int main(int argc,char*argv[]){
 double *A,*B,*C,*D,*E;
 int i,j,k,N;
 int check=1;
 double timetick;

 //Controla los argumentos al programa
  if (argc < 3){
   printf("\n Faltan argumentos:: N dimension de la matriz, T cantidad de threads \n");
   return 0;
  }
 
   N=atoi(argv[1]);
   int numThreads = atoi(argv[2]);
   omp_set_num_threads(numThreads);

 //Aloca memoria para las matrices
  A=(double*)malloc(sizeof(double)*N*N);
  B=(double*)malloc(sizeof(double)*N*N);
  C=(double*)malloc(sizeof(double)*N*N);
  D=(double*)malloc(sizeof(double)*N*N);
  E=(double*)malloc(sizeof(double)*N*N);
 

 //Inicializa las matrices A, B,C en 1
  for(i=0;i<N;i++){
   for(j=0;j<N;j++){
	A[i*N+j]=1;
	B[i+j*N]=1;
	C[i*N+j]=1;
   }
  }   

  timetick = dwalltime();

 //Realiza la multiplicacion D= AxB
  for(i=0;i<N;i++){
   for(j=0;j<N;j++){
    D[i*N+j]=0;
    for(k=0;k<N;k++){
	D[i*N+j]= D[i*N+j] + A[i*N+k]*B[k+j*N];
    }
   }
  }   

   //Realiza la multiplicacion E= CxB
  for(i=0;i<N;i++){
   for(j=0;j<N;j++){
    E[i*N+j]=0;
    for(k=0;k<N;k++){
	E[i*N+j]= E[i*N+j] + C[i*N+k]*B[k+j*N];
    }
   }
  }   

  printf("Tiempo en segundos %f \n", dwalltime() - timetick);

 //Verifica el resultado
  for(i=0;i<N;i++){
   for(j=0;j<N;j++){
	check=check&&(D[i*N+j]==N)&&(E[i*N+j]==N);
   }
  }   

  if(check){
   printf("Multiplicacion de matrices resultado correcto\n");
  }else{
   printf("Multiplicacion de matrices resultado erroneo\n");
  }
 free(A);
 free(B);
 free(C);
 free(D);
 free(E);
 return(0);
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


