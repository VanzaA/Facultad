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
 An introspection event, recording the death of an agent within the
 platform.
 
 @author Giovanni Rimassa -  Universita' di Parma
 @version $Date: 2006-02-21 08:56:26 +0100 (mar, 21 feb 2006) $ $Revision: 5860 $
 */

public class DeadAgent implements Event {
	
	/**
	 A string constant for the name of this event.
	 */
	public static final String NAME = "Dead-Agent";
	
	private AID agent;
	private ContainerID where;
	private Boolean containerRemoved;
	
	
	/**
	 Default constructor. A default constructor is necessary for
	 ontological classes.
	 */
	public DeadAgent() {
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
	 newly dead agent was deployed.
	 */
	public void setWhere(ContainerID id) {
		where = id;
	}
	
	/**
	 Retrieve the value of the <code>where</code> slot of this
	 event, containing the container identifier of the container
	 where the newly dead agent was deployed.
	 @return The value of the <code>where</code> slot, or
	 <code>null</code> if no value was set.
	 */
	public ContainerID getWhere() {
		return where;
	}
	
	/**
	 Set the <code>agent</code> slot of this event.
	 @param id The agent identifier of the newly dead agent.
	 */
	public void setAgent(AID id) {
		agent = id;
	}
	
	/**
	 Retrieve the value of the <code>agent</code> slot of this
	 event, containing the agent identifier of the newly dead agent.
	 @return The value of the <code>agent</code> slot, or
	 <code>null</code> if no value was set.
	 */
	public AID getAgent() {
		return agent;
	}
	
	public Boolean getContainerRemoved() {
		return containerRemoved;
	}
	
	public void setContainerRemoved(Boolean cr) {
		containerRemoved = cr;
	}
}
