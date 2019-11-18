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

import javax.swing.tree.TreeModel;

import jade.core.AID;

import jade.gui.AgentTree;
import jade.gui.AgentTreeModel;

/**
   
   @author Francisco Regi, Andrea Soracchi - Universita` di Parma
   @version $Date: 2001-10-15 12:54:58 +0200 (lun, 15 ott 2001) $ $Revision: 2790 $
 */
  class ResumeAction extends AgentAction {

  private rma myRMA;

  public ResumeAction(rma anRMA,ActionProcessor actPro) {
    super ("ResumeActionIcon","Resume",actPro);
    myRMA = anRMA;
  }

  public void doAction(AgentTree.AgentNode node ) {

    //node.setState("Running");
    //node.changeIcon(false);
    String toResume = node.getName();
    AID agentID = new AID();
    agentID.setName(toResume);

    myRMA.resumeAgent(agentID);
    //AgentTreeModel myModel = myRMA.getModel();
    //myModel.nodeChanged(node);
  }

}
