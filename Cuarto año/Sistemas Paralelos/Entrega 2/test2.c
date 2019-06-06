#include <stdlib.h>
#include <stdio.h>
#include <mpi.h>
#include <sys/time.h>

double dwalltime(){
  double sec;
  struct timeval tv;
  gettimeofday(&tv,NULL);
  sec = tv.tv_sec + tv.tv_usec/1000000.0;
  return sec;
}

void master(int vector_size, int n, int v, int rank, int cant_proc);
void slave(int vector_size, int n, int v, int rank, int cant_proc);
int calcular(int *vector, int n, int *vector_index, int v, int vector_size, int index, int *buffer);

int main(int argc, char** argv) {
    MPI_Init(&argc, &argv);
    int rank, amount;
    MPI_Comm_size(MPI_COMM_WORLD, &amount);
    MPI_Comm_rank(MPI_COMM_WORLD, &rank);
    int vector_size = atoi(argv[1]);
    int v = atoi(argv[2]);
    int n = atoi(argv[3]);    

    if (argc < 4) {
        printf("You must specify:\n\n\t- Vector size\n\t- Final value\n\t- Number of combinations");
        return 1;
    }
    if(n > vector_size){
        printf("la combinacion de numeros -n- no puede ser mayor al tamaño del vector");
        return 1;
    }

    if (rank == 0){
        master(vector_size, n, v, rank, amount);
    }
    else{
        slave(vector_size, n, v, rank, amount);
    }
    
    MPI_Finalize();
    return 0;
}

int calcular(int *vector, int n, int *vector_index, int v, int vector_size, int index, int *total_combinations){
    int j = n - 1;
    int loop = 1;
    unsigned long long int iteraciones = 0;
    
    while(loop){
        iteraciones++;
        int sum = 0;
        for (int i = 0; i < n; i++){
            sum += vector[vector_index[i]];
        }
        if(sum == v){
            total_combinations++;
        }
        vector_index[j]++;
        while(vector_index[j] == (vector_size - n + j + 1)){
            if(j > 0){
                vector_index[j - 1]++;
                j--;
            }
            else{   
                break;
            }
        }
        if(j == 0){
            break;
        } 
        if(loop){
            for (; j < (n - 1); j++){
                vector_index[j + 1] = vector_index[j] + 1;
            }
        }
    }
    return iteraciones;
}

void master(int vector_size, int n, int v, int rank, int cant_proc){
    int *vector = (int*) malloc(sizeof(int) * vector_size);
    int *vector_index = (int*) malloc(sizeof(int) * n);
    // Initialize vector
    int total_combinations = 0;
    unsigned long long int iteraciones = 0;
   
    for (int i = 0; i < vector_size; i++) {
        vector[i] = rand()%10;
    }

    int index = 0;
    int messageAvailable = 0;
    int buffer[2];
    MPI_Status status;
    
    MPI_Barrier(MPI_COMM_WORLD);
    MPI_Bcast(vector, vector_size, MPI_INT, rank, MPI_COMM_WORLD);
    for(int id = 1; id < cant_proc; id++){
        MPI_Recv(&buffer, 2, MPI_INT, status.MPI_SOURCE, 0, MPI_COMM_WORLD, &status);
        MPI_Send(&index, 1, MPI_INT, id, 0, MPI_COMM_WORLD);
        index++;
    }
    
    double initial_time = dwalltime();

    while(index <  vector_size - n){
        MPI_Iprobe(MPI_ANY_SOURCE, 0, MPI_COMM_WORLD, &messageAvailable, &status);
        if(messageAvailable){
            MPI_Recv(&buffer, 2, MPI_INT, status.MPI_SOURCE, 0, MPI_COMM_WORLD, &status);
            MPI_Send(&index, 1, MPI_INT, status.MPI_SOURCE, 0, MPI_COMM_WORLD);
            total_combinations += buffer[0];
            iteraciones += buffer[1];
            index++;
            messageAvailable = 0;
        } else { 
        iteraciones += calcular(vector, n, vector_index, v, vector_size, index, &total_combinations);
        index++;
        }
        index = -1;
        for(int id = 1; id < cant_proc; id++){
            MPI_Send(&index, 1, MPI_INT, id, 0, MPI_COMM_WORLD);
            MPI_Recv(&buffer, 2, MPI_INT, id, 0, MPI_COMM_WORLD, &status);
            total_combinations += buffer[0];
            iteraciones += buffer[1];
        }
        
    }
    printf("Total combinations = %d\n", buffer[0]);
    printf("iteraciones del proceso %d totales %llu\n\n", rank,buffer[1]);  
    printf("proceso: %d Time %g\n", rank, dwalltime() - initial_time);
  
    free(vector_index);
    free(vector);
}

void slave(int vector_size, int n, int v, int rank, int cant_proc){
    int *vector = (int*) malloc(sizeof(int) * vector_size);
    int *vector_index = (int*) malloc(sizeof(int) * n);
    
    int buffer[2];
    int index;
    int j = n - 1;
    int total_combinations = 0;
    int loop = 1;
    unsigned long long int iteraciones = 0;
  
    MPI_Barrier(MPI_COMM_WORLD);
    MPI_Bcast(vector, vector_size, MPI_INT, 0, MPI_COMM_WORLD);
    
    MPI_Send(&buffer, 2, MPI_INT, 0, 0, MPI_COMM_WORLD);
    MPI_Recv(&index, 1, MPI_INT, 0, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
    for(int i = 0; i < n; i++){ 
        vector_index[i] = index + i;
    }
        
    double initial_time = dwalltime();
    while(index != -1){
        buffer[0] = 0;
        buffer[1] = calcular(vector, n, vector_index, v, vector_size, index, &buffer[0]);
        MPI_Send(&buffer, 2, MPI_INT, 0, 0, MPI_COMM_WORLD);
        MPI_Recv(&index, 1, MPI_INT, 0, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
    }
    MPI_Send(&buffer, 2, MPI_INT, 0, 0, MPI_COMM_WORLD);

    printf("proceso: %d Time %g\n", rank, dwalltime() - initial_time);
  
    free(vector_index);
    free(vector);
}
