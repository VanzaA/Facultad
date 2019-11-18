/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop multi-agent systems in compliance with the FIPA specifications.
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

package jade.core.behaviours;

import jade.core.Agent;


/**
   Atomic behaviour that must be executed forever. This abstract class
   can be extended by application programmers to create behaviours
   that keep executing continuously (e.g. simple reactive behaviours).


   @author Giovanni Rimassa - Universita' di Parma
   @version $Date: 2003-11-18 14:24:25 +0100 (mar, 18 nov 2003) $ $Revision: 4550 $

 */
public abstract class CyclicBehaviour extends SimpleBehaviour {

  /**
     Default constructor. It does not set the owner agent.
  */
  public CyclicBehaviour() {
    super();
  }

  /**
     This constructor sets the owner agent for this
     <code>CyclicBehaviour</code>.
     @param a The agent this behaviour must belong to.
  */
  public CyclicBehaviour(Agent a) {
    super(a);
  }

  /**
     This is the method that makes <code>CyclicBehaviour</code>
     cyclic, because it always returns <code>false</code>.
     @return Always <code>false</code>.
  */
  public final boolean done() {
    return false;
  }

}

