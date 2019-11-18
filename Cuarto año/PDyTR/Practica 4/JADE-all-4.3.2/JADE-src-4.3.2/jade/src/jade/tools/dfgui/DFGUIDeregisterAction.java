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


package jade.tools.dfgui;

// Import required Java classes 
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

// Import required JADE classes
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAException;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFGUIAdapter;
import jade.gui.GuiEvent;

/**
@author Tiziana Trucco - CSELT S.p.A
@version $Date: 2004-04-06 11:39:40 +0200 (mar, 06 apr 2004) $ $Revision: 4967 $
*/

class DFGUIDeregisterAction extends AbstractAction
{
	private DFGUI gui;

	public DFGUIDeregisterAction(DFGUI gui)
	{
		super ("Deregister");
		this.gui = gui;
	}
	
	public void actionPerformed(ActionEvent e) 
	{
		DFAgentDescription dfd;
	  AID df =null;
	  AID name;
		int kind = gui.kindOfOperation();
	  
	  if ( kind == DFGUI.AGENT_VIEW || kind == DFGUI.CHILDREN_VIEW)
		{
      // Deregister an agent from the descriptor table		
			name = gui.getSelectedAgentInTable();
			if (name != null)
			{
				df = gui.myAgent.getDescriptionOfThisDF().getName();			
				dfd = new DFAgentDescription();
				dfd.setName(name); 
			}
			else
			  return;
		}
		else
		if (kind == DFGUI.PARENT_VIEW)
		{
			//Deregister the df from a selected parent 

			df = gui.getSelectedAgentInTable();
			if (df != null)
			    {
			    	dfd = gui.myAgent.getDescriptionOfThisDF(df);
			      if(dfd == null)
			    	  return; //should never happen
			    }
			else 
			    return;
		}
		else // kind=LASTSEARCH_VIEW
		{
			name = gui.getSelectedAgentInTable();
			if (name != null)
			{
				df = gui.getLastDF();			
				dfd = new DFAgentDescription();
				dfd.setName(name); 
			}
			else
			  return;
		}
			
		GuiEvent ev = new GuiEvent((Object)gui,DFGUIAdapter.DEREGISTER);
		ev.addParameter(df);
		ev.addParameter(dfd);
		gui.myAgent.postGuiEvent(ev);

	}
}
	
