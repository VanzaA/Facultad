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
import jade.proto.states.*;
import java.util.Date;

/**
 Common base class for all classes implementing the Single Session 
 version of the Responder
 role in interaction protocols where the responder is expected
 to receive more than one message from the initiator and reply 
 to each of them.
 @author Giovanni Caire - TILAB
 */
abstract class SSResponder extends FSMBehaviour {
	/**
	 Key to retrieve from the DataStore of the behaviour the initiation
	 ACLMessage that triggered this responder session
	 */
	public final String INITIATION_KEY = "__Initiation_key" + hashCode();
	
	/**
	 Key to retrieve from the DataStore of the behaviour the last received
	 ACLMessage
	 */
	public final String RECEIVED_KEY = "__Received_key" + hashCode();
	
	/**
	 Key to set into the DataStore of the behaviour the new ACLMessage 
	 to be sent back to the initiator as a reply.
	 */
	public final String REPLY_KEY = "__Reply_key" + hashCode();
	
	private static final int OUT_OF_SEQUENCE_EXIT_CODE = -98765; // Very strange number
	
	//#APIDOC_EXCLUDE_BEGIN
	// FSM states names
	protected static final String RECEIVE_NEXT = "Receive-Next";
	protected static final String CHECK_IN_SEQ = "Check-In-seq";
	protected static final String HANDLE_OUT_OF_SEQUENCE = "Handle-Out-of-seq";
	protected static final String SEND_REPLY = "Send-Reply";
	protected static final String DUMMY_FINAL = "Dummy-Final";
	
	private ACLMessage initiation;
	private String initiationKey;
	
	
	/**
	 */
	public SSResponder(Agent a, ACLMessage initiation, DataStore store, boolean useInitiationKey) {
		super(a);
		setDataStore(store);
		this.initiation = initiation;
		initiationKey = (useInitiationKey ? INITIATION_KEY : RECEIVED_KEY);
		
		registerDefaultTransition(RECEIVE_NEXT, CHECK_IN_SEQ);
		registerTransition(CHECK_IN_SEQ, HANDLE_OUT_OF_SEQUENCE, OUT_OF_SEQUENCE_EXIT_CODE);
		registerDefaultTransition(HANDLE_OUT_OF_SEQUENCE, RECEIVE_NEXT, new String[] {HANDLE_OUT_OF_SEQUENCE});
		registerDefaultTransition(SEND_REPLY, DUMMY_FINAL);
		
		
		Behaviour b;
		
		// RECEIVE_NEXT 
		b = new NextMsgReceiver(myAgent, getDataStore(), RECEIVED_KEY);
		registerState(b, RECEIVE_NEXT);
		
		// CHECK_IN_SEQ
		b = new SeqChecker(myAgent);
		registerDSState(b, CHECK_IN_SEQ);
		
		// HANDLE_OUT_OF_SEQUENCE
		b = new OutOfSeqHandler(myAgent);
		registerDSState(b, HANDLE_OUT_OF_SEQUENCE);
		
		// SEND_REPLY
		b = new NextReplySender(myAgent, REPLY_KEY, initiationKey);
		registerDSState(b, SEND_REPLY);
		
		// DUMMY_FINAL
		b = new DummyFinal(myAgent);
		registerLastState(b, DUMMY_FINAL);
		b.setDataStore(getDataStore());
	}
	
	public void onStart() {
		getDataStore().put(initiationKey, initiation);
		super.onStart();
	}
	//#APIDOC_EXCLUDE_END
	
	
	/**
	 This method is called whenever a message is received that does
	 not comply to the protocol rules.
	 This default implementation does nothing.
	 Programmers may override it in case they need to react to this event.
	 @param  msg the received out-of-sequence message.
	 */
	protected void handleOutOfSequence(ACLMessage msg) {
	}
	
	/**
	 This method allows to register a user defined <code>Behaviour</code>
	 in the HANDLE_OUT_OF_SEQ state.
	 This behaviour would override the homonymous method.
	 This method also sets the 
	 data store of the registered <code>Behaviour</code> to the
	 DataStore of this current behaviour.
	 The registered behaviour can retrieve
	 the <code>out of sequence</code> ACLMessage object received
	 from the datastore at the <code>RECEIVED_KEY</code>
	 key.
	 @param b the Behaviour that will handle this state
	 */
	public void registerHandleOutOfSequence(Behaviour b) {
		registerDSState(b, HANDLE_OUT_OF_SEQUENCE);
	}
	
	/**
	 Reset this behaviour.
	 */
	public void reset() {
		reinit();
		super.reset();
	}
	
	/**
	 Re-initialize the internal state without performing a complete reset.
	 */
	protected void reinit() {
		DataStore ds = getDataStore();
		ds.remove(RECEIVED_KEY);
		ds.remove(REPLY_KEY);
		
		setMessageToReplyKey(initiationKey);
	}
	
	//#APIDOC_EXCLUDE_BEGIN
	/**
	 Check whether a received message complies with the protocol rules.
	 */
	protected boolean checkInSequence(ACLMessage received) {
		return false;
	}
	
	/**
	 This method can be redefined by protocol specific implementations
	 to customize a reply that is going to be sent back to the initiator.
	 This default implementation does nothing.
	 */
	protected void beforeReply(ACLMessage reply) {
	}
	
	/**
	 This method can be redefined by protocol specific implementations
	 to update the status of the protocol just after a reply has been sent.
	 This default implementation does nothing.
	 */
	protected void afterReply(ACLMessage reply) {
	}
	
	/**
	 This method can be redefined by protocol specific implementations
	 to take proper actions after the completion of the current protocol
	 session.
	 */
	protected void sessionTerminated() {
	}
	
	/**
	 Utility method to register a behaviour in a state of the 
	 protocol and set the DataStore appropriately
	 */
	protected void registerDSState(Behaviour b, String name) {
		b.setDataStore(getDataStore());
		registerState(b,name);
	}
	//#APIDOC_EXCLUDE_END
	
	private void setMessageToReplyKey(String key) {
		ReplySender rs = (ReplySender) getState(SEND_REPLY);
		rs.setMsgKey(key);
	}
	
	
	/**
	 Inner class NextMsgReceiver
	 */
	private static class NextMsgReceiver extends MsgReceiver {
		private static final long     serialVersionUID = 4487495895818001L;
		
		public NextMsgReceiver(Agent a, DataStore ds, String key) {
			super(a, null, INFINITE, ds, key);
		}
		
		public int onEnd() {
			// The next reply (if any) will be a reply to the received message 
			SSResponder parent = (SSResponder) getParent();
			parent.setMessageToReplyKey((String) receivedMsgKey);
			
			return super.onEnd();
		}
	} // End of inner class NextMsgReceiver
	
	
	/**
	 Inner class SeqChecker
	 */
	private static class SeqChecker extends OneShotBehaviour {
		private int ret;
		private static final long     serialVersionUID = 4487495895818002L;
		
		public SeqChecker(Agent a) {
			super(a);
		}
		
		public void action() {
			SSResponder parent = (SSResponder) getParent();
			ACLMessage received = (ACLMessage)getDataStore().get(parent.RECEIVED_KEY);
			if (received != null && parent.checkInSequence(received)) {
				ret = received.getPerformative();
			}
			else {
				ret = OUT_OF_SEQUENCE_EXIT_CODE;
			}
		}
		
		public int onEnd() {
			return ret;
		}
	} // End of inner class SeqChecker
	
	
	/**
	 Inner class OutOfSeqHandler
	 */
	private static class OutOfSeqHandler extends OneShotBehaviour {
		private static final long     serialVersionUID = 4487495895818003L;
		
		public OutOfSeqHandler(Agent a) {
			super(a);
		}
		
		public void action() {
			SSResponder parent = (SSResponder) getParent();
			parent.handleOutOfSequence((ACLMessage)getDataStore().get(parent.RECEIVED_KEY));
		}
	} // End of inner class OutOfSeqHandler
	
	
	/**
	 Inner class NextReplySender
	 */
	private static class NextReplySender extends ReplySender {
		private static final long     serialVersionUID = 4487495895818004L;
		
		public NextReplySender(Agent a, String replyKey, String msgKey) {
			super(a, replyKey, msgKey);
		}
		
		public void onStart() {
			SSResponder parent = (SSResponder) getParent();
			ACLMessage reply = (ACLMessage)getDataStore().get(parent.REPLY_KEY);
			parent.beforeReply(reply);
		}
		
		public int onEnd() {
			int ret = super.onEnd();
			SSResponder parent = (SSResponder) getParent();
			
			// If a reply was sent back, adjust the template and deadline of the 
			// RECEIVE_NEXT state
			ACLMessage reply = (ACLMessage)getDataStore().get(parent.REPLY_KEY);
			if (reply != null) {
				MsgReceiver mr = (MsgReceiver) parent.getState(RECEIVE_NEXT);
				mr.setTemplate(createNextMsgTemplate(reply));
				
				Date d = reply.getReplyByDate();
				if (d != null && d.getTime() > System.currentTimeMillis()) {
					mr.setDeadline(d.getTime());
				}
				else {
					mr.setDeadline(MsgReceiver.INFINITE);
				}
			}
			
			parent.afterReply(reply);
			return ret;
		}
		
		private MessageTemplate createNextMsgTemplate(ACLMessage reply) {
			return MessageTemplate.and(
					MessageTemplate.MatchConversationId(reply.getConversationId()),
					MessageTemplate.not(MessageTemplate.MatchCustom(reply, true)));
		}
	} // End of inner class NextReplySender
	
	
	/**
	 Inner class DummyFinal
	 */
	private static class DummyFinal extends OneShotBehaviour {
		private static final long     serialVersionUID = 4487495895818005L;
		
		public DummyFinal(Agent a) {
			super(a);
		}
		
		public void action() {
			SSResponder parent = (SSResponder) getParent();
			parent.sessionTerminated();
		}
	} // End of inner class DummyFinal  
}
