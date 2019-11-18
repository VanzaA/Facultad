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

package jade.domain;

//#MIDP_EXCLUDE_FILE

import jade.core.CaseInsensitiveString;
import jade.core.Agent;
import jade.core.AID;
import jade.core.Location;
import jade.core.ServiceException;
import jade.content.Concept;
import jade.content.Predicate;
import jade.content.onto.basic.Action;
import jade.content.onto.basic.Result;
import jade.content.onto.basic.Done;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.leap.ArrayList;
import jade.util.leap.List;
import jade.domain.JADEAgentManagement.*;
import jade.domain.FIPAAgentManagement.UnsupportedFunction;
import jade.domain.mobility.*;
import jade.mtp.MTPDescriptor;
import jade.security.JADESecurityException;
import jade.security.CredentialsHelper;
import jade.security.JADEPrincipal;
import jade.security.Credentials;

/**
   This behaviour serves the actions of the JADE management ontology 
   supported by the AMS.
   Extends RequestManagementBehaviour and implements performAction() to 
   i) call the method of the AMS corresponding to the requested 
   action and ii) prepare the result notification depending on 
   - whether a result should be returned (RESULT or DONE)
   - whether the notification can be sent immediately or must be delayed
   at a later time.
   @author Tiziana Trucco - Tilab
   @author Giovanni Caire - Tilab
   @version $Date: 2008-08-19 12:27:27 +0200 (mar, 19 ago 2008) $ $Revision: 6043 $
 */

class AMSJadeAgentManagementBehaviour extends RequestManagementBehaviour{

	private ams theAMS;

	protected AMSJadeAgentManagementBehaviour(ams a, MessageTemplate mt) {
		super(a,mt);
		theAMS = a;
	}

	/**
     Call the proper method of the ams and prepare the notification 
     message
	 */
	protected ACLMessage performAction(Action slAction, ACLMessage request) throws JADESecurityException, FIPAException {
		Concept action = slAction.getAction();
		Object result = null;
		boolean resultNeeded = false;
		Object asynchNotificationKey = null;

		JADEPrincipal requesterPrincipal = null;
		Credentials requesterCredentials = null;
		try {
			CredentialsHelper ch = (CredentialsHelper) myAgent.getHelper("jade.core.security.Security");
			requesterPrincipal = ch.getPrincipal(request);
			requesterCredentials = ch.getCredentials(request);
		}
		catch (ServiceException se) {
			// Security plug in not installed --> Ignore it
		}

		// CREATE AGENT
		if (action instanceof CreateAgent) {
			theAMS.createAgentAction((CreateAgent) action, request.getSender(), requesterPrincipal, requesterCredentials);
			String agentName = JADEManagementOntology.adjustAgentName(((CreateAgent) action).getAgentName(), new String[] {((CreateAgent) action).getContainer().getName()});
			asynchNotificationKey = new AID(agentName, AID.ISLOCALNAME);
		}
		// KILL AGENT (asynchronous notification to requester)
		else if (action instanceof KillAgent) {
			theAMS.killAgentAction((KillAgent) action, request.getSender(), requesterPrincipal, requesterCredentials);
			asynchNotificationKey = ((KillAgent) action).getAgent();
		}
		// CLONE AGENT (asynchronous notification to requester)
		// Note that CloneAction extends MoveAction --> must be considered first!!!
		else if (action instanceof CloneAction) {
			theAMS.cloneAgentAction((CloneAction) action, request.getSender());
			asynchNotificationKey = new AID(((CloneAction) action).getNewName(), AID.ISLOCALNAME); 
		}
		// MOVE AGENT (asynchronous notification to requester)
		else if (action instanceof MoveAction) {
			theAMS.moveAgentAction((MoveAction) action, request.getSender());
			asynchNotificationKey = ((MoveAction) action).getMobileAgentDescription().getName();
		}
		// KILL CONTAINER (asynchronous notification to requester)
		else if (action instanceof KillContainer) {
			theAMS.killContainerAction((KillContainer) action, request.getSender(), requesterPrincipal, requesterCredentials);
			asynchNotificationKey = ((KillContainer) action).getContainer();
		}
		// SHUT DOWN PLATFORM
		else if (action instanceof ShutdownPlatform) {
			// Synchronous notification since we will not be here to send the notification 
			// when the platform will have shut down.
			theAMS.shutdownPlatformAction((ShutdownPlatform)action, request.getSender(), requesterPrincipal, requesterCredentials);
		}
		// INSTALL MTP
		else if (action instanceof InstallMTP) {
			MTPDescriptor dsc = theAMS.installMTPAction((InstallMTP) action, request.getSender());
			result = dsc.getAddresses()[0];
			resultNeeded = true;
		}
		// UNINSTALL MTP
		else if (action instanceof UninstallMTP) {
			theAMS.uninstallMTPAction((UninstallMTP) action, request.getSender());
		}
		// SNIFF ON
		else if (action instanceof SniffOn) {
			theAMS.sniffOnAction((SniffOn) action, request.getSender());
		}
		// SNIFF OFF
		else if (action instanceof SniffOff) {
			theAMS.sniffOffAction((SniffOff) action, request.getSender());
		}
		// DEBUG ON
		else if (action instanceof DebugOn) {
			theAMS.debugOnAction((DebugOn) action, request.getSender());
		}
		// DEBUG OFF
		else if (action instanceof DebugOff) {
			theAMS.debugOffAction((DebugOff) action, request.getSender());
		}
		// WHERE IS AGENT
		else if (action instanceof WhereIsAgentAction) {
			result = theAMS.whereIsAgentAction((WhereIsAgentAction) action, request.getSender());
			resultNeeded = true;
		}
		// QUERY PLATFORM LOCATIONS
		else if (action instanceof QueryPlatformLocationsAction) {
			result = theAMS.queryPlatformLocationsAction((QueryPlatformLocationsAction) action, request.getSender());
			resultNeeded = true;
		}
		// QUERY AGENTS ON LOCATION
		else if (action instanceof QueryAgentsOnLocation) {
			result = theAMS.queryAgentsOnLocationAction((QueryAgentsOnLocation) action, request.getSender());
			resultNeeded = true;
		}
		else {
			throw new UnsupportedFunction();
		}

		// Prepare the notification
		ACLMessage notification = request.createReply();
		notification.setPerformative(ACLMessage.INFORM);
		Predicate p = null;
		if (resultNeeded) {
			// The action produced a result
			p = new Result(slAction, result);
		}
		else {
			p = new Done(slAction);
		}
		try {
			theAMS.getContentManager().fillContent(notification, p);
		}
		catch (Exception e) {
			// Should never happen
			e.printStackTrace();
		}

		if (asynchNotificationKey != null) {
			// The event forced by the action has not happened yet. Store the
			// notification so that the AMS will send it when the event will
			// be happened.
			theAMS.storeNotification(action, asynchNotificationKey, notification);
			return null;
		}
		else {
			return notification;
		}
	}  
}
