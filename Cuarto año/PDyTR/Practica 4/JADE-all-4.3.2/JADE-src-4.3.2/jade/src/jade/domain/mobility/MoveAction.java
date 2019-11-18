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

package jade.domain.mobility;

import jade.core.*;
import jade.content.*;

/**

  This class represents the <code>move-agent</code> action, requesting
  to move an agent within the platform.

  @author Giovanni Rimassa -  Universita' di Parma
  @version $Date: 2003-11-19 17:04:37 +0100 (mer, 19 nov 2003) $ $Revision: 4567 $
*/
public class MoveAction implements AgentAction {

    private MobileAgentDescription agentToMove;


    /**
       Default constructor. A default constructor is necessary for
       ontological classes.
    */
    public MoveAction() {
    }

    /**
       Set the <code>mobile-agent-description</code> slot of this
       action.
       @param desc The description of the agent to migrate.
    */
    public void setMobileAgentDescription(MobileAgentDescription desc) {
      agentToMove = desc;
    }

    /**
       Retrieve the value of the <code>mobile-agent-description</code>
       slot of this event, containing the description of the agent to
       migrate.
       @return The value of the <code>mobile-agent-description</code>
       slot, or <code>null</code> if no value was set.
    */
    public MobileAgentDescription getMobileAgentDescription() {
      return agentToMove;
    }

}
