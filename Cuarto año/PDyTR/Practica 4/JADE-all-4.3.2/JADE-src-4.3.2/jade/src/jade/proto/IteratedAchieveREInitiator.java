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
import java.util.Vector;

/**
 This class implements the initiator role in the iterated version of
 fipa-request like interaction protocols. In the iterated version, having 
 received all the result notifications from the responders, the initiator
 may send further initiation messages.
 The session of such a protocol with a given responder terminates when 
 one of the followings occurs:
 i) The initiator sends an explicit CANCEL message instead of the next 
 initiation message to the responder.
 ii) The responder replies with a negative reply i.e. REFUSE, NOT_UNDERSTOOD 
 or FAILURE 
 ii) The responder attaches a termination flag to an INFORM result notification. 
 That termination flag can be detected using the 
 <code>isSessionTerminated()</code> method.   
 
 @author Giovanni Caire - TILab
 */
public class IteratedAchieveREInitiator extends AchieveREInitiator {
	/** 
	 Key to retrieve from the DataStore of the behaviour the vector of
	 ACLMessage objects that will be sent at next round.
	 */
	public final String ALL_NEXT_REQUESTS_KEY = "__all-next-requests" + hashCode();
	
	public static final String REINIT = "Reinit";
	
	/**
	 Construct an <code>IteratedAchieveREInitiator</code> with an empty DataStore
	 */
	public IteratedAchieveREInitiator(Agent a, ACLMessage msg){
		this(a,msg,new DataStore());
	}
	
	/**
	 Construct an <code>IteratedAchieveREInitiator</code> with a given DataStore
	 @param a The agent performing the protocol
	 @param msg The message that must be used to initiate the protocol.
	 Notice that the default implementation of the 
	 <code>prepareRequest()</code> method returns an array composed of 
	 only this message. The values of the slot 
	 <code>reply-with</code> is ignored and a different value is assigned
	 automatically by this class for each receiver.
	 @param store The <code>DataStore</code> that will be used by this 
	 <code>AchieveREInitiator</code>
	 */
	public IteratedAchieveREInitiator(Agent a, ACLMessage msg, DataStore store) {
		super(a, msg, store);
		
		// The HANDLE_ALL_RESULT_NOTIFICATIONS state must no longer be final
		Behaviour b = deregisterState(HANDLE_ALL_RESULT_NOTIFICATIONS);
		b.setDataStore(getDataStore());
		registerState(b, HANDLE_ALL_RESULT_NOTIFICATIONS);
		
		// REINIT
		b = new OneShotBehaviour(myAgent) {
			public void action() {
				prepareForNextRound();
			}
		};
		b.setDataStore(getDataStore());
		registerState(b, REINIT);
		
		// Register the FSM transitions specific to the Iterated-Achieve-RE protocol
		registerDefaultTransition(HANDLE_ALL_RESULT_NOTIFICATIONS, REINIT);					
		registerDefaultTransition(REINIT, SEND_INITIATIONS);		
	}
	
	//#APIDOC_EXCLUDE_BEGIN
	protected void prepareForNextRound() {
		// Reset local variables, clean data store, reset children and copy the
		// "next-requests" of previous round to the "requests" of the next round.
		Vector v = (Vector) getDataStore().get(ALL_NEXT_REQUESTS_KEY);
		reinit();
		resetChildren();
		initializeDataStore(null);
		getDataStore().put(ALL_REQUESTS_KEY, v);
	}
	
	protected void initializeDataStore(ACLMessage msg) {
		super.initializeDataStore(msg);
		Vector v = new Vector();
		getDataStore().put(ALL_NEXT_REQUESTS_KEY, v);
	}  		
	
	protected ProtocolSession getSession(ACLMessage msg, int sessionIndex) {
		if (msg.getPerformative() == ACLMessage.CANCEL) {
			return null;
		}
		else {
			return super.getSession(msg, sessionIndex);
		}
	}   
	//#APIDOC_EXCLUDE_END
	
	
	/**
	 This method is called every time an <code>inform</code>
	 message is received, which is not out-of-sequence according
	 to the protocol rules.
	 This default implementation does nothing; programmers might
	 wish to override this method in case they need to react to this event.
	 @param inform the received inform message
	 @param nextRequest the Vector of ACLMessage objects to be sent at 
	 next round
	 */
	protected void handleInform(ACLMessage inform, Vector nextRequests) {
	}
	
	/** 
	 This method is redefined to call the proper overloaded method
	 */
	protected final void handleInform(ACLMessage inform) {
		Vector v = (Vector) getDataStore().get(ALL_NEXT_REQUESTS_KEY);
		handleInform(inform, v);  
	}
	
	/**
	 This method is called when all the result notification messages 
	 of the current round have been collected. 
	 By result notification message we intend here all the <code>inform, 
	 failure</code> received messages, which are not out-of-sequence 
	 according to the protocol rules.
	 This default implementation does nothing; programmers might
	 wish to override this method in case they need to react to this event
	 by analysing all the messages in just one call.
	 @param resultNodifications the Vector of ACLMessage object received 
	 @param nextRequest the Vector of ACLMessage objects to be sent at 
	 next round
	 */
	protected void handleAllResultNotifications(Vector resultNotifications, Vector nextRequests) {
	}
	
	/** 
	 This method is redefined to call the proper overloaded method
	 */
	protected final void handleAllResultNotifications(Vector resultNotifications) {
		Vector v = (Vector) getDataStore().get(ALL_NEXT_REQUESTS_KEY);
		handleAllResultNotifications(resultNotifications, v);  
	}
	
	/**
	 This method allows to register a user defined <code>Behaviour</code>
	 in the HANDLE_REFUSE state.
	 This behaviour would override the homonymous method.
	 This method also set the 
	 data store of the registered <code>Behaviour</code> to the
	 DataStore of this current behaviour.
	 The registered behaviour can retrieve the received <code>inform</code> 
	 ACLMessage object from the datastore at the <code>REPLY_KEY</code>
	 key and the Vector of ACLMessage objects to be sent at next round
	 at the <code>ALL_NEXT_REQUESTS_KEY</code>.
	 @param b the Behaviour that will handle this state
	 */
	public void registerHandleInform(Behaviour b) {
		// This is redefined for Javadoc purposes only.
		super.registerHandleInform(b);
	}
	
	/**
	 This method allows to register a user defined <code>Behaviour</code>
	 in the HANDLE_ALL_RESULT_NOTIFICATIONS state.
	 This behaviour would override the homonymous method.
	 This method also set the 
	 data store of the registered <code>Behaviour</code> to the
	 DataStore of this current behaviour.
	 The registered behaviour can retrieve
	 the Vector of ACLMessage objects, received as a result notification,
	 from the datastore at the <code>ALL_RESULT_NOTIFICATIONS_KEY</code>
	 key and the Vector of ACLMessage objects to be sent at next round
	 at the <code>ALL_NEXT_REQUESTS_KEY</code>.
	 @param b the Behaviour that will handle this state
	 */
	public void registerHandleAllResultNotifications(Behaviour b) {
		// Method redefined since HANDLE_ALL_RESULT_NOTIFICATION must not be 
		// registered as a final state
		registerState(b, HANDLE_ALL_RESULT_NOTIFICATIONS);
		b.setDataStore(getDataStore());
	}    
	
	/**
	 * Check if the responder has closed the session just after sending this <code>inform</code> message.
	 */
	public static boolean isSessionTerminated(ACLMessage inform) {
		String terminatedStr = inform.getUserDefinedParameter(SSIteratedAchieveREResponder.ACL_USERDEF_TERMINATED_SESSION);
		return "true".equals(terminatedStr);
	}
}



