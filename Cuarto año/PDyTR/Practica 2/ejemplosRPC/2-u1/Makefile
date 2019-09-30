.c.o:
	gcc -c -Wall $<

all: client server

COBJ = userlookup_clnt.o userlookup_xdr.o userlookup_client.o

SOBJ = userlookup_svc.o userlookup_xdr.o userlookup_services.o

client: $(COBJ)
	gcc -o client $(COBJ) -lnsl


server: $(SOBJ)
	gcc -o server $(SOBJ) -lrpcsvc -lnsl


userlookup.h: userlookup.x
	rpcgen -C userlookup.x

userlookup_clnt.c: userlookup.x
	rpcgen -C userlookup.x

userlookup_svc.c: userlookup.x
	rpcgen -C userlookup.x

userlookup_xdr.c: userlookup.x
	rpcgen -C userlookup.x

clean: 
	rm *.o userlookup.h userlookup_clnt.c userlookup_svc.c userlookup_xdr.* *~

