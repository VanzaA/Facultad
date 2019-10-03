#include <stdio.h>
#include <stdlib.h>
#include <stddef.h>
#include <string.h>
#include <inttypes.h>

#include <include/command.h>
#include <include/util.h>
#include <ftp.h>

/* Wrapper function takes care of calling the RPC procedure */
int
ftp_read(CLIENT *clnt, ftp_param_t *param)
{
        char *src = param->src;
        char *dest = param->dest;
        uint64_t bytes = param->bytes;
        uint64_t initial_pos = param->initial_pos;

        FILE* file;
        ftp_file *ftp_file_data;

        ftp_req req = {
                .name = src,
                .pos = initial_pos,
                .bytes = bytes,
        };

        ftp_file_data = read_1(req, clnt);
        if (ftp_file_data == NULL)
        {
                fprintf(stderr, "Trouble calling remote procedure\n");
                exit(0);
        }

        printf("Storing file %s...\n", dest);
        file = fopen(dest, "w");
        if (file == NULL)
        {
                fprintf(stderr, "Error opening file %s\n", src);
                exit(1);
        }
        fwrite(ftp_file_data->data.data_val, sizeof(char), ftp_file_data->data.data_len, file);
        fclose(file);

        return 1;
}

/* Wrapper function takes care of calling the RPC procedure */
int
ftp_write(CLIENT *clnt, ftp_param_t *param)
{
        char *src = param->src;
        char *dest = param->dest;
        uint64_t bytes = param->bytes;
        uint64_t initial_pos = param->initial_pos;

        FILE* file;

        file = fopen(src, "r");
        if (file == NULL)
        {
                fprintf(stderr, "Error opening file %s\n", src);
                exit(1);
        }

        ftp_file ftp_file_data;
        int *result;

        /* Gather everything into a single data structure to send to the server */
        ftp_file_data.data.data_val = (char *) malloc(DATA_SIZE);
        ftp_file_data.data.data_len = fread(ftp_file_data.data.data_val, sizeof(char), bytes, file);
        ftp_file_data.name = (char *) malloc(PATH_MAX);
        ftp_file_data.name = strcpy(ftp_file_data.name, dest);
        ftp_file_data.checksum = hash(ftp_file_data.data.data_val);


        fclose(file);


        /* Call the client stub created by rpcgen */
        result = write_1(ftp_file_data, clnt);
        if (result == NULL)
        {
                fprintf(stderr,"Trouble calling remote procedure\n");
                exit(0);
        }
        else if (*result == -1)
        {
                fprintf(stderr, "Error creating file 'store/%s' in server\n", dest);
        }

        printf("File stored at 'store/%s'\n", dest);

        return(*result);
}

/* Wrapper function takes care of calling the RPC procedure */