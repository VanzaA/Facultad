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


package jade.tools.introspector.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Container;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Window;
import java.awt.EventQueue;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.*;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

 /**
 * This class is invoked the the user selects the AboutBox item on the menu. A dialog
 * box appears providing informations about the authors and program version of the Introspector agent.
 * 
 * @author  Tiziana Trucco TiLab S.p.A.
 * @version $Date: 2005-04-15 17:45:02 +0200 (ven, 15 apr 2005) $ $Revision: 5669 $
 */


public class AboutBoxAction extends AbstractAction{

  private JFrame gui;
  private JLabel label;
  private String imageFile = "images/jadelogo.jpg";


  Color dark_blue = new java.awt.Color(0,0,160);

	public AboutBoxAction(JFrame gui)
	{
		super ("About Introspector");
		this.gui = gui;
		setEnabled(true);
	}

	public void actionPerformed(ActionEvent e)
	{

    final AboutFrame f = new AboutFrame(gui,"About INTROSPECTOR");

    f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                f.disposeAsync();
            }
        });

		f.addMouseListener(new MouseClick());

		Container theCont = f.getContentPane();
	  			GridBagLayout gridbag = new GridBagLayout();
	        GridBagConstraints c = new GridBagConstraints();
	        theCont.setLayout(gridbag);

	        theCont.setBackground(Color.white);
	        c.fill = GridBagConstraints.HORIZONTAL;

	        label = new JLabel("The Introspector for:");
          label.setForeground(dark_blue);
	        c.weightx = 0.5;
					c.gridwidth = 3;
					c.gridx =0;
					c.gridy=0;
					gridbag.setConstraints(label,c);
	        theCont.add(label);
	        
	        ImageIcon jadeicon = new ImageIcon(getClass().getResource(imageFile));
					
					label = new JLabel(jadeicon);
				  c.weightx = 0.5;
					c.gridwidth = 3;
					c.gridx =0;
					c.gridy=1;
					gridbag.setConstraints(label,c);
	        theCont.add(label);
	        
	        String CVSname = "$Name: JADE-3_3 $";
					int colonPos = CVSname.indexOf(":");
	    		int dollarPos = CVSname.lastIndexOf('$');
	    		String name = CVSname.substring(colonPos + 1, dollarPos);
	    		
	    		if(name.indexOf("JADE") == -1)
							name = "JADE snapshot";
	    		else {
	        			name = name.replace('-', ' ');
								name = name.replace('_', '.');
								name = name.trim();
	    		}
	
	    		label = new JLabel(name);
	        label.setForeground(dark_blue);
					c.ipady = 1;
					c.gridwidth = 1;
					c.gridx = 1;
					c.gridy = 2;
					gridbag.setConstraints(label,c);
					theCont.add(label);
					
	        label = new JLabel(" ");
	       	c.gridwidth = 1;
					c.gridx = 0;
					c.gridy = 3;
	        gridbag.setConstraints(label,c);
					theCont.add(label);

         
					label = new JLabel("Developed by Andrea Squeri");
	        label.setForeground(dark_blue);
          c.weightx = 0.1;
					c.gridwidth = 3;
					c.gridx = 0;
					c.gridy = 5;
	        gridbag.setConstraints(label,c);
					theCont.add(label);

                                        
	        label = new JLabel("Universita' degli Studi di Parma");
	        label.setForeground(dark_blue);
          c.weightx= 0.1;
					c.gridwidth = 3;
					c.gridx = 0;
					c.gridy = 6;
	        gridbag.setConstraints(label,c);
					theCont.add(label);


	        f.setModal(true);
	        f.setSize(360,300);
	        f.setLocation(gui.getX()+(gui.getWidth()- f.getWidth())/2, gui.getY()+
	        (gui.getHeight() - f.getHeight())/ 2);
	        f.setVisible(true);
	}
	
	

private class AboutFrame extends JDialog {
	
	public AboutFrame(JFrame owner, String name){
		super(owner, name);
		//setModal(true);
		
	
	}
	
  public void disposeAsync() {

    class disposeIt implements Runnable {
      private Window toDispose;

      public disposeIt(Window w) {
			toDispose = w;
      }

      public void run() {
			toDispose.dispose();
      }

    }
    
    EventQueue.invokeLater(new disposeIt(this));

  }
  
  }
	
private class MouseClick implements MouseListener
{
	public void mouseClicked(MouseEvent event)
	{
		((AboutFrame)(event.getSource())).disposeAsync();	
	
	}
	public void mouseReleased(MouseEvent event){}
	public void mouseEntered(MouseEvent event){}
	public void mouseExited(MouseEvent event){}
	public void mousePressed(MouseEvent event){}
	
}	


} 