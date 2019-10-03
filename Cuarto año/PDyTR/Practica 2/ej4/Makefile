.c.o:
	gcc -c -Wall -I./ -DRPC_SVC_FG $<

all: ftp.h client server

client: ftpclient.o ftp_clnt.o ftp_xdr.o util.o command.o
	gcc -o client ftpclient.o ftp_clnt.o ftp_xdr.o util.o command.o -lnsl -I./ -Wall


server: ftpservice.o ftp_svc.o ftp_xdr.o util.o
	gcc -o server ftpservice.o ftp_svc.o ftp_xdr.o util.o -lrpcsvc -lnsl -I./ -Wall

ftp.h: ftp.x
	rpcgen -N ftp.x

clean: 
	rm *.o ftp.h ftp_svc.* ftp_clnt.* ftp_xdr.* client server

ftpclient.c: ftp.h
ftpservice.c: ftp.h
util.c: include/util.h
command.c: include/command.h