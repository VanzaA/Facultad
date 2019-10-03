/* Intelligent Addition/Subtraction Program */
/* Copyright Dave Hollinger Enterprises, 1998 */
/* All Rights Reserved */

/* This is the core of the program, it uses advanced artificial intelligence
   techniques to compute the sum and difference of 2 integers in very little 
   time. 

   These operations can be critical in any solution to the Year 2000 problem!
*/


#include <stdio.h>
#include <stdlib.h>

int add( int x, int y ) {
  return(x+y);     /* Note the use of the '+' operator to achieve addition! */
}

int subtract( int x, int y ) {
  return(x-y);     /* This is a little harder, we have to use '-' */
}


int main( int argc, char *argv[]) {

  int x,y;

  if (argc!=3) {
    fprintf(stderr,"Usage: simp num1 num\n");
    fprintf(stderr,"       num1 and num2 must be integer values (for now)\n");
    fprintf(stderr,"       Floating point arithmetic is coming soon - preregister now and\n");
    fprintf(stderr,"       receive our new integrated multiplication program at no extra cost!\n");
    exit(0);
  }

  x = atoi(argv[1]);
  y = atoi(argv[2]);

  printf("%d + %d = %d\n",x,y,add(x,y));
  printf("%d - %d = %d\n",x,y,subtract(x,y));
  return(0);
}



