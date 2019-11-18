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

import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.DataStore;
import jade.core.CaseInsensitiveString;
import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.ACLMessage;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPANames;
import jade.util.leap.Iterator;

/**
 * This is a single homogeneous and effective implementation of
 * all the FIPA-Request-like interaction protocols defined by FIPA,
 * that is all those protocols where the initiator sends a single message
 * (i.e. it performs a single communicative act) within the scope
 * of an interaction protocol in order to verify if the RE (Rational
 * Effect) of the communicative act has been achieved or not.
 * Note that this is a simple implementation of the <code>AchieveREResponder</code>.
 * that does not  allow to register Behaviour for the Prepare Response 
 * and Prepare Result Notification states of the protocol
 *
 * @see SimpleAchieveREInitiator
 * @see AchieveREInitiator
 * @see AchieveREResponder
 * @author Tiziana Trucco - TILab
 * @version $Date: 2013-05-21 11:40:42 +0200 (mar, 21 mag 2013) $ $Revision: 6673 $
 **/

public class SimpleAchieveREResponder extends SimpleBehaviour implements FIPANames.InteractionProtocol {

	private final static int WAITING_MSG_STATE = 0;
	private final static int PREPARE_RESPONSE_STATE = 1;
	private final static int SEND_RESPONSE_STATE = 2;
	private final static int PREPARE_RES_NOT_STATE = 3;
	private final static int SEND_RESULT_NOTIFICATION_STATE = 4;
	private final static int RESET_STATE = 5;

	/** 
	 * @see AchieveREResponder#REQUEST_KEY
	 **/
	public final String REQUEST_KEY = "_request" + hashCode();

	/** 
	 * @see AchieveREResponder#RESPONSE_KEY
	 **/
	public final String RESPONSE_KEY = "_response" + hashCode();

	/** 
	 * @see AchieveREResponder#RESULT_NOTIFICATION_KEY
	 **/
	public final String RESULT_NOTIFICATION_KEY = "_result-notification" + hashCode();

	private MessageTemplate template;
	private int state = WAITING_MSG_STATE;
	private boolean finished = false;

	/**
       This static method can be used 
       to set the proper message Template (based on the interaction protocol 
       and the performative)
       into the constructor of this behaviour.
       @see FIPANames.InteractionProtocol
	 **/
	public static MessageTemplate createMessageTemplate(String iprotocol){
		return AchieveREResponder.createMessageTemplate(iprotocol);
	}

	/**
	 * Constructor of the behaviour that creates a new empty DataStore
	 **/
	public SimpleAchieveREResponder(Agent a,MessageTemplate mt){
		this(a,mt,new DataStore());
	}

	/**
	 * Constructor.
	 * @param a is the reference to the Agent object
	 * @param mt is the MessageTemplate that must be used to match
	 * the initiator message. Take care that 
	 * if mt is null every message is consumed by this protocol.
	 * @param store the DataStore for this protocol
	 **/
	public SimpleAchieveREResponder(Agent a,MessageTemplate mt, DataStore store){
		super(a);
		setDataStore(store);
		template = mt;
		finished = false;
	}

	//#APIDOC_EXCLUDE_BEGIN

	public final void action(){

		switch(state){
		case WAITING_MSG_STATE:{
			ACLMessage request = myAgent.receive(template);
			if(request != null){
				getDataStore().put(REQUEST_KEY,request);
				state = PREPARE_RESPONSE_STATE;
			}else
				block();
			break;
		}
		case PREPARE_RESPONSE_STATE:{
			DataStore ds = getDataStore();
			ACLMessage request = (ACLMessage)ds.get(REQUEST_KEY);
			ACLMessage response = null;
			state = SEND_RESPONSE_STATE;
			try{
				response = prepareResponse(request);
			}catch(NotUnderstoodException nue){
				response = request.createReply();
				response.setContent(nue.getMessage());
				response.setPerformative(ACLMessage.NOT_UNDERSTOOD);
			}catch(RefuseException re){
				response = request.createReply();
				response.setContent(re.getMessage());
				response.setPerformative(ACLMessage.REFUSE);
			}
			ds.put(RESPONSE_KEY,response);
			break;
		}
		case SEND_RESPONSE_STATE:{
			DataStore ds = getDataStore();
			ACLMessage response = (ACLMessage)ds.get(RESPONSE_KEY);
			if(response != null){
				ACLMessage receivedMsg = (ACLMessage)ds.get(REQUEST_KEY);

				response = arrangeMessage(receivedMsg,response);
				myAgent.send(response);
				if(response.getPerformative() == ACLMessage.AGREE)
					state = PREPARE_RES_NOT_STATE;
				else 
					state = RESET_STATE;

			}else{
				//could directly send a resultNotification message.
				state = PREPARE_RES_NOT_STATE;
			}
			break;
		}
		case PREPARE_RES_NOT_STATE:{
			state = SEND_RESULT_NOTIFICATION_STATE;
			DataStore ds = getDataStore();
			ACLMessage request = (ACLMessage)ds.get(REQUEST_KEY);
			ACLMessage response = (ACLMessage)ds.get(RESPONSE_KEY);
			ACLMessage resNotification = null;
			try{
				resNotification = prepareResultNotification(request,response);
			}catch(FailureException fe){
				resNotification = request.createReply();
				resNotification.setContent(fe.getMessage());
				resNotification.setPerformative(ACLMessage.FAILURE);
			}
			ds.put(RESULT_NOTIFICATION_KEY,resNotification);
			break;
		}
		case SEND_RESULT_NOTIFICATION_STATE:{
			state = RESET_STATE;
			DataStore ds = getDataStore();
			ACLMessage resNotification = (ACLMessage)ds.get(RESULT_NOTIFICATION_KEY);
			if(resNotification != null){
				ACLMessage receivedMsg = (ACLMessage)ds.get(REQUEST_KEY);

				myAgent.send(arrangeMessage(receivedMsg,resNotification));
			}
			break;
		}
		case RESET_STATE:{
			reset();
			break;
		}
		}
	}

	//#APIDOC_EXCLUDE_END

	/**   
	 * This method is called when the initiator's
	 * message is received that matches the message template
	 * passed in the constructor. 
	 * This default implementation return null which has
	 * the effect of sending no reponse. Programmers should
	 * override the method in case they need to react to this event.
	 * @param request the received message
	 * @return the ACLMessage to be sent as a response (i.e. one of
	 * <code>agree, refuse, not-understood, inform</code>. <b>Remind</b> to
	 * use the method createReply of the class ACLMessage in order
	 * to create a good reply message
	 * @see jade.lang.acl.ACLMessage#createReply()
	 **/
	protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
		System.out.println("prepareResponse() method not re-defined");
		return null;
	}

	/**   
	 * This method is called after the response has been sent
	 * and only when one of the folliwing two cases arise:
	 * the response was an <code>agree</code> message OR no response
	 * message was sent.
	 * This default implementation return null which has
	 * the effect of sending no result notification. Programmers should
	 * override the method in case they need to react to this event.
	 * @param request the received message
	 * @param response the previously sent response message
	 * @return the ACLMessage to be sent as a result notification (i.e. one of
	 * <code>inform, failure</code>. <b>Remind</b> to
	 * use the method createReply of the class ACLMessage in order
	 * to create a good reply message
	 * @see jade.lang.acl.ACLMessage#createReply()
	 * @see #prepareResponse(ACLMessage)
	 **/
	protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
		System.out.println("prepareResultNotification() method not re-defined");
		return null;
	}

	/**
       Reset this behaviour using the same MessageTemplate.
	 */
	public void reset(){
		finished = false;
		state = WAITING_MSG_STATE;
		DataStore ds = getDataStore();
		ds.remove(REQUEST_KEY);
		ds.remove(RESPONSE_KEY);
		ds.remove(RESULT_NOTIFICATION_KEY);
		super.reset();
	}

	/**
       This method allows to change the <code>MessageTemplate</code>
       that defines what messages this FIPARequestResponder will react to and reset the protocol.
	 */
	public void reset(MessageTemplate mt){
		template = mt;
		reset();
	}

	/**
	 * This method checks whether this behaviour has finished or not.
	 * @return <code>true</code> if this behaviour has completed its
	 * task, <code>false</code> otherwise.
	 */
	public boolean done() {
		return finished;
	}

	//this method arrange the reply according to the request received.
	//set the conversationID, inReplyTo, Protocol and the receivers.
	private ACLMessage arrangeMessage(ACLMessage request, ACLMessage reply){
		//set conversationId
		reply.setConversationId(request.getConversationId());
		//set the inReplyTo
		reply.setInReplyTo(request.getReplyWith());
		//set the protocol
		reply.setProtocol(request.getProtocol());
		//set the receivers
		if (!reply.getAllReceiver().hasNext()) {
			Iterator it = request.getAllReplyTo();
			int r = 0;
			while (it.hasNext()) {
				reply.addReceiver((AID)it.next());
				r++;
			}
			if (r == 0) {
				reply.addReceiver(request.getSender());
			}
		}
		return reply;
	}
}//end class SimpleAchieveREResponder
