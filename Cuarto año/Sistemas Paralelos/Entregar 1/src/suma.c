#include <stdio.h>
#include <stdlib.h>
#include <sys/time.h>
#include <omp.h>

int N;
double *A, *B, *C, *D, *total;


void  print_matrix(double *matrix){
	for(int i = 0; i < N; i++){
		for(int j = 0; j < N; j++){
			printf(" %f ", matrix[i * N + j]);
		}
		printf("\n");
	}
	
}

double dwalltime()
{
  double sec;
  struct timeval tv;

  gettimeofday(&tv,NULL);
  sec = tv.tv_sec + tv.tv_usec/1000000.0;
  return sec;
}

int main(int argc, char * argv[]){

	if(argc < 3){
		printf("You must specify:\n\t- thread number\n\t- matrix size\n");
		exit(1);
	}
	double aux1, aux2, aux3;

	int thread_number = atoi(argv[1]);
	N = atoi(argv[2]);
	omp_set_num_threads(thread_number);

	A = (double*)malloc(sizeof(double)*N*N);
	B = (double*)malloc(sizeof(double)*N*N);
	C = (double*)malloc(sizeof(double)*N*N);
	D = (double*)malloc(sizeof(double)*N*N);
	total = (double*)malloc(sizeof(double)*N*N);

	int i, j, k;

	for(i = 0; i < N; i++){
		for(j = 0; j < N; j++){
			A[i * N + j] = 1;
			B[i * N + j] = 1;
			C[i * N + j] = 1;
			D[i * N + j] = 1;
		}
	}
	double timetick = dwalltime();
	#pragma omp parallel for private(i, j, k, aux1, aux2, aux3)
	for (i = 0; i < N; i++){
		for(j = 0; j < N; j++){
			aux1 = 0;
			aux2 = 0;
			aux3 = 0;
			for(int k = 0; k < N; k++){
				aux1 += A[i * N + k] * A[k + j * N];
				aux2 += A[i * N + k] * B[k + j * N];
				aux3 += C[i * N + k] * D[k + j * N];
			}
			total[i * N + j] = aux1 + aux2 + aux3;
		}
		
	}

	printf("tiempo total %f\n", dwalltime() - timetick);
	int check = 1;
	for(i = 0; i < N; i++){
		for(j = 0; j < N; j++){
			check = check && (total[i * N + j] == (N*3));
		}
	}
	if(!check){
    		printf("Sum error!\n");
    		return 2;
  	}

  	printf("Everything it's ok!\n");
	
	return 0;
}
