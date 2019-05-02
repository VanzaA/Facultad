#include <stdio.h>
#include <stdlib.h>
#include <sys/time.h>
#include <float.h>
#include <pthread.h>

int m, N, matrix_index, thread_number;
double **matrices;
double *sum_total;
double total, min, max, average;
pthread_mutex_t lock;

void* min_max_average(void *arg){
    
    int tid = *(int*)arg;
    double local_max = FLT_MIN;
    double local_min = FLT_MAX;
    double local_average = 0.0; 
    int start = ((N * N) / thread_number * tid);
    int end = ((N * N) / thread_number * (tid + 1));

    for(int i = start; i < end; i++){
        for(int j = 0; j < N; j++){
            local_average += matrices[matrix_index][i * N + j];
            if (matrices[matrix_index][i * N + j] < local_min) {
                local_min = matrices[matrix_index][i * N + j];
            }
            if (matrices[matrix_index][i * N + j] > local_max) {
                local_max = matrices[matrix_index][i * N + j];
            } 
        }
    }

    pthread_mutex_lock(&lock);
    min = min < local_min ? min : local_min;
    max = max > local_max ? max : local_max;
    pthread_mutex_unlock(&lock);
    pthread_exit(NULL);
    return 0;
    }

void * mul_total_matrix(void *arg){
    int tid = *(int*)arg;
    int start = ((N * N) / thread_number * tid);
    int end = ((N * N) / thread_number * (tid + 1));
    for(int i = start; i < end; i++){
        for(int j = 0; j < N; j++){
            matrices[matrix_index][i * N + j] *= total;
        }
    }
    pthread_exit(NULL);
    return 0;
}

void * sum_matrix_total(void *arg){
    int tid = *(int*)arg;
    int start = ((N * N) / thread_number * tid);
    int end = ((N * N) / thread_number * (tid + 1));
    for(int i = start; i < end; i++){
        for(int j = 0; j < N; j++){
            sum_total[i * N + j] += matrices[matrix_index][i * N + j];
        }
    }
    pthread_exit(NULL);
    return 0;
}


void  print_matrix(double *matrix, int N){
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
                matrices[matrix_index][i * N + j] = rand()%10; 
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
    for (matrix_index = 0; matrix_index < m; matrix_index++){
        for (int i = 0; i < thread_number; i++) {
            ids[i] = i;
            pthread_create(&threads[i], NULL, min_max_average, &ids[i]);
        }

        // Wait for all threads
        for (int i = 0; i < thread_number; i++) {
            pthread_join(threads[i], NULL);
        }
        
        for (int i = 0; i < thread_number; i++) {
            ids[i] = i;
            pthread_create(&threads[i], NULL, mul_total_matrix, &ids[i]);
        }

        // Wait for all threads
        for (int i = 0; i < thread_number; i++) {
            pthread_join(threads[i], NULL);
        }
        for (int i = 0; i < thread_number; i++) {
            ids[i] = i;
            pthread_create(&threads[i], NULL, sum_matrix_total, &ids[i]);
        }

        // Wait for all threads
        for (int i = 0; i < thread_number; i++) {
            pthread_join(threads[i], NULL);
        }  
    }
    printf("tiempo total: %f\n", dwalltime() - timetick);
    return 0;
}