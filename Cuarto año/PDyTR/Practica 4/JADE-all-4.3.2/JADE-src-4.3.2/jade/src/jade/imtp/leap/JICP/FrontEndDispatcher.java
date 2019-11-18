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

package jade.imtp.leap.JICP;

import jade.core.FEConnectionManager;
import jade.core.FrontEnd;
import jade.core.BackEnd;
import jade.core.IMTPException;
import jade.core.MicroRuntime;
import jade.core.Specifier;
import jade.core.Timer;
import jade.core.TimerListener;
import jade.core.TimerDispatcher;
import jade.mtp.TransportAddress;
import jade.imtp.leap.BackEndStub;
import jade.imtp.leap.ConnectionDropped;
import jade.imtp.leap.ICPDispatchException;
import jade.imtp.leap.MicroSkeleton;
import jade.imtp.leap.FrontEndSkel;
import jade.imtp.leap.Dispatcher;
import jade.imtp.leap.ICPException;
import jade.imtp.leap.ConnectionListener;
import jade.util.Logger;
import jade.util.leap.Properties;

import java.io.*;
import java.util.Vector;

/**
 * Single full-duplex connection based Front End side dispatcher class
 * @author Giovanni Caire - TILAB
 */
public class FrontEndDispatcher implements FEConnectionManager, Dispatcher, TimerListener, Runnable {
	private static final int KEEP_ALIVE_RESPONSE_TIMEOUT = 10000;

	private MicroSkeleton mySkel = null;
	private BackEndStub myStub = null;

	protected String myMediatorClass = "jade.imtp.leap.nio.BackEndDispatcher";

	// Variables related to the connection with the Mediator
	private Properties myProperties;
	private String[] backEndAddresses;
	private TransportAddress mediatorTA;
	private String myMediatorID;
	private int creationAttempts = JICPProtocol.DEFAULT_CREATION_ATTEMPTS;
	private long retryTime = JICPProtocol.DEFAULT_RETRY_TIME;
	private long maxDisconnectionTime = JICPProtocol.DEFAULT_MAX_DISCONNECTION_TIME;
	private long keepAliveTime = JICPProtocol.DEFAULT_KEEP_ALIVE_TIME;
	private long connectionDropDownTime = -1;

	private Timer kaTimer, cdTimer;
	// Lock used to synchronize sections managing timers for KEEP_ALIVE and DROP_DOWN. 
	// Such sections never contain possibly blocking code --> Cannot cause deadlock
	private Object timersLock = new Object(); 

	private IncomingCommandServer myCommandServer;
	private ConnectionReader myConnectionReader;
	private Connection myConnection = null;
	public boolean refreshingConnection = false;
	private Object connectionLock = new Object();
	// Lock used to synchronize threads waiting for a response with the ConnectionReader thread 
	// receiving data from the network --> Cannot cause deadlock
	private Object responseLock = new Object();  
	private ConnectionListener myConnectionListener;

	private boolean active = false;
	private boolean connectionDropped = false;
	private boolean waitingForFlush = false;
	private byte lastSid = 0x0f; // SID of the last successfully processed incoming COMMAND
	private int outCnt = 0; // Counter used set SIDs of commands for the BE
	private JICPPacket lastOutgoingResponse = null;
	private Thread terminator;
	private int reconnectionAttemptCnt = 0;

	private Logger myLogger = Logger.getMyLogger(getClass().getName());


	//////////////////////////////////////////////
	// FEConnectionManager interface implementation
	//////////////////////////////////////////////

	/**
	 * Connect to a remote BackEnd and return a stub to communicate with it
	 */
	public BackEnd getBackEnd(FrontEnd fe, Properties props) throws IMTPException {
		myProperties = props;
		//manageRemoteConfig();
		myMediatorID = myProperties.getProperty(JICPProtocol.MEDIATOR_ID_KEY);
		try {
			String tmp = props.getProperty(FrontEnd.REMOTE_BACK_END_ADDRESSES);
			backEndAddresses = parseBackEndAddresses(tmp);

			// Host
			String host = props.getProperty(MicroRuntime.HOST_KEY);
			if (host == null) {
				host = "localhost";
			}

			// Port
			int port = JICPProtocol.DEFAULT_PORT;
			try {
				port = Integer.parseInt(props.getProperty(MicroRuntime.PORT_KEY));
			} catch (NumberFormatException nfe) {
				// Use default
			}

			// Compose URL 
			mediatorTA = JICPProtocol.getInstance().buildAddress(host, String.valueOf(port), null, null);

			mediatorTA = JICPProtocol.getInstance().buildAddress(host, String.valueOf(port), null, null);
			if (myLogger.isLoggable(Logger.CONFIG)) {
				myLogger.log(Logger.CONFIG, "Remote URL="+JICPProtocol.getInstance().addrToString(mediatorTA));
			}

			// Mediator class
			tmp = props.getProperty(JICPProtocol.MEDIATOR_CLASS_KEY);
			if (tmp != null) {
				myMediatorClass = tmp;
			}
			else{
				//set the default mediator class.
				props.setProperty(JICPProtocol.MEDIATOR_CLASS_KEY, myMediatorClass);
			}
			if (myLogger.isLoggable(Logger.CONFIG)) {
				myLogger.log(Logger.CONFIG, "Mediator class="+myMediatorClass);
			}

			// Creation attempts
			tmp = props.getProperty(JICPProtocol.CREATION_ATTEMPTS_KEY);
			try {
				creationAttempts = Integer.parseInt(tmp);
			} catch (Exception e) {
				// Use default
			}
			if (myLogger.isLoggable(Logger.CONFIG)) {
				myLogger.log(Logger.CONFIG, "Creation attempts="+creationAttempts);
			}

			// (re)connection retry time
			tmp = props.getProperty(JICPProtocol.RECONNECTION_RETRY_TIME_KEY);
			try {
				retryTime = Long.parseLong(tmp);
			} catch (Exception e) {
				// Use default
			}
			if (myLogger.isLoggable(Logger.CONFIG)) {
				myLogger.log(Logger.CONFIG, "Reconnection time="+retryTime);
			}

			// Max disconnection time
			tmp = props.getProperty(JICPProtocol.MAX_DISCONNECTION_TIME_KEY);
			try {
				maxDisconnectionTime = Long.parseLong(tmp);
			} catch (Exception e) {
				// Use default
			}
			if (myLogger.isLoggable(Logger.CONFIG)) {
				myLogger.log(Logger.CONFIG, "Max discon. time="+maxDisconnectionTime);
			}

			// Keep-alive time 
			tmp = props.getProperty(JICPProtocol.KEEP_ALIVE_TIME_KEY);
			try {
				keepAliveTime = Long.parseLong(tmp);
			} 
			catch (Exception e) {
				// Use default
				props.setProperty(JICPProtocol.KEEP_ALIVE_TIME_KEY, String.valueOf(keepAliveTime));
			}
			if (myLogger.isLoggable(Logger.CONFIG)) {
				myLogger.log(Logger.CONFIG, "Keep-alive time="+keepAliveTime);
			}

			// Connection-drop-down time 
			tmp = props.getProperty(JICPProtocol.DROP_DOWN_TIME_KEY);
			try {
				connectionDropDownTime = Long.parseLong(tmp);
			} 
			catch (Exception e) {
				// Use default
			}
			if (myLogger.isLoggable(Logger.CONFIG)) {
				myLogger.log(Logger.CONFIG, "Connection-drop-down time="+connectionDropDownTime);
			}

			// Retrieve the ConnectionListener if any
			try {
				Object obj = props.get("connection-listener");
				if (obj instanceof ConnectionListener) {
					myConnectionListener = (ConnectionListener) obj;
				}
				else {
					myConnectionListener = (ConnectionListener) Class.forName(obj.toString()).newInstance();
				}
			}
			catch (Exception e) {
				// Just ignore it
			}

			// Create the BackEnd in the fixed network
			mySkel = new FrontEndSkel(fe);
			myStub = new BackEndStub(this, props);

			// Set active to true before creating the back-end. This is because in this phase 
			// we may receive a SYNCH command and the CommandServer must be there to process it
			active = true; 
			Connection c = createBackEnd();
			
			updateTimers();
			startConnectionReader(c);

			return myStub;
		} 
		catch (ICPException icpe) {
			throw new IMTPException("Connection error", icpe);
		}
	}
	
	/*private void manageRemoteConfig() throws IMTPException {
		String remoteConfigHost = myProperties.getProperty(MicroRuntime.REMOTE_CONFIG_HOST_KEY);
		String remoteConfigPort = myProperties.getProperty(MicroRuntime.REMOTE_CONFIG_PORT_KEY);
		if (remoteConfigHost != null && remoteConfigPort != null) {
			// Remote configuration options specified: Retrieve connectivity related
			// configurations from the indicated host and port
			JICPConnection c = getConnection(new JICPAddress(remoteConfigHost, remoteConfigPort, myMediatorID, ""));
			JICPPacket pkt = new JICPPacket(JICPProtocol.GET_CONFIG_OPTIONS_TYPE, JICPProtocol.DEFAULT_INFO, null);
			writePacket(pkt, c);
			pkt = c.readPacket();
			c.close();
			...
			String replyMsg = new String(pkt.getData());
			if (pkt.getType() != JICPProtocol.ERROR_TYPE) {
				// BackEnd creation successful
				BackEndStub.parseCreateMediatorResponse(replyMsg, myProperties);

		}
	}*/

	/**
	   Send the CREATE_MEDIATOR command with the necessary parameter
	   in order to create the BackEnd in the fixed network.
	   Executed 
	   - at bootstrap time by the thread that creates the FrontEndContainer. 
	   - To re-attach to the platform after a fault of the BackEnd
	 */
	private JICPConnection createBackEnd() throws IMTPException {
		StringBuffer sb = BackEndStub.encodeCreateMediatorRequest(myProperties);
		if (myMediatorID != null) {
			// This is a request to re-create my expired back-end
			BackEndStub.appendProp(sb, JICPProtocol.MEDIATOR_ID_KEY, myMediatorID);
			BackEndStub.appendProp(sb, "outcnt", String.valueOf(outCnt));
			BackEndStub.appendProp(sb, "lastsid", String.valueOf(lastSid));
		}
		JICPPacket pkt = new JICPPacket(JICPProtocol.CREATE_MEDIATOR_TYPE, JICPProtocol.DEFAULT_INFO, null, sb.toString().getBytes());

		// Try first with the current transport address, then with the various backup addresses
		for(int i = -1; i < backEndAddresses.length; i++) {

			if (i >= 0) {
				// Set the mediator address to a new address..
				String addr = backEndAddresses[i];
				int colonPos = addr.indexOf(':');
				String host = addr.substring(0, colonPos);
				String port = addr.substring(colonPos + 1, addr.length());
				mediatorTA = new JICPAddress(host, port, myMediatorID, "");
			}

			for (int k = 0; k < creationAttempts; k++) {
				try {
					JICPConnection c = create(pkt);
					// If requested, initialize the SERVER_TIME_OFFSET property 
					if ("true".equals(myProperties.getProperty(JICPProtocol.GET_SERVER_TIME_KEY))) {
						initServerTimeOffset(c);
					}
					return c;
				}
				catch (IOException ioe) {
					// Connection error --> Retry 5 times then move to next address
					myLogger.log(Logger.WARNING, "Connection error. "+ioe.toString());
				}
				catch (ICPException icpe) {
					// JICP Error --> No need to try again with this address
					myLogger.log(Logger.WARNING, "BackEnd creation error: "+icpe.getMessage());
					break;
				}
			}
		}

		// No address succeeded: try to handle the problem...
		throw new IMTPException("Error creating the BackEnd.");
	}
	
	private JICPConnection create(JICPPacket createPkt) throws IOException, ICPException {
		myLogger.log(Logger.INFO, "Creating BackEnd on jicp://"+mediatorTA.getHost()+":"+mediatorTA.getPort());				
		JICPConnection con = openConnection(mediatorTA);

		writePacket(createPkt, con);
		JICPPacket createRsp = con.readPacket();

		String replyMsg = new String(createRsp.getData());
		if (createRsp.getType() != JICPProtocol.ERROR_TYPE) {
			// BackEnd creation successful
			BackEndStub.parseCreateMediatorResponse(replyMsg, myProperties);
			myMediatorID = myProperties.getProperty(JICPProtocol.MEDIATOR_ID_KEY);
			// Complete the mediator address with the mediator ID
			mediatorTA = new JICPAddress(mediatorTA.getHost(), mediatorTA.getPort(), myMediatorID, null);
			myLogger.log(Logger.INFO, "BackEnd creation OK: mediator-id = "+myMediatorID);
			return con;	      
		}
		else {
			// JICP Error
			if (myConnectionListener != null && replyMsg != null && replyMsg.startsWith(JICPProtocol.NOT_AUTHORIZED_ERROR)) {
				myConnectionListener.handleConnectionEvent(ConnectionListener.NOT_AUTHORIZED, replyMsg);
			}
			throw new ICPException(replyMsg);
		}
	}

	private String[] parseBackEndAddresses(String addressesText) {
		Vector addrs = Specifier.parseList(addressesText, ';');
		// Convert the list into an array of strings
		String[] result = new String[addrs.size()];
		for(int i = 0; i < result.length; i++) {
			result[i] = (String)addrs.elementAt(i);
		}
		return result;
	}
	
	private void initServerTimeOffset(Connection con) {
		JICPPacket pkt = new JICPPacket(JICPProtocol.GET_SERVER_TIME_TYPE, JICPProtocol.DEFAULT_INFO, null);
		
		try {
			long start = System.currentTimeMillis();
			writePacket(pkt, con);
			JICPPacket rsp = con.readPacket();
			long now = System.currentTimeMillis();
			long elapsed =  now - start;
	
			if (rsp.getType() != JICPProtocol.ERROR_TYPE) {
				String rspDataStr = new String(rsp.getData());
				// This assumes the time to deliver the GET_SERVER_TIME packet to the server 
				// is more or less the same as that to deliver the response back to the client
				long uncertanty = elapsed / 2;
				long serverTime = Long.parseLong(rspDataStr) + uncertanty;
				long offset = serverTime - now;
				myProperties.put(JICPProtocol.SERVER_TIME_OFFSET_KEY, new Long(offset));
				myLogger.log(Logger.INFO, "Server time initialized: value="+serverTime+", offset="+offset+", uncertanty= +/- "+uncertanty);
			}
			else {
				myLogger.log(Logger.WARNING, "Error response received retrieving server time");
			}
		}
		catch (Exception e) {
			myLogger.log(Logger.WARNING, "Error retrieving server time", e);
		}
	}

	/**
	   Make this FrontEndDispatcher terminate.
	 */
	public synchronized void shutdown() {
		if (active) {
			active = false;
			clearTimers();

			terminator = Thread.currentThread();
			if (terminator != myCommandServer) {
				// This is a self-initiated shut down --> we must explicitly
				// notify the BackEnd.
				JICPPacket terminationPacket = null;
				try {
					if (connectionDropped) {
						myConnection = openConnection(mediatorTA);
						terminationPacket = new JICPPacket(JICPProtocol.CONNECT_MEDIATOR_TYPE, JICPProtocol.TERMINATED_INFO, mediatorTA.getFile(), null);
					}
					else {
						terminationPacket = new JICPPacket(JICPProtocol.COMMAND_TYPE, JICPProtocol.TERMINATED_INFO, null);
					}

					if (myConnection != null) {
						myLogger.log(Logger.INFO, "Sending termination notification");
						writePacket(terminationPacket, myConnection);
						myConnection.close();
					}
				}
				catch (Exception e) {
					// Ignore any exception
				}
			}
		}
	}

	//////////////////////////////////////////////
	// Dispatcher interface implementation
	//////////////////////////////////////////////
	/**
	 Deliver a serialized command to the BackEnd.
	 @return The serialized response
	 */
	public synchronized byte[] dispatch(byte[] payload, boolean flush, int oldSessionId) throws ICPException {
		if (connectionDropped) {
			myLogger.log(Logger.INFO, myMediatorID+" - Dispatching with connection dropped. Reconnecting...");
			undrop();
		}

		if (myConnection != null) {
			if (waitingForFlush && !flush) {
				throw new ICPException("Upsetting dispatching order [4]");
			}
			waitingForFlush = false;
			
			JICPPacket pkt = new JICPPacket(JICPProtocol.COMMAND_TYPE, JICPProtocol.DEFAULT_INFO, payload);
			if (flush && oldSessionId != -1) {
				// This is a postponed command whose previous dispatch failed --> Use the
				// old sessionId, so that if the server already received it (previous dispatch 
				// failed due to a response delivering error) the command will be recognized 
				// as duplicated and properly managed
				outCnt = oldSessionId;
			}
			pkt.setSessionID((byte) outCnt);
			myLogger.log(Logger.INFO, myMediatorID+" - Issuing outgoing command "+outCnt);
			try {
				lastOutgoingResponse = null;
				clearTimers();
				writePacket(pkt, myConnection);
				myLogger.log(Logger.INFO, myMediatorID+" - Waiting for response, SID = "+pkt.getSessionID());
				JICPPacket response = waitForResponse(outCnt, JICPProtocol.DEFAULT_RESPONSE_TIMEOUT_OFFSET);
				if (response != null) {
					updateTimers();
					//System.out.println("Response received from BE "+response.getSessionID());
					if (myLogger.isLoggable(Logger.INFO)) {
						myLogger.log(Logger.INFO, myMediatorID+" - Response received "+response.getSessionID());
					}
					if (response.getType() == JICPProtocol.ERROR_TYPE) {
						// Communication OK, but there was a JICP error on the peer
						throw new ICPException(new String(response.getData()));
					}
					return response.getData();
				}
				else {
					myLogger.log(Logger.WARNING, myMediatorID+" - Response timeout expired. SID = "+pkt.getSessionID());
					handleDisconnection();
					throw new ICPDispatchException("Response timeout expired [2]", pkt.getSessionID());
				}
			}
			catch (IOException ioe) {
				// Can't reach the BackEnd. 
				myLogger.log(Logger.WARNING, myMediatorID+" - Error writing command. SID = "+pkt.getSessionID(), ioe);
				handleDisconnection();
				throw new ICPDispatchException("I/O error [1]", ioe, pkt.getSessionID());
			}
			finally {
				outCnt = (outCnt+1) & 0x0f;
			}
		}
		else {
			reconnectionAttemptCnt = 0;
			if (!refreshingConnection) {
				// The previous attempt to reconnect failed. Activate the reconnection mechanism again 
				handleDisconnection();
			}
			throw new ICPException("Unreachable [3]");
		}
	} 


	//////////////////////////////////////////////////
	// Connection reader 
	//////////////////////////////////////////////////
	private void startConnectionReader(Connection c) {
		myConnection = c;
		myConnectionReader = new ConnectionReader(myConnection);
		myConnectionReader.start();
	}

	// This variable is only used within the ConnectionReader class,
	// but is declared externally since it must "survive" when 
	// a ConnectionReader is replaced
	private int cnt = 0;

	/**
	 Inner class ConnectionReader.
	 This class is responsible for reading incoming packets (incoming commands and responses
	 to outgoing commands)
	 */
	private class ConnectionReader extends Thread {
		private int myId;
		private Connection myConnection = null;

		public ConnectionReader(Connection c) {
			super();
			myConnection = c;
			//#MIDP_EXCLUDE_BEGIN
			setName("ConnectionReader-"+myId);
			//#MIDP_EXCLUDE_END
		}

		public void run() {
			myId = cnt++;
			myLogger.log(Logger.INFO, myMediatorID+" - CR-"+myId+" started");

			try {
				while (isConnected()) {
					JICPPacket pkt = myConnection.readPacket();
					myLogger.log(Logger.INFO, myMediatorID+" - CR-"+myId+" packet received, SID="+pkt.getSessionID());

					pkt = handleIncomingPacket(pkt);
					if (pkt != null) {
						writePacket(pkt, myConnection);
					}
				}
			}
			catch (IOException ioe) {
				synchronized (connectionLock) {
					if (active && !connectionDropped) {
						myLogger.log(Logger.WARNING, myMediatorID+" - CR Exception ", ioe);
						// This synchronized check avoids that an old connection reader suddenly realizes that its connection is down
						// and tries to refresh an already restored connection
						if (this == FrontEndDispatcher.this.myConnectionReader) {
							// Unblock any Thread waiting for a response. It will behave as if the response timeout was expired
							notifyOutgoingResponseReceived(null);
							handleDisconnection();
						}
					}
				}
			}

			myLogger.log(Logger.INFO, myMediatorID+" - CR-"+myId+" terminated");
		}


		private JICPPacket handleIncomingPacket(JICPPacket pkt) {
			switch(pkt.getType()) {
			case JICPProtocol.COMMAND_TYPE:
				myLogger.log(Logger.INFO, myMediatorID+" - CR-"+myId+" COMMAND received from BE, SID="+pkt.getSessionID());
				updateTimers();
				serveCommand(pkt);
				myLogger.log(Logger.INFO, myMediatorID+" - CR-"+myId+" Incoming command passed to asynchronous command server");
				break;
			case JICPProtocol.KEEP_ALIVE_TYPE:
				// Server-side initiated keep-alive
				updateKeepAlive();
				return handleIncomingKeepAlive(pkt);
			case JICPProtocol.RESPONSE_TYPE:
			case JICPProtocol.ERROR_TYPE:
				myLogger.log(Logger.INFO, myMediatorID+" - CR-"+myId+" RESPONSE/ERROR received from BE. "+pkt.getSessionID());
				notifyOutgoingResponseReceived(pkt);
				break;
			default:
				myLogger.log(Logger.WARNING, myMediatorID+" - Unexpected incoming packet type: "+pkt.getType());
			}
			return null;
		}

		private final boolean isConnected() {
			return myConnection != null;
		}
	} // END of inner class ConnectionReader

	private JICPPacket handleIncomingCommand(JICPPacket cmd) {
		// Incoming command
		if (myLogger.isLoggable(Logger.FINE)) {
			myLogger.log(Logger.FINE, "Incoming command received "+cmd.getSessionID());
		}
		byte[] rspData = mySkel.handleCommand(cmd.getData());
		if (myLogger.isLoggable(Logger.FINER)) {
			myLogger.log(Logger.FINER, "Incoming command served "+ cmd.getSessionID());
		}
		return new JICPPacket(JICPProtocol.RESPONSE_TYPE, JICPProtocol.DEFAULT_INFO, rspData);
	}

	private JICPPacket handleIncomingKeepAlive(JICPPacket ka) {
		return new JICPPacket(JICPProtocol.RESPONSE_TYPE, JICPProtocol.DEFAULT_INFO, null);
	}

	private JICPConnection openConnection(TransportAddress ta) throws IOException {
		if (myConnectionListener != null) {
			myConnectionListener.handleConnectionEvent(ConnectionListener.BEFORE_CONNECTION,null);
		}
		JICPConnection c = getConnection(ta);
		return c;
	}

	/**
	 * subclasses may overwrite this to provide their version of a JICPConnection
	 * @param ta
	 * @return
	 * @throws IOException
	 */
	protected JICPConnection getConnection(TransportAddress ta) throws IOException {
		return new JICPConnection(ta);
	}

	// This is synchronized to be sure that commands and responses are always written in a non-overlapping way
	private synchronized void writePacket(JICPPacket pkt, Connection c) throws IOException {
		c.writePacket(pkt);
		if (Thread.currentThread() == terminator) {
			myConnection.close();
		}
		/*else {
			updateKeepAlive();
			if (pkt.getType() != JICPProtocol.KEEP_ALIVE_TYPE && pkt.getType() != JICPProtocol.DROP_DOWN_TYPE) {
				updateConnectionDropDown();
			}
		}*/
	}

	private JICPPacket waitForResponse(int sessionID, long timeout) {
		// Mutual exclusion with notifyOutgoingResponseReceived()
		synchronized(responseLock) {
			try {
				while (lastOutgoingResponse == null ) {
					responseLock.wait(timeout);
					if (lastOutgoingResponse != null && (sessionID != -1 && lastOutgoingResponse.getSessionID() != sessionID)) {
						myLogger.log(Logger.WARNING, "Wrong sessionID in response from BE: type="+lastOutgoingResponse.getType()+" info="+lastOutgoingResponse.getInfo()+" SID="+lastOutgoingResponse.getSessionID()+" while "+sessionID+" was expected.");
						// Go back waiting
						lastOutgoingResponse = null;
						continue;
					}
					break;
				}
			}
			catch (Exception e) {}
			return lastOutgoingResponse;
		}
	}

	private void notifyOutgoingResponseReceived(JICPPacket rsp) {
		// Mutual exclusion with waitForResponse()
		synchronized(responseLock) {
			lastOutgoingResponse = rsp;
			responseLock.notifyAll();
		}
	}


	///////////////////////////////////////////////////////
	// Reconnection related methods
	///////////////////////////////////////////////////////
	public void run() {
		// This is for logging purpose only
		int cnt = 0; 
		// This is used to switch between short and large interval between successive reconnection attempts. 
		// It is put back to 0 whenever a new dispatching failure occurs 
		reconnectionAttemptCnt = 0; 
		long startTime = System.currentTimeMillis();
		while (active) {
			try {
				connect(cnt);
				updateTimers();
				return;
			}
			catch (IOException ioe) {
				myLogger.log(Logger.WARNING, myMediatorID+" - Connect failed. " + ioe);
			} 
			catch (IMTPException imtpe) {
				myLogger.log(Logger.WARNING, myMediatorID+" - BE recreation failed.");
			}
			catch (ICPException icpe) {
				// NO need to try further
				handleReconnectionError("JICP Error. " + icpe.getMessage());
				return;
			}
			catch (Exception imtpe) {
				// Unexpected error. This may be due to strange situations where for instance a spurious KA response is interpreted as a CONNECT_MEDIATOR response
				myLogger.log(Logger.WARNING, myMediatorID+" - Unexpected error trying to connect", imtpe);
			}

			if ((System.currentTimeMillis() - startTime) > maxDisconnectionTime) {
				handleReconnectionError("Max disconnection time expired ("+System.currentTimeMillis()+")");
				return;
			}

			// 10 "fast" attempts to reconnect then wait a bit before trying again
			cnt++;
			reconnectionAttemptCnt++;
			if (reconnectionAttemptCnt > 10) {
				waitABit(retryTime);
			}
		}	
	}
	
	private void connect(int attemptCnt) throws IOException, IMTPException, ICPException {
		myLogger.log(Logger.INFO, myMediatorID+" - Connecting to " + mediatorTA.getHost() + ":" + mediatorTA.getPort() + " " + attemptCnt);
		Connection c = openConnection(mediatorTA);
		myLogger.log(Logger.INFO, myMediatorID+" - Connection opened");
		JICPPacket pkt = new JICPPacket(JICPProtocol.CONNECT_MEDIATOR_TYPE, JICPProtocol.DEFAULT_INFO, mediatorTA.getFile(), null);
		writePacket(pkt, c);
		myLogger.log(Logger.INFO, myMediatorID+" - Connect maediator packet written");
		pkt = c.readPacket();
		myLogger.log(Logger.INFO, myMediatorID+" - Connect maediator responce received");
		if (pkt.getType() == JICPProtocol.ERROR_TYPE) {
			String errorMsg = new String(pkt.getData());
			c.close();
			if (errorMsg.equals(JICPProtocol.NOT_FOUND_ERROR)) {
				// The JICPMediatorManager didn't find my Mediator anymore. Either 
				// there was a fault or max disconnection time expired. 
				// Try to recreate the BackEnd
				handleBENotFound();
				// Try to recreate the BE
				c = createBackEnd();  
				handleReconnection(c);
				return;
			} 
			else {
				// There was a JICP error. Abort
				throw new ICPException(errorMsg);
			}
		} 
		else {
			// Connect successful. The local-host address may have changed
			myProperties.setProperty(JICPProtocol.LOCAL_HOST_KEY, new String(pkt.getData()));
			myLogger.log(Logger.INFO, myMediatorID+" - Connect OK");
			handleReconnection(c);
			return;
		}
	}

	/**
	 * Start the asynchronous reconnection process implemented in the run() method
	 */
	private void handleDisconnection() {
		synchronized (connectionLock) {
			if (!refreshingConnection) {
				refreshingConnection = true;

				// Close the Connection if still there (It may be null due to a previous 
				if (myConnection != null) {
					try {
						myConnection.close();
					}
					catch (Exception e) {}
					myConnection = null;
					if (myConnectionListener != null) {
						myConnectionListener.handleConnectionEvent(ConnectionListener.DISCONNECTED, null);
					}
				}

				// Asynchronously try to recreate the Connection
				Thread t = new Thread(this);
				t.start();
			}
		}
	}

	protected void handleReconnection(Connection c) {
		synchronized (connectionLock) {
			startConnectionReader(c);
			refreshingConnection = false;
			waitingForFlush = myStub.flush();
			if (myConnectionListener != null) {
				myConnectionListener.handleConnectionEvent(ConnectionListener.RECONNECTED, null);
			}
		}
	}

	private void handleReconnectionError(String msg) {
		myLogger.log(Logger.SEVERE, "Can't reconnect: "+msg);

		refreshingConnection = false;
		if (myConnectionListener != null) {
			myConnectionListener.handleConnectionEvent(ConnectionListener.RECONNECTION_FAILURE, null);
		}
	}

	private void handleBENotFound() {
		if (myConnectionListener != null) {
			myConnectionListener.handleConnectionEvent(ConnectionListener.BE_NOT_FOUND, null);
		}
	}

	private void waitABit(long period) {
		try {
			Thread.sleep(period);
		} catch (Exception e) {}
	}


	////////////////////////////////////////////////////////////////
	// Keep-alive and connection drop-down mechanism management
	////////////////////////////////////////////////////////////////
	private void updateTimers() {
		updateKeepAlive();
		updateConnectionDropDown();
	}
	
	/**
	 * Clear the keep-alive and drop-down timers.
	 * Mutual exclusion with updateKeepAlive(), updateConnectionDropDown() and doTimeOut()
	 */
	private void clearTimers() {
		synchronized (timersLock) { 
			TimerDispatcher td = TimerDispatcher.getTimerDispatcher();
			if (kaTimer != null) {
				td.remove(kaTimer);
			}
			if (cdTimer != null) {
				td.remove(cdTimer);
			}
		}
	}

	/**
	 * Refresh the keep-alive timer.
	 * Mutual exclusion with clearTimers(), updateConnectionDropDown() and doTimeOut()
	 */
	private void updateKeepAlive() {
		synchronized (timersLock) {
			if (keepAliveTime > 0) {
				TimerDispatcher td = TimerDispatcher.getTimerDispatcher();
				if (kaTimer != null) {
					td.remove(kaTimer);
				}
				kaTimer = td.add(new Timer(System.currentTimeMillis()+keepAliveTime, this));
			}
		}
	}

	/**
	 * Refresh the connection drop-down timer.
	 * Mutual exclusion with clearTimers(), updateKeepAlive() and doTimeOut()
	 */
	private void updateConnectionDropDown() {
		synchronized (timersLock) {
			if (connectionDropDownTime > 0) {
				TimerDispatcher td = TimerDispatcher.getTimerDispatcher();
				if (cdTimer != null) {
					td.remove(cdTimer);
				}
				cdTimer = td.add(new Timer(System.currentTimeMillis()+connectionDropDownTime, this));
			}
		}
	}

	/**
	 * Mutual exclusion with updateKeepAlive(), updateConnectionDropDown() and clearTimers()
	 */	
	public void doTimeOut(final Timer t) {
		synchronized (timersLock) {
			if (t == kaTimer) {
				// Send KEEP_ALIVE may take time --> Do it in a dedicated Thread 
				Thread thr = new Thread() {
					public void run() {
						sendKeepAlive(t);
					}
				};
				thr.start();
			}
			else if (t == cdTimer) {
				// Send DROP_DOWN may take time --> Do it in a dedicated Thread 
				Thread thr = new Thread() {
					public void run() {
						dropDownConnection(t);
					}
				};
				thr.start();
			}
		}
	}

	/**
	 * Send a KEEP_ALIVE packet to the BE.
	 * Mutual exclusion with dispatch() and dropDownConnection().
	 */
	private synchronized void sendKeepAlive(Timer t) {
		if (t != kaTimer) {
			// Some data was exchanged just after t was expired --> just do nothing
			// NOTE: See comment in dropDownConnection(). In this case the point is even 
			// less important since in the worst case we send an un-necessary KA 
			return;
		}
		
		if (myConnection != null && !connectionDropped) {
			JICPPacket pkt = new JICPPacket(JICPProtocol.KEEP_ALIVE_TYPE, JICPProtocol.DEFAULT_INFO, null);
			try {
				if (myLogger.isLoggable(Logger.INFO)) {
					myLogger.log(Logger.INFO, myMediatorID+" - Writing KA.");
				}
				lastOutgoingResponse = null;
				writePacket(pkt, myConnection);
				JICPPacket rsp = waitForResponse(-1, KEEP_ALIVE_RESPONSE_TIMEOUT);
				if (rsp != null) {
					myLogger.log(Logger.INFO, myMediatorID+" - KA response received");
					updateKeepAlive();
				}
				else {
					myLogger.log(Logger.WARNING, myMediatorID+" - KA Response timeout expired.");
					handleDisconnection();
				}
			}
			catch (Exception e) {
				// Can't reach the BackEnd. 
				myLogger.log(Logger.WARNING, myMediatorID+" - Error writing KA", e);
				handleDisconnection();
			}
		}
	}  

	/**
	 * Send a DROP_DOWN packet to the BE and close the connection.
	 * Mutual exclusion with dispatch() and sendKeepAlive().
	 */
	private synchronized void dropDownConnection(Timer t) {
		if (t != cdTimer) {
			// Some data was exchanged just after t was expired --> Do nothing!
			// NOTE: This check is important for data sent by the FE to the BE since:
			// - Data sent 1 sec before t expiration --> Connection NOT dropped
			// - Data sent just after t expiration --> Connection dropped
			// - Data sent 1 sec after t expiration --> Connection dropped, but immediately restored --> NOT dropped
			// In such case the check works properly thanks to the fact that dropDownConnection() 
			// and dispatch() are executed in mutual exclusion.
			// On the contrary, for data sent by the BE to the FE the check may not work since 
			// the ConnectionReader is not (and must not be) synchronized. In that case however
			// the check is not important since:
			// - Data received 1 sec before t expiration --> Connection NOT dropped
			// - Data issued by BE 1 sec after t expiration --> Connection dropped (data cannot be received)
			return;
		}
		if (myConnection != null && !connectionDropped) {
			myLogger.log(Logger.INFO, "Writing DROP_DOWN request");
			JICPPacket pkt = prepareDropDownRequest();
			try {
				lastOutgoingResponse = null;
				writePacket(pkt, myConnection);
				JICPPacket rsp = waitForResponse(-1, JICPProtocol.DEFAULT_RESPONSE_TIMEOUT_OFFSET);
				myLogger.log(Logger.INFO, "DROP_DOWN response received");

				if (rsp.getType() != JICPProtocol.ERROR_TYPE) {
					// Now close the Connection
					synchronized (connectionLock) {
						try {
							myConnection.close();
						}
						catch (Exception e) {}
						myConnection = null;
						connectionDropped = true;
						if (myConnectionListener != null) {
							myConnectionListener.handleConnectionEvent(ConnectionListener.DROPPED, null);
						}
						myLogger.log(Logger.INFO, "Connection dropped");
					}
				}
				else {
					// The BE refused to drop down the connection
					myLogger.log(Logger.INFO, "DROP_DOWN refused");
				}
			}
			catch (Exception e) {
				// Can't reach the BackEnd. 
				myLogger.log(Logger.WARNING, "Exception sending DROP_DOWN request. "+e);
				handleDisconnection();
			}	  	
		}
	}

	protected JICPPacket prepareDropDownRequest() {
		return new JICPPacket(JICPProtocol.DROP_DOWN_TYPE, JICPProtocol.DEFAULT_INFO, null);
	}

	protected void undrop() throws ICPException {
		// NOTE that reconnecting inside a dispatch process would cause a
		// deadlock between dispatch and flush --> Make the MicroStub postpone 
		// the command and start the reconnection process 
		connectionDropped = false;
		handleDisconnection();
		throw new ConnectionDropped();		
	}	


	//////////////////////////////////////////////////////////////////
	// Asynchronous command serving part
	//////////////////////////////////////////////////////////////////
	private void serveCommand(JICPPacket command) {
		if (myCommandServer == null) {
			myCommandServer = new IncomingCommandServer();
			myCommandServer.start();
		}
		myCommandServer.serve(command);
	}

	/**
	 * Inner class IncomingCommandServer
	 * Serving incoming commands asynchronously is necessary to support commands whose serving process 
	 * involves issuing one or more outgoing commands. If such commands were served directly by the 
	 * ConnectionReader thread, in facts, there would be no chance to get the response to triggered 
	 * outgoing commands. 
	 */
	private class IncomingCommandServer extends Thread {
		private JICPPacket currentCommand = null;
		private JICPPacket lastResponse = null;

		public IncomingCommandServer() {
			super();
			//#MIDP_EXCLUDE_BEGIN
			setName("CommandServer");
			//#MIDP_EXCLUDE_END
		}

		public synchronized void serve(JICPPacket command) {
			try {
				while (currentCommand != null) {
					wait();
				}
			}
			catch (Exception e) {}
			currentCommand = command;
			notifyAll();
		}

		public void run() {
			while (active) {
				myLogger.log(Logger.INFO, myMediatorID+" - CS Waiting for next command to serve");
				acquireCurrentCommand();

				byte sid = currentCommand.getSessionID();
				myLogger.log(Logger.INFO, myMediatorID+" - CS Start serving command, SID="+sid);
				if (sid == lastSid) {
					// Duplicated incoming packet
					myLogger.log(Logger.WARNING, myMediatorID+" - Duplicated command from BE: info="+currentCommand.getInfo()+", SID="+sid);
				}
				else {
					lastResponse = handleIncomingCommand(currentCommand);
					if (Thread.currentThread() == terminator) {
						// Attach the TERMINATED_INFO flag to the response
						lastResponse.setTerminatedInfo(true);
					}
					lastResponse.setSessionID(sid);
					lastSid = sid;
				}
				myLogger.log(Logger.INFO, myMediatorID+" - CS COMMAND served");
				try {
					writePacket(lastResponse, myConnection);
					myLogger.log(Logger.INFO, myMediatorID+" - CS responce sent back");
				}
				catch (Exception e) {
					myLogger.log(Logger.WARNING, myMediatorID+" - Communication error sending back response. "+e);	
				}

				releaseCurrentCommand();
			}
			myLogger.log(Logger.INFO, myMediatorID+" - CS terminated");
			myCommandServer = null;
		}

		private synchronized void acquireCurrentCommand() {
			try {
				while (currentCommand == null) {
					wait();
				}
			}
			catch (Exception e) {}
		}

		private synchronized void releaseCurrentCommand() {
			currentCommand = null;
			notifyAll();
		}
	} // END of inner class IncomingCommandServer
}
