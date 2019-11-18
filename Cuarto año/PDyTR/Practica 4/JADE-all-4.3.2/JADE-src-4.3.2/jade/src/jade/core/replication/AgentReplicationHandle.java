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

import jade.core.Agent;
import jade.core.ServiceNotActiveException;
import jade.util.Logger;

/**
 * Utility class the provides simplified access to the method-replication feature of the
 * AgentReplicationService.
 */
public class AgentReplicationHandle {
	static ThreadLocal<Boolean> replicatedCalls = new ThreadLocal<Boolean>();
	
	private static Logger myLogger = Logger.getJADELogger(AgentReplicationHandle.class.getName());
	
	/**
	 * Utility method to be called at the beginning of each replicated method as 
	 * exemplified below<br>
	 * <code>
	 * public void aReplicatedMethod(T1 a, T2 b) {
	 *   AgentReplicationHandle.replicate(this, "aReplicatedMethod", new Object[]{a, b});
	 *   
	 *   ...
	 * }
	 * </code>
	 * 
	 * @param a The agent providing the replicated method
	 * @param methodName The name of the replicated method
	 * @param arguments The arguments passed to the replicated method
	 */
	public static void replicate(Agent a, String methodName, Object[] arguments) {
		if (isReplicatedCall()) {
			// This method has been invoked within an already replicated call (see 
			// AgentReplicationService.ServiceComponent.invokeAgentMethod()) --> just do nothing
			return;
		}
		try {
			AgentReplicationHelper helper = (AgentReplicationHelper) a.getHelper(AgentReplicationHelper.SERVICE_NAME);
			helper.invokeReplicatedMethod(methodName, arguments);
		}
		catch (ServiceNotActiveException snae) {
			// AgentReplicationService not installed --> For sure this agent is not replicated --> do nothing 
		}
		catch (Exception e) {
			myLogger.log(Logger.WARNING, "Error accessing replication helper of agent "+a.getLocalName(), e);
		}
	}
	
	/**
	 * Checks whether we are inside the execution of a replicated or original call.  
	 */
	public static boolean isReplicatedCall() {
		Boolean b = replicatedCalls.get();
		return b != null && b.booleanValue();
	}
	
	static void enterReplicatedCall() {
		if (myLogger.isLoggable(Logger.FINER))
			myLogger.log(Logger.FINER, "Entering replicated call...");
		replicatedCalls.set(true);
	}
	
	static void exitReplicatedCall() {
		replicatedCalls.remove();
		if (myLogger.isLoggable(Logger.FINER))
			myLogger.log(Logger.FINER, "Exited from replicated call...");
	}
}
