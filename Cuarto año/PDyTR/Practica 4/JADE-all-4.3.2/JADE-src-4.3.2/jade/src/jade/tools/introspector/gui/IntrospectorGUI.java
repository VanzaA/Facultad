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

import java.awt.*;
import java.awt.event.*;
import java.net.InetAddress;
import java.util.*;

import javax.swing.*;
import javax.swing.tree.*;

import jade.core.AID;
import jade.gui.AgentTreeModel;
import jade.gui.AgentTree;
import jade.gui.AboutJadeAction;
import jade.domain.introspection.*;

import jade.tools.introspector.Introspector;


/**
   This is the main window, containing the agent tree and a
   JDesktopPane to whom internal debugging window are added.

   @author Andrea Squeri, -  Universita` di Parma
*/
public class IntrospectorGUI extends JFrame implements WindowListener {

  private Introspector debugger;

  private TreePanel panel;
  private JDesktopPane desk;
  private JSplitPane split;
  private JScrollPane scroll;

  private JMenuBar bar;
  private JMenu menuFile;
  private JMenu menuAbout;
  private JMenuItem item;
 

  private String logoIntrospector =  "images/bug.gif";
  
public IntrospectorGUI(Introspector i) {

    debugger = i;

    panel = new TreePanel(this);
    panel.treeAgent.setNewPopupMenu(AgentTree.AGENT_TYPE, new TreeAgentPopupMenu(debugger, panel.treeAgent));

    scroll = new JScrollPane();
    desk = new JDesktopPane();
    split = new JSplitPane();

    bar = new JMenuBar();
    menuFile = new JMenu();
    item = new JMenuItem();
    menuAbout = new JMenu();


    build();

  }

  public void build(){
    String title = debugger.getAID().getName();
    this.setTitle(title);

    Image image = getToolkit().getImage(getClass().getResource(logoIntrospector));
    setIconImage(image);
    Font f = new Font("Monospaced", 0, 10);

    split.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
    split.setContinuousLayout(true);
    getContentPane().add(split);

    menuFile.setText("File");
    item.setText("Exit");
    menuFile.add(item);
    bar.add(menuFile);
    this.setJMenuBar(bar);
    menuAbout.setText("About");
    menuAbout.add(new AboutBoxAction(this));
    menuAbout.add(new AboutJadeAction(this));
    bar.add(menuAbout);
    
    	
    item.addActionListener(new ActionListener()  {
      public void actionPerformed(ActionEvent e) {
        exit_actionPerformed(e);
      }
    });

   
    scroll.getViewport().add(desk);

    split.add(panel, JSplitPane.LEFT);
    split.add(scroll,JSplitPane.RIGHT);
    split.setDividerLocation(180);
    this.addWindowListener(this);
    this.pack();
    this.setSize(new Dimension(680, 435));
    panel.adjustDividerLocation();
    setVisible(true);
  }


  void exit_actionPerformed(ActionEvent e){
    this.debugger.doDelete();
  }


  public JDesktopPane getDesktop(){
    return desk;
  }


  // interface WindowListener
  public void windowClosing(WindowEvent e){
    this.exit_actionPerformed(null);
  }

  public void windowClosed(WindowEvent e){}
  public void windowOpened(WindowEvent e){}
  public void windowIconified(WindowEvent e){}
  public void windowDeiconified(WindowEvent e){}
  public void windowDeactivated(WindowEvent e){}
  public void windowActivated(WindowEvent e){}

 public void showError(String errMsg) {
  JOptionPane.showMessageDialog(null, errMsg, "Error in " + debugger.getName(), JOptionPane.ERROR_MESSAGE);
 }

  
  // Methods called by the Introspector agent.

  public void messageSent(MainWindow f, SentMessage sm) {
    MessagePanel mp = f.getMessagePanel();
    SwingUtilities.invokeLater(new TableUpdater(mp, sm));
  }

  public void messagePosted(MainWindow f, PostedMessage pm) {
    MessagePanel mp = f.getMessagePanel();
    SwingUtilities.invokeLater(new TableUpdater(mp, pm));
  }

  public void messageReceived(MainWindow f, ReceivedMessage rm) {
    MessagePanel mp = f.getMessagePanel();
    SwingUtilities.invokeLater(new TableUpdater(mp, rm));
  }

  public void changedAgentState(MainWindow f, ChangedAgentState cas) {
    StatePanel sp = f.getStatePanel();
    SwingUtilities.invokeLater(new StateUpdater(sp, cas));
  }

  public void behaviourAdded(MainWindow f, AddedBehaviour e) {
      BehaviourPanel bp = f.getBehaviourPanel();
      SwingUtilities.invokeLater(new TreeUpdater(e,bp));
  }
  
  public void behaviourRemoved(MainWindow f, RemovedBehaviour e) {
      BehaviourPanel bp = f.getBehaviourPanel();
      SwingUtilities.invokeLater(new TreeUpdater(e,bp));
  }      
  
  public void behaviourChangeState(MainWindow f, ChangedBehaviourState e) {
      BehaviourPanel bp = f.getBehaviourPanel();
      SwingUtilities.invokeLater(new TreeUpdater(e,bp));
  }

    /*
  public void messageEvent(MessageEvent e, MainWindow f){
    MessagePanel mp=f.getMessagePanel();
    EventQueue.invokeLater(new TableUpdater(e, mp));
  }
  public void stateEvent(StateEvent e,MainWindow f){
    StatePanel sp=f.getStatePanel();
    EventQueue.invokeLater(new StateUpdater(e,sp));
  }


  //l'initEvent viene scomposto in tanti eventi
  //minori.Questo lo rende + lento ma meno complesso
  public void initEvent(InitEvent e,MainWindow f){

    Iterator it=e.getAllBehaviours();
    BehaviourPanel bp=f.getBehaviourPanel();
    while(it.hasNext()){
      BehaviourRapp b=(BehaviourRapp)it.next();
      BehaviourEvent event=new BehaviourEvent(b,false,true);
      EventQueue.invokeLater(new TreeUpdater(event,bp));
    }

    it=e.getAllinMessages();
    MessagePanel mp=f.getMessagePanel();
    while(it.hasNext()){
      MessageRapp b=(MessageRapp)it.next();
      MessageEvent event=new MessageEvent(b,true,true);
      EventQueue.invokeLater(new TableUpdater(event,mp));
    }

    it=e.getAlloutMessages();
    while(it.hasNext()){
      MessageRapp b=(MessageRapp)it.next();
      MessageEvent event=new MessageEvent(b,false,true);
      EventQueue.invokeLater(new TableUpdater(event,mp));
    }

    StateEvent se=new StateEvent(e.getState());
    StatePanel sp=f.getStatePanel();
    EventQueue.invokeLater(new StateUpdater(se,sp));
  }

    */

  // Adds a new InternalFrame (MainWindow)
  public void addWindow(MainWindow m) {
    desk.add(m);
    m.pack();
    m.setSize(600, 400);
    m.setVisible(true);
    m.adjustDividerLocation();

  }

  // Shuts down the desktop
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
    SwingUtilities.invokeLater(new disposeIt(this));
  }

  // Shuts down an InternalFrame
  public void closeInternal(MainWindow m){
    class DisposeItMain implements Runnable{
      MainWindow wnd;
      DisposeItMain(MainWindow l){
        wnd=l;
      }
      public void run(){
        wnd.dispose();
      }
    }
    EventQueue.invokeLater(new DisposeItMain(m));
  }


  // Methods for AgentTree management

  public void addAgent(final String containerName, final AID agentID) {

    // Add an agent to the specified container
    Runnable addIt = new Runnable() {
      public void run() {
	String agentName = agentID.getName();
       	//AgentTree.Node node = panel.treeAgent.createNewNode(agentName, 1);
        panel.treeAgent.addAgentNode(agentName, "agentAddress", containerName);
        //panel.treeAgent.addAgentNode((AgentTree.AgentNode)node, containerName, agentName, "agentAddress", "FIPAAGENT");
      }
    };
    SwingUtilities.invokeLater(addIt);
  }

  public void removeAgent(final String containerName, final AID agentID) {

    // Remove an agent from the specified container
    Runnable removeIt = new Runnable() {
      public void run() {
	String agentName = agentID.getName();
	panel.treeAgent.removeAgentNode(containerName, agentName);
      }
    };
    SwingUtilities.invokeLater(removeIt);
  }
 public AgentTreeModel getModel() {
    return panel.treeAgent.getModel();
  }

  public void resetTree() {
      Runnable resetIt = new Runnable() {

	  public void run() {
	      panel.treeAgent.clearLocalPlatform();
          }
      };
      SwingUtilities.invokeLater(resetIt);
  }

  public void addContainer(final String name, final InetAddress addr) {
    Runnable addIt = new Runnable() {
      public void run() {
        //MutableTreeNode node = panel.treeAgent.createNewNode(name, 0);
        //panel.treeAgent.addContainerNode((AgentTree.ContainerNode)node, "FIPACONTAINER", addr);
        panel.treeAgent.addContainerNode(name, addr);
      }
    };
    SwingUtilities.invokeLater(addIt);
  }

  public void removeContainer(final String name) {

    // Remove a container from the tree model
    Runnable removeIt = new Runnable() {

      public void run() {
       panel.treeAgent.removeContainerNode(name);
     }
    };
    SwingUtilities.invokeLater(removeIt);
  }


  public Introspector getAgent(){
    return debugger;
  }



}



