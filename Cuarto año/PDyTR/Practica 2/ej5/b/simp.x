/* Protocol definition file for a simple RPC application */
/* There are 2 remote procedures - an add procedure and
   a subtract procedure. Each must be called with a single
   parameter, a structure that holds 2 integers. The return
   value of each procedure is an int.

   Notice that I can put comments in the protocol definition file!
*/

/* I can also put defines in here - and use them, although they will
not show up in the .h file produced by rpcgen. 
*/

#define VERSION_NUMBER 1

/* If I want to put something explicitly in the .h file produced by rpcgen
   I can start it with %:
*/

%#define foo 127

/* rpcgen just strips the '%' and puts the rest in the .h file */


/* here is the definition of the data type that will be passed to
   both of my remote procedures */

struct operands {
	int x;
	int y;
};


/* note that this data type will be defined in the .h file produced by
   rpcgen. AND it will be typedef'd as well, so I can refer to it as
   type 'operands', I don't need to use 'struct operands'. 
*/





/* OK - here is the real stuff - the program, version and procedure
definitions. Remember this is not C, although it looks similar */

program SIMP_PROG {
   version SIMP_VERSION {
     int ADD(operands) = 1;
     int SUB(operands) = 2;
   } = VERSION_NUMBER;
} = 555555555;


/* This defines the RPC program number as 555555555, although within the
client and server code I can just refer to it as SIMP_PROG. In other words,
rpcgen will put this line in the .h file:

#define SIMP_PROG 555555555

The version number is 1, but I can use SIMP_VERSION.

The remote procedure numbers are 1 for the add routine and 2 for the
subtract routine, although I can use the symbolic constants ADD and SUB.

------------------------------
On the client side, a stub will be generated in simp_clnt.c for each
of the remote procedures. The stub prototype for the add procedure looks like
this:

int * add_1(operands *, CLIENT *);

the "add" part of the name comes from the ADD in the protocol
definition. the "_1" indicates the version number (if we set the
version to 100, the stub name would be "add_100").

When we call this routine in the client, this stub takes care of 
all the RPC stuff for us, all we have to do is give it the address
of an operands structure with the x and y fields set, and an RPC handle
that is already established (the CLIENT * arg).

If this isn't easy enough - just do this:

rpcgen -C -Sc simp.x

the -C tells rpcgen to output ANSI C code (not K&R).
the -Sc tells rpcgen to output sample client code that makes a
call to each of the remote procedures, you can take this
code and adjust it to your needs.

---------------------------------

On the server side, rpcgen creates the file simp_svc.h that includes
a main() and an RPC dispatch routine. All we need to supply is the
actual remote procedure subroutines. The server expects these routines
to have prototypes like this:

int *add_1_svc(operands*, struct svc_req *);
int *sub_1_svc(operands*, struct svc_req *);

We need to write these routines, and include them in the link used
to create the server executable.
 
*/


