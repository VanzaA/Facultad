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

	import javax.swing.JButton;
	import javax.swing.ImageIcon;
	import javax.swing.border.Border;
	import java.awt.event.ActionListener;
	import javax.swing.BorderFactory;
	import java.awt.event.ActionEvent;
	
	/**
   
   Button to start a web browser showing the JADE Home page.
   
   @author Tiziana Trucco - CSELT S.p.A.
   @version $Date: 2003-12-15 12:09:35 +0100 (lun, 15 dic 2003) $ $Revision: 4664 $

 */

	
	public class JadeLogoButton extends JButton
	{
		private static String logojade ="images/jadelogo.jpg";

	    /**
	       Default constructor.
	    */
		public JadeLogoButton()
		{
			ImageIcon jadeicon = new ImageIcon(getClass().getResource(logojade));
    	//JButton logo = new JButton(jadeicon);
			setIcon(jadeicon);
    	Border raisedbevel = BorderFactory.createRaisedBevelBorder();
    	setBorder(raisedbevel);
      setToolTipText("Go to JADE Home Page"); 
      addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{						
					try{
						BrowserLauncher.openURL(BrowserLauncher.jadeURL);
					}catch(java.io.IOException ex){ex.printStackTrace();}
				}	
			} );
			}

	}
