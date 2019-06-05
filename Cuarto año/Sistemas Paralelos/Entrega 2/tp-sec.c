#include <stdio.h>
#include <stdlib.h>
#include <sys/time.h>

double dwalltime(){
  double sec;
  struct timeval tv;

  gettimeofday(&tv,NULL);
  sec = tv.tv_sec + tv.tv_usec/1000000.0;
  return sec;
}

int main(int argc, char* argv[]){
  // Check for arguments
  if (argc < 4) {
    printf("You must specify:\n\n\t- Vector size\n\t- Final value\n\t- Number of combinations");
    return 1;
  }
  // First argument = Vector size
  // Second argument = Final value
  // Third argument = Number of combinations

  // Initialize variable
  int vector_size = atoll(argv[1]);
  int value = atoi(argv[2]);
  int combinations = atoi(argv[3]); 
  if(combinations > vector_size){
    printf("la combinacion de numeros -n- no puede ser mayor al tamaño del vector");
    return 1;
  }

  unsigned long long int iteraciones = 0;
  
  int sum;
  int j = combinations - 1;
  int total_combinations = 0;
  int loop = 1; 
  // Alloc memory
  int *vector = (int*) malloc(sizeof(int) * vector_size);
  int *vector_index = (int*) malloc(sizeof(int) * combinations);
  
  
  // Initialize vector
  for (int i = 0; i < vector_size; i++) {
    vector[i] = rand()%10;
  }
   
  for(int i = 0; i < combinations; i++){
    vector_index[i] = i;
  }
  
  
  //Start processor time
  int initial_time = dwalltime();
  
  while(loop){
    iteraciones++;
    sum = 0;
    for (int i = 0; i < combinations; i++){
      sum += vector[vector_index[i]];
    }
    if(sum == value){
      total_combinations++;
    }
    vector_index[j]++;
    while(vector_index[j] == (vector_size - combinations + j + 1)){
      if(j > 0){
        vector_index[j - 1]++;
        j--;
      }
      else{
        loop = 0;
        break;
      }
    }
     if(loop){
      for (; j < (combinations - 1); j++){
        vector_index[j + 1] = vector_index[j] + 1;
      }
    }
  }

  printf("Time %g\n", dwalltime() - initial_time);
  printf("Total combinations = %d\n", total_combinations);
  printf("iteraciones totales %llu\n\n", iteraciones);

  free(vector_index);
  free(vector);
  return 0;
}
