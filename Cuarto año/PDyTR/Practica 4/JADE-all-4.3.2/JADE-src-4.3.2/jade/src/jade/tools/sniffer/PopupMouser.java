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
import javax.swing.JTree;
import javax.swing.tree.TreePath;
import jade.gui.AgentTree;
import javax.swing.tree.TreeSelectionModel;

/**
 Javadoc documentation for the file
 @author Francisco Regi, Andrea Soracchi - Universita` di Parma
 <Br>
 <a href="mailto:a_soracchi@libero.it"> Andrea Soracchi(e-mail) </a>
 @version $Date: 2005-11-03 10:14:43 +0100 (gio, 03 nov 2005) $ $Revision: 5809 $
 */

/**
 * This is the listener for the tree
 */
public class PopupMouser extends MouseAdapter {
	JTree tree;
	AgentTree agentTree;

	public PopupMouser(JTree tree, AgentTree agentTree) {
		this.tree = tree;
		this.agentTree = agentTree;
	}

	public void mouseReleased(MouseEvent e) {
		handleMouseEvent(e);
	}

	public void mousePressed(MouseEvent e) {
		handleMouseEvent(e);
	}

	private void handleMouseEvent(MouseEvent e) {
		if (e.isPopupTrigger()) {
			JPopupMenu popup = getPopup(e);
			if (popup != null) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}
	
	private JPopupMenu getPopup(MouseEvent e) {
		AgentTree.Node current;
		String typeNode;
		int selRow = tree.getRowForLocation(e.getX(), e.getY());
		TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());

		if (selRow != -1) {
			TreePath[] paths = tree.getSelectionPaths();
			current = (AgentTree.Node) selPath.getLastPathComponent();
			typeNode = current.getType();
			TreeSelectionModel model = tree.getSelectionModel();
			if (!tree.isRowSelected(selRow))
				model.setSelectionPath(selPath);
			else {
				model.setSelectionPaths(paths);
				//sameTypeNode(typeNode, paths, model);
			}
			return agentTree.getPopupMenu(typeNode);
		} 
		else {
			return null;
		}
	}

	/*private void sameTypeNode(String typeNode, TreePath[] paths, TreeSelectionModel model) {
		AgentTree.Node current;

		for (int i = 0; i < paths.length; i++) {
			current = (AgentTree.Node) paths[i].getLastPathComponent();
			if (!typeNode.equals(current.getType()))
				model.removeSelectionPath(paths[i]);
		}
	}*/
}