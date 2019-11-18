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
import java.util.logging.Level;

/**
This class implements the BIFEDispatcher related BackEnd dispatcher 
managable by an asynchronous JICPMediatorManager  
@author Giovanni Caire - Telecom Italia LAB S.p.A.
 */
public class NIOBEDispatcher implements NIOMediator, BEConnectionManager, Dispatcher {

    private static final long RESPONSE_TIMEOUT = 60000;
    private long keepAliveTime;
    private boolean enableServerKeepAlive;
    private long lastReceivedTime;
    private boolean active = true;
    private boolean peerActive = true;
    private boolean connectionDropped = false;
    private JICPMediatorManager myMediatorManager;
    private String myID;
    private Properties myProperties;
    private BackEndContainer myContainer = null;
    protected InputManager inpManager;
    protected OutputManager outManager;
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
    Initialize this JICPMediator
     */
    public void init(JICPMediatorManager mgr, String id, Properties props) throws ICPException {
        myMediatorManager = mgr;
        myID = id;
        myProperties = props;

        // Max disconnection time
        long maxDisconnectionTime = JICPProtocol.DEFAULT_MAX_DISCONNECTION_TIME;
        try {
            maxDisconnectionTime = Long.parseLong(props.getProperty(JICPProtocol.MAX_DISCONNECTION_TIME_KEY));
        } catch (Exception e) {
            // Keep default
        }

        // Keep-alive time
        keepAliveTime = JICPProtocol.DEFAULT_KEEP_ALIVE_TIME;
        try {
            keepAliveTime = Long.parseLong(props.getProperty(JICPProtocol.KEEP_ALIVE_TIME_KEY));
        } catch (Exception e) {
            // Keep default
        }

        // Server-keep-alive time
        enableServerKeepAlive = false;
        try {
            enableServerKeepAlive = Boolean.valueOf(props.getProperty("enable-server-keep-alive")).booleanValue();
        } catch (Exception e) {
            // Keep default
        }

        // inpCnt
        int inpCnt = 0;
        try {
            inpCnt = (Integer.parseInt(props.getProperty("lastsid")) + 1) & 0x0f;
        } catch (Exception e) {
            // Keep default
        }
        System.out.println("Next command for FE will have sessionID " + inpCnt);

        /* lastSid
        int lastSid = 0x0f;
        try {
            lastSid = (byte) (Integer.parseInt(props.getProperty("outcnt")) - 1);
            if (lastSid < 0) {
                lastSid = 0x0f;
            }
        } catch (Exception e) {
            // Keep default
        }*/

        FrontEndStub st = new FrontEndStub(this);
        inpManager = new InputManager(inpCnt, st);

        BackEndSkel sk = startBackEndContainer(props);
        outManager = new OutputManager(0x0f, sk, maxDisconnectionTime);
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
            if (myLogger.isLoggable(Logger.CONFIG)) {
                myLogger.log(Logger.CONFIG, "BackEndContainer " + myID + " successfully joined the platform");
            }
            return new BackEndSkel(myContainer);
        } catch (ProfileException pe) {
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
        checkTerminatedInfo(pkt);

        if (peerActive) {
            if (connectionDropped) {
                droppedToDisconnected();
            }

            // Update keep-alive info
            lastReceivedTime = System.currentTimeMillis();

            boolean inp = false;
            byte[] data = pkt.getData();
            if (data.length == 1) {
                inp = (data[0] == 1);
            }
            if (inp) {
                inpManager.setConnection((NIOJICPConnection) c);
                if (myLogger.isLoggable(Logger.CONFIG)) {
                    myLogger.log(Logger.CONFIG, myID + ": New INP Connection establishd");
                }
            } else {
                outManager.setConnection(c);
                if (myLogger.isLoggable(Logger.CONFIG)) {
                    myLogger.log(Logger.CONFIG, myID + ": New OUT Connection establishd");
                }
            }

            return true;
        } else {
            // The remote FrontEnd has terminated spontaneously -->
            // Kill the above container (this will also kill this NIOBEDispatcher).
            kill();
            return false;
        }
    }

    /**
    Notify this NIOMediator that an error occurred on one of the
    Connections it was using. This information is important since,
    unlike normal mediators, a NIOMediator typically does not read
    packets from
    connections on its own (the JICPMediatorManager does that in general).
     */
    public void handleConnectionError(Connection c, Exception e) {
        myLogger.log(Logger.WARNING, "connection error", e);
        if (active && peerActive) {
            // Try assuming it is the input connection
            try {
                inpManager.checkConnection(c);
                myLogger.log(Logger.WARNING, myID + ": IC Disconnection detected");
                inpManager.resetConnection();
            } catch (ICPException icpe) {
                // Then try assuming it is the output connection
                try {
                    outManager.checkConnection(c);
                    myLogger.log(Logger.WARNING, myID + ": OC Disconnection detected");
                    outManager.resetConnection();
                } catch (ICPException icpe2) {
                    // Ignore it
                }
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
        if (pkt.getType() == JICPProtocol.DROP_DOWN_TYPE) {
            // Note that the return packet is written inside the handleDropDown()
            // method since the connection must be closed after the response has
            // been sent back.
            handleDropDown(c, pkt, addr, port);
            return null;
        }

        checkTerminatedInfo(pkt);

        // Update keep-alive info
        lastReceivedTime = System.currentTimeMillis();

        byte type = pkt.getType();
        if (type == JICPProtocol.COMMAND_TYPE) {
            if (peerActive) {
                return outManager.handleCommand(c, pkt);
            } else {
                // The remote FrontEnd has terminated spontaneously -->
                // Kill the above container (this will also kill this NIOBEDispatcher).
                kill();
                return null;
            }
        } else if (type == JICPProtocol.KEEP_ALIVE_TYPE) {
            if (enableServerKeepAlive) {
                inpManager.sendServerKeepAlive();
            }
            return outManager.handleKeepAlive(c, pkt);
        } /* Asynch-reply
        else if (type == JICPProtocol.RESPONSE_TYPE || type == JICPProtocol.ERROR_TYPE) {
        inpManager.handleResponse(c, pkt);
        return null;
        }*/ else {
            throw new ICPException("Unexpected packet type " + type);
        }
    }

    /**
    This is periodically called by the JICPMediatorManager and is
    used by this NIOMediator to evaluate the elapsed time without
    the need of a dedicated thread or timer.
     */
    public final void tick(long currentTime) {
        if (active && !connectionDropped) {
            // 1) If there is a blocking read operation in place check the
            // response timeout
            inpManager.checkResponseTime(currentTime);

            // 2) Evaluate the keep alive
            if (keepAliveTime > 0) {
                if ((currentTime - lastReceivedTime) > (keepAliveTime + RESPONSE_TIMEOUT)) {
                    // Missing keep-alive.
                    // The OUT connection is no longer valid
                    if (outManager.isConnected()) {
                        myLogger.log(Logger.WARNING, myID + ": Missing keep-alive");
                        outManager.resetConnection();
                    }
                    // Check the INP connection. Since this method must return
                    // asap, does it in a separated Thread
                    if (inpManager.isConnected()) {
                        Thread t = new Thread() {

                            public void run() {
                                try {
                                    //JICPPacket pkt = new JICPPacket(JICPProtocol.KEEP_ALIVE_TYPE, JICPProtocol.DEFAULT_INFO, null);
                                    //inpManager.dispatch(pkt, false);
                                    inpManager.sendServerKeepAlive();
                                    if (myLogger.isLoggable(Logger.CONFIG)) {
                                        myLogger.log(Logger.CONFIG, myID + ": IC valid");
                                    }
                                } catch (Exception e) {
                                    // Just do nothing: the INP connection has been reset
                                }
                            }
                        };
                        t.start();
                    }
                }
            }

            // 3) Evaluate the max disconnection time
            if (outManager.checkMaxDisconnectionTime(currentTime)) {
                myLogger.log(Logger.SEVERE, myID + ": Max disconnection time expired.");
                // Consider as if the FrontEnd has terminated spontaneously -->
                // Kill the above container (this will also kill this NIOBEDispatcher).
                kill();
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
        return inpManager.getStub();
    }

    /**
    Make this NIOBEDispatcher terminate.
     */
    public void shutdown() {
        active = false;
        if (myLogger.isLoggable(Logger.INFO)) {
            myLogger.log(Logger.INFO, myID + ": shutting down");
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
    public byte[] dispatch(byte[] payload, boolean flush, int oldSessionId) throws ICPException {
        if (connectionDropped) {
            // Move from DROPPED state to DISCONNECTED state and wait
            // for the FE to reconnect
            droppedToDisconnected();
            requestRefresh();
            throw new ICPException("Connection dropped");
        } else {
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
        if (myLogger.isLoggable(Logger.INFO)) {
            myLogger.log(Logger.INFO, myID + ": DROP_DOWN request received.");
        }

        try {
            // If the INP connection is down or we have some postponed command
            // to flush, refuse dropping the connection
            if (inpManager.isConnected() && inpManager.isEmpty()) {
                JICPPacket rsp = new JICPPacket(JICPProtocol.RESPONSE_TYPE, JICPProtocol.DEFAULT_INFO, null);
                c.writePacket(rsp);

                inpManager.resetConnection();
                outManager.resetConnection();
                connectionDropped = true;
            } else {
                myLogger.log(Logger.WARNING, myID + ": DROP_DOWN request refused.");
                JICPPacket rsp = new JICPPacket(JICPProtocol.ERROR_TYPE, getReconnectInfo(), null);
                c.writePacket(rsp);
            }
        } catch (Exception e) {
            myLogger.log(Logger.WARNING, myID + ": Error writing DROP_DOWN response. " + e);
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
        outManager.setExpirationDeadline();
    }

    /**
    Request the FE to refresh the connection.
     */
    protected void requestRefresh() {
    }

    public boolean isConnected() {
        return inpManager.isConnected() && outManager.isConnected();
    }

    private void updateConnectedState() {
        myProperties.put(BEManagementHelper.CONNECTED, (isConnected() ? "true" : "false"));
    }

    /**
    Inner class InputManager.
    This class manages the delivery of commands to the FrontEnd
     */
    protected class InputManager {

        private NIOJICPConnection myConnection;
        private boolean dispatching = false;
        private boolean connectionRefreshed;
        private boolean waitingForFlush;
        private long readStartTime = -1;
        private JICPPacket currentReply;
        private Object dispatchLock = new Object();
        private int inpCnt;
        private FrontEndStub myStub;

        InputManager(int c, FrontEndStub s) {
            inpCnt = c;
            myStub = s;
        }

        FrontEndStub getStub() {
            return myStub;
        }

        synchronized void setConnection(NIOJICPConnection c) {
            // Reset the old connection
            resetConnection();

            // Set the new connection
            myConnection = c;
            connectionRefreshed = true;
            waitingForFlush = myStub.flush();
            //myContainer.notifyInputConnectionReady();
            updateConnectedState();
        }

        synchronized void resetConnection() {
            // Close the connection if it was in place
            if (myConnection != null) {
                close(myConnection);
                myConnection = null;
            }
            // Asynch-reply
            // If there was someone waiting for a response on the
            // connection notify it.
            //notifyAll();
            updateConnectedState();
        }

        final void checkConnection(Connection c) throws ICPException {
            if (c != myConnection) {
                throw new ICPException("Wrong connection");
            }
        }

        final boolean isConnected() {
            return myConnection != null;
        }

        final boolean isEmpty() {
            // We are empty if we are not dispatching a JICPPacket and our stub
            // has no postponed commands waiting to be delivered.
            return (!dispatching) && myStub.isEmpty();
        }

        void shutdown() {
            resetConnection();
        }

        /**
        Dispatch a JICP command to the FE and get back a reply.
        This method must NOT be executed in mutual exclusion with
        setConnection() and resetConnection() since it performs a
        blocking read operation --> It can't just be declared synchronized.
         */
        final JICPPacket dispatch(JICPPacket pkt, boolean flush, int oldSessionId) throws ICPException {
            synchronized (dispatchLock) {
                dispatching = true;
                try {
                    synchronized (this) {
                        if ((!active) || (myConnection == null) || (waitingForFlush && (!flush))) {
                            // If we are waiting for flushed packets and the current packet
                            // is a normal (i.e. non-flushed) one, then throw an exception -->
                            // The packet will be put in the queue of packets to be flushed
                            throw new ICPException("Unreachable");
                        }

                        waitingForFlush = false;
                        connectionRefreshed = false;
                    }

                    try {
            			if (flush && oldSessionId != -1) {
            				// This is a postponed command whose previous dispatch failed --> Use the
            				// old sessionId, so that if the server already received it (previous dispatch 
            				// failed due to a response delivering error) the command will be recognized 
            				// as duplicated and properly managed
            				inpCnt = oldSessionId;
            			}
                        pkt.setSessionID((byte) inpCnt);
                        if (myLogger.isLoggable(Logger.FINE)) {
                            myLogger.log(Logger.FINE, myID + ": Sending command " + inpCnt + " to FE");
                        }

                        long start = System.currentTimeMillis();
                        myConnection.writePacket(pkt);
                        // Asynch-reply: JICPPacket reply = waitForReply(RESPONSE_TIMEOUT);
                        readStartTime = System.currentTimeMillis();
                        JICPPacket reply = myConnection.readPacket();
                        readStartTime = -1;
                        checkTerminatedInfo(reply);
                        lastReceivedTime = System.currentTimeMillis();
                        long end = lastReceivedTime;
                        System.out.println("INP Session " + inpCnt + ". Dispatching time = " + (end - start));

                        if (myLogger.isLoggable(Logger.FINER)) {
                            myLogger.log(Logger.FINER, myID + ": Received response " + inpCnt + " from FE");
                        }
                        if (reply.getType() == JICPProtocol.ERROR_TYPE) {
                            // Communication OK, but there was a JICP error on the peer
                            throw new ICPException(new String(pkt.getData()));
                        }
                        if (!peerActive) {
                            // This is the response to an exit command --> Suicide, without
                            // killing the above container since it is already dying.
                            NIOBEDispatcher.this.shutdown();
                        }
                        return reply;
                    } catch (NullPointerException npe) {
                        // This can happen if a resetConnection() occurs just before
                        // myConnection.writePacket()/readPacket() is called.
                        throw new ICPException("Connection reset.");
                    } catch (IOException ioe) {
                        synchronized (this) {
                            if (myConnection != null && !connectionRefreshed) {
                                // There was an IO exception writing data to the connection
                                // --> reset the connection.
                                myLogger.log(Logger.WARNING, myID + ": IOException IC. " + ioe);
                                resetConnection();
                            }
                        }
                        readStartTime = -1;
                        throw new ICPDispatchException("Dispatching error.", ioe, inpCnt);
                    }
                    finally {
                        inpCnt = (inpCnt + 1) & 0x0f;
                    }
                } finally {
                    dispatching = false;
                }
            }
        }

        public final void checkResponseTime(long currentTime) {
            if (readStartTime > 0 && (currentTime - readStartTime) > RESPONSE_TIMEOUT) {
                myLogger.log(Logger.WARNING, myID + ": Response timeout expired.");
                resetConnection();
            }
        }

        public void sendServerKeepAlive() throws ICPException {
            JICPPacket pkt = new JICPPacket(JICPProtocol.KEEP_ALIVE_TYPE, JICPProtocol.DEFAULT_INFO, null);
            dispatch(pkt, false, -1);
        }
        /* Asynch-reply
        final synchronized void handleResponse(Connection c, JICPPacket reply) throws ICPException {
        checkConnection(c);
        currentReply = reply;
        notifyAll();
        }

        private synchronized JICPPacket waitForReply(long timeout) throws ICPException, IOException {
        try {
        if (currentReply == null) {
        wait(timeout);
        if (currentReply == null) {
        if (isConnected()) {
        if (connectionRefreshed) {
        throw new ICPException("Connection refreshed");
        }
        else {
        throw new IOException("Response timeout expired");
        }
        }
        else {
        throw new ICPException("Connection reset");
        }
        }
        }
        JICPPacket tmp = currentReply;
        currentReply = null;
        return tmp;
        }
        catch (InterruptedException ie) {
        throw new ICPException("Interrupted");
        }
        }*/
    } // END of inner class InputManager

    /**
    Inner class OutputManager
    This class manages the reception of commands and keep-alive
    packets from the FrontEnd.
    This class also manages the maxDisconnectionTime, i.e. the remote
    FrontEnd is considered dead if it cannot re-establish the
    OUT connection within the maxDisconnectionTime.
     */
    protected class OutputManager {

        private Connection myConnection;
        private JICPPacket lastResponse;
        private int lastSid;
        private BackEndSkel mySkel;
        private long maxDisconnectionTime, expirationDeadline;

        OutputManager(int n, BackEndSkel s, long t) {
            lastSid = n;
            mySkel = s;
            maxDisconnectionTime = t;
        }

        synchronized void setConnection(Connection c) {
            // Close the old connection if any
            if (myConnection != null) {
                close(myConnection);
            }
            // Set the new connection
            myConnection = c;
            updateConnectedState();
        }

        synchronized void resetConnection() {
            if (myConnection != null) {
                setExpirationDeadline();
                close(myConnection);
            }
            myConnection = null;
            updateConnectedState();
        }

        synchronized void setExpirationDeadline() {
            expirationDeadline = System.currentTimeMillis() + maxDisconnectionTime;
        }

        final void checkConnection(Connection c) throws ICPException {
            if (c != myConnection) {
                throw new ICPException("Wrong connection");
            }
        }

        final boolean isConnected() {
            return (myConnection != null);
        }

        void shutdown() {
            resetConnection();
        }

        final synchronized JICPPacket handleCommand(Connection c, JICPPacket cmd) throws ICPException {
            checkConnection(c);
            JICPPacket reply = null;
            byte sid = cmd.getSessionID();
            if (sid == lastSid && lastResponse != null) {
                myLogger.log(Logger.WARNING, myID + ": Duplicated command from FE " + sid);
                reply = lastResponse;
            } else {
                if (myLogger.isLoggable(Logger.FINE)) {
                    myLogger.log(Logger.FINE, myID + ": Received command " + sid + " from FE");
                }

                byte[] rspData = mySkel.handleCommand(cmd.getData());
                if (myLogger.isLoggable(Logger.FINER)) {
                    myLogger.log(Logger.FINER, myID + ": Command " + sid + " from FE served ");
                }

                reply = new JICPPacket(JICPProtocol.RESPONSE_TYPE, getReconnectInfo(), rspData);
                reply.setSessionID(sid);
                lastSid = sid;
                lastResponse = reply;
            }
            return reply;
        }

        synchronized JICPPacket handleKeepAlive(Connection c, JICPPacket command) throws ICPException {
            checkConnection(c);
            if (myLogger.isLoggable(Logger.FINEST)) {
                myLogger.log(Logger.FINEST, myID + ": Keep-alive received");
            }
            return new JICPPacket(JICPProtocol.RESPONSE_TYPE, getReconnectInfo(), null);
        }

        final synchronized boolean checkMaxDisconnectionTime(long currentTime) {
            return (!isConnected()) && (currentTime > expirationDeadline);
        }
    } // END of inner class OutputManager

    private final byte getReconnectInfo() {
        byte info = JICPProtocol.DEFAULT_INFO;
        // If the inpConnection is null request the FrontEnd to reconnect
        if (!inpManager.isConnected()) {
            info |= JICPProtocol.RECONNECT_INFO;
        }
        return info;
    }

    private final void checkTerminatedInfo(JICPPacket pkt) {
        if ((pkt.getInfo() & JICPProtocol.TERMINATED_INFO) != 0) {
            peerActive = false;
            if (myLogger.isLoggable(Logger.INFO)) {
                myLogger.log(Logger.INFO, myID + ": Peer termination notification received");
            }
        }
    }

    private void close(Connection c) {
        try {
            c.close();
        } catch (IOException ioe) {
        }
    }
}

