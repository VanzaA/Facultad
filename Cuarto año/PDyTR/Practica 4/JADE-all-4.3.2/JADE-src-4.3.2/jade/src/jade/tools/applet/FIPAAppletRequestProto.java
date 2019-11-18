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
**************************************************************/

package jade.tools.applet;

import java.io.PrintStream;
import jade.util.leap.List;
import jade.util.leap.Iterator;
import jade.util.leap.ArrayList;
import java.util.Date;

import jade.content.lang.Codec;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.ACLParser;
import jade.lang.acl.ParseException;

import jade.content.lang.sl.SLCodec;
import jade.core.AID;

import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;

import jade.content.onto.Ontology;
import jade.content.onto.basic.Action;
import jade.content.onto.basic.Result;
import jade.tools.dfgui.DFGUI;
 /**
 * This class extends the AppletRequestProto in order to send a request for a FIPA Action.
 * @author Tiziana Trucco - CSELT S.p.A.
 * @version $Date: 2003-02-25 13:29:42 +0100 (mar, 25 feb 2003) $ $Revision: 3687 $
 */

public class FIPAAppletRequestProto extends AppletRequestProto
{

	public static class NotYetReady extends Exception
  {
  	NotYetReady()
  	{
  		super("Requested message is not ready yet.");
  	}
  }


	Codec c;
	String action;
	Object dfd;
	private static Ontology o = FIPAManagementOntology.getInstance();
	AID receiver;
	DFGUI gui;
	DFAppletCommunicator dfApplet;
	ACLMessage lastMsg;
	/**
	Constructor of the class. It creates the request message accoding to the action specified.
	@param out the PrintStream used to send the message.
	@param parser the parser to
	@param sender the sender of the message
	@param receiver the receiver of the message
	@param actionthe action requested
	@param agentDescription the agentdescription
	@param sc constraints the search contraints for a search operation
	*/
	FIPAAppletRequestProto(DFAppletCommunicator communicator,AID receiver, String actionName, Object agentDescription, SearchConstraints sc) throws FIPAException
	{
		super (communicator.getStream(), communicator.getParser(), new ACLMessage(ACLMessage.REQUEST));

		this.gui = communicator.getGUI();
		this.dfApplet = communicator;

    this.reqMsg = new ACLMessage(ACLMessage.REQUEST);

    this.reqMsg.addReceiver(receiver);
    this.reqMsg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
    this.reqMsg.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
    this.reqMsg.setOntology(FIPAManagementOntology.NAME);
    this.reqMsg.setReplyWith("rw"+(new Date()).getTime());
    this.reqMsg.setConversationId("conv"+(new Date()).getTime());

    this.action = actionName;
    this.dfd = agentDescription;
    this.receiver =receiver;

    Action act = new Action();
    act.setActor(receiver);
    if(actionName.equalsIgnoreCase(FIPAManagementVocabulary.REGISTER))
    {
    	Register action = new Register();
    	action.setDescription(agentDescription);
    	act.setAction(action);
    }else if (actionName.equalsIgnoreCase(FIPAManagementVocabulary.DEREGISTER)) {
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
       action.setConstraints(sc);
       act.setAction(action);
     }
     else
       throw new UnsupportedFunction();

     // initialize SL0 Codec and FIPAAgentManagementOntology
     //FIXME for applet I have not the agent c = sender.lookupLanguage(SL0Codec.NAME);
     //if (c == null)
       c = new SLCodec();

     // Write the action in the :content slot of the request
     this.reqMsg.setContent(AppletRequestProto.encode(act,(SLCodec)c,o));


	}

	/**
	Returns the results of a search operation.
	*/

  public List getSearchResult() throws FIPAException, NotYetReady
  {
  	if (notYetReady)
  		throw new NotYetReady();
  	if(lastMsg.getPerformative() != ACLMessage.INFORM)
  		throw new FIPAException(lastMsg);
  	Result r = AppletRequestProto.extractContent(lastMsg.getContent(),(SLCodec)c,o);
        Iterator i = r.getItems().iterator(); //this is the set of DFAgentDescription
        List l = new ArrayList();
        while (i.hasNext())
          l.add(i.next());
        return l;
  }

  	public void handleInform(ACLMessage msg)
   	{

   		notYetReady = false;
      lastMsg = (ACLMessage)msg.clone();
   		try{
   		  if (this.action.equalsIgnoreCase(FIPAManagementVocabulary.REGISTER))
   		  {    // refresh the gui
   		  	   // I register a df, so it will be a child.
   		  		if(dfApplet.isADF((DFAgentDescription)this.dfd))
   		  		  gui.addChildren(((DFAgentDescription)this.dfd).getName());
   		  	  gui.addAgentDesc(((DFAgentDescription)this.dfd).getName());
   		  	  gui.showStatusMsg("Registration of agent:" + ((DFAgentDescription)this.dfd).getName().getName()+" done.");

   		  }
   		  else
   		  if(this.action.equalsIgnoreCase(FIPAManagementVocabulary.SEARCH))
   		  { //extract the results and  update the gui
   		  	try{
            gui.showStatusMsg("Search request Processed. Ready for new request.");
   		  		gui.refreshLastSearchResults(getSearchResult(), msg.getSender());
   		  	}catch(NotYetReady nyr){
   		  	}catch(FIPAException e){}

   		  }
   		  else if(this.action.equalsIgnoreCase(FIPAManagementVocabulary.DEREGISTER))
   		  {
   		  	//update the gui
   		  	try{
   		  		gui.removeChildren(((DFAgentDescription)dfd).getName()); //if the agent deregistered was a child
   		  	}catch(Exception e){}

   		    gui.removeAgentDesc(((DFAgentDescription)dfd).getName(),dfApplet.getDescriptionOfThisDF().getName());
   		  	gui.showStatusMsg("Deregistration of agent: "+((DFAgentDescription)dfd).getName().getName() + " done.");

   		  }else if(this.action.equalsIgnoreCase(FIPAManagementVocabulary.MODIFY))
   		  {
   		  	gui.removeAgentDesc(((DFAgentDescription)dfd).getName(),dfApplet.getDescriptionOfThisDF().getName());
   		  	gui.addAgentDesc(((DFAgentDescription)dfd).getName());
   		  	gui.showStatusMsg("Modify of Agent: "+((DFAgentDescription)dfd).getName().getName() + " done.");
   		  }


   		}catch(NullPointerException e){}
   	}

   	protected void handleAgree(ACLMessage msg)
   	{
   		try{
   			gui.showStatusMsg("Process your request & waiting for result..");
   		}catch(NullPointerException e){}
   	}

    protected void handleNotUnderstood(ACLMessage msg)
    {
      notYetReady = false;
      lastMsg = (ACLMessage)msg.clone();
    }
   	protected void handleOtherMessage(ACLMessage msg){}

   	protected void handleRefuse(ACLMessage msg){
   	  notYetReady = false;
   	  lastMsg = (ACLMessage)msg.clone();
   	}
   	protected void handleFailure(ACLMessage msg){
   		notYetReady = false;
   		lastMsg = (ACLMessage)msg.clone();
   	}



}
