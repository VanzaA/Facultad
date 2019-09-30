.c.o:
	gcc -c -DRPC_SVC_FG -Wall $<

all: client server


client: llclient.o ll_clnt.o ll_xdr.o
	gcc -o client llclient.o ll_clnt.o ll_xdr.o -lnsl


server: llservice.o ll_svc.o ll_xdr.o
	gcc -o server llservice.o ll_svc.o ll_xdr.o -lrpcsvc -lnsl

ll.h: ll.x
	rpcgen -C ll.x

clean: 
	rm *.o ll.h ll_svc.* ll_clnt.* ll_xdr.*

llclient.c: ll.h
llservice.c: ll.h





