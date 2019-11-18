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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.TreePath;
import jade.gui.AgentTree;
import javax.swing.tree.TreeSelectionModel;

/**
   Javadoc documentation for the file
   @author Francisco Regi, Andrea Soracchi - Universita` di Parma
   @version $Date: 2002-12-13 12:40:04 +0100 (ven, 13 dic 2002) $ $Revision: 3524 $
 */
class PopUpMouser extends MouseAdapter
{
    JPopupMenu popup;
    JTree tree;
    AgentTree agentTree;

    public PopUpMouser(JTree tree,AgentTree agentTree){
        this.tree=tree;
        this.agentTree=agentTree;
    }

    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger())
         if (setPopup(e)) popup.show(e.getComponent(), e.getX(), e.getY());

    }

    public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger())
           if (setPopup(e)) popup.show(e.getComponent(), e.getX(), e.getY());

    }

  private boolean setPopup(MouseEvent e){
    AgentTree.Node current;
    String typeNode;
    TreeSelectionModel model;

    int selRow = tree.getRowForLocation(e.getX(), e.getY());
    TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
    if(selRow != -1) {
      if(!tree.isRowSelected(selRow)) {
	      model=tree.getSelectionModel();
	      model.setSelectionPath(selPath);
      }
      current=(AgentTree.Node)selPath.getLastPathComponent();
      typeNode=current.getType();
      if(!typeNode.equals("")) {
	popup = agentTree.getPopupMenu(typeNode);
	if(popup == null)
	  return false;
      }

      return true;
    }
    else return false;
  }

} 