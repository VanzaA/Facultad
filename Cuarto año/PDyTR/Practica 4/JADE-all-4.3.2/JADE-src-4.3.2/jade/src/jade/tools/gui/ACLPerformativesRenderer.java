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
import javax.swing.*;

/**
 *  Render the colors of performatives of the performatices combobox
 *
 * @author     Chris van Aart - Acklin B.V., the Netherlands
 * @created    April 26, 2002
 */

public class ACLPerformativesRenderer extends JLabel implements ListCellRenderer {

  /**
   *  Constructor for the ACLPerformativesRenderer object
   */
  public ACLPerformativesRenderer() {
    setOpaque(true);
    setFont(new java.awt.Font("Dialog", 0, 10));
  }


  /**
   *  Determine color of a peformative
   *
   * @param  performative  the performative
   * @return               the choosen Color
   */
  public static Color determineColor(String performative) {
    if (performative.equalsIgnoreCase("refuse") ||
      performative.equalsIgnoreCase("disagree") ||
      performative.equalsIgnoreCase("failure") ||
      performative.startsWith("NOT")) {
      return Color.red;
    }
    if (performative.equalsIgnoreCase("agree") ||
      performative.equalsIgnoreCase("cancel") ||
      performative.startsWith("ACCEPT")) {
      return Color.green;
    }
    if (performative.startsWith("INFORM")) {
      return Color.orange;
    }
    if (performative.startsWith("REQUEST") ||
      performative.startsWith("QUERY") ||
      performative.equalsIgnoreCase("cfp") ||
      performative.equalsIgnoreCase("subscribe")
      ) {
      return Color.blue;
    }
    return Color.black;
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
      setForeground(isSelected ? Color.white : determineColor(sValue));
    }
    return this;
  }
}
//  ***EOF***
