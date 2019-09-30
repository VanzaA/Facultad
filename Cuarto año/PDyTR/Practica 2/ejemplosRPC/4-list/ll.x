/* Linked list example - a linked list of integers,
   The remote procedure returns the sum
*/
#define VERSION_NUMBER 1

struct foo {
	int x;
	foo *next;
};


program LL_PROG {
   version LL_VERSION {
     int SUM(foo) = 1;
   } = VERSION_NUMBER;
} = 555553555;

