#include <stdio.h>
int main(){

int res, dia, mes, anio;
do {
 res = scanf(" %2d/ %2d/ %4d", &dia, &mes, &anio);
 printf("scanf retorno %d\n", res);
 if ((res != EOF)&&(dia > 0 && dia <= 31)&&(mes > 0 && mes <= 12)&&(anio > 1000 && anio <= 9999)) {
  if (res != 3) {
   printf("ERROR: El formato debe ser dd/mm/yyyy\n");
  } else {
   printf("Fecha: %d/ %d/ %d\n", dia, mes, anio);
   }
 }
} while ((res != EOF)&&(res != 0));
return 0;
}
