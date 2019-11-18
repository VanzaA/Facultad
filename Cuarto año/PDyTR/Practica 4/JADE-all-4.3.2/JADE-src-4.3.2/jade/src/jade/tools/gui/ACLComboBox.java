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

import java.awt.Color;
import java.awt.event.*;
import java.lang.*;
import java.lang.reflect.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

import jade.lang.acl.*;
import jade.domain.FIPANames;

/**
 *  This class is used for selecting performatives or protocols of ACLMessage.
 *
 * @author     Chris van Aart - Acklin B.V., the Netherlands
 * @created    April 26, 2002
 */

public class ACLComboBox extends JComboBox implements Observer {

  /**
   *  Sets the Editable attribute of the ACLComboBox object
   *
   * @param  theBool  The new Editable value
   */
  public void setEditable(boolean theBool) {
    super.setEditable(theBool);
    this.setEnabled(theBool);
  }


  /**
   *  register performatives in the comboBoxModel
   *
   * @param  arg  part of observer interface
   */
  public void registerPerformatives(Object arg) {
    comboBoxModel.removeAllElements();
    String[] names = ACLMessage.getAllPerformativeNames();
    for (int i = 0; i < names.length; i++) {
      comboBoxModel.addElement(names[i]);
    }

    this.setModel(comboBoxModel);
    this.setRenderer(new ACLPerformativesRenderer());
    this.msg = (ACLMessage)arg;
    this.setSelectedIndex(msg.getPerformative());
    String methodName = "get" + fieldName;
    String theType = "java.lang.String";
    this.setBackground(Color.white);
    this.setFont(new java.awt.Font("Dialog", 1, 11));
    Color foreGround = ACLPerformativesRenderer.determineColor(msg.getPerformative(msg.getPerformative()));
    this.setForeground(foreGround);
    UIManager.put("ComboBox.disabledForeground", foreGround);
    UIManager.put("ComboBox.disabledBackground", Color.white);
    this.addItemListener(
      new java.awt.event.ItemListener() {
        public void itemStateChanged(ItemEvent e) {
          performativeItemStateChanged();
        }
      });
    repaint();
    updateUI();
  }


  /**
   *  Register protocols from ACLMessage
   *
   * @param  arg  ACLMessage
   */
  public void registerProtocol(Object arg) {
    comboBoxModel.removeAllElements();
    for (int i = 0; i < fipaProtocols.length; i++) {
      comboBoxModel.addElement(fipaProtocols[i]);
    }

    this.setModel(comboBoxModel);
    this.msg = (ACLMessage)arg;

    this.setSelectedItem(msg.getProtocol());
    String methodName = "get" + fieldName;
    String theType = "java.lang.String";
    this.setBackground(Color.white);
    UIManager.put("ComboBox.disabledForeground", Color.black);
    UIManager.put("ComboBox.disabledBackground", Color.white);

    this.setFont(new java.awt.Font("Dialog", 0, 11));
    this.setEditable(true);

    this.addItemListener(
      new java.awt.event.ItemListener() {
        public void itemStateChanged(ItemEvent e) {
          protocolItemStateChanged();
        }
      });
    repaint();
    updateUI();
  }


  /**
   *  part of observer interface
   *
   * @param  ob   observable
   * @param  arg  what to observe
   */
  public void update(Observable ob, Object arg) {
  }


  /**
   *  triggered when focus lost
   *
   * @param  e  FocusEvent
   */
  public void focusLost(FocusEvent e) {
  }


  /**
   *  Description of the Method
   */
  void performativeItemStateChanged() {
    if (this.getSelectedItem() == null) {
      return;
    }
    String value = (String)this.getSelectedItem();
    this.setForeground(ACLPerformativesRenderer.determineColor(value));

    if (this.isEnabled()) {
      int i = this.getSelectedIndex();
      msg.setPerformative(i);
    }

  }


  /**
   *  Description of the Method
   */
  void protocolItemStateChanged() {
    if (this.getSelectedItem() == null) {
      return;
    }
    if (this.isEnabled()) {
      String value = (String)this.getSelectedItem();
      msg.setProtocol(value);
    }
  }


  private static String fipaProtocols[] = {
    "",
    FIPANames.InteractionProtocol.FIPA_REQUEST,
    FIPANames.InteractionProtocol.FIPA_QUERY,
    FIPANames.InteractionProtocol.FIPA_REQUEST_WHEN,
    FIPANames.InteractionProtocol.FIPA_ENGLISH_AUCTION,
    FIPANames.InteractionProtocol.FIPA_DUTCH_AUCTION,
    FIPANames.InteractionProtocol.FIPA_BROKERING,
    FIPANames.InteractionProtocol.FIPA_RECRUITING,
    FIPANames.InteractionProtocol.FIPA_PROPOSE,
    FIPANames.InteractionProtocol.FIPA_CONTRACT_NET,
    FIPANames.InteractionProtocol.FIPA_ITERATED_CONTRACT_NET
    };

  private DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();

  private ACLMessage msg;
  private String fieldName;
}
//  ***EOF***
