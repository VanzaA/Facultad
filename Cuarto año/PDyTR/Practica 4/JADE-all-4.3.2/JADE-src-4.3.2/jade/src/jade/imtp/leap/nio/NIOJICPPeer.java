/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jade.imtp.leap.nio;

//#J2ME_EXCLUDE_FILE

import jade.imtp.leap.JICP.Connection;
import jade.imtp.leap.JICP.ConnectionFactory;
import jade.imtp.leap.JICP.JICPPeer;
import jade.mtp.TransportAddress;
import java.io.IOException;
import java.net.Socket;

/**
/**
 * This class provides a {@link ConnectionFactory} that will construct {@link NIOJICPConnection NIOJICPConnections}.
 * Before the NIOJICPConnections can be used {@link NIOJICPConnection#init(java.nio.channels.SelectionKey)} must be called.
 * @author eduard
 */
public class NIOJICPPeer extends JICPPeer {

    public ConnectionFactory getConnectionFactory() {
        return new ConnectionFactory() {

            public Connection createConnection(Socket s) {
                return new NIOJICPConnection();
            }

            public Connection createConnection(TransportAddress ta) throws IOException {
                return new NIOJICPConnection();
            }
        };
    }



}
