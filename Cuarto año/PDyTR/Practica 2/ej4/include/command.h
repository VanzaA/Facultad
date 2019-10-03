#ifndef COMMANDS_H
#define COMMANDS_H
#include <ftp.h>  /* Created for us by rpcgen - has everything we need ! */

#include <stddef.h>

typedef struct ftp_param
{
        char *src;
        char *dest;
        uint64_t bytes;
        uint64_t initial_pos;
} ftp_param_t;

typedef int (*command_fn_t)(CLIENT *, ftp_param_t *);

typedef struct command
{
        char name[15];
        char description[100];
        command_fn_t handle;
} command_t;

int ftp_read(CLIENT *clnt, ftp_param_t *param);
int ftp_write(CLIENT *clnt, ftp_param_t *param);

#endif