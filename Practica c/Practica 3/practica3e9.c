#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/*char * my_strcat(char *dest, const char *src){
  printf("llegue aca1\n");
  int uno;

  int size_src = strlen(src);
  int size_dest = strlen(dest);
  int i;
  printf("llegue aca\n");
  dest = realloc(dest,(size_dest + size_src) -1);
  printf("llegue acax2\n");
  scanf("%d",&uno);
  for (i = 0; i <= size_src; i++){
    dest[size_dest+i] = src[i];
  }
  return dest;
}
*/
char *my_strcat (char *dest, const char *src)
  {
    const char *p;
    char *q;

    for (q = dest; *q != '\0'; q++)
       ;

    for(p = src; *p != '\0'; p++, q++)
       *q = *p;

    *q = '\0';

    return dest;
  }
int main(){

  char s1[] = {'g','a','t','o',' ','n','e','g','r','o','\0'};
  char s2[] = {' ','h','o','l','a',' ','t','u','t','u','\0'};
  printf("%s dest \n",my_strcat(s1,s2));
  return 0;

}
