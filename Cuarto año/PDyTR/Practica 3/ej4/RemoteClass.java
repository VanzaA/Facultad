/*
* RemoteClass.java
* Just implements the RemoteMethod interface as an extension to
* UnicastRemoteObject
*
*/
/* Needed for implementing remote method/s */
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.FileInputStream;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.nio.file.DirectoryStream;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
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
            if(datalength < data.length){
                data = Arrays.copyOf(data, datalength);
            }
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
            File file = new File("store/"+filename);
            FileInputStream fileStream = new FileInputStream(file);
            fileStream.skip(pos);
            byte[] data = new byte[Math.min(buffersize, fileStream.available())]; 
            
            int readed = fileStream.read(data);
            fileStream.close();

            System.out.println("Reading " + pos + " <------> " + data.length);
            
            return data;
        } catch(Exception e) {
            System.out.println(e.toString());
            return new byte[0];
        }
    }

    public boolean timeout(int sleep) throws RemoteException
    {
        try {
            System.out.println("Sleeping " + sleep + " seconds");
            TimeUnit.SECONDS.sleep(sleep);
        } catch(InterruptedException e) {
            return false;
        }
        return true;
    }

    public boolean min_time() throws RemoteException
    {
        return true;
    }
}