#include <stdio.h>
#include <mpi.h>
#include <unistd.h>
#include <stdlib.h>
#include <sys/time.h>

double dwalltime(){
  double sec;
  struct timeval tv;
  gettimeofday(&tv,NULL);
  sec = tv.tv_sec + tv.tv_usec/1000000.0;
  return sec;
}

void master(int vector_size,int *vector, int n, int v, int cant_proc);
void slave(int vector_size,int *vector, int n, int v, int rank, int cant_proc);
int calcular(int *vector, int n,  int v, int vector_size, int index);

int main(int argc, char** argv) {
    MPI_Init(&argc, &argv);
    int rank, amount;
    MPI_Comm_size(MPI_COMM_WORLD, &amount);
    MPI_Comm_rank(MPI_COMM_WORLD, &rank);
    if (argc < 4) {
        printf("You must specify:\n\n\t- Vector size\n\t- Final value\n\t- Number of combinations\n\n");
        return 1;
    }
    
    int vector_size = atoi(argv[1]);
    int v = atoi(argv[2]);
    int n = atoi(argv[3]);    
    int *vector = (int*) malloc(sizeof(int) * vector_size);
    if(n > vector_size) {
        printf("la combinacion de numeros -n- no puede ser mayor al tamaño del vector");
        return 1;
    }

    if (rank == 0) {
        master(vector_size, vector,n, v, amount);
    }
    else {
        slave(vector_size, vector, n, v, rank, amount);
    }
    
    MPI_Finalize();
    return 0;
}

int calcular(int *vector, int n, int v, int vector_size, int index){
    int j = n - 1;
    int loop = 1;
    int *vector_index = (int*) malloc(sizeof(int) * n);
    for (int i = 0; i < n; i++) {
        vector_index[i]= index + i;
    }
    long unsigned total_combinations = 0;
    
    while(loop) {
        int sum = 0;
        for (int i = 0; i < n; i++) {
            sum += vector[vector_index[i]];
        }
        if(sum == v) {
            total_combinations++;
        }
        vector_index[j]++;
        while(vector_index[j] == (vector_size - n + j + 1)) {
            if(j > 1) {
                vector_index[j - 1]++;
                j--;
            }
            else {
                loop = 0;
                break;
            }
        }
        if(j == 0) {
            break;
        }
        if(loop) {
            for (; j < (n - 1); j++) {
                vector_index[j + 1] = vector_index[j] + 1;
            }
        }
    }

    free(vector_index);
    return total_combinations;
}

void master(int vector_size,int *vector, int n, int v,  int cant_proc){
    int index = 0;
    int id_slave;
    int messageAvailable = 0;
    MPI_Status status;
    long unsigned total_combinations = 0;
    // Initialize vector
    for (int i = 0; i < vector_size; i++) {
        vector[i] = 1;
    }
    MPI_Bcast(vector, vector_size, MPI_INT, 0, MPI_COMM_WORLD);

    double initial_time = dwalltime();

    while(index <  vector_size - n) {
        MPI_Iprobe(MPI_ANY_SOURCE, 0, MPI_COMM_WORLD, &messageAvailable, &status);
        if(messageAvailable) {
            MPI_Recv(&id_slave, 1, MPI_INT, status.MPI_SOURCE, 0, MPI_COMM_WORLD, &status);
            MPI_send(&index, 1, MPI_INT, id_slave, 0, MPI_COMM_WORLD, MPI_COMM_WORLD);
            index++;
            messageAvailable = 0;
        } else {
            total_combinations += calcular(vector, n, v, vector_size, index);
            index++;
        }
    }

    index = -1;
    for(int id = 1; id < cant_proc; id++) {
        MPI_Send(&index, 1, MPI_INT, id, 0, MPI_COMM_WORLD);
    }
    long unsigned global_combinations = 0 ;

    MPI_Reduce(&zero, &global_combinations, 1, MPI_UNSIGNED_LONG, MPI_SUM, 0, MPI_COMM_WORLD);

    printf("proceso: (%d) ---- Tiempo total = %g, combinaciones = %lu\n\n", rank, dwalltime() - initial_time, total_combinations);
    
    printf("Total combinations : %lu\n\n", global_combinations);

    free(vector);
}

void slave(int vector_size, int *vector, int n, int v, int rank, int cant_proc){
    int index = 0;
    long unsigned total_combinations = 0;

    MPI_Bcast(vector, vector_size, MPI_INT, 0, MPI_COMM_WORLD);

    double initial_time = dwalltime();
    while(index != -1) {
        MPI_Send(&rank, 1, MPI_INT, 0, 0, MPI_COMM_WORLD);
        MPI_Recv(&index, 1, MPI_INT, 0, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
        if (index != -1) {
            total_combinations += calcular(vector, n, v, vector_size, index);
        }
    }

    int global_combinations = 0;

    MPI_Reduce(&total_combinations, &global_combinations, 1, MPI_UNSIGNED_LONG, MPI_SUM, 0, MPI_COMM_WORLD);

    printf("proceso: (%d) ---- Tiempo total = %g, combinaciones = %lu\n\n", rank, dwalltime() - initial_time, total_combinations);

    free(vector);
}
