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

import java.util.Vector;
import java.util.Date;

import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.DataStore;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.leap.Iterator;
import jade.core.Agent;
import jade.core.AID;
import jade.util.Logger;

/**
 * This is  simple implementation of the AchieveREInitiator.
 * This implementation in particular is 1:1 and does not allow
 * the possibility to add Handler. 
 *  
 * This is a single homogeneous and effective implementation of
 * all the FIPA-Request-like interaction protocols defined by FIPA,
 * that is all those protocols where the initiator sends a single message
 * (i.e. it performs a single communicative act) within the scope
 * of an interaction protocol in order to verify if the RE (Rational
 * Effect) of the communicative act has been achieved or not.

 * <p>
 * FIPA has already specified a number of these interaction protocols, like 
 * FIPA-Request, FIPA-query, FIPA-Request-When, FIPA-recruiting,
 * FIPA-brokering, FIPA-subscribe, that allows the initiator to verify if the 
 * expected rational effect of a single communicative act has been achieved. 
 * <p>
 * The structure of these protocols is equal.
 * The initiator sends a message (in general it performs a communicative act).
 * <p>
 * The responder can then reply by sending a <code>not-understood</code>, or a
 * <code>refuse</code> to 
 * achieve the rational effect of the communicative act, or also 
 * an <code>agree</code> message to communicate the agreement to perform 
 * (possibly in the future) the communicative act.  This first category
 * of reply messages has been here identified as a response.
 * <p> The responder performs the action and, finally, must respond with an 
 * <code>inform</code> of the result of the action (eventually just that the 
 * action has been done) or with a <code>failure</code> if anything went wrong.
 * This second category of reply messages has been here identified as a
 * result notification.
 * <p> Notice that we have extended the protocol to make optional the 
 * transmission of the agree message. Infact, in most cases performing the 
 * action takes so short time that sending the agree message is just an 
 * useless and uneffective overhead; in such cases, the agree to perform the 
 * communicative act is subsumed by the reception of the following message in 
 * the protocol.
 * <p>
 * Read carefully the section of the 
 * <a href="..\..\..\programmersguide.pdf"> JADE programmer's guide </a>
 * that describes
 * the usage of this class.
 * <p> <b>Known bugs:</b>
 * <i> The handler <code>handleAllResponses</code> is not called if the <code>
 * agree</code> message is skipped and the <code>inform</code> message
 * is received instead.
 *
 * @see SimpleAchieveREResponder
 * @see AchieveREInitiator
 * @see AchieveREResponder
 * @author Tiziana Trucco - TILab
 * @version $Date: 2005-09-16 15:54:46 +0200 (ven, 16 set 2005) $ $Revision: 5780 $
 **/


public class SimpleAchieveREInitiator extends SimpleBehaviour{

    private final static int PREPARE_MSG_STATE =0;
    private final static int SEND_MSG_STATE = 1;
    private final static int RECEIVE_REPLY_STATE = 2;
    private final static int RECEIVE_2ND_REPLY_STATE = 3;
    private final static int ALL_REPLIES_RECEIVED_STATE = 4;
    private final static int ALL_RESULT_NOTIFICATION_RECEIVED_STATE = 5;
  

    /**
     * key to retrive from the datastore the ACLMessage passed in the constructor
     **/
    public final String REQUEST_KEY = "_request" + hashCode() ;

    /**
     * key to retrive from the datastore the ACLMessage that has been sent.
     **/
    public final String REQUEST_SENT_KEY = "_request_sent" + hashCode(); 

    /**
     * key to retrive the second reply received.
     **/
    public final String SECOND_REPLY_KEY = "_2nd_reply" + hashCode();

    /**
     * key to retrive all the responses received.
     **/
    public final String ALL_RESPONSES_KEY = "_all-responses" + hashCode();

    /**
     * key to retrive the result notification received.
     **/
    public final String ALL_RESULT_NOTIFICATIONS_KEY = "_all-result-notification" + hashCode();

    //private ACLMessage request = null;
    private MessageTemplate mt = null;

    private int state = PREPARE_MSG_STATE;
    private boolean finished;
    private long timeout = -1;
    private long endingTime = 0;
	
	private Logger logger= Logger.getMyLogger(this.getClass().getName());
    /**
     * Construct for the class by creating a new empty DataStore
     * @see #SimpleAchieveREInitiator(Agent, ACLMessage, DataStore)
     **/
    public SimpleAchieveREInitiator(Agent a,ACLMessage msg){
	this(a,msg,new DataStore());
    }

    /**
     * Constructs a <code>SimpleAchieveREInitiator</code> behaviour
     * @param a The agent performing the protocol
     * @param msg The message that must be used to initiate the protocol.
     * Notice that in this simple implementation, the 
     * <code>prepareMessage</code>
     * method returns a single message.
     * @param s The <code>DataStore</code> that will be used by this 
     * <code>SimpleAchieveREInitiator</code>
     */

    public SimpleAchieveREInitiator(Agent a,ACLMessage msg,DataStore store){
	super(a);
	setDataStore(store);
	//	request = msg;
	getDataStore().put(REQUEST_KEY,msg);
	finished = false;
    }


    public final void action(){

	switch(state){
	case PREPARE_MSG_STATE:{
	    //retrive the message to send
	    ACLMessage msg = prepareRequest((ACLMessage)getDataStore().get(REQUEST_KEY));
	    getDataStore().put(REQUEST_SENT_KEY,msg);
	    state = SEND_MSG_STATE;
	    break;
	}
	case SEND_MSG_STATE:{
	    //send the message. If there is more than one receiver only the first will be taken into account.
	    DataStore ds = getDataStore();
	    String conversationID = null;
	    ACLMessage request = (ACLMessage)ds.get(REQUEST_SENT_KEY);
	    if (request == null){
		//no message to send --> protocol finished;
		//state = FINAL_STATE;
		finished = true;
		break;
	    }else{
		if(request.getConversationId()== null){
		    conversationID = "C" +hashCode()+"_" + System.currentTimeMillis();
		    request.setConversationId(conversationID);
		}
		else 
		    conversationID = request.getConversationId();

		mt = MessageTemplate.MatchConversationId(conversationID);

		//send the message only to the first receiver.
		Iterator receivers = request.getAllReceiver();
		AID r = (AID)receivers.next();
		request.clearAllReceiver();
		request.addReceiver(r);
 		if(receivers.hasNext())
		    if(logger.isLoggable(Logger.WARNING))
		    	logger.log(Logger.WARNING,"The message you are sending has more than one receivers. The message will be sent only to the first one !!");
		if(r.equals(myAgent.getAID())){
		    //if myAgent is the receiver then modify the messageTemplate 
		    //to avoid intercepting the request as it was a reply.
		    mt = MessageTemplate.and(mt,MessageTemplate.not(MessageTemplate.MatchCustom(request,true)));
		}

		//set the timeout
		//FIXME: if the Timeout is already expired before the message will be sent, it will be considered a infinite timeout
		Date d = request.getReplyByDate();
		if(d!=null)
		    timeout = d.getTime() - (new Date()).getTime();
		else
		    timeout = -1;
		endingTime = System.currentTimeMillis()+ timeout;
	
		myAgent.send(request);
		state = RECEIVE_REPLY_STATE;
	    }
	    break;
	}
	case RECEIVE_REPLY_STATE:{
	    ACLMessage firstReply = myAgent.receive(mt);
	    if(firstReply != null){
		DataStore ds = getDataStore();
		switch(firstReply.getPerformative()){
		case ACLMessage.AGREE:{
		    state = RECEIVE_2ND_REPLY_STATE;
		    Vector allResp = (Vector)ds.get(ALL_RESPONSES_KEY);
		    allResp.addElement(firstReply);
		    handleAgree(firstReply);
		    //all the responses have been collected.
		    handleAllResponses((Vector)getDataStore().get(ALL_RESPONSES_KEY));
		    break;
		}
		case ACLMessage.REFUSE:{
		    Vector allResp = (Vector) ds.get(ALL_RESPONSES_KEY);
		    allResp.addElement(firstReply);
		    state = ALL_REPLIES_RECEIVED_STATE;		 
		    handleRefuse(firstReply);
		    break;
		}
		case ACLMessage.NOT_UNDERSTOOD:{
		    Vector allResp = (Vector) ds.get(ALL_RESPONSES_KEY);
		    allResp.addElement(firstReply);
		    state = ALL_REPLIES_RECEIVED_STATE;		  
		    handleNotUnderstood(firstReply);
		    break;
		}
		case ACLMessage.FAILURE:{
		    Vector allResNot = (Vector) ds.get(ALL_RESULT_NOTIFICATIONS_KEY);
		    allResNot.addElement(firstReply);
		    state = ALL_RESULT_NOTIFICATION_RECEIVED_STATE;		 
		    handleFailure(firstReply);
		    break;
		}
		case ACLMessage.INFORM:{
		    Vector allResNot= (Vector) ds.get(ALL_RESULT_NOTIFICATIONS_KEY);
		    allResNot.addElement(firstReply);
		    state = ALL_RESULT_NOTIFICATION_RECEIVED_STATE;		    
		    handleInform(firstReply);
		    break;
		}
		default:{
		    state = RECEIVE_REPLY_STATE;		 
		    handleOutOfSequence(firstReply);
		    break;
		}
		}
		break;
	    }else{
	
		if(timeout > 0){
		    long blockTime = endingTime - System.currentTimeMillis();
		  
		    if(blockTime <=0 )
			//timeout Expired
			state = ALL_REPLIES_RECEIVED_STATE;
		    else //timeout not yet expired.
			block(blockTime); 
		   
	          
		}else //request without timeout.
		    block();
		
		break;
	    }
	   
	}
	case RECEIVE_2ND_REPLY_STATE:{
	    //after received an AGREE message. Wait for the second message.
	 
	    ACLMessage secondReply = myAgent.receive(mt);
	    if(secondReply != null){
		DataStore ds = getDataStore();
		switch (secondReply.getPerformative()){
		case ACLMessage.INFORM:{
		    //call the method handleAllResponses since if an agree was arrived it was not called.
		    state = ALL_RESULT_NOTIFICATION_RECEIVED_STATE;
		   
		    Vector allResNot = (Vector) ds.get(ALL_RESULT_NOTIFICATIONS_KEY);
		    allResNot.addElement(secondReply);
		    handleInform(secondReply);
		    break;
		}
		case ACLMessage.FAILURE:{
		    state = ALL_RESULT_NOTIFICATION_RECEIVED_STATE;		   
		   
		    Vector allResNot = (Vector) ds.get(ALL_RESULT_NOTIFICATIONS_KEY);
		    allResNot.addElement(secondReply);
		    handleFailure(secondReply);		    
		    break;
		}
		default:{
		    state = RECEIVE_REPLY_STATE;		   
		    handleOutOfSequence(secondReply);
		    break;
		}
		}
		break;
	    }else{
		block();
		break;
	    }
	}
	case ALL_REPLIES_RECEIVED_STATE:{
	    //after received a NOT-UNDERSTOOD and REFUSE message.
	    //call the handleAllResponses and then the handleAllResultNotification without any message.
	    state = ALL_RESULT_NOTIFICATION_RECEIVED_STATE;
	    handleAllResponses((Vector)(getDataStore().get(ALL_RESPONSES_KEY)));
	    break;
	}
	case ALL_RESULT_NOTIFICATION_RECEIVED_STATE:{
	    //after an INFORM or FAILURE message arrived.
	    finished = true;	  
	    handleAllResultNotifications((Vector)(getDataStore().get(ALL_RESULT_NOTIFICATIONS_KEY)));
	    break;
	}
	default: break;
	}
    }

    public void onStart(){
	initializeDataStore();
    }

    public boolean done(){
  	return finished;
    }

    /**
     * This method must return the ACLMessage to be sent.
     * This default implementation just return the ACLMessage object passed in the constructor.
     * Programmer might override the method in order to return a different ACLMessage.
     * Note that for this simple version of protocol, the message will be just send to the first receiver set.
     * @param msg the ACLMessage object passed in the constructor.
     * @return a ACLMessage.
     **/
    protected ACLMessage prepareRequest(ACLMessage msg){
	return msg;
    }

    /**
     * This method is called every time an <code>agree</code>
     * message is received, which is not out-of-sequence according
     * to the protocol rules.
     * This default implementation does nothing; programmers might
     * wish to override the method in case they need to react to this event.
     * @param agree the received agree message
     **/
    protected void handleAgree(ACLMessage msg){
		if(logger.isLoggable(Logger.FINE))
			logger.log(Logger.FINE,"in HandleAgree: " + msg.toString());

    }   

    /**
     * This method is called every time a <code>refuse</code>
     * message is received, which is not out-of-sequence according
     * to the protocol rules.
     * This default implementation does nothing; programmers might
     * wish to override the method in case they need to react to this event.
     * @param refuse the received refuse message
     **/
    protected void handleRefuse(ACLMessage msg){
		if(logger.isLoggable(Logger.FINE))
			logger.log(Logger.FINE,"in HandleRefuse: " + msg.toString());
    }

    /**
     * This method is called every time a <code>not-understood</code>
     * message is received, which is not out-of-sequence according
     * to the protocol rules.
     * This default implementation does nothing; programmers might
     * wish to override the method in case they need to react to this event.
     * @param notUnderstood the received not-understood message
     **/
    protected void handleNotUnderstood(ACLMessage msg){
		if(logger.isLoggable(Logger.FINE))
			logger.log(Logger.FINE,"in HandleNotUnderstood: " + msg.toString());
    }

    /**
     * This method is called every time a <code>inform</code>
     * message is received, which is not out-of-sequence according
     * to the protocol rules.
     * This default implementation does nothing; programmers might
     * wish to override the method in case they need to react to this event.
     * @param inform the received inform message
     **/
    protected void handleInform(ACLMessage msg){
	if(logger.isLoggable(Logger.FINE))
		logger.log(Logger.FINE,"in HandleInform: " + msg.toString());
    }

    /**
     * This method is called every time a <code>failure</code>
     * message is received, which is not out-of-sequence according
     * to the protocol rules.
     * This default implementation does nothing; programmers might
     * wish to override the method in case they need to react to this event.
     * @param failure the received failure message
     **/
    protected void handleFailure(ACLMessage msg){
	if(logger.isLoggable(Logger.FINEST))
		logger.log(Logger.FINEST,"in HandleFailure: " + msg.toString());
    }

    /**
     * This method is called every time a 
     * message is received, which is out-of-sequence according
     * to the protocol rules.
     * This default implementation does nothing; programmers might
     * wish to override the method in case they need to react to this event.
     * @param msg the received message
     **/
    protected void handleOutOfSequence(ACLMessage msg){
	if(logger.isLoggable(Logger.FINEST))
		logger.log(Logger.FINEST,"in HandleOutOfSequence: " + msg.toString());
    }

    /**
     * This method is called when all the responses have been
     * collected or when the timeout is expired.
     * By response message we intend here all the <code>agree, not-understood,
     * refuse</code> received messages, which are not
     * not out-of-sequence according
     * to the protocol rules.
     * This default implementation does nothing; programmers might
     * wish to override the method in case they need to react to this event
     * by analysing all the messages in just one call.
     * @param responses the Vector of ACLMessage objects that have been received 
     **/
    protected void handleAllResponses(Vector msgs){
	if(logger.isLoggable(Logger.FINEST))
		logger.log(Logger.FINEST,myAgent.getName()+"in handleAllResponses: ");    
    }

    /**
     * This method is called when all the result notification messages 
     * have been collected. 
     * By result notification message we intend here all the <code>inform, 
     * failure</code> received messages, which are not
     * not out-of-sequence according to the protocol rules.
     * This default implementation does nothing; programmers might
     * wish to override the method in case they need to react to this event
     * by analysing all the messages in just one call.
     * @param resultNodifications the Vector of ACLMessage object received 
     **/
    protected void handleAllResultNotifications(Vector msgs){
	if(logger.isLoggable(Logger.FINEST))
		logger.log(Logger.FINEST,myAgent.getName()+ "in HandleAllResultNotification: ");
    }

    /**
       This method resets this behaviour so that it restarts from the initial 
       state of the protocol with a null message.   
    */
    public void reset(){
	reset(null);
    }

    /**
       This method resets this behaviour so that it restarts the protocol with 
       another request message.
       @param msg updates message to be sent.
    */
    public void reset(ACLMessage msg){

	finished = false;
	state = PREPARE_MSG_STATE;
	getDataStore().put(REQUEST_KEY,msg);
	initializeDataStore();
	super.reset();
    }

    private void initializeDataStore(){

	Vector l = new Vector();
	getDataStore().put(ALL_RESPONSES_KEY,l);
	l = new Vector();
	getDataStore().put(ALL_RESULT_NOTIFICATIONS_KEY,l);
    }

}//end class SimpleAchieveREInitiator
 
