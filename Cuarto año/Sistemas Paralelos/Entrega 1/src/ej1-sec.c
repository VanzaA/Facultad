#include <stdio.h>
#include <stdlib.h>
#include <sys/time.h>

#define ANSI_COLOR_RED "\x1b[31m"
#define ANSI_COLOR_YELLOW "\x1b[33m"
#define ANSI_COLOR_RESET "\x1b[0m"

void print_matrix(double *matrix, int N){
	for(int i = 0; i < N; i++){
		for(int j = 0; j < N; j++){
			printf(" %g ", matrix[i * N + j]);
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

	#ifdef DEBUG
	int debug = 0;
	if (getenv("DEBUG")) {
			debug = atoi(getenv("DEBUG"));
			printf(ANSI_COLOR_RED "Debug mode - Level %d\n" ANSI_COLOR_RESET, debug);
	}  else {
			printf(ANSI_COLOR_YELLOW "Debug binary\nSet DEBUG environment variable with a level\n" ANSI_COLOR_RESET);
	}
	#endif

	int i, j, k, N = atoi(argv[1]), check = 1;
	double *A, *A_trans, *B, *C, *D, *aux_m, *aux_m2, *aux_m3;
	double aux;

	#ifdef DEBUG
	if (debug > 1) {
		printf("N: %d\n\n", N);
	}
	#endif

	A = (double*)malloc(sizeof(double) * N * N);
	A_trans = (double*)malloc(sizeof(double) * N * N);
	B = (double*)malloc(sizeof(double) * N * N);
	C = (double*)malloc(sizeof(double) * N * N);
	D = (double*)malloc(sizeof(double) * N * N);
	aux_m = (double*)malloc(sizeof(double)*N*N);
	aux_m2 = (double*)malloc(sizeof(double)*N*N);
	aux_m3 = (double*)malloc(sizeof(double)*N*N);

	for(i = 0; i < N; i++){
		for(j = 0; j < N; j++){
			A[i * N + j] = 1;
			B[i * N + j] = 1;
			C[i * N + j] = 1;
			D[i * N + j] = 1;
		}
	}

	#ifdef DEBUG
	if (debug > 0) {
		printf("Initial matrix A\n");
		print_matrix(A, N);
		printf("Initial matrix B\n");
		print_matrix(B, N);
		printf("Initial matrix C\n");
		print_matrix(C, N);
		printf("Initial matrix D\n");
		print_matrix(D, N);
		printf("\n\n");
	}
	#endif

	double timetick = dwalltime();

	for (i = 0; i < N; i++){
		for(j = 0; j < N; j++){
			A_trans[i * N + j] = A[j * N + i];
		}
	}

	for (i = 0; i < N; i++){
		for(j = 0; j < N; j++){
			aux = 0;
			for(k = 0; k < N; k++){
				aux += A[i * N + k] * A_trans[i * N + k];
			}
			aux_m[i * N + j] = aux;
		}
	}

	for (i = 0; i < N; i++){
		for(j = 0; j < N; j++){
			aux = 0;
			for(k = 0; k < N; k++){
				aux += A[i * N + k] * B[k * N + j];
			}
			aux_m2[i * N + j] = aux;
		}
		
	}

	for (i = 0; i < N; i++){
		for(j = 0; j < N; j++){
			aux = 0;
			for(k = 0; k < N; k++){
				aux += C[i * N + k] * D[k * N + j];
			}
			aux_m3[i * N + j] = aux;
		}
	}

	for (i = 0; i < N; i++){
		for(j = 0; j < N; j++){
			aux_m[i * N + j] += aux_m2[i * N + j];
		}
	}


	for (i = 0; i < N; i++){
		for(j = 0; j < N; j++){
			aux_m[i * N + j] += aux_m3[i * N + j];
		}
	}
	printf("tiempo total %f\n", dwalltime() - timetick);

	for(i = 0; i < N; i++){
		for(j = 0; j < N; j++){
			check = check && (aux_m[i * N + j] == (N*3));
		}
	}

	if(!check){
		printf("Sum error!\n");
		return 2;
	}

	printf("Everything it's ok!\n");
	
	#ifdef DEBUG
	if (debug > 1) {
		printf("\n\nFinal matrix A\n");
		print_matrix(aux_m, N);
	}
	#endif

	free(A);
	free(A_trans);
	free(B);
	free(C);
	free(D);
	free(aux_m);
	free(aux_m2);
	free(aux_m3);

	return 0;
}
