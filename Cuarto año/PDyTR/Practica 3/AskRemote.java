import java.rmi.Naming;
import java.rmi.registry.Registry;
public class AskRemote{
    public static void main(String[] args){
        if (args.length != 1){
            System.out.println("1 argument needed: (remote) hostname");
            System.exit(1);
        }
        try {
            String rname = "//" + args[0] + ":" + Registry.REGISTRY_PORT + "/remote";
            IfaceRemoteClass remote = (IfaceRemoteClass) Naming.lookup(rname);
            int bufferlength = 100;
            byte[] buffer = new byte[bufferlength];
            remote.sendThisBack(buffer);
            System.out.println("Done");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}