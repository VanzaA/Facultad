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

package jade.core.behaviours;

import jade.core.Agent;


/**
   An atomic behaviour. This abstract class models behaviours that are
   made by a single, monolithic task and cannot be interrupted.
   
   
   @author Giovanni Rimassa - Universita` di Parma
   @version $Date: 2000-10-09 09:03:44 +0200 (lun, 09 ott 2000) $ $Revision: 1919 $

*/
public abstract class SimpleBehaviour extends Behaviour {

  /**
     Default constructor. It does not set the owner agent for this
     behaviour.
  */
  public SimpleBehaviour() {
    super();
  }

  /**
     This constructor sets the owner agent for this behaviour.
     @param a The agent this behaviour belongs to.
  */
  public SimpleBehaviour(Agent a) {
    super(a);
  }    

  /**
     Resets a <code>SimpleBehaviour</code>. 
  */
  public void reset() {
    super.reset();
  }

}
