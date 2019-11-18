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

/**
   @author Giovanni Caire - TILAB
 */
public abstract class SMSManager {
	public static final byte BINARY = 0;
	public static final byte TEXT = 1;
	
	public static final int UNDEFINED = -1;
	
	public static final String IMPLEMENTATION = "jade_imtp_leap_sms_SMSManager_implementation";
	
	// The logger
	private static Logger myLogger = Logger.getMyLogger(SMSManager.class.getName());
	
	// The singleton instance of the SMSManager
	private static SMSManager theInstance; 
	
	// The configuration properties for this SMSManager
	protected Properties myProperties;
	
	public static SMSManager getInstance(Properties pp) {
		if (theInstance == null) {			
			// Initialize the singleton instance
			Object tmp = pp.get(IMPLEMENTATION);
			if (tmp == null) {
				tmp = "jade.imtp.leap.sms.PhoneBasedSMSManager";
			}
			if (tmp instanceof SMSManager) {
				theInstance = (SMSManager) tmp;
			}
			else {
				// Try as a string specifying the class to load
				try {
					myLogger.log(Logger.FINE, "Creating the SMSManager singleton instance: class is "+tmp);
					theInstance = (SMSManager) Class.forName((String) tmp).newInstance();
					theInstance.init(pp);
				}
				catch (Throwable t) {
					myLogger.log(Logger.SEVERE, "Error creating the SMSManager singleton instance. "+t);
					return null;
				}
			}
			myLogger.log(Logger.INFO, "SMSManager singleton instance correctly initialized");
		}
		return theInstance;
	}
	
	protected void init(Properties pp) throws ICPException {
		myProperties = pp;
	}
	
	public synchronized void sendTextMessage(String msisdn, int port, String text) {
		byte[] data = (text != null ? text.getBytes() : null);
		send(msisdn, port, TEXT, data);
	}
	
	public synchronized void sendBinaryMessage(String msisdn, int port, byte[] data) {
		send(msisdn, port, BINARY, data);
	}
	
	/**
	   Send an SMS of a given type (BINARY or TEXT) to a given msisdn on
	   a given port.
	   Note that SMS are not reliable --> Being sure that the SMS has
	   been sent does not guarantee that the destination msisdn receives
	   it --> This is why this method does not throw any exception
	 */
	protected abstract void send(String msisdn, int port, byte type, byte[] data);	
}