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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPopupMenu;

  /**
   Javadoc documentation for the file
   @author Francisco Regi, Andrea Soracchi - Universita` di Parma
   <Br>
   <a href="mailto:a_soracchi@libero.it"> Andrea Soracchi(e-mail) </a>
   @version $Date: 2002-12-13 12:40:04 +0100 (ven, 13 dic 2002) $ $Revision: 3524 $
 */

 /**
  * This is the listener for the canvas of agents
  * @see jade.tools.sniffer.PopMouserMessage
  */

public class PopMouserAgent extends MouseAdapter {

 PopupAgent popup;
 MMCanvas canvAgent;
 Agent agent;

 public PopMouserAgent(MMCanvas canvAgent,Sniffer mySniffer) {
   popup=new PopupAgent(mySniffer,canvAgent);
   this.canvAgent=canvAgent;
 }

 public void mouseReleased(MouseEvent e) {
    if (e.isPopupTrigger())
         if (checkCoordinate(e)) {
          popup.setAgent(agent);
          popup.show(e.getComponent(), e.getX(), e.getY());
         }
  }

 public void mousePressed(MouseEvent e) {
     if (e.isPopupTrigger())
        if(checkCoordinate(e)) {
         popup.setAgent(agent);
         popup.show(e.getComponent(), e.getX(), e.getY());
        }
 }

 private boolean checkCoordinate(MouseEvent evt) {
   if ((agent= canvAgent.selAgent(evt)) != null) return true;

   else return false;

 }

} 