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

import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;


/**
   
   @author Francisco Regi, Andrea Soracchi - Universita` di Parma
   @version $Date: 2004-01-07 18:01:33 +0100 (mer, 07 gen 2004) $ $Revision: 4760 $
 */
class PopupMenuContainer extends JPopupMenu{

 public PopupMenuContainer(ActionProcessor actPro) {
  super();
   JMenuItem tmp;

   tmp=add((RMAAction)actPro.actions.get(actPro.START_ACTION));
   tmp.setIcon(null);
   tmp=add((RMAAction)actPro.actions.get(actPro.LOADAGENT_ACTION));
   tmp.setIcon(null);

   addSeparator();

   tmp=add((RMAAction)actPro.actions.get(actPro.INSTALL_MTP_ACTION));
   tmp.setIcon(null);

   tmp=add((RMAAction)actPro.actions.get(actPro.UNINSTALL_MTP_ACTION));
   tmp.setIcon(null);

   addSeparator();

   tmp=add((RMAAction)actPro.actions.get(actPro.SAVECONTAINER_ACTION));
   tmp.setIcon(null);

   tmp=add((RMAAction)actPro.actions.get(actPro.LOADCONTAINER_ACTION));
   tmp.setIcon(null);

   tmp=add((RMAAction)actPro.actions.get(actPro.KILL_ACTION));
   tmp.setIcon(null);
 }

} 
