#!/usr/bin/python
# 
# SHELL
#
import os, time
print '''
------------------------------------------------------
----------- Esta es la ISO DIR/LS SHELL --------------
------------------------------------------------------
# exit (para salir)
'''

cmd = raw_input("iso:\> ")

while cmd <> 'exit':

    if cmd == '':
	cmd = raw_input("iso:\> ")
    else:

        newpid = os.fork()

        if newpid == 0:
            # Seccion del hijo
	    lista = cmd.split(' ')

            os.execvp(lista[0], lista)

	    print "Imprimir AAAAAAAAAA"
            exit(0)
	    print "Imprimir BBBBBBBBBB"

        else:
            # Seccion del padre
            os.wait()
	    cmd = raw_input("iso:\> ")

exit(0)




