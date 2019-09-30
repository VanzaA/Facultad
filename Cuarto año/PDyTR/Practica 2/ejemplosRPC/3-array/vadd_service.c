/* Definition of the remote add procedure used by 
   simple RPC example 
   rpcgen will create a template for you that contains much of the code
   needed in this file is you give it the "-Ss" command line arg.
*/

#include "vadd.h"
#include <stdio.h>

/* Here is the actual remote procedure */
/* The return value of this procedure must be a pointer to int! */
/* we declare the variable result as static so we can return a 
   pointer to it */

int *
vadd_1_svc(iarray *argp, struct svc_req *rqstp)
{
	static int  result;
	int i;
	
	printf("Got request: adding %d numbers\n",
	       argp->iarray_len);

	result=0;
	for (i=0;i<argp->iarray_len;i++)
	  result += argp->iarray_val[i];

	return (&result);
}


