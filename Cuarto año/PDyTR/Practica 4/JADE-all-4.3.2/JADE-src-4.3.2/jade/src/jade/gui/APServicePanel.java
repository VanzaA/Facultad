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

//#J2ME_EXCLUDE_FILE

// Import required Java classes 
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Iterator;

// Import required JADE classes
import jade.domain.FIPAAgentManagement.APService;
import jade.domain.FIPAAgentManagement.Property;

/**
To show an <code>APService</code> object within a Swing dialog.
@author Tiziana Trucco - CSELT S.p.A.
@version $Date: 2010-04-15 11:26:09 +0200 (gio, 15 apr 2010) $ $Revision: 6311 $
*/

public class APServicePanel extends JPanel 
{
	/** @serial*/
	
	JTextField name_Field;
	/** @serial*/
	JTextField type_Field;
	
	VisualStringList address_List;
	
	// CONSTRUCTORS
	/**
	Create a panel to show an APService.
	*/
	APServicePanel(Dialog parent) 
	{
		super();
				
	 	GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints  c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.SOUTH;
		setLayout(gridBag);
			
		JLabel label = new JLabel("Name: ");
	
	  c.gridx = 0;
		c.gridy = 0;
	  c.gridwidth = 1;
		gridBag.setConstraints(label,c);
		add(label);
	
		label = new JLabel("Type: ");
		c.ipady = 0;
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		gridBag.setConstraints(label,c);
		add(label);
		
		
    name_Field = new JTextField();
    name_Field.setEditable(false);
		name_Field.setBackground(java.awt.Color.white);
		c.gridx = 1;
		c.gridy = 0;
    gridBag.setConstraints(name_Field,c);
		add(name_Field);

		type_Field = new JTextField();
		type_Field.setEditable(false);
		type_Field.setBackground(java.awt.Color.white);
		c.gridx = 1;
		c.gridy = 1;
    gridBag.setConstraints(type_Field,c);
    add(type_Field);
  
    JPanel addressPanel = new JPanel();
    addressPanel.setLayout(new BoxLayout(addressPanel,BoxLayout.Y_AXIS));
    addressPanel.setBorder(BorderFactory.createTitledBorder("Addresses"));

    java.util.ArrayList a = new java.util.ArrayList();
    address_List = new VisualStringList(a.iterator(),parent);
	  address_List.setEnabled(false); // to sets the popUpMenu to show the three choices Add Edit Remove
	  address_List.setDimension(new Dimension(200,50));
	  
	  addressPanel.add(address_List);
	  
	  c.gridx = 0;
	  c.gridy = 2;
	  c.gridwidth = 2;
	  
	  gridBag.setConstraints(addressPanel,c);
	  add(addressPanel);
		
		
	}
	
	/**
	Set the field of the gui.
	*/
	private void setAPService(APService ap){
	
		if(ap != null)
		{
			name_Field.setText(ap.getName());
			type_Field.setText(ap.getType());
			//#DOTNET_EXCLUDE_BEGIN
			address_List.resetContent(ap.getAllAddresses());
			//#DOTNET_EXCLUDE_END
		}
	}

	/**
	To show an <code>APService</code> in A JDialog.
	*/
	public static void viewAPServiceDialog(APService ap,Dialog parent,String title){
	
		final JDialog tempDlg = new JDialog(parent, title, true);
  
		APServicePanel MTP_Panel = new APServicePanel(parent);
		MTP_Panel.setAPService(ap);

		JButton okButton = new JButton("OK");
		JPanel buttonPanel = new JPanel();
		// Use default (FlowLayout) layout manager to dispose the OK button
		buttonPanel.add(okButton);

		tempDlg.getContentPane().setLayout(new BorderLayout());
		tempDlg.getContentPane().add("Center", MTP_Panel);
		tempDlg.getContentPane().add("South", buttonPanel);

		okButton.addActionListener(new ActionListener()
									   {
											public void actionPerformed(ActionEvent e)
											{
												tempDlg.dispose();
											}
									   } );

		tempDlg.pack();
		tempDlg.setResizable(false);
		if (parent != null) {
		  int locx = parent.getX() + (parent.getWidth() - tempDlg.getWidth()) / 2;
		  if (locx < 0)
		    locx = 0;
		  int locy = parent.getY() + (parent.getHeight() - tempDlg.getHeight()) / 2;
		  if (locy < 0)
		    locy = 0;
		  tempDlg.setLocation(locx,locy);
		}
		tempDlg.setVisible(true);

	}

	


}
