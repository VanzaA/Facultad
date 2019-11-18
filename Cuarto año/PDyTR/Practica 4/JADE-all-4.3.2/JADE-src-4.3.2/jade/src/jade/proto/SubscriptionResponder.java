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

package jade.proto;

//#CUSTOM_EXCLUDE_FILE

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPANames;
import jade.proto.states.*;
import jade.util.Logger;
import jade.util.leap.*;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;


/**
 * This is a single homogeneous and effective implementation of the responder role in 
 * all the FIPA-Subscribe-like interaction protocols defined by FIPA,
 * that is all those protocols 
 * where the initiator sends a single "subscription" message
 * and receives notifications each time a given condition becomes true. 
 * @see SubscriptionInitiator
 * @author Elisabetta Cortese - TILAB
 * @author Giovanni Caire - TILAB
 */
public class SubscriptionResponder extends FSMBehaviour implements FIPANames.InteractionProtocol {
	
	/** 
	 *  key to retrieve from the DataStore of the behaviour the ACLMessage 
	 *	object sent by the initiator as a subscription.
	 **/
	public final String SUBSCRIPTION_KEY = "__subs_canc" + hashCode();
	/** 
	 *  key to retrieve from the DataStore of the behaviour the ACLMessage 
	 *	object sent by the initiator to cancel a subscription.
	 **/
	public final String CANCEL_KEY = SUBSCRIPTION_KEY;
	/** 
	 *  key to retrieve from the DataStore of the behaviour the ACLMessage 
	 *	object sent as a response to the initiator.
	 **/
	public final String RESPONSE_KEY = "__response" + hashCode();
	
	// FSM states names
	private static final String RECEIVE_SUBSCRIPTION = "Receive-subscription";
	private static final String HANDLE_SUBSCRIPTION = "Handle-subscription";
	private static final String HANDLE_CANCEL = "Handle-cancel";
	private static final String SEND_RESPONSE = "Send-response";
	private static final String SEND_NOTIFICATIONS = "Send-notifications";
	
	// The MsgReceiver behaviour used to receive subscription messages
	private MsgReceiver msgRecBehaviour = null;
	
	private Hashtable subscriptions = new Hashtable();
	private List notifications = new ArrayList();
	
	/**
	 The <code>SubscriptionManager</code> used by this 
	 <code>SubscriptionResponder</code> to register subscriptions
	 */
	protected SubscriptionManager mySubscriptionManager = null;
	
	private Logger myLogger = Logger.getJADELogger(getClass().getName());
	
	/**
	 This static method can be used 
	 to set the proper message Template (based on the performative of the
	 subscription message) into the constructor of this behaviour.
	 @param perf The performative of the subscription message
	 */
	public static MessageTemplate createMessageTemplate(int perf) {
		return MessageTemplate.and(
				MessageTemplate.MatchProtocol(FIPA_SUBSCRIBE),
				MessageTemplate.or(MessageTemplate.MatchPerformative(perf), MessageTemplate.MatchPerformative(ACLMessage.CANCEL)));
	}
	
	/**
	 * Construct a SubscriptionResponder behaviour that handles subscription messages matching a given template. 
	 * @see #SubscriptionResponder(Agent,MessageTemplate,SubscriptionResponder.SubscriptionManager,DataStore)	 
	 **/
	public SubscriptionResponder(Agent a, MessageTemplate mt){
		this(a, mt, null, new DataStore());
	}
	
	/**
	 * Construct a SubscriptionResponder behaviour that handles subscription messages matching a given template and
	 * notifies a given SubscriptionManager about subscription/un-subscription events. 
	 * @see #SubscriptionResponder(Agent,MessageTemplate,SubscriptionResponder.SubscriptionManager,DataStore)	 
	 **/
	public SubscriptionResponder(Agent a, MessageTemplate mt, SubscriptionManager sm){
		this(a, mt, sm, new DataStore());
	}
	
	/**
	 * Construct a SubscriptionResponder behaviour that handles subscription messages matching a given template,
	 * notifies a given SubscriptionManager about subscription/un-subscription events and uses a given DataStore. 
	 * @param a is the reference to the Agent performing this behaviour.
	 * @param mt is the MessageTemplate that must be used to match
	 * subscription messages sent by the initiators. Take care that 
	 * if mt is null every message is consumed by this protocol.
	 * @param sm The <code>SubscriptionManager</code> object that is notified about subscription/un-subscription events
	 * @param store the DataStore that will be used by protocol
	 **/
	public SubscriptionResponder(Agent a, MessageTemplate mt, SubscriptionManager sm, DataStore store) {
		super(a);
		setDataStore(store);
		mySubscriptionManager = sm;
		
		// Register the FSM transitions
		registerDefaultTransition(RECEIVE_SUBSCRIPTION, HANDLE_SUBSCRIPTION);
		registerTransition(RECEIVE_SUBSCRIPTION, HANDLE_CANCEL, ACLMessage.CANCEL);
		registerTransition(RECEIVE_SUBSCRIPTION, SEND_NOTIFICATIONS, MsgReceiver.INTERRUPTED);
		registerDefaultTransition(HANDLE_SUBSCRIPTION, SEND_RESPONSE);
		registerDefaultTransition(HANDLE_CANCEL, SEND_RESPONSE);
		registerDefaultTransition(SEND_RESPONSE, RECEIVE_SUBSCRIPTION, new String[] {HANDLE_SUBSCRIPTION, HANDLE_CANCEL}); 
		registerDefaultTransition(SEND_NOTIFICATIONS, RECEIVE_SUBSCRIPTION); 
		
		//***********************************************
		// For each state create and register a behaviour	
		//***********************************************
		Behaviour b = null;
		
		// RECEIVE_SUBSCRIPTION
		msgRecBehaviour = new MsgReceiver(myAgent, mt, MsgReceiver.INFINITE, getDataStore(), SUBSCRIPTION_KEY);
		registerFirstState(msgRecBehaviour, RECEIVE_SUBSCRIPTION);
		
		// HANDLE_SUBSCRIPTION
		b = new OneShotBehaviour(myAgent) {
			
			public void action() {
				DataStore ds = getDataStore();
				ACLMessage subscription = (ACLMessage) ds.get(SUBSCRIPTION_KEY);
				ACLMessage response = null;
				try {
					response = handleSubscription(subscription); 
				}
				catch (NotUnderstoodException nue) {
					response = nue.getACLMessage();
				}
				catch (RefuseException re) {
					response = re.getACLMessage();
				}
				ds.put(RESPONSE_KEY, response);
			}
		};
		b.setDataStore(getDataStore());		
		registerState(b, HANDLE_SUBSCRIPTION);
		
		// HANDLE_CANCEL 
		b = new OneShotBehaviour(myAgent) {
			public void action() {
				DataStore ds = getDataStore();
				ACLMessage cancel = (ACLMessage) ds.get(CANCEL_KEY);
				ACLMessage response = null;
				try {
					response = handleCancel(cancel); 
				}
				catch (FailureException fe) {
					response = fe.getACLMessage();
				}
				ds.put(RESPONSE_KEY, response);
			}
		};
		b.setDataStore(getDataStore());		
		registerState(b, HANDLE_CANCEL);	
		
		// SEND_RESPONSE 
		b = new ReplySender(myAgent, RESPONSE_KEY, SUBSCRIPTION_KEY);
		b.setDataStore(getDataStore());		
		registerState(b, SEND_RESPONSE);	
		
		// SEND_NOTIFICATIONS 
		b = new OneShotBehaviour(myAgent) {
			public void action() {
				sendNotifications();
			}
		};
		b.setDataStore(getDataStore());		
		registerState(b, SEND_NOTIFICATIONS);	
		
	} // End of Constructor
	
	
	/**
	 Reset this behaviour
	 */
	// FIXME: reset deve resettare anche le sottoscrizioni?
	public void reset() {
		super.reset();
		DataStore ds = getDataStore();
		ds.remove(SUBSCRIPTION_KEY);
		ds.remove(RESPONSE_KEY);
	}
	
	/**
	 This method resets the protocol and allows to change the 
	 <code>MessageTemplate</code>
	 that defines what messages this SubscriptionResponder 
	 will react to.
	 */
	public void reset(MessageTemplate mt) {
		this.reset();
		msgRecBehaviour.reset(mt, MsgReceiver.INFINITE, getDataStore(), SUBSCRIPTION_KEY);
	}
		
	/**   
	 * This method is called when a subscription
	 * message is received that matches the message template
	 * specified in the constructor. 
	 * The default implementation creates an new <code>Subscription</code>
	 * object, stores it internally and notify the <code>SubscriptionManager</code> 
	 * used by this responder if any. Then it returns null which has
	 * the effect of sending no response. Programmers in general do not need
	 * to override this method. In case they need to manage Subscription objects in an application specific
	 * way they should rather use a <code>SubscriptionManager</code> with the <code>register()</code> method properly implemented. 
	 * However they could override it in case they need to react to the reception of a 
	 * subscription message in a different way, e.g. by sending back an AGREE message.
	 * @param subscription the received message
	 * @return the ACLMessage to be sent as a response: typically one of
	 * <code>agree, refuse, not-understood</code> or null if no response must be sent back. 
	 */
	protected ACLMessage handleSubscription(ACLMessage subscription) throws NotUnderstoodException, RefuseException {
		// Call prepareResponse() for backward compatibility
		return prepareResponse(subscription);
	}
	
	/**
	 * @deprecated Use handleSubscription() instead   
	 */
	protected ACLMessage prepareResponse(ACLMessage subscription) throws NotUnderstoodException, RefuseException {
		Subscription subs = createSubscription(subscription);
		if (mySubscriptionManager != null) {
			mySubscriptionManager.register(subs);
		}
		return null;
	}
	
	/**   
	 * This method is called when a CANCEL message is received for a previous subscription. 
	 * The default implementation retrieves the <code>Subscription</code>
	 * object the received cancel message refers to, notifies the
	 * <code>SubscriptionManager</code> used by this responder if any and remove the Subscription from its internal structures. 
	 * Then it returns null which has the effect of sending no response. 
	 * Programmers in general do not need
	 * to override this method. In case they need to manage Subscription objects in an application specific
	 * way they should rather use a <code>SubscriptionManager</code> with the <code>deregister()</code> method properly implemented.
	 * However they could override it in case they need to react to the reception of a 
	 * cancel message in a different way, e.g. by sending back an INFORM.
	 * @param cancel the received CANCEL message
	 * @return the ACLMessage to be sent as a response to the 
	 * cancel operation: typically one of <code>inform</code> and <code>failure</code> or null if no response must be sent back. 
	 */
	protected ACLMessage handleCancel(ACLMessage cancel) throws FailureException {
		Subscription s = getSubscription(cancel);
		if (s != null) {
			if (mySubscriptionManager != null) {
				mySubscriptionManager.deregister(s);
			}
			s.close();
		}
		return null;
	}
	
	/**
	 This method allows to register a user defined <code>Behaviour</code>
	 in the HANDLE_SUBSCRIPTION state.
	 This behaviour overrides the homonymous method.
	 This method also sets the 
	 data store of the registered <code>Behaviour</code> to the
	 DataStore of this current behaviour.
	 It is responsibility of the registered behaviour to put the
	 response (if any) to be sent back into the datastore at the 
	 <code>RESPONSE_KEY</code> key.
	 The incoming subscription message can be retrieved from the 
	 datastore at the <code>SUBSCRIPTION_KEY</code> key
	 @param b the Behaviour that will handle this state
	 */
	public void registerHandleSubscription(Behaviour b) {
		registerState(b, HANDLE_SUBSCRIPTION);
		b.setDataStore(getDataStore());
	}
	
	/**
	 * @deprecated Use registerHandleSubscription() instead.
	 */
	public void registerPrepareResponse(Behaviour b) {
		registerHandleSubscription(b);
	}
	
	/**
	 This method allows to register a user defined <code>Behaviour</code>
	 in the HANDLE_CANCEL state.
	 This behaviour overrides the homonymous method.
	 This method also sets the 
	 data store of the registered <code>Behaviour</code> to the
	 DataStore of this current behaviour.
	 It is responsibility of the registered behaviour to put the
	 response (if any) to be sent back into the datastore at the 
	 <code>RESPONSE_KEY</code> key.
	 The incoming CANCEL message can be retrieved from the 
	 datastore at the <code>CANCEL_KEY</code> key
	 @param b the Behaviour that will handle this state
	 */
	public void registerHandleCancel(Behaviour b) {
		registerState(b, HANDLE_CANCEL);
		b.setDataStore(getDataStore());
	}
	
	/**
	 Utility method to correctly create a new <code>Subscription</code> object 
	 managed by this <code>SubscriptionResponder</code>
	 */
	public Subscription createSubscription(ACLMessage subsMsg) {
		Subscription s = new Subscription(this, subsMsg);
		String convId = subsMsg.getConversationId();
		if (convId != null) {
			Subscription old = (Subscription) subscriptions.put(convId, s);
			if (old != null) {
				myLogger.log(Logger.WARNING, "Agent "+myAgent.getLocalName()+" - Subscription from agent "+old.getMessage().getSender().getLocalName()+" overridden by agent "+subsMsg.getSender().getLocalName());
			}
		}
		return s;
	}
	
	/**
	 Utility method to correctly retrieve the 
	 <code>Subscription</code> object that is related to the conversation
	 message <code>msg</code> belongs to.
	 @param msg The message whose <code>conversation-id</code> indicates the conversation
	 @return the <code>Subscription</code> object related to the conversation the given message belongs to
	 */
	public Subscription getSubscription(ACLMessage msg) {
		String convId = msg.getConversationId();
		return getSubscription(convId);
	}
	
	/**
	 Utility method to correctly retrieve the 
	 <code>Subscription</code> object that is related a given conversation.
	 @param convId The id of the conversation
	 @return the <code>Subscription</code> object related to the given conversation
	 */
	public Subscription getSubscription(String convId) {
		Subscription s = null;
		if (convId != null) {
			s = (Subscription) subscriptions.get(convId);
		}
		return s;
	}
	
	/**
	 * Utility method that retrieves all Subscription-s done by a given agent
	 * @param subscriber The AID of the agent whose subscriptions must be retrieved
	 * @return A <code>Vector</code> including all <code>Subscription</code>-s made by the given agent
	 */
	public Vector getSubscriptions(AID subscriber) {
		// Synchronization is needed to avoid concurrent modification exception in case this method is 
		// invoked from within a separate Thread
		synchronized (subscriptions) {
			Vector ss = new Vector();
			Enumeration en = subscriptions.elements();
			while (en.hasMoreElements()) {
				Subscription s = (Subscription) en.nextElement();
				if (s.getMessage().getSender().equals(subscriber)) {
					ss.addElement(s);
				}
			}
			return ss;
		}
	}
	
	/**
	 * Utility method that retrieves all Subscription-s managed by this <code>SubscriptionResponder</code>
	 * @return A <code>Vector</code> including all <code>Subscription</code>-s managed by this <code>SubscriptionResponder</code>
	 */
	public Vector getSubscriptions() {
		// Synchronization is needed to avoid concurrent modification exception in case this method is 
		// invoked from within a separate Thread
		synchronized (subscriptions) {
			Vector ss = new Vector();
			Enumeration en = subscriptions.elements();
			while (en.hasMoreElements()) {
				Subscription s = (Subscription) en.nextElement();
				ss.addElement(s);
			}
			return ss;
		}
	}
	
	/**
	 This is called by a Subscription object when a notification has
	 to be sent to the corresponding subscribed agent.
	 Executed in mutual exclusion with sendNotifications(). Note that this
	 synchronization is not needed in general, but we never know how users
	 manages Subscription objects (possibly in another thread)
	 */
	private synchronized void addNotification(ACLMessage notification, ACLMessage subscription) {
		ACLMessage[] tmp = new ACLMessage[] {notification, subscription};
		notifications.add(tmp);
		msgRecBehaviour.interrupt();
	}
	
	/**
	 This is called within the SEND_NOTIFICATIONS state.
	 Executed in mutual exclusion with addNotification(). Note that this
	 synchronization is not needed in general, but we never know how users
	 manages Subscription objects (possibly in another thread)
	 */
	private synchronized void sendNotifications() {
		Iterator it = notifications.iterator();
		while (it.hasNext()) {
			boolean receiversNull = true;
			boolean replyWithNull = true;
			ACLMessage[] tmp = (ACLMessage[]) it.next();
			if (tmp[0].getAllReceiver().hasNext()) {
				receiversNull = false;
			}
			if (tmp[0].getReplyWith() != null) {
				replyWithNull = false;
			}
			ReplySender.adjustReply(myAgent, tmp[0], tmp[1]);
			myAgent.send(tmp[0]);
			// If the message was modified --> restore it
			if (receiversNull) {
				tmp[0].clearAllReceiver();
			}
			if (replyWithNull) {
				tmp[0].setReplyWith(null);
			}
		}
		notifications.clear();
	}
	
	/**
	 Inner interface SubscriptionManager.
	 <p>
	 A <code>SubscriptionResponder</code>, besides enforcing and
	 controlling the sequence of messages in a subscription conversation, also stores current subscriptions
	 into an internal table. In many cases however it is desirable to manage Subscription objects in an application specific way 
	 (e.g. storing them to a persistent support such as a DB). To enable that, it is possible to pass a 
	 SubscriptionManager implementation to the SubscriptionResponder. The SubscriptionManager is notified 
	 about subscription and cancellation events by means of the register() and deregister() methods.     
	 <p>
	 */
	public static interface SubscriptionManager {
		/**
		 Register a new Subscription object
		 @param s The Subscription object to be registered
		 @return The boolean value returned by this method provides an 
		 indication to the <code>SubscriptionResponder</code> about whether
		 or not an AGREE message should be sent back to the initiator. The
		 default implementation of the <code>handleSubscription()</code> method
		 of the <code>SubscriptionResponder</code> ignores this indication,
		 but programmers can override it.
		 */
		boolean register(Subscription s) throws RefuseException, NotUnderstoodException;
		/**
		 Deregister a Subscription object
		 @return The boolean value returned by this method provides an 
		 indication to the <code>SubscriptionResponder</code> about whether
		 or not an INFORM message should be sent back to the initiator. The
		 default implementation of the <code>handleCancel()</code> method
		 of the <code>SubscriptionResponder</code> ignores this indication,
		 but programmers can override it.
		 */
		boolean deregister(Subscription s) throws FailureException;
	} // END of inner interface SubscriptionManager

	
	/**
	 Inner calss Subscription
	 <p>
	 This class represents a subscription. When a notification has to 
	 be sent to a subscribed agent the notification message should not 
	 be directly sent to the subscribed agent, but should be passed to the
	 <code>Subscription</code> object representing the subscription of that 
	 agent by means of its <code>notify()</code> method. This automatically 
	 handles sequencing and protocol fields appropriately.
	 <code>Subscription</code> objects must be created by means of the
	 <code>createSubscription()</code> method.
	 */
	public static class Subscription {
		
		private ACLMessage subscription;
		private SubscriptionResponder myResponder;
		
		/**
		 Private constructor. The <code>createSubscription()</code>
		 must be used instead.
		 @param r The <code>SubscriptionResponder</code> that received
		 the subscription message corresponding to this 
		 <code>Subscription</code>
		 @param s The subscription message corresponding to this 
		 <code>Subscription</code>
		 */
		private Subscription(SubscriptionResponder r, ACLMessage s){
			myResponder = r;
			subscription = s;
		}
		
		/**
		 Retrieve the ACL message with which this
		 subscription object was created.
		 @return the subscription message corresponding to this 
		 <code>Subscription</code>
		 */
		public ACLMessage getMessage() {
			return subscription;
		}
		
		/** 
		 This method allows sending back a notification message to the subscribed 
		 agent associated to this <code>Subscription</code> object. The user 
		 should call this method, instead of directly using the <code>send()</code>
		 method of the <code>Agent</code> class, as it automatically 
		 handles sequencing and protocol fields appropriately.
		 */			   
		public void notify(ACLMessage notification){
			myResponder.addNotification(notification, subscription);
		}
		
		/** 
		 This method removes the current Subscription object from the SubscriptionResponder internal tables.
		 */			   
		public void close(){
			String convId = subscription.getConversationId();
			if (convId != null) {
				myResponder.subscriptions.remove(convId);
			}
		}
		
		public boolean equals(Object obj) {
			if (obj != null && obj instanceof Subscription) {
				// They are equals if they have the same conversation-id
				return subscription.getConversationId().equals(((Subscription) obj).subscription.getConversationId());
			}
			return false;
		}

		public int hashCode() {
			return subscription.getConversationId().hashCode();
		}
	} // END of inner class Subscription
	
}
