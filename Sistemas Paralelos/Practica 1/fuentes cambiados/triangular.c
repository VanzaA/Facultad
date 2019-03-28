#include<stdio.h>
#include<stdlib.h>

int N=100;

//Para calcular tiempo
double dwalltime(){
        double sec;
        struct timeval tv;

        gettimeofday(&tv,NULL);
        sec = tv.tv_sec + tv.tv_usec/1000000.0;
        return sec;
}

int main(int argc,char* argv[]){

 double *A,*BT,*C;
 int i,j,k;
 int check = 1;
 double timetick;

 //Controla los argumentos al programa
 if ((argc != 2) || ((N = atoi(argv[1])) <= 0) ) {
    printf("\nUsar: %s n\n  n: Dimension de la matriz (nxn X nxn)\n", argv[0]);
    exit(1);
  }
 
 //Aloca memoria para las matrices
 A=(double*)malloc(sizeof(double)*N*N);
 BT=(double*)malloc(sizeof(double)*N*N);
 C=(double*)malloc(sizeof(double)*N*N);

 //Inicializa las matrices
 //La matriz A se inicializa todas las columnas en 0 menos la ultima
 //La matriz BT se inicializa triangular inferior con 1
 //Esto permite que el resultado sean todos 1
 //La matriz C se inicializa en 0
 for(i=0;i<N;i++){
  for(j=0;j<N;j++){

   if(j<N-1){
    A[i*N+j]=0;
   }else{
    A[i*N+j]=1;
   }

   if(i>=j){
    BT[i+N*j]=N;
   }else{
    BT[i+N*j]=0;	
   }

   C[i*N+j]=0;	
  }
 }

 //Realiza la multiplicacion 
 timetick = dwalltime();

 for(i=0;i<N;i++){
  for(j=0;j<N;j++){
   for(k=0;k<N;k++){
    C[i*N+j]=C[i*N+j] + A[i*N+k]*BT[k+j*N];
   }
  }
 }

 printf("Tiempo en segundos %f\n", dwalltime() - timetick);

 //Verifica el resultado
 for(i=0;i<N;i++){
  for(j=0;j<N;j++){
   check = check && (C[i*N+j]==N);
  }
 }

 if(check){
  printf("Multiplicacion de matriz triangular correcta\n");
 }else{
  printf("Multiplicacion de matriz triangular erroneo\n");
 }
 
 free(A);
 free(BT);
 free(C);

 return(0);
}
