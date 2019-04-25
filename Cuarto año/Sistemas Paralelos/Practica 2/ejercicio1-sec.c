#include <stdio.h>
#include <stdlib.h>
#include <sys/time.h>


double dwalltime()
{
  double sec;
  struct timeval tv;

  gettimeofday(&tv,NULL);
  sec = tv.tv_sec + tv.tv_usec/1000000.0;
  return sec;
}

void print_matrix(double *matrix, int N)
{
  for (int i = 0; i < N; i++) {
    for(int j = 0; j < N; j++) {
      printf(" %g ", matrix[i*N+j]);
    }
    printf("\n");
  }
}

int main(int argc, char* argv[])
{
  // Check arguments
  if (argc < 2)
  {
    printf("You must specify:\n\t- matrix size\n");
    exit(1);
  }


  // Initialize variable
  double initial_time;
  int check = 1;
  int N = atoi(argv[1]);;
  double *A; // Matriz A
  double *B; // Matriz B
  double *C; // Matriz C
  
  // Alloc matrix A, B and C
  A = (double*)malloc(sizeof(double)*N*N);
  B = (double*)malloc(sizeof(double)*N*N);
  C = (double*)malloc(sizeof(double)*N*N);

  // Matrix initialization
  for(int i = 0; i < N; i++){
    for(int j = 0; j < N; j++){
      A[i * N + j] = 1; 
      B[i * N + j] = 1;
    }
  }   

  //Start processor time
  initial_time = dwalltime();

  for(int i = 0; i < N; i++){
    for(int j = 0; j < N; j++){
      int aux = 0;
      for(int k = 0; k < N; k++){
        aux += A[i*N+k] * B[k+j*N];
      }
      C[i*N+j] = aux;
    }
  }

  printf("Time %g\n", dwalltime() - initial_time);

  //Verifica el resultado
  for(int i = 0; i < N; i++){
    for(int j = 0; j < N; j++){
      check = check && (C[i*N+j] == N);
    }
  }


  free(A);
  free(B);
  free(C);
  
  if(!check){
    printf("Multiplication error!\n");
    return 2;
  }

  printf("Everything it's ok!\n");
  return 0;
}