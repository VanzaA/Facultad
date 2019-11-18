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
import jade.core.Agent;
import jade.core.AID;
import jade.domain.DFGUIAdapter;
import jade.gui.DFAgentDscDlg;
import jade.gui.GuiEvent;

import jade.util.Logger;

/**
@author Giovanni Caire - CSELT S.p.A
@version $Date: 2004-07-19 17:54:06 +0200 (lun, 19 lug 2004) $ $Revision: 5217 $
*/

class DFGUIModifyAction extends AbstractAction
{
	private DFGUI gui;
	
	//logging
	private Logger logger = Logger.getMyLogger(this.getClass().getName());

	public DFGUIModifyAction(DFGUI gui)
	{
		super ("Modify");
		this.gui = gui;
	}
	
	public void actionPerformed(ActionEvent e) 
	{
		int kind = gui.kindOfOperation();
		
	  AID name = gui.getSelectedAgentInTable();
	  DFAgentDescription dfd = null;
	  AID df = gui.myAgent.getDescriptionOfThisDF().getName();;

    if (name != null) //something was selected
    {
    		if ( kind == DFGUI.AGENT_VIEW)
    		try{
    			dfd = gui.myAgent.getDFAgentDsc(name); //agent registered

    		}catch(FIPAException fe){
    			if(logger.isLoggable(Logger.WARNING))
    				logger.log(Logger.WARNING, "No agent called " + name + " is currently registered with this DF");
				  return;

    		}
    		else
    		if(kind == DFGUI.LASTSEARCH_VIEW)
    		{
    			dfd = gui.getDFAgentSearchDsc(name); // the dsc is maintained in a variable of the gui
    			df = gui.getLastDF();
    		}
    	

    		DFAgentDscDlg dlg = new DFAgentDscDlg((Frame) gui);
			  DFAgentDescription editedDfd = dlg.ShowDFDGui(dfd,true,true);
        
			  if (editedDfd != null)
			  {
			    GuiEvent ev = new GuiEvent((Object)gui, DFGUIAdapter.MODIFY);
	        ev.addParameter(df);
		      ev.addParameter(editedDfd);
		      gui.myAgent.postGuiEvent(ev);

			  } 
    
    }
	
	}
}
	