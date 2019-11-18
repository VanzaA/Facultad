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

package jade.core;

//#APIDOC_EXCLUDE_FILE


import jade.lang.acl.ACLMessage;
import jade.security.JADESecurityException;

/**
@author Giovanni Caire - TILAB
 */

public interface BackEnd {
	/**
	   Notify the BackEnd container that a new agent is born.
	   @param name The name of the new agent.
	   @return The name actually assigned to the newly born agent.
	 */
	String bornAgent(String name) throws IMTPException, JADESecurityException;

	/**
	   Notify the BackEnd container that an agent has died. 
	   Note that this does not throw NotFoundException as, if the 
	   BackEnd does not contain an AgentImage for the deadAgent it 
	   just ignores the call.
	   @param name The name of the dead agent.
	 */
	void deadAgent(String name) throws IMTPException;

	/**
	   Notify the BackEnd container that an agent has suspended.
	   @param name The name of the suspended agent.
	 */
	void suspendedAgent(String name) throws NotFoundException, IMTPException;

	/**
	   Notify the BackEnd container that an agent has resumed.
	   @param name The name of the resumed agent.
	 */
	void resumedAgent(String name) throws NotFoundException, IMTPException;

	/**
	   Pass an ACLMessage to the BackEnd for dispatching.
	   @param msg The message to be dispatched.
	   @param sender The name of the sender agent.
	 */
	void messageOut(ACLMessage msg, String sender) throws NotFoundException, IMTPException;  

	/**
	 * Pass a service helper method invocation request to the BackEnd
	 * @param actor The name of the agent that invoked the method
	 * @param serviceName The name of the service
	 * @param methodName The name of the invoked method
	 * @param methodParams The parameters of the invoked methods as an array of Objects
	 * @return the result of the invoked method or <code>null</code> if the method returns <code>void</code>
	 */
	Object serviceInvokation(String actor, String serviceName, String methodName, Object[] methodParams) throws NotFoundException, ServiceException, IMTPException;
}

