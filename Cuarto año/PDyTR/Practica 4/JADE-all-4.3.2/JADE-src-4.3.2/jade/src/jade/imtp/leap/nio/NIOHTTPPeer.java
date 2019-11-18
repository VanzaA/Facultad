/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jade.imtp.leap.nio;

//#J2ME_EXCLUDE_FILE

import jade.imtp.leap.JICP.Connection;
import jade.imtp.leap.JICP.ConnectionFactory;
import jade.imtp.leap.TransportProtocol;
import jade.imtp.leap.http.HTTPProtocol;
import jade.mtp.TransportAddress;
import java.io.IOException;
import java.net.Socket;

/**
 *
 * @author eduard
 */
public class NIOHTTPPeer extends NIOJICPPeer {

    public ConnectionFactory getConnectionFactory() {
        return new ConnectionFactory() {

            public Connection createConnection(Socket s) {
                return new NIOHTTPConnection();
            }

            public Connection createConnection(TransportAddress ta) throws IOException {
                return new NIOHTTPConnection();
            }
        };
    }

    public TransportProtocol getProtocol() {
        return HTTPProtocol.getInstance();
    }

}
