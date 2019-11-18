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


package jade.domain.introspection;

//#APIDOC_EXCLUDE_FILE

import jade.core.AID;
import jade.core.AgentState;

/**
   An introspection event, recording the change of the atate of an
   agent within the platform.
  
   @author Giovanni Rimassa -  Universita' di Parma
   @version $Date: 2005-02-16 18:18:28 +0100 (mer, 16 feb 2005) $ $Revision: 5552 $
*/

public class ChangedAgentState implements Event {

    /**
       A string constant for the name of this event.
    */ 
    public static final String NAME = "Changed-Agent-State";

    private AID agent;
    private AgentState from;
    private AgentState to;


    /**
       Default constructor. A default constructor is necessary for
       ontological classes.
    */
    public ChangedAgentState() {
    }

    /**
       Retrieve the name of this event.
       @return A constant value for the event name.
    */
    public String getName() {
	return NAME;
    }

    /**
       Set the <code>agent</code> slot of this event.
       @param id The agent identifier of the agent whose state
       changed.
    */
    public void setAgent(AID id) {
	agent = id;
    }

    /**
       Retrieve the value of the <code>agent</code> slot of this
       event, containing the agent identifier of the agent whose
       state changed.
       @return The value of the <code>agent</code> slot, or
       <code>null</code> if no value was set.
    */
    public AID getAgent() {
	return agent;
    }

    /**
       Set the <code>from</code> slot of this event.
       @param as The name of the entity the state the agent was in
       before this event occurred.
    */
    public void setFrom(AgentState as) {
	from = as;
    }

    /**
       Retrieve the value of the <code>from</code> slot of this event,
       containing the name of the state the agent was in before this
       event occurred.
       @return The value of the <code>from</code> slot, or
       <code>null</code> if no value was set.
    */
    public AgentState getFrom() {
	return from;
    }

    /**
       Set the <code>to</code> slot of this event.
       @param as The name of the state the agent was in after this
       event occurred.
    */
    public void setTo(AgentState as) {
	to = as;
    }

    /**
       Retrieve the value of the <code>to</code> slot of this event,
       containing the name of the state the agent was in after this
       event occurred.
       @return The value of the <code>to</code> slot, or
       <code>null</code> if no value was set.
    */
    public AgentState getTo() {
	return to;
    }

}
