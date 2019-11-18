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

import java.awt.Frame;
import jade.gui.AgentTree;
import jade.core.AID;

/**
   
   @author Tiziana Trucco - CSELT S.p.A.
   @version $Date: 2002-12-13 14:34:24 +0100 (ven, 13 dic 2002) $ $Revision: 3529 $
 */
class CloneAgentAction extends AgentAction {
 
 private rma myRMA;
 private Frame mainWnd;
 
 CloneAgentAction(rma anRMA, ActionProcessor act,Frame f)
 {
 	super ("CloneAgentActionIcon","Clone Agent",act);
 	myRMA = anRMA;
 	mainWnd = f;
 }
 
 public void doAction(AgentTree.AgentNode node ) {

 	 String agentName  = node.getName();
   int result = MoveDialog.showMoveDialog("", mainWnd,true);
   
   if (result == MoveDialog.OK_BUTTON) {
     
      String container = MoveDialog.getContainer();
      String newAgentName = MoveDialog.getAgentName();
      
      if((container.trim().length() > 0) && (agentName.trim().length() >0))
      {
      	AID agentAid = new AID();
      	agentAid.setName(agentName);
      	myRMA.cloneAgent(agentAid,newAgentName,container);
      }
    }
    
  }

} 