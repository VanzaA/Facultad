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

import jade.proto.*;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;

import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.FailureException;

import jade.proto.states.*;

import jade.domain.FIPANames;

import java.util.Date;

/**
 * Behaviour class for <code>fipa-contract-net</code>
 * <em>Responder</em> role. This  behaviour implements the
 * <code>fipa-contract-net</code> interaction protocol from the point
 * of view of a responder to a call for proposal (<code>cfp</code>)
 * message.<p>
 * The API of this class is similar and homogeneous to the 
 * <code>AchieveREResponder</code>. 
 * <p>
 * Read also the introduction to
 * <a href="ContractNetInitiator.html">ContractNetInitiator</a>
 * for a description of the protocol.
 * <p>
 * When a message arrives
 * that matches the message template passed to the constructor,
 * the callback method <code>prepareResponse</code> is executed
 * that must return the wished response, for instance the <code>PROPOSE</code>
 * reply message. Any other type of returned communicative act 
 * is sent and then closes the
 * protocol.
 * <p>
 * Then, if the initiator accepted the proposal, i.e. if 
 * an <code>ACCEPT-PROPOSAL</code> message was received, the callback
 * method <code>prepareResultNotification</code> would be executed that
 * must return the message with the result notification, i.e. 
 * <code>INFORM</code> or <code>FAILURE</code>.
 * <br>
 * In alternative, if the initiator rejected the proposal, i.e. if 
 * an <code>REJECT-PROPOSAL</code> message was received, the callback
 * method <code>handleRejectProposal</code> would be executed and
 * the protocol terminated. 
 * <p>
 * If a message were received, with the same value of this 
 * <code>conversation-id</code>, but that does not comply with the FIPA 
 * protocol, than the method <code>handleOutOfSequence</code> would be called.
 * <p>
 * This class can be extended by the programmer by overriding all the needed
 * handle methods or, in alternative, appropriate behaviours can be
 * registered for each handle via the <code>registerHandle</code>-type
 * of methods. This last case is more difficult to use and proper
 * care must be taken to properly use the <code>DataStore</code> of the
 * <code>Behaviour</code> as a shared memory mechanism with the
 * registered behaviour.
 * <p>
 * Read carefully the section of the 
 * <a href="..\..\..\programmersguide.pdf"> JADE programmer's guide </a>
 * that describes
 * the usage of this class.
 * @see jade.proto.ContractNetInitiator
 * @see jade.proto.AchieveREResponder
 * 
 * @author Fabio Bellifemine - TILAB
 * @author Giovanni Caire - TILAB
 * @author Marco Monticone - TILAB
 * @version $Date: 2006-05-25 15:29:42 +0200 (gio, 25 mag 2006) $ $Revision: 5884 $
 */

public class ContractNetResponder extends SSContractNetResponder {
	/**
	 @deprecated Use <code>REPLY_KEY</code>
	 */
	public final String RESPONSE_KEY = REPLY_KEY;
	/** 
	 @deprecated Use either <code>ACCEPT_PROPOSAL_KEY</code> or 
	 <code>REJECT_PROPOSAL_KEY</code> according to the message 
	 that has been received
	 */             
	public final String PROPOSE_ACCEPTANCE_KEY = RECEIVED_KEY;
	/** 
	 @deprecated Use <code>REPLY_KEY</code>
	 */
	public final String RESULT_NOTIFICATION_KEY = REPLY_KEY;
	
	public static final String RECEIVE_CFP = "Receive-Cfp";
	
	/**
	 * Constructor of the behaviour that creates a new empty DataStore
	 * @see #ContractNetResponder(Agent a, MessageTemplate mt, DataStore store) 
	 **/
	public ContractNetResponder(Agent a,MessageTemplate mt) {
		this(a,mt, new DataStore());
	}
	
	/**
	 * Constructor of the behaviour.
	 * @param a is the reference to the Agent object
	 * @param mt is the MessageTemplate that must be used to match
	 * the initiator message. Take care that 
	 * if mt is null every message is consumed by this protocol.
	 * The best practice is to have a MessageTemplate that matches
	 * the protocol slot; the static method <code>createMessageTemplate</code>
	 * might be usefull. 
	 * @param store the DataStore for this protocol behaviour
	 **/
	public ContractNetResponder(Agent a,MessageTemplate mt,DataStore store) {
		super(a, null, store);
		
		Behaviour b = null;
		
		// RECEIVE_CFP
		b = new MsgReceiver(myAgent, mt, -1, getDataStore(), CFP_KEY);
		registerFirstState(b, RECEIVE_CFP);
		
		// The DUMMY_FINAL state must no longer be final
		b = deregisterState(DUMMY_FINAL);
		registerDSState(b, DUMMY_FINAL);
		
		registerDefaultTransition(RECEIVE_CFP, HANDLE_CFP);
		registerDefaultTransition(DUMMY_FINAL, RECEIVE_CFP);
	}
	
	/**
	 @deprecated Use <code>handleCfp()</code> instead
	 */
	protected ACLMessage prepareResponse(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
		return null;
	}
	
	/**
	 @deprecated Use <code>handleAcceptProposal()</code> instead.
	 */
	protected ACLMessage prepareResultNotification(ACLMessage cfp, ACLMessage propose,ACLMessage accept ) throws FailureException {
		return null;
	}	
	
	/**
	 @deprecated Use <code>registerHandleCfp()</code> instead.
	 */
	public void registerPrepareResponse(Behaviour b) {
		registerHandleCfp(b);
	}
	
	/**
	 @deprecated Use <code>registerHandleAcceptProposal()</code> instead.
	 */
	public void registerPrepareResultNotification(Behaviour b) {
		registerHandleAcceptProposal(b);
	}
	
	
	//#APIDOC_EXCLUDE_BEGIN
	/**
	 Redefine this method to call prepareResponse()
	 */
	protected ACLMessage handleCfp(ACLMessage cfp) throws RefuseException, FailureException, NotUnderstoodException {
		return prepareResponse(cfp);
	}
	
	/**
	 Redefine this method to call prepareResultNotification()
	 */
	protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
		return prepareResultNotification(cfp, propose, accept);
	}
	
	/**
	 Redefine this method so that the HANDLE_CFP state is not registered
	 as first state
	 */
	public void registerHandleCfp(Behaviour b) {
		registerDSState(b, HANDLE_CFP);
	}
	
	protected void sessionTerminated() {
		// Once the current session is terminated reinit the 
		// internal state to handle the next one
		reinit();
		
		// Be sure all children can be correctly re-executed
		resetChildren();
	}
	//#APIDOC_EXCLUDE_END
	
	
	/**
	 This static method can be used 
	 to set the proper message Template (based on the interaction protocol 
	 and the performative) to be passed to the constructor of this behaviour.
	 @see jade.domain.FIPANames.InteractionProtocol
	 */
	public static MessageTemplate createMessageTemplate(String iprotocol){
		if(CaseInsensitiveString.equalsIgnoreCase(FIPANames.InteractionProtocol.FIPA_ITERATED_CONTRACT_NET,iprotocol)) {
			return MessageTemplate.and(MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_ITERATED_CONTRACT_NET),MessageTemplate.MatchPerformative(ACLMessage.CFP));
		}
		else if(CaseInsensitiveString.equalsIgnoreCase(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET,iprotocol)) {
			return MessageTemplate.and(MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),MessageTemplate.MatchPerformative(ACLMessage.CFP));
		}
		else {
			return MessageTemplate.MatchProtocol(iprotocol);
		}
	}  
}
