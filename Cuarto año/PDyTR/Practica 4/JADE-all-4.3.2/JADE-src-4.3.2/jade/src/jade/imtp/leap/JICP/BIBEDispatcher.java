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

//#J2ME_EXCLUDE_FILE

import jade.core.BackEndContainer;
import jade.core.BEConnectionManager;
import jade.core.BackEnd;
import jade.core.FrontEnd;
import jade.core.IMTPException;
import jade.core.Profile;
import jade.core.ProfileException;
import jade.imtp.leap.FrontEndStub;
import jade.imtp.leap.ICPDispatchException;
import jade.imtp.leap.MicroSkeleton;
import jade.imtp.leap.BackEndSkel;
import jade.imtp.leap.Dispatcher;
import jade.imtp.leap.ICPException;
import jade.util.leap.Properties;
import jade.util.Logger;

import java.io.*;
import java.net.*;
//import java.util.*;

/**
 * Class declaration
 * @author Giovanni Caire - TILAB
 */
public class BIBEDispatcher extends Thread implements BEConnectionManager, Dispatcher, JICPMediator {
	private static final long RESPONSE_TIMEOUT = 60000;

	private static final int REACHABLE = 1;
	private static final int UNREACHABLE = 0;

	private int frontEndStatus = UNREACHABLE;
	private long              maxDisconnectionTime;
	private long              keepAliveTime;
	private long              lastReceivedTime;

	private JICPMediatorManager        myMediatorManager;
	private String            myID;

	private byte lastSid = 0x0f;
	private int inpCnt = 0;
	private boolean active = true;

	protected InpConnectionHolder  inpHolder = new InpConnectionHolder();
	protected OutConnectionHolder  outHolder = new OutConnectionHolder();

	private MicroSkeleton mySkel = null;
	private FrontEndStub myStub = null;
	private BackEndContainer myContainer = null;

	private Logger myLogger = Logger.getMyLogger(getClass().getName());

	/**
	 * Constructor declaration
	 */
	public BIBEDispatcher() {
	}

	/////////////////////////////////////
	// JICPMediator interface implementation
	/////////////////////////////////////
	public String getID() {
		return myID;
	}

	/**
     Initialize parameters and start the embedded thread
	 */
	public void init(JICPMediatorManager mgr, String id, Properties props) throws ICPException {
		myMediatorManager = mgr;
		myID = id;

		// Verbosity
		int verbosity = 1;
		try {
			verbosity = Integer.parseInt(props.getProperty("verbosity"));
		}
		catch (NumberFormatException nfe) {
			// Use default (1)
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
		try {
			inpCnt = (Integer.parseInt(props.getProperty("lastsid")) + 1) & 0x0f;
		}
		catch (Exception e) {
			// Keep default
		}

		/* lastSid
		try {
			lastSid = (byte) (Integer.parseInt(props.getProperty("outcnt")) -1);
			if (lastSid < 0) {
				lastSid = 0x0f;
			}
		}
		catch (Exception e) {
			// Keep default
		}*/

		// Start the embedded thread dealing with outgoing commands
		start();

		myStub = new FrontEndStub(this);
		mySkel = startBackEndContainer(props);
	}

	protected final BackEndSkel startBackEndContainer(Properties props) throws ICPException {
		try {

			String nodeName = myID.replace(':', '_');
			props.setProperty(Profile.CONTAINER_NAME, nodeName);

			myContainer = new BackEndContainer(props, this);
			// BOOTSTRAP_AGENTS Gestire nuovo valore di ritorno
			if (!myContainer.connect()) {
				throw new ICPException("BackEnd container failed to join the platform");
			}
			// Possibly the node name was re-assigned by the main
			myID = myContainer.here().getName();
			if(myLogger.isLoggable(Logger.CONFIG)) {
				myLogger.log(Logger.CONFIG,"BackEndContainer "+myID+" successfully joined the platform.");
			}
			return new BackEndSkel(myContainer);
		}
		catch (ProfileException pe) {
			// should never happen
			pe.printStackTrace();
			throw new ICPException("Error creating profile");
		}
	}

	private Object shutdownLock = new Object();

	/**
     Shutdown self initiated or forced by the JICPServer this
     BackEndContainer is attached to.
	 */
	public void kill() {
		// Avoid killing two times
		synchronized (shutdownLock) {
			if (active) {
				active = false;
				// Force the BackEndContainer to terminate. This will also
				// cause this BIBEDispatcher to terminate and deregister
				// from the JICPServer
				myContainer.shutDown();
			}
		}
	}

	/**
     This is called by the JICPServer when a JICP packet addressing this
     mediator as recipient-ID is received. In the case of the BIBEDispatcher
     this should never happen.
	 */
	public JICPPacket handleJICPPacket(JICPPacket p, InetAddress addr, int port) throws ICPException {
		return null;
	}

	/**
     This is called by the JICPServer when a JICP CREATE_MEDIATOR or
     CONNECT_MEDIATOR is received.
	 */
	public boolean handleIncomingConnection(Connection c, JICPPacket pkt, InetAddress addr, int port) {
		boolean inp = false;
		byte[] data = pkt.getData();
		if (data.length == 1) {
			inp = (data[0] == 1);
		}
		else {
			// Backward compatibility
			try {
				inp = (new String(data)).equals("inp");
			}
			catch (Exception e) {}
		}
		if (inp) {
			inpHolder.setConnection(c);
		}
		else {
			outHolder.setConnection(c);
		}

		// Update keep-alive info
		lastReceivedTime = System.currentTimeMillis();

		return true;
	}

	public void tick(long currentTime) {
		if (keepAliveTime > 0) {
			if ((currentTime - lastReceivedTime) > (keepAliveTime + 60000)) {
				// Missing keep-alive.
				// The OUT connection is no longer valid
				if (outHolder.isConnected()) {
					if(myLogger.isLoggable(Logger.WARNING))
						myLogger.log(Logger.WARNING,myID+" - Missing keep-alive");
					outHolder.resetConnection();
				}
				// Check the INP connection. Since this method must return
				// asap, does it in a separated Thread
				if (inpHolder.isConnected()) {
					Thread t = new Thread() {
						public void run() {
							try {
								JICPPacket pkt = new JICPPacket(JICPProtocol.KEEP_ALIVE_TYPE, JICPProtocol.DEFAULT_INFO, null);
								dispatchPacket(pkt, false, -1);
								if(myLogger.isLoggable(Logger.FINE))
									myLogger.log(Logger.FINE, myID+" - IC valid");
							}
							catch (Exception e) {
								// Just do nothing
							}
						}
					};
					t.start();
				}
			}
		}
	}

	////////////////////////////////////////////////
	// BEConnectionManager interface implementation
	////////////////////////////////////////////////
	/**
	   Return a stub of the remote FrontEnd that is connected to the
	   local BackEnd.
	   @param be The local BackEnd
	   @param props Additional (implementation dependent) connection
	   configuration properties.
	   @return A stub of the remote FrontEnd.
	 */
	public FrontEnd getFrontEnd(BackEnd be, Properties props) throws IMTPException {
		return myStub;
	}


	/**
     Make this BackEndDispatcher terminate.
	 */
	public void shutdown() {
		if(myLogger.isLoggable(Logger.FINE))
			myLogger.log(Logger.FINE,myID+" - Initiate BIBEDispatcher shutdown");


		// Deregister from the JICPServer
		if (myID != null) {
			myMediatorManager.deregisterMediator(myID);
			myID = null;
		}

		active = false;
		inpHolder.resetConnection(true);
		outHolder.resetConnection();
	}

	//////////////////////////////////////////
	// Dispatcher interface implementation 
	//////////////////////////////////////////
	public byte[] dispatch(byte[] payload, boolean flush, int oldSessionId) throws ICPException {
		JICPPacket pkt = new JICPPacket(JICPProtocol.COMMAND_TYPE, JICPProtocol.DEFAULT_INFO, payload);
		pkt = dispatchPacket(pkt, flush, oldSessionId);
		return pkt.getData();
	}

	private synchronized JICPPacket dispatchPacket(JICPPacket pkt, boolean flush, int oldSessionId) throws ICPException {
		Connection inpConnection = inpHolder.getConnection(flush);
		if (inpConnection != null && active) {
			int status = 0;
			if (flush && oldSessionId != -1) {
				// This is a postponed command whose previous dispatch failed --> Use the
				// old sessionId, so that if the server already received it (previous dispatch 
				// failed due to a response delivering error) the command will be recognized 
				// as duplicated and properly managed
				inpCnt = oldSessionId;
			}
			pkt.setSessionID((byte) inpCnt);
			if (pkt.getType() == JICPProtocol.KEEP_ALIVE_TYPE) {
				if(myLogger.isLoggable(Logger.FINER)) {
					myLogger.log(Logger.FINER,myID+" - Issuing Keep-alive to FE "+inpCnt);
				}
			}
			else {
				if(myLogger.isLoggable(Logger.FINER)) {
					myLogger.log(Logger.FINER,myID+" - Issuing command to FE "+inpCnt);
				}
			}
			try {
				inpConnection.writePacket(pkt);
				status = 1;

				// Create a watch-dog to avoid waiting forever
				inpHolder.startWatchDog(RESPONSE_TIMEOUT);
				pkt = readPacket(inpConnection);
				// Reply received --> Remove the watch-dog
				inpHolder.stopWatchDog();
				status = 2;

				if(myLogger.isLoggable(Logger.FINER))
					myLogger.log(Logger.FINER,myID+" - Response received from FE "+pkt.getSessionID());

				if (pkt.getType() == JICPProtocol.ERROR_TYPE) {
					// Communication OK, but there was a JICP error on the peer
					throw new ICPException(new String(pkt.getData()));
				}
				if ((pkt.getInfo() & JICPProtocol.TERMINATED_INFO) != 0) {
					// This is the response to an exit command --> Suicide
					shutdown();
				}
				return pkt;
			}
			catch (IOException ioe) {
				// Can't reach the FrontEnd.
				if(myLogger.isLoggable(Logger.WARNING))
					myLogger.log(Logger.WARNING,myID+" - IOException IC["+status+"]"+ioe);

				inpHolder.resetConnection(false);
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

	//////////////////////////////////////////////////
	// The embedded Thread handling outgoing commands
	//////////////////////////////////////////////////
	private JICPPacket lastResponse;

	public void run() {
		lastResponse = null;
		int status = 0;

		if(myLogger.isLoggable(Logger.FINE))
			myLogger.log(Logger.FINE,myID+" - BIBEDispatcher thread started");

		while (active) {
			try {
				while (active) {
					status = 0;
					Connection outConnection = outHolder.getConnection();
					if (outConnection != null) {
						JICPPacket pkt = readPacket(outConnection);
						status = 1;
						pkt = handlePacket(pkt);
						if (pkt != null) {
							status = 2;

							outConnection.writePacket(pkt);
							status = 3;
						}
					}
					else {
						handlePeerExited("Max disconnection timeout expired");
					}
				}
			}
			catch (IOException ioe) {
				if (active) {
					if(myLogger.isLoggable(Logger.WARNING))
						myLogger.log(Logger.WARNING,myID+" - IOException OC["+status+"]"+ioe);

					outHolder.resetConnection();
				}
			}
		}
		if(myLogger.isLoggable(Logger.FINE))
			myLogger.log(Logger.FINE,myID+" - BIBEDispatcher Thread terminated");

	}

	protected JICPPacket handlePacket(JICPPacket pkt) {
		JICPPacket reply = null;
		if (pkt.getType() == JICPProtocol.KEEP_ALIVE_TYPE) {
			// Keep-alive packet
			if(myLogger.isLoggable(Logger.FINEST)) {
				myLogger.log(Logger.FINEST,myID+" - Keep-alive received");
			}

			reply = new JICPPacket(JICPProtocol.RESPONSE_TYPE, getReconnectInfo(), null);
		}
		else {
			// Outgoing command
			if ((pkt.getInfo() & JICPProtocol.TERMINATED_INFO) != 0) {
				// PEER TERMINATION NOTIFICATION
				// The remote FrontEnd has terminated spontaneously -->
				// Terminate and notify up.
				handlePeerExited("Peer termination notification received");
				return null;
			}
			byte sid = pkt.getSessionID();
			if (sid == lastSid && lastResponse != null) {
				if(myLogger.isLoggable(Logger.WARNING))
					myLogger.log(Logger.WARNING,myID+" - Duplicated command from FE "+sid);

				reply = lastResponse;
			}
			else {
				if(myLogger.isLoggable(Logger.FINER))
					myLogger.log(Logger.FINER,myID+" - Command from FE received "+sid);

				byte[] rspData = mySkel.handleCommand(pkt.getData());
				if(myLogger.isLoggable(Logger.FINER))
					myLogger.log(Logger.FINER,myID+" - Command from FE served "+ sid);

				reply = new JICPPacket(JICPProtocol.RESPONSE_TYPE, getReconnectInfo(), rspData);
				reply.setSessionID(sid);
				lastSid = sid;
				lastResponse = reply;
			}
		}
		return reply;
	}


	private byte getReconnectInfo() {
		byte info = JICPProtocol.DEFAULT_INFO;
		// If the inpConnection is null request the FrontEnd to reconnect
		if (!inpHolder.isConnected()) {
			info |= JICPProtocol.RECONNECT_INFO;
		}
		return info;
	}

	private void handlePeerExited(String msg) {
		if(myLogger.isLoggable(Logger.INFO))
			myLogger.log(Logger.INFO,myID+" - "+msg);

		kill();
	}

	private JICPPacket readPacket(Connection c) throws IOException {
		JICPPacket pkt = c.readPacket();
		// Update keep-alive info
		lastReceivedTime = System.currentTimeMillis();
		return pkt;
	}

	/**
     Inner class InpConnectionHolder.
     Wrapper for the connection used to deliver commands to the FrontEnd
	 */
	protected class InpConnectionHolder {
		private Connection myConnection;
		private boolean connectionRefreshed;
		private boolean waitingForFlush = false;
		private Thread watchDog = null;

		private synchronized void setConnection(Connection c) {
			if(myLogger.isLoggable(Logger.FINE))
				myLogger.log(Logger.FINE,myID+" - New input connection.");

			// Close the old connection
			if (myConnection != null) {
				close(myConnection);
			}
			// Stop the WatchDog if any
			stopWatchDog();
			// Set the new connection
			myConnection = c;
			connectionRefreshed = true;
			waitingForFlush = myStub.flush();
			//myContainer.notifyInputConnectionReady();
		}

		private synchronized Connection getConnection(boolean flush) {
			if (waitingForFlush && (!flush)) {
				return null;
			}
			waitingForFlush = false;
			connectionRefreshed = false;
			return myConnection;
		}

		public synchronized void resetConnection(boolean force) {
			if (!connectionRefreshed || force) {
				if (myConnection != null) {
					close(myConnection);
				}
				myConnection = null;
			}
		}

		private synchronized boolean isConnected() {
			return myConnection != null;
		}

		private synchronized void startWatchDog(final long timeout) {
			watchDog = new Thread() {
				public void run() {
					try {
						Thread.sleep(timeout);
						// WatchDog expired --> close the connection
						if(myLogger.isLoggable(Logger.WARNING))
							myLogger.log(Logger.WARNING,myID+" - Response timeout expired");

						resetConnection(false);
					}
					catch (InterruptedException ie) {
						// Watch dog removed. Just do nothing
					}
				}
			};
			watchDog.start();
		}

		private synchronized void stopWatchDog() {
			if (watchDog != null) {
				watchDog.interrupt();
				watchDog = null;
			}
		}
	} // END of inner class InpConnectionHolder


	/**
     Inner class OutConnectionHolder
     Wrapper for the connection used to receive commands from the FrontEnd
	 */
	protected class OutConnectionHolder {
		private Connection myConnection;
		private boolean connectionRefreshed;

		private synchronized void setConnection(Connection c) {
			if(myLogger.isLoggable(Logger.FINE))
				myLogger.log(Logger.FINE,myID+" - New output connection.");

			if (myConnection != null) {
				close(myConnection);
			}
			myConnection = c;
			connectionRefreshed = true;
			notifyAll();
		}

		private synchronized Connection getConnection() {
			while (myConnection == null) {
				try {
					wait(maxDisconnectionTime);
					if (myConnection == null) {
						return null;
					}
				}
				catch (Exception e) {
					if(myLogger.isLoggable(Logger.WARNING))
						myLogger.log(Logger.WARNING,myID+" - Spurious wake up");
				}
			}
			connectionRefreshed = false;
			return myConnection;
		}

		public synchronized void resetConnection() {
			if (!connectionRefreshed) {
				if (myConnection != null) {
					close(myConnection);
				}
				myConnection = null;
			}
		}

		private synchronized boolean isConnected() {
			return myConnection != null;
		}
	} // END of inner class OutConnectionHolder

	private void close(Connection c) {
		try {
			c.close();
		}
		catch (IOException ioe) {
		}
	}
}

