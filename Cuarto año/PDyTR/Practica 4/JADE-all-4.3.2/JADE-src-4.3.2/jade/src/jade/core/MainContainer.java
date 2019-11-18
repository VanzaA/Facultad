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


import jade.mtp.MTPDescriptor;

import jade.security.JADEPrincipal;

import jade.util.leap.List;

/**
   @author Giovanni Rimassa - Universita' di Parma
   @version $Date: 2006-02-21 08:56:26 +0100 (mar, 21 feb 2006) $ $Revision: 5860 $
*/

public interface MainContainer {

    void bornAgent(AID name, ContainerID cid, JADEPrincipal principal, String ownership, boolean forceReplacement) throws NameClashException, NotFoundException;
    void deadAgent(AID name, boolean containerRemoved) throws NotFoundException;
    void suspendedAgent(AID name) throws NotFoundException;
    void resumedAgent(AID name) throws NotFoundException;
  	void movedAgent(AID agentID, ContainerID from, ContainerID to) throws NotFoundException;
    void frozenAgent(AID name, ContainerID bufferContainer) throws NotFoundException;
    void thawedAgent(AID name, ContainerID bufferContainer) throws NotFoundException;

    void newMTP(MTPDescriptor mtp, ContainerID cid) throws IMTPException;
    void deadMTP(MTPDescriptor mtp, ContainerID cid) throws IMTPException;

    void toolAdded(AID tool);
    void toolRemoved(AID tool);

    ContainerID[] containerIDs();
    AID[] agentNames();
    List containerMTPs(ContainerID cid) throws NotFoundException;
    List containerAgents(ContainerID cid) throws NotFoundException;
    ContainerID getContainerID(AID agentID) throws NotFoundException;
    NodeDescriptor getContainerNode(ContainerID cid) throws NotFoundException;
    
    //#MIDP_EXCLUDE_BEGIN
    AgentDescriptor acquireAgentDescriptor(AID agentID);
    void releaseAgentDescriptor(AID agentID);
    //#MIDP_EXCLUDE_END

}
