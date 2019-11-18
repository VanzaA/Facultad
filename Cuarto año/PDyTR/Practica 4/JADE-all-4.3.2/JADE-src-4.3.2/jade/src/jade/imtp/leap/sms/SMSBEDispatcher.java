/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/

package jade.imtp.leap.sms;

//#J2ME_EXCLUDE_FILE

import jade.imtp.leap.JICP.*;
import jade.imtp.leap.nio.NIOBEDispatcher;
import jade.imtp.leap.ICPException;
import jade.util.leap.Properties;
import jade.util.Logger;

import java.net.*;
import java.io.*;

/**
   BackEnd side dispatcher class that extends <code>NIOBEDispatcher</code>
   and uses SMS to perform OUT-of-bound notifications to the FrontEnd
   when the connection (currently dropped) must be re-established.
   @see SMSFEDispatcher
   @author Giovanni Caire - TILAB
 */
public class SMSBEDispatcher extends NIOBEDispatcher {

	private SMSManager theSMSManager; 
	private int smsPort;
	private String msisdn;
	private Logger myLogger = Logger.getMyLogger(getClass().getName());
	
  public void init(JICPMediatorManager mgr, String id, Properties props) throws ICPException {
  	// Get the msisdn
  	msisdn = props.getProperty("msisdn");
  	if (msisdn == null) {
  		throw new ICPException("Missing MSISDN");
  	}
  	// Get the singleton SMSManager
		theSMSManager = SMSManager.getInstance(props);
		if (theSMSManager == null) {
			throw new ICPException("Cannot connect to the SMSManager");
		}
		super.init(mgr, id, props);
  }
  
	protected void handleDropDown(Connection c, JICPPacket pkt, InetAddress addr, int port) {
		super.handleDropDown(c, pkt, addr, port);

		// Read the SMS port
		try {
			String portStr = new String(pkt.getData());
			smsPort = Integer.parseInt(portStr);
			myLogger.log(Logger.CONFIG, "OUT-of-bound notification param: msisdn="+msisdn+" port="+smsPort);
		}
		catch (Exception e) {
			myLogger.log(Logger.SEVERE, "Cannot get FE port for OUT-of-bound notifications via SMS!!!!!!! "+e);
			e.printStackTrace();
		}
	}

  /**
     Request the FE to refresh the connection.
   */
  protected void requestRefresh() {
  	if (msisdn.startsWith("39")) {
  		msisdn = "+"+msisdn;
  	}
  	theSMSManager.sendTextMessage(msisdn, smsPort, null);
  }  
}

