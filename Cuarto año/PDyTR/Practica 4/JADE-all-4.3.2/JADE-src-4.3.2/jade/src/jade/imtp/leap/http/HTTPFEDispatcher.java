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

package jade.imtp.leap.http;

import jade.core.FEConnectionManager;
import jade.core.FrontEnd;
import jade.core.BackEnd;
import jade.core.IMTPException;
import jade.core.Specifier;
import jade.core.TimerDispatcher;
import jade.core.Timer;
import jade.core.TimerListener;
import jade.mtp.TransportAddress;
import jade.imtp.leap.BackEndStub;
import jade.imtp.leap.MicroSkeleton;
import jade.imtp.leap.FrontEndSkel;
import jade.imtp.leap.Dispatcher;
import jade.imtp.leap.ICPException;
import jade.imtp.leap.ConnectionListener;
import jade.imtp.leap.JICP.*;

import jade.util.leap.Properties;
import jade.util.Logger;

import java.util.Vector;
import java.io.*;

/*#MIDP_INCLUDE_BEGIN
//import javax.microedition.io.*;
#MIDP_INCLUDE_END*/

/**
   FrontEnd-side dispatcher class using JICP over HTTP as transport protocol
   @author Giovanni Caire - TILAB
 */
public class HTTPFEDispatcher implements FEConnectionManager, Dispatcher, TimerListener {

	private MicroSkeleton mySkel;
	private BackEndStub myStub;

	private Thread terminator;
	private DisconnectionManager myDisconnectionManager;
	private KeepAliveManager myKeepAliveManager;
	private InputManager myInputManager;
	private int outCnt;
	private boolean waitingForFlush = false;
	private long maxDisconnectionTime;
	private long keepAliveTime;
	private Properties props;

	private TransportAddress mediatorTA;
	private String myMediatorID;

	private String owner;

	private String beAddrsText;
	private String[] backEndAddresses;

	private ConnectionListener myConnectionListener;

	private Object connectorLock = new Object();
	private boolean locked = false;

	private int verbosity = 1;
	private Logger myLogger = Logger.getMyLogger(getClass().getName());

	protected String myMediatorClass = "jade.imtp.leap.http.HTTPBEDispatcher";

	////////////////////////////////////////////////
	// FEConnectionManager interface implementation
	////////////////////////////////////////////////

	/**
	   Create a BackEnd in the fixed network and return a stub to 
	   communicate with it
	 */
	public BackEnd getBackEnd(FrontEnd fe, Properties p) throws IMTPException {
		props = p;

		beAddrsText = props.getProperty(FrontEnd.REMOTE_BACK_END_ADDRESSES);
		backEndAddresses = parseBackEndAddresses(beAddrsText);

		// Host
		String host = props.getProperty("host");
		if (host == null) {
			host = "localhost";
		}
		// Port
		int port = JICPProtocol.DEFAULT_PORT;
		try {
			port = Integer.parseInt(props.getProperty("port"));
		}
		catch (NumberFormatException nfe) {
			// Use default
		}
		// Compose URL. Note that we build a JICPAddress just to avoid loading the HTTPAddress class.
		mediatorTA = JICPProtocol.getInstance().buildAddress(host, String.valueOf(port), null, null);

		// Mediator class
		String tmp = props.getProperty(JICPProtocol.MEDIATOR_CLASS_KEY);
		if (tmp != null) {
			myMediatorClass = tmp;
		}else{
			// Set the default mediator class since this must be propagated to the mediator.
			props.setProperty(JICPProtocol.MEDIATOR_CLASS_KEY, myMediatorClass);
		}

		// Read re-connection retry time
		long retryTime = JICPProtocol.DEFAULT_RETRY_TIME;
		try {
			retryTime = Long.parseLong(props.getProperty(JICPProtocol.RECONNECTION_RETRY_TIME_KEY));
		} 
		catch (Exception e) {
			// Use default
		} 

		// Read Max disconnection time
		maxDisconnectionTime = JICPProtocol.DEFAULT_MAX_DISCONNECTION_TIME;
		try {
			maxDisconnectionTime = Long.parseLong(props.getProperty(JICPProtocol.MAX_DISCONNECTION_TIME_KEY));
		} 
		catch (Exception e) {
			// Use default
			props.setProperty(JICPProtocol.MAX_DISCONNECTION_TIME_KEY, String.valueOf(maxDisconnectionTime));
		} 

		// Read Keep-alive time
		keepAliveTime = JICPProtocol.DEFAULT_KEEP_ALIVE_TIME;
		try {
			keepAliveTime = Long.parseLong(props.getProperty(JICPProtocol.KEEP_ALIVE_TIME_KEY));
		} 
		catch (Exception e) {
			// Use default
			props.setProperty(JICPProtocol.KEEP_ALIVE_TIME_KEY, String.valueOf(keepAliveTime));
		} 

		if (myLogger.isLoggable(Logger.CONFIG)) {
			myLogger.log(Logger.CONFIG, "Remote URL = http://"+host+":"+port);
			myLogger.log(Logger.CONFIG, "Mediator class = "+myMediatorClass);
			myLogger.log(Logger.CONFIG, "Reconnection retry time = "+retryTime);
			myLogger.log(Logger.CONFIG, "Max disconnection time = "+maxDisconnectionTime);
			myLogger.log(Logger.CONFIG, "Keep-alive time = "+keepAliveTime);
		}
		
		myDisconnectionManager = new DisconnectionManager(retryTime, maxDisconnectionTime);
		myKeepAliveManager = new KeepAliveManager(keepAliveTime);
		myInputManager = new InputManager();

		// Read the owner if any
		owner = props.getProperty("owner");

		// Create the BackEnd stub and the FrontEnd skeleton
		myStub = new BackEndStub(this, props);
		mySkel = new FrontEndSkel(fe);

		// Start the InputManager
		myInputManager.start();

		// Create the remote BackEnd
		createBackEnd();

		return myStub;
	}

	/**
     Make this HTTPFEDispatcher terminate.
     Note that when the BackEnd receives the termination notification
     (explicitly sent in case of a self-initiated shutdown or 
     attached to the response to the EXIT command), it closes the 
     input connection. The InputManager gets an exception and,
     since it has been killed, terminates.
	 */
	public void shutdown() {
		terminator = Thread.currentThread();
		myLogger.log(Logger.INFO, "Dispatcher shutting down. Self-initiated = "+(terminator != myInputManager));
		if (terminator != myInputManager) {
			// Self-initiated shut down
			// If connected, explicitly notify the BackEnd. 
			if (myDisconnectionManager.isReachable()) {
				JICPPacket pkt = new JICPPacket(JICPProtocol.COMMAND_TYPE, (byte) (JICPProtocol.DEFAULT_INFO), null);
				myLogger.log(Logger.INFO, "Pushing termination notification");
				try {
					deliver(pkt);
				}
				catch (IOException ioe) {
					// When the BackEnd receives the termination notification,
					// it just closes the connection --> we always have this exception
					myLogger.log(Logger.FINE, "BackEnd closed");
				}
			}
			// Kill the InputManager 
			myInputManager.kill();
		} 		
	}

	/**
     Send the CREATE_MEDIATOR command with the necessary parameter
     in order to create the BackEnd in the fixed network.
	   Executed 
	   - at bootstrap time by the thread that creates the FrontEndContainer. 
	   - To re-attach to the platform after a fault of the BackEnd
	 */
	private synchronized void createBackEnd() throws IMTPException {
		StringBuffer sb = BackEndStub.encodeCreateMediatorRequest(props);
		if (myMediatorID != null) {
			// This is a request to re-create my expired back-end
			BackEndStub.appendProp(sb, JICPProtocol.MEDIATOR_ID_KEY, myMediatorID);
			BackEndStub.appendProp(sb, "outcnt", String.valueOf(outCnt));
			BackEndStub.appendProp(sb, "lastsid", String.valueOf(lastSid));
		}
		JICPPacket pkt = new JICPPacket(JICPProtocol.CREATE_MEDIATOR_TYPE, JICPProtocol.DEFAULT_INFO, null, sb.toString().getBytes());

		// Try first with the current transport address, then with the various backup addresses
		for(int i = -1; i < backEndAddresses.length; i++) {

			if(i >= 0) {
				// Set the mediator address to a new address..
				String addr = backEndAddresses[i];
				int colonPos = addr.indexOf(':');
				String host = addr.substring(0, colonPos);
				String port = addr.substring(colonPos + 1, addr.length());
				mediatorTA = new JICPAddress(host, port, myMediatorID, "");
			}

			try {
				HTTPClientConnection hc = (HTTPClientConnection)getConnection(mediatorTA);
				myLogger.log(Logger.INFO, "Creating BackEnd on "+hc.getProtocol()+mediatorTA.getHost()+":"+mediatorTA.getPort());
				pkt = deliver(pkt);

				String replyMsg = new String(pkt.getData());
				if (pkt.getType() != JICPProtocol.ERROR_TYPE) {
					// BackEnd creation successful
					BackEndStub.parseCreateMediatorResponse(replyMsg, props);
					myMediatorID = props.getProperty(JICPProtocol.MEDIATOR_ID_KEY);
					// Complete the mediator address with the mediator ID
					mediatorTA = new JICPAddress(mediatorTA.getHost(), mediatorTA.getPort(), myMediatorID, null);
					myDisconnectionManager.setReachable();
					myKeepAliveManager.update();
					myLogger.log(Logger.INFO, "BackEnd OK. Mediator ID is "+myMediatorID);
					return;
				}
				else {
					myLogger.log(Logger.WARNING, "Mediator error: "+replyMsg);
				}
			}
			catch (IOException ioe) {
				// Ignore it, and try the next address...
				myLogger.log(Logger.WARNING, "Connection error", ioe);
			}
		}

		// No address succeeded!
		throw new IMTPException("Error creating the BackEnd.");
	}

	//////////////////////////////////////////////
	// Dispatcher interface implementation
	//////////////////////////////////////////////
	/**
	 * Dispatch a serialized command to the BackEnd and get back a serialized response.
	 * Mutual exclusion with itself to preserve dispatching order 
	 */
	public synchronized byte[] dispatch(byte[] payload, boolean flush, int oldSessionId) throws ICPException {
		// Note that we don't even try to dispatch packets while the
		// device is not reachable to preserve dispatching order.
		// If dispatching succeeded in fact this command would overcome
		// any postponed command waiting to be flushed.
		if (myDisconnectionManager.isReachable()) {
			// The following check preserves dispatching order when the 
			// device has just reconnected but flushing has not started yet
			if (waitingForFlush && !flush) {
				throw new ICPException("Upsetting dispatching order");
			}
			waitingForFlush = false;

			if (flush && oldSessionId != -1) {
				// This is a postponed command whose previous dispatch failed --> Use the
				// old sessionId, so that if the server already received it (previous dispatch 
				// failed due to a response delivering error) the command will be recognized 
				// as duplicated and properly managed
				outCnt = oldSessionId;
			}
			int sid = outCnt;
			outCnt = (outCnt+1) & 0x0f;
			myLogger.log(Logger.FINE, "Issuing outgoing command "+sid);
			try {
				JICPPacket pkt = new JICPPacket(JICPProtocol.COMMAND_TYPE, JICPProtocol.DEFAULT_INFO, payload);
				pkt.setSessionID((byte) sid);
				pkt = deliver(pkt);
				myLogger.log(Logger.FINE, "Response received "+pkt.getSessionID()); 
				if (pkt.getType() == JICPProtocol.ERROR_TYPE) {
					// Communication OK, but there was a JICP error on the peer
					throw new ICPException(new String(pkt.getData()));
				}
				return pkt.getData();
			}
			catch (IOException ioe) {
				// Can't reach the BackEnd. Assume we are unreachable 
				myLogger.log(Logger.WARNING, "IOException on output connection", ioe);
				myDisconnectionManager.setUnreachable(false);
				throw new ICPException("Dispatching error.", ioe);
			}
		}
		else {
			throw new ICPException("Unreachable");
		}
	} 

	// These variables are only used within the InputManager class,
	// but are declared externally since they must "survive" when 
	// an InputManager is replaced
	private JICPPacket lastResponse = null;
	private byte lastSid = 0x10;
	private int cnt = 0;

	/**
     Inner class InputManager
     This class deals with incoming commands (possibly keep-alive packets)
	 */
	private class InputManager extends Thread {

		private boolean active = true;
		private Connection myConnection = null;
		private int myId;

		public void run() {
			if (cnt == 0) {
				// Give precedence to the Thread that is creating the BackEnd
				Thread.yield();

				// In the meanwhile load the ConnectionListener if any 
				try {
					myConnectionListener = (ConnectionListener) Class.forName(props.getProperty("connection-listener")).newInstance();
				}
				catch (Exception e) {
					// Just ignore it
				}
			}
			myId = cnt++;
			myLogger.log(Logger.INFO, "IM-"+myId+" started");

			// Prepare an initial dummy response
			JICPPacket rsp = new JICPPacket(JICPProtocol.RESPONSE_TYPE, JICPProtocol.DEFAULT_INFO, null);
			while (active) {
				try {
					// Prepare the connection for next incoming command
					refreshConnection();
					
					myDisconnectionManager.waitUntilReachable();
					// Deliver the response to the previous incoming command and wait for the next one 
					// If we are delivering the response to an exit command, set active to false to avoid an annoying stack trace 
					// (due to the fact that the back-end will close the connection instead of sending further commands) and exit
					if (this == terminator) {
						active = false;
					}
					JICPPacket cmd = deliver(rsp, myConnection);
					myKeepAliveManager.update();
					if (cmd.getType() == JICPProtocol.KEEP_ALIVE_TYPE) {
						// Keep-alive 
						if (myLogger.isLoggable(Logger.FINER)) {
							myLogger.log(Logger.FINER, "Keep-alive received");
						}
						rsp = new JICPPacket(JICPProtocol.RESPONSE_TYPE, JICPProtocol.OK_INFO, null);
					}
					else {
						// Command
						byte sid = cmd.getSessionID();
						if (sid == lastSid) {
							myLogger.log(Logger.WARNING, "Duplicated command received "+sid);
							rsp = lastResponse;
						}
						else {
							myLogger.log(Logger.FINE, "Incoming command received "+sid);
							byte[] rspData = mySkel.handleCommand(cmd.getData());
							myLogger.log(Logger.FINE, "Incoming command served "+ sid);
							rsp = new JICPPacket(JICPProtocol.RESPONSE_TYPE, JICPProtocol.DEFAULT_INFO, rspData);
							rsp.setSessionID(sid);
							lastSid = sid;
							lastResponse = rsp;
						}
					}
				}
				catch (Exception e) {
					if (active) {
						myLogger.log(Logger.WARNING, "Exception on input connection", e);
						// Note that the boolean value passed to setUnreachable() only indicates if the unreachability was detected by a missing keep-alive
						myDisconnectionManager.setUnreachable(false); 
					}
				}
			}
			myLogger.log(Logger.INFO, "IM-"+myId+" terminated");
		}

		private synchronized void refreshConnection() throws IOException {
			if (active) {
				if (myConnection != null) {
					myConnection.close();
				}
				myConnection = getConnection(mediatorTA);
			}
			else {
				// We were killed 
				throw new IOException("Killed");
			}
		}
		
		private synchronized void kill() {
			active = false;
			try {
				myConnection.close();
			}
			catch (Exception e) {}
			myConnection = null;
		}
	} // END of inner class InputManager

	protected Connection getConnection(TransportAddress ta) {
		return new HTTPClientConnection(ta);
	}

	/**
	 * Deliver a packet to the BackEnd and get back a response using a fresh one-shot connection
	 */
	private JICPPacket deliver(JICPPacket pkt) throws IOException {
		Connection c = getConnection(mediatorTA);
		try {
			return deliver(pkt, c);
		}
		finally {
			try {
				c.close();
			}
			catch (Exception e) {}
		}
	}

	/**
	 * Deliver a packet over a given connection and get back a response
	 */
	private JICPPacket deliver(JICPPacket pkt, Connection c) throws IOException {
		boolean lastPacket = false;
		if (Thread.currentThread() == terminator) {
			pkt.setTerminatedInfo(true);
			lastPacket = true;
		}
		pkt.setRecipientID(mediatorTA.getFile());
		byte type = pkt.getType();

		int status = 0;
		try {
			c.writePacket(pkt);
			status = 1;
			/*#MIDP_INCLUDE_BEGIN
			lock();
			if (type == JICPProtocol.RESPONSE_TYPE) {
				TimerDispatcher.getTimerDispatcher().add(new Timer(System.currentTimeMillis()+5000, this));
			}
			#MIDP_INCLUDE_END*/
			pkt = c.readPacket();
			
			status = 2;
			if (lastPacket && (pkt.getInfo() & JICPProtocol.TERMINATED_INFO) != 0) {
				// When we send a packet marked with the terminated-info, the back-end may either close 
				// the connection (in this case we would have got an Exception) or reply with another 
				// packet marked with the terminated-info --> throws an Exception to expose a uniform behaviour
				myLogger.log(Logger.INFO, "Termination notification ACK received");
				throw new IOException("Terminated-info");
			}
			return pkt;
		}
		catch (IOException ioe) {
			// Re-throw the exception adding the status
			throw new IOException(ioe.getMessage()+'['+status+']');
		}
		finally {
			/*#MIDP_INCLUDE_BEGIN
			if (type != JICPProtocol.RESPONSE_TYPE) {
				// If we delivered a RESPONSE unlock() is already called by the TimerDispatcher
				unlock();
			}
			#MIDP_INCLUDE_END*/
		}
	}
	
	public void doTimeOut(Timer t) {
		unlock();
	}

	private void lock() {
		synchronized (connectorLock) {
			while (locked) {
				try {
					connectorLock.wait();
				}
				catch (Exception e) {}
			}
			locked = true;
		}
	}

	private void unlock() {
		synchronized (connectorLock) {
			locked = false;
			connectorLock.notifyAll();
		}
	}

	/**
	 * Inner class DisconnectionManager.
	 * Manages issues related to disconnection of the device.
	 */
	class DisconnectionManager implements Runnable {
		private boolean reachable = false;
		private boolean pingOK = false;
		private Thread myThread;
		private long retryTime;
		private long maxDisconnectionTime;

		private DisconnectionManager(long retryTime, long maxDisconnectionTime) {
			this.retryTime = retryTime;
			this.maxDisconnectionTime = maxDisconnectionTime;
		}

		private synchronized final boolean isReachable() {
			return reachable;
		}

		/**
		 * Set the reachability state as "unreachable" and starts
		 * a separate thread that periodically ping the back-end to
		 * detect when we are reachable again
		 */
		private synchronized void setUnreachable(boolean missingKA) {
			if (reachable) {
				if (missingKA || !pingOK) {
					if (myConnectionListener != null) {
						myConnectionListener.handleConnectionEvent(ConnectionListener.DISCONNECTED, null);
					}
					reachable = false;
					myLogger.log(Logger.INFO, "Starting DM ("+System.currentTimeMillis()+").");
					myThread = new Thread(this);
					myThread.start();
					if (pingOK) {
						// The InputManager is blocked waiting for data that will never arrive
						// Kill it and create a new one
						myInputManager.kill();
						myInputManager = new InputManager();
						myInputManager.start();
					}
				}
			}
		}

		/**
  	   Set the reachability state as "reachable" and notify
  	   the InputManager thread in case it is waiting in waitUntilReachable().
		 */
		private synchronized void setReachable() {
			reachable = true;
			if (myConnectionListener != null) {
				myConnectionListener.handleConnectionEvent(ConnectionListener.RECONNECTED, null);
			}
			notifyAll();
		}

		/**
		 * Wait until the device is reachable again. This is
		 * executed by the InputManager thread before sending a response
		 */
		synchronized void waitUntilReachable() {
			while (!reachable) {
				try {
					wait();
				}
				catch (InterruptedException ie) {
				}
			}
			pingOK = false;
		}

		/**
		   Periodically ping (that is send a CONNECT_MEDIATOR packet) the
		   BackEnd to detect when the device is reachable again.
		   When the BackEnd receives a CONNECT_MEDIATOR packet it
		   resets the input connection --> If blocked waiting for incoming 
		   commands, the InputManager thread should get an IOException.
		 */
		public void run() {
			int attemptCnt = 0;
			long startTime = System.currentTimeMillis();
			try {	
				while (!ping(attemptCnt)) {
					attemptCnt++;
					if ((System.currentTimeMillis() - startTime) > maxDisconnectionTime) {
						throw new ICPException("Max disconnection timeout expired");
					}
					else {
						waitABit(retryTime);
					}
				}

				// Ping succeeded
				myLogger.log(Logger.INFO, "Reconnection ping OK.");
				synchronized (this) {
					pingOK = true;
					setReachable();
					myKeepAliveManager.update();
					// Activate postponed commands flushing
					waitingForFlush = myStub.flush();
				}
			}
			catch (ICPException icpe) {
				// Impossible to reconnect to the BackEnd
				myLogger.log(Logger.SEVERE, "Impossible to reconnect to the BackEnd ("+System.currentTimeMillis()+")", icpe);
				if (myConnectionListener != null) {
					myConnectionListener.handleConnectionEvent(ConnectionListener.RECONNECTION_FAILURE, null);
				}
			}
		}

		private void waitABit(long time) {
			try {
				Thread.sleep(time);
			} 
			catch (InterruptedException ie) {
			}
		}  	
	}  // END of Inner class DisconnectionManager


	/**
     Inner class KeepAliveManager
     This class is responsible for taking track of keep-alive packets
     and detect problems when they miss.
	 */
	private class KeepAliveManager implements TimerListener {
		private long kaTimeout = -1;
		private Timer kaTimer;

		private KeepAliveManager(long keepAliveTime) {
			if (keepAliveTime > 0) {
				kaTimeout = keepAliveTime*2;
			}
		}

		public synchronized void doTimeOut(Timer t) {
			if (t == kaTimer) {
				// Missing keep-alive --> Try to reconnect
				myLogger.log(Logger.WARNING, "Missing Keep-alive");
				myDisconnectionManager.setUnreachable(true);
			}
		}

		private synchronized void update() {
			if (kaTimeout > 0) {
				TimerDispatcher td = TimerDispatcher.getTimerDispatcher();
				if (kaTimer != null) {
					td.remove(kaTimer);
				}
				kaTimer = td.add(new Timer(System.currentTimeMillis() + kaTimeout, this));
			}
		}
	} // END of inner class KeepAliveManager


	/**
	 * Send a CONNECT_MEDIATOR packet to the BackEnd to check if it is reachable
	 */
	private boolean ping(int cnt) throws ICPException {
		// Try first with the current transport address, then with the various backup addresses
		for(int i = -1; i < backEndAddresses.length; i++) {
			if(i >= 0) {
				// Set the mediator address to a new address..
				String addr = backEndAddresses[i];
				int colonPos = addr.indexOf(':');
				String host = addr.substring(0, colonPos);
				String port = addr.substring(colonPos + 1, addr.length());
				mediatorTA = new JICPAddress(host, port, myMediatorID, "");
			}

			try {
				myLogger.log(Logger.FINE, "Ping "+mediatorTA.getHost()+":"+mediatorTA.getPort()+"("+cnt+")...");
				JICPPacket pkt = new JICPPacket(JICPProtocol.CONNECT_MEDIATOR_TYPE, JICPProtocol.DEFAULT_INFO, null);
				pkt = deliver(pkt);
				if (pkt.getType() == JICPProtocol.ERROR_TYPE) {
					// Communication OK, but there was a JICP error.
					String errorMsg = new String(pkt.getData());
					if (errorMsg.equals(JICPProtocol.NOT_FOUND_ERROR)) {
						// Back-end not found: either the max disconnection time expired server side or there was a fault and restart
						// --> Try to recreate the Back-end
						myLogger.log(Logger.WARNING, "Communication OK, but Back-end no longer present. Try to recreate it");
						if (myConnectionListener != null) {
							myConnectionListener.handleConnectionEvent(ConnectionListener.BE_NOT_FOUND, null);
						}
						try {
							createBackEnd();
						}
						catch (IMTPException imtpe) {
							myLogger.log(Logger.WARNING, "Error re-creating the Back-end.");
							return false;
						}
					}
					else {
						// Generic JICP error. No need to go on 
						throw new ICPException("JICP error. "+errorMsg);
					}
				}
				return true;
			}
			catch (IOException ioe) {
				// Ignore it, and try the next address...
				myLogger.log(Logger.FINE, "Ping KO", ioe);
			}
		}

		// No address succeeded.
		return false;
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
	

	/*private String[] parseBackEndAddresses(String addressesText) {
		Vector addrs = new Vector();

		if(addressesText != null && !addressesText.equals("")) {
			// Copy the string with the specifiers into an array of char
			char[] addressesChars = new char[addressesText.length()];

			addressesText.getChars(0, addressesText.length(), addressesChars, 0);

			// Create the StringBuffer to hold the first address
			StringBuffer sbAddr = new StringBuffer();
			int i = 0;

			while(i < addressesChars.length) {
				char c = addressesChars[i];

				if((c != ',') && (c != ';') && (c != ' ') && (c != '\n') && (c != '\t')) {
					sbAddr.append(c);
				}
				else {

					// The address is terminated --> Add it to the result list
					String tmp = sbAddr.toString().trim();

					if (tmp.length() > 0) {
						// Add the Address to the list
						addrs.addElement(tmp);
					}

					// Create the StringBuffer to hold the next specifier
					sbAddr = new StringBuffer();
				}

				++i;
			}

			// Handle the last specifier
			String tmp = sbAddr.toString().trim();

			if(tmp.length() > 0) {
				// Add the Address to the list
				addrs.addElement(tmp);
			}
		}

		// Convert the list into an array of strings
		String[] result = new String[addrs.size()];
		for(int i = 0; i < result.length; i++) {
			result[i] = (String)addrs.elementAt(i);
		}

		return result;

	}*/
}

