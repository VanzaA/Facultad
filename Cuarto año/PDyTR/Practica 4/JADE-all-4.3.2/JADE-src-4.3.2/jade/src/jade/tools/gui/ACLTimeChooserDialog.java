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
import java.text.*;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.swing.*;
import javax.swing.JTextField;
import javax.swing.border.*;

/**
 *  The TimeChooser class can be used to let the user define a certain point
 *  in time by means of a dialog window. Used for for filling the reply-by
 *  attribute of an ACL.
 *
 * @author     Giovanni Rimassa - Universita` di Parma, Chris van Aart -
 *      Acklin
 * @created    May 19, 2002
 * @version    $Date: 2010-04-15 11:26:09 +0200 (gio, 15 apr 2010) $ $Revision: 6311 $
 */

public class ACLTimeChooserDialog implements ActionListener {

  /**
   *  Constructor for the TimeChooserDialog object
   */
  public ACLTimeChooserDialog() {
    retVal = CANCEL;
    date = null;
  }


  /**
   *  Constructor for the TimeChooserDialog object
   *
   * @param  d  Description of Parameter
   */
  public ACLTimeChooserDialog(Date d) {
    retVal = CANCEL;
    date = d;
  }


  /**
   * @param  args  The command line arguments
   */
  public static void main(String[] args) {
    ACLTimeChooserDialog tc = new ACLTimeChooserDialog();
    tc.setDate(new Date());
    tc.showViewTimeDlg(null);
  }


  /**
   *  Gets the Date attribute of the TimeChooserDialog object
   *
   * @return    The Date value
   */
  public Date getDate() {
    return (date);
  }


  /**
   *  Sets the Date attribute of the TimeChooserDialog object
   *
   * @param  d  The new Date value
   */
  public void setDate(Date d) {
    date = d;
  }


  /**
   *  Description of the Method
   *
   * @param  parent  Description of Parameter
   * @return         Description of the Returned Value
   */
  public int showEditTimeDlg(JFrame parent) {
    Calendar cal;

    dlg = new JDialog(parent, "Edit time");
    dlg.setBackground(Color.white);

    /*
        THIS PART IS RELATED TO THE EDITING OF RELATIVE TIMES (NOT YET IMPLEMENTED)
        JPanel modePanel = new JPanel();
        absButton = new JToggleButton("Absolute");
        relButton = new JToggleButton("Relative");
        relButton.setPreferredSize(absButton.getPreferredSize());
        absButton.setSelected(true);
        relButton.setSelected(false);
        mode = ABSOLUTE;
        absButton.addActionListener(this);
        modePanel.add(absButton);
        relButton.addActionListener(this);
        modePanel.add(relButton);
        dlg.getContentPane().add(modePanel);
        dlg.getContentPane().add(Box.createVerticalStrut(5));
      */
    // Controls to set the time
    cal = new GregorianCalendar();
    if (date != null) {
      cal.setTime(date);
    }

    JPanel timePanel = new JPanel();
    timePanel.setLayout(new GridLayout(2, 3));
    ((GridLayout)(timePanel.getLayout())).setHgap(5);
    timePanel.setBackground(Color.white);
    year = new JTextField(4);
    year.setEditable(false);
    addTimeUnitLine(timePanel, cal.get(Calendar.YEAR), year, "Year:", Integer.MAX_VALUE);
    month = new JTextField(4);
    month.setEditable(false);
    addTimeUnitLine(timePanel, cal.get(Calendar.MONTH) + 1, month, "Month:", 12);
    day = new JTextField(4);
    day.setEditable(false);
    addTimeUnitLine(timePanel, cal.get(Calendar.DATE), day, "Day:", 31);
    hour = new JTextField(4);
    hour.setEditable(false);
    addTimeUnitLine(timePanel, cal.get(Calendar.HOUR_OF_DAY), hour, "Hour:", 23);
    min = new JTextField(4);
    min.setEditable(false);
    addTimeUnitLine(timePanel, cal.get(Calendar.MINUTE), min, "Min:", 59);
    sec = new JTextField(4);
    sec.setEditable(false);
    addTimeUnitLine(timePanel, cal.get(Calendar.SECOND), sec, "Sec:", 59);

    timePanel.setBorder(new TitledBorder("Time"));
    dlg.getContentPane().add(timePanel, BorderLayout.CENTER);

    // Buttons to set/reset the edited time
    JPanel buttonPanel = new JPanel();
    buttonPanel.setBackground(Color.white);
    JButton setButton = new JButton("Set");
    setButton.setBackground(Color.white);
    setButton.setFont(new java.awt.Font("Dialog", 0, 11));
    JButton resetButton = new JButton("Reset");
    resetButton.setBackground(Color.white);
    resetButton.setFont(new java.awt.Font("Dialog", 0, 11));
    JButton cancelButton = new JButton("Cancel");
    cancelButton.setBackground(Color.white);
    cancelButton.setFont(new java.awt.Font("Dialog", 0, 11));
    setButton.setPreferredSize(cancelButton.getPreferredSize());
    resetButton.setPreferredSize(cancelButton.getPreferredSize());
    setButton.addActionListener(this);
    resetButton.addActionListener(this);
    cancelButton.addActionListener(this);
    buttonPanel.add(setButton);
    buttonPanel.add(resetButton);
    buttonPanel.add(cancelButton);

    dlg.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

    // Display the dialog window
    dlg.setModal(true);
    dlg.pack();
    dlg.setResizable(false);
    if (parent != null) {// Locate the dialog relatively to the parent

      dlg.setLocation(parent.getX() + (parent.getWidth() - dlg.getWidth()) / 2, parent.getY() + (parent.getHeight() - dlg.getHeight()) / 2);
    }

    else {// Locate the dialog relatively to the screen

      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      int centerX = (int)screenSize.getWidth() / 2;
      int centerY = (int)screenSize.getHeight() / 2;
      dlg.setLocation(centerX - dlg.getWidth() / 2, centerY - dlg.getHeight() / 2);
    }
    dlg.setVisible(true);

    return (retVal);
  }


  /**
   *  Description of the Method
   *
   * @param  parent  Description of Parameter
   */
  public void showViewTimeDlg(JFrame parent) {
    String s;
    JPanel p;

    dlg = new JDialog(parent, "View Time");
    dlg.getContentPane().setLayout(new BoxLayout(dlg.getContentPane(), BoxLayout.Y_AXIS));

    // Time indication label
    p = new JPanel();
    p.setBackground(Color.white);
    if (date == null) {
      s = "No time indication to display";
    }

    else {
      DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
      s = df.format(date);
    }
    JLabel l = new JLabel(s);
    l.setBackground(Color.white);
    l.setFont(new java.awt.Font("Dialog", 0, 11));
    p.add(l);
    dlg.getContentPane().add(p);

//    dlg.getContentPane().add(Box.createVerticalStrut(5));

    // Close button
    p = new JPanel();
    p.setBackground(Color.white);
    JButton b = new JButton("Close");
    b.setBackground(Color.white);
    b.setFont(new java.awt.Font("Dialog", 0, 11));
    b.addActionListener(this);
    p.add(b);
    dlg.getContentPane().add(p);
    dlg.setBackground(Color.white);
    // Display the dialog window
    dlg.setModal(true);
    dlg.pack();
    dlg.setResizable(false);
    if (parent != null) {// Locate the dialog relatively to the parent

      dlg.setLocation(parent.getX() + (parent.getWidth() - dlg.getWidth()) / 2, parent.getY() + (parent.getHeight() - dlg.getHeight()) / 2);
    }

    else {// Locate the dialog relatively to the screen

      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      int centerX = (int)screenSize.getWidth() / 2;
      int centerY = (int)screenSize.getHeight() / 2;
      dlg.setLocation(centerX - dlg.getWidth() / 2, centerY - dlg.getHeight() / 2);
    }
    dlg.setVisible(true);
  }


  /**
   *  Description of the Method
   *
   * @param  e  Description of Parameter
   */
  public void actionPerformed(ActionEvent e) {
    String command = e.getActionCommand();
    if (command.equals("Set")) {
      Integer I;
      I = new Integer(year.getText());
      int YY = I.intValue();
      I = new Integer(month.getText());
      int MM = I.intValue();
      I = new Integer(day.getText());
      int DD = I.intValue();
      I = new Integer(hour.getText());
      int hh = I.intValue();
      I = new Integer(min.getText());
      int mm = I.intValue();
      I = new Integer(sec.getText());
      int ss = I.intValue();

      Calendar cal = new GregorianCalendar(YY, MM - 1, DD, hh, mm, ss);
      date = cal.getTime();

      retVal = OK;
      dlg.dispose();
    }
    else if (command.equals("Reset")) {
      date = null;
      retVal = OK;
      dlg.dispose();
    }
    else if (command.equals("Cancel")) {
      retVal = CANCEL;
      dlg.dispose();
    }
    else if (command.equals("Close")) {
      dlg.dispose();
    }

    else if (command.equals("Absolute")) {
      absButton.setSelected(true);
      relButton.setSelected(false);
      mode = ABSOLUTE;
    }
    else if (command.equals("Relative")) {
      relButton.setSelected(true);
      absButton.setSelected(false);
      mode = RELATIVE;
    }

  }


  /**
   *  Adds a feature to the TimeUnitLine attribute of the TimeChooserDialog
   *  object
   *
   * @param  tp             The feature to be added to the TimeUnitLine
   *      attribute
   * @param  timeUnit       The feature to be added to the TimeUnitLine
   *      attribute
   * @param  timeUnitEdit   The feature to be added to the TimeUnitLine
   *      attribute
   * @param  timeUnitLabel  The feature to be added to the TimeUnitLine
   *      attribute
   * @param  limit          The feature to be added to the TimeUnitLine
   *      attribute
   */
  private void addTimeUnitLine(JPanel tp, int timeUnit, final JTextField timeUnitEdit, final String timeUnitLabel, final int limit) {
    JPanel up = new JPanel();
    ((FlowLayout)(up.getLayout())).setHgap(0);
    up.setBackground(Color.white);

    JLabel l = new JLabel(timeUnitLabel);
    l.setBackground(Color.white);
    l.setFont(new java.awt.Font("Dialog", 0, 11));

    timeUnitEdit.setText(String.valueOf(timeUnit));

    JButton B1 = new JButton("+");
    B1.setBackground(Color.white);
    B1.setFont(new java.awt.Font("Dialog", 0, 11));
    B1.addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          Integer i = new Integer(timeUnitEdit.getText());
          int ii = i.intValue() + 1;
          if (ii <= limit) {
            timeUnitEdit.setText(String.valueOf(ii));
          }

        }
      });
    JButton B2 = new JButton("-");
    B2.setBackground(Color.white);
    B2.setFont(new java.awt.Font("Dialog", 0, 11));
    B2.addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          Integer i = new Integer(timeUnitEdit.getText());
          int ii = i.intValue() - 1;
          int inf_limit = (timeUnitLabel.equalsIgnoreCase("Hour:") || timeUnitLabel.equalsIgnoreCase("Min:") || timeUnitLabel.equalsIgnoreCase("Sec:") ? 0 : 1);
          if (ii >= inf_limit) {
            timeUnitEdit.setText(String.valueOf(ii));
          }

        }
      });

    B1.setMargin(new Insets(2, 4, 2, 4));
    B2.setMargin(new Insets(2, 4, 2, 4));
    Dimension d = new Dimension();
    d.height = B1.getPreferredSize().height;
    d.width = (new JLabel("XXXXX")).getPreferredSize().width;
    l.setPreferredSize(d);
    l.setAlignmentX((float)1);
    timeUnitEdit.setPreferredSize(new Dimension(50, d.height));

    up.add(l);
    up.add(B1);
    up.add(timeUnitEdit);
    up.add(B2);
    tp.add(up);
  }


  /**
   *  Description of the Field
   */
  public final static int ABSOLUTE = 0;
  /**
   *  Description of the Field
   */
  public final static int RELATIVE = 1;

  /**
   *  Description of the Field
   */
  public final static int OK = 1;
  /**
   *  Description of the Field
   */
  public final static int CANCEL = 0;
  JToggleButton absButton;
  JToggleButton relButton;
  private Date date;
  private int mode;
  private int retVal;
  private JDialog dlg;
  private JTextField year, month, day, hour, min, sec;

}
//  ***EOF***
