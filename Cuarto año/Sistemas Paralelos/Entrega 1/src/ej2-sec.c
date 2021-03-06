#include <stdio.h>
#include <stdlib.h>
#include <sys/time.h>
#include <float.h>

#define ANSI_COLOR_RED "\x1b[31m"
#define ANSI_COLOR_YELLOW "\x1b[33m"
#define ANSI_COLOR_RESET "\x1b[0m"

void print_matrix(double *matrix, int N){
	for(int i = 0; i < N; i++){
		for(int j = 0; j < N; j++){
			printf(" %g ", matrix[i * N + j]);
		}
		printf("\n\n");
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

int main(int argc, char *argv[]){
    if(argc < 3){
		printf("You must specify:\n\t- matrix number\n\t- matrix size\n");
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
   
    int m = atoi(argv[1]);
    int N = atoi(argv[2]);
    double *matrices[m];
    double *sum_total;
    int matrix_size = N * N;
    double max, min, average, total;
 
    // Memory initialize
    for (int i = 0; i < m; i++){
        matrices[i] = (double*)malloc(sizeof(double) * N * N); 
    }
    sum_total = (double*)malloc(sizeof(double) * N * N);

    // Matrix initialize
    for (int matrix_index = 0; matrix_index < m; matrix_index++){
        for(int i = 0; i < N; i++){
            for(int j = 0; j < N; j++){
                matrices[matrix_index][i * N + j] = 1; 
            }
        }
    }

    for(int i = 0; i < N; i++){
        for(int j = 0; j < N; j++){
            sum_total[i * N + j] = 0; 
        }
    }

    #ifdef DEBUG
    if (debug > 2) {
        for (int matrix_index = 0; matrix_index < m; matrix_index++) {
            printf("Initial matrix %d\n", matrix_index);
            print_matrix(matrices[matrix_index], N);
        }
        printf("Initial sum_total matrix\n");
        print_matrix(sum_total, N);
    }
    #endif

    double timetick = dwalltime();

    for (int matrix_index = 0; matrix_index < m; matrix_index++){
        max = FLT_MIN;
        min = FLT_MAX;
        average = 0.0; 
        for(int i = 0; i < N; i++){
            for(int j = 0; j < N; j++){
                average += matrices[matrix_index][i * N + j];
                if (matrices[matrix_index][i * N + j] < min) {
                    min = matrices[matrix_index][i * N + j];
                }
                if (matrices[matrix_index][i * N + j] > max) {
                    max = matrices[matrix_index][i * N + j];
                } 
            }
        }
        average = average / matrix_size;
        total = ((max - min)/average);
        
        for(int i = 0; i < N; i++){
            for(int j = 0; j < N; j++){
                matrices[matrix_index][i * N + j] *= total;
            }
        }
        for(int i = 0; i < N; i++){
            for(int j = 0; j < N; j++){
                sum_total[i * N + j] += matrices[matrix_index][i * N + j];
            }
        }

        #ifdef DEBUG
        printf("Matrix number: %d\nmin: %g  max: %g\n avg: %g\n", matrix_index, min, max, average);
        #endif
    }

    printf("tiempo total: %f\n", dwalltime() - timetick);

    
    for (int i = 0; i < m; i++){
        free(matrices[i]); 
    }
    
    free(sum_total);
    return 0;
}