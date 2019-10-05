/* Definition of the remote add and subtract procedure used by 
   simple RPC example 
   rpcgen will create a template for you that contains much of the code
   needed in this file is you give it the "-Ss" command line arg.
*/
#include <stddef.h>
#include <stdio.h>
#include <errno.h>
#include <string.h>
#include <inttypes.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <dirent.h>

#include <include/util.h>
#include <ftp.h>

/* Here is the actual remote procedure */
/* The return value of this procedure must be a pointer to int! */
/* we declare the variable result as static so we can return a 
   pointer to it */

int *
write_1_svc(ftp_file arg, struct svc_req *rqstp)
{
        char *name = arg.name;
        char *buffer = (char *) malloc(1024);
        strcpy(buffer, arg.data.data_val);
        int length = arg.data.data_len;

        printf("name: %s - buffer length: %d\n", name, length);

        // Declare variables
	FILE *file;
        DIR *dir;
        static int result;
        char path[PATH_MAX];

        // Set path
        snprintf(path, PATH_MAX, "%s/%s", "store", name);
        
        dir = opendir("store");
        if (dir)
        {
                closedir(dir);
        }
        else if (ENOENT == errno)
        {
                mkdir("store", 0777);
        }
        else
        {
                fprintf(stderr, "Error creating file '%s'\n", path);
                result = -1;
                return &result;
        }

        // Open file and check errors
        file = fopen(path, "a");
        if (file == NULL)
        {
                fprintf(stderr, "Error creating file '%s'\n", path);
                result = -1;
                return &result;
        }
    
        // Write file
        result = fwrite(arg.data.data_val, sizeof(char), length, file);

        fclose(file);
        printf("Storing %s...\n", path);

        return ((int*) &result);
}

ftp_file *
read_1_svc(ftp_req arg, struct svc_req *rqstp)
{
        char *name = arg.name;
	uint64_t pos = arg.pos;
	uint64_t bytes = arg.bytes;

        FILE *file;
        ftp_file *file_struct;

        file_struct = (ftp_file *) malloc(sizeof(char *) + sizeof(unsigned int) + sizeof(char *) + sizeof(uint64_t)+ sizeof(int));
        if(bytes > 0 && bytes <= 1024) {
                file_struct->data.data_val = (char *) malloc(bytes);
        } else {
                file_struct->data.data_val = (char *) malloc(sizeof(char) * 1024);
                bytes = 1024;
        }

        file = fopen(name, "r");
        if (file == NULL)
        {
                fprintf(stderr, "Error opening file %s\n", name);
                file_struct->data.data_len = -1;
                return file_struct;
        }
        fseek(file, pos, SEEK_SET);
        
        file_struct->data.data_len = fread(file_struct->data.data_val, sizeof(char), bytes, file);
        file_struct->name = (char *) malloc(PATH_MAX);
        file_struct->name = strcpy(file_struct->name, name);
        file_struct->reading = (feof(file)) ? 0 : 1;
        printf("Reading amount %d\n", file_struct->data.data_len);

        return file_struct;
}