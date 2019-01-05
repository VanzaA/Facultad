# 
# El padre espera que terminen sus hijos antes de retirarse
#
import os, time
hijos = 0
print '\n\n\nSoy el PROCESO', os.getpid() , 'y tengo', hijos, 'hijos\n'
while True:
    newpid = os.fork()
    if newpid == 0:
        #
        # Seccion del hijo
        #
    	time.sleep(30)
	print '\t(Hijo', os.getpid(), ') - Se va a jugar a la play'
	exit(0)
    else:
        #
        # Seccion del padre
        #
	hijos = hijos + 1
	print '\t(Padre) Tuve un hijo!!!! Se llama', newpid
    if raw_input( ) == 'q': break

print '\n(Padre) - VAYAN A JUGAR A LA PELOTA!!!!'

while hijos > 0:
    os.wait()
    print '\t(Padre) - Joya, uno menos para cuidar!!!\n'
    hijos = hijos - 1

print '\n(Padre) - Listo, se fueron todos, me voy a dormir'
exit(0)




