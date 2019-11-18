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

import jade.core.AID;

import jade.gui.AgentTree;
import jade.gui.AgentTreeModel;

/**   
   @author Francisco Regi, Andrea Soracchi - Universita` di Parma
   @version $Date: 2002-12-13 12:40:04 +0100 (ven, 13 dic 2002) $ $Revision: 3524 $
 */
class SuspendAction extends AgentAction {

  private rma myRMA;

  public SuspendAction(rma anRMA,ActionProcessor actPro) {
    super ("SuspendActionIcon","Suspend",actPro);
    myRMA = anRMA;
  }

  public void doAction(AgentTree.AgentNode node ) {
    //node.setState("Suspended");
    //node.changeIcon(true);
    String toSuspend = node.getName();
    AID agentID = new AID();
    agentID.setName(toSuspend);

    myRMA.suspendAgent(agentID);
    //AgentTreeModel myModel = myRMA.getModel();
    //myModel.nodeChanged(node);

  }

} 