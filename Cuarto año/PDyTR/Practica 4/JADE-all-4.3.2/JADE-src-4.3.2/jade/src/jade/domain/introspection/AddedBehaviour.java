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

/**
   An introspection event, recording that a behaviour was added to an
   agent.

   @author Giovanni Rimassa -  Universita' di Parma
   @version $Date: 2005-02-16 18:18:28 +0100 (mer, 16 feb 2005) $ $Revision: 5552 $
*/
public class AddedBehaviour implements Event {

    /**
       A string constant for the name of this event.
    */
    public static final String NAME = "Added-Behaviour";
  
    private AID agent;
    private BehaviourID behaviour;

    /**
       Default constructor. A default constructor is necessary for
       ontological classes.
    */
    public AddedBehaviour() {
    }

    /**
       Set the <code>agent</code> of this event.
       @param id The agent identifier of the agent the behaviour was
       added to.
    */
    public void setAgent(AID id) {
	agent = id;
    }

    /**
       Retrieve the value of the <code>agent</code> slot of this
       event, containing the agent identifier of the agent the
       behaviour was added to.
       @return The value of the <code>agent</code> slot, or
       <code>null</code> if no value was set.
    */
    public AID getAgent() {
	return agent;
    }

    /**
       Set the <code>behaviour</code> of this event.
       @param id The behaviour identifier of the newly added
       behaviour.
    */
    public void setBehaviour(BehaviourID id) {
	behaviour = id;
    }

    /**
       Retrieve the value of the <code>behaviour</code> slot of this
       event, containing the behaviour identifier of the newly added
       behaviour.
       @return The value of the <code>behaviour</code> slot, or
       <code>null</code> if no value was set.
    */
    public BehaviourID getBehaviour() {
	return behaviour;
    }

    /**
       Retrieve the name of this event.
       @return A constant value for the event name.
    */
    public String getName() {
	return NAME;
    }

}
