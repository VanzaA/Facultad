package jade.imtp.leap.nio;

//#J2ME_EXCLUDE_FILE

import jade.core.FrontEnd;
import jade.core.BackEnd;
import jade.core.BackEndContainer;
import jade.core.BEConnectionManager;
import jade.core.Profile;
import jade.core.ProfileException;
import jade.core.IMTPException;
import jade.imtp.leap.BackEndSkel;
import jade.imtp.leap.FrontEndStub;
import jade.imtp.leap.Dispatcher;
import jade.imtp.leap.ICPDispatchException;
import jade.imtp.leap.ICPException;
import jade.imtp.leap.JICP.JICPProtocol;
import jade.imtp.leap.JICP.JICPMediatorManager;
import jade.imtp.leap.JICP.JICPPacket;
import jade.imtp.leap.JICP.Connection;
import jade.util.leap.Properties;
import jade.util.Logger;

import java.io.IOException;
import java.net.InetAddress;

/**
 This class implements the FrontEndDispatcher related BackEnd dispatcher 
 managable by an asynchronous JICPMediatorManager  
 @author Giovanni Caire - Telecom Italia LAB S.p.A.
 */
public class BackEndDispatcher implements NIOMediator, BEConnectionManager, Dispatcher {
	
	private long responseTimeoutOffset;
	private double responseTimeoutMultiplicativeFactor;
	private long keepAliveTime;
	private long maxDisconnectionTime;
	private long expirationDeadline;
	private long lastReceivedTime;
	private boolean active = true;
	private boolean peerActive = true;
	private boolean connectionDropped = false;
	private long dropTimeStamp = -1;
	
	private JICPMediatorManager myMediatorManager;
	private String myID;
	private Properties myProperties;
	private BackEndContainer myContainer = null;
	
	private Connection myConnection = null;
	private Object writeLock = new Object();
	protected InputManager  inpManager;
	protected OutputManager  outManager;
	
	private Logger myLogger = Logger.getMyLogger(getClass().getName());
	
	/**
	   Retrieve the ID of this mediator. Returns null if this mediator
	   is not active
	 */
	public String getID() {
		return (active ? myID : null);
	}
	
	/**
	   Retrieve the startup Properties for this NIOBEDispatcher.
	 */
	public Properties getProperties() {
		return myProperties;
	}
	
	/**
	  Initialize this NIOMediator
	 */
	public void init(JICPMediatorManager mgr, String id, Properties props) throws ICPException {
		myLogger.log(Logger.INFO, "BackEndDispatcher starting...");
		myMediatorManager = mgr;
		myID = id;
		myProperties = props;
		

		// Response timeout offset
		responseTimeoutOffset = JICPProtocol.DEFAULT_RESPONSE_TIMEOUT_OFFSET;
		try {
			responseTimeoutOffset = Long.parseLong(props.getProperty(JICPProtocol.RESPONSE_TIMEOUT_OFFSET_KEY));
		}
		catch (Exception e) {
			// Keep default
		}

		// Response timeout multiplicative factor
		responseTimeoutMultiplicativeFactor = JICPProtocol.DEFAULT_RESPONSE_TIMEOUT_MULTIPLICATIVE_FACTOR;
		try {
			responseTimeoutMultiplicativeFactor = Double.parseDouble(props.getProperty(JICPProtocol.RESPONSE_TIMEOUT_MULTIPLICATIVE_FACTOR_KEY));
		}
		catch (Exception e) {
			// Keep default
		}
		
		// Max disconnection time
		maxDisconnectionTime = JICPProtocol.DEFAULT_MAX_DISCONNECTION_TIME;
		try {
			maxDisconnectionTime = Long.parseLong(props.getProperty(JICPProtocol.MAX_DISCONNECTION_TIME_KEY));
		}
		catch (Exception e) {
			// Keep default
		}
		
		// Keep-alive time
		keepAliveTime = JICPProtocol.DEFAULT_KEEP_ALIVE_TIME;
		try {
			keepAliveTime = Long.parseLong(props.getProperty(JICPProtocol.KEEP_ALIVE_TIME_KEY));
		}
		catch (Exception e) {
			// Keep default
		}
		
		// inpCnt
		int inpCnt = 0;
		try {
			inpCnt = (Integer.parseInt(props.getProperty("lastsid")) + 1) & 0x0f;
		}
		catch (Exception e) {
			// Keep default
		}
		myLogger.log(Logger.INFO, "Next command for FE will have sessionID "+inpCnt);
		
		/* lastSid
		int lastSid = 0x0f;
		try {
			lastSid = (byte) (Integer.parseInt(props.getProperty("outcnt")) -1);
			if (lastSid < 0) {
				lastSid = 0x0f;
			}
		}
		catch (Exception e) {
			// Keep default
		}
		myLogger.log(Logger.INFO, "Last command successfully received from FE had sessionID "+lastSid);
		*/
		FrontEndStub st = new FrontEndStub(this);
		inpManager = new InputManager(inpCnt, st);
		
		BackEndSkel sk = startBackEndContainer(props);
		outManager = new OutputManager(0x0f, sk);
	}
	
	protected final BackEndSkel startBackEndContainer(Properties props) throws ICPException {
		try {
			String nodeName = myID.replace(':', '_');
			props.setProperty(Profile.CONTAINER_NAME, nodeName);
			
			myContainer = new BackEndContainer(props, this);
			if (!myContainer.connect()) {
				throw new ICPException("BackEnd container failed to join the platform");
			}
			// Possibly the node name was re-assigned by the main
			myID = myContainer.here().getName();
			if(myLogger.isLoggable(Logger.CONFIG)) {
				myLogger.log(Logger.CONFIG,"BackEndContainer "+myID+" successfully joined the platform");
			}
			return new BackEndSkel(myContainer);
		}
		catch (ProfileException pe) {
			// should never happen
			pe.printStackTrace();
			throw new ICPException("Error creating profile");
		}
	}
	
	// Local variable only used in the kill() method
	private Object shutdownLock = new Object();
	
	/**
	   Kill the above container.
	   This may be called by the JICPMediatorManager or when 
	   a peer termination notification is received.
	 */
	public void kill() {
		// Avoid killing the above container two times
		synchronized (shutdownLock) {
			if (active) {
				active = false;
				myContainer.shutDown();
			}
		}
	}
	
	/**
	 * Passes to this JICPMediator the connection opened by the mediated 
	 * entity.
	 * This is called by the JICPMediatorManager this Mediator is attached to
	 * as soon as the mediated entity (re)connects.
	 * @param c the connection to the mediated entity
	 * @param pkt the packet that was sent by the mediated entity when 
	 * opening this connection
	 * @param addr the address of the mediated entity
	 * @param port the local port used by the mediated entity
	 * @return an indication to the JICPMediatorManager to keep the 
	 * connection open.
	 */
	public boolean handleIncomingConnection(Connection c, JICPPacket pkt, InetAddress addr, int port) {
		if (pkt.getType() == JICPProtocol.CONNECT_MEDIATOR_TYPE) {
			// Unblock any Thread waiting for a response. It will behave as if the response timeout was expired
			inpManager.notifyIncomingResponseReceived(null);
		}
		
		// Lock the buffer of pending commands (if any) for flushing (MicroStub.beginFlush()) 
		// before entering the synchronized block to avoid deadlock with a dispatching Thread
		// that already locked the buffer of pending commands for dispatching (MicroStub.beginDispatch()),
		// but did not enter the dispatch() method yet
		Thread flusher = inpManager.prepareFlush();
		
		synchronized (this) {
			checkTerminatedInfo(pkt);
			
			// Update keep-alive info
			lastReceivedTime = System.currentTimeMillis();
	
			if (peerActive) {
				// In some cases the front-end disconnects and we do not detect that.
				// When it reconnects, close the previous connection if still there.
				if (myConnection != null && myConnection != c) {
					try {
						myConnection.close();
					} catch(Exception e) {
					}
				}
				
				myConnection = c;
				myLogger.log(Logger.INFO, myID+": Connection = "+myConnection);
				
				updateConnectedState();	
				connectionDropped = false;
				
				// Activate flushing (if needed) only when the connection has been fully re-established
				if (flusher != null) {
					flusher.start();
				}
				return true;
			}
			else {
				// The FrontEnd has terminated --> No need to flush anything
				if (flusher != null) {
					inpManager.abortFlush();
				}
				// The remote FrontEnd has terminated spontaneously -->
				// Kill the above container (this will also kill this BackEndDispatcher).
				kill();
				return false;
			}
		}
	}
	
	/**
	   Notify this NIOMediator that an error occurred on one of the 
	   Connections it is using. This information is important since, 
	   unlike normal mediators, a NIOMediator typically does not read 
	   packets from 
	   connections on its own (the JICPMediatorManager does that in general).
	 */
	public void handleConnectionError(Connection c, Exception e) {
		// In case someone was waiting for a response notify it the response will never arrive
		inpManager.notifyIncomingResponseReceived(null);
		
		synchronized (this) {
			if (active && peerActive) {
				if (c == myConnection) {
					myConnection = null;
					updateConnectedState();
					myLogger.log(Logger.WARNING, myID+": Disconnection detected");
					setExpirationDeadline();
				}
			}
			try {
				c.close();
			} catch(Exception e1) {
				myLogger.log(Logger.WARNING, myID+": Unexpected error closing Connection = "+c, e1);
			}
		}
	}
	
	/**
	   Passes to this mediator a JICPPacket received by the 
	   JICPMediatorManager this mediator is attached to.
	   In a NIOMediator this should never be called.
	 */
	public JICPPacket handleJICPPacket(JICPPacket p, InetAddress addr, int port) throws ICPException {
		throw new ICPException("Unexpected call");
	}
	
	/**
	   Overloaded version of the handleJICPPacket() method including
	   the <code>Connection</code> the incoming JICPPacket was received
	   from. This information is important since, unlike normal mediators,
	   a NIOMediator may not read packets from connections on its own (the
	   JICPMediatorManager does that in general).
	 */
	public JICPPacket handleJICPPacket(Connection c, JICPPacket pkt, InetAddress addr, int port) throws ICPException {
		checkTerminatedInfo(pkt);
		
		// Update keep-alive info
		lastReceivedTime = System.currentTimeMillis();
		
		JICPPacket reply = null;
		byte type = pkt.getType();
		switch (type) {
		case JICPProtocol.DROP_DOWN_TYPE:
			myLogger.log(Logger.INFO, "BE "+myID+" - DROP_DOWN received: "+pkt.getSessionID());
			// Note that the return packet is written inside the handleDropDown() 
			// method since the connection must be closed after the response has 
			// been sent back.
			handleDropDown(c, pkt, addr, port);
			break;
		case JICPProtocol.COMMAND_TYPE:
			myLogger.log(Logger.INFO, "BE "+myID+" - COMMAND received: "+pkt.getSessionID());
			if (peerActive) {
				reply = outManager.handleCommand(pkt);
			}
			else {
				// The remote FrontEnd has terminated spontaneously -->
				// Kill the above container (this will also kill this NIOBEDispatcher).
				kill();
			}
			break;
		case JICPProtocol.KEEP_ALIVE_TYPE:
			myLogger.log(Logger.INFO, "BE "+myID+" - KEEP_ALIVE received");
			reply = outManager.handleKeepAlive(pkt);
			break;
		case JICPProtocol.RESPONSE_TYPE:
		case JICPProtocol.ERROR_TYPE:
			myLogger.log(Logger.INFO, "BE "+myID+" - RESPONSE/ERROR received: "+pkt.getSessionID());
			inpManager.notifyIncomingResponseReceived(pkt);
			break;
		default:
			myLogger.log(Logger.WARNING, "BE "+myID+" - Unexpected incoming packet type: "+type);
		}
		
		if (reply != null) {
			try {
				writePacket(myConnection, reply);
				myLogger.log(Logger.INFO, "BE "+myID+" - RESPONSE sent back: "+reply.getSessionID());
			}
			catch (IOException ioe) {
	      		myLogger.log(Logger.WARNING, myID+": Communication error sending back response. "+ioe);
			}
		}
		return null;
	}

	private void writePacket(Connection c, JICPPacket pkt) throws IOException {
		// This is done to ensure that commands and responses are sent to FE separately
		synchronized (writeLock) {
			c.writePacket(pkt);
		}
	}
	
	/**
	   This is periodically called by the JICPMediatorManager and is
	   used by this NIOMediator to evaluate the elapsed time without
	   the need of a dedicated thread or timer.
	 */
	public final void tick(long currentTime) {
		if (myLogger.isLoggable(Logger.FINE)) {
			myLogger.log(Logger.FINE,  myID+": Tick.");
		}
		if (active) {
			if (!connectionDropped) {
				// 1) Evaluate the keep alive
				if (keepAliveTime > 0 && myConnection != null) {
					if ((currentTime - lastReceivedTime) > (keepAliveTime + responseTimeoutOffset)) {
						// Missing keep-alive.
						myLogger.log(Logger.WARNING,  myID+": Missing keep-alive."); 
						handleConnectionError(myConnection, null);
					}
				}
				
				// 2) Evaluate the max disconnection time
				if (checkMaxDisconnectionTime(currentTime)) {
					myLogger.log(Logger.WARNING,  myID+": Max disconnection time expired. FrontEnd is likely dead --> Close BackEnd"); 
					// Consider as if the FrontEnd has terminated spontaneously -->
					// Kill the above container (this will also kill this BackEndDispatcher).
					kill();
				}
			}
			else {
				// If the FE stays dropped for to much time (1h), very likely it dead  --> kill the above container
				if ((currentTime - dropTimeStamp) > 3600000) {
					myLogger.log(Logger.WARNING,  myID+": Max drop-down time expired. FrontEnd is likely dead --> Close BackEnd"); 
					kill();
				}
			}
		}
	}
	
	
	////////////////////////////////////////////////
	// BEConnectionManager interface implementation
	////////////////////////////////////////////////
	/**
	   Return a stub of the remote FrontEnd that is connected to the local BackEnd.
	   @param be The local BackEnd
	   @param props Additional (implementation dependent) connection configuration properties.
	   @return A stub of the remote FrontEnd.
	 */
	public FrontEnd getFrontEnd(BackEnd be, Properties props) throws IMTPException {
		return inpManager.getStub();
	}
	
	/**
	   Make this BackEndDispatcher terminate.
	 */
	public void shutdown() {
		active = false;
		if(myLogger.isLoggable(Logger.INFO)) {
			myLogger.log(Logger.INFO, myID+": shutting down");
		}
		
		// Deregister from the JICPServer
		if (myID != null) {
			myMediatorManager.deregisterMediator(myID);
		}
		
		inpManager.shutdown();
		outManager.shutdown();
	}
	
	
	//////////////////////////////////////////
	// Dispatcher interface implementation
	//////////////////////////////////////////
	public synchronized byte[] dispatch(byte[] payload, boolean flush, int oldSessionId) throws ICPException {
		if (connectionDropped) {
			// Move from DROPPED state to DISCONNECTED state and wait 
			// for the FE to reconnect
			droppedToDisconnected();
			throw new ICPException("Connection dropped");
		}
		else {
			// Normal dispatch
			JICPPacket pkt = new JICPPacket(JICPProtocol.COMMAND_TYPE, JICPProtocol.DEFAULT_INFO, payload);
			pkt = inpManager.dispatch(pkt, flush, oldSessionId);
			return pkt.getData();
		}
	}
	
	
	//////////////////////////////////////////////////////
	// Methods related to connection drop-down management
	//////////////////////////////////////////////////////
	/**
	 Handle a connection DROP_DOWN request from the FE.
	 */
	protected void handleDropDown(Connection c, JICPPacket pkt, InetAddress addr, int port) {
		try {
			if (inpManager.isEmpty()) {
				JICPPacket rsp = new JICPPacket(JICPProtocol.RESPONSE_TYPE, JICPProtocol.DEFAULT_INFO, null);
				writePacket(c, rsp);
				
				// Should never happen (the inpManager would not be empty), but, in case someone was waiting for a
				// response notify it the response will never arrive
				inpManager.notifyIncomingResponseReceived(null);
				
				myConnection = null;
				updateConnectedState();
				connectionDropped = true;
				dropTimeStamp = System.currentTimeMillis();
			}
			else {
				// If we have some postponed command to flush, refuse dropping the connection
				myLogger.log(Logger.WARNING,  myID+": DROP_DOWN request refused.");
				JICPPacket rsp = new JICPPacket(JICPProtocol.ERROR_TYPE, JICPProtocol.DEFAULT_INFO, null);
				writePacket(c, rsp);
			}
		}
		catch (Exception e) {
			myLogger.log(Logger.WARNING,  myID+": Error writing DROP_DOWN response. "+e);
		}
	}
	
	/**
	 Move from the connectionDropped state to the Disconnected state.
	 This may happen when
	 - a packet must be dispatched to the FE.
	 - an incoming connection is detected
	 */
	private void droppedToDisconnected() {
		connectionDropped = false;
		setExpirationDeadline();
		requestRefresh();
	}
	
	/**
	   Request the FE to refresh the connection.
	   This default implementation does nothing. Subclasses may redefine this method to exploit some
	   application specific out-of-band channel
	 */
	protected void requestRefresh() {
	}
	
	public synchronized boolean isConnected() {
		return myConnection != null;
	}
	
	private void updateConnectedState() {
		myProperties.put(BEManagementHelper.CONNECTED, (isConnected() ? "true" : "false"));
	}
	
	
	/**
	   Inner class InputManager.
	   This class manages the delivery of commands to the FrontEnd
	 */
	protected class InputManager {
		private boolean dispatching = false;
		private boolean waitingForFlush;
		private JICPPacket lastIncomingResponse;
		
		private int inpCnt;
		private FrontEndStub myStub;
		
		InputManager(int c, FrontEndStub s) {
			inpCnt = c;
			myStub = s;
		}
		
		FrontEndStub getStub() {
			return myStub;
		}
		
		Thread prepareFlush() {
			Thread flusher = myStub.checkFlush();
			waitingForFlush = flusher != null;
			return flusher;
		}
		
		void abortFlush() {
			myStub.endFlush();
		}
		
		final boolean isEmpty() {
			// We are empty if we are not dispatching a JICPPacket and our stub 
			// has no postponed commands waiting to be delivered.
			return (!dispatching) && myStub.isEmpty();
		}
		
		void shutdown() {
			// In case someone was waiting for a response notify it the response will never arrive
			notifyIncomingResponseReceived(null);
		}
		
		/**
		   Dispatch a JICP command to the FE and get back a reply.
		*/
		final JICPPacket dispatch(JICPPacket pkt, boolean flush, int oldSessionId) throws ICPException {
			dispatching = true;
			try {
				if (active && isConnected()) { 
					if (waitingForFlush && !flush) {
						throw new ICPException("Upsetting dispatching order");
					}
					waitingForFlush = false;
				
					if (flush && oldSessionId != -1) {
						// This is a postponed command whose previous dispatch failed --> Use the
						// old sessionId, so that if the server already received it (previous dispatch 
						// failed due to a response delivering error) the command will be recognized 
						// as duplicated and properly managed
						inpCnt = oldSessionId;
					}
					pkt.setSessionID((byte) inpCnt);
					try {
						lastIncomingResponse = null;
						
						if(myLogger.isLoggable(Logger.FINE)) {
							myLogger.log(Logger.FINE, "[Thread="+Thread.currentThread().getName()+"] BE "+myID+" - Sending command to FE "+pkt.getSessionID());
						}
						writePacket(myConnection, pkt);
						if(myLogger.isLoggable(Logger.FINE)) {
							myLogger.log(Logger.FINE, "[Thread="+Thread.currentThread().getName()+"] BE "+myID+" - Waiting for response from FE "+pkt.getSessionID());
						}
						
						long responseTimeout = responseTimeoutOffset + (long)(responseTimeoutMultiplicativeFactor * pkt.getLength());
						pkt = waitForResponse(inpCnt, responseTimeout);
						if (pkt != null) {
							if(myLogger.isLoggable(Logger.FINE)) {
								myLogger.log(Logger.FINE, "[Thread="+Thread.currentThread().getName()+"] BE "+myID+" - Response received from FE "+pkt.getSessionID());
							}
							if (pkt.getType() == JICPProtocol.ERROR_TYPE) {
								// Communication OK, but there was a JICP error on the peer
								throw new ICPException(new String(pkt.getData()));
							}
							if (!peerActive) {
								// This is the response to an exit command --> Suicide, without
								// killing the above container since it is already dying. 
								BackEndDispatcher.this.shutdown();
							}
							return pkt;
						}
						else {
							myLogger.log(Logger.WARNING, "[Thread="+Thread.currentThread().getName()+"] BE "+myID+": Response timeout expired");
							handleConnectionError(myConnection, null);
							throw new ICPDispatchException("Response timeout expired", inpCnt);
						}
					}
					catch (IOException ioe) {
						// There was an IO exception writing data to the connection
						// --> reset the connection.
						myLogger.log(Logger.WARNING, "[Thread="+Thread.currentThread().getName()+"] BE "+myID+": "+ioe);
						handleConnectionError(myConnection, ioe);
						throw new ICPDispatchException("Dispatching error.", ioe, inpCnt);
					}
					finally {
						inpCnt = (inpCnt+1) & 0x0f;
					}
				}
				else {
					throw new ICPException("Unreachable");
				}
			}
			finally {
				dispatching = false;
			}
		}
		
		private synchronized JICPPacket waitForResponse(int sessionID, long timeout) {
			try {
				while (lastIncomingResponse == null ) {
					wait(timeout);
					if (lastIncomingResponse != null && lastIncomingResponse.getSessionID() != sessionID) {
						myLogger.log(Logger.WARNING, myID+": Duplicated response from FE: type="+lastIncomingResponse.getType()+" info="+lastIncomingResponse.getInfo()+" SID="+lastIncomingResponse.getSessionID());
						// Go back waiting
						lastIncomingResponse = null;
						continue;
					}
					break;
				}
			}
			catch (Exception e) {}
			return lastIncomingResponse;
		}
		
		private synchronized void notifyIncomingResponseReceived(JICPPacket rsp) {
			lastIncomingResponse = rsp;
			notifyAll();
		}
	} // END of inner class InputManager
	
	
	/**
	   Inner class OutputManager
	   This class manages the reception of commands and keep-alive
	   packets from the FrontEnd.
	 */
	protected class OutputManager {
		private JICPPacket lastResponse;
		private int lastSid;
		private BackEndSkel mySkel;
		
		OutputManager(int n, BackEndSkel s) {
			lastSid = n;
			mySkel = s;
		}
		
		void shutdown() {
		}
		
		final JICPPacket handleCommand(JICPPacket cmd) throws ICPException {
			JICPPacket reply = null;
			byte sid = cmd.getSessionID();
			if (sid == lastSid && lastResponse != null) {
				myLogger.log(Logger.WARNING, myID+": Duplicated packet from FE: pkt-type="+cmd.getType()+" info="+cmd.getInfo()+" SID="+sid);
				reply = lastResponse;
			}
			else {
				if(myLogger.isLoggable(Logger.FINE)) {
					myLogger.log(Logger.FINE, myID+": Received command "+sid+" from FE");
				}
				
				byte[] rspData = mySkel.handleCommand(cmd.getData());
				if(myLogger.isLoggable(Logger.FINER)) {
					myLogger.log(Logger.FINER, myID+": Command "+sid+" from FE served ");
				}
				
				reply = new JICPPacket(JICPProtocol.RESPONSE_TYPE, JICPProtocol.DEFAULT_INFO, rspData);
				reply.setSessionID(sid);
				lastSid = sid;
				lastResponse = reply;
			}
			return reply;
		}
		
		final JICPPacket handleKeepAlive(JICPPacket command) throws ICPException {
			if(myLogger.isLoggable(Logger.FINEST)) {
				myLogger.log(Logger.FINEST,myID+": Keep-alive received");
			}
			return new JICPPacket(JICPProtocol.RESPONSE_TYPE, JICPProtocol.DEFAULT_INFO, null);
		}
	} // END of inner class OutputManager
	
	
	private synchronized final void setExpirationDeadline() {
		expirationDeadline = System.currentTimeMillis() + maxDisconnectionTime;
	}
	
	private synchronized final boolean checkMaxDisconnectionTime(long currentTime) {
		return (!isConnected()) && (currentTime > expirationDeadline);
	}  		
	
	private final boolean checkTerminatedInfo(JICPPacket pkt) {
		if ((pkt.getInfo() & JICPProtocol.TERMINATED_INFO) != 0) {
			// In some mysterious cases we receive dirty data from the network 
			// If the second byte has bit 7 = 1, we may confuse such dirty data with a 
			// termination packet --> Check that the packet is valid
			int type = pkt.getType();
			if (type == JICPProtocol.COMMAND_TYPE || type == JICPProtocol.RESPONSE_TYPE || type == JICPProtocol.CONNECT_MEDIATOR_TYPE) {
				peerActive = false;
				if (myLogger.isLoggable(Logger.INFO)) {
					myLogger.log(Logger.INFO, myID+": Peer termination notification received");
				}
				if (pkt.getType() == JICPProtocol.COMMAND_TYPE) {
					// Spontaneous FE termination. Unblock any Thread waiting for a response. It will behave as if the response timeout was expired
					inpManager.notifyIncomingResponseReceived(null);
				}
			}
		}
		return peerActive;
	}
}

