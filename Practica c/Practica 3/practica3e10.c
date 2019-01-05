#include <stdio.h>
#include <string.h>
int es_palindromo(const char *word){
  int ok = 1,i=0;
  unsigned p = strlen(word)-1;
  printf("%d \n",p);
  while((ok)&&(p>=i)){
    if (word[i] == word[p])
      ok = 1;
    else
      ok = 0;
    i++;
    p--;
  }
  return ok;
}

int main(){
  char s1[] = {'g','i','r','a','f','a','r','i','g','\0'};
  char s2[] = {'h','o','l','a','\0'};
  if (es_palindromo(s1))
    printf("Es palindromo\n");
  else
    printf("No es palindromo\n");
  if (es_palindromo(s2))
    printf("Es palindromo\n");
  else
    printf("No es palindromo\n");
  return 0;
}
