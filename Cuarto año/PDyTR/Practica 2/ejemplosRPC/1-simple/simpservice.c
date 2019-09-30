/* Definition of the remote add and subtract procedure used by 
   simple RPC example 
   rpcgen will create a template for you that contains much of the code
   needed in this file is you give it the "-Ss" command line arg.
*/
#include <stdio.h>
#include "simp.h"

/* Here is the actual remote procedure */
/* The return value of this procedure must be a pointer to int! */
/* we declare the variable result as static so we can return a 
   pointer to it */

int *
add_1_svc(operands *argp, struct svc_req *rqstp)
{
	static int  result;

	sleep(26);	
	printf("Got request: adding %d, %d\n",
	       argp->x, argp->y);

	result = argp->x + argp->y;


	return (&result);
}



int *
sub_1_svc(operands *argp, struct svc_req *rqstp)
{
	static int  result;
	printf("Got request: subtracting %d, %d\n",
	       argp->x, argp->y);

	result = argp->x - argp->y;


	return (&result);
}



