#include <stdio.h>
#include <stdlib.h>
#include <sys/time.h>
#include <float.h>
#include <pthread.h>

#define ANSI_COLOR_RED "\x1b[31m"
#define ANSI_COLOR_YELLOW "\x1b[33m"
#define ANSI_COLOR_RESET "\x1b[0m"

#ifdef DEBUG
int debug = 0;
#endif

int m, N, matrix_size, thread_number;
double **matrices;
double *sum_total;
pthread_barrier_t barrier;

void print_matrix(double *matrix, int N){
	for(int i = 0; i < N; i++) {
		for(int j = 0; j < N; j++) {
			printf(" %g ", matrix[i * N + j]);
		}
		printf("\n\n");
	}
}

void print_matrix_as_vector(double *matrix, int N){
	for(int i = 0; i < N; i++){
		for(int j = 0; j < N; j++){
			printf(" %g + ", matrix[i * N + j]);
		}
	}
    printf("\n");
}


int summatory_value(int tid){
    int start = (m / thread_number) * tid;
    int limit =  (m / thread_number) * (tid + 1);
    double max, min, average, division;

    #ifdef DEBUG
    if (debug > 1) {
        printf("Hi from thread %d\nWith start = %d, limit = %d\n\n", tid, start, limit);
    }
    #endif

    for(int matrix_index = start; matrix_index < limit; matrix_index++) {
        min = matrices[matrix_index][0];
        max = matrices[matrix_index][0];
        average = 0.0; 

        for(int i = 1; i < N; i++) {
            for(int j = 0; j < N; j++) {
                average += matrices[matrix_index][i * N + j];
                if (matrices[matrix_index][i * N + j] < min) {
                    min = matrices[matrix_index][i * N + j];
                }
                if (matrices[matrix_index][i * N + j] > max) {
                    max = matrices[matrix_index][i * N + j];
                }
            }
        }
        average = average / (N * N);
        division = (max - min) / average;
        #ifdef DEBUG
        if (debug > 1) {
            printf("min = %g\nmax = %g\naverage = %g\ndivision = %g\n\n", min, max, average, division);
        }
        #endif
        for(int i = 0; i < N; i++) {
            for(int j = 0; j < N; j++) {
                matrices[matrix_index][i * N + j] *= division;
            }
        }
    }

    return 0;
}

int summatory_operation(int tid){
    int start = (N / thread_number) * tid;
    int limit =  (N / thread_number) * (tid + 1);

    #ifdef DEBUG
    if (debug > 1) {
        printf("Hi from thread %d\nWith start = %d, limit = %d\n\n", tid, start, limit);
    }
    #endif

    for(int matrix_index = 0; matrix_index < m; matrix_index++) {
        for(int i = start; i < limit; i++) {
            for(int j = 0; j < N; j++) {
                sum_total[i * N + j] += matrices[matrix_index][i * N + j];
            }
        }
    }

    return 0;
}

void* summatory(void *arg){
    int tid = *(int *)arg;

    summatory_value(tid);

    pthread_barrier_wait(&barrier);

    summatory_operation(tid);

    pthread_exit(NULL);
    return 0;
}

double dwalltime(){
  double sec;
  struct timeval tv;

  gettimeofday(&tv,NULL);
  sec = tv.tv_sec + tv.tv_usec/1000000.0;
  return sec;
}

int main(int argc, char *argv[]){
    if(argc < 4){
		printf("You must specify:\n\t- thread number\n\t- matrix number\n\t- matrix size\n");
		exit(1);
	}

    thread_number = atoi(argv[1]);
    m = atoi(argv[2]);
    N = atoi(argv[3]);
    matrix_size = N * N;

    #ifdef DEBUG
    if (getenv("DEBUG")) {
        debug = atoi(getenv("DEBUG"));
        printf(ANSI_COLOR_RED "Debug mode - Level %d\n" ANSI_COLOR_RESET, debug);
    }  else {
        printf(ANSI_COLOR_YELLOW "Debug binary\nSet DEBUG environment variable with a level\n" ANSI_COLOR_RESET);
    }
    #endif

    matrices = (double**)malloc(sizeof(double*)*m);
    int ids[thread_number];
 
    // memory assign
    for (int i = 0; i < m; i++) {
        matrices[i] = (double*)malloc(sizeof(double) * matrix_size);
    }
    sum_total = (double*)malloc(sizeof(double) * matrix_size);

    // matrix initialize
    for (int matrix_index = 0; matrix_index < m; matrix_index++) {
        for(int i = 0; i < N; i++) {
            for(int j = 0; j < N; j++) {
                matrices[matrix_index][i * N + j] = rand()%10;
            }
        }
    }

    for(int i = 0; i < N; i++) {
        for(int j = 0; j < N; j++) {
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

    // Declare threads and mutex
    pthread_t threads[thread_number];
    pthread_barrier_init(&barrier, NULL, thread_number);


    // Start processor time
     double timetick = dwalltime();

    for (int i = 0; i < thread_number; i++) {
        ids[i] = i;
        pthread_create(&threads[i], NULL, summatory, &ids[i]);
    }

    // Wait for all threads
    for (int i = 0; i < thread_number; i++) {
        pthread_join(threads[i], NULL);
    }

    printf("tiempo total: %f\n", dwalltime() - timetick);

    #ifdef DEBUG
    if (debug > 3) {
        printf("\n\nFinal sum_total matrix\n");
        print_matrix(sum_total, N);
    }
    #endif
    
    for (int i = 0; i < m; i++){
        free(matrices[i]); 
    }
    
    return 0;
}