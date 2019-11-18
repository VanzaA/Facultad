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
import java.awt.event.*;

import java.util.Iterator;

import javax.swing.*;
import javax.swing.tree.*;

import jade.core.behaviours.*;


/**
   This class adds or removes behaviours to/from the given agent.

   @author Andrea Squeri,Corti Denis,Ballestracci Paolo -  Universita` di Parma
*/
public class TreePopupMenuListener implements ActionListener {
  private boolean addBehaviour;
  private JTree myTree;

  public TreePopupMenuListener(JTree tree){
    myTree=tree;
  }
  public void actionPerformed(ActionEvent e){
    JMenuItem act=(JMenuItem) e.getSource();
    if (act.getName().equals("add")) addBehaviour=true;
    else addBehaviour=false;
  }

    /*
  public void createTree(DefaultMutableTreeNode r,Iterator v){
    while(v.hasNext()){
      BehaviourRapp b=(BehaviourRapp)v.next();
      if (b.getSimple().booleanValue()) r.add(new DefaultMutableTreeNode(b));
      else{
        DefaultMutableTreeNode rc=new DefaultMutableTreeNode(b);
        createTree(rc,b.getAllChildren());
        r.add(rc);
      }
    }
  }
  */
/*
  public void run()
  {
    if(!addBehaviour){
      TreePath path=myTree.getSelectionPath();
          DefaultMutableTreeNode t=(DefaultMutableTreeNode) path.getLastPathComponent();
      if(!t.isRoot()){
        DefaultMutableTreeNode parent=(DefaultMutableTreeNode)t.getParent();
              SwingUtilities.invokeLater(new TreeModifier(myTree,t,false));
      }
    }
      else
      {
          MyDialog dialog=new MyDialog(new Frame(),"New Behaviour",true);
      dialog.showCorrect() ;
      String classs=dialog.text;
          if (classs!= null)
          {
        Class c=null;
        Object o=null;
              try
              {
          c=Class.forName(classs);
          o=(Object)c.newInstance();
              }
              catch(Exception e)
              {
                  e.printStackTrace();
              }
              
              if(o!=null)
              {
          Behaviour b = (Behaviour)o;
                  DefaultMutableTreeNode node=new DefaultMutableTreeNode(b);
          if(!rapp.getSimple().booleanValue()){
            createTree(node,rapp.getAllChildren());
          }
                  SwingUtilities.invokeLater(new TreeModifier(myTree,node,true));
        }
      }
    }
  }
    */
}
