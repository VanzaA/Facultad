#include <pthread.h>
#include <stdlib.h>
#include <stdio.h>

//funcion para testear
void * hello_world(void * arg){
    printf("hola putos soy el thread %d \n", *(int*)arg);
    return 0;
}

int main(int argc, char *argv[]){
   
    //si no ingrese parametros los pido
    if (argc < 2){
        puts("eh vo, pone parametro amigos");
        return 1;
    }
    
    //variables que siempre voy a declarar
    int thread_number = atoi(argv[1]);
    int ids[thread_number];
    pthread_attr_t attr;
    pthread_attr_init(&attr);
    pthread_t threads[thread_number];

    
    //creo los threads
    for(int i=0; i < thread_number;i++){
        ids[i]=i;
        pthread_create(&threads[i], &attr, hello_world, &ids[i]);
    }

    //el join ayuda a esperar a los threads
    for(int i=0; i < thread_number;i++){
        pthread_join(threads[i],NULL);
    }

    return 0;
}
