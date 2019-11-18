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

package jade.core.nodeMonitoring;

//#APIDOC_EXCLUDE_FILE
// Take care that the DOTNET build file (dotnet.xml) uses this file (it is copied just after the preprocessor excluded it)
//#J2ME_EXCLUDE_FILE

import jade.core.IMTPException;
import jade.core.Profile;

import java.io.IOException;
import java.nio.ByteBuffer;

//#DOTNET_EXCLUDE_BEGIN
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.BindException;
//#DOTNET_EXCLUDE_END

/*#DOTNET_INCLUDE_BEGIN
import jade.core.Profile;
import System.Net.*;
import System.Net.Sockets.*;
#DOTNET_INCLUDE_END*/

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import jade.util.Logger;

/**
 * The <code>UDPMonitorServer</code> is used by any instance of the class
 * <code>UDPNodeFailureMonitor</code> to receive UDP ping messages from nodes.
 * 
 * The server is only running if there are nodes to supervise. By default it
 * waits at port 28000 for incomming UDP datagrams. The maximum time between
 * two ping messages is by default 3 second. After 5 seconds a node is marked 
 * for removing from the platform.
 * <p>
 * 
 * @author Roland Mungenast - Profactor
 * @since JADE 3.3
 * @author Federico Pieri - ERXA
 * @since JADE 3.3.NET
 */
class UDPMonitorServer {

	private Logger logger;
	
	private UDPNodeMonitoringService myService = null;
	
	private String host;
	private boolean acceptLocalHostOnly;
	private int port;
	private int pingDelay;
	private int pingDelayLimit;
	private int unreachLimit;
	
	private NetworkChecker checker;

	//#DOTNET_EXCLUDE_BEGIN
	private DatagramChannel server;
	private Selector selector;
	//#DOTNET_EXCLUDE_END
	
	private Hashtable targets = new Hashtable();
	private PingHandler pingHandler;
	private Timer timer;
	private Hashtable deadlines = new Hashtable();
	
	private int orphanNodePingsCnt;
	private int maxTracedUnknownPings;
	private Hashtable unknownPingCounters = new Hashtable();

	/*#DOTNET_INCLUDE_BEGIN
	 private Socket server;
	 #DOTNET_INCLUDE_END*/

	private static long currentId = 0;

	private synchronized static long getUniqueId() {
		return currentId++;
	}

	/**
	 * Class to store a deadline for the next ping
	 * of a targeted node
	 */
	private class Deadline extends TimerTask {

		private String nodeID;

		private long id;

		public Deadline(String nodeID) {
			this.nodeID = nodeID;
			this.id = getUniqueId();
		}

		public long getID() {
			return id;
		}

		public void run() {
			UDPNodeFailureMonitor mon = (UDPNodeFailureMonitor) targets.get(nodeID);

			// node is still supervised and there are no new deadlines
			if (mon != null) {
				synchronized (mon) { // Mutual exclusion with pingReceived()
					if (mon.getDeadlineID() == id) {
						timeout(nodeID, mon);
					} else {
						logger.log(Logger.WARNING, "expired Deadline "+id+" for node "+nodeID+" is not the same as monitor Deadline "+mon.getDeadlineID());
					}
				}
			}
		}

		public String toString() {
			return "Deadline{nodeID="+nodeID+" id="+id+"}";
		}
	}

	/**
	 * Class to handles incomming ping messages
	 */
	private class PingHandler implements Runnable {

		private final byte TERMINATING_INFO = 1; // bit 1
		private boolean interrupted = false;
		private Thread thread;

		public PingHandler(String name) {
			thread = new Thread(this, name);
		}

		private void handlePing() throws IOException {
			// allocate maximum size of one UDP packet
			ByteBuffer datagramBuffer = ByteBuffer.allocate(1 << 16);

			//#DOTNET_EXCLUDE_BEGIN
			SocketAddress address = server.receive(datagramBuffer);
			//#DOTNET_EXCLUDE_END

			/*#DOTNET_INCLUDE_BEGIN
			 ubyte[] recData = new ubyte[datagramBuffer.getUByte().length];

			 if ( server != null)
			 {
			 try
			 {
			 if (server.get_Available() <= 0)
			 return;
			 }
			 catch (System.ObjectDisposedException ode)
			 {
			 return;
			 }
			 }
			 else
			 return;

			 try
			 {
			 server.Receive(recData, 0, server.get_Available(), SocketFlags.None);
			 }
			 catch (SocketException se)
			 {
			 int socketError = se.get_ErrorCode();
			 return;
			 }
			 IPEndPoint IPendPt  = (IPEndPoint) server.get_LocalEndPoint();
			 IPAddress address	= IPendPt.get_Address();
			 datagramBuffer.copyUByte(recData);
			 #DOTNET_INCLUDE_END*/

			datagramBuffer.position(0);

			if (address != null) {

				int nodeIDLength = datagramBuffer.getInt();

				// get node ID
				byte[] bb = new byte[nodeIDLength];
				datagramBuffer.get(bb, 0, nodeIDLength);
				String nodeID = new String(bb);

				// analyse info byte
				byte info = datagramBuffer.get();
				boolean isTerminating = (info & TERMINATING_INFO) != 0;

				pingReceived(nodeID, isTerminating);
			}
		}

		public void run() {
			while (!interrupted) { // endless loop
				try {
					//#DOTNET_EXCLUDE_BEGIN
					selector.select();

					Set keys = selector.selectedKeys();
					interrupted = keys.size() == 0;
					Iterator i = keys.iterator();

					while (i.hasNext()) {
						SelectionKey key = (SelectionKey) i.next();
						i.remove();
						if (key.isValid() && key.isReadable()) {
							//#DOTNET_EXCLUDE_END
							handlePing();
							//#DOTNET_EXCLUDE_BEGIN
						}
					}
					//#DOTNET_EXCLUDE_END 
				} catch (Exception e) // .net requires I catch Exception instead of IOException
				{
					if (logger.isLoggable(Logger.SEVERE))
						logger.log(Logger.SEVERE, "UDP Connection error ");
				}
			} // for
		}

		public void start() {
			thread.start();
		}

		public void stop() {
			interrupted = true;
		}
	}

	/**
	 * Constructs a new UDPMonitorServer object
	 */
	UDPMonitorServer(UDPNodeMonitoringService s, String h, boolean alho, int p, int pd, int pdl, int ul, int onpc, int mtup, NetworkChecker ch) {
		myService = s;
		host = h;
		acceptLocalHostOnly = alho;
		port = p;
		pingDelay = pd;
		pingDelayLimit = pdl;
		unreachLimit = ul;
		orphanNodePingsCnt = onpc;
		maxTracedUnknownPings = mtup;
		checker = ch;

		logger = Logger.getMyLogger(UDPNodeMonitoringService.NAME);
		try {
			//#DOTNET_EXCLUDE_BEGIN
			server = DatagramChannel.open();
			//#DOTNET_EXCLUDE_END

			/*#DOTNET_INCLUDE_BEGIN
			 server = new Socket(AddressFamily.InterNetwork, SocketType.Dgram, ProtocolType.Udp);
			 #DOTNET_INCLUDE_END*/
		} catch (Exception e) { // .net requires I catch Exception instead of IOException
			logger.log(Logger.SEVERE, "Cannot open UDP channel. " + e);
			e.printStackTrace();
		}
	}

	String getHost() {
		return host;
	}
	
	int getPort() {
		return port;
	}
	
	int getPingDelay() {
		return pingDelay;
	}
	
	int getPingDelayLimit() {
		return pingDelayLimit;
	}
	
	int getUnreachableLimit() {
		return unreachLimit;
	}
	
	/**
	 * Starts the UDP server
	 */
	synchronized void start() throws Exception {
		// Start UDP server

		//#DOTNET_EXCLUDE_BEGIN
		server.configureBlocking(false);
        try {
        	if (acceptLocalHostOnly) {
	            logger.log(Logger.INFO, "Binding UDP Server Socket on host "+host+", port " + port);
	            server.socket().bind(new InetSocketAddress(host, port));
        	}
        	else {
	            logger.log(Logger.INFO, "Binding UDP Server Socket on port " + port);
	            server.socket().bind(new InetSocketAddress(port));
        	}
        } catch (BindException e){
        	if (!acceptLocalHostOnly || Profile.isLocalHost(host)) {
        		// Port busy --> select a free one
	            logger.log(Logger.WARNING, "Cannot bind UDP Server Socket on port " + port + ". Let the system select a free one.");
	            if (acceptLocalHostOnly) {
		            server.socket().bind(new InetSocketAddress(host, 0));
	            }
	            else {
		            server.socket().bind(new InetSocketAddress(0));
	            }
	            port  = server.socket().getLocalPort();
        	}
        	else {
        		// Specified host does not represent a local host --> Exception
        		throw e;
        	}
        }
        //#DOTNET_EXCLUDE_END

		/*#DOTNET_INCLUDE_BEGIN
		 server = new Socket(AddressFamily.InterNetwork, SocketType.Dgram, ProtocolType.Udp);
		 server.set_Blocking( false );
		 server.SetSocketOption(SocketOptionLevel.Socket, SocketOptionName.ReuseAddress, 1);
		 String defaultNetworkName = Profile.getDefaultNetworkName();
		 System.Net.IPHostEntry hostEntry = System.Net.Dns.GetHostByName( defaultNetworkName );
		 System.Net.IPAddress[] ipAddresses = hostEntry.get_AddressList();
		 long ipAddressLong = ipAddresses[0].get_Address();
		 if ( !server.get_Connected() )
		 {
		 IPEndPoint ipPoint = new IPEndPoint(IPAddress.Any, port);
		 try
		 {
		 server.Bind( ipPoint );
		 }
		 catch(SocketException se)
		 {
		 int socketError = se.get_ErrorCode();
		 }
		 }
		 #DOTNET_INCLUDE_END*/

		//#DOTNET_EXCLUDE_BEGIN
		// Create and register Selector
		selector = Selector.open();
		server.register(selector, SelectionKey.OP_READ);
		//#DOTNET_EXCLUDE_END

		// Start PingHandler thread
		pingHandler = new PingHandler("UDPNodeFailureMonitor-PingHandler");
		pingHandler.start();

		// start timer for deadlines
		timer = new Timer();
	}

	/**
	 * Stops the UDP server
	 */
	synchronized void stop() {
		try {
			pingHandler.stop();
			timer.cancel();
			deadlines.clear();

			//#DOTNET_EXCLUDE_BEGIN
			server.disconnect();
			//#DOTNET_EXCLUDE_END

			/*#DOTNET_INCLUDE_BEGIN
			 server.Shutdown(SocketShutdown.Both);
			 server.Close();
			 #DOTNET_INCLUDE_END*/

			if (logger.isLoggable(Logger.INFO))
				logger.log(Logger.INFO, "UDP monitoring server has been stopped.");

		} catch (Exception e) {
			if (logger.isLoggable(Logger.SEVERE))
				logger.log(Logger.SEVERE, "Error shutting down the UDP monitor server");
		}
	}

	/**
	 * Registers a <code>UDPNodeFailureMonitor</code>.
	 * All nodes targeted by this monitor are now supervised
	 * by the <code>UDPMonitorServer</code>. Its listener
	 * gets informed about any state changes.
	 */
	public void register(UDPNodeFailureMonitor m) {
		String nodeID = m.getNode().getName();
		targets.put(nodeID, m);
		addDeadline(nodeID, pingDelayLimit);
	}

	/**
	 * Deregisters a <code>UDPNodeFailureMonitor</code> and all
	 * its targeted nodes from monitoring.
	 */
	public void deregister(UDPNodeFailureMonitor m) {
		String nodeId = m.getNode().getName();
		targets.remove(nodeId);
	}

	/**
	 * This method is invoked by a PingHandler thread when a 
	 * new ping message has been received
	 * @param nodeID identification of the sender node
	 * @param isTerminating true if the sender is currently shutting down
	 */
	protected void pingReceived(String nodeID, boolean isTerminating) {

		if (logger.isLoggable(Logger.FINEST)) {
			logger.log(Logger.FINEST, "UDP ping message for node '" + nodeID + "' received. (termination-flag: " + isTerminating + ")");
		}
		// Cancel the existing deadline if any
		TimerTask currDeadline = (TimerTask) deadlines.remove(nodeID);
		if (currDeadline != null) {
			currDeadline.cancel();
		}
		
		UDPNodeFailureMonitor mon = (UDPNodeFailureMonitor) targets.get(nodeID);
		if (mon != null) {
			unknownPingCounters.remove(nodeID);
			synchronized (mon) { // Mutual exclusion with Timer expiration
				mon.setLastPing(System.currentTimeMillis()); // update time for last ping

				int state = mon.getState();

				if (isTerminating) {
					mon.setState(UDPNodeFailureMonitor.STATE_FINAL);
				}
				else {
					if (state == UDPNodeFailureMonitor.STATE_UNREACHABLE) {
						mon.setState(UDPNodeFailureMonitor.STATE_CONNECTED);
					}
					addDeadline(nodeID, pingDelayLimit);
				}
			}
		} 
		else {
			handleUnknownPing(nodeID);
		}
	}

	private void handleUnknownPing(String nodeID) {
		Counter cnt = (Counter) unknownPingCounters.get(nodeID);
		if (cnt == null) {
			cnt = new Counter();
			unknownPingCounters.put(nodeID, cnt);
		}
		if (cnt.getValue() < maxTracedUnknownPings) {
			logger.log(Logger.WARNING, "UDP ping message with the unknown node ID '" + nodeID + "' received");
			cnt.increment();
			if (cnt.getValue() == orphanNodePingsCnt) {
				// A node is considered orphan only once after the reception of 10 "unknown pings"
				final String id = nodeID;
				Thread t = new Thread() {
					public void run() {
						myService.handleOrphanNode(id);
					}
				};
				t.start();
			}
		}
	}
	
	
	private class Counter {
		private int value = 0; 
		
		private void increment() {
			value++;
		}
		
		private int getValue() {
			return value;
		}
	}
	
	
	/**
	 * This method is invoked by a TimeoutHandler at a timeout
	 */
	protected void timeout(String nodeID, UDPNodeFailureMonitor mon) {
		int oldState = mon.getState();
		int newState = oldState;

		if (logger.isLoggable(Logger.FINEST)) {
			logger.log(Logger.FINEST, "Timeout for '" + nodeID + "'");
		}

		if (oldState == UDPNodeFailureMonitor.STATE_CONNECTED) {
			// Try to ping the monitored node explicitly to be sure it is actually disconnected
			try {
				myService.pingNode(nodeID);
				// For some reason we are not receiving PING packets, but the node is alive and reachable.
				// Print a warning and do as if we received a ping
				logger.log(Logger.WARNING, "Missing UDP-PING packets from reachable node "+nodeID);
				pingReceived(nodeID, false);
			}
			catch (IMTPException imtpe) {
				// The node is actually unreachable.
				newState = UDPNodeFailureMonitor.STATE_UNREACHABLE;
				addDeadline(nodeID, unreachLimit);
			}
		} else if (oldState == UDPNodeFailureMonitor.STATE_UNREACHABLE) {
			if (checker == null || checker.isNetworkUp()) {
				// Unreachable-limit Expired! If no NetworkChecker is specified or the NetworkChecker says
				// that the network is properly working, consider the monitored node DEAD.
				newState = UDPNodeFailureMonitor.STATE_FINAL;
			}
			else {
				// Network down --> do not consider the node dead
				logger.log(Logger.WARNING, "Unreachable limit exceeded for node "+nodeID+", however the network appears to be down --> Give the node another chance");
				addDeadline(nodeID, unreachLimit);
			}
		}
		
		if (newState != oldState)
			mon.setState(newState);
	}

	private void addDeadline(String nodeID, int delay) {
		Deadline deadline = new Deadline(nodeID);
		UDPNodeFailureMonitor mon = (UDPNodeFailureMonitor) targets.get(nodeID);
		if (mon != null) {
			synchronized (mon) {
				mon.setDeadlineID(deadline.getID());
				deadlines.put(nodeID, deadline);
				timer.schedule(deadline, delay);
			}
		}
	}
}
