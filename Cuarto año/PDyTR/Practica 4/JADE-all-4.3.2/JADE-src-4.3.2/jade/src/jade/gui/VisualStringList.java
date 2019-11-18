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

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Enumeration;

/**
This class extends JPanel in order to provide a panel to show a list of string.
Clicking the right button of the mouse a popUp menu with the allowed operations is showed.
The method <code>setEnabled</code> permits to set the operation allowed.
If enabled, three are the operation allowed: Add new item, Edit an existing item, Remove a selected item; 
otherwise only the view operation is allowed.
Double clicking on a selected item permits the view/edit on it

Example of using this class:

	JDialog d = new JDialog();
	...
	ArrayList a = new ArrayList();
	a.add("element");
	....
	VisualStringList listGui = new VisualStringList(a.iterator);
	list.setEnabled(true); // to sets the popUpMenu to show the three choices Add Edit Remove
	list.setDimension(new Dimension(..,..));
	d.getContentPane.().add(listGui);
	
If the user needs to show more complex items, can extend this class and override the needed methods.
@author Tiziana Trucco - CSELT S.p.A
@version $Date: 2005-04-15 17:45:02 +0200 (ven, 15 apr 2005) $ $Revision: 5669 $
**/

public class VisualStringList extends JPanel
{
	/**
  @serial
  */
  Component owner;
	
	/**
  @serial
  */
  JList genericList;
	/**
  @serial
  */
  JScrollPane pane;
	/**
  @serial
  */
  DefaultListModel listModel;
	/**
  @serial
  */
  JPopupMenu popUp;
	/**
  @serial
  */
  JMenuItem editItem;
	/**
  @serial
  */
  JMenuItem addItem;
	/**
  @serial
  */
  JMenuItem removeItem;
	
	static String VIEW_LABEL = "View";
	static String EDIT_LABEL = "Edit";
	static String ADD_LABEL = "Add";
	static String REMOVE_LABEL = "Remove";
	
	/**
	* Constructor of a panel showing a list of strings.
	* By default the panel is editable and all the three operations (Add - Edit - Remove) are allowed.
	*
	* @param content an iterator of the items to show in the panel
	*
	*/

	public VisualStringList(Iterator content,Component ownerGui)
	{
		owner = ownerGui;
		listModel = new DefaultListModel();

		try
		{
			while(content.hasNext())
			{
				listModel.addElement(content.next());
			}
	 		
		}
		catch(Exception e){e.printStackTrace();}
		create();
	}

	
	/*#DOTNET_INCLUDE_BEGIN
	public VisualStringList(jade.util.leap.Iterator content,Component ownerGui)
	{
		owner = ownerGui;
		listModel = new DefaultListModel();

		try
		{
			while(content.hasNext())
			{
				listModel.addElement(content.next());
			}
	 		
		}
		catch(Exception e){e.printStackTrace();}
		create();
	}
	#DOTNET_INCLUDE_END*/

	private void create()
	{
	  genericList = new JList(listModel);
	  genericList.setCellRenderer(new VisualListCellRenderer(this));
	  genericList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	  genericList.setSelectedIndex(0);
	  
	  pane = new JScrollPane(genericList);
	  
	  genericList.setToolTipText("Right mouse click to show the popup menu");
	  genericList.addMouseListener(new PopupMouser());
	  	
		popUp = new JPopupMenu();
		
		popUp.setLightWeightPopupEnabled(false);
		
		//initilized with poUp editable
		editItem = new JMenuItem(EDIT_LABEL);
		editItem.addActionListener(new ActionListener()
		  {
	    	public void actionPerformed(ActionEvent e)
	    	{
	    		String param = e.getActionCommand();
	    		if(param.equals(EDIT_LABEL))
	    			editAction();
	    		else
	    		if(param.equals(VIEW_LABEL))
	    			viewAction();	
	    	}	  
	    });

		popUp.add(editItem);
		
	  
		addItem = new JMenuItem(ADD_LABEL);
	  addItem.addActionListener(new ActionListener()
	    {
	    	public void actionPerformed(ActionEvent e)
	    	{
	    		String param = e.getActionCommand();
	    		if(param.equals(ADD_LABEL))
	    		{		
	    			Object el = editElement(null,true);
	    			
	    			if(el != null)
	    				addElement(el);
	    		
	    		}
	    	
	    	}	  
	    });
	  popUp.insert(addItem,0);
	    
	  removeItem = new JMenuItem(REMOVE_LABEL);
	  removeItem.addActionListener(new ActionListener()
	    {
	    	public void actionPerformed(ActionEvent e)
	    	{ 
	    		String param = e.getActionCommand();
	    		if(param.equals(REMOVE_LABEL))
	    			{
	    				if(genericList.getModel().getSize() > 0)
	    				{
	    					Object el =  genericList.getSelectedValue();
	    				  if(el != null)
	    				  	removeElement(el);
	    				}
	    				
	    			}
	    	}
	    
	    });
	  popUp.insert(removeItem,2);
		
	  this.add(pane);
	}
	
	
	private void viewAction()
	{
		if(genericList.getModel().getSize() > 0)
		{
			Object el = genericList.getSelectedValue();
			if(el != null)
				editElement(el,false);
		}
	}
	
	private void editAction()
	{
		if(genericList.getModel().getSize() > 0)
	  {
	    Object el = genericList.getSelectedValue();
	    int index = genericList.getSelectedIndex();
	    			
	    if(el != null)
	    {
	      Object new_el = editElement(el,true);
	    	if(new_el != null)
	    	  listModel.set(index, new_el);
	    }
	  }
	}
	
	/**
	* Use this method to enable/disable the Add and Remove fields of the popUp menu.
	*/
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
	
		addItem.setEnabled(enabled);
		removeItem.setEnabled(enabled);
		
		if(!enabled)
			editItem.setText(VIEW_LABEL);
			
	}
	
	
	
	/** 
	* This method returns the string that will be shown on the panel.
	* Can be override by subclasses for more complex elements.
	*/
	protected String getElementName(Object el)
	{
		return el.toString();
	}
	
	//Inserts a new element in the list
	private void addElement(Object el)
	{
		listModel.addElement(el);
	}
	
	//Removes an element from the list
	protected void removeElement(Object el)
	{
		listModel.removeElement(el);
	}
	
	//Reset the items to show in the panel
	protected void resetContent(Iterator content)
	{
		listModel = new DefaultListModel();
		try
	  {
	  	while(content.hasNext())
	  	{
	  		listModel.addElement(content.next());
	  	}
	 		
	  }catch(Exception e){e.printStackTrace();}
	  
	  genericList.setModel(listModel);

	}
	
	//Can be override by subclasses. Default implementation is for string
	protected Object editElement(Object el, boolean isEditable)
	{
		Object out = null;
		
		if(isEditable)
		{

			StringDlg dlgString = new StringDlg(owner,"Insert a new value:");
      out = dlgString.editString((String)el);
      
		}
		else
		{

			StringDlg dlgString = new StringDlg(owner,"Value:");
      dlgString.viewString((String)el);
      
		}
	
		return out;
		
	}
	
	/**
	* Returns the list of items 
	*/
	public Enumeration getContent()
	{
		return (listModel.elements());
	}
	
	/**
	* Sets the dimension of the panel.
	*/
	public void setDimension(Dimension d)
	{
		pane.setPreferredSize(d);
	  pane.setMinimumSize(d);
	  pane.setMaximumSize(d);

	}

	
	private class VisualListCellRenderer extends DefaultListCellRenderer 
	{
		private VisualStringList myList;
		VisualListCellRenderer( VisualStringList l) 
		{
			super();
		  myList = l;
		}
		
	  public Component getListCellRendererComponent(
		  JList list,
		  Object value,
		  int index,
		  boolean isSelected,
		  boolean cellHasFocus)
	  {
		    Font courier = new Font("Courier", Font.BOLD, 12);
		    setFont(courier);
		    setText(myList.getElementName(value));
	  	  setBackground(isSelected ? Color.lightGray : Color.white);
		  
		    return this;
		}
	}
	
	      
  private class PopupMouser extends MouseAdapter
  {
    
    public PopupMouser(){}

    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger())
         popUp.show(e.getComponent(), e.getX(), e.getY());

    }

    public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger())
           popUp.show(e.getComponent(), e.getX(), e.getY());

    }
    
    public void mouseClicked(MouseEvent e){
    	if(e.getClickCount() == 2)
    	{
    		if(e.getComponent().getParent().getParent().getParent().isEnabled())
    			((VisualStringList)e.getComponent().getParent().getParent().getParent()).editAction();
    		else
    		  ((VisualStringList)e.getComponent().getParent().getParent().getParent()).viewAction();
    		  
    	}
    }
}   
} 
