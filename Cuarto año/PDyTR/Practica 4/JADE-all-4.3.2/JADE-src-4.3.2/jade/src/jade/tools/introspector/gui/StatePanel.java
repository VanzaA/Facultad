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

import java.util.Map;
import java.util.HashMap;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;


import jade.core.Agent;
import jade.core.AgentState;
import jade.core.mobility.AgentMobilityService;

/**
   This is a panel, with a top part containing state
   buttons and a bottom part containing some action buttons. It
   allows to manage the state of an agent.

   @author Giovanni Rimassa, Andrea Squeri, Corti Denis, Ballestracci
   Paolo - Universita` di Parma

*/
public class StatePanel extends JPanel {

  private int state;

  private ButtonGroup leds;
  private JRadioButton waitingLed;
  private JRadioButton activeLed;
  private JRadioButton suspendedLed;
  private JRadioButton deletedLed;
  private JRadioButton movingLed;
  private JRadioButton idleLed;
  private JButton suspendAction;
  private JButton waitAction;
  private JButton wakeUpAction;
  private JButton killAction;

  private Icon ledOff = new ImageIcon(getClass().getResource("images/rbs.gif"));
  private Icon ledOn = new ImageIcon(getClass().getResource("images/rbrs.gif"));
  private Icon button = new ImageIcon(getClass().getResource("images/rb.gif"));
  private Icon pressedButton = new ImageIcon(getClass().getResource("images/rbp.gif"));
  private Font myFont = new Font("Monospaced", Font.BOLD, 10);

  private MainBarListener listener;

  private Map ledMap = new HashMap();

  public StatePanel(MainBarListener list){
    super();
    leds = new ButtonGroup();
    listener = list;
    build();
  }

  public void build(){
    Border line = BorderFactory.createEtchedBorder();
    JPanel ledPanel = new JPanel();
    ledPanel.setLayout(new BoxLayout(ledPanel, BoxLayout.Y_AXIS));
    ledPanel.setAlignmentX(JPanel.CENTER_ALIGNMENT);
    ledPanel.setBorder(BorderFactory.createTitledBorder(line, "Current State", TitledBorder.CENTER, TitledBorder.TOP, new Font("Dialog", Font.BOLD, 10)));

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
    buttonPanel.setAlignmentX(JPanel.CENTER_ALIGNMENT);
    buttonPanel.setBorder(BorderFactory.createTitledBorder(line, "Change State", TitledBorder.CENTER, TitledBorder.TOP, new Font("Dialog", Font.BOLD, 10)));

    activeLed = new JRadioButton("Active", ledOff);
    activeLed.setFont(myFont);
    activeLed.setAlignmentX(JButton.LEFT_ALIGNMENT);
    activeLed.setDisabledSelectedIcon(ledOn);
    activeLed.setDisabledIcon(ledOff);
    activeLed.setEnabled(false);
    leds.add(activeLed);
    ledMap.put(AgentState.getInstance(Agent.AP_ACTIVE), activeLed);
    ledPanel.add(activeLed);

    suspendedLed = new JRadioButton("Suspended", ledOff);
    suspendedLed.setFont(myFont);
    suspendedLed.setAlignmentX(JButton.LEFT_ALIGNMENT);
    suspendedLed.setDisabledSelectedIcon(ledOn);
    suspendedLed.setDisabledIcon(ledOff);
    suspendedLed.setEnabled(false);
    leds.add(suspendedLed);
    ledMap.put(AgentState.getInstance(Agent.AP_SUSPENDED), suspendedLed);
    ledPanel.add(suspendedLed);

    idleLed = new JRadioButton("Idle", ledOff);
    idleLed.setFont(myFont);
    idleLed.setAlignmentX(JButton.LEFT_ALIGNMENT);
    idleLed.setDisabledSelectedIcon(ledOn);
    idleLed.setDisabledIcon(ledOff);
    idleLed.setEnabled(false);
    leds.add(idleLed);
    ledMap.put(AgentState.getInstance(Agent.AP_IDLE), idleLed);
    ledPanel.add(idleLed);

    waitingLed = new JRadioButton("Waiting", ledOff);
    waitingLed.setFont(myFont);
    waitingLed.setAlignmentX(JButton.LEFT_ALIGNMENT);
    waitingLed.setDisabledSelectedIcon(ledOn);
    waitingLed.setDisabledIcon(ledOff);
    waitingLed.setEnabled(false);
    leds.add(waitingLed);
    ledMap.put(AgentState.getInstance(Agent.AP_WAITING), waitingLed);
    ledPanel.add(waitingLed);

    movingLed = new JRadioButton("Moving", ledOff);
    movingLed.setFont(myFont);
    movingLed.setAlignmentX(JButton.LEFT_ALIGNMENT);
    movingLed.setDisabledSelectedIcon(ledOn);
    movingLed.setDisabledIcon(ledOff);
    movingLed.setEnabled(false);
    leds.add(movingLed);
    ledMap.put(AgentState.getInstance(AgentMobilityService.AP_TRANSIT), movingLed);
    ledPanel.add(movingLed);

    deletedLed = new JRadioButton("Dead", ledOff);
    deletedLed.setFont(myFont);
    deletedLed.setAlignmentX(JButton.LEFT_ALIGNMENT);
    deletedLed.setDisabledSelectedIcon(ledOn);
    deletedLed.setDisabledIcon(ledOff);
    deletedLed.setEnabled(false);
    leds.add(deletedLed);
    ledMap.put(AgentState.getInstance(Agent.AP_DELETED), deletedLed);
    ledPanel.add(deletedLed);

    suspendAction = new JButton("Suspend", button);
    configurePushButton(suspendAction);
    suspendAction.setMnemonic(5);

    waitAction = new JButton("Wait", button);
    configurePushButton(waitAction);
    waitAction.setMnemonic(7);

    wakeUpAction = new JButton("Wake Up", button);
    configurePushButton(wakeUpAction);
    wakeUpAction.setMnemonic(6);

    killAction = new JButton("Kill", button);
    configurePushButton(killAction);
    killAction.setMnemonic(4);

    buttonPanel.add(suspendAction);
    buttonPanel.add(waitAction);
    buttonPanel.add(wakeUpAction);
    buttonPanel.add(killAction);

    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    add(Box.createVerticalStrut(15));
    add(ledPanel);
    add(Box.createVerticalStrut(10));
    add(buttonPanel);
    

  }

  public void switchTo(AgentState as) {
    JRadioButton led = (JRadioButton)ledMap.get(as);
    if(led != null)
      led.setSelected(true);
  }

  private void configureLED(JRadioButton led) {

  }

  private void configurePushButton(JButton but) {
    but.setPressedIcon(pressedButton);
    but.setBorderPainted(false);
    but.setFocusPainted(false);
    but.setBorder(BorderFactory.createEmptyBorder());
    but.setAlignmentX(JButton.LEFT_ALIGNMENT);
    but.setFont(myFont);
    but.addActionListener(listener);    
  }

}
