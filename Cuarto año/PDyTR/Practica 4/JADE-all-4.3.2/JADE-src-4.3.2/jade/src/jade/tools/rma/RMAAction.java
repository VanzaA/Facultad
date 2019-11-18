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

import javax.swing.Icon;
import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;
import jade.gui.AgentTree;
import jade.gui.GuiProperties;

/**
   
   @author Francisco Regi, Andrea Soracchi - Universita` di Parma
   @version $Date: 2000-11-27 10:09:40 +0100 (lun, 27 nov 2000) $ $Revision: 1973 $
 */
abstract class RMAAction extends AbstractAction {

  protected Icon img;
  protected String ActionName = "Action";
  protected ActionProcessor actPro;
  protected AgentTree aTree;

  public RMAAction(String IconKey,String ActionName) {
    this.img = GuiProperties.getIcon("RMAAction."+IconKey);
    this.ActionName = ActionName;
    if(this.img != null)
    {
    	putValue(Action.SMALL_ICON,img);
    	putValue(Action.DEFAULT,img);
    }
    putValue(Action.NAME,ActionName);
  }

  public RMAAction (String IconPath,String ActionName,ActionProcessor actPro) {
    this(IconPath,ActionName);
    this.actPro=actPro;
  }

  public String getActionName() {
    return ActionName;
  }

  public synchronized void setIcon (Icon i) {
    img = i;
  }

 public void actionPerformed(ActionEvent avt) {
   actPro.process(this);
  }

}
