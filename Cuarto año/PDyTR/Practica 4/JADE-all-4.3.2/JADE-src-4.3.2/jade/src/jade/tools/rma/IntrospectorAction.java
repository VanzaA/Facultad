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
 * DummyAgentAction spawns an external application passing as parameters a 
 * String containing ALL agents selected in the Tree
 * @see jade.gui.AMSAbstractAction
 * @author Tiziana Trucco - TiLab S.p.A
 */

  class IntrospectorAction extends ContainerAction {

  /**
   * Progressive Number to give always a new name to IntrospectoAgent
   */
  private int progressiveNumber = 0;

  private rma myRMA;

  public IntrospectorAction(rma anRMA,ActionProcessor actPro) {
      super ("IntrospectorActionIcon","Start IntrospectorAgent",actPro);
      progressiveNumber = 0;
      myRMA = anRMA;
    }

   public void doAction(AgentTree.ContainerNode node) {
   	 
   	String containerName = "";
   	if(node != null)
   	  	containerName = node.getName();
      myRMA.newAgent("Introspector"+progressiveNumber, "jade.tools.introspector.Introspector", new Object[0], containerName);
      progressiveNumber++;
     }

}  // End of IntrospectAction
