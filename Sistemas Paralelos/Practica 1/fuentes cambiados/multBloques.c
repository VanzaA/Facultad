#include <stdio.h>
#include <malloc.h>
#include <stdlib.h>
#include <math.h>

/* Time in seconds from some point in the past */
double dwalltime();

void producto(double *A,double *B,double *C, int r,int N,int sizeMatrix,int sizeBlock);
void crearIdentidad(double *S, int sizeBlock, int sizeMatrix,int N,int r);
void crearMatriz(double *S, int sizeMatrix);
void imprimeMatriz(double *S,int N,int r);
void imprimeVector(double *S, int sizeMatrix);


int main (int argc, char *argv[]){

 double *A; // Matriz A
 double *B; // Matriz B
 double *C; // Matriz C
 double timetick;


//El tamano de la matriz sera n= N*r , donde N y r se reciben
//por parametro se tendran N*N bloques de r*r cada uno

if (argc < 4){
  printf("\n Falta un parametro ");
  printf("\n 1. Cantidad de bloques por dimension ");
  printf("\n 2. Dimension de cada bloque ");
  printf("\n 3. 0/1 para imprimir/no imprimir resultados ");
  return 0;
}

 int N = atoi(argv[1]);
 int r = atoi(argv[2]);
 int imprimir=atoi(argv[3]);

 int n = N*r; //dimension de la matriz
 int sizeMatrix=n*n; //cantidad total de datos matriz
 int sizeBlock=r*r; //cantidad total de datos del bloque
 int i;

 A= (double *)malloc(sizeMatrix*sizeof(double)); //aloca memoria para A
 B= (double *)malloc(sizeMatrix*sizeof(double)); //aloca memoria para B
 C= (double *)malloc(sizeMatrix*sizeof(double)); //aloca memoria para C

 crearMatriz(A, sizeMatrix);			//Inicializa A 
 crearIdentidad(B,sizeBlock,sizeMatrix,N,r); //Inicializa B como matriz identidad

  timetick = dwalltime();
 producto(A,B,C,r,N,sizeMatrix,sizeBlock);
  printf("Tiempo en segundos %f \n", dwalltime() - timetick);

//tiempo
 if (imprimir ==1){
     printf("\n\n  A (como esta almacenada): \n" );
    imprimeVector(A, sizeMatrix);

     printf("\n\n  B (como esta almacenada): \n" );
    imprimeVector(B,sizeMatrix);

    printf("\n\n  A: \n" );
    imprimeMatriz(A,N,r);

    printf(" \n\n B: \n" );
    imprimeMatriz(B,N,r);

    printf("\n\n  C: \n" );
    imprimeMatriz(C,N,r);

 } 


 printf(" \n\n Realizando comprobacion ... \n" );
 for (i=0;i<sizeMatrix ;i++ )
 {
	 if (A[i]!=C[i])
	 {
       printf("\n Error %f", C[i] );
	 }
 }
//imprimir tiempo

 free(A);
 free(B);
 free(C);

 return 0;
} //FIN MAIN


//SOLO PARA MATRICES DE IGUAL DIMENSION DE BLOQUES (N)
void producto(double *A,double *B,double *C, int r,int N,int sizeMatrix, int sizeBlock){
   int I,J,K,i,j,k;
   int despA, despB, despC,desp;

 for (i=0; i<sizeMatrix ;i++)
	  C[i]=0.0;
 
	for (I=0;I<N;I++){
		for (J=0;J<N;J++){
			despC = (I*N+J)*sizeBlock;
			for (K=0;K<N;K++){
				despA = (I*N+K)*sizeBlock;
				despB = (K*N+J)*sizeBlock;
				for (i=0;i<r;i++){
					for (j=0;j<r;j++){
						desp = despC + i*r+j;
						for (k=0;k<r;k++){
							C[desp] += A[despA + i*r+k]*B[despB + k*r+j]; 
						};
					}
				};
			};
		};	
	}; 
}


void crearIdentidad(double *S, int sizeBlock, int sizeMatrix,int N,int r){
//Inicializa la matriz S como una matriz identidad
//pone cada bloque en 0, y a los bloques diagonales pone 1 en su diag. interna

//inicializa en cero la matriz
  int i,j;
  for (i=0; i<sizeMatrix ;i++){
	  S[i]=0.0;
  };//end for j

//inicializa los N bloques de la diagonal como identidad
  for (i=0; i<sizeMatrix; i=i+(N+1)*sizeBlock){
	//en i commienza el bloque a actualizar
	  for (j=0; j<sizeBlock; j=j+r+1){
		  S[i+j]= 1.0;
	  }
  };//end for i
}

void crearMatriz(double *S, int sizeMatrix){
  int i;
  for(i=0 ;i<sizeMatrix;i++){
	S[i] = rand()%10;
  };//end i
}

void imprimeVector(double *S, int sizeMatrix){
  int i;
  printf("\n ");
  for(i=0 ;i<sizeMatrix;i++)
	printf(" %f " ,S[i]);
 
  printf("\n\n ");
}


void imprimeMatriz(double *S,int N,int r){
// Imprime la matriz pasada por parametro
// N es la cantidad de bloques, r dimension de cada bloque
  int i,j,I,J,despB;

  printf("Contenido de la matriz: \n" );
  for (I= 0; I< N; I++){
    //para cada fila de bloques (I)
    for (i= 0; i< r; i++){
       for(J=0;J<N;J++){
		   despB=(I*N+J)*r*r;
	  for (j=0;j<r;j++){
	     printf("%f ",S[despB+ i*r+j]);
	
	   };//end for j
	};//end for J
        printf("\n ");
     };//end for i

  };//end for I
  printf(" \n\n");
}


/*****************************************************************/

#include <sys/time.h>

double dwalltime()
{
	double sec;
	struct timeval tv;

	gettimeofday(&tv,NULL);
	sec = tv.tv_sec + tv.tv_usec/1000000.0;
	return sec;
}

