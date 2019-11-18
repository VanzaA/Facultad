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
import java.awt.event.*;
import java.util.*;
import java.util.Enumeration;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.*;
import jade.gui.GuiEvent;
import demo.MeetingScheduler.Ontology.*;
import CalendarBean.JCalendar;
import java.text.DateFormat;
import java.text.*;

/**
Javadoc documentation for the file
@author Fabio Bellifemine - CSELT S.p.A
@version $Date: 2001-10-26 18:00:40 +0200 (ven, 26 ott 2001) $ $Revision: 2827 $
*/
public class FixApp extends JFrame
{

	class Close implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			errFrame.setVisible(false);
		}
	}		

	public FixApp()
	{


		setSize(530,600);
		setBackground(new Color(12632256));
		textArea1 = new JTextArea("",0,0);
		//prosegue le parole lunghe nella riga successiva
		textArea1.setLineWrap(true);
                textArea1.setText(" meets with ..");
		Border brd = BorderFactory.createMatteBorder( 1, 1, 1, 1, Color.black);
    		textArea1.setBorder(brd);
		appointmentScrollPane = new JScrollPane(textArea1,ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS , ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		port = appointmentScrollPane.getViewport();
		appointmentScrollPane.add(appointmentScrollPane.createVerticalScrollBar());
		//impostazione dimensioni componente TextArea1
		textArea1.setMinimumSize(new Dimension(350,100));
		textArea1.setPreferredSize(new Dimension(350,100));
		textArea1.setMaximumSize(new Dimension(550,250));
		calendar1 = new JCalendar();
		calendar1.setFont(new Font("Dialog", Font.BOLD, 10));		
		calendar2 = new JCalendar();
		calendar2.setFont(new Font("Dialog", Font.BOLD, 10));
		label2 = new JLabel("Starting On",JLabel.CENTER);
		label2.setFont(new Font("Dialog", Font.BOLD, 12));
		label4 = new JLabel("Ending With",JLabel.CENTER);
		label4.setFont(new Font("Dialog", Font.BOLD, 12));
		invitedModel = new DefaultListModel();
		listInvitedPersons = new JList(invitedModel);
		listInvitedPersons.setFixedCellWidth(70);
		listInvitedPersons.setVisibleRowCount(4);
		invitedScrollPane = new JScrollPane(listInvitedPersons,ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS , ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);				
		knownModel = new DefaultListModel();
		listKnownPersons = new JList(knownModel);
		listKnownPersons.setFixedCellWidth(70);
		listKnownPersons.setVisibleRowCount(4);
		knownScrollPane = new JScrollPane(listKnownPersons, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS , ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);								
		label1 = new JLabel("Known Persons",JLabel.CENTER);
		label1.setFont(new Font("Dialog", Font.BOLD, 12));
		label3 = new JLabel("Invited Persons",JLabel.CENTER);
		label3.setFont(new Font("Dialog", Font.BOLD, 12));
		buttonAddPerson = new JButton(">>");
		buttonAddPerson.setBackground(new Color(12632256));		
		buttonRemovePerson = new JButton("<<");
		buttonRemovePerson.setBackground(new Color(12632256));
		label5 = new JLabel("Appointment Description",JLabel.CENTER);
		label5.setFont(new Font("Dialog", Font.BOLD, 12));
		buttonOk = new JButton();
		buttonOk.setText("Ok");
		buttonOk.setBackground(new Color(12632256));	
		buttonExit = new JButton();
		buttonExit.setText("Exit");
		buttonExit.setBackground(new Color(12632256));		
		textFieldErrMsg = new JLabel();
		textFieldErrMsg.setVisible(true);
		setTitle("Fix New Appointment");
		errFrame = new JFrame();
		errFrame.setTitle("Error");
		errFrame.setSize(300,120);
		errFrame.setVisible(false);
		errFrame.getContentPane().setLayout(new BoxLayout(errFrame.getContentPane() , BoxLayout.Y_AXIS));
		errFrame.getContentPane().add(Box.createRigidArea(new Dimension(0,15)));		
		errFrame.getContentPane().add(textFieldErrMsg);
		errButton = new JButton("Ok");
		errButton.addActionListener(new Close());
		errButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		errPanel = new JPanel();
		errPanel.setLayout(new BoxLayout(errPanel , BoxLayout.X_AXIS));
		errPanel.add(Box.createHorizontalGlue());
		errPanel.add(errButton);
		errPanel.add(Box.createHorizontalGlue());
		errFrame.getContentPane().add(Box.createRigidArea(new Dimension(0,15)));		
		errFrame.getContentPane().add(errPanel);
		errFrame.getContentPane().add(Box.createRigidArea(new Dimension(0,15)));		


		//PANELS


		JPanel p1 = new JPanel();
		JPanel p2 = new JPanel();
		JPanel p3 = new JPanel();
		JPanel p4 = new JPanel();
		JPanel p5 = new JPanel();
		JPanel p6 = new JPanel();
		JPanel p7 = new JPanel();
		JPanel p8 = new JPanel();
		JPanel p9 = new JPanel();
		JPanel p10 = new JPanel();
		JPanel p11 = new JPanel();


		p2.setLayout(new BoxLayout(p2,BoxLayout.Y_AXIS));
		p2.add(Box.createRigidArea(new Dimension(0,20)));
		label5.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		p2.add(label5);
		p2.add(Box.createRigidArea(new Dimension(0,10)));		
		p11.add(Box.createRigidArea(new Dimension(10,0)));
		p11.add(appointmentScrollPane);
		p11.add(Box.createRigidArea(new Dimension(10,0)));
		p2.add(p11);
		p3.setLayout(new BoxLayout(p3,BoxLayout.Y_AXIS));
		p3.add(label2);
		label2.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		p3.add(Box.createRigidArea(new Dimension(0,5)));
		p3.add(calendar1);
		p4.setLayout(new BoxLayout(p4,BoxLayout.Y_AXIS));
		label4.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		p4.add(label4);
		p4.add(Box.createRigidArea(new Dimension(0,5)));
		p4.add(calendar2);
		p5.setLayout(new BoxLayout(p5 , BoxLayout.X_AXIS));
		p5.add(Box.createRigidArea(new Dimension(30,0)));
		p5.add(p3);
		p5.add(Box.createRigidArea(new Dimension(30,0)));
		p5.add(p4);
		p5.add(Box.createRigidArea(new Dimension(30,0)));
		p6.setLayout(new BoxLayout(p6,BoxLayout.Y_AXIS));
		label1.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		p6.add(label1);
		p6.add(Box.createRigidArea(new Dimension(0,5)));
		p6.add(knownScrollPane);
		//p6.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		p7.setLayout(new BoxLayout(p7,BoxLayout.Y_AXIS));
		label3.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		p7.add(label3);
		p7.add(Box.createRigidArea(new Dimension(0,5)));
		p7.add(invitedScrollPane);
		//p7.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		p8.setLayout(new BoxLayout(p8 , BoxLayout.Y_AXIS));
		p8.add(Box.createRigidArea(new Dimension(0,5)));
		p8.add(buttonAddPerson);
		p8.add(Box.createRigidArea(new Dimension(0,5)));
		p8.add(buttonRemovePerson);
		p8.add(Box.createRigidArea(new Dimension(0,5)));
		p8.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		p9.setLayout(new BoxLayout(p9 , BoxLayout.X_AXIS));
		p9.add(Box.createRigidArea(new Dimension(30,0)));
		p9.add(p6);
		p9.add(Box.createRigidArea(new Dimension(30,0)));
		p9.add(p8);
		p9.add(Box.createRigidArea(new Dimension(30,0)));
		p9.add(p7);
		p9.add(Box.createRigidArea(new Dimension(30,0)));
		p10.setLayout(new BoxLayout(p10 , BoxLayout.X_AXIS));
		p10.add(Box.createHorizontalGlue());
		p10.add(buttonOk);
		p10.add(Box.createRigidArea(new Dimension(10,0)));
		p10.add(buttonExit);
		p10.add(Box.createHorizontalGlue());
		p1.setLayout(new BoxLayout(p1 , BoxLayout.Y_AXIS));
		p2.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		p1.add(p2);
		p1.add(Box.createRigidArea(new Dimension(0,30)));
		p1.add(p5);
		p1.add(Box.createVerticalGlue());
		p1.add(p9);
		p1.add(Box.createRigidArea(new Dimension(0,30)));
		p1.add(p10);
		p1.add(Box.createRigidArea(new Dimension(0,20)));
		getContentPane().add(p1);



		//{{INIT_MENUS
		//}}

		//{{REGISTER_LISTENERS
		SymWindow aSymWindow = new SymWindow();
		this.addWindowListener(aSymWindow);
		SymMouse aSymMouse = new SymMouse();
		buttonExit.addMouseListener(aSymMouse);
		buttonOk.addMouseListener(aSymMouse);
		buttonAddPerson.addMouseListener(aSymMouse);
		buttonRemovePerson.addMouseListener(aSymMouse);
		//}}

		setLocation(50, 50);
	}

	public FixApp(String title)
	{
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


    MeetingSchedulerAgent myAgent; 
    public FixApp(MeetingSchedulerAgent a, Calendar selectedDate) {											
        this();
        myAgent = a;
	calendar1.setCalendar(selectedDate); 
	calendar2.setCalendar(selectedDate);
	Enumeration e = myAgent.getKnownPersons();
        knownModel.clear();
	int k = 0;
        while (e.hasMoreElements()) {
	  knownModel.addElement(((Person)e.nextElement()).getName());
        }
	
    }
    
    /**
     * Shows or hides the component depending on the boolean flag b.
     * @param b  if true, show the component; otherwise, hide the component.
     * @see java.awt.Component#isVisible
     */
  /**
    public void setVisible(boolean b)
	{
	  if(b)
	      setLocation(50, 50);
	  super.setVisible(b);
	}
	**/
	static public void main(String args[])
	{
		(new FixApp()).setVisible(true);
	}
	
  /**
   public void addNotify() {
     // Record the size of the window prior to calling parents addNotify.
     Dimension d = getSize();
     
     super.addNotify();

     if (fComponentsAdjusted)
       return;

     // Adjust components according to the insets
     setSize(insets().left + insets().right + d.width, insets().top + insets().bottom + d.height);
     Component components[] = getComponents();
     for (int i = 0; i < components.length; i++)
       {
	 Point p = components[i].getLocation();
	 p.translate(insets().left, insets().top);
	 components[i].setLocation(p);
       }
     fComponentsAdjusted = true;
   }
   
  // Used for addNotify check.
  boolean fComponentsAdjusted = false;
  **/
	//{{DECLARE_CONTROLS
	JTextArea textArea1;
	JCalendar calendar1;
	JCalendar calendar2;
	JLabel label2;
	JLabel label4;
	JList listInvitedPersons;
	DefaultListModel knownModel;
	DefaultListModel invitedModel;
	JList listKnownPersons;
	JLabel label1;
	JLabel label3;
	JButton buttonAddPerson;
	JButton buttonRemovePerson;
	JLabel label5;
	JButton buttonOk;
	JButton buttonExit;
	JButton errButton;
	JFrame errFrame;
	JLabel textFieldErrMsg;
	JScrollPane invitedScrollPane;
	JScrollPane knownScrollPane;
	JScrollPane appointmentScrollPane;
	JPanel errPanel;
	JViewport port;
	String[] data;
	//}}

	//{{DECLARE_MENUS
	//}}

	class SymWindow extends java.awt.event.WindowAdapter
	{
		public void windowClosing(java.awt.event.WindowEvent event)
		{
			Object object = event.getSource();
			if (object == FixApp.this)
				Frame1_WindowClosing(event);
		}
	}
	
	void Frame1_WindowClosing(java.awt.event.WindowEvent event)
	{
		setVisible(false);		 // hide the Frame
	}

	class SymMouse extends java.awt.event.MouseAdapter
	{
		public void mouseClicked(java.awt.event.MouseEvent event)
		{
			Object object = event.getSource();
			if (object == buttonExit)
				buttonExit_MouseClicked(event);
			else if (object == buttonOk)
				buttonOk_MouseClicked(event);
			else if (object == buttonAddPerson)
				buttonAddPerson_MouseClicked(event);
			else if (object == buttonRemovePerson)
				buttonRemovePerson_MouseClicked(event);
		}
	}

	void buttonExit_MouseClicked(java.awt.event.MouseEvent event)
	{
		// Invalidate the Frame
		dispose();
	}

	void buttonOk_MouseClicked(java.awt.event.MouseEvent event)
	{
	    Calendar c;
	    Date d;
	    //textFieldErrMsg.setVisible(false);	
	    		
	    //{{CONNECTION
	    Appointment a = new Appointment();
	    a.setInviter(myAgent.getAID());
	    a.setDescription(textArea1.getText());	    
	    c = calendar1.getCalendar();
	    d = c.getTime();
	    //System.out.println("Prova : " + d.toString());
	    a.setStartingOn(c.getTime());
	    c = calendar2.getCalendar();
	    a.setEndingWith(c.getTime());
	    for (int i=0; i<invitedModel.getSize(); i++) 
	      a.addInvitedPersons(myAgent.getPerson((String)invitedModel.get(i)));
	    try {
	      a.isValid();
	      //System.err.println(" Fixing appointment "+a.toString());
	      GuiEvent ev = new GuiEvent(this, myAgent.FIXAPPOINTMENT);
	      ev.addParameter(a);
	      myAgent.postGuiEvent(ev); 
	      dispose();
	    }
	    catch (Exception e) { 
	      showErrorMessage(e.getMessage());
	    }
	}

    void showErrorMessage(String msg) {
        textFieldErrMsg.setText(msg);
	errFrame.setVisible(true);
    }
    
	void buttonAddPerson_MouseClicked(java.awt.event.MouseEvent event)
	{
	  // Add a string to the List... Get the current item text
	  if (listKnownPersons.getSelectedValue() != null)
	    invitedModel.addElement(knownModel.get(listKnownPersons.getSelectedIndex()));
	}

	void buttonRemovePerson_MouseClicked(java.awt.event.MouseEvent event)
	{
	  // Delete an item from the List... Get the current item index
	  if (listInvitedPersons.getSelectedIndex() >= 0)
	    invitedModel.remove(listInvitedPersons.getSelectedIndex());
	}
}
