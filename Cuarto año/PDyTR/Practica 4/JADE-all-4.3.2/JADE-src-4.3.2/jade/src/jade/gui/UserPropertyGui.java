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

import javax.swing.*;
import java.awt.Dimension;
import java.awt.event.*;
import java.awt.Toolkit;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

/**
This class provides a gui for the user defined property. 
@author Tiziana Trucco - CSELT S.p.A
@version $Date: 2002-12-11 10:39:45 +0100 (mer, 11 dic 2002) $ $Revision: 3515 $
*/


public class UserPropertyGui extends JDialog
 	{
 		/**
    @serial
    */
    boolean editable;
 		/**
    @serial
    */
    JTextField nameValue;
 		/**
    @serial
    */
    JTextField valueValue;
 		/**
    @serial
    */
    SingleProperty out;
 		
    UserPropertyGui thisGui;
    Component parentGUI;
    
 
 		UserPropertyGui(Component owner)
 		{
 			super();
 			thisGui = this;
 			parentGUI = owner;
 		}

 		/**
 		* To show a given property. 
 		* The boolean parameter permits to edit the fields or not.
 		* If editable the method returns a property or null (if the Cancel button is pressed). 
 		* If not editable returns null.
 		*/
 		SingleProperty ShowProperty(SingleProperty p, boolean e)
 		{
      JLabel l;
      JPanel tmpPanel;
 			out = null;
 			editable = e;
 			
 			if(p == null)
 				p = new SingleProperty("", "");
 				
 			setTitle("Property");
 			JPanel uPane = new JPanel();
			uPane.setLayout(new BoxLayout(uPane,BoxLayout.Y_AXIS) );							
			tmpPanel = new JPanel();
			tmpPanel.setLayout(new BoxLayout(tmpPanel,BoxLayout.X_AXIS));
			
			l = new JLabel("NAME:");
			l.setPreferredSize(new Dimension(70,26));
			l.setMinimumSize(new Dimension(70,26));
			l.setMaximumSize(new Dimension(70,26));
			nameValue = new JTextField();
			nameValue.setEditable(editable);
			nameValue.setBackground(Color.white);
			nameValue.setText(p.getKey());
			tmpPanel.add(l);
			tmpPanel.add(nameValue);
			uPane.add(tmpPanel);
			
			tmpPanel = new JPanel();
			tmpPanel.setLayout(new BoxLayout(tmpPanel,BoxLayout.X_AXIS));
			l = new JLabel("VALUE:");
			l.setPreferredSize(new Dimension(70,26));
			l.setMinimumSize(new Dimension(70,26));
			l.setMaximumSize(new Dimension(70,26));

			valueValue = new JTextField();	
			valueValue.setEditable(editable);
			valueValue.setBackground(Color.white);
			valueValue.setText(p.getValue());
			tmpPanel.add(l);
			tmpPanel.add(valueValue);
			
			uPane.add(tmpPanel);
			
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
			JButton okButton = new JButton("OK");
			
			okButton.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						String param = (String)e.getActionCommand();
						if(param.equals("OK"))
						{
						  if(editable)
						  {
						  	String name = (nameValue.getText()).trim();
							  String value = (valueValue.getText()).trim();
							
							  if(name.length() >0 && value.length() >0)
								  out = new SingleProperty(name,value);
								else 
								{
									JOptionPane.showMessageDialog(thisGui,"Must have non-empty fields !","Error Message",JOptionPane.ERROR_MESSAGE);
									return;
								}
							}	
				      dispose();			
						}
					}
				});

			
			buttonPane.add(okButton);
			if(editable)
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						String param = (String)e.getActionCommand();
						if(param.equals("Cancel"))
						{
				      dispose();			
						}
					}
				});
				
				buttonPane.add(cancelButton);
				
				
			}
			
			uPane.add(buttonPane);
    
      getContentPane().add(uPane, BorderLayout.CENTER);
      
      setResizable(false);
      setModal(true);
     
      ShowCorrect();
      return out;
 		}
 		
 		private void ShowCorrect() 
 		{
 			pack();
      setLocation(parentGUI.getX() + (parentGUI.getWidth() - getWidth()) / 2, parentGUI.getY() + (parentGUI.getHeight() - getHeight()) / 2);    
      setVisible(true);
      toFront();
 	 }

 	}

 