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

//#J2ME_EXCLUDE_FILE

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.proto.states.*;
import java.util.Date;

/**
 * Class description
 * @author Elena Quarantotto - TILAB
 * @author Giovanni Caire - TILAB
 */
public class TwoPhResponder extends Responder {
	// FSM states names 
	private static final String RECEIVE_CFP = RECEIVE_INITIATION;
	private static final String HANDLE_CFP = "Handle-Cfp";
	private static final String HANDLE_QUERY_IF = "Handle-Query-If";
	private static final String HANDLE_ACCEPT_PROPOSAL = "Handle-Accept";
	private static final String HANDLE_REJECT_PROPOSAL = "Handle-Reject";
	
	private int phase = 0;
	
	/**
	* Constructor of the behaviour that creates a new empty DataStore
	* @see #TwoPhResponder(Agent a, MessageTemplate mt, DataStore store)
	**/
	public TwoPhResponder(Agent a, MessageTemplate mt) {
	     this(a, mt, new DataStore());
	}
	
	/**
	 * Constructor of the behaviour.
	 * @param a is the reference to the Agent object
	 * @param mt is the MessageTemplate that must be used to match
	 * the initiator message. Take care that if mt is null every message is
	 * consumed by this protocol.
	 * The best practice is to have a MessageTemplate that matches
	 * the protocol slot; the static method <code>createMessageTemplate</code>
	 * might be usefull.
	 * @param store the DataStore for this protocol behaviour
	 **/
	public TwoPhResponder(Agent a, MessageTemplate mt, DataStore store) {
		super(a, mt, store);
		
		registerTransition(CHECK_IN_SEQ, HANDLE_CFP, ACLMessage.CFP);
		registerTransition(CHECK_IN_SEQ, HANDLE_QUERY_IF, ACLMessage.QUERY_IF);
		registerTransition(CHECK_IN_SEQ, HANDLE_ACCEPT_PROPOSAL, ACLMessage.ACCEPT_PROPOSAL);
		registerTransition(CHECK_IN_SEQ, HANDLE_REJECT_PROPOSAL, ACLMessage.REJECT_PROPOSAL);

		registerDefaultTransition(HANDLE_CFP, SEND_REPLY);
		registerDefaultTransition(HANDLE_QUERY_IF, SEND_REPLY);
		registerDefaultTransition(HANDLE_ACCEPT_PROPOSAL, SEND_REPLY);
		registerDefaultTransition(HANDLE_REJECT_PROPOSAL, SEND_REPLY);
		
		registerTransition(SEND_REPLY, RECEIVE_NEXT, ACLMessage.PROPOSE, new String[] {HANDLE_CFP});
		registerTransition(SEND_REPLY, RECEIVE_NEXT, ACLMessage.CONFIRM);
		registerTransition(SEND_REPLY, RECEIVE_CFP, ACLMessage.INFORM);
		registerTransition(SEND_REPLY, RECEIVE_CFP, ACLMessage.DISCONFIRM);
		registerTransition(SEND_REPLY, RECEIVE_CFP, ACLMessage.FAILURE);
		registerTransition(SEND_REPLY, RECEIVE_CFP, ACLMessage.NOT_UNDERSTOOD);		
		
		
		Behaviour b;
        
		// HANDLE_CFP
		b = new OneShotBehaviour(myAgent) {
	  	private static final long     serialVersionUID = 4487495895818001L;
	  	
			public void action() {
			    ACLMessage reply = handleCfp((ACLMessage) getDataStore().get(RECEIVED_KEY));
			    getDataStore().put(REPLY_KEY, reply);
			}
		};
		b.setDataStore(getDataStore());		
		registerState(b, HANDLE_CFP);
	
		// HANDLE_QUERY_IF
		b = new OneShotBehaviour(myAgent) {
	  	private static final long     serialVersionUID = 4487495895818002L;
	  	
			public void action() {
			    ACLMessage reply = handleQueryIf((ACLMessage) getDataStore().get(RECEIVED_KEY));
			    getDataStore().put(REPLY_KEY, reply);
			}
		};
		b.setDataStore(getDataStore());		
		registerState(b, HANDLE_QUERY_IF);
	
		// HANDLE_ACCEPT_PROPOSAL
		b = new OneShotBehaviour(myAgent) {
	  	private static final long     serialVersionUID = 4487495895818003L;
	  	
			public void action() {
			    ACLMessage reply = handleAcceptProposal((ACLMessage) getDataStore().get(RECEIVED_KEY));
			    getDataStore().put(REPLY_KEY, reply);
			}
		};
		b.setDataStore(getDataStore());		
		registerState(b, HANDLE_ACCEPT_PROPOSAL);
	
		// HANDLE_REJECT_PROPOSAL
		b = new OneShotBehaviour(myAgent) {
	  	private static final long     serialVersionUID = 4487495895818004L;
	  	
			public void action() {
			    ACLMessage reply = handleRejectProposal((ACLMessage) getDataStore().get(RECEIVED_KEY));
			    getDataStore().put(REPLY_KEY, reply);
			}
		};
		b.setDataStore(getDataStore());		
		registerState(b, HANDLE_REJECT_PROPOSAL);
  }

  /**
   * This method is called when the initiator's
   * message is received that matches the message template
   * passed in the constructor.
   * This default implementation return null which has
   * the effect of sending no reponse. Programmers should
   * override the method in case they need to react to this event.
   * @param cfp the received message
   * @return the ACLMessage to be sent as a response (i.e. one of
   * <code>PROPOSE, FAILURE</code>. <b>Remind</b> to
   * use the method <code>createReply</code> of the class ACLMessage in order
   * to create a valid reply message
   * @see jade.lang.acl.ACLMessage#createReply()
   **/
  protected ACLMessage handleCfp(ACLMessage cfp) {
      return null;
  }

  /**
  * This method is called after the <code>QUERY-IF</code> has been received.
  * This default implementation return null which has
  * the effect of sending no result notification. Programmers should
  * override the method in case they need to react to this event.
  * @param queryIf the received message
  * @return the ACLMessage to be sent as a result notification (i.e. one of
  * <code>CONFIRM, INFORM, DISCONFIRM</code>. <b>Remind</b> to
  * use the method createReply of the class ACLMessage in order
  * to create a valid reply message
  * @see jade.lang.acl.ACLMessage#createReply()
  **/
  protected ACLMessage handleQueryIf(ACLMessage queryIf) {
      return null;
  }

  /**
  * This method is called after the <code>REJECT-PROPOSAL</code> has been received.
  * This default implementation do nothing.
  * Programmers should override the method in case they need to react to this event.
  * @param reject the received message
  * @return the ACLMessage to be sent as a result notification (i.e. an
  * <code>INFORM</code>. <b>Remind</b> to
  * use the method createReply of the class ACLMessage in order
  * to create a valid reply message
  * @see jade.lang.acl.ACLMessage#createReply()
  **/
  protected ACLMessage handleRejectProposal(ACLMessage reject) {
      return null;
  }

  /**
  * This method is called after the <code>ACCEPT-PROPOSAL</code> has been received.
  * This default implementation return null which has
  * the effect of sending no result notification. Programmers should
  * override the method in case they need to react to this event.
  * @param accept the received message
  * @return the ACLMessage to be sent as a result notification (i.e. an
  * <code>INFORM</code>. <b>Remind</b> to use the method createReply of
  * the class ACLMessage in order to create a valid reply message
  * @see jade.lang.acl.ACLMessage#createReply()
  **/
  protected ACLMessage handleAcceptProposal(ACLMessage accept) {
      return null;
  }

  /**
   * This method allows to register a user defined <code>Behaviour</code>
   * in the PREPARE_PROPOSE state. This behaviour would override the homonymous
   * method. This method also set the data store of the registered
   * <code>Behaviour</code> to the DataStore of this current behaviour.
   * It is responsibility of the registered behaviour to put the response
   * to be sent into the datastore at the <code>PROPOSE_KEY</code> key.
   * @param b the Behaviour that will handle this state
   **/
  public void registerHandleCfp(Behaviour b) {
      registerDSState(b, HANDLE_CFP);
  }

  /**
   * This method allows to register a user defined <code>Behaviour</code>
   * in the HANDLE_QUERY_IF state. This behaviour would override the homonymous
   * method. This method also set the data store of the registered
   * <code>Behaviour</code> to the DataStore of this current behaviour.
   * It is responsibility of the registered behaviour to put the response
   * to be sent into the datastore at the <code>REPLY_KEY</code> key.
   * @param b the Behaviour that will handle this state
   **/
  public void registerHandleQueryIf(Behaviour b) {
     registerDSState(b, HANDLE_QUERY_IF);
  }

  /**
   * This method allows to register a user defined <code>Behaviour</code>
   * in the HANDLE_REJECT state. This behaviour would override the homonymous
   * method. This method also set the data store of the registered
   * <code>Behaviour</code> to the DataStore of this current behaviour.
   * It is responsibility of the registered behaviour to put the response
   * to be sent into the datastore at the <code>REPLY_KEY</code> key.
   * @param b the Behaviour that will handle this state
   **/
  public void registerHandleRejectProposal(Behaviour b) {
      registerDSState(b, HANDLE_REJECT_PROPOSAL);
  }

  /**
   * This method allows to register a user defined <code>Behaviour</code>
   * in the HANDLE_ACCEPTANCE state. This behaviour would override the homonymous
   * method. This method also set the data store of the registered
   * <code>Behaviour</code> to the DataStore of this current behaviour.
   * It is responsibility of the registered behaviour to put the response
   * to be sent into the datastore at the <code>REPLY_KEY</code> key.
   * @param b the Behaviour that will handle this state
   **/
  public void registerHandleAcceptProposal(Behaviour b) {
      registerDSState(b, HANDLE_ACCEPT_PROPOSAL);
  }

  /**todo@ Da rivedere il createMessageTemplate E I COMMENTI!!!! */
  /**  This static method can be used to set the proper message Template
   * (based on the interaction protocol and the performative) to be passed to the constructor of this behaviour.
    *  @see jade.domain.FIPANames.InteractionProtocol
    **/
  public static MessageTemplate createMessageTemplate() {
      return MessageTemplate.and(MessageTemplate.MatchProtocol(TwoPhConstants.JADE_TWO_PHASE_COMMIT),
              MessageTemplate.MatchPerformative(ACLMessage.CFP));
  }

  public void reset() {
		super.reset();
		phase = 0;
  }
  
  //#APIDOC_EXCLUDE_BEGIN
  protected boolean checkInSequence(ACLMessage received) {
  	int perf = received.getPerformative();
  	switch (phase) {
  	case 0:
  		return (perf == ACLMessage.CFP);
  	case 1:
  		return (perf == ACLMessage.CFP || perf == ACLMessage.QUERY_IF || perf == ACLMessage.REJECT_PROPOSAL);
  	case 2:
  		return (perf == ACLMessage.ACCEPT_PROPOSAL || perf == ACLMessage.REJECT_PROPOSAL);
  	}
  	return false;
  }
  
  protected void replySent(int exitValue) {
		switch (exitValue) {
		case ACLMessage.PROPOSE:
			phase = 1;
			break;
		case ACLMessage.CONFIRM:
			phase = 2;
			break;
		case ACLMessage.INFORM:
		case ACLMessage.DISCONFIRM:
		case ACLMessage.FAILURE:
		case ACLMessage.NOT_UNDERSTOOD:
			reset();
			break;
		}
  }
  //#APIDOC_EXCLUDE_END
}
