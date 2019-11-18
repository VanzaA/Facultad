package jade.imtp.leap.nio;

//#J2ME_EXCLUDE_FILE

import jade.core.BackEnd;
import jade.core.BackEndContainer;
import jade.core.BEConnectionManager;
import jade.core.FrontEnd;
import jade.core.IMTPException;
import jade.core.Profile;
import jade.core.ProfileException;
import jade.core.Runtime;
import jade.core.Timer;
import jade.core.TimerListener;
import jade.imtp.leap.BackEndSkel;
import jade.imtp.leap.FrontEndStub;
import jade.imtp.leap.ICPDispatchException;
import jade.imtp.leap.ICPException;
import jade.imtp.leap.MicroSkeleton;
import jade.imtp.leap.Dispatcher;
import jade.imtp.leap.JICP.Connection;
import jade.imtp.leap.JICP.JICPMediatorManager;
import jade.imtp.leap.JICP.JICPProtocol;
import jade.imtp.leap.JICP.JICPPacket;
import jade.util.Logger;
import jade.util.leap.Properties;

import java.io.IOException;
import java.net.InetAddress;

/**
 *
 * @author Eduard Drenth: Logica, 11-jul-2009
 * 
 */
public class NIOHTTPBEDispatcher implements NIOMediator, Dispatcher, BEConnectionManager {
	// Local statuses
	private static final int ACTIVE = 0;
	private static final int NOT_ACTIVE = 1;
	
	// Front-end statuses
	private static final int CONNECTED = 0;
	private static final int CONNECTING = 1;
	private static final int DISCONNECTED = 2;
	private static final int TERMINATED = 3;
	
	private static final long OUTGOING_COMMANDS_CONNECTION_TIMEOUT = 30000; // 30 sec
	
	private static final long RESPONSE_TIMEOUT = 30000; // 30 sec
	private static final long RESPONSE_TIMEOUT_INCREMENT = 100; // 100 msec
	
	private static final int MAX_SID = 0x0f;

	private JICPMediatorManager myMediatorManager;
	private String myID;
	private MicroSkeleton mySkel = null;
	private FrontEndStub myStub = null;
	private BackEndContainer myContainer = null;
	
	private int status = ACTIVE;
	private int frontEndStatus = CONNECTING;
	private long maxDisconnectionTime;
	private Timer maxDisconnectionTimer = null;
	private long keepAliveTime;
	private Timer keepAliveTimer = null;

	private JICPPacket lastResponse = null;
	private byte lastIncomingCommandSid;

	private boolean waitingForFlush = false;	
	private Connection outgoingCommandsConnection = null;
	private Object outgoingCommandsConnectionLock = new Object();
	private int nextOutgoingCommandSid;
	private JICPPacket responseToLastOutgoingCommand = null;
	private Object responseToLastOutgoingCommandLock = new Object();

	private Logger myLogger = Logger.getMyLogger(getClass().getName());

	//////////////////////////////////////////
	// NIOMediator interface implementation
	//////////////////////////////////////////
	public String getID() {
		return myID;
	}

	/**
	 * Initialize parameters and activate the BackEndContainer
	 */
	public void init(JICPMediatorManager mgr, String id, Properties props) throws ICPException {
		myMediatorManager = mgr;
		myID = id;

		// Read parameters
		// Max disconnection time
		maxDisconnectionTime = JICPProtocol.DEFAULT_MAX_DISCONNECTION_TIME;
		try {
			maxDisconnectionTime = Long.parseLong(props.getProperty(JICPProtocol.MAX_DISCONNECTION_TIME_KEY));
		} catch (Exception e) {
			// Keep default
		}

		// Max disconnection time
		keepAliveTime = JICPProtocol.DEFAULT_KEEP_ALIVE_TIME;
		try {
			keepAliveTime = Long.parseLong(props.getProperty(JICPProtocol.KEEP_ALIVE_TIME_KEY));
		} catch (Exception e) {
			// Keep default
		}

		// Counter to assign the SID to the next command to be delivered to the FE (only present if this is a back-end re-creation)
		nextOutgoingCommandSid = 0;
		try {
			// The FE indicates the SID of the last command it received --> Increment it by 1
			nextOutgoingCommandSid = increment(Integer.parseInt(props.getProperty("lastsid")));
		}
		catch (Exception e) {
			// Keep default
		}
		
		// SID of last command received from the FE (only present if this is a back-end re-creation)
		lastIncomingCommandSid = 0x10;
		try {
			// The FE indicates the SID of the next command it will send us --> Decrement it by 1
			lastIncomingCommandSid = (byte) decrement(Integer.parseInt(props.getProperty("outcnt")));
		}
		catch (Exception e) {
			// Keep default
		}
		
		myLogger.log(Logger.INFO, "Created NIOHTTPBEDispatcher V1.0. ID = " + myID + "\n- Max-disconnection-time = " + maxDisconnectionTime+ "\n- Keep-alive-time = " + keepAliveTime);
		myLogger.log(Logger.CONFIG, myID+" - Next command for FE will have SID = " + nextOutgoingCommandSid);

		myStub = new FrontEndStub(this);
		mySkel = startBackEndContainer(props);
	}

	private final BackEndSkel startBackEndContainer(Properties props) throws ICPException {
		try {
			String nodeName = myID.replace(':', '_');
			props.setProperty(Profile.CONTAINER_NAME, nodeName);

			myContainer = new BackEndContainer(props, this);
			if (!myContainer.connect()) {
				throw new ICPException("BackEnd container failed to join the platform");
			}
			//Possibly the node name was re-assigned by the main
			myID = myContainer.here().getName();
			myLogger.log(Logger.CONFIG, myID+" - BackEndContainer " + myID + " successfully joined the platform.");
			return new BackEndSkel(myContainer);
		} catch (ProfileException pe) {
			// should never happen
			pe.printStackTrace();
			throw new ICPException("Error creating profile");
		}
	}

	/**
	 * Termination self initiated or forced by the MediatorManager we are attached to.
	 */
	public void kill() {
		status = NOT_ACTIVE;
		// Force the BackEndContainer to terminate. 
		// This will also cause this NIOHTTPBEDispatcher to terminate and deregister from the MediatorManager
		myContainer.shutDown();
	}

	public void tick(long time) {
		// Not used: just do nothing
	}
	
	/**
	 * Handle an incoming connection. This is called by the MediatorManager
	 * when a CREATE or CONNECT_MEDIATOR request is received.
	 * In both cases the Front-end is connecting -->
	 * Set the front-end status to CONNECTING, but don't use this connection: to allow us  
	 * sending commands to the front-end an initial dummy response will be received soon. 
	 */
	public synchronized boolean handleIncomingConnection(Connection c, JICPPacket pkt, InetAddress addr, int port) {
		myLogger.log(Logger.INFO, myID+" - Front-end connecting ["+addr+":"+port+"]");
		setFrontEndConnecting();
		// Returning false will make the MediatorManager close the connection
		return false;
	}

	/**
	 * Handle an incoming JICP packet received by the MediatorManager 
	 */
	public JICPPacket handleJICPPacket(Connection c, JICPPacket pkt, InetAddress addr, int port) throws ICPException {
		JICPPacket response = null;
		if ((status == ACTIVE) && (frontEndStatus != TERMINATED)) {
			if (myLogger.isLoggable(Logger.FINE)) {
				myLogger.log(Logger.FINE, myID+" - Incoming packet. Type = "+pkt.getType()+", SID = "+pkt.getSessionID()+", terminated-info = "+((pkt.getInfo() & JICPProtocol.TERMINATED_INFO) != 0));
			}
			String from = " [" + addr + ":" + port + "]";
			if (pkt.getType() == JICPProtocol.COMMAND_TYPE) {
				// COMMAND
				if ((pkt.getInfo() & JICPProtocol.TERMINATED_INFO) != 0) {
					// PEER TERMINATION NOTIFICATION
					// The remote FrontEnd terminated spontaneously --> Terminate and notify up.
					myLogger.log(Logger.INFO, myID+" - Peer termination notification received");
					handlePeerSelfTermination();
					// Since we return null the MediatorManager would keep the connection open --> close it explicitly 
					return createTerminationNotificationAck();
				} else {
					// NORMAL COMMAND
					// Serve the incoming command and send back the response
					byte sid = pkt.getSessionID();
					if (sid == lastIncomingCommandSid && lastResponse != null) {
						myLogger.log(Logger.WARNING, myID+" - Duplicated command received. SID = " + sid);
						response = lastResponse;
					} else {
						if (myLogger.isLoggable(Logger.FINE)) {
							myLogger.log(Logger.FINE, myID+" - Incoming command received. SID = " + sid);
						}
	
						byte[] rspData = mySkel.handleCommand(pkt.getData());
						if (myLogger.isLoggable(Logger.FINE)) {
							myLogger.log(Logger.FINE, myID+" - Incoming command served. SID = " + sid);
						}
						response = new JICPPacket(JICPProtocol.RESPONSE_TYPE, JICPProtocol.DEFAULT_INFO, rspData);
						response.setSessionID(sid);
						lastIncomingCommandSid = sid;
						lastResponse = response;
					}
				}
			} else {
				// RESPONSE.
				handleResponse(c, pkt, from);
				if ((pkt.getInfo() & JICPProtocol.TERMINATED_INFO) != 0) {
					// PEER TERMINATION NOTIFICATION. 
					// The remote FrontEnd terminated as a consequence of an EXIT command
					setFrontEndTerminated();
					shutdown();
					// Since we return null the MediatorManager would keep the connection open --> close it explicitly 
					return createTerminationNotificationAck();
				}
			}
		}
		else {
			myLogger.log(Logger.FINE, "Unexpected packet received after termination. Type = "+pkt.getType()+", SID = "+pkt.getSessionID()+", terminated-info = "+((pkt.getInfo() & JICPProtocol.TERMINATED_INFO) != 0));
		}

		return response;
	}

	public JICPPacket handleJICPPacket(JICPPacket pkt, InetAddress addr, int port) throws ICPException {
		throw new ICPException("Unexpected call");
	}

	public void handleConnectionError(Connection c, Exception e) {
		// The MediatorManager got an exception reading from connection c
		// FIXME: What should we do? For the moment just print a warning
		if ((status == ACTIVE) && (frontEndStatus != TERMINATED)) {
			myLogger.log(Logger.WARNING, myID+" - Exception reading from the connection", e);
		}
	}

	public Properties getProperties() {
		return new Properties();
	}

	private void handlePeerSelfTermination() {
		// The FrontEnd exited --> Set its new status and then suicide!
		setFrontEndTerminated();
		kill();
	}

	////////////////////////////////////////////////
	// BEConnectionManager interface implementation
	////////////////////////////////////////////////
	/**
	 * Return a stub of the remote FrontEnd that is connected to the local BackEnd.
	 * @param be The local BackEnd
	 * @param props Additional (implementation dependent) connection configuration properties.
	 * @return A stub of the remote FrontEnd.
	 */
	public FrontEnd getFrontEnd(BackEnd be, Properties props) throws IMTPException {
		return myStub;
	}

	/**
	 * Termination initiated by the BackEndContainer (i.e. by the platform).
	 * When the BackEndContainer exits  
	 */
	public void shutdown() {
		myLogger.log(Logger.INFO, myID+" - Initiate NIOHTTPBEDispatcher shutdown");
		status = NOT_ACTIVE;

		// Deregister from the MediatorManager
		if (myID != null) {
			myMediatorManager.deregisterMediator(myID);
			myID = null;
		}
		
		// Clean everything
		clean();
	}

	
	//////////////////////////////////////////
	// Dispatcher interface implementation
	//////////////////////////////////////////
	/**
	 * This is called by the Stub using this Dispatcher to dispatch a serialized command to the FrontEnd.
	 * Mutual exclusion with itself to ensure one command at a time is dispatched.
	 */
	public synchronized byte[] dispatch(byte[] payload, boolean flush, int oldSessionId) throws ICPException {
		if (status == ACTIVE) {
			if (frontEndStatus == CONNECTED) {
				// The following check preserves dispatching order when the
				// front-end has just reconnected but flushing of postponed commands has not started yet
				if (waitingForFlush && !flush) {
					throw new ICPException("Upsetting dispatching order");
				}
				waitingForFlush = false;
	
				// Wait for the connection to deliver outgoing commands to be ready 
				Connection c = getOutgoingCommandsConnection();
	
				// Send the command to the front-end
				JICPPacket cmd = new JICPPacket(JICPProtocol.COMMAND_TYPE, JICPProtocol.DEFAULT_INFO, payload);
				if (flush && oldSessionId != -1) {
					// This is a postponed command whose previous dispatch failed --> Use the
					// old sessionId, so that if the server already received it (previous dispatch 
					// failed due to a response delivering error) the command will be recognized 
					// as duplicated and properly managed
					nextOutgoingCommandSid = oldSessionId;
				}
				int sid = nextOutgoingCommandSid;
				nextOutgoingCommandSid = increment(nextOutgoingCommandSid);
				cmd.setSessionID((byte) sid);
				if (myLogger.isLoggable(Logger.FINE)) {
					myLogger.log(Logger.FINE, myID+" - Delivering outgoing command to front-end. SID = " + sid);
				}
				try {
					c.writePacket(cmd);
					close(c);
					// Wait for the response 
					JICPPacket response = getResponse(RESPONSE_TIMEOUT + RESPONSE_TIMEOUT_INCREMENT * (cmd.getLength() / 1024));
					if (myLogger.isLoggable(Logger.FINE)) {
						myLogger.log(Logger.FINE, myID+" - Response got. SID = " + sid);
					}
					if (response.getType() == JICPProtocol.ERROR_TYPE) {
						// Communication OK, but there was a JICP error on the peer
						throw new ICPException(new String(response.getData()));
					}
					return response.getData();
				}
				catch (IOException ioe) {
					setFrontEndDisconnected();
					throw new ICPDispatchException("Error delivering outgoing command to front-end. ", cmd.getSessionID());
				}
				catch (ICPException icpe) {
					// Note that in this case setFrontEndDisconnected() is already called within getResponse() or getOutgoingCommandsConnection()
					throw new ICPDispatchException(icpe.getMessage(), cmd.getSessionID());
				}
			}
			else {
				throw new ICPException("Front-end not connected");
			}
		}
		else {
			throw new ICPException("Not-active");
		}
	}
	
	private synchronized void dispatchKeepAlive() {
		if (status == ACTIVE) {
			if (frontEndStatus == CONNECTED) {
				try {
					// Wait for the connection to deliver outgoing commands to be ready 
					Connection c = getOutgoingCommandsConnection();
		
					// Send the command to the front-end
					if (myLogger.isLoggable(Logger.FINER)) {
						myLogger.log(Logger.FINER, myID+" - Delivering keep-alive to front-end");
					}
					JICPPacket cmd = new JICPPacket(JICPProtocol.KEEP_ALIVE_TYPE, JICPProtocol.DEFAULT_INFO, null);
					c.writePacket(cmd);
					close(c);
					// Wait for the response 
					JICPPacket response = getResponse(RESPONSE_TIMEOUT + RESPONSE_TIMEOUT_INCREMENT * (cmd.getLength() / 1024));
					if (isKeepAliveResponse(response)) {
						if (myLogger.isLoggable(Logger.FINER)) {
							myLogger.log(Logger.FINER, myID+" - Keep-alive response got");
						}
					}
					else {
						// Should never happen
						myLogger.log(Logger.WARNING, "Unexpected response received while waiting for Keep-alive response");
					}
				}
				catch (IOException ioe) {
					myLogger.log(Logger.WARNING, myID+" - Error delivering keep-alive packet to the front-end", ioe);
					setFrontEndDisconnected();
				}
				catch (ICPException icpe) {
					// Note that in this case setFrontEndDisconnected() is already called within getResponse() or getOutgoingCommandsConnection()
					if (frontEndStatus != TERMINATED) {
						myLogger.log(Logger.WARNING, myID+" - Keep-alive error. "+icpe.getMessage());
					}
				}
			}
		}
	}

	/**
	 * Wait until a connection to deliver commands to the FrontEnd is ready
	 * @see handleResponse()
	 */
	private Connection getOutgoingCommandsConnection() throws ICPException {
		try {
			synchronized (outgoingCommandsConnectionLock) {
				while (outgoingCommandsConnection == null) {
					outgoingCommandsConnectionLock.wait(OUTGOING_COMMANDS_CONNECTION_TIMEOUT);
					if (outgoingCommandsConnection == null) {
						if (frontEndStatus == TERMINATED) {
							// We terminated in the meanwhile
							throw new ICPException("Terminated");
						}
						else {
							// Timeout expired
							setFrontEndDisconnected();
							throw new ICPException("Response timeout");
						}
					}
				}
				Connection c = outgoingCommandsConnection;
				outgoingCommandsConnection = null;
				return c;
			}
		}
		catch (InterruptedException ie) {
			throw new ICPException("Interrupted while waiting for outgoing-commands-connection");
		}
	}

	/**
	 * Wait until the response to the last outgoing command is received
	 * @see handleResponse()
	 */
	private JICPPacket getResponse(long timeout) throws ICPException {
		try {
			synchronized (responseToLastOutgoingCommandLock) {
				while (responseToLastOutgoingCommand == null) {
					responseToLastOutgoingCommandLock.wait(timeout);
					if (responseToLastOutgoingCommand == null) {
						if (frontEndStatus == TERMINATED) {
							// We terminated in the meanwhile
							throw new ICPException("Terminated");
						}
						else {
							// Timeout expired
							setFrontEndDisconnected();
							throw new ICPException("Response timeout");
						}
					}
				}
				JICPPacket response = responseToLastOutgoingCommand;
				responseToLastOutgoingCommand = null;
				return response;
			}
		}
		catch (InterruptedException ie) {
			throw new ICPException("Interrupted while waiting for response to outgoing command");
		}
	}

	private void handleResponse(Connection c, JICPPacket response, String from) {
		if (frontEndStatus != CONNECTED) {
			if (frontEndStatus == CONNECTING) {
				// Initial dummy response
				myLogger.log(Logger.INFO, myID+" - Initial dummy response received.");
			}
			else {
				// Unexpected response
				myLogger.log(Logger.WARNING, myID+" - Unexpected (likely out of time) response received.", new Exception("DUMMY!!!!"));
			}
			// In any case now we are connected 
			setFrontEndConnected();
		}
		else {
			// Normal response: pass it to the thread that dispatched the command
			if (isKeepAliveResponse(response)) {
				if (myLogger.isLoggable(Logger.FINER)) {
					myLogger.log(Logger.FINER, myID+" - Keep-alive response received");
				}
			}
			else {
				if (myLogger.isLoggable(Logger.FINE)) {
					myLogger.log(Logger.FINE, myID+" - Response received. SID = " + response.getSessionID());
				}
			}
			synchronized (responseToLastOutgoingCommandLock) {
				responseToLastOutgoingCommand = response;
				responseToLastOutgoingCommandLock.notifyAll();
			}
		}

		// Store the connection: it will be used to deliver the next outgoing command
		synchronized (outgoingCommandsConnectionLock) {
			outgoingCommandsConnection = c;
			outgoingCommandsConnectionLock.notifyAll();
		}
		
		updateKeepAliveTimer();
	}


	private void close(Connection c) {
		try {
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private int increment(int val) {
		return (val + 1) & MAX_SID;
	}
	
	private int decrement(int val) {
		val--;
		if (val < 0) {
			val = MAX_SID;
		}
		return val;
	}
	
	private boolean isKeepAliveResponse(JICPPacket response) {
		// The OK_INFO bit is set only on KEEP-ALIVE responses
		return (response.getInfo() & JICPProtocol.OK_INFO) != 0;
	}
	
	private void setFrontEndConnecting() {
		frontEndStatus = CONNECTING;
		resetMaxDisconnectionTimer();	
		outgoingCommandsConnection = null;
		responseToLastOutgoingCommand = null;
	}
	
	private void setFrontEndConnected() {
		frontEndStatus = CONNECTED;
		resetMaxDisconnectionTimer();
		waitingForFlush = myStub.flush();
	}
	
	private void setFrontEndDisconnected() {
		frontEndStatus = DISCONNECTED;
		activateMaxDisconnectionTimer();
	}
	
	private void setFrontEndTerminated() {
		frontEndStatus = TERMINATED;
	}
	
	private synchronized void updateKeepAliveTimer() {
		if (keepAliveTime > 0) {
			// Update the timer that triggers the delivery of a KEEP-ALIVE packet
			if (keepAliveTimer != null) {
				Runtime.instance().getTimerDispatcher().remove(keepAliveTimer);
			}
			long now = System.currentTimeMillis();
			keepAliveTimer = new Timer(now + keepAliveTime, new TimerListener() {
				public void doTimeOut(Timer t) {
					dispatchKeepAlive();
				}
			});
			keepAliveTimer = Runtime.instance().getTimerDispatcher().add(keepAliveTimer);
			if (myLogger.isLoggable(Logger.FINEST)) {
				myLogger.log(Logger.FINEST, myID+" - Keep-alive timer activated.");
			}
		}
	}
	
	// No need for synchronization as this is always executed within a synchronized block
	private void activateMaxDisconnectionTimer() {
		// Set the disconnection timer
		long now = System.currentTimeMillis();
		maxDisconnectionTimer = new Timer(now + maxDisconnectionTime, new TimerListener() {
			public void doTimeOut(Timer t) {
				synchronized (NIOHTTPBEDispatcher.this) {
					if (frontEndStatus != CONNECTED) {
						myLogger.log(Logger.WARNING, myID+" - Max disconnection timeout expired.");
						// The remote FrontEnd is probably down --> notify up.
						handlePeerSelfTermination();
					}
				}
			}
		});
		maxDisconnectionTimer = Runtime.instance().getTimerDispatcher().add(maxDisconnectionTimer);
		myLogger.log(Logger.INFO, myID+" - Max-disconnection-timer activated.");
	}

	private void resetMaxDisconnectionTimer() {
		if (maxDisconnectionTimer != null) {
			Runtime.instance().getTimerDispatcher().remove(maxDisconnectionTimer);
			maxDisconnectionTimer = null;
		}
	}
	
	private void clean() {
		// Be sure not to leave any pending timer
		resetMaxDisconnectionTimer();
		// If there is some thread waiting for something, make it exit
		synchronized (responseToLastOutgoingCommandLock) {
			responseToLastOutgoingCommand = null;
			responseToLastOutgoingCommandLock.notifyAll();
		}
		synchronized (outgoingCommandsConnectionLock) {
			outgoingCommandsConnection = null;
			outgoingCommandsConnectionLock.notifyAll();
		}
	}
	
	private JICPPacket createTerminationNotificationAck() {
		return new JICPPacket(JICPProtocol.COMMAND_TYPE, JICPProtocol.TERMINATED_INFO, null);
	}
}
