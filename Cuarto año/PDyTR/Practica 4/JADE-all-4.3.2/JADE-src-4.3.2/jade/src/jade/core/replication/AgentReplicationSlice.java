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
import jade.core.IMTPException;
import jade.core.Location;
import jade.core.NotFoundException;
import jade.core.Service.Slice;
import jade.core.ServiceException;

public interface AgentReplicationSlice extends Slice {

	static final String H_INVOKEAGENTMETHOD = "I";
	static final String H_GETAGENTLOCATION = "G";
	static final String H_REPLICACREATIONREQUESTED = "R";
	static final String H_SYNCHREPLICATION = "S";
	
	static final String H_NOTIFYBECOMEMASTER = "NB";
	static final String H_NOTIFYREPLICAREMOVED = "NR";
	
	// NOTE: These HCommands are always broadcasted --> no need to a dedicated method
	static final String H_NEWVIRTUALAGENT = "NE";
	static final String H_ADDREPLICA = "A";
	static final String H_MASTERREPLICACHANGED = "M";
	static final String H_VIRTUALAGENTDEAD = "V";
	

	void invokeAgentMethod(AID aid, String methodName, Object[] arguments) throws IMTPException, ServiceException, NotFoundException;
	// FIXME: Refactor with MessagingService.getAgentLocation()?
	ContainerID getAgentLocation(AID aid) throws IMTPException, NotFoundException;
	// This is used to notify a slice that a replica of a given virtual agent is going to
	// be created there. This is necessary since the newly created replica agent may 
	// access its AgentReplicationHelper right in the afterClone() method and, at that time, 
	// the slice of the new replica may not have been notified yet about the 
	// new-replica --> virtual-agent correspondence. This in facts is done by the master
	// replica slice only after the successful completion of the master cloning process.
	void replicaCreationRequested(AID virtualAid, AID replicaAid) throws IMTPException;
	void synchReplication(GlobalReplicationInfo info) throws IMTPException;
	
	void notifyBecomeMaster(AID masterAid) throws IMTPException;
	void notifyReplicaRemoved(AID masterAid, AID removedReplica, Location where) throws IMTPException;
}
