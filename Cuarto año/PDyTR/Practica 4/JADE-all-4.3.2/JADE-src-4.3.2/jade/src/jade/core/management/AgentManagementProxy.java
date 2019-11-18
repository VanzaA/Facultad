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
import jade.core.SliceProxy;
import jade.core.Filter;
import jade.core.Node;
import jade.core.AID;
import jade.core.ContainerID;
import jade.core.GenericCommand;
import jade.core.Command;
import jade.core.IMTPException;
import jade.core.ServiceException;
import jade.core.NameClashException;
import jade.core.NotFoundException;

import jade.security.JADEPrincipal;
import jade.security.Credentials;
import jade.security.JADESecurityException;

/**

   The remote proxy for the JADE kernel-level service managing
   the basic agent life cycle: creation, destruction, suspension and
   resumption.

   @author Giovanni Rimassa - FRAMeTech s.r.l.

 */
public class AgentManagementProxy extends SliceProxy implements AgentManagementSlice {

	public void createAgent(AID agentID, String className, Object arguments[], JADEPrincipal owner, Credentials initialCredentials, boolean startIt, Command sourceCmd) throws IMTPException, NotFoundException, NameClashException, JADESecurityException {
		try {
			GenericCommand cmd = new GenericCommand(H_CREATEAGENT, AgentManagementSlice.NAME, null);
			cmd.addParam(agentID);
			cmd.addParam(className);
			cmd.addParam(arguments);
			cmd.addParam(owner);
			cmd.addParam(initialCredentials);
			cmd.addParam(new Boolean(startIt));
			cmd.setPrincipal(sourceCmd.getPrincipal());
			cmd.setCredentials(sourceCmd.getCredentials());

			Node n = getNode();
			Object result = n.accept(cmd);
			if((result != null) && (result instanceof Throwable)) {
				if(result instanceof IMTPException) {
					throw (IMTPException)result;
				}
				else if(result instanceof NotFoundException) {
					throw (NotFoundException)result;
				}
				else if(result instanceof NameClashException) {
					throw (NameClashException)result;
				}
				else if(result instanceof JADESecurityException) {
					throw (JADESecurityException)result;
				}
				else {
					throw new IMTPException("An undeclared exception was thrown", (Throwable)result);
				}
			}
		}
		catch(ServiceException se) {
			throw new IMTPException("Unable to access remote node", se);
		}
	}

	public void killAgent(AID agentID, Command sourceCmd) throws IMTPException, NotFoundException {
		try {
			GenericCommand cmd = new GenericCommand(H_KILLAGENT, AgentManagementSlice.NAME, null);
			cmd.addParam(agentID);
			cmd.setPrincipal(sourceCmd.getPrincipal());
			cmd.setCredentials(sourceCmd.getCredentials());

			Node n = getNode();
			Object result = n.accept(cmd);
			if((result != null) && (result instanceof Throwable)) {
				if(result instanceof IMTPException) {
					throw (IMTPException)result;
				}
				else if(result instanceof NotFoundException) {
					throw (NotFoundException)result;
				}
				else {
					throw new IMTPException("An undeclared exception was thrown", (Throwable)result);
				}
			}
		}
		catch(ServiceException se) {
			throw new IMTPException("Unable to access remote node", se);
		}
	}

	public void changeAgentState(AID agentID, int newState) throws IMTPException, NotFoundException {
		try {
			GenericCommand cmd = new GenericCommand(H_CHANGEAGENTSTATE, AgentManagementSlice.NAME, null);
			cmd.addParam(agentID);
			cmd.addParam(new Integer(newState));

			Node n = getNode();
			Object result = n.accept(cmd);
			if((result != null) && (result instanceof Throwable)) {
				if(result instanceof IMTPException) {
					throw (IMTPException)result;
				}
				else if(result instanceof NotFoundException) {
					throw (NotFoundException)result;
				}
				else {
					throw new IMTPException("An undeclared exception was thrown", (Throwable)result);
				}
			}
		}
		catch(ServiceException se) {
			throw new IMTPException("Unable to access remote node", se);
		}
	}

	public void bornAgent(AID name, ContainerID cid, Command sourceCmd) throws IMTPException, NameClashException, NotFoundException, JADESecurityException {
		try {
			GenericCommand cmd = new GenericCommand(H_BORNAGENT, AgentManagementSlice.NAME, null);
			cmd.addParam(name);
			cmd.addParam(cid);
			cmd.setPrincipal(sourceCmd.getPrincipal());
			cmd.setCredentials(sourceCmd.getCredentials());

			Node n = getNode();
			Object result = n.accept(cmd);
			if((result != null) && (result instanceof Throwable)) {
				if(result instanceof IMTPException) {
					throw (IMTPException)result;
				}
				else if(result instanceof NotFoundException) {
					throw (NotFoundException)result;
				}
				else if(result instanceof NameClashException) {
					throw (NameClashException)result;
				}
				else if(result instanceof JADESecurityException) {
					throw (JADESecurityException)result;
				}
				else {
					throw new IMTPException("An undeclared exception was thrown", (Throwable)result);
				}
			}
		}
		catch(ServiceException se) {
			throw new IMTPException("Unable to access remote node", se);
		}
	}

	public void deadAgent(AID name, Command sourceCmd) throws IMTPException, NotFoundException {
		try {
			GenericCommand cmd = new GenericCommand(H_DEADAGENT, AgentManagementSlice.NAME, null);
			cmd.addParam(name);
			cmd.setPrincipal(sourceCmd.getPrincipal());
			cmd.setCredentials(sourceCmd.getCredentials());

			Node n = getNode();
			Object result = n.accept(cmd);
			if((result != null) && (result instanceof Throwable)) {
				if(result instanceof IMTPException) {
					throw (IMTPException)result;
				}
				else if(result instanceof NotFoundException) {
					throw (NotFoundException)result;
				}
				else {
					throw new IMTPException("An undeclared exception was thrown", (Throwable)result);
				}
			}
		}
		catch(ServiceException se) {
			throw new IMTPException("Unable to access remote node", se);
		}
	}

	public void suspendedAgent(AID name) throws IMTPException, NotFoundException {
		try {
			GenericCommand cmd = new GenericCommand(H_SUSPENDEDAGENT, AgentManagementSlice.NAME, null);
			cmd.addParam(name);

			Node n = getNode();
			Object result = n.accept(cmd);
			if((result != null) && (result instanceof Throwable)) {
				if(result instanceof IMTPException) {
					throw (IMTPException)result;
				}
				else if(result instanceof NotFoundException) {
					throw (NotFoundException)result;
				}
				else {
					throw new IMTPException("An undeclared exception was thrown", (Throwable)result);
				}
			}
		}
		catch(ServiceException se) {
			throw new IMTPException("Unable to access remote node", se);
		}
	}

	public void resumedAgent(AID name) throws IMTPException, NotFoundException {
		try {
			GenericCommand cmd = new GenericCommand(H_RESUMEDAGENT, AgentManagementSlice.NAME, null);
			cmd.addParam(name);

			Node n = getNode();
			Object result = n.accept(cmd);
			if((result != null) && (result instanceof Throwable)) {
				if(result instanceof IMTPException) {
					throw (IMTPException)result;
				}
				else if(result instanceof NotFoundException) {
					throw (NotFoundException)result;
				}
				else {
					throw new IMTPException("An undeclared exception was thrown", (Throwable)result);
				}
			}
		}
		catch(ServiceException se) {
			throw new IMTPException("Unable to access remote node", se);
		}
	}

	public void exitContainer() throws IMTPException, NotFoundException {
		try {
			GenericCommand cmd = new GenericCommand(H_EXITCONTAINER, AgentManagementSlice.NAME, null);

			Node n = getNode();
			Object result = n.accept(cmd);
			if((result != null) && (result instanceof Throwable)) {
				if(result instanceof IMTPException) {
					throw (IMTPException)result;
				}
				else if(result instanceof NotFoundException) {
					throw (NotFoundException)result;
				}
				else {
					throw new IMTPException("An undeclared exception was thrown", (Throwable)result);
				}
			}
		}
		catch(ServiceException se) {
			throw new IMTPException("Unable to access remote node", se);
		}
	}



}
