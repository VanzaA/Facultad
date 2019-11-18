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

import javax.swing.DefaultListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import jade.core.AID;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.ACLMessage;
import jade.util.leap.*;

/**
 *  This class shows a list of AID addresses
 *
 * @author     Chris van Aart - Acklin B.V., the Netherlands
 * @created    April 26, 2002
 */

public class AIDAddressList extends JPanel {

  /**
   *  Constructor for the AIDAddressesList object
   */
  public AIDAddressList() {
    try {
      jbInit();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }


  /**
   *  Sets the Editable attribute of the AIDAddressesList object
   *
   * @param  theBool  The new Editable value
   */
  public void setEditable(boolean theBool) {
    if (!theBool) {
      editable = false;
      this.addButton.setEnabled(false);
      this.deleteButton.setEnabled(false);
    }
  }


  /**
   *  Description of the Method
   *
   * @param  theAID  Description of Parameter
   */
  public void register(AID theAID) {
    listModel.removeAllElements();
    this.theAID = theAID;
    Iterator itor = theAID.getAllAddresses();
    while (itor.hasNext()) {
      String theAddresss = (String)itor.next();
      listModel.addElement(theAddresss);
    }

    theDataListener = new AIDAddressListListener();
    theDataListener.register(theAID, "Address");
    listModel.addListDataListener(theDataListener);
    contentList.setModel(listModel);
  }


  /**
   *  Description of the Method
   */
  public void doDelete() {
    int index = contentList.getSelectedIndex();
    if (index >= 0) {
      theDataListener.registerRemovedAddress((String)listModel.getElementAt(index));
      this.listModel.remove(index);
    }
  }


  /**
   *  Description of the Method
   */
  public void doAdd() {
    AIDAddressDialog theDialog = new AIDAddressDialog();
    theDialog.setTitle("<new address>");
    theDialog.setLocation((int)getLocationOnScreen().getX(), (int)getLocationOnScreen().getY());
    theDialog.setVisible(true);
    if (theDialog.getOK()) {
      listModel.addElement(theDialog.getItsAddress());
    }

  }


  /**
   *  Description of the Method
   */
  public void doView() {

    int index = this.contentList.getSelectedIndex();
    if (index < 0) {
      return;
    }
    String currentAddress = (String)listModel.getElementAt(index);
    String editAddress = currentAddress;
    AIDAddressDialog theDialog = new AIDAddressDialog();
    theDialog.setLocation((int)getLocationOnScreen().getX(), (int)getLocationOnScreen().getY());
    theDialog.setEditable(editable);
    theDialog.setTitle(editable ? "Edit address: " + currentAddress : "View address:" + currentAddress);
    theDialog.setItsAddress(editAddress);
    theDialog.setVisible(true);
    if (theDialog.getOK()) {
      theDataListener.registerChangedAddress(currentAddress);
      listModel.setElementAt(theDialog.getItsAddress(), index);
    }
  }


  /**
   *  Description of the Method
   *
   * @param  e  Description of Parameter
   */
  void deleteButton_actionPerformed(ActionEvent e) {
    doDelete();
  }


  /**
   *  Adds a feature to the Button_actionPerformed attribute of the
   *  AIDAddressesList object
   *
   * @param  e  The feature to be added to the Button_actionPerformed
   *      attribute
   */
  void addButton_actionPerformed(ActionEvent e) {
    doAdd();
  }


  /**
   *  Description of the Method
   *
   * @param  e  Description of Parameter
   */
  void viewButton_actionPerformed(ActionEvent e) {
    doView();
  }


  /**
   *  Description of the Method
   *
   * @param  e  Description of Parameter
   */
  void contentList_keyPressed(KeyEvent e) {
    if (e.getKeyCode() == e.VK_ENTER) {
      doView();
    }

    if (!editable) {
      return;
    }
    if (e.getKeyCode() == e.VK_INSERT) {
      doAdd();
    }

    if (e.getKeyCode() == e.VK_DELETE) {
      doDelete();
    }

  }


  void contentList_mouseClicked(MouseEvent e) {
    if (e.getClickCount() > 1) {
      doView();
    }

  }


  /**
   *  Description of the Method
   *
   * @exception  Exception  Description of Exception
   */
  private void jbInit() throws Exception {
    this.setLayout(gridBagLayout1);
    viewButton.setBackground(Color.white);
    viewButton.setFont(new java.awt.Font("Dialog", 0, 11));
    viewButton.setForeground(new Color(0, 0, 83));
    viewButton.setMinimumSize(new Dimension(13, 5));
    viewButton.setPreferredSize(new Dimension(13, 25));
    viewButton.setToolTipText("edit/view address");
    viewButton.setMargin(new Insets(0, 0, 0, 0));
    viewButton.setText("v");
    viewButton.addActionListener(
      new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          viewButton_actionPerformed(e);
        }
      });
    addButton.setBackground(Color.white);
    addButton.setFont(new java.awt.Font("Dialog", 0, 11));
    addButton.setForeground(new Color(0, 0, 83));
    addButton.setMinimumSize(new Dimension(13, 5));
    addButton.setToolTipText("add address");
    addButton.setMargin(new Insets(0, 0, 0, 0));
    addButton.setText("+");
    addButton.addActionListener(
      new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          addButton_actionPerformed(e);
        }
      });
    deleteButton.setBackground(Color.white);
    deleteButton.setFont(new java.awt.Font("Dialog", 0, 11));
    deleteButton.setForeground(new Color(0, 0, 83));
    deleteButton.setMinimumSize(new Dimension(13, 5));
    deleteButton.setToolTipText("delete address");
    deleteButton.setMargin(new Insets(0, 0, 0, 0));
    deleteButton.setText("x");
    deleteButton.addActionListener(
      new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          deleteButton_actionPerformed(e);
        }
      });
    contentList.setFont(new java.awt.Font("Dialog", 0, 11));
    contentList.addMouseListener(
      new java.awt.event.MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          contentList_mouseClicked(e);
        }
      });
    contentList.addKeyListener(
      new java.awt.event.KeyAdapter() {
        public void keyPressed(KeyEvent e) {
          contentList_keyPressed(e);
        }
      });
    contentScrollPane.setBorder(BorderFactory.createLineBorder(Color.black));
    contentScrollPane.getViewport().add(contentList, null);
    this.add(contentScrollPane, new GridBagConstraints(0, 1, 1, 3, 1.0, 1.0
      , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    this.add(addButton, new GridBagConstraints(1, 2, 1, 1, 0.0, 1.0
      , GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));
    this.add(deleteButton, new GridBagConstraints(1, 3, 1, 1, 0.0, 1.0
      , GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));
    this.add(viewButton, new GridBagConstraints(1, 1, 1, 1, 0.0, 1.0
      , GridBagConstraints.SOUTHEAST, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));
  }


  /**
   *  This class listenes to the AIDAddressList
   *
   * @author     Chris van Aart - Acklin B.V., the Netherlands
   * @created    April 26, 2002
   */

  private class AIDAddressListListener implements ListDataListener {

    /**
     *  Constructor for the AddressListListener object
     */
    public AIDAddressListListener() { }


    /**
     *  Description of the Method
     *
     * @param  obj  Description of Parameter
     * @param  arg  Description of Parameter
     */
    public void register(Object obj, String arg) {
      itsAID = (AID)obj;
    }


    /**
     *  Description of the Method
     *
     * @param  parm1  Description of Parameter
     */
    public void intervalAdded(ListDataEvent parm1) {
      DefaultListModel lm = (DefaultListModel)parm1.getSource();
      int index = parm1.getIndex0();
      String newAddress = (String)lm.elementAt(index);
      itsAID.addAddresses(newAddress);
    }


    /**
     *  Description of the Method
     *
     * @param  theRemovedAddress  Description of Parameter
     */
    public void registerRemovedAddress(String theRemovedAddress) {
      this.theRemovedAddress = theRemovedAddress;
    }


    /**
     *  Description of the Method
     *
     * @param  parm1  Description of Parameter
     */
    public void intervalRemoved(ListDataEvent parm1) {
      itsAID.removeAddresses(theRemovedAddress);
    }


    /**
     *  Description of the Method
     *
     * @param  theChangedAddress  Description of Parameter
     */
    public void registerChangedAddress(String theChangedAddress) {
      this.theChangedAddress = theChangedAddress;
    }


    /**
     *  Description of the Method
     *
     * @param  parm1  Description of Parameter
     */
    public void contentsChanged(ListDataEvent parm1) {
      DefaultListModel lm = (DefaultListModel)parm1.getSource();
      int index = parm1.getIndex0();
      String currentAddress = (String)lm.getElementAt(index);
      itsAID.removeAddresses(currentAddress);
      itsAID.addAddresses(theChangedAddress);
    }


    private String theRemovedAddress, theChangedAddress;
    private AID itsAID;
  }


  /**
   *  This class show a dialog where the address of an AID can be viewed and
   *  edited.
   *
   * @author     Chris van Aart - Acklin B.V., the Netherlands
   * @created    April 26, 2002
   */

  private class AIDAddressDialog extends JDialog {

    /**
     *  Constructor for the ACLAddressDialog object
     */
    public AIDAddressDialog() {
      this.setModal(true);
      try {
        jbInit();
        this.setSize(380, 100);
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }
    }


    /**
     *  Gets the ItsAddress attribute of the ACLAddressDialog object
     *
     * @return    The ItsAddress value
     */
    public String getItsAddress() {
      return itsAddress;
    }


    /**
     *  Gets the OK attribute of the ACLAddressDialog object
     *
     * @return    The OK value
     */
    public boolean getOK() {
      return userAction.equals(OK);
    }


    /**
     *  Sets the Editable attribute of the ACLAddressDialog object
     *
     * @param  theBool  The new Editable value
     */
    public void setEditable(boolean theBool) {
      if (!theBool) {
        OK = "CLOSED";
        this.cancelButton.setVisible(false);
        this.theAddressField.setEnabled(false);
      }
    }


    /**
     *  Sets the ItsAddress attribute of the ACLAddressDialog object
     *
     * @param  newItsAddress  The new ItsAddress value
     */
    public void setItsAddress(String newItsAddress) {
      itsAddress = newItsAddress;
      this.theAddressField.setText(itsAddress);
    }


    /**
     *  Description of the Method
     *
     * @exception  Exception  Description of Exception
     */
    void jbInit() throws Exception {
      this.getContentPane().setLayout(gridBagLayout1);
      jLabel1.setFont(new java.awt.Font("Dialog", 0, 11));
      jLabel1.setText("address");
      this.getContentPane().setBackground(Color.white);
      theAddressField.setFont(new java.awt.Font("Dialog", 0, 11));
      theAddressField.setDisabledTextColor(Color.black);
      buttonPanel.setBackground(Color.white);
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
      this.getContentPane().add(theAddressField, new GridBagConstraints(1, 0, 2, 1, 1.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
      this.getContentPane().add(jLabel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
      this.getContentPane().add(buttonPanel, new GridBagConstraints(0, 2, 3, 1, 1.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
      buttonPanel.add(okButton, null);
      buttonPanel.add(cancelButton, null);
    }


    /**
     *  Description of the Method
     *
     * @param  e  Description of Parameter
     */
    void cancelButton_actionPerformed(ActionEvent e) {
      setUserAction(CANCELLED);
      setVisible(false);
    }


    /**
     *  Description of the Method
     *
     * @param  e  Description of Parameter
     */
    void okButton_actionPerformed(ActionEvent e) {
      setItsAddress(theAddressField.getText());
      setUserAction(OK);
      setVisible(false);
    }


    /**
     *  Gets the UserAction attribute of the ACLAddressDialog object
     *
     * @return    The UserAction value
     */
    private String getUserAction() {
      return userAction;
    }


    /**
     *  Sets the UserAction attribute of the ACLAddressDialog object
     *
     * @param  newUserAction  The new UserAction value
     */
    private void setUserAction(String newUserAction) {
      userAction = newUserAction;
    }


    GridBagLayout gridBagLayout1 = new GridBagLayout();
    JTextField theAddressField = new JTextField();
    JLabel jLabel1 = new JLabel();
    JPanel buttonPanel = new JPanel();
    JButton okButton = new JButton();
    JButton cancelButton = new JButton();

    private String OK = "ok";
    private String CANCELLED = "cancelled";
    private String userAction;
    private String itsAddress;

  }


  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JList contentList = new JList();
  private JButton viewButton = new JButton();
  private JButton addButton = new JButton();
  private JButton deleteButton = new JButton();

  private DefaultListModel listModel = new DefaultListModel();
  private JScrollPane contentScrollPane = new JScrollPane();

  private boolean editable = true;
  private AIDAddressListListener theDataListener;
  private AID theAID;

}
//  ***EOF***
