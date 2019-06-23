CC := gcc
STD := -std=gnu99
SRC_DIR := src
TEMPLATE_DIR := templates
BIN_DIR := bin
MACROS := -D DEBUG
OPENPM_SOURCES := ej1 ej1-sec
PTHREAD_SOURCES := ej2 ej2-sec
PTHREAD_FLAGS := -pthread
OPENMP_FLAGS := -fopenmp

all: folders
	@echo "Compiling '$(PTHREAD_SOURCES) $(OPENPM_SOURCES)'\n"
	@for src in $(PTHREAD_SOURCES); do \
		echo "$(CC) -Wall $(PTHREAD_FLAGS) $(STD) -o $(BIN_DIR)/$$src $(SRC_DIR)/$$src.c" ;\
		$(CC) -Wall $(PTHREAD_FLAGS) $(STD) -o $(BIN_DIR)/$$src $(SRC_DIR)/$$src.c ;\
	done
	@for src in $(OPENPM_SOURCES); do \
		echo "$(CC) -Wall $(OPENMP_FLAGS) $(STD) -o $(BIN_DIR)/$$src $(SRC_DIR)/$$src.c" ;\
		$(CC) -Wall $(OPENMP_FLAGS) $(STD) -o $(BIN_DIR)/$$src $(SRC_DIR)/$$src.c ;\
	done

debug: folders
	@echo "Compiling in debug mode '$(PTHREAD_SOURCES) $(OPENPM_SOURCES)'\n"
	@for src in $(PTHREAD_SOURCES); do \
		echo "$(CC) -Wall $(PTHREAD_FLAGS) $(STD) $(MACROS) -o $(BIN_DIR)/$$src-debug $(SRC_DIR)/$$src.c" ;\
		$(CC) -Wall $(PTHREAD_FLAGS) $(STD) $(MACROS) -o $(BIN_DIR)/$$src-debug $(SRC_DIR)/$$src.c ;\
	done
	@for src in $(OPENPM_SOURCES); do \
		echo "$(CC) -Wall $(OPENMP_FLAGS) $(STD) $(MACROS) -o $(BIN_DIR)/$$src-debug $(SRC_DIR)/$$src.c" ;\
		$(CC) -Wall $(OPENMP_FLAGS) $(STD) $(MACROS) -o $(BIN_DIR)/$$src-debug $(SRC_DIR)/$$src.c ;\
	done

folders:
	@-mkdir bin

clean:
	-rm -r bin
