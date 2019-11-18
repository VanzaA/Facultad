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

package jade.core.mobility;

//#MIDP_EXCLUDE_FILE

import jade.core.Node;
import jade.core.Service;
import jade.core.SliceProxy;
import jade.core.Filter;
import jade.core.GenericCommand;
import jade.core.AID;
import jade.core.ContainerID;
import jade.core.Location;
import jade.core.IMTPException;
import jade.core.ServiceException;
import jade.core.NotFoundException;
import jade.core.NameClashException;
import jade.core.management.AgentManagementSlice;

import jade.security.Credentials;
import jade.security.JADESecurityException;

import jade.util.leap.List;

/**

   The remote proxy for the JADE kernel-level service managing
   the mobility-related agent life cycle: migration and clonation.

   @author Giovanni Rimassa - FRAMeTech s.r.l.
 */
public class AgentMobilityProxy extends SliceProxy implements AgentMobilitySlice {


	public void createAgent(AID agentID, byte[] serializedInstance, String classSiteName, boolean isCloned, boolean startIt) throws IMTPException, ServiceException, NotFoundException, NameClashException, JADESecurityException {
		try {
			GenericCommand cmd = new GenericCommand(H_CREATEAGENT, AgentMobilitySlice.NAME, null);
			cmd.addParam(agentID);
			cmd.addParam(serializedInstance);
			cmd.addParam(classSiteName);
			cmd.addParam(new Boolean(isCloned));
			cmd.addParam(new Boolean(startIt));


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

	public byte[] fetchClassFile(String className, String agentName) throws IMTPException, ClassNotFoundException {
		try {
			GenericCommand cmd = new GenericCommand(H_FETCHCLASSFILE, AgentMobilitySlice.NAME, null);
			cmd.addParam(className);
			cmd.addParam(agentName);


			Node n = getNode();
			Object result = n.accept(cmd);
			if((result != null) && (result instanceof Throwable)) {
				if(result instanceof IMTPException) {
					throw (IMTPException)result;
				}
				else if(result instanceof ClassNotFoundException) {
					throw (ClassNotFoundException)result;
				}
				else {
					throw new IMTPException("An undeclared exception was thrown", (Throwable)result);
				}
			}
			return (byte[])result;
		}
		catch(ServiceException se) {
			throw new IMTPException("Unable to access remote node", se);
		}
	}

	public void moveAgent(AID agentID, Location where) throws IMTPException, NotFoundException {
		try {
			GenericCommand cmd = new GenericCommand(H_MOVEAGENT, AgentMobilitySlice.NAME, null);
			cmd.addParam(agentID);
			cmd.addParam(where);


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

	public void copyAgent(AID agentID, Location where, String newName) throws IMTPException, NotFoundException {
		try {
			GenericCommand cmd = new GenericCommand(H_COPYAGENT, AgentMobilitySlice.NAME, null);
			cmd.addParam(agentID);
			cmd.addParam(where);
			cmd.addParam(newName);


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

	public boolean prepare() throws IMTPException {
		try {
			GenericCommand cmd = new GenericCommand(H_PREPARE, AgentMobilitySlice.NAME, null);


			Node n = getNode();
			Object result = n.accept(cmd);
			if((result != null) && (result instanceof Throwable)) {
				if(result instanceof IMTPException) {
					throw (IMTPException)result;
				}
				else {
					throw new IMTPException("An undeclared exception was thrown", (Throwable)result);
				}
			}

			return ((Boolean)result).booleanValue();
		}
		catch(ServiceException se) {
			throw new IMTPException("Unable to access remote node", se);
		}
	}

	public boolean transferIdentity(AID agentID, Location src, Location dest) throws IMTPException, NotFoundException {
		try {
			GenericCommand cmd = new GenericCommand(H_TRANSFERIDENTITY, AgentMobilitySlice.NAME, null);
			cmd.addParam(agentID);
			cmd.addParam(src);
			cmd.addParam(dest);


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

			return ((Boolean)result).booleanValue();
		}
		catch(ServiceException se) {
			throw new IMTPException("Unable to access remote node", se);
		}
	}

	public void handleTransferResult(AID agentID, boolean result, List messages) throws IMTPException, NotFoundException {
		try {
			GenericCommand cmd = new GenericCommand(H_HANDLETRANSFERRESULT, AgentMobilitySlice.NAME, null);
			cmd.addParam(agentID);
			cmd.addParam(new Boolean(result));
			cmd.addParam(messages);


			Node n = getNode();
			Object res = n.accept(cmd);
			if((res != null) && (res instanceof Throwable)) {
				if(res instanceof IMTPException) {
					throw (IMTPException)res;
				}
				else if(res instanceof NotFoundException) {
					throw (NotFoundException)res;
				}
				else {
					throw new IMTPException("An undeclared exception was thrown", (Throwable)res);
				}
			}
		}
		catch(ServiceException se) {
			throw new IMTPException("Unable to access remote node", se);
		}
	}

	public void clonedAgent(AID agentID, ContainerID cid, Credentials creds) throws IMTPException, JADESecurityException, NotFoundException, NameClashException {
		try {
			GenericCommand cmd = new GenericCommand(H_CLONEDAGENT, AgentMobilitySlice.NAME, null);
			cmd.addParam(agentID);
			cmd.addParam(cid);
			cmd.addParam(creds);


			Node n = getNode();
			Object result = n.accept(cmd);
			if((result != null) && (result instanceof Throwable)) {
				if(result instanceof IMTPException) {
					throw (IMTPException)result;
				}
				else if(result instanceof JADESecurityException) {
					throw (JADESecurityException)result;
				}
				else if(result instanceof NotFoundException) {
					throw (NotFoundException)result;
				}
				else if(result instanceof NameClashException) {
					throw (NameClashException)result;
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

	//#J2ME_EXCLUDE_BEGIN
	public void cloneCodeLocatorEntry(AID oldAgentID, AID newAgentID) throws IMTPException, NotFoundException {
		try {
			GenericCommand cmd = new GenericCommand(H_CLONECODELOCATORENTRY, AgentMobilitySlice.NAME, null);
			cmd.addParam(oldAgentID);
			cmd.addParam(newAgentID);

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

	public void removeCodeLocatorEntry(AID name) throws IMTPException, NotFoundException {
		try {
			GenericCommand cmd = new GenericCommand(H_REMOVECODELOCATORENTRY, AgentMobilitySlice.NAME, null);
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
	//#J2ME_EXCLUDE_END
}
