#include <stdio.h>
#include <stdlib.h>
#include <sys/time.h>

double dwalltime()
{
	double sec;
	struct timeval tv;

	gettimeofday(&tv,NULL);
	sec = tv.tv_sec + tv.tv_usec/1000000.0;
	return sec;
}

void printf_vector(int *vector, int vector_size)
{
  printf("[");
  for (int i = 0; i < vector_size; i++) {
    printf(" %d", vector[i]);
    if (i != vector_size-1) {
      printf(",");
    }
  }
  printf(" ]\n");
}

int main(int argc, char *argv[])
{
  if (argc < 2) {
    printf("You must specify a verctor size\n");
    return 1;
  }

  int initial_time, vector_size = atoi(argv[1]);
  int *vector;


  vector = (int*) malloc(sizeof(int) * vector_size);

  for (int i = 0; i < vector_size; i++) {
    vector[i] = rand()%10;
  }


  initial_time = dwalltime();



  printf("Time %g\n", dwalltime() - initial_time);

  return 0;
}