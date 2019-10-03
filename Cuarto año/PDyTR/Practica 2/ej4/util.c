#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <stddef.h>
#include <inttypes.h>
#include <unistd.h>
#include <getopt.h>
#include <sys/time.h>
#include <sys/types.h>

uint64_t
hash(char *str)
{
        uint64_t hash = 5381;
        int c;

        while ((c = *str++))
                hash = ((hash << 5) + hash) + c; /* hash * 33 + c */

        return hash;
}


double
dwalltime(void)
{
        double sec;
        struct timeval tv;

        gettimeofday(&tv, NULL);
        sec = tv.tv_sec + tv.tv_usec/1000000.0;
        return sec;
}


void
ini_params(int argc, char *argv[], char *host, char *src, char *dest,
           uint64_t *bytes, uint64_t *initial_pos)
{
        static int verbose_flag;
        static int all_flag = 0;
        int c;

        while (1)
        {
                static struct option long_options[] =
                {
                        {"all", no_argument, &all_flag, 1},
                        {"bytes", required_argument, 0, 'a'},
                        {"pos", required_argument, 0, 'p'},
                        {"host", required_argument, 0, 'h'},
                        {"src", required_argument, 0, 's'},
                        {"dest", required_argument, 0, 'd'},
                        {0, 0, 0, 0}
                };
                /* getopt_long stores the option index here. */
                int option_index = 0;

                c = getopt_long(argc, argv, "s:d:h:a:p:",
                                long_options, &option_index);

                /* Detect the end of the options. */
                if (c == -1)
                        break;

                switch (c)
                {
                case 0:
                        /* If this option set a flag, do nothing else now. */
                        if (long_options[option_index].flag != 0)
                                break;
                        printf("option %s", long_options[option_index].name);
                        if (optarg)
                                printf(" with arg %s", optarg);
                        printf("\n");
                        break;
                case 's':
                        strcpy(src, optarg);
                        break;
                case 'd':
                        strcpy(dest, optarg);
                        break;
                case 'h':
                        strcpy(host, optarg);
                        break;
                case 'a':
                        *bytes = atoi(optarg);
                        break;
                case 'p':
                        *initial_pos = atoi(optarg);
                        break;
                case '?':
                        /* getopt_long already printed an error message. */
                        break;
                default:
                        exit(0);
                }
        }

}