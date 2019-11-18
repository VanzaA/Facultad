/******************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2002 TILAB S.p.A.
 *
 * This file is donated by Acklin B.V. to the JADE project.
 *
 *
 * GNU Lesser General Public License
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * ***************************************************************/
package jade.tools.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.*;
import java.util.Comparator;
import java.util.Date;
import java.util.Date;
import java.util.StringTokenizer;
import javax.swing.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.text.*;
import javax.swing.tree.*;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.StringACLCodec;
import jade.tools.sl.SLFormatter;
import jade.util.leap.*;
import jade.util.Logger;

/**
 *  This class show a List of incoming and outgoing ACLmessages. When clicking
 *  on the right button a pop menu wil be shown, containing operations on the
 *  selected ACLMessage
 *
 * @author     Chris van Aart - Acklin B.V., the Netherlands
 * @created    April 26, 2002
 */

public class ACLTracePanel extends JPanel {

  //logging
  
  private Logger logger = Logger.getMyLogger(this.getClass().getName());
  /**
   *  Constructor for the ACLTreePanel object
   *
   * @param  agent  link to the agent
   */
  public ACLTracePanel(Agent agent) {
    this.agent = agent;
    inMsgCount = 0;
    outMsgCount = 0;
    aclIndex = 0;
    try {
      jbInit();
      aclTree.setCellRenderer(aclTreeRenderer);
      // since JDK1.3 aclTree.setToggleClickCount(3);

      this.fillSortComboBoxModel();
      this.sortComboBox.setModel(this.sortComboBoxModel);
      this.sortComboBox.setRenderer(new SortRenderer());
      this.sortComboBox.setSelectedIndex(0);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }


  public ACLMessage getCurrentACL() {
    return currentACL;
  }


  public void doSystemOut() {
    if (currentACL == null) {
      return;
    }
    if(logger.isLoggable(Logger.CONFIG))
    	logger.log(Logger.CONFIG,"\n" + currentACL.toString() + "\n");
  }


  /**
   *  Adds a ACLMessageNode to the List
   *
   * @param  theNode  the ACLMessageNode
   */
  public void addMessageNode(ACLMessageNode theNode) {
    addMessageNode(theNode.getDirection(), theNode.getTime(), theNode.getMessage());
  }


  /**
   *  Adds a ACLMessage to the List
   *
   * @param  direction  the direction of the ACLMessage
   * @param  theACL     the ACLMessage to be added
   */
  public void addMessageNode(String direction, ACLMessage theACL) {
    String theTime = getTimeStamp();
    addMessageNode(direction, theTime, theACL);
  }


  /**
   *  Adds a ACLMessage to the List
   *
   * @param  direction  the direction of the ACLMessage
   * @param  theTime    timeStamp
   * @param  theACL     the ACLMessage to be added
   */
  public void addMessageNode(String direction, String theTime, ACLMessage theACL) {

    String aclString = theACL.toString();
    aclString = aclString.replace('\n', ' ');

    String contentString = (theACL.getContent() != null ? theACL.getContent().replace('\n', ' ') : "");

    String theSender = theACL.getSender().getLocalName();
    String theReceiver = "<none>";
    if (theACL.getAllReceiver().hasNext()) {
      theReceiver = ((AID)theACL.getAllReceiver().next()).getLocalName();
    }

    if (direction.startsWith("i")) {
      this.inMsgCount++;
      aclString = theACL.getPerformative(theACL.getPerformative()) +
        ": from  " + theSender + " - " + contentString + " ";
    }
    else {
      this.outMsgCount++;
      aclString = theACL.getPerformative(theACL.getPerformative()) +
        ": to  " + theReceiver + " - " + contentString + " ";

    }

    DefaultMutableTreeNode contentNode = new DefaultMutableTreeNode(":content");
    ACLMessageNode messageNode = new ACLMessageNode(theTime + " " + direction + ": " + aclString);
    messageNode.setMessage(theACL);
    messageNode.setDirection(direction);
    messageNode.setTime(theTime);
    aclModel.insertNodeInto(messageNode, aclRoot, aclIndex++);

    String contentLanguage = (theACL.getLanguage() != null ? theACL.getLanguage() : "<unknown>");
    String plainContent = (theACL.getContent() != null ? theACL.getContent() : "");

    if (contentLanguage.indexOf("SL") >= 0) {
      //Only format when SL
      try {
        plainContent = (String)new SLFormatter().format(plainContent);
      }
      catch (Exception ex) {
        //too bad!
      }
    }
    while ((plainContent.indexOf('\n')) == 0) {
      plainContent = plainContent.substring(1);
    }

    StringTokenizer stok = new StringTokenizer(plainContent, "\n");
    while (stok.hasMoreTokens()) {
      contentNode.add(new DefaultMutableTreeNode(stok.nextToken()));
    }

    int i = 0;
    aclModel.insertNodeInto(new DefaultMutableTreeNode(":act " + theACL.getPerformative(theACL.getPerformative())), messageNode, i++);

    AID sender = theACL.getSender();
    if (sender != null) {
      aclModel.insertNodeInto(new DefaultMutableTreeNode(":sender " + sender.toString()), messageNode, i++);
    }

    AID aid = new AID();
    Iterator it = theACL.getAllReceiver();
    while (it.hasNext()) {
      aid = (AID)it.next();
      aclModel.insertNodeInto(new DefaultMutableTreeNode(":receiver " + aid.toString()), messageNode, i++);
    }

    //reply-to
    Iterator replyItor = theACL.getAllReplyTo();
    while (replyItor.hasNext()) {
      aid = (AID)replyItor.next();
      aclModel.insertNodeInto(new DefaultMutableTreeNode(":reply-to " + aid.toString()), messageNode, i++);
    }

    aclModel.insertNodeInto(new DefaultMutableTreeNode("--- Description of Content ---"), messageNode, i++);
    aclModel.insertNodeInto(contentNode, messageNode, i++);

    String language = theACL.getLanguage();
    if (language != null) {
      aclModel.insertNodeInto(new DefaultMutableTreeNode(":language " + language), messageNode, i++);
    }

    String encoding = theACL.getEncoding();
    if (encoding != null) {
      aclModel.insertNodeInto(new DefaultMutableTreeNode(":encoding " + encoding), messageNode, i++);
    }

    String ontology = theACL.getOntology();
    if (ontology != null) {
      aclModel.insertNodeInto(new DefaultMutableTreeNode(":ontology " + ontology), messageNode, i++);
    }

    aclModel.insertNodeInto(new DefaultMutableTreeNode("--- Message control ---"), messageNode, i++);

    String protocol = theACL.getProtocol();
    if (protocol != null) {
      aclModel.insertNodeInto(new DefaultMutableTreeNode(":protocol " + protocol), messageNode, i++);
    }

    String convId = theACL.getConversationId();
    if (convId != null) {
      aclModel.insertNodeInto(new DefaultMutableTreeNode(":conversation-id " + convId), messageNode, i++);
    }

    String inreplyto = theACL.getInReplyTo();
    if (inreplyto != null) {
      aclModel.insertNodeInto(new DefaultMutableTreeNode(":in-reply-to " + inreplyto), messageNode, i++);
    }

    String replywith = theACL.getReplyWith();
    if (replywith != null) {
      aclModel.insertNodeInto(new DefaultMutableTreeNode(":reply-with " + replywith), messageNode, i++);
    }

    Date replyBy = theACL.getReplyByDate();
    if (replyBy != null) {
      aclModel.insertNodeInto(new DefaultMutableTreeNode(":reply-by " + replyBy.toString()), messageNode, i++);
    }

    jade.util.leap.Properties prop = theACL.getAllUserDefinedParameters();
    if (prop.size() > 0) {
      aclModel.insertNodeInto(new DefaultMutableTreeNode("--- User Defined Parameters ---"), messageNode, i++);
      java.util.Enumeration enumeration = prop.elements();
      while (enumeration.hasMoreElements()) {
        String key = (String)enumeration.nextElement();
        String value = (String)prop.getProperty(key);
        aclModel.insertNodeInto(new DefaultMutableTreeNode(":X-" + key + " " + value), messageNode, i++);
      }
    }
    if (!sorting) {
      doSort();
    }
    aclTree.expandPath(new TreePath(aclRoot.getPath()));
  }


  /**
   *  delete the current selected ACLMessage from the list
   */
  public void deleteCurrent() {
      //since JDK1.3 TreePath tp = aclTree.getAnchorSelectionPath();
    TreePath tp = aclTree.getSelectionPath();
    if (tp == null) {
      currentACL = null;
    }

    else {
      currentACL = ((ACLMessageNode)tp.getPathComponent(1)).getMessage();
      this.aclModel.removeNodeFromParent((ACLMessageNode)tp.getPathComponent(1));
      this.refresh();
      aclIndex--;
    }
  }


  /**
   *  show about screen
   */
  public void doShowAbout() {
    new AboutFrame().setVisible(true);
  }


  /**
   *  save the current selected ACLMessage to a textfile
   */
  public void saveACL() {

    UIManager.put("FileChooser.saveButtonToolTipText", "Save ACLMessage");

    JFileChooser chooser = new JFileChooser();
    chooser.setFileFilter(new ACLFileFilter());
    //since JDK1.3 chooser.setAcceptAllFileFilterUsed(false);
    chooser.setSelectedFile(new File("itsmessage.acl"));
    chooser.setDialogTitle("Save ACLMessage");
    if (currentDir != null) {
      chooser.setCurrentDirectory(currentDir);
    }

    int returnVal = chooser.showSaveDialog(null);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      currentDir = chooser.getCurrentDirectory();
      String fileName = chooser.getSelectedFile().getAbsolutePath();

      try {
        FileWriter f = new FileWriter(fileName);
        StringACLCodec codec = new StringACLCodec(null, f);
        // ACLMessage ACLmsg = currentMsgGui.getMsg();
        codec.write(currentACL);
        f.close();

      }
      catch (FileNotFoundException e3) {
       if(logger.isLoggable(Logger.WARNING))
       	logger.log(Logger.WARNING,"Can't open file: " + fileName);
      }
      catch (IOException e4) {
        if(logger.isLoggable(Logger.WARNING))
        	logger.log(Logger.WARNING,"IO Exception");
      }
    }

  }


  public void doShowCurrentACL() {
    showCurrentACL(x, y);
  }


  public void showStastistics() {
    ACLStatisticsFrame.show(this.aclModel);
  }


  public void saveQueue() {

    UIManager.put("FileChooser.saveButtonToolTipText", "Save ACLMessage Trace");

    JFileChooser chooser = new JFileChooser();
    chooser.setFileFilter(new TraceFileFilter());
    // since JDK1.3 chooser.setAcceptAllFileFilterUsed(false);
    chooser.setSelectedFile(new File("itsTrace.trc"));
    chooser.setDialogTitle("Save ACLMessage Trace");
    if (currentDir != null) {
      chooser.setCurrentDirectory(currentDir);
    }

    int returnVal = chooser.showSaveDialog(null);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      currentDir = chooser.getCurrentDirectory();
      String fileName = chooser.getSelectedFile().getAbsolutePath();

      if (!fileName.endsWith(".trc")) {
        fileName = fileName + ".trc";
      }

      try {
        FileWriter f = new FileWriter(fileName);
        BufferedWriter bw = new BufferedWriter(f);
        int size = aclModel.getChildCount(this.aclRoot);
        for (int i = 0; i < size; i++) {
          System.out.println("aclModel: " + aclModel.getChild(this.aclRoot, i).getClass());
          ACLMessageNode theNode = (ACLMessageNode)aclModel.getChild(this.aclRoot, i);
          String direction = theNode.getDirection();
          String theTime = theNode.getTime();
          ACLMessage theMsg = theNode.getMessage();
          bw.newLine();
          bw.write(direction);
          bw.newLine();
          bw.write(theTime);
          bw.newLine();
          bw.write(theMsg.toString());
          bw.newLine();
        }
        bw.flush();
        bw.close();
        f.close();

      }
      catch (FileNotFoundException e3) {
        if(logger.isLoggable(Logger.WARNING))
        	logger.log(Logger.WARNING,"Can't open file: " + fileName);
      }
      catch (IOException e4) {
        if(logger.isLoggable(Logger.WARNING))
	        logger.log(Logger.WARNING,"IO Exception");
      }
    }
  }


  public void loadQueue() {

    UIManager.put("FileChooser.openButtonToolTipText", "Open ACLMessage Trace");

    JFileChooser chooser = new JFileChooser();
    chooser.setFileFilter(new TraceFileFilter());
    // since JDK1.3 chooser.setAcceptAllFileFilterUsed(false);
    chooser.setDialogTitle("Open ACLMessage Trace From File");
    if (currentDir != null) {
      chooser.setCurrentDirectory(currentDir);
    }

    int returnVal = chooser.showOpenDialog(null);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      currentDir = chooser.getCurrentDirectory();
      String fileName = chooser.getSelectedFile().getAbsolutePath();

      try {
        clearACLModel();
        FileReader f = new FileReader(fileName);
        BufferedReader br = new BufferedReader(f);
        String line = br.readLine();
        String direction;
        String theTime;
        while (line != null) {
          direction = br.readLine();
          theTime = br.readLine();

          String theMessageLine = "";
          line = br.readLine();
          while ((line != null) && (!line.equals(""))) {
            theMessageLine = theMessageLine + "\n" + line;
            line = br.readLine();
          }
          char[] chararray = theMessageLine.toCharArray();

          StringACLCodec codec = new StringACLCodec(new StringReader(new String(chararray)), null);
          ACLMessage theMsg = codec.decode();

          this.addMessageNode(direction, theTime, theMsg);

        }
        br.close();
        f.close();

      }
      catch (FileNotFoundException e3) {
        if(logger.isLoggable(Logger.WARNING))
        	logger.log(Logger.WARNING,"Can't open file: " + fileName);
      }
      catch (IOException e4) {
        if(logger.isLoggable(Logger.WARNING))
        	logger.log(Logger.WARNING,"IO Exception");
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }


  public void clearACLModel() {

    int size = aclModel.getChildCount(this.aclRoot);
    for (int i = size - 1; i >= 0; i--) {
      ACLMessageNode theNode = (ACLMessageNode)aclModel.getChild(this.aclRoot, i);
      this.aclRoot.remove(theNode);
    }
    this.aclIndex = 0;
    this.aclModel.reload();
    aclTree.validate();
    aclTree.repaint();
  }


  public void doSort() {

    int direction = this.sortComboBox.getSelectedIndex();
    int ascending = (this.ascRadioButton.isSelected() ? 1 : -1);
    ACLMessageNodeComparator anc = new ACLMessageNodeComparator(direction, ascending);
    int size = aclModel.getChildCount(this.aclRoot);
    if (size < 2) {
      return;
    }
    Object[] theList = new Object[size];

    for (int i = 0; i < size; i++) {
      ACLMessageNode theNode = (ACLMessageNode)aclModel.getChild(this.aclRoot, i);
      theList[i] = theNode;
    }

    java.util.Arrays.sort(theList, anc);
    this.clearACLModel();
    sorting = true;
    for (int i = 0; i < size; i++) {
      ACLMessageNode theNode = (ACLMessageNode)theList[i];
      this.addMessageNode(theNode);
    }
    sorting = false;
    this.aclModel.reload();
    aclTree.validate();
    aclTree.repaint();

  }



  /**
   *  Gets the TimeStamp attribute of the ACLTreePanel object
   *
   * @return    The TimeStamp value
   */
  String getTimeStamp() {

    return dateFormat.format(new Date());
  }


  /**
   *  refresh the List
   */
  void refresh() {
    aclTree.validate();
    aclTree.updateUI();
  }


  /**
   *  triggered when mousePressed
   *
   * @param  e  the MouseEvent
   */
  void aclTree_mouseClicked(MouseEvent e) {
    try {
	//since JDK1.3 TreePath tp = aclTree.getAnchorSelectionPath();
      TreePath tp = aclTree.getSelectionPath();
      if (tp == null) {
        currentACL = null;
      }

      else {
        currentACL = ((ACLMessageNode)tp.getPathComponent(1)).getMessage();
      }

      if (e.getModifiers() == 4) {
        if (currentACL == null) {
          JOptionPane.showMessageDialog(null, "No ACL", "Select a Message", JOptionPane.ERROR_MESSAGE);
          return;
        }
        x = e.getX();
        y = e.getY();
        this.thePopupMenu.show(this, e.getX(), e.getY());
      }
      if ((e.getModifiers() == 16) && (e.getClickCount() == 2)) {
        if (currentACL == null) {
          JOptionPane.showMessageDialog(null, "No ACL", "Select a Message", JOptionPane.ERROR_MESSAGE);
          return;
        }
        showCurrentACL(e.getX(), e.getY());
      }
    }
    catch (Exception ex) {
      //index out of range
    }
  }


  /**
   *  triggered when systemMenuItem
   *
   * @param  e  ActionEvent
   */
  void systemMenuItem_actionPerformed(ActionEvent e) {
    if (currentACL == null) {
      return;
    }
  }


  /**
   *  showCurrentACL
   *
   * @param  e  ActionEvent
   */
  void zoomMenuItem_actionPerformed(ActionEvent e) {
    doShowCurrentACL();
  }


  /**
   *  saveACL
   *
   * @param  e  ActionEvent
   */
  void saveMenuItem_actionPerformed(ActionEvent e) {
    saveACL();
  }


  /**
   *  show ACLStatisticsFrame
   *
   * @param  e  ActionEvent
   */
  void stasticsMenuItem_actionPerformed(ActionEvent e) {
    showStastistics();
  }



  /**
   *  listener for keyTyped
   *
   * @param  e  KeyEvent
   */
  void aclTree_keyTyped(KeyEvent e) {
    if (e.getKeyCode() == e.VK_DELETE) {
      deleteCurrent();
    }

  }


  /**
   *  deleteMenuItem
   *
   * @param  e  ActionEvent
   */
  void deleteMenuItem_actionPerformed(ActionEvent e) {
    this.deleteCurrent();
  }


  void sortButton_actionPerformed(ActionEvent e) {
    this.doSort();
  }


  void sortComboBox_itemStateChanged(ItemEvent e) {
    doSort();
  }


  void ascRadioButton_itemStateChanged(ItemEvent e) {
    doSort();
  }


  void descRadioButton_itemStateChanged(ItemEvent e) {
    doSort();
  }


  void aboutMenuItem_mouseClicked(MouseEvent e) {
    this.doShowAbout();
  }


  void saveQMenuItem_actionPerformed(ActionEvent e) {
    this.saveQueue();
  }


  void clearQMenuItem_actionPerformed(ActionEvent e) {
    this.clearACLModel();
  }


  void openQMenuItem_actionPerformed(ActionEvent e) {
    this.loadQueue();
  }


  void aboutMenuItem_actionPerformed(ActionEvent e) {
    this.doShowAbout();
  }



  private void fillSortComboBoxModel() {
    sortComboBoxModel.addElement("date");
    sortComboBoxModel.addElement("direction");
    sortComboBoxModel.addElement("sender");
    sortComboBoxModel.addElement("receiver");
    sortComboBoxModel.addElement("performative");
    sortComboBoxModel.addElement("ontology");
  }



  /**
   *  show the Current ACLMessage
   *
   * @param  x  x location
   * @param  y  y location
   */
  private void showCurrentACL(int x, int y) {
    if (currentACL == null) {
      return;
    }
    ACLFrame.show(currentACL, agent);
  }


  /**
   *  build up TreePanel
   *
   * @exception  Exception  Description of Exception
   */
  private void jbInit() throws Exception {
    border1 = BorderFactory.createLineBorder(Color.black, 2);
    titledBorder1 = new TitledBorder(border1, "actions");
    border2 = BorderFactory.createLineBorder(Color.black, 0);
    border3 = BorderFactory.createLineBorder(Color.darkGray, 1);
    this.setLayout(gridBagLayout2);
    aclTree.setModel(aclModel);
    aclTree.addKeyListener(new ACLTreePanel_aclTree_keyAdapter(this));
    aclTree.addMouseListener(new ACLTreePanel_aclTree_mouseAdapter(this));
    this.addMouseListener(new ACLTreePanel_this_mouseAdapter(this));
    systemMenuItem.setBackground(Color.white);
    systemMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
    systemMenuItem.setMnemonic('O');
    systemMenuItem.setText("System.out");
    systemMenuItem.addActionListener(new ACLTreePanel_systemMenuItem_actionAdapter(this));
    zoomMenuItem.setBackground(Color.white);
    zoomMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
    zoomMenuItem.setMnemonic('V');
    zoomMenuItem.setText("View ACLMessage");
    zoomMenuItem.addActionListener(new ACLTreePanel_zoomMenuItem_actionAdapter(this));
    thePopupMenu.setBackground(Color.white);
    thePopupMenu.setBorder(BorderFactory.createLineBorder(Color.black));
    thePopupMenu.setOpaque(false);
    saveMenuItem.setBackground(Color.white);
    saveMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
    saveMenuItem.setMnemonic('S');
    saveMenuItem.setText("Save ACLMessage");
    saveMenuItem.addActionListener(new ACLTreePanel_saveMenuItem_actionAdapter(this));
    stasticsMenuItem.setBackground(Color.white);
    stasticsMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
    stasticsMenuItem.setMnemonic('T');
    stasticsMenuItem.setText("Show Statistics");
    stasticsMenuItem.addActionListener(new ACLTreePanel_stasticsMenuItem_actionAdapter(this));
    aboutMenuItem.setBackground(Color.white);
    aboutMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
    aboutMenuItem.setForeground(new Color(0, 0, 140));
    aboutMenuItem.setMnemonic('A');
    aboutMenuItem.setText("About");
    aboutMenuItem.addActionListener(new ACLTracePanel_aboutMenuItem_actionAdapter(this));
    deleteMenuItem.setBackground(Color.white);
    deleteMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
    deleteMenuItem.setMnemonic('D');
    deleteMenuItem.setText("Delete ACLMessage");
    deleteMenuItem.addActionListener(new ACLTreePanel_deleteMenuItem_actionAdapter(this));
    jLabel1.setBackground(Color.white);
    jLabel1.setFont(new java.awt.Font("Dialog", 0, 12));
    jLabel1.setOpaque(true);
    jLabel1.setText("sort by:");
    ascRadioButton.setSelected(true);
    ascRadioButton.setText("Ascending");
    ascRadioButton.setToolTipText("Sort ACLMessage Trace Ascending");
    ascRadioButton.setBackground(Color.white);
    ascRadioButton.setFont(new java.awt.Font("Dialog", 0, 12));
    ascRadioButton.addItemListener(new ACLTracePanel_ascRadioButton_itemAdapter(this));
    descRadioButton.setText("Descending");
    descRadioButton.setToolTipText("Sort ACLMessage Trace Descending");
    descRadioButton.setBackground(Color.white);
    descRadioButton.setFont(new java.awt.Font("Dialog", 0, 12));
    descRadioButton.addItemListener(new ACLTracePanel_descRadioButton_itemAdapter(this));
    sortButton.setForeground(Color.white);
    sortButton.setBorder(border2);
    sortButton.setMaximumSize(new Dimension(23, 20));
    sortButton.setPreferredSize(new Dimension(23, 20));
    sortButton.setToolTipText("Sort ACLMessage Trace");
    sortButton.setIcon(sortIcon);
    sortButton.addActionListener(new ACLTracePanel_sortButton_actionAdapter(this));
    sortButton.addActionListener(new ACLTracePanel_sortButton_actionAdapter(this));

    this.setBackground(Color.white);
    sortComboBox.setBackground(Color.white);
    sortComboBox.setFont(new java.awt.Font("Dialog", 0, 12));
    sortComboBox.setForeground(Color.blue);
    sortComboBox.setToolTipText("Select on which field to sort the ACLMessage Trace");
    sortComboBox.addItemListener(new ACLTracePanel_sortComboBox_itemAdapter(this));
    aclTreeScrollPane.setBorder(border3);
    saveQMenuItem.setBackground(Color.white);
    saveQMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
    saveQMenuItem.setMnemonic('A');
    saveQMenuItem.setText("Save ACLMessage Trace");
    saveQMenuItem.addActionListener(
      new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          saveQMenuItem_actionPerformed(e);
        }
      });
    openQMenuItem.setBackground(Color.white);
    openQMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
    openQMenuItem.setMnemonic('P');
    openQMenuItem.setText("Open ACLMessage Trace");
    openQMenuItem.addActionListener(new ACLTracePanel_openQMenuItem_actionAdapter(this));
    clearQMenuItem.setBackground(Color.white);
    clearQMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
    clearQMenuItem.setMnemonic('C');
    clearQMenuItem.setText("Clear ACLMessage Trace");
    clearQMenuItem.addActionListener(new ACLTracePanel_clearQMenuItem_actionAdapter(this));
    this.add(aclTreeScrollPane, new GridBagConstraints(0, 1, 5, 1, 1.0, 1.0
      , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jLabel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
      , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(sortComboBox, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
      , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    this.add(ascRadioButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
      , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(descRadioButton, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0
      , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(sortButton, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0
      , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    aclTreeScrollPane.getViewport().add(aclTree, null);
    thePopupMenu.add(systemMenuItem);
    thePopupMenu.add(zoomMenuItem);
    thePopupMenu.addSeparator();
    thePopupMenu.add(saveMenuItem);
    thePopupMenu.add(deleteMenuItem);
    thePopupMenu.addSeparator();
    thePopupMenu.add(saveQMenuItem);
    thePopupMenu.add(openQMenuItem);
    thePopupMenu.add(clearQMenuItem);
    thePopupMenu.addSeparator();
    thePopupMenu.add(stasticsMenuItem);
    thePopupMenu.add(aboutMenuItem);
    sortingButtonGroup.add(ascRadioButton);
    sortingButtonGroup.add(descRadioButton);
  }


  private class ACLMessageNodeComparator implements Comparator {

    public ACLMessageNodeComparator(int mode, int sorting) {
      this.mode = mode;
      this.sorting = sorting;
    }


    public int compare(Object o1, Object o2) {
      ACLMessageNode node1 = (ACLMessageNode)o1;
      ACLMessageNode node2 = (ACLMessageNode)o2;

      String comp1 = "";
      String comp2 = "";

      switch (mode) {
        case DIRECTION:
          comp1 = (node1.getDirection() != null ? node1.getDirection() : "");
          comp2 = (node2.getDirection() != null ? node2.getDirection() : "");
          break;
        case DATE:
          Date date1 = node1.getTheDate();
          Date date2 = node2.getTheDate();
          return sorting * date1.compareTo(date2);
        case SENDER:
          comp1 = (node1.getMessage().getSender() != null ? node1.getMessage().getSender().getName() : "");
          comp2 = (node2.getMessage().getSender() != null ? node2.getMessage().getSender().getName() : "");
          break;
        case RECEIVER:
          comp1 = (node1.getMessage().getAllReceiver().hasNext() ? ((AID)node1.getMessage().getAllReceiver().next()).getName() : "");
          comp2 = (node2.getMessage().getAllReceiver().hasNext() ? ((AID)node2.getMessage().getAllReceiver().next()).getName() : "");
          break;
        case PERFORMATIVE:
          comp1 = ACLMessage.getPerformative(node1.getMessage().getPerformative());
          comp2 = ACLMessage.getPerformative(node2.getMessage().getPerformative());
          break;
        case ONTOLOGY:
          comp1 = (node1.getMessage().getOntology() != null ? node1.getMessage().getOntology() : "");
          comp2 = (node2.getMessage().getOntology() != null ? node2.getMessage().getOntology() : "");
          break;
      }

      return sorting * comp1.compareTo(comp2);
    }


    public boolean equals(Object obj) {

      throw new java.lang.UnsupportedOperationException("Method equals() not yet implemented.");
    }


    final static int DATE = 0;
    final static int DIRECTION = 1;
    final static int SENDER = 2;
    final static int RECEIVER = 3;
    final static int PERFORMATIVE = 4;
    final static int ONTOLOGY = 5;

    final static int SORT_ASCENDING = 1;
    final static int SORT_DESCENDING = -1;

    private int sorting;
    private int mode;

  }


//  ***EOF***


  private class ACLTracePanel_sortComboBox_itemAdapter implements java.awt.event.ItemListener {

    ACLTracePanel_sortComboBox_itemAdapter(ACLTracePanel adaptee) {
      this.adaptee = adaptee;
    }


    public void itemStateChanged(ItemEvent e) {
      adaptee.sortComboBox_itemStateChanged(e);
    }


    ACLTracePanel adaptee;
  }


  private class ACLTracePanel_ascRadioButton_itemAdapter implements java.awt.event.ItemListener {

    ACLTracePanel_ascRadioButton_itemAdapter(ACLTracePanel adaptee) {
      this.adaptee = adaptee;
    }


    public void itemStateChanged(ItemEvent e) {
      adaptee.ascRadioButton_itemStateChanged(e);
    }


    ACLTracePanel adaptee;
  }


  private class ACLTracePanel_descRadioButton_itemAdapter implements java.awt.event.ItemListener {

    ACLTracePanel_descRadioButton_itemAdapter(ACLTracePanel adaptee) {
      this.adaptee = adaptee;
    }


    public void itemStateChanged(ItemEvent e) {
      adaptee.descRadioButton_itemStateChanged(e);
    }


    ACLTracePanel adaptee;
  }



  private class ACLTracePanel_sortButton_actionAdapter implements java.awt.event.ActionListener {

    ACLTracePanel_sortButton_actionAdapter(ACLTracePanel adaptee) {
      this.adaptee = adaptee;
    }


    public void actionPerformed(ActionEvent e) {
      adaptee.sortButton_actionPerformed(e);
    }


    ACLTracePanel adaptee;
  }


  private class ACLFileFilter extends javax.swing.filechooser.FileFilter {

    public ACLFileFilter() { }


    /**
     *  The description of this filter. For example: "JPG and GIF Images"
     *
     * @return    The Description value
     * @see       FileView#getName
     */
    public String getDescription() {
      return "ACLMessage files (*.acl)";
    }


    public boolean accept(File pathName) {
      if (pathName.isDirectory()) {
        return true;
      }
      else if (pathName.isFile() &&
        (pathName.getName().endsWith(".acl"))) {
        return true;
      }
      else {
        return false;
      }
    }
  }


  private class TraceFileFilter extends javax.swing.filechooser.FileFilter {

    public TraceFileFilter() { }


    /**
     *  The description of this filter. For example: "JPG and GIF Images"
     *
     * @return    The Description value
     * @see       FileView#getName
     */
    public String getDescription() {
      return "ACLMessageTrace files (*.trc)";
    }


    public boolean accept(File pathName) {
      if (pathName.isDirectory()) {
        return true;
      }
      else if (pathName.isFile() &&
        (pathName.getName().endsWith(".trc"))) {
        return true;
      }
      else {
        return false;
      }
    }


    private String extensions[] = {".trc"};
  }



  private class AboutFrame extends JWindow {

    public AboutFrame() {
      try {
        jbInit();
        this.setSize(400, 175);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(screenSize.width / 2 - this.getSize().width / 2,
          screenSize.height / 2 - this.getSize().height / 2);

      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }


    void logoLabel_mouseClicked(MouseEvent e) {
      this.setVisible(false);
    }


    void logoLabel3_mousePressed(MouseEvent e) {
      this.setVisible(false);
    }


    void jLabel2_mouseClicked(MouseEvent e) {
      this.setVisible(false);
    }


    void logoLabel3_mouseClicked(MouseEvent e) {
      this.setVisible(false);
    }


    void logoLabel_mouseEntered(MouseEvent e) {
      this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }


    void logoLabel_mouseExited(MouseEvent e) {
      this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }


    void logoLabel3_mouseEntered(MouseEvent e) {
      this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }


    void logoLabel3_mouseExited(MouseEvent e) {
      this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }


    void jLabel2_mouseEntered(MouseEvent e) {
      this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }


    void jLabel2_mouseExited(MouseEvent e) {
      this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }


    void logoLabel_mousePressed(MouseEvent e) {
      this.setVisible(false);
    }


    void logoLabel_mouseReleased(MouseEvent e) {
      this.setVisible(false);
    }


    void jLabel2_mousePressed(MouseEvent e) {
      this.setVisible(false);
    }


    void jLabel2_mouseReleased(MouseEvent e) {
      this.setVisible(false);
    }


    void jLabel4_mouseClicked(MouseEvent e) {
      this.setVisible(false);
    }


    void contentPanel_mouseClicked(MouseEvent e) {
      this.setVisible(false);
    }


    void jLabel1_mouseClicked(MouseEvent e) {
      this.setVisible(false);
    }


    void jLabel4_mouseEntered(MouseEvent e) {
      this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }


    void jLabel1_mouseEntered(MouseEvent e) {
      this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }


    void contentPanel_mouseEntered(MouseEvent e) {
      this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }


    void jLabel4_mouseExited(MouseEvent e) {
      this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }


    void contentPanel_mouseExited(MouseEvent e) {
      this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }


    private void jbInit() throws Exception {
      // this.setClosable(true);
      //this.setOpaque(false);
      border1 = new TitledBorder(BorderFactory.createLineBorder(new Color(0, 0, 128), 1), "jade.tools.gui");
      this.getContentPane().setBackground(Color.white);
      this.getContentPane().setLayout(gridBagLayout1);
      contentPanel.setLayout(gridBagLayout2);
      jLabel1.setText("donated by Acklin B.V. to the Jade project");
      jLabel1.addMouseListener(
        new java.awt.event.MouseAdapter() {
          public void mouseClicked(MouseEvent e) {
            jLabel1_mouseClicked(e);
          }


          public void mouseEntered(MouseEvent e) {
            jLabel1_mouseEntered(e);
          }
        });
      jLabel2.setFont(new java.awt.Font("Dialog", 0, 12));
      jLabel2.setText("web: www.acklin.nl  |  email: info@acklin.nl");
      jLabel2.addMouseListener(
        new java.awt.event.MouseAdapter() {
          public void mouseClicked(MouseEvent e) {
            jLabel2_mouseClicked(e);
          }


          public void mouseEntered(MouseEvent e) {
            jLabel2_mouseEntered(e);
          }


          public void mouseExited(MouseEvent e) {
            jLabel2_mouseExited(e);
          }
        });
      contentPanel.setBackground(Color.white);
      contentPanel.setFont(new java.awt.Font("Dialog", 0, 11));
      contentPanel.setBorder(border1);
      contentPanel.addMouseListener(
        new java.awt.event.MouseAdapter() {
          public void mouseClicked(MouseEvent e) {
            contentPanel_mouseClicked(e);
          }


          public void mouseEntered(MouseEvent e) {
            contentPanel_mouseEntered(e);
          }


          public void mouseExited(MouseEvent e) {
            contentPanel_mouseExited(e);
          }
        });
      logoLabel3.setFont(new java.awt.Font("SansSerif", 1, 60));
      logoLabel3.setForeground(new Color(0, 0, 128));
      logoLabel3.setText("Acklin");
      logoLabel3.addMouseListener(
        new java.awt.event.MouseAdapter() {
          public void mouseClicked(MouseEvent e) {
            logoLabel3_mouseClicked(e);
          }


          public void mouseEntered(MouseEvent e) {
            logoLabel3_mouseEntered(e);
          }


          public void mouseExited(MouseEvent e) {
            logoLabel3_mouseExited(e);
          }
        });
      jLabel4.setFont(new java.awt.Font("Dialog", 1, 20));
      jLabel4.setForeground(Color.darkGray);
      jLabel4.setText("agent based support");
      jLabel4.addMouseListener(
        new java.awt.event.MouseAdapter() {
          public void mouseClicked(MouseEvent e) {
            jLabel4_mouseClicked(e);
          }


          public void mouseEntered(MouseEvent e) {
            jLabel4_mouseEntered(e);
          }


          public void mouseExited(MouseEvent e) {
            jLabel4_mouseExited(e);
          }
        });
      this.getContentPane().add(contentPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
      contentPanel.add(jLabel1, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 5, 0), 0, 0));
      contentPanel.add(jLabel2, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(10, 0, 10, 0), 0, 0));
      contentPanel.add(logoLabel3, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
      contentPanel.add(jLabel4, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 20, 0), 0, 0));
    }


    GridBagLayout gridBagLayout1 = new GridBagLayout();

    JPanel contentPanel = new JPanel();
    GridBagLayout gridBagLayout2 = new GridBagLayout();
    JLabel jLabel1 = new JLabel();
    JLabel jLabel2 = new JLabel();
    JLabel logoLabel3 = new JLabel();
    JLabel jLabel4 = new JLabel();
    Border border1;

  }


  private class ACLTreePanel_stasticsMenuItem_actionAdapter implements java.awt.event.ActionListener {

    /**
     *  Constructor for the ACLTreePanel_stasticsMenuItem_actionAdapter object
     *
     * @param  adaptee  Description of Parameter
     */
    ACLTreePanel_stasticsMenuItem_actionAdapter(ACLTracePanel adaptee) {
      this.adaptee = adaptee;
    }


    /**
     *  Description of the Method
     *
     * @param  e  Description of Parameter
     */
    public void actionPerformed(ActionEvent e) {
      adaptee.stasticsMenuItem_actionPerformed(e);
    }


    ACLTracePanel adaptee;
  }
//  ***EOF***


//  ***EOF***

  private class ACLTreePanel_this_mouseAdapter extends java.awt.event.MouseAdapter {

    /**
     *  Constructor for the ACLTreePanel_this_mouseAdapter object
     *
     * @param  adaptee  Description of Parameter
     */
    ACLTreePanel_this_mouseAdapter(ACLTracePanel adaptee) {
      this.adaptee = adaptee;
    }


    /**
     *  Description of the Method
     *
     * @param  e  Description of Parameter
     */
    public void mouseClicked(MouseEvent e) {

    }


    ACLTracePanel adaptee;

  }
//  ***EOF***

  private class ACLTreePanel_aclTree_mouseAdapter extends java.awt.event.MouseAdapter {

    /**
     *  Constructor for the ACLTreePanel_aclTree_mouseAdapter object
     *
     * @param  adaptee  Description of Parameter
     */
    ACLTreePanel_aclTree_mouseAdapter(ACLTracePanel adaptee) {
      this.adaptee = adaptee;
    }


    /**
     *  Description of the Method
     *
     * @param  e  Description of Parameter
     */
    public void mouseClicked(MouseEvent e) {
      adaptee.aclTree_mouseClicked(e);
    }


    ACLTracePanel adaptee;
  }


  private class ACLTreePanel_systemMenuItem_actionAdapter implements java.awt.event.ActionListener {

    /**
     *  Constructor for the ACLTreePanel_systemMenuItem_actionAdapter object
     *
     * @param  adaptee  Description of Parameter
     */
    ACLTreePanel_systemMenuItem_actionAdapter(ACLTracePanel adaptee) {
      this.adaptee = adaptee;
    }


    /**
     *  Description of the Method
     *
     * @param  e  Description of Parameter
     */
    public void actionPerformed(ActionEvent e) {
      adaptee.systemMenuItem_actionPerformed(e);
    }


    ACLTracePanel adaptee;
  }


  private class ACLTreePanel_zoomMenuItem_actionAdapter implements java.awt.event.ActionListener {

    /**
     *  Constructor for the ACLTreePanel_zoomMenuItem_actionAdapter object
     *
     * @param  adaptee  Description of Parameter
     */
    ACLTreePanel_zoomMenuItem_actionAdapter(ACLTracePanel adaptee) {
      this.adaptee = adaptee;
    }


    /**
     *  Description of the Method
     *
     * @param  e  Description of Parameter
     */
    public void actionPerformed(ActionEvent e) {
      adaptee.zoomMenuItem_actionPerformed(e);
    }


    ACLTracePanel adaptee;
  }
//  ***EOF***

  private class ACLTreePanel_saveMenuItem_actionAdapter implements java.awt.event.ActionListener {

    /**
     *  Constructor for the ACLTreePanel_saveMenuItem_actionAdapter object
     *
     * @param  adaptee  Description of Parameter
     */
    ACLTreePanel_saveMenuItem_actionAdapter(ACLTracePanel adaptee) {
      this.adaptee = adaptee;
    }


    /**
     *  Description of the Method
     *
     * @param  e  Description of Parameter
     */
    public void actionPerformed(ActionEvent e) {
      adaptee.saveMenuItem_actionPerformed(e);
    }


    ACLTracePanel adaptee;
  }

//  ***EOF***

  private class ACLTreePanel_aclTree_keyAdapter extends java.awt.event.KeyAdapter {

    /**
     *  Constructor for the ACLTreePanel_aclTree_keyAdapter object
     *
     * @param  adaptee  Description of Parameter
     */
    ACLTreePanel_aclTree_keyAdapter(ACLTracePanel adaptee) {
      this.adaptee = adaptee;
    }


    /**
     *  Description of the Method
     *
     * @param  e  Description of Parameter
     */
    public void keyTyped(KeyEvent e) {
      adaptee.aclTree_keyTyped(e);
    }


    ACLTracePanel adaptee;
  }

//  ***EOF***

  private class ACLTreePanel_deleteMenuItem_actionAdapter implements java.awt.event.ActionListener {

    /**
     *  Constructor for the ACLTreePanel_deleteMenuItem_actionAdapter object
     *
     * @param  adaptee  Description of Parameter
     */
    ACLTreePanel_deleteMenuItem_actionAdapter(ACLTracePanel adaptee) {
      this.adaptee = adaptee;
    }


    /**
     *  Description of the Method
     *
     * @param  e  Description of Parameter
     */
    public void actionPerformed(ActionEvent e) {
      adaptee.deleteMenuItem_actionPerformed(e);
    }


    ACLTracePanel adaptee;
  }


  /**
   *  This class renderes the ACLTreePanel
   *
   * @author     Chris van Aart - Acklin B.V., the Netherlands
   * @created    April 26, 2002
   */

  private class ACLTreeRenderer extends JLabel implements TreeCellRenderer {

    /**
     *  Constructor for the ACLTreeRenderer object
     */
    ACLTreeRenderer() {
      try {
        jbInit();
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }


    /**
     *  Gets the TreeCellRendererComponent attribute of the ACLTreeRenderer
     *  object
     *
     * @param  tree        Description of Parameter
     * @param  value       Description of Parameter
     * @param  isSelected  Description of Parameter
     * @param  expanded    Description of Parameter
     * @param  leaf        Description of Parameter
     * @param  row         Description of Parameter
     * @param  hasFocus    Description of Parameter
     * @return             The TreeCellRendererComponent value
     */
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected,
                                                  boolean expanded, boolean leaf, int row, boolean hasFocus) {

      setText(((DefaultMutableTreeNode)value).toString());
      int depth = ((DefaultMutableTreeNode)value).getLevel();
      this.setFont(new java.awt.Font("SansSerif", 0, 11));
      Color theColor = Color.black;
      String sValue = value.toString();
      switch (depth) {
        case 0:// root
          setIcon(rootIcon);
          break;
        case 1:
          if (sValue.indexOf("in:") > 0) {
            setIcon(incomingIcon);
            theColor = Color.red;
          }
          else {
            setIcon(outgoingIcon);
            theColor = Color.blue;
          }
          break;
        case 2:
          setIcon(nodeIcon);
          if (sValue.startsWith(":act ")) {
            setIcon(this.messageTypeIcon);
            String performative = sValue.substring(5, sValue.length());
            theColor = ACLPerformativesRenderer.determineColor(performative);
            this.setFont(new java.awt.Font("SansSerif", 1, 11));
          }
          if (sValue.startsWith(":content")) {
            setIcon(this.detailsIcon);
          }

          if (sValue.startsWith(":sender")) {
            setIcon(this.smallAgentIcon);
          }

          if ((sValue.startsWith(":receiver")) || (sValue.startsWith(":reply-to"))) {
            setIcon(this.smallAgentIcon);
          }

          break;
        case 3:
          setIcon(this.detailsIcon);
          break;
        //default:
        //  setIcon(nodeIcon);
      }
      setBackground(isSelected ? Color.blue : Color.white);
      setForeground(isSelected ? Color.white : theColor);

      return this;
    }


    /**
     *  build up renderer
     *
     * @exception  Exception  Description of Exception
     */
    private void jbInit() throws Exception {
      this.setFont(new java.awt.Font("SansSerif", 0, 11));
      this.setOpaque(true);
    }


    public ImageIcon rootIcon = new ImageIcon(this.getClass().getResource("images/inbox.gif"));
    public ImageIcon nodeIcon = new ImageIcon(this.getClass().getResource("images/service.gif"));
    public ImageIcon smallAgentIcon = new ImageIcon(this.getClass().getResource("images/smallagent.gif"));
    public ImageIcon messageTypeIcon = new ImageIcon(this.getClass().getResource("images/messagetype.gif"));
    public ImageIcon detailsIcon = new ImageIcon(this.getClass().getResource("images/details.gif"));
    public ImageIcon incomingIcon = new ImageIcon(this.getClass().getResource("images/incoming.gif"));
    public ImageIcon outgoingIcon = new ImageIcon(this.getClass().getResource("images/outgoing.gif"));
  }


  private class SortRenderer extends JLabel implements ListCellRenderer {

    /**
     *  Constructor for the ACLPerformativesRenderer object
     */
    public SortRenderer() {
      setOpaque(true);
      setFont(new java.awt.Font("Dialog", 0, 10));
    }



    /**
     *  Gets the ListCellRendererComponent attribute of the
     *  ACLPerformativesRenderer object
     *
     * @param  list          Description of Parameter
     * @param  value         Description of Parameter
     * @param  index         Description of Parameter
     * @param  isSelected    Description of Parameter
     * @param  cellHasFocus  Description of Parameter
     * @return               The ListCellRendererComponent value
     */
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      if (value != null) {
        String sValue = (String)value;
        setText((String)value);

        setBackground(isSelected ? Color.blue : Color.white);
        setForeground(isSelected ? Color.white : Color.blue);
      }
      return this;
    }
  }


  private class ACLTracePanel_aboutMenuItem_mouseAdapter extends java.awt.event.MouseAdapter {

    ACLTracePanel_aboutMenuItem_mouseAdapter(ACLTracePanel adaptee) {
      this.adaptee = adaptee;
    }


    public void mouseClicked(MouseEvent e) {
      adaptee.aboutMenuItem_mouseClicked(e);
    }


    ACLTracePanel adaptee;
  }


  private class ACLTracePanel_clearQMenuItem_actionAdapter implements java.awt.event.ActionListener {

    ACLTracePanel_clearQMenuItem_actionAdapter(ACLTracePanel adaptee) {
      this.adaptee = adaptee;
    }


    public void actionPerformed(ActionEvent e) {
      adaptee.clearQMenuItem_actionPerformed(e);
    }


    ACLTracePanel adaptee;
  }


  private class ACLTracePanel_openQMenuItem_actionAdapter implements java.awt.event.ActionListener {

    ACLTracePanel_openQMenuItem_actionAdapter(ACLTracePanel adaptee) {
      this.adaptee = adaptee;
    }


    public void actionPerformed(ActionEvent e) {
      adaptee.openQMenuItem_actionPerformed(e);
    }


    ACLTracePanel adaptee;
  }


  private class ACLTracePanel_aboutMenuItem_actionAdapter implements java.awt.event.ActionListener {

    ACLTracePanel_aboutMenuItem_actionAdapter(ACLTracePanel adaptee) {
      this.adaptee = adaptee;
    }


    public void actionPerformed(ActionEvent e) {
      adaptee.aboutMenuItem_actionPerformed(e);
    }


    ACLTracePanel adaptee;
  }


  public final static String DIRECTION_IN = "in";
  public final static String DIRECTION_OUT = "out";

  private static DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
  JLabel jLabel1 = new JLabel();
  ButtonGroup sortingButtonGroup = new ButtonGroup();
  JComboBox sortComboBox = new JComboBox();
  JRadioButton ascRadioButton = new JRadioButton();
  JRadioButton descRadioButton = new JRadioButton();
  JButton sortButton = new JButton();
  JMenuItem saveQMenuItem = new JMenuItem();
  JMenuItem openQMenuItem = new JMenuItem();
  JMenuItem clearQMenuItem = new JMenuItem();
  Border border2;
  Border border3;
  private boolean sorting = false;
  private JMenuItem deleteMenuItem = new JMenuItem();

  private int inMsgCount = 0, outMsgCount = 0;

  private int aclIndex = 0;
  private DefaultMutableTreeNode aclRoot = new DefaultMutableTreeNode("messagetrace");
  private DefaultTreeModel aclModel = new DefaultTreeModel(aclRoot);
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private GridBagLayout gridBagLayout2 = new GridBagLayout();
  private JScrollPane aclTreeScrollPane = new JScrollPane();
  private JTree aclTree = new JTree();
  private ACLTreeRenderer aclTreeRenderer = new ACLTreeRenderer();
  private JPopupMenu thePopupMenu = new JPopupMenu();
  private JMenuItem systemMenuItem = new JMenuItem();
  private JMenuItem zoomMenuItem = new JMenuItem();

  private int currentSelection = 0;
  private JMenuItem saveMenuItem = new JMenuItem();
  private JMenuItem stasticsMenuItem = new JMenuItem();
  private JMenuItem aboutMenuItem = new JMenuItem();

  private ImageIcon sortIcon =
    new ImageIcon(this.getClass().getResource("images/sort.gif"));

  private DefaultComboBoxModel sortComboBoxModel = new DefaultComboBoxModel();
  private Agent agent;

  private File currentDir;

  private int x, y;
  private ACLMessage currentACL;
  private Border border1;
  private TitledBorder titledBorder1;
}
//  ***EOF***
