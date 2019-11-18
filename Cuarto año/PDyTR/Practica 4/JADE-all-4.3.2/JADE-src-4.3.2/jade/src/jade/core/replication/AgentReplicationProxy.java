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
//#APIDOC_EXCLUDE_FILE

import jade.core.AID;
import jade.core.ContainerID;
import jade.core.GenericCommand;
import jade.core.IMTPException;
import jade.core.Location;
import jade.core.Node;
import jade.core.NotFoundException;
import jade.core.ServiceException;
import jade.core.SliceProxy;

public class AgentReplicationProxy extends SliceProxy implements AgentReplicationSlice {

	public void invokeAgentMethod(AID aid, String methodName, Object[] arguments) throws IMTPException, ServiceException, NotFoundException {
		GenericCommand cmd = new GenericCommand(H_INVOKEAGENTMETHOD, AgentReplicationService.NAME, null);
		cmd.addParam(aid);
		cmd.addParam(methodName);
		cmd.addParam(arguments);
		
		Node n = getNode();
		Object result = n.accept(cmd);
		if((result != null) && (result instanceof Throwable)) {
			if(result instanceof NotFoundException) {
				throw (NotFoundException)result;
			}
			else if(result instanceof ServiceException) {
				throw (ServiceException)result;
			}
			else if(result instanceof IMTPException) {
				throw (IMTPException)result;
			}
			else {
				throw new IMTPException("An undeclared exception was thrown", (Throwable)result);
			}
		}
	}
	
	public ContainerID getAgentLocation(AID aid) throws IMTPException, NotFoundException {
		GenericCommand cmd = new GenericCommand(H_GETAGENTLOCATION, AgentReplicationService.NAME, null);
		cmd.addParam(aid);
		
		try {
			Node n = getNode();
			Object result = n.accept(cmd);
			if ((result != null) && (result instanceof Throwable)) {
				if(result instanceof NotFoundException) {
					throw (NotFoundException)result;
				}
				else if(result instanceof IMTPException) {
					throw (IMTPException)result;
				}
				else {
					throw new IMTPException("An undeclared exception was thrown", (Throwable)result);
				}
			}
			return (ContainerID)result;
		}
		catch (ServiceException se) {
			throw new IMTPException("Error accessing remote node", se);
		}
	}
	
	public void replicaCreationRequested(AID virtualAid, AID replicaAid) throws IMTPException {
		GenericCommand cmd = new GenericCommand(H_REPLICACREATIONREQUESTED, AgentReplicationService.NAME, null);
		cmd.addParam(virtualAid);
		cmd.addParam(replicaAid);
		
		try {
			Node n = getNode();
			Object result = n.accept(cmd);
			if ((result != null) && (result instanceof Throwable)) {
				if(result instanceof IMTPException) {
					throw (IMTPException)result;
				}
				else {
					throw new IMTPException("An undeclared exception was thrown", (Throwable)result);
				}
			}
		}
		catch (ServiceException se) {
			throw new IMTPException("Error accessing remote node", se);
		}
	}

	public void synchReplication(GlobalReplicationInfo info) throws IMTPException {
		GenericCommand cmd = new GenericCommand(H_SYNCHREPLICATION, AgentReplicationService.NAME, null);
		cmd.addParam(info.getVirtual());
		cmd.addParam(info.getMaster());
		cmd.addParam(info.getReplicationMode());
		cmd.addParam(info.getAllReplicas());
		
		try {
			Node n = getNode();
			Object result = n.accept(cmd);
			if ((result != null) && (result instanceof Throwable)) {
				if(result instanceof IMTPException) {
					throw (IMTPException)result;
				}
				else {
					throw new IMTPException("An undeclared exception was thrown", (Throwable)result);
				}
			}
		}
		catch (ServiceException se) {
			throw new IMTPException("Error accessing remote node", se);
		}
	}

	public void notifyBecomeMaster(AID masterAid) throws IMTPException {
		GenericCommand cmd = new GenericCommand(H_NOTIFYBECOMEMASTER, AgentReplicationService.NAME, null);
		cmd.addParam(masterAid);
		
		try {
			Node n = getNode();
			Object result = n.accept(cmd);
			if ((result != null) && (result instanceof Throwable)) {
				if(result instanceof IMTPException) {
					throw (IMTPException)result;
				}
				else {
					throw new IMTPException("An undeclared exception was thrown", (Throwable)result);
				}
			}
		}
		catch (ServiceException se) {
			throw new IMTPException("Error accessing remote node", se);
		}
	}

	public void notifyReplicaRemoved(AID masterAid, AID removedReplica, Location where) throws IMTPException {
		GenericCommand cmd = new GenericCommand(H_NOTIFYREPLICAREMOVED, AgentReplicationService.NAME, null);
		cmd.addParam(masterAid);
		cmd.addParam(removedReplica);
		cmd.addParam(where);
		
		try {
			Node n = getNode();
			Object result = n.accept(cmd);
			if ((result != null) && (result instanceof Throwable)) {
				if(result instanceof IMTPException) {
					throw (IMTPException)result;
				}
				else {
					throw new IMTPException("An undeclared exception was thrown", (Throwable)result);
				}
			}
		}
		catch (ServiceException se) {
			throw new IMTPException("Error accessing remote node", se);
		}
	}
}
