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

package jade.domain.JADEAgentManagement;

import jade.core.*;
import jade.content.*;

/**
  This class represents the <code>query-agents-on-location</code> action of 
  the <code>JADE-agent-management ontology</code>.
  This action can be requested to the JADE AMS to retrieve the list
  of agents running on a given container. 

  @author Giovanni Rimassa -  Universita' di Parma
  @version $Date: 2003-11-24 14:47:00 +0100 (lun, 24 nov 2003) $ $Revision: 4597 $
*/
public class QueryAgentsOnLocation implements AgentAction {

    private Location location;


    /**
       Default constructor. A default constructor is necessary for
       ontological classes.
    */
    public QueryAgentsOnLocation() {
    }

    /**
       Set the <code>location</code> slot of this action.
       @param loc The location for which the agent list is requested.
    */  
    public void setLocation(Location loc) {
	location = loc;
    }

    /**
       Retrieve the value of the <code>location</code> slot of this
       event, containing the location for which the agent list is
       requested.
       @return The value of the <code>location</code> slot, or
       <code>null</code> if no value was set.
    */
    public Location getLocation() {
	return location;
    }

}
