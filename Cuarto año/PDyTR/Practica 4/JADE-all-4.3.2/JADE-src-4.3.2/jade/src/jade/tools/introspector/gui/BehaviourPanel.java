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

import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;

import jade.core.behaviours.Behaviour;

/**
   This panel contains the behaviour tree for a given agent. It adds a
   TreeMouseListener component to the tree.

   @author Andrea Squeri, -  Universita` di Parma
*/
public class BehaviourPanel extends JSplitPane{
  private JTree behaviourTree;
  private JTextArea text;
  private JScrollPane behaviourScroll;
  private JScrollPane textScroll;
  private JPanel treePanel;
  private TreeMouseListener treeListener;
  private Icon readyIcon;
  private Icon runningIcon;
  private Icon blockedIcon;
  
  public BehaviourPanel(DefaultTreeModel model ){
    super();
    behaviourTree=new JTree(model);
    treeListener = new TreeMouseListener(this);
    behaviourTree.addMouseListener(treeListener);
    behaviourTree.setCellRenderer(new BehaviourCellRenderer());
    this.build();
  }

  public void build(){

    text=new JTextArea();
    behaviourScroll=new JScrollPane();
    textScroll=new JScrollPane();
    treePanel=new JPanel();

    treePanel.setLayout(new BorderLayout());

    //readyIcon = new ImageIcon(getClass().getResource("images"+java.io.File.separator+"behaviour.gif"));
    //blockedIcon = new ImageIcon(getClass().getResource("images"+java.io.File.separator+"blocked.gif"));
    //runningIcon = new ImageIcon(getClass().getResource("images"+java.io.File.separator+"running.gif"));
    
    readyIcon = GuiProperties.getIcon("Introspector.readyIcon");
    blockedIcon = GuiProperties.getIcon("Introspector.blockedIcon");
    runningIcon = GuiProperties.getIcon("Introspector.runningIcon");
    
    //behaviorTree.addMouseListener(new TreeListener);
    behaviourTree.putClientProperty("JTree.lineStyle","Angled");
    behaviourTree.setShowsRootHandles(true);
    
    DefaultTreeCellRenderer rend = (DefaultTreeCellRenderer)behaviourTree.getCellRenderer();
    rend.setLeafIcon(readyIcon);
    rend.setOpenIcon(readyIcon);
    rend.setClosedIcon(readyIcon);

    treePanel.add(behaviourTree,BorderLayout.CENTER);

    behaviourScroll.getViewport().add(treePanel,null);
    textScroll.getViewport().add(text,null);

   // behaviourScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    //textScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    this.add(behaviourScroll,JSplitPane.LEFT);
    this.add(textScroll,JSplitPane.RIGHT);

    this.setContinuousLayout(true);
    this.setDividerLocation(200);    
  }

  public JTree getBehaviourTree(){
    return behaviourTree;
  }

  public JTextArea getBehaviourText(){
    return text;
  }
      
  /**
   * The BehaviourCellRenderer class manages rendering nodes in the
   * behaviour tree.
   */
  class BehaviourCellRenderer extends DefaultTreeCellRenderer {
          
    public Component getTreeCellRendererComponent(JTree tree,
                                                  Object value,
                                                  boolean sel,
                                                  boolean expanded,
                                                  boolean leaf,
                                                  int row,
                                                  boolean hasFocus) {
        
        // Try to cast the object to a default mutable tree node.
        // If we succeed, try to cast the user object to a behaviour
        // tree node. If this succeeds, check the status of the object.
        try
        {
            DefaultMutableTreeNode mut = (DefaultMutableTreeNode)value;
            BehaviourTreeNode node = (BehaviourTreeNode)mut.getUserObject();
            
            String state = node.getState();
            if (state.equals(Behaviour.STATE_BLOCKED)) {
            	changeIcon(blockedIcon);
            }
            else if (state.equals(Behaviour.STATE_RUNNING)) {
            	changeIcon(runningIcon);
            }
            else {
            	changeIcon(readyIcon);
            }
        }
        catch(Exception e)
        {
            setLeafIcon(getDefaultLeafIcon());
            setOpenIcon(getDefaultOpenIcon());
            setClosedIcon(getDefaultClosedIcon());
        }
            
        return super.getTreeCellRendererComponent(tree,
                                                  value,
                                                  sel,
                                                  expanded, 
                                                  leaf,
                                                  row,
                                                  hasFocus);
    }

    private void changeIcon(Icon ico) {
        setOpenIcon(ico);
        setLeafIcon(ico);
        setClosedIcon(ico);
    }
  }        
}
