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
  
  int sum;
  int j = combinations;
  int total_combinations = 0;
  // Alloc memory
  int *vector = (int*) malloc(sizeof(int) * vector_size);
  int *vector_index = (int*) malloc(sizeof(int) * combinations);
  
  
  // Initialize vector
  //printf("el vector contiene los siguientes valores[");
  for (int i = 0; i < vector_size; i++) {
    vector[i] = rand()%10;
  //  printf("%d, ", vector[i]);
  }
  //printf("] \n\n");
  for(int i = 1; i <= combinations; i++){
    vector_index[i] = i;
  }
  
  //Start processor time
  int initial_time = dwalltime();
  
  while(j > 0){
    while(vector_index[j] <= (vector_size - combinations + j)){
      sum = 0;
      printf("[");
      for (int i = 1; i < (combinations + 1); i++){
        sum += vector[vector_index[i]];
        printf("%d, ", vector[vector_index[i]]);
      }
      printf("]\n");
      if(sum == value){
        total_combinations++;
      }
      if(vector_index[j] == vector_size - combinations + j){
          break;
      }
      vector_index[j]++;
    }
  
  
  //cuando llega al final del arreglo, reseteo el valor del ultimo indice al que comenzo esta iteracion

    while(vector_index[j] == (vector_size - combinations + j)){
      vector_index[j] = vector_index[j-1]+1;
      if(j < combinations){
          vector_index[j+1] = vector_index[j]+1;
      }
      j--;
    }

  //si j llega a 0, significa que se incrementaron todos al maximo
    if(j == 0){
      break;
    }
  //incremento el indice actual
    vector_index[j]++;

    for(; j < combinations ;j++){
      //si el indice actual es igual al siguiente, incremento el siguiente
      if(vector_index[j] == vector_index[j+1]){
          vector_index[j+1]++;
      }
    }

  }


  printf("Time %g\n", dwalltime() - initial_time);
  printf("Total combinations = %d\n", total_combinations);

  free(vector_index);
  free(vector);
  return 0;
}
