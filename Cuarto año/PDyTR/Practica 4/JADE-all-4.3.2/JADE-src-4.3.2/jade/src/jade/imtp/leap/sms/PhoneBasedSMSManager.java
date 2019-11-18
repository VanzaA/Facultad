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

import jade.util.Logger;
import jade.util.leap.Properties;
import jade.imtp.leap.ICPException;
import jade.imtp.leap.JICP.JICPConnection;
import jade.imtp.leap.JICP.Connection;
import jade.imtp.leap.JICP.JICPPacket;
import jade.imtp.leap.JICP.JICPProtocol;

import java.net.*;
import java.io.*;

/**
   @author Giovanni Caire - TILAB
 */
public class PhoneBasedSMSManager extends SMSManager {
	private static final String LOCAL_PORT = "jade_imtp_leap_sms_PhoneBasedSMSManager_local-port";
	
	private static final int DEFAULT_LOCAL_PORT = 1100;
	
	private static final int IDLE = 0;
	private static final int CONNECTED = 1;
	private static final int TERMINATED = 2;
	
	private int status = IDLE;
	
	private Connection myConnection;
	
	private Logger myLogger = Logger.getMyLogger(getClass().getName());
	
	protected void init(Properties pp) throws ICPException {
		super.init(pp);
		
		int localPort = DEFAULT_LOCAL_PORT;
		try {
			localPort = Integer.parseInt(myProperties.getProperty(LOCAL_PORT));
		}
		catch (Throwable t) {
			// Ignore and keep default
		}
		
		initConnection(localPort);
	}
	
	private void initConnection(final int localPort) throws ICPException {
		// Open a ServerSocket and start a Thread that waits for the 
		// helper phone to connect
    try {
      final ServerSocket server = new ServerSocket(localPort);
      Thread t = new Thread() {
      	public void run() {
		      try {
		      	// Accept connection
		        myLogger.log(Logger.INFO, "PB-SMSManager waiting for the helper phone to connect on port "+localPort+" ...");
		        Socket s = server.accept();
		        myLogger.log(Logger.INFO, "PB-SMSManager: Helper phone connected");
		        try {
			        server.close();
		        }
		        catch (IOException ioe) {
		        	// Just ignore it
		        }
		        notifyConnected(new JICPConnection(s));
		      } 
		      catch (Exception e) {
	          myLogger.log(Logger.SEVERE, "PB-SMSManager: Problems accepting connection from the helper phone.");
	          e.printStackTrace();
	          shutDown();
		      }
      	}
      };
      t.start();
    } 
    catch (IOException ioe) {
      throw new ICPException("I/O error opening server socket on port "+localPort);
    } 
	}
	
	private void notifyConnected(Connection c) {
		myConnection = c;
		status = CONNECTED;
	}
	
  /**
     Shut down this JICP server
   */
  public void shutDown() {
	  myLogger.log(Logger.FINE, "PB-SMSManager shutting down...");
    status = TERMINATED;
    if (myConnection != null) {
	    try {
	    	myConnection.close();
	    }
	    catch (IOException ioe) {
	    	// Just ignore it
	    }
    }
  } 
	
	protected void send(String msisdn, int port, byte type, byte[] data) {
		// We use the info byte to transport the text/bynary indication
		// and the recipientID to transport msisdn:port
		String recipientID = msisdn;
		if (port != UNDEFINED) {
			recipientID = recipientID + ":" +port;
		}
		
		if (status == CONNECTED) {
			myLogger.log(Logger.FINEST, "PB-SMSManager sending SMS request to the helper phone. Recipient: "+recipientID);
			JICPPacket pkt = new JICPPacket(JICPProtocol.COMMAND_TYPE, type, recipientID, data);
			try {
				myConnection.writePacket(pkt);
				myLogger.log(Logger.FINEST, "PB-SMSManager: request correctly sent.");
				// We are not expecting any response
			}
			catch (IOException ioe) {
				myLogger.log(Logger.WARNING, "PB-SMSManager: Error sending command to the helper phone. "+ioe);
			}
		}
		else {
			myLogger.log(Logger.WARNING, "PB-SMSManager: Helper phone not connected. Can't send SMS to "+recipientID);
		}
	}	
}