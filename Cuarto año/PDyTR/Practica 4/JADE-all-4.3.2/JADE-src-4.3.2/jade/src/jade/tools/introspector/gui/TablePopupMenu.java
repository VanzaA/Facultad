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
   The context menu associated to the message table.

   @author Andrea Squeri, Corti Denis, Ballestracci Paolo -  Universita` di Parma
*/
public class TablePopupMenu extends JPopupMenu {
  private TablePopupMenuListener listener;
  private JMenuItem clearMessages;
  private JMenuItem removeMessage;
  private JMenuItem viewMessage;
  private JTable table;

  public TablePopupMenu() {
    super();
    listener = new TablePopupMenuListener();
    viewMessage = new JMenuItem("View Message");
    removeMessage = new JMenuItem("Remove Message");
    clearMessages = new JMenuItem("Clear All Messages");

    viewMessage.setName("view");
    removeMessage.setName("remove");
    clearMessages.setName("clear");

    viewMessage.addActionListener(listener);
    removeMessage.addActionListener(listener);
    clearMessages.addActionListener(listener);

    this.add(viewMessage);
    this.add(removeMessage);
    this.addSeparator();
    this.add(clearMessages);

  }

  public void setTable(JTable t) {
    table = t;
  }

  public JTable getTable() {
    return table;
  }
}
