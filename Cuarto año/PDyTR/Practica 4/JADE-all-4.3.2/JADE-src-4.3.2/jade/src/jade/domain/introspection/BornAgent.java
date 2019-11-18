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

import jade.core.AID;
import jade.core.ContainerID;


/**
   An introspection event, recording the birth of a new agent within
   the platform.

   @author Giovanni Rimassa -  Universita' di Parma
   @version $Date: 2006-12-14 17:26:48 +0100 (gio, 14 dic 2006) $ $Revision: 5916 $
 */
public class BornAgent implements Event {

	/**
       A string constant for the name of this event.
	 */
	public static final String NAME = "Born-Agent";

	private AID agent;
	private ContainerID where;
	private String state;
	private String ownership;
	private String className;


	/**
       Default constructor. A default constructor is necessary for
       ontological classes.
	 */
	public BornAgent() {
	}

	/**
       Retrieve the name of this event.
       @return A constant value for the event name.
	 */
	public String getName() {
		return NAME;
	}

	/**
       Set the <code>where</code> slot of this event.
       @param id The container identifier of the container where the
       newly added agent was born.
	 */
	public void setWhere(ContainerID id) {
		where = id;
	}

	/**
       Retrieve the value of the <code>where</code> slot of this
       event, containing the container identifier of the container
       where the newly added agent was born.
       @return The value of the <code>where</code> slot, or
       <code>null</code> if no value was set.
	 */
	public ContainerID getWhere() {
		return where;
	}

	/**
       Set the <code>agent</code> slot of this event.
       @param id The agent identifier of the newly born agent.
	 */
	public void setAgent(AID id) {
		agent = id;
	}

	/**
       Retrieve the value of the <code>agent</code> slot of this
       event, containing the agent identifier of the newly born agent.
       @return The value of the <code>agent</code> slot, or
       <code>null</code> if no value was set.
	 */
	public AID getAgent() {
		return agent;
	}

	/**
       Set the <code>state</code> slot of this event.
       @param as The name of the initial state of the newly born
       agent.
	 */
	public void setState(String as) {
		state = as;
	}

	/**
       Retrieve the value of the <code>state</code> slot of this
       event, containing the initial state of the newly born agent.
       @return The value of the <code>state</code> slot, or
       <code>null</code> if no value was set.
	 */
	public String getState() {
		return state;
	}

	/**
       Set the <code>ownership</code> slot of this event.
       @param o The name of the entity owning the newly born agent.
	 */
	public void setOwnership(String o) {
		ownership = o;
	}

	/**
       @return the value of the <code>ownership</code> slot of this
       event, containing the name of the entity owning the newly born
       agent.
	 */
	public String getOwnership() {
		return ownership;
	}

	/**
    Set the <code>className</code> slot of this event.
    @param c The class name of the newly born agent.
	 */
	public void setClassName(String c) {
		className = c;
	}

	/**
    @return the value of the <code>className</code> slot of this
    event, containing the class name of the newly born
    agent.
	 */
	public String getClassName() {
		return className;
	}
}
