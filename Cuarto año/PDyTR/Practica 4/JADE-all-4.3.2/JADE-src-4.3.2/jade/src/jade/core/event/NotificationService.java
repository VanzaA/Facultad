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

package jade.core.event;

//#J2ME_EXCLUDE_FILE

import jade.core.HorizontalCommand;
import jade.core.VerticalCommand;
import jade.core.Service;
import jade.core.BaseService;
import jade.core.Sink;
import jade.core.Filter;
import jade.core.Node;

import jade.core.Agent;
import jade.core.AgentContainer;
import jade.core.MainContainer;
import jade.core.AID;
import jade.core.ContainerID;
import jade.core.BehaviourID;
import jade.core.AgentState;
import jade.core.Channel;
import jade.core.Profile;
import jade.core.ProfileException;
import jade.core.ServiceException;
import jade.core.IMTPException;
import jade.core.NotFoundException;
import jade.core.ServiceHelper;

import jade.core.messaging.GenericMessage;

import jade.core.behaviours.Behaviour;

import jade.lang.acl.ACLMessage;
import jade.security.JADEPrincipal;
import jade.tools.ToolNotifier;

import jade.util.SynchList;
import jade.util.leap.Iterator;
import jade.util.leap.Map;
import jade.util.leap.HashMap;
import jade.util.leap.List;
import jade.util.leap.LinkedList;

/**
 
 The JADE service to manage the event notification subsystem installed
 on the platform.
 
 @author Giovanni Rimassa - FRAMeTech s.r.l.
 
 */
public class NotificationService extends BaseService {
	/**
	 The name of this service.
	 */
	public static final String NAME = "jade.core.event.Notification";
	
	private static final String[] OWNED_COMMANDS = new String[] {
		NotificationSlice.SNIFF_ON,
		NotificationSlice.SNIFF_OFF,
		NotificationSlice.DEBUG_ON,
		NotificationSlice.DEBUG_OFF,
		NotificationSlice.NOTIFY_POSTED,
		NotificationSlice.NOTIFY_RECEIVED,
		NotificationSlice.NOTIFY_CHANGED_AGENT_STATE,
		NotificationSlice.NOTIFY_CHANGED_AGENT_PRINCIPAL,
		NotificationSlice.NOTIFY_BEHAVIOUR_ADDED,
		NotificationSlice.NOTIFY_BEHAVIOUR_REMOVED,
		NotificationSlice.NOTIFY_CHANGED_BEHAVIOUR_STATE	
	};
	
	
	// The special name of an auxiliary thread used to avoid deadlock when debugging the AMS
	private final static String AMS_DEBUG_HELPER = "AMS-debug-helper";
	
	// The concrete agent container, providing access to LADT, etc.
	private AgentContainer myContainer;
	
	// The local slice for this service
	private ServiceComponent localSlice;
	private Sink sourceSink = new NotificationSourceSink();
	private Filter outgoingFilter = new NotificationOutgoingFilter();
	private Filter incomingFilter = new NotificationIncomingFilter();
	private NotificationHelper helper = new NotificationHelperImpl();
	
	// The list of all listeners of ACL messaging related events (uses RW-locking)
	private SynchList messageListeners = new SynchList();
	
	// The list of all listeners of agent life cycle events (uses RW-locking)
	private SynchList agentListeners = new SynchList();
	
	// The list of all listeners of container events (uses RW-locking)
	private SynchList containerListeners = new SynchList();
	
	// This maps a debugged agent into the list of debuggers that are 
	// currently debugging it. It is used to know when an agent is no longer
	// debugged by any debugger and behaviour event generation can be turned off.
	private Map debuggers = new HashMap();
	
	
	public void init(AgentContainer ac, Profile p) throws ProfileException {
		super.init(ac, p);		
		myContainer = ac;
		
		// Create a local slice
		localSlice = new ServiceComponent();	
	}
	
	public String getName() {
		return NotificationSlice.NAME;
	}
	
	public Class getHorizontalInterface() {
		return NotificationSlice.class;
	}
	
	public Slice getLocalSlice() {
		return localSlice;
	}
	
	public Filter getCommandFilter(boolean direction) {
		if (direction == Filter.OUTGOING) {
			return outgoingFilter;
		}
		else {
			return incomingFilter;
		}
	}
	
	public Sink getCommandSink(boolean side) {
		if (side == Sink.COMMAND_SOURCE) {
			return sourceSink;
		}
		else {
			return null;
		}
	}
	
	public ServiceHelper getHelper(Agent a) throws ServiceException {
		return helper;
	}
	
	public String[] getOwnedCommands() {
		return OWNED_COMMANDS;
	}

	
	/**
	 * Inner class NotificationSourceSink
	 */
	private class NotificationSourceSink implements Sink {
	    public void consume(VerticalCommand cmd) {
			try {
				String name = cmd.getName();
				if(name.equals(NotificationSlice.SNIFF_ON)) {
					handleSniffOn(cmd);
				}
				if(name.equals(NotificationSlice.SNIFF_OFF)) {
					handleSniffOff(cmd);
				}
				else if(name.equals(NotificationSlice.DEBUG_ON)) {
					handleDebugOn(cmd);
				}
				else if(name.equals(NotificationSlice.DEBUG_OFF)) {
					handleDebugOff(cmd);
				}
				else if(name.equals(NotificationSlice.NOTIFY_POSTED)) {
					handleNotifyPosted(cmd);
				}
				else if(name.equals(NotificationSlice.NOTIFY_RECEIVED)) {
					handleNotifyReceived(cmd);
				}
				else if(name.equals(NotificationSlice.NOTIFY_CHANGED_AGENT_PRINCIPAL)) {
					handleNotifyChangedAgentPrincipal(cmd);
				}
				else if(name.equals(NotificationSlice.NOTIFY_BEHAVIOUR_ADDED)) {
					handleNotifyAddedBehaviour(cmd);
				}
				else if(name.equals(NotificationSlice.NOTIFY_BEHAVIOUR_REMOVED)) {
					handleNotifyRemovedBehaviour(cmd);
				}
				else if(name.equals(NotificationSlice.NOTIFY_CHANGED_BEHAVIOUR_STATE)) {
					handleNotifyChangedBehaviourState(cmd);
				}
			}
			catch(Throwable t) {
				cmd.setReturnValue(t);
			}
		}
				
		private void handleSniffOn(VerticalCommand cmd) throws IMTPException, ServiceException, NotFoundException {
			Object[] params = cmd.getParams();
			AID sniffer = (AID)params[0];
			List targets = (List)params[1];
			
			MainContainer impl = myContainer.getMain();
			if(impl != null) {		
				// Activate sniffing each element of the list
				Iterator it = targets.iterator();
				while(it.hasNext()) {
					AID target = (AID)it.next();
					ContainerID cid = impl.getContainerID(target);
					
					NotificationSlice slice = (NotificationSlice)getSlice(cid.getName());
					try {
						slice.sniffOn(sniffer, target);
					}
					catch(IMTPException imtpe) {
						// Try to get a newer slice and repeat...
						slice = (NotificationSlice)getFreshSlice(cid.getName());
						slice.sniffOn(sniffer, target);
					}
				}
			}
			else {
				// Do nothing for now, but could also route the command to the main slice, thus enabling e.g. AMS replication
			}
		}
		
		private void handleSniffOff(VerticalCommand cmd) throws IMTPException, ServiceException, NotFoundException {
			Object[] params = cmd.getParams();
			AID sniffer = (AID)params[0];
			List targets = (List)params[1];
			
			MainContainer impl = myContainer.getMain();
			if(impl != null) {
				// Deactivate sniffing each element of the list
				Iterator it = targets.iterator();
				while(it.hasNext()) {
					AID target = (AID)it.next();
					ContainerID cid = impl.getContainerID(target);
					
					NotificationSlice slice = (NotificationSlice)getSlice(cid.getName());
					try {
						slice.sniffOff(sniffer, target);
					}
					catch(IMTPException imtpe) {
						// Try to get a newer slice and repeat...
						slice = (NotificationSlice)getFreshSlice(cid.getName());
						slice.sniffOff(sniffer, target);
					}
				}
			}
			else {
				// Do nothing for now, but could also route the command to the main slice, thus enabling e.g. AMS replication
			}
		}
		
		private void handleDebugOn(VerticalCommand cmd) throws IMTPException, ServiceException, NotFoundException {
			Object[] params = cmd.getParams();
			AID introspector = (AID)params[0];
			List targets = (List)params[1];
			
			MainContainer impl = myContainer.getMain();
			if(impl != null) {
				// Activate debugging each element of the list
				Iterator it = targets.iterator();
				while(it.hasNext()) {
					AID target = (AID)it.next();
					ContainerID cid = impl.getContainerID(target);
					
					NotificationSlice slice = (NotificationSlice)getSlice(cid.getName());
					try {
						slice.debugOn(introspector, target);
					}
					catch(IMTPException imtpe) {
						// Try to get a newer slice and repeat...
						slice = (NotificationSlice)getFreshSlice(cid.getName());
						slice.debugOn(introspector, target);
					}
				}
			}
			else {
				// Do nothing for now, but could also route the command to the main slice, thus enabling e.g. AMS replication
			}
		}
		
		private void handleDebugOff(VerticalCommand cmd) throws IMTPException, ServiceException, NotFoundException {
			Object[] params = cmd.getParams();
			AID introspector = (AID)params[0];
			List targets = (List)params[1];
			
			MainContainer impl = myContainer.getMain();
			if(impl != null) {
				// Deactivate debugging each element of the list
				Iterator it = targets.iterator();
				while(it.hasNext()) {
					AID target = (AID)it.next();
					ContainerID cid = impl.getContainerID(target);
					
					NotificationSlice slice = (NotificationSlice)getSlice(cid.getName());
					try {
						slice.debugOff(introspector, target);
					}
					catch(IMTPException imtpe) {
						// Try to get a newer slice and repeat...
						slice = (NotificationSlice)getFreshSlice(cid.getName());
						slice.debugOff(introspector, target);
					}
				}
			}
			else {
				// Do nothing for now, but could also route the command to the main slice, thus enabling e.g. AMS replication
			}
		}
		
		private void handleNotifyPosted(VerticalCommand cmd) {
			Object[] params = cmd.getParams();
			ACLMessage msg = (ACLMessage)params[0];
			AID receiver = (AID)params[1];
			
			firePostedMessage(msg, receiver);
		}
		
		private void handleNotifyReceived(VerticalCommand cmd) {
			Object[] params = cmd.getParams();
			ACLMessage msg = (ACLMessage)params[0];
			AID receiver = (AID)params[1];
			
			fireReceivedMessage(msg, receiver);
		}
		
		private void handleNotifyChangedAgentPrincipal(VerticalCommand cmd) {
			Object[] params = cmd.getParams();
			AID id = (AID)params[0];
			JADEPrincipal from = (JADEPrincipal)params[1];
			JADEPrincipal to = (JADEPrincipal)params[2];
			
			fireChangedAgentPrincipal(id, from, to);
		}
		
		private void handleNotifyAddedBehaviour(VerticalCommand cmd) {
			Object[] params = cmd.getParams();
			AID id = (AID)params[0];
			Behaviour b = (Behaviour)params[1];
			
			fireAddedBehaviour(id, b);
		}
		
		private void handleNotifyRemovedBehaviour(VerticalCommand cmd) {
			Object[] params = cmd.getParams();
			AID id = (AID)params[0];
			Behaviour b = (Behaviour)params[1];
			
			fireRemovedBehaviour(id, b);
		}
		
		private void handleNotifyChangedBehaviourState(VerticalCommand cmd) {
			Object[] params = cmd.getParams();
			AID id = (AID)params[0];
			Behaviour b = (Behaviour)params[1];
			String from = (String)params[2];
			String to = (String)params[3];
			
			fireChangedBehaviourState(id, b, from, to);
		}
	} // End of inner class NotificationSourceSink
	
	
	/**
	 * Inner class NotificationOutgoingFilter
	 */
	private class NotificationOutgoingFilter extends Filter {
		public boolean accept(VerticalCommand cmd) {		
			try {
				String name = cmd.getName();
				if(name.equals(jade.core.messaging.MessagingSlice.SEND_MESSAGE)) {
					handleSendMessage(cmd);
				}
				else if(name.equals(jade.core.management.AgentManagementSlice.INFORM_CREATED)) {
					handleInformCreated(cmd);
				}
				else if(name.equals(jade.core.management.AgentManagementSlice.INFORM_KILLED)) {
					handleInformKilled(cmd);
				}
				else if(name.equals(jade.core.management.AgentManagementSlice.INFORM_STATE_CHANGED)) {
					handleInformStateChanged(cmd);
				}
				else if(name.equals(jade.core.replication.MainReplicationSlice.LEADERSHIP_ACQUIRED)) {
					handleLeadershipAcquired(cmd);
				}
			}
			catch(Throwable t) {
				cmd.setReturnValue(t);
			}
			
			// Never veto a command
			return true;
		}
		
		private void handleSendMessage(VerticalCommand cmd) {
			Object[] params = cmd.getParams();
			AID sender = (AID)params[0];
			ACLMessage msg = ((GenericMessage)params[1]).getACLMessage();
			AID receiver = (AID)params[2];
			
			fireSentMessage(msg, sender, receiver);
		}
		
		private void handleInformCreated(VerticalCommand cmd) {
			Object[] params = cmd.getParams();
			AID agent = (AID)params[0];
			
			fireBornAgent(agent);
		}
		
		private void handleInformKilled(VerticalCommand cmd) {
			Object[] params = cmd.getParams();
			AID agent = (AID)params[0];
			
			fireDeadAgent(agent);
		}
		
		private void handleInformStateChanged(VerticalCommand cmd) {
			Object[] params = cmd.getParams();
			AID id = (AID)params[0];
			AgentState from = (AgentState)params[1];
			AgentState to = (AgentState)params[2];
			
			fireChangedAgentState(id, from, to);
		}
		
		private void handleLeadershipAcquired(VerticalCommand cmd) {
			fireLeadershipAcquired();
		}
	} // END of inner class NotificationOutgoingFilter
	
	
	/**
	 * Inner class NotificationIncomingFilter
	 */
	private class NotificationIncomingFilter extends Filter {
		// Notify listeners about the REATTACHED and RECONNECTED events only when the reattachment/reconnection procedure 
		// has been completed
		public void postProcess(VerticalCommand cmd) {		
			try {
				String name = cmd.getName();
				if(name.equals(jade.core.Service.REATTACHED)) {
					handleReattached(cmd);
				}
				else if(name.equals(jade.core.Service.RECONNECTED)) {
					handleReconnected(cmd);
				}
			}
			catch(Throwable t) {
				cmd.setReturnValue(t);
			}
		}
		
		private void handleReattached(VerticalCommand cmd) {
			fireReattached();
		}
		
		private void handleReconnected(VerticalCommand cmd) {
			fireReconnected();
		}
	} // END of inner class NotificationIncomingFilter
	
	
	/**
	 * Inner class ServiceComponent
	 */
	private class ServiceComponent implements Service.Slice {
		public Service getService() {
			return NotificationService.this;
		}
		
		public Node getNode() throws ServiceException {
			try {
				return NotificationService.this.getLocalNode();
			}
			catch(IMTPException imtpe) {
				throw new ServiceException("Problem in contacting the IMTP Manager", imtpe);
			}
		}
		
		public VerticalCommand serve(HorizontalCommand cmd) {
			try {
				String cmdName = cmd.getName();
				Object[] params = cmd.getParams();
				
				if(cmdName.equals(NotificationSlice.H_SNIFFON)) {
					AID snifferName = (AID)params[0];
					AID targetName = (AID)params[1];
					
					sniffOn(snifferName, targetName);
				}
				else if(cmdName.equals(NotificationSlice.H_SNIFFOFF)) {
					AID snifferName = (AID)params[0];
					AID targetName = (AID)params[1];
					
					sniffOff(snifferName, targetName);
				}
				else if(cmdName.equals(NotificationSlice.H_DEBUGON)) {
					AID introspectorName = (AID)params[0];
					AID targetName = (AID)params[1];
					
					debugOn(introspectorName, targetName);
				}
				else if(cmdName.equals(NotificationSlice.H_DEBUGOFF)) {
					AID introspectorName = (AID)params[0];
					AID targetName = (AID)params[1];
					
					debugOff(introspectorName, targetName);
				}
			}
			catch(Throwable t) {
				cmd.setReturnValue(t);
			}
			
			return null;
		}
		
		private void sniffOn(AID snifferName, AID targetName) throws IMTPException {
			ToolNotifier tn = findNotifier(snifferName);
			if(tn == null) { // Need a new notifier 
				tn = new ToolNotifier(snifferName);
				AID id = new AID(snifferName.getLocalName() + "-on-" + myID().getName(), AID.ISLOCALNAME);
				try {
					myContainer.initAgent(id, tn, null, null); // FIXME: Modify to use a proper owner Principal
					myContainer.powerUpLocalAgent(id);
					helper.registerMessageListener(tn);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
			tn.addObservedAgent(targetName);
		}
		
		private void sniffOff(AID snifferName, AID targetName) throws IMTPException {
			ToolNotifier tn = findNotifier(snifferName);
			if(tn != null) { 
				tn.removeObservedAgent(targetName);
			}
		}
		
		private void debugOn(AID introspectorName, AID targetName) throws IMTPException {
			// AMS debug enabling must be done by a separated Thread to avoid
			// deadlock with ToolNotifier startup
			if (targetName.equals(myContainer.getAMS()) && !(Thread.currentThread().getName().equals(AMS_DEBUG_HELPER))) {
				final AID in = introspectorName;
				final AID tg = targetName;
				Thread helper = new Thread(new Runnable() {
					public void run() {
						try {
							debugOn(in, tg);
						}
						catch(IMTPException imtpe) {
							imtpe.printStackTrace();
						}
					}
				});
				helper.setName(AMS_DEBUG_HELPER);
				helper.start();
				return;
			}
			
			// Get the ToolNotifier for the indicated debugger (or create a new one
			// if not yet there)
			ToolNotifier tn = findNotifier(introspectorName);
			if(tn == null) { // Need a new notifier
				tn = new ToolNotifier(introspectorName);
				AID id = new AID(introspectorName.getLocalName() + "-on-" + myID().getName(), AID.ISLOCALNAME);
				try {
					myContainer.initAgent(id, tn, null, null); // FIXME: Modify to use a proper owner Principal
					myContainer.powerUpLocalAgent(id);
					if (targetName.equals(myContainer.getAMS())) {
						// If we are debugging the AMS, let's wait for the ToolNotifier 
						// to be ready to avoid deadlock problems. Note also that in 
						// this case this code is executed by the ams-debug-helper thread and not
						// by the AMS thread
						tn.waitUntilStarted();
					}
					// Wait a bit to let the ToolNotifier pass in ACTIVE_STATE
					try {Thread.sleep(1000);} catch (Exception e) {};
					helper.registerMessageListener(tn);
					helper.registerAgentListener(tn);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
			tn.addObservedAgent(targetName);
			
			// Update the map of debuggers currently debugging the targetName agent
			synchronized (debuggers) {
				List l = (List) debuggers.get(targetName);
				if (l == null) {
					l = new LinkedList();
					debuggers.put(targetName, l);
				}
				if (!l.contains(introspectorName)) {
					l.add(introspectorName);
				}
			}
			
			Agent a = myContainer.acquireLocalAgent(targetName);
			
			// Activate generation of behaviour-related events on the
			// target agent
			a.setGenerateBehaviourEvents(true);
			
			// Retrieve the current agent state
			AgentState as = a.getAgentState();
			
			// Retrieve the list of pending ACL messages
			List messages = new LinkedList();
			myContainer.fillListFromMessageQueue(messages, a);
			
			// Retrieve the list of ready and blocked agent behaviour IDs
			List readyBehaviours = new LinkedList();
			myContainer.fillListFromReadyBehaviours(readyBehaviours, a);
			List blockedBehaviours = new LinkedList();
			myContainer.fillListFromBlockedBehaviours(blockedBehaviours, a);
			
			myContainer.releaseLocalAgent(targetName);
			
			// Notify all the needed events	
			fireChangedAgentState(targetName, as, as);
			
			Iterator itReady = readyBehaviours.iterator();
			while(itReady.hasNext()) {
				BehaviourID bid = (BehaviourID)itReady.next();
				AgentEvent ev = new AgentEvent(AgentEvent.ADDED_BEHAVIOUR, targetName, bid, myContainer.getID());
				tn.addedBehaviour(ev);
			}
			
			Iterator itBlocked = blockedBehaviours.iterator();
			while(itBlocked.hasNext()) {
				BehaviourID bid = (BehaviourID)itBlocked.next();
				AgentEvent ev = new AgentEvent(AgentEvent.ADDED_BEHAVIOUR, targetName, bid, myContainer.getID());
				tn.addedBehaviour(ev);
				ev = new AgentEvent(AgentEvent.CHANGED_BEHAVIOUR_STATE, targetName, bid, Behaviour.STATE_READY, Behaviour.STATE_BLOCKED, myContainer.getID());
				tn.changedBehaviourState(ev);
			}
			
			Iterator itMessages = messages.iterator();
			while(itMessages.hasNext()) {
				ACLMessage msg = (ACLMessage)itMessages.next();
				MessageEvent ev = new MessageEvent(MessageEvent.POSTED_MESSAGE, msg, null, targetName, myContainer.getID());
				tn.postedMessage(ev);
			}
		}
		
		private void debugOff(AID introspectorName, AID targetName) throws IMTPException {
			ToolNotifier tn = findNotifier(introspectorName);
			if(tn != null) { 
				tn.removeObservedAgent(targetName);
			}
			
			boolean resetGenerateBehaviourEvents = true;
			synchronized (debuggers) {
				List l = (List) debuggers.get(targetName);
				if (l != null) {
					l.remove(introspectorName);
					if (l.size() > 0) {
						// There is still at least 1 debugger debugging the agent 
						// Do not stop generation of behaviour events
						resetGenerateBehaviourEvents = false;
					}
					else {
						debuggers.remove(targetName);
					}
				}
			}
			
			if (resetGenerateBehaviourEvents) {
				Agent a = myContainer.acquireLocalAgent(targetName);
				if (a != null) {
					a.setGenerateBehaviourEvents(false);
				}
				myContainer.releaseLocalAgent(targetName);
			}
		}
	} // End of ServiceComponent class
	
	
	/**
	 * Inner class NotificationHelperImpl
	 */
	private class NotificationHelperImpl implements NotificationHelper {
		public void init(Agent a) {
		}
		
		public void registerMessageListener(MessageListener ml) {
			List l = messageListeners.startModifying();
			l.add(ml);
			messageListeners.stopModifying();
		}
		
		public void deregisterMessageListener(MessageListener ml) {
			List l = messageListeners.startModifying();
			l.remove(ml);
			messageListeners.stopModifying();    
		}
		
		public void registerAgentListener(AgentListener al) {
			List l = agentListeners.startModifying();
			l.add(al);
			agentListeners.stopModifying();    
		}
		
		public void deregisterAgentListener(AgentListener al) {
			List l = agentListeners.startModifying();
			l.remove(al);
			agentListeners.stopModifying();    
		}
		
		public void registerContainerListener(ContainerListener cl) {
			List l = containerListeners.startModifying();
			l.add(cl);
			containerListeners.stopModifying();    
		}
		
		public void deregisterContainerListener(ContainerListener cl) {
			List l = containerListeners.startModifying();
			l.remove(cl);
			containerListeners.stopModifying();    
		}
	} // END of inner class NotificationHelperImpl
	
	/////////////////////////////////////
	// Event dispatching methods
	/////////////////////////////////////
	private void fireSentMessage(ACLMessage msg, AID sender, AID receiver) {
		// NOTE: A normal synchronized block could create deadlock problems
		// as it prevents concurrent scannings of the listeners list.
		List l = messageListeners.startScanning();
		if (l != null) {
			MessageEvent ev = new MessageEvent(MessageEvent.SENT_MESSAGE, msg, sender, receiver, myID());
			Iterator it = l.iterator();
			while (it.hasNext()) {
				MessageListener ml = (MessageListener) it.next();
				ml.sentMessage(ev);
			}
			messageListeners.stopScanning();
		}	
	}
	
	private void firePostedMessage(ACLMessage msg, AID receiver) {
		// Set the sender explicitly only if different than that included in the message
		String realSenderName = msg.getUserDefinedParameter(ACLMessage.REAL_SENDER);
		AID sender = null;
		if (realSenderName != null) {
			sender = new AID(realSenderName, AID.ISGUID);
		}
		// NOTE: A normal synchronized block could create deadlock problems
		// as it prevents concurrent scannings of the listeners list.
		List l = messageListeners.startScanning();
		if (l != null) {
			MessageEvent ev = new MessageEvent(MessageEvent.POSTED_MESSAGE, msg, sender, receiver, myID());
			Iterator it = l.iterator();
			while (it.hasNext()) {
				MessageListener ml = (MessageListener) it.next();
				ml.postedMessage(ev);
			}
			messageListeners.stopScanning();
		}
	}
	
	private void fireReceivedMessage(ACLMessage msg, AID receiver) {
		// Set the sender explicitly only if different than that included in the message
		String realSenderName = msg.getUserDefinedParameter(ACLMessage.REAL_SENDER);
		AID sender = null;
		if (realSenderName != null) {
			sender = new AID(realSenderName, AID.ISGUID);
		}
		// NOTE: A normal synchronized block could create deadlock problems
		// as it prevents concurrent scannings of the listeners list.
		List l = messageListeners.startScanning();
		if (l != null) {
			MessageEvent ev = new MessageEvent(MessageEvent.RECEIVED_MESSAGE, msg, sender, receiver, myID());
			Iterator it = l.iterator();
			while (it.hasNext()) {
				MessageListener ml = (MessageListener) it.next();
				ml.receivedMessage(ev);
			}
			messageListeners.stopScanning();
		}
	}
	
	private void fireRoutedMessage(ACLMessage msg, Channel from, Channel to) {
		// NOTE: A normal synchronized block could create deadlock problems
		// as it prevents concurrent scannings of the listeners list.
		List l = messageListeners.startScanning();
		if (l != null) {
			MessageEvent ev = new MessageEvent(MessageEvent.ROUTED_MESSAGE, msg, from, to, myID());
			Iterator it = l.iterator();
			while (it.hasNext()) {
				MessageListener ml = (MessageListener) it.next();
				ml.routedMessage(ev);
			}
			messageListeners.stopScanning();
		}
	}
	
	private void fireAddedBehaviour(AID agentID, Behaviour b) {
		// NOTE: A normal synchronized block could create deadlock problems
		// as it prevents concurrent scannings of the listeners list.
		List l = agentListeners.startScanning();
		if (l != null) {
			AgentEvent ev = null;
			if (b == b.root()) {
				// The behaviour has been added to the Agent
				ev = new AgentEvent(AgentEvent.ADDED_BEHAVIOUR, agentID, new BehaviourID(b), myID());
			}
			else {
				// The behaviour is actually a new child that has been added to a CompositeBehaviour
				//FIXME: TO be done
				//ev = new AgentEvent(AgentEvent.ADDED_SUB_BEHAVIOUR, agentID, new BehaviourID(b.getParent()), new BehaviourID(b), myID());
			}
			
			Iterator it = l.iterator();
			while (it.hasNext()) {
				AgentListener al = (AgentListener) it.next();
				al.addedBehaviour(ev);
			}
			agentListeners.stopScanning();
		}
	}
	
	private void fireRemovedBehaviour(AID agentID, Behaviour b) {
		// NOTE: A normal synchronized block could create deadlock problems
		// as it prevents concurrent scannings of the listeners list.
		List l = agentListeners.startScanning();
		if (l != null) {
			AgentEvent ev = new AgentEvent(AgentEvent.REMOVED_BEHAVIOUR, agentID, new BehaviourID(b), myID());
			Iterator it = l.iterator();
			while (it.hasNext()) {
				AgentListener al = (AgentListener) it.next();
				al.removedBehaviour(ev);
			}
			agentListeners.stopScanning();
		}
	}
	
	private void fireChangedBehaviourState(AID agentID, Behaviour b, String from, String to) {
		// NOTE: A normal synchronized block could create deadlock problems
		// as it prevents concurrent scannings of the listeners list.
		List l = agentListeners.startScanning();
		if (l != null) {
			AgentEvent ev = new AgentEvent(AgentEvent.CHANGED_BEHAVIOUR_STATE, agentID, new BehaviourID(b), from, to, myID());
			Iterator it = l.iterator();
			while (it.hasNext()) {
				AgentListener al = (AgentListener) it.next();
				al.changedBehaviourState(ev);
			}
			agentListeners.stopScanning();
		}
	}
	
	private void fireChangedAgentPrincipal(AID agentID, JADEPrincipal from, JADEPrincipal to) {
		// NOTE: A normal synchronized block could create deadlock problems
		// as it prevents concurrent scannings of the listeners list.
		List l = agentListeners.startScanning();
		if (l != null) {
			AgentEvent ev = new AgentEvent(AgentEvent.CHANGED_AGENT_PRINCIPAL, agentID, from, to, myID());
			Iterator it = l.iterator();
			while (it.hasNext()) {
				AgentListener al = (AgentListener) it.next();
				al.changedAgentPrincipal(ev);
			}
			agentListeners.stopScanning();
		}
	}
	
	private void fireChangedAgentState(AID agentID, AgentState from, AgentState to) {
		// NOTE: A normal synchronized block could create deadlock problems
		// as it prevents concurrent scannings of the listeners list.
		List l = agentListeners.startScanning();
		if (l != null) {
			AgentEvent ev = new AgentEvent(AgentEvent.CHANGED_AGENT_STATE, agentID, from, to, myID());
			Iterator it = l.iterator();
			while (it.hasNext()) {
				AgentListener al = (AgentListener) it.next();
				al.changedAgentState(ev);
			}
			agentListeners.stopScanning();
		}
	}
	
	private void fireBornAgent(AID agentID) {
		// NOTE: A normal synchronized block could create deadlock problems
		// as it prevents concurrent scannings of the listeners list.
		List l = containerListeners.startScanning();
		if (l != null) {
			ContainerEvent ev = new ContainerEvent(ContainerEvent.BORN_AGENT, agentID, myID());
			Iterator it = l.iterator();
			while (it.hasNext()) {
				ContainerListener cl = (ContainerListener) it.next();
				cl.bornAgent(ev);
			}
			containerListeners.stopScanning();
		}
	}
	
	private void fireDeadAgent(AID agentID) {
		// NOTE: A normal synchronized block could create deadlock problems
		// as it prevents concurrent scannings of the listeners list.
		List l = containerListeners.startScanning();
		if (l != null) {
			ContainerEvent ev = new ContainerEvent(ContainerEvent.DEAD_AGENT, agentID, myID());
			Iterator it = l.iterator();
			while (it.hasNext()) {
				ContainerListener cl = (ContainerListener) it.next();
				cl.deadAgent(ev);
			}
			containerListeners.stopScanning();
		}
	}
	
	private void fireReattached() {
		// NOTE: A normal synchronized block could create deadlock problems
		// as it prevents concurrent scannings of the listeners list.
		List l = containerListeners.startScanning();
		if (l != null) {
			ContainerEvent ev = new ContainerEvent(ContainerEvent.REATTACHED, null, myID());
			Iterator it = l.iterator();
			while (it.hasNext()) {
				ContainerListener cl = (ContainerListener) it.next();
				cl.reattached(ev);
			}
			containerListeners.stopScanning();
		}
	}
	
	private void fireReconnected() {
		// NOTE: A normal synchronized block could create deadlock problems
		// as it prevents concurrent scannings of the listeners list.
		List l = containerListeners.startScanning();
		if (l != null) {
			ContainerEvent ev = new ContainerEvent(ContainerEvent.RECONNECTED, null, myID());
			Iterator it = l.iterator();
			while (it.hasNext()) {
				ContainerListener cl = (ContainerListener) it.next();
				cl.reconnected(ev);
			}
			containerListeners.stopScanning();
		}
	}
	
	private void fireLeadershipAcquired() {
		// NOTE: A normal synchronized block could create deadlock problems
		// as it prevents concurrent scannings of the listeners list.
		List l = containerListeners.startScanning();
		if (l != null) {
			ContainerEvent ev = new ContainerEvent(ContainerEvent.LEADERSHIP_ACQUIRED, null, myID());
			Iterator it = l.iterator();
			while (it.hasNext()) {
				ContainerListener cl = (ContainerListener) it.next();
				cl.leadershipAcquired(ev);
			}
			containerListeners.stopScanning();
		}
	}
	
	
	////////////////////////////
	// Utility methods
	////////////////////////////
	private ToolNotifier findNotifier(AID observerName) {
		ToolNotifier tn = null;
		// Note that if a ToolNotifier exists it must be among the messageListeners
		// --> There is no need to search it also among the agentListeners.
		List l = messageListeners.startScanning();
		if (l != null) {
			Iterator it = l.iterator();
			while(it.hasNext()) {
				Object obj = it.next();
				if(obj instanceof ToolNotifier) {
					ToolNotifier tni = (ToolNotifier) obj;
					AID id = tni.getObserver();
					if(id.equals(observerName)) {
						tn = tni;
						break;
					}
				}
			}
			messageListeners.stopScanning();
		}
		
		// Redundant check: this condition may happen at platform shutdown
		if(tn != null && tn.getState() == Agent.AP_DELETED) { // A formerly dead notifier
			helper.deregisterMessageListener(tn);
			helper.deregisterAgentListener(tn);
			tn = null;
		}
		return tn;
	}
	
	private ContainerID myID() {
		return (ContainerID) myContainer.here();
	}	
}
