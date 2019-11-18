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

// Import required Jade classes
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAException;
import jade.core.AID;

import jade.gui.DFAgentDscDlg;

/**
@author Giovanni Caire - CSELT S.p.A.
@version $Date: 2004-04-06 11:39:40 +0200 (mar, 06 apr 2004) $ $Revision: 4967 $
*/

class DFGUIViewAction extends AbstractAction
{
	private DFGUI gui;
	

	public DFGUIViewAction(DFGUI gui)
	{
		super ("View");
		this.gui = gui;

	}
	
	public void actionPerformed(ActionEvent e) 
	{
	    DFAgentDescription dfd = new DFAgentDescription();
	    int kind = gui.kindOfOperation();
		
	    AID name = gui.getSelectedAgentInTable();
	  
	    //something was selected
		  if (name != null)
		  	if ( kind == DFGUI.AGENT_VIEW || kind == DFGUI.CHILDREN_VIEW || kind == DFGUI.LASTSEARCH_VIEW)
	      {
	      	try
	      	{
	      		if(kind == DFGUI.LASTSEARCH_VIEW)
	      	  	dfd = gui.getDFAgentSearchDsc(name); // the dsc is maintained in a variable of the gui
	          else
	  			    dfd = gui.myAgent.getDFAgentDsc(name); // agent registered
	      	}catch (FIPAException fe){
	  			  gui.showStatusMsg("WARNING! No description for agent called " + name + " is found");
	  			  return;}
	  	  }
	      else
	  	  {
	  	  	if (kind == DFGUI.PARENT_VIEW)
	  	      // In this case the description that will be shown will be the description used to federate the df 
	  		    dfd = gui.myAgent.getDescriptionOfThisDF(name);
	  	  }
	    else //nothing selected
	  	  return;

	    if(dfd != null && kind != -1)
	    {
	    	DFAgentDscDlg dlg = new DFAgentDscDlg((Frame) gui);
	    	dlg.ShowDFDGui(dfd,false,false);
	    }
	    
		
		
	}
}
	
