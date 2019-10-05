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
        int read_all = 0;

        FILE* file;
        ftp_file *ftp_file_data;
        ftp_file_data = (ftp_file *) malloc(sizeof(char *) + sizeof(unsigned int) + sizeof(char *) + sizeof(uint64_t) + sizeof(int));
        
        ftp_file_data->reading = 1;

        ftp_req req = {
                .name = src,
                .pos = initial_pos,
                .bytes = bytes,
        };

        if(!bytes){
                read_all = 1;
        }
        fclose(fopen(dest, "w")); //erasing file data cabeza mode
        file = fopen(dest, "a");
        if (file == NULL)
        {
                fprintf(stderr, "Error opening file %s\n", src);
                exit(1);
        }

        while(ftp_file_data->reading && (bytes > 0 || read_all)) {

                ftp_file_data = read_1(req, clnt);
                if (ftp_file_data == NULL)
                {
                        fprintf(stderr, "Trouble calling remote procedure\n");
                        exit(0);
                }

                printf("Storing file %s...\n", dest);
                fwrite(ftp_file_data->data.data_val, sizeof(char), ftp_file_data->data.data_len, file);
                req.pos = req.pos + ftp_file_data->data.data_len;
                
                if(!read_all) {
                        req.bytes = req.bytes - ftp_file_data->data.data_len;
                        bytes = req.bytes;
                }
        }
        printf("End storing\n");

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

        FILE* file;

        file = fopen(src, "r");
        if (file == NULL)
        {
                fprintf(stderr, "Error opening file %s\n", src);
                exit(1);
        }

        int read_all = 0;

        if(!bytes){
                read_all = 1;
        }

        ftp_file ftp_file_data;
        int *result;
        int bytes_to_read = 1024;
        ftp_file_data.data.data_val = (char *) malloc(DATA_SIZE);
        ftp_file_data.name = (char *) malloc(PATH_MAX);
        ftp_file_data.name = strcpy(ftp_file_data.name, dest);
        
        while(!feof(file) && (read_all || bytes > 0)){
                if(bytes < 1024 && !read_all) {
                        bytes_to_read = bytes;
                }
                ftp_file_data.data.data_len = fread(ftp_file_data.data.data_val, sizeof(char), bytes_to_read, file);
                bytes -= ftp_file_data.data.data_len;
                if(!ftp_file_data.data.data_len){
                        break;
                }
                result = write_1(ftp_file_data, clnt);
                printf("sending data\n");
        }


        fclose(file);


        /* Call the client stub created by rpcgen */
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