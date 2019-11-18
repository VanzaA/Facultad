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
import java.util.Properties;
import java.util.Enumeration;
import java.util.ArrayList;
import java.awt.Dialog;

/**
* This class extends the VisualStringList in order to show a list of user defined property.
* To show these items correctly, have been overridden the methods:
* 
* <code>editElement</code> to show the UserPropertyGui when required.
* <code>removeElements</code> since the superclass only maintain the key values is necessary 
* to update properties.
* @see jade.gui.VisualStringList
* @see jade.gui.UserPropertyGui

@author Tiziana Trucco - CSELT S.p.A
@version $Date: 2003-11-20 11:55:37 +0100 (gio, 20 nov 2003) $ $Revision: 4572 $
*/

public class VisualPropertiesList extends VisualStringList
{
	/**
  @serial
  */
  private Properties userDefinedSlots;
	
	VisualPropertiesList(Properties content, Component owner)
	{
		super(new ArrayList().iterator(),owner);
		
		this.userDefinedSlots = content;
		
		Enumeration e = content.propertyNames();
	
		ArrayList list = new ArrayList();
		while(e.hasMoreElements())
			list.add(e.nextElement());
		
		resetContent(list.iterator());
	}
	
	
	
	protected Object editElement(Object el, boolean isEditable)
	{
		SingleProperty p;
    String out = null;
    
		if (el == null)
			p = new SingleProperty("", "");
		else 
			p = new SingleProperty((String)el,userDefinedSlots.getProperty((String)el));
		UserPropertyGui gui = new UserPropertyGui(owner);
		p = gui.ShowProperty(p,isEditable);

		if(p != null)
		{
			out = p.getKey();
			userDefinedSlots.setProperty(out,p.getValue());
		 } 
		 
		return out;	
 		
	}
	
	// This method have been overridden because in this case 
	//is necessary to update the properties 
	protected void removeElement(Object el)
	{
		super.removeElement(el);
		userDefinedSlots.remove(el);
	}
	
	/**
	* This method must be used to retry the actual properties. 
	* The method getContent of the super class only returns the keys.
	*/
	
	public Properties getContentProperties()
	{
		return userDefinedSlots;
	}
	
	/**
	* This method must be used after a resetContent on this list to update the properties.
	*/
	
	public void setContentProperties(Properties p)
	{
		this.userDefinedSlots = p;
	}
	
}
