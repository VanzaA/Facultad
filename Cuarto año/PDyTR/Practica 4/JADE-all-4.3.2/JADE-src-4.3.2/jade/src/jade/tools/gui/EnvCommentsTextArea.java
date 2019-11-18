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

import java.awt.event.*;
import java.lang.*;
import java.lang.reflect.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import jade.domain.FIPAAgentManagement.Envelope;

import jade.lang.acl.ACLMessage;
import jade.util.Logger;

/**
 *  This class displays a singe string from an ACLMessage
 *
 * @author     Chris van Aart - Acklin B.V., the Netherlands
 * @created    April 26, 2002
 */

public class EnvCommentsTextArea extends JTextArea implements Observer {

  /**
   *  register an ACLMessage with the accompagnied membername
   *
   * @param  arg        the ACLMessage
   * @param  fieldName  membername of the ACLMessage
   */
  public void register(Object arg, String fieldName) {
    this.setFont(new java.awt.Font("Dialog", 0, 11));
    this.theObj = arg;
    this.fieldName = fieldName;
    String methodName = "get" + fieldName;
    String theType = "java.lang.String";
    try {
      Method sn = theObj.getClass().getMethod(methodName, (Class[]) null);
      Object res = sn.invoke(theObj, new Object[]{});
      setText(res != null ? res.toString() : "");
    }
    catch (Exception ex) {
      Logger.getMyLogger(this.getClass().getName()).log(Logger.WARNING,"failed class: " + theObj.getClass() + " for " + methodName);
      ex.printStackTrace();
    }

  }


  /**
   *  unregister ACLMessage
   *
   * @param  arg  the ACLMessage
   * @param  str  Description of Parameter
   */
  public void unregister(Object arg, String str) {

  }


  /**
   *  update textField
   *
   * @param  ob   the object
   * @param  arg  the argument
   */
  public void update(Observable ob, Object arg) {
    String methodName = "get" + fieldName;
    String theType = "java.lang.String";
    try {
      Method sn = theObj.getClass().getMethod(methodName, (Class[]) null);
      Object res = sn.invoke(theObj, new Object[]{});
      setText(res != null ? res.toString() : "");
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }

  }


  /**
   *  triggered when focus lossed
   *
   * @param  e  the FocusEvent
   */
  public void focusLost(FocusEvent e) {
    String value = getText();
    String methodName = "set" + fieldName;
    String theType = "java.lang.String";
    try {
      Method sn = theObj.getClass().getMethod(methodName, new Class[]{Class.forName(theType)});
      Object os = value;
      sn.invoke(theObj, new Object[]{os});
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }


  /**
   *  processFocusEvent listener
   *
   * @param  e  the FocusEvent
   */
  protected void processFocusEvent(FocusEvent e) {
    super.processFocusEvent(e);
    if (e.getID() == e.FOCUS_LOST) {
      focusLost(e);
    }

  }


  private Object theObj;
  private String fieldName;
}
//  ***EOF***
