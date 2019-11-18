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

// Take care that the DOTNET build file (dotnet.xml) uses this file (it is copied just after the preprocessor excluded it)
//#J2ME_EXCLUDE_FILE
//#APIDOC_EXCLUDE_FILE

import jade.core.AgentContainer;
import jade.core.ContainerID;
import jade.core.GenericCommand;
import jade.core.HorizontalCommand;
import jade.core.MainContainer;
import jade.core.NodeDescriptor;
import jade.core.NotFoundException;
import jade.core.Profile;
import jade.core.ProfileException;
import jade.core.NodeFailureMonitor;
import jade.core.Node;
import jade.core.Service;
import jade.core.IMTPException;
import jade.core.ServiceException;
import jade.core.VerticalCommand;
import jade.core.Filter;
import jade.core.ServiceManager;

import jade.util.Logger;

import java.util.Hashtable;
import java.util.Enumeration;

/**
 * UDP based implementation of the NodeMonitoringService. 
 */
public class UDPNodeMonitoringService extends NodeMonitoringService {
	private static final String PREFIX = "jade_core_nodeMonitoring_UDPNodeMonitoringService_";

	/**
	 * The name of this service
	 */
	public static final String NAME = "jade.core.nodeMonitoring.UDPNodeMonitoring";

	/**
	 * This constant is the name of the property whose value contains 
	 * the hostname where the Main Container is listening for UDP pings.
	 * If not specified the value of the Profile.LOCAL_HOST option is used.
	 * If this is null too the default network name is used. 
	 */
	public static final String HOST = PREFIX + "host";
	
	/**
	 * This constant is the name of the property whose value contains 
	 * a boolean indication that specifies whether or not UDP pings must be accepted 
	 * on the indicated local-host only (default = false). 
	 */
	public static final String ACCEPT_LOCAL_HOST_ONLY = PREFIX + "acceptlocalhostonly";
	
	/**
	 * This constant is the name of the property whose value contains an
	 * integer representing the port number where the Main Container is
	 * listening for UDP pings. 
	 */
	public static final String PORT = PREFIX + "port";

	/**
	 * This constant is the name of the property whose value contains an
	 * integer representing the time interval (in milliseconds) in which a peripheral
	 * container sends UDP ping messages to the Main Container.<br>
	 * This property is only meaningful on a peripheral container.
	 */
	public static final String PING_DELAY = PREFIX + "pingdelay";

	/**
	 * This constant is the name of the property whose value contains an
	 * integer representing the maximum time (in milliseconds) the main container 
	 * waits for a ping message before considering the peripheral container
	 * unreachable.<br>
	 * This property is only meaningful on a main container.
	 */
	public static final String PING_DELAY_LIMIT = PREFIX + "pingdelaylimit";

	/**
	 * This constant is the name of the property whose value contains an
	 * integer representing the maximum time a node can stay unreachable after it gets removed
	 * from the platform.<br>
	 * This property is only meaningful on a main container.
	 */
	public static final String UNREACHABLE_LIMIT = PREFIX + "unreachablelimit";
	
	/**
	 * This constant is the name of the property whose value contains an
	 * integer representing the number of UDP ping packets that must be received from an un-monitored node
	 * before considering it an orphan node and issuing an Orphan-Node Vertical command.<br>
	 * The default for this property is 10.<br>
	 * This property is only meaningful on a main container.
	 */
	public static final String ORPHAN_NODE_PINGS_CNT = PREFIX + "orphannodepingscnt";
	
	/**
	 * This constant is the name of the property whose value contains an
	 * integer representing the maximum number of UDP ping packets received from an un-monitored node that are traced.
	 * Successive packets from the same un-monitored node will be completely ignored
	 * The default for this property is 100.<br>
	 * This property is only meaningful on a main container.
	 */
	public static final String MAX_TRACED_UNKNOWN_PINGS = PREFIX + "maxtracedunknownpings";
	
	/**
	 * This constants is the name of the property whose value contains the fully qualified 
	 * class name of a concrete implementation of the <code>NetworkChecker</code> interface.
	 * If this property is set, before considering dead a monitored node that remains
	 * unreachable for more than the <code>unreachable-limit</code>, the specified 
	 * <code>NetworkChecker</code> is activated to check if the lack of UDP ping packets depends
	 * on a network disconnection. If this is the case the node is kept in the <code>UNREACHABLE</code>
	 * state and is not removed.<br>
	 * This property is only meaningful on a main container.
	 * @see NetworkChecker  
	 */
	public static final String NETWORK_CHECKER = PREFIX + "networkchecker";
	
	/**
	 * Default port on which the server is waiting for ping messages
	 */
	public static final int DEFAULT_PORT = 28000;

	/**
	 * Default time between two outgoing pings
	 */
	public static final int DEFAULT_PING_DELAY = 1000;

	/**
	 * Default maximum time the server waits for a ping
	 */
	public static final int DEFAULT_PING_DELAY_LIMIT = 3000;

	/**
	 * Default maximum time a node can stay unreachable
	 */
	public static final int DEFAULT_UNREACHABLE_LIMIT = 10000;

	/**
	   Vertical command issued on the Main Container 
	   when a given number of ping packets are received from an unknown node
	 */
	public static final String ORPHAN_NODE = "Orphan-Node";
	
	private static final String[] OWNED_COMMANDS = new String[] {
		NODE_UNREACHABLE,
		NODE_REACHABLE,
		ORPHAN_NODE
	};
	
	
	private UDPMonitorServer myServer;
	private Hashtable myClients = new Hashtable(2);
	
	private ServiceManager myServiceManager;
	private MainContainer mainContainer;
	
	private ServiceComponent localSlice = new ServiceComponent();
	private Filter incFilter = new UDPMonitorIncomingFilter();

	public String getName() {
		return NAME;
	}

	public String[] getOwnedCommands() {
		return OWNED_COMMANDS;
	}
	
	public void init(AgentContainer ac, Profile p) throws ProfileException {
		super.init(ac, p);	
		myServiceManager = ac.getServiceManager();
		
		mainContainer = ac.getMain();
		if (mainContainer != null) {
			// We are on the main container --> launch a UDPMonitorServer
			String host = p.getParameter(HOST, p.getParameter(Profile.LOCAL_HOST, Profile.getDefaultNetworkName(p.getBooleanProperty(Profile.PRIVILEDGE_LOGICAL_NAME, false)))); 
			boolean acceptLocalHostOnly = p.getBooleanProperty(ACCEPT_LOCAL_HOST_ONLY, false);
			int port = getPosIntValue(p, PORT, DEFAULT_PORT);
			int pingDelay = getPosIntValue(p, PING_DELAY, DEFAULT_PING_DELAY);
			int pingDelayLimit = getPosIntValue(p, PING_DELAY_LIMIT, DEFAULT_PING_DELAY_LIMIT);
			int unreachLimit = getPosIntValue(p, UNREACHABLE_LIMIT, DEFAULT_UNREACHABLE_LIMIT);
			int orphanNodePingsCnt = getPosIntValue(p, ORPHAN_NODE_PINGS_CNT, 10);
			int maxTracedUnknownPings = getPosIntValue(p, MAX_TRACED_UNKNOWN_PINGS, 100);
			
			NetworkChecker checker = initNetworkChecker(p);
			
			try {
				myServer = new UDPMonitorServer(this, host, acceptLocalHostOnly, port, pingDelay, pingDelayLimit, unreachLimit, orphanNodePingsCnt, maxTracedUnknownPings, checker);
				myServer.start();
				// Port may have changed
				port = myServer.getPort();
				myLogger.log(Logger.INFO, "UDPMonitorServer successfully started. Host = " + host + ", port = " + port + " pingdelaylimit = " + pingDelayLimit + " unreachablelimit = " + unreachLimit);
			} catch (Exception e) {
				String s = "Error creating UDP monitoring server";
				myLogger.log(Logger.SEVERE, s);
				throw new ProfileException(s, e);
			}
		} 
	}

	private NetworkChecker initNetworkChecker(Profile p) {
		NetworkChecker checker = null;
		String networkCheckerClass = p.getParameter(NETWORK_CHECKER, null);
		if (networkCheckerClass != null) {
			try {
				checker = (NetworkChecker) Class.forName(networkCheckerClass).newInstance();
				checker.initialize(p);
			}
			catch (Exception e) {
				myLogger.log(Logger.WARNING, "NetworkChecker "+networkCheckerClass+" cannot be created, instantiated or initialized.", e);
			}
		}
		return checker;
	}

	public NodeFailureMonitor getFailureMonitor() {
		if (myServer != null) {
			return new UDPNodeFailureMonitor(myServer, this);
		} else {
			return null;
		}
	}

	public void shutdown() {
		// Stop the server
		if (myServer != null) {
			myServer.stop();
			myServer = null;
		}
		// Stop all clients
		synchronized (myClients) {
			Enumeration en = myClients.elements();
			while (en.hasMoreElements()) {
				UDPMonitorClient client = (UDPMonitorClient) en.nextElement();
				client.stop(true);
			}
			myClients.clear();
		}
	}

	public Filter getCommandFilter(boolean direction){
	    if (direction == Filter.INCOMING){
	    	return incFilter;
	    }
	    else {
	    	return null;
	    }
	}
	
	public Class getHorizontalInterface() {
		return UDPNodeMonitoringSlice.class;
	}
	
	public Service.Slice getLocalSlice() {
		return localSlice;
	}
	
	// NOTE: This method is only used to support tests
	protected void setClientsPingDelay(int delay) {
		synchronized (myClients) {
			Enumeration en = myClients.elements();
			while (en.hasMoreElements()) {
				UDPMonitorClient client = (UDPMonitorClient) en.nextElement();
				client.setPingDelay(delay);
			}
		}		
	}
	
	/**
	 * Extracts an integer value from a given profile. If the value
	 * is less than zero it returns the specified default value
	 * @param p profile
	 * @param paramName name of the parameter in the profile
	 * @param defaultValue default value
	 */
	private static int getPosIntValue(Profile p, String paramName, int defaultValue) {
		int value = Integer.valueOf(p.getParameter(paramName, "-1")).intValue();
		if (value >= 0) {
			return value;
		} else {
			return defaultValue;
		}
	}
	
	void activateUDP(Node n, long key) {
		if (myServer != null) {
			myLogger.log(Logger.CONFIG, "Requesting UDP activation to node "+n.getName());
			try {
				UDPNodeMonitoringSlice slice = (UDPNodeMonitoringSlice) getSlice(n.getName());
				try {
					slice.activateUDP(myServiceManager.getLocalAddress(), myServer.getHost(), myServer.getPort(), myServer.getPingDelay(), key);
				}
				catch (IMTPException imtpe) {
					// Get a fresh slice and try again
					slice = (UDPNodeMonitoringSlice) getFreshSlice(n.getName());
					slice.activateUDP(myServiceManager.getLocalAddress(), myServer.getHost(), myServer.getPort(), myServer.getPingDelay(), key);
				}
			}
			catch (NullPointerException npe) {
				// Slice is null --> The UDPNodeMonitoringService is not installed on the node to be monitored
				myLogger.log(Logger.WARNING, "Can't monitor node "+n.getName()+". UDPNodeMonitoringService not installed.");
			}
			catch (ServiceException se) {
				myLogger.log(Logger.WARNING, "Can't monitor node "+n.getName()+". Service error on remote node: "+se.getMessage());
			}
			catch (IMTPException imtpe1) {
				myLogger.log(Logger.WARNING, "Can't monitor node "+n.getName()+". Node unreachable.");
				imtpe1.printStackTrace();
			}
		}
	}
	
	void deactivateUDP(Node n, long key) {
		try {
			// Ping the node first to avoid using a stale network connection (in case the remote node is dead).
			// In that case in fact we could wait for a long time before getting the socket exception
			n.ping(false);
			UDPNodeMonitoringSlice slice = (UDPNodeMonitoringSlice) getSlice(n.getName());
			// Note that there can't be caching problems in this case since they were (if present) already solved in activateUDP() 
			if (slice != null) {
				slice.deactivateUDP(myServiceManager.getLocalAddress(), key);
			}
		}
		catch (Exception e) {
			// The node is likely dead --> Ignore it.
		}		
	}
	
	private void startUDPClient(String label, String host, int port, int pingDelay, long key) throws ServiceException {
		try {
			// Stop any previous client associated to the same label
			stopUDPClient(label, -1, false);
			UDPMonitorClient client = new UDPMonitorClient(getLocalNode(), host, port, pingDelay, key);
			myClients.put(label, client);
			client.start();
			myLogger.log(Logger.INFO, "UDP Monitor Client for "+label+" successfully started. Host = " + host + " port = " + port + " pingdelay = " + pingDelay);
		} 
		catch (Exception e) {
			// Rollback
			myClients.remove(label);
			throw new ServiceException("Error starting UDP Monitor client.", e);
		}
	}
	
	private void stopUDPClient(String label, long key, boolean sendTerminationFlag) {
		UDPMonitorClient client = (UDPMonitorClient) myClients.get(label);
		if (client != null) {
			if ((key == -1) || (key == client.getKey())) {
				client.stop(sendTerminationFlag);
				myLogger.log(Logger.INFO, "UDP Monitor Client for "+label+" stopped.");
				myClients.remove(label);
			}
		}
	}
	
	void handleOrphanNode(String nodeID) {
		try {
			GenericCommand cmd = new GenericCommand(ORPHAN_NODE, NAME, null);
			cmd.addParam(nodeID);
			submit(cmd);
		}
		catch (Exception e) {
			// Should never happen
			e.printStackTrace();
		}
	}
	
	void pingNode(String nodeID) throws IMTPException {
		try {
			// This method is invoked by the UDPMonitorServer --> it can only be invoked on a Main Container 
			NodeDescriptor dsc = mainContainer.getContainerNode(new ContainerID(nodeID, null));
			dsc.getNode().ping(false);
		}
		catch (NotFoundException nfe) {
			// Node unknown! This should never happen. DO as if it was unreachable
			throw new IMTPException("Unknown node");
		}
	}
	
	/**
	 * Inner class ServiceComponent
	 * A slice is needed to process the H_ACTIVATEUDP and H_DEACTIVATEUDP horizontal commands
	 * that deal with UPD Monitor clients activation 
	 */
	private class ServiceComponent implements Service.Slice {
		public Service getService() {
			return UDPNodeMonitoringService.this;
		}

		public Node getNode() throws ServiceException {
			try {
				return UDPNodeMonitoringService.this.getLocalNode();
			} 
			catch (IMTPException imtpe) {
				throw new ServiceException("Problem in contacting the local IMTP Manager", imtpe);
			}
		}
		
	    public VerticalCommand serve(HorizontalCommand cmd) {
			try {
				String cmdName = cmd.getName();
				Object[] params = cmd.getParams();

				if (cmdName.equals(UDPNodeMonitoringSlice.H_ACTIVATEUDP)) {
					String label = (String) params[0];
					String host = (String) params[1];
					int port = ((Integer) params[2]).intValue();
					int pingDelay = ((Integer) params[3]).intValue();
					long key = ((Long) params[4]).longValue();
					startUDPClient(label, host, port, pingDelay, key);
				} 
				else if (cmdName.equals(UDPNodeMonitoringSlice.H_DEACTIVATEUDP)) {
					String label = (String) params[0];
					long key = ((Long) params[1]).longValue();
					stopUDPClient(label, key, true);
				}
			} 
			catch (Throwable t) {
				cmd.setReturnValue(t);
			}
			return null;
		}
	}
	
	
	/**
	 * Inner class UDPMonitorIncomingFilter
	 * An incoming filter is needed to react to failures of main containers
	 * that are monitoring this node by stopping the related UDP Monitor clients.
	 * This main container is either the main container a node is attached to
	 * or a main container replica if the Main Replication Service is active    
	 */
	private class UDPMonitorIncomingFilter extends Filter {
		public boolean accept(VerticalCommand cmd) {
			String name = cmd.getName();
			Object[] params = cmd.getParams();
			if (name.equals(Service.DEAD_PLATFORM_MANAGER)) {
				String address = (String) params[0];
				stopUDPClient(address, -1, true);
			}
			else if (name.equals(Service.DEAD_REPLICA)) {
				String address = (String) params[0];
				stopUDPClient(address, -1, true);
			}
			// Never veto a command
			return true;
		}

	}
}
