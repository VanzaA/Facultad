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


package jade.tools.rma;

import jade.lang.acl.ACLMessage;

import jade.domain.*;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.ShowGui;

import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;


import jade.util.leap.List;
import jade.util.leap.ArrayList;

/**
   
   @author Francisco Regi, Andrea Soracchi - Universita` di Parma
   @version $Date: 2003-02-25 13:29:42 +0100 (mar, 25 feb 2003) $ $Revision: 3687 $
 */
class ShowDFGuiAction extends FixedAction
{

  private rma myRMA;
  private ACLMessage msg;
  
  ShowDFGuiAction(rma anRMA,ActionProcessor actPro ) {

     // Note: this class uses the DummyAgentActionIcon just because it
     // never displays an icon, but a parameter must anyway be passed.

     super ("DGGUIActionIcon","Show the DF GUI",actPro);
     myRMA = anRMA;
     msg = new ACLMessage(ACLMessage.REQUEST);
     msg.addReceiver(myRMA.getDefaultDF());
     msg.setOntology(JADEManagementOntology.NAME);
     msg.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);
     msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
     Action a = new Action();
     a.setActor(myRMA.getDefaultDF());
     a.setAction(new ShowGui());
     
     try {
     	myRMA.getContentManager().fillContent(msg,a);
     } catch (Exception e) {
     	e.printStackTrace();
     }
  }

   public void doAction() {
     myRMA.send(msg);
  }

}  // End of ShowDFGuiAction


