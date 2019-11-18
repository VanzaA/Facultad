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
import jade.proto.states.MsgReceiver;
import java.util.Date;
import java.util.Vector;
import java.util.Enumeration;
import jade.util.leap.Iterator;
import jade.util.leap.Map;
import jade.util.leap.HashMap;
import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Serializable;

/**
 * This is a single homogeneous and effective implementation of the initiator role in 
 * all the FIPA-Subscribe-like interaction protocols defined by FIPA,
 * that is all those protocols 
 * where the initiator sends a single "subscription" message
 * and receives notifications each time a given condition becomes true. 
 * This implementation works both for 1:1 and 1:N conversations.
 * <p>
 * FIPA has already specified a number of these interaction protocols, like 
 * FIPA-subscribe and FIPA-request-whenever.
 * <p>
 * The structure of these protocols is always the same.
 * The initiator sends a "subscription" message (in general it performs a communicative act).
 * <p>
 * The responder can then reply by sending a <code>not-understood</code>, a
 * <code>refuse</code> or
 * an <code>agree</code> message to communicate that the subscription has been 
 * agreed. This first category
 * of reply messages has been here identified as a "response". Sending 
 * no response is allowed and is equivalent to sending an <code>agree</code>.
 * <p> 
 * Each time the condition indicated within the subscription message becomes true,
 * the responder sends proper "notification" messages to the initiator. 
 * <p>
 * This behaviour terminates if a) neither a response nor a notification has been 
 * received before the timeout set by the <code>reply-by</code> of the
 * subscription message has expired or b) all responders replied with REFUSE
 * or NOT_UNDERSTOOD. Otherwise the behaviour will run forever.
 * <p>
 * NOTE that this implementation is in an experimental state and the API will 
 * possibly change in next versions also taking into account that the FIPA
 * specifications related to subscribe-like protocols are not yet stable.
 * <p>
 * Read carefully the section of the 
 * <a href="..\..\..\programmersguide.pdf"> JADE programmer's guide </a>
 * that describes
 * the usage of this class.
 * <br> One message for every receiver is sent instead of a single
 * message for all the receivers. </i>
 * @author Giovanni Caire - TILab
 **/
public class SubscriptionInitiator extends Initiator {
	
	// Private data store keys (can't be static since if we register another instance of this class as stare of the FSM 
	//using the same data store the new values overrides the old one. 
	/** 
	 *  key to retrieve from the DataStore of the behaviour the subscription ACLMessage 
	 *	object passed in the constructor of the class.
	 **/
	public final String SUBSCRIPTION_KEY = INITIATION_K;
	/** 
	 * key to retrieve from the DataStore of the behaviour the vector of
	 * subscription ACLMessage objects that have been sent.
	 **/
	public final String ALL_SUBSCRIPTIONS_KEY = ALL_INITIATIONS_K;
	/** 
	 * key to retrieve from the DataStore of the behaviour the last
	 * ACLMessage object that has been received (null if the timeout
	 * expired). 
	 **/
	public final String REPLY_KEY = REPLY_K;
	/** 
	 * key to retrieve from the DataStore of the behaviour the vector of
	 * ACLMessage objects that have been received as responses.
	 **/
	public final String ALL_RESPONSES_KEY = "__all-responses" + hashCode();
	
	// FSM states names specific to the Subscription protocol 
	private static final String HANDLE_AGREE = "Handle-agree";
	private static final String HANDLE_REFUSE = "Handle-refuse";
	private static final String HANDLE_INFORM = "Handle-inform";
	private static final String HANDLE_ALL_RESPONSES = "Handle-all-responses";
	private static final String CHECK_AGAIN = "Check-again";
	
	// States exit values
	private static final int ALL_RESPONSES_RECEIVED = 1;
	private static final int TERMINATED = 2;
	
	// If set to true all expected responses have been received
	private boolean allResponsesReceived = false;
	
	/**
	 * Construct a <code>SubscriptionInitiator</code> with an empty DataStore
	 * @see #SubscriptionInitiator(Agent, ACLMessage, DataStore)
	 **/
	public SubscriptionInitiator(Agent a, ACLMessage msg){
		this(a,msg,new DataStore());
	}
	
	/**
	 * Construct a <code>SubscriptionInitiator</code> with a given DataStore
	 * @param a The agent performing the protocol
	 * @param msg The message that must be used to initiate the protocol.
	 * Notice that the default implementation of the 
	 * <code>prepareSubscription()</code>
	 * method returns
	 * an array composed of only this message.
	 * The values of the slot 
	 * <code>reply-with</code> is ignored and a different value is assigned
	 * automatically by this class for each receiver.
	 * @param store The <code>DataStore</code> that will be used by this 
	 * <code>SubscriptionInitiator</code>
	 */
	public SubscriptionInitiator(Agent a, ACLMessage msg, DataStore store) {
		super(a, msg, store);
		
		// Register the FSM transitions specific to the Achieve-RE protocol
		registerTransition(CHECK_IN_SEQ, HANDLE_AGREE, ACLMessage.AGREE);		
		registerTransition(CHECK_IN_SEQ, HANDLE_INFORM, ACLMessage.INFORM);		
		registerTransition(CHECK_IN_SEQ, HANDLE_REFUSE, ACLMessage.REFUSE);		
		registerDefaultTransition(HANDLE_AGREE, CHECK_SESSIONS);
		registerDefaultTransition(HANDLE_INFORM, CHECK_SESSIONS);
		registerDefaultTransition(HANDLE_REFUSE, CHECK_SESSIONS);
		registerTransition(CHECK_SESSIONS, HANDLE_ALL_RESPONSES, ALL_RESPONSES_RECEIVED);
		registerTransition(CHECK_SESSIONS, DUMMY_FINAL, TERMINATED);
		registerDefaultTransition(HANDLE_ALL_RESPONSES, CHECK_AGAIN);
		registerTransition(CHECK_AGAIN, DUMMY_FINAL, 0);
		registerDefaultTransition(CHECK_AGAIN, RECEIVE_REPLY, getToBeReset());
		
		// Create and register the states specific to the Subscription protocol
		Behaviour b = null;
		// HANDLE_AGREE
		b = new OneShotBehaviour(myAgent) {
			private static final long     serialVersionUID = 3487495895820003L;
			
			public void action() {
				handleAgree((ACLMessage) getDataStore().get(REPLY_K));
			}
		};
		b.setDataStore(getDataStore());		
		registerState(b, HANDLE_AGREE);
		
		// HANDLE_REFUSE
		b = new OneShotBehaviour(myAgent) {
			private static final long     serialVersionUID = 3487495895820004L;
			
			public void action() {
				handleRefuse((ACLMessage) getDataStore().get(REPLY_K));
			}
		};
		b.setDataStore(getDataStore());		
		registerState(b, HANDLE_REFUSE);
		
		// HANDLE_INFORM
		b = new OneShotBehaviour(myAgent) {
			private static final long     serialVersionUID = 3487495895820006L;
			
			public void action() {
				handleInform((ACLMessage) getDataStore().get(REPLY_K));
			}
		};
		b.setDataStore(getDataStore());		
		registerState(b, HANDLE_INFORM);
		
		// HANDLE_ALL_RESPONSES
		b = new OneShotBehaviour(myAgent) {
			
			public void action() {
				handleAllResponses((Vector) getDataStore().get(ALL_RESPONSES_KEY));
			}
		};
		b.setDataStore(getDataStore());		
		registerState(b, HANDLE_ALL_RESPONSES);
		
		// CHECK_AGAIN
		b = new OneShotBehaviour(myAgent) {
			public void action() {
			}
			public int onEnd() {
				return sessions.size();
			}
		};
		b.setDataStore(getDataStore());		
		registerState(b, CHECK_AGAIN);
	}
	
	//#APIDOC_EXCLUDE_BEGIN
	/**
	 This method is called internally by the framework and is not intended 
	 to be called by the user
	 */    
	protected Vector prepareInitiations(ACLMessage initiation) {
		return prepareSubscriptions(initiation);
	}
	
	
	/**
	 Check whether a reply is in-sequence and update the appropriate Session.
	 This method is called internally by the framework and is not intended 
	 to be called by the user       
	 */    
	protected boolean checkInSequence(ACLMessage reply) {
		String inReplyTo = reply.getInReplyTo();
		Session s = (Session) sessions.get(inReplyTo);
		if (s != null) {
			int perf = reply.getPerformative();
			if (s.update(perf)) {
				// The reply is compliant to the protocol 
				switch (s.getState()) {
				case Session.POSITIVE_RESPONSE_RECEIVED:
				case Session.NEGATIVE_RESPONSE_RECEIVED:
					// The reply is a response
					Vector allRsp = (Vector) getDataStore().get(ALL_RESPONSES_KEY);
					allRsp.addElement(reply);
					break;
				case Session.NOTIFICATION_RECEIVED:
					// Nothing to do
					break;
				default:
					// Something went wrong. Return false --> we will go to the HANDLE_OUT_OF_SEQ state
					return false;
				}
				// If the session is completed then remove it.
				if (s.isCompleted()) {
					sessions.remove(inReplyTo);
				}
				return true;
			}
		}
		return false;
	}
	
	/**
	 Check the status of the sessions after the reception of the last reply
	 or the expiration of the timeout.
	 This method is called internally by the framework and is not intended 
	 to be called by the user       
	 */    
	protected int checkSessions(ACLMessage reply) {
		int ret = -1;
		if (getLastExitValue() == MsgReceiver.TIMEOUT_EXPIRED) {
			if (!allResponsesReceived) {
				// Special case 1: Timeout has expired
				// Remove all the sessions for which no response has been received yet
				List sessionsToRemove = new ArrayList(sessions.size());
				for (Iterator i=sessions.keySet().iterator(); i.hasNext(); ) {
					Object key = i.next();
					Session s = (Session)sessions.get(key);
					if ( s.getState() == Session.INIT ) {
						sessionsToRemove.add(key);
					}
				}
				for (Iterator i=sessionsToRemove.iterator(); i.hasNext(); ) {
					sessions.remove(i.next());
				}
				sessionsToRemove=null;  //frees memory
			}
			else {
				// Special case 2: All responses have already been received 
				// and an additional timeout (set e.g. through replyReceiver.setDeadline())
				// expired. Remove all sessions
				sessions.clear();
			}
		}
		
		if (!allResponsesReceived) {
			// Check whether all responses have been received (this is the 
			// case when no active session is still in the INIT state).
			allResponsesReceived = true;
			Iterator it = sessions.values().iterator();
			while (it.hasNext()) {
				Session s = (Session) it.next();
				if (s.getState() == Session.INIT) {
					allResponsesReceived = false;
					break;
				}
			}
			if (allResponsesReceived) {
				// Set an infite timeout to the replyReceiver.
				replyReceiver.setDeadline(MsgReceiver.INFINITE);
				ret = ALL_RESPONSES_RECEIVED;
			}
		}
		else {
			// Note that this check must be done only if the HANDLE_ALL_RESPONSES
			// has already been visited.
			if (sessions.size() == 0) {
				// There are no more active sessions --> Terminate
				ret = TERMINATED;
			}
		}
		return ret;
	}
	
	private String[] toBeReset = null;
	
	/**
	 */
	protected String[] getToBeReset() {
		if (toBeReset == null) {
			toBeReset = new String[] {
					HANDLE_AGREE, 
					HANDLE_REFUSE,
					HANDLE_NOT_UNDERSTOOD,
					HANDLE_INFORM,
					HANDLE_FAILURE,
					HANDLE_OUT_OF_SEQ
			};
		}
		return toBeReset;
	}
	//#APIDOC_EXCLUDE_END
	
	/**
	 * This method must return the vector of subscription ACLMessage objects to be
	 * sent. It is called in the first state of this protocol.
	 * This default implementation just returns the ACLMessage object
	 * passed in the constructor. Programmers might prefer to override
	 * this method in order to return a vector of objects for 1:N conversations
	 * or also to prepare the messages during the execution of the behaviour.
	 * @param subscription the ACLMessage object passed in the constructor
	 * @return a Vector of ACLMessage objects.
	 * The values of the slot 
	 * <code>reply-with</code> is ignored and a different value is assigned
	 *  automatically by this class for each receiver.
	 **/    
	protected Vector prepareSubscriptions(ACLMessage subscription) {
		Vector l = new Vector(1);
		l.addElement(subscription);
		return l;
	}
	
	/**
	 * This method is called every time an <code>agree</code>
	 * message is received, which is not out-of-sequence according
	 * to the protocol rules.
	 * This default implementation does nothing; programmers might
	 * wish to override the method in case they need to react to this event.
	 * @param agree the received agree message
	 **/
	protected void handleAgree(ACLMessage agree) {
	}
	
	/**
	 * This method is called every time a <code>refuse</code>
	 * message is received, which is not out-of-sequence according
	 * to the protocol rules.
	 * This default implementation does nothing; programmers might
	 * wish to override the method in case they need to react to this event.
	 * @param refuse the received refuse message
	 **/
	protected void handleRefuse(ACLMessage refuse) {
	}
	
	/**
	 * This method is called every time a <code>inform</code>
	 * message is received, which is not out-of-sequence according
	 * to the protocol rules.
	 * This default implementation does nothing; programmers might
	 * wish to override the method in case they need to react to this event.
	 * @param inform the received inform message
	 **/
	protected void handleInform(ACLMessage inform) {
	}
	
	/**
	 * This method is called when all the responses have been
	 * collected or when the timeout is expired.
	 * The used timeout is the minimum value of the slot <code>replyBy</code> 
	 * of all the sent messages. 
	 * By response message we intend here all the <code>agree, not-understood,
	 * refuse, failure</code> received messages, which are
	 * not out-of-sequence according
	 * to the protocol rules.
	 * This default implementation does nothing; programmers might
	 * wish to override the method in case they need to react to this event
	 * by analysing all the messages in just one call.
	 * @param responses the Vector of ACLMessage objects that have been received 
	 **/
	protected void handleAllResponses(Vector responses) {
	}
	
	/**
	 This method allows to register a user defined <code>Behaviour</code>
	 in the PREPARE_SUBSCRIPTIONS state. 
	 This behaviour would override the homonymous method.
	 This method also sets the 
	 data store of the registered <code>Behaviour</code> to the
	 DataStore of this current behaviour.
	 It is responsibility of the registered behaviour to put the
	 Vector of ACLMessage objects to be sent 
	 into the datastore at the <code>ALL_SUBSCRIPTIONS_KEY</code>
	 key.
	 The values of the slot 
	 <code>reply-with</code> is ignored and a different value is assigned
	 automatically by this class for each receiver.
	 @param b the Behaviour that will handle this state
	 */
	public void registerPrepareSubscriptions(Behaviour b) {
		registerPrepareInitiations(b);
	}
	
	/**
	 This method allows to register a user defined <code>Behaviour</code>
	 in the HANDLE_AGREE state.
	 This behaviour would override the homonymous method.
	 This method also sets the 
	 data store of the registered <code>Behaviour</code> to the
	 DataStore of this current behaviour.
	 The registered behaviour can retrieve
	 the <code>agree</code> ACLMessage object received
	 from the datastore at the <code>REPLY_KEY</code>
	 key.
	 @param b the Behaviour that will handle this state
	 */
	public void registerHandleAgree(Behaviour b) {
		registerState(b, HANDLE_AGREE);
		b.setDataStore(getDataStore());
	}
	
	/**
	 This method allows to register a user defined <code>Behaviour</code>
	 in the HANDLE_INFORM state.
	 This behaviour would override the homonymous method.
	 This method also set the 
	 data store of the registered <code>Behaviour</code> to the
	 DataStore of this current behaviour.
	 The registered behaviour can retrieve
	 the <code>inform</code> ACLMessage object received
	 from the datastore at the <code>REPLY_KEY</code>
	 key.
	 @param b the Behaviour that will handle this state
	 */
	public void registerHandleInform(Behaviour b) {
		registerState(b, HANDLE_INFORM);
		b.setDataStore(getDataStore());
	}
	
	/**
	 This method allows to register a user defined <code>Behaviour</code>
	 in the HANDLE_REFUSE state.
	 This behaviour would override the homonymous method.
	 This method also set the 
	 data store of the registered <code>Behaviour</code> to the
	 DataStore of this current behaviour.
	 The registered behaviour can retrieve
	 the <code>refuse</code> ACLMessage object received
	 from the datastore at the <code>REPLY_KEY</code>
	 key.
	 @param b the Behaviour that will handle this state
	 */
	public void registerHandleRefuse(Behaviour b) {
		registerState(b, HANDLE_REFUSE);
		b.setDataStore(getDataStore());
	}
	
	/**
	 This method allows to register a user defined <code>Behaviour</code>
	 in the HANDLE_ALL_RESPONSES state.
	 This behaviour would override the homonymous method.
	 This method also sets the 
	 data store of the registered <code>Behaviour</code> to the
	 DataStore of this current behaviour.
	 The registered behaviour can retrieve
	 the vector of ACLMessage objects, received as a response,
	 from the datastore at the <code>ALL_RESPONSES_KEY</code>
	 key.
	 @param b the Behaviour that will handle this state
	 */
	public void registerHandleAllResponses(Behaviour b) {
		registerState(b, HANDLE_ALL_RESPONSES);
		b.setDataStore(getDataStore());
	}
	
	/**
	 Cancel the subscription to agent <code>receiver</code>.
	 This method retrieves the subscription message sent to 
	 <code>receiver</code> and sends a suitable CANCEL message with
	 the conversationID and all other protocol fields appropriately set.
	 The <code>:content</code> slot of this CANCEL message is filled
	 in by means of the <code>fillCancelContent()</code>
	 method. The way the CANCEL content is set in fact is application
	 specific.
	 @param receiver The agent to whom we are cancelling the subscription.
	 @param ignoreResponse When receiving a CANCEL, the responder may 
	 send back a response to notify that the subscription has been 
	 cancelled (INFORM) or not (FAILURE). If this parameter is set to 
	 <code>true</code> this response is ignored and the session with
	 agent <code>receiver</code> is immediately terminated. When 
	 <code>ignoreResponse</code> is set to <code>false</code>, on the
	 other hand, the session with agent <code>receiver</code> remains
	 active and the INFORM or FAILURE massage (if any) will be handled by the 
	 <code>HANDLE_INFORM</code> and <code>HANDLE_FAILURE</code> states
	 as if they were normal notifications. It is responsibility of
	 the programmer to distinguish them and actually terminate the 
	 session with agent <code>receiver</code> by calling the 
	 <code>cancellationCompleted()</code> method.
	 @see #fillCancelContent(ACLMessage, ACLMessage)
	 @see #cancellationCompleted(AID)
	 */
	public void cancel(AID receiver, boolean ignoreResponse) {
		ACLMessage subscription = (ACLMessage) getDataStore().get(receiver);
		Session s = (Session) sessions.get(subscription.getReplyWith());
		if (s != null) {
			if (ignoreResponse) {
				sessions.remove(subscription.getReplyWith());
			}
			else {
				s.cancel();
			}
			// If the session was still active, send the CANCEL message
			ACLMessage cancel = new ACLMessage(ACLMessage.CANCEL);
			cancel.addReceiver(receiver);
			cancel.setLanguage(subscription.getLanguage());
			cancel.setOntology(subscription.getOntology());
			cancel.setProtocol(subscription.getProtocol());
			cancel.setConversationId(subscription.getConversationId());
			if (!ignoreResponse) {
				cancel.setReplyWith(subscription.getReplyWith());
			}
			fillCancelContent(subscription, cancel);
			myAgent.send(cancel);
			// Interrupt the ReplyReceiver to check if this SubscriptionInitiator 
			// should terminate
			replyReceiver.interrupt();
		}		
	}
	
	/**
	 This method is used to fill the <code>:content</code> slot
	 of the CANCEL message that is being sent to an agent to cancel 
	 the subscription previously activated by means of the 
	 <code>subscription</code> message. Note that all other relevant
	 fields of the <code>cancel</code> message have already been 
	 set appropriately and the programmer should not modify them.
	 The default implementation just sets a null content (the responder 
	 should be able to identify the subscription that has to be 
	 cancelled on the basis of the sender and conversationID fields
	 of the CANCEL message). Programmers may override this method to 
	 create an appropriate content as exemplified in the code below.
	 
	 <pr><hr><blockquote><pre>
	 try {
	 AID receiver = (AID) cancel.getAllReceiver().next();
	 Action a = new Action(receiver, OntoACLMessage.wrap(subscription));
	 getContentManager.fillContent(cancel, a);
	 }
	 catch (Exception e) {
	 e.printStackTrace();
	 }
	 </pre></blockquote><hr>
	 
	 @see #cancel(AID, boolean)
	 */
	protected void fillCancelContent(ACLMessage subscription, ACLMessage cancel) {
		cancel.setContent(null);
	}
	
	/**
	 This method should be called when the notification of a 
	 successful subscription cancellation is received from agent
	 <code>receiver</code> to terminate the session with him.
	 This method has some effect only if a cancellation for
	 agent <code>receiver</code> was previously activated by 
	 means of the <code>cancel()</code> method.
	 @see #cancel(AID, boolean)
	 */       
	public void cancellationCompleted(AID receiver) {
		ACLMessage subscription = (ACLMessage) getDataStore().get(receiver);
		Session s = (Session) sessions.get(subscription.getReplyWith());
		if (s != null && s.isCancelled()) {
			sessions.remove(subscription.getReplyWith());
			// Interrupt the ReplyReceiver to check if this SubscriptionInitiator 
			// should terminate
			replyReceiver.interrupt();
		}		
	}
	
	/**
	 **/
	protected void reinit(){
		allResponsesReceived = false;
		super.reinit();
	}
	
	
	//#APIDOC_EXCLUDE_BEGIN
	/**
	 Initialize the data store. 
	 This method is called internally by the framework and is not intended 
	 to be called by the user       
	 **/
	protected void initializeDataStore(ACLMessage msg){
		super.initializeDataStore(msg);
		Vector l = new Vector();
		getDataStore().put(ALL_RESPONSES_KEY, l);
	}
	
	
	protected ProtocolSession getSession(ACLMessage msg, int sessionIndex) {
		// Store the subscription message actually sent to a given receiver (note that msg has 1 and only 1 receiver) 
		// so that it is possible to retrieve it later on in the cancellation phase
		getDataStore().put((AID) msg.getAllReceiver().next(), msg);
		return new Session();
	}
	//#APIDOC_EXCLUDE_END
	
	/**
	 Inner class Session
	 */
	private static class Session implements ProtocolSession, Serializable {
		// Session states
		static final int INIT = 0;
		static final int POSITIVE_RESPONSE_RECEIVED = 1;
		static final int NEGATIVE_RESPONSE_RECEIVED = 2;
		static final int NOTIFICATION_RECEIVED = 3;
		
		private int state = INIT;
		private boolean cancelled = false;
		
		public String getId() {
			return null;
		}
		
		/**
		 return true if the received performative is valid with respect to
		 the current session state.
		 */
		public boolean update(int perf) {
			switch (state) {
			case INIT:
				switch (perf) {
				case ACLMessage.AGREE:
					state = POSITIVE_RESPONSE_RECEIVED;
					return true;
				case ACLMessage.REFUSE:
				case ACLMessage.NOT_UNDERSTOOD:
					state = NEGATIVE_RESPONSE_RECEIVED;
					return true;
				case ACLMessage.INFORM:
				case ACLMessage.FAILURE:
					state = NOTIFICATION_RECEIVED;
					return true;
				default:
					return false;
				}
			case POSITIVE_RESPONSE_RECEIVED:
			case NOTIFICATION_RECEIVED:
				switch (perf) {
				case ACLMessage.INFORM:
				case ACLMessage.FAILURE:
					state = NOTIFICATION_RECEIVED;
					return true;
				default:
					return false;
				}
			default:
				return false;
			}
		}
		
		public int getState() {
			return state;
		}
		
		public boolean isCompleted() {
			return (state == NEGATIVE_RESPONSE_RECEIVED);
		}
		
		void cancel() {
			cancelled = true;
		}
		
		boolean isCancelled() {
			return cancelled;
		}
	} // End of inner class Session
}



