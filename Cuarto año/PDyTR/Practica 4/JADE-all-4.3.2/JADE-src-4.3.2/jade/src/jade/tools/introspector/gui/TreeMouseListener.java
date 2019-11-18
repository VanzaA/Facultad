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
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;

import jade.core.behaviours.Behaviour;
import jade.core.BehaviourID;

/**
   Listens to mouse events over the behaviour tree.

   @author Andrea Squeri,Corti Denis,Ballestracci Paolo -  Universita` di Parma
*/
class TreeMouseListener implements MouseListener{
  private BehaviourPanel parent;
  private TreePopupMenu popMenu;
  private JTree myTree;

  public TreeMouseListener(BehaviourPanel gui){
    parent=gui;
    myTree=gui.getBehaviourTree();
    popMenu=new TreePopupMenu(myTree);
  }

  //interface MouseListener
  public void mouseClicked(MouseEvent e){
    int button=e.getModifiers();
    if((button==MouseEvent.BUTTON2_MASK) ||(button==MouseEvent.BUTTON3_MASK)){
      popMenu.show(e.getComponent(),e.getX(),e.getY());
    }

    else{//se e' il sinistro scrivo nella textArea le caratteristiche
      TreePath path=myTree.getSelectionPath();
      if(path!=null){
        DefaultMutableTreeNode t=
              (DefaultMutableTreeNode) path.getLastPathComponent();
        if(!t.isRoot()){
            BehaviourTreeNode b = (BehaviourTreeNode)t.getUserObject();
            TreeUpdater.description(parent.getBehaviourText(), b.getBehaviour());
        }
      }
    }
  }
  public void mouseEntered(MouseEvent e){}
  public void mouseExited(MouseEvent e){}
  public void mousePressed(MouseEvent e){}
  public void mouseReleased(MouseEvent e){}

}//fine classe Tre
