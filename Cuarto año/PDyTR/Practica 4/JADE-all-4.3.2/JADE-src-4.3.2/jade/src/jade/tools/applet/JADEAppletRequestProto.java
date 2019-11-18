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
import java.util.Date;
import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;

import jade.content.onto.Ontology;

import jade.domain.DFGUIManagement.*;
import jade.core.AID;
import jade.lang.acl.ACLParser;
import jade.lang.acl.ACLMessage;

import jade.content.abs.AbsContentElement;
import jade.content.onto.basic.Action;
import jade.content.onto.OntologyException;

import jade.domain.FIPANames;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.UnsupportedFunction;

import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Result;

import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.tools.dfgui.DFGUI;

/**
* This class extends the AppletRequestProto in order to request
* the df for an applet action.
* @author Tiziana Trucco - CSELT S.p.A.
* @version $Date: 2003-08-26 11:37:47 +0200 (mar, 26 ago 2003) $ $Revision: 4252 $
*/


public class JADEAppletRequestProto extends AppletRequestProto
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
	private static Ontology o = DFAppletOntology.getInstance();
	AID receiver;
	AID parent;
	DFGUI gui;
	DFAppletCommunicator dfApplet;
	ACLMessage lastMsg;

	/**
	@param out
	@param parser
	@param sender the sender of the message
	@param receiver the receiver of the message
	@param actionName the action requested
	@param parentDF the df to wich request an action (used for federate action)
	*/
	JADEAppletRequestProto(DFAppletCommunicator communicator,AID receiver, String actionName,Object description,Object parentDF,SearchConstraints constraints) throws FIPAException
	{
		super (communicator.getStream(),communicator.getParser(), new ACLMessage(ACLMessage.REQUEST));

		this.gui = communicator.getGUI();
		this.dfApplet = communicator;
    ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
    //request.setSender(sender);
    request.addReceiver(receiver);
    request.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
    request.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
    request.setOntology(DFAppletOntology.NAME);
    request.setReplyWith("rw"+(new Date()).getTime());
    request.setConversationId("conv"+(new Date()).getTime());

    this.reqMsg = (ACLMessage)request.clone();
    this.action = actionName;
    this.receiver = receiver;
    this.parent = (AID)parentDF;

    Action act = new Action();
    act.setActor(receiver);
    if(actionName.equalsIgnoreCase(DFAppletVocabulary.FEDERATE))
    {

    	Federate action = new Federate();
    	action.setDf((AID) parentDF);
      action.setDescription((DFAgentDescription) description);

    	act.setAction(action);

    }
    else
    if(actionName.equalsIgnoreCase(DFAppletVocabulary.GETDESCRIPTION))
    	act.setAction(new GetDescription());

    else
    if(actionName.equalsIgnoreCase(DFAppletVocabulary.GETPARENTS))
    	act.setAction(new GetParents());
    else
    if(actionName.equalsIgnoreCase(DFAppletVocabulary.GETDESCRIPTIONUSED))
    {
      GetDescriptionUsed action = new GetDescriptionUsed();
      action.setParentDF((AID) parentDF);
      act.setAction(action);
    }
    else
    if(actionName.equalsIgnoreCase(DFAppletVocabulary.DEREGISTERFROM))
    {
    	DeregisterFrom action = new DeregisterFrom();
    	action.setDf((AID) parentDF);
    	action.setDescription((DFAgentDescription) description);

    	act.setAction(action);
    }
    else
    if(actionName.equalsIgnoreCase(DFAppletVocabulary.REGISTERWITH))
    {
    	RegisterWith action = new RegisterWith();
    	action.setDf((AID)parentDF);
    	action.setDescription((DFAgentDescription)description);

    	act.setAction(action);
    }
    else
    if(actionName.equalsIgnoreCase(DFAppletVocabulary.SEARCHON))
    {
    	SearchOn action = new SearchOn();
    	action.setDf((AID)parentDF);
    	action.setDescription((DFAgentDescription)description);
    	action.setConstraints(constraints);

    	act.setAction(action);
    }
    else
    if(actionName.equalsIgnoreCase(DFAppletVocabulary.MODIFYON))
    {
    	ModifyOn action = new ModifyOn();
    	action.setDf((AID)parentDF);
    	action.setDescription((DFAgentDescription)description);

    	act.setAction(action);
    }
    else
    throw new UnsupportedFunction();


     // initialize SL0 Codec and FIPAAgentManagementOntology
     //FIXME for applet I have not the agent c = sender.lookupLanguage(SL0Codec.NAME);
     //if (c == null)
       c = new SLCodec();

     // Write the action in the :content slot of the request
     List content = new ArrayList();
     content.add(act);

     try {
      String s = ((SLCodec)c).encode(o, (AbsContentElement)o.fromObject(act));
      this.reqMsg.setContent(s);
    } catch(OntologyException oe) {
      oe.printStackTrace();
      throw new FIPAException("Ontology error: " + oe.getMessage());
    } catch(Exception e)
    {
    	e.printStackTrace();
    }


	}

	JADEAppletRequestProto(DFAppletCommunicator communicator,AID receiver, String actionName,Object description,Object parentDF) throws FIPAException
	{
		this(communicator,receiver,actionName,description, parentDF,null);
	}


	/**
	returns the results of an action requested.
	*/
  public List getResult() throws FIPAException, NotYetReady
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
	protected void handleAgree(ACLMessage msg)
   	{
   		try{
   			lastMsg = (ACLMessage)msg.clone();
   			gui.showStatusMsg("Process your request & waiting for result...");
   		}catch(Exception e){}
   	}

   	protected void handleInform(ACLMessage msg)
   	{
   		try{
   	      notYetReady = false;
   				lastMsg = (ACLMessage)msg.clone();
   				if(this.action.equalsIgnoreCase(DFAppletVocabulary.FEDERATE))
   				   {
   				   	  gui.showStatusMsg("Request processed. Ready for new  request.");
   				   	  gui.addParent(this.parent);
   				   }
   				 else
   				 if(this.action.equalsIgnoreCase(DFAppletVocabulary.GETDESCRIPTION))
   				 {
   				 	//UPDATE the thisDf variable.
   				  try{
   				  	List result = getResult();
   				  	dfApplet.setDescription((DFAgentDescription)result.get(0));
   				  }catch(NotYetReady nyr){
   				  	//FIXME: what should happen in this case ?
   				   nyr.printStackTrace();
   				  }
   				 }
   				 else
   				 if(this.action.equalsIgnoreCase(DFAppletVocabulary.GETPARENTS))
             	gui.showStatusMsg("Request processed. Ready for new Request.");
           else
           if(this.action.equalsIgnoreCase(DFAppletVocabulary.DEREGISTERFROM))
           {
           		gui.showStatusMsg("Request processed. Ready for a new request");
           		gui.removeParent(this.parent);
           }
           else
           if(this.action.equalsIgnoreCase(DFAppletVocabulary.REGISTERWITH))
           	gui.showStatusMsg("Request processed. Ready for new request.");
           else
           if(this.action.equalsIgnoreCase(DFAppletVocabulary.SEARCHON))
           {
           	gui.refreshLastSearchResults(getResult(),(AID)parent);
           	gui.showStatusMsg("Request processed. Ready for new request.");
           }
           else
           if(this.action.equalsIgnoreCase(DFAppletVocabulary.MODIFYON))
             gui.showStatusMsg("Request processed. Ready for new request.");

   		}catch(Exception e){
   		e.printStackTrace();
   		}
   	}

   	protected void handleFailure(ACLMessage msg)
   	{
   	  try{
   	    notYetReady = false;
   	    lastMsg = (ACLMessage)msg.clone();
   	  	gui.showStatusMsg("Request refused.");
   	  	//FIXME: if no default description returned what should happen ?
   	  }catch(Exception e){
   	  e.printStackTrace();
   	  }
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

}
