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
import javax.swing.*;
import javax.swing.event.InternalFrameListener;
import javax.swing.event.InternalFrameEvent;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Vector;

import jade.core.AID;
import jade.tools.introspector.Introspector;
import jade.tools.introspector.Sensor;

/**
   Main Window class.

   @author Andrea Squeri,Corti Denis,Ballestracci Paolo -  Universita` di Parma

*/
public class MainWindow extends JInternalFrame implements InternalFrameListener
{

  private Sensor debuggerSensor;
  private AID debuggedID;
  private JSplitPane splitPanel;
  private MainBar mainBar;
  private MessagePanel messagePanel;
  private StatePanel statePanel;
  private BehaviourPanel behaviourPanel;
  private MainBarListener list;
  private int lastDividerLocation;

  public MainWindow(Sensor sn, AID id){
    super(id.getName());
    debuggerSensor = sn;
    debuggedID = id;
    MessageTableModel mi1 = new MessageTableModel(new Vector(), "Incoming Messages -- Pending");
    MessageTableModel mi2 = new MessageTableModel(new Vector(), "Incoming Messages --  Received");
    MessageTableModel mo1 = new MessageTableModel(new Vector(), "Outgoing Messages -- Pending");
    MessageTableModel mo2 = new MessageTableModel(new Vector(), "Outgoing Messages -- Sent");
    DefaultTreeModel r = new DefaultTreeModel(new DefaultMutableTreeNode("Behaviours"));
    int s = 1;

    list = new MainBarListener(this, debuggerSensor);
    mainBar = new MainBar(list);
    messagePanel = new MessagePanel(mi1, mi2, mo1, mo2);
    statePanel = new StatePanel(list);
    behaviourPanel = new BehaviourPanel(r);
    splitPanel = new JSplitPane();

    build();

  }

  public AID getDebugged() {
  	return debuggedID;
  }
  
  public void build() {

    /*layout*/
    this.getContentPane().setLayout(new BorderLayout());
    this.getContentPane().add(splitPanel, BorderLayout.CENTER);
    this.getContentPane().add(statePanel, BorderLayout.WEST);

    splitPanel.setOrientation(JSplitPane.VERTICAL_SPLIT);
    splitPanel.setContinuousLayout(true);

    this.setBehaviourPanelVisible(true);
    this.setMessagePanelVisible(true);

    this.setClosable(false);
    this.setIconifiable(true);
    this.setMaximizable(true);
    this.setResizable(true);
    this.setJMenuBar(mainBar);
  }

  public void adjustDividerLocation() {
    splitPanel.setDividerLocation(getHeight() / 2);
    lastDividerLocation = splitPanel.getDividerLocation();
  }

  public void setMessagePanelVisible(boolean b) {
    if(!b) {
      lastDividerLocation = splitPanel.getDividerLocation();
      splitPanel.remove(messagePanel);
    }
    else {
      splitPanel.add(messagePanel, JSplitPane.TOP);
      splitPanel.setDividerLocation(lastDividerLocation);
    }
  }

  public void setBehaviourPanelVisible(boolean b) {
    if(!b) {
      lastDividerLocation = splitPanel.getDividerLocation();
      splitPanel.remove(behaviourPanel);
    }
    else{
      splitPanel.add(behaviourPanel, JSplitPane.BOTTOM);
      splitPanel.setDividerLocation(lastDividerLocation);
    }
  }

  public MessagePanel getMessagePanel() {
    return messagePanel;
  }

  public BehaviourPanel getBehaviourPanel() {
    return behaviourPanel;
  }

  public StatePanel getStatePanel() {
    return statePanel;
  }

  //inerface InternalFrameListener

  public void internalFrameActivated(InternalFrameEvent e) {
    this.moveToFront();
  }

  public void internalFrameDeactivated(InternalFrameEvent e) {}
  public void internalFrameClosed(InternalFrameEvent e) {}
  public void internalFrameClosing(InternalFrameEvent e) {}
  public void internalFrameIconified(InternalFrameEvent e) {}
  public void internalFrameDeiconified(InternalFrameEvent e) {}
  public void internalFrameOpened(InternalFrameEvent e) {}
}

