#include <stdio.h>
#include <stdlib.h>
#include <sys/time.h>

int N;
int *A, *B, *C, *D, *total;


void  printMatrix(int *matrix){
	for(int i = 0; i < N; i++){
		for(int j = 0; j < N; j++){
			printf(" %d ", matrix[i * N + j]);
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

	if(argc < 2){
		printf("You must specify:\n\t- matrix size\n");
		exit(1);
	}
	int aux1, aux2, aux3;

	//numThreads = atoi(argv[1]);
	N = atoi(argv[1]);


	A = (int*)malloc(sizeof(int)*N*N);
	B = (int*)malloc(sizeof(int)*N*N);
	C = (int*)malloc(sizeof(int)*N*N);
	D = (int*)malloc(sizeof(int)*N*N);
	total = (int*)malloc(sizeof(int)*N*N);

	int i, j;

	for(i = 0; i < N; i++){
		for(j = 0; j < N; j++){
			A[i * N + j] = 1;
			B[i * N + j] = 1;
			C[i * N + j] = 1;
			D[i * N + j] = 1;
		}
	}
	double timetick = dwalltime();
	for (i = 0; i < N; i++){
		for(int k = 0; k < N; k++){
			aux1 = 0;
			aux2 = 0;
			aux3 = 0;
			for(j = 0; j < N; j++){
				aux1 += A[i * N + j] * A[j + k * N];
				aux2 += A[i * N + j] * B[j + k * N];
				aux3 += C[i * N + j] * D[j + k * N];
			}
			total[i * N + k] = aux1 + aux2 + aux3;
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
