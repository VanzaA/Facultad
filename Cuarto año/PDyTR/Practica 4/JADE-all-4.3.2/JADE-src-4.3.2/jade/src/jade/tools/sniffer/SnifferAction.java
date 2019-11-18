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

package jade.tools.sniffer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import java.awt.event.ActionEvent;

/** 
 * SnifferAction is the superclass of the actions
 * performed by Sniffer GUI controls.
 *	
 * Subclasses of SnifferAction are:
 * @see  jade.tools.sniffer.ClearCanvasAction
 * @see  jade.tools.sniffer.ExitAction 
 * @see  jade.tools.sniffer.WriteLogFileAction 
 * @see  jade.tools.sniffer.DisplayLogFileAction
 * @see  jade.tools.sniffer.WriteMessageListAction
 * @see  jade.tools.sniffer.DoSnifferAction
 * @see  jade.tools.sniffer.DoNotSnifferAction
 * @see jade.tools.sniffer.ShowOnlyAction
 */


public abstract class SnifferAction extends AbstractAction{

 private ActionProcessor actPro;
 private Icon img;
 private String ActionName;

 public SnifferAction(String IconKey,String ActionName,ActionProcessor actPro) {
    //#DOTNET_EXCLUDE_BEGIN
	this.img = GuiProperties.getIcon("SnifferAction."+IconKey);
    putValue(Action.SMALL_ICON,img);
    putValue(Action.DEFAULT,img);
    //#DOTNET_EXCLUDE_END
	putValue(Action.NAME,ActionName);
    this.ActionName = ActionName;
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

 //#JAVA_EXCLUDE_BEGIN
 /*#DOTNET_INCLUDE_BEGIN
 public void OnClick(Object sender, System.EventArgs e)
 {
	actPro.process(this);
 }	
 #DOTNET_INCLUDE_END*/
 //#JAVA_EXCLUDE_END
} 
