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

import jade.gui.AgentTree;

/**
   Register a remote Agent with local AMS.
   @author Tiziana Trucco - CSELT S.p.A.
   @version $Date: 2002-12-13 12:40:04 +0100 (ven, 13 dic 2002) $ $Revision: 3524 $
 */
class RegisterRemoteAgentAction extends AgentAction {

  private rma myRMA;

  public RegisterRemoteAgentAction(rma anRMA, ActionProcessor actPro) {
    super ("RegisterRemoteAgentIcon", "Register Remote Agent with local AMS", actPro);
    myRMA = anRMA;
  }

  public void doAction(AgentTree.AgentNode node ) {

    	if(node instanceof AgentTree.RemoteAgentNode){
	  		//System.out.println("Register Remote Agent with local AMS");
    	  myRMA.registerRemoteAgentWithAMS(((AgentTree.RemoteAgentNode)node).getAMSDescription());
    		
    	}
  }

} 
