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
 Single Session version of the Responder role in the Iterated-Fipa-Request
 protocol.
 
 @author Giovanni Caire - TILAB
 */
public class SSIteratedAchieveREResponder extends SSResponder {
	/**
	 Key to retrieve from the DataStore of the behaviour the last received
	 REQUEST ACLMessage
	 */
	public final String REQUEST_KEY = RECEIVED_KEY;
	/**
	 Key to retrieve from the DataStore of the behaviour the last received
	 CANCEL ACLMessage
	 */
	public final String CANCEL_KEY = RECEIVED_KEY;
	
	public static final String HANDLE_REQUEST = "Handle-Request";
	public static final String HANDLE_CANCEL = "Handle-Cancel";
	
	static final String ACL_USERDEF_TERMINATED_SESSION = "iterated-fipa-request-terminated-session";
	
	private boolean sessionClosed = false;
	private int initiationPerformative;
	
	/**
	 Construct a SSIteratedAchieveREResponder that is activated 
	 by the reception of a given initiation REQUEST message.
	 */
	public SSIteratedAchieveREResponder(Agent a, ACLMessage request) {
		this(a, request, new DataStore());
	}
	
	/**
	 Construct a SSIteratedAchieveREResponder that is activated 
	 by the reception of a given initiation REQUEST message and uses 
	 a given DataStore.
	 */
	public SSIteratedAchieveREResponder(Agent a, ACLMessage request, DataStore store) {
		// 4th parameter is false since in this protocol we treat the initiation message exactly as all subsequent incoming messages
		super(a, request, store, false);
		
		initiationPerformative = request.getPerformative();
		
		registerDefaultTransition(HANDLE_REQUEST, SEND_REPLY);
		registerTransition(SEND_REPLY, RECEIVE_NEXT, ACLMessage.INFORM);
		registerTransition(RECEIVE_NEXT, HANDLE_CANCEL, MsgReceiver.TIMEOUT_EXPIRED); 
		registerTransition(CHECK_IN_SEQ, HANDLE_REQUEST, initiationPerformative, new String[]{HANDLE_REQUEST, SEND_REPLY, RECEIVE_NEXT, CHECK_IN_SEQ}); 
		registerTransition(CHECK_IN_SEQ, HANDLE_CANCEL, ACLMessage.CANCEL); 
		registerDefaultTransition(HANDLE_CANCEL, DUMMY_FINAL);
		
		Behaviour b;
		
		// HANDLE_REQUEST
		b = new RequestHandler(myAgent);
		registerFirstState(b, HANDLE_REQUEST);
		b.setDataStore(getDataStore());
		
		// HANDLE_CANCEL
		b = new CancelHandler(myAgent);
		registerDSState(b, HANDLE_CANCEL);
	}
	
	
	/**
	 This method is called to handle the initial REQUEST message and 
	 then again whenever a REQUEST message is received.
	 This default implementation does nothing and returns null.
	 Programmers have to override it to react to this event.
	 @param  request the REQUEST message to handle.
	 @return the reply message to be sent back to the initiator. Returning 
	 a message defferent from INFORM (or returning null) terminates the protocol. 
	 An optional AGREE message can be sent back to the initiator by
	 calling the <code>sendAgree()</code> method.
	 @exception RefuseException if the REQUEST is refused. Throwing a 
	 RefuseException has the same effect as returning a REFUSE message, 
	 but automatically manages the <code>:content</code> slot.
	 @exception FailureException if there is an error serving the REQUEST.
	 Throwing a FailureException has the same effect as returning a FAILURE
	 message, but automatically manages the <code>:content</code> slot.
	 @exception NotUnderstoodException if the REQUEST content is not undrerstood.
	 Throwing a NotUnderstoodException has the same effect as returning a NOT_UNDERSTOOD
	 message, but automatically manages the <code>:content</code> slot.
	 */
	protected ACLMessage handleRequest(ACLMessage request) throws RefuseException, FailureException, NotUnderstoodException {
		return null;
	}
	
	/**
	 This method is called when a CANCEL message is received from the 
	 initiator.
	 This default implementation does nothing.
	 Programmers may override it to react to this event.
	 @param cancel the received CANCEL message or null if no 
	 further REQUEST message is received from the initiator within the timeout
	 specified in the <code>:reply-by</code> slot of the last INFORM message.
	 */
	protected void handleCancel(ACLMessage cancel) {
	}
	
	/**
	 This method allows to register a user defined <code>Behaviour</code>
	 in the HANDLE_REQUEST state.
	 This behaviour would override the homonymous method.
	 This method also sets the 
	 data store of the registered <code>Behaviour</code> to the
	 DataStore of this current behaviour.
	 <br>
	 The registered behaviour can retrieve the received <code>REQUEST</code> 
	 message from the datastore at the <code>REQUEST_KEY</code> key.
	 <br>
	 It is responsibility of the registered behaviour to put the
	 reply to be sent back to the initiator into the datastore at the 
	 <code>REPLY_KEY</code> key. Putting a message defferent from INFORM 
	 (or null) terminates the protocol. 
	 An optional AGREE message can be sent back to the initiator by
	 calling the <code>sendAgree()</code> method.
	 @param b the Behaviour that will handle this state
	 */
	public void registerHandleRequest(Behaviour b) {
		registerFirstState(b, HANDLE_REQUEST);
		b.setDataStore(getDataStore());
	}
	
	/**
	 This method allows to register a user defined <code>Behaviour</code>
	 in the HANDLE_CANCEL state.
	 This behaviour would override the homonymous method.
	 This method also sets the 
	 data store of the registered <code>Behaviour</code> to the
	 DataStore of this current behaviour.
	 <br>
	 The registered behaviour can retrieve the <code>CANCEL</code> 
	 message received from the datastore at the <code>CANCEL_KEY</code> key.
	 <br>
	 @param b the Behaviour that will handle this state
	 */
	public void registerHandleCancel(Behaviour b) {
		registerDSState(b, HANDLE_CANCEL);
	}
	
	/**
	 Utility method to send an optional AGREE message back to the 
	 initiator ensuring that all protocol fields are properly set.
	 */
	public void sendAgree(ACLMessage agree) {
		agree.setPerformative(ACLMessage.AGREE);
		ReplySender.adjustReply(myAgent, agree, (ACLMessage) getDataStore().get(REQUEST_KEY));
		myAgent.send(agree);
	}
	
	/**
	 Close the ongoing session, as soon as the next INFORM will 
	 be sent back to the initiator without the need for an explicit 
	 CANCEL message. The initiator will be able to detect that the 
	 session has been closed by calling the 
	 <code>isSessionTerminated()</code> method of the 
	 <code>SSIteratedAchieveREInitiator</code> class.
	 */
	public void closeSessionOnNextReply() {
		sessionClosed = true;
	}
	
	/**
	 Reset this protocol behaviour
	 */
	public void reset() {
		sessionClosed = false;
		super.reset();
	}
	
	
	//#APIDOC_EXCLUDE_BEGIN
	protected boolean checkInSequence(ACLMessage received) {
		return received.getPerformative() == initiationPerformative || received.getPerformative() == ACLMessage.CANCEL;
	}
	
	protected void beforeReply(ACLMessage reply) {
		if (sessionClosed && reply != null) {
			// Set the user defined parameter that will allow the
			// initiator to detect that the session has been closed
			reply.addUserDefinedParameter(ACL_USERDEF_TERMINATED_SESSION, String.valueOf(true));
			// Terminate the protocol by forcing a transition to the 
			// DUMMY_FINAL state
			forceTransitionTo(DUMMY_FINAL);
		}
	}
	//#APIDOC_EXCLUDE_END
	
	
	/**
	 Inner class RequestHandler
	 */
	private static class RequestHandler extends OneShotBehaviour {
		private static final long     serialVersionUID = 5463827646358001L;
		
		public RequestHandler(Agent a) {
			super(a);
		}
		
		public void action() {
			SSIteratedAchieveREResponder parent = (SSIteratedAchieveREResponder) getParent();
			ACLMessage reply = null;
			try {
				reply = parent.handleRequest((ACLMessage) getDataStore().get(parent.REQUEST_KEY));
			}
			catch (FIPAException fe) {
				reply = fe.getACLMessage();
			}
			getDataStore().put(parent.REPLY_KEY, reply);
		}
	} // End of inner class RequestHandler
	
	
	/**
	 Inner class CancelHandler
	 */
	private static class CancelHandler extends OneShotBehaviour {
		private static final long     serialVersionUID = 5463827646358002L;
		
		public CancelHandler(Agent a) {
			super(a);
		}
		
		public void action() {
			SSIteratedAchieveREResponder parent = (SSIteratedAchieveREResponder) getParent();
			parent.handleCancel((ACLMessage) getDataStore().get(parent.CANCEL_KEY));
		}
	} // End of inner class CancelHandler  
}	


