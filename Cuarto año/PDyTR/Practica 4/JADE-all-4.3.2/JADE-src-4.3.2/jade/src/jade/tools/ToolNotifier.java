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

package jade.tools;


//#APIDOC_EXCLUDE_FILE

import java.util.Set;
import java.util.Map;
import java.util.HashSet;

import jade.core.AID;
import jade.core.Agent;
import jade.core.AgentState;

import jade.core.behaviours.*;

import jade.core.event.JADEEvent;
import jade.core.event.MessageEvent;
import jade.core.event.MessageListener;
import jade.core.event.AgentEvent;
import jade.core.event.AgentListener;
import jade.core.event.NotificationHelper;
import jade.core.event.NotificationService;

import jade.domain.FIPANames;
import jade.domain.introspection.*;
import jade.domain.FIPAAgentManagement.Envelope;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.StringACLCodec;

import jade.util.leap.List;
import jade.util.leap.Iterator;
import jade.util.leap.ArrayList;
import jade.util.leap.HashMap;
import jade.util.Logger;

import jade.tools.ToolAgent;

import jade.content.onto.basic.*;


/*
 @author Giovanni Rimassa -  Universita' di Parma
 @author Giovanni Caire -  TILAB
 */
public class ToolNotifier extends ToolAgent implements MessageListener, AgentListener {
	private static final int IDLE_STATE = 0;
	private static final int ACTIVE_STATE = 1;
	private static final int TERMINATING_STATE = 2;
	
	private AID observerAgent;
	private Set observedAgents = new HashSet();
	private HashMap pendingEvents = new HashMap();
	private SequentialBehaviour AMSSubscribe = new SequentialBehaviour();
	
	private volatile int state = IDLE_STATE;
	
	private Logger myLogger;
	
	/**
	 Inner class NotifierAMSListenerBehaviour
	 */
	class NotifierAMSListenerBehaviour extends AMSListenerBehaviour {
		protected void installHandlers(Map handlersTable) {
			// Fill the event handler table.
			handlersTable.put(IntrospectionOntology.DEADAGENT, new EventHandler() {
				public void handle(Event ev) {
					DeadAgent da = (DeadAgent)ev;
					AID dead = da.getAgent();
					removeObservedAgent(dead);
					if (dead.equals(observerAgent)) {
						suicide();
					}
				}
			});
			
			handlersTable.put(IntrospectionOntology.MOVEDAGENT, new EventHandler() {
				public void handle(Event ev) {
					MovedAgent ma = (MovedAgent)ev;
					AID moved = ma.getAgent();
					if (!here().equals(ma.getTo())) {
						removeObservedAgent(moved);
					}
				}
			});
			
		} // END of installHandlers() method
		
	} // END of inner class NotifierAMSListenerBehaviour
	
	
	
	public ToolNotifier(AID id) {
		observerAgent = id;
	}
	
	
	protected void toolSetup() {
		state = ACTIVE_STATE;
		
		myLogger = Logger.getMyLogger(getName());
		
		// Send 'subscribe' message to the AMS
		AMSSubscribe.addSubBehaviour(new SenderBehaviour(this, getSubscribe()));
		
		// Handle incoming 'inform' messages from the AMS
		AMSSubscribe.addSubBehaviour(new NotifierAMSListenerBehaviour());
		addBehaviour(AMSSubscribe);		
	}
	
	protected void toolTakeDown() {
		state = TERMINATING_STATE;
		send(getCancel());
		// If there are still threads waiting for some JADE event to be processed
		// wake up them
		notifyAllPendingEvents();
	}
	
	public void addObservedAgent(AID id) {
		if  (!id.equals(getAID())) {
			observedAgents.add(id);
			// Notify the Observer that we will notify it about events related to agent id
			informStartNotify(id);
		}
		else {
			// Observing myself would generate an endless loop
			myLogger.log(Logger.WARNING, "ToolNotifier "+getName()+": Cannot observe myself!");
		}
	}
	
	public void removeObservedAgent(AID id) {
		observedAgents.remove(id);
		notifyPendingEvents(id);
		if (isEmpty()) {
			suicide();
		}
	}
	
	public AID getObserver() {
		return observerAgent;
	}
	
	public boolean isEmpty() {
		return observedAgents.isEmpty();
	}
	
	private void suicide() {
		try {
			NotificationHelper helper = (NotificationHelper) getHelper(NotificationService.NAME);
			helper.deregisterMessageListener(this);
			helper.deregisterAgentListener(this);
		}
		catch (Exception e) {
			// Should never happen
			e.printStackTrace();
		}
		doDelete();
	}
	
	/////////////////////////////////////
	// MessageListener Interface
	/////////////////////////////////////
	public void sentMessage(MessageEvent ev) {
		if (state == ACTIVE_STATE) {
			AID sender = ev.getSender();
			AID receiver = ev.getReceiver();
			if(observedAgents.contains(sender)) {
				ACLMessage msg = ev.getMessage();
				
				jade.domain.introspection.ACLMessage m = new jade.domain.introspection.ACLMessage();
				// Note that we need to clone the Envelope otherwise we would 
				// overwrite the acl representation (if any) previously set in the
				// Envelope
				Envelope env = msg.getEnvelope();
				if (env != null) {
					m.setEnvelope((Envelope) msg.getEnvelope().clone());
				}
				m.setAclRepresentation(StringACLCodec.NAME);
				m.setPayload(msg.toString());
				
				SentMessage sm = new SentMessage();
				sm.setSender(sender);
				sm.setReceiver(receiver);
				sm.setMessage(m);
				
				sendEvent(sm, null);
			}
		}
	}
	
	public void postedMessage(MessageEvent ev) {
		if (state == ACTIVE_STATE) {
			AID sender = ev.getSender();
			AID receiver = ev.getReceiver();
			if(observedAgents.contains(receiver)) {
				ACLMessage msg = ev.getMessage();
				
				jade.domain.introspection.ACLMessage m = new jade.domain.introspection.ACLMessage();
				Object env = msg.getEnvelope();
				if(env != null) {
					m.setEnvelope(msg.getEnvelope());
					m.setAclRepresentation(StringACLCodec.NAME);
				}
				
				m.setPayload(msg.toString());
				
				PostedMessage pm = new PostedMessage();
				pm.setSender(sender);
				pm.setReceiver(receiver);
				pm.setMessage(m);
				
				sendEvent(pm, null);
			}
		}		
	}
	
	public void receivedMessage(MessageEvent ev) {
		if (state == ACTIVE_STATE) {
			AID sender = ev.getSender();
			AID receiver = ev.getReceiver();
			if(observedAgents.contains(receiver)) {
				ACLMessage msg = ev.getMessage();
				
				jade.domain.introspection.ACLMessage m = new jade.domain.introspection.ACLMessage();
				Object env = msg.getEnvelope();
				if(env != null) {
					m.setEnvelope(msg.getEnvelope());
					m.setAclRepresentation(StringACLCodec.NAME);
				}
				
				m.setPayload(msg.toString());
				
				ReceivedMessage rm = new ReceivedMessage();
				rm.setSender(sender);
				rm.setReceiver(receiver);
				rm.setMessage(m);
				
				sendEvent(rm, null);
			}
		}
	}
	
	public void routedMessage(MessageEvent ev) {
		// No tool is interested in this type of event --> Do nothing
	}
	
	/////////////////////////////////////
	// AgentListener Interface
	/////////////////////////////////////
	public void changedAgentState(AgentEvent ev) {
		if (state == ACTIVE_STATE) {
			AID id = ev.getAgent();
			if(observedAgents.contains(id)) {
				
				AgentState from = ev.getFrom();
				AgentState to = ev.getTo();
				
				ChangedAgentState cas = new ChangedAgentState();
				cas.setAgent(id);
				cas.setFrom(from);
				cas.setTo(to);
				
				sendEvent(cas, null);
			}
		}
	}
	
	public void addedBehaviour(AgentEvent ev) {
		if (state == ACTIVE_STATE) {
			AID id = ev.getAgent();
			if(observedAgents.contains(id)) {
				AddedBehaviour ab = new AddedBehaviour();
				ab.setAgent(id);
				ab.setBehaviour(ev.getBehaviour());
				
				sendEvent(ab, null);
			}
		}
	}
	
	public void removedBehaviour(AgentEvent ev) {
		if (state == ACTIVE_STATE) {
			AID id = ev.getAgent();
			if(observedAgents.contains(id))
			{
				RemovedBehaviour rb = new RemovedBehaviour();
				rb.setAgent(id);
				rb.setBehaviour(ev.getBehaviour());
				
				sendEvent(rb, null);
			}
		}
	}
	
	public void changedBehaviourState(AgentEvent ev) {
		if (state == ACTIVE_STATE) {
			AID id = ev.getAgent();
			if(observedAgents.contains(id)) {
				ChangedBehaviourState cs = new ChangedBehaviourState();
				cs.setAgent(id);
				cs.setBehaviour(ev.getBehaviour());
				cs.setFrom(ev.getBehaviourFrom());
				cs.setTo(ev.getBehaviourTo());
				
				if (ev.getBehaviourTo().equals(Behaviour.STATE_RUNNING) && ev.getBehaviour().isSimple()) {
					// This event requires synchronous handling. As it may have already
					// been processed by other listeners reset its processed status
					ev.reset();
					addPendingEvent(ev, id);
					addBehaviour(new SynchEventInformer(this, cs, ev));
					try {
						ev.waitUntilProcessed();
					}
					catch (InterruptedException ie) {
						// This is the thread of the observed agent. If it has been interrupted
						// the agent is exiting or moving --> just do nothing
					}
					return;
				}
				else {
					sendEvent(cs, null);
				}
			}
		}
	}
	
	
	public void changedAgentPrincipal(AgentEvent ev) {
		// No tool is interested in this type of event --> Do nothing
	}
	
	
	private final ACLMessage createObserverMessage() {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.addReceiver(observerAgent);
		msg.setOntology(IntrospectionOntology.NAME);
		msg.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);
		return msg;
	}
	
	private void informStartNotify(final AID id) {
		if (state == ACTIVE_STATE) {
			StartNotify sn = new StartNotify();
			sn.setObserved(id);
			
			Action a = new Action();
			a.setAction(sn);
			a.setActor(getAID());
			Done d = new Done();
			d.setAction(a);
			ACLMessage msg = createObserverMessage();
			try {
				getContentManager().fillContent(msg, d);
				msg.setConversationId(observerAgent.getName() + "-control");
				send(msg);
			} catch(Exception fe) {
				fe.printStackTrace();
			}
		}
		else {
			// If we are not active yet we can't use the ContentManager --> Do the operation in a behaviour
			addBehaviour(new OneShotBehaviour(this) {
				public void action() {
					informStartNotify(id);
				}
			});
		}
	}
		
	private void sendEvent(Event ev, String replyWith) {
		if (state == ACTIVE_STATE) {
			EventRecord er = new EventRecord(ev, here());
			Occurred o = new Occurred();
			o.setWhat(er);
			ACLMessage msg = createObserverMessage();
			try {
				getContentManager().fillContent(msg, o);
				msg.setConversationId(observerAgent.getName() + "-event");
				if (replyWith != null) {
					msg.setReplyWith(replyWith);
				}
				else {
					// We do not expect any reply
					msg.addUserDefinedParameter(ACLMessage.IGNORE_FAILURE, "true");
				}
				send(msg);
			} catch(Exception fe) {
				fe.printStackTrace();
			}
		}
	}
	
		
	/**
	 Inner class SynchEventInformer.
	 When the observation of an event must be synchronous (i.e. the
	 thread that generated the event must block until the observer
	 has finished observing the event) we must
	 - inform the observer
	 - wait for the observer to send back a proper indication
	 - wake up the thread that generated the event
	 */
	private class SynchEventInformer extends SimpleBehaviour {
		private Event ev;
		private JADEEvent jev;
		private boolean finished = false;
		private MessageTemplate template;
		
		SynchEventInformer(Agent a, Event ev, JADEEvent jev) {
			super(a);
			this.ev = ev;
			this.jev = jev;
		}

		public void onStart() {
			String replyWith = String.valueOf(jev.hashCode());
			sendEvent(ev, replyWith);
			template = MessageTemplate.MatchInReplyTo(replyWith);
		}
		
		public void action() {
			ACLMessage msg = myAgent.receive(template);
			if (msg != null) {
				jev.notifyProcessed(null);
				removePendingEvent(jev);
				finished = true;
			}
			else {
				block();
			}
		}
		
		public boolean done() {
			return finished;
		}
	}
	
	
	//////////////////////////////////////////////
	// Utility methods dealing with pending events
	private void addPendingEvent(JADEEvent ev, AID id) {
		synchronized (pendingEvents) {
			List l = (List) pendingEvents.get(id);
			if (l == null) {
				l = new ArrayList();
				pendingEvents.put(id, l);
			}
			l.add(ev);
		}
	}
	
	private void removePendingEvent(JADEEvent ev) {
		synchronized (pendingEvents) {
			AID id = null;
			if (ev instanceof AgentEvent) {
				id = ((AgentEvent) ev).getAgent();
			}
			else if (ev instanceof MessageEvent) {
				id = ((MessageEvent) ev).getAgent();
			}
			List l = (List) pendingEvents.get(id);
			if (l != null) {
				l.remove(ev);
				if (l.isEmpty()) {
					pendingEvents.remove(id);
				}
			}
		}
	}
	
	private void notifyPendingEvents(AID id) {
		synchronized (pendingEvents) {
			List l = (List) pendingEvents.remove(id);
			if (l != null) {
				Iterator it = l.iterator();
				while (it.hasNext()) {
					JADEEvent ev = (JADEEvent) it.next();
					ev.notifyProcessed(null);
				}
			}
		}
	}
	
	private void notifyAllPendingEvents() {
		synchronized (pendingEvents) {
			Iterator it1 = pendingEvents.values().iterator();
			while (it1.hasNext()) {
				List l = (List) it1.next();
				Iterator it2 = l.iterator();
				while (it2.hasNext()) {
					JADEEvent ev = (JADEEvent) it2.next();
					ev.notifyProcessed(null);
				}
			}
		}
	}
}
