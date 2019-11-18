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

//#MIDP_EXCLUDE_FILE

import jade.core.Profile;
import jade.imtp.leap.*;
import jade.security.JADESecurityException;
import jade.util.Logger;
import jade.util.leap.Properties;

import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;

import java.net.*;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Class declaration
 * @author Giovanni Caire - TILAB
 * @author Ronnie Taib - Motorola
 * @author Nicolas Lhuillier - Motorola
 * @author Steffen Rusitschka - Siemens
 */
public class JICPServer extends Thread 
//#J2ME_EXCLUDE_BEGIN
implements PDPContextManager.Listener, JICPMediatorManager 
//#J2ME_EXCLUDE_END
{	
	private static final int INIT = 0;
	private static final int REQUEST_READ = 1;
	private static final int REQUEST_SERVED = 2;
	private static final int RESPONSE_SENT = 3;
	
	public static final String ACCEPT_LOCAL_HOST_ONLY = "jade_imtp_leap_JICP_JICPServer_acceptlocalhostonly";
	public static final String UNCHECK_LOCAL_HOST = "jade_imtp_leap_JICP_JICPServer_unchecklocalhost";
	
	private static final int LISTENING = 0;
	private static final int TERMINATING = 1;
	
	private int      state = LISTENING;
	
	private String host;
	private ServerSocket server;
	private ICP.Listener cmdListener;
	
	private int            mediatorCnt = 1;
	private Hashtable      mediators = new Hashtable();
	//#J2ME_EXCLUDE_BEGIN
	public static final String ACCEPT_MEDIATORS = "jade_imtp_leap_JICP_JICPServer_acceptmediators";
	private boolean acceptMediators = true;
	private Properties     leapProps = new Properties();
	private PDPContextManager  myPDPContextManager;
	//#J2ME_EXCLUDE_END
	
	private int maxHandlers;
	private Vector connectionHandlers;
	
	private ConnectionFactory connFactory;
	
	private Logger myLogger;
	
	/**
	 * Constructor declaration
	 */
	public JICPServer(Profile p, JICPPeer myPeer, ICP.Listener l, ConnectionFactory f, int max) throws ICPException {
		
		connectionHandlers = new Vector();
		cmdListener = l;
		connFactory = f;
		maxHandlers = max;
		myLogger = Logger.getMyLogger(getClass().getName());
		
		StringBuffer sb = null;
		int idLength;
		String peerID = myPeer.getID();
		if (peerID != null) {
			sb = new StringBuffer(peerID);
			sb.append('-');
			idLength = sb.length();
		} 
		else {
			sb = new StringBuffer();
			idLength = 0;
		}
		
		// Local host
		sb.append(JICPProtocol.LOCAL_HOST_KEY);
		host = p.getParameter(sb.toString(), null);
		boolean acceptLocalHostOnly = false;
		if (host == null || host.equals(Profile.LOCALHOST_CONSTANT)) {
			// Local host not specified --> Get it automatically
			sb.setLength(idLength);
			sb.append(JICPProtocol.REMOTE_URL_KEY);
			String remoteURL = p.getParameter(sb.toString(), null);
			if (remoteURL != null) {
				// Retrieve the local host address by means of the GET_ADDRESS JICP functionality
				host = myPeer.getAddress(remoteURL);
			} 
			else {
				// Retrieve the host address/name from the underlying operating system
				host = Profile.getDefaultNetworkName(p.getBooleanProperty(Profile.PRIVILEDGE_LOGICAL_NAME, false));
			}
		}
		else {
			// Unless the UNCKECK_LOCAL_HOST property is set, if a local-host is explicitly specified check 
			// that it is a valid local address
			if (!p.getBooleanProperty(UNCHECK_LOCAL_HOST, false) && !Profile.isLocalHost(host)) {
				throw new ICPException("Error: Not possible to launch JADE on a remote host ("+host+"). Check the -host and -local-host options.");
			}
			// Then if the ACCEPT_LOCAL_HOST_ONLY property is specified,
			// we will accept connections only on the specified local network address 
			acceptLocalHostOnly = p.getBooleanProperty(ACCEPT_LOCAL_HOST_ONLY, false);
		}
		
		// Local port: a peripheral container can change it if busy...
		int port = JICPProtocol.DEFAULT_PORT;
		boolean changePortIfBusy = !p.getBooleanProperty(Profile.MAIN, true) || p.getBooleanProperty(LEAPIMTPManager.CHANGE_PORT_IF_BUSY, false);

		sb.setLength(idLength);
		sb.append(JICPProtocol.LOCAL_PORT_KEY);
		String strPort = p.getParameter(sb.toString(), null);
		try {
			port = Integer.parseInt(strPort);
		} 
		catch (Exception e) {
			// Try to use the Peer-ID as the port number
			try {
				port = Integer.parseInt(peerID);
			} 
			catch (Exception e1) {
				// Keep default
			}
		}
		
		//#J2ME_EXCLUDE_BEGIN
		// Get the accept-mediators option
		acceptMediators = p.getBooleanProperty(ACCEPT_MEDIATORS, true);
		
		if (acceptMediators) {
			// Read the LEAP configuration properties
			sb.setLength(idLength);
			sb.append(LEAP_PROPERTY_FILE);
			String fileName = p.getParameter(sb.toString(), LEAP_PROPERTY_FILE_DEFAULT);
			try {
				leapProps.load(fileName);
			} catch (Exception e) {
				myLogger.log(Logger.FINE, "Can't read LEAP property file " + fileName + ". " + e);
				// Ignore: no back end properties specified
			}
			
			// Initialize the PDPContextManager if specified
			String pdpContextManagerClass = leapProps.getProperty(PDP_CONTEXT_MANAGER_CLASS);
			if (pdpContextManagerClass != null) {
				try {
					myLogger.log(Logger.INFO, "Loading PDPContextManager of class " + pdpContextManagerClass);
					myPDPContextManager = (PDPContextManager) Class.forName(pdpContextManagerClass).newInstance();
					myPDPContextManager.init(leapProps);
					myPDPContextManager.registerListener(this);
				} catch (Throwable t) {
					t.printStackTrace();
					myPDPContextManager = null;
				}
			}
		}
		//#J2ME_EXCLUDE_END
		
		// Create the ServerSocket.  
		server = myPeer.getServerSocket((acceptLocalHostOnly ? host : null), port, changePortIfBusy);
		
		setDaemon(true);
		setName("JICPServer-" + getLocalPort());
	}
	
	public int getLocalPort() {
		return server.getLocalPort();
	}
	
	public String getLocalHost() {
		// If a local-host was not specified, we accept connection on all local network addresses,
		// but we expose the local host address we "prefer".
		return host;
	}
	
	/**
	 Shut down this JICP server
	 */
	public synchronized void shutdown() {
		
		if(myLogger.isLoggable(Logger.FINE))
			myLogger.log(Logger.FINE,"Shutting down JICPServer...");
		
		state = TERMINATING;
		
		try {
			// Force the listening thread (this) to exit from the accept()
			// Calling this.interrupt(); should be the right way, but it seems
			// not to work...so do that by closing the server socket.
			server.close();
			
			// Wait for the listening thread to complete
			this.join();
		} 
		catch (IOException ioe) {
			ioe.printStackTrace();
		} 
		catch (InterruptedException ie) {
			ie.printStackTrace();
		} 
	} 
	
	/**
	 * JICPServer thread entry point. Accept incoming connections 
	 * and for each of them start a ConnectionHandler that handles it. 
	 */
	public void run() {
		while (state != TERMINATING) {
			try {
				// Accept connection
				Socket s = server.accept();
				InetAddress addr = s.getInetAddress();
				int port = s.getPort();
				if(myLogger.isLoggable(Logger.FINEST))
					myLogger.log(Logger.FINEST,"Incoming connection from "+addr+":"+port);
				
				Connection c = connFactory.createConnection(s);
				ConnectionHandler ch = new ConnectionHandler(c, addr, port);

				if(myLogger.isLoggable(Logger.FINEST))
					myLogger.log(Logger.FINEST,"Create new ConnectionHandler ("+ch+")");
				
				connectionHandlers.addElement(ch);
				
				ch.start();    // start a handler and go back to listening
			} 
			catch (InterruptedIOException e) {
				// These can be generated by socket timeout (just ignore
				// the exception) or by a call to the shutdown()
				// method (the state has been set to TERMINATING and the
				// server will exit).
			} 
			catch (Exception e) {
				if (state == LISTENING) {
					if(myLogger.isLoggable(Logger.WARNING))
						myLogger.log(Logger.WARNING,"Problems accepting a new connection");
					e.printStackTrace();
					
					// Stop listening
					state = TERMINATING;
				}
			} 
		} // END of while(listen) 
		
		if(myLogger.isLoggable(Logger.FINE))
			myLogger.log(Logger.FINE,"JICPServer terminated");
		
		// release socket
		try {
			server.close();
		} 
		catch (IOException io) {
			if(myLogger.isLoggable(Logger.WARNING))
				myLogger.log(Logger.WARNING,"I/O error closing the server socket");
			io.printStackTrace();
		} 
		
		server = null;

		// Close all connection handler
		synchronized (connectionHandlers) {
			ConnectionHandler ch;
			Enumeration en = connectionHandlers.elements();
			while(en.hasMoreElements()) {
				ch = (ConnectionHandler) en.nextElement();
				ch.close();
			}
		}
		
		//#J2ME_EXCLUDE_BEGIN
		// Close all mediators
		Enumeration e = mediators.elements();
		while (e.hasMoreElements()) {
			JICPMediator m = (JICPMediator) e.nextElement();
			m.kill();
		} 
		mediators.clear();
		//#J2ME_EXCLUDE_END
	} 
	
	/**
	 Called by the JICPPeer ticker at each tick
	 */
	public void tick(long currentTime) {
		//#J2ME_EXCLUDE_BEGIN
		synchronized (mediators) {
			Enumeration e = mediators.elements();
			while (e.hasMoreElements()) {
				JICPMediator m = (JICPMediator) e.nextElement();
				m.tick(currentTime);
			}
		}
		//#J2ME_EXCLUDE_END
	}
	
	/**
	 Inner class ConnectionHandler.
	 Handle a connection accepted by this JICPServer
	 */
	class ConnectionHandler extends Thread {
		private Connection c;
		private InetAddress addr;
		private int port;
		private boolean loop = false;
		private int status = INIT;
		private boolean closeConnection = true;
		
		
		/**
		 * Constructor declaration
		 * @param s
		 */
		public ConnectionHandler(Connection c, InetAddress addr, int port) {
			this.c = c;
			this.addr = addr;
			this.port = port;
		}

		/**
		 * close connection handler
		 */
		public void close() {
			
			if (status != RESPONSE_SENT) {
				// We are serving a request --> Prepare to close connection handler
				loop = false;
				closeConnection = true;

				if(myLogger.isLoggable(Logger.FINEST))
					myLogger.log(Logger.FINEST,"Predispose to close connection handler ("+this+")");

			} else {
				// We are waiting for the next request --> Close connection to force connection handler termination
				try {
					if(myLogger.isLoggable(Logger.FINEST))
						myLogger.log(Logger.FINEST,"Close connection socket to force exit from connection handler ("+this+")");
					
					c.close();
				} catch (IOException e) {
					if(myLogger.isLoggable(Logger.FINEST))
						myLogger.log(Logger.FINEST,"Exception closing connection with "+addr+":"+port);
				}
			}
		}
		
		/**
		 * Thread entry point
		 */
		public void run() {
			if(myLogger.isLoggable(Logger.FINEST))
				myLogger.log(Logger.FINEST,"CommandHandler started");
			
			byte type = (byte) 0;
			try {
				do {
					// Read the incoming JICPPacket
					JICPPacket pkt = c.readPacket();
					JICPPacket reply = null;
					status = REQUEST_READ;
					
					type = pkt.getType();
					switch (type) {
					case JICPProtocol.COMMAND_TYPE:
					case JICPProtocol.RESPONSE_TYPE:
						// Get the right recipient and let it process the command.
						String recipientID = pkt.getRecipientID();
						if(myLogger.isLoggable(Logger.FINEST))
							myLogger.log(Logger.FINEST,"Recipient: "+recipientID);
						if (recipientID != null) {
							//#J2ME_EXCLUDE_BEGIN
							// The recipient is one of the mediators
							JICPMediator m = (JICPMediator) mediators.get(recipientID);
							if (m != null) {
								if(myLogger.isLoggable(Logger.FINEST))
									myLogger.log(Logger.FINEST,"Passing incoming packet to mediator "+recipientID);
								reply = m.handleJICPPacket(pkt, addr, port);
							} 
							else {
								// If the packet is a response we don't need to reply
								if (type == JICPProtocol.COMMAND_TYPE) { 
									reply = new JICPPacket("Unknown recipient "+recipientID, null);
								}
							} 
							//#J2ME_EXCLUDE_END
						} 
						else {
							// The recipient is my ICP.Listener (the local CommandDispatcher)
							loop = true;
							if (type == JICPProtocol.COMMAND_TYPE) { 
								if(myLogger.isLoggable(Logger.FINEST))
									myLogger.log(Logger.FINEST,"Passing incoming COMMAND to local listener");
								
								byte[] rsp = cmdListener.handleCommand(pkt.getData());
								byte dataInfo = JICPProtocol.DEFAULT_INFO;
								if (connectionHandlers.size() >= maxHandlers) {
									// Too many connections open --> close the connection as soon as the command has been served
									dataInfo |= JICPProtocol.TERMINATED_INFO;
									loop = false;
								}
								reply = new JICPPacket(JICPProtocol.RESPONSE_TYPE, dataInfo, rsp);
							}

							if ((pkt.getInfo() & JICPProtocol.TERMINATED_INFO) != 0) {
								loop = false;
							}
						} 
						break;
						
						//#J2ME_EXCLUDE_BEGIN
					case JICPProtocol.GET_ADDRESS_TYPE:
						// Respond sending back the caller address
						if(myLogger.isLoggable(Logger.INFO))
							myLogger.log(Logger.INFO,"Received a GET_ADDRESS request from "+addr+":"+port);
						reply = new JICPPacket(JICPProtocol.GET_ADDRESS_TYPE, JICPProtocol.DEFAULT_INFO, addr.getHostAddress().getBytes());
						break;
						
					case JICPProtocol.CREATE_MEDIATOR_TYPE:
						if (acceptMediators) {
							if(myLogger.isLoggable(Logger.INFO))
								myLogger.log(Logger.INFO,"Received a CREATE_MEDIATOR request from "+ addr + ":" + port);
							
							// Starts a new Mediator and sends back its ID
							String s = new String(pkt.getData());
							Properties p = FrontEndStub.parseCreateMediatorRequest(s);

							// If the platform-name is specified refuse the request: JICPServer does not accept this kind of mediator creation request
							String pn = p.getProperty(Profile.PLATFORM_ID);
							if (pn != null) {
								myLogger.log(Logger.WARNING, "CREATE_MEDIATOR request with specified platform-name: "+pn);
								reply = new JICPPacket(JICPProtocol.NOT_AUTHORIZED_ERROR, new JADESecurityException("Platform-name specified"));
								break;
							}

							// If there is a PDPContextManager add the PDP context properties
							if (myPDPContextManager != null) {
								try{
									//FIXME: Get username and password specified among the CREATE_MEDIATOR properties
									Properties pdpContextInfo = myPDPContextManager.getPDPContextInfo(addr, p.getProperty(Profile.OWNER));
									myLogger.log(Logger.FINE, "PDPContext properties = "+pdpContextInfo);
									mergeProperties(p, pdpContextInfo);
								}catch(JADESecurityException jse){
									if(myLogger.isLoggable(Logger.WARNING))
										myLogger.log(Logger.WARNING,"CREATE_MEDIATOR request from non authorized address: "+addr);
									reply = new JICPPacket(JICPProtocol.NOT_AUTHORIZED_ERROR, jse);
									break;
								}
							}
							
							// Get mediator ID from the passed properties (if present)
							String id = p.getProperty(JICPProtocol.MEDIATOR_ID_KEY); 
							String msisdn = p.getProperty(PDPContextManager.MSISDN);
							if(id != null) {
								if (msisdn != null && !msisdn.equals(id)) {
									// Security attack: Someone is pretending to be someone other
									if(myLogger.isLoggable(Logger.WARNING))
										myLogger.log(Logger.WARNING,"CREATE_MEDIATOR request with mediator-id != MSISDN. Address is: "+addr);
									reply = new JICPPacket(JICPProtocol.NOT_AUTHORIZED_ERROR, new JADESecurityException("Inconsistent mediator-id and msisdn"));
									break;
								}	
								// An existing front-end whose back-end was lost. The BackEnd must resynch 
								p.setProperty(jade.core.BackEndContainer.RESYNCH, "true");
							}
							else {
								// Use the MSISDN (if present) 
								id = msisdn;
								if (id == null) {
									// Construct a default id using the string representation of the server's TCP endpoint
									id = "BE-"+getLocalHost() + ':' + getLocalPort() + '-' + String.valueOf(mediatorCnt++);
								}
							}
							
							// If last connection from the same device aborted, the old 
							// BackEnd may still exist as a zombie. In case ids are assigned
							// using the MSISDN the new name is equals to the old one.
							if (id.equals(msisdn)) {
								JICPMediator old = (JICPMediator) mediators.get(id);
								
								if (old != null) {
									// This is a zombie mediator --> kill it
									myLogger.log(Logger.INFO, "Replacing old mediator "+id);
									old.kill();
									// Be sure the zombie container has been removed
									waitABit(1000);
								}
							}
							
							// Start the mediator
							JICPMediator m = startMediator(id, p);
							closeConnection = !m.handleIncomingConnection(c, pkt, addr, port);
							mediators.put(m.getID(), m);
							
							// Create an ad-hoc reply including the assigned mediator-id and the IP address
							p.setProperty(JICPProtocol.MEDIATOR_ID_KEY, m.getID());
							p.setProperty(JICPProtocol.LOCAL_HOST_KEY, addr.getHostAddress());
							String replyMsg = FrontEndStub.encodeCreateMediatorResponse(p);
							reply = new JICPPacket(JICPProtocol.RESPONSE_TYPE, JICPProtocol.DEFAULT_INFO, replyMsg.getBytes());
							reply.setSessionID((byte) 31); // Dummy session ID != from valid ones
						}
						else {
							myLogger.log(Logger.WARNING,"CREATE_MEDIATOR request received with accept-mediator option set to false. Address is: "+addr);
							reply = new JICPPacket(JICPProtocol.NOT_AUTHORIZED_ERROR, null);
						}
						break;
						
					case JICPProtocol.CONNECT_MEDIATOR_TYPE:
						if (acceptMediators) {
							// A mediated container is (re)connecting to its mediator
							recipientID = pkt.getRecipientID();
							
							// FIXME: If there is a PDPContextManager  check that the recipientID is the MSISDN
							
							if(myLogger.isLoggable(Logger.INFO))
								myLogger.log(Logger.INFO,"Received a CONNECT_MEDIATOR request from "+addr+":"+port+". Mediator ID is "+recipientID);
							JICPMediator m = (JICPMediator) mediators.get(recipientID);
							if (m != null) {
								// Don't close the connection, but pass it to the proper 
								// mediator. 
								closeConnection = !m.handleIncomingConnection(c, pkt, addr, port);
								reply = new JICPPacket(JICPProtocol.RESPONSE_TYPE, JICPProtocol.DEFAULT_INFO, addr.getHostAddress().getBytes());
							}
							else {
								if(myLogger.isLoggable(Logger.INFO))
									myLogger.log(Logger.INFO,"Mediator "+recipientID+" not found");
								reply = new JICPPacket(JICPProtocol.NOT_FOUND_ERROR, null);
							}
						}
						else {
							myLogger.log(Logger.WARNING,"CONNECT_MEDIATOR request received with accept-mediator option set to false. Address is: "+addr);
							reply = new JICPPacket(JICPProtocol.NOT_AUTHORIZED_ERROR, null);
						}
						break;
						//#J2ME_EXCLUDE_END
						
					default:
						// Send back an error response
						if(myLogger.isLoggable(Logger.WARNING))
							myLogger.log(Logger.WARNING,"Uncorrect JICP data type: "+pkt.getType());
					reply = new JICPPacket("Uncorrect JICP data type: "+pkt.getType(), null);
					}
					status = REQUEST_SERVED;
					
					// Send the actual response data
					if (reply != null) {
						//reply.writeTo(out);
						c.writePacket(reply);
					}
					status = RESPONSE_SENT;
				} while (loop); 
			} 
			catch (Exception e) {
				switch (status) {
				case INIT:{
					if(myLogger.isLoggable(Logger.SEVERE))
						myLogger.log(Logger.SEVERE,"Communication error reading incoming packet from "+addr+":"+port);
					e.printStackTrace();
				}
				break;
				case REQUEST_READ:
					if(myLogger.isLoggable(Logger.SEVERE))
						myLogger.log(Logger.SEVERE,"Error handling incoming packet");
					e.printStackTrace();
					// If the incoming packet was a command, try 
					// to send back a generic error response
					if (type == JICPProtocol.COMMAND_TYPE && c != null) {
						try {
							c.writePacket(new JICPPacket("Unexpected error", e));
						} 
						catch (IOException ioe) {   
							// Just print a warning
							if(myLogger.isLoggable(Logger.WARNING))
								myLogger.log(Logger.WARNING,"Can't send back error indication "+ioe);
						} 
					}
					break;
				case REQUEST_SERVED:
					if(myLogger.isLoggable(Logger.SEVERE))
						myLogger.log(Logger.SEVERE,"Communication error writing return packet to "+addr+":"+port+" ["+e.toString()+"]");
					break;
				case RESPONSE_SENT:
					// This is a re-used connection waiting for the next incoming packet
					if (e instanceof EOFException) {
						if(myLogger.isLoggable(Logger.FINE))
							myLogger.log(Logger.FINE,"Client "+addr+":"+port+" has closed the connection.");
					}
					else {
						if(myLogger.isLoggable(Logger.FINE))
							myLogger.log(Logger.FINE,"Unexpected client "+addr+":"+port+" termination. "+e.toString());
					}
				}
			} 
			finally {
				try {
					if (closeConnection) {
						// Close connection
						if(myLogger.isLoggable(Logger.FINEST))
							myLogger.log(Logger.FINEST,"Closing connection with "+addr+":"+port);
						
						c.close();
					} 
				} 
				catch (IOException io) {
					if(myLogger.isLoggable(Logger.INFO))
						myLogger.log(Logger.INFO,"I/O error while closing the connection");
					io.printStackTrace();
				} 

				connectionHandlers.remove(this);

				if(myLogger.isLoggable(Logger.FINEST))
					myLogger.log(Logger.FINEST,"ConnectionHandler closed ("+this+")");
			} 
		} 
	} // END of inner class ConnectionHandler
	
	//#J2ME_EXCLUDE_BEGIN
	/**
	 * Called by a Mediator to notify that it is no longer active
	 */
	public void deregisterMediator(String id) {
		myLogger.log(Logger.FINE, "Deregistering mediator "+id);
		mediators.remove(id);
	} 
	
	/**
	 Called by the PDPContextManager (if any)
	 */
	public void handlePDPContextClosed(String id) {
		// FIXME: to be implemented
	}
	
	private void mergeProperties(Properties p1, Properties p2) {
		Enumeration e = p2.propertyNames();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			p1.setProperty(key, p2.getProperty(key));
		}
	}
	
	private JICPMediator startMediator(String id, Properties p) throws Exception {
		String className = p.getProperty(JICPProtocol.MEDIATOR_CLASS_KEY);
		if (className != null) {
			JICPMediator m = (JICPMediator) Class.forName(className).newInstance();
			mergeProperties(p, leapProps);
			myLogger.log(Logger.FINE, "Initializing mediator "+id+" with properties "+p);
			m.init(this, id, p);
			return m;
		}
		else {
			throw new ICPException("No JICPMediator class specified.");
		}
	}
	
	private void waitABit(long t) {
		try {
			Thread.sleep(t);
		}
		catch (InterruptedException ie) {
		}
	}
	//#J2ME_EXCLUDE_END
}

