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

import javax.swing.*;
import java.awt.*;
import jade.core.Agent;
import jade.gui.GuiEvent;

/**
Javadoc documentation for the file
@author Fabio Bellifemine - CSELT S.p.A
@version $Date: 2001-10-26 18:00:40 +0200 (ven, 26 ott 2001) $ $Revision: 2827 $
*/

public class PasswordDialog extends JFrame // ModalDialog
{
  
  MeetingSchedulerAgent myAgent; // pointer to AppointmentAgent
  
public PasswordDialog(MeetingSchedulerAgent a, String title) {
this((JFrame)null,title);
  myAgent=a;
}
		public PasswordDialog(JFrame parent, String title)	{

	        setTitle(title);
		setSize(280, 130);
		getContentPane().setLayout(new BorderLayout());
		nameLabel = new JLabel("Name:");	
		passwordLabel = new JLabel("Password:");
		userTextField = new JTextField(1);
		userTextField.setText(title);
		userTextField.setPreferredSize(new Dimension(100,22));
		passTextField= new JPasswordField(1);
		passTextField.setPreferredSize(new Dimension(100,22));
		okButton = new JButton("OK");
		JPanel p1 = new JPanel();
		p1.setLayout(new BoxLayout(p1 , BoxLayout.Y_AXIS));
		JPanel p2 = new JPanel();
		p2.setLayout(new BoxLayout(p2 , BoxLayout.X_AXIS));
		nameLabel.setPreferredSize(new Dimension(75,15));
		p2.add(Box.createRigidArea(new Dimension(10,0)));
		p2.add(nameLabel);
		p2.add(Box.createHorizontalGlue());
		p2.add(userTextField);
		p2.add(Box.createRigidArea(new Dimension(10,0)));
		JPanel p3 = new JPanel();
		p3.setLayout(new BoxLayout(p3, BoxLayout.X_AXIS));
		p3.add(Box.createRigidArea(new Dimension(10,0)));
		passwordLabel.setPreferredSize(new Dimension(75,15));
		p3.add(passwordLabel);		
		p3.add(Box.createHorizontalGlue());
		p3.add(passTextField);
		p3.add(Box.createRigidArea(new Dimension(10,0)));
		JPanel p4 = new JPanel();
		p4.setLayout(new BoxLayout(p4, BoxLayout.X_AXIS));
		p4.add(Box.createHorizontalGlue());
		p4.add(okButton);
		p4.add(Box.createHorizontalGlue());
		p1.add(Box.createRigidArea(new Dimension(0,15)));
		p1.add(p2);
		p1.add(Box.createRigidArea(new Dimension(0,5)));
		p1.add(p3);
		p1.add(Box.createRigidArea(new Dimension(0,5)));
		p1.add(p4);
		p1.add(Box.createRigidArea(new Dimension(0,5)));
		getContentPane().add(p1);		



			
		//{{REGISTER_LISTENERS
		SymMouse aSymMouse = new SymMouse();
		okButton.addMouseListener(aSymMouse);
		SymAction lSymAction = new SymAction();
		userTextField.addActionListener(lSymAction);
		passTextField.addActionListener(lSymAction);
		//}}
	}

	public PasswordDialog(JFrame parent)
	{
		this(parent, "Username/Password");
	}

	// Add a constructor for Interactions (ignoring modal)
	public PasswordDialog(JFrame parent, boolean modal)
	{
		this(parent);
	}

	// Add a constructor for Interactions (ignoring modal)
	public PasswordDialog(JFrame parent, String title, boolean modal)
	{
		this(parent, title);
	}

	public String getUserName()
	{
		return userTextField.getText();
	}
	public char[] getPassword()
	{
		return passTextField.getPassword();
	}

	public void setUserName(String name)
	{
		userTextField.setText(name);
	}

	public void setPassword(String pass)
	{
		passTextField.setText(pass);
	}


	//{{DECLARE_CONTROLS
	JLabel nameLabel;
	JLabel passwordLabel;
	JTextField userTextField;
	JPasswordField passTextField;
	JButton  okButton;

	//}}

	class SymMouse extends java.awt.event.MouseAdapter
	{
		public void mouseClicked(java.awt.event.MouseEvent event)
		{
			Object object = event.getSource();
			if (object == okButton)
				okButton_MouseClicked(event);
		}
	}

	void okButton_MouseClicked(java.awt.event.MouseEvent event)
	{
		dispose();
		GuiEvent ev = new GuiEvent(this, myAgent.STARTTASKS);
		ev.addParameter(getUserName());
		myAgent.postGuiEvent(ev);
	}

	class SymAction implements java.awt.event.ActionListener
	{
		public void actionPerformed(java.awt.event.ActionEvent event)
		{
			Object object = event.getSource();
			if (object == userTextField)
				userTextField_EnterHit(event);
			else if (object == passTextField)
				passTextField_EnterHit(event);
		}
	}

	void userTextField_EnterHit(java.awt.event.ActionEvent event)
	{
		// Request the focus
		passTextField.requestFocus();
	}

	void passTextField_EnterHit(java.awt.event.ActionEvent event)
	{
	  okButton_MouseClicked(null);			 
	}
}
