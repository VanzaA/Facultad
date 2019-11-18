package jade.proto;

import jade.core.Agent;
import jade.core.behaviours.DataStore;
import jade.lang.acl.ACLMessage;

public class SSIteratedContractNetResponder extends SSContractNetResponder {
	
	public SSIteratedContractNetResponder(Agent a, ACLMessage cfp) {
		this(a, cfp, new DataStore());
	}
	
	/**
	 Construct a SSIteratedContractNetResponder that is activated 
	 by the reception of a given initiation CFP message and uses 
	 a given DataStore.
	 */
	public SSIteratedContractNetResponder(Agent a, ACLMessage cfp, DataStore store) {
		super(a, cfp, store);
		
		registerTransition(CHECK_IN_SEQ, HANDLE_CFP, ACLMessage.CFP, new String[]{HANDLE_CFP, SEND_REPLY, RECEIVE_NEXT}); 
	}
	
	protected boolean checkInSequence(ACLMessage received) {
		if (received.getPerformative() == ACLMessage.CFP) {
			// New iteration --> Move the received message to the CFP_KEY and return true
			getDataStore().put(this.CFP_KEY, received);
			return true;
		}
		else {
			return super.checkInSequence(received);
		}
	}
	
	protected void beforeReply(ACLMessage reply) {
		ACLMessage lastReceivedMsg = (ACLMessage)getDataStore().get(RECEIVED_KEY);
		if (lastReceivedMsg != null && lastReceivedMsg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
			// We are sending the reply to the ACCEPT_PROPOSAL --> Jump out and terminate just after sending this reply
			forceTransitionTo(DUMMY_FINAL);
		}
	}
	
	protected void afterReply(ACLMessage reply) {
	}
}
