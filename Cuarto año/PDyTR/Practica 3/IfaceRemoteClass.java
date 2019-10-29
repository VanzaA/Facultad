import java.rmi.Remote;
import java.rmi.RemoteException;
public interface IfaceRemoteClass extends Remote{
    public byte[] sendThisBack(byte[] data) throws RemoteException;
}