
## Solución

La solucion planteada consiste en partir de una permutacion de indices inicial, para luego aumentar el ultima hasta llegar a su limite, luego se aumentan los  anteriores conforme sea necesaria, mientras se controla cuales de las permutaciones generadas suman el numero V deseado, 

Al principio se planteo repartir de manera equitativa la cantidad de indices, pero al hacer esto, habia un desbalance de carga muy grande dado que al proceso que le tocaba los ultimos indices, trabaja muy poco y al que le tocaba los primeros, trabajaba todo el tiempo(dado que la cantidad de permutaciones van decreciendo a medida que aumentan los indices),

Luego se implemento la siguiente solucion: un proceso master que administra una cola de indices, la cual suministra indices bajo demanda para los procesos slave(a medida que terminan con un indice solicitan otro),
Esta solucion a pesar de ser mejor que la anterior planteada, tiene un problema, se pierde bastante tiempo en el pasaje de mensajes, por eso es que termina dando tiempos practicamente iguales para cada proceso, y a su vez, si se trabaja con un 'n' mayor, aumentaria el desbalanceo



## Tiempos

|   N    |  n  | Tiempo Secuencial |  Tiempo 4 Hilos (2 por máquinas)  | Tiempo 8 Hilos (4 por máquinas)   |
|--------|-----|-------------------|------------------|------------------|
|  2048  |  3  |     14,024        |     4,59568      |     2,07895      |
|  120   |  6  |     63,005        |     20,8231      |     9,4715       |
|   86   |  7  |     107,89        |     36,7878      |     16,2037      |

### Speedup

| N      |  n  | S<sub>p</sub> 4 hilos (2 por máquinas)| S<sub>p</sub> 8 Hilos (4 por máquinas)|
|--------|-----|-------------------|-------------------------|
|  2048  |  3  | 3,05156146642     |      6,74571298011      |
|  120   |  6  | 3,02572623673     |      6,6520614475       |
|  86    |  7  | 2,93276575386     |      6,65835580762      |

### Eficiencia

| N      |  n  | E<sub>p</sub> 4 hilos (2 por máquinas)| E<sub>p</sub> 8 Hilos (4 por máquinas)|
|--------|-----|-------------------------|-------------------------|
|  2048  |  3  | 0,7628903666      |      0,843214122514     |
|  120   |  6  | 0,756431559183    |      0,831507680938     |
|  86    |  7  | 0,733191438465    |      0,832294475952     |


### Balance de carga

| N      |  n  |  4 hilos (2 por máquinas)| 8 Hilos (4 por máquinas)|
|--------|-----|--------------------------|-------------------------|
|  2048  |  3  | 0,00001305582            |      0,00019266762     |
|  120   |  6  | 0,0000432204             |      0.00106774903     |
|  86    |  7  | 0,00000543662            |      0.00001234293     |


