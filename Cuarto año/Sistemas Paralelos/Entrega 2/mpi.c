#include <stdlib.h>
#include <stdio.h>
#include <time.h>
#include <mpi.h>

void master(int vector_size, int n, int v, int rank, int cant_proc);
void slave(int vector_size, int n, int v, int rank, int cant_proc);

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

void master(int vector_size, int n, int v, int rank, int cant_proc){
    int *vector = (int*) malloc(sizeof(int) * vector_size);
    int *vector_index = (int*) malloc(sizeof(int) * n);
    // Initialize vector
    for (int i = 0; i < vector_size; i++) {
        vector[i] = rand()%10;
    }
    printf("master: [");
    for (int i = 0; i < vector_size; i++){
        printf("%d, ", vector[i]);
    }
    printf("]\n");  
    
    for(int i = 1; i < cant_proc; i++){
        int inicio = vector_size / cant_proc * i;
        MPI_Send(&inicio, 1, MPI_INT, i, 0, MPI_COMM_WORLD);   
    }

    MPI_Barrier(MPI_COMM_WORLD);
    MPI_Bcast(vector, vector_size, MPI_INT, rank, MPI_COMM_WORLD);
    for(int i = 0; i < n; i++){
        vector_index[i] = i;
    }
    int j = n - 1;
    int total_combinations = 0;
    int loop = 1;
    int cant = 0;
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
                loop = 0;
                break;
            }
        }
        if(j ==0){
            cant++;
        }
        if (cant == vector_size / cant_proc){
            break;
        } 
        if(loop){
            for (; j < (n - 1); j++){
                vector_index[j + 1] = vector_index[j] + 1;
            }
        }
    }

    printf("Total combinations = %d\n", total_combinations);
    printf("iteraciones del proceso %d totales %llu\n\n", rank,iteraciones);    
    free(vector_index);
    free(vector);
}

void slave(int vector_size, int n, int v, int rank, int cant_proc){
    int *vector = (int*) malloc(sizeof(int) * vector_size);
    int *vector_index = (int*) malloc(sizeof(int) * n);
    
    MPI_Barrier(MPI_COMM_WORLD);
    MPI_Bcast(vector, vector_size, MPI_INT, 0, MPI_COMM_WORLD);
    
    printf("slave: [");
    for (int i = 0; i < vector_size; i++){
        printf("%d, ", vector[i]);
    }
    printf("]\n");  
    int inicio;
    MPI_Recv(&inicio, 1, MPI_INT, 0, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
    
    for(int i = 0; i < n; i++){
        vector_index[i] = inicio;
        inicio ++;
    }
    int j = n - 1;
    int total_combinations = 0;
    int loop = 1;
    int cant = 0;
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
                loop = 0;
                break;
            }
        }
        if(j ==0){
            cant++;
        }
        if (cant == vector_size / cant_proc){
            break;
        } 
        if(loop){
            for (; j < (n - 1); j++){
                vector_index[j + 1] = vector_index[j] + 1;
            }
        }
    }
    
    printf("Total combinations = %d\n", total_combinations);
    printf("iteraciones del proceso %d totales %llu\n\n", rank,iteraciones);

    free(vector_index);
    free(vector);
}
