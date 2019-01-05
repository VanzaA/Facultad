#include <stdio.h>
#include <stdlib.h>

int max(int, int);
int min(int, int);
int * suma(int *, int *, int, int);

int main(int argc, char const *argv[])
{
  int v1[] = {1, 2, 3, 4, 5};
  int v2[] = {6, 7, 8, 9};
  int * v3 = suma(v1, v2, sizeof(v1)/sizeof(int), sizeof(v2)/sizeof(int));
  int i;
  for (i = 0; i < max(sizeof(v1)/sizeof(int), sizeof(v2)/sizeof(int)); i++) {
    printf("%d\n", v3[i]);
  }
  return 0;
}

int max(int a, int b)
{
  return a > b ? a : b;
}

int min(int a, int b)
{
  return a < b ? a : b;
}

int * suma(int * v1, int * v2, int dim1, int dim2)
{
    int max_dim = max(dim1, dim2),
        min_dim = min(dim1, dim2),
        i;
    int * result = l(int *) malloc(max_dim*sizeof(int));

    for (i = 0; i < max_dim; i++) {
      if (i < min_dim) {
        result[i] = v1[i] + v2[i];
      }
      else {
        result[i] = max_dim = dim1 ? v1[i] : v2[i];
      }
    }
    return result;
}
