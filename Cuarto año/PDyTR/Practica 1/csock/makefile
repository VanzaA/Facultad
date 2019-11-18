BUILD_FOLDER=bin
BUFFER_SIZE=1000

.PHONY: all help folders clean all_buffers
all: folders server-${BUFFER_SIZE} client-${BUFFER_SIZE}

server-${BUFFER_SIZE}:
	gcc -o ${BUILD_FOLDER}/server-${BUFFER_SIZE} server.c -D BUFFER_SIZE=${BUFFER_SIZE} -Wall

client-${BUFFER_SIZE}:
	gcc -o ${BUILD_FOLDER}/client-${BUFFER_SIZE} client.c -D BUFFER_SIZE=${BUFFER_SIZE} -Wall

10000: BUFFER_SIZE=10000
10000: all

100000: BUFFER_SIZE=100000
100000: all

1000000: BUFFER_SIZE=1000000
1000000: all

all_buffers:
	$(MAKE) all
	$(MAKE) 10000
	$(MAKE) 100000
	$(MAKE) 1000000

help:
	@echo "Set variable like this:"
	@echo "\tmake BUFFER_SIZE=n\n"
	@echo "Default value at: $(BUFFER_SIZE)"

folders:
	- mkdir ${BUILD_FOLDER}

clean:
	- rm -r ${BUILD_FOLDER}