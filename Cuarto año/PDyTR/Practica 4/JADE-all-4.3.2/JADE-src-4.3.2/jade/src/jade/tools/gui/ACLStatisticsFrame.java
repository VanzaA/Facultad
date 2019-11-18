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
import java.util.Vector;

import javax.swing.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.*;
import javax.swing.tree.*;
import javax.swing.tree.*;
import jade.core.Agent;

import jade.lang.acl.ACLMessage;
import jade.tools.sl.SLFormatter;

import jade.util.leap.*;

/**
 *  Description of the Class
 *
 * @author     Chris van Aart - Acklin B.V., the Netherlands
 * @created    April 26, 2002
 */

public class ACLStatisticsFrame extends JFrame {

  /**
   *  Constructor for the ACLStatisticsFrame object
   *
   * @param  aclTreeModel  Description of Parameter
   */
  public ACLStatisticsFrame(DefaultTreeModel aclTreeModel) {
    this.aclTreeModel = aclTreeModel;
    aclTableModel = new ACLStatiscticsTableModel(aclTreeModel);

    try {
      jbInit();
      setFrameIcon("images/details.gif");
      this.statisticsTable.setModel(aclTableModel);
      this.statisticsTable.updateUI();
      itemBoxModel.addElement("performative");
      itemBoxModel.addElement("send-to");
      itemBoxModel.addElement("received-from");
      itemBoxModel.addElement("ontology");
      itemBoxModel.addElement("traffic");
      itemComboBox.setModel(this.itemBoxModel);
      itemComboBox.setRenderer(new SomeLabelRenderer());

    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }


  /**
   *  Description of the Method
   *
   * @param  aclModel  Description of Parameter
   */
  public static void show(DefaultTreeModel aclModel) {
    ACLStatisticsFrame frame = new ACLStatisticsFrame(aclModel);
    frame.setSize(300, 300);
    frame.setVisible(true);

  }


  /**
   *  Description of the Method
   *
   * @param  e  Description of Parameter
   */
  void closeButton_actionPerformed(ActionEvent e) {
    this.setVisible(false);
  }


  /**
   *  Description of the Method
   *
   * @param  e  Description of Parameter
   */
  void itemComboBox_itemStateChanged(ItemEvent e) {
    Object o = this.itemComboBox.getSelectedItem();
    if (o == null) {
      return;
    }
    String item = (String)o;
    this.aclTableModel.fillThis(item);
    this.statisticsTable.validate();
    this.statisticsTable.updateUI();
  }


  /**
   *  Sets the FrameIcon attribute of the ACLFrame object
   *
   * @param  iconpath  The new FrameIcon value
   */
  private void setFrameIcon(String iconpath) {
    ImageIcon image = new ImageIcon(this.getClass().getResource(iconpath));
    setIconImage(image.getImage());
  }


  /**
   *  Description of the Method
   *
   * @exception  Exception  Description of Exception
   */
  private void jbInit() throws Exception {
    jLabel1.setFont(new java.awt.Font("Dialog", 0, 11));
    jLabel1.setText("statistics:");
    this.setForeground(Color.white);
    this.getContentPane().setBackground(Color.white);
    this.getContentPane().setLayout(gridBagLayout1);
    closeButton.setBackground(Color.white);
    closeButton.setFont(new java.awt.Font("Dialog", 0, 11));
    closeButton.setText("close");
    closeButton.addActionListener(new ACLStatisticsFrame_closeButton_actionAdapter(this));
    statisticsTable.setFont(new java.awt.Font("Dialog", 0, 11));
    tableScrollPane.getViewport().setBackground(Color.white);
    itemComboBox.addItemListener(new ACLStatisticsFrame_itemComboBox_itemAdapter(this));
    itemComboBox.setBackground(Color.white);
    itemComboBox.setFont(new java.awt.Font("Dialog", 0, 11));
    this.getContentPane().add(jLabel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
      , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.getContentPane().add(tableScrollPane, new GridBagConstraints(0, 1, 2, 1, 1.0, 1.0
      , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    tableScrollPane.getViewport().add(statisticsTable, null);
    this.getContentPane().add(closeButton, new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0
      , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.getContentPane().add(itemComboBox, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
      , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
  }


  private class ACLStatiscticsTableModel extends DefaultTableModel {

    /**
     *  Constructor for the StatisticsTableModel object
     *
     * @param  aclModel  Description of Parameter
     */
    public ACLStatiscticsTableModel(DefaultTreeModel aclModel) {
      this.aclModel = aclModel;
      fillThis("performative");
    }


    /**
     *  Gets the ColumnCount attribute of the StatisticsTableModel object
     *
     * @return    The ColumnCount value
     */
    public int getColumnCount() {
      return 2;
    }


    /**
     *  Gets the ColumnName attribute of the StatisticsTableModel object
     *
     * @param  column  Description of Parameter
     * @return         The ColumnName value
     */
    public String getColumnName(int column) {
      if (column == 0) {
        return this.theItem;
      }
      if (column == 1) {
        return "number";
      }
      return "?";
    }


    /**
     *  Gets the ValueAt attribute of the StatisticsTableModel object
     *
     * @param  row     Description of Parameter
     * @param  column  Description of Parameter
     * @return         The ValueAt value
     */
    public Object getValueAt(int row, int column) {
      Vector v = (Vector)getDataVector().elementAt(row);
      String result = "<?>";
      if (column == 0) {
        result = (String)v.get(0);
      }

      if (column == 1) {
        result = (String)v.get(1);
      }

      return result;
    }


    /**
     *  Description of the Method
     *
     * @param  item  Description of Parameter
     */
    public void fillThis(String item) {
      this.getDataVector().clear();
      DefaultMutableTreeNode aclRoot = (DefaultMutableTreeNode)aclModel.getRoot();
      this.theItem = item;
      int i = 0;
      countTable.clear();
      while (i < aclRoot.getChildCount()) {
        ACLMessageNode mn = (ACLMessageNode)aclRoot.getChildAt(i);
        String toCount = "<unknown>";
        if (theItem.equals("performative")) {
          toCount = mn.getDirection() + ":" + mn.getPerformative();
        }

        if (theItem.equals("send-to")) {
          toCount = mn.getDirection() + ":" + mn.getSendTo();
        }

        if (theItem.equals("received-from")) {
          toCount = mn.getDirection() + ":" + mn.receivedFrom();
        }

        if (theItem.equals("ontology")) {
          toCount = mn.getDirection() + ":" + mn.getOntology();
        }

        if (theItem.equals("traffic")) {
          toCount = mn.getDirection();
        }

        Object o = countTable.get(toCount);
        if (o == null) {
          countTable.put(toCount, "1");
        }

        else {
          int value = Integer.parseInt((String)o);
          value++;
          countTable.remove(toCount);
          countTable.put(toCount, "" + value);
        }
        i++;
      }
      Iterator itor = countTable.keySet().iterator();
      while (itor.hasNext()) {
        Vector v = new Vector();
        String key = (String)itor.next();
        String value = (String)countTable.get(key);

        v.add(key);
        v.add(value);
        this.getDataVector().add(v);
      }

    }


    HashMap countTable = new HashMap();

    String theItem;
    DefaultTreeModel aclModel;

  }


  private class ACLStatisticsFrame_closeButton_actionAdapter implements java.awt.event.ActionListener {

    /**
     *  Constructor for the ACLStatisticsFrame_closeButton_actionAdapter
     *  object
     *
     * @param  adaptee  Description of Parameter
     */
    ACLStatisticsFrame_closeButton_actionAdapter(ACLStatisticsFrame adaptee) {
      this.adaptee = adaptee;
    }


    /**
     *  Description of the Method
     *
     * @param  e  Description of Parameter
     */
    public void actionPerformed(ActionEvent e) {
      adaptee.closeButton_actionPerformed(e);
    }


    ACLStatisticsFrame adaptee;
  }


  private class SomeLabelRenderer extends JLabel implements ListCellRenderer {
    /**
     *  Constructor for the SomeLabelRenderer object
     */
    public SomeLabelRenderer() {
      setOpaque(true);
      setFont(new java.awt.Font("Dialog", 0, 10));
    }


    /**
     *  Gets the ListCellRendererComponent attribute of the SomeLabelRenderer
     *  object
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
        //     setIcon(new ImageIcon(getClass().getResource("images/start.gif")));
        setText((String)value);
      }
      setBackground(isSelected ? Color.blue : Color.white);
      setForeground(isSelected ? Color.white : Color.black);
      return this;
    }
  }


  GridBagLayout gridBagLayout1 = new GridBagLayout();
  JLabel jLabel1 = new JLabel();
  JScrollPane tableScrollPane = new JScrollPane();
  JButton closeButton = new JButton();
  JComboBox itemComboBox = new JComboBox();
  JTable statisticsTable = new JTable();
  DefaultComboBoxModel itemBoxModel = new DefaultComboBoxModel();
  DefaultTreeModel aclTreeModel;
  ACLStatiscticsTableModel aclTableModel;

}

class ACLStatisticsFrame_itemComboBox_itemAdapter implements java.awt.event.ItemListener {


  /**
   *  Constructor for the ACLStatisticsFrame_itemComboBox_itemAdapter object
   *
   * @param  adaptee  Description of Parameter
   */
  ACLStatisticsFrame_itemComboBox_itemAdapter(ACLStatisticsFrame adaptee) {
    this.adaptee = adaptee;
  }


  /**
   *  Description of the Method
   *
   * @param  e  Description of Parameter
   */
  public void itemStateChanged(ItemEvent e) {
    adaptee.itemComboBox_itemStateChanged(e);
  }


  /**
   *  This class contains a table model for the ACLStaticsFrame
   *
   * @author     Chris van Aart - Acklin B.V., the Netherlands
   * @created    April 26, 2002
   */

  ACLStatisticsFrame adaptee;

//  ***EOF***
}
//  ***EOF***
