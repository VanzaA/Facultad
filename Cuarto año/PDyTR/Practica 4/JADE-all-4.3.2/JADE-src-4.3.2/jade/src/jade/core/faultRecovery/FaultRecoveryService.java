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

package jade.core.faultRecovery;

//#J2ME_EXCLUDE_FILE
//#APIDOC_EXCLUDE_FILE

import jade.core.GenericCommand;
import jade.core.HorizontalCommand;
import jade.core.ServiceHelper;
import jade.core.VerticalCommand;
import jade.core.Service;
import jade.core.BaseService;
import jade.core.ServiceException;
import jade.core.Filter;
import jade.core.Agent;
import jade.core.AID;
import jade.core.Node;
import jade.core.NodeDescriptor;
import jade.core.AgentContainer;
import jade.core.MainContainer;
import jade.core.Profile;
import jade.core.ProfileException;
import jade.core.IMTPException;
import jade.core.nodeMonitoring.NodeMonitoringService;
import jade.core.nodeMonitoring.UDPNodeMonitoringService;
import jade.core.replication.MainReplicationService;

import jade.util.Logger;

import java.io.*;
import java.util.Map;
import java.util.Iterator;


/**
 The FaultRecovery service allows recovering a platform after a fault
 and a successive restart of the main container.
 
 On a Main container this service keeps track of platform nodes in a 
 persistent storage. When the platform shuts down the persistent storage
 is cleared. At bootstrap time the service gets all nodes from
 the persistent storage (there are nodes only if the main container 
 is restarting after a crash) and notifies them (if still alive)
 about the recovery.
 
 On peripheral containers, the node, when notified about a main recovery,
 re-adds itself to the recovered main (actually this is done by the
 ServiceManager) and issues a Service.REATTACHED incoming V-Command.
 The FaultRecovery service filter intercepts this command and re-adds
 all agents living in the container.
 
 @author Giovanni Caire - TILAB
 */
public class FaultRecoveryService extends BaseService {
	public static final String NAME = FaultRecoveryHelper.SERVICE_NAME;
	
	public static final String CLEAN_STORAGE = "jade_core_faultRecovery_FaultRecoveryService_cleanstorage";
	public static final String PERSISTENT_STORAGE_CLASS = "jade_core_faultRecovery_FaultRecoveryService_persistentstorage";
	public static final String ORPHAN_NODE_POLICY = "jade_core_faultRecovery_FaultRecoveryService_orphannodepolicy";
	
	public static final String PERSISTENT_STORAGE_CLASS_DEFAULT = "jade.core.faultRecovery.FSPersistentStorage";
	
	public static final String ORPHAN_NODE_POLICY_RECOVER = "RECOVER";
	public static final String ORPHAN_NODE_POLICY_KILL = "KILL";
	public static final String ORPHAN_NODE_POLICY_IGNORE = "IGNORE";
	
	// Horizontal command to force the termination of an orphan node when the KILL Orphan Node Policy is set
	public static final String H_KILLNODE = "1";
	
	private AgentContainer myContainer;
	private MainContainer myMain;
	
	private Filter inpFilter;
	private Filter outFilter;
	
	private PersistentStorage myPS;
	private NodeSerializer nodeSerializer;
	
	private boolean bootComplete = false;
	private String orphanNodePolicy;
	
	public void init(AgentContainer ac, Profile p) throws ProfileException {
		super.init(ac, p);
		myContainer = ac;
		myMain = myContainer.getMain();
		if (myMain != null) {
			// Create the command filters
			inpFilter = new MainCommandIncomingFilter();
			outFilter = new MainCommandOutgoingFilter();
			
			// Initialize the serializers
			nodeSerializer = new NodeSerializer();
			
			// Read the policy to handle orphan nodes
			orphanNodePolicy = p.getParameter(ORPHAN_NODE_POLICY, ORPHAN_NODE_POLICY_RECOVER);
			
			// Initialize the PersistentStorage
			String psClass = p.getParameter(PERSISTENT_STORAGE_CLASS, PERSISTENT_STORAGE_CLASS_DEFAULT);
			try {
				myLogger.log(Logger.CONFIG, "Loading PersistentStorage of class "+psClass);
				myPS = (PersistentStorage) Class.forName(psClass).newInstance();
				myPS.init(p);
				boolean cleanStorage = p.getBooleanProperty(CLEAN_STORAGE, false);
				if (cleanStorage) {
					myLogger.log(Logger.CONFIG, "Clearing PersistentStorage ...");
					myPS.clear(true);
				}
			}
			catch (Exception e) {
				String msg = "Error initializing PersistentStorage. ";
				myLogger.log(Logger.SEVERE, msg, e);
				if (myPS != null) {
					myPS.close();
				}
				throw new ProfileException(msg, e);
			}
		}
		else {
			// Create the command incoming filter
			inpFilter = new ContainerCommandIncomingFilter();
		}
	}
	
	public void boot(Profile p) throws ServiceException {
		if (myMain != null) {
			try {
				String[] platformInfo = myPS.getPlatformInfo();
				String oldPlatformName = (platformInfo != null ? platformInfo[0] : null);
				String oldAddress = (platformInfo != null ? platformInfo[1] : null);
				String currentPlatformName = myContainer.getPlatformID();
				String currentAddress = myContainer.getServiceManager().getLocalAddress();
				myPS.storePlatformInfo(currentPlatformName, currentAddress);
				if (currentPlatformName.equals(oldPlatformName)) {
					MainReplicationService replService = (MainReplicationService) myContainer.getServiceFinder().findService(MainReplicationService.NAME);
					// Do not activate the fault recovery procedure if the Main Container is replicated: in that case all containers were already adopted
					// by existing Main Container replicas. 
					if (replService == null || replService.getAllSlices().length <= 1) {
						// FAULT RECOVERY PROCEDURE
						myLogger.log(Logger.INFO, "Initiating fault recovery procedure...");
						// Recover all non-child nodes first
						Map allNodes = myPS.getAllNodes(false);
						Iterator it = allNodes.keySet().iterator();
						while (it.hasNext()) {
							String name = (String) it.next();
							checkNode(name, (byte[]) allNodes.get(name), oldAddress, currentAddress);
						}
						// Then recover all child nodes
						allNodes = myPS.getAllNodes(true);
						it = allNodes.keySet().iterator();
						while (it.hasNext()) {
							String name = (String) it.next();
							checkNode(name, (byte[]) allNodes.get(name), oldAddress, currentAddress);
						}
						
						myLogger.log(Logger.INFO, "Fault recovery procedure completed.");
					}
				}
				else {
					// This is a different platform and we don't want to reattach agents with a different platform name.
					// Also clear old stuff in the persistent storage
					myPS.clear(false);
				}
			}
			catch (Exception e) {
				String msg = "Error recovering from previous fault. ";
				myLogger.log(Logger.SEVERE, msg, e);
				throw new ServiceException(msg, e);
			}
		}
		bootComplete = true;
	}
	
	public void shutdown() {
		if (myPS != null) {
			try {
				myPS.clear(true);
				myPS.close();
			}
			catch (Exception e) {
				myLogger.log(Logger.WARNING, "Unexpected error clearing PersistentStorage. ", e);
			}
		}
	}
	
	public String getName() {
		return NAME;
	}
	
	public Filter getCommandFilter(boolean direction) {
		if(direction == Filter.INCOMING) {
			return inpFilter;
		}
		else {
			return outFilter;
		}
	}
	
	public Service.Slice getLocalSlice() {
		return new Service.Slice() {
			public Service getService() {
				return FaultRecoveryService.this;
			}
			
			public Node getNode() throws ServiceException {
				try {
					return FaultRecoveryService.this.getLocalNode();
				} catch (IMTPException imtpe) {
					throw new ServiceException("Problem in contacting the IMTP Manager", imtpe);
				}
			}
			
			public VerticalCommand serve(HorizontalCommand cmd) {
				try {
					String cmdName = cmd.getName();
					if (cmdName.equals(H_KILLNODE)) {
						suicide();
					}
				} catch (Throwable t) {
					cmd.setReturnValue(t);
				}
				return null;
			}
		};
	}
	
	public ServiceHelper getHelper(Agent a) throws ServiceException {
		// The agent is passed to the helper in the init() method
		return new FaultRecoveryHelper() {
			public void init(Agent a) {
				// Just do nothing
			}
			
			public void reattach() throws ServiceException {
				try {
					Node n = myContainer.getNodeDescriptor().getNode();
					String pmAddress = myContainer.getServiceManager().getLocalAddress();
					n.platformManagerDead(pmAddress, pmAddress);
				}
				catch (IMTPException imtpe) {
					throw new ServiceException("Communication error: "+imtpe.getMessage(), imtpe);
				}
			}
		};
	}
	
	private void checkNode(String name, byte[] nn, String oldAddress, String currentAddress) {
		if (myLogger.isLoggable(Logger.FINE)) {
			myLogger.log(Logger.FINE, "Recovering node "+name+" ...");
		}
		Node node = null;
		try {
			node = nodeSerializer.deserialize(nn);
			node.platformManagerDead(oldAddress, currentAddress);
			myLogger.log(Logger.INFO, "Node "+name+" successfully recovered.");
			return;
		}
		catch (IMTPException imtpe) {
			myLogger.log(Logger.INFO, "Node "+name+" unreachable. It has likely been killed in the meanwhile");
		}
		catch (Exception e) {
			myLogger.log(Logger.WARNING, "Error deserializing node "+name+". ", e);
		}
		// If we get here the node either has been killed in the meanwhile or cannot be deserialized -->
		// In any case remove it from the PS
		try {
			myPS.removeNode(node.getName());
		}
		catch (Exception ex) {
			myLogger.log(Logger.WARNING, "Cannot remove node "+node.getName()+" from persistent storage. ", ex);
		}
	}
		
	private void killNode(String name, byte[] nn) {
		Node node = null;
		try {
			node = nodeSerializer.deserialize(nn);
			HorizontalCommand cmd = new GenericCommand(H_KILLNODE, NAME, null);
			node.accept(cmd);
			myLogger.log(Logger.INFO, "Node "+name+" successfully killed.");
		}
		catch (IMTPException imtpe) {
			myLogger.log(Logger.INFO, "Node "+name+" unreachable (it has likely been killed in the meanwhile) or does not support fault recovery.");
		}
		catch (Exception e) {
			myLogger.log(Logger.WARNING, "Error deserializing node "+name+". ", e);
		}
		// In any case remove the node from the PS
		try {
			myPS.removeNode(node.getName());
		}
		catch (Exception ex) {
			myLogger.log(Logger.WARNING, "Cannot remove node "+node.getName()+" from persistent storage. ", ex);
		}
	}
	
	
	/**
	 Inner class MainCommandIncomingFilter.
	 This filter is installed on a Main Container and intercepts 
	 the NEW_NODE and DEAD_NODE V-Commands
	 */
	private class MainCommandIncomingFilter extends Filter {
		public boolean accept(VerticalCommand cmd) {
			String name = cmd.getName();
			try {
				if (name.equals(Service.NEW_NODE)) {
					handleNewNode((NodeDescriptor) cmd.getParams()[0]);
				}
				if (name.equals(Service.ADOPTED_NODE)) {
					// An adopted node is treated exactly as a new node
					handleNewNode((NodeDescriptor) cmd.getParams()[0]);
				}
				else if (name.equals(Service.DEAD_NODE)) {
					handleDeadNode((NodeDescriptor) cmd.getParams()[0]);
				}
			}
			catch (Exception e) {
				myLogger.log(Logger.WARNING, "Error processing command "+name+". ", e);
			}
			
			// Never veto a command
			return true;
		}
	} // END of inner class MainCommandIncomingFilter
	
	
	/**
	 Inner class MainCommandOutgoingFilter.
	 This filter is installed on a Main Container and intercepts the 
	 NODE_UNREACHABLE and NODE_REACHABLE VCommands of the NodeMonitoringService
	 and the ORPHAN_NODE VCommand of the UDPNodeMonitoringService. 
	 */
	private class MainCommandOutgoingFilter extends Filter {
		public boolean accept(VerticalCommand cmd) {
			String name = cmd.getName();
			try {
				if (name.equals(NodeMonitoringService.NODE_UNREACHABLE)) {
					handleNodeUnreachable((Node) cmd.getParams()[0]);
				}
				else if (name.equals(NodeMonitoringService.NODE_REACHABLE)) {
					handleNodeReachable((Node) cmd.getParams()[0]);
				}
				else if (name.equals(UDPNodeMonitoringService.ORPHAN_NODE)) {
					handleOrphanNode((String) cmd.getParams()[0]);
				}
			}
			catch (Exception e) {
				myLogger.log(Logger.WARNING, "Error processing command "+name+". ", e);
			}
			
			// Never veto a command
			return true;
		}
	} // END of inner class MainCommandOutgoingFilter
	
	
	/**
	 Inner class ContainerCommandIncomingFilter.
	 This filter is installed on a peripheral Container and intercepts 
	 the REATTACHED V-Commands
	 */
	private class ContainerCommandIncomingFilter extends Filter {
		public boolean accept(VerticalCommand cmd) {
			String name = cmd.getName();
			try {
				if (name.equals(Service.REATTACHED)) {
					handleReattached();
				}
			}
			catch (Exception e) {
				myLogger.log(Logger.WARNING, "Error processing command "+name+". ", e);
			}
			
			// Never veto a command
			return true;
		}
	} // END of inner class ContainerCommandIncomingFilter
	
	
	////////////////////////////////////////////
	// Methods called by the filters
	////////////////////////////////////////////
	
	/**
	 Add a newly born node to the persistent storage
	 */
	private void handleNewNode(NodeDescriptor dsc) throws Exception {
		Node node = dsc.getNode();
		if (!node.hasPlatformManager()) {
			byte[] nn = nodeSerializer.serialize(node);
			myPS.storeNode(node.getName(), (dsc.getParentNode() != null), nn);			
			myLogger.log(Logger.FINE, "Node "+node.getName()+" added to persistent storage.");
		}
	}
	
	/**
	 Remove a dead node from the persistent storage
	 */
	private void handleDeadNode(NodeDescriptor dsc) throws Exception {
		Node node = dsc.getNode();
		if (!node.hasPlatformManager()) {
			myPS.removeNode(node.getName());			
			myLogger.log(Logger.FINE, "Node "+node.getName()+" removed from persistent storage.");
		}
	}
	
	/**
	 Mark a node as unreachable in the persistent storage
	 */
	private void handleNodeUnreachable(Node node) throws Exception {
		if (!node.hasPlatformManager()) {
			myPS.setUnreachable(node.getName());
			myLogger.log(Logger.FINE, "Node "+node.getName()+" marked as unreachable.");
		}
	}
	
	/**
	 Remove the unreachable mark from a node in the persistent storage
	 */
	private void handleNodeReachable(Node node) throws Exception {
		if (!node.hasPlatformManager()) {
			myPS.resetUnreachable(node.getName());
			myLogger.log(Logger.FINE, "Node "+node.getName()+" marked as reachable.");
		}
	}
	
	/**
	 * Try to recover the orphan node
	 */
	private void handleOrphanNode(String nodeName) {
		if (bootComplete) {
			myLogger.log(Logger.INFO, "Handling orphan node "+nodeName+"...");
			try {
				byte[] nn = myPS.getUnreachableNode(nodeName);
				if (nn != null) {
					if (orphanNodePolicy.equals(ORPHAN_NODE_POLICY_RECOVER)) {
						myLogger.log(Logger.INFO, "Try to recover orphan node "+nodeName);
						String address = myContainer.getServiceManager().getLocalAddress();
						checkNode(nodeName, nn, address, address);
					}
					else if (orphanNodePolicy.equals(ORPHAN_NODE_POLICY_KILL)) {
						myLogger.log(Logger.INFO, "Try to kill orphan node "+nodeName);
						killNode(nodeName, nn);
					}
					else if (orphanNodePolicy.equals(ORPHAN_NODE_POLICY_IGNORE)) {
						// Just do nothing
					}
				}
				else {
					myLogger.log(Logger.INFO, "Orphan node "+nodeName+" not found in persistent storage");
				}
			}
			catch (Exception e) {
				myLogger.log(Logger.WARNING, "Error searching for unreachable node "+nodeName+" in the persistent storage", e);
			}
		}
	}
	
	/**
	 The container reattached to a recovered Main. Inform the new Main about 
	 all local agents.
	 */
	private void handleReattached() {
		myLogger.log(Logger.INFO, "Re-adding all local agents to recovered main container...");
		AID[] ids = myContainer.agentNames();
		for (int i = 0; i < ids.length; ++i) {
			AID id = ids[i];
			Agent agent = myContainer.acquireLocalAgent(id);
			if (agent != null) {
				if(myLogger.isLoggable(Logger.CONFIG)) {
					myLogger.log(Logger.CONFIG, "Re-adding agent "+id.getName());
				}
				try {
					// Note that we pass null owner principal and null initial credentials.
					// The Security service (if active) will insert the existing ones.
					// Note also that we use agent.getAID() instead of id since the latter may have addresses not up to date.
					myContainer.initAgent(agent.getAID(), agent, null, null);
				}
				catch (Exception e) {
					myLogger.log(Logger.SEVERE, "Error reattaching agent "+id.getName()+". ", e);
				}
			}
			myContainer.releaseLocalAgent(id);
		}
	}
		
	private void suicide() {
		myLogger.log(Logger.WARNING, "Activating suicide procedure.....");
		Thread t = new Thread() {
			public void run() {
				try {
					Thread.sleep(2000);
				}
				catch (Exception e) {}
				myLogger.log(Logger.WARNING, "Suiciding NOW!!!!!!");
				System.exit(0);
			}
		};
		t.start();
	}
	
	
	/**
	 * Inner class NodeSerializer
	 */
	public class NodeSerializer {
		public byte[] serialize(Node n) throws Exception {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ObjectOutputStream encoder = new ObjectOutputStream(out);
			encoder.writeObject(n);
			return out.toByteArray();
		}
		
		public Node deserialize(byte[] bb) throws Exception {
			ByteArrayInputStream inp = new ByteArrayInputStream(bb);
			ObjectInputStream decoder = new ObjectInputStream(inp);
			return (Node) decoder.readObject();
		}
	} // END of inner class NodeSerializer 
}
