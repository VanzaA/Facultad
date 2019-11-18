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
//#APIDOC_EXCLUDE_FILE

import java.util.Date;
import java.util.Vector;

import jade.util.leap.*; 

import jade.core.*;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.*;

import jade.content.onto.Ontology;
import jade.content.onto.basic.Action;
import jade.content.onto.basic.Result;

import jade.content.ContentElementList;
import jade.content.ContentElement;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;

import jade.proto.SimpleAchieveREInitiator;

/** 
  This class extends the <code>FipaRequestIntiatorBehaviour</code> in order to request an agent, e.g. <em>DF or AMS</em> 
  to perform a specific action. <br>
  This class implements all the abstract method of the super classes, therefore the behaviour can be immediately added to an agent.  <br>
In some cases, it might be usefull to extend this class to override 
some of its methods and
  react to the received messages in an application-specific manner. <br>
  The class has two constructor. The first generic constructor can be used for all the action an agent can perform. In the case of a 
a search action the default search constraints are used. <br> 
  The second constructor is specific for a search action and it allows
to specify additional search constraints.
  
  @see jade.domain.DFService
  @see jade.domain.AMSService
  
  @author Fabio Bellifemine (CSELT S.p.A.)
  @version $Date: 2003-11-24 14:47:00 +0100 (lun, 24 nov 2003) $ $Revision: 4597 $
*/

public class RequestFIPAServiceBehaviour extends SimpleAchieveREInitiator{

  /**
   Exception class for timeouts. This exception is thrown when trying
   to obtain an <code>ACLMessage</code> from an <code>Handle</code>
   and no message has been yet received and the timeout limit is not yet
   elapsed.
   @see jade.core.behaviours.ReceiverBehaviour.Handle#getMessage()
  */
  public static class NotYetReady extends Exception {
    NotYetReady() {
      super("Requested message is not ready yet.");
    }
  }

    
  /**
  @serial
	*/
  private ACLMessage lastMsg;
  /**
  @serial
	*/
  private boolean notYetReady;
  /**
  @serial
	*/
      //to set a timeout for the search request: 5 minutes.
    private long timeout = 300000;
    

  /**
  *  Create a behaviour to request an agent to perform a specific action. 
  *  Using this constructor, is possible to pass all information necessary to
  *  request a search operation. 
  *  @param a The agent this behaviour belongs to, i.e. the agent who is
  *  interested in the search result.
  *  @param receiver The agent who will be requested to perform the action.
  *  @param agentDescription An agent descriptor used according to the action required.
  *  @param constraints The search contraints for the search action. 
     @exception FIPAException A suitable exception can be thrown 
      when
     the method locally discovers that the passed parameters are not valid.
  *  @see jade.domain.FIPAAgentManagement.SearchConstraints
  */
   public RequestFIPAServiceBehaviour(Agent a, AID receiver, String actionName, Object agentDescription, SearchConstraints constraints) throws FIPAException {
       //super(a, new ACLMessage(ACLMessage.REQUEST), mt);
       super(a,new ACLMessage(ACLMessage.REQUEST));
       ACLMessage msg = FIPAService.createRequestMessage(a,receiver);
     Action act = new Action();
     act.setActor(receiver);
     if (actionName.equalsIgnoreCase(FIPAManagementVocabulary.REGISTER)) {
       Register action = new Register();
       action.setDescription(agentDescription);
       act.setAction(action);
     }
     else if (actionName.equalsIgnoreCase(FIPAManagementVocabulary.DEREGISTER)) {
       Deregister action = new Deregister();
       action.setDescription(agentDescription);
       act.setAction(action);
     }
     else if (actionName.equalsIgnoreCase(FIPAManagementVocabulary.MODIFY)) {
       Modify action = new Modify();
       action.setDescription(agentDescription);
       act.setAction(action);
     }
     else if (actionName.equalsIgnoreCase(FIPAManagementVocabulary.SEARCH)) {
       Search action = new Search();
       action.setDescription(agentDescription);
       action.setConstraints(constraints);
       act.setAction(action);
       // set a timeout for the recursive search.
       msg.setReplyByDate(new Date(System.currentTimeMillis()+ timeout));
     }
     else
       throw new UnsupportedFunction();

     // initialize SL0 Codec and FIPAManagementVocabulary
     if (a.getContentManager().lookupOntology(FIPAManagementOntology.NAME) == null)
     	a.getContentManager().registerOntology(FIPAManagementOntology.getInstance());    
     if (a.getContentManager().lookupLanguage(FIPANames.ContentLanguage.FIPA_SL0) == null)
     	a.getContentManager().registerLanguage(new SLCodec(0),FIPANames.ContentLanguage.FIPA_SL0);

     // Write the action in the :content slot of the request
     try {
     	a.getContentManager().fillContent(msg, act);
  	 } catch (Exception e) {
  	 	e.printStackTrace();
  	 	throw new UnrecognisedValue("content");
  	 } 	
     reset(msg);
     notYetReady=true;
  }

  /**
  * Create a behaviour to request an agent to perform a specific action.
  * The default search constraints are used.
  *
  * @param a The agent this behaviour belongs to, i.e the agent who is interested in the action.
  * @param dfName The DF who will perform the action.
  * @param dfAction The action requested to the DF.
  * @param dfd An agent descriptor that will be use according to the action required to the DF.
  * @see #RequestFIPAServiceBehaviour(Agent a, AID receiver, String actionName, Object agentDescription, SearchConstraints constraints)
  */
  public RequestFIPAServiceBehaviour(Agent a, AID receiver, String actionName, Object agentDescription) throws FIPAException
  {
  	this(a,receiver,actionName,agentDescription,new SearchConstraints());
  }

  /**
    Method to handle <code>not-understood</code> replies.
    @param reply The actual ACL message received. It is of
    <code>not-understood</code> type and matches the conversation
    template.
  */
  protected void handleNotUnderstood(ACLMessage reply) {
    notYetReady=false;
    lastMsg=(ACLMessage)reply.clone();
  }

  /**
    Method to handle <code>refuse</code> replies.
    @param reply The actual ACL message received. It is of
    <code>refuse</code> type and matches the conversation
    template.
  */
  protected void handleRefuse(ACLMessage reply) {
    notYetReady=false;
    lastMsg=(ACLMessage)reply.clone();
  }

  /**
    Method to handle <code>agree</code> replies.
    @param reply The actual ACL message received. It is of
    <code>agree</code> type and matches the conversation
    template.
  */
  protected void handleAgree(ACLMessage reply) {
  }

  /**
    Method to handle <code>failure</code> replies.
    @param reply The actual ACL message received. It is of
    <code>failure</code> type and matches the conversation
    template.
  */
  protected void handleFailure(ACLMessage reply) {
    notYetReady=false;
    lastMsg=(ACLMessage)reply.clone();
  }

  /**
    Method to handle <code>inform</code> replies.
    @param reply The actual ACL message received. It is of
    <code>inform</code> type and matches the conversation
    template.
  */
  protected void handleInform(ACLMessage reply) {
    notYetReady=false;
    lastMsg=(ACLMessage)reply.clone();
  }

  /**
    Method to handle out of sequence replies.
    @param reply The actual ACL message received.
  */
    protected void handleOutOfSequence(ACLMessage reply){
	notYetReady = false;
	lastMsg = (ACLMessage)reply.clone();
    }

    /**
       This method handle is called after receiving all the responses and when the timeout is expired( in this case no reply has been received.
     */
    protected void handleAllResponses(Vector reply){
	notYetReady = false;
	if(reply.size() == 0)
	    //the timeout has expired: no replies received.
	    lastMsg = new ACLMessage(ACLMessage.NOT_UNDERSTOOD);
	else
	    lastMsg = (ACLMessage)((ACLMessage)reply.elementAt(0)).clone();
    }

    // This exception object records last outcome. When it is
    // 'null', all went OK.
    /**
    @serial
	  */
    private FIPAException outcome = null;

  /**
    This public method allows to get the INFORM message received in the final
    * state of this FIPA-Request protocol. 
    *@return the ACLMessage received
     @exception FIPAException A suitable exception can be thrown 
      when the protocol was finished with a FAILURE/REFUSE or NOT-UNDERSTOOD
      * performative.
      * @exception NotYetReady is thrown if the protocol is not yet finished.
  **/
  public ACLMessage getLastMsg() throws FIPAException,NotYetReady {
    if (notYetReady)
      throw new NotYetReady();
    if (lastMsg.getPerformative() != ACLMessage.INFORM)
      throw new FIPAException(lastMsg);
    return lastMsg;
  }

  //#ALL_EXCLUDE_BEGIN
  /**
    This public method allows to get the results of a search operation. 
    @return the List of Objects received an a result of the search. 
    @exception FIPAException A suitable exception can be thrown 
    when the protocol was finished with a FAILURE/REFUSE or NOT-UNDERSTOOD
    performative.
    @exception NotYetReady is thrown if the protocol is not yet finished.  
    @deprecated Use getSearchResults() instead.
   */
  public java.util.List getSearchResult() throws FIPAException,NotYetReady {
		Object[] r = getSearchResults();
		java.util.List l = new java.util.ArrayList();
		for (int i = 0; i < r.length; ++i) {
			l.add(r[i]);
		}
		return l;
  }
  //#ALL_EXCLUDE_END
    
  /**
    This public method allows to get the results of a search operation. 
    *@return An array of Objects containing the items found as 
    the result of the search. 
     @exception FIPAException A suitable exception can be thrown 
      when the protocol was finished with a FAILURE/REFUSE or NOT-UNDERSTOOD
      * performative.
      * @exception NotYetReady is thrown if the protocol is not yet finished.
  **/
  public Object[] getSearchResults() throws FIPAException,NotYetReady {
    if (notYetReady)
      throw new NotYetReady();
    if (lastMsg.getPerformative() != ACLMessage.INFORM)
      throw new FIPAException(lastMsg);
      
    List l = null;
    try {
    	Result r = (Result)((ContentElement)myAgent.getContentManager().extractContent(lastMsg)); 
    	l = r.getItems(); 
	} catch (Exception e) {
	    e.printStackTrace();
  	 	throw new UnrecognisedValue("content");		
	}
	return l.toArray(); 
	
  }

}
