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
package jade.imtp.leap.nio;

//#J2ME_EXCLUDE_FILE
import jade.core.*;
import jade.core.sam.AverageMeasureProviderImpl;
import jade.core.sam.CounterValueProvider;
import jade.core.sam.MeasureProvider;
import jade.core.sam.SAMHelper;
import jade.imtp.leap.ICPException;
import jade.util.Logger;
import jade.util.ThreadDumpManager;
import jade.util.leap.Properties;
import jade.imtp.leap.TransportProtocol;
import jade.imtp.leap.FrontEndStub;
import jade.imtp.leap.JICP.*;
import jade.security.JADESecurityException;

import java.io.*;
import java.nio.channels.*;
import java.net.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Enumeration;

// FIXME: Aggiornare il Javadoc a fronte dell'introduzione dell'opzione di accept
/**
This service handles BEContainer creation requests and manages IO operations
for data exchanged between the created BEContainers and their FEContainers
in an asynchronous way using java.nio
<br><br>
Having this functionality implemented as a service allows propagating
(through the ServiceHelper) BackEnd related events (e.g. disconnections and 
reconnections) at the agent level.
<br><br>
This service accepts the following configuration parameters:<br>
<code>jade_imtp_leap_nio_BEManagementService_servers</code>: list of of IOEventServer ids separated by ';'<br>
Actually this service is a collection of IOEventServer objects.
Each IOEventServer opens and manages a server socket, accepts 
BackEnd creation/connection requests and passes incoming data to BEDispatchers
independently from the others.
If the above parameter is not specified a single IOEventServer will
be started and its ID will be <code>jade_imtp_leap_nio_BEManagementService</code>.
All other parameters are related to a single IOEventServer and must 
be specified in the form<br> 
serverid_parametername<br>
They are:
<ul>
<li>
<code>serverid_local-host</code>: Specifies the local network interface 
for the server socket managed by this server (defaults to localhost).
</li>
<li>
<code>serverid_local-port</code>: Specifies the local port for the server 
socket managed by this server (defaults to 2099)
</li>
<li>
<code>serverid_protocol</code>: Specifies the protocol used by this 
server in the form of <code>jade.imtp.leap.JICP.ProtocolManager<code> 
class
</li>
<li>
<code>serverid_additional-services</code>: Specifies additional services 
that have to be activated in each BackEnd Container started by the indicated
server  
</li>
<li>
<code>serverid_leap-property-file</code>: Specifies the leap-property
file to be used by this server.
</li>
<li>
<ode>serverid_poolsize</code>: Specifies the number of threads used by 
this server to manage IO events.
</li>
<ul>

@author Giovanni Caire - TILAB
 */
// FIXME: Complete javadoc describing the accept option
public class BEManagementService extends BaseService {

	public static final String NAME = "BEManagement";

	private static final String PREFIX = "jade_imtp_leap_nio_BEManagementService_";

	public static final String ACCEPT = PREFIX+"accept";
	public static final String SERVERS = PREFIX+"servers";
	
	public static final String ADDITIONAL_SERVICES = "additional-services";

	private static final int DEFAULT_PORT = 2099;
	private static final int DEFAULT_POOL_SIZE = 5;
	private static final int INIT_STATE = 0;
	private static final int ACTIVE_STATE = 1;
	private static final int TERMINATING_STATE = 2;
	private static final int TERMINATED_STATE = 3;
	private static final int ERROR_STATE = -1;
	
	public static final String INCOMING_CONNECTION = "Incoming-Connection";
	
	// since this is the only service dealing with NIO it is ok to set the size used for enlarging buffers here
	private static int bufferIncreaseSize = 1024;
	/**
	 * this property can be used to set how many bytes will be used when nio buffer need to be increased
	 */
	public static final String BUFFERINCREASE = "bufferincrease";

	private static final String[] OWNED_COMMANDS = new String[]{
		INCOMING_CONNECTION
	};

	private static final Map<String, Class> protocolManagers = new HashMap<String, Class>();
	static {
		protocolManagers.put(MicroRuntime.SOCKET_PROTOCOL, NIOJICPPeer.class);
		protocolManagers.put(MicroRuntime.SSL_PROTOCOL, NIOJICPSPeer.class);
		protocolManagers.put(MicroRuntime.HTTP_PROTOCOL, NIOHTTPPeer.class);
		protocolManagers.put(MicroRuntime.HTTPS_PROTOCOL, NIOHTTPSPeer.class);
	}

	private Hashtable servers = new Hashtable(2);
	private Ticker myTicker;
	private ServiceHelper myHelper;
	private String platformName;
	// The list of addresses considered malicious. Connections from
	// these addresses will be rejected.
	// FIXME: The mechanism for filling/clearing this list is not yet
	// defined/implemented
	private Vector maliciousAddresses = new Vector();
	
	private String configOptionsFileName = "feOptions.properties";
	
	private AgentContainer myContainer;
	
	// SAM related variables
	private long createMediatorCounter = 0;
	private long connectMediatorCounter = 0;
	private long mediatorNotFoundCounter = 0;
	private long incomingCommandCounter = 0;
	private long keepAliveCounter = 0;
	private long dropDownCounter = 0;
	private long processingTimeGT1SecCounter = 0;
	private long processingTimeGT10SecCounter = 0;
	private long incomingPacketServingErrorCounter = 0;
	private long incomingPacketReadingErrorCounter = 0;
	private AverageMeasureProviderImpl dataProcessingTimeProvider = null;
	private AverageMeasureProviderImpl waitForDataTimeProvider = null;
	
	private Logger myLogger = Logger.getJADELogger(getClass().getName());

	/**
    @return The name of this service.
	 */
	public String getName() {
		String className = getClass().getName();
		return className.substring(0, className.indexOf("Service"));
	}

	public static final int getBufferIncreaseSize() {
		return bufferIncreaseSize;
	}

	public String[] getOwnedCommands() {
		return OWNED_COMMANDS;
	}

	public void init(AgentContainer ac, Profile p) throws ProfileException {
		super.init(ac, p);

		platformName = ac.getPlatformID();
		myContainer = ac;

		// Initialize the BE-Manager
		BackEndManager.getInstance(p);

		handleAcceptOption(p);
		
		// Just for debugging purpose
		//StuckSimulator.init();
	}

	private void handleAcceptOption(Profile p) throws ProfileException {
		// Manage the shortcut configuration option jade_imtp_leap_nio_BEManagementService_accept
		String acceptOption = p.getParameter(ACCEPT, null);
		if (acceptOption != null) {
			// The jade_imtp_leap_nio_BEManagementService_accept option has a form of type
			// socket;http(8080)
			// This means that we will accept plain sockets on the default port and HTTP on port 8080
			jade.util.leap.List acceptedProtocols = p.getSpecifiers(ACCEPT);
			// Do not overwrite explicitly specified servers if any
			String serverIDs = p.getParameter(SERVERS, null);
			serverIDs = (serverIDs != null ? serverIDs += ';' : "");
			for (int i = 0; i < acceptedProtocols.size(); ++i) {
				Specifier s = (Specifier) acceptedProtocols.get(i);
				String proto = s.getClassName();
				if (proto.equalsIgnoreCase(MicroRuntime.SOCKET_PROTOCOL) ||
						proto.equalsIgnoreCase(MicroRuntime.SSL_PROTOCOL) ||	
						proto.equalsIgnoreCase(MicroRuntime.HTTP_PROTOCOL) ||	
						proto.equalsIgnoreCase(MicroRuntime.HTTPS_PROTOCOL)) {	
					String semicolon = ((i+1) == acceptedProtocols.size() ? "" : ";");
					serverIDs = serverIDs + manageAcceptedProtocol(s, proto.toLowerCase(), p)+semicolon;
				}
				else {
					myLogger.log(Logger.WARNING, "Unsupported protocol "+proto+". Permitted values are socket, ssl, http and https!!!!");
				}
			}
			p.setParameter(SERVERS, serverIDs);
		}
	}

	private String manageAcceptedProtocol(Specifier s, String protocolName, Profile p) {
		String id = PREFIX+protocolName;
		p.setParameter(id + '_' + "protocol", protocolManagers.get(protocolName).getName());
		if (s.getArgs() != null && s.getArgs().length > 0) {
			p.setParameter(id + '_' + JICPProtocol.LOCAL_PORT_KEY, (String) s.getArgs()[0]);
		}
		return id;
	}

	/**
    This method is called by the JADE runtime just after this service
    has been installed.
    It activates the IO event servers specified in the Profile and the
    Ticker thread.
	 */
	public void boot(Profile p) throws ServiceException {
		// Get IDs of servers to install
		String defaultServerIDs = PREFIX.substring(0, PREFIX.length() - 1);
		String serverIDs = p.getParameter(SERVERS, defaultServerIDs);
		Vector v = Specifier.parseList(serverIDs, ';'); 

		// Activate all servers
		Enumeration e = v.elements();
		while (e.hasMoreElements()) {
			String id = (String) e.nextElement();
			try {
				IOEventServer srv = new IOEventServer();
				srv.init(id, p);
				servers.put(id, srv);
				srv.activate();
			} catch (Throwable t) {
				myLogger.log(Logger.WARNING, "Error activating IOEventServer " + id + ". " + t);
				t.printStackTrace();
			}
		}
		if (servers.size() == 0) {
			throw new ServiceException("NO IO-Event-Server active");
		}

		// Activate the ticker
		long tickTime = 60000;
		try {
			tickTime = Long.parseLong(p.getParameter(PREFIX + "ticktime", null));
		} catch (Exception ex) {
		}
		try {
			// set buffer increase size
			bufferIncreaseSize = Integer.parseInt(p.getParameter(PREFIX + BUFFERINCREASE, null));
		} catch (Exception ex) {
		}
		myTicker = new Ticker(tickTime);
		myTicker.start();
		
		// Initialize messaging-related System Activity Monitoring
		initializeSAM();
	}

	private void initializeSAM() {
		try {
			Service sam = myContainer.getServiceFinder().findService(SAMHelper.SERVICE_NAME);
			if (sam != null) {
				SAMHelper samHelper = (SAMHelper) sam.getHelper(null);
		
				// Average data processing time
				dataProcessingTimeProvider = new AverageMeasureProviderImpl();
				samHelper.addEntityMeasureProvider("Avg_Data_Processing_Time", dataProcessingTimeProvider);
				
				// Average time a LoopManager spends waiting for network data to process
				waitForDataTimeProvider = new AverageMeasureProviderImpl();
				samHelper.addEntityMeasureProvider("Avg_Wait_For_Data_Time", waitForDataTimeProvider);
	
				// Counter of packets whose processing required more than 1 sec and less than 10 sec
				samHelper.addCounterValueProvider("Processing_Time_GT1Sec_Count", new CounterValueProvider() {
					public long getValue() {
						return processingTimeGT1SecCounter;
					}
					public boolean isDifferential() {
						return false;
					}
				});
	
				// Counter of packets whose processing required more than 10 sec
				samHelper.addCounterValueProvider("Processing_Time_GT10Sec_Count", new CounterValueProvider() {
					public long getValue() {
						return processingTimeGT10SecCounter;
					}
					public boolean isDifferential() {
						return false;
					}
				});
	
				// Number of active BackEnds
				samHelper.addEntityMeasureProvider("BackEnd_Number", new MeasureProvider() {
					public Number getValue() {
						int cnt = 0;
						Iterator it = servers.values().iterator();
						while (it.hasNext()) {
							cnt += ((IOEventServer) it.next()).mediators.values().size();
						}
						return cnt;
					}
				});
				
				// Number of opened socket
				samHelper.addEntityMeasureProvider("Socket_Number", new MeasureProvider() {
					public Number getValue() {
						int cnt = 0;
						Iterator it = servers.values().iterator();
						while (it.hasNext()) {
							cnt += ((IOEventServer) it.next()).getSocketCnt();
						}
						return cnt;
					}
				});

				// JICP event counters
				samHelper.addCounterValueProvider("Create_Mediator_Count", new CounterValueProvider() {
					public long getValue() {
						return createMediatorCounter;
					}
					public boolean isDifferential() {
						return false;
					}
				});
				samHelper.addCounterValueProvider("Connect_Mediator_Count", new CounterValueProvider() {
					public long getValue() {
						return connectMediatorCounter;
					}
					public boolean isDifferential() {
						return false;
					}
				});
				samHelper.addCounterValueProvider("Mediator_Not_Found_Count", new CounterValueProvider() {
					public long getValue() {
						return mediatorNotFoundCounter;
					}
					public boolean isDifferential() {
						return false;
					}
				});
				samHelper.addCounterValueProvider("Incoming_Command_Count", new CounterValueProvider() {
					public long getValue() {
						return incomingCommandCounter;
					}
					public boolean isDifferential() {
						return false;
					}
				});
				samHelper.addCounterValueProvider("Keep_Alive_Count", new CounterValueProvider() {
					public long getValue() {
						return keepAliveCounter;
					}
					public boolean isDifferential() {
						return false;
					}
				});
				samHelper.addCounterValueProvider("Drop_Down_Count", new CounterValueProvider() {
					public long getValue() {
						return dropDownCounter;
					}
					public boolean isDifferential() {
						return false;
					}
				});
				
				// Error counters
				samHelper.addCounterValueProvider("Incoming_Packet_Reading_Error_Count", new CounterValueProvider() {
					public long getValue() {
						return incomingPacketReadingErrorCounter;
					}
					public boolean isDifferential() {
						return false;
					}
				});
				samHelper.addCounterValueProvider("Incoming_Packet_Serving_Error_Count", new CounterValueProvider() {
					public long getValue() {
						return incomingPacketServingErrorCounter;
					}
					public boolean isDifferential() {
						return false;
					}
				});
			}
		}
		catch (ServiceNotActiveException snae) {
			// SAMService not active --> just do nothing
		}
		catch (Exception e) {
			// Should never happen
			myLogger.log(Logger.WARNING, "Error accessing the local SAMService.", e);
		}
	}

	/**
    This is called by the JADE Runtime when this service is deinstalled.
    It stops the Ticker thread and all active IO event servers.
	 */
	public void shutdown() {
		myLogger.log(Logger.CONFIG, "BEManagementService initiating shutdown procedure...");
		// Shutdown the Ticker
		if (myTicker != null) {
			myTicker.shutdown();
		}

		// Shutdown all servers
		Object[] ss = servers.values().toArray();
		for (int i = 0; i < ss.length; ++i) {
			((IOEventServer) ss[i]).shutdown();
		}
	}

	/**
    Retrieve the helper of this Service
	 */
	public ServiceHelper getHelper(Agent a) {
		if (myHelper == null) {
			myHelper = new BEManagementHelperImpl();
		}
		return myHelper;
	}
	
	private String encodeConfigOptionsResponse() throws Exception {
		Properties pp = new Properties();
		// Search first in the file system and then in the classpath
		pp.load(configOptionsFileName);
		return FrontEndStub.encodeProperties(pp);
	}

	/**
    Inner class IOEventServer.
    This class asynchronously manages a server socket and all IO Events
    that happen on it and on all sockets opened through it.
    The BEManagementService is basically a collection of these servers.
	 */
	private class IOEventServer implements PDPContextManager, PDPContextManager.Listener, JICPMediatorManager {

		private String myID;
		private String myLogPrefix;
		private int state = INIT_STATE;
		private ServerSocketChannel mySSChannel;
		private long mediatorCnt = 1;
		private Hashtable<String, NIOMediator> mediators = new Hashtable<String, NIOMediator>();
		private Vector<String> deregisteredMediators = new Vector<String>();
		private String host;
		private int port;
		private Properties leapProps = new Properties();
		private PDPContextManager myPDPContextManager;
		private TransportProtocol myProtocol;
		private ConnectionFactory myConnectionFactory;
		private LoopManager[] loopers;

		/**
        Initialize this IOEventServer according to the Profile
		 */
		public void init(String id, Profile p) {
			myID = id;
			myLogPrefix = (PREFIX.startsWith(myID) ? "" : "Server " + myID + ": ");

			// Local host
			host = p.getParameter(id + '_' + JICPProtocol.LOCAL_HOST_KEY, null);

			// Local port
			port = DEFAULT_PORT;
			String strPort = p.getParameter(id + '_' + JICPProtocol.LOCAL_PORT_KEY, myID);
			try {
				port = Integer.parseInt(strPort);
			} catch (Exception e) {
				// Keep default
			}

			// Protocol
			/*
			 * Actually we only need a ConnectionFactory at least the way it is build at the moment.
			 *
			 * Perhaps a NIOJICPServer should do the Selector handling, now that is controlled by instances of a inner class
			 * LoopManager.
			 * Than a NIOJICPPeer controls this server and provides the ConnectionFactory
			 *
			 * In that way we stay closer the regular blocking io handling, more work involved though.....
			 *
			 */

			String protoManagerClass = p.getParameter(id + '_' + "protocol", null);
			ProtocolManager pm = null;
			try {
				pm = (ProtocolManager) Class.forName(protoManagerClass).newInstance();
			} catch (Exception e) {
				if (protoManagerClass != null) {
					myLogger.log(Logger.WARNING, myLogPrefix + "Unable to load protocol-manager class " + protoManagerClass + ", fallback to default " + NIOJICPPeer.class.getName()+"!");
				}
				pm = new NIOJICPPeer();
			}
			myLogger.log(Logger.INFO, myLogPrefix + "ProtocolManager class = " + pm.getClass().getName());
			myProtocol = pm.getProtocol();
			myConnectionFactory = pm.getConnectionFactory();

			// Read the LEAP configuration properties
			String fileName = p.getParameter(id + '_' + LEAP_PROPERTY_FILE, LEAP_PROPERTY_FILE_DEFAULT);
			try {
				leapProps.load(fileName);
				myLogger.log(Logger.INFO, myLogPrefix + "Applying properties from file " + fileName + " to all back-ends");
			} catch (Exception e) {
				myLogger.log(Logger.CONFIG, myLogPrefix + "Can't read LEAP property file " + fileName + ". Keep default. [" + e + "]");
				// Ignore: no back end properties specified
			}
			leapProps.setProperty(BackEndContainer.USE_BACKEND_MANAGER, "true");
			
			String additionalService = p.getParameter(id + '_' + ADDITIONAL_SERVICES, null);
			if (additionalService != null) {
				leapProps.setProperty(ADDITIONAL_SERVICES, additionalService);
			}

			// Initialize the PDPContextManager if specified
			String pdpContextManagerClass = leapProps.getProperty(PDP_CONTEXT_MANAGER_CLASS);
			if (pdpContextManagerClass != null) {
				try {
					myLogger.log(Logger.INFO, myLogPrefix + "Loading PDPContextManager of class " + pdpContextManagerClass);
					myPDPContextManager = (PDPContextManager) Class.forName(pdpContextManagerClass).newInstance();
					myPDPContextManager.init(leapProps);
					myPDPContextManager.registerListener(this);
				} catch (Throwable t) {
					myLogger.log(Logger.WARNING, myLogPrefix + "Cannot load PDPContext manager " + pdpContextManagerClass, t);
					myPDPContextManager = null;
				}
			} else {
				// Use itself as default
				myPDPContextManager = this;
			}

			// Looper pool size
			int poolSize = DEFAULT_POOL_SIZE;
			String strPoolSize = p.getParameter(id + '_' + "poolsize", null);
			try {
				poolSize = Integer.parseInt(strPoolSize);
			} catch (Exception e) {
				// Keep default
			}
			loopers = new LoopManager[poolSize];
			for (int i = 0; i < loopers.length; ++i) {
				loopers[i] = new LoopManager(this, i);
			}
		}

		/**
        Start listening for IO events
		 */
		public synchronized void activate() throws Throwable {
			// Create the ServerSocketChannel
			myLogger.log(Logger.CONFIG, myLogPrefix + "Opening server socket channel.");
			mySSChannel = ServerSocketChannel.open();
			mySSChannel.configureBlocking(false);

			// Bind the server socket to the proper host and port
			myLogger.log(Logger.CONFIG, myLogPrefix + "Binding server socket to." + host + ":" + port);
			ServerSocket ss = mySSChannel.socket();
			InetSocketAddress addr = null;
			if (host != null) {
				addr = new InetSocketAddress(host, port);
			} else {
				addr = new InetSocketAddress(port);
				host = Profile.getDefaultNetworkName();
			}
			ss.bind(addr);

			// Register for asynchronous IO events
			myLogger.log(Logger.CONFIG, myLogPrefix + "Registering for asynchronous IO events.");

			// Register the Selector of LoopManager 0 to the ServerSocketChannel to be notified about ACCEPT operations.
			// NOTE that registrations to Socket Channels for READ operations must be done as long as SocketChannel are created
			mySSChannel.register(loopers[0].getSelector(), SelectionKey.OP_ACCEPT);
			myLogger.log(Logger.INFO, myLogPrefix + "Ready to accept I/O events on address " + myProtocol.buildAddress(host, String.valueOf(port), null, null));

			// Start the loop managers
			for (int i = 0; i < loopers.length; ++i) {
				loopers[i].start();
			}
		}
		
		void replaceLoopManager(int index, LoopManager newLoopManager) {
			LoopManager oldLoopManager = loopers[index];
			Map<SelectableChannel, KeyManager> managers = new HashMap<SelectableChannel, KeyManager>();
			Iterator<SelectionKey> it = oldLoopManager.getSelector().keys().iterator();
			while (it.hasNext()) {
				SelectionKey selectionKey = it.next();
				int interestOps = selectionKey.interestOps();
				SelectableChannel channel = selectionKey.channel();
				managers.put(channel, (KeyManager)selectionKey.attachment());
				try {
					channel.register(newLoopManager.getSelector(), interestOps);
					myLogger.log(Logger.INFO, Thread.currentThread().getName() + "- New Selector "+newLoopManager.getSelector()+" successfully registered to Channel "+channel+" with interest options = "+interestOps);
				}
				catch (Exception e) {
					myLogger.log(Logger.SEVERE, Thread.currentThread().getName() + "- Error registering new Selector "+newLoopManager.getSelector()+" to Channel "+channel+" with interest options = "+interestOps+"["+e+"]");
				}
			}
			
			it = newLoopManager.getSelector().keys().iterator();
			while (it.hasNext()) {
				SelectionKey newKey = it.next();
				newKey.attach(managers.get(newKey.channel()));
			}
			
			loopers[index] = newLoopManager;
			newLoopManager.start();
			oldLoopManager.stop();
		}

		/**
        @return The port this server is listening for connection on
		 */
		public int getLocalPort() {
			return mySSChannel.socket().getLocalPort();
		}

		/**
        @return The host this server is listening for connection on
		 */
		public String getLocalHost() {
			return host;
		}

		/**
        Make this IOEventServer terminate
		 */
		public synchronized void shutdown() {
			myLogger.log(Logger.CONFIG, myLogPrefix + "Shutting down...");

			try {
				// Close the server socket
				if (mySSChannel != null) {
					mySSChannel.close();
					myLogger.log(Logger.FINEST, myLogPrefix + "Server Socket Channel closed");
				}
				// Force the looper threads to terminate.
				if (loopers != null) {
					for (int i = 0; i < loopers.length; ++i) {
						myLogger.log(Logger.FINE, myLogPrefix + "Stopping LoopManager #"+i);
						if (!loopers[i].isStuck()) {
							loopers[i].stop();
							loopers[i].join();
							myLogger.log(Logger.FINEST, myLogPrefix + "LoopManager #"+i+" terminated");
						}
					}
				}
			} catch (IOException ioe) {
				myLogger.log(Logger.WARNING, myLogPrefix + "Error closing Server Socket Channel", ioe);
			} catch (InterruptedException ie) {
				myLogger.log(Logger.WARNING, myLogPrefix + "Interrupted while waiting for LoopManager to termnate", ie);
			}

			// Close all mediators
			synchronized (mediators) {
				for (NIOMediator m : mediators.values()) {
					myLogger.log(Logger.FINE, myLogPrefix + "Killing mediator " + m.getID());
					m.kill();
				}
			}
			mediators.clear();
			myLogger.log(Logger.CONFIG, myLogPrefix + "Shutdown complete");
		}

		final String getID() {
			return myID;
		}

		final String getLogPrefix() {
			return myLogPrefix;
		}
		
		final synchronized long nextMediatorCnt() {
			return mediatorCnt++;
		}

		/**
        Get the LoopManager with the minimum number of registered
        keys.
		 */
		final LoopManager getLooper() throws NotFoundException {
			int minSize = 999999; // Big value;
			int index = -1;
			// Start from 1: LM-0 is dedicated to handle ACCEPT-OP on the ServerSocketChannel
			for (int i = 1; i < loopers.length; ++i) {
				if (!loopers[i].isStuck()) {
					int size = loopers[i].size();
					if (size < minSize) {
						minSize = size;
						index = i;
					}
				}
			}
			if (index < 0) {
				// No LoopManager selected
				throw new NotFoundException("NO LoopManager selected");
			}
			return loopers[index];
		}

		final NIOJICPConnection createConnection(SelectionKey key) throws ICPException {
			Socket s = null;
			NIOJICPConnection conn = (NIOJICPConnection) myConnectionFactory.createConnection(s);
			conn.init((SocketChannel) key.channel());
			if (myLogger.isLoggable(Logger.FINE)) {
				myLogger.log(Logger.FINE, "create connection " + conn.getClass().getName());
			}
			return conn;
		}

		/**
        Serve an IO event conveied by a SelectionKey (a JICPPacket or an IO exception).
        This method is executed by one of the threads in the
        server Thread pool.
		 */
		public void servePacket(KeyManager mgr, JICPPacket pkt) {
			if (pkt == null) {
				return;
			}

			SelectionKey key = mgr.getKey();
			SocketChannel sc = (SocketChannel) key.channel();
			Socket s = sc.socket();
			InetAddress address = s.getInetAddress();
			int port = s.getPort();

			NIOJICPConnectionWrapper connection = mgr.getConnection();
			NIOMediator mediator = mgr.getMediator();
			JICPPacket reply = null;
			// If there is no mediator associated to this key prepare to close
			// the connection when the packet will have been processed
			boolean closeConnection = (mediator == null);
			// If the connection will be locked (see NIOJICPConnectionWrapper) prepare 
			// to unlock it on completion
			boolean keepLock = false;

			// STEP 1) Serve the received packet
			int type = pkt.getType();
			String recipientID = pkt.getRecipientID();
			try {
				switch (type) {
				case JICPProtocol.GET_SERVER_TIME_TYPE: {
					connection.lock();
					// Respond sending back the current time encoded as a String
					myLogger.log(Logger.INFO, myLogPrefix + "GET_SERVER_TIME request received from " + address + ":" + port);
					reply = new JICPPacket(JICPProtocol.RESPONSE_TYPE, JICPProtocol.DEFAULT_INFO, String.valueOf(System.currentTimeMillis()).getBytes());
					break;
				}
				case JICPProtocol.GET_ADDRESS_TYPE: {
					// Respond sending back the caller address
					myLogger.log(Logger.INFO, myLogPrefix + "GET_ADDRESS request received from " + address + ":" + port);
					reply = new JICPPacket(JICPProtocol.RESPONSE_TYPE, JICPProtocol.DEFAULT_INFO, address.getHostAddress().getBytes());
					break;
				}
				case JICPProtocol.GET_CONFIG_OPTIONS_TYPE: {
					// Respond sending back the configuration options
					myLogger.log(Logger.INFO, myLogPrefix + "GET_CONFIGURATION_OPTIONS request received from " + address + ":" + port);
					String replyMsg = encodeConfigOptionsResponse();
					reply = new JICPPacket(JICPProtocol.RESPONSE_TYPE, JICPProtocol.DEFAULT_INFO, replyMsg.getBytes());
					break;
				}
				case JICPProtocol.CREATE_MEDIATOR_TYPE: {
					createMediatorCounter++;
					if (mediator == null) {
						// Create a new Mediator
						myLogger.log(Logger.INFO, myLogPrefix + "CREATE_MEDIATOR request received from " + address + ":" + port);

						// Lock the connection to be sure nothing is written back until we have sent the response (see NIOJICPConnectionWrapper)
						connection.lock();
						Properties p = FrontEndStub.parseCreateMediatorRequest(new String(pkt.getData()));

						// If the platform-name is specified check if it is consistent
						String pn = p.getProperty(Profile.PLATFORM_ID);
						if (pn != null && !pn.equals(platformName)) {
							myLogger.log(Logger.WARNING, myLogPrefix + "Security attack! CREATE_MEDIATOR request with wrong platform name: " + pn);
							reply = new JICPPacket(JICPProtocol.NOT_AUTHORIZED_ERROR, new JADESecurityException("Wrong platform-name"));
							break;
						}

						p.setProperty(BEManagementHelper.FRONT_END_HOST, address.getHostAddress());

						String owner = p.getProperty(Profile.OWNER);
						myLogger.log(Logger.CONFIG, myLogPrefix + "Owner = " + owner);
						try {
							Properties pdpContextInfo = myPDPContextManager.getPDPContextInfo(address, owner);
							mergeProperties(p, pdpContextInfo);
						} catch (JADESecurityException jse) {
							myLogger.log(Logger.WARNING, myLogPrefix + "Security error! CREATE_MEDIATOR request from non authorized client [" + address + "]. "+jse.getMessage());
							reply = new JICPPacket(JICPProtocol.NOT_AUTHORIZED_ERROR, jse);
							break;
						}
						// Get mediator ID from the passed properties (if present)
						String id = p.getProperty(JICPProtocol.MEDIATOR_ID_KEY);
						String msisdn = p.getProperty(PDPContextManager.MSISDN);
						if (id != null) {
							if (msisdn != null && !msisdn.equals(id)) {
								// Security attack: Someone is pretending to be someone else
								myLogger.log(Logger.WARNING, myLogPrefix + "Security attack! CREATE_MEDIATOR request with mediator-id != MSISDN. Address is: " + address);
								reply = new JICPPacket(JICPProtocol.NOT_AUTHORIZED_ERROR, new JADESecurityException("Inconsistent mediator-id and msisdn"));
								break;
							}
							// An existing front-end whose back-end was lost. The BackEnd must resynch
							p.setProperty(jade.core.BackEndContainer.RESYNCH, "true");
						} else {
							// Use the MSISDN (if present)
							id = msisdn;
							if (id == null) {
								// Construct a default id using the string representation of the server's TCP endpoint
								id = "BE-" + getLocalHost() + '_' + getLocalPort() + '-' + nextMediatorCnt();
							}
						}

						// If last connection from the same device aborted, the old
						// BackEnd may still exist as a zombie. In case ids are assigned
						// using the MSISDN the new name is equals to the old one.
						if (id.equals(msisdn)) {
							NIOMediator old = mediators.get(id);
							if (old != null) {
								// This is a zombie mediator --> kill it
								myLogger.log(Logger.INFO, myLogPrefix + "Replacing old mediator " + id);
								old.kill();
								// Be sure the zombie container has been removed from the Main Container tables
								waitABit(1000);
							}
						}

						// Create and start the new mediator
						mediator = startMediator(id, p);
						configureBlocking(pkt, key);
						closeConnection = !mediator.handleIncomingConnection(connection, pkt, address, port);
						mediators.put(mediator.getID(), mediator);

						if (!closeConnection) {
							// The mediator wants to keep this connection open --> associate
							// it to the current key
							mgr.setMediator(mediator);
						}

						// Create an ad-hoc reply including the assigned mediator-id and the IP address
						p.setProperty(JICPProtocol.MEDIATOR_ID_KEY, mediator.getID());
						p.setProperty(JICPProtocol.LOCAL_HOST_KEY, address.getHostAddress());
						String replyMsg = FrontEndStub.encodeCreateMediatorResponse(p);
						reply = new JICPPacket(JICPProtocol.RESPONSE_TYPE, JICPProtocol.DEFAULT_INFO, replyMsg.getBytes());
						reply.setSessionID((byte) 31); // Dummy session ID != from valid ones
						
						if ("true".equals(p.getProperty(JICPProtocol.GET_SERVER_TIME_KEY))) {
							// A GET_SERVER_TIME request will arrive just after mediator creation --> do not unlock the connection (this will be done after serving the GET_SERVER_TIME request) 
							keepLock = true;
							// FIXME: Should we start a WatchDog?
						}
					} else {
						myLogger.log(Logger.WARNING, myLogPrefix + "CREATE_MEDIATOR request received on a connection already linked to an existing mediator");
						reply = new JICPPacket("Unexpected packet type", null);
					}
					break;
				}
				case JICPProtocol.CONNECT_MEDIATOR_TYPE: {
					connectMediatorCounter++;
					if (mediator == null) {
						myLogger.log(Logger.INFO, myLogPrefix + "CONNECT_MEDIATOR request received from " + address + ":" + port + ". ID=" + recipientID);

						// FIXME: If there is a PDPContextManager  check that the recipientID is the MSISDN.
						// Where should we get the owner from? It should likely be replicated in each
						// CONNECT_MEDIATOR request.
						/*if (myPDPContextManager != null) {
                            Properties pdpContextInfo = myPDPContextManager.getPDPContextInfo(address, "OWNER???");
                            if (pdpContextInfo != null) {
                            String msisdn = pdpContextInfo.getProperty(PDPContextManager.MSISDN);
                            if (msisdn == null || !msisdn.equals(recipientID)) {
                            myLogger.log(Logger.WARNING, myLogPrefix+"Security attack! CONNECT_MEDIATOR request with mediator-id != MSISDN. Address is: "+address);
                            reply = new JICPPacket(JICPProtocol.NOT_AUTHORIZED_ERROR, null);
                            break;
                            }
                            }
                            else {
                            myLogger.log(Logger.WARNING, myLogPrefix+"Security attack! CONNECT_MEDIATOR request from non authorized address: "+address);
                            reply = new JICPPacket(JICPProtocol.NOT_AUTHORIZED_ERROR, null);
                            break;
                            }
                            }*/

						// Retrieve the mediator to connect to
						mediator = getFromID(recipientID);

						if (mediator != null && mediator.getID() != null) {
							configureBlocking(pkt, key);
							closeConnection = !mediator.handleIncomingConnection(connection, pkt, address, port);
							if (!closeConnection) {
								// The mediator wants to keep this connection open --> associate
								// it to the current key
								mgr.setMediator(mediator);
							}
							reply = new JICPPacket(JICPProtocol.RESPONSE_TYPE, JICPProtocol.DEFAULT_INFO, address.getHostAddress().getBytes());
						} else {
							mediatorNotFoundCounter++;
							myLogger.log(Logger.WARNING, myLogPrefix + "Mediator " + recipientID + " not found");
							reply = new JICPPacket(JICPProtocol.NOT_FOUND_ERROR, null);
						}
					} else {
						myLogger.log(Logger.WARNING, myLogPrefix + "CONNECT_MEDIATOR request received on a connection already linked to an existing mediator");
						reply = new JICPPacket("Unexpected packet type", null);
					}
					break;
				}
				default: {
					// Pass all other JICP packets (commands, responses, keep-alives ...) to the proper mediator.
					if (type == JICPProtocol.COMMAND_TYPE) {
						incomingCommandCounter++;
					}
					else if (type == JICPProtocol.KEEP_ALIVE_TYPE) {
						keepAliveCounter++;
					}
					else if (type == JICPProtocol.DROP_DOWN_TYPE) {
						dropDownCounter++;
					}
					
					if (mediator == null) {
						mediator = getFromID(recipientID);
					}

					if (mediator != null) {
						if (myLogger.isLoggable(Logger.FINEST)) {
							myLogger.log(Logger.FINEST, myLogPrefix + "Passing packet of type " + type + " to mediator " + mediator.getID());
						}
						reply = mediator.handleJICPPacket(connection, pkt, address, port);
					} else {
						myLogger.log(Logger.WARNING, myLogPrefix + "No mediator for incoming packet of type " + type);
						if (type == JICPProtocol.COMMAND_TYPE) {
							reply = new JICPPacket(JICPProtocol.NOT_FOUND_ERROR, null);
						}
					}
				}
				} // END of switch
			} catch (Exception e) {
				// Error handling the received packet
				incomingPacketServingErrorCounter++;
				myLogger.log(Logger.WARNING, myLogPrefix + stringify(mediator) + "Error handling incoming packet. ", e);
				
				// If the incoming packet was a request, send back a generic error response
				if (type == JICPProtocol.COMMAND_TYPE ||
						type == JICPProtocol.CREATE_MEDIATOR_TYPE ||
						type == JICPProtocol.CREATE_MEDIATOR_TYPE ||
						type == JICPProtocol.GET_ADDRESS_TYPE) {
					reply = new JICPPacket("Unexpected error", e);
				}
			}

			// STEP 2) Send back the response if any
			if (reply != null) {
				try {
					connection.writePacket(reply);
				} catch (Exception e) {
					incomingPacketServingErrorCounter++;
					myLogger.log(Logger.WARNING, myLogPrefix + stringify(mediator) + "Communication error writing return packet to " + address + ":" + port + " [" + e + "]", e);
					closeConnection = true;
				}
			} else {
				// The mediator will reply asynchronously --> keep the connection open
				closeConnection = false;
			}

			// STEP 3) Close the connection if necessary
			try {
				if (closeConnection) {
					try {
						// Close connection
						if (myLogger.isLoggable(Logger.FINEST)) {
							myLogger.log(Logger.FINEST, myLogPrefix + stringify(mediator) + "Closing connection with " + address + ":" + port);
						}
						connection.close();
					} catch (IOException io) {
						myLogger.log(Logger.WARNING, myLogPrefix + stringify(mediator) + "I/O error while closing connection with " + address + ":" + port);
						io.printStackTrace();
					}
				}
			}
			finally {
				// STEP 4) Unlock the connection if necessary
				// (be sure this is executed even if there is an unexpected error in the closure step)
				if (connection.isLocked() && !keepLock) {
					connection.unlock();
				}
			}
		}
		
		private String stringify(NIOMediator mediator) {
			return (mediator != null ? mediator.getID()+" - " : "null");
		}

		private void configureBlocking(JICPPacket pkt, SelectionKey key) {
			if (pkt.getData().length==1 && pkt.getData()[0]==1) {
				try {
					// TODO: better to use non blocking, but refactoring needed
					key.cancel();
					key.channel().configureBlocking(true);
				} catch (Exception e) {
					myLogger.log(Logger.SEVERE, "error configuring blocking", e);
				}
			}
		}

		public void serveException(KeyManager mgr, Exception e) {
			// There was an exception reading the packet. If the current key
			// is associated to a mediator, let it process the exception.
			// Otherwise print a warning.
			NIOJICPConnection connection = mgr.getConnection();
			// If we are closing the connection do not even print the warning
			if (!connection.isClosed()) {
				incomingPacketReadingErrorCounter++;
				NIOMediator mediator = mgr.getMediator();
				if (mediator != null) {
					mediator.handleConnectionError(connection, e);
				} else {
					SelectionKey key = mgr.getKey();
					SocketChannel sc = (SocketChannel) key.channel();
					Socket s = sc.socket();
					InetAddress address = s.getInetAddress();
					int port = s.getPort();
					myLogger.log(Logger.WARNING, myLogPrefix + "Exception reading incoming packet from " + address + ":" + port + " [" + e + "]");
				}
			}

			// Always close the connection
			try {
				connection.close();
			} catch (Exception ex) {
			}
		}

		public NIOMediator getFromID(String id) {
			if (id != null) {
				return mediators.get(id);
			}
			return null;
		}

		/**
        Called by a Mediator to notify that it is no longer active.
        This is often called within the tick() method. In this case
        directly removing the deregistering mediator from the mediators table
        would cause a ConcurrentModificationException --> We just add
        the deregistering mediator to a queue of mediators to be removed.
        The actual remotion will occur at the next tick in a synchronized
        way.
		 */
		public void deregisterMediator(String id) {
			myLogger.log(Logger.CONFIG, myLogPrefix + "Deregistering mediator " + id);
			deregisteredMediators.add(id);
		}

		public void tick(long currentTime) {
			// 1) Check if some LoopManager is stuck 
			int newStuckLMCnt = 0;
			for (LoopManager lm : loopers) {
				if (!lm.isStuck()) {
					if (lm.getReadElapsedTime(currentTime) > 60000) {
						// LoopManager stuck!!
						Thread lmThread = lm.myThread;
						if (lmThread.isInterrupted()) {
							// This LoopManager was already interrupted once and did not recover --> There is nothing we can do. Mark it as STUCK
							lm.setStuck();
							int runningLoopersCnt = countRunningLoopers();
							myLogger.log(Logger.WARNING, "LM-"+lm.myIndex+" did not recover after last interrupt --> Mark it as STUCK. "+runningLoopersCnt+" LoopManagers still working properly");
							if (runningLoopersCnt < (loopers.length / 2)) {
								// More than 50% of LoopManagers are stuck --> Kill JVM
								myLogger.log(Logger.SEVERE, "More than 50% of LoopManagers are stuck --> Kill JVM!");
								System.exit(300);
							}
						}
						else {
							newStuckLMCnt++;
							StackTraceElement[] stackTrace = lmThread.getStackTrace();
							StringBuffer sb = new StringBuffer();
							for(int i=0; i<stackTrace.length; i++) {
								sb.append("\t at " + stackTrace[i] + "\n");
							}
							myLogger.log(Logger.WARNING, "LM-"+lm.myIndex+" appears to be stuck in handling incoming data from the network. Try to kill it! Thread stack trace is\n"+sb.toString());
							lmThread.interrupt();
						}
					}
				}
			}
			if (newStuckLMCnt > 0) {
				myLogger.log(Logger.WARNING, "Full thread dump\n----------------------------------\n"+ThreadDumpManager.dumpAllThreads());
			}
			// 2) Forward the tick to all mediators
			NIOMediator[] mm = null;
			synchronized (mediators) {
				mm = mediators.values().toArray(new NIOMediator[0]);
			}
			for (NIOMediator m : mm) {
				m.tick(currentTime);
			}
			// 3) Remove mediators that have deregistered since the last tick
			String[] dms = null;
			synchronized (deregisteredMediators) {
				dms = deregisteredMediators.toArray(new String[0]);
				deregisteredMediators.clear();
			}
			for (String id : dms) {
				synchronized (mediators) {
					NIOMediator m = mediators.remove(id);
					if (m.getID() != null) {
						// A new mediator with the same ID started in the meanwhile.
						// It must not be removed.
						mediators.put(m.getID(), m);
					}
				}
			}
		}

		///////////////////////////////////////
		// PDPContextManager interface
		///////////////////////////////////////
		/**
        If no PDPContextManager is specified in the properties file
        the IOEventServer itself is used and the default behaviour
        is to issue an INCOMING_CONNECTION outgoing command
		 */
		public Properties getPDPContextInfo(InetAddress addr, String owner) throws JADESecurityException {
			GenericCommand cmd = new GenericCommand(INCOMING_CONNECTION, getName(), null);
			cmd.addParam(myID);
			cmd.addParam(addr);
			cmd.addParam(owner);

			try {
				myLogger.log(Logger.CONFIG, myLogPrefix + "Issuing V-Command " + INCOMING_CONNECTION);
				Object ret = submit(cmd);
				if (ret != null) {
					if (ret instanceof Properties) {
						// PDP Context properties detected
						myLogger.log(Logger.FINER, myLogPrefix + "PDPContextProperties for address " + addr + " owner " + owner + " = " + ret);
						return (Properties) ret;
					} else if (ret instanceof JADESecurityException) {
						// Incoming connection from non-authorized device
						myLogger.log(Logger.WARNING, myLogPrefix + "Address " + addr + " owner " + owner + " not authenticated.");
						throw ((JADESecurityException) ret);
					} else if (ret instanceof Throwable) {
						// Unexpected exception
						myLogger.log(Logger.WARNING, myLogPrefix + "Error retrieving PDPContextPropert for address " + addr + " owner " + owner, (Throwable) ret);
						throw new JADESecurityException(((Throwable) ret).getMessage());
					}
				}

				// If we get here, no installed service is able to retrieve
				// the PDPContext properties --> Let all through with
				// empty properties
				return new Properties();
			} catch (ServiceException se) {
				// Should never happen
				se.printStackTrace();
				return null;
			}
		}

		public void init(Properties pp) {
			// Just do nothing
		}

		public void registerListener(PDPContextManager.Listener l) {
			// Just do nothing
		}

		///////////////////////////////////////
		// PDPContextManager.Listener interface
		///////////////////////////////////////
		/**
        Called by the PDPContextManager (if any)
		 */
		public void handlePDPContextClosed(String id) {
			// FIXME: to be implemented
		}

		protected NIOMediator startMediator(String id, Properties p) throws Exception {
			String className = p.getProperty(JICPProtocol.MEDIATOR_CLASS_KEY);
			if (className == null) {
				// Default NIOMediator class
				className = NIOBEDispatcher.class.getName();
			}
			NIOMediator m = (NIOMediator) Class.forName(className).newInstance();
			mergeProperties(p, leapProps);
			myLogger.log(Logger.INFO, myLogPrefix + "Initializing mediator " + id + " with properties " + p);
			m.init(this, id, p);
			return m;
		}
		
		private int countRunningLoopers() {
			int cnt = 0;
			for (LoopManager lm : loopers) {
				if (!lm.isStuck()) {
					cnt++;
				}
			}
			return cnt;
		}
		
		public int getSocketCnt() {
			int cnt = 0;
			// Start from 1: LM 0 does not manage sockets
			for (int i = 1; i < loopers.length; ++i) {
				cnt += loopers[i].size();
			}
			return cnt;
		}
	} // END of inner class IOEventServer


	/**
    Inner class KeyManager
    Keep a SelectionKey together with the information associated to it
    such as the connection wrapping the key channel and the mediator
    using that connection (if any).
	 */
	private class KeyManager {

		private SelectionKey key;
		private NIOJICPConnectionWrapper connection;
		private NIOMediator mediator;
		private IOEventServer server;

		public KeyManager(SelectionKey k, NIOJICPConnectionWrapper c, IOEventServer s) {
			key = k;
			connection = c;
			server = s;
		}

		public final NIOMediator getMediator() {
			return mediator;
		}

		public final void setMediator(NIOMediator m) {
			mediator = m;
		}

		public final NIOJICPConnectionWrapper getConnection() {
			return connection;
		}

		public final SelectionKey getKey() {
			return key;
		}

		/**
        Read some data from the connection associated to the managed key
        and let the IOEventServer serve it
		 */
		public final void read() {
			
			try {
				do {
					JICPPacket pkt = connection.readPacket();
					server.servePacket(this, pkt);
				} while (connection.moreDataAvailable());
			} catch (PacketIncompleteException pie) {
				// The data ready to be read is not enough to complete
				// a packet. Just do nothing and wait until more data is ready
				if (myLogger.isLoggable(Logger.FINE)) {
					myLogger.log(Logger.FINE, "Incomplete JICPPacket (" + pie.getMessage() + ") from connection " + connection + ". Wait for more data..");
				}
			} catch (Exception e) {
				server.serveException(this, e);
			}
		}
	} // END of inner class KeyManager

	/**
	 * Inner class LoopManager
	 */
	private class LoopManager implements Runnable {

		private int myIndex;
		private String displayId;
		private int state = INIT_STATE;
		private int replaceCnt;
		private Selector mySelector;
		private Thread myThread;
		private IOEventServer myServer;
		private boolean pendingChannelPresent = false;
		private List pendingChannels = new ArrayList();
		
		private long readStartTime = -1;
		private boolean stuck = false;

		public LoopManager(IOEventServer server, int index) {
			myServer = server;
			myIndex = index;
			String id = myServer.getID();
			displayId = "BEManagementService" + (PREFIX.startsWith(id) ? "" : "-" + id);
			replaceCnt = 0;

			try {
				mySelector = Selector.open();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		
		private LoopManager(LoopManager lm) {
			myServer = lm.myServer;
			myIndex = lm.myIndex;
			displayId = lm.displayId;
			pendingChannelPresent = lm.pendingChannelPresent;
			pendingChannels = lm.pendingChannels;
			replaceCnt = lm.replaceCnt + 1;
			
			try {
				mySelector = Selector.open();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}

		public void start() {
			state = ACTIVE_STATE;
			myThread = new Thread(this);
			myThread.setName(displayId + "-LM" + myIndex + "-R" + replaceCnt);
			myThread.start();
		}

		public void stop() {
			state = TERMINATING_STATE;
			mySelector.wakeup();
		}

		public void join() throws InterruptedException {
			myThread.join(5000);
			if (myThread.isAlive()) {
				myLogger.log(Logger.WARNING, "Thread " + myThread.getName() + " did not terminate when requested to do so");
			}
		}

		public void run() {
			myLogger.log(Logger.INFO, "Thread " + Thread.currentThread().getName() + " started");
			String prefix = myServer.getLogPrefix() + "LM-" + myIndex +": ";
			
			// This call is necessary if this is a replaced LoopManager. It has no effects otherwise
			handlePendingChannels(prefix);
			
			int selectBugCounter = 0;
			while (state == ACTIVE_STATE) {
				int n = 0;
				try {
					// Wait for the next IO events
					long startSelect = System.currentTimeMillis();
					n = mySelector.select();
					if (waitForDataTimeProvider != null) {
						waitForDataTimeProvider.addSample(System.currentTimeMillis() - startSelect);
					}
				} catch (NullPointerException npe) {
					// There seems to be a bug in java.nio (http://www.limewire.org/techdocs/nio2.html).
					// Just retry.
					myLogger.log(Logger.WARNING, myServer.getLogPrefix() + "NullPointerException in select. Ignore and retry.");
					continue;
				} catch (Exception e) {
					if (state == ACTIVE_STATE) {
						myLogger.log(Logger.SEVERE, myServer.getLogPrefix() + "Error selecting next IO event. ", e);
						// Abort
						state = ERROR_STATE;
					}
				}
				if (state == ACTIVE_STATE) {
					Set keys = mySelector.selectedKeys();
					int keysSize = keys.size();
					if(myLogger.isLoggable(Logger.FINE)) {
						myLogger.log(Logger.FINE, prefix + " n=" + n + " selected-keys=" + keysSize + (n != keysSize ? " This is very strange!!!!" : ""));
					}
					if (n > 0 && keysSize > 0) {
						// NOTE that the check on keys.size() is redundant and is there just to handle the "select bug" (see handelSelectBug())
						selectBugCounter = 0;
						Iterator it = keys.iterator();
						while (it.hasNext()) {
							SelectionKey key = (SelectionKey) it.next();
							if (key.isValid()) {
								if ((key.readyOps() & SelectionKey.OP_ACCEPT) != 0) {
									// This is an incoming connection. The channel must be the SerevrSocketChannel server
									if (myLogger.isLoggable(Logger.FINER)) {
										myLogger.log(Logger.FINER, prefix + "------------------ ACCEPT_OP on key "+key);
									}
									handleAcceptOp(key, prefix);
								} else if ((key.readyOps() & SelectionKey.OP_READ) != 0) {
									try {
										// This is some incoming data for one of the BE
										if (myLogger.isLoggable(Logger.FINER)) {
											myLogger.log(Logger.FINER, prefix + "READ_OP on key "+key);
										}
										handleReadOp(key, prefix);
									} catch (ICPException ex) {
										myLogger.log(Logger.SEVERE, "failed to read from socket", ex);
									}
								}
								else {
									myLogger.log(Logger.WARNING, prefix + "SelectedKey "+key+" has unknown OPTIONS "+key.readyOps());
								}
							}
							else {
								myLogger.log(Logger.WARNING, prefix + "SelectedKey "+key+" NOT VALID");
							}
							it.remove();
						}
					} else {
						selectBugCounter++;

						if (selectBugCounter > 100) {
							// This means that we are looping due to problems in the select() (known Linux JVM bug) 
							handleSelectBug();
						}
					}
					handlePendingChannels(prefix);
				}
			} // END of while

			try {
				mySelector.close();
			}
			catch (Exception e) {}
			state = TERMINATED_STATE;
			
			myLogger.log(Logger.INFO, "Thread " + Thread.currentThread().getName() + " terminated");			
		}
		
		
		/**
		 * JDK 1.6 in Linux has a BUG so that Selector.select() in certain cases do not block causing an infinite loop.
		 * The workaround is to recreate the Selector. Actually we recreate the whole LoopManager
		 */
		private synchronized void handleSelectBug() {
			// Mutual exclusion with register()
			myLogger.log(Logger.WARNING, Thread.currentThread().getName() + " SELECT BUG OCCURRED!!!! Replacing LoopManager");
			LoopManager lm = new LoopManager(this);
			myServer.replaceLoopManager(myIndex, lm); // This also makes the currentLoopManager terminate
		}

		private final void handleAcceptOp(SelectionKey key, String prefix) {
			try {
				SocketChannel sc = ((ServerSocketChannel) key.channel()).accept();

				checkAddress(sc);

				sc.configureBlocking(false);
				LoopManager lm = myServer.getLooper();
				lm.register(sc);
				myLogger.log(Logger.INFO, prefix + "Incoming socket " + sc + " assigned to LM-"+lm.myIndex+" ("+lm.size()+")");
			} catch (JADESecurityException jse) {
				myLogger.log(Logger.WARNING, prefix + "Connection attempt from malicious address " + jse.getMessage());
			} catch (NotFoundException nfe) {
				myLogger.log(Logger.SEVERE, prefix + "NO LoopManager available to handle incoming socket!!!!");
			} catch (Exception e) {
				myLogger.log(Logger.WARNING, prefix + "Error accepting incoming connection. ", e);
			}
		}

		private final void handleReadOp(SelectionKey key, String prefix) throws ICPException {
			readStartTime = System.currentTimeMillis();
			boolean isNewConnection = false; 
			try {
				KeyManager mgr = (KeyManager) key.attachment();
				if (mgr == null) {
					NIOJICPConnection c = myServer.createConnection(key);
					isNewConnection = true;
					mgr = new KeyManager(key, new NIOJICPConnectionWrapper(c), myServer);
					key.attach(mgr);
					myLogger.log(Logger.INFO, prefix + "Connection " + c + " created and associated to KeyManager "+mgr);
				}
				mgr.read();
			}
			finally {
				long elapsedTime = System.currentTimeMillis() - readStartTime;
				if (dataProcessingTimeProvider != null) {
					dataProcessingTimeProvider.addSample(elapsedTime);
				}
				if (elapsedTime > 1000) {
					if (!isNewConnection || elapsedTime > 10000) {
						// If this is the first packet of a new connection (CREATE or CONNECT MEDIATOR)
						// print the warning only if the elapsed time is VERY high
						myLogger.log(Logger.WARNING, prefix + " *** Serve time = "+elapsedTime);
					}
					if (elapsedTime > 10000) {
						processingTimeGT10SecCounter++;
					}
					else {
						processingTimeGT1SecCounter++;
					}
				}
				readStartTime = -1;
			}
		}

		// This is called by LM-0 following an ACCEPT-OP
		private synchronized final void register(SocketChannel sc) {
			pendingChannels.add(sc);
			pendingChannelPresent = true;
			mySelector.wakeup();
		}

		private synchronized final void handlePendingChannels(String prefix) {
			if (pendingChannelPresent) {
				for (int i = 0; i < pendingChannels.size(); ++i) {
					SocketChannel sc = (SocketChannel) pendingChannels.get(i);
					if (myLogger.isLoggable(Logger.FINE)) {
						myLogger.log(Logger.FINE, prefix+"Registering Selector "+mySelector+" on channel " + sc  +" for READ operations");
					}
					try {
						sc.register(mySelector, SelectionKey.OP_READ);
					} catch (Exception e) {
						myLogger.log(Logger.WARNING, prefix + "Error registering socket channel for asynchronous IO. ", e);
					}
				}
				pendingChannels.clear();
				pendingChannelPresent = false;
			}
		}

		public final Selector getSelector() {
			return mySelector;
		}
		
		public final int size() {
			return mySelector.keys().size() + pendingChannels.size();
		}
		
		public final long getReadElapsedTime(long now) {
			if (readStartTime > 0) {
				return now - readStartTime;
			}
			else {
				return -1;
			}
		}
		
		public final boolean isStuck() {
			return stuck;
		}
		
		public final void setStuck() {
			stuck = true;
		}
	} // END of inner class LoopManager

	/**
	 * Inner class Ticker
	 */
	private class Ticker extends Thread {

		private long period;
		private boolean active = false;

		private Ticker(long period) {
			super();
			this.period = period;
		}

		public void start() {
			active = true;
			setName("BEManagementService-ticker-master");
			super.start();
		}

		public void run() {
			while (active) {
				try {
					Thread.sleep(period);
					final long currentTime = System.currentTimeMillis();
					if(myLogger.isLoggable(Logger.FINE)) {
						myLogger.log(Logger.FINE,  "Ticker: Tick begin. Current time = "+currentTime);
					}
					Thread t = new Thread() {
						public void run() {
							Object[] ss = servers.values().toArray();
							for (int i = 0; i < ss.length; ++i) {
								((IOEventServer) ss[i]).tick(currentTime);
							}
							if(myLogger.isLoggable(Logger.FINE)) {
								myLogger.log(Logger.FINE,  "Ticker: Tick end. Current time = "+currentTime);
							}
						}
					};
					t.setName("BEManagementService-ticker-"+currentTime);
					t.setDaemon(true);
					t.start();
				} catch (Throwable t) {
					if (active) {
						myLogger.log(Logger.WARNING, "BEManagementService-Ticker: Unexpected exception ", t);
					}
				}
			}
		}

		public void shutdown() {
			active = false;
			interrupt();
		}
	} // END of inner class Ticker

	/**
	 * Inner class BEManagementHelperImpl.
	 * Implementation class for the BEManagementHelper.
	 */
	private class BEManagementHelperImpl implements BEManagementHelper {

		public void init(Agent a) {
			// Just do nothing;
		}

		public String getProperty(String containerName, String key) {
			String value = null;
			NIOMediator mediator = findMediatorGlobally(containerName);
			if (mediator != null) {
				Properties pp = mediator.getProperties();
				if (pp != null) {
					value = pp.getProperty(key);
				}
			}
			return value;
		}

		private NIOMediator findMediatorGlobally(String id) {
			Object[] ss = servers.values().toArray();
			for (int i = 0; i < ss.length; ++i) {
				NIOMediator m = ((IOEventServer) ss[i]).getFromID(id);
				if (m != null) {
					return m;
				}
			}
			return null;
		}
	} // END of inner class BEManagementHelperImpl

	///////////////////////////////////////
	// Utility methods
	///////////////////////////////////////
	private void mergeProperties(Properties p1, Properties p2) {
		Enumeration e = p2.propertyNames();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			p1.setProperty(key, p2.getProperty(key));
		}
	}

	private void waitABit(long t) {
		try {
			Thread.sleep(t);
		} catch (InterruptedException ie) {
		}
	}

	/**
    Check that the address of the initiator of a new connection
    is not in the list of addresses considered as malicious.
	 */
	private final void checkAddress(SocketChannel sc) throws JADESecurityException {
		Socket s = sc.socket();
		InetAddress address = s.getInetAddress();
		if (maliciousAddresses.contains(address)) {
			try {
				sc.close();
			} catch (Exception e) {
			}
			throw new JADESecurityException(address.toString());
		}
	}
}

