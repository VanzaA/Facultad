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

package jade.domain;

//#MIDP_EXCLUDE_FILE

import jade.content.Concept;
import jade.content.Predicate;
import jade.content.onto.basic.Action;
import jade.content.onto.basic.Done;
import jade.content.onto.basic.Result;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFGUIManagement.*;
import jade.domain.FIPAAgentManagement.UnsupportedFunction;
import jade.security.JADESecurityException;

/**
   This behaviour serves the actions of the DF_Applet ontology 
   supported by the DF.
   It extends RequestManagementBehaviour and implements performAction() to 
   i) call the method of the DF corresponding to the requested 
   action and ii) prepare the result notification.
   @author Tiziana Trucco - TILAB
   @author Fabio Bellifemine - TILAB
   @author Giovanni Caire - TILAB
 */
class DFAppletManagementBehaviour extends RequestManagementBehaviour {

  private df theDF;
  
  protected DFAppletManagementBehaviour(df a, MessageTemplate mt) {
		super(a, mt);
		theDF = a;
  }

  /**
     Call the proper method of the DF and prepare the notification 
     message
   */
  protected ACLMessage performAction(Action slAction, ACLMessage request) throws JADESecurityException, FIPAException {
  	Concept action = slAction.getAction();
  	Object result = null;
  	boolean asynchNotificationRequired = false;
  	
  	// GET_PARENTS
  	if (action instanceof GetParents) {
  		result = theDF.getParentsAction((GetParents) action, request.getSender());
  	}
  	// GET_DESCRIPTION
  	else if (action instanceof GetDescription) {
  		result = theDF.getDescriptionAction((GetDescription) action, request.getSender());
  	}
  	// GET_DESCRIPTION_USED
  	else if (action instanceof GetDescriptionUsed) {
  		result = theDF.getDescriptionUsedAction((GetDescriptionUsed) action, request.getSender());
  	}
  	// FEDERATE
  	else if (action instanceof Federate) {
  		theDF.federateAction((Federate) action, request.getSender());
			asynchNotificationRequired = true;
  	}
  	// REGISTER_WITH
  	else if (action instanceof RegisterWith) {
  		theDF.registerWithAction((RegisterWith) action, request.getSender());
			asynchNotificationRequired = true;
  	}
  	// DEREGISTER_FROM
  	else if (action instanceof DeregisterFrom) {
  		theDF.deregisterFromAction((DeregisterFrom) action, request.getSender());
			asynchNotificationRequired = true;
  	}
  	// MODIFY_ON
  	else if (action instanceof ModifyOn) {
  		theDF.modifyOnAction((ModifyOn) action, request.getSender());
			asynchNotificationRequired = true;
  	}
  	// SEARCH_ON 
  	else if (action instanceof SearchOn) {
  		theDF.searchOnAction((SearchOn) action, request.getSender());
			asynchNotificationRequired = true;
  	}
  	else {
  		throw new UnsupportedFunction();
  	}
  	
  	if (!asynchNotificationRequired) {
  		// The requested action has been completed. Prepare the notification
	  	ACLMessage notification = request.createReply();
	  	notification.setPerformative(ACLMessage.INFORM);
	  	Predicate p = null;
	  	if (result != null) {
	  		// The action produced a result
	  		p = new Result(slAction, result);
	  	}
	  	else {
	  		p = new Done(slAction);
	  	}
	  	try {
		  	theDF.getContentManager().fillContent(notification, p);
	  	}
	  	catch (Exception e) {
	  		// Should never happen
	  		e.printStackTrace();
	  	}
	  	return notification;
  	}
		else {
			// The requested action is being processed by a Behaviour. Store
			// the request message for later retrieval.
  		theDF.storePendingRequest(action, request);
  		return null;
  	}
  }
    /*
    In this method we can be send : AGREE- NOT UNDERSTOOD and Refuse.
    in this method we parse the content in order to know the action required to the DF.
    if the action is unsupported a NOT UDERSTOOD message is sent.
    if something went wrong with the ontology a REFUSE message will be sent, otherwise an AGREE will be sent.
    and performs the action.
    *
    protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException{
	isAnSLRequest(request);
	try{
	    //extract the content of the message
	    SLAction = (Action) myAgent.getContentManager().extractContent(request);
	    action = SLAction .getAction();
	    
	    if(action instanceof GetParent)
		actionID = GETPARENT;
	    else if(action instanceof GetDefaultDescription)
		actionID = GETDEFAULTDESCRIPTION;
	    else if(action instanceof Federate)
		actionID = FEDERATEWITH;
	    else if(action instanceof GetDescriptionUsed)
		actionID = GETDESCRIPTIONUSED;
	    else if(action instanceof DeregisterFrom)
		actionID = DEREGISTERFROM;
	    else if(action instanceof RegisterWith)
		actionID = REGISTERWITH;
	    else if(action instanceof SearchOn)
		actionID = SEARCHON;
	    else if(action instanceof ModifyOn)
		actionID = MODIFYON;
	    else{
		//action not supported.
		actionID = UNSUPPORTED;
		//should never occur since the parser throws an exception before.
		//FXIME:the exception should have a parameter.
		UnsupportedFunction uf = new UnsupportedFunction();
		createExceptionalMsgContent(SLAction,uf,request);
		throw uf;
	    }
	 
	    //if everything is OK returns an AGREE message.
	    ACLMessage agree = request.createReply();
	    agree.setPerformative(ACLMessage.AGREE);
	    agree.setContent("( ( true ) )");
	    return agree;
	    
	}catch(RefuseException re){
	    throw re;
	}catch(Exception e){
	    //Exception thrown by the parser.
	    e.printStackTrace();
	    UnrecognisedValue uv2 = new UnrecognisedValue("content");
	    createExceptionalMsgContent(SLAction,uv2,request);
	    throw uv2;
	}   
    }*/
    
    /**
       Send the Inform message.
     *
    protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException{

	ACLMessage reply = null;

	switch (actionID){
	case GETPARENT:
	    reply = myAgent.getParentAction(SLAction,request);
	    break;
	case GETDEFAULTDESCRIPTION:
	    reply = myAgent.getDescriptionOfThisDFAction(SLAction,request);
	    break;
	case FEDERATEWITH : 
	    myAgent.federateWithAction(SLAction,request);
	    break;
	case GETDESCRIPTIONUSED :
	    reply = myAgent.getDescriptionUsedAction(SLAction,request);
	    break; 
	case DEREGISTERFROM : 
	    myAgent.deregisterFromAction(SLAction,request);
	    break;
	case REGISTERWITH: 
	    myAgent.registerWithAction(SLAction,request);
	    break;
	case SEARCHON: 
	    myAgent.searchOnAction(SLAction,request);
	    break;
	case MODIFYON: 
	    myAgent.modifyOnAction(SLAction,request);
	    break;
	default: break; //FIXME: should never occur
	}

	return reply;
    }
    
    //to reset the action
    public void reset(){
	super.reset();
	action = null;
	actionID = UNSUPPORTED;
	SLAction = null;
    }*/
   
}
