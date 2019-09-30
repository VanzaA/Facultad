
# This is a template makefile generated		by rpcgen 

CC=gcc
# Parameters 

CLIENT = vadd_client
SERVER = vadd_service

SOURCES_CLNT.c = 
SOURCES_CLNT.h = 
SOURCES_SVC.c = 
SOURCES_SVC.h = 
SOURCES.x = vadd.x

TARGETS_SVC.c = vadd_svc.c vadd_service.c vadd_xdr.c 
TARGETS_CLNT.c = vadd_clnt.c vadd_client.c vadd_xdr.c 
TARGETS = vadd.h vadd_xdr.c vadd_clnt.c vadd_svc.c vadd_client.c vadd_service.c

OBJECTS_CLNT = $(SOURCES_CLNT.c:%.c=%.o) $(TARGETS_CLNT.c:%.c=%.o) 
OBJECTS_SVC = $(SOURCES_SVC.c:%.c=%.o) $(TARGETS_SVC.c:%.c=%.o) 
# Compiler flags 

CFLAGS += -Wall -DRPC_SVC_FG
LDLIBS += -lnsl
RPCGENFLAGS = -C

# Targets 

all : $(CLIENT) $(SERVER)

$(TARGETS) : $(SOURCES.x) 
	rpcgen $(RPCGENFLAGS) $(SOURCES.x)

$(OBJECTS_CLNT) : $(SOURCES_CLNT.c) $(SOURCES_CLNT.h) $(TARGETS_CLNT.c) 

$(OBJECTS_SVC) : $(SOURCES_SVC.c) $(SOURCES_SVC.h) $(TARGETS_SVC.c) 

$(CLIENT) : $(OBJECTS_CLNT) 
	$(LINK.c) -o $(CLIENT) $(OBJECTS_CLNT) $(LDLIBS) 

$(SERVER) : $(OBJECTS_SVC) 
	$(LINK.c) -o $(SERVER) $(OBJECTS_SVC) $(LDLIBS)

 clean:
	 $(RM) core $(TARGETS) $(OBJECTS_CLNT) $(OBJECTS_SVC) $(CLIENT) $(SERVER)

