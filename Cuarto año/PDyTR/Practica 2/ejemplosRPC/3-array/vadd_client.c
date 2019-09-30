/* RPC client for addition of variable length array */

#include <stdio.h>
#include <stdlib.h>
#include "vadd.h"  /* Created for us by rpcgen - has everything we need ! */

/* Wrapper function takes care of calling the RPC procedure */

int vadd( CLIENT *clnt, int *x, int n) {
  iarray arr;
  int *result;

  /* Set up the iarray to send to the server */
  arr.iarray_len = n;
  arr.iarray_val = x;

  /* Call the client stub created by rpcgen */
  result = vadd_1(&arr,clnt);
  if (result==NULL) {
    fprintf(stderr,"Trouble calling remote procedure\n");
    exit(0);
  }
  return(*result);
}


int main( int argc, char *argv[]) {
  CLIENT *clnt;
  int *ints,n;
  int i;
  int res;
  if (argc<3) {
    fprintf(stderr,"Usage: %s hostname num1 num2 ...\n",argv[0]);
    exit(0);
  }

  /* Create a CLIENT data structure that reference the RPC
     procedure VADD_PROG, version VADD_VERSION running on the
     host specified by the 1st command line arg. */

  clnt = clnt_create(argv[1], VADD_PROG, VADD_VERSION, "udp");

  /* Make sure the create worked */
  if (clnt == (CLIENT *) NULL) {
    clnt_pcreateerror(argv[1]);
    exit(1);
  }

  /* get the 2 numbers that should be added */
  n = argc-2;
  ints = (int *) malloc(n * sizeof( int ));
  if (ints==NULL) {
    fprintf(stderr,"Error allocating memory\n");
    exit(0);
  }
  for (i=2;i<argc;i++) {
    ints[i-2] = atoi(argv[i]);
  }

  res = vadd(clnt,ints,n);
  printf("%d",ints[0]);
  for (i=1;i<n;i++) 
    printf(" + %d",ints[i]);
  printf(" = %d\n",res);
  return(0);
}



