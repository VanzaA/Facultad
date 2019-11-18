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
import javax.swing.table.*;
import java.util.*;

// Import required JADE classes
import jade.domain.*;
import jade.core.AID;

/**
This class extends the AbstractTableModel to provide an appropriate model for the table used 
to display agents in the gui of the DF.

@see javax.swing.table.AbstractTableModel
@author Giovanni Caire Adriana Quinto - CSELT S.p.A.
@version $Date: 2008-10-09 14:04:02 +0200 (gio, 09 ott 2008) $ $Revision: 6051 $
*/

class AgentNameTableModel extends AbstractTableModel 
{
	Vector names;

	// CONSTRUCTORS
	public AgentNameTableModel() 
	{
		super();
		names = new Vector();
	}

	// ADD
	public void add(AID name)
	{
		names.add(name);
	}
	//REMOVE
	public void remove(AID name)
	{
		names.remove(name);
	}
	
	// GETELEMENTAT
	public AID getElementAt(int index)
	{
		return((AID) names.get(index));
	}

	// CLEAR
	public void clear()
	{
		names.clear();
	}

	// Methods to be implemented to have a concrete class
	public int getRowCount()
	{
		return(names.size());
	}

	public int getColumnCount()
	{
		return(3);
	}

	public Object getValueAt(int row, int column)
	{
		AID aid=getElementAt(row);
		String out = "";
		switch (column) {
		  case 0:  out = aid.getName(); break;
		  case 1:  for ( Iterator i=aid.getAllAddresses(); i.hasNext(); )
		  	         try{
		  	         	out = out+(String)i.next()+" "; 
		  	         }catch(Exception e){
		  	         	e.printStackTrace();
		  	         out = " ";
		  	         }
		  	      break;
		  case 2:  for ( Iterator i=aid.getAllResolvers(); i.hasNext(); )
		  	         try{
		  	         	out = out+((AID)i.next()).getName()+" "; 
		  	         }catch(Exception e1){
		  	         	e1.printStackTrace();
		  	          out = " ";
		  	         }
		  	      break;		}
		return out;
	}
}