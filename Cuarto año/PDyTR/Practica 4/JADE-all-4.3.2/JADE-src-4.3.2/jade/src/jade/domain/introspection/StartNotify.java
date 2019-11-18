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

import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;

//#APIDOC_EXCLUDE_FILE

import jade.core.AID;

import jade.content.AgentAction;

/**

  This class represents the <code>start-notify</code> action,
  requesting to start a continuous notification of some events via ACL
  messages.

  @author Giovanni Rimassa - Universita' di Parma
  @version $Date: 2005-02-16 18:18:28 +0100 (mer, 16 feb 2005) $ $Revision: 5552 $

*/
public class StartNotify implements AgentAction {

    private AID observed;
    private List events = new ArrayList();

    /**
       Default constructor. A default constructor is necessary for
       ontological classes.
    */
    public StartNotify() {
    }

    /**
       Set the <code>observed</code> slot of this action.
       @param id The agent identifier of the agent whose events will
       be notified from now on.
    */
    public void setObserved(AID id) {
	observed = id;
    }

    /**
       Retrieve the value of the <code>observed</code> slot of this
       event, containing the agent identifier of the agent whose
       events will be notified from now on.
       @return The value of the <code>observed</code> slot, or
       <code>null</code> if no value was set.
    */
    public AID getObserved() {
	return observed;
    }

    /**
       Add an event name to the <code>events</code> slot collection of
       this object.
       @param evName The event name to add to the collection.
    */
    public void addEvents(String evName) {
	events.add(evName);
    }

    /**
       Remove all event names from the <code>events</code> slot
       collection of this object.
    */
    public Iterator getAllEvents() {
	return events.iterator();
    }


}
