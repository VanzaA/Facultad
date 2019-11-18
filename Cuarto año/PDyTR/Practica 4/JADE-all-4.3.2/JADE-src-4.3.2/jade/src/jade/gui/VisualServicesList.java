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
import jade.domain.FIPAAgentManagement.ServiceDescription;
import javax.swing.JDialog;
import java.awt.Dialog;

/**
* This class extends the VisualStringList in order to show a list of services
* @see jade.gui.VisualStringList
* @see jade.gui.ServiceDscDlg
* @see jade.domain.FIPAAgentManagement.ServiceDescription
* @author Tiziana Trucco - CSELT S.p.A
* @version $Date: 2005-04-15 17:45:02 +0200 (ven, 15 apr 2005) $ $Revision: 5669 $
*/

public class VisualServicesList extends VisualStringList
{
	/**
  @serial
  */
  boolean checkSlots;
	VisualServicesList(Iterator content, Component owner)
	{
		super(content,owner);
		checkSlots = true;
	}

	/*#DOTNET_INCLUDE_BEGIN
	VisualServicesList(jade.util.leap.Iterator content, Component owner)
	{
		super(content,owner);
		checkSlots = true;
	}
	#DOTNET_INCLUDE_END*/
	
	protected String getElementName(Object el)
	{
		return (((ServiceDescription)el).getName());
	}
	
	protected Object editElement(Object el, boolean isEditable)
	{
		
		ServiceDscDlg gui = new ServiceDscDlg((Dialog)owner);
 		return gui.viewSD((ServiceDescription)el,isEditable, checkSlots);
 		
	}
	
	
	/**
	This method is used to ensure that the mandatory fields would be corrected inserted. 
	*/
	public void setCheckMandatorySlots(boolean checkMandatorySlots)
	{
		this.checkSlots = checkMandatorySlots;
	}

	
	
}
