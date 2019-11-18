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

//#DOTNET_EXCLUDE_BEGIN
import javax.swing.tree.MutableTreeNode;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Color;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.SwingUtilities;
//#DOTNET_EXCLUDE_END
import java.net.InetAddress;

/*#DOTNET_INCLUDE_BEGIN
import System.Windows.Forms.*;
import System.Drawing.*;
import javax.swing.*;
#DOTNET_INCLUDE_END*/

import jade.gui.AgentTree;
import jade.core.AID;
import jade.util.ExtendedProperties;

  /**
   Javadoc documentation for the file
   @author Francisco Regi, Andrea Soracchi - Universita` di Parma
   <Br>
   <a href="mailto:a_soracchi@libero.it"> Andrea Soracchi(e-mail) </a>
   @version $Date: 2010-04-19 16:16:41 +0200 (lun, 19 apr 2010) $ $Revision: 6320 $
 */

 /**
 * This class performs the <em>Sniffer</em> main-windows GUI setup. Also provides method for
 * asynchronous disposal at takedown.
 *
 * @see	javax.swing.JFrame
 */


public class MainWindow 
	//#DOTNET_EXCLUDE_BEGIN
	extends JFrame {
	//#DOTNET_EXCLUDE_END
	/*#DOTNET_INCLUDE_BEGIN
	extends Form {
	#DOTNET_INCLUDE_END*/

  protected MainPanel mainPanel;
  protected ActionProcessor actPro; // Making this public allows us to get directly to the sniff agent action.
  //#DOTNET_EXCLUDE_BEGIN
  private PopupMenuAgent popA;
  //#DOTNET_EXCLUDE_END
  private Sniffer mySniffer;
  private ExtendedProperties myProperties;
  private String snifferLogo = "images/sniffer.gif";
  /*#DOTNET_INCLUDE_BEGIN
  private System.Windows.Forms.Panel MainPanelW;
  private System.Windows.Forms.Splitter splitter2;
  private System.Windows.Forms.Panel PanelCanvasW;
  private System.Windows.Forms.Panel CanvAgentW;
  private System.Windows.Forms.Panel CanvMessW;
  private System.Windows.Forms.Panel TreePanelW;
  private System.Windows.Forms.TreeView AgentTreeW;
  protected System.Windows.Forms.TextBox textBox1;
  protected java.awt.Frame myJavaFrame	= null;
  protected PopupAgent popAg			      = null;
  protected PopupMessage popMess		    = null;
  private Message mess					        = null;
  #DOTNET_INCLUDE_END*/
   
  public MainWindow(Sniffer mySniffer, ExtendedProperties myProperties) {
     //#DOTNET_EXCLUDE_BEGIN
	 super(mySniffer.getName() + " - Sniffer Agent");
     //#DOTNET_EXCLUDE_END
     /*#DOTNET_INCLUDE_BEGIN
	 super();
	 #DOTNET_INCLUDE_END*/
     this.mySniffer=mySniffer;
     this.myProperties=myProperties;
     mainPanel = new MainPanel(mySniffer, this);
	 /*#DOTNET_INCLUDE_BEGIN
	 InitializeComponent();

	 mainPanel.setPanel( MainPanelW );
	 mainPanel.treeAgent.setPanel( TreePanelW );
	 mainPanel.treeAgent.tree = AgentTreeW;
	 mainPanel.treeAgent.InitializeTreeView();
	 mainPanel.panelcan.setPanel( PanelCanvasW );
	 mainPanel.panelcan.canvAgent.setPanel( CanvAgentW );
	 mainPanel.panelcan.canvMess.setPanel( CanvMessW );
	 mainPanel.textArea = textBox1;

	 set_Text( mySniffer.getName() + " - Sniffer Agent" );
	 set_ForeColor( Color.get_Black() );
	 set_BackColor( Color.get_LightGray() );
	 set_Visible( true );
	 ResumeLayout( false);
	 #DOTNET_INCLUDE_END*/

     actPro=new ActionProcessor(mySniffer,mainPanel);
     //#DOTNET_EXCLUDE_BEGIN
	 setJMenuBar(new MainMenu(this,actPro));
     popA=new PopupMenuAgent(actPro);
     setForeground(Color.black);
     setBackground(Color.lightGray);
     Image image = getToolkit().getImage(getClass().getResource(snifferLogo));
     setIconImage(image);

     addWindowListener(new WindowAdapter() {
	     public void windowClosing(WindowEvent e) {
		 MainWindow.this.mySniffer.doDelete();
	     }
     });
     mainPanel.treeAgent.setNewPopupMenu(AgentTree.AGENT_TYPE, popA);
     getContentPane().add(new ToolBar(actPro),"North");
     getContentPane().add(mainPanel,"Center");
     //#DOTNET_EXCLUDE_END
		
	 /*#DOTNET_INCLUDE_BEGIN
	 popAg = new PopupAgent(mySniffer, mainPanel.panelcan.canvAgent);
	 popMess = new PopupMessage( this );

	 System.Drawing.Point p = this.get_Location();
	 myJavaFrame = new java.awt.Frame();
	 myJavaFrame.setLocation( p.get_X(), p.get_Y() );
	 myJavaFrame.pack();
		
	 mainPanel.treeAgent.tree.set_ContextMenu( popAg );
	 mySniffer.startBehaviours();
	 
	 this.set_Menu( new MainMenuSniffer(this,actPro) );
	 this.add_Closing( new System.ComponentModel.CancelEventHandler(this.OnClosing) );
	 
	 #DOTNET_INCLUDE_END*/
 }


 public void ShowCorrect() {
  //#DOTNET_EXCLUDE_BEGIN
  pack();
  setSize(new Dimension(700,500));
  mainPanel.adjustDividerLocation();
  this.setVisible(true);
  toFront();
  //#DOTNET_EXCLUDE_END
  /*#DOTNET_EXCLUDE_BEGIN
  set_Visible( true );
  Show();
  #DOTNET_EXCLUDE_END*/
 }

 public ExtendedProperties getProperties() {
    return myProperties;
 }

  public void resetTree() {
	  //#DOTNET_EXCLUDE_BEGIN
      Runnable resetIt = new Runnable() {

	  public void run() {
	  //#DOTNET_EXCLUDE_END
	      mainPanel.treeAgent.clearLocalPlatform();
	  //#DOTNET_EXCLUDE_BEGIN
          }
      };
      SwingUtilities.invokeLater(resetIt);
	  //#DOTNET_EXCLUDE_END
  }

    /*#DOTNET_INCLUDE_BEGIN
  	private void InitializeComponent ()
	{
		this.MainPanelW = new System.Windows.Forms.Panel();
		this.TreePanelW = new System.Windows.Forms.Panel();
		this.AgentTreeW = new System.Windows.Forms.TreeView();
		this.PanelCanvasW = new System.Windows.Forms.Panel();
		this.CanvAgentW = new System.Windows.Forms.Panel();
		this.CanvMessW = new System.Windows.Forms.Panel();
		this.textBox1 = new System.Windows.Forms.TextBox();
		this.MainPanelW.SuspendLayout();
		this.TreePanelW.SuspendLayout();
		this.PanelCanvasW.SuspendLayout();
		this.SuspendLayout();
		// 
		// MainPanelW
		// 
		this.MainPanelW.set_Anchor(((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) 
			| System.Windows.Forms.AnchorStyles.Left) 
			| System.Windows.Forms.AnchorStyles.Right))));
		this.MainPanelW.set_BorderStyle(System.Windows.Forms.BorderStyle.FixedSingle);
		this.MainPanelW.get_Controls().Add(this.TreePanelW);
		this.MainPanelW.get_Controls().Add(this.PanelCanvasW);
		this.MainPanelW.get_Controls().Add(this.textBox1);
		this.MainPanelW.set_Location(new System.Drawing.Point(16, 16));
		this.MainPanelW.set_Name("MainPanelW");
		this.MainPanelW.set_Size(new System.Drawing.Size(680, 296));
		this.MainPanelW.set_TabIndex(6);
		// 
		// TreePanelW
		// 
		this.TreePanelW.get_Controls().Add(this.AgentTreeW);
		this.TreePanelW.set_Dock(System.Windows.Forms.DockStyle.Fill);
		this.TreePanelW.set_Location(new System.Drawing.Point(0, 0));
		this.TreePanelW.set_Name("TreePanelW");
		this.TreePanelW.set_Size(new System.Drawing.Size(166, 272));
		this.TreePanelW.set_TabIndex(3);
		// 
		// AgentTreeW
		// 
		this.AgentTreeW.set_BackColor(System.Drawing.SystemColors.get_Window());
		this.AgentTreeW.set_Dock(System.Windows.Forms.DockStyle.Fill);
		this.AgentTreeW.set_ImageIndex(-1);
		this.AgentTreeW.set_Location(new System.Drawing.Point(0, 0));
		this.AgentTreeW.set_Name("AgentTreeW");
		this.AgentTreeW.set_SelectedImageIndex(-1);
		this.AgentTreeW.set_Size(new System.Drawing.Size(166, 272));
		this.AgentTreeW.set_TabIndex(0);
		this.AgentTreeW.add_MouseDown( new System.Windows.Forms.MouseEventHandler(this.AgentTreeW_MouseDown) );
		// 
		// PanelCanvasW
		// 
		this.PanelCanvasW.set_AutoScroll(true);
		this.PanelCanvasW.get_Controls().Add(this.CanvAgentW);
		this.PanelCanvasW.get_Controls().Add(this.CanvMessW);
		this.PanelCanvasW.set_Dock(System.Windows.Forms.DockStyle.Right);
		this.PanelCanvasW.set_Location(new System.Drawing.Point(166, 0));
		this.PanelCanvasW.set_Name("PanelCanvasW");
		this.PanelCanvasW.set_Size(new System.Drawing.Size(512, 272));
		this.PanelCanvasW.set_TabIndex(1);
		this.PanelCanvasW.add_MouseDown( new System.Windows.Forms.MouseEventHandler(this.CanvMessW_MouseDown) );
		// 
		// CanvAgentW
		// 
		this.CanvAgentW.set_Anchor(((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
			| System.Windows.Forms.AnchorStyles.Right))));
		this.CanvAgentW.set_AutoScroll(true);
		this.CanvAgentW.set_Location(new System.Drawing.Point(0, 0));
		this.CanvAgentW.set_Name("CanvAgentW");
		this.CanvAgentW.set_Size(new System.Drawing.Size(512, 48));
		this.CanvAgentW.set_TabIndex(1);
		// 
		// CanvMessW
		// 
		this.CanvMessW.set_Anchor(((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
			| System.Windows.Forms.AnchorStyles.Right))));
		this.CanvMessW.set_AutoScroll(true);
		this.CanvMessW.set_AutoScrollMinSize(new System.Drawing.Size(480, 265));
		this.CanvMessW.set_Location(new System.Drawing.Point(0, 0));
		this.CanvMessW.set_Name("CanvMessW");
		this.CanvMessW.set_Size(new System.Drawing.Size(512, 272));
		this.CanvMessW.set_TabIndex(2);
		this.CanvMessW.add_DoubleClick( new System.EventHandler(this.CanvMessW_DoubleClick) );
		this.CanvMessW.add_MouseDown( new System.Windows.Forms.MouseEventHandler(this.CanvMessW_MouseDown) );
		// 
		// textBox1
		// 
		this.textBox1.set_BackColor(System.Drawing.SystemColors.get_Control());
		this.textBox1.set_Dock(System.Windows.Forms.DockStyle.Bottom);
		this.textBox1.set_Font(new System.Drawing.Font("Microsoft Sans Serif", 9.75F, System.Drawing.FontStyle.Italic, System.Drawing.GraphicsUnit.Point, ((ubyte)(System.Byte)(((ubyte)0)))));
		this.textBox1.set_Location(new System.Drawing.Point(0, 272));
		this.textBox1.set_Name("textBox1");
		this.textBox1.set_ReadOnly(true);
		this.textBox1.set_Size(new System.Drawing.Size(678, 22));
		this.textBox1.set_TabIndex(9);
		this.textBox1.set_Text("No Message");
		this.textBox1.set_TextAlign(System.Windows.Forms.HorizontalAlignment.Center);
		// 
		// MainWindow
		// 
		this.set_AutoScale(false);
		this.set_AutoScaleBaseSize(new System.Drawing.Size(5, 13));
		this.set_ClientSize(new System.Drawing.Size(712, 348));
		this.get_Controls().Add(this.MainPanelW);
		this.set_Font(new System.Drawing.Font("Arial", 8.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((ubyte)(System.Byte)(((ubyte)0)))));
		this.set_Name("MainWindow");
		this.MainPanelW.ResumeLayout(false);
		this.TreePanelW.ResumeLayout(false);
		this.PanelCanvasW.ResumeLayout(false);
		this.ResumeLayout(false);
	}
    #DOTNET_INCLUDE_END*/

 /**
 * Tells the Agent Tree to add a container.
 *
 * @param	cont name of the container to be added
 */

 public void addContainer(final String name, final InetAddress addr) {
  //#DOTNET_EXCLUDE_BEGIN
  Runnable addIt = new Runnable() {
   public void run() {
    //MutableTreeNode node = mainPanel.treeAgent.createNewNode(name,0);
    //mainPanel.treeAgent.addContainerNode((AgentTree.ContainerNode)node,"FIPACONTAINER",addr);
	mainPanel.treeAgent.addContainerNode(name, addr);
   }
  };
  SwingUtilities.invokeLater(addIt);
  //#DOTNET_EXCLUDE_END
  /*#DOTNET_INCLUDE_BEGIN
  //AgentTree.Node node = mainPanel.treeAgent.createNewNode(name,0);
  //mainPanel.treeAgent.addContainerNode((AgentTree.ContainerNode)node,"FIPACONTAINER",addr);
  mainPanel.treeAgent.addContainerNode(name, addr);
  #DOTNET_INCLUDE_END*/

 }

 /**
 * Tells the Agent Tree to remove a specified container.
 *
 * @param	cont name of the container to be removed
 */

 public void removeContainer(final String name) {
  //#DOTNET_EXCLUDE_BEGIN
  Runnable removeIt = new Runnable() {
   public void run() {
    mainPanel.treeAgent.removeContainerNode(name);
   }
  };
  SwingUtilities.invokeLater(removeIt);
  //#DOTNET_EXCLUDE_END
  /*#DOTNET_INCLUDE_BEGIN
  mainPanel.treeAgent.removeContainerNode(name);
  #DOTNET_INCLUDE_END*/
 }

 /**
 * Tells the Agent Tree to add an agent.
 *
 * @param	cont name of the container to contain the new agent
 * @param name name of the agent to be created
 * @param addr address of the agent to be created
 * @param comm comment (usually type of the agent)
 */

 public void addAgent(final String containerName, final AID agentID) {
   //#DOTNET_EXCLUDE_BEGIN
   Runnable addIt = new Runnable() {
   public void run() {
     String agentName = agentID.getName();
     //AgentTree.Node node = mainPanel.treeAgent.createNewNode(agentName, 1);
     mainPanel.treeAgent.addAgentNode(agentName, "agentAddress", containerName);
     //mainPanel.treeAgent.addAgentNode((AgentTree.AgentNode)node, containerName, agentName, "agentAddress", "FIPAAGENT");
   }
  };
  SwingUtilities.invokeLater(addIt);
  //#DOTNET_EXCLUDE_END
  /*#DOTNET_INCLUDE_BEGIN
  String agentName = agentID.getName();
  //AgentTree.Node node = mainPanel.treeAgent.createNewNode(agentName, 1);
  mainPanel.treeAgent.addAgentNode(agentName, "agentAddress", containerName);
  //mainPanel.treeAgent.addAgentNode((AgentTree.AgentNode)node, containerName, agentName, "agentAddress", "FIPAAGENT");
  #DOTNET_INCLUDE_END*/
 }

 /**
 * Tells the Agent Tree to remove a specified agent.
 *
 * @param	cont name of the container containing the agent
 * @param name name of the agent to be removed
 */

 public void removeAgent(final String containerName, final AID agentID) {
   //#DOTNET_EXCLUDE_BEGIN
   Runnable removeIt = new Runnable() {
   public void run() {
     String agentName = agentID.getName();
     mainPanel.treeAgent.removeAgentNode(containerName, agentName);
     mainPanel.panelcan.canvAgent.removeAgent(agentName);
     mainPanel.panelcan.canvAgent.repaintNoSniffedAgent(new Agent(agentID));
   }
  };
  SwingUtilities.invokeLater(removeIt);
  //#DOTNET_EXCLUDE_END
  /*#DOTNET_INCLUDE_BEGIN
  String agentName = agentID.getName();
  mainPanel.treeAgent.removeAgentNode(containerName, agentName);
  mainPanel.panelcan.canvAgent.removeAgent(agentName);
  mainPanel.panelcan.canvAgent.repaintNoSniffedAgent(new Agent(agentID));
  #DOTNET_INCLUDE_END*/
 }

 /**
 * Displays a dialog box with the error string.
 *
 * @param	errMsg error message to print
 */

 public void showError(String errMsg) {
   //#DOTNET_EXCLUDE_BEGIN
   JOptionPane.showMessageDialog(null, errMsg, "Error in " + mySniffer.getName(), JOptionPane.ERROR_MESSAGE);
   //#DOTNET_EXCLUDE_END
   /*#DOTNET_INCLUDE_BEGIN
   MessageBox.Show(errMsg, "Error in " + mySniffer.getName(), MessageBoxButtons.OK, MessageBoxIcon.Error);
   #DOTNET_INCLUDE_END*/
 }

 //#DOTNET_EXCLUDE_BEGIN
 public Dimension getPreferredSize() {
	return new Dimension(700,500);
 }

 private void setUI(String ui) {
  try {
   UIManager.setLookAndFeel("javax.swing.plaf."+ui);
   SwingUtilities.updateComponentTreeUI(this);
   pack();
  } catch(Exception e){
     System.out.println(e);
     e.printStackTrace(System.out);
    }
 }

 /**
  enables Motif L&F
 */

 public void setUI2Motif() {
  setUI("motif.MotifLookAndFeel");
 }

 /**
  enables Windows L&F
 */

 public void setUI2Windows() {
  setUI("windows.WindowsLookAndFeel");
 }

 /**
 enables Multi L&F
 */

 public void setUI2Multi() {
  setUI("multi.MultiLookAndFeel");
 }

 /**
 enables Metal L&F
 */
	public void setUI2Metal()
	{
		setUI("metal.MetalLookAndFeel");
    }
 //#DOTNET_EXCLUDE_END

 /**
 * Provides async disposal of the gui to prevent deadlock when not running in
 * awt event dispatcher
 */

  public void disposeAsync() {
	//#DOTNET_EXCLUDE_BEGIN
    class disposeIt implements Runnable {
      private Window toDispose;

      public disposeIt(Window w) {
			toDispose = w;
      }

      public void run() {
			toDispose.dispose();
      }
    }
    SwingUtilities.invokeLater(new disposeIt(this));
    //#DOTNET_EXCLUDE_END
    /*#DOTNET_INCLUDE_BEGIN
	this.Close();
	this.Dispose();
	#DOTNET_INCLUDE_END*/
  }

  //#JAVA_EXCLUDE_BEGIN
  /*#DOTNET_INCLUDE_BEGIN
  	private void AgentTreeW_MouseDown (Object sender, System.Windows.Forms.MouseEventArgs e)
	{
		mainPanel.treeAgent.tree.set_SelectedNode( AgentTreeW.GetNodeAt(e.get_X(), e.get_Y() ) );
		TreeNode aNode = mainPanel.treeAgent.tree.get_SelectedNode();

		if (e.get_Button().equals( MouseButtons.Right) && aNode.get_Nodes().get_Count() == 0)
		{
			Agent agent = new Agent( mainPanel.treeAgent.tree.get_SelectedNode().get_Text() );
			popAg.setAgent( agent );
		}
	}

	private void CanvMessW_MouseDown (Object sender, System.Windows.Forms.MouseEventArgs e)
	{
		mainPanel.panelcan.canvMess.OnMousePressed(sender, e);
		mess = mainPanel.panelcan.canvMess.selMessage(e);
		
		if (mess == null)
			return;

		popMess.setMessage( mess );

		if ( e.get_Button().equals( MouseButtons.Right) )
		{
			popMess.Show(this, new Point(e.get_X(), e.get_Y() ) );
		}

	}

	private void CanvMessW_DoubleClick (Object sender, System.EventArgs e)
	{
		popMess.showMessage(sender, e);
	}
	
    private void OnClosing (Object sender, System.ComponentModel.CancelEventArgs e)
	{
		mySniffer.doDelete();
	}
  #DOTNET_INCLUDE_END*/
  //#JAVA_EXCLUDE_END
} 
