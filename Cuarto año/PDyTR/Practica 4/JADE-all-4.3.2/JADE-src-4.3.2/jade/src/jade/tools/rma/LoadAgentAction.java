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
import javax.swing.JOptionPane;

import jade.core.AID;
import jade.gui.AgentTree;
import jade.gui.AIDGui;


/**

   @author Giovanni Rimassa - FRAMeTech s.r.l.
 */
class LoadAgentAction extends ContainerAction {

    private rma myRMA;
    private Frame mainWnd;

    LoadAgentAction(rma anRMA, ActionProcessor act, Frame f)
    {
 	super("LoadAgentActionIcon", "Load Agent", act);
 	myRMA = anRMA;
 	mainWnd = f;
    }
 
    public void doAction(AgentTree.ContainerNode node) {

	//	String agentName = JOptionPane.showInputDialog(mainWnd, "Insert the agent local name:");
	String container  = node.getName();
	AIDGui gui = new AIDGui(mainWnd);
	gui.setTitle("Enter the AID for the agent to load");
	AID agentAid = gui.ShowAIDGui(null, true, false);
	if(agentAid != null) {
	    myRMA.loadAgent(agentAid, "JADE-DB", container);
	}
    }

}
