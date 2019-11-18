/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop multi-agent systems in compliance with the FIPA specifications.
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

package jade.tools.sniffer;

import javax.swing.JToolBar;        
import javax.swing.JButton;
import javax.swing.Box;
import java.awt.Insets;
import java.awt.Dimension;

import jade.gui.JadeLogoButton;

   /**
 
   @author Francisco Regi, Andrea Soracchi - Universita` di Parma
   <Br>
   <a href="mailto:a_soracchi@libero.it"> Andrea Soracchi(e-mail) </a>
   @version $Date: 2001-05-24 14:27:23 +0200 (gio, 24 mag 2001) $ $Revision: 2321 $
 */

 /**
  * Sets up the toolbar for the main Sniffer Gui
  *
  * @see javax.swing.JToolBar
  */

final public class ToolBar extends JToolBar {

 private ActionProcessor actPro;
 private SnifferAction obj;
 public ToolBar(ActionProcessor actPro) {
  setBorderPainted(true);
  this.actPro=actPro;
  addSeparator();
  addAction();
  add(Box.createHorizontalGlue());
  JadeLogoButton logo = new JadeLogoButton();
  add(logo);
 }

 private void setButton(JButton b) {
  b.setToolTipText(obj.getActionName());
  b.setText("");
  b.setRequestFocusEnabled(false);
  b.setMargin(new Insets(1,1,1,1));
 }

 private void addAction() {

    obj=(SnifferAction)actPro.actions.get(actPro.CLEARCANVAS_ACTION);
    setButton(add(obj));

    addSeparator();

    obj=(SnifferAction)actPro.actions.get(actPro.DISPLAYLOGFILE_ACTION);
    setButton(add(obj));

    obj=(SnifferAction)actPro.actions.get(actPro.WRITELOGFILE_ACTION);
    setButton(add(obj));

    obj=(SnifferAction)actPro.actions.get(actPro.WRITEMESSAGELIST_ACTION);
    setButton(add(obj));

    addSeparator();

    obj=(SnifferAction)actPro.actions.get(actPro.DO_SNIFFER_ACTION);
    setButton(add(obj));

    obj=(SnifferAction)actPro.actions.get(actPro.DO_NOT_SNIFFER_ACTION);
    setButton(add(obj));

    obj=(SnifferAction)actPro.actions.get(actPro.SWOW_ONLY_ACTION);
    setButton(add(obj));


    addSeparator();

    obj=(SnifferAction)actPro.actions.get(actPro. EXIT_SNIFFER_ACTION);
    setButton(add(obj));
 }

}
