#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/time.h>


int *numbers;
long long int ocurrences = 0;
long long int block_size;
int number;
pthread_mutex_t lock;

void * calculate_ocurrences(void *arg) {
  int id = *(int*) arg;
  long long int local_ocurrences = 0;
  long long int start = id*block_size;
  long long int limit = (1+id)*block_size;

  for (long long int i = start; i < limit; i++) {
    if (numbers[i] == number) {
      local_ocurrences++;
    }
  }

  printf("Local ocurrences = %lld\n\n", local_ocurrences);
  pthread_mutex_lock(&lock);
  ocurrences += local_ocurrences;
  pthread_mutex_unlock(&lock);
  pthread_exit(NULL);
  return 0;
}

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
  // Check arguments
  if (argc < 4) {
    printf("You must specify:\n\n\t- thread number\n\t- verctor size\n\t- number to search\n");
    return 1;
  }

  // Set thread_number and initialize threads ids array
  int thread_number = atoi(argv[1]);
  int ids[thread_number];
  long long int vector_size = atoll(argv[2]);
  number = atoi(argv[3]);

  block_size = vector_size / thread_number;


  // Alloc memory
  numbers = (int*) malloc(sizeof(int) * vector_size);

  // Initialize vector
  for (long long int i = 0; i < vector_size; i++) {
    numbers[i] = rand()%10;
  }

  // Declare threads and mutex
  pthread_t threads[thread_number];
  pthread_mutex_init(&lock, NULL);

  //Start processor time
  int initial_time = dwalltime();

  // Run threads
  for (int i = 0; i < thread_number; i++) {
    ids[i] = i;
    pthread_create(&threads[i], NULL, calculate_ocurrences, &ids[i]);
  }

  // Wait for all threads
  for (int i = 0; i < thread_number; i++) {
    pthread_join(threads[i], NULL);
  }

  printf("Time %g\n", dwalltime() - initial_time);
  printf("Total ocurrences = %lld\n", ocurrences);

  free(numbers);
  return 0;
}