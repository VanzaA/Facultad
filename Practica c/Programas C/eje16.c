#include <stdio.h>
#define mi_macro(t, a, b) {t _z##a##_##b = a; a = b; b = _z##a##_##b;}

int main(){

	int a = 6;
	int b = 7;
	mi_macro(int, a, b);
	printf("%d\n", a);
	return 0;


}
