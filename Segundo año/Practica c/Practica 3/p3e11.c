#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int es_palindromo(const char *word){
  int ok = 1,i=0;
  unsigned p = strlen(word)-1;
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

int cuenta_palabras(const char *s){
  char *aux;
  int i = 0,j, cant = 0;
  while ((i < strlen(s))&&(s[i]!= '\0')){
    int d = 0;
    j = 1;
    aux=(char *)malloc(sizeof(char));
    while ((s[i] == ' ')&&(i < strlen(s))&&(s[i]!= '\0'))
      i++;
    while ((s[i] != ' ')&&(i < strlen(s))&&(s[i]!= '\0')) {
      aux[d] = s[i];
      aux=(char *)realloc(aux,(sizeof(char)*j));
      d++;
      i++;
      j++;
    }
    if(s[i]!='\0'){
      aux[d+1] = '\0';
      printf("aux = %s\n",aux);
      if (es_palindromo(aux))
        cant ++;
    }
    free(aux);
  }
  return cant;
}

int main(){
  char s[]={' ',' ','h','a','b','i','a',' ',' ',' ','u','n','a',' ','v','e','z',' ','u','n',' ','h','e','r','m','o','s','o',' ','g','i','r','a','f','a','r','i','g',' ','q','u','e',' ','v','i','v','i','a',' ','e','n',' ','n','e','u','q','u','e','n',' ','d','o','n','d','e',' ','g','o','b','e','r','n','a','b','a',' ','m','e','n','e','m',' ',' ','\0'};
  printf("La cantidad de palabras capicua en el string dado es %d\n",cuenta_palabras(s));
  return 0;

}
