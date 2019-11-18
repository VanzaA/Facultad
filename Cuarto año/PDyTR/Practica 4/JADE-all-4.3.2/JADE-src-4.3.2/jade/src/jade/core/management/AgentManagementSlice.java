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

package jade.core.management;

import jade.core.Service;
import jade.core.Filter;
import jade.core.Node;
import jade.core.AID;
import jade.core.ContainerID;
import jade.core.IMTPException;
import jade.core.NameClashException;
import jade.core.NotFoundException;
import jade.core.Command;

import jade.security.Credentials;
import jade.security.JADEPrincipal;
import jade.security.JADESecurityException;


/**

   The horizontal interface for the JADE kernel-level service managing
   the basic agent life cycle: creation, destruction, suspension and
   resumption.

   @author Giovanni Rimassa - FRAMeTech s.r.l.

 */
public interface AgentManagementSlice extends Service.Slice {

	/**
       The name of this service.
	 */
	static final String NAME = "jade.core.management.AgentManagement";

	/**
       This command name represents the <code>create-agent</code>
       action. The target agent identifier in this command is set to
       <code>null</code>, because no agent exists yet.
       This command object represents only the <i>first half</i> of
       the complete agent creation process. Even if this command is
       accepted by the kernel, there is no guarantee that the
       requested creation will ever happen. Only when the
       <code>InformCreated</code> command is issued can one assume
       that the agent creation has taken place.
	 */
	static final String REQUEST_CREATE = "Request-Create";

	/**
       This command name represents the <code>kill-agent</code>
       action.
       This command object represents only the <i>first half</i> of
       the complete agent destruction process. Even if this command is
       accepted by the kernel, there is no guarantee that the
       requested destruction will ever happen. Only when the
       <code>InformKilled</code> command is issued can one assume that
       the agent destruction has taken place.
	 */
	static final String REQUEST_KILL = "Request-Kill";

	/**
       This command name represents all agent management actions requesting
       a change in the life cycle state of their target agent
       (suspend, resume, etc.).
       This command object represents only the <i>first half</i> of
       the complete agent state change process. Even if this command
       is accepted by the kernel, there is no guarantee that the
       requested state change will ever happen. Only when the
       <code>InformStateChanged</code> command is issued can one
       assume that the state change has taken place.
	 */
	static final String REQUEST_STATE_CHANGE = "Request-State-Change";

	/**
       This command is issued by an agent that has just been created,
       and causes JADE runtime to actually start up the agent thread.
       The agent creation can be the outcome of a previously issued
       <code>RequestCreate</code> command. In that case, this command
       represents only the <i>second half</i> of the complete agent
       creation process.
	 */
	static final String INFORM_CREATED = "Inform-Created";

	/**
       This command is issued by an agent that has just been destroyed
       and whose thread is terminating.
       The agent destruction can either be an autonomous move of the
       agent or the outcome of a previously issued
       <code>RequestKill</code> command. In the second case, this
       command represents only the <i>second half</i> of the complete
       agent destruction process.

	 */
	static final String INFORM_KILLED = "Inform-Killed";

	/**
       This command is issued by an agent that has just changed its
       life-cycle state.
       The agent state change can either be an autonomous move of the
       agent or the outcome of a previously issued
       <code>RequestStateChange</code> command. In that case, this
       command represents only the <i>second half</i> of the complete
       agent state tansition process.
	 */
	static final String INFORM_STATE_CHANGED = "Inform-State-Changed";

	/**
      This command name represents the <code>shutdown-platform</code> action.
      This command has no effect but informing interested services that the platform 
      shutdown process is starting.  
	 */
	static final String SHUTDOWN_PLATFORM = "Shutdown-Platform";

	/**
       This command name represents the <code>kill-container</code> action.
	 */
	static final String KILL_CONTAINER = "Kill-Container";

	/**
       This command name represents the action of adding a new tool to
       the platform.
	 */
	static final String ADD_TOOL = "Add-Tool";

	/**
       This command name represents the action of removing an
       existing tool from the platform.
	 */
	static final String REMOVE_TOOL = "Remove-Tool";



	static final boolean CREATE_AND_START = true;
	static final boolean CREATE_ONLY = false;


	// Constants for the names of horizontal commands associated to methods
	static final String H_CREATEAGENT = "1";
	static final String H_KILLAGENT = "2";
	static final String H_CHANGEAGENTSTATE = "3";
	static final String H_BORNAGENT = "4";
	static final String H_DEADAGENT = "5";
	static final String H_SUSPENDEDAGENT = "6";
	static final String H_RESUMEDAGENT = "7";
	static final String H_EXITCONTAINER = "8";

	void createAgent(AID agentID, String className, Object arguments[], JADEPrincipal owner, Credentials initialCredentials, boolean startIt, Command sourceCmd) throws IMTPException, NotFoundException, NameClashException, JADESecurityException;
	void killAgent(AID agentID, Command sourceCmd) throws IMTPException, NotFoundException;
	void changeAgentState(AID agentID, int newState) throws IMTPException, NotFoundException;

	void bornAgent(AID name, ContainerID cid, Command sourceCmd) throws IMTPException, NameClashException, NotFoundException, JADESecurityException;
	void deadAgent(AID name, Command sourceCmd) throws IMTPException, NotFoundException;
	void suspendedAgent(AID name) throws IMTPException, NotFoundException;
	void resumedAgent(AID name) throws IMTPException, NotFoundException;

	void exitContainer() throws IMTPException, NotFoundException;

}
