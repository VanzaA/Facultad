/* Protocol definition file for a variable length add  RPC application */

typedef int iarray<>;


program VADD_PROG {
   version VADD_VERSION {
     int VADD(iarray) = 1;
   } = 1;
} = 555575555;

