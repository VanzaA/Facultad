#include <stdio.h>
#include <stdlib.h>
#include <sys/time.h>
#include <pthread.h>

int N;
int thread_number;
int block_size;
double *A; // Matriz A
double *B; // Matriz B
double *C; // Matriz C

double dwalltime()
{
  double sec;
  struct timeval tv;

  gettimeofday(&tv,NULL);
  sec = tv.tv_sec + tv.tv_usec/1000000.0;
  return sec;
}

void* mult(void* arg)
{
  int id = *((int*)arg);
  int start = id * block_size;
  int limit = (id + 1) * block_size;


  for(int i = start; i < limit; i++){
    for(int j = 0; j < N; j++){
      int aux = 0;
      for(int k = 0; k < N; k++){
        aux += A[i*N+k] * B[k+j*N];
      }
      C[i*N+j] = aux;
    }
  }
  pthread_exit(0);
  return 0;
}

void print_matrix(double *matrix)
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
  if (argc < 3)
  {
    printf("You must specify:\n\t- thread number\n\t- matrix size\n");
    exit(1);
  }


  // Initialize variable
  double initial_time;
  int check = 1;
  thread_number = atoi(argv[1]);
  N = atoi(argv[2]);
  block_size = N / thread_number;
  int ids[thread_number];
  
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

  // Threads declaration
  pthread_t threads[thread_number];

  //Start processor time
  initial_time = dwalltime();

  // Run threads
  for(int i = 0; i < thread_number; i++){
    ids[i]= i;
    pthread_create(&threads[i], NULL, mult, &ids[i]);
  }   

  // Wait for all threads
  for(int i = 0; i < thread_number; i++){
    pthread_join(threads[i], NULL);
  }

  printf("Time %g\n", dwalltime() - initial_time);

  // Verify result
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
