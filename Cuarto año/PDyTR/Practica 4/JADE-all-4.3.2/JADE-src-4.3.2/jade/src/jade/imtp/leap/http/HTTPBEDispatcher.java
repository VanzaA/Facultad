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
import jade.imtp.leap.JICP.*;
import jade.core.Timer;
import jade.core.TimerListener;
import jade.core.Runtime;

import java.net.*;

/**
 * Class declaration
 * @author Giovanni Caire - TILAB
 */
public class HTTPBEDispatcher implements BEConnectionManager, Dispatcher, JICPMediator {

	private JICPMediatorManager myMediatorManager;
	private String myID;
	private MicroSkeleton mySkel = null;
	private FrontEndStub myStub = null;
	private BackEndContainer myContainer = null;
	private OutgoingsHandler myOutgoingsHandler;
	private JICPPacket lastResponse = null;
	private byte lastSid = 0x10;
	private Logger myLogger = Logger.getMyLogger(this.getClass().getName());

	/////////////////////////////////////
	// JICPMediator interface implementation
	/////////////////////////////////////
	public String getID() {
		return myID;
	}

	/**
    Initialize parameters and activate the BackEndContainer
	 */
	public void init(JICPMediatorManager mgr, String id, Properties props) throws ICPException {
		myMediatorManager = mgr;
		myID = id;

		// Read parameters
		// Max disconnection time
		long maxDisconnectionTime = JICPProtocol.DEFAULT_MAX_DISCONNECTION_TIME;
		try {
			maxDisconnectionTime = Long.parseLong(props.getProperty(JICPProtocol.MAX_DISCONNECTION_TIME_KEY));
		} catch (Exception e) {
			// Keep default
		}

		// Max disconnection time
		long keepAliveTime = JICPProtocol.DEFAULT_KEEP_ALIVE_TIME;
		try {
			keepAliveTime = Long.parseLong(props.getProperty(JICPProtocol.KEEP_ALIVE_TIME_KEY));
		} catch (Exception e) {
			// Keep default
		}

		// FIXME: Properly manage SID of next outgoing command and last incoming command in case of BE re-creation
		
		myOutgoingsHandler = new OutgoingsHandler(maxDisconnectionTime, keepAliveTime);

		if (myLogger.isLoggable(Logger.INFO)) {
			myLogger.log(Logger.INFO, "Created HTTPBEDispatcher V2.0. ID = " + myID + "\n- MaxDisconnectionTime = " + maxDisconnectionTime);
		}

		myStub = new FrontEndStub(this);
		mySkel = startBackEndContainer(props);
	}

	protected final BackEndSkel startBackEndContainer(Properties props) throws ICPException {
		try {
			String nodeName = myID.replace(':', '_');
			props.setProperty(Profile.CONTAINER_NAME, nodeName);

			myContainer = new BackEndContainer(props, this);
			if (!myContainer.connect()) {
				throw new ICPException("BackEnd container failed to join the platform");
			}
			//Possibly the node name was re-assigned by the main
			myID = myContainer.here().getName();
			if (myLogger.isLoggable(Logger.CONFIG)) {
				myLogger.log(Logger.CONFIG, "BackEndContainer " + myID + " successfully joined the platform.");
			}
			return new BackEndSkel(myContainer);
		} catch (ProfileException pe) {
			// should never happen
			pe.printStackTrace();
			throw new ICPException("Error creating profile");
		}
	}

	/**
    Shutdown self initiated or forced by the MediatorManager this
    BackEndContainer is attached to.
	 */
	public void kill() {
		// Force the BackEndContainer to terminate. This will also
		// cause this HTTPBEDispatcher to terminate and deregister
		// from the MediatorManager
		myContainer.shutDown();
	}

	/**
    Handle an incoming JICP packet received by the MediatorManager.
	 */
	public JICPPacket handleJICPPacket(JICPPacket pkt, InetAddress addr, int port) throws ICPException {
		String from = " [" + addr + ":" + port + "]";
		if (pkt.getType() == JICPProtocol.COMMAND_TYPE) {
			// COMMAND
			if ((pkt.getInfo() & JICPProtocol.TERMINATED_INFO) != 0) {
				// PEER TERMINATION NOTIFICATION
				// The remote FrontEnd has terminated spontaneously --> Terminate and notify up.
				myLogger.log(Logger.INFO, "Peer termination notification received. Peer address is " + from);
				handlePeerExited();
				return null;
			} else {
				// NORMAL COMMAND
				// Serve the incoming command and send back the response
				byte sid = pkt.getSessionID();
				if (sid == lastSid && lastResponse != null) {
					if (myLogger.isLoggable(Logger.WARNING)) {
						myLogger.log(Logger.WARNING, "Duplicated command received " + sid + " " + from);
					}
					pkt = lastResponse;
				} else {
					if (myLogger.isLoggable(Logger.FINE)) {
						myLogger.log(Logger.FINE, "Incoming command received " + sid + " " + from);
					}

					byte[] rspData = mySkel.handleCommand(pkt.getData());
					if (myLogger.isLoggable(Logger.FINE)) {
						myLogger.log(Logger.FINE, "Incoming command served " + sid);
					}
					pkt = new JICPPacket(JICPProtocol.RESPONSE_TYPE, JICPProtocol.DEFAULT_INFO, rspData);
					pkt.setSessionID(sid);
					lastSid = sid;
					lastResponse = pkt;
				}
				return pkt;
			}
		} else {
			// RESPONSE
			return myOutgoingsHandler.dispatchResponse(pkt, from);
		}
	}

	/**
    Handle an incoming connection. This is called by the MediatorManager
    when a CREATE or CONNECT_MEDIATOR request is received.
    The HTTPBEDispatcher reacts to this call by resetting the current situation
	 */
	public boolean handleIncomingConnection(Connection c, JICPPacket pkt, InetAddress addr, int port) {
		myOutgoingsHandler.setConnecting();
		return false;
	}

	private void ensureFERunning(final long timeout) {
		Thread t = new Thread() {

			public void run() {
				if (timeout > 0) {
					if (!myOutgoingsHandler.waitForInitialResponse(timeout)) {
						if (myLogger.isLoggable(Logger.INFO)) {
							myLogger.log(Logger.INFO, "Missing initial dummy response after reconnection");
						}
					}
				}
			}
		};
		t.start();
	}

	public void tick(long currentTime) {
		// Not used
	}

	//////////////////////////////////////////
	// Dispatcher interface implementation
	//////////////////////////////////////////
	/**
    This is called by the Stub using this Dispatcher to dispatch
    a serialized command to the FrontEnd.
    Mutual exclusion with itself to ensure one command at a time
    is dispatched.
	 */
	public synchronized byte[] dispatch(byte[] payload, boolean flush, int oldSessionId) throws ICPException {
		JICPPacket pkt = new JICPPacket(JICPProtocol.COMMAND_TYPE, JICPProtocol.DEFAULT_INFO, payload);
		pkt = myOutgoingsHandler.deliverCommand(pkt, flush, oldSessionId);
		if (pkt.getType() == JICPProtocol.ERROR_TYPE) {
			// Communication OK, but there was a JICP error on the peer
			throw new ICPException(new String(pkt.getData()));
		}
		return pkt.getData();
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
    Clean up this HTTPBEDispatcher.
    The shutdown process can be activated in the following cases:
    1) The local container is requested to exit --> The exit commad
    is forwarded to the FrontEnd
    1.a) Forwarding OK. The FrontEnd sends back a response with
    the TERMINATED_INFO set. When this response is received the
    shutdown() method is called (see handleJICPPacket()).
    1.b) Forwarding failed. The BackEndContainer ignores the
    exception and directly calls the shutdown() method.

    Note that in the case the FrontEnd spontaneously exits and in the
    case the max disconnection time expires the kill() method is
    called --> see case 1.
	 */
	public void shutdown() {
		if (myLogger.isLoggable(Logger.FINE)) {
			myLogger.log(Logger.FINE, "Initiate HTTPBEDispatcher shutdown");
		}

		// Deregister from the JICPServer
		if (myID != null) {
			myMediatorManager.deregisterMediator(myID);
			myID = null;
		}

		// In case shutdown() is called while the device is disconnected
		// this resets the disconnection timer (if any).
		myOutgoingsHandler.setTerminating();
	}

	protected void handlePeerExited() {
		// The FrontEnd has exited --> suicide!
		myOutgoingsHandler.setTerminating();
		kill();
	}

	protected void handleConnectionError() {
		// The FrontEnd is probably dead --> suicide!
		// FIXME: If there are pending messages that will never be delivered
		// we should notify a FAILURE to the sender
		myOutgoingsHandler.setTerminating();
		kill();
	}

	/**
    Inner class OutgoingsHandler.
    This class manages outgoing commands i.e. commands that must be sent to the FrontEnd.

    NOTE that, since HTTPBEDispatcher.dispatch() is synchronized only one thread at
    a time can execute the deliverCommand() method. This also ensures
    that only one thread at a time can execute the dispatchResponse()
    method. As a consequence it's impossible that at a certain point in
    time there is both a thread waiting for a command and a thread waiting
    for a response.
	 */
	private class OutgoingsHandler implements TimerListener {

		private static final int REACHABLE = 0;
		private static final int CONNECTING = 1;
		private static final int UNREACHABLE = 2;
		private static final int TERMINATED = 3;
		private static final long RESPONSE_TIMEOUT = 5000; // 30 sec
		private static final int MAX_SID = 0x0f;
		
		private int frontEndStatus = CONNECTING;
		private int outCnt = 0;
		private Thread responseWaiter = null;
		private JICPPacket currentCommand = null;
		private JICPPacket currentResponse = null;
		private boolean commandReady = false;
		private boolean responseReady = false;
		private boolean connectionReset = false;
		private long maxDisconnectionTime;
		private long keepAliveTime;
		private Timer maxDisconnectionTimer = null;
		private boolean waitingForFlush = false;
		private Object initialResponseLock = new Object();
		private boolean initialResponseReceived;

		public OutgoingsHandler(long maxDisconnectionTime, long keepAliveTime) {
			this.maxDisconnectionTime = maxDisconnectionTime;
			this.keepAliveTime = (keepAliveTime >= 0 ? keepAliveTime : 0);
		}

		/**
        Schedule a command for delivery, wait for the response from the
        FrontEnd and return it.
        @exception ICPException if 1) the frontEndStatus is not REACHABLE,
        2) the response timeout expires (the frontEndStatus is set to
        UNREACHABLE) or 3) the OutgoingsHandler is reset (the frontEndStatus
        is set to CONNECTING).
        Called by HTTPBEDispatcher#dispatch()
		 */
		public synchronized JICPPacket deliverCommand(JICPPacket cmd, boolean flush, int oldSessionId) throws ICPException {
			if (frontEndStatus == REACHABLE) {
				// The following check preserves dispatching order when the
				// front-end has just reconnected but flushing of postponed commands has not started yet
				if (waitingForFlush && !flush) {
					throw new ICPException("Upsetting dispatching order");
				}
				waitingForFlush = false;

				// 1) Schedule the command for delivery
				if (flush && oldSessionId != -1) {
					// This is a postponed command whose previous dispatch failed --> Use the
					// old sessionId, so that if the server already received it (previous dispatch 
					// failed due to a response delivering error) the command will be recognized 
					// as duplicated and properly managed
					outCnt = oldSessionId;
				}
				int sid = outCnt;
				outCnt = (outCnt + 1) & MAX_SID;
				if (myLogger.isLoggable(Logger.FINE)) {
					myLogger.log(Logger.FINE, "Scheduling outgoing command for delivery " + sid);
				}
				cmd.setSessionID((byte) sid);
				currentCommand = cmd;
				commandReady = true;
				// Notify the thread that dispatched the response to the previous command (see dispatchResponse()) 
				notifyAll();

				// 2) Wait for the response
				while (!responseReady) {
					try {
						responseWaiter = Thread.currentThread();
						wait(RESPONSE_TIMEOUT * (1 + cmd.getLength() / 4096));
						responseWaiter = null;
						if (!responseReady) {
							if (frontEndStatus == CONNECTING) {
								// The connection was reset
								myLogger.log(Logger.WARNING, "Connection reset while waiting for response " + sid);
							} else {
								if (frontEndStatus != TERMINATED) {
									// Response Timeout expired
									myLogger.log(Logger.WARNING, "Response timeout expired " + sid);
									setUnreachable();
								}
							}
							throw new ICPDispatchException("Missing response", cmd.getSessionID());
						}
					} catch (InterruptedException ie) {
					}
				}
				if (myLogger.isLoggable(Logger.FINE)) {
					myLogger.log(Logger.FINE, "Response to scheduled command received " + currentResponse.getSessionID());
				}
				responseReady = false;
				return currentResponse;
			} else {
				throw new ICPException("Unreachable");
			}
		}

		/**
        Dispatch a response received from the FrontEnd to the issuer of the command
        this response refers to.
        If no one is waiting for this response (the frontEndStatus must be
        different from REACHABLE), set the frontEndStatus to REACHABLE.
        Then wait for the next command to transfer to the FrontEnd.
        @return the next outgoing command to be transferred to the FrontEnd
        or null if the OutgoingsHandler is reset.
        Called by HTTPBEDispatcher#handleJICPPacket()
		 */
		public synchronized JICPPacket dispatchResponse(JICPPacket rsp, String from) {
			// 1) Handle the response
			if ((rsp.getInfo() & JICPProtocol.OK_INFO) != 0) {
				// Keep-alive response
				if (myLogger.isLoggable(Logger.FINER)) {
					myLogger.log(Logger.FINER, "Keep-alive response received");
				}
				// Maybe there is someone waiting to deliver a command
				notifyAll();
			} else {
				if (responseWaiter != null) {
					// There was someone waiting for this response. Dispatch it
					if (myLogger.isLoggable(Logger.FINE)) {
						myLogger.log(Logger.FINE, "Response received " + rsp.getSessionID() +" from "+ from);
					}
					responseReady = true;
					currentResponse = rsp;
					notifyAll();
				} else {
					// No one was waiting for this response. It must be the initial dummy response or a 
					// response that arrives after the timeout has expired.
					if (frontEndStatus == CONNECTING) {
						if (myLogger.isLoggable(Logger.INFO)) {
							myLogger.log(Logger.INFO, "Initial dummy response received " + rsp.getSessionID() + from);
						}
						notifyInitialResponseReceived();
					} else {
						if (myLogger.isLoggable(Logger.WARNING)) {
							myLogger.log(Logger.WARNING, "Unexpected response received (likely an out of time respose) " + rsp.getSessionID() + from);
						}
					}
				}
			}
			
			if (frontEndStatus != REACHABLE) {
				// Certainly the front-end is reachable now. If it was unreachable reset the max-disconnection timer and 
				// start flushing postponed commands
				frontEndStatus = REACHABLE;
				resetMaxDisconnectionTimer();
				waitingForFlush = myStub.flush();
			}

			// 2) Check if this is the last response
			if ((rsp.getInfo() & JICPProtocol.TERMINATED_INFO) != 0) {
				// The FrontEnd has terminated as a consequence of a command issued
				// by the local BackEnd. Terminate
				if (myLogger.isLoggable(Logger.INFO)) {
					myLogger.log(Logger.INFO, "Last response detected");
				}
				shutdown();
				return null;
			}

			// 3) Wait for the next command that must be delivered to the front-end
			while (!commandReady) {
				try {
					wait(keepAliveTime);
					if (!commandReady) {
						if (connectionReset) {
							// The connection was reset
							if (myLogger.isLoggable(Logger.FINE)) {
								myLogger.log(Logger.FINE, "Return with no command to deliver");
							}
							return null;
						} else {
							// Keep alive timeout expired --> send a keep-alive packet
							if (myLogger.isLoggable(Logger.FINER)) {
								myLogger.log(Logger.FINER, "Sending keep-alive packet");
							}
							return new JICPPacket(JICPProtocol.KEEP_ALIVE_TYPE, JICPProtocol.DEFAULT_INFO, null);
						}
					}
				} catch (InterruptedException ie) {
				}
			}
			if (myLogger.isLoggable(Logger.FINE)) {
				myLogger.log(Logger.FINE, "Delivering outgoing command " + currentCommand.getSessionID());
			}
			commandReady = false;
			return currentCommand;
		}

		/**
        Reset this OutgoingsHandler and set the frontEndStatus to CONNECTING.
        If there is a thread waiting for a command to deliver to the
        FrontEnd it will return null.
        If there is a thread waiting for a response it will exit with
        an Exception.
        The frontEndStatus is set to CONNECTING.
        Called by HTTPBEDispatcher#handleIncomingConnection()
		 */
		public synchronized void setConnecting() {
			if (myLogger.isLoggable(Logger.FINE)) {
				myLogger.log(Logger.FINE, "Resetting the connection");
			}
			initialResponseReceived = false;
			frontEndStatus = CONNECTING;
			reset();

			Thread t = new Thread() {

				public void run() {
					if (!myOutgoingsHandler.waitForInitialResponse(60000)) {
						if (myLogger.isLoggable(Logger.FINE)) {
							myLogger.log(Logger.FINE, "Missing initial dummy response after reconnection");
						}
						setUnreachable();
					}
				}
			};
			t.start();
		}

		private synchronized void setUnreachable() {
			frontEndStatus = UNREACHABLE;
			activateMaxDisconnectionTimer(maxDisconnectionTime);
		}

		/**
        Reset this OutgoingsHandler and set the frontEndStatus to TERMINATED.
		 */
		public synchronized void setTerminating() {
			frontEndStatus = TERMINATED;
			reset();
		}

		private void reset() {
			commandReady = false;
			responseReady = false;
			currentCommand = null;
			currentResponse = null;
			resetMaxDisconnectionTimer();
			notifyAll();
		}

		private void activateMaxDisconnectionTimer(long timeout) {
			// Set the disconnection timer
			long now = System.currentTimeMillis();
			maxDisconnectionTimer = new Timer(now + timeout, this);
			maxDisconnectionTimer = Runtime.instance().getTimerDispatcher().add(maxDisconnectionTimer);
			if (myLogger.isLoggable(Logger.FINE)) {
				myLogger.log(Logger.FINE, "Disconnection timer activated.");
			}
		}

		private void resetMaxDisconnectionTimer() {
			if (maxDisconnectionTimer != null) {
				Runtime.instance().getTimerDispatcher().remove(maxDisconnectionTimer);
				maxDisconnectionTimer = null;
			}
		}

		public synchronized void doTimeOut(Timer t) {
			if (frontEndStatus != REACHABLE) {
				if (myLogger.isLoggable(Logger.WARNING)) {
					myLogger.log(Logger.WARNING, "Max disconnection timeout expired.");
				}
				// The remote FrontEnd is probably down --> notify up.
				handleConnectionError();
			}
		}

		private boolean waitForInitialResponse(long timeout) {
			synchronized (initialResponseLock) {
				if (!initialResponseReceived) {
					try {
						initialResponseLock.wait(timeout);
					} catch (Exception e) {
					}
				}
				return initialResponseReceived;
			}
		}

		private void notifyInitialResponseReceived() {
			synchronized (initialResponseLock) {
				initialResponseReceived = true;
				initialResponseLock.notifyAll();
			}
		}
	} // END of inner class OutgoingsHandler
}

