# 
# El padre puede terminar antes que los hijos
#

import os, time
hijos = 0
print '\n\n\nSoy el PROCESO PADRE. PID: ', os.getpid() , 'y tengo', hijos, 'hijos\n'
print '\n\n\tQuiero tener un hijo? (sn)'
respuesta = raw_input()
while (respuesta <> 'n'):
    newpid = os.fork()
    if newpid == 0:
        # Seccion del hijo
        #
        time.sleep(10)
        print '\t\t\t\t', os.getpid(),' - Me aburro. Me voy a jugar a la PLAY'
        exit(0)
    else:
        # seccion del padre
        #   
        hijos = hijos + 1
        print '\t\tTuve un hijo!!!! Tiene el PID: ', newpid
	print '\n\tQuiero tener otro hijo? (s|n)'
	respuesta = raw_input()
print '\nBueno, hasta aca llegue. Me voy a dormir. Ya con', hijos ,'hijos es suficiente'
exit(0)



