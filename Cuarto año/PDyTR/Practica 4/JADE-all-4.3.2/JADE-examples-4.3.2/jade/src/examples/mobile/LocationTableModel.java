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

package examples.mobile;

// Import required Java classes 
import jade.core.Location;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

/**
@author Tiziana Trucco - CSELT S.p.A.
@version $Date: 2008-10-09 14:04:02 +0200 (gio, 09 ott 2008) $ $Revision: 6051 $
*/

public class LocationTableModel extends AbstractTableModel 
{
	Vector names;

	// CONSTRUCTORS
	public LocationTableModel() 
	{
		super();
		names = new Vector();
	}

	// ADD
	public void add(Location loc)
	{
		names.add((Object) loc);
	}
	
	// GETELEMENTAT
	public Location getElementAt(int index)
	{
		return((Location) names.get(index));
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
		return(4);
	}

	public Object getValueAt(int row, int column)
	{
		String id, name, protocol, address;
		String value = ""; 
		Location loc = (Location) names.get(row);

		switch(column)
		{
		case 0:
			value = loc.getID();
			
			break;
		case 1:
				value = loc.getName();
			break;
		case 2:
			value = loc.getProtocol();
			break;
			case 3:
			value = loc.getAddress();
			break;
	
		}
		return ((Object) value);	
	}
}
