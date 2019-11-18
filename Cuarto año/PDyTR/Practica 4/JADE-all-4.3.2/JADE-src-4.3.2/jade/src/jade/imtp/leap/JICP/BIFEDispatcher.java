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

import jade.core.MicroRuntime;
import jade.core.FEConnectionManager;
import jade.core.FrontEnd;
import jade.core.BackEnd;
import jade.core.IMTPException;
import jade.core.TimerDispatcher;
import jade.core.Timer;
import jade.core.TimerListener;
import jade.core.Specifier;
import jade.mtp.TransportAddress;
import jade.imtp.leap.BackEndStub;
import jade.imtp.leap.MicroSkeleton;
import jade.imtp.leap.FrontEndSkel;
import jade.imtp.leap.Dispatcher;
import jade.imtp.leap.ICPException;
import jade.imtp.leap.ConnectionListener;
import jade.util.leap.Properties;
import jade.util.Logger;

import java.io.*;
import java.util.Vector;

/**
 * Class declaration
 * @author Giovanni Caire - TILAB
 */
public class BIFEDispatcher implements FEConnectionManager, Dispatcher, TimerListener, Runnable {

    protected static final byte INP = (byte) 1;
    protected static final byte OUT = (byte) 0;
    private static final int RESPONSE_TIMEOUT = 30000;
    protected String myMediatorClass = "jade.imtp.leap.JICP.BIBEDispatcher";
    private MicroSkeleton mySkel = null;
    private BackEndStub myStub = null;
    // Variables related to the connection with the Mediator
    protected TransportAddress mediatorTA;
    private String myMediatorID;
    private long retryTime = JICPProtocol.DEFAULT_RETRY_TIME;
    private long maxDisconnectionTime = JICPProtocol.DEFAULT_MAX_DISCONNECTION_TIME;
    private long keepAliveTime = JICPProtocol.DEFAULT_KEEP_ALIVE_TIME;
    private long connectionDropDownTime = -1;
    private Timer kaTimer, cdTimer;
    private Properties props;
    protected Connection outConnection;
    protected InputManager myInputManager;
    private ConnectionListener myConnectionListener;
    private boolean active = true;
    private boolean connectionDropped = false;
    private boolean waitingForFlush = false;
    protected boolean refreshingInput = false;
    protected boolean refreshingOutput = false;
    private byte lastSid = 0x0f;
    private int outCnt = 0;
    private Thread terminator;
    private String beAddrsText;
    private String[] backEndAddresses;
    private Logger myLogger = Logger.getMyLogger(getClass().getName());

    //////////////////////////////////////////////
    // FEConnectionManager interface implementation
    //////////////////////////////////////////////
    /**
     * Connect to a remote BackEnd and return a stub to communicate with it
     */
    public BackEnd getBackEnd(FrontEnd fe, Properties props) throws IMTPException {
        this.props = props;
        myMediatorID = props.getProperty(JICPProtocol.MEDIATOR_ID_KEY);
        try {

            beAddrsText = props.getProperty(FrontEnd.REMOTE_BACK_END_ADDRESSES);
            backEndAddresses = parseBackEndAddresses(beAddrsText);

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
            if (myLogger.isLoggable(Logger.CONFIG)) {
                myLogger.log(Logger.CONFIG, "Remote URL=" + JICPProtocol.getInstance().addrToString(mediatorTA));
            }

            // Mediator class
            String tmp = props.getProperty(JICPProtocol.MEDIATOR_CLASS_KEY);
            if (tmp != null) {
                myMediatorClass = tmp;
            } else {
                //set the default mediator class.
                props.setProperty(JICPProtocol.MEDIATOR_CLASS_KEY, myMediatorClass);
            }
            if (myLogger.isLoggable(Logger.CONFIG)) {
                myLogger.log(Logger.CONFIG, "Mediator class=" + myMediatorClass);
            }

            // Read (re)connection retry time
            tmp = props.getProperty(JICPProtocol.RECONNECTION_RETRY_TIME_KEY);
            try {
                retryTime = Long.parseLong(tmp);
            } catch (Exception e) {
                // Use default
            }
            if (myLogger.isLoggable(Logger.CONFIG)) {
                myLogger.log(Logger.CONFIG, "Reconnection time=" + retryTime);
            }

            // Read Max disconnection time
            tmp = props.getProperty(JICPProtocol.MAX_DISCONNECTION_TIME_KEY);
            try {
                maxDisconnectionTime = Long.parseLong(tmp);
            } catch (Exception e) {
                // Use default
                props.setProperty(JICPProtocol.MAX_DISCONNECTION_TIME_KEY, String.valueOf(maxDisconnectionTime));
            }
            if (myLogger.isLoggable(Logger.CONFIG)) {
                myLogger.log(Logger.CONFIG, "Max discon. time=" + maxDisconnectionTime);
            }

            // Read Keep-alive time
            tmp = props.getProperty(JICPProtocol.KEEP_ALIVE_TIME_KEY);
            try {
                keepAliveTime = Long.parseLong(tmp);
            } catch (Exception e) {
                // Use default
                props.setProperty(JICPProtocol.KEEP_ALIVE_TIME_KEY, String.valueOf(keepAliveTime));
            }
            if (myLogger.isLoggable(Logger.CONFIG)) {
                myLogger.log(Logger.CONFIG, "Keep-alive time=" + keepAliveTime);
            }

            // Read Connection-drop-down time
            tmp = props.getProperty(JICPProtocol.DROP_DOWN_TIME_KEY);
            try {
                connectionDropDownTime = Long.parseLong(tmp);
            } catch (Exception e) {
                // Use default
            }
            if (myLogger.isLoggable(Logger.CONFIG)) {
                myLogger.log(Logger.CONFIG, "Connection-drop-down time=" + connectionDropDownTime);
            }

            // Retrieve the ConnectionListener if any
            try {
                Object obj = props.get("connection-listener");
                if (obj instanceof ConnectionListener) {
                    myConnectionListener = (ConnectionListener) obj;
                } else {
                    myConnectionListener = (ConnectionListener) Class.forName(obj.toString()).newInstance();
                }
            } catch (Exception e) {
                // Just ignore it
            }

            // Create the BackEnd stub and the FrontEnd skeleton
            myStub = new BackEndStub(this, props);
            mySkel = new FrontEndSkel(fe);

            outConnection = createBackEnd();

            return myStub;
        } catch (ICPException icpe) {
            throw new IMTPException("Connection error", icpe);
        }
    }

    /**
    Make this BIFEDispatcher terminate.
     */
    public synchronized void shutdown() {
        active = false;

        terminator = Thread.currentThread();
        if (terminator != myInputManager) {
            // This is a self-initiated shut down --> we must explicitly
            // notify the BackEnd.
            JICPPacket terminationPacket = null;
            try {
                if (connectionDropped) {
                    outConnection = openConnection(mediatorTA, RESPONSE_TIMEOUT);
                    terminationPacket = new JICPPacket(JICPProtocol.CONNECT_MEDIATOR_TYPE, JICPProtocol.TERMINATED_INFO, mediatorTA.getFile(), new byte[]{OUT});
                } else {
                    terminationPacket = new JICPPacket(JICPProtocol.COMMAND_TYPE, JICPProtocol.TERMINATED_INFO, null);
                }

                if (outConnection != null) {
                    myLogger.log(Logger.INFO, "Sending termination notification");
                    writePacket(terminationPacket, outConnection);
                }
            } catch (Exception e) {
                // When the BackEnd receives the termination notification,
                // it just closes the connection --> we always have this
                // exception
            }
        }
    }

    /**
    Send the CREATE_MEDIATOR command with the necessary parameter
    in order to create the BackEnd in the fixed network.
    Executed
    - at bootstrap time by the thread that creates the FrontEndContainer.
    - To re-attach to the platform after a fault of the BackEnd
     */
    private JICPConnection createBackEnd() throws IMTPException {
        StringBuffer sb = BackEndStub.encodeCreateMediatorRequest(props);
        if (myMediatorID != null) {
            // This is a request to re-create my expired back-end
            BackEndStub.appendProp(sb, JICPProtocol.MEDIATOR_ID_KEY, myMediatorID);
            BackEndStub.appendProp(sb, "outcnt", String.valueOf(outCnt));
            BackEndStub.appendProp(sb, "lastsid", String.valueOf(lastSid));
        }
        JICPPacket pkt = new JICPPacket(JICPProtocol.CREATE_MEDIATOR_TYPE, JICPProtocol.DEFAULT_INFO, null, sb.toString().getBytes());

        // Try first with the current transport address, then with the various backup addresses
        for (int i = -1; i < backEndAddresses.length; i++) {

            if (i >= 0) {
                // Set the mediator address to a new address..
                String addr = backEndAddresses[i];
                int colonPos = addr.indexOf(':');
                String host = addr.substring(0, colonPos);
                String port = addr.substring(colonPos + 1, addr.length());
                mediatorTA = new JICPAddress(host, port, myMediatorID, "");
            }

            try {
                myLogger.log(Logger.INFO, "Creating BackEnd on jicp://" + mediatorTA.getHost() + ":" + mediatorTA.getPort());

                JICPConnection con = openConnection(mediatorTA, RESPONSE_TIMEOUT);

                writePacket(pkt, con);

                pkt = con.readPacket();

                String replyMsg = new String(pkt.getData());
                if (pkt.getType() != JICPProtocol.ERROR_TYPE) {
                    // BackEnd creation successful
                    BackEndStub.parseCreateMediatorResponse(replyMsg, props);
                    myMediatorID = props.getProperty(JICPProtocol.MEDIATOR_ID_KEY);
                    // Complete the mediator address with the mediator ID
                    mediatorTA = new JICPAddress(mediatorTA.getHost(), mediatorTA.getPort(), myMediatorID, null);
                    myLogger.log(Logger.INFO, "BackEnd OK: mediator-id = " + myMediatorID);
                    // The BE has just been created --> refresh the INP connection too
                    refreshInp();
                    return con;
                } else {
                    myLogger.log(Logger.WARNING, "Mediator error: " + replyMsg);
                    if (myConnectionListener != null && replyMsg != null && replyMsg.startsWith(JICPProtocol.NOT_AUTHORIZED_ERROR)) {
                        myConnectionListener.handleConnectionEvent(ConnectionListener.NOT_AUTHORIZED, replyMsg);
                    }
                }
            } catch (IOException ioe) {
                // Ignore it, and try the next address...
                myLogger.log(Logger.WARNING, "Connection error. " + ioe.toString());
            }
        }

        // No address succeeded: try to handle the problem...
        throw new IMTPException("Error creating the BackEnd.");
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
            dispatchWhileDropped();
            throw new ICPException("Connection dropped");
        } else {
            if (outConnection != null) {
                if (waitingForFlush && !flush) {
                    throw new ICPException("Upsetting dispatching order");
                }
                waitingForFlush = false;

                int status = 0;
                JICPPacket pkt = new JICPPacket(JICPProtocol.COMMAND_TYPE, JICPProtocol.DEFAULT_INFO, payload);
    			if (flush && oldSessionId != -1) {
    				// This is a postponed command whose previous dispatch failed --> Use the
    				// old sessionId, so that if the server already received it (previous dispatch 
    				// failed due to a response delivering error) the command will be recognized 
    				// as duplicated and properly managed
    				outCnt = oldSessionId;
    			}
                pkt.setSessionID((byte) outCnt);
                myLogger.log(Logger.FINE, "Issuing outgoing command " + outCnt);
                try {
                    writePacket(pkt, outConnection);
                    status = 1;
                    pkt = outConnection.readPacket();
                    if (pkt.getSessionID() != outCnt) {
                        pkt = outConnection.readPacket();
                    }
                    status = 2;
                    if (myLogger.isLoggable(Logger.FINER)) {
                        myLogger.log(Logger.FINER, "Response received " + pkt.getSessionID());
                    }
                    if (pkt.getType() == JICPProtocol.ERROR_TYPE) {
                        // Communication OK, but there was a JICP error on the peer
                        throw new ICPException(new String(pkt.getData()));
                    }
                    if ((pkt.getInfo() & JICPProtocol.RECONNECT_INFO) != 0) {
                        // The BackEnd is considering the input connection no longer valid
                        refreshInp();
                    }
                    return pkt.getData();
                } catch (IOException ioe) {
                    // Can't reach the BackEnd.
                    myLogger.log(Logger.WARNING, "IOException OC[" + status + "]" + ioe);
                    refreshOut();
                    throw new ICPException("Dispatching error.", ioe);
                }
                finally {
                    outCnt = (outCnt + 1) & 0x0f;
                }
            } else {
                System.out.println("Out Connection null: refreshingOut = " + refreshingOutput);
                throw new ICPException("Unreachable");
            }
        }
    }
    // These variables are only used within the InputManager class,
    // but are declared externally since they must "survive" when
    // an InputManager is replaced
    private JICPPacket lastResponse = null;
    private int cnt = 0;

    /**
    Inner class InputManager.
    This class is responsible for serving incoming commands
     */
    private class InputManager extends Thread {

        private int myId;
        private Connection myConnection = null;

        public void run() {
            myId = cnt++;
            if (myLogger.isLoggable(Logger.INFO)) {
                myLogger.log(Logger.INFO, "IM-" + myId + " started");
            }

            int status = 0;
            connectInp();
            //connect(INP);
            try {
                while (isConnected()) {
                    status = 0;
                    JICPPacket pkt = myConnection.readPacket();
                    // HACK!: For some misterious reason just after a BE re-creation it
                    // may happen that we get a RESPONSE packet here. Waiting for a cleaner
                    // solution, we just ignore it and go back reading the next incoming
                    // command
                    if (pkt.getType() == JICPProtocol.RESPONSE_TYPE) {
                        myLogger.log(Logger.WARNING, "Unexpected response packet received on INP connection. Ignore it");
                        continue;
                    }
                    status = 1;
                    byte sid = pkt.getSessionID();
                    if (sid == lastSid) {
                        // Duplicated packet
                        if (myLogger.isLoggable(Logger.WARNING)) {
                            myLogger.log(Logger.WARNING, "Duplicated packet from BE: pkt-type=" + pkt.getType() + " info=" + pkt.getInfo() + " SID=" + sid);
                        }
                        pkt = lastResponse;
                    } else {
                        if (pkt.getType() == JICPProtocol.KEEP_ALIVE_TYPE) {
                            // Keep-alive
                            pkt = new JICPPacket(JICPProtocol.RESPONSE_TYPE, JICPProtocol.DEFAULT_INFO, null);
                        } else {
                            // Incoming command
                            if (myLogger.isLoggable(Logger.FINE)) {
                                myLogger.log(Logger.FINE, "Incoming command received " + sid + " pkt-type=" + pkt.getType());
                            }
                            byte[] rspData = mySkel.handleCommand(pkt.getData());
                            if (myLogger.isLoggable(Logger.FINER)) {
                                myLogger.log(Logger.FINER, "Incoming command served " + sid);
                            }
                            pkt = new JICPPacket(JICPProtocol.RESPONSE_TYPE, JICPProtocol.DEFAULT_INFO, rspData);
                        }
                        pkt.setSessionID(sid);
                        if (Thread.currentThread() == terminator) {
                            // Attach the TERMINATED_INFO flag to the response
                            pkt.setTerminatedInfo(true);
                        }
                        lastSid = sid;
                        lastResponse = pkt;
                    }
                    status = 2;
                    writePacket(pkt, myConnection);
                    status = 3;
                }
            } catch (IOException ioe) {
                if (active) {
                    myLogger.log(Logger.WARNING, "IOException IC[" + status + "]" + ioe);
                    refreshInp();
                }
            }

            if (myLogger.isLoggable(Logger.INFO)) {
                myLogger.log(Logger.INFO, "IM-" + myId + " terminated");
            }
        }

        private void close() {
            try {
                myConnection.close();
            } catch (Exception e) {
            }
            myConnection = null;
        }

        private final void setConnection(Connection c) {
            myConnection = c;
        }

        private final boolean isConnected() {
            return myConnection != null;
        }
    } // END of inner class InputManager

    /**
    Close the current InputManager (if any) and start a new one
     */
    protected synchronized void refreshInp() {
        // Avoid 2 refreshing processes at the same time.
        // Also avoid restoring the INP connection just after a DROP_DOWN
        if (active && !refreshingInput && !connectionDropped) {
            // Close the current InputManager
            if (myInputManager != null && myInputManager.isConnected()) {
                myInputManager.close();
                if (outConnection != null && myConnectionListener != null) {
                    myConnectionListener.handleConnectionEvent(ConnectionListener.DISCONNECTED, null);
                }
            }

            // Start a new InputManager
            refreshingInput = true;
            myInputManager = new InputManager();
            myInputManager.start();
        }
    }

    /**
    Close the current outConnection (if any) and starts a new thread
    that asynchronously tries to restore it.
     */
    protected synchronized void refreshOut() {
        // Avoid having two refreshing processes at the same time
        if (!refreshingOutput) {
            // Close the outConnection
            if (outConnection != null) {
                try {
                    outConnection.close();
                } catch (Exception e) {
                }
                outConnection = null;
                if (myInputManager.isConnected() && myConnectionListener != null) {
                    myConnectionListener.handleConnectionEvent(ConnectionListener.DISCONNECTED, null);
                }
            }

            // Asynchronously try to recreate the outConnection
            refreshingOutput = true;
            Thread t = new Thread(this);
            t.start();
        }
    }

    /**
    Asynchronously restore the OUT connection
     */
    public void run() {
        connectOut();
        //connect(OUT);
    }

    /* TO BE REMOVED
    private void connect(byte type) {
    int cnt = 0;
    long startTime = System.currentTimeMillis();
    while (active) {
    try {
    if (myLogger.isLoggable(Logger.INFO)) {
    myLogger.log(Logger.INFO, "Connecting to "+mediatorTA.getHost()+":"+mediatorTA.getPort()+" "+type+"("+cnt+")");
    }
    int t = (type == OUT ? RESPONSE_TIMEOUT : -1);
    Connection c = openConnection(mediatorTA, t);
    JICPPacket pkt = new JICPPacket(JICPProtocol.CONNECT_MEDIATOR_TYPE, JICPProtocol.DEFAULT_INFO, mediatorTA.getFile(), new byte[]{type});
    writePacket(pkt, c);
    pkt = c.readPacket();
    if (pkt.getType() == JICPProtocol.ERROR_TYPE) {
    String errorMsg = new String(pkt.getData());
    myLogger.log(Logger.WARNING, "JICP Error "+type+". "+errorMsg);
    c.close();
    if (errorMsg.equals(JICPProtocol.NOT_FOUND_ERROR)) {
    // The JICPMediatorManager didn't find my Mediator anymore. Either
    // there was a fault our max disconnection time expired.
    // Try to recreate the BackEnd
    if (type == OUT) {
    // BackEnd recreation is attempted only when restoring the
    // OUT connection since the BackEnd uses the connection that
    // creates it to receive outgoing commands. Moreover this ensures
    // that (if the BackEnd is created on a different host) we do not
    // end up with the INP and OUT connections pointing to different
    // hosts.
    // Finally, if BE re-creation fails, we behave as if there was an
    // IOException when trying to reconnect.
    try {
    handleBENotFound();
    // In some cases the INP connection may be re-established by another
    // thread just after the BackEnd has been re-created but the
    // outConnection has not been set yet. In these cases the resynch
    // process may fail since it finds the outConnection null.
    // The synchronized block avoids this.
    synchronized (this) {
    c = createBackEnd();
    handleReconnection(c, type);
    }
    }
    catch (IMTPException imtpe) {
    // Behave as if there was an IOException --> go back sleeping
    throw new IOException("BE-recreation failed");
    }
    }
    else {
    // In case the outConnection still appears to be OK, refresh it.
    refreshOut();
    // Then behave as if there was an IOException --> go back sleeping
    throw new IOException();
    }
    }
    else {
    // There was a JICP error. Abort
    handleError();
    }
    }
    else {
    // The local-host address may have changed
    props.setProperty(JICPProtocol.LOCAL_HOST_KEY, new String(pkt.getData()));
    if (myLogger.isLoggable(Logger.INFO)) {
    myLogger.log(Logger.INFO, "Connect OK "+type);
    }
    handleReconnection(c, type);
    }
    return;
    }
    catch (IOException ioe) {
    myLogger.log(Logger.WARNING, "Connect failed "+type+". "+ioe);
    cnt++;
    if (type == OUT) {
    // Max disconnection time expiration is detected only when
    // restoring the OUT connection. In this way we avoid having
    // one connection restored while the other is declared dead.
    if ((System.currentTimeMillis() - startTime) > maxDisconnectionTime) {
    handleError();
    return;
    }
    }

    // Wait a bit before trying again
    try {
    Thread.sleep(retryTime);
    }
    catch (Exception e) {}
    }
    }
    }*/
    private void connectInp() {
        int cnt = 0;
        while (active) {
            try {
                synchronized (this) { // Mutual exclusion with BE re-creation
                    myLogger.log(Logger.INFO, "Connecting to " + mediatorTA.getHost() + ":" + mediatorTA.getPort() + " " + cnt + " (INP)");
                    Connection c = openConnection(mediatorTA, -1);
                    JICPPacket pkt = new JICPPacket(JICPProtocol.CONNECT_MEDIATOR_TYPE, JICPProtocol.DEFAULT_INFO, mediatorTA.getFile(), new byte[]{INP});
                    writePacket(pkt, c);
                    pkt = c.readPacket();
                    if (pkt.getType() == JICPProtocol.ERROR_TYPE) {
                        String errorMsg = new String(pkt.getData());
                        myLogger.log(Logger.WARNING, "JICP Error (INP). " + errorMsg);
                        c.close();
                        // In case the outConnection still appears to be OK, refresh it.
                        refreshOut();
                        refreshingInput = false;
                    } else {
                        // The local-host address may have changed
                        props.setProperty(JICPProtocol.LOCAL_HOST_KEY, new String(pkt.getData()));
                        myLogger.log(Logger.INFO, "Connect OK (INP)");
                        handleInpReconnection(c);
                    }
                    return;
                }
            } catch (IOException ioe) {
                myLogger.log(Logger.WARNING, "Connect failed (INP). " + ioe);
            }

            // Wait a bit before trying again
            cnt++;
            waitABit(retryTime);
        }
    }

    private void connectOut() {
        int cnt = 0;
        long startTime = System.currentTimeMillis();
        boolean backEndExists = true;
        while (active) {
            try {
                if (backEndExists) {
                    myLogger.log(Logger.INFO, "Connecting to " + mediatorTA.getHost() + ":" + mediatorTA.getPort() + " " + cnt + " (OUT)");
                    Connection c = openConnection(mediatorTA, RESPONSE_TIMEOUT);
                    JICPPacket pkt = new JICPPacket(JICPProtocol.CONNECT_MEDIATOR_TYPE, JICPProtocol.DEFAULT_INFO, mediatorTA.getFile(), new byte[]{OUT});
                    writePacket(pkt, c);
                    pkt = c.readPacket();
                    if (pkt.getType() == JICPProtocol.ERROR_TYPE) {
                        String errorMsg = new String(pkt.getData());
                        myLogger.log(Logger.WARNING, "JICP Error (OUT). " + errorMsg);
                        c.close();
                        if (errorMsg.equals(JICPProtocol.NOT_FOUND_ERROR)) {
                            // The JICPMediatorManager didn't find my Mediator anymore. Either
                            // there was a fault our max disconnection time expired.
                            // Try to recreate the BackEnd
                            handleBENotFound();
                            backEndExists = false;
                            continue;
                        } else {
                            // There was a JICP error. Abort
                            handleError();
                            return;
                        }
                    } else {
                        // The local-host address may have changed
                        props.setProperty(JICPProtocol.LOCAL_HOST_KEY, new String(pkt.getData()));
                        myLogger.log(Logger.INFO, "Connect OK (OUT)");
                        handleOutReconnection(c);
                        return;
                    }
                } else {
                    // Try to recreate the BE
                    synchronized (this) {
                        Connection c = createBackEnd();
                        handleOutReconnection(c);
                        return;
                    }
                }
            } catch (IOException ioe) {
                myLogger.log(Logger.WARNING, "Connect failed (OUT). " + ioe);
            } catch (IMTPException imtpe) {
                myLogger.log(Logger.WARNING, "BE recreation failed.");
            }

            if ((System.currentTimeMillis() - startTime) > maxDisconnectionTime) {
                handleError();
                return;
            }

            // Wait a bit before trying again
            cnt++;
            waitABit(retryTime);
        }
    }

    private void waitABit(long period) {
        try {
            Thread.sleep(period);
        } catch (Exception e) {
        }
    }

    protected synchronized void handleInpReconnection(Connection c) {
        myInputManager.setConnection(c);
        refreshingInput = false;
        if (outConnection != null && myConnectionListener != null) {
            myConnectionListener.handleConnectionEvent(ConnectionListener.RECONNECTED, null);
        }
    }

    protected synchronized void handleOutReconnection(Connection c) {
        if (connectionDropped) {
            // If we have just reconnected after a connection drop-down,
            // refresh the INP connection too.
            connectionDropped = false;
            refreshInp();
        }

        outConnection = c;
        refreshingOutput = false;
        // The Output connection is available again -->
        // Activate postponed commands flushing
        waitingForFlush = myStub.flush();
        if (myInputManager.isConnected() && myConnectionListener != null) {
            myConnectionListener.handleConnectionEvent(ConnectionListener.RECONNECTED, null);
        }
    }

    /* TO BE REMOVED
    protected synchronized void handleReconnection(Connection c, byte type) {
    boolean transition = false;
    if (type == INP) {
    myInputManager.setConnection(c);
    if (outConnection != null) {
    transition = true;
    }
    }
    else if (type == OUT) {
    if (connectionDropped) {
    // If we have just reconnected after a connection drop-down,
    // refresh the INP connection too.
    connectionDropped = false;
    refreshInp();
    }

    outConnection = c;
    refreshingOutput = false;
    // The Output connection is available again -->
    // Activate postponed commands flushing
    waitingForFlush = myStub.flush();
    if (myInputManager.isConnected()) {
    transition = true;
    }
    }
    if (transition && myConnectionListener != null) {
    myConnectionListener.handleConnectionEvent(ConnectionListener.RECONNECTED, null);
    }
    }*/
    private void handleError() {
        myLogger.log(Logger.SEVERE, "Can't reconnect (" + System.currentTimeMillis() + ")");
        (new Exception("Dummy")).printStackTrace();

        if (myConnectionListener != null) {
            myConnectionListener.handleConnectionEvent(ConnectionListener.RECONNECTION_FAILURE, null);
        }
        myInputManager.close();
        active = false;
    }

    private String[] parseBackEndAddresses(String addressesText) {
        Vector addrs = Specifier.parseList(addressesText, ';');
        // Convert the list into an array of strings
        String[] result = new String[addrs.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = (String) addrs.elementAt(i);
        }

        return result;
    }

    protected void writePacket(JICPPacket pkt, Connection c) throws IOException {
        c.writePacket(pkt);
        if (Thread.currentThread() == terminator) {
            myInputManager.close();
        } else {
            updateKeepAlive();
            if (pkt.getType() != JICPProtocol.KEEP_ALIVE_TYPE && pkt.getType() != JICPProtocol.DROP_DOWN_TYPE) {
                updateConnectionDropDown();
            }
        }
    }

    ////////////////////////////////////////////////////////////////
    // Keep-alive and connection drop-down mechanism management
    ////////////////////////////////////////////////////////////////
    /**
    Refresh the keep-alive timer.
    Mutual exclusion with doTimeOut()
     */
    private synchronized void updateKeepAlive() {
        if (keepAliveTime > 0) {
            TimerDispatcher td = TimerDispatcher.getTimerDispatcher();
            if (kaTimer != null) {
                td.remove(kaTimer);
            }
            kaTimer = td.add(new Timer(System.currentTimeMillis() + keepAliveTime, this));
        }
    }

    /**
    Refresh the connection drop-down timer.
    Mutual exclusion with doTimeOut()
     */
    private synchronized void updateConnectionDropDown() {
        if (connectionDropDownTime > 0) {
            TimerDispatcher td = TimerDispatcher.getTimerDispatcher();
            if (cdTimer != null) {
                td.remove(cdTimer);
            }
            cdTimer = td.add(new Timer(System.currentTimeMillis() + connectionDropDownTime, this));
        }
    }

    public void doTimeOut(Timer t) {
        // Mutual exclusion with updateKeepAlive() and updateConnectionDropDown()
        synchronized (this) {
            if (t == kaTimer) {
                // [WATCHDOG] startWatchDog(outConnection);
                sendKeepAlive();
            } else if (t == cdTimer) {
                dropDownConnection();
            }
        }
    }

    /**
    Send a KEEP_ALIVE packet to the BE.
    This is executed within a synchronized block --> Mutual exclusion
    with dispatch() is guaranteed.
     */
    protected void sendKeepAlive() {
        if (outConnection != null) {
            JICPPacket pkt = new JICPPacket(JICPProtocol.KEEP_ALIVE_TYPE, JICPProtocol.DEFAULT_INFO, null);
            try {
                if (myLogger.isLoggable(Logger.FINEST)) {
                    myLogger.log(Logger.FINEST, "Writing KA.");
                }
                writePacket(pkt, outConnection);
                pkt = outConnection.readPacket();
                // [WATCHDOG] stopWatchDog();
                if ((pkt.getInfo() & JICPProtocol.RECONNECT_INFO) != 0) {
                    // The BackEnd is considering the input connection no longer valid
                    refreshInp();
                }
            } catch (IOException ioe) {
                myLogger.log(Logger.WARNING, "IOException OC sending KA. " + ioe);
                // [WATCHDOG] stopWatchDog();
                refreshOut();
            }
        } else {
            // [WATCHDOG] stopWatchDog();
        }
    }

    /**
    Send a DROP_DOWN packet to the BE. The latter will also close
    the INP connection.
    This is executed within a synchronized block --> Mutual exclusion
    with dispatch() is guaranteed.
     */
    private void dropDownConnection() {
        if (outConnection != null && !refreshingInput && !connectionDropped) {
            myLogger.log(Logger.INFO, "Writing DROP_DOWN request");
            JICPPacket pkt = prepareDropDownRequest();
            try {
                writePacket(pkt, outConnection);
                JICPPacket rsp = outConnection.readPacket();
                myLogger.log(Logger.INFO, "DROP_DOWN response received");

                if (rsp.getType() != JICPProtocol.ERROR_TYPE) {
                    // Now close the outConnection
                    try {
                        outConnection.close();
                        outConnection = null;
                    } catch (IOException ioe) {
                        // Just print a warning
                        myLogger.log(Logger.WARNING, "Exception in connection drop-down closing the OUT connection. " + ioe);
                    }

                    myLogger.log(Logger.INFO, "Connection dropped");
                    connectionDropped = true;
                    if (myConnectionListener != null) {
                        myConnectionListener.handleConnectionEvent(ConnectionListener.DROPPED, null);
                    }
                } else {
                    // The BE refused to drop down the connection
                    myLogger.log(Logger.INFO, "DROP_DOWN refused");
                    if ((rsp.getInfo() & JICPProtocol.RECONNECT_INFO) != 0) {
                        // The BE has the INP connection down and we didn't know that.
                        // Refresh the INP connection
                        myLogger.log(Logger.INFO, "INP connection refresh request from BE");
                        refreshInp();
                    }
                }
            } catch (IOException ioe) {
                // Can't reach the BackEnd.
                myLogger.log(Logger.WARNING, "IOException sending DROP_DOWN request. " + ioe);
                refreshOut();
            }
        }
    }

    protected JICPPacket prepareDropDownRequest() {
        return new JICPPacket(JICPProtocol.DROP_DOWN_TYPE, JICPProtocol.DEFAULT_INFO, null);
    }

    protected void dispatchWhileDropped() {
        myLogger.log(Logger.INFO, "Dispatch with connection dropped. Reconnecting.");
        // The connectionDropped flag will be set to false as soon as we
        // re-establish the OUT connection. This is needed in handleOutReconnection()
        refreshOut();
    }

    /* [WATCHDOG]
    private Object watchDogLock = new Object();
    private Thread watchDogThread = null;
    private boolean done = false;

    private void startWatchDog(final Connection c) {
    synchronized (watchDogLock) {
    // If a watch dog is already active, don't start another one.
    if (watchDogThread == null) {
    myLogger.log(Logger.INFO, "Starting WatchDog thread.");
    done = false;
    watchDogThread = new Thread() {
    public void run() {
    synchronized (watchDogLock) {
    try {
    if (!done) {
    watchDogLock.wait(2*RESPONSE_TIMEOUT);
    if (!done) {
    // Timeout expired
    myLogger.log(Logger.WARNING, "WatchDog: timer expired.");
    try {
    c.close();
    myLogger.log(Logger.INFO, "WatchDog: connection closed.");
    }
    catch (IOException ioe) {
    myLogger.log(Logger.WARNING, "WatchDog: IOException closing connection.");
    }
    }
    }
    }
    catch (Exception e) {
    myLogger.log(Logger.WARNING, "WatchDog: Unexpected Exception "+e);
    }
    watchDogThread = null;
    myLogger.log(Logger.INFO, "WatchDog: terminated.");
    }
    }
    };
    watchDogThread.start();
    }
    }
    }

    private void stopWatchDog() {
    myLogger.log(Logger.INFO, "Stopping WatchDog thread.");
    synchronized (watchDogLock) {
    done = true;
    watchDogLock.notifyAll();
    }
    }
    // [WATCHDOG] */
    private JICPConnection openConnection(TransportAddress ta, int timeout) throws IOException {
        if (myConnectionListener != null) {
            myConnectionListener.handleConnectionEvent(ConnectionListener.BEFORE_CONNECTION, null);
        }
        // TODO let factory provide connection
        JICPConnection c = getConnection(ta);
        //#MIDP_EXCLUDE_BEGIN
        if (timeout > 0) {
            c.setReadTimeout(timeout);
        }
        //#MIDP_EXCLUDE_END
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

    private void handleBENotFound() {
        if (myConnectionListener != null) {
            myConnectionListener.handleConnectionEvent(ConnectionListener.BE_NOT_FOUND, null);
        }
    }
}

