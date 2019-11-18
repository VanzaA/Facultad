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
import jade.core.BehaviourID;
import jade.core.behaviours.Behaviour;

/**
 * This class represents a tree node for a behaviour in the Introspector
 * GUI. It maintains the state of the behaviour.
 * 
 * @author Brian Remick, University of Utah
*/
public class BehaviourTreeNode {
      
    //private boolean blocked;
    private BehaviourID behaviour;
    private String state;
      
    public BehaviourTreeNode(BehaviourID beh) {
        behaviour = beh;
        //blocked = false;
        state = Behaviour.STATE_READY;
    }
      
    public BehaviourTreeNode(BehaviourID beh, String s) {
        behaviour = beh;
        state = s;
    }

    /*public BehaviourTreeNode(BehaviourID beh, boolean bl) {
        behaviour = beh;
        blocked = bl;
    }
      
    public void setBlocked(boolean b) {
        blocked = b;
    }
      
    public boolean isBlocked() {
        return blocked;
    }*/
    public void setState(String s) {
    	state = s;
    }
    
    public String getState() {
    	return state;
    }
    
    public String toString() {
        return behaviour.toString();
    }
    
    public BehaviourID getBehaviour() {
        return behaviour;
    }
    
    public void setBehaviour(BehaviourID b) {
        behaviour = b;
    }
}
