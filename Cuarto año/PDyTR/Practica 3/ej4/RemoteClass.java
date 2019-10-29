/*
* RemoteClass.java
* Just implements the RemoteMethod interface as an extension to
* UnicastRemoteObject
*
*/
/* Needed for implementing remote method/s */
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.nio.file.StandardOpenOption;
import java.io.IOException;

/* This class implements the interface with remote methods */
public class RemoteClass extends UnicastRemoteObject implements IfaceRemoteClass
{
    protected RemoteClass() throws RemoteException
    {
        super();
    }
    
    /* Remote method implementation */
    public int write(String filename, byte[] data, int datalength) throws RemoteException {
        try {
            try {
                Files.write(Paths.get("store/" + filename), data, StandardOpenOption.APPEND);
            }
            catch (IOException e) {
                Files.createFile(Paths.get("store/" + filename));
                Files.write(Paths.get("store/" + filename), data, StandardOpenOption.APPEND);
            }
            System.out.println("Writed " + datalength  + " -> " + data);
            return datalength;
        } catch(Exception e) {
            System.out.println(e.toString());
            return -1;
        }
    }

    public byte[] read(String filename, int pos, int buffersize) throws RemoteException
    {
        try{
            byte[] fileContent = Files.readAllBytes(Paths.get("store/"+filename));
            byte[] partialContent;
            System.out.println("Reading " + pos + " <------> " + fileContent.length);
            if(pos >= fileContent.length){
                return new byte[0];
            }

            if(buffersize < fileContent.length - pos){
                partialContent = Arrays.copyOfRange(fileContent, pos, pos + buffersize);
            }else {
                partialContent = Arrays.copyOfRange(fileContent, pos, fileContent.length);
            }
            
            return partialContent;
        } catch(Exception e) {
            System.out.println(e.toString());
            return new byte[0];
        }
    }
}