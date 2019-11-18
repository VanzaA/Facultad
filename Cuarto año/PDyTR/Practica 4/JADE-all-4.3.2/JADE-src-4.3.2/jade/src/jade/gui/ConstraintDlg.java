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
import javax.swing.border.*;

import java.awt.event.*;
import java.awt.*;
import java.util.List;

import jade.domain.FIPAAgentManagement.SearchConstraints;

/*

*/

/**
 * This class implements the gui to insert the constraints for the search operation.
 *
 * @author Tiziana Trucco - CSELT S.p.A.
 * @version $Date: 2005-04-15 17:45:02 +0200 (ven, 15 apr 2005) $ $Revision: 5669 $
 */
public class ConstraintDlg extends JDialog
{
	/**
  @serial
  */
  JTextField maxDepth,	maxResult ;
	/**
  @serial
  */
  SearchConstraints constraints = new SearchConstraints();

  /*#DOTNET_INCLUDE_BEGIN
  Component myComponent;
  private Component getOwner() {return myComponent;}
  #DOTNET_INCLUDE_END*/
	
    /**
       Create a dialog with the given parent.
       @param parent The <code>Frame</code> that is the parent window
       of this dialog.
    */
    public ConstraintDlg(Frame parent)
    {
	super(parent);
	/*#DOTNET_INCLUDE_BEGIN
	myComponent = parent;
	#DOTNET_INCLUDE_END*/
    }

    /**
       Create a dialog with the given parent.
       @param parent The <code>Dialog</code> that is the parent window
       of this dialog.
    */
    public ConstraintDlg(Dialog parent)
    {
	super(parent);
	/*#DOTNET_INCLUDE_BEGIN
	myComponent = parent;
	#DOTNET_INCLUDE_END*/
    }
	
	/**
	This method display a gui to insert the search
	constraints. Return a <code>SearchConstraints </code> if the
	OK button is pressed null otherwise.
	*/
	public SearchConstraints setConstraint()
	{
			
		setTitle("Insert Search Constraints");
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.Y_AXIS));

		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p,BoxLayout.X_AXIS));
		
		JLabel label = new JLabel("Max propagation depth:");
		label.setPreferredSize(new Dimension(160,26));
		label.setMinimumSize(new Dimension(160,26));
	  label.setMaximumSize(new Dimension(160,26));

		p.add(label);
		maxDepth = new JTextField();
		maxDepth.setPreferredSize(new Dimension(30,26));
		maxDepth.setMinimumSize(new Dimension(30,26));
	  maxDepth.setMaximumSize(new Dimension(30,26));

		p.add(maxDepth);
		mainPanel.add(p);
                /*
                 * The maximum nuber of result to return for the search.
                 * A negative value indicates that the sender agent is willing
                 * to allow the search to propagate across all DFs.
                 * The default value is 1
                 */	
		p = new JPanel();
		label= new JLabel("Max number of results:");
		label.setPreferredSize(new Dimension(155,26));
		label.setMinimumSize(new Dimension(155,26));
	  label.setMaximumSize(new Dimension(155,26));
		p.add(label);
		maxResult = new JTextField("100");
		maxResult.setPreferredSize(new Dimension(30,26));
		maxResult.setMinimumSize(new Dimension(30,26));
	  maxResult.setMaximumSize(new Dimension(30,26));

		p.add(maxResult);
		
		mainPanel.add(p);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
	
		// Ok Button
		JButton okB = new JButton("OK");
		okB.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
 				{
 					String param = e.getActionCommand();
 					if(param == "OK")
 					{
            String depth = maxDepth.getText().trim();
            String result = maxResult.getText().trim();
            try
            {
            	//if no values inserted use the default ones.
            	if(depth.length() != 0)
            	{
            		Long d = Long.valueOf(depth);
            		
            		if(d.compareTo(new Long(0)) >= 0)
            		  constraints.setMaxDepth(d);
            		else
            			{
            				JOptionPane.showMessageDialog(null,"The propagation depth must be positive !!!.","Error Message",JOptionPane.ERROR_MESSAGE); 
								    return;
            			}    
            	}
            	
            	if(result.length() !=0)
            	{
                     /*
                      * The max result must be a positive value. It's initialized to 100.
                     */
                    Long r = Long.valueOf(result);
					if(r.compareTo(new Long(0)) >= 0)
                    	constraints.setMaxResults(r);              	
					else{
						JOptionPane.showMessageDialog(null,"The max result must be positive !!!.","Error Message",JOptionPane.ERROR_MESSAGE); 
						return;
					}
            	}
               else {
                     /* the user didnt set any value so set the value of max-result
                      * to the default value
                     */
                      constraints.setMaxResults(new Long(100));
                }
 
            	dispose();

            }	catch(Exception e1){
            		JOptionPane.showMessageDialog(null,"The inserted values must be numbers !!!.","Error Message",JOptionPane.ERROR_MESSAGE); 
								return;
            }			 
 					}
 				}
		});
		
		buttonPanel.add(okB);
		
		// Cancel Button
		JButton cancelB = new JButton("Cancel");
		cancelB.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
 				{
 					String param = e.getActionCommand();
 					if(param == "Cancel")
 					{
            constraints = null; 
 						dispose(); 
 					}
 				}
		});
		
		buttonPanel.add(cancelB);
		mainPanel.add(buttonPanel);
		getContentPane().add(mainPanel);
		
		setSize(new Dimension(200,200));
		setResizable(false);
		setModal(true);	
		//pack();
		//show();
		ShowCorrect();
		
		return constraints;
	}
	
	private void ShowCorrect() 
 	 {
    pack();
    
    try{
		//#DOTNET_EXCLUDE_BEGIN
    	int x = getOwner().getX() + (getOwner().getWidth() - getWidth()) / 2;
    	int y = getOwner().getY() + (getOwner().getHeight() - getHeight()) / 2; 
		//#DOTNET_EXCLUDE_END
		/*#DOTNET_INCLUDE_BEGIN
    	int x = getOwner().getX() + (getOwner().WIDTH - getWidth()) / 2;
    	int y = getOwner().getY() + (getOwner().HEIGHT - getHeight()) / 2; 
		#DOTNET_INCLUDE_END*/
    	setLocation(x>0 ? x:0,y>0 ? y:0);
    }catch(Exception e){}
    
    setVisible(true);
    toFront();
 	 }

	
}
