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
 * Copyright (C) 2001 Motorola.
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

import jade.mtp.TransportAddress;
import jade.core.MicroRuntime;
import jade.imtp.leap.TransportProtocol;
import jade.imtp.leap.ICPException;

import java.util.Vector;

/**
 * Class declaration
 * @author Giovanni Caire - TILAB
 * @author Ronnie Taib - Motorola
 * @author Steffen Rusitschka - Siemens
 */
public class JICPProtocol extends TransportProtocol {

	/**
	 * The protocol's name, as used in a URL protocol field.
	 */
	public static final String NAME = "jicp";
	public static final int    DEFAULT_PORT = 1099;

	// JICP packet types

	/** ID code for packets carrying IMTP commands */
	public static final byte          COMMAND_TYPE = 0;
	/** ID code for packets carrying IMTP responses */
	public static final byte          RESPONSE_TYPE = 1;
	/** ID code for packets carrying keep-alive  */
	public static final byte          KEEP_ALIVE_TYPE = 2;
	/** ID code for packets carrying requests to get the local address */
	public static final byte          GET_CONFIG_OPTIONS_TYPE = 20;
	/** ID code for packets carrying requests to get the local address */
	public static final byte          GET_ADDRESS_TYPE = 21;
	/** ID code for packets carrying requests to create a Mediator */
	public static final byte          CREATE_MEDIATOR_TYPE = 22;
	/** ID code for packets carrying requests to connect to a Mediator */
	public static final byte          CONNECT_MEDIATOR_TYPE = 23;
	/** ID code for packets carrying requests to retrieve the current server time */
	public static final byte          GET_SERVER_TIME_TYPE = 24;
	/** ID code for packets carrying requests to drop-down the connection with the mediator */
	public static final byte          DROP_DOWN_TYPE = 30;
	/** ID code for packets carrying JICP protocol errors */
	public static final byte          ERROR_TYPE = 100;


	/**
	 * bit encoded data info constants
	 */
	public static final byte DEFAULT_INFO = 0;                  // All bits = 0
	public static final byte COMPRESSED_INFO = 1;               // bit 1 == 1  (less significant bit)
	public static final byte RECIPIENT_ID_PRESENT_INFO = 2;     // bit 2 == 1
	public static final byte SESSION_ID_PRESENT_INFO = 4;       // bit 3 == 1
	public static final byte DATA_PRESENT_INFO = 8;             // bit 4 == 1
	public static final byte RECONNECT_INFO = 16;               // bit 5 == 1 
	public static final byte OK_INFO = 32;                      // bit 6 == 1
	public static final byte TERMINATED_INFO = 64;              // bit 7 == 1
	// Always distinguished from the context
	//public static final byte BLOCKING_IMTP_PING_INFO = RECONNECT_INFO;
	//public static final byte NON_BLOCKING_IMTP_PING_INFO = OK_INFO;  

	/**
	 * Default recipient ID
	 */
	public static final String  DEFAULT_RECIPIENT_ID = "";

	public static final int DEFAULT_CREATION_ATTEMPTS = 1; // No retry
	/**
	 * Default MaxDisconnection and retry times for the mediator mechanism
	 */
	public static final long DEFAULT_MAX_DISCONNECTION_TIME = 600000; // 10 min
	public static final long DEFAULT_RETRY_TIME = 10000;              // 10 sec
	public static final long DEFAULT_KEEP_ALIVE_TIME = 60000;         // 1 min
	public static final long DEFAULT_RESPONSE_TIMEOUT_OFFSET = 30000;  // 30 sec
	public static final double DEFAULT_RESPONSE_TIMEOUT_MULTIPLICATIVE_FACTOR = ((double)1000) / (5 * 1024);  // 5Kb/1sec
	
	/**
	 * Keys
	 */
	public static final String LOCAL_PORT_KEY = "local-port";
	public static final String LOCAL_HOST_KEY = "local-host";
	// In case it is not possible to get the local host address, it is possible to specify a remote URL where to find it by means of the GET_ADDRESS JICP command
	public static final String REMOTE_URL_KEY = "remote-url";
	public static final String UNREACHABLE_KEY = "unreachable";
	// How many attempts to create the BackEnd in case the CREATE_MEDIATOR fails due to network problems  
	public static final String CREATION_ATTEMPTS_KEY = "creation-attempts";
	public static final String RECONNECTION_RETRY_TIME_KEY = "reconnection-retry-time";
	public static final String MAX_DISCONNECTION_TIME_KEY = "max-disconnection-time";
	public static final String KEEP_ALIVE_TIME_KEY = "keep-alive-time";
	public static final String DROP_DOWN_TIME_KEY = "drop-down-time";
	public static final String MEDIATOR_CLASS_KEY = "mediator-class";
	public static final String MEDIATOR_ID_KEY = "mediator-id";	
	public static final String MSISDN_KEY = "msisdn";	
	public static final String VERSION_KEY = "version";	
	public static final String RESPONSE_TIMEOUT_OFFSET_KEY = "response-timeout-offset";
	public static final String RESPONSE_TIMEOUT_MULTIPLICATIVE_FACTOR_KEY = "response-timeout-multiplicative-factor";
	
	// The property to be specified among FE startup properties to activate server time synchronization
	// If set to true this property is also passed in the CREATE_MEDIATOR request to tell the server that a GET_SERVER_TIME will be sent immediately after mediator creation completion
	public static final String GET_SERVER_TIME_KEY = "get-server-time";	
	// The property keeping the server time offset set by the JADE runtime after startup 
	public static final String SERVER_TIME_OFFSET_KEY = "server-time-offset";	
	
	/**
	 * @deprecated Use <code>MicroRuntime.OWNER_KEY</code>
	 */
	public static final String OWNER_KEY = MicroRuntime.OWNER_KEY;	

	public static final String DUMMY_ID = "_UNKNOWN_";
	
	// Error messages 
	public static final String NOT_FOUND_ERROR = "Not-found";	
	public static final String NOT_AUTHORIZED_ERROR = "Not-authorized";	

	private static JICPProtocol theInstance = new JICPProtocol();

	public static JICPProtocol getInstance() {
		return theInstance;
	}

	/**
	 * Constructor declaration
	 * @param url
	 */
	public String addrToString(TransportAddress ta) throws ICPException {

		// Check that the specified ta is actually a JICP address
		JICPAddress jta = null;

		try {
			jta = (JICPAddress) ta;
		} 
		catch (ClassCastException cce) {
			throw new ICPException("The TransportAddress "+ta.toString()+" is not a JICPAddress");
		} 

		return jta.toString();
	} 

	/**
	 * Method declaration
	 * @param s
	 * @return
	 * @throws ICPException
	 * @see
	 */
	public TransportAddress stringToAddr(String s) throws ICPException {
		Vector  addressFields = parseURL(s);
		String protocol = (String) addressFields.elementAt(0);

		if (!NAME.equals(protocol)) {
			throw new ICPException("Unexpected protocol \""+protocol+"\" when \""+NAME+"\" was expected.");
		} 

		String host = (String) addressFields.elementAt(1);
		String port = (String) addressFields.elementAt(2);
		String file = (String) addressFields.elementAt(3);
		String anchor = (String) addressFields.elementAt(4);

		return new JICPAddress(host, port, file, anchor);
	} 

	/**
	 */
	public TransportAddress buildAddress(String host, String port, String file, String anchor) {
		return new JICPAddress(host, port, file, anchor);
	} 

	/**
	 * Method declaration
	 * @return
	 * @see
	 */
	public String getName() {
		return NAME;
	} 

}

