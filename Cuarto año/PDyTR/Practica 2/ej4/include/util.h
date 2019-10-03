#ifndef UTILS_H
#define UTILS_H

#include <stddef.h>

uint64_t hash(char *str);
double dwalltime();
void ini_params(int argc, char *argv[], char *host, char *src, char *dest,
                uint64_t *bytes, uint64_t *initial_pos);

#endif
