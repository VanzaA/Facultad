/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jade.imtp.leap.nio;

//#J2ME_EXCLUDE_FILE

import jade.imtp.leap.ICPException;
import java.nio.channels.SocketChannel;
import java.util.logging.Logger;

/**
 *
 * @author eduard
 */
public class NIOHTTPConnection extends NIOJICPConnection {

    private NIOHTTPHelper helper;

    @Override
    void init(SocketChannel channel) throws ICPException {
        super.init(channel);
        helper = new NIOHTTPHelper(this);
        addBufferTransformer(helper);
    }
  
    private static Logger log = Logger.getLogger(NIOHTTPConnection.class.getName());

}
