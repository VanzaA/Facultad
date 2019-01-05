int retorna_car (int x){
 if (x>0 && x<=255){
  char c = x;
  return c;
 }
 else return 1;
}

int main (){
 int num;
 char c;
 printf("Ingrese un numero: "\n);
 scanf(%d,&num);
 c = retorna_car(num);
 printf("Usted ingresó %c\n",c)
 return 0;

}
