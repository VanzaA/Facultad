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

import javax.swing.table.AbstractTableModel;
import java.util.List;

/**
   The model class for the message table.

   @author Andrea Squeri,Corti Denis,Ballestracci Paolo -  Universita` di Parma
*/
public class MessageTableModel extends AbstractTableModel {
  private List items;
  String title;

  public MessageTableModel(List l, String title) {
    super();
    items = l;
    this.title = title;
  }

  public int getRowCount() {
    return items.size();
  }

  public int getColumnCount() {
    return 1;
  }

  public Class getColumnClass(int columnIndex) {
    return items.get(0).getClass();
  }

  public Object getValueAt(int r, int c) {
    return items.get(r);
  }

  public String getColumnName(int r) {
    return title;
  }

  public void addRow(Object o, int index) {
    items.add(index, o);
    fireTableRowsInserted(index, index);
  }

  public void addRow(Object o) {
    items.add(o);
    int index = items.size();
    this.fireTableRowsInserted(index, index);
  }

  public void removeRow(int index) {
    items.remove(index);
    this.fireTableRowsDeleted(index, index);
  }

  public void clearRows() {
    int index = items.size();
    items.clear();
    this.fireTableRowsDeleted(0, index);
  }

}
