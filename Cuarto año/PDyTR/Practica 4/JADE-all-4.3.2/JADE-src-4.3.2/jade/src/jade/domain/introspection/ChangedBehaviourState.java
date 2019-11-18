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
import jade.core.BehaviourID;
import jade.domain.introspection.Event;

/**

   An introspection event, recording the change of the state of an
   agent behaviour within the platform.

   @author Giovanni Rimassa -  Universita' di Parma
   @version $Date: 2008-10-09 14:04:02 +0200 (gio, 09 ott 2008) $ $Revision: 6051 $
*/

public class ChangedBehaviourState implements Event {

    /**
       A string constant for the name of this event.
    */ 
    public static final String NAME = "Changed-Behaviour-State";

    private AID agent;
    private BehaviourID behaviour;
    private String from;
    private String to;


    /**
       Default constructor. A default constructor is necessary for
       ontological classes.
    */
    public ChangedBehaviourState() {
    }

    /**
       Set the <code>agent</code> slot of this event.
       @param id The agent identifier of the agent whose behaviour
       state changed.
    */
    public void setAgent(AID id) {
	agent = id;
    }

    /**
       Retrieve the value of the <code>agent</code> slot of this
       event, containing the agent identifier of the agent whose
       behaviour state changed.
       @return The value of the <code>agent</code> slot, or
       <code>null</code> if no value was set.
    */
    public AID getAgent() {
	return agent;
    }

    /**
       Set the <code>behaviour</code> slot of this event.
       @param id The behaviour identifier of the behaviour whose state
       changed.
    */
    public void setBehaviour(BehaviourID id) {
	behaviour = id;
    }

    /**
       Retrieve the value of the <code>behaviour</code> slot of this
       event, containing the behaviour identifier of the behaviour
       whose state changed.
       @return The value of the <code>behaviour</code> slot, or
       <code>null</code> if no value was set.
    */
    public BehaviourID getBehaviour() {
	return behaviour;
    }

    /**
       Set the <code>from</code> slot of this event.
       @param s The name of the state the behaviour was in before this
       event occurred.
    */
    public void setFrom(String s) {
	from = s;
    }

    /**
       Retrieve the value of the <code>from</code> slot of this event,
       containing the name of the state the behaviour was in before
       this event occurred.
       @return The value of the <code>from</code> slot, or
       <code>null</code> if no value was set.
    */
    public String getFrom() {
	return from;
    }

    /**
       Set the <code>to</code> slot of this event.
       @param s The name of the state the behaviour was in after this
       event occurred.
    */
    public void setTo(String s) {
	to = s;
    }

    /**
       Retrieve the value of the <code>to</code> slot of this event,
       containing the name of the state the behaviour was in after
       this event occurred.
       @return The value of the <code>to</code> slot, or
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

    /**
       Retrieve an SL0-like string representation for this object.
    */
    public String toString() {
  	return NAME+ " Name: "+ behaviour.getName() + " from: " + from + " to: " + to;
    }

}
