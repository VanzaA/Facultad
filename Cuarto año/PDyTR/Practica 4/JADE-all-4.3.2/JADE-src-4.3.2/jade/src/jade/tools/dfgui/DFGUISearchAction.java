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
import java.util.Enumeration;

// Import required JADE classes
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAException;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.DFGUIAdapter;
import jade.gui.ConstraintDlg;
import jade.gui.DFAgentDscDlg;
import jade.gui.GuiEvent;
/**
@author Tiziana Trucco - CSELT S.p.A.
@version $Date: 2000-11-08 15:54:37 +0100 (mer, 08 nov 2000) $ $Revision: 1961 $
*/

class DFGUISearchAction extends AbstractAction
{
	private DFGUI gui;

	public DFGUISearchAction(DFGUI gui)
	{
		super ("Search");
		this.gui = gui;
	}
	
	public void actionPerformed(ActionEvent e) 
	{
	
		int kind = gui.kindOfOperation();
		AID df; 
		
		if ((kind == DFGUI.PARENT_VIEW) || (kind == DFGUI.CHILDREN_VIEW))// search on parent
		   {
		    
		   	  AID name = gui.getSelectedAgentInTable();
		      if (name != null)
		    	  df = name; //find the address of the parent-df
		      else	
			      df = gui.myAgent.getDescriptionOfThisDF().getName();
		   	
		   }	
		else 
		 	df = gui.myAgent.getDescriptionOfThisDF().getName();
		
		ConstraintDlg constraintsGui = new ConstraintDlg(gui);
		//insert the constraints for the search.
	  SearchConstraints constraints = constraintsGui.setConstraint();
		
	  if(constraints == null) //pressed the cancel button
	  	return;
	  	
	  DFAgentDscDlg dlg = new DFAgentDscDlg((Frame) gui);
	
		DFAgentDescription editedDfd = dlg.ShowDFDGui(null,true,false); //checkMandatorySlots = false

		//If no df is selected, the df of the platform is used. 
		if (editedDfd != null)
		{	
			GuiEvent ev = new GuiEvent((Object)gui,DFGUIAdapter.SEARCH);
	    ev.addParameter(df);
	    ev.addParameter(editedDfd);
	    ev.addParameter(constraints);
		  gui.myAgent.postGuiEvent(ev);
			gui.setTab("Search",df);
		}
	}
}
	
