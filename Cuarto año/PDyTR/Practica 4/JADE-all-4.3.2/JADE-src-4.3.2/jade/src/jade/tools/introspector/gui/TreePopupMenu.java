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

import javax.swing.*;
import java.awt.*;

/**
   The popup menu associated with the behaviours tree.

   @author Andrea Squeri,Corti Denis,Ballestracci Paolo -  Universita` di Parma
*/
public class TreePopupMenu extends JPopupMenu{
  private TreePopupMenuListener listener;
  private JMenuItem addBehaviour;
  private JMenuItem removeBehaviour;

  public TreePopupMenu(JTree tree){
    super();
    listener = new TreePopupMenuListener(tree);
    addBehaviour= new JMenuItem("Add Behaviour");
    removeBehaviour= new JMenuItem("Remove Behaviour");
    addBehaviour.setName("add");
    removeBehaviour.setName("remove");
    addBehaviour.addActionListener(listener);
    removeBehaviour.addActionListener(listener);
    this.add(addBehaviour);
    this.addSeparator();
    this.add(removeBehaviour);
  }
}
