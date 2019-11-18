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
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.text.*;
import javax.swing.tree.*;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.tools.sl.SLFormatter;

/**
 *  This class is used in the model of the ACLTree. The MessageNode contains
 *  an ACLMessage, a direction and a date/timestamp
 *
 * @author     Chris van Aart - Acklin B.V., the Netherlands
 * @created    April 26, 2002
 */

public class ACLMessageNode extends DefaultMutableTreeNode {

  /**
   *  Constructor for the MessageNode object
   *
   * @param  str  Description of Parameter
   */
  ACLMessageNode(String str) {
    super(str);
  }


  /**
   *  Gets the Message attribute of the MessageNode object
   *
   * @return    The Message value
   */
  public ACLMessage getMessage() {
    return theMessage;
  }


  /**
   *  Gets the Performative attribute of the MessageNode object
   *
   * @return    The Performative value
   */
  public String getPerformative() {
    return theMessage.getPerformative(theMessage.getPerformative());
  }


  /**
   *  Gets the SendTo attribute of the MessageNode object
   *
   * @return    The SendTo value
   */
  public String getSendTo() {
    if (theMessage.getAllReceiver().hasNext()) {
      AID sender = (AID)theMessage.getAllReceiver().next();
      return sender.getName();
    }
    return "<unknown>";
  }


  /**
   *  Gets the Ontology attribute of the MessageNode object
   *
   * @return    The Ontology value
   */
  public String getOntology() {
    String ontology = theMessage.getOntology();
    if (ontology != null) {
      return ontology;
    }
    return "<unknown>";
  }


  /**
   *  Gets the Direction attribute of the MessageNode object
   *
   * @return    The Direction value
   */
  public String getDirection() {
    return direction;
  }


  public String getTime() {
    return time;
  }


  public Date getTheDate() {
    return theDate;
  }


  /**
   *  Sets the Message attribute of the MessageNode object
   *
   * @param  msg  The new Message value
   */
  public void setMessage(ACLMessage msg) {
    theMessage = (ACLMessage)msg.clone();
  }


  /**
   *  Sets the Direction attribute of the MessageNode object
   *
   * @param  theDirection  The new Direction value
   */
  public void setDirection(String theDirection) {
    direction = theDirection;
  }


  public void setTime(String theTime) {
    time = theTime;
    try {
      this.theDate = dateFormat.parse(time);
    }
    catch (Exception ex) {
      jade.util.Logger.getMyLogger(this.getClass().getName()).log(jade.util.Logger.WARNING,ex.getMessage());
    }
  }


  public void setTheDate(Date theTheDate) {
    theDate = theTheDate;
  }


  public String receivedFrom() {
    if (theMessage.getSender() != null) {
      AID sender = theMessage.getSender();
      return sender.getName();
    }
    return "<unknown>";
  }


  private static DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
  private Date theDate = new Date();
  private ACLMessage theMessage;
  private String direction;
  private String time;
}
//  ***EOF***
