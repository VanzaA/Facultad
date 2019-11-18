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

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.tree.TreePath;
import java.awt.*;

import jade.gui.AgentTree;
import jade.gui.GuiProperties;

/**
 * This is the Table on the left

   
   @author Francisco Regi, Andrea Soracchi - Universita` di Parma
   @version $Date: 2003-12-23 14:55:41 +0100 (mar, 23 dic 2003) $ $Revision: 4737 $
 */
class TablePanel extends JPanel {


  JTable      tableView;
  JScrollPane scrollpane;
  JScrollPane tableAggregate;
  Dimension   origin = new Dimension(0, 0);

  JComponent  selectionModeButtons;
  JComponent  resizeModeButtons;

  JPanel      mainPanel;
  JPanel      controlPanel;
  TableModel dataModel;

  // final
  /**@clientCardinality **/
  final String[] names = {"name", "addresses", "state", "owner"};
  // Create the dummy data (a few rows of names)
  /**@clientCardinality **/
  Object[][] data = {
    {"NAME", "ADDRESSES", "STATE", "OWNER"},
  };

  public TablePanel() {
    super();

    setLayout(new BorderLayout());
    mainPanel = this;
    JPanel column1 = new JPanel (new ColumnLayout() );
    JPanel column2 = new JPanel (new ColumnLayout() );
    
    tableAggregate = createTable();
    mainPanel.add(tableAggregate, BorderLayout.CENTER);
  }

  public ImageIcon loadImageIcon(String filename, String description) {
    return new ImageIcon(filename, description);
  }

  private ImageIcon loadIcon(String name, String description) {
    String path = GuiProperties.ImagePath + name;
    return loadImageIcon(path, description);
  }


  public JScrollPane createTable() {

    // Create a model of the data.
    dataModel = new AbstractTableModel() {
      public int getColumnCount() { return names.length; }
      public int getRowCount() { return data.length;}
      public Object getValueAt(int row, int col) {return data[row][col];}
      public String getColumnName(int column) {return names[column];}
      public Class getColumnClass(int c) {return String.class;}
      //public boolean isCellEditable(int row, int col) {return true;}
      public void setValueAt(Object aValue, int row, int column) { data[row][column] = aValue; }
    };

    // Create the table
    tableView = new JTable(dataModel);

    // Show colors by rendering them in their own color.
    DefaultTableCellRenderer colorRenderer = new DefaultTableCellRenderer() {
      public void setValue(Object value) {
	if (value instanceof Color) {
	  Color c = (Color)value;
	  setForeground(c);
	  setText(c.getRed() + ", " + c.getGreen() + ", " + c.getBlue());
	}
      }

    };
    tableView.setRowHeight(20);

    scrollpane = new JScrollPane(tableView);
    tableView.setPreferredScrollableViewportSize(new Dimension(200, 70));
    return scrollpane;
  }

  public void setData (TreePath paths[]) {

   int numPaths =  paths.length;
   data = new Object[numPaths][names.length];
   Object relCur[];
   AgentTree.AgentNode current;
    for(int i=0;i<numPaths;i++) {
       relCur= paths[i].getPath();
         for (int j=0;j<relCur.length;j++) {
	     if(relCur[j] instanceof AgentTree.AgentNode) {
		 current = (AgentTree.AgentNode)relCur[j];
		 data[i][0] = current.getName();
		 data[i][1] = current.getAddress();
		 data[i][2] = current.getState();
		 data[i][3] = current.getOwnership();
		 //data[i][2] = current.getType();
            }
         }
      }
      
      //tableView.repaint();
      ((AbstractTableModel)dataModel).fireTableDataChanged();
 }

} // End of TablePanel

class ColumnLayout implements LayoutManager {

  int xInset = 5;
  int yInset = 5;
  int yGap = 2;

  public void addLayoutComponent(String s, Component c) {}

  public void layoutContainer(Container c) {
    Insets insets = c.getInsets();
    int height = yInset + insets.top;

    Component[] children = c.getComponents();
    Dimension compSize = null;
    for (int i = 0; i < children.length; i++) {
      compSize = children[i].getPreferredSize();
      children[i].setSize(compSize.width, compSize.height);
      children[i].setLocation( xInset + insets.left, height);
      height += compSize.height + yGap;
    }

  }

  public Dimension minimumLayoutSize(Container c) {
    Insets insets = c.getInsets();
    int height = yInset + insets.top;
    int width = 0 + insets.left + insets.right;

    Component[] children = c.getComponents();
    Dimension compSize = null;
    for (int i = 0; i < children.length; i++) {
      compSize = children[i].getPreferredSize();
      height += compSize.height + yGap;
      width = Math.max(width, compSize.width + insets.left + insets.right + xInset*2);
    }
    height += insets.bottom;
    return new Dimension( width, height);
  }

  public Dimension preferredLayoutSize(Container c) {
    return minimumLayoutSize(c);
  }

  public void removeLayoutComponent(Component c) {}

} 
