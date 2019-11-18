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

// Import required java classes
import java.io.*;
import java.util.*;

import java.awt.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

// Import required JADE classes
import jade.core.*;
import jade.lang.acl.*;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.Envelope;
import jade.domain.FIPAAgentManagement.ReceivedObject;
import jade.tools.sl.SLFormatter;
import jade.domain.FIPANames;
import jade.util.Logger;

/**
 * The AclGui class extends the Swing JPanel class by adding all the controls 
 * required to properly edit/show the fields of an an ACL message   
 * compliant to the <b>FIPA 97</b> specs. <p>
 * <p>
 * There are basically two ways of using the AclGui class.
 * <ul>
 * <li> <b>Non Static Mode</b>. As AclGui extends JPanel, an
 * instance of AclGui can be directly added to whatever Container thus providing an easy way 
 * to permanently insert into a GUI a panel for the editing/display of an ACL message.<br>
 * The <em>setMsg()</em> and <em>getMsg()</em> methods can be used to display a given ACL message in the panel 
 * and to retrieve the ACL message currently displayed.<br>
 * The <em>setEnabled()</em> and <em>setSenderEnabled()</em> 
 * methods can be used to enable/disable modifications to all fields in the ACL message
 * and/or the sender field respectively.<br>
 * E.g.<br>
 * This code creates an agent GUI with a panel (in the left part of the GUI) that displays each new 
 * message received by the agent
 * <code>
 * .....<br>
 * AclGui acl;<br>
 * .....<br>
 * JFrame agentGui = new JFrame();<br>
 * agentGui.getContentPane().setLayout(new BorderLayout());<br>
 * acl = new AclGui();<br>
 * acl.setEnabled(false);<br>
 * agentGui.getContentPane().add("West", acl);<br>
 * .....<br>
 * </code>
 * Each time a new message is received (assuming the message has been stored 
 * in the msg variable of type ACLMessage)
 * <code>
 * acl.setMsg(msg);<br>
 * </code>
 * </li> 
 * <li> <b>Static Mode</b>. The AclGui class also provides the <em>editMsgInDialog()</em> and <em>showMsgInDlg()</em>
 * static methods that pop up a temporary dialog window (including an AclGui panel and the proper OK and
 * Cancel buttons) by means of which it is possible to edit and show a given ACL message.<br>
 * E.g.<br>
 * This code creates a button that allows the user to edit an ACL message 
 * by means of a temporary dialog window
 * <code>
 * .....<br>
 * ACLMessage msg;<br>
 * .....<br>
 * JButton b = new JButton("edit");<br>
 * b.addActionListener(new ActionListener()<br>
 * {<br>
 *    public void actionPerformed(ActionEvent e)<br>
 *    {<br>
 *      msg = AclGui.editMsgInDialog(new ACLMessage("", null);<br>
 *    }<br>
 * } );<br>
 * </code>
 * </li>
 * </ul>

 @author Giovanni Caire - CSELT
 @version $Date: 2010-07-14 18:59:27 +0200 (mer, 14 lug 2010) $ $Revision: 6360 $
 @see jade.lang.acl.ACLMessage

 */
public class AclGui extends JPanel
{
	// Controls for ACL message parameter editing
	static String ADD_NEW_RECEIVER = "Insert receiver"; 

	//the owner  of the panel.
	private Component ownerGui;

	//the logger
	private Logger logger = Logger.getMyLogger(this.getClass().getName());  
	
	AID SenderAID = new AID();
	AID newAIDSender = null;
	AID fromAID = new AID();
	AID newAIDFrom = null;
	
	VisualAIDList receiverListPanel;
	VisualAIDList replyToListPanel;
	VisualPropertiesList propertiesListPanel;
	
	private boolean      guiEnabledFlag;
	private JTextField   sender;
	private boolean      senderEnabledFlag;
	private JComboBox    communicativeAct;
	private JTextArea    content;
	private JTextField   language;
	private JTextField   ontology;
	private JComboBox    protocol;
	private JTextField   conversationId;
	private JTextField   inReplyTo;
	private JTextField   replyWith;
	private JTextField   replyBy;
	private JTextField   encoding;
	private JButton      replyBySet;
	private Date         replyByDate;
	private Date    dateDate;
	private Date    dateRecDate;
	
	// Data for panel layout definition
	GridBagLayout lm = new GridBagLayout();
	GridBagConstraints constraint = new GridBagConstraints();
	private int leftBorder, rightBorder, topBorder, bottomBorder;
	private int xSpacing, ySpacing;
	private int gridNCol, gridNRow;
	private int colWidth[];
	private static final int TEXT_SIZE = 30;

	private Vector fipaActVector;

	private static int    N_FIPA_PROTOCOLS = 8;
	private static String fipaProtocols[] = {FIPANames.InteractionProtocol.FIPA_ENGLISH_AUCTION,
		FIPANames.InteractionProtocol.FIPA_DUTCH_AUCTION,
		FIPANames.InteractionProtocol.FIPA_CONTRACT_NET,
		FIPANames.InteractionProtocol.FIPA_ITERATED_CONTRACT_NET,
		FIPANames.InteractionProtocol.FIPA_QUERY,
		FIPANames.InteractionProtocol.FIPA_REQUEST,
		FIPANames.InteractionProtocol.FIPA_REQUEST_WHEN,
		FIPANames.InteractionProtocol.FIPA_PROPOSE };

	private ArrayList fipaProtocolArrayList;


	// Data for the editing of user defined iteration protocols
	private int    lastSelectedIndex;
	private String lastSelectedItem;
	private static final String LABEL_TO_ADD_PROT = "ADD USER-DEF PROTOCOL";

	// These data are used to correctly handle the resizing of the AclGui panel
	private JPanel       aclPanel;
	private Dimension    minDim;
	private boolean      firstPaintFlag;

	// Other data used
	private static ACLMessage editedMsg;
	private JButton senderButton;
	private VisualAIDList toPanel;
	private JTextField from;
	private JTextArea comments;
	private JTextField representation;
	private JTextField payloadLength;
	private JTextField payloadEncoding;
	private JTextField date;
	private VisualAIDList intendedReceiverPanel;
	private JButton defaultEnvelopeButton;
	private JButton fromButton;
	private JButton dateButton;
	private JButton dateRecButton;
	private JTextField by;
	private JTextField fromRec;
	private JTextField dateRec;
	private JTextField via;
	private JTextField id;


	private class AclMessagePanel extends JPanel
	//#DOTNET_EXCLUDE_BEGIN
	implements DropTargetListener
	//#DOTNET_EXCLUDE_END
	{
		AclMessagePanel()
		{

			JLabel l;
			int    i;

			// Initialize the Vector of interaction protocols
			fipaProtocolArrayList = new ArrayList();

			for (i = 0;i < N_FIPA_PROTOCOLS; ++i)
				fipaProtocolArrayList.add((Object) fipaProtocols[i]);

			aclPanel = new JPanel();

			//#DOTNET_EXCLUDE_BEGIN
			new DropTarget(aclPanel, this);
			//#DOTNET_EXCLUDE_END

			aclPanel.setLayout(lm);

			formatGrid(20,   // N of rows 
					3,   // N of columns
					5,   // Right border 
					5,   // Left border
					5,   // Top boredr
					5,   // Bottom border
					2,   // Space between columns
					2);  // Space between rows
			setGridColumnWidth(0, 115);
			setGridColumnWidth(1, 40);
			setGridColumnWidth(2, 170);


			// Sender  (line # 0)
			l = new JLabel("Sender:");
			//#DOTNET_EXCLUDE_BEGIN
			new DropTarget(l, this);
			//#DOTNET_EXCLUDE_END
			put(aclPanel,l, 0, 0, 1, 1, false); 
			senderEnabledFlag = true; // The sender field is enabled by default, but can be disabled with the setSenderEnabled() method.
			sender = new JTextField();
			sender.setPreferredSize(new Dimension(80,26));
			sender.setMinimumSize(new Dimension(80,26));
			sender.setMaximumSize(new Dimension(80,26));
			sender.setEditable(false);
			sender.setBackground(Color.white);
			senderButton = new JButton("Set");
			senderButton.setMargin(new Insets(2,3,2,3));

			put(aclPanel,senderButton,1,0,1,1,false);
			put(aclPanel,sender, 2, 0, 1, 1, false);  

			senderButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e)
				{
					String command = e.getActionCommand();
					AIDGui guiSender = new AIDGui(getChildrenOwner());

					if(command.equals("Set"))
					{
						AID senderToView = SenderAID;
						//another sender was already inserted.
						if(newAIDSender != null)
							senderToView = newAIDSender;
						senderToView = guiSender.ShowAIDGui(senderToView,true,true);
						//if the cancel button was clicked --> maintain the old value inserted.
						if (senderToView != null)
						{newAIDSender = senderToView;
						//the name can be different
						sender.setText(newAIDSender.getName());
						}
					}
					else
						if(command.equals("View"))
							guiSender.ShowAIDGui(SenderAID, false,false);

				}
			});

			// Receiver (line # 1)
			l = new JLabel("Receivers:");
			//#DOTNET_EXCLUDE_BEGIN
			new DropTarget(l, this);
			//#DOTNET_EXCLUDE_END
			put(aclPanel,l,0,1,1,1,false);
			receiverListPanel = new VisualAIDList(new ArrayList().iterator(),getChildrenOwner());
			receiverListPanel.setDimension(new Dimension(205,37));
			put(aclPanel,receiverListPanel,1,1,2,1,false);


			//Reply-to (line #2)
			l = new JLabel("Reply-to:");
			//#DOTNET_EXCLUDE_BEGIN
			new DropTarget(l, this);
			//#DOTNET_EXCLUDE_END
			put(aclPanel,l, 0, 2, 1, 1,false);
			replyToListPanel = new VisualAIDList(new ArrayList().iterator(),getChildrenOwner());
			replyToListPanel.setDimension(new Dimension(205,37));
			put(aclPanel,replyToListPanel,1,2,2,1,false);

			// Communicative act (line # 3)
			l = new JLabel("Communicative act:");
			//#DOTNET_EXCLUDE_BEGIN
			new DropTarget(l, this);
			//#DOTNET_EXCLUDE_END
			put(aclPanel,l, 0, 3, 1, 1, false);  
			communicativeAct = new JComboBox(); 

			String[] comm_Act = ACLMessage.getAllPerformativeNames();
			for (int ii=0; ii<comm_Act.length; ii++)
				communicativeAct.addItem(comm_Act[ii].toLowerCase());

			communicativeAct.setSelectedIndex(0);
			put(aclPanel,communicativeAct, 1, 3, 2, 1, true);

			// Content (line # 4-8)
			l = new JLabel("Content:");
			//#DOTNET_EXCLUDE_BEGIN
			new DropTarget(l, this);
			//#DOTNET_EXCLUDE_END
			put(aclPanel,l, 0, 4, 3, 1, false);     
			content = new JTextArea(5,TEXT_SIZE);
			JScrollPane contentPane = new JScrollPane();
			contentPane.getViewport().setView(content);   
			put(aclPanel,contentPane, 0, 5, 3, 4, false);
			contentPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);    
			contentPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);    

			// Language (line # 9)
			l = new JLabel("Language:");
			//#DOTNET_EXCLUDE_BEGIN
			new DropTarget(l, this);
			//#DOTNET_EXCLUDE_END
			put(aclPanel,l, 0, 9, 1, 1, false);     
			language = new JTextField();
			language.setBackground(Color.white);
			put(aclPanel,language, 1, 9, 2, 1, false);  

			//Encoding (line # 10)
			l = new JLabel("Encoding:");
			//#DOTNET_EXCLUDE_BEGIN
			new DropTarget(l, this);
			//#DOTNET_EXCLUDE_END
			put(aclPanel,l, 0, 10, 1, 1, false);      
			encoding = new JTextField(); 
			encoding.setBackground(Color.white);
			put(aclPanel,encoding, 1, 10, 2, 1, false); 

			// Ontology (line # 11)
			l = new JLabel("Ontology:");
			//#DOTNET_EXCLUDE_BEGIN
			new DropTarget(l, this);
			//#DOTNET_EXCLUDE_END
			put(aclPanel,l, 0, 11, 1, 1, false);      
			ontology = new JTextField();
			ontology.setBackground(Color.white);
			put(aclPanel,ontology, 1, 11, 2, 1, false); 

			// Protocol (line # 12)
			l = new JLabel("Protocol:");
			//#DOTNET_EXCLUDE_BEGIN
			new DropTarget(l, this);
			//#DOTNET_EXCLUDE_END
			put(aclPanel,l, 0, 12, 1, 1, false);      
			protocol = new JComboBox();   
			for (i = 0;i < fipaProtocolArrayList.size(); ++i)
				protocol.addItem((String) fipaProtocolArrayList.get(i));
			protocol.addItem(LABEL_TO_ADD_PROT);
			protocol.addItem("Null");
			protocol.setSelectedItem("Null");
			lastSelectedIndex = protocol.getSelectedIndex();
			lastSelectedItem = (String) protocol.getSelectedItem();
			put(aclPanel,protocol, 1, 12, 2, 1, true);
			protocol.addActionListener( new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{    
					String param = (String) protocol.getSelectedItem();

					// BEFORE THE CURRENT SELECTION THE JComboBox WAS NON EDITABLE (a FIPA protocol or null was selected)
					if (!protocol.isEditable()) 
					{
						// If a user defined protocol has just been selected --> set editable to true
						if (fipaProtocolArrayList.indexOf((Object) param) < 0 && !param.equals("Null"))
						{
							protocol.setEditable(true);
						}
					}
					// BEFORE THE CURRENT SELECTION THE JComboBox WAS EDITABLE (an editable protocol was selected)
					else 
					{
						// The user selected a FIPA protocol or null (he didn't perform any editing operation) 
						if (fipaProtocolArrayList.indexOf((Object) param) >= 0 || param.equals("Null"))
						{
							protocol.setEditable(false);
							protocol.setSelectedItem(param);
						}
						// The user selected the label to add a new protocol (he didn't perform any editing operation) 
						else if (param.equals(LABEL_TO_ADD_PROT))
						{
							protocol.setSelectedItem(param);
						} 
						// The user added a new protocol
						else if (lastSelectedItem.equals(LABEL_TO_ADD_PROT))
						{     
							// The new protocol is actually added only if it is != "" and is not already present  
							if (!param.equals("")) 
							{
								protocol.addItem(param);
								int cnt = protocol.getItemCount();
								protocol.setSelectedItem(param);
								int n = protocol.getSelectedIndex();
								if (n != cnt-1)
									protocol.removeItemAt(cnt-1);
							}
							else 
							{
								protocol.setEditable(false);
								protocol.setSelectedItem("Null");
							}
						}
						// The user modified/deleted a previously added user defined protocol
						else if (lastSelectedItem != LABEL_TO_ADD_PROT)
						{
							protocol.removeItemAt(lastSelectedIndex);  // The old protocol is removed
							if (param.equals("")) // Deletion
							{
								protocol.setEditable(false);
								protocol.setSelectedItem("Null");
							}
							else // Modification
							{
								protocol.addItem(param);
								protocol.setSelectedItem(param);
							}
						}
					}

					lastSelectedIndex = protocol.getSelectedIndex();
					lastSelectedItem = (String) protocol.getSelectedItem();
				}
			} );

			// Conversation-id (line # 13)
			l = new JLabel("Conversation-id:");
			//#DOTNET_EXCLUDE_BEGIN
			new DropTarget(l, this);
			//#DOTNET_EXCLUDE_END
			put(aclPanel,l, 0, 13, 1, 1, false);      
			conversationId = new JTextField();
			conversationId.setBackground(Color.white);
			put(aclPanel,conversationId, 1, 13, 2, 1, false); 

			// In-reply-to (line # 14)
			l = new JLabel("In-reply-to:");
			//#DOTNET_EXCLUDE_BEGIN
			new DropTarget(l, this);
			//#DOTNET_EXCLUDE_END
			put(aclPanel,l, 0, 14, 1, 1, false);      
			inReplyTo = new JTextField();   
			inReplyTo.setBackground(Color.white);
			put(aclPanel,inReplyTo, 1, 14, 2, 1, false);  

			// Reply-with (line # 15)
			l = new JLabel("Reply-with:");
			//#DOTNET_EXCLUDE_BEGIN
			new DropTarget(l, this);
			//#DOTNET_EXCLUDE_END
			put(aclPanel,l, 0, 15, 1, 1, false);      
			replyWith = new JTextField();   
			replyWith.setBackground(Color.white);
			put(aclPanel,replyWith, 1, 15, 2, 1, false);  

			// Reply-by (line # 16)
			replyByDate = null;
			l = new JLabel("Reply-by:");
			//#DOTNET_EXCLUDE_BEGIN
			new DropTarget(l, this);
			//#DOTNET_EXCLUDE_END
			put(aclPanel,l, 0, 16, 1, 1, false);
			replyBySet = new JButton("Set");
			replyBySet.setMargin(new Insets(2,3,2,3));
			replyBy = new JTextField();
			replyBy.setBackground(Color.white);
			put(aclPanel,replyBySet, 1, 16, 1, 1, false);
			put(aclPanel,replyBy, 2, 16, 1, 1, false);  
			replyBySet.addActionListener(new  ActionListener()
			{ // BEGIN anonumous class
				public void actionPerformed(ActionEvent e)
				{
					String command = e.getActionCommand();
					//TimeChooser t = new TimeChooser(replyByDate);
					TimeChooser t = new TimeChooser();
					String d = replyBy.getText();
					if (!d.equals(""))
					{
						try
						{
							t.setDate(ISO8601.toDate(d));
						}
						catch (Exception ee) { 
							if(logger.isLoggable(Logger.SEVERE))
								logger.log(Logger.WARNING,"Incorrect date format"); }
					}
					if (command.equals("Set"))
					{
						if (t.showEditTimeDlg(null) == TimeChooser.OK)
						{
							replyByDate = t.getDate();
							if (replyByDate == null)
								replyBy.setText("");
							else
								replyBy.setText(ISO8601.toString(replyByDate));
						}
					}
					else if (command.equals("View"))
					{         
						t.showViewTimeDlg(null);
					}
				} // END actionPerformed(ActionEvent e)
			} // END anonymous class
			);


			//Properties (line #17)
			l = new JLabel("User Properties:");
			//#DOTNET_EXCLUDE_BEGIN
			new DropTarget(l, this);
			//#DOTNET_EXCLUDE_END
			put(aclPanel,l, 0, 17, 1, 1, false);
			propertiesListPanel = new VisualPropertiesList(new Properties(),getChildrenOwner());
			propertiesListPanel.setDimension(new Dimension(205,37));
			put(aclPanel,propertiesListPanel,1,17,2,1,false);

			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			add(aclPanel);

		} // END AclMessagePanel()

		//#DOTNET_EXCLUDE_BEGIN
		public void dragOver(java.awt.dnd.DropTargetDragEvent p1) {
			if(logger.isLoggable(Logger.FINEST))
				logger.log(Logger.FINEST,"dragOver");
		}    

		public void dropActionChanged(java.awt.dnd.DropTargetDragEvent p1) {
			if(logger.isLoggable(Logger.FINEST))
				logger.log(Logger.FINEST,"dropActionChanged");
		}

		public void dragEnter(java.awt.dnd.DropTargetDragEvent dragEvent) {
			if(logger.isLoggable(Logger.FINEST))
				logger.log(Logger.FINEST,"dragEnter");
		}

		public void dragExit(java.awt.dnd.DropTargetEvent p1) {
			if(logger.isLoggable(Logger.FINEST))
				logger.log(Logger.FINEST,"dragExit");
		}

		public void drop(java.awt.dnd.DropTargetDropEvent dropEvent)
		{
			boolean completionStatus = false;
			java.util.List fileList = null;

			try {
				dropEvent.acceptDrop(DnDConstants.ACTION_COPY);

				Transferable xferInfo = dropEvent.getTransferable();
				fileList = (java.util.List)(xferInfo.getTransferData(DataFlavor.javaFileListFlavor));
				completionStatus = true;
			}
			catch (UnsupportedFlavorException exc) {
				completionStatus = false;
			}
			catch (IOException exc) {
				if(logger.isLoggable(Logger.WARNING))
					logger.log(Logger.WARNING,"DragAndDrop operation failed: " + exc);
				completionStatus = false;
			}
			finally {
				dropEvent.dropComplete(completionStatus);
			}

			if (fileList != null)
			{
				Iterator fileItor = fileList.iterator();
				ACLParser aclParser = ACLParser.create();
				while (fileItor.hasNext())
				{
					try {
						java.io.File f = (java.io.File)(fileItor.next());
						FileReader aclMsgFile = new FileReader(f);
						Enumeration receivers = receiverListPanel.getContent();
						setMsg( aclParser.parse(aclMsgFile) );
						if ( receivers.hasMoreElements() ) {
							if(logger.isLoggable(Logger.FINE))
								logger.log(Logger.FINE,"revert to saved list");
							ArrayList list = new ArrayList();
							while(receivers.hasMoreElements()) {
								list.add(receivers.nextElement());
							}
							receiverListPanel.resetContent(list.iterator());
						}
					}
					catch (IOException exc) {
						if(logger.isLoggable(Logger.WARNING))
							logger.log(Logger.WARNING,"DragAndDrop operation failed: " + exc);
					}
					catch (ParseException exc) {
						if(logger.isLoggable(Logger.WARNING))
							logger.log(Logger.WARNING,"DragAndDrop operation failed: " + exc);
					}
					catch (Exception exc) {
						if(logger.isLoggable(Logger.WARNING))
							logger.log(Logger.WARNING,"DragAndDrop operation failed: " + exc);
					}
					catch (Error exc) {
						if(logger.isLoggable(Logger.WARNING))
							logger.log(Logger.WARNING,"DragAndDrop operation failed: " + exc);
					}
					catch (Throwable exc) {
						if(logger.isLoggable(Logger.WARNING))
							logger.log(Logger.WARNING,"DragAndDrop operation failed: " + exc);
					}
				} //~ while (fileItor.hasNext())
			} //~ if (selectedItems != null)

		} // END drop(dropEvent)
		//#DOTNET_EXCLUDE_END

	} // END class AclMessagePanel

	//this private class build a panel to show the envelope field of an ACLMessage
	private class EnvelopePanel extends JPanel
	{
		EnvelopePanel()
		{

			JLabel l;
			int    i;

			//minDim = new Dimension();
			aclPanel = new JPanel();
			//aclPanel.setBackground(Color.lightGray); 
			aclPanel.setLayout(lm);

			formatGrid(20,   // N of rows 
					3,   // N of columns
					5,   // Right border 
					5,   // Left border
					5,   // Top boredr
					5,   // Bottom border
					2,   // Space between columns
					2);  // Space between rows
			setGridColumnWidth(0, 115);
			setGridColumnWidth(1, 40);
			setGridColumnWidth(2, 170);

			// To  (line # 0)
			l = new JLabel("To:");
			put(aclPanel,l, 0, 0, 1, 1, false); 
			toPanel = new VisualAIDList(new ArrayList().iterator(),getChildrenOwner());
			toPanel.setDimension(new Dimension(205,37));  
			put(aclPanel,toPanel, 1, 0, 2, 1, false); 

			//From (line #1)
			l = new JLabel("From:");
			put(aclPanel,l, 0, 1, 1, 1,false);
			fromButton = new JButton("Set");
			fromButton.setMargin(new Insets(2,3,2,3));
			put(aclPanel,fromButton,1,1,1,1,false);
			from = new JTextField();
			from.setEditable(false);
			from.setBackground(Color.white);
			put(aclPanel,from,2,1,1,1,false);
			fromButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					String command = e.getActionCommand();
					AIDGui guiFrom = new AIDGui(ownerGui);
					if(command.equals("Set"))
					{
						AID fromToView = fromAID;
						if (newAIDFrom != null)
							fromToView = newAIDFrom;
						fromToView = guiFrom.ShowAIDGui(fromToView,true,true);
						if(fromToView != null)
						{
							newAIDFrom = fromToView;
							from.setText(newAIDFrom.getName());
						}
					}else
					{
						if(command.equals("View"))
							guiFrom.ShowAIDGui(fromAID,false,false);
					}
				}
			});

			//Comments (line # 2-6)
			l = new JLabel("Comments:");
			put(aclPanel,l,0,2,1,1,false);
			comments = new JTextArea(4,TEXT_SIZE);
			JScrollPane commentsPane = new JScrollPane();
			commentsPane.getViewport().setView(comments);
			put(aclPanel,commentsPane,0,3,3,4,false);
			commentsPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			commentsPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

			//aclRappresentation (line # 7)
			l = new JLabel("ACLRepresentation:");
			put(aclPanel,l, 0, 7, 1, 1, false);     
			representation = new JTextField(); 
			representation.setBackground(Color.white);
			put(aclPanel,representation, 1, 7, 2, 1, false);  

			//payloadLength (line # 8)
			l = new JLabel("Payload Length:");
			put(aclPanel,l, 0, 8, 1, 1, false);     
			payloadLength = new JTextField(); 
			payloadLength.setBackground(Color.white);
			put(aclPanel,payloadLength, 1, 8, 2, 1, false);

			//payloadEncoding (line # 9)
			l = new JLabel("Payload Encoding:");
			put(aclPanel,l, 0, 9, 1, 1, false);     
			payloadEncoding = new JTextField(); 
			payloadEncoding.setBackground(Color.white);
			put(aclPanel,payloadEncoding, 1, 9, 2, 1, false);

			//Date (line # 10)
			dateDate = null;
			l = new JLabel("Date:");
			put(aclPanel,l, 0, 10, 1, 1, false);
			dateButton = new JButton("Set");
			dateButton.setMargin(new Insets(2,3,2,3));
			date = new JTextField();
			date.setBackground(Color.white);
			put(aclPanel,dateButton, 1, 10, 1, 1, false);
			put(aclPanel,date, 2, 10, 1, 1, false); 
			dateButton.addActionListener(new  ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					String command = e.getActionCommand();
					//TimeChooser t = new TimeChooser(replyByDate);
					TimeChooser t = new TimeChooser();
					String d = date.getText();
					if (!d.equals(""))
					{
						try
						{
							t.setDate(ISO8601.toDate(d));
						}
						catch (Exception ee) { 
							if(logger.isLoggable(Logger.WARNING))
								logger.log(Logger.WARNING,"Incorrect date format"); }
					}
					if (command.equals("Set"))
					{
						if (t.showEditTimeDlg(null) == TimeChooser.OK)
						{
							dateDate = t.getDate();
							if (dateDate == null)
								date.setText("");
							else
								date.setText(ISO8601.toString(dateDate));
						}
					}
					else if (command.equals("View"))
					{         
						t.showViewTimeDlg(null);
					}
				}
			} );

			//intendedReceiver (line #11)
			l = new JLabel("Intended Receiver:");
			put(aclPanel,l,0,11,1,1,false);
			intendedReceiverPanel = new VisualAIDList(new ArrayList().iterator(),getChildrenOwner());
			intendedReceiverPanel.setDimension(new Dimension(205,37));
			put(aclPanel,intendedReceiverPanel, 1, 11,2,1,false);

			//ReceivedObject (line #12-15)
			JPanel recPanel = new JPanel();
			recPanel.setLayout(new BoxLayout(recPanel,BoxLayout.Y_AXIS));
			JPanel tempPane = new JPanel();
			tempPane.setLayout(new BoxLayout(tempPane,BoxLayout.X_AXIS));
			recPanel.setBorder(new TitledBorder("Received Object"));
			l = new JLabel("By:");
			l.setPreferredSize(new Dimension(115,24));
			l.setMinimumSize(new Dimension(115,24));
			l.setMaximumSize(new Dimension(115,24));
			tempPane.add(l);
			by = new JTextField();
			by.setBackground(Color.white);
			tempPane.add(by);
			recPanel.add(tempPane);
			tempPane = new JPanel();
			tempPane.setLayout(new BoxLayout(tempPane,BoxLayout.X_AXIS));

			l = new JLabel("From:");
			l.setPreferredSize(new Dimension(115,24));
			l.setMinimumSize(new Dimension(115,24));
			l.setMaximumSize(new Dimension(115,24));
			tempPane.add(l);
			fromRec = new JTextField();
			fromRec.setBackground(Color.white);
			tempPane.add(fromRec);
			recPanel.add(tempPane);

			tempPane = new JPanel();
			tempPane.setLayout(new BoxLayout(tempPane,BoxLayout.X_AXIS));

			dateRecDate = null;
			l = new JLabel("Date:");
			l.setPreferredSize(new Dimension(115,24));
			l.setMinimumSize(new Dimension(115,24));
			l.setMaximumSize(new Dimension(115,24));
			tempPane.add(l);
			dateRecButton = new JButton("Set");
			tempPane.add(dateRecButton);
			dateRecButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					String command = e.getActionCommand();
					//TimeChooser t = new TimeChooser(replyByDate);
					TimeChooser t = new TimeChooser();
					String d = dateRec.getText();
					if (!d.equals(""))
					{
						try
						{
							t.setDate(ISO8601.toDate(d));
						}
						catch (Exception ee) { 
							if(logger.isLoggable(Logger.WARNING))
								logger.log(Logger.WARNING,"Incorrect date format"); }
					}
					if (command.equals("Set"))
					{
						if (t.showEditTimeDlg(null) == TimeChooser.OK)
						{
							dateRecDate = t.getDate();
							if (dateRecDate == null)
								dateRec.setText("");
							else
								dateRec.setText(ISO8601.toString(dateRecDate));
						}
					}
					else if (command.equals("View"))
					{         
						t.showViewTimeDlg(null);
					}
				}
			} );

			dateRec = new JTextField();
			dateRec.setBackground(Color.white);
			tempPane.add(dateRec);
			recPanel.add(tempPane);

			tempPane = new JPanel();
			tempPane.setLayout(new BoxLayout(tempPane,BoxLayout.X_AXIS));

			l = new JLabel("ID:");
			l.setPreferredSize(new Dimension(115,24));
			l.setMinimumSize(new Dimension(115,24));
			l.setMaximumSize(new Dimension(115,24));
			tempPane.add(l);
			id = new JTextField();
			id.setBackground(Color.white);
			tempPane.add(id);
			recPanel.add(tempPane);

			tempPane = new JPanel();
			tempPane.setLayout(new BoxLayout(tempPane,BoxLayout.X_AXIS));

			l = new JLabel("Via:");
			l.setPreferredSize(new Dimension(115,24));
			l.setMinimumSize(new Dimension(115,24));
			l.setMaximumSize(new Dimension(115,24));
			tempPane.add(l);
			via = new JTextField();
			via.setBackground(Color.white);
			tempPane.add(via);
			recPanel.add(tempPane);

			put(aclPanel,recPanel,0,12,3,1,false);

			//(line 17)
			JPanel tmpPanel = new JPanel();
			//tmpPanel.setBackground(Color.lightGray);
			defaultEnvelopeButton = new JButton("Set Default Envelope");
			tmpPanel.add(defaultEnvelopeButton);

			defaultEnvelopeButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e)
				{
					String command = e.getActionCommand();
					if(command.equals("Set Default Envelope"))
					{
						ACLMessage tmp = getMsg();
						tmp.setDefaultEnvelope();
						Envelope envtmp = tmp.getEnvelope();
						showEnvelope(envtmp);
					}
				}
			});
			put(aclPanel,tmpPanel,0,17,3,1,false);
			//setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			add(aclPanel);


		}

	}


	/////////////////
	// CONSTRUCTOR
	/////////////////
	/**
    Ordinary <code>AclGui</code> constructor.
    @see jade.lang.acl.ACLMessage#ACLMessage(int)
	 */

	public AclGui(Component owner)
	{ 

		firstPaintFlag = true;
		guiEnabledFlag = true;
		minDim = new Dimension();
		ownerGui = owner;

		JTabbedPane tabbed = new JTabbedPane();
		AclMessagePanel aclPane = new AclMessagePanel();
		EnvelopePanel envelope = new EnvelopePanel();
		tabbed.addTab("ACLMessage",aclPane);
		tabbed.addTab("Envelope",envelope);
		//to enable the textfields if needed.
		updateEnabled();  
		add(tabbed);

		// Try inserting formatted SL content.
		// any Exception is catched in order to remove unwished dependency
		// on the jade.tools.sl package from this package at run-time.
		try { 
			slFormatter = new SLFormatter();
		} catch (Exception e) {
		}
	}
	
	private Component getChildrenOwner() {
		// If we have a parent return it, else return myself
		return ownerGui != null ? ownerGui : this;
	}
	
	SLFormatter slFormatter;
	////////////////////
	// PRIVATE METHODS
	////////////////////
	private void formatGrid(int nr, int nc, int lb, int rb, int tb, int bb, int xs, int ys)
	{
		gridNRow = nr;
		gridNCol = nc;
		colWidth = new int[3];
		//colWidth[0] = 120;
		//colWidth[1] = 63;
		//colWidth[2] = 180;
		leftBorder = lb;
		rightBorder = rb;
		topBorder = tb;
		bottomBorder = bb;
		xSpacing = xs;
		ySpacing = ys;
	}

	private void setGridColumnWidth(int col, int width)
	{
		colWidth[col] = width;
	}

	private void put(JPanel panel, JComponent c, int x, int y, int dx, int dy, boolean fill)
	{
		int leftMargin, rightMargin, topMargin, bottomMargin;
		int preferredWidth, preferredHeight;

		constraint.gridx = x;
		constraint.gridy = y;
		constraint.gridwidth = dx;
		constraint.gridheight = dy;
		constraint.anchor = GridBagConstraints.WEST;
		if (fill)
			constraint.fill = GridBagConstraints.BOTH;
		else
			constraint.fill = GridBagConstraints.VERTICAL;

		leftMargin =   (x == 0 ? leftBorder : 0);
		rightMargin =  (x+dx == gridNCol ? rightBorder : xSpacing);
		topMargin =    (y == 0 ? topBorder : 0);
		bottomMargin = (y+dy == gridNRow ? bottomBorder : ySpacing);

		int i;
		preferredWidth = 0; 
		for (i = 0; i < dx; ++i)
			preferredWidth += colWidth[x+i] + xSpacing;
		preferredWidth -= xSpacing;
		preferredHeight = c.getPreferredSize().height;
		c.setPreferredSize(new Dimension(preferredWidth, preferredHeight));

		constraint.insets = new Insets(topMargin, leftMargin, bottomMargin, rightMargin);
		lm.setConstraints(c,constraint); 
		panel.add(c);
	}

	private void updateEnabled()
	{
		communicativeAct.setEnabled(guiEnabledFlag);
		senderButton.setText((guiEnabledFlag && senderEnabledFlag) ? "Set" : "View");

		receiverListPanel.setEnabled(guiEnabledFlag);
		replyToListPanel.setEnabled(guiEnabledFlag);
		propertiesListPanel.setEnabled(guiEnabledFlag);

		replyWith.setEditable(guiEnabledFlag);
		inReplyTo.setEditable(guiEnabledFlag);
		conversationId.setEditable(guiEnabledFlag);
		replyBy.setEditable(false);
		replyBySet.setEnabled(true);
		replyBySet.setText(guiEnabledFlag ? "Set" : "View");
		encoding.setEditable(guiEnabledFlag);
		protocol.setEnabled(guiEnabledFlag);
		language.setEditable(guiEnabledFlag);
		ontology.setEditable(guiEnabledFlag);
		content.setEditable(guiEnabledFlag);

		//Envelope
		fromButton.setText(guiEnabledFlag && senderEnabledFlag ? "Set": "View");
		toPanel.setEnabled(guiEnabledFlag);
		comments.setEnabled(guiEnabledFlag);
		representation.setEnabled(guiEnabledFlag);
		payloadLength.setEnabled(guiEnabledFlag);
		payloadEncoding.setEnabled(guiEnabledFlag);
		date.setEditable(false);
		dateButton.setText(guiEnabledFlag ? "Set" : "View");

		intendedReceiverPanel.setEnabled(guiEnabledFlag);
		defaultEnvelopeButton.setVisible(guiEnabledFlag);
		//ReceivedObject
		by.setEditable(guiEnabledFlag);
		fromRec.setEditable(guiEnabledFlag);
		dateRec.setEditable(false);
		dateRecButton.setText(guiEnabledFlag ? "Set" : "View");
		id.setEditable(guiEnabledFlag);
		via.setEditable(guiEnabledFlag);
	}

	private void showEnvelope(Envelope envelope)
	{
		String param; 
		try {
			this.fromAID = envelope.getFrom();
			param = fromAID.getName();
		} catch (NullPointerException e1) {
			param = "";
			this.fromAID = new AID();
		}
		from.setText(param);

		//#DOTNET_EXCLUDE_BEGIN
		toPanel.resetContent(envelope.getAllTo());
		//#DOTNET_EXCLUDE_END
		try{
			AID fromAID = envelope.getFrom();
			param = fromAID.getName();

		}catch(NullPointerException e1){param = "";}
		from.setText(param);

		try{
			param = envelope.getComments();
		}catch(NullPointerException e1){param ="";}
		comments.setText(param);

		try{
			param = envelope.getAclRepresentation();
		}catch(NullPointerException e1){param ="";}
		representation.setText(param);

		try{
			param = envelope.getPayloadLength().toString();
		}catch(NullPointerException e1){param ="-1";}
		payloadLength.setText(param);

		try{
			param = envelope.getPayloadEncoding();
		}catch(NullPointerException e1){param ="";}
		payloadEncoding.setText(param);

		//Date
		dateDate = envelope.getDate();
		if (dateDate != null)
			date.setText(ISO8601.toString(dateDate)); 
		else
			date.setText("");

		//#DOTNET_EXCLUDE_BEGIN
		intendedReceiverPanel.resetContent(envelope.getAllIntendedReceiver());
		//#DOTNET_EXCLUDE_END

		ReceivedObject recObject = envelope.getReceived();
		try{
			param = recObject.getBy();
		}catch(NullPointerException e){
			param = "";
		}
		by.setText(param);
		try{
			param = recObject.getFrom();
		}catch(NullPointerException e){
			param = "";
		}
		fromRec.setText(param);

		try{
			dateRecDate = recObject.getDate();
			param = ISO8601.toString(dateRecDate);
		}catch(NullPointerException e){
			param = "";      
		}
		dateRec.setText(param);

		try{
			param = recObject.getId();
		}catch(NullPointerException e){
			param = "";
		}
		id.setText(param);
		try{
			param = recObject.getVia();
		}catch(NullPointerException e){
			param = "";
		}
		via.setText(param);
	}

	/////////////////////////////////////////////
	// MESSAGE GETTING and SETTING PUBLIC METHODS
	/////////////////////////////////////////////
	/**
    Displays the specified ACL message into the AclGui panel 
    @param msg The ACL message to be displayed
    @see AclGui#getMsg()
	 */
	public void setMsg(ACLMessage msg)
	{
		int    i;
		String param, lowerCase;

		int perf = msg.getPerformative(); 
		lowerCase = (ACLMessage.getPerformative(perf)).toLowerCase();

		//No control if the ACLMessage is a well-known one
		//if not present the first of the comboBox is selected
		communicativeAct.setSelectedItem(lowerCase);  

		try {
			this.SenderAID = msg.getSender();
			param = SenderAID.getName();
		} catch (NullPointerException e) {
			param = "";
			this.SenderAID = new AID();
		}

		sender.setText(param);

		//#DOTNET_EXCLUDE_BEGIN
		receiverListPanel.resetContent(msg.getAllReceiver());
		replyToListPanel.resetContent(msg.getAllReplyTo());
		//#DOTNET_EXCLUDE_END

		Enumeration e =   msg.getAllUserDefinedParameters().propertyNames();
		ArrayList list = new ArrayList();
		while(e.hasMoreElements())
			list.add(e.nextElement());
		propertiesListPanel.resetContent(list.iterator());
		propertiesListPanel.setContentProperties(msg.getAllUserDefinedParameters());

		if ((param = msg.getReplyWith()) == null) param = "";
		replyWith.setText(param);
		if ((param = msg.getInReplyTo()) == null) param = "";
		inReplyTo.setText(param);
		if ((param = msg.getConversationId()) == null) param = "";
		conversationId.setText(param);
		try {
			param=ISO8601.toString(msg.getReplyByDate());
		} catch (Exception exc) {
			param="";
		}
		replyBy.setText(param);

		if((param = msg.getProtocol()) == null)
			protocol.setSelectedItem("Null");
		else if (param.equals("") || param.equalsIgnoreCase("Null"))
			protocol.setSelectedItem("Null");
		else
		{
			lowerCase = param.toLowerCase();
			if ((i = fipaProtocolArrayList.indexOf((Object) lowerCase)) < 0)
			{
				// This is done to avoid inserting the same user-defined protocol more than once
				protocol.addItem(param);
				int cnt = protocol.getItemCount();
				protocol.setSelectedItem(param);
				int n = protocol.getSelectedIndex();
				if (n != cnt-1)
					protocol.removeItemAt(cnt-1);
			}
			else
				protocol.setSelectedIndex(i);
		}
		String lang;
		if ((lang = msg.getLanguage()) == null) lang = "";
		language.setText(lang);
		if ((param = msg.getOntology()) == null) param = "";
		ontology.setText(param);

		if ((param = msg.getContent()) == null) param = "";
		if ( (lang.equalsIgnoreCase(FIPANames.ContentLanguage.FIPA_SL0) ||
				lang.equalsIgnoreCase(FIPANames.ContentLanguage.FIPA_SL1) ||
				lang.equalsIgnoreCase(FIPANames.ContentLanguage.FIPA_SL2) ||
				lang.equalsIgnoreCase(FIPANames.ContentLanguage.FIPA_SL)) &&
				(slFormatter != null))
			// Try inserting formatted SL content.
			param = slFormatter.format(param);
		content.setText(param);

		if((param = msg.getEncoding())== null) param = "";
		encoding.setText(param);

		//Envelope
		Envelope envelope = msg.getEnvelope();

		if(envelope != null)
			showEnvelope(envelope);


	}


	/**
    Get the ACL message currently displayed by the AclGui panel 
    @return The ACL message currently displayed by the AclGui panel as an ACLMessage object
    @see AclGui#setMsg(ACLMessage msg)
	 */
	public ACLMessage getMsg()
	{
		String param;
		param = (String) communicativeAct.getSelectedItem();
		int perf = ACLMessage.getInteger(param);
		ACLMessage msg = new ACLMessage(perf);

		if(newAIDSender != null)
			SenderAID = newAIDSender;

		/*if ( ((param = sender.getText()).trim()).length() > 0 )
      SenderAID.setName(param);*/
		// check if SenderAID has a guid. SenderAID is surely not null here
		if (SenderAID.getName().length() > 0)
			msg.setSender(SenderAID);

		Enumeration rec_Enum = receiverListPanel.getContent();
		while(rec_Enum.hasMoreElements())
			msg.addReceiver((AID)rec_Enum.nextElement());

		Enumeration replyTo_Enum = replyToListPanel.getContent();
		while(replyTo_Enum.hasMoreElements())
			msg.addReplyTo((AID)replyTo_Enum.nextElement());

		Properties user_Prop = propertiesListPanel.getContentProperties();
		Enumeration keys = user_Prop.propertyNames();
		while(keys.hasMoreElements())
		{
			String k = (String)keys.nextElement();
			msg.addUserDefinedParameter(k,user_Prop.getProperty(k));
		}

		param = replyWith.getText().trim();
		if (param.length() > 0)
			msg.setReplyWith(param);

		param = inReplyTo.getText().trim(); 
		if (param.length() > 0)
			msg.setInReplyTo(param);

		param = conversationId.getText().trim();  
		if (param.length() > 0)
			msg.setConversationId(param);

		param = replyBy.getText().trim(); 
		try {
			msg.setReplyByDate(ISO8601.toDate(param));
		} catch (Exception e) {}

		if (!(param = (String) protocol.getSelectedItem()).equals("Null"))
			msg.setProtocol(param);

		param = language.getText().trim();
		if (param.length()>0)
			msg.setLanguage(param);

		param = ontology.getText().trim();  
		if (param.length()>0)
			msg.setOntology(param);

		param = content.getText().trim();   
		if (param.length()>0)
			msg.setContent(param);

		param = (encoding.getText()).trim();
		if(param.length() > 0)
			msg.setEncoding(param);

		Envelope env = new Envelope();  

		Enumeration to_Enum = toPanel.getContent();
		while(to_Enum.hasMoreElements())
			env.addTo((AID)to_Enum.nextElement());

		if(newAIDFrom!= null)
			fromAID = newAIDFrom;
		if (fromAID.getName().length() > 0)
			env.setFrom(fromAID);

		param = comments.getText().trim();
		if(param.length()>0)
			env.setComments(param);

		param = representation.getText().trim();
		if(param.length()>0)
			env.setAclRepresentation(param);

		try {
			param = payloadLength.getText().trim();
			env.setPayloadLength(new Long(param));
		} catch (Exception e) { 
			//System.err.println("Incorrect int format. payloadLength must be an integer. Automatic reset to -1.");
			//env.setPayloadLength(new Long(-1));
			//payloadLength.setText("-1");
		}

		param = payloadEncoding.getText().trim();
		if(param.length()>0)
			env.setPayloadEncoding(param);

		//setDate require a Date not a String
		if (dateDate != null) 
			env.setDate(dateDate);

		Enumeration int_Enum = intendedReceiverPanel.getContent();
		while(int_Enum.hasMoreElements())
			env.addIntendedReceiver((AID)int_Enum.nextElement());


		param = language.getText().trim();
		if (param.length()>0)
			msg.setLanguage(param);


		/* ReceivedObject recObject = new ReceivedObject();
    boolean filled = false;
    param = by.getText().trim();

    if(param.length()>0)
    {
      filled = true;
      recObject.setBy(param);
    }
    param = fromRec.getText().trim();
    if(param.length()>0)
    {
      filled = true;
      recObject.setFrom(param);
    }

    if (dateRecDate != null)  
    {
      filled = true;
      recObject.setDate(dateRecDate);
    }

    param = id.getText().trim();
    if(param.length()>0)
    {
      filled = true;
      recObject.setId(param);
    }

    param = via.getText().trim();
    if(param.length()>0)
    {
      filled = true;
      recObject.setVia(param);
    }

    if(filled)
      env.setReceived(recObject);
		 */
		msg.setEnvelope(env);
		return msg;
	}


	/////////////////////////
	// UTILITY PUBLIC METHODS
	/////////////////////////
	/** 
    Enables/disables the editability of all the controls in an AclGui panel (default is enabled)
    @param enabledFlag If true enables editability 
    @see AclGui#setSenderEnabled(boolean enabledFlag)
	 */
	public void setEnabled(boolean enabledFlag)
	{
		guiEnabledFlag = enabledFlag;
		updateEnabled();
	}

	/** 
    Enables/disables the editability of the sender field of an AclGui panel (default is enabled)
    @param enabledFlag If true enables editability 
    @see AclGui#setEnabled(boolean enabledFlag)
	 */
	public void setSenderEnabled(boolean enabledFlag)
	{
		senderEnabledFlag = enabledFlag;
		updateEnabled();
	}

	/** 
    Set the specified border to the AclGui panel
    @param b Specifies the type of border
	 */
	/*public void setBorder(Border b)
  {
    if (aclPanel != null)
      aclPanel.setBorder(b);
  }*/

	/** 
    Paint the AclGui panel
	 */
	public void paint(Graphics g)
	{
		if (firstPaintFlag)
		{
			firstPaintFlag = false;
			minDim = aclPanel.getSize();
		}
		else
			aclPanel.setMinimumSize(minDim);

		super.paint(g);
	}


	//////////////////
	// STATIC METHODS
	//////////////////
	/**
    Pops up a dialog window including an editing-disabled AclGui panel and displays the specified 
    ACL message in it. 
    @param m The ACL message to be displayed
    @param parent The parent window of the dialog window
    @see AclGui#editMsgInDialog(ACLMessage msg, Frame parent)
	 */
	public static void showMsgInDialog(ACLMessage msg, Frame parent)
	{
		final JDialog tempAclDlg = new JDialog(parent, "ACL Message", true);

		AclGui aclPanel = new AclGui(parent);
		//aclPanel.setBorder(new BevelBorder(BevelBorder.RAISED));
		aclPanel.setEnabled(false);
		aclPanel.setMsg(msg);

		JButton okButton = new JButton("OK");
		JPanel buttonPanel = new JPanel();
		// Use default (FlowLayout) layout manager to dispose the OK button
		buttonPanel.add(okButton);

		tempAclDlg.getContentPane().setLayout(new BorderLayout());
		tempAclDlg.getContentPane().add("Center", aclPanel);
		tempAclDlg.getContentPane().add("South", buttonPanel);

		okButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				tempAclDlg.dispose();
			}
		} );

		tempAclDlg.pack();
		tempAclDlg.setResizable(false);
		if (parent != null) {
			int locx = parent.getX() + (parent.getWidth() - tempAclDlg.getWidth()) / 2;
			if (locx < 0)
				locx = 0;
			int locy = parent.getY() + (parent.getHeight() - tempAclDlg.getHeight()) / 2;
			if (locy < 0)
				locy = 0;
			tempAclDlg.setLocation(locx,locy);
		}
		tempAclDlg.setVisible(true);
	}

	/**
    Pops up a dialog window including an editing-enabled AclGui panel and displays the specified 
    ACL message in it. The dialog window also includes an OK and a Cancel button to accept or 
    discard the performed editing. 
    @param m The ACL message to be initially displayed
    @param parent The parent window of the dialog window
    @return The ACL message displayed in the dialog window or null depending on whether the user close the window
    by clicking the OK or Cancel button 
    @see AclGui#showMsgInDialog(ACLMessage msg, Frame parent)
	 */
	public static ACLMessage editMsgInDialog(ACLMessage msg, Frame parent)
	{
		final JDialog tempAclDlg = new JDialog(parent, "ACL Message", true);
		final AclGui  aclPanel = new AclGui(parent);
		aclPanel.setBorder(new BevelBorder(BevelBorder.RAISED));
		aclPanel.setSenderEnabled(true);
		aclPanel.setMsg(msg);

		JButton okButton = new JButton("OK");
		JButton cancelButton = new JButton("Cancel");
		okButton.setPreferredSize(cancelButton.getPreferredSize());
		JPanel buttonPanel = new JPanel();
		// Use default (FlowLayout) layout manager to dispose the OK and Cancel buttons
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);  

		tempAclDlg.getContentPane().setLayout(new BorderLayout());
		tempAclDlg.getContentPane().add("Center", aclPanel);
		tempAclDlg.getContentPane().add("South", buttonPanel);

		okButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				editedMsg = aclPanel.getMsg();
				tempAclDlg.dispose();
			}
		} );
		cancelButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				editedMsg = null;
				tempAclDlg.dispose();
			}
		} );

		tempAclDlg.pack();
		tempAclDlg.setResizable(false);

		if (parent != null)
		{
			int x = parent.getX() + (parent.getWidth() - tempAclDlg.getWidth()) / 2;
			int y = parent.getY() + (parent.getHeight() - tempAclDlg.getHeight()) / 2;
			tempAclDlg.setLocation(x > 0 ? x :0, y>0 ? y :0);
		}

		tempAclDlg.setVisible(true);

		ACLMessage m = null;
		if (editedMsg != null)
			m = (ACLMessage) editedMsg.clone();

		return m;
	}

	/*public static void main(String[] args)
  {
    JFrame f = new JFrame();

    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
    msg.setLanguage("language");
    msg.setOntology("onto");
    Envelope env = new Envelope();
    env.setComments("Commento");
    env.setAclRepresentation("ACLRepresentation");
    msg.setEnvelope(env);
    AclGui.showMsgInDialog(msg,f);
    f.pack();
    f.show();
  }*/
}
