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

//java
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import javax.swing.*;
import javax.swing.text.*;
import jade.core.AID;

import jade.core.Agent;
import jade.domain.FIPAAgentManagement.Envelope;
import jade.domain.FIPAAgentManagement.ReceivedObject;
import jade.lang.acl.*;
import jade.tools.sl.SLFormatter;

import jade.util.Logger;

/**
 *  Description of the Class
 *
 * @author     Chris van Aart - Acklin B.V., the Netherlands
 * @created    April 26, 2002
 */

public class ACLPanel extends JPanel {

   private Logger logger = Logger.getMyLogger(this.getClass().getName());
  /**
   *  Constructor for the ACLPanel object
   *
   * @param  agent  Description of Parameter
   */
  public ACLPanel(Agent agent) {
    try {
      this.agent = agent;
      receiverList = new ACLAIDList(agent);
      replytoList = new ACLAIDList(agent);
      envToList = new ACLAIDList(agent);
      envIntendedReceiversList = new ACLAIDList(agent);
      jbInit();

      itsMsg.setSender(agent.getAID());
      setItsMsg(itsMsg);

      editsVector.add(performativesComboBox);
      editsVector.add(languageTextField);
      editsVector.add(ontologyTextField);
      editsVector.add(encodingTextField);
      editsVector.add(protocolComboBox);
      editsVector.add(inreplytoTextField);
      editsVector.add(replywithTextField);
      editsVector.add(ontologyTextField);
      editsVector.add(conversationTextField);
      editsVector.add(receiverList);
      editsVector.add(replytoList);
      editsVector.add(contentTextArea);
      editsVector.add(userpropList);

      editsVector.add(envToList);
      editsVector.add(envCommentsTextArea);
      editsVector.add(envACLReprTextField);
      editsVector.add(envPayloadEncodingTextField);
      editsVector.add(envPayloadLengthTextField);
      editsVector.add(envIntendedReceiversList);

    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }


  /**
   *  Gets the ItsMsg attribute of the ACLPanel object
   *
   * @return    The ItsMsg value
   */
  public ACLMessage getItsMsg() {
    return itsMsg;
  }


  /**
   *  Sets the ItsMsg attribute of the ACLPanel object
   *
   * @param  msg  The new ItsMsg value
   */
  public void setItsMsg(ACLMessage msg) {
    itsMsg = msg;
    itsEnvelope = msg.getEnvelope();
    if (itsEnvelope == null) {
      itsEnvelope = new Envelope();
      msg.setEnvelope(itsEnvelope);
    }

    //message
    senderTextField.setText(itsMsg.getSender() != null ? itsMsg.getSender().getName() : "");
    performativesComboBox.registerPerformatives(itsMsg);
    languageTextField.register(itsMsg, "Language");
    ontologyTextField.register(itsMsg, "Ontology");
    encodingTextField.register(itsMsg, "Encoding");
    protocolComboBox.registerProtocol(itsMsg);
    inreplytoTextField.register(itsMsg, "InReplyTo");
    replywithTextField.register(itsMsg, "ReplyWith");
    replybyTextField.setText(itsMsg.getReplyByDate() != null ? ISO8601.toString(itsMsg.getReplyByDate()) : "");
    conversationTextField.register(itsMsg, "ConversationId");
    receiverList.register(itsMsg, "Receiver");
    replytoList.register(itsMsg, "ReplyTo");
    contentTextArea.register(itsMsg, "Content");
    userpropList.register(itsMsg, "");
    //env

    envToList.register(itsEnvelope, "To");
    envFromTextField.setText((itsEnvelope.getFrom() != null ? itsEnvelope.getFrom().getName() : ""));
    envCommentsTextArea.register(itsEnvelope, "Comments");
    envACLReprTextField.register(itsEnvelope, "AclRepresentation");
    envPayloadEncodingTextField.register(itsEnvelope, "PayloadEncoding");
    envPayloadLengthTextField.register(itsEnvelope, "PayloadLength");
    envDateTextField.setText(itsEnvelope.getDate() != null ? ISO8601.toString(itsEnvelope.getDate()) : "");
    envIntendedReceiversList.register(itsEnvelope, "IntendedReceiver");

  }


  /**
   *  Sets the ReadOnly attribute of the ACLPanel object
   */
  public void setReadOnly() {
    setEnabled(false);
    editable = false;
  }


  /**
   *  Sets the Disabled attribute of the ACLPanel object
   *
   * @param  theBool  The new Enabled value
   */
  public void setEnabled(boolean theBool) {
    editable = theBool;
    this.defaultEnvelopeButton.setEnabled(theBool);
    String methodName = "setEditable";
    for (int i = 0; i < editsVector.size(); i++) {
      Object obj = editsVector.get(i);
      try {
        Method sn = obj.getClass().getMethod(methodName, new Class[]{Boolean.TYPE});
        Object os = new Boolean(editable);
        sn.invoke(obj, new Object[]{os});
      }
      catch (Exception ex) {
        if(logger.isLoggable(Logger.WARNING))
        	logger.log(Logger.WARNING,"Obj: " + obj.getClass().toString() + " " + ex.getMessage());
      }
    }
  }


  /**
   *  Description of the Method
   */
  public void doSystemOut() {
    if(logger.isLoggable(Logger.WARNING))
    	logger.log(Logger.INFO,"\n" + itsMsg.toString() + "\n");
  }


  /**
   *  Description of the Method
   */
  public void saveACL() {

    UIManager.put("FileChooser.saveButtonToolTipText", "Save ACLMessage");

    JFileChooser chooser = new JFileChooser();
    chooser.setFileFilter(new ACLFileFilter());
    // since JDK1.3 chooser.setAcceptAllFileFilterUsed(false);
    chooser.setSelectedFile(new File("itsmessage.acl"));
    chooser.setDialogTitle("Save ACLMessage");
    if (currentDir != null) {
      chooser.setCurrentDirectory(currentDir);
    }

    int returnVal = chooser.showSaveDialog(null);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      currentDir = chooser.getCurrentDirectory();
      String fileName = chooser.getSelectedFile().getAbsolutePath();

      if (!fileName.endsWith(".acl")) {
        fileName = fileName + ".acl";
      }

      try {
        FileWriter f = new FileWriter(fileName);
        StringACLCodec codec = new StringACLCodec(null, f);
        codec.write(itsMsg);
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


  public void loadACL() {

    UIManager.put("FileChooser.openButtonToolTipText", "Open ACLMessage");

    JFileChooser chooser = new JFileChooser();
    chooser.setFileFilter(new ACLFileFilter());
    // since JDK1.3 chooser.setAcceptAllFileFilterUsed(false);
    chooser.setDialogTitle("Open ACLMessage");

    if (currentDir != null) {
      chooser.setCurrentDirectory(currentDir);
    }
    int returnVal = chooser.showOpenDialog(null);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      currentDir = chooser.getCurrentDirectory();
      String fileName = chooser.getSelectedFile().getAbsolutePath();

      try {
        StringACLCodec codec = new StringACLCodec(new FileReader(fileName), null);
        ACLMessage msg = codec.decode();
        this.setItsMsg(msg);
      }
      catch (FileNotFoundException e1) {
        JOptionPane.showMessageDialog(null, "File not found: " + fileName + e1.getMessage(), "Error Message", JOptionPane.ERROR_MESSAGE);
        if(logger.isLoggable(Logger.WARNING))
        	logger.log(Logger.WARNING,"File Not Found: " + fileName);
      }
      catch (ACLCodec.CodecException e2) {
        if(logger.isLoggable(Logger.WARNING))
        	logger.log(Logger.WARNING,"Wrong ACL Message in file: " + fileName);
        // e2.printStackTrace();
        JOptionPane.showMessageDialog(null, "Wrong ACL Message in file: " + fileName + "\n" + e2.getMessage(), "Error Message", JOptionPane.ERROR_MESSAGE);
      }
    }
  }


  public void doZoomContent() {

    ACLContentDialog theDialog = new ACLContentDialog(null, "ACLMessage content", true);
    theDialog.setItsContent(this.getItsMsg());
    theDialog.setEditable(editable);
    theDialog.setLocation((int)getLocationOnScreen().getX(), (int)getLocationOnScreen().getY());
    theDialog.setVisible(true);
    this.contentTextArea.update();
  }


  void setDefaultEnvelope() {
    itsMsg.setDefaultEnvelope();
    setItsMsg(itsMsg);
    if(logger.isLoggable(Logger.WARNING))
    	logger.log(Logger.CONFIG,":" + this.itsEnvelope.toString());
  }



  /**
   *  Description of the Method
   *
   * @param  e  Description of Parameter
   */
  void senderButton_actionPerformed(ActionEvent e) {
    doShowSender();
  }


  /**
   *  Description of the Method
   */
  void doShowSender() {
    ACLAIDDialog aidGui = new ACLAIDDialog(agent);
    AID currentAID = itsMsg.getSender();
    AID editAID = (AID)currentAID.clone();
    aidGui.setItsAID(editAID);
    aidGui.setLocation((int)getLocationOnScreen().getX(), (int)getLocationOnScreen().getY());
    aidGui.setTitle(editable ? "edit ACL: " + editAID.getName() : "view ACL: " + editAID.getName());
    aidGui.setEditable(editable);
    aidGui.setVisible(true);
    if (aidGui.getOK()) {
      itsMsg.setSender(aidGui.getItsAID());
      senderTextField.setText(itsMsg.getSender().getName());
    }

  }


  void doShowFrom() {
    ACLAIDDialog aidGui = new ACLAIDDialog(agent);
    AID currentAID = (itsMsg.getEnvelope().getFrom() != null ? itsMsg.getEnvelope().getFrom() : new AID());
    AID editAID = (AID)currentAID.clone();
    aidGui.setLocation((int)getLocationOnScreen().getX(), (int)getLocationOnScreen().getY());
    aidGui.setItsAID(editAID);
    aidGui.setTitle(editable ? "edit ACL: " + editAID.getName() : "view ACL: " + editAID.getName());
    aidGui.setEditable(editable);
    aidGui.setVisible(true);
    if (aidGui.getOK()) {
      itsMsg.getEnvelope().setFrom(aidGui.getItsAID());
      this.envFromTextField.setText(itsMsg.getEnvelope().getFrom().getName());
    }
  }



  /**
   *  Description of the Method
   *
   * @param  e  Description of Parameter
   */
  void replyByButton_actionPerformed(ActionEvent e) {
    doShowTimeDialog();
  }


  /**
   *  Description of the Method
   */
  void doShowTimeDialog() {
    ACLTimeChooserDialog t = new ACLTimeChooserDialog();
    Date theDate = itsMsg.getReplyByDate();
    if (theDate != null) {
      try {
        t.setDate(theDate);
      }
      catch (Exception ee) {
        JOptionPane.showMessageDialog(this, ee.getMessage(), "Incorrect date format", JOptionPane.ERROR_MESSAGE);
        if(logger.isLoggable(Logger.WARNING))
        	logger.log(Logger.WARNING,"Incorrect date format");
        return;
      }
    }

    if (editable) {
      if (t.showEditTimeDlg(null) == ACLTimeChooserDialog.OK) {
        itsMsg.setReplyByDate(t.getDate());
        if (itsMsg.getReplyByDate() == null) {
          this.replybyTextField.setText("");
        }
        else {
          this.replybyTextField.setText(ISO8601.toString(itsMsg.getReplyByDate()));
        }
      }
    }

    if (!editable) {
      t.showViewTimeDlg(null);
    }

  }



  void doShowEnvTimeDialog() {
    ACLTimeChooserDialog t = new ACLTimeChooserDialog();
    Date theDate = itsMsg.getEnvelope().getDate();
    if (theDate != null) {
      try {
        t.setDate(theDate);
      }
      catch (Exception ee) {
        JOptionPane.showMessageDialog(this, ee.getMessage(), "Incorrect date format", JOptionPane.ERROR_MESSAGE);
        if(logger.isLoggable(Logger.WARNING))
        	logger.log(Logger.WARNING,"Incorrect date format");
        return;
      }
    }

    if (editable) {
      if (t.showEditTimeDlg(null) == ACLTimeChooserDialog.OK) {
        itsMsg.getEnvelope().setDate(t.getDate());
        if (itsMsg.getEnvelope().getDate() == null) {
          this.envDateTextField.setText("");
        }

        else {
          this.envDateTextField.setText(ISO8601.toString(itsMsg.getEnvelope().getDate()));
        }

      }
    }

    if (!editable) {
      t.showViewTimeDlg(null);
    }

  }


  /**
   *  Description of the Method
   *
   * @param  e  Description of Parameter
   */
  void contentTextArea_mouseClicked(MouseEvent e) {
    if (e.getClickCount() > 2) {
      if(logger.isLoggable(Logger.WARNING))
      	logger.log(Logger.WARNING,"to do display content dialog");
    }

  }


  /**
   *  Description of the Method
   *
   * @param  e  Description of Parameter
   */
  void senderTextField_mouseClicked(MouseEvent e) {
    doShowSender();
  }


  /**
   *  Description of the Method
   *
   * @param  e  Description of Parameter
   */
  void replybyTextField_mouseClicked(MouseEvent e) {
    doShowTimeDialog();
  }


  void defaultEnvelopeButton_actionPerformed(ActionEvent e) {
    setDefaultEnvelope();
  }


  void fromButton_actionPerformed(ActionEvent e) {
    doShowFrom();
  }


  void envDateButton_actionPerformed(ActionEvent e) {
    this.doShowEnvTimeDialog();
  }


  void contentZoomButton_actionPerformed(ActionEvent e) {
    doZoomContent();
  }


  void envFromTextField_mouseClicked(MouseEvent e) {
    doShowFrom();
  }


  void envDateTextField_mouseClicked(MouseEvent e) {
    this.doShowEnvTimeDialog();
  }





  /**
   *  Description of the Method
   *
   * @exception  Exception  Description of Exception
   */
  private void jbInit() throws Exception {
    senderLabel.setFont(new java.awt.Font("Dialog", 0, 11));
    senderLabel.setText("sender:");
    this.setBackground(Color.white);
    this.setOpaque(false);
    this.setLayout(gridBagLayout1);
    receiverLabel.setFont(new java.awt.Font("Dialog", 0, 11));
    receiverLabel.setText("receivers:");
    replytoLabel.setFont(new java.awt.Font("Dialog", 0, 11));
    replytoLabel.setText("reply-to");
    contentLabel.setFont(new java.awt.Font("Dialog", 0, 11));
    contentLabel.setText("content");
    languageLabel.setFont(new java.awt.Font("Dialog", 0, 11));
    languageLabel.setText("language");
    encodingLabel.setFont(new java.awt.Font("Dialog", 0, 11));
    encodingLabel.setText("encoding");
    ontologyjLabel.setFont(new java.awt.Font("Dialog", 0, 11));
    ontologyjLabel.setText("ontology");
    protocolLabel.setFont(new java.awt.Font("Dialog", 0, 11));
    protocolLabel.setText("protocol");
    inreplytoLabel.setFont(new java.awt.Font("Dialog", 0, 11));
    inreplytoLabel.setText("in-reply-to");
    replywithLabel.setFont(new java.awt.Font("Dialog", 0, 11));
    replywithLabel.setText("reply-with");
    replybyLabel.setFont(new java.awt.Font("Dialog", 0, 11));
    replybyLabel.setText("reply-by");
    userpropLabel.setFont(new java.awt.Font("Dialog", 0, 11));
    userpropLabel.setText("User-prop.");
    protocolComboBox.setBackground(Color.white);
    protocolComboBox.setFont(new java.awt.Font("Dialog", 0, 11));
    replytoList.setFont(new java.awt.Font("Dialog", 0, 11));
    convidLabel.setFont(new java.awt.Font("Dialog", 0, 11));
    convidLabel.setText("conv.-id");
    userpropList.setFont(new java.awt.Font("Dialog", 0, 11));

    performativeLabel.setFont(new java.awt.Font("Dialog", 0, 11));
    performativeLabel.setText("performative");
    senderButton.setBackground(Color.white);
    senderButton.setFont(new java.awt.Font("Dialog", 0, 12));
    senderButton.setBorder(null);
    senderButton.setMaximumSize(new Dimension(23, 20));
    senderButton.setMinimumSize(new Dimension(25, 20));
    senderButton.setPreferredSize(new Dimension(25, 20));
    senderButton.setToolTipText("Edit/View Sender");
    senderButton.setIcon(zoomIcon);
    senderButton.setMargin(new Insets(0, 0, 0, 0));
    senderButton.addActionListener(
      new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          senderButton_actionPerformed(e);
        }
      });
    replyByButton.setBackground(Color.white);
    replyByButton.setFont(new java.awt.Font("Dialog", 0, 12));
    replyByButton.setBorder(null);
    replyByButton.setMaximumSize(new Dimension(23, 20));
    replyByButton.setMinimumSize(new Dimension(23, 20));
    replyByButton.setPreferredSize(new Dimension(23, 20));
    replyByButton.setToolTipText("Edit/View Reply-by");
    replyByButton.setIcon(dateIcon);
    replyByButton.setMargin(new Insets(0, 0, 0, 0));
    replyByButton.addActionListener(
      new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          replyByButton_actionPerformed(e);
        }
      });
    senderTextField.setFont(new java.awt.Font("Dialog", 0, 11));
    senderTextField.setDisabledTextColor(Color.black);
    senderTextField.setEnabled(false);
    senderTextField.addMouseListener(
      new java.awt.event.MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          senderTextField_mouseClicked(e);
        }
      });
    replybyTextField.setFont(new java.awt.Font("Dialog", 0, 11));
    replybyTextField.setDisabledTextColor(Color.black);
    replybyTextField.setEnabled(false);
    replybyTextField.addMouseListener(
      new java.awt.event.MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          replybyTextField_mouseClicked(e);
        }
      });
    performativesComboBox.setBackground(Color.white);

    aclTab.setLayout(gridBagLayout2);
    theTabbedPane.setBackground(Color.white);
    theTabbedPane.setFont(new java.awt.Font("Dialog", 0, 12));
    theTabbedPane.setBorder(BorderFactory.createLineBorder(Color.black));
    aclTab.setBackground(Color.white);
    aclTab.setBorder(BorderFactory.createLoweredBevelBorder());
    envelopeTab.setLayout(gridBagLayout3);
    envelopeTab.setBackground(Color.white);
    toLabel.setFont(new java.awt.Font("Dialog", 0, 11));
    toLabel.setText("to");
    fromLabel.setFont(new java.awt.Font("Dialog", 0, 11));
    fromLabel.setText("from");
    commentsLabel.setFont(new java.awt.Font("Dialog", 0, 11));
    commentsLabel.setText("comments");
    aclRepreLabel.setFont(new java.awt.Font("Dialog", 0, 11));
    aclRepreLabel.setToolTipText("ACL Repr");
    aclRepreLabel.setText("acl repr.");
    payLoadLabel.setFont(new java.awt.Font("Dialog", 0, 11));
    payLoadLabel.setToolTipText("Payload Length");
    payLoadLabel.setText("payload len.");
    envDateLabel.setFont(new java.awt.Font("Dialog", 0, 11));
    envDateLabel.setToolTipText("Date");
    envDateLabel.setText("date");
    intReceiversLabel.setFont(new java.awt.Font("Dialog", 0, 11));
    intReceiversLabel.setToolTipText("Intended Receivers");
    intReceiversLabel.setText("int. receivers");
    jLabel1.setFont(new java.awt.Font("Dialog", 0, 11));
    jLabel1.setToolTipText("Payload Encoding");
    jLabel1.setText("payload enc.");
    envDateTextField.setFont(new java.awt.Font("Dialog", 0, 11));
    envDateTextField.setToolTipText("");
    envDateTextField.setDisabledTextColor(Color.black);
    envDateTextField.addMouseListener(
      new java.awt.event.MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          envDateTextField_mouseClicked(e);
        }
      });
    envDateTextField.setEnabled(false);
    defaultEnvelopeButton.setBackground(Color.white);
    defaultEnvelopeButton.setFont(new java.awt.Font("Dialog", 0, 12));
    defaultEnvelopeButton.setIcon(envelopeIcon);
    defaultEnvelopeButton.setText("set Default Envelope");
    defaultEnvelopeButton.addActionListener(
      new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          defaultEnvelopeButton_actionPerformed(e);
        }
      });
    fromButton.addActionListener(
      new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          fromButton_actionPerformed(e);
        }
      });
    fromButton.setMargin(new Insets(0, 0, 0, 0));
    fromButton.setToolTipText("Edit/View From");
    fromButton.setIcon(zoomIcon);
    fromButton.setPreferredSize(new Dimension(23, 20));
    fromButton.setMinimumSize(new Dimension(23, 20));
    fromButton.setMaximumSize(new Dimension(23, 20));
    fromButton.setFont(new java.awt.Font("Dialog", 0, 12));
    fromButton.setBackground(Color.white);
    envFromTextField.setFont(new java.awt.Font("Dialog", 0, 11));
    envFromTextField.setDisabledTextColor(Color.black);
    envFromTextField.addMouseListener(
      new java.awt.event.MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          envFromTextField_mouseClicked(e);
        }
      });
    envFromTextField.setEnabled(false);
    envDateButton.setBackground(Color.white);
    envDateButton.setFont(new java.awt.Font("Dialog", 0, 12));
    envDateButton.setMaximumSize(new Dimension(23, 20));
    envDateButton.setMinimumSize(new Dimension(23, 20));
    envDateButton.setPreferredSize(new Dimension(23, 20));
    envDateButton.setToolTipText("Edit/View Date");
    envDateButton.setIcon(dateIcon);
    envDateButton.addActionListener(
      new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          envDateButton_actionPerformed(e);
        }
      });
    contentZoomButton.setBorder(null);
    contentZoomButton.setMaximumSize(new Dimension(23, 20));
    contentZoomButton.setMinimumSize(new Dimension(23, 20));
    contentZoomButton.setToolTipText("Zoom Content Of ACLMessage");
    contentZoomButton.setIcon(zoomIcon);
    contentZoomButton.addActionListener(
      new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          contentZoomButton_actionPerformed(e);
        }
      });
    contentTextArea.setText("");
    contentTextArea.setAutoscrolls(true);
    contentTextArea.setFont(new java.awt.Font("Dialog", 0, 11));
    aclTab.add(senderLabel, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0
      , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    aclTab.add(senderTextField, new GridBagConstraints(2, 1, 1, 1, 1.0, 0.0
      , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    aclTab.add(receiverLabel, new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0
      , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    aclTab.add(replytoLabel, new GridBagConstraints(0, 3, 2, 1, 0.0, 0.0
      , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    aclTab.add(replytoList, new GridBagConstraints(2, 3, 3, 1, 1.0, 1.0
      , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

    aclTab.add(languageLabel, new GridBagConstraints(0, 6, 2, 1, 0.0, 0.0
      , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    aclTab.add(encodingLabel, new GridBagConstraints(0, 7, 2, 1, 0.0, 0.0
      , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    aclTab.add(ontologyjLabel, new GridBagConstraints(0, 8, 2, 1, 0.0, 0.0
      , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    aclTab.add(protocolLabel, new GridBagConstraints(0, 9, 2, 1, 0.0, 0.0
      , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    aclTab.add(inreplytoLabel, new GridBagConstraints(0, 11, 2, 1, 0.0, 0.0
      , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    aclTab.add(replywithLabel, new GridBagConstraints(0, 12, 2, 1, 0.0, 0.0
      , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    aclTab.add(replybyLabel, new GridBagConstraints(0, 13, 2, 1, 0.0, 0.0
      , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    aclTab.add(userpropLabel, new GridBagConstraints(0, 14, 2, 1, 0.0, 0.0
      , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    aclTab.add(userpropList, new GridBagConstraints(2, 14, 3, 1, 1.0, 1.0
      , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    aclTab.add(encodingTextField, new GridBagConstraints(2, 7, 3, 1, 1.0, 0.0
      , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    aclTab.add(ontologyTextField, new GridBagConstraints(2, 8, 3, 1, 1.0, 0.0
      , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    aclTab.add(protocolComboBox, new GridBagConstraints(2, 9, 3, 1, 1.0, 0.0
      , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    aclTab.add(replywithTextField, new GridBagConstraints(2, 12, 3, 1, 1.0, 0.0
      , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    aclTab.add(replybyTextField, new GridBagConstraints(2, 13, 1, 1, 1.0, 0.0
      , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    aclTab.add(convidLabel, new GridBagConstraints(0, 10, 2, 1, 0.0, 0.0
      , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    aclTab.add(inreplytoTextField, new GridBagConstraints(2, 11, 3, 1, 1.0, 0.0
      , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    aclTab.add(conversationTextField, new GridBagConstraints(2, 10, 3, 1, 1.0, 0.0
      , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    aclTab.add(receiverList, new GridBagConstraints(2, 2, 3, 1, 1.0, 1.0
      , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    aclTab.add(languageTextField, new GridBagConstraints(2, 6, 3, 1, 1.0, 0.0
      , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    aclTab.add(performativesComboBox, new GridBagConstraints(2, 0, 3, 1, 1.0, 0.0
      , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    aclTab.add(performativeLabel, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0
      , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    aclTab.add(senderButton, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0
      , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    aclTab.add(replyByButton, new GridBagConstraints(3, 13, 1, 1, 0.0, 0.0
      , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    aclTab.add(contentLabel, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
      , GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    aclTab.add(contentZoomButton, new GridBagConstraints(3, 4, 1, 1, 0.0, 0.0
      , GridBagConstraints.NORTH, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    aclTab.add(contentTextArea, new GridBagConstraints(0, 5, 4, 1, 2.0, 2.0
      , GridBagConstraints.SOUTH, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

    envelopeTab.add(toLabel, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0
      , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    envelopeTab.add(commentsLabel, new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0
      , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    envelopeTab.add(aclRepreLabel, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
      , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    envelopeTab.add(payLoadLabel, new GridBagConstraints(0, 4, 2, 1, 0.0, 0.0
      , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    envelopeTab.add(envDateLabel, new GridBagConstraints(0, 6, 2, 1, 0.0, 0.0
      , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    //envelopeTab.add(encryptedLabel, new GridBagConstraints(0, 7, 2, 1, 0.0, 0.0
    //  , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    envelopeTab.add(intReceiversLabel, new GridBagConstraints(0, 7, 2, 1, 0.0, 0.0
      , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    envelopeTab.add(envACLReprTextField, new GridBagConstraints(2, 3, 2, 1, 1.0, 0.0
      , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    envelopeTab.add(jLabel1, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
      , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    envelopeTab.add(envPayloadLengthTextField, new GridBagConstraints(2, 4, 2, 1, 1.0, 0.0
      , GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    envelopeTab.add(envPayloadEncodingTextField, new GridBagConstraints(2, 5, 2, 1, 1.0, 0.0
      , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    envelopeTab.add(fromLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
      , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    envelopeTab.add(envToList, new GridBagConstraints(2, 1, 2, 1, 1.0, 1.0
      , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    envelopeTab.add(envDateTextField, new GridBagConstraints(2, 6, 1, 1, 1.0, 0.0
      , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    //envelopeTab.add(envEncryptedList, new GridBagConstraints(2, 7, 2, 1, 1.0, 1.0
    //  , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    envelopeTab.add(envIntendedReceiversList, new GridBagConstraints(2, 7, 2, 1, 1.0, 1.0
      , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    envelopeTab.add(defaultEnvelopeButton, new GridBagConstraints(2, 8, 1, 1, 0.0, 0.0
      , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 0, 0, 0), 0, 0));
    envelopeTab.add(envFromTextField, new GridBagConstraints(2, 0, 1, 1, 1.0, 0.0
      , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    envelopeTab.add(fromButton, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0
      , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    envelopeTab.add(envDateButton, new GridBagConstraints(3, 6, 1, 1, 0.0, 0.0
      , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    envelopeTab.add(commentsScrollPane, new GridBagConstraints(2, 2, 2, 1, 1.0, 1.0
      , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    commentsScrollPane.getViewport().add(envCommentsTextArea, null);
    this.add(theTabbedPane, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
      , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

    theTabbedPane.addTab("message", this.messageIcon, aclTab, "<html><h1>Message Tab</h1>" +
      "This tab represents an ACL message compliant to the <p>" +
      "<b>FIPA 2000</b> <p>FIPA ACL Message Structure Specification <p>" +
      "(fipa000061) specifications. <p>" +
      "All parameters are couples of <p>" +
      "<em>keyword: value</em> </html>");

    theTabbedPane.addTab("envelope", this.envelopeIcon, envelopeTab, "<html><h1>Envelope Tab</h1>" +
      " This tab represents an envelope <p>" +
      "attached to the ACLMessage. <p>" +
      "The envelope is used by the <p>" +
      "<b><it>ACC</it></b> for inter-platform messaging.</html>");
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


    private String extensions[] = {".acl"};
  }


  GridBagLayout gridBagLayout1 = new GridBagLayout();
  JLabel senderLabel = new JLabel();
  JLabel receiverLabel = new JLabel();
  JLabel replytoLabel = new JLabel();
  JLabel contentLabel = new JLabel();
  JLabel languageLabel = new JLabel();
  JLabel encodingLabel = new JLabel();
  JLabel ontologyjLabel = new JLabel();
  JLabel protocolLabel = new JLabel();
  JLabel inreplytoLabel = new JLabel();
  JLabel replywithLabel = new JLabel();
  JLabel replybyLabel = new JLabel();
  JLabel userpropLabel = new JLabel();

  JLabel convidLabel = new JLabel();
  JLabel performativeLabel = new JLabel();
  JButton senderButton = new JButton();
  JButton replyByButton = new JButton();
  JLabel toLabel = new JLabel();
  JLabel fromLabel = new JLabel();
  JLabel commentsLabel = new JLabel();
  JLabel aclRepreLabel = new JLabel();
  JLabel payLoadLabel = new JLabel();
  JLabel envDateLabel = new JLabel();
  JLabel intReceiversLabel = new JLabel();
  JLabel jLabel1 = new JLabel();
  JTextField envDateTextField = new JTextField();
  JButton defaultEnvelopeButton = new JButton();
  JButton fromButton = new JButton();
  JButton envDateButton = new JButton();
  JButton contentZoomButton = new JButton();
  ACLTextArea contentTextArea = new ACLTextArea();
  private ACLTextField envACLReprTextField = new ACLTextField();
  private ACLTextField envPayloadLengthTextField = new ACLTextField();
  private ACLTextField envPayloadEncodingTextField = new ACLTextField();
  private JTextField envFromTextField = new JTextField();
  private boolean editable = true;
  private JTextField senderTextField = new JTextField();
  private ACLPropertyList userpropList = new ACLPropertyList();

  private ACLTextField conversationTextField = new ACLTextField();
  private ACLTextField languageTextField = new ACLTextField();
  private ACLTextField encodingTextField = new ACLTextField();
  private ACLTextField ontologyTextField = new ACLTextField();
  private ACLComboBox protocolComboBox = new ACLComboBox();
  private ACLTextField inreplytoTextField = new ACLTextField();
  private ACLTextField replywithTextField = new ACLTextField();
  private JTextField replybyTextField = new JTextField();
  private ACLComboBox performativesComboBox = new ACLComboBox();
  private ACLMessage itsMsg = new ACLMessage(ACLMessage.INFORM);

  private Vector editsVector = new Vector();
  private JPanel aclTab = new JPanel();
  private GridBagLayout gridBagLayout2 = new GridBagLayout();
  private JTabbedPane theTabbedPane = new JTabbedPane();
  private JPanel envelopeTab = new JPanel();
  private GridBagLayout gridBagLayout3 = new GridBagLayout();
  private JScrollPane commentsScrollPane = new JScrollPane();
  private EnvCommentsTextArea envCommentsTextArea = new EnvCommentsTextArea();

  private ImageIcon zoomIcon =
    new ImageIcon(this.getClass().getResource("images/zoom.gif"));
  private ImageIcon dateIcon =
    new ImageIcon(this.getClass().getResource("images/date.gif"));
  private ImageIcon envelopeIcon =
    new ImageIcon(this.getClass().getResource("images/envelope.gif"));
  private ImageIcon messageIcon =
    new ImageIcon(this.getClass().getResource("images/message.gif"));
  private ACLAIDList envToList;
  private ACLAIDList envIntendedReceiversList;
  private Agent agent;
  private ACLAIDList receiverList;
  private ACLAIDList replytoList;

  private File currentDir;
  private Envelope itsEnvelope;

}
//  ***EOF***
