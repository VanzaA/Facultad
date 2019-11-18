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
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;

/**
 Single Session version of the Responder role in the Fipa-Contract-Net
 protocol.
 
 @author Giovanni Caire - TILAB
 */
public class SSContractNetResponder extends SSResponder {
	/**
	 Key to retrieve from the DataStore of the behaviour the last received
	 CFP ACLMessage
	 */
	public final String CFP_KEY	= INITIATION_KEY;
	/**
	 Key to retrieve from the DataStore of the behaviour the last sent
	 PROPOSE ACLMessage
	 */
	public final String PROPOSE_KEY = REPLY_KEY;
	/**
	 Key to retrieve from the DataStore of the behaviour the last received
	 ACCEPT_PROPOSAL ACLMessage
	 */
	public final String ACCEPT_PROPOSAL_KEY = RECEIVED_KEY;
	/**
	 Key to retrieve from the DataStore of the behaviour the last received
	 REJECT_PROPOSAL ACLMessage
	 */
	public final String REJECT_PROPOSAL_KEY = RECEIVED_KEY;
	
	public static final String HANDLE_CFP = "Handle-Cfp";
	public static final String HANDLE_ACCEPT_PROPOSAL = "Handle-Accept-Proposal";
	public static final String HANDLE_REJECT_PROPOSAL = "Handle-Reject-Proposal";
	
	private boolean proposeSent = false;
	
	/**
	 Construct a SSContractNetResponder that is activated 
	 by the reception of a given initiation CFP message.
	 */
	public SSContractNetResponder(Agent a, ACLMessage cfp) {
		this(a, cfp, new DataStore());
	}
	
	/**
	 Construct a SSContractNetResponder that is activated 
	 by the reception of a given initiation CFP message and uses 
	 a given DataStore.
	 */
	public SSContractNetResponder(Agent a, ACLMessage cfp, DataStore store) {
		super(a, cfp, store, true);
		
		registerDefaultTransition(HANDLE_CFP, SEND_REPLY);
		registerTransition(SEND_REPLY, RECEIVE_NEXT, ACLMessage.PROPOSE);
		registerTransition(RECEIVE_NEXT, HANDLE_REJECT_PROPOSAL, MsgReceiver.TIMEOUT_EXPIRED); 
		registerTransition(CHECK_IN_SEQ, HANDLE_ACCEPT_PROPOSAL, ACLMessage.ACCEPT_PROPOSAL, new String[]{SEND_REPLY}); 
		registerTransition(CHECK_IN_SEQ, HANDLE_REJECT_PROPOSAL, ACLMessage.REJECT_PROPOSAL); 
		registerDefaultTransition(HANDLE_ACCEPT_PROPOSAL, SEND_REPLY);
		registerDefaultTransition(HANDLE_REJECT_PROPOSAL, DUMMY_FINAL);
		
		Behaviour b;
		
		// HANDLE_CFP
		b = new CfpHandler(myAgent);
		registerFirstState(b, HANDLE_CFP);
		b.setDataStore(getDataStore());
		
		// HANDLE_ACCEPT_PROPOSAL
		b = new AcceptHandler(myAgent);
		registerDSState(b, HANDLE_ACCEPT_PROPOSAL);
		
		// HANDLE_REJECT_PROPOSAL
		b = new RejectHandler(myAgent);
		registerDSState(b, HANDLE_REJECT_PROPOSAL);
	}
	
	
	/**
	 This method is called to handle the initial CFP message.
	 This default implementation does nothing and returns null.
	 Programmers have to override it to react to this event.
	 @param  cfp the initial CFP message to handle.
	 @return the reply message to be sent back to the initiator. Returning 
	 a message different than PROPOSE (or returning null) terminates the protocol. 
	 @exception RefuseException if the CFP is refused. Throwing a 
	 RefuseException has the same effect as returning a REFUSE message, 
	 but automatically manages the <code>:content</code> slot.
	 @exception FailureException if there is an error serving the CFP.
	 Throwing a FailureException has the same effect as returning a FAILURE
	 message, but automatically manages the <code>:content</code> slot.
	 @exception NotUnderstoodException if the CFP content is not understood.
	 Throwing a NotUnderstoodException has the same effect as returning a NOT_UNDERSTOOD
	 message, but automatically manages the <code>:content</code> slot.
	 */
	protected ACLMessage handleCfp(ACLMessage cfp) throws RefuseException, FailureException, NotUnderstoodException {
		return null;
	}
	
	/**
	 This method is called when an ACCEPT_PROPOSAL message is received from the 
	 initiator.
	 This default implementation does nothing and returns null.
	 Programmers have to override it to react to this event.
	 @param cfp the initial CFP message.
	 @param propose the PROPOSE message sent back as reply to the initial 
	 CFP message.
	 @param accept the received ACCEPT_PROPOSAL message.
	 @return the reply message to be sent back to the initiator. 
	 @exception FailureException if there is an error serving the ACCEPT_PROPOSAL.
	 Throwing a FailureException has the same effect as returning a FAILURE
	 message, but automatically manages the <code>:content</code> slot.
	 */
	protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
		return null;
	}
	
	/**
	 This method is called when a REJECT_PROPOSAL message is received from the 
	 initiator.
	 This default implementation does nothing.
	 Programmers may override it to react to this event.
	 @param cfp the initial CFP message.
	 @param propose the PROPOSE message sent back as reply to the initial 
	 CFP message.
	 @param reject the received REJECT_PROPOSAL message or null if no 
	 acceptance message is received from the initiator within the timeout
	 specified in the <code>:reply-by</code> slot of the PROPOSE message.
	 */
	protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
	}
	
	/**
	 This method is called whenever a message is received that does
	 not comply to the protocol rules.
	 This default implementation does nothing.
	 Programmers may override it in case they need to react to this event.
	 @param cfp the initial CFP message.
	 @param propose the PROPOSE message sent back as reply to the initial 
	 CFP message.
	 @param  msg the received out-of-sequence message.
	 */
	protected void handleOutOfSequence(ACLMessage cfp,ACLMessage propose,ACLMessage msg){
	}
	
	/**
	 This method allows to register a user defined <code>Behaviour</code>
	 in the HANDLE_CFP state.
	 This behaviour would override the homonymous method.
	 This method also sets the 
	 data store of the registered <code>Behaviour</code> to the
	 DataStore of this current behaviour.
	 <br>
	 The registered behaviour can retrieve the initial <code>CFP</code> 
	 message from the datastore at the <code>CFP_KEY</code> key.
	 <br>
	 It is responsibility of the registered behaviour to put the
	 reply to be sent back to the initiator into the datastore at the 
	 <code>REPLY_KEY</code> key. Putting a message defferent from PROPOSE 
	 (or putting no message) terminates the protocol. 
	 @param b the Behaviour that will handle this state
	 */
	public void registerHandleCfp(Behaviour b) {
		registerFirstState(b, HANDLE_CFP);
		b.setDataStore(getDataStore());
	}
	
	/**
	 This method allows to register a user defined <code>Behaviour</code>
	 in the HANDLE_ACCEPT_PROPOSAL state.
	 This behaviour would override the homonymous method.
	 This method also sets the 
	 data store of the registered <code>Behaviour</code> to the
	 DataStore of this current behaviour.
	 <br>
	 The registered behaviour can retrieve the received <code>ACCEPT_PROPOSAL</code> 
	 message from the datastore at the <code>ACCEPT_PROPOSAL_KEY</code> 
	 key, the initial <code>CFP</code> message at the <code>CFP_KEY</code>
	 and the previously sent <code>PROPOSE</code> message at the 
	 <code>PROPOSE_KEY</code>.
	 <br>
	 It is responsibility of the registered behaviour to put the
	 reply to be sent back to the initiator into the datastore at the 
	 <code>REPLY_KEY</code> key. 
	 @param b the Behaviour that will handle this state
	 */
	public void registerHandleAcceptProposal(Behaviour b) {
		registerDSState(b, HANDLE_ACCEPT_PROPOSAL);
	}
	
	/**
	 This method allows to register a user defined <code>Behaviour</code>
	 in the HANDLE_REJECT_PROPOSAL state.
	 This behaviour would override the homonymous method.
	 This method also sets the 
	 data store of the registered <code>Behaviour</code> to the
	 DataStore of this current behaviour.
	 <br>
	 The registered behaviour can retrieve the received <code>REJECT_PROPOSAL</code> 
	 message from the datastore at the <code>REJECT_PROPOSAL_KEY</code> 
	 key, the initial <code>CFP</code> message at the <code>CFP_KEY</code>
	 and the previously sent <code>PROPOSE</code> message at the 
	 <code>PROPOSE_KEY</code>.
	 @param b the Behaviour that will handle this state
	 */
	public void registerHandleRejectProposal(Behaviour b) {
		registerDSState(b, HANDLE_REJECT_PROPOSAL);
	}
	
	/**
	 Re-initialize the internal state without performing a complete reset.
	 */
	protected void reinit() {
		proposeSent = false;
		getDataStore().remove(CFP_KEY);
		super.reinit();
	}
	
	
	//#APIDOC_EXCLUDE_BEGIN
	/**
	 Redefine this method to call the overloaded version with 3 parameters.
	 */
	protected void handleOutOfSequence(ACLMessage msg) {
		ACLMessage cfp = (ACLMessage) getDataStore().get(CFP_KEY);
		ACLMessage propose = (ACLMessage) getDataStore().get(PROPOSE_KEY);
		handleOutOfSequence(cfp, propose, msg);
	}
	
	protected boolean checkInSequence(ACLMessage received) {
		return received.getPerformative() == ACLMessage.ACCEPT_PROPOSAL || received.getPerformative() == ACLMessage.REJECT_PROPOSAL;
	}
	
	protected void beforeReply(ACLMessage reply) {
		if (proposeSent) {
			// If this is the reply to an ACCEPT_PROPOSAL force the 
			// protocol termination to avoid (in case the user erroneously
			// sent back another PROPOSE message) ending up in a MsgReceiver
			// state that will never exit.
			forceTransitionTo(DUMMY_FINAL);
		}
	}
	
	protected void afterReply(ACLMessage reply) {
		if (reply != null && reply.getPerformative() == ACLMessage.PROPOSE) {
			proposeSent = true;
		}
	}
	//#APIDOC_EXCLUDE_END
	
	
	/**
	 Inner class CfpHandler
	 */
	private static class CfpHandler extends OneShotBehaviour {
		private static final long     serialVersionUID = 4766407563773001L;
		
		public CfpHandler(Agent a) {
			super(a);
		}
		
		public void action() {
			SSContractNetResponder parent = (SSContractNetResponder) getParent();
			ACLMessage reply = null;
			try {
				reply = parent.handleCfp((ACLMessage) getDataStore().get(parent.CFP_KEY));
			}
			catch (FIPAException fe) {
				reply = fe.getACLMessage();
			}
			getDataStore().put(parent.REPLY_KEY, reply);
		}
	} // End of inner class CfpHandler
	
	
	/**
	 Inner class AcceptHandler
	 */
	private static class AcceptHandler extends OneShotBehaviour {
		private static final long     serialVersionUID = 4766407563773002L;
		
		public AcceptHandler(Agent a) {
			super(a);
		}
		
		public void action() {
			SSContractNetResponder parent = (SSContractNetResponder) getParent();
			ACLMessage reply = null;
			try {
				ACLMessage cfp = (ACLMessage) getDataStore().get(parent.CFP_KEY);
				ACLMessage propose = (ACLMessage) getDataStore().get(parent.PROPOSE_KEY);
				ACLMessage accept = (ACLMessage) getDataStore().get(parent.ACCEPT_PROPOSAL_KEY);
				reply = parent.handleAcceptProposal(cfp, propose, accept);
			}
			catch (FIPAException fe) {
				reply = fe.getACLMessage();
			}
			getDataStore().put(parent.REPLY_KEY, reply);
		}
	} // End of inner class AcceptHandler  
	
	
	/**
	 Inner class RejectHandler
	 */
	private static class RejectHandler extends OneShotBehaviour {
		private static final long     serialVersionUID = 4766407563773003L;
		
		public RejectHandler(Agent a) {
			super(a);
		}
		
		public void action() {
			SSContractNetResponder parent = (SSContractNetResponder) getParent();
			ACLMessage cfp = (ACLMessage) getDataStore().get(parent.CFP_KEY);
			ACLMessage propose = (ACLMessage) getDataStore().get(parent.PROPOSE_KEY);
			ACLMessage reject = (ACLMessage) getDataStore().get(parent.REJECT_PROPOSAL_KEY);
			parent.handleRejectProposal(cfp, propose, reject);
		}
	} // End of inner class RejectHandler  
}	


