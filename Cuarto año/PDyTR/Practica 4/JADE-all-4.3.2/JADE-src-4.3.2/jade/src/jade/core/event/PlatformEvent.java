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

package jade.core.event;

import jade.core.AID;
import jade.core.ContainerID;

/**
 * This class represents an event related to the platform life cycle
 * and configuration.
 *
 * @author Giovanni Rimassa - Universita` di Parma
 * @author David I. Bell, Dick Cowan - Hewlett-Packard
 *
 * @version $Date: 2006-02-21 08:56:26 +0100 (mar, 21 feb 2006) $ $Revision: 5860 $
 */
public class PlatformEvent extends JADEEvent implements jade.wrapper.PlatformEvent {
	
	public static final int ADDED_CONTAINER = 1;
	public static final int REMOVED_CONTAINER = 2;
	public static final int MOVED_AGENT = 5;
	public static final int SUSPENDED_AGENT = 6;
	public static final int RESUMED_AGENT = 7;
	public static final int CHANGED_AGENT_PRINCIPAL = 8;
	public static final int CHANGED_CONTAINER_PRINCIPAL = 9;
	public static final int FROZEN_AGENT = 10;
	public static final int THAWED_AGENT = 11;
	
	private ContainerID newContainer = null;  // set with constructors which specify two container IDs
	private String myPlatformName = null;  // the name of the platform that generated this event
	private AID agent = null;
	private String oldOwnership = null;
	private String newOwnership = null;
	private boolean containerRemoved = false;
	
	/**
	 * This constructor is used to create a PlatformEvent when a container is
	 * added, deleted, or requested to be killed.  If the id parameter is not either
	 * {@link #ADDED_CONTAINER ADDED_CONTAINER} or
	 * {@link #REMOVED_CONTAINER REMOVED_CONTAINER},
	 * an {@link jade.domain.FIPAAgentManagement.InternalError InternalError}
	 * exception will be thrown.
	 * <p>
	 * The {@link #getContainer() getContainer()} method can be used to get
	 * the target container.
	 * The {@link #getAgent() getAgent()} and
	 * {@link #getNewContainer() getNewContainer()} methods will both return null.
	 * <p>
	 * @param id The event ID must be either
	 * {@link #ADDED_CONTAINER ADDED_CONTAINER} or
	 * {@link #REMOVED_CONTAINER REMOVED_CONTAINER}.
	 * @param eventSource The container id of the source of the event.
	 * <p>
	 * @see #getContainer()
	 */
	public PlatformEvent(int id, ContainerID eventSource) {
		super(id, eventSource);
		if(!isContainerBD()) {
			throw new InternalError("Bad event kind: it must be a container related kind.");
		}
	}
	
	/**
	 * This constructor is used to create a PlatformEvent when an agent is
	 * born or dies.  If the id parameter is not either
	 * {@link #BORN_AGENT BORN_AGENT}, {@link #DEAD_AGENT DEAD_AGENT},
	 * {@link #SUSPENDED_AGENT SUSPENDED_AGENT} or {@link #RESUMED_AGENT RESUMED_AGENT},
	 * an {@link jade.domain.FIPAAgentManagement.InternalError InternalError}
	 * exception will be thrown.
	 * <p>
	 * The {@link #getContainer() getContainer()} method can be used to get
	 * the event source.
	 * The {@link #getNewContainer() getNewContainer()} method will return null
	 * for a {@link #BORN_AGENT BORN_AGENT},
	 * {@link #DEAD_AGENT DEAD_AGENT},
	 * {@link #SUSPENDED_AGENT SUSPENDED_AGENT} or
	 * {@link #RESUMED_AGENT RESUMED_AGENT} event.
	 * <p>
	 * @param id The event ID must be either
	 * {@link #BORN_AGENT BORN_AGENT},
	 * {@link #DEAD_AGENT DEAD_AGENT},
	 * {@link #SUSPENDED_AGENT SUSPENDED_AGENT} or
	 * {@link #RESUMED_AGENT RESUMED_AGENT}.
	 * @param The {@link jade.core.AID AID} of the new or dead agent.
	 * @param eventSource The container id of the source of the event.
	 * The container ID of the container where the agent was born or died should
	 * be passed, even though the main container is the object that will acutally
	 * generate the event.
	 * <p>
	 * @see #getContainer()
	 * @see #getAgent()
	 */
	public PlatformEvent(int id, AID aid, ContainerID eventSource) {
		super(id, eventSource);
		if(!isAgentBD()) {
			throw new InternalError("Bad event kind: it must be an agent related kind.");
		}
		agent = aid;
	}
	
	public PlatformEvent(int id, AID aid, ContainerID eventSource, boolean cr) {
		super(id, eventSource);
		if(!isAgentBD()) {
			throw new InternalError("Bad event kind: it must be an agent related kind.");
		}
		agent = aid;
		containerRemoved = cr;
	}
	
	/**
	 * This constructor is used to create a PlatformEvent when an agent moves
	 * from one container to another.
	 * <p>
	 * The event source is the container from which the agent departed.
	 * The {@link #getContainer() getContainer()} method can be used to get
	 * the event source.
	 * The {@link #getNewContainer() getNewContainer()} method will return the
	 * container ID of the container to which agent is moved.
	 * <p>
	 * @param The {@link jade.core.AID AID} of the moved agent.   
	 * @param eventSource The container id from which the agent departed.
	 * @param to The container id of the container to which the agent moved.
	 * <p>
	 * @see #getContainer()
	 * @see #getNewContainer()
	 * @see #getAgent()
	 */
	public PlatformEvent(AID aid, ContainerID eventSource, ContainerID to) {
		super(MOVED_AGENT, eventSource);
		agent = aid;
		newContainer = to;
	}
	
	//#APIDOC_EXCLUDE_BEGIN
	public PlatformEvent(int id, AID aid, ContainerID from, ContainerID to) {
		super(id, from);
		agent = aid;
		newContainer = to;
	}
	//#APIDOC_EXCLUDE_END
	
	public PlatformEvent(int id, AID aid, ContainerID eventSource, String oldOwnership, String newOwnership) {
		super(id, eventSource);
		agent = aid;
		this.oldOwnership = oldOwnership;
		this.newOwnership = newOwnership;
	}
	
	/**
	 * Returns the {@link jade.core.ContainerID ContainerID} of the event source.
	 * <p>
	 * In the case of {@link #ADDED_CONTAINER ADDED_CONTAINER} or
	 * {@link #REMOVED_CONTAINER REMOVED_CONTAINER} the event source
	 * will always be the main container since the addition or deletion of a
	 * container is essentially an action in the main container.
	 * <p>
	 * In the case of the a {@link #BORN_AGENT BORN_AGENT} or
	 * {@link #DEAD_AGENT DEAD_AGENT} event, the event source
	 * will be the container where the agent was born or died.
	 * <p>
	 * In the case a {@link #MOVED_AGENT MOVED_AGENT} event, 
	 * the  event source is the container from which the agent departed.
	 * The container to which the agent moved can be retrieved using
	 * the {@link #getNewContainer() getNewContainer()} method.
	 * <p>
	 * @return The {@link jade.core.ContainerID ContainerID} of the event source.
	 * @see #getNewContainer()
	 */
	public ContainerID getContainer() {
		return (ContainerID)(getSource());
	}
	
	/**
	 * Returns the {@link jade.core.ContainerID ContainerID} of the container
	 * to which an agent moved when the event type is
	 * {@link #MOVED_AGENT MOVED_AGENT}.
	 * For all other event types, this method returns null.
	 * <p>
	 * @return The {@link jade.core.ContainerID ContainerID} of the container
	 * to which an agent moved.
	 * @see #getContainer()
	 */
	public ContainerID getNewContainer() {
		return newContainer;
	}
	
	/**
	 * Returns the {@link jade.core.AID AID} of the agent that was born,
	 * died, or moved.  If the event type is either
	 * {@link #ADDED_CONTAINER ADDED_CONTAINER} or
	 * {@link #REMOVED_CONTAINER REMOVED_CONTAINER},
	 * this method returns null since there is no agent
	 * involved.
	 * <p>
	 * @return The {@link jade.core.AID AID} of the agent that was born,
	 * died, or moved.
	 * @see #getContainer()
	 */
	public AID getAgent() {
		return agent;
	}
	
	public String getOldOwnership() {
		return oldOwnership;
	}
	
	public String getNewOwnership() {
		return newOwnership;
	}
	
	public boolean getContainerRemoved() {
		return containerRemoved;
	}
	
	/**
	 * Returns a boolean to indicate if the event type is either
	 * {@link #ADDED_CONTAINER ADDED_CONTAINER}
	 * or {@link #REMOVED_CONTAINER REMOVED_CONTAINER}.
	 * <p>
	 * @return True if the event type is either
	 * {@link #ADDED_CONTAINER ADDED_CONTAINER} or
	 * {@link #REMOVED_CONTAINER REMOVED_CONTAINER},
	 * otherwise, false is returned.
	 */
	public boolean isContainerBD() {
		return (type == ADDED_CONTAINER) || (type == REMOVED_CONTAINER);
	}
	
	/**
	 * Returns a boolean to indicate if the event type is either
	 * {@link #BORN_AGENT BORN_AGENT} or
	 * {@link #DEAD_AGENT DEAD_AGENT}.
	 * <p>
	 * @return True if the event type is either
	 * {@link #BORN_AGENT BORN_AGENT} or
	 * {@link #DEAD_AGENT DEAD_AGENT}, otherwise, false is returned.
	 */
	public boolean isAgentBD() {
		return (type == BORN_AGENT) || (type == DEAD_AGENT) ||
		(type == SUSPENDED_AGENT) || (type == RESUMED_AGENT) || (type == FROZEN_AGENT);
	}
	
	/**
	 * Returns the event type which will be one of
	 * {@link #ADDED_CONTAINER ADDED_CONTAINER},
	 * {@link #REMOVED_CONTAINER REMOVED_CONTAINER},
	 * {@link #BORN_AGENT BORN_AGENT},
	 * {@link #DEAD_AGENT DEAD_AGENT},
	 * {@link #SUSPENDED_AGENT SUSPENDED_AGENT},
	 * {@link #RESUMED_AGENT RESUMED_AGENT}, or
	 * {@link #MOVED_AGENT MOVED_AGENT}.
	 * <p>
	 * @return The event type.
	 */
	public int getEventType() {
		return type;
	}
	
	/**
	 * 
	 */
	public void setSource(Object s) {
		source = s;
		if (source instanceof jade.wrapper.PlatformController) {
			myPlatformName = ((jade.wrapper.PlatformController) source).getName();
		}
	}
	
	public String getPlatformName() {
		return myPlatformName;
	}
	
	public String getAgentGUID() {
		if (agent != null) {
			return agent.getName();
		}
		else {
			return null;
		}
	}
	
	/**
	 * Returns a string representation of this event.
	 *<p>
	 * @return A sring representation of this event.
	 */
	public String toString()
	{
		StringBuffer buf = new StringBuffer(128);
		
		buf.append("PlatformEvent[");
		switch (type) {
		case ADDED_CONTAINER:
			buf.append("add container: ").append(getSource());
			break;
		case REMOVED_CONTAINER:
			buf.append("remove container: ").append(getSource());
			break;
		case BORN_AGENT:
			buf.append("born agent: ").append(agent)
			.append(" in ").append(getSource());
			break;
		case DEAD_AGENT:
			buf.append("dead agent: ").append(agent)
			.append(" in ").append(getSource());
			break;
		case SUSPENDED_AGENT:
			buf.append("suspended agent: ").append(agent)
			.append(" in ").append(getSource());
			break;
		case RESUMED_AGENT:
			buf.append("resumed agent: ").append(agent)
			.append(" in ").append(getSource());
			break;
		case MOVED_AGENT:
			buf.append("moved agent: ").append(agent)
			.append(" from: ").append(getSource())
			.append(" to: ").append(newContainer);
			break;
//			__SECURITY__BEGIN
		case CHANGED_AGENT_PRINCIPAL:
			buf.append("changed agent ownership: ").append(agent)
			.append(" in: ").append(getSource())
			.append(" from: ").append(oldOwnership)
			.append(" to: ").append(newOwnership);
			break;
		case CHANGED_CONTAINER_PRINCIPAL:
			buf.append("changed container ownership: ").append(getSource())
			.append(" from: ").append(oldOwnership)
			.append(" to: ").append(newOwnership);
			break;
//			__SECURITY__END
		default:
			// This should never happen, but just in case...
			buf.append("Error: bad event type ID in PlatformEvent.toString()");
		break;
		} // END switch(myID)
		
		buf.append("]");
		return buf.toString();
	} // END toString()
	
} 
