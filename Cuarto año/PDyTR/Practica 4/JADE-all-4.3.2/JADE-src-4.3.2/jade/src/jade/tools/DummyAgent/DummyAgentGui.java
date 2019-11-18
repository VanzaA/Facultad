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



package jade.tools.DummyAgent;

// Import required Java classes 
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import java.util.*;
import java.io.*;

// Import required Jade classes
import jade.core.*;
import jade.lang.acl.*;
import jade.gui.*;
import jade.domain.FIPAAgentManagement.Envelope;

import jade.util.Logger;

/**
@author Giovanni Caire - CSELT S.p.A
@version $Date: 2011-06-06 09:19:06 +0200(lun, 06 giu 2011) $ $Revision: 6415 $
*/
class DummyAgentGui extends JFrame 
{
	DummyAgent        myAgent;
	AID               agentName;
	AclGui            currentMsgGui;
	DefaultListModel  queuedMsgListModel;
	JList             queuedMsgList;
	File              currentDir;
  String 						logoDummy = "images/dummyagent.gif";
	DummyAgentGui thisGUI;
	
	//logging
	private Logger logger = Logger.getMyLogger(this.getClass().getName());
	
  // Constructor
	DummyAgentGui(DummyAgent a)
	{
		//////////////////////////
		// Call JFrame constructor
		super();

		thisGUI = this;
		
		//////////////////////////////////////////////////////////
		// Store pointer to the Dummy agent controlled by this GUI
		myAgent = a;

		/////////////////////////////////////////////////////////////////////
		// Get agent name and initialize the saving/opening directory to null 
		agentName = myAgent.getAID();
		currentDir = null;

		////////////////////////////////////////////////////////////////
		// Prepare for killing the agent when the agent window is closed
    		addWindowListener(new	WindowAdapter()
		                      	{
							// This is executed when the user attempts to close the DummyAgent 
							// GUI window using the button on the upper right corner
  							public void windowClosing(WindowEvent e) 
							{
								myAgent.doDelete();
							}
						} );

		//////////////////////////
		// Set title in GUI window
		try{
			setTitle(agentName.getName() + " - DummyAgent");
		}catch(Exception e){setTitle("DummyAgent");}
		
	  Image image = getToolkit().getImage(getClass().getResource(logoDummy));
    setIconImage(image);


		////////////////////////////////
		// Set GUI window layout manager
		getContentPane().setLayout(new BorderLayout());

		//////////////////////////////////////////////////////////////////////////////////////
		// Add the queued message scroll pane to the CENTER part of the border layout manager
		queuedMsgListModel = new DefaultListModel();
		queuedMsgList = new JList(queuedMsgListModel);
		queuedMsgList.setCellRenderer(new ToFromCellRenderer());
		JScrollPane pane = new JScrollPane();
		pane.getViewport().setView(queuedMsgList);
		getContentPane().add("Center", pane);

		///////////////////////////////////////////////////////////////////////////////////////////////////
		// Add the current message editing fields (an AclGui) to the WEST part of the border layout manager
		currentMsgGui = new AclGui(this);
		//currentMsgGui.setBorder(new TitledBorder("Current message"));
		ACLMessage msg = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
		
		msg.setSender(agentName);
		
		currentMsgGui.setMsg(msg);
		getContentPane().add("West", currentMsgGui);

		/////////////////////////////////////
		// Add main menu to the GUI window
		JMenuBar jmb = new JMenuBar();
		JMenuItem item;

		JMenu generalMenu = new JMenu ("General");
		generalMenu.add (item = new JMenuItem ("Exit"));
		Action exitAction = new AbstractAction("Exit"){
			public void actionPerformed(ActionEvent e)
			{
					myAgent.doDelete();
			}
		};
		item.addActionListener (exitAction);
		jmb.add (generalMenu);
		
    Icon resetImg = GuiProperties.getIcon("reset");
    Icon sendImg = GuiProperties.getIcon("send");
		Icon openImg = GuiProperties.getIcon("open");
    Icon saveImg = GuiProperties.getIcon("save");
    Icon openQImg = GuiProperties.getIcon("openq");
    Icon saveQImg = GuiProperties.getIcon("saveq");
    Icon setImg = GuiProperties.getIcon("set");
    Icon replyImg = GuiProperties.getIcon("reply");
    Icon viewImg = GuiProperties.getIcon("view");
    Icon deleteImg = GuiProperties.getIcon("delete");
    		
    JMenu currentMsgMenu = new JMenu ("Current message");
		currentMsgMenu.add (item = new JMenuItem ("Reset"));
		
		Action currentMessageAction = new AbstractAction("Current message", resetImg){
		public void actionPerformed(ActionEvent e)
		{
			ACLMessage m = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
			m.setSender(agentName);
	    m.setEnvelope(new jade.domain.FIPAAgentManagement.Envelope());	
			currentMsgGui.setMsg(m);
		}
		};
		

		item.addActionListener(currentMessageAction);
	  item.setIcon(resetImg);

	  currentMsgMenu.add (item = new JMenuItem ("Send"));
		Action sendAction = new AbstractAction("Send", sendImg){
			public void actionPerformed(ActionEvent e) {
			  ACLMessage m = currentMsgGui.getMsg();
			  queuedMsgListModel.add(0, (Object) new MsgIndication(m, MsgIndication.OUTGOING, new Date()));
		    StringACLCodec codec = new StringACLCodec();
		    try {
          String charset;  
          Envelope env;
          if (((env = m.getEnvelope()) == null) ||
              ((charset = env.getPayloadEncoding()) == null)) {
                charset = ACLCodec.DEFAULT_CHARSET;
              }
          codec.decode(codec.encode(m,charset),charset);
          myAgent.send(m);
		    } 
        catch (ACLCodec.CodecException ce) {	
		  	  if(logger.isLoggable(Logger.WARNING))
		  	  	logger.log(Logger.WARNING,"Wrong ACL Message " + m.toString());
			    ce.printStackTrace();
		      JOptionPane.showMessageDialog(null,"Wrong ACL Message: "+"\n"+ ce.getMessage(),"Error Message",JOptionPane.ERROR_MESSAGE);
        }
			}
		};
		
	  item.addActionListener (sendAction);
		item.setIcon(sendImg);
		
		currentMsgMenu.add (item = new JMenuItem ("Open"));
	  Action openAction = new AbstractAction("Open", openImg){
	  	public void actionPerformed(ActionEvent e)
	  	{
	  		JFileChooser chooser = new JFileChooser(); 
		  	if (currentDir != null)
				  chooser.setCurrentDirectory(currentDir); 
		  	int returnVal = chooser.showOpenDialog(null); 
			  if(returnVal == JFileChooser.APPROVE_OPTION)
			  {
				  currentDir = chooser.getCurrentDirectory();
				  String fileName = chooser.getSelectedFile().getAbsolutePath();

				try {
          // Note the save/read functionality uses default US-ASCII charset
          StringACLCodec codec = new StringACLCodec(new FileReader(fileName),null);
          currentMsgGui.setMsg(codec.decode());
				}
				catch(FileNotFoundException e1) {
						JOptionPane.showMessageDialog(null,"File not found: "+ fileName + e1.getMessage(),"Error Message",JOptionPane.ERROR_MESSAGE);
					if(logger.isLoggable(Logger.WARNING))
						logger.log(Logger.WARNING,"File Not Found: " + fileName); }
				catch (ACLCodec.CodecException e2) {
					if(logger.isLoggable(Logger.WARNING))
						logger.log(Logger.WARNING,"Wrong ACL Message in file: " +fileName);
					// e2.printStackTrace(); 
					JOptionPane.showMessageDialog(null,"Wrong ACL Message in file: "+ fileName +"\n"+ e2.getMessage(),"Error Message",JOptionPane.ERROR_MESSAGE);
				}
			  } 
	  	}
	  };
		item.addActionListener (openAction);
		item.setIcon(openImg);
		
		currentMsgMenu.add (item = new JMenuItem ("Save"));
		Action saveAction = new AbstractAction("Save", saveImg){
	  	public void actionPerformed(ActionEvent e)
	  	{
	  		JFileChooser chooser = new JFileChooser();
			  if (currentDir != null)
				  chooser.setCurrentDirectory(currentDir); 
			  int returnVal = chooser.showSaveDialog(null); 
			  if(returnVal == JFileChooser.APPROVE_OPTION)
			  {
			  	currentDir = chooser.getCurrentDirectory();
			  	String fileName = chooser.getSelectedFile().getAbsolutePath();

				  try {
				    FileWriter f = new FileWriter(fileName);
            // Note the save/read functionality uses default US-ASCII charset
					  StringACLCodec codec = new StringACLCodec(null,f);
				  	ACLMessage ACLmsg = currentMsgGui.getMsg();
				  	codec.write(ACLmsg);
            f.close();
				  }
				  catch(FileNotFoundException e3) { 
				  if(logger.isLoggable(Logger.WARNING))
				  	logger.log(Logger.WARNING,"Can't open file: " + fileName); }
				  catch(IOException e4) {
				  	if(logger.isLoggable(Logger.WARNING))
				  		logger.log(Logger.WARNING,"IO Exception"); }
			  } 
	  	}
		};
    item.addActionListener (saveAction);
		item.setIcon(saveImg);
		currentMsgMenu.addSeparator();
		jmb.add (currentMsgMenu);

		JMenu queuedMsgMenu = new JMenu ("Queued message");
		queuedMsgMenu.add (item = new JMenuItem ("Open queue"));
		Action openQAction = new AbstractAction("Open queue", openQImg){
	  public void actionPerformed(ActionEvent e)
	  {
	  	JFileChooser chooser = new JFileChooser(); 
			if (currentDir != null)
				chooser.setCurrentDirectory(currentDir); 
			int returnVal = chooser.showOpenDialog(null); 
			if(returnVal == JFileChooser.APPROVE_OPTION)
			{
				// Flush current queue
				for (int i = 0;i < queuedMsgListModel.getSize(); ++i)
				{
					queuedMsgListModel.removeElementAt(i);
				}

				currentDir = chooser.getCurrentDirectory();
				String fileName = chooser.getSelectedFile().getAbsolutePath();

				try
				{
					BufferedReader inp = new BufferedReader(new FileReader(fileName));
					// Read the number of messages in the queue
					int n = -1;
					try
					{
						Integer nn = new Integer(inp.readLine());
						n = nn.intValue();
					}
					catch(IOException ioEx) { 
					if(logger.isLoggable(Logger.WARNING))
						logger.log(Logger.WARNING,"IO Exception reading the number of messages in the queue"); }
					
					// Read the messages and insert them in the queue
					MsgIndication mi; 
					for (int i = 0;i < n; ++i)
					{
						mi = MsgIndication.fromText(inp);
						queuedMsgListModel.add(i, (Object) mi);
					}
				}
				catch(FileNotFoundException e5) { 
				if(logger.isLoggable(Logger.WARNING))
					logger.log(Logger.WARNING,"Can't open file: " + fileName); }
				catch(IOException e6) {
					if(logger.isLoggable(Logger.WARNING))
					 logger.log(Logger.WARNING,"IO Exception"); }
			} 

	  	}
		};
		item.addActionListener (openQAction);
		item.setIcon(openQImg);
		
		queuedMsgMenu.add (item = new JMenuItem ("Save queue"));
		Action saveQAction = new AbstractAction("Save queue", saveQImg){
	  public void actionPerformed(ActionEvent e)
	  {
	  	JFileChooser chooser = new JFileChooser(); 
			if (currentDir != null)
				chooser.setCurrentDirectory(currentDir); 
			int returnVal = chooser.showSaveDialog(null); 
			if(returnVal == JFileChooser.APPROVE_OPTION)
			{
				currentDir = chooser.getCurrentDirectory();
				String fileName = chooser.getSelectedFile().getAbsolutePath();

				try
				{
					BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
					// Write the number of messages in the queue
					try
					{
						out.write(String.valueOf(queuedMsgListModel.getSize()));
						out.newLine();
					}
					catch(IOException ioEx) { 
					if(logger.isLoggable(Logger.WARNING))
						logger.log(Logger.WARNING,"IO Exception writing the number of messages in the queue"); }

					// Write the messages
					MsgIndication mi;
					for (int i = 0;i < queuedMsgListModel.getSize(); ++i)
					{
						mi = (MsgIndication) queuedMsgListModel.get(i);
						mi.toText(out);
					}
				}
				catch(FileNotFoundException e5) { 
				if(logger.isLoggable(Logger.WARNING))
					logger.log(Logger.WARNING,"Can't open file: " + fileName); }
				catch(IOException e6) { 
				if(logger.isLoggable(Logger.WARNING))
					logger.log(Logger.WARNING,"IO Exception"); }
			} 

	  }
	  };
		item.addActionListener (saveQAction);
		item.setIcon(saveQImg);
		
		queuedMsgMenu.add (item = new JMenuItem ("Set as current"));
		Action setAction = new AbstractAction("Set as current", setImg){
	  public void actionPerformed(ActionEvent e)
	  {
	  	int i = queuedMsgList.getSelectedIndex();
			if (i != -1)
			{
				MsgIndication mi = (MsgIndication) queuedMsgListModel.getElementAt(i);
				ACLMessage m = mi.getMessage();
				currentMsgGui.setMsg(m);
			}

	  }
	  };

		item.addActionListener (setAction);
		item.setIcon(setImg);
		
		queuedMsgMenu.add (item = new JMenuItem ("Reply"));
    Action replyAction = new AbstractAction("Reply", replyImg){
	  public void actionPerformed(ActionEvent e)
	  {
	  	int i = queuedMsgList.getSelectedIndex();
			if (i != -1)
			{
				MsgIndication mi = (MsgIndication) queuedMsgListModel.getElementAt(i);
				ACLMessage m = mi.getMessage();
				ACLMessage reply = m.createReply();
				reply.setEnvelope(new jade.domain.FIPAAgentManagement.Envelope());
				//reply.setSender(myAgent.getAID());
				//currentMsgGui.setMsg(m.createReply());
				currentMsgGui.setMsg(reply);
			}

	  }
	  };
		item.addActionListener (replyAction);
		item.setIcon(replyImg);
		
		queuedMsgMenu.add (item = new JMenuItem ("View"));
		Action viewAction = new AbstractAction("View", viewImg){
	  public void actionPerformed(ActionEvent e)
	  {
	  	int i = queuedMsgList.getSelectedIndex();
			if (i != -1)
			{
				MsgIndication mi = (MsgIndication) queuedMsgListModel.getElementAt(i);
				ACLMessage m = mi.getMessage();
				AclGui.showMsgInDialog(m, thisGUI);
			}
	  }
	  };
		item.addActionListener (viewAction);
		item.setIcon(viewImg);
		
		queuedMsgMenu.add (item = new JMenuItem ("Delete"));
		Action deleteAction = new AbstractAction("Delete", deleteImg){
	  public void actionPerformed(ActionEvent e)
	  {
	  	int i = queuedMsgList.getSelectedIndex();
			if (i != -1)
			{
				queuedMsgListModel.removeElementAt(i);
			}

	  }
	  };
		item.addActionListener (deleteAction);
		item.setIcon(deleteImg);
		jmb.add (queuedMsgMenu);

		setJMenuBar(jmb);

		/////////////////////////////////////////////////////
		// Add Toolbar to the NORTH part of the border layout 
		JToolBar bar = new JToolBar();

		
		JButton resetB = new JButton();
	  //resetB.setText("Reset");
		resetB.setText("");
	  resetB.setIcon(resetImg);
		resetB.setToolTipText("New the current ACL message");
		resetB.addActionListener(currentMessageAction);
		bar.add(resetB);	
		
		JButton sendB = new JButton();
	  sendB.setText("");
		sendB.setIcon(sendImg);
		sendB.setToolTipText("Send the current ACL message");
		sendB.addActionListener(sendAction);
		bar.add(sendB);		
		
		JButton openB = new JButton();
		openB.setText("");
		openB.setIcon(openImg);
		openB.setToolTipText("Read the current ACL message from file");
		openB.addActionListener(openAction);
		bar.add(openB);

		
		JButton saveB = new JButton();
		saveB.setText("");
		saveB.setIcon(saveImg);
		saveB.setToolTipText("Save the current ACL message to file");
		saveB.addActionListener(saveAction);
		bar.add(saveB);

		bar.addSeparator(new Dimension(50,30));

	
		JButton openQB = new JButton();
	  openQB.setText("");
		openQB.setIcon(openQImg);
		openQB.setToolTipText("Read the queue of sent/received messages from file");
		openQB.addActionListener(openQAction);
		bar.add(openQB);


		JButton saveQB = new JButton();
	  saveQB.setText("");
		saveQB.setIcon(saveQImg);
		saveQB.setToolTipText("Save the queue of sent/received messages to file");
		saveQB.addActionListener(saveQAction);
		bar.add(saveQB);

		bar.addSeparator();
		
		JButton setB = new JButton();
		setB.setText("");
		setB.setIcon(setImg);
		setB.setToolTipText("Set the selected ACL message to be the current message");
		setB.addActionListener(setAction);
		bar.add(setB);


		JButton replyB = new JButton();
	  replyB.setText("");
		replyB.setIcon(replyImg);
		replyB.setToolTipText("Prepare a message to reply to the selected message");
		replyB.addActionListener(replyAction);
		bar.add(replyB);

		bar.addSeparator();
	
		JButton viewB = new JButton();
		viewB.setText("");
		viewB.setIcon(viewImg);
		viewB.setToolTipText("View the selected ACL message");
		viewB.addActionListener(viewAction);
		bar.add(viewB);


		JButton deleteB = new JButton();
	  deleteB.setText("");
		deleteB.setIcon(deleteImg);
		deleteB.setToolTipText("Delete the selected ACL message");
		deleteB.addActionListener(deleteAction);
		bar.add(deleteB);

		bar.add(Box.createHorizontalGlue());
		JadeLogoButton logo = new JadeLogoButton();
		bar.add(logo);
		getContentPane().add("North", bar);
	}

	void showCorrect()
	{
		///////////////////////////////////////////
		// Arrange and display GUI window correctly
		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int centerX = (int)screenSize.getWidth() / 2;
		int centerY = (int)screenSize.getHeight() / 2;
		setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
		setVisible(true);
	}

}
