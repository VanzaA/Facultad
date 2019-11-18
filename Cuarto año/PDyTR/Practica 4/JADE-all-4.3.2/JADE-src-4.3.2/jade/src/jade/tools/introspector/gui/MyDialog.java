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


package jade.tools.introspector.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

/**
   Simple dialog, used to input a String value.

   @author Andrea Squeri,Corti Denis,Ballestracci Paolo -  Universita` di Parma
*/
public class MyDialog extends JDialog implements ActionListener,WindowListener {
  String text=null;
  JPanel panel1 = new JPanel();
  JPanel panel2 = new JPanel();
  JButton button1 = new JButton();
  JButton button2 = new JButton();
  Border border1;
  JPanel jPanel1 = new JPanel();
  GridBagLayout gridBagLayout1 = new GridBagLayout();
  GridLayout gridLayout1 = new GridLayout();
  JTextField jTextField1 = new JTextField();
  BorderLayout borderLayout1 = new BorderLayout();

  public MyDialog(Frame frame, String title, boolean modal) {
    super(frame, title, modal);
    try {
      jbInit();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    pack();
  }
  public MyDialog(Frame frame, String title) {
    this(frame, title, false);
  }
  public MyDialog(Frame frame) {
    this(frame, "", false);
  }
  private void jbInit() throws Exception {
    border1 = BorderFactory.createRaisedBevelBorder();
    jPanel1.setLayout(gridLayout1);
    panel2.setBorder(border1);
    panel2.setLayout(borderLayout1);
    button1.setText("OK");
    button1.addActionListener(this);
    button2.setText("Cancel");
    gridLayout1.setHgap(4);
    button2.addActionListener(this);
    this.addWindowListener(this);
    panel1.setLayout(gridBagLayout1);
    panel1.add(panel2, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    panel2.add(jTextField1, BorderLayout.NORTH);
    panel1.add(jPanel1, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(4, 8, 4, 8), 0, 0));
    jPanel1.add(button1, null);
    jPanel1.add(button2, null);
    getContentPane().add(panel1);
  }

  public void actionPerformed(ActionEvent e){
    JButton b=(JButton) e.getSource();
    if (b==button1) this.ok_actionPerformed(e);
    else this.cancel_actionPerformed(e);
  }

  public void windowClosing(WindowEvent e) {
    this.close_window(e);
  }

  // OK
  void ok_actionPerformed(ActionEvent e) {
    text=jTextField1.getText();
    dispose();
  }

  // Cancel
  void cancel_actionPerformed(ActionEvent e) {
    text=null;
    dispose();
  }

  //close
  void close_window(WindowEvent e) {
    text=null;
    dispose();
  }

  //interface WindowListener
  public void windowClosed(WindowEvent e){}
  public void windowOpened(WindowEvent e){}
  public void windowIconified(WindowEvent e){}
  public void windowDeiconified(WindowEvent e){}
  public void windowDeactivated(WindowEvent e){}
  public void windowActivated(WindowEvent e){}

  public void showCorrect(){

    this.setLocation(250,180);
    this.setVisible(true);

  }
}

