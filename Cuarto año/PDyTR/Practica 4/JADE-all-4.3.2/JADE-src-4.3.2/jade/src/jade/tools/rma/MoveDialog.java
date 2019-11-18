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


package jade.tools.rma;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;

/**
   
   @author Francisco Regi, Andrea Soracchi - Universita` di Parma
   @version $Date: 2002-12-13 12:40:04 +0100 (ven, 13 dic 2002) $ $Revision: 3524 $
 */
class MoveDialog extends JDialog implements ActionListener{
  protected static JTextField agentName;
  protected static JTextField container;


  protected static JLabel agentNameL= new JLabel("Agent Name");
  protected static JLabel containerL= new JLabel("Container");

  protected static JButton OKButton = new JButton ("OK");
  protected static JButton CancelButton = new JButton ("Cancel");

  protected static String agentNameToolTip = "Name of the Agent to move";
  protected static String containerToolTip = "Container on which the Agent will move";

  protected static String result  = "";
  protected static int OK_BUTTON = 0;
  protected static int CANCEL_BUTTON = 1;
  protected static int choice = CANCEL_BUTTON;

  static {

    agentName = new JTextField ("");
    agentName.setEditable(true);
    agentName.setToolTipText(agentNameToolTip);
    agentNameL.setToolTipText(agentNameToolTip);

    
    container = new JTextField ();
    container.setEditable(true);
    container.setToolTipText(containerToolTip);
    containerL.setToolTipText(containerToolTip);

  }

  protected MoveDialog (String agentNameP, Frame frame) {
    super(frame,"Insert Parameters",true);

    JTextField warningField = new JTextField();
    warningField.setEditable(false);
    warningField.setText("Warning: Some agents might not be able to migrate or be cloned because of\nlack of serialization support in their implementation. If you are not sure about the \nimplemementation of this agent, Cancel this operation.");

    GridBagLayout grid = new GridBagLayout();
    getContentPane().setLayout(grid);
    GridBagConstraints c = new GridBagConstraints();
    c.fill= GridBagConstraints.HORIZONTAL;
    
    agentName.setText(agentNameP);
    c.weightx= 0.5;
    c.gridx =0;
    c.gridy =0;
    grid.setConstraints(agentNameL,c);
    getContentPane().add(agentNameL);
    
    c.gridx=1;
    c.gridy =0;
    grid.setConstraints(agentName,c);
    getContentPane().add(agentName);

    c.gridx=0;
    c.gridy=1;
    grid.setConstraints(containerL,c);
    getContentPane().add(containerL);
    
    c.gridx=1;
    c.gridy=1;
    grid.setConstraints(container,c);
    getContentPane().add(container);


    OKButton.addActionListener(this);
    CancelButton.addActionListener(this);

    c.gridx=0;
    c.gridy=2;
    grid.setConstraints(OKButton,c);
    getContentPane().add(OKButton);
    c.gridx=1;
    c.gridy=2;
    grid.setConstraints(CancelButton,c);
    getContentPane().add(CancelButton);
    
    c.gridx=0;
    c.gridy=3;
    c.ipady = 10;
    c.ipadx = 100;
    c.weightx= 0.0;
    c.gridwidth=2;
    grid.setConstraints(warningField,c);
    getContentPane().add(warningField);
    setSize(getPreferredSize());
    setLocation(frame.getX() + (frame.getWidth() - getWidth()) / 2, frame.getY() + (frame.getHeight() - getHeight()) / 2);
    setVisible(true);
  }

  public Dimension getPreferredSize () {
    return (new Dimension(450,170));
  }

  public void actionPerformed (ActionEvent evt) {
    choice = CANCEL_BUTTON;
    if (evt.getSource()==OKButton) {
      choice = OK_BUTTON;
    }
    dispose();
  }

  public static int showMoveDialog(String agent, Frame owner,boolean editable) {
    choice=CANCEL_BUTTON;
    agentName.setEditable(editable);
    container.setEditable(true);
    setAgentName(agent);
    MoveDialog panel = new MoveDialog(agent, owner);
    return choice;
  }

  public static String getAgentName() {
    return agentName.getText();
  }

  
  public static String getContainer() {
    return container.getText();
  }

  public static void setAgentName(String agentNameP) {
    agentName.setText(agentNameP);
  }

  public static void setContainer(String containerP) {
    container.setText(containerP);
  }

}