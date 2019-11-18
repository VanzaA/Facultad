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

package jade.tools;

//#APIDOC_EXCLUDE_FILE
 
import jade.core.Agent;
import jade.core.ServiceException;
import jade.core.event.ContainerAdapter;
import jade.core.event.ContainerEvent;
import jade.core.event.ContainerListener;
import jade.core.event.NotificationHelper;
import jade.core.event.NotificationService;

import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.JADEAgentManagement.*;
import jade.domain.introspection.*;

import jade.lang.acl.ACLMessage;
import jade.util.Logger;

import jade.content.lang.sl.SLCodec;

/**
 This abstract class is the common ancestor of all JADE tools (RMA,
 Sniffer, Introspector, etc.). It provides suitable behaviours to
 interact with the AMS, registering for interesting events and
 requesting actions when needed.
 
 @author Giovanni Rimassa - Universita' di Parma
 @version $Date: 2009-05-18 17:12:10 +0200 (lun, 18 mag 2009) $ $Revision: 6124 $
 */
public abstract class ToolAgent extends Agent {
	
	private ACLMessage AMSSubscription = new ACLMessage(ACLMessage.SUBSCRIBE);
	private ACLMessage AMSCancellation = new ACLMessage(ACLMessage.CANCEL);
	
	private transient ContainerListener myContainerListener = null;
	protected transient Logger logger;
	
	
	// This is left here for backward compatibility
	public static interface EventHandler extends AMSSubscriber.EventHandler {
	}
	
	/**
	 This abstract behaviour is used to receive notifications from
	 the AMS. 
	 */
	protected abstract class AMSListenerBehaviour extends AMSSubscriber {
		// Redefine the onStart() method not to automatically subscribe.
		public void onStart() {
		}
	} // End of AMSListenerBehaviour class
	
	/**
	 * Default constructor.
	 */
	public ToolAgent() {
	}
	
	/**
	 This method is invoked just after the generic agent
	 setup. Subclasses must use this method the same way ordinary
	 agents use their <code>setup()</code> method.
	 */
	protected void toolSetup() {
		
	}
	
	/**
	 This method is invoked just before the generic agent
	 takedown. Subclasses must use this method the same way ordinary
	 agents use their <code>takeDown()</code> method.
	 */
	protected void toolTakeDown() {
		
	}
	
	/**
	 Retrieve the <code>subscribe</code> ACL message with which this
	 tool agent subscribed with the AMS.
	 @return The subscription ACL message.
	 */
	protected ACLMessage getSubscribe() {
		return AMSSubscription;
	}
	
	/**
	 Retrieve the <code>cancel</code> ACL message with which this
	 tool agent removes its subscription with the AMS.
	 @return The cancellation ACL message.
	 */
	protected ACLMessage getCancel() {
		return AMSCancellation;
	}
	
	/**
	 Retrieve the <code>request</code> ACL message with which this
	 tool agent requests the AMS tool-specific actions.
	 @return The request ACL message.
	 */
	protected ACLMessage getRequest() {
		ACLMessage AMSRequest = new ACLMessage(ACLMessage.REQUEST);
		AMSRequest.setSender(getAID());
		AMSRequest.addReceiver(getAMS());
		AMSRequest.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
		AMSRequest.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);
		return AMSRequest;
	}
	
	public final void setup() {	
		init();
		
		// Fill ACL messages fields
		AMSSubscription.setSender(getAID());
		AMSSubscription.clearAllReceiver();
		AMSSubscription.addReceiver(getAMS());
		AMSSubscription.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);
		AMSSubscription.setOntology(IntrospectionOntology.NAME);
		AMSSubscription.setReplyWith(AMSSubscriber.AMS_SUBSCRIPTION);
		AMSSubscription.setConversationId(getLocalName());
		AMSSubscription.setContent(AMSSubscriber.PLATFORM_EVENTS);
		
		AMSCancellation.setSender(getAID());
		AMSCancellation.clearAllReceiver();
		AMSCancellation.addReceiver(getAMS());
		AMSCancellation.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);
		AMSCancellation.setOntology(IntrospectionOntology.NAME);
		AMSCancellation.setReplyWith(AMSSubscriber.AMS_CANCELLATION);
		AMSCancellation.setConversationId(getLocalName());
		// No content is needed (cfr. FIPA 97 Part 2 page 26)
		
		// Call tool-specific setup
		toolSetup();
	}
	
	protected final void takeDown() {
		clean();
		
		// Call tool-specific takedown
		toolTakeDown();
	}
	
	protected void afterClone() {
		init();
	}
	
	protected void beforeMove() {
		clean();
	}
	
	protected void afterMove() {
		init();
	}
	
	protected void afterLoad() {
		init();
	}
	
	protected void afterThaw() {
		init();
	}
	
	protected void afterReload() {
		init();
	}
	
	protected void beforeSave() {
	}
	
	protected void beforeFreeze() {
		clean();
	}
	
	protected void beforeReload() {
		clean();
	}
	
	private void init() {
		logger = Logger.getMyLogger(getName());
		
		// Register the supported ontologies
		getContentManager().registerOntology(JADEManagementOntology.getInstance());
		getContentManager().registerOntology(IntrospectionOntology.getInstance());
		getContentManager().registerOntology(FIPAManagementOntology.getInstance());
		
		// Register the supported languages
		SLCodec codec = new SLCodec();
		getContentManager().registerLanguage(codec, FIPANames.ContentLanguage.FIPA_SL0);
		getContentManager().registerLanguage(codec, FIPANames.ContentLanguage.FIPA_SL1);
		getContentManager().registerLanguage(codec, FIPANames.ContentLanguage.FIPA_SL2);
		getContentManager().registerLanguage(codec, FIPANames.ContentLanguage.FIPA_SL);
		
		// Register to be notified about the REATTACHED event in order to handle Main Container faults
		try {
			NotificationHelper helper = (NotificationHelper) getHelper(NotificationService.NAME);
			myContainerListener = new ContainerAdapter() {
				public void reattached(ContainerEvent ev) {
					// The Main Container lost my subscription --> Subscribe again
					send(getSubscribe());
				}
			};
			helper.registerContainerListener(myContainerListener);
		}
		catch (ServiceException se) {
			// Just print a warning since this does not affect the normal operation of a ToolAgent
			logger.log(Logger.WARNING, "NotificationService not installed. Some tool may not work properly.");
		}
	}
	
	private void clean() {
		if (myContainerListener != null) {
			try {
				NotificationHelper helper = (NotificationHelper) getHelper(NotificationService.NAME);
				helper.deregisterContainerListener(myContainerListener);
			}
			catch (ServiceException se) {
				// Just do nothing since this does not affect the normal operation of a ToolAgent
			}
		}
	}
}
