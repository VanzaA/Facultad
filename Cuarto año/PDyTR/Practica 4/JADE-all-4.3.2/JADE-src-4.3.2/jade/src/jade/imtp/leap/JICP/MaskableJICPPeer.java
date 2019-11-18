/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/**
 * ***************************************************************
 * The LEAP libraries, when combined with certain JADE platform components,
 * provide a run-time environment for enabling FIPA agents to execute on
 * lightweight devices running Java. LEAP and JADE teams have jointly
 * designed the API for ease of integration and hence to take advantage
 * of these dual developments and extensions so that users only see
 * one development platform and a
 * single homogeneous set of APIs. Enabling deployment to a wide range of
 * devices whilst still having access to the full development
 * environment and functionalities that JADE provides.
 * Copyright (C) 2001 Telecom Italia LAB S.p.A.
 * 
 * GNU Lesser General Public License
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */


package jade.imtp.leap.JICP;

//#MIDP_EXCLUDE_FILE

import jade.core.Profile;
import jade.core.ProfileException;
import jade.core.Specifier;
import jade.util.leap.*;
import jade.imtp.leap.*;
import jade.mtp.TransportAddress;
import java.util.Vector;
import java.util.Enumeration;

/**
 * Class declaration
 * @author Giovanni Caire - TILAB
 */
public class MaskableJICPPeer extends JICPPeer {
  private Vector              masks = new Vector();

  /**
   * Default constructor
   */
  public MaskableJICPPeer() {
    super();
  }

  /**
   * Start listening for internal platform messages on the specified port
   */
  public TransportAddress activate(ICP.Listener l, String peerID, Profile p) throws ICPException {
  	// Initialize masks
  	try {
	  	StringBuffer sb = null;
			int idLength;
			if (peerID != null) {
	  		sb = new StringBuffer(peerID);
				sb.append('-');
				idLength = sb.length();
			}
			else {
				sb = new StringBuffer();
				idLength = 0;
			}
			
			// Unreachable networks
			sb.append(JICPProtocol.UNREACHABLE_KEY);
			List ls = p.getSpecifiers(sb.toString());
			if (ls != null) {
				Iterator it = ls.iterator();
	    	while (it.hasNext()) {
	    		Specifier s = (Specifier) it.next();	
	    		// Note that in this case the className field of a specifier does 
	    		// not have anything to do with a class.
      		updateMask(s.getClassName());
	    	}
    	} 
  	}
  	catch (ProfileException pe) {
  		throw new ICPException("Profile error. "+pe.getMessage());
  	}
			
    return super.activate(l, peerID, p);
  }
  	
  /**
   * deliver a serialized command to a given transport address
   */
  public byte[] deliverCommand(TransportAddress ta, byte[] payload, boolean requireFreshConnection) throws ICPException {
    if (!isMasked(ta.getHost())) {
      return super.deliverCommand(ta, payload, requireFreshConnection);
    } 
    else {
      throw new ICPException("Destination masked");
    } 
  } 

  /**
   * Method declaration
   * 
   * @param m
   * 
   * @throws ICPException
   * 
   * @see
   */
  private void updateMask(String m) throws ICPException {
    int[] mask = parseIP(m);
    masks.addElement(mask);
  } 

  /**
   * Method declaration
   * 
   * @param host
   * 
   * @return
   * 
   * @see
   */
  private boolean isMasked(String host) {
    try {
      int[]       ipAddr = parseIP(host);

      Enumeration e = masks.elements();
      // Loop on mask items
      while (e.hasMoreElements()) {
        int[]   mask = (int[]) e.nextElement();
        // Check whether the host is masked by this mask item
        boolean masked = true;
        for (int i = 0; i < mask.length; ++i) {
          if (ipAddr[i] != mask[i]) {
            masked = false;

            break;
          } 
        } 

        if (masked) {
          return true;
        } 
      } 

      return false;
    } 
    catch (ICPException icpe) {
      // If the host is not in the form a.b.c.d --> it cannot be masked
      return false;
    } 
  } 

  /**
   * Method declaration
   * 
   * @param addr
   * 
   * @return
   * 
   * @throws ICPException
   * 
   * @see
   */
  private int[] parseIP(String addr) throws ICPException {
    int[] abcd = new int[4];
    int   first = 0;
    int   n = 0;
    try {
      boolean stop = false;
      while (n < 3 &&!stop) {
        int last = addr.indexOf('.', first);
        if (last < 0) {
          last = addr.length();
          stop = true;
        } 

        String tmp = addr.substring(first, last);
        abcd[n] = Integer.parseInt(tmp);
        first = last+1;
        n++;
      } 

    } 
    catch (NumberFormatException nfe) {
    } 

    if (n == 0) {
      throw new ICPException("Wrong mask");
    } 

    int[] ipAddr = new int[n];
    for (int i = 0; i < n; ++i) {
      ipAddr[i] = abcd[i];
    } 

    abcd = null;

    return ipAddr;
  } 

}

