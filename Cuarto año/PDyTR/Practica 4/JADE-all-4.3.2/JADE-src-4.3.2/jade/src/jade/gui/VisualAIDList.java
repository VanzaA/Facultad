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

package jade.gui;

//#APIDOC_EXCLUDE_FILE
//#J2ME_EXCLUDE_FILE

import java.awt.Component;
import java.util.Iterator;
import jade.core.AID;
import java.awt.Dialog;
/**
* This class extends the VisualStringList in order to show a list of AID (Agent-Identifer).
* To show these items correctly, have been overridden the methods:
* <code>getElementName</code> to return the name of the agent (shown in the list).
* <code>editElement</code> to show the ADIGui when required.
*
* @see jade.gui.VisualStringList
* @see jade.gui.AIDGui
* @author Tiziana Trucco - CSELT S.p.A
* @version $Date: 2003-11-20 11:55:37 +0100 (gio, 20 nov 2003) $ $Revision: 4572 $
*/
public class VisualAIDList extends VisualStringList
{
	/**
  @serial
  */
  boolean checkSlots;
	
	VisualAIDList(Iterator content,Component owner)
	{
		super(content,owner);
		checkSlots = true;//the default behaviour is that all the mandatory slots are checked.	
	}
	
	protected String getElementName(Object el)
	{
		return (((AID)el).getName());
	}

    /**
       Allow the user to edit the chosen agent identifier.
       @param el The chosen agent identifier.
       @param isEditable A boolean flag telling whether the user is
       allowed to modify che chosen agent identifier or not.
    */
	protected Object editElement(Object el, boolean isEditable)
	{
		
		AIDGui gui = new AIDGui(owner);
 		return gui.ShowAIDGui((AID)el,isEditable,checkSlots);
 		
	}
	/**
	This method is used to ensure that the mandatory fields would be corrected inserted. 
	*/
	public void setCheckMandatorySlots(boolean checkMandatorySlots)
	{
		this.checkSlots = checkMandatorySlots;
	}
	
}
