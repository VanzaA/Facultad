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


package jade.tools.sniffer;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import java.awt.Font;
import jade.gui.AgentTree;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.Color;

/*#DOTNET_INCLUDE_BEGIN
import System.Windows.Forms.*;
import System.Drawing.*;
#DOTNET_INCLUDE_END*/

  /**
   Javadoc documentation for the file
   @author Francisco Regi, Andrea Soracchi - Universita` di Parma
   <Br>
   <a href="mailto:a_soracchi@libero.it"> Andrea Soracchi(e-mail) </a>
   @version $Date: 2005-11-03 10:14:43 +0100 (gio, 03 nov 2005) $ $Revision: 5809 $
 */

  /**
   * Sets the tree and the two canvas inside the MainWindow
   * @see jade.tools.sniffer.PanelCanvas
   */

public class MainPanel 
//#DOTNET_EXCLUDE_BEGIN
	extends JPanel 
//#DOTNET_EXCLUDE_END
/*#DOTNET_INCLUDE_BEGIN
    extends Panel
#DOTNET_INCLUDE_END*/
{
 protected AgentTree treeAgent;
 //#DOTNET_EXCLUDE_BEGIN
 protected PanelCanvas panelcan;
 private JSplitPane pane;
 private PopupMouser popM;
 public JTextArea textArea;
 private Font font = new Font("Helvetica",Font.ITALIC,12);
 //#DOTNET_EXCLUDE_END
 /*#DOTNET_INCLUDE_BEGIN
 private System.Drawing.Font font = new System.Drawing.Font("Helvetica",12, FontStyle.Italic);
 protected PanelCanvas panelcan;
 public TextBox textArea;
 private Panel myPanel;
 #DOTNET_INCLUDE_END*/
 int pos;

 public MainPanel(Sniffer mySniffer,MainWindow mwnd) {
    //#DOTNET_EXCLUDE_BEGIN
	Font f;
    f = new Font("SanSerif",Font.PLAIN,14);
    setFont(f);
    setLayout(new BorderLayout(10,10));
    //#DOTNET_EXCLUDE_END

    /*#DOTNET_INCLUDE_BEGIN
	super();
	System.Drawing.Font f;
	f = new System.Drawing.Font("SanSerif", 14, FontStyle.Regular, GraphicsUnit.Pixel);
	#DOTNET_INCLUDE_END*/

    treeAgent = new AgentTree(f);

    panelcan = new PanelCanvas(mwnd,this,mySniffer);

    // Slight edit to this by R. Kessler.  We use a Scrollpane's ability to have
    // a column header by setting the columnheaderview to be the canvAgent.  Now,
    // when you get too long, the agents are still available on the top.
    //#DOTNET_EXCLUDE_BEGIN
    JScrollPane pc = new JScrollPane();
    pc.setColumnHeaderView(panelcan.canvAgent);
    pc.setViewportView(panelcan);
    pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,new JScrollPane(treeAgent.tree),pc);
    pane.setContinuousLayout(true);
    add(pane);
    textArea=new JTextArea();
    textArea.setBackground(Color.lightGray);
    textArea.setFont(font);
    textArea.setRows(1);
    textArea.setText("                                                                 No Message");
    textArea.setEditable(false);
    add(textArea,"South");
    //popM=new PopupMouser(treeAgent.tree,treeAgent);
    //treeAgent.tree.addMouseListener(popM);
    //#DOTNET_EXCLUDE_END

 }

    /*#DOTNET_INCLUDE_BEGIN
	public void setPanel(Panel p)
	{
		myPanel = p;
	}

	public Panel getPanel()
	{
		return myPanel;
	}
	#DOTNET_INCLUDE_END*/


  public void adjustDividerLocation() 
  {
	//#DOTNET_EXCLUDE_BEGIN
    pane.setDividerLocation(0.3);
    //#DOTNET_EXCLUDE_END
  }

} 
