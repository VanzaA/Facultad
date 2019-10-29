import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
public class RemoteClass extends UnicastRemoteObject implements IfaceRemoteClass{
    protected RemoteClass() throws RemoteException{
        super();
    }
    public byte[] sendThisBack(byte[] data) throws RemoteException{
        System.out.println("Data back to client");
        return data;
    }
}