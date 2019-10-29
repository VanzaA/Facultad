/*
* AskRemote.java
* a) Looks up for the remote object
* b) "Makes" the RMI
*/
import java.rmi.Naming; /* lookup */
import java.rmi.registry.Registry; /* REGISTRY_PORT */

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.nio.file.StandardOpenOption;
import java.io.IOException;


public class AskRemote
{

    public static void write(IfaceRemoteClass remote, String filename, int start, int buffersize){
        try{

            byte[] fileContent=Files.readAllBytes(Paths.get(filename));
            byte[] partialContent;
            int fileSize=fileContent.length;
            int bytesReaded = start;
            
            while(bytesReaded < fileSize){
                if(buffersize < fileContent.length - bytesReaded){
                    partialContent = Arrays.copyOfRange(fileContent, bytesReaded, bytesReaded + buffersize);
                }else {
                    partialContent = Arrays.copyOfRange(fileContent, bytesReaded, fileContent.length);
                }
                System.out.println(partialContent);

                remote.write(filename, partialContent, partialContent.length);
                bytesReaded += buffersize;
            }
            
            System.out.println("File writed!");
        }catch (Exception e){
            System.out.println(e.toString());
            System.exit(1);
        }
    }

    public static void read(IfaceRemoteClass remote, String filename, int start, int buffersize){
        try{
            int pos = start;
            byte[] data;
            

            data = remote.read(filename, pos, buffersize);
            pos += data.length;
            while(data.length > 0){
                try {
                    Files.write(Paths.get(filename), data, StandardOpenOption.APPEND);
                }
                catch (IOException e) {
                    Files.createFile(Paths.get(filename));
                    Files.write(Paths.get(filename), data, StandardOpenOption.APPEND);
                }

                data = remote.read(filename, pos, buffersize);
                pos += data.length;
            }
            System.out.println("File readed!");
        }catch (Exception e){
            System.out.println(e.toString());
            System.exit(1);
        }
    }

    public static void main(String[] args)
    {
    /* Look for hostname and msg length in the command line */
        try {

            if (args.length < 2)
            {
                System.out.println("1 argument needed: (remote) hostname and operation");
                System.exit(1);
            }

            String rname = "//" + args[0] + ":" + Registry.REGISTRY_PORT + "/remote";
            IfaceRemoteClass remote = (IfaceRemoteClass) Naming.lookup(rname);
            int buffersize = 1024;
            long startTime;
            long stopTime;

            int start = 0;
            if(args.length == 4){
                start = Integer.valueOf(args[3]);
            }

            switch(args[1]){
                case "write":
                    if(args.length < 3){
                        System.out.println("3 arguments needed: Hostname, operation, filename and buffer size (optional)");
                    }
                    write(remote, args[2], start, buffersize);
                    break;
                case "read":
                    if(args.length < 3){
                        System.out.println("3 arguments needed: Hostname, operation and filename");
                    }
                    read(remote, args[2], start, buffersize);
                    break;
                 case "min_time":
                    startTime = System.nanoTime();
                    remote.min_time();
                    stopTime = System.nanoTime();
                    System.out.println(stopTime - startTime);
                    break;
                case "timeout":
                    if(args.length < 3){
                        System.out.println("3 arguments needed: Hostname, operation and time");
                    }
                    System.out.println("timeout command...");
                    startTime = System.nanoTime();                    
                    Boolean ret = remote.timeout(Integer.parseInt(args[2]));
                    stopTime = System.nanoTime();
                    System.out.println(stopTime - startTime);
                    System.out.println(ret);
                    break;    
                default: 
                    System.out.println("Command unavailable");
                    break;
            }

        } catch (Exception e) {
            System.out.println(e.toString());
        }

    }
}