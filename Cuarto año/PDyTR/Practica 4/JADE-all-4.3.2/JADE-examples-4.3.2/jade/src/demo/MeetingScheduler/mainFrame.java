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


package demo.MeetingScheduler;

import java.awt.*;
import java.util.*;
import java.util.*;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.IOException;

import jade.core.Agent;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.*; 
import demo.MeetingScheduler.Ontology.*;
import javax.swing.*;
import javax.swing.event.*;

import jade.gui.GuiEvent;
import CalendarBean.JCalendar;
import java.beans.*;
import java.text.*;
import java.text.DateFormat;
import java.util.EventListener;

/**
Javadoc documentation for the file
@author Fabio Bellifemine-Alberto Adorni-Luca Grulla-Gabriele Torelli
@version $Date: 2004-07-09 12:40:50 +0200 (ven, 09 lug 2004) $ $Revision: 5199 $
*/
public class mainFrame extends JFrame  {



 MeetingSchedulerAgent myAgent;
 int currentAction;   // indicates the action currently being executed
 final static int VIEWKNOWNPERSONS = 0;
 final static int VIEWKNOWNDF = 1;


public mainFrame(MeetingSchedulerAgent a, String title) {
  this(title);
  myAgent = a;
}


class Lis implements PropertyChangeListener {
	public void propertyChange(PropertyChangeEvent e) {
		calendar1_Action();
	}
}	
class NameListener implements ListSelectionListener {
	public void valueChanged(ListSelectionEvent e) {
		listNames_ItemStateChanged();
	}
}

public mainFrame() {
  //{{INIT_CONTROLS
  java.util.Locale.setDefault(Locale.US);
  getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
  getContentPane().setVisible(true);
  setSize(300,400);
  
  //pannello contenente la schermata principale
  p2=new JPanel();
  p2.setLayout(new BoxLayout(p2,BoxLayout.Y_AXIS));
  getContentPane().add(p2);
  p2.setVisible(true);
  p2.add(Box.createRigidArea(new Dimension(0,25)));
  
  calendar1 = new JCalendar();
  calendar1.setFont(new Font("Dialog", Font.BOLD, 10));
  calendar1.addPropertyChangeListener(new Lis());
  
  calendarPanel = new JPanel();
  calendarPanel.setLayout(new BoxLayout(calendarPanel , BoxLayout.X_AXIS));
  calendarPanel.add(Box.createRigidArea(new Dimension(15,0)));
  calendarPanel.add(calendar1);
  calendarPanel.add(Box.createRigidArea(new Dimension(15,0)));
  p2.add(calendarPanel);
  p2.add(Box.createRigidArea(new Dimension(0,10)));
  //area di note sugli appuntamenti
  description=new JLabel("Appointment Description");
  description.setAlignmentX(JComponent.CENTER_ALIGNMENT);
  p2.add(description);
  p2.add(Box.createRigidArea(new Dimension(0,5)));
  descriptionPanel = new JPanel();
  descriptionPanel.setLayout(new BoxLayout(descriptionPanel,BoxLayout.X_AXIS));
  descriptionPanel.add(Box.createRigidArea(new Dimension(15,0)));
  textArea1 = new JTextArea("",3,0);
  textArea1.setEditable(false);
  textArea1.setLineWrap(true);
  textArea1.setAlignmentX(JComponent.CENTER_ALIGNMENT);
  //dimensione TextArea descrizione appuntamenti
  textArea1.setMinimumSize(new Dimension(100,20));
  textArea1.setPreferredSize(new Dimension(700,80));
  textArea1.setMaximumSize(new Dimension(700,80));
  descriptionPanel.add(textArea1);
  descriptionPanel.add(Box.createRigidArea(new Dimension(15,0)));
  p2.add(descriptionPanel);
  
  //pannello *register with a facilitator* 
  facFrame = new JFrame();
  facFrame.setVisible(false);
  facFrame.setSize(400,150);
  p1=new JPanel();
  p1.setLayout(new BoxLayout(p1,BoxLayout.Y_AXIS));
  facFrame.getContentPane().add(p1);
  p1.setVisible(true);
  p1.add(Box.createRigidArea(new Dimension(0,20)));
  labelInsertDF = new JLabel("Insert agent address of the DF");
  labelInsertDF.setVisible(true);
  labelInsertDF.setAlignmentX(CENTER_ALIGNMENT);
  labelInsertDF.setFont(new Font("Dialog", Font.BOLD, 12));
  p1.add(labelInsertDF);
  p1.add(Box.createRigidArea(new Dimension(0,10)));
  textFieldDFaddress = new JTextField(80);
  textFieldDFaddress.setVisible(true);
  textFieldDFaddress.setMinimumSize(new Dimension(100,25));
  textFieldDFaddress.setPreferredSize(new Dimension(100,25));
  textFieldDFaddress.setMaximumSize(new Dimension(200,25));
  textFieldDFaddress.setAlignmentX(CENTER_ALIGNMENT);
  p1.add(textFieldDFaddress);
  p1.add(Box.createRigidArea(new Dimension(0,20)));
  facilitatorMessage = new JLabel("Press Enter when done");
  facilitatorMessage.setAlignmentX(JComponent.CENTER_ALIGNMENT);
  p1.add(facilitatorMessage);
  p1.add(Box.createRigidArea(new Dimension(0,20)));

  //pannello di *view known facilitator* e *view known persons*
  knowFrame = new JFrame();
  knowFrame.setVisible(false);
//  knowFrame.setTitle("view known");
  knowFrame.setSize(450,400);
  p3=new JPanel();
  p3.setLayout(new BoxLayout(p3,BoxLayout.Y_AXIS));
  p3.setVisible(true);
  knowFrame.getContentPane().add(p3);
  p3.add(Box.createRigidArea(new Dimension(0,20)));
  description=new JLabel();
  description.setVisible(true);
  description.setAlignmentX(JComponent.CENTER_ALIGNMENT);
  p3.add(description);
  knowFrame.setTitle("view known "+description.getText());
  p3.add(Box.createRigidArea(new Dimension(0,10)));
  textArea2=new JTextArea("",0,0);
  textArea2.setVisible(true);
  textArea2.setLineWrap(true);
  textArea2.setEditable(false);
  persPanel = new JPanel();
  persPanel.setLayout(new BoxLayout(persPanel , BoxLayout.X_AXIS));
  persPanel.add(Box.createRigidArea(new Dimension(15,0)));
  persPanel.add(textArea2);
  persPanel.add(Box.createRigidArea(new Dimension(15,0)));
  p3.add(persPanel);
  p3.add(Box.createRigidArea(new Dimension(0,35)));
  lModel = new DefaultListModel();
  listNames = new JList(lModel);
  //listNames.setMinimumSize(new Dimension(100,100));
  listNames.setFixedCellWidth(100);
  listNames.setVisibleRowCount(4);
  sPane = new JScrollPane(listNames , ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS , ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
  sPane.setVisible(true);
  //getContentPane().add(listNames);
  listPanel = new JPanel();
  listPanel.setLayout(new BoxLayout(listPanel , BoxLayout.X_AXIS));
  listPanel.add(Box.createRigidArea(new Dimension(15,0)));
  listPanel.add(sPane);
  listPanel.add(Box.createRigidArea(new Dimension(15,0)));
  p3.add(listPanel);
  p3.add(Box.createRigidArea(new Dimension(0,20)));
  doneButton = new JButton("Done");
  doneButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);
  doneButton.addActionListener(new SymAction());
  p3.add(doneButton);
  p3.add(Box.createRigidArea(new Dimension(0,20)));
  
  //pannello riportante le Infomrmazioni di sistema
  p4=new JPanel();
  p4.setLayout(new BoxLayout(p4,BoxLayout.Y_AXIS));
  p4.setVisible(true);
  getContentPane().add(p4);
  p4.add(Box.createRigidArea(new Dimension(0,20)));
  LInfo=new JLabel("Information");
  LInfo.setAlignmentX(JComponent.CENTER_ALIGNMENT);
  p4.add(LInfo);
  infoPanel = new JPanel();
  infoPanel.setLayout(new BoxLayout(infoPanel , BoxLayout.X_AXIS));
  infoPanel.add(Box.createRigidArea(new Dimension(10,0)));
  Info=new JTextField();
  Info.setVisible(true);
  Info.setEditable(false);
  
  
  Info.setFont(new Font("Dialog", Font.ITALIC, 10));
  Info.setForeground(new Color(0));
  Info.setBackground(new Color(16776960));
  Info.setAlignmentX(JComponent.CENTER_ALIGNMENT);
  infoPanel.add(Info);
  infoPanel.add(Box.createRigidArea(new Dimension(10,0)));
  p4.add(infoPanel);
  p4.add(Box.createRigidArea(new Dimension(0,10)));
  getContentPane().add(Box.createVerticalGlue());
  
  
  
  //{{INIT_MENUS
  mainMenuBar = new JMenuBar();
  menu1 = new JMenu("Directory");
  miRegWithDF = new JMenuItem("Register with a Facilitator");
  menu1.add(miRegWithDF);
  miViewDF = new JMenuItem("View Known Facilitators");
  menu1.add(miViewDF);
  menuItem3 = new JMenuItem("View Known Persons");
  menu1.add(menuItem3);
  menuItem4 = new JMenuItem("Update Known Persons with the Facilitators");
  menu1.add(menuItem4);
  
  mainMenuBar.add(menu1);
  appMenu = new JMenu("Appointment");
  menuItem5 = new JMenuItem("Show");
  appMenu.add(menuItem5);
  menuItem2 = new JMenuItem("Fix");
  appMenu.add(menuItem2);
  menuItem1 = new JMenuItem("Cancel");
  appMenu.add(menuItem1);
  mainMenuBar.add(appMenu);
  setJMenuBar(mainMenuBar);
		
  //{{REGISTER_LISTENERS
  SymWindow aSymWindow = new SymWindow();
  this.addWindowListener(aSymWindow);
  SymAction lSymAction = new SymAction();
  miViewDF.addActionListener(lSymAction);
  miRegWithDF.addActionListener(lSymAction);
  menuItem1.addActionListener(lSymAction);
  menuItem2.addActionListener(lSymAction);
  
  //listener di TextFieldAddress
  textFieldDFaddress.addActionListener(lSymAction);
  
  menuItem4.addActionListener(lSymAction);
  menuItem3.addActionListener(lSymAction);
  SymItem lSymItem = new SymItem();
  //listNames.addItemListener(lSymItem);
  listNames.addListSelectionListener(new NameListener());
  menuItem5.addActionListener(lSymAction);
  setLocation(50, 50);
}
	
public mainFrame(String title) {
  this();
  setTitle(title);
}
	
	public void addNotify()
	{
		// Record the size of the window prior to calling parents addNotify.
		Dimension d = getSize();
		
		super.addNotify();
	
		if (fComponentsAdjusted)
			return;
	
	}
	
	// Used for addNotify check.
	boolean fComponentsAdjusted = false;

  //{{DECLARE_CONTROLS
  JCalendar calendar1;
  JTextArea textArea1;
  JTextField textFieldErrMsg;
  JLabel labelInsertDF;
  JTextField textFieldDFaddress;
  JList listNames;
  DefaultListModel lModel;
  JPanel p1;
  JPanel p2;
  JPanel p3;
  JPanel p4;
  JPanel p5;
  JPanel calendarPanel;
  JPanel descriptionPanel;
  JPanel infoPanel;
  JPanel persPanel;
  JPanel listPanel;
  JTextArea textArea2;
  JLabel facilitatorMessage;
  JTextField Info;
  JLabel appointment;
  JLabel description;
  JLabel LInfo;
  JFrame facFrame;
  JFrame knowFrame;
  JButton doneButton;
  JScrollPane sPane;
  
  //{{DECLARE_MENUS
  JMenuBar mainMenuBar;
  JMenu menu1;
  JMenuItem miRegWithDF;
  JMenuItem miViewDF;
  JMenuItem menuItem3;
  JMenuItem menuItem4;
  JMenu appMenu;
  JMenuItem menuItem5;
  JMenuItem menuItem2;
  JMenuItem menuItem1;
	
class SymWindow extends java.awt.event.WindowAdapter {
	public void windowClosing(java.awt.event.WindowEvent event) {
  		  Object object = event.getSource();
		  if (object == mainFrame.this)
		    Frame1_WindowClosing(event);
	}
}
	
  void Frame1_WindowClosing(java.awt.event.WindowEvent event)
  {
    setVisible(false);	// hide the Frame
    dispose();			// free the system resources
    myAgent.doDelete();
  }
	
  class SymAction implements java.awt.event.ActionListener
  {
	public void actionPerformed(java.awt.event.ActionEvent event)    {
      Object object = event.getSource();
      if (object == miViewDF)
	miViewDF_Action(event);
      else if (object == miRegWithDF)
	miRegWithDF_Action(event);
      else if (object == menuItem1)
	menuItem1_ActionPerformed(event);
      else if (object == menuItem2)
	menuItem2_ActionPerformed(event);
      else if (object == menuItem4)
	menuItem4_ActionPerformed(event);
      else if (object == textFieldDFaddress)
	textFieldDFaddress_EnterHit(event);
      else if (object == menuItem3)
	menuItem3_ActionPerformed(event);
      else if (object == menuItem5)
	menuItem5_ActionPerformed(event);
      else if (object == doneButton)
	close_know_Frame();
    }
  }
	
	
	/** View Known DF */
	void miViewDF_Action(java.awt.event.ActionEvent event)
	{
	  clearFrame();
          knowFrame.setVisible(true);
	  description.setText("Known Facilitators");
	  //listNames.clearSelection();
	  //lModel.removeAllElements();	
	  lModel.clear();
	  for (Enumeration e = myAgent.getKnownDF(); e.hasMoreElements(); ) {
            //listNames.addItem(((AID)e.nextElement()).getName());
	    lModel.addElement(((AID)e.nextElement()).getName());
	  } 
	  currentAction = VIEWKNOWNDF;
	  //listNames.select(0);
	  listNames.setSelectedIndex(0);
	  listNames_ItemStateChanged(); 
	}
	
	//Chiude il Frame Known Persons e Known Facilitators	
	void close_know_Frame() {
		knowFrame.setVisible(false);
	}

	void miExit_Action(java.awt.event.ActionEvent event)
	{
	  // Action from Exit Create and show as modal
	  //System.err.println("miExit_ACtion");
	}
	
	void miRegWithDF_Action(java.awt.event.ActionEvent event)
	{ // Register with a DF
	  
	  clearFrame();
	   facFrame.setVisible(true);
	  /*
	  labelInsertDF.setVisible(true);
	  textFieldDFaddress.setVisible(true);	
	  */
	  textFieldDFaddress.requestFocus();
	  
	}

    /** View Known Persons **/
	void menuItem3_ActionPerformed(java.awt.event.ActionEvent event)
	{
	  clearFrame();
	  knowFrame.setVisible(true);
	  description.setText("Known persons");
	  //listNames.clearSelection();
	  //lModel.removeAllElements();
	  //lModel.removeAllElements();
	  lModel.clear();
	  for (Enumeration e = myAgent.getKnownPersons(); e.hasMoreElements(); ) {
            //listNames.addItem(((Person)e.nextElement()).getName());
	    lModel.addElement(((Person)e.nextElement()).getName());
	  }
	  currentAction=VIEWKNOWNPERSONS;
	  //listNames.select(0);
	  listNames.setSelectedIndex(0);
	  listNames_ItemStateChanged(); 
	}

	void calendar1_Action()
	{
	  Appointment a = null;
	  
	  clearFrame();
	  p2.setVisible(true);
	  p4.setVisible(true);
	  textArea1.setText("");
	  java.util.Calendar c = calendar1.getCalendar();
	  a = myAgent.getMyAppointment(c.getTime());
	  if (a != null) {
	      //System.out.println("C'e' un appuntamento");
            textArea1.setText(a.getDescription());
	  }
	}



    /**
      * This method sets to not visible all the components of this frame
      * except the Menu Bar.
      */
    void clearFrame () {
      knowFrame.setVisible(false);  
      facFrame.setVisible(false);
    }
    
	void menuItem1_ActionPerformed(java.awt.event.ActionEvent event)
	{ // Remove an appointment

	  calendar1_Action();
	  GuiEvent ev = new GuiEvent(this,myAgent.CANCELAPPOINTMENT);
	  ev.addParameter(calendar1.getCalendar().getTime());
	  myAgent.postGuiEvent(ev);
	  calendar1_Action();
	}

        /** Fix an appointment */
	void menuItem2_ActionPerformed(java.awt.event.ActionEvent event)
	{
	  calendar1_Action();
	  // Create and show the Frame
	  //System.out.println("Trasmetto : " + calendar1.getCalendar());
	 (new FixApp(myAgent, calendar1.getCalendar() )).setVisible(true);
	}

	
	/** This method is called to update the list of known persons */
	void menuItem4_ActionPerformed(java.awt.event.ActionEvent event)
	{
	  AID dfName; 
	  Enumeration e = myAgent.getKnownDF();
	  clearFrame();
	  //getContentPane().setSize(255,100);
	  while (e.hasMoreElements()) {
            dfName=(AID)e.nextElement();
	    GuiEvent ev = new GuiEvent(this,myAgent.SEARCHWITHDF);
	    ev.addParameter(dfName);
	    myAgent.postGuiEvent(ev);	  
	  } 
	}

	
	//stampa i msg di update
	void showErrorMessage(String text) {
	  //clearFrame();
	  p4.setVisible(true);
	  //textFieldErrMsg.setVisible(true);
	  Info.setText(text);
	  //System.err.println(text);
	}
//da sistemare..chiama anche lui showErrorMessage
	void textFieldDFaddress_EnterHit(java.awt.event.ActionEvent event)
	{
	  clearFrame();	 
	  GuiEvent ev = new GuiEvent(this,myAgent.REGISTERWITHDF);
	  ev.addParameter(textFieldDFaddress.getText());
	  myAgent.postGuiEvent(ev);	  
	}

	class SymItem implements java.awt.event.ItemListener
	{
		public void itemStateChanged(java.awt.event.ItemEvent event)
		{
			//Object object = event.getSource();
			//if (object == listNames)
				//listNames_ItemStateChanged(event);
		}
	}

	void listNames_ItemStateChanged()
	{
	  //String cur = listNames.getSelectedItem();
	  String cur = (String)lModel.getElementAt(listNames.getSelectedIndex());
	  if (currentAction == VIEWKNOWNPERSONS)
	    textArea2.setText(myAgent.getPerson(cur).toString());
	  else if (currentAction == VIEWKNOWNDF)
	    textArea2.setText(cur); 
	}

	void menuItem5_ActionPerformed(java.awt.event.ActionEvent event)
	{
		calendar1_Action();
	}
}

