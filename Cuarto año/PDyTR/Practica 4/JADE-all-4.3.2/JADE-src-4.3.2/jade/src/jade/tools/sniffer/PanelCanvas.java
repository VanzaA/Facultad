/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop multi-agent systems in compliance with the FIPA specifications.
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


package jade.tools.sniffer;

//#DOTNET_EXCLUDE_BEGIN
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
//#DOTNET_EXCLUDE_END
/*#DOTNET_INCLUDE_BEGIN
import System.Windows.Forms.*;
import System.Drawing.*;
#DOTNET_INCLUDE_END*/

  /**
   Javadoc documentation for the file
   @author Francisco Regi, Andrea Soracchi - Universita` di Parma
   <Br>
   <a href="mailto:a_soracchi@libero.it"> Andrea Soracchi(e-mail) </a>
   @version $Date: 2005-04-15 17:45:02 +0200 (ven, 15 apr 2005) $ $Revision: 5669 $
 */

 /**
  * Makes the two canvas.One is the agent canvas  which is useful
  * for drawing the agent's box and other is the message canvas
  * which useful for drawing the message with blue arrows.
  * @see jade.tools.sniffer.MainPanel
  */

public class PanelCanvas 
	//#DOTNET_EXCLUDE_BEGIN
	extends JPanel
	//#DOTNET_EXCLUDE_END
	/*#DOTNET_INCLUDE_BEGIN
	extends Panel
	#DOTNET_INCLUDE_END*/
{

  protected MMCanvas canvAgent;
  protected MMCanvas canvMess;
  //#DOTNET_EXCLUDE_BEGIN
  private PopMouserMessage popMess;
  private PopMouserAgent popAgent;
  //#DOTNET_EXCLUDE_END
  /*#DOTNET_INCLUDE_BEGIN
  private Panel myPanel;
  private PopupAgent popAgent;
  private PopupMessage popMess;
  #DOTNET_INCLUDE_END*/
  private Sniffer mySniffer;

  public PanelCanvas(MainWindow mWnd,MainPanel mPan,Sniffer mySniffer) {
   /*#DOTNET_INCLUDE_BEGIN
   super(); 
   #DOTNET_INCLUDE_END*/
   this.mySniffer=mySniffer;
   //#DOTNET_EXCLUDE_BEGIN
   GridBagConstraints gbc;
   setLayout(new GridBagLayout());
   gbc = new GridBagConstraints();
   gbc.gridx = 0;
   gbc.gridy = 0;
   gbc.gridwidth = GridBagConstraints.REMAINDER;
   gbc.gridheight = 1;
   gbc.anchor = GridBagConstraints.NORTHWEST;
   gbc.weightx = 0.5;
   gbc.weighty = 0;
   gbc.fill = GridBagConstraints.BOTH;
   canvAgent=new MMCanvas(true,mWnd,this,mPan,null);
   popAgent=new PopMouserAgent(canvAgent,mySniffer);
   canvAgent.addMouseListener(popAgent);
   add(canvAgent,gbc);

   gbc = new GridBagConstraints();
   gbc.gridx = 0;
   gbc.gridy = 1;
   gbc.gridwidth = GridBagConstraints.REMAINDER;
   gbc.gridheight = 100;
   gbc.anchor = GridBagConstraints.NORTHWEST;
   gbc.fill = GridBagConstraints.BOTH;
   gbc.weightx = 0.5;
   gbc.weighty = 1;
  
   //E' il canvas per i messaggi
   canvMess = new MMCanvas(false,mWnd,this,mPan,canvAgent);

   popMess=new PopMouserMessage(canvMess,mWnd);
   canvMess.addMouseListener(popMess);
   add(canvMess,gbc);
   //#DOTNET_EXCLUDE_END
   /*#DOTNET_INCLUDE_BEGIN
   canvAgent=new MMCanvas(true,mWnd,this,mPan,null);
   
   //E' il canvas per i messaggi
   canvMess = new MMCanvas(false,mWnd,this,mPan,canvAgent);
   
   this.popAgent = mWnd.popAg;
   this.popMess = mWnd.popMess;
   #DOTNET_INCLUDE_END*/
 }

   /*#DOTNET_INCLUDE_BEGIN
   	public void setPanel(Panel p)
	{
		myPanel = p;
		myPanel.add_MouseDown( new MouseEventHandler( this.mouser ) );
	}

	public Panel getPanel()
	{
		return myPanel;
	}
	
   	public void mouser(Object o, MouseEventArgs e)
	{
		Message mess = this.canvMess.selMessage(e);
		if (mess != null)
		{
			popMess.setMessage( mess );
		}

		if ( e.get_Button().Equals(MouseButtons.Right) )
		{
			popMess.Show(myPanel, new Point(e.get_X(), e.get_Y()) );
		}
		else
		{
			
		}
	}
   #DOTNET_INCLUDE_END*/
} 
