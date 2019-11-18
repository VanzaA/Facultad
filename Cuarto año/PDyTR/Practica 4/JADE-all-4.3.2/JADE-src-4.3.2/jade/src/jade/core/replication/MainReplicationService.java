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

package jade.core.replication;

//#J2ME_EXCLUDE_FILE

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Vector;

import jade.core.HorizontalCommand;
import jade.core.PlatformManagerImpl;
import jade.core.VerticalCommand;
import jade.core.GenericCommand;
import jade.core.Service;
import jade.core.BaseService;
import jade.core.ServiceException;
import jade.core.Filter;
import jade.core.Node;
import jade.core.NodeDescriptor;
import jade.core.NodeEventListener;
import jade.core.NodeFailureMonitor;

import jade.core.AgentContainer;
import jade.core.MainContainerImpl;
import jade.core.Profile;
import jade.core.ProfileException;
import jade.core.IMTPException;
import jade.core.NotFoundException;
import jade.core.NameClashException;

import jade.core.AID;
import jade.core.ContainerID;

import jade.domain.AMSEventQueueFeeder;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;

import jade.mtp.MTPDescriptor;

import jade.security.Credentials;
import jade.security.JADEPrincipal;
import jade.security.JADESecurityException;

import jade.util.leap.List;
import jade.util.leap.LinkedList;
import jade.util.leap.Iterator;
import jade.util.leap.Map;
import jade.util.leap.HashMap;
import jade.util.InputQueue;
import jade.util.Logger;


/**
 A kernel-level service to manage a ring of Main Containers,
 keeping the various replicas in sync and providing failure
 detection and recovery to make JADE tolerate Main Container
 crashes.

 @author Giovanni Rimassa - FRAMeTech s.r.l.

 */
public class MainReplicationService extends BaseService {
	public static final String NAME = MainReplicationSlice.NAME;
	public static final String SNAPSHOT_ON_FAILURE = "jade_core_replication_MainReplicationService_snapshotonfailure";
	
	// Actions to be performed when the node monitoring system considers an unreachable node as dead
	public static final int REMOVE_NODE = 0;
	public static final int WAIT = 1;
	public static final int SUICIDE = 2;
	

	private static final boolean EXCLUDE_MYSELF = false;

	private static final boolean INCLUDE_MYSELF = true;

	private static final String[] OWNED_COMMANDS = new String[] {MainReplicationSlice.LEADERSHIP_ACQUIRED};

	public void init(AgentContainer ac, Profile p) throws ProfileException {
		super.init(ac, p);

		myContainer = ac;

		// Create a local slice
		localSlice = new ServiceComponent();

		// Create the command filters
		outFilter = new CommandOutgoingFilter();
		inFilter = new CommandIncomingFilter();

		snapshotOnFailure = p.getBooleanProperty(SNAPSHOT_ON_FAILURE, false);
	}

	public String getName() {
		return MainReplicationSlice.NAME;
	}

	public Class getHorizontalInterface() {
		try {
			return Class.forName(MainReplicationSlice.NAME + "Slice");
		} catch (ClassNotFoundException cnfe) {
			return null;
		}
	}

	public Service.Slice getLocalSlice() {
		return localSlice;
	}

	public Filter getCommandFilter(boolean direction) {
		if (direction == Filter.OUTGOING) {
			return outFilter;
		} else {
			return inFilter;
		}
	}

	public String[] getOwnedCommands() {
		return OWNED_COMMANDS;
	}

	public void boot(Profile p) throws ServiceException {
		try {
			// Initialize the label of this node
			Service.Slice[] slices = getAllSlices();
			myLabel = slices.length - 1;

			// Temporarily store the slices into an array...
			MainReplicationSlice[] temp = new MainReplicationSlice[slices.length];
			// Besides notifying GADT information, the MainReplication slice that will monitor this newly started
			// slice will also have to issue a NEW_NODE VCommand and a NEW_SLICE VCommands for each local service
			// to allow services to notify service specific information
			NodeDescriptor dsc = myContainer.getNodeDescriptor();
			Vector localServices = myContainer.getServiceManager().getLocalServices();
			String localNodeName = getLocalNode().getName();
			for (int i = 0; i < slices.length; i++) {
				try {
					MainReplicationSlice slice = (MainReplicationSlice) slices[i];
					String sliceName = slice.getNode().getName();
					int label = slice.getLabel();

					temp[label] = slice;

					if (!sliceName.equals(localNodeName)) {
						slice.addReplica(localNodeName, myPlatformManager.getLocalAddress(), myLabel, dsc, localServices);
					}

					if (label == myLabel - 1) {
						localSlice.attachTo(label, slice);
					}
				} catch (IMTPException imtpe) {
					// Ignore it: stale slice...
				}
			}

			// copy all the slices from the temporary array to the slice list
			for (int i = 0; i < temp.length; i++) {
				replicas.add(temp[i]);
			}

			if (myLabel > 0) {
				myLogger.log(Logger.INFO, "Main container ring re-arranged: label = "+myLabel+" monitored label = "+localSlice.monitoredLabel);
			}
		} catch (IMTPException imtpe) {
			throw new ServiceException("An error occurred during service startup.", imtpe);
		}

	}

	public void shutdown() {
		if (localSlice != null) {
			localSlice.stopMonitoring();
		}
	}

	/**
	 * This method is invoked when the monitored main node remains unreachable for too long and is therefore considered 
	 * dead. The default implementation simply returns REMOVE_NODE so that the node is removed.
	 * Subclasses may redefine this method to implement application-specific checks aimed at detecting whether 
	 * the unreachability depends on a real death or on temporary network problems and returns different actions 
	 * according to the situation.
	 * @param unreachableNode The node that is considered dead due to long unreachability
	 * @return One of REMOVE_NODE, WAIT or SUICIDE.
	 */
	protected int checkConnectivity(Node unreachableNode) {
		return REMOVE_NODE;
	}

	/**
	 * Inner class CommandOutgoingFilter
	 * Keep tool agents information in synch among replicas 
	 */
	private class CommandOutgoingFilter extends Filter {

		public boolean accept(VerticalCommand cmd) {

			try {
				String name = cmd.getName();

				if (name.equals(jade.core.management.AgentManagementSlice.ADD_TOOL)) {
					handleNewTool(cmd);
				} 
				else if (name.equals(jade.core.management.AgentManagementSlice.REMOVE_TOOL)) {
					handleDeadTool(cmd);
				} 
				//#PJAVA_EXCLUDE_BEGIN
				else if (name.equals(jade.core.nodeMonitoring.UDPNodeMonitoringService.ORPHAN_NODE)) {
					handleOrphanNode(cmd);
				}
				//#PJAVA_EXCLUDE_END
			} catch (IMTPException imtpe) {
				cmd.setReturnValue(imtpe);
			} catch (ServiceException se) {
				cmd.setReturnValue(se);
			}

			// Never veto a command
			return true;
		}

		private void handleNewTool(VerticalCommand cmd) throws IMTPException, ServiceException {
			Object[] params = cmd.getParams();
			AID tool = (AID) params[0];

			GenericCommand hCmd = new GenericCommand(MainReplicationSlice.H_NEWTOOL, MainReplicationSlice.NAME, null);
			hCmd.addParam(tool);

			broadcastToReplicas(hCmd, EXCLUDE_MYSELF);
		}

		private void handleDeadTool(VerticalCommand cmd) throws IMTPException, ServiceException {
			Object[] params = cmd.getParams();
			AID tool = (AID) params[0];

			GenericCommand hCmd = new GenericCommand(MainReplicationSlice.H_DEADTOOL, MainReplicationSlice.NAME, null);
			hCmd.addParam(tool);

			broadcastToReplicas(hCmd, EXCLUDE_MYSELF);
		}
		
		private void handleOrphanNode(VerticalCommand cmd) throws IMTPException, ServiceException {
			String nodeId = (String) cmd.getParam(0);
			localSlice.handleOrphanNode(nodeId);
		}
	} // End of CommandOutgoingFilter class


	/**
	 * Inner class CommandIncomingFilter
	 * Keep agents and MTPs information in synch among replicas 
	 */
	private class CommandIncomingFilter extends Filter {

		public void postProcess(VerticalCommand cmd) {
			try {
				String name = cmd.getName();

				if (name.equals(jade.core.management.AgentManagementSlice.INFORM_CREATED)) {
					handleInformCreated(cmd);
				} else if (name.equals(jade.core.management.AgentManagementSlice.INFORM_KILLED)) {
					handleInformKilled(cmd);
				} else if (name.equals(jade.core.management.AgentManagementSlice.INFORM_STATE_CHANGED)) {
					handleInformStateChanged(cmd);
				} else if (name.equals(jade.core.messaging.MessagingSlice.NEW_MTP)) {
					handleNewMTP(cmd);
				} else if (name.equals(jade.core.messaging.MessagingSlice.DEAD_MTP)) {
					handleDeadMTP(cmd);
				} 
			} 
			catch (Throwable t) {
				cmd.setReturnValue(t);
			}
		}

		private void handleInformCreated(VerticalCommand cmd) throws IMTPException, NotFoundException, NameClashException, JADESecurityException, ServiceException {
			Object ret = cmd.getReturnValue();
			// Avoid propagating to other slices in case the agent creation failed due to a name-clash
			if (!(ret != null && ret instanceof NameClashException)) {
				Object[] params = cmd.getParams();

				AID agentID = (AID) params[0];
				ContainerID cid = (ContainerID) params[1];

				GenericCommand hCmd = new GenericCommand(MainReplicationSlice.H_BORNAGENT, MainReplicationSlice.NAME, null);
				hCmd.addParam(agentID);
				hCmd.addParam(cid);
				hCmd.setPrincipal(cmd.getPrincipal());
				hCmd.setCredentials(cmd.getCredentials());

				broadcastToReplicas(hCmd, EXCLUDE_MYSELF);
			}
		}

		private void handleInformKilled(VerticalCommand cmd) throws IMTPException, NotFoundException, ServiceException {
			Object[] params = cmd.getParams();
			AID agentID = (AID) params[0];

			GenericCommand hCmd = new GenericCommand(MainReplicationSlice.H_DEADAGENT, MainReplicationSlice.NAME, null);
			hCmd.addParam(agentID);

			broadcastToReplicas(hCmd, EXCLUDE_MYSELF);
		}

		private void handleInformStateChanged(VerticalCommand cmd) throws IMTPException, NotFoundException, ServiceException {
			Object[] params = cmd.getParams();
			AID agentID = (AID) params[0];
			String newState = (String) params[1];

			if (newState.equals(jade.domain.FIPAAgentManagement.AMSAgentDescription.SUSPENDED)) {
				GenericCommand hCmd = new GenericCommand(MainReplicationSlice.H_SUSPENDEDAGENT, MainReplicationSlice.NAME, null);
				hCmd.addParam(agentID);

				broadcastToReplicas(hCmd, EXCLUDE_MYSELF);
			} else if (newState.equals(jade.domain.FIPAAgentManagement.AMSAgentDescription.ACTIVE)) {
				GenericCommand hCmd = new GenericCommand(MainReplicationSlice.H_RESUMEDAGENT, MainReplicationSlice.NAME, null);
				hCmd.addParam(agentID);

				broadcastToReplicas(hCmd, EXCLUDE_MYSELF);
			}

		}

		private void handleNewMTP(VerticalCommand cmd) throws IMTPException, ServiceException {
			Object[] params = cmd.getParams();
			MTPDescriptor mtp = (MTPDescriptor) params[0];
			ContainerID cid = (ContainerID) params[1];

			GenericCommand hCmd = new GenericCommand(MainReplicationSlice.H_NEWMTP, MainReplicationSlice.NAME, null);
			hCmd.addParam(mtp);
			hCmd.addParam(cid);

			broadcastToReplicas(hCmd, EXCLUDE_MYSELF);
		}

		private void handleDeadMTP(VerticalCommand cmd) throws IMTPException, ServiceException {
			Object[] params = cmd.getParams();
			MTPDescriptor mtp = (MTPDescriptor) params[0];
			ContainerID cid = (ContainerID) params[1];

			GenericCommand hCmd = new GenericCommand(MainReplicationSlice.H_DEADMTP, MainReplicationSlice.NAME, null);
			hCmd.addParam(mtp);
			hCmd.addParam(cid);

			broadcastToReplicas(hCmd, EXCLUDE_MYSELF);
		}
	} // End of CommandIncomingFilter class


	/**
	 * Inner class ServiceComponent
	 */
	private class ServiceComponent implements Service.Slice, NodeEventListener {

		public ServiceComponent() {
			myMain = (MainContainerImpl) myContainer.getMain();
			myPlatformManager = (PlatformManagerImpl) myMain.getPlatformManager();
		}

		public void stopMonitoring() {
			if (nodeMonitor != null) {
				if (myLogger.isLoggable(Logger.CONFIG))
					myLogger.log(Logger.CONFIG, "Stop monitoring node <" + nodeMonitor.getNode().getName() + ">");
				nodeMonitor.stop();
			}
		}

		private void attachTo(int label, MainReplicationSlice slice) throws IMTPException, ServiceException {
			// Stop the previous monitor, if any
			stopMonitoring();

			// Store the label of the monitored slice
			monitoredLabel = label;

			// Avoid monitoring yourself
			if (monitoredLabel == myLabel) {
				return;
			}

			// Store the Service Manager address for the monitored slice
			monitoredSvcMgr = slice.getPlatformManagerAddress();

			// Set up a failure monitor on the target slice...
			nodeMonitor = NodeFailureMonitor.getFailureMonitor();
			nodeMonitor.start(slice.getNode(), this);
		}

		// Implementation of the Service.Slice interface

		public Service getService() {
			return MainReplicationService.this;
		}

		public Node getNode() throws ServiceException {
			try {
				return MainReplicationService.this.getLocalNode();
			} catch (IMTPException imtpe) {
				throw new ServiceException("Problem in contacting the IMTP Manager", imtpe);
			}
		}

		public VerticalCommand serve(HorizontalCommand cmd) {
			try {
				String cmdName = cmd.getName();
				Object[] params = cmd.getParams();

				if (cmdName.equals(MainReplicationSlice.H_GETLABEL)) {
					Integer i = new Integer(getLabel());
					cmd.setReturnValue(i);
				} else if (cmdName.equals(MainReplicationSlice.H_INVOKESERVICEMETHOD)) { 
					String serviceName = (String) params[0];
					String methodName = (String) params[1];
					Object[] methodParams = (Object[]) params[2];
					invokeServiceMethod(serviceName, methodName, methodParams);
				} else if (cmdName.equals(MainReplicationSlice.H_GETPLATFORMMANAGERADDRESS)) {
					cmd.setReturnValue(getPlatformManagerAddress());
				} else if (cmdName.equals(MainReplicationSlice.H_ADDREPLICA)) {
					String sliceName = (String) params[0];
					String smAddr = (String) params[1];
					int sliceIndex = ((Integer) params[2]).intValue();
					NodeDescriptor dsc = (NodeDescriptor) params[3];
					Vector services = (Vector) params[4]; 
					addReplica(sliceName, smAddr, sliceIndex, dsc, services);
				} else if (cmdName.equals(MainReplicationSlice.H_REMOVEREPLICA)) {
					String smAddr = (String) params[0];
					int sliceIndex = ((Integer) params[1]).intValue();
					removeReplica(smAddr, sliceIndex);
				} else if (cmdName.equals(MainReplicationSlice.H_FILLGADT)) {
					AID[] agents = (AID[]) params[0];
					ContainerID[] containers = (ContainerID[]) params[1];
					fillGADT(agents, containers);
				} else if (cmdName.equals(MainReplicationSlice.H_BORNAGENT)) {
					AID name = (AID) params[0];
					ContainerID cid = (ContainerID) params[1];
					bornAgent(name, cid, cmd.getPrincipal(), cmd.getCredentials());
				} else if (cmdName.equals(MainReplicationSlice.H_DEADAGENT)) {
					AID name = (AID) params[0];
					deadAgent(name);
				} else if (cmdName.equals(MainReplicationSlice.H_SUSPENDEDAGENT)) {
					AID name = (AID) params[0];
					suspendedAgent(name);
				} else if (cmdName.equals(MainReplicationSlice.H_RESUMEDAGENT)) {
					AID name = (AID) params[0];
					resumedAgent(name);
				} else if (cmdName.equals(MainReplicationSlice.H_NEWMTP)) {
					MTPDescriptor mtp = (MTPDescriptor) params[0];
					ContainerID cid = (ContainerID) params[1];
					newMTP(mtp, cid);
				} else if (cmdName.equals(MainReplicationSlice.H_DEADMTP)) {
					MTPDescriptor mtp = (MTPDescriptor) params[0];
					ContainerID cid = (ContainerID) params[1];
					deadMTP(mtp, cid);
				} else if (cmdName.equals(MainReplicationSlice.H_NEWTOOL)) {
					AID tool = (AID) params[0];
					newTool(tool);
				} else if (cmdName.equals(MainReplicationSlice.H_DEADTOOL)) {
					AID tool = (AID) params[0];
					deadTool(tool);
				}
			} catch (Throwable t) {
				cmd.setReturnValue(t);
			}
			return null;
		}

		private int getLabel() throws IMTPException {
			return myLabel;
		}

		private void invokeServiceMethod(String serviceName, String methodName, Object[] args) throws Throwable {
			Service svc = myContainer.getServiceFinder().findService(serviceName);
			Method m = getMethod(svc, methodName);
			try {
				myLogger.log(Logger.INFO, "Invoking replicated method "+methodName+" on service "+serviceName);
				m.invoke(svc, args);
			}
			catch (InvocationTargetException ite) {
				throw ite.getCause();
			}
		}
		
		private Method getMethod(Service svc, String methodName) throws Exception {
			String key = svc.getName()+'#'+methodName;
			Method m = (Method) cachedServiceMethods.get(key);
			if (m == null) {
				Method[] mm = svc.getClass().getMethods();
				for (int i = 0; i < mm.length; ++i) {
					if (mm[i].getName().equals(methodName)) {
						m = mm[i];
						cachedServiceMethods.put(key, m);
						break;
					}
				}
			}
			if (m == null) {
				throw new NoSuchMethodException("Method "+methodName+" not found is service "+svc.getName());
			}
			return m;
		}
		
		private String getPlatformManagerAddress() throws IMTPException {
			return myPlatformManager.getLocalAddress();
		}

		private void addReplica(String sliceName, String smAddr, int sliceIndex, NodeDescriptor dsc, Vector services) throws IMTPException, ServiceException {
			//Get a fresh slice: in this way the address is always right (and old address is overridden!!!)
			MainReplicationSlice slice = (MainReplicationSlice) getFreshSlice(sliceName);
			replicas.add(sliceIndex, slice);
			// If first in line, close the ring by monitoring the newly arrived slice,
			// and start sending data to the new slice...
			if (myLabel == 0) {
				attachTo(sliceIndex, slice);

				// Send all the data about the GADT...
				AID[] names = myMain.agentNames();
				ContainerID[] containers = new ContainerID[names.length];
				for (int i = 0; i < names.length; i++) {
					try {
						containers[i] = myMain.getContainerID(names[i]);
					} catch (NotFoundException nfe) {
						// It should never happen...
						nfe.printStackTrace();
					}
				}

				// FIXME: What about principal and ownership?
				slice.fillGADT(names, containers);

				// Update the status of each suspended agent...
				AMSAgentDescription amsd = new AMSAgentDescription();
				amsd.setState(AMSAgentDescription.SUSPENDED);
				List suspendedAgents = myMain.amsSearch(amsd, -1); // '-1' means 'all the results'

				Iterator it = suspendedAgents.iterator();
				while (it.hasNext()) {
					AMSAgentDescription desc = (AMSAgentDescription) it.next();
					try {
						slice.suspendedAgent(desc.getName());
					} catch (NotFoundException nfe) {
						// It should never happen...
						nfe.printStackTrace();
					}
				}

				// Send the tool list...
				AID[] tools = myMain.agentTools();
				for (int i = 0; i < tools.length; i++) {
					slice.newTool(tools[i]);
				}

				// Finally issue a NEW_NODE VCommand and a NEW_SLICE VCommand for each service to allow
				// local services to propagate service specific information to their slices in the new 
				// Main Container node.
				try {
					myPlatformManager.addMainContainerNode(dsc, services);
				}
				catch (JADESecurityException jse) {
					// Should we do something more?
					myLogger.log(Logger.WARNING, "Unauthorized Main Container node "+dsc.getNode().getName(), jse);
				}

			}
			myLogger.log(Logger.INFO, "Main container ring re-arranged: label = "+myLabel+" monitored label = "+monitoredLabel);
		}

		private void removeReplica(String smAddr, int index) throws IMTPException {
			replicas.remove(index);
			adjustLabels(index);
		}

		private void adjustLabels(int index) {
			if (index < myLabel) {
				myLabel--;
				monitoredLabel--;
				if (monitoredLabel == -1) {
					monitoredLabel += replicas.size();
				}
			} else if (myLabel == 0) {
				// Handle the ring wrap-around case...
				monitoredLabel--;
			}
			myLogger.log(Logger.INFO, "Main container ring re-arranged: label = "+myLabel+" monitored label = "+monitoredLabel);
		}

		private void fillGADT(AID[] agents, ContainerID[] containers) throws JADESecurityException {
			for (int i = 0; i < agents.length; i++) {

				try {
					// FIXME: What about principal and ownership?
					myMain.bornAgent(agents[i], containers[i], null, null, true);
					//log("Agent "+agents[i].getName()+" inserted into GADT", 2);
					if (myLogger.isLoggable(Logger.CONFIG))
						myLogger.log(Logger.CONFIG, "Agent " + agents[i].getName() + " inserted into GADT");

				} catch (NotFoundException nfe) {
					// It should never happen...
					nfe.printStackTrace();
				} catch (NameClashException nce) {
					// It should never happen...
					nce.printStackTrace();
				}
			}

		}

		private void bornAgent(AID name, ContainerID cid, JADEPrincipal principal, Credentials credentials) throws NameClashException, NotFoundException {
			// Retrieve the ownership from the credentials
			String ownership = "NONE";
			if (credentials != null) {
				JADEPrincipal ownerPr = credentials.getOwner();
				if (ownerPr != null) {
					ownership = ownerPr.getName();
				}
			}
			try {
				// If the name is already in the GADT, throws NameClashException
				myMain.bornAgent(name, cid, principal, ownership, false);
				//log("Agent "+name.getName()+" inserted into GADT", 2);
				if (myLogger.isLoggable(Logger.CONFIG))
					myLogger.log(Logger.CONFIG, "Agent " + name.getName() + " inserted into GADT");

			} catch (NameClashException nce) {
				try {
					ContainerID oldCid = myMain.getContainerID(name);
					Node n = myMain.getContainerNode(oldCid).getNode();

					// Perform a non-blocking ping to check...
					n.ping(false);

					// Ping succeeded: rethrow the NameClashException
					throw nce;
				} catch (NameClashException nce2) {
					throw nce2; // Let this one through...
				} catch (Exception e) {
					// Ping failed: forcibly replace the dead agent...
					myMain.bornAgent(name, cid, principal, ownership, true);
					//log("Agent "+name.getName()+" inserted into GADT", 2);
					if (myLogger.isLoggable(Logger.CONFIG))
						myLogger.log(Logger.CONFIG, "Agent " + name.getName() + " inserted into GADT");

				}
			}
		}

		private void deadAgent(AID name) throws NotFoundException {
			myMain.deadAgent(name, false);
		}

		private void suspendedAgent(AID name) throws NotFoundException {
			myMain.suspendedAgent(name);
		}

		private void resumedAgent(AID name) throws NotFoundException {
			myMain.resumedAgent(name);
		}

		private void newMTP(MTPDescriptor mtp, ContainerID cid) throws IMTPException {
			myMain.newMTP(mtp, cid);
		}

		private void deadMTP(MTPDescriptor mtp, ContainerID cid) throws IMTPException {
			myMain.deadMTP(mtp, cid);
		}

		private void newTool(AID tool) throws IMTPException {
			myMain.toolAdded(tool);
		}

		private void deadTool(AID tool) throws IMTPException {
			myMain.toolRemoved(tool);
		}

		public void dumpReplicas() {
			try {
				System.out.println("--- " + getLocalNode().getName() + "[" + myLabel + "] ---");
				System.out.println("--- Monitoring node [" + monitoredLabel + "] ---");
				System.out.println("--- Replica list ---");
				Object[] slices = replicas.toArray();
				for (int i = 0; i < slices.length; i++) {
					MainReplicationSlice slice = (MainReplicationSlice) slices[i];
					System.out.println("----- " + slice.getNode().getName() + "[" + i + "] -----");
				}
				System.out.println("--- End ---");
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}

		private void dumpGADT() {
			AID[] agents = myMain.agentNames();
			System.out.println("--- Agent List ---");
			for (int i = 0; i < agents.length; i++) {
				System.out.println("    Agent: " + agents[i].getLocalName());
			}
			System.out.println("------------------");
		}

		// Implementation of the NodeEventListener interface

		public void nodeAdded(Node n) {
			myLogger.log(Logger.INFO, "Start monitoring main node <" + n.getName() + ">");
			monitoredNodeUnreachable = false;
		}

		public void nodeRemoved(Node n) {
			int action = REMOVE_NODE;
			if (monitoredNodeUnreachable) {
				// The monitored Main Container appears to be dead. Clearly we cannot distinguish for sure between a
				// real death and a long network disconnection --> By redefining the checkConnectivity() method
				// Applications have an opportunity to perform 
				// environment specific checks and to force one of the following actions: 
				// - REMOVE_NODE: the default behaviour. Node n is actually removed and if necessary the leadership is acquired
				// - WAIT: nothing is done. As soon as an ORPHAN_NODE command is intercepted for node n a new monitor is activated
				// This action requires the UDPNodeMonitoringService to be active.
				// - SUICIDE: kill the local container to avoid creating problems when connectivity is re-established
				action = checkConnectivity(n);
			}

			switch (action) {
			case REMOVE_NODE:
				removeTerminatedNode(n);
				break;
			case WAIT:
				myLogger.log(Logger.WARNING, "Network problems are preventing the monitoring of main node <" + n.getName() + ">. Stop monitor it");
				toBeMonitored = n;
				break;
			case SUICIDE:
				suicide();
				break;
			}
		}

		public void nodeUnreachable(Node n) {
			myLogger.log(Logger.WARNING, "Main node <" + n.getName() + "> UNREACHABLE");
			monitoredNodeUnreachable = true;
		}

		public void nodeReachable(Node n) {
			myLogger.log(Logger.INFO, "Main Node <" + n.getName() + "> REACHABLE");
			monitoredNodeUnreachable = false;
		}

		private void removeTerminatedNode(Node n) {
			myLogger.log(Logger.INFO, "Main node <" + n.getName() + "> TERMINATED");
			
			try {
				replicas.remove(monitoredLabel);

				// Possibly the AMS is dead --> Start intercepting platform and MTP events on behalf of the 
				// new AMS if any. 
				AMSEventQueueFeeder feeder = null;
				if (!snapshotOnFailure) {
					feeder = new AMSEventQueueFeeder(new InputQueue(), myContainer.getID());
					myMain.addListener(feeder);
				}

				myPlatformManager.removeReplica(monitoredSvcMgr, false);
				myPlatformManager.removeNode(new NodeDescriptor(n), false);

				// Broadcast a 'removeReplica()' method (exclude yourself from bcast)
				GenericCommand hCmd = new GenericCommand(MainReplicationSlice.H_REMOVEREPLICA, MainReplicationSlice.NAME, null);
				hCmd.addParam(monitoredSvcMgr);
				hCmd.addParam(new Integer(monitoredLabel));
				broadcastToReplicas(hCmd, EXCLUDE_MYSELF);

				int oldLabel = myLabel;

				adjustLabels(monitoredLabel);

				// -- Attach to the new neighbour slice...
				MainReplicationSlice newSlice = (MainReplicationSlice) replicas.get(monitoredLabel);
				attachTo(monitoredLabel, newSlice);

				// Become the new leader if it is the case...
				if ((oldLabel != 0) && (myLabel == 0)) {
					myLogger.log(Logger.INFO, "-- I'm the new leader ---");
					myContainer.becomeLeader(feeder);
					VerticalCommand cmd = new GenericCommand(MainReplicationSlice.LEADERSHIP_ACQUIRED, NAME, null);
					submit(cmd);
				}
				else {
					if (feeder != null) {
						// NO new AMS --> No need for intercepting events anymore
						myMain.removeListener(feeder);
					}
				}

			} catch (IMTPException imtpe) {
				imtpe.printStackTrace();
			} catch (ServiceException se) {
				se.printStackTrace();
			}
		}
		
		private void suicide() {
			myLogger.log(Logger.WARNING, "Due to network problems I'm isolated --> The rest of the platform will consider me dead. Suicide now!!!!!!!!");
			System.exit(0);
		}
		
		private void handleOrphanNode(String nodeId) {
			if (toBeMonitored != null && toBeMonitored.getName().equals(nodeId)) {
				// The node to be monitored is reachable again --> Start monitoring it again
				myLogger.log(Logger.INFO, "Ping received from node "+nodeId+" --> The network is working again. Re-start monitoring node");
				nodeMonitor = NodeFailureMonitor.getFailureMonitor();
				nodeMonitor.start(toBeMonitored, this);
				toBeMonitored = null;
			}
		}
		
		// The active object monitoring the remote node
		NodeFailureMonitor nodeMonitor;

		// The integer label of the monitored slice
		int monitoredLabel;

		String monitoredSvcMgr;
		private Node toBeMonitored;
		private boolean monitoredNodeUnreachable = false;

	} // End of ServiceComponent class

	private AgentContainer myContainer;

	private ServiceComponent localSlice;

	private Filter outFilter;
	private Filter inFilter;

	private int myLabel = -1;
	private final List replicas = new LinkedList();
	private boolean snapshotOnFailure = false;

	// Owned copies of Main Container and Service Manager
	private MainContainerImpl myMain;
	private PlatformManagerImpl myPlatformManager;
	
	private Map cachedServiceMethods = new HashMap();

	void broadcastToReplicas(HorizontalCommand cmd, boolean includeSelf) throws IMTPException, ServiceException {
		Object[] slices = replicas.toArray();

		String localNodeName = getLocalNode().getName();
		for (int i = 0; i < slices.length; i++) {
			MainReplicationSlice slice = (MainReplicationSlice) slices[i];

			String sliceName = slice.getNode().getName();
			if (includeSelf || !sliceName.equals(localNodeName)) {
				slice.serve(cmd);
				Object ret = cmd.getReturnValue();
				if (ret instanceof Throwable) {
					// FIXME: This may happen due to the fact that the replica is terminating. E.g. a tool running on 
					// the terminating replica that deregisters from the AMS: the DeadTool event may be processed
					// when the replica is already dead. In these cases we should find a way to hide the exception
					myLogger.log(Logger.SEVERE, "Error propagating H-command " + cmd.getName() + " to slice " + sliceName, (Throwable) ret);
				}
			}
		}
	}
	
	public String dump(String key) {
		StringBuffer sb = new StringBuffer();
		sb.append("- Replicas:\n");
		try {
			Object[] slices = replicas.toArray();
			String localNodeName = getLocalNode().getName();
			for (int i = 0; i < slices.length; i++) {
				MainReplicationSlice slice = (MainReplicationSlice) slices[i];
				String sliceName = slice.getNode().getName();
				if (!sliceName.equals(localNodeName)) {
					sb.append("  - "+sliceName+"\n");
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			sb.append(e.toString());
		}
		sb.append("- Label = "+myLabel+"\n");
		sb.append("- Monitored Label = ").append(localSlice.monitoredLabel).append("\n");
		sb.append("- Monitored PlatformManager replica = ").append(localSlice.monitoredSvcMgr).append("\n");
		String monitoredNodeStr = "UNKNOWN(Monitor null)";
		if (localSlice.nodeMonitor != null) {
			Node n = localSlice.nodeMonitor.getNode();
			monitoredNodeStr = (n != null ? n.getName() : "null");
		}
		sb.append("- Monitored Node = ").append(monitoredNodeStr).append("\n");
		sb.append(super.dump(key));
		return (sb.toString());
	}
}
