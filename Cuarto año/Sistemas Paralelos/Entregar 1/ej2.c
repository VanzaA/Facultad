#include <stdio.h>
#include <stdlib.h>
#include <sys/time.h>
#include <float.h>
#include <pthread.h>

int m, N, thread_number;
double **matrices;
double *sum_total;
pthread_mutex_t lock;

void  print_matrix(double *matrix, int N){
	for(int i = 0; i < N; i++){
		for(int j = 0; j < N; j++){
			printf(" %g ", matrix[i * N + j]);
		}
		printf("\n\n");
	}
}


void* summatory_value(void *arg){
    int tid = *(int *)arg;
    int start = (m / thread_number) * tid;
    int limit =  (m / thread_number) * (tid + 1);
    double max, min, average, division;

    for(int matrix_index = start; matrix_index < limit; matrix_index++){
        max = DBL_MIN;
        min = DBL_MAX;
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
        average = average / (N * N);
        division = (max - min) / average;
        for(int i = 0; i < N; i++){
            for(int j = 0; j < N; j++){
                matrices[matrix_index][i * N + j] *= division;
            }
        }
    }
    pthread_exit(NULL);
    return 0;
}

void* summatory_operation(void* arg){
    int tid = *(int *)arg;
    int start = (m / thread_number) * tid;
    int limit =  (m / thread_number) * (tid + 1);
    double *matrix_aux;
    
    matrix_aux = (double*)calloc(N * N, sizeof(double));
    
    for(int matrix_index = start; matrix_index < limit; matrix_index++){
        for(int i = 0; i < N; i++){
            for(int j = 0; j < N; j++){
                matrix_aux[i * N + j] += matrices[matrix_index][i * N + j];
            }
        }
    }
    pthread_mutex_lock(&lock);
    for(int i = 0; i < N; i++){
        for(int j = 0; j < N; j++){
            sum_total[i * N + j] += matrix_aux[i * N + j];
        }
    }
    pthread_mutex_unlock(&lock);
    free(matrix_aux);
    pthread_exit(0);
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

    matrices = (double**)malloc(sizeof(double*)*m);
    int ids[thread_number];
 
    //asigno memoria
    for (int i = 0; i < m; i++){
        matrices[i] = (double*)malloc(sizeof(double) * N * N);
    }
    sum_total = (double*)malloc(sizeof(double) * N * N);

    //inicializo matrices
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

    // Declare threads and mutex
    pthread_t threads[thread_number];
    pthread_mutex_init(&lock, NULL);

    // Start processor time
     double timetick = dwalltime();

    for (int i = 0; i < thread_number; i++) {
        ids[i] = i;
        pthread_create(&threads[i], NULL, summatory_value, &ids[i]);
    }

        // Wait for all threads
    for (int i = 0; i < thread_number; i++) {
        pthread_join(threads[i], NULL);
    }
    
    for (int i = 0; i < thread_number; i++) {
        ids[i] = i;
        pthread_create(&threads[i], NULL, summatory_operation, &ids[i]);
    }

        // Wait for all threads
    for (int i = 0; i < thread_number; i++) {
        pthread_join(threads[i], NULL);
    }
    
        
    printf("tiempo total: %f\n", dwalltime() - timetick);
    
    for (int i = 0; i < m; i++){
        free(matrices[i]); 
    }
    
    free(sum_total);
    free(matrices);
    return 0;
}