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

import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JToolBar;
import javax.swing.Box;
import javax.swing.JButton;
import jade.gui.JadeLogoButton;

/**
   
   @author Francisco Regi, Andrea Soracchi - Universita` di Parma
   @version $Date: 2004-07-01 15:15:48 +0200 (gio, 01 lug 2004) $ $Revision: 5176 $
 */
final class ToolBar  extends JToolBar  implements ActionListener {

  //protected JComboBox ShowChoice = new JComboBox ();
  protected MainPanel tree;
  protected Frame mainWnd;
  protected ActionProcessor actPro;
  private RMAAction obj;
 
  public ToolBar  (MainPanel treeP, Frame mainWnd,ActionProcessor actPro) { // RMAAction[] actions come arg 3
    super();
    tree = treeP;
    setBorderPainted(true);
    setFloatable(false);
    this.mainWnd=mainWnd;
    this.actPro=actPro;
    addSeparator();
    addAction();


    add(Box.createHorizontalGlue());
    JadeLogoButton logo = new JadeLogoButton();	
    add(logo);
    
    //ShowChoice.setToolTipText("Show Agent as...");
    //ShowChoice.addItem("White Pages");
    //ShowChoice.addItem("Yellow Pages");
    //ShowChoice.addActionListener(this);
    //ShowChoice.setEnabled(false);      // Disabled
    //add(ShowChoice);
  }

  private void setButton(JButton b) {
    b.setToolTipText(obj.getActionName());
    b.setText("");
    b.setRequestFocusEnabled(false);
    b.setMargin(new Insets(1,1,1,1));
  }

  private void addAction() {
    obj=(RMAAction)actPro.actions.get(actPro.START_ACTION);
    setButton(add(obj));

    obj=(RMAAction)actPro.actions.get(actPro.KILL_ACTION);
    setButton(add(obj));

    obj=(RMAAction)actPro.actions.get(actPro.SUSPEND_ACTION);
    setButton(add(obj));

    obj=(RMAAction)actPro.actions.get(actPro.RESUME_ACTION);
    setButton(add(obj));

    obj=(RMAAction)actPro.actions.get(actPro.CUSTOM_ACTION);
    setButton(add(obj));
    
    obj =(RMAAction)actPro.actions.get(actPro.MOVEAGENT_ACTION);
    setButton(add(obj));
    
    obj =(RMAAction)actPro.actions.get(actPro.CLONEAGENT_ACTION);
    setButton(add(obj));

    addSeparator();

    obj=(RMAAction)actPro.actions.get(actPro.LOADAGENT_ACTION);
    setButton(add(obj));

    obj=(RMAAction)actPro.actions.get(actPro.SAVEAGENT_ACTION);
    setButton(add(obj));

    addSeparator();

    obj=(RMAAction)actPro.actions.get(actPro.FREEZEAGENT_ACTION);
    setButton(add(obj));

    obj=(RMAAction)actPro.actions.get(actPro.THAWAGENT_ACTION);
    setButton(add(obj));

    addSeparator();         // to add space between Sniffer,DummyAgent button and others buttons
    addSeparator();

    obj=(RMAAction)actPro.actions.get(actPro.SNIFFER_ACTION);
    setButton(add(obj));

    obj=(RMAAction)actPro.actions.get(actPro.DUMMYAG_ACTION);
    setButton(add(obj));

    obj=(RMAAction)actPro.actions.get(actPro.LOGGERAG_ACTION);
    setButton(add(obj));
    
    obj =(RMAAction)actPro.actions.get(actPro.INTROSPECTOR_ACTION);
    setButton(add(obj));

    obj =(RMAAction)actPro.actions.get(actPro.ADDREMOTEPLATFORM_ACTION);
    setButton(add(obj));
    

}

  public void actionPerformed (ActionEvent evt) {
    //TreeIconRenderer.setShowType(ShowChoice.getSelectedIndex());
    tree.repaint();
  }
  
  

}
