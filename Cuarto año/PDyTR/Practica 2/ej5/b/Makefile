.c.o:
	gcc -c -Wall -DRPC_SVC_FG $<

all: simp client server


simp: simp.o
	gcc -o simp simp.o 

client: simpclient.o simp_clnt.o simp_xdr.o
	gcc -o client simpclient.o simp_clnt.o simp_xdr.o -lnsl


server: simpservice.o simp_svc.o simp_xdr.o
	gcc -o server simpservice.o simp_svc.o simp_xdr.o -lrpcsvc -lnsl

simp.h: simp.x
	rpcgen -C simp.x

clean: 
	rm *.o simp.h simp_svc.* simp_clnt.* simp_xdr.*

simpclient.c: simp.h
simpservice.c: simp.h
