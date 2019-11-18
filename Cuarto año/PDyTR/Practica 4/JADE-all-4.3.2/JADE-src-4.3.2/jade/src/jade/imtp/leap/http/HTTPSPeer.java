package jade.imtp.leap.http;

//#J2ME_EXCLUDE_FILE

import jade.imtp.leap.JICP.Connection;
import jade.imtp.leap.JICP.ConnectionFactory;
import jade.imtp.leap.JICP.JICPSPeer;
import jade.imtp.leap.TransportProtocol;
import jade.mtp.TransportAddress;
import java.io.IOException;
import java.net.Socket;

/**
 *
 * @author Eduard Drenth: Logica, 1-okt-2009
 * 
 */
public class HTTPSPeer extends JICPSPeer {

    @Override
    public ConnectionFactory getConnectionFactory() {
        return new ConnectionFactory() {

            public Connection createConnection(Socket s) {
                return new HTTPServerConnection(s);
            }

            public Connection createConnection(TransportAddress ta) throws IOException {
                return new HTTPSClientConnection(ta);
            }
        };
    }

    @Override
    public TransportProtocol getProtocol() {
        return HTTPSProtocol.getInstance();
    }

    
}
