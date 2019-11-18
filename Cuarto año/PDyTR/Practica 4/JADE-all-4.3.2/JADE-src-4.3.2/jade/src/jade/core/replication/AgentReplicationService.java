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
//#APIDOC_EXCLUDE_FILE

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import jade.core.AID;
import jade.core.Agent;
import jade.core.AgentContainer;
import jade.core.BaseService;
import jade.core.ContainerID;
import jade.core.Filter;
import jade.core.GenericCommand;
import jade.core.HorizontalCommand;
import jade.core.IMTPException;
import jade.core.Location;
import jade.core.MainContainer;
import jade.core.Node;
import jade.core.NotFoundException;
import jade.core.Profile;
import jade.core.ProfileException;
import jade.core.Service;
import jade.core.ServiceException;
import jade.core.ServiceHelper;
import jade.core.VerticalCommand;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.management.AgentManagementSlice;
import jade.core.messaging.GenericMessage;
import jade.core.messaging.MessagingService;
import jade.core.messaging.MessagingSlice;
import jade.core.mobility.AgentMobilityHelper;
import jade.core.mobility.AgentMobilityService;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.util.Logger;

public class AgentReplicationService extends BaseService {
	public static final String NAME = AgentReplicationHelper.SERVICE_NAME;

	private AgentContainer myContainer;
	private MessagingService theMessagingService;

	private Filter outFilter;
	private Filter incFilter;
	private ServiceComponent localSlice;

	// Map a virtual agent to the set of global information associated to it 
	private Map<AID, GlobalReplicationInfo> globalReplications = new Hashtable<AID, GlobalReplicationInfo>();
	// Map a replica agent to the related virtual agent
	private Map<AID, AID> replicaToVirtualMap = new Hashtable<AID, AID>();
	// Map a master replica agent to the pending replica creation requests
	private Map<AID, List<ReplicaInfo>> pendingReplicaCreationRequests = new Hashtable<AID, List<ReplicaInfo>>();

	private Map<String, Method> cachedAgentMethods = new HashMap<String, Method>();


	public String getName() {
		return NAME;
	}

	@Override
	public void init(AgentContainer ac, Profile p) throws ProfileException {
		super.init(ac, p);
		myContainer = ac;

		outFilter = new CommandOutgoingFilter();
		incFilter = new CommandIncomingFilter();

		localSlice = new ServiceComponent();
	}

	@Override
	public void boot(Profile p) throws ServiceException {
		super.boot(p);
		try {
			// Initialize the MessagingService. No need to check if it is != null since it is mandatory
			theMessagingService = (MessagingService) myContainer.getServiceFinder().findService(MessagingService.NAME);
		}
		catch (IMTPException imtpe) {
			// Should never happen since this is a local call
			throw new ServiceException("Cannot retrieve the local MessagingService.", imtpe);
		}
		try {
			// Check that the MobilityService is installed
			if (myContainer.getServiceFinder().findService(AgentMobilityService.NAME) == null) {
				throw new ServiceException("AgentMobilityService not installed. AgentReplicationService cannot work properly");
			}
		}
		catch (IMTPException imtpe) {
			// Should never happen since this is a local call
			throw new ServiceException("Cannot retrieve the local MessagingService.", imtpe);
		}
	}

	/**
	 * Return the AgentReplicationHelper for a given agent
	 */
	@Override
	public ServiceHelper getHelper(Agent a) throws ServiceException {
		// The agent is passed to the helper in the init() method
		return new AgentReplicationHelperImpl();
	}

	@Override
	public Filter getCommandFilter(boolean direction) {
		if(direction == Filter.OUTGOING) {
			return outFilter;
		}
		else {
			return incFilter;
		}
	}

	@Override
	public Class getHorizontalInterface() {
		try {
			return Class.forName(NAME + "Slice");
		} catch (ClassNotFoundException cnfe) {
			return null;
		}
	}

	@Override
	public Service.Slice getLocalSlice() {
		return localSlice;
	}


	/**
	 * Inner class AgentReplicationHelperImpl
	 */
	private class AgentReplicationHelperImpl implements AgentReplicationHelper {
		private AID myAid;
		private AID virtualAid;
		private List<ReplicaInfo> peerReplicas = new ArrayList<ReplicaInfo>();
		private ReplicaInfo[] peerReplicasArray = new ReplicaInfo[0];

		public void init(Agent a) {
			myAid = a.getAID();

			// If the agent retrieving this helper is already a replica, initialize virtualAid 
			// and peerReplicas from the global replication information
			virtualAid = replicaToVirtualMap.get(myAid);
			if (virtualAid != null) {
				GlobalReplicationInfo info = globalReplications.get(virtualAid);
				if (info != null) {
					AID[] currentReplicas = info.getAllReplicas();
					for (AID replica : currentReplicas) {
						// Exclude the agent itself from peer replicas
						if (!replica.equals(myAid)) {
							try {
								Location location = getLocation(replica);
								addPeerReplica(new ReplicaInfo(replica, location));
							}
							catch (NotFoundException nfe) {
								myLogger.log(Logger.WARNING, "Replica "+replica.getLocalName()+" not found. Likely it died in the meanwhile");
							}
							catch (Exception e) {
								myLogger.log(Logger.SEVERE, "Error retrieving location for agent "+replica.getLocalName(), e);
							}
						}
					}
				}
				else {
					myLogger.log(Logger.SEVERE, "Virtual agent "+virtualAid.getLocalName()+" for replica agent "+myAid.getLocalName()+" not found");
				}
			}
		}

		public AID makeVirtual(String virtualName, int replicationMode) throws ServiceException {
			if (virtualAid == null) {
				virtualAid = new AID(virtualName, AID.ISLOCALNAME);

				// Reserve the virtualName by registering the virtual AID to the AMS
				AMSAgentDescription amsd = new AMSAgentDescription();
				amsd.setName(virtualAid);
				amsd.setState(AMSAgentDescription.ACTIVE);	
				Agent agent = myContainer.acquireLocalAgent(myAid);
				// Immediately release the agent: AMSService.register() requires that the agent receives the AMS reply
				myContainer.releaseLocalAgent(myAid);
				if (agent != null) {
					try {
						AMSService.register(agent, amsd);
					}
					catch (Exception e) {
						throw new ServiceException("Error registering virtual name "+virtualName, e);
					}
				}

				broadcastNewVirtualAgent(virtualAid, myAid, replicationMode);
				return virtualAid;
			}
			else {
				throw new ServiceException("Agent "+myAid.getLocalName()+" has already been made virtual");
			}
		}

		public void createReplica(String replicaName, Location where) throws ServiceException {
			if (virtualAid != null) {
				if (isMaster()) {
					// The agent this helper belongs to is virtualized and is the master replica 
					// Go on with the replica creation process
					AgentReplicationSlice slice = (AgentReplicationSlice) getSlice(where.getName());
					if (slice != null) {
						// Notify the destination slice that a replica for our virtual agent is 
						// going to be created there (see comment in AgentReplicationSlice)
						AID replicaAid = new AID(replicaName, AID.ISLOCALNAME);
						try {
							slice.replicaCreationRequested(virtualAid, replicaAid);
						}
						catch (IMTPException imtpe) {
							// Get a fresh slice and retry
							slice = (AgentReplicationSlice) getFreshSlice(where.getName());
							try {
								slice.replicaCreationRequested(virtualAid, replicaAid);
							}
							catch (IMTPException imtpe1) {
								throw new ServiceException("IMTP error contacting destination slice", imtpe1);
							}
						}

						// Enqueue the replica creation request
						List<ReplicaInfo> rr = pendingReplicaCreationRequests.get(myAid);
						if (rr == null) {
							rr = new ArrayList<ReplicaInfo>();
							pendingReplicaCreationRequests.put(myAid, rr);
						}
						rr.add(new ReplicaInfo(replicaAid, where));

						// If there are no other ongoing replica creation processes --> directly clone the (master) agent. 
						if (rr.size() == 1) {
							cloneReplica(myAid, replicaName, where);
						}
					}
					else {
						throw new ServiceException("AgentReplicationService not installed in the destination container "+where.getName()+" for replica "+replicaName);
					}
				}
				else {
					throw new ServiceException("Agent "+myAid.getLocalName()+" is not the master replica");
				}
			}
			else {
				throw new ServiceException("Agent "+myAid.getLocalName()+" has not been made virtual");
			}
		}

		public AID getVirtualAid() {
			return virtualAid;
		}

		public AID getMasterAid() {
			if (virtualAid != null) {
				GlobalReplicationInfo info = globalReplications.get(virtualAid);
				return info.getMaster();
			}
			return null;
		}

		public boolean isMaster() {
			if (virtualAid != null) {
				GlobalReplicationInfo info = globalReplications.get(virtualAid);
				return myAid.equals(info.getMaster());
			}
			return false;
		}
		
		public Map<AID, Location> getReplicas() {
			// FIXME: To be implemented
			return null;
		}

		public void invokeReplicatedMethod(String methodName, Object[] arguments) {
			ReplicaInfo[] tmp = peerReplicasArray;
			myLogger.log(Logger.FINE, "Invoking method "+methodName+" on "+tmp.length+" replica(s)");
			for (ReplicaInfo r : tmp) {
				try {
					if (!invokeOnReplica(methodName, arguments, r)) {
						// This replica agent does not exist anymore --> remove it
						removePeerReplica(r);
						GlobalReplicationInfo info = globalReplications.get(virtualAid);
						if (info != null) {
							info.removeReplica(r.replicaAid);
						}
					}
				}
				catch (Exception e) {
					myLogger.log(Logger.SEVERE, "Error propagating call to method " + methodName + " to agent " + r.replicaAid.getLocalName(), e);
				}
			}
		}

		private boolean invokeOnReplica(String methodName, Object[] arguments, ReplicaInfo r) throws Exception {
			myLogger.log(Logger.FINER, "Invoking method "+methodName+" on replica "+r.replicaAid.getLocalName());
			// If we get an Exception, refresh the location of the replica (it 
			// may have moved or be recreated somewhere else) and retry until OK.
			// If not found in Main Container --> Ignore: replica has terminated in the meanwhile
			do {
				AgentReplicationSlice slice = (AgentReplicationSlice) getSlice(r.where.getName());
				if (slice != null) {
					try {
						try {
							slice.invokeAgentMethod(r.replicaAid, methodName, arguments);
							// Done: Jump out
							break;
						}
						catch (IMTPException imtpe) {
							// Try to get a newer slice and repeat...
							slice = (AgentReplicationSlice) getFreshSlice(r.where.getName());
							slice.invokeAgentMethod(r.replicaAid, methodName, arguments);
							// Done: Jump out
							break;
						}
					}
					catch (NotFoundException nfe) {
						// The replica agent was not found on the container where it was supposed to be
						// Possibly it has moved elsewhere --> Check with the Main Container
					}
				}
				
				// Not done: Update the replica location and retry
				try {
					myLogger.log(Logger.CONFIG, "Updating location of replica "+r.replicaAid.getLocalName());
					Location l = getLocation(r.replicaAid);
					// The replica agent is alive: update its location and retry
					r.where = l;
				}
				catch (NotFoundException nfe1) {
					// The replica agent does not exist anymore in the whole platform --> silently remove it
					return false;
				}
			} while (true);

			return true;
		}

		private synchronized void addPeerReplica(ReplicaInfo r) {
			if (!peerReplicas.contains(r)) {
				myLogger.log(Logger.CONFIG, "Adding replica "+r.replicaAid.getLocalName()+" to Helper of agent "+myAid.getLocalName());
				peerReplicas.add(r);
				peerReplicasArray = peerReplicas.toArray(new ReplicaInfo[0]);
			}
		}

		private synchronized void removePeerReplica(ReplicaInfo r) {
			if (peerReplicas.remove(r)) {
				myLogger.log(Logger.CONFIG, "Removing replica "+r.replicaAid.getLocalName()+" from Helper of agent "+myAid.getLocalName());
				peerReplicasArray = peerReplicas.toArray(new ReplicaInfo[0]);
			}
		}
	}  // END of inner class AgentReplicationHelperImpl


	/**
	 * Inner class CommandOutgoingFilter
	 */
	private class CommandOutgoingFilter extends Filter {
		public CommandOutgoingFilter() {
			super();
			setPreferredPosition(2);  // Before the Messaging (encoding) filter and the security related ones
		}

		@Override
		public final boolean accept(VerticalCommand cmd) {
			String name = cmd.getName();
			if (name.equals(MessagingSlice.SEND_MESSAGE)) {
				AID receiver = (AID) cmd.getParam(2);
				GlobalReplicationInfo info = globalReplications.get(receiver);
				if (info != null) {
					// Receiver is a virtual AID --> Redirect the SEND_MESSAGE command to one of the implementation replicas
					AID replica = info.getReplica();
					AID sender = (AID) cmd.getParam(0);
					GenericMessage gMsg = (GenericMessage) cmd.getParam(1);	
					// In case the selected replica is no longer valid, the message will have to be delivered 
					// again to another replica --> instruct JADE not to clear the message content (see 
					// jade.core.messaging.OutBox.addLast())
					gMsg.setModifiable(false);
					ACLMessage msg = gMsg.getACLMessage();
					if (msg != null) {
						msg.addUserDefinedParameter(AgentReplicationHelper.VIRTUAL_RECEIVER, receiver.getLocalName());
					}
					sendMessage(sender, gMsg, replica);

					// Veto the original command
					return false;
				}
			}
			else if (name.equals(MessagingSlice.NOTIFY_FAILURE)) {
				GenericMessage gMsg = (GenericMessage) cmd.getParam(0);
				ACLMessage msg = gMsg.getACLMessage();
				if (msg != null) {
					String virtualName = msg.getUserDefinedParameter(AgentReplicationHelper.VIRTUAL_RECEIVER);
					if (virtualName != null) {
						// This message was originally sent to a virtual agent. The selected 
						// implementation replica is no longer there or is unreachable however --> 
						// Remove the dirty replica, then select a new one and retry
						AID virtualAid = new AID(virtualName, AID.ISLOCALNAME);
						AID receiver = (AID) cmd.getParam(1);
						removeReplica(virtualAid, receiver);
						
						GlobalReplicationInfo info = globalReplications.get(virtualAid);
						if (info != null) {
							AID newReplica = info.getReplica();
							myLogger.log(Logger.FINE, "Redirecting message "+ACLMessage.getPerformative(msg.getPerformative())+"["+msg.getContent()+"] from dirty replica "+receiver.getLocalName()+" to new replica "+newReplica.getLocalName());
							if (receiver.equals(newReplica)) {
								// This may happen in COLD_REPLICATION mode when the master replica
								// has just died and has not been replaced yet. In this case sending 
								// this message will certainly fail, but we do it anyway until a new
								// master replica is selected. Just wait a little bit in order to avoid
								// entering a CPU consuming loop
								try {Thread.sleep(100);} catch (Exception e) {}
							}
							sendMessage(msg.getSender(), gMsg, newReplica);

							// Veto the original command
							return false;
						}
					}
				}
			}
			else if (name.equals(MainReplicationSlice.LEADERSHIP_ACQUIRED)) {
				// The master Main Container died and this backup Main just took the leadership.
				// Other peripheral containers may have died in the meanwhile -->
				// Check all replicated agents
				checkAllReplications();
			}
			return true;
		}

		@Override
		public final void postProcess(VerticalCommand cmd) {
			if (cmd.getName().equals(AgentMobilityHelper.INFORM_CLONED)) {
				AID id = (AID) cmd.getParam(0);
				Location where = (Location) cmd.getParam(1);
				String newName = (String) cmd.getParam(2);
				List<ReplicaInfo> rr = pendingReplicaCreationRequests.get(id);
				if (rr != null) {
					ReplicaInfo r = rr.get(0);
					if (r.where.equals(where) && r.replicaAid.getLocalName().equals(newName)) {
						// This cloning process was triggered by a replica creation request.
						// Remove the pending replica creation request, check if cloning was 
						// successful and, if this is the case register the new replica.
						rr.remove(0);
						boolean success = cmd.getReturnValue() == null;
						if (success) {
							broadcastAddReplica(id, r);
							localNotifyReplicaAddedToMaster(id, r);
						}


						// Finally, if there are other pending replica creation requests, serve the next one
						if (rr.size() > 0) {
							ReplicaInfo nextR = rr.get(0);
							// In this very moment the agent state is already AP_COPY --> Cloning the agent now would have no effect
							asynchCloneReplica(id, nextR.replicaAid.getLocalName(), nextR.where);
						}
						else {
							// NO more pending replica creation requests for agent id
							pendingReplicaCreationRequests.remove(id);
						}
					}
				}
			}
		}
	}  // END of inner class CommandOutgoingFilter


	/**
	 * Inner class CommandIncomingFilter
	 */
	private class CommandIncomingFilter extends Filter {
		public CommandIncomingFilter() {
			super();
			setPreferredPosition(2);  // Before the Messaging (encoding) filter and the security related ones
		}

		@Override
		public final boolean accept(VerticalCommand cmd) {
			String name = cmd.getName();
			if (myContainer.getMain() != null) {
				if (name.equals(AgentManagementSlice.INFORM_KILLED)) {
					// If the dead agent is a master replica of a virtual agent, select a new master replica and broadcast the information 
					AID deadAgent = (AID) cmd.getParam(0);
					handleInformKilled(deadAgent);
				}
				else if (name.equals(Service.NEW_SLICE)) {
					// If the new slice is an AgentReplicationSlice, notify it about the current virtual agents
					if (cmd.getService().equals(NAME)) {
						String sliceName = (String) cmd.getParam(0);
						handleNewSlice(sliceName);
					}
				}
				else if (name.equals(Service.DEAD_NODE)) {
					// A node monitored by this Main Container has just been removed
					// If it was a sudden termination (e.g. fault) INFORM_KILLED VCommands 
					// for agents in the dead node were not issued --> 
					// Check all replicated agents
					checkAllReplications();
				}
			}
			else {
				if (name.equals(Service.REATTACHED)) {
					// The Main lost all information related to this container --> Notify it again
					handleReattached();
				}
			}
			// Never veto a Command
			return true;
		}

		private void handleInformKilled(AID deadAid) {
			AID virtualAid = replicaToVirtualMap.remove(deadAid);
			if (virtualAid != null) {
				GlobalReplicationInfo info = globalReplications.get(virtualAid);
				if (info != null) {
					if (deadAid.equals(info.getMaster())) {
						// The dead agent is the master replica of a virtual agent
						handleMasterReplicaDead(info);
					}
					else {
						// The dead agent is a non-master replica --> Just notify the master
						notifyReplicaRemovedToMaster(info.getMaster(), deadAid, null);
					}
				}
			}
		}

		private void handleNewSlice(String newSliceName) {
			try {
				// Be sure to get the new (fresh) slice --> Bypass the service cache
				AgentReplicationSlice newSlice = (AgentReplicationSlice) getFreshSlice(newSliceName);
				GlobalReplicationInfo[] allInfos = globalReplications.values().toArray(new GlobalReplicationInfo[0]);
				
				for (GlobalReplicationInfo info : allInfos) {
					newSlice.synchReplication(info);
				}
			}
			catch (Throwable t) {
				myLogger.log(Logger.WARNING, "Error notifying new slice "+newSliceName+" about current replication information", t);
			}
		}

		private void handleReattached() {
			try {
				// Be sure to get a fresh slice --> Bypass the service cache
				AgentReplicationSlice mainSlice = (AgentReplicationSlice) getFreshSlice(MAIN_SLICE);
				// Notify the new Main Slice all replication information related to virtual agents
				// for which an implementation replica lives in the local container.
				// NOTE that the same replication information can be notified to the new Main Slice
				// by more than one container, but this case is properly taken into account so
				// that duplications are avoided.
				AID[] aa = replicaToVirtualMap.keySet().toArray(new AID[0]);
				List<AID> vv = new ArrayList<AID>();
				for (AID aid : aa) {
					if (myContainer.isLocalAgent(aid)) {
						AID virtualAid = replicaToVirtualMap.get(aid);
						if (virtualAid != null && !vv.contains(virtualAid)) {
							GlobalReplicationInfo info = globalReplications.get(virtualAid);
							if (info != null) {
								try {
									mainSlice.synchReplication(info);
								}
								catch (Exception e) {
									myLogger.log(Logger.WARNING, "Error notifying main slice about current local replication information", e);
								}
							}
							vv.add(virtualAid);
						}
					}
				}
			}
			catch (Throwable t) {
				myLogger.log(Logger.WARNING, "Error retrieving main slice.", t);
			}
		}
	}  // END of inner class CommandIncomingFilter


	/**
	 * Inner class ReplicaInfo
	 */
	private class ReplicaInfo {
		private AID replicaAid;
		private Location where;

		private ReplicaInfo(AID replicaAid, Location where) {
			this.replicaAid = replicaAid;
			this.where = where;
		}

		@Override
		public int hashCode() {
			return replicaAid.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return replicaAid.equals(((ReplicaInfo) obj).replicaAid);
		}
	}  // END of inner class ReplicaInfo


	private void localNotifyReplicaAddedToMaster(AID masterAid, ReplicaInfo r) {
		// Note that this method is invoked in the master replica container -->
		// Unlike other notifyXXX() methods, only the "local" version is needed
		Agent agent = myContainer.acquireLocalAgent(masterAid);
		if (agent != null) {
			myContainer.releaseLocalAgent(masterAid);
			try {
				if (agent instanceof AgentReplicationHelper.Listener) {
					((AgentReplicationHelper.Listener) agent).replicaAdded(r.replicaAid, r.where);
				}
			}
			catch (Exception e) {
				myLogger.log(Logger.WARNING, "Unexpected exception notifying master agent (replicaAdded())", e);
			}
		}
	}
	
	private void notifyReplicaRemovedToMaster(AID masterAid, AID removedReplica, Location where) {
		try {
			// This is always invoked on the Main Container
			if (where == null) {
				try {
					where = getAgentLocation(removedReplica);
				}
				catch (NotFoundException nfe) {
					// If this is triggered by a container fault, the GADT has already been cleaned.
					// We are not able to fill the removed replica location. 
				}
			}
			Location masterLocation = getAgentLocation(masterAid);
			AgentReplicationSlice slice = (AgentReplicationSlice) getFreshSlice(masterLocation.getName());
			slice.notifyReplicaRemoved(masterAid, removedReplica, where);
		}
		catch (Exception e) {
			// Should never happen as this masterAid has just been selected (and checked) as new master
			myLogger.log(Logger.WARNING, "Error notifying master replica "+masterAid.getLocalName()+" that replica "+removedReplica.getLocalName()+" has been removed", e);
		}
	}

	private void localNotifyReplicaRemovedToMaster(AID masterAid, AID removedReplica, Location where) {
		Agent agent = myContainer.acquireLocalAgent(masterAid);
		if (agent != null) {
			myContainer.releaseLocalAgent(masterAid);
			try {
				if (agent instanceof AgentReplicationHelper.Listener) {
					((AgentReplicationHelper.Listener) agent).replicaRemoved(removedReplica, where);
				}
			}
			catch (Exception e) {
				myLogger.log(Logger.WARNING, "Unexpected exception notifying master agent (becomeMaster())", e);
			}
		}
	}

	private void notifyBecomeMasterToMaster(AID masterAid) {
		try {
			// This is always invoked on the Main Container
			Location masterLocation = getAgentLocation(masterAid);
			AgentReplicationSlice slice = (AgentReplicationSlice) getFreshSlice(masterLocation.getName());
			slice.notifyBecomeMaster(masterAid);
		}
		catch (Exception e) {
			// Should never happen as this masterAid has just been selected (and checked) as new master
			myLogger.log(Logger.WARNING, "Error notifying new master replica "+masterAid.getLocalName()+" it just took the leadership", e);
		}
	}

	private void localNotifyBecomeMasterToMaster(AID masterAid) {
		Agent agent = myContainer.acquireLocalAgent(masterAid);
		if (agent != null) {
			myContainer.releaseLocalAgent(masterAid);
			try {
				if (agent instanceof AgentReplicationHelper.Listener) {
					((AgentReplicationHelper.Listener) agent).becomeMaster();
				}
			}
			catch (Exception e) {
				myLogger.log(Logger.WARNING, "Unexpected exception notifying master agent (becomeMaster())", e);
			}
		}
	}
	
	private Location getLocation(AID aid) throws Exception {
		if (myContainer.isLocalAgent(aid)) {
			return myContainer.getID();
		}

		// Aid lives somewhere else --> ask the slice on the Main Container
		AgentReplicationSlice mainSlice = (AgentReplicationSlice) getSlice(MAIN_SLICE);
		try {
			return mainSlice.getAgentLocation(aid);
		}
		catch (IMTPException imtpe) {
			// Get a fresh slice and retry
			mainSlice = (AgentReplicationSlice) getFreshSlice(MAIN_SLICE);
			return mainSlice.getAgentLocation(aid);
		}
	}

	private AID getVirtualAid(AID aid) {
		Agent agent = myContainer.acquireLocalAgent(aid);
		if (agent != null) {
			try {
				AgentReplicationHelper helper = (AgentReplicationHelper) agent.getHelper(NAME);
				return helper.getVirtualAid();
			}
			catch (ServiceException se) {
				// Should never happen since the AgentReplicationService is certainly there
				myLogger.log(Logger.WARNING, "Unexpected error retrieving AgentReplicationHelper for agent "+aid.getName());
				return null;
			}
			finally {
				myContainer.releaseLocalAgent(aid);
			}
		}
		else {
			return null;
		}
	}

	private void broadcastNewVirtualAgent(AID virtualAid, AID masterAid, int replicationMode) {
		myLogger.log(Logger.CONFIG, "Broadcasting new virtual agent "+virtualAid.getLocalName());
		GenericCommand cmd = new GenericCommand(AgentReplicationSlice.H_NEWVIRTUALAGENT, NAME, null);
		cmd.addParam(virtualAid);
		cmd.addParam(masterAid);
		cmd.addParam(replicationMode);

		try {
			broadcast(cmd, true); // Include myself
		}
		catch (Exception e) {
			myLogger.log(Logger.SEVERE, "Error broadcasting new virtual agent " + virtualAid.getLocalName(), e);
		}
	}

	private void broadcastAddReplica(AID masterAid, ReplicaInfo r) {
		AID virtualAid = getVirtualAid(masterAid);
		if (virtualAid != null) {
			myLogger.log(Logger.CONFIG, "Broadcasting new replica "+r.replicaAid.getLocalName()+" of virtual agent "+virtualAid.getLocalName());
			GenericCommand cmd = new GenericCommand(AgentReplicationSlice.H_ADDREPLICA, NAME, null);
			cmd.addParam(virtualAid);
			cmd.addParam(r.replicaAid);
			cmd.addParam(r.where);

			try {
				broadcast(cmd, true); // Include myself
			}
			catch (Exception e) {
				myLogger.log(Logger.SEVERE, "Error broadcasting new replica " + r.replicaAid.getLocalName() + " of virtual agent " + virtualAid.getLocalName(), e);
			}
		}
		else {
			myLogger.log(Logger.WARNING, "Cannot find virtual agent for master replica " + masterAid.getLocalName());
		}
	}
	
	private void broadcastMasterReplicaChanged(AID virtualAid, AID newMasterAid) {
		myLogger.log(Logger.CONFIG, "Broadcasting master replica changed for virtual agent "+virtualAid.getLocalName()+". New master replica = "+newMasterAid.getLocalName());
		GenericCommand cmd = new GenericCommand(AgentReplicationSlice.H_MASTERREPLICACHANGED, NAME, null);
		cmd.addParam(virtualAid);
		cmd.addParam(newMasterAid);

		try {
			broadcast(cmd, false); // Do NOT include myself since the change in the local container has already been done
		}
		catch (Exception e) {
			myLogger.log(Logger.WARNING, "Error broadcasting master replica changed for virtual agent " + virtualAid.getLocalName(), e);
		}
	}
	
	private void broadcastVirtualAgentDead(AID virtualAid) {
		myLogger.log(Logger.CONFIG, "Broadcasting virtual agent "+virtualAid.getLocalName()+" dead");
		GenericCommand cmd = new GenericCommand(AgentReplicationSlice.H_VIRTUALAGENTDEAD, NAME, null);
		cmd.addParam(virtualAid);

		try {
			broadcast(cmd, true); // Include myself
		}
		catch (Exception e) {
			myLogger.log(Logger.WARNING, "Error broadcasting master replica changed for virtual agent " + virtualAid.getLocalName(), e);
		}
	}
	

	private void cloneReplica(AID aid, String replicaName, Location where) {
		Agent agent = myContainer.acquireLocalAgent(aid);
		if (agent != null) {
			myLogger.log(Logger.CONFIG, "Cloning agent "+aid.getLocalName()+" to create replica "+replicaName+" on container "+where.getName());
			agent.doClone(where, replicaName);
			myContainer.releaseLocalAgent(aid);
		}
	}

	private void asynchCloneReplica(AID aid, final String replicaName, final Location where) {
		Agent agent = myContainer.acquireLocalAgent(aid);
		if (agent != null) {
			agent.addBehaviour(new OneShotBehaviour(agent) {
				@Override
				public void action() {
					myLogger.log(Logger.CONFIG, "Cloning agent "+myAgent.getLocalName()+" to create replica "+replicaName+" on container "+where.getName());
					myAgent.doClone(where, replicaName);
				}
			});
			myContainer.releaseLocalAgent(aid);
		}
	}

	private final void sendMessage(AID sender, GenericMessage gMsg, AID receiver) {
		GenericCommand cmd = new GenericCommand(MessagingSlice.SEND_MESSAGE, MessagingService.NAME, null);
		cmd.addParam(sender);
		cmd.addParam(gMsg);
		cmd.addParam(receiver);

		try {
			theMessagingService.submit(cmd);
		}
		catch (ServiceException se) {
			// Should never happen
			se.printStackTrace();
		}
	}

	private void invokeAgentMethod(AID aid, String methodName, Object[] arguments) throws NotFoundException, ServiceException {
		Agent agent = myContainer.acquireLocalAgent(aid);
		if (agent != null) {
			myContainer.releaseLocalAgent(aid);
			try {
				Method m = getMethod(agent, methodName);
				if (myLogger.isLoggable(Logger.FINE)) {
					myLogger.log(Logger.FINE, "Invoking replicated method "+methodName+" on agent "+aid.getLocalName());
				}
				AgentReplicationHandle.enterReplicatedCall();
				m.invoke(agent, arguments);
			}
			catch (NoSuchMethodException nsme) {
				throw new ServiceException("Method "+methodName+" not found in class "+agent.getClass().getName()+" of agent "+agent.getLocalName());
			}
			catch (IllegalAccessException iae) {
				throw new ServiceException("Method "+methodName+" of class "+agent.getClass().getName()+" of agent "+agent.getLocalName()+" cannot be accessed");
			}
			catch (InvocationTargetException ite) {
				throw new ServiceException("Exception excecuting method "+methodName+" of agent "+aid.getLocalName(), ite.getCause());
			}
			finally {
				AgentReplicationHandle.exitReplicatedCall();
			}
		}
		else {
			throw new NotFoundException("Agent "+aid.getLocalName()+" not found");
		}
	}

	private void addReplica(AID virtualAid, AID replicaAid, Location where) throws Exception {
		myLogger.log(Logger.CONFIG, "Received new replica information: virtual="+virtualAid.getLocalName()+", replica="+replicaAid.getLocalName()+", location="+where.getName());

		addReplicaVirtualMapping(replicaAid, virtualAid);

		GlobalReplicationInfo info = globalReplications.get(virtualAid);
		if (info != null) {
			// If some of the current replicas live in the local container, update the related helper
			AID[] currentReplicas = info.getAllReplicas();
			for (int i = 0; i < currentReplicas.length; ++i) {
				AID aid = currentReplicas[i];
				Agent agent = myContainer.acquireLocalAgent(aid);
				if (agent != null) {
					try {
						AgentReplicationHelperImpl helper = (AgentReplicationHelperImpl) agent.getHelper(NAME);
						helper.addPeerReplica(new ReplicaInfo(replicaAid, where));
					}
					catch (ServiceException se) {
						// Should never happen since the AgentReplicationService is certainly installed in this container
						myLogger.log(Logger.WARNING, "Unexpected error retrieving AgentReplicationHelper for agent "+aid.getName(), se);
					}
					finally {
						myContainer.releaseLocalAgent(aid);
					}
				}
			}

			// Then add the new replica. In this way the helper of the new replica will never
			// be updated with the information of the new replica itself
			info.addReplica(replicaAid);
		}
		else {
			myLogger.log(Logger.WARNING, "Global Replication information for virtual agent "+virtualAid.getLocalName()+" not found in container "+myContainer.getID().getName());
		}
	}

	private void removeReplica(AID virtualAid, AID replicaAid) {
		removeReplicaVirtualMapping(replicaAid);

		GlobalReplicationInfo info = globalReplications.get(virtualAid);
		if (info != null) {
			info.removeReplica(replicaAid);

			// If some of the remaining replicas live in the local container, update the related helper
			AID[] currentReplicas = info.getAllReplicas();
			for (int i = 0; i < currentReplicas.length; ++i) {
				AID aid = currentReplicas[i];
				Agent agent = myContainer.acquireLocalAgent(aid);
				if (agent != null) {
					try {
						AgentReplicationHelperImpl helper = (AgentReplicationHelperImpl) agent.getHelper(NAME);
						helper.removePeerReplica(new ReplicaInfo(replicaAid, null));
					}
					catch (ServiceException se) {
						// Should never happen since the AgentReplicationService is certainly installed in this container
						myLogger.log(Logger.WARNING, "Unexpected error retrieving AgentReplicationHelper for agent "+aid.getLocalName(), se);
					}
					finally {
						myContainer.releaseLocalAgent(aid);
					}
				}
			}
		}
	}

	private void addReplicaVirtualMapping(AID replicaAid, AID virtualAid) {
		AID oldVirtualAid = replicaToVirtualMap.put(replicaAid, virtualAid);
		if (oldVirtualAid == null || !oldVirtualAid.equals(virtualAid)) {
			myLogger.log(Logger.CONFIG, "Added replica-to-virtual mapping: "+replicaAid.getLocalName()+"-->"+virtualAid.getLocalName());
		}
	}

	private void removeReplicaVirtualMapping(AID replicaAid) {
		AID virtualAid = replicaToVirtualMap.remove(replicaAid);
		if (virtualAid != null) {
			myLogger.log(Logger.CONFIG, "Removed replica-to-virtual mapping: "+replicaAid.getLocalName()+"-->"+virtualAid.getLocalName());
		}
	}

	private GlobalReplicationInfo newVirtualAgent(AID virtualAid, AID masterAid, int replicationMode) throws Exception {
		GlobalReplicationInfo info = null;
		// In the reattach procedure this method can be called in parallel for the same virtual agent 
		synchronized (globalReplications) {
			info = globalReplications.get(virtualAid);
			if (info == null) {
				myLogger.log(Logger.CONFIG, "New virtual agent: virtual="+virtualAid.getLocalName()+", master="+masterAid.getLocalName());
				info = new GlobalReplicationInfo(virtualAid, masterAid, replicationMode);
				globalReplications.put(virtualAid, info);
			}
			else {
				// Just check that information are consistent
				if (!masterAid.equals(info.getMaster())) {
					throw new ServiceException("Inconsistent replication information for virtual agent "+virtualAid.getLocalName()+": current-master = "+info.getMaster().getLocalName()+", new-master = "+masterAid.getLocalName());
				}
			}
		}
		addReplicaVirtualMapping(masterAid, virtualAid);
		return info;
	}

	private ContainerID getAgentLocation(AID aid) throws NotFoundException {
		MainContainer impl = myContainer.getMain();
		if(impl != null) {
			return impl.getContainerID(aid);
		}
		else {
			// Should never happen
			throw new NotFoundException("getAgentLocation() invoked on a non-main container");
		}
	}
	
	private void handleMasterReplicaDead(GlobalReplicationInfo info) {
		// No need to check if impl is null since this can only be executed on a Main Container
		MainContainer impl = myContainer.getMain();
		do {
			AID newMasterAid = info.masterReplicaDead();
			if (newMasterAid != null) {
				// New master replica selected. Check if it is actually alive. Otherwise try again
				if (impl.acquireAgentDescriptor(newMasterAid) != null) {
					// New master replica alive --> Broadcast the change to all other slices 
					impl.releaseAgentDescriptor(newMasterAid);
					broadcastMasterReplicaChanged(info.getVirtual(), newMasterAid);
					// Then notify the selected agent that it became the new master
					notifyBecomeMasterToMaster(newMasterAid);
					return;
				}
			}
			else {
				// This virtual agent does not have replicas anymore --> remove it
				try {
					impl.deadAgent(info.getVirtual(), false);
				}
				catch (NotFoundException nfe) {
					// Just ignore 
				}
				broadcastVirtualAgentDead(info.getVirtual());
				return;
			}
		} while (true);
	}
	
	private void checkAllReplications() {
		// No need to check if impl is null since this can only be executed on a Main Container
		MainContainer impl = myContainer.getMain();
		// For each virtual agent 
		// - check if the related master replica is still alive.
		// If not handle the dead master replica case.
		// - then for each replica check if it is still alive. If not notify the master
		GlobalReplicationInfo[] gg = globalReplications.values().toArray(new GlobalReplicationInfo[0]);
		for (GlobalReplicationInfo info : gg) {
			// Check master
			AID masterAid = info.getMaster();
			if (impl.acquireAgentDescriptor(masterAid) != null) {
				// Master replica still there --> nothing to do
				impl.releaseAgentDescriptor(masterAid);
				myLogger.log(Logger.INFO, "Master replica "+masterAid.getLocalName()+" of virtual agent "+info.getVirtual().getLocalName()+" ALIVE");
			}
			else {
				// Master replica is not there anymore --> DEAD
				handleMasterReplicaDead(info);
				masterAid = info.getMaster();
			}
			
			// Check other replicas
			AID[] allReplicas = info.getAllReplicas();
			for (AID replicaAid : allReplicas) {
				if (!replicaAid.equals(masterAid)) {
					if (impl.acquireAgentDescriptor(replicaAid) != null) {
						// Replica still there --> nothing to do
						impl.releaseAgentDescriptor(replicaAid);
						myLogger.log(Logger.INFO, "Replica "+replicaAid.getLocalName()+" of virtual agent "+info.getVirtual().getLocalName()+" ALIVE");
					}
					else {
						// Replica is not there anymore --> DEAD
						notifyReplicaRemovedToMaster(masterAid, replicaAid, null);
					}
				}
			}
		}
	}


	/**
	 * Inner class ServiceComponent
	 */
	private class ServiceComponent implements Service.Slice {
		public Service getService() {
			return AgentReplicationService.this;
		}

		public Node getNode() throws ServiceException {
			try {
				return AgentReplicationService.this.getLocalNode();
			}
			catch(IMTPException imtpe) {
				throw new ServiceException("Error retrieving local node", imtpe);
			}
		}

		public VerticalCommand serve(HorizontalCommand cmd) {
			try {
				String cmdName = cmd.getName();				
				if (cmdName.equals(AgentReplicationSlice.H_INVOKEAGENTMETHOD)) {
					AID aid = (AID) cmd.getParam(0);
					String methodName = (String) cmd.getParam(1);
					Object[] arguments = (Object[]) cmd.getParam(2);
					invokeAgentMethod(aid, methodName, arguments);
				}
				else if(cmdName.equals(AgentReplicationSlice.H_ADDREPLICA)) {
					AID virtualAid = (AID) cmd.getParam(0);
					AID replicaAid = (AID) cmd.getParam(1);
					Location where = (Location) cmd.getParam(2);
					addReplica(virtualAid, replicaAid, where);
				}
				else if(cmdName.equals(AgentReplicationSlice.H_NEWVIRTUALAGENT)) {
					AID virtualAid = (AID) cmd.getParam(0);
					AID masterAid = (AID) cmd.getParam(1);
					int replicationMode = (Integer) cmd.getParam(2);
					newVirtualAgent(virtualAid, masterAid, replicationMode);
				}
				else if(cmdName.equals(AgentReplicationSlice.H_GETAGENTLOCATION)) {
					AID aid = (AID) cmd.getParam(0);
					cmd.setReturnValue(getAgentLocation(aid));
				}
				else if(cmdName.equals(AgentReplicationSlice.H_REPLICACREATIONREQUESTED)) {
					AID virtualAid = (AID) cmd.getParam(0);
					AID replicaAid = (AID) cmd.getParam(1);
					addReplicaVirtualMapping(replicaAid, virtualAid);
				}
				else if(cmdName.equals(AgentReplicationSlice.H_SYNCHREPLICATION)) {
					AID virtualAid = (AID) cmd.getParam(0);
					AID masterAid = (AID) cmd.getParam(1);
					int replicationMode = (Integer) cmd.getParam(2);
					AID[] allReplicas = (AID[]) cmd.getParam(3);
					GlobalReplicationInfo info = newVirtualAgent(virtualAid, masterAid, replicationMode);
					// NOTE that this call is used to notify a starting slice about the current 
					// situation --> No replica agent can live in the newly started local container
					// --> We don't use the whole addReplica() method  
					for (AID replicaAid : allReplicas) {
						if (!replicaAid.equals(masterAid)) {
							info.addReplica(replicaAid);
							addReplicaVirtualMapping(replicaAid, virtualAid);
						}
					}
				}
				else if(cmdName.equals(AgentReplicationSlice.H_MASTERREPLICACHANGED)) {
					AID virtualAid = (AID) cmd.getParam(0);
					AID newMasterAid = (AID) cmd.getParam(1);
					GlobalReplicationInfo info = globalReplications.get(virtualAid);
					if (info != null) {
						info.masterReplicaChanged(newMasterAid);
					}
				}
				else if(cmdName.equals(AgentReplicationSlice.H_VIRTUALAGENTDEAD)) {
					AID virtualAid = (AID) cmd.getParam(0);
					globalReplications.remove(virtualAid);
					myLogger.log(Logger.CONFIG, "Virtual agent "+virtualAid.getLocalName()+" removed");
				}
				else if(cmdName.equals(AgentReplicationSlice.H_NOTIFYBECOMEMASTER)) {
					AID newMasterAid = (AID) cmd.getParam(0);
					localNotifyBecomeMasterToMaster(newMasterAid);
				}
				else if(cmdName.equals(AgentReplicationSlice.H_NOTIFYREPLICAREMOVED)) {
					AID masterAid = (AID) cmd.getParam(0);
					AID removedReplica = (AID) cmd.getParam(1);
					Location where = (Location) cmd.getParam(2);
					localNotifyReplicaRemovedToMaster(masterAid, removedReplica, where);
				}
			}
			catch (Throwable t) {
				cmd.setReturnValue(t);
			}
			return null;
		}

	}  // END of inner class ServiceComponent

	private Method getMethod(Agent agent, String methodName) throws NoSuchMethodException {
		String key = agent.getLocalName()+'#'+methodName;
		Method m = (Method) cachedAgentMethods.get(key);
		if (m == null) {
			// NOTE: We cannot use Class.getMethod() since we have parameter values, but not parameter types
			Method[] mm = agent.getClass().getMethods();
			for (int i = 0; i < mm.length; ++i) {
				if (mm[i].getName().equals(methodName)) {
					m = mm[i];
					cachedAgentMethods.put(key, m);
					break;
				}
			}
		}
		if (m == null) {
			throw new NoSuchMethodException(methodName);
		}
		return m;
	}


}
