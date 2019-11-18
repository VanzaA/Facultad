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
import java.lang.reflect.*;
import javax.swing.*;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Iterator;


/**
 *  This class is used to edit a particular AID.
 *
 * @author     Chris van Aart - Acklin B.V., the Netherlands
 * @created    April 26, 2002
 */

public class ACLAIDDialog extends JDialog {
  /**
   *  Constructor for the ACLAIDDialog object
   *
   * @param  agent  link to agent
   */
  public ACLAIDDialog(Agent agent) {
    this.agent = agent;
    this.setModal(true);
    try {
      this.resolverList = new ACLAIDList(agent);
      jbInit();
      this.setSize(350, 250);
      setItsAID(new AID("", true));
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }


  /**
   *  Gets the ItsAID attribute of the ACLAIDDialog object
   *
   * @return    The ItsAID value
   */
  public AID getItsAID() {
    return itsAID;
  }


  /**
   *  Gets the OK attribute of the ACLAIDDialog object. OK is true when the
   *  user has pressed the ob button of the dialog.
   *
   * @return    The OK value
   */
  public boolean getOK() {
    return userAction.equals(OK);
  }


  /**
   *  Sets the Editable attribute of the ACLAIDDialog object
   *
   * @param  theBool  The new Editable value
   */
  public void setEditable(boolean theBool) {
    if (!theBool) {
      OK = "CLOSED";
      this.cancelButton.setVisible(false);
      this.nameTextField.setEnabled(false);
      this.localCheckBox.setEnabled(false);
      this.resolverList.setEditable(false);
      this.addressesList.setEditable(false);
    }
  }


  /**
   *  Sets the ItsAID attribute of the ACLAIDDialog object
   *
   * @param  newItsAID  The new ItsAID value
   */
  public void setItsAID(AID newItsAID) {
    itsAID = newItsAID;
    nameTextField.setText(itsAID.getName());
    addressesList.register(itsAID);
    resolverList.register(itsAID, "Resolvers");

    String name = itsAID.getName();
    if ((name != null) && (!name.endsWith(agent.getHap()))) {
      this.localCheckBox.setSelected(true);
    }

  }


  /**
   *  Sets the UserAction attribute of the ACLAIDDialog object
   *
   * @param  newUserAction  The new UserAction value
   */
  public void setUserAction(String newUserAction) {
    userAction = newUserAction;
  }


  /**
   *  Method triggered by the OK button
   *
   * @param  e  ActionEvent of the OK button
   */
  void okButton_actionPerformed(ActionEvent e) {
    setUserAction(OK);
    this.setVisible(false);
  }


  /**
   *  Method triggered by the cancel button
   *
   * @param  e  ActionEvent of the cancel button
   */
  void cancelButton_actionPerformed(ActionEvent e) {
    setUserAction(CANCELLED);
    this.setVisible(false);
  }


  /**
   *  Method triggered when leaving the textfield of "name"
   *
   * @param  e  FocusEvent of FocusLoust
   */
  void nameTextField_focusLost(FocusEvent e) {
    updateSenderName();
  }


  /**
   *  Updates the name field of the current AID. The localCheckBox indicates
   *  wheter the agent is local or not.
   */
  void updateSenderName() {
    if (localCheckBox.isSelected()) {
      itsAID.setName(nameTextField.getText());
    }

    else {
      itsAID.setLocalName(nameTextField.getText());
    }

  }


  /**
   *  Method triggered by changing the state of the checkbox of "local"
   *
   * @param  e  ItemEvent belonging to itemStateChanged
   */
  void localCheckBox_itemStateChanged(ItemEvent e) {
    updateSenderName();
  }


  /**
   *  builds up the dialog
   *
   * @exception  Exception  thrown when someting goes wrong
   */
  private void jbInit() throws Exception {
    jLabel1.setFont(new java.awt.Font("Dialog", 0, 11));
    jLabel1.setText("sender:");
    this.getContentPane().setLayout(gridBagLayout1);
    nameTextField.setFont(new java.awt.Font("Dialog", 0, 11));
    nameTextField.setDisabledTextColor(Color.black);
    nameTextField.setText("someagent");
    nameTextField.addFocusListener(
      new java.awt.event.FocusAdapter() {
        public void focusLost(FocusEvent e) {
          nameTextField_focusLost(e);
        }
      });
    this.getContentPane().setBackground(Color.white);
    jLabel2.setFont(new java.awt.Font("Dialog", 0, 11));
    jLabel2.setText("addresses:");
    jLabel3.setFont(new java.awt.Font("Dialog", 0, 11));
    jLabel3.setText("resolvers");
    localCheckBox.setToolTipText("Select if the name is not a GUID.");
    localCheckBox.setBackground(Color.white);
    localCheckBox.setFont(new java.awt.Font("Dialog", 0, 11));
    localCheckBox.addItemListener(
      new java.awt.event.ItemListener() {
        public void itemStateChanged(ItemEvent e) {
          localCheckBox_itemStateChanged(e);
        }
      });
    okButton.setBackground(Color.white);
    okButton.setFont(new java.awt.Font("Dialog", 0, 12));
    okButton.setText("ok");
    okButton.addActionListener(
      new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          okButton_actionPerformed(e);
        }
      });
    cancelButton.setBackground(Color.white);
    cancelButton.setFont(new java.awt.Font("Dialog", 0, 12));
    cancelButton.setText("cancel");
    cancelButton.addActionListener(
      new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          cancelButton_actionPerformed(e);
        }
      });
    buttonPanel.setBackground(Color.white);
    this.getContentPane().add(nameTextField, new GridBagConstraints(2, 0, 1, 1, 1.0, 0.0
      , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    this.getContentPane().add(addressesList, new GridBagConstraints(2, 1, 2, 1, 1.0, 1.0
      , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    this.getContentPane().add(jLabel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
      , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.getContentPane().add(jLabel2, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0
      , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.getContentPane().add(resolverList, new GridBagConstraints(2, 2, 2, 1, 1.0, 1.0
      , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    this.getContentPane().add(jLabel3, new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0
      , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.getContentPane().add(buttonPanel, new GridBagConstraints(0, 3, 4, 1, 1.0, 0.0
      , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    buttonPanel.add(okButton, null);
    buttonPanel.add(cancelButton, null);
    this.getContentPane().add(localCheckBox, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
      , GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
  }


  public JCheckBox localCheckBox = new JCheckBox();

  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JLabel jLabel1 = new JLabel();
  private JTextField nameTextField = new JTextField();
  private JLabel jLabel2 = new JLabel();
  private JLabel jLabel3 = new JLabel();
  private JPanel buttonPanel = new JPanel();
  private JButton okButton = new JButton();
  private JButton cancelButton = new JButton();

  private String OK = "ok";
  private String CANCELLED = "cancelled";
  private AIDAddressList addressesList = new AIDAddressList();
  private boolean editable = true;
  private String userAction = CANCELLED;
  private ACLAIDList resolverList;

  private Agent agent;
  private jade.core.AID itsAID;
}
//  ***EOF***
