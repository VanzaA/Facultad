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

import javax.swing.AbstractAction;
import javax.swing.Action;

 /**
   Javadoc documentation for the file
   @author Francisco Regi, Andrea Soracchi - Universita` di Parma
   <Br>
   <a href="mailto:a_soracchi@libero.it"> Andrea Soracchi(e-mail) </a>
   @version $Date: 2002-12-13 14:34:24 +0100 (ven, 13 dic 2002) $ $Revision: 3529 $
 */

 /**
 * AbstractPopup is the superclass of the actions
 * performed by PopupMenu on the Canvas.
 *
 * This class is abstract because it does not define the
 * ActionPerformed(ActionEvent evt) method. In every subClass of
 * AbstractPopup this method performs a specific action.
 *
 * Subclasses of AbstractPopup are:
 * @see  jade.tools.sniffer.PopSniffAgent
 * @see  jade.tools.sniffer.PopNoSniffAgent
 * @see  jade.tools.sniffer.PopShowAgent
 * @see  jade.tools.sniffer.ViewMessage
 */


abstract public class AbstractPopup extends AbstractAction {

  public AbstractPopup(String actionName) {
    putValue(Action.NAME,actionName);
  }

}