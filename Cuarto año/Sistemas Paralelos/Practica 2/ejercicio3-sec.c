#include <pthread.h>
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

int main(int argc, char* argv[])
{
  // Check for arguments
  if (argc < 2) {
    printf("You must specify a verctor size\n");
    return 1;
  }

  // Set thread_number and initialize threads ids array
  long long int vector_size = atoll(argv[1]);
  int *numbers;
  long long int sum = 0;


  // Alloc memory
  numbers = (int*) malloc(sizeof(int) * vector_size);

  // Initialize vector
  for (long long int i = 0; i < vector_size; i++) {
    numbers[i] = rand()%10;
  }

  //Start processor time
  int initial_time = dwalltime();

  for (int i = 0; i < vector_size; i++) {
    sum += numbers[i];
  }

  printf("Time %g\n", dwalltime() - initial_time);
  printf("Average = %lld\n", sum/vector_size);

  free(numbers);
  return 0;
}
