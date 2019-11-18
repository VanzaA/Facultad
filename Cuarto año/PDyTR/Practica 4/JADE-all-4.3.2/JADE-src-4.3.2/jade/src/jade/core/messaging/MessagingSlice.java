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

package jade.core.messaging;

import java.util.Hashtable;

import jade.core.Service;
import jade.core.AID;
import jade.core.ContainerID;
import jade.core.IMTPException;
import jade.core.ServiceException;
import jade.core.NotFoundException;
import jade.security.JADESecurityException;
import jade.util.leap.List;
import jade.util.leap.Map;

import jade.mtp.MTPDescriptor;
import jade.mtp.MTPException;

import jade.domain.FIPAAgentManagement.Envelope;

/**
   The horizontal interface for the JADE kernel-level service managing
   the message passing subsystem installed in the platform.

   @author Giovanni Rimassa - FRAMeTech s.r.l.
 */
public interface MessagingSlice extends Service.Slice {

	// Constants for the names of the service vertical commands

	/**
     The name of this service.
	 */
	static final String NAME = "jade.core.messaging.Messaging";

	/**
     This command name represents the action of sending an ACL message from an agent to another.
	 */
	static final String SEND_MESSAGE = "Send-Message";

	/**
     This command name represents the action of sending back a FAILURE ACL message to notify the 
     message originator of a failed delivery.
	 */
	static final String NOTIFY_FAILURE = "Notify-Failure";

	/**
     This command name represents the <code>install-mtp</code> action.
	 */
	static final String INSTALL_MTP = "Install-MTP";

	/**
     This command name represents the <code>uninstall-mtp</code> action.
	 */
	static final String UNINSTALL_MTP = "Uninstall-MTP";

	/**
     This command name represents the <code>new-mtp</code> event.
	 */
	static final String NEW_MTP = "New-MTP";

	/**
     This command name represents the <code>dead-mtp</code> action.
	 */
	static final String DEAD_MTP = "Dead-MTP";

	/**
     This command name represents the <code>set-platform-addresses</code> action.
	 */
	static final String SET_PLATFORM_ADDRESSES = "Set-Platform-Addresses";



	// Constants for the names of horizontal commands associated to methods
	static final String H_DISPATCHLOCALLY = "1";
	static final String H_ROUTEOUT = "2";
	static final String H_GETAGENTLOCATION = "3";
	static final String H_INSTALLMTP = "4";
	static final String H_UNINSTALLMTP ="5";
	static final String H_NEWMTP = "6";
	static final String H_DEADMTP = "7";
	static final String H_ADDROUTE = "8";
	static final String H_REMOVEROUTE = "9";
	static final String H_NEWALIAS = "10";
	static final String H_DEADALIAS = "11";
	static final String H_CURRENTALIASES = "12";
	static final String H_TRANSFERLOCALALIASES = "13";

	void dispatchLocally(AID senderAID, GenericMessage msg, AID receiverID) throws IMTPException, NotFoundException, JADESecurityException;
	void routeOut(Envelope env, byte[] payload, AID receiverID, String address) throws IMTPException, MTPException;
	ContainerID getAgentLocation(AID agentID) throws IMTPException, NotFoundException;

	MTPDescriptor installMTP(String address, String className) throws IMTPException, ServiceException, MTPException;
	void uninstallMTP(String address) throws IMTPException, ServiceException, NotFoundException, MTPException;

	void newMTP(MTPDescriptor mtp, ContainerID cid) throws IMTPException, ServiceException;
	void deadMTP(MTPDescriptor mtp, ContainerID cid) throws IMTPException, ServiceException;

	void addRoute(MTPDescriptor mtp, String sliceName) throws IMTPException, ServiceException;
	void removeRoute(MTPDescriptor mtp, String sliceName) throws IMTPException, ServiceException;

	void newAlias(AID alias, AID agent) throws IMTPException;
	void deadAlias(AID alias) throws IMTPException;
	void currentAliases(Hashtable aliases) throws IMTPException;
	void transferLocalAliases(AID agent, List aliases) throws IMTPException;
}
