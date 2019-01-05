  #include <stdio.h>
  #include <string.h>
  void invertir(char s[]){
    for (int i = strlen(s); i>=0;i--)
      printf("%c",s[i]);
    printf("\n");
  }

  int main(){
    char cadena[256];
    int s,cant = 0 ;
    printf("Ingrese un string: \n");
    while ((cadena[cant] != '\0') && (cant <= 256)){
        cadena[cant] = getchar();
        cant ++;
    }
    scanf("%s",&*s);
    invertir(s);

  }
