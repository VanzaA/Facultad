/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jade.imtp.leap.JICP;

//#J2ME_EXCLUDE_FILE

import jade.mtp.TransportAddress;
import java.io.IOException;

/**
 * class for setting up a secure BIFEDispatcher
 * @author eduard
 */
public class BIFESDispatcher extends BIFEDispatcher {

    /**
     *
     * @param ta
     * @return a {@link JICPSConnection}
     * @throws IOException
     */
    protected JICPConnection getConnection(TransportAddress ta) throws IOException {
        return new JICPSConnection(ta);
    }


}
