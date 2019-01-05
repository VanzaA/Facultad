#include <stdio.h>
#include <stdlib.h>


int main(int argc, char const *argv[])
{
	size_t s;
	char *buffer;
	size_t  bufsize = 32;
	buffer = (char*)malloc(bufsize * sizeof(char));
	s=getline(&buffer,&bufsize,stdin);
	printf("%s\n",buffer);		
	printf("%ld\n",s);
	return 0;
}