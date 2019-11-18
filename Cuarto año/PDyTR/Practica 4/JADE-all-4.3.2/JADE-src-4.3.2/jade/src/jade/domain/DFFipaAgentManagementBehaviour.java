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

import jade.content.Concept;
import jade.content.Predicate;
import jade.content.onto.basic.Action;
import jade.content.onto.basic.Done;
import jade.content.onto.basic.Result;
import jade.core.ServiceNotActiveException;

//#PJAVA_EXCLUDE_BEGIN
//#DOTNET_EXCLUDE_BEGIN
import jade.core.sam.AverageMeasureProviderImpl;
import jade.core.sam.SAMHelper;
//#DOTNET_EXCLUDE_END
//#PJAVA_EXCLUDE_END

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.FIPAAgentManagement.*;
import jade.security.JADESecurityException;
import jade.util.Logger;

/**
   This behaviour serves the actions of the FIPA management ontology 
   supported by the DF.
   It extends RequestManagementBehaviour and implements performAction() to 
   i) call the method of the DF corresponding to the requested 
   action and ii) prepare the result notification.
   @author Tiziana Trucco - TILAB
   @author Fabio Bellifemine - TILAB
   @author Giovanni Caire - TILAB
 */
class DFFipaAgentManagementBehaviour extends RequestManagementBehaviour {

	private df theDF;
	//#PJAVA_EXCLUDE_BEGIN
	//#DOTNET_EXCLUDE_BEGIN
	private AverageMeasureProviderImpl serveRequestTimeProvider;
	//#DOTNET_EXCLUDE_END
	//#PJAVA_EXCLUDE_END

	protected DFFipaAgentManagementBehaviour(df a, MessageTemplate mt){
		super(a, mt);
		theDF = a;
	}
	
	public void onStart() {
		super.onStart();
		//#PJAVA_EXCLUDE_BEGIN
		//#DOTNET_EXCLUDE_BEGIN
		try {
			SAMHelper samHelper = (SAMHelper) myAgent.getHelper(SAMHelper.SERVICE_NAME);
			serveRequestTimeProvider = new AverageMeasureProviderImpl();
			samHelper.addEntityMeasureProvider("DF-Serve-Request-Avg-Time", serveRequestTimeProvider);
		}
		catch (ServiceNotActiveException snae) {
			// SAM Service not active 
		}
		catch (Exception e) {
			myLogger.log(Logger.WARNING, "Error initializing SAM providers", e);
		}
		//#DOTNET_EXCLUDE_END
		//#PJAVA_EXCLUDE_END
	}

	/**
     Call the proper method of the DF and prepare the notification 
     message
	 */
	protected ACLMessage performAction(Action slAction, ACLMessage request) throws JADESecurityException, FIPAException {
		Concept action = slAction.getAction();
		Object result = null;
		boolean asynchNotificationRequired = false;

		// REGISTER
		if (action instanceof Register) {
			theDF.registerAction((Register) action, request.getSender());
		}
		// DEREGISTER
		else if (action instanceof Deregister) {
			theDF.deregisterAction((Deregister) action, request.getSender());
		}
		// MODIFY
		else if (action instanceof Modify) {
			theDF.modifyAction((Modify) action, request.getSender());
		}
		// SEARCH
		else if (action instanceof Search) {
			theDF.storePendingRequest(action, request);
			result = theDF.searchAction((Search) action, request.getSender());
			if (result == null) {
				asynchNotificationRequired = true;
			}
		}
		else {
			throw new UnsupportedFunction();
		}

		if (!asynchNotificationRequired) {
			theDF.removePendingRequest(action);
			// The requested action has been completed. Prepare the notification
			ACLMessage notification = request.createReply();
			notification.setPerformative(ACLMessage.INFORM);
			Predicate p = null;
			if (result != null) {
				// The action produced a result
				p = new Result(slAction, result);
			}
			else {
				p = new Done(slAction);
			}
			try {
				theDF.getContentManager().fillContent(notification, p);
			}
			catch (Exception e) {
				// Should never happen
				e.printStackTrace();
			}
			
			//#PJAVA_EXCLUDE_BEGIN
			//#DOTNET_EXCLUDE_BEGIN
			long serveTime = System.currentTimeMillis() - request.getPostTimeStamp();
			if (serveRequestTimeProvider != null) {
				serveRequestTimeProvider.addSample(serveTime);
			}
			//#DOTNET_EXCLUDE_END
			//#PJAVA_EXCLUDE_END
			
			return notification;
		}
		else {
			// The requested action is being processed by a Behaviour.
			return null;
		}
	}
}
