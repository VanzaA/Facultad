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
import jade.core.AID;
import jade.domain.DFGUIAdapter;
import jade.gui.AIDGui;
import jade.gui.DFAgentDscDlg;
import jade.gui.GuiEvent;

/**
@author Tiziana Trucco - CSELT S.p.A
@version $Date: 2000-12-01 15:48:38 +0100 (ven, 01 dic 2000) $ $Revision: 1991 $
*/

class DFGUIFederateAction extends AbstractAction
{
	private DFGUI gui;

	public DFGUIFederateAction(DFGUI gui)
	{
		super ("Federate");
		this.gui = gui;
		
	}
	
	public void actionPerformed(ActionEvent e) 
	{
	
		gui.setTab("Federate",null);
	
		DFAgentDescription editedDfd ;
		DFAgentDescription dfd = gui.myAgent.getDescriptionOfThisDF();
		AIDGui insertDlg = new AIDGui(gui);
		insertDlg.setTitle("Insert the AID of the DF with which federate");
		AID parent = insertDlg.ShowAIDGui(null,true,true); 
		
		if (parent != null)
		{
			DFAgentDscDlg dlg = new DFAgentDscDlg((Frame) gui);
			//FIXME the AID should not be editable
			editedDfd = dlg.ShowDFDGui(dfd,true,true);
		
		  if (editedDfd != null)
			  {
			  	GuiEvent ev = new GuiEvent((Object)gui, DFGUIAdapter.FEDERATE);
		      ev.addParameter(parent);
		      ev.addParameter(editedDfd);
		      gui.myAgent.postGuiEvent(ev);
	   	    gui.setTab("Federate",null);
			  }
		}
	
	}
}
	