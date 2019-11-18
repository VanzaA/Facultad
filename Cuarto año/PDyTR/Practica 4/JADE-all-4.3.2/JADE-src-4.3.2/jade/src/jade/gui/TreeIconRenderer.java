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

package jade.gui;

//#APIDOC_EXCLUDE_FILE
//#J2ME_EXCLUDE_FILE

import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * This is the renderer of the Tree.
 * The method getTreeCellRendererComponent is messaged
 * when OS repaints the Tree. In this class we describe as
 * a node of the tree must appear

   
   @author Francisco Regi, Andrea Soracchi - Universita` di Parma
   @version $Date: 2008-10-09 14:04:02 +0200 (gio, 09 ott 2008) $ $Revision: 6051 $

 */

public class TreeIconRenderer extends JLabel implements TreeCellRenderer {

 public TreeIconRenderer() {
		setOpaque(true);
 }


 public Component getTreeCellRendererComponent(JTree tree,
                            Object value,
                            boolean selected,
                            boolean expanded,
                            boolean leaf,
                            int row,
                            boolean hasFocus) {
  setFont(tree.getFont());
    if(selected){
      setForeground(tree.getBackground());
      setBackground(tree.getForeground());
    }
    else {
      setBackground(tree.getBackground());
      setForeground(tree.getForeground());
    }
     AgentTree.Node data= (AgentTree.Node) value;
     if (data!=null) {
       setToolTipText(data.getToolTipText());
       setIcon(data.getIcon(data.getType()));
       setText(data.getName());
     }
      else {
       setIcon(null);
       setText("");
     }
    return this;
 }

}
