
#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/time.h>

int sizeBlock, n;
double *A; // Matriz A
double *B; // Matriz B
double *C; // Matriz C


//funcion para calcular el tiempo
double dwalltime()
{
	double sec;
	struct timeval tv;

	gettimeofday(&tv,NULL);
	sec = tv.tv_sec + tv.tv_usec/1000000.0;
	return sec;
}

//funcion para multiplar matrices
void * mult(void * arg)
{
  int id= *(int *) arg;
  int desp, despA, despB, despC;
  printf("Start mult in thread %d\n", id);

  for (int j = 0; j < n; j++){
    despC = (id*n+j)*sizeBlock;
    for (int k=0; k<n; k++){
      despA = (id*n+k)*sizeBlock;
      despB = (k*n+j)*sizeBlock;
      for (int i=0; i < n; i++){
        for (int j=0;j< n;j++){
          desp = despC + i*n+j;
          for (k=0;k<n;k++){
            printf("i = %d\nj = %d\nk = %d\ndesp = %d\ndespA = %d\ndespB = %d\ndespC = %d\n\n", i, j, k, desp, despA, despB, despC);
            C[desp] += A[despA + i*n+k]*B[despB + k*n+j]; 
          }
        }
      }
    }
  }
  return 0;
}

void imprimeMatriz(double *S,int N,int r){
// Imprime la matriz pasada por parametro
// N es la cantidad de bloques, r dimension de cada bloque
  int i,j,I,J,despB;

  printf("Contenido de la matriz: \n");
  for (I= 0; I< N; I++){
    //para cada fila de bloques (I)
    for (i= 0; i< r; i++){
       for(J=0;J<N;J++){
		   despB=(I*N+J)*r*r;
	  for (j=0;j<r;j++){
	     printf("\t%g ",S[despB+ i*r+j]);
	
	   };//end for j
	};//end for J
        printf("\n");
     };//end for i

  };//end for I
  printf(" \n\n");
}

void crearIdentidad(double *S, int sizeBlock, int sizeMatrix,int N,int r){
//Inicializa la matriz S como una matriz identidad
//pone cada bloque en 0, y a los bloques diagonales pone 1 en su diag. interna

//inicializa en cero la matriz
  int i,j;
  for (i=0; i<sizeMatrix ;i++){
	  S[i]=0.0;
  };//end for j

//inicializa los N bloques de la diagonal como identidad
  for (i=0; i<sizeMatrix; i=i+(N+1)*sizeBlock){
	//en i commienza el bloque a actualizar
	  for (j=0; j<sizeBlock; j=j+r+1){
		  S[i+j]= 1.0;
	  }
  };//end for i
}

void crearMatriz(double *S, int sizeMatrix){
  int i;
  for(i=0 ;i<sizeMatrix;i++){
	S[i] = rand()%10;
  };//end i
}


int main(int argc, char* argv[])
{
  // Check for thread_number
  if (argc < 2) {
    printf("You must specify a number\n");
    return 1;
  }

  // Set thread_number and initialize threads ids array
  int thread_number = atoi(argv[1]);
  n = atoi(argv[2]);

  printf("thread_number %d\n", thread_number);
  int N = n * n;
  int ids[thread_number];
  A= (double *)malloc(N*sizeof(double)); //aloca memoria para A
  B= (double *)malloc(N*sizeof(double)); //aloca memoria para B
  C= (double *)malloc(N*sizeof(double)); //aloca memoria para C


  for (int i = 0; i < n; i++) {
    for (int j = 0; j < n; j++) {
      A[i*n+j] = 1;
      B[i*n+j] = 1;
      C[i*n+j] = 0;
    }
  } 

  // Declare threads config
  pthread_attr_t attr;
  pthread_t threads[thread_number];
  pthread_attr_init(&attr);

  blockNumber =  thread_number;
  sizeBlock = N / blockNumber;

  for (int i=0; i < thread_number; i++){
    ids[i] = i;
    pthread_create(&threads[i], &attr, mult, &ids[i]);
	}

  // Wait for all threads
  for (int i = 0; i < thread_number; i++) {
    pthread_join(threads[i], NULL);
  }

  printf("A: \n" );
  imprimeMatriz(A,n,1);

  printf("B: \n" );
  imprimeMatriz(B,n,1);

  printf("C: \n" );
  imprimeMatriz(C,n,1);

  return 0;
}
