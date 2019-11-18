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
import javax.swing.tree.TreePath;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import java.awt.*;
import jade.gui.AgentTree;

/**
   Javadoc documentation for the file
   @author Francisco Regi, Andrea Soracchi - Universita` di Parma
   @version $Date: 2005-11-03 10:14:43 +0100 (gio, 03 nov 2005) $ $Revision: 5809 $
 */
class TreePanel extends JPanel implements TreeSelectionListener {

  public JTextArea selArea;
  AgentTree treeAgent; // FIXME: It should be private
  private JScrollPane scroll;
  private JSplitPane pan;
  private JSplitPane pane;
  private IntrospectorGUI mainWnd;
  private PopUpMouser popM;

  public TreePanel(IntrospectorGUI  mainWnd) {
    this.mainWnd=mainWnd;
    Font f;
    f = new Font("SanSerif",Font.PLAIN,14);
    setFont(f);
    setLayout(new BorderLayout(10,10));

    treeAgent = new AgentTree(f);
    selArea = new JTextArea(5,20);
    selArea.setEditable(true);
    pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,new JScrollPane(treeAgent.tree),new JScrollPane(selArea));
    pane.setContinuousLayout(true);

   
    treeAgent.tree.addTreeSelectionListener(this);
    //popM=new PopUpMouser(treeAgent.tree,treeAgent);
    //treeAgent.tree.addMouseListener(popM);
    add(pane,BorderLayout.CENTER);
  }

 public void  valueChanged(TreeSelectionEvent e) {
   String wholePath;
   String typeNode;
   TreePath paths[] = treeAgent.tree.getSelectionPaths();
   AgentTree.Node current;
   AgentTree.ContainerNode currentC;
   Object[] relCur;

   if (paths!=null) {
    current=(AgentTree.Node)paths[0].getLastPathComponent();
     int numPaths=paths.length;
     selArea.setText(" ");
      for(int i=0;i<numPaths;i++) {
       relCur= paths[i].getPath();
        wholePath="";
        for (int j=0;j<relCur.length;j++) {
         if (relCur[j] instanceof AgentTree.Node) {
           current=(AgentTree.Node)relCur[j];
           wholePath=wholePath.concat(current.getName()+".");
          }
        }
       selArea.append(wholePath+" \n");
     }

   }
 }

  public void adjustDividerLocation() {
      pane.setDividerLocation(0.8);
  }


  public Dimension getPreferredSize() {
    return new Dimension(200, 200);
  }

} 
