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
import jade.core.ContainerID;

import jade.security.JADEPrincipal;

/**
   An introspection event, recording the change of the ownership of an
   agent within the platform.
  
   @author Michele Tomaiuolo -  Universita' di Parma
   @version $Date: 2005-02-16 18:18:28 +0100 (mer, 16 feb 2005) $ $Revision: 5552 $
*/
public class ChangedAgentOwnership implements Event {

    /**
       A string constant for the name of this event.
    */
    public static final String NAME = "Changed-Agent-Ownership";

    private AID agent;
    private String from;
    private String to;

    private ContainerID where;

    /**
       Default constructor. A default constructor is necessary for
       ontological classes.
    */
    public ChangedAgentOwnership() {
    }

    /**
       Set the <code>where</code> slot of this event.
       @param id The container identifier of the container where the
       agent is deployed.
    */
    public void setWhere(ContainerID id) {
	where = id;
    }

    /**
       Retrieve the value of the <code>where</code> slot of this
       event, containing the container identifier of the container
       where the agent is deployed.
       @return The value of the <code>where</code> slot, or
       <code>null</code> if no value was set.
    */
    public ContainerID getWhere() {
	return where;
    }

    /**
       Set the <code>agent</code> slot of this event.
       @param id The agent identifier of the agent whose ownership
       changed.
    */
    public void setAgent(AID id) {
	agent = id;
    }

    /**
       Retrieve the value of the <code>agent</code> slot of this
       event, containing the agent identifier of the agent whose
       ownership changed.
       @return The value of the <code>agent</code> slot, or
       <code>null</code> if no value was set.
    */
    public AID getAgent() {
	return agent;
    }

    /**
       Set the <code>from</code> slot of this event.
       @param o The name of the entity that owned the agent before
       this event occurred.
    */
    public void setFrom(String o) {
	from = o;
    }

    /**
       Retrieve the value of the <code>from</code> slot of this event,
       containing the name of the entity that owned the agent before
       this event occurred.
       @return The value of the <code>from</code> slot, or
       <code>null</code> if no value was set.
    */
    public String getFrom() {
	return from;
    }

    /**
       Set the <code>to</code> slot of this event.
       @param o The name of the entity owning the agent after this
       event occurred.
    */
    public void setTo(String o) {
	to = o;
    }

    /**
       Retrieve the value of the <code>to</code> slot of this
       event, containing the name of the entity owning the agent after this
       event occurred.
       @return The value of the <code>agent</code> slot, or
       <code>null</code> if no value was set.
    */
    public String getTo() {
	return to;
    }

    /**
       Retrieve the name of this event.
       @return A constant value for the event name.
    */
    public String getName() {
	return NAME;
    }

}
