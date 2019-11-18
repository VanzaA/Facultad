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


import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.TreePath;

import jade.core.AID;
import jade.gui.AgentTree;
import jade.tools.introspector.Introspector;


class TreeAgentPopupMenu extends JPopupMenu {
  Introspector debugger;
  String agentName;
  private AgentTree tree;

  public TreeAgentPopupMenu(Introspector d, AgentTree t) {
    debugger = d;
    tree = t;
    build();
  }

  void build () {
    JMenuItem debugOn = new JMenuItem("Debug On");
    JMenuItem debugOff = new JMenuItem("Debug Off");
    TreeAgentPopupMenuListener listener = new TreeAgentPopupMenuListener();
    debugOn.addActionListener(listener);
    debugOn.setName("on");
    debugOff.addActionListener(listener);
    debugOff.setName("off");
    this.add(debugOn);
    this.addSeparator();
    this.add(debugOff);
  }

  class TreeAgentPopupMenuListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      JMenuItem source = (JMenuItem)e.getSource();
      TreePath[] paths = tree.tree.getSelectionPaths();
      for(int i = 0; i < paths.length; i++) {
	AgentTree.Node node = (AgentTree.Node) (paths[i].getLastPathComponent());
	String agentName = node.getName();
	if(source.getName().equals("on")) {
	  debugger.addAgent(new AID(agentName, AID.ISGUID));
	}
	else if(source.getName().equals("off")) {
	  debugger.removeAgent(new AID(agentName, AID.ISGUID));
	}
      }
    }
  }

}
