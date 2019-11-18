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
import jade.content.onto.basic.Action;
import jade.content.onto.basic.Done;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.JADEAgentManagement.*;
import jade.domain.FIPAAgentManagement.UnsupportedFunction;
import jade.security.JADESecurityException;

/**
   This behaviour serves the actions of the JADE management ontology 
   supported by the DF.
   It extends RequestManagementBehaviour and implements performAction() to 
   i) call the method of the DF corresponding to the requested 
   action and ii) prepare the result notification.
   @author Tiziana Trucco - TILAB
   @author Fabio Bellifemine - TILAB
   @author Giovanni Caire - TILAB
 */
class DFJadeAgentManagementBehaviour extends RequestManagementBehaviour {

	private df theDF;
	
  protected DFJadeAgentManagementBehaviour(df a, MessageTemplate mt){
		super(a,mt);
		theDF = a;
  }

  /**
     Call the proper method of the DF and prepare the notification 
     message
   */
  protected ACLMessage performAction(Action slAction, ACLMessage request) throws JADESecurityException, FIPAException {
  	Concept action = slAction.getAction();
  	
  	// SHOW_GUI
  	if (action instanceof ShowGui) {
  		theDF.showGuiAction((ShowGui) action, request.getSender());
  	}
  	else {
  		throw new UnsupportedFunction();
  	}
  	
  	// Prepare the notification
  	ACLMessage notification = request.createReply();
  	notification.setPerformative(ACLMessage.INFORM);
  	Done d = new Done(slAction);
  	try {
	  	theDF.getContentManager().fillContent(notification, d);
  	}
  	catch (Exception e) {
  		// Should never happen
  		e.printStackTrace();
  	} 
  	return notification;
  }
}
