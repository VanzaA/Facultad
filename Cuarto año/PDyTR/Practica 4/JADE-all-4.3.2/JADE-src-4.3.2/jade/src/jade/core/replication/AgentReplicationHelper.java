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

package jade.core.replication;

//#J2ME_EXCLUDE_FILE

import java.util.Map;

import jade.core.AID;
import jade.core.Location;
import jade.core.ServiceException;
import jade.core.ServiceHelper;

/**
 * Helper class that allows agents to exploit the features of the AGentReplicationService
 * @see jade.core.Agent#getHelper(String)
 */
public interface AgentReplicationHelper extends ServiceHelper {
	/**
	 * The name of the AgentReplicationService that must be specified in the 
	 * getHelper() method of the Agent class to retrieve the AgentReplicationHelper.
	 */
	static final String SERVICE_NAME = "jade.core.replication.AgentReplication";
	
	/**
	 * The constant indicating that replicas of a virtual agent are treated as 
	 * hot resources. That is messages directed to the virtual agent are delivered 
	 * indifferently to one of the available replica
	 */
	static final int HOT_REPLICATION = 1;
	/**
	 * The constant indicating that replicas of a virtual agent are treated as 
	 * cold resources. That is messages directed to the virtual agent are always 
	 * delivered to the master replica. Other replica(s) are only there for backup 
	 * purposes
	 */
	static final int COLD_REPLICATION = 2;
	
	/**
	 * ACL Message user defined parameter key specifying the local name of the virtual agent 
	 * the message was sent to. This is automatically set by the AgentReplicationService when 
	 * resolving a virtual agent receiver to one of its implementation replicas
	 */
	static final String VIRTUAL_RECEIVER = "JADE-virtual-receiver";
	
	/**
	 * The interface to be implemented by a replicated agent for the master replica
	 * to be notified about replica addition/removal and master replica changes.
	 * If a replicated agent does not implement this interface such events will not be 
	 * notified.
	 */
	static interface Listener {
		/**
		 * Notify the master replica that a new replica has just been added
		 */
		void replicaAdded(AID replicaAid, Location where);
		/**
		 * Notify the master replica that a replica has just been removed
		 */
		void replicaRemoved(AID replicaAid, Location where);
		/**
		 * Notify the master replica that a replica failed to start
		 */
		void replicaCreationFailed(AID replicaAid, Location where);
		/**
		 * Notify a replica that it became the new master replica
		 */
		void becomeMaster();
	}


	/**
	 * Define a virtual agent and sets the agent that invokes this method as 
	 * its master replica.
	 * @param virtualName The name of the virtual agent.
	 * @param replicationMode Indicates whether replicas of the defined virtual agent must be 
	 * treated as hot or cold resources.
	 * @return The AID of the newly defined virtual agent
	 * @throws ServiceException If an error occurs
	 */
	AID makeVirtual(String virtualName, int replicationMode) throws ServiceException;
	
	/**
	 * Create a new replica of a previously defined virtual agent. Note that the 
	 * actual replica creation process occurs asynchronously. The master replica
	 * invoking this method is notified about successful or unsuccessful replica 
	 * creation by means of the replicaAdded() and replicaCreationFailed() methods
	 * of the Listener interface.
	 * @param replicaName The name of the new replica agent
	 * @param where The location where the new replica agent must be cloned
	 * @throws ServiceException If the agent invoking this method did not define a 
	 * virtual agent yet, it is not the master replica or an error occurs. 
	 */
	void createReplica(String replicaName, Location where) throws ServiceException;
	
	/**
	 * Returns the AID of the virtual agent previously defined by means of the makeVirtual() 
	 * method or null if no virtual agent has been defined.
	 * @return The AID of the virtual agent previously defined by means of the makeVirtual() 
	 * method or null if no virtual agent has been defined.
	 */
	AID getVirtualAid();
	/**
	 * Returns the AID of the master replica agent
	 * @return The AID of the master replica agent
	 */
	AID getMasterAid();
	/**
	 * Returns true if this agent is the master replica. false Otherwise
	 * @return true if this agent is the master replica. false Otherwise
	 */
	boolean isMaster(); 
	/**
	 * Returns a Map mapping all replicas to their Location 
	 * @return a Map mapping all replicas to their Location
	 */
	Map<AID, Location> getReplicas(); 

	/**
	 * Invoke a method on all other replicas. This method should not be invoked 
	 * directly. AgentReplicationHanlde.replicate() should be used instead.
	 */
	void invokeReplicatedMethod(String methodName, Object[] arguments);
}
