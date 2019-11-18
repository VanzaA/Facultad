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

import jade.core.BehaviourID;
import jade.core.behaviours.Behaviour;
import jade.domain.introspection.AddedBehaviour;
import jade.domain.introspection.RemovedBehaviour;
import jade.domain.introspection.ChangedBehaviourState;

import javax.swing.tree.*;
import javax.swing.JTextArea;
import java.util.*;
import javax.swing.JTree;
import java.lang.reflect.*;

/**
 Receives a BehaviourEvent and updates the behaviour tree.

 @author Andrea Squeri,Corti Denis,Ballestracci Paolo -  Universita` di Parma
 */
public class TreeUpdater implements Runnable {
    
    private BehaviourID behaviour;
    private BehaviourPanel gui;
    private int action;
    //private boolean blocked;
    private String state;
    
    private static final int ADD_NODE       = 0;
    private static final int REMOVE_NODE    = 1;
    private static final int CHANGE_NODE    = 2;
    
    public TreeUpdater(AddedBehaviour b, BehaviourPanel bp) {
        behaviour = b.getBehaviour();
        gui = bp;
        action = ADD_NODE;
        //blocked = false;
        state = Behaviour.STATE_READY;
    }
    
    public TreeUpdater(RemovedBehaviour b, BehaviourPanel bp) {
        behaviour = b.getBehaviour();
        gui=bp;
        action = REMOVE_NODE;
        //blocked = false;
        state = Behaviour.STATE_READY;
    }
    
    public TreeUpdater(ChangedBehaviourState b, BehaviourPanel bp) {
        behaviour = b.getBehaviour();
        gui=bp;
        action = CHANGE_NODE;
        
        /*if (b.getTo().equals(Behaviour.STATE_BLOCKED)) {
            blocked = true;
        } else {
            blocked = false;
        }*/
        state = b.getTo();
    }

    public void createTree(DefaultMutableTreeNode r, Iterator v) {
        while(v.hasNext()){
            BehaviourID b=(BehaviourID)v.next();
            //DefaultMutableTreeNode rc = new DefaultMutableTreeNode(new BehaviourTreeNode(b, blocked));
            DefaultMutableTreeNode rc = new DefaultMutableTreeNode(new BehaviourTreeNode(b, state));
            if (!b.isSimple()) {
                createTree(rc,b.getAllChildren());
            }
            r.add(rc);
        }
    }
        
    public void run() {
        
        JTree tree = gui.getBehaviourTree();
        DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
        DefaultMutableTreeNode root=(DefaultMutableTreeNode)model.getRoot();
        
        if (action == CHANGE_NODE)
        {
            boolean bFound = false;
            
            tree.clearSelection();
            Enumeration e=root.breadthFirstEnumeration();
            
            // Skip over the first element (the behaviours folder)
            if (e.hasMoreElements())
                e.nextElement();
            
            while(e.hasMoreElements())
            {
                DefaultMutableTreeNode node =(DefaultMutableTreeNode)e.nextElement();
                BehaviourTreeNode bNode = (BehaviourTreeNode)node.getUserObject();
                BehaviourID b = bNode.getBehaviour();

                if (b.getCode() == behaviour.getCode() && b.equals(behaviour))
                {
                    Object[]o=node.getPath();
                    TreePath tp= new TreePath(o);
                    tree.setSelectionPath(tp);
                    description(gui.getBehaviourText(), b);
                    
                    // Update the blocked status.
                    /*if ((blocked && !bNode.isBlocked()) ||
                        (!blocked && bNode.isBlocked())) {
                        bNode.setBlocked(blocked);
                        model.nodeChanged(node);
                    }*/
                    if (!state.equals(bNode.getState())) {
                    	bNode.setState(state);
                    	model.nodeChanged(node);
                    }

                    bFound = true;
                    break;
                }
            }
            
            /* If we didn't find the node in the tree, add it now.
            if (!bFound)
                action = ADD_NODE;
            */
        }

        if (action == ADD_NODE)
        {
            //DefaultMutableTreeNode beh = new DefaultMutableTreeNode(new BehaviourTreeNode(behaviour, blocked));
            DefaultMutableTreeNode beh = new DefaultMutableTreeNode(new BehaviourTreeNode(behaviour, state));

            if (!behaviour.isSimple()) {
                createTree(beh, behaviour.getAllChildren());
            }

            model.insertNodeInto(beh,root,model.getChildCount(root));
        }
        
        if (action == REMOVE_NODE)
        {
            Enumeration e=root.breadthFirstEnumeration();
            e.nextElement();
            while(e.hasMoreElements())
            {
                DefaultMutableTreeNode node =(DefaultMutableTreeNode)e.nextElement();
                BehaviourTreeNode b = (BehaviourTreeNode)node.getUserObject();
                if (b.getBehaviour().equals(behaviour)){
                    model.removeNodeFromParent(node);
                    break;
                }
            }
        }
    }
    
    public static void description (JTextArea t, BehaviourID b){
        t.setText("Name:\t"+b.getName()+"\nClass:\t"+b.getClassName()+"\nKind:\t"+b.getKind());
    }
}
