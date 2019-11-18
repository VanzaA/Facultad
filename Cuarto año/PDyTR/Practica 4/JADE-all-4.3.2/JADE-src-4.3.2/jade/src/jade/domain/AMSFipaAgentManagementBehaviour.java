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

import jade.core.CaseInsensitiveString;

import jade.content.Concept;
import jade.content.Predicate;
import jade.content.onto.basic.Action;
import jade.content.onto.basic.Result;
import jade.content.onto.basic.Done;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import jade.domain.FIPAAgentManagement.*;

import jade.util.leap.Iterator;
import jade.util.leap.ArrayList;
import jade.util.leap.List;

import jade.security.JADESecurityException;

/**
   This behaviour serves the actions of the FIPA management ontology 
   supported by the AMS.
   Extends RequestManagementBehaviour and implements performAction() to 
   i) call the method of the AMS corresponding to the requested 
   action and ii) prepare the result notification depending on 
   whether a result should be returned (RESULT or DONE).
   Note that all actions in the FIPAManagementOntology produce a 
   "synchronous" effect --> there is no need to delay the notification 
   at a later time as for some actions in the JADEManagementOntology.
   @author Tiziana Trucco - Tilab
   @author Giovanni Caire - Tilab
  @version $Date: 2004-05-19 18:01:19 +0200 (mer, 19 mag 2004) $ $Revision: 5083 $
*/
class AMSFipaAgentManagementBehaviour extends RequestManagementBehaviour{

	private ams theAMS;
	
  protected AMSFipaAgentManagementBehaviour(ams a, MessageTemplate mt) {
		super(a,mt);
		theAMS = a;
  }
	
  /**
     Call the proper method of the ams and prepare the notification 
     message
   */
  protected ACLMessage performAction(Action slAction, ACLMessage request) throws JADESecurityException, FIPAException {
  	Concept action = slAction.getAction();
  	List resultItems = null;
  	
  	// REGISTER
  	if (action instanceof Register) {
  		theAMS.registerAction((Register) action, request.getSender());
  	}
  	// DEREGISTER
  	else if (action instanceof Deregister) {
  		theAMS.deregisterAction((Deregister) action, request.getSender());
  	}
  	// MODIFY
  	else if (action instanceof Modify) {
  		theAMS.modifyAction((Modify) action, request.getSender());
  	}
  	// SEARCH
  	else if (action instanceof Search) {
  		resultItems = theAMS.searchAction((Search) action, request.getSender());
  	}
  	// GET_DESCRIPTION
  	else if (action instanceof GetDescription) {
  		APDescription dsc = theAMS.getDescriptionAction(request.getSender());
  		resultItems = new ArrayList();
  		resultItems.add(dsc);
  	}
  	else {
  		throw new UnsupportedFunction();
  	}
  	
  	// Prepare the notification
  	ACLMessage notification = request.createReply();
  	notification.setPerformative(ACLMessage.INFORM);
  	Predicate p = null;
  	if (resultItems != null) {
  		// The action produced a result
  		p = new Result(slAction, resultItems);
  	}
  	else {
  		p = new Done(slAction);
  	}
  	try {
	  	theAMS.getContentManager().fillContent(notification, p);
  	}
  	catch (Exception e) {
  		// Should never happen
  		e.printStackTrace();
  	} 
  	return notification;
  }  
}
