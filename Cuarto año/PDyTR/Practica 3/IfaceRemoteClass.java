/*
* IfaceRemoteClass.java
* Interface defining only one method which can be invoked remotely
*
*/
/* Needed for defining remote method/s */
import java.rmi.Remote;
import java.rmi.RemoteException;
/* This interface will need an implementing class */
public interface IfaceRemoteClass extends Remote
{
/* It will be possible to invoke this method from an application in other JVM */
public byte[] sendThisBack(byte[] data) throws RemoteException;
}