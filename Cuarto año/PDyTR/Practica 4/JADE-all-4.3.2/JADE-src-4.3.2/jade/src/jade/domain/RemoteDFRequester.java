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

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;

import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAAgentManagement.InternalError;
import jade.domain.JADEAgentManagement.*;
import jade.domain.DFGUIManagement.*;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import jade.proto.SimpleAchieveREInitiator;

import jade.content.*;
import jade.content.onto.*;
import jade.content.onto.basic.*;

/**
   This behaviour is used by the df agent only (it is not defined 
   as an Inner class due to compilation problems in PJAVA) and handles 
   a request to another DF. These requests can 
   be triggered by an action of the DF-Applet ontology (e.g. Federate) 
   or by a DF GUI event. The onEnd() method must be re-defined to manage
   the result properly.
   @author Giovanni Caire - TILAB
 */
class RemoteDFRequester extends SimpleAchieveREInitiator {
	private AID remoteDF;
	private Concept myAction;
	private Object result = null;
	
	RemoteDFRequester(AID remoteDF, Concept myAction) {
		super(null, null);
		this.remoteDF = remoteDF;
		this.myAction = myAction;
	}
	
  protected ACLMessage prepareRequest(ACLMessage msg){
  	ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
  	request.addReceiver(remoteDF);
  	request.setOntology(FIPAManagementOntology.getInstance().getName());
  	request.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);
  	request.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
  	
  	Action act = new Action(remoteDF, myAction);
  	try {
  		myAgent.getContentManager().fillContent(request, act);
  		return request;
  	}
  	catch (Exception e) {
  		// Should never happen
  		e.printStackTrace();
  		result = new InternalError("Error encoding request ["+e.getMessage()+"]");
  	}
  	return null;
  }
  
  protected void handleInform(ACLMessage inform) {
  	try {
  		Predicate p = (Predicate) myAgent.getContentManager().extractContent(inform);
  		if (p instanceof Result) {
  			result = ((Result) p).getValue();
  		}
  	}
  	catch (Exception e) {
  		result = new InternalError("Error decoding response ["+e.getMessage()+"]");
  	}
  }
  
  protected void handleRefuse(ACLMessage refuse) {
  	result = new InternalError("Agent "+remoteDF.getName()+" replyed with "+ACLMessage.getPerformative(refuse.getPerformative()));
  }
  
  protected void handleNotUnderstood(ACLMessage notUnderstood) {
  	result = new InternalError("Agent "+remoteDF.getName()+" replyed with "+ACLMessage.getPerformative(notUnderstood.getPerformative()));
  }
  
  protected void handleFailure(ACLMessage failure) {
  	result = new InternalError("Agent "+remoteDF.getName()+" replyed with "+ACLMessage.getPerformative(failure.getPerformative()));
  }
  
  protected Object getResult() {
  	return result;
  }
  
  protected AID getRemoteDF() {
  	return remoteDF;
  }
  
  protected Concept getAction() {
  	return myAction;
  }
}
    	
