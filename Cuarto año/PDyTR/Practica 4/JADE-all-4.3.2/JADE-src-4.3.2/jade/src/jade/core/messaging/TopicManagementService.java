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

//#MIDP_EXCLUDE_FILE
//#APIDOC_EXCLUDE_FILE

import jade.core.*;
import jade.core.messaging.MessagingService;
import jade.core.messaging.MessagingSlice;
import jade.core.messaging.GenericMessage;
import jade.core.management.AgentManagementSlice;
import jade.lang.acl.ACLMessage;
import jade.util.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Iterator;

/**
 * TopicManagement service main class
 * @author Giovanni Caire - TILAB
 */
public class TopicManagementService extends BaseService {
	public static final String NAME = TopicManagementHelper.SERVICE_NAME;
		
	private AgentContainer myContainer;
	private MainContainer myMain;
	
	private Filter incFilter;
	private Filter outFilter;
	private ServiceComponent localSlice;
	
	private TopicTable topicTable = new TopicTable();
	private MessagingService theMessagingService;
	
	public void init(AgentContainer ac, Profile p) throws ProfileException {
		super.init(ac, p);
		myContainer = ac;
		myMain = ac.getMain();
		
		// Create filters
		outFilter = new CommandOutgoingFilter();
		incFilter = new CommandIncomingFilter();
		// Create local slice
		localSlice = new ServiceComponent();
	}
	
	public void boot(Profile p) throws ServiceException {
		super.boot(p);
		try {
			if (myContainer.getPlatformID().equals(TopicManagementHelper.TOPIC_SUFFIX)) {
				throw new ServiceException("The TopicManagementService cannot be used within a platform called with the reserved name "+TopicManagementHelper.TOPIC_SUFFIX);
			}
			theMessagingService = (MessagingService) myContainer.getServiceFinder().findService(MessagingService.NAME);
		}
		catch (IMTPException imtpe) {
			// Should never happen since this is a local call
			throw new ServiceException("Cannot retrieve the local MessagingService.", imtpe);
		}
	}
	
	public String getName() {
		return NAME;
	}
	
	public Filter getCommandFilter(boolean direction) {
		if(direction == Filter.INCOMING) {
			return incFilter;
		}
		else {
			return outFilter;
		}
	}
	
	public Class getHorizontalInterface() {
		return TopicManagementSlice.class;
	}
	
	/**
	 * Retrieve the locally installed slice of this service.
	 */
	public Service.Slice getLocalSlice() {
		return localSlice;
	}
	
	/**
	 * Return the TopicManagementHelper for a given agent
	 */
	public ServiceHelper getHelper(Agent a) throws ServiceException {
		// The agent is passed to the helper in the init() method
		return new TopicHelperImpl();
	}
	
	
	public String dump(String key) {
		StringBuffer sb = new StringBuffer();
		sb.append(topicTable.toString());
		sb.append(super.dump(key));
		return (sb.toString());
	}
	
	/**
	 * Inner class CommandOutgoingFilter.
	 * Intercepts the SEND_MESSAGE VCommand and broadcast messages directed to a topic to all
	 * agents interested in that topic
	 */
	private class CommandOutgoingFilter extends Filter {
		public CommandOutgoingFilter() {
			super();
			setPreferredPosition(2);  // Before the Messaging (encoding) filter and the security related ones
		}
		
		public final boolean accept(VerticalCommand cmd) {
			String name = cmd.getName();
			if (name.equals(MessagingSlice.SEND_MESSAGE)) {
				AID sender = (AID) cmd.getParam(0);
				GenericMessage gMsg = (GenericMessage) cmd.getParam(1);
				AID receiver = (AID) cmd.getParam(2);
				
				if (TopicUtility.isTopic(receiver)) {
					// This message is directed to a Topic
					AID topic = receiver;
					if (myLogger.isLoggable(Logger.FINE)) {
						myLogger.log(Logger.FINE, "Handling message about topic "+topic.getLocalName());
					}
					ACLMessage msg = gMsg.getACLMessage();
					Collection interestedAgents = topicTable.getInterestedAgents(topic, msg);
					if (interestedAgents.size() > 0) {
						// Forward the message to all agents interested in that topic.
						// Note that if no agents are currently listening to this topic, the message is simply swallowed
						msg.addUserDefinedParameter(ACLMessage.IGNORE_FAILURE, "true");
						gMsg.setModifiable(false);
						Iterator it = interestedAgents.iterator();
						while (it.hasNext()) {
							AID target = (AID) it.next();
							if (myLogger.isLoggable(Logger.FINE)) {
								myLogger.log(Logger.FINE, "Forwarding message to agent "+target.getName());
							}
							sendMessage(sender, gMsg, target);
						}
					}
					// Veto the original SEND_MESSAGE command
					return false;
				}
			}
			// Never veto other commands
			return true;
		}
	} // END of inner class CommandOutgoingFilter
	
	
	private final void sendMessage(AID sender, GenericMessage gMsg, AID receiver) {
		GenericCommand cmd = new GenericCommand(MessagingSlice.SEND_MESSAGE, MessagingService.NAME, null);
		cmd.addParam(sender);
		cmd.addParam(gMsg);
		cmd.addParam(receiver);
		
		try {
			theMessagingService.submit(cmd);
		}
		catch (ServiceException se) {
			// Should never happen
			se.printStackTrace();
		}
	}
	
	
	/**
	 * Inner class CommandIncomingFilter.
	 */
	private class CommandIncomingFilter extends Filter {
		public boolean accept(VerticalCommand cmd) {
			String name = cmd.getName();
			if (myMain != null) {
				if (name.equals(AgentManagementSlice.INFORM_KILLED)) {
					// If the dead agent was registered to some topic, deregister it
					handleInformKilled(cmd);
				}
				if (name.equals(Service.NEW_SLICE)) {
					// If the new slice is a TopicManagementSlice, notify it about the currently registered agents
					handleNewSlice(cmd);
				}
			}
			else {
				if (name.equals(Service.REATTACHED)) {
					// The Main lost all information related to this container --> Notify it again
					handleReattached(cmd);
				}
			}
			// Never veto a Command
			return true;
		}
	} // END of inner class CommandIncomingFilter
	
	
	//////////////////////////////////////////////////
	// Methods called by the CommandIncomingFilter
	//////////////////////////////////////////////////
	/**
	 * If the dead agent was interested in some topic, notify all slices that its interest is no longer valid
	 */ 
	private void handleInformKilled(VerticalCommand cmd) {
		Object[] params = cmd.getParams();
		AID aid = (AID) params[0];
		List topics = topicTable.getRelevantTopics(aid);
		if (topics.size() > 0) {
			try {
				Service.Slice[] slices = getAllSlices();
				Iterator it = topics.iterator();
				while (it.hasNext()) {
					broadcastDeregistration(aid, (AID) it.next(), slices);
				}
			}
			catch (Throwable t) {
				myLogger.log(Logger.WARNING, "Error retrieving topic-management-slices when trying to broadcast topic de-registration due to agent death. ", t);
			}
		}
	}
	
	/**
	 * If the new slice is a TopicManagementSlice notify it about all current registrations
	 */
	private void handleNewSlice(VerticalCommand cmd) {
		if (cmd.getService().equals(NAME)) {
			Object[] params = cmd.getParams();
			String newSliceName = (String) params[0];
			try {
				// Be sure to get the new (fresh) slice --> Bypass the service cache
				TopicManagementSlice newSlice = (TopicManagementSlice) getFreshSlice(newSliceName);
				List registrations = topicTable.getAllRegistrations();
				Iterator it = registrations.iterator();
				while (it.hasNext()) {
					TopicRegistration reg = (TopicRegistration) it.next();
					newSlice.register(reg.getAID(), reg.getTopic());
				}
			}
			catch (Throwable t) {
				myLogger.log(Logger.WARNING, "Error notifying new slice "+newSliceName+" about current topic registrations", t);
			}
		}
	}
	
	/**
	 * The Main lost all information about the local node --> Notify the Main slice about all local registrations
	 */
	private void handleReattached(VerticalCommand cmd) {
		try {
			// Be sure to get a fresh slice --> Bypass the service cache
			TopicManagementSlice newSlice = (TopicManagementSlice) getFreshSlice(MAIN_SLICE);
			List registrations = topicTable.getAllRegistrations();
			Iterator it = registrations.iterator();
			while (it.hasNext()) {
				TopicRegistration reg = (TopicRegistration) it.next();
				AID aid = reg.getAID();
				if (myContainer.acquireLocalAgent(aid) != null) {
					try {
						newSlice.register(aid, reg.getTopic());
					}
					catch (Exception e) {
						myLogger.log(Logger.WARNING, "Error notifying main slice about current local topic registrations", e);
					}
					myContainer.releaseLocalAgent(aid);
				}
			}
		}
		catch (Throwable t) {
			myLogger.log(Logger.WARNING, "Error retrieving main slice.", t);
		}
	}
	
	
	/**
	 * Inner class ServiceComponent
	 */
	private class ServiceComponent implements Service.Slice {
		public Service getService() {
			return TopicManagementService.this;
		}
		
		public Node getNode() throws ServiceException {
			try {
				return TopicManagementService.this.getLocalNode();
			}
			catch(IMTPException imtpe) {
				throw new ServiceException("Error retrieving local node", imtpe);
			}
		}
		
		public VerticalCommand serve(HorizontalCommand cmd) {
			try {
				String cmdName = cmd.getName();
				Object[] params = cmd.getParams();
				
				if (cmdName.equals(TopicManagementSlice.H_REGISTER)) {
					AID aid = (AID) params[0];
					AID topic = (AID) params[1];
					//System.out.println("Received registration of agent "+aid.getName()+" to topic "+topic.getLocalName());
					if (myLogger.isLoggable(Logger.FINE)) {
						myLogger.log(Logger.FINE, "Received registration of agent "+aid.getName()+" to topic "+topic.getLocalName());
					}					
					register(aid, topic);
				}
				else if(cmdName.equals(TopicManagementSlice.H_DEREGISTER)) {
					AID aid = (AID) params[0];
					AID topic = (AID) params[1];
					//System.out.println("Received deregistration of agent "+aid.getName()+" from topic "+topic.getLocalName());
					if (myLogger.isLoggable(Logger.FINE)) {
						myLogger.log(Logger.FINE, "Received deregistration of agent "+aid.getName()+" from topic "+topic.getLocalName());
					}					
					deregister(aid, topic);
				}
			}
			catch (Throwable t) {
				cmd.setReturnValue(t);
			}
			return null;
		}
		
		private void register(AID aid, AID topic) {
			topicTable.register(aid, topic);
		}
		
		private void deregister(AID aid, AID topic) {
			topicTable.deregister(aid, topic);
		}
	} // END of inner class ServiceComponent
	
	/**
	 * Inner class TopicHelperImpl
	 */
	private class TopicHelperImpl implements TopicManagementHelper {
		private AID aid;
		
		public void init(Agent a) {
			aid = a.getAID();
		}
		
		public AID createTopic(String topicName) {
			return TopicUtility.createTopic(topicName);
		}
		
		public boolean isTopic(AID id) {
			return TopicUtility.isTopic(id);
		}
		
		public void register(AID topic) throws ServiceException {
			Service.Slice[] slices = getAllSlices();
			broadcastRegistration(aid, topic, slices);
		}
		
		public void register(AID id, AID topic) throws ServiceException {
			Service.Slice[] slices = getAllSlices();
			broadcastRegistration(id, topic, slices);
		}
		
		public void deregister(AID topic) throws ServiceException {
			Service.Slice[] slices = getAllSlices();
			broadcastDeregistration(aid, topic, slices);
		}
		
		public void deregister(AID id, AID topic) throws ServiceException {
			Service.Slice[] slices = getAllSlices();
			broadcastDeregistration(id, topic, slices);
		}
	}  // END of inner class TopicHelperImpl
	
	
	///////////////////////////////////////////////////
	// Utility methods
	///////////////////////////////////////////////////
	private void broadcastRegistration(AID aid, AID topic, Service.Slice[] slices) throws ServiceException {
		if (myLogger.isLoggable(Logger.CONFIG)) {
			myLogger.log(Logger.CONFIG, "Registering agent "+aid.getName()+" to topic "+topic.getLocalName());
		}
		for (int i = 0; i < slices.length; i++) {
			String sliceName = null;
			try {
				TopicManagementSlice slice = (TopicManagementSlice) slices[i];
				sliceName = slice.getNode().getName();
				if (myLogger.isLoggable(Logger.FINER)) {
					myLogger.log(Logger.FINER, "Propagating registration of agent "+aid.getName()+" to slice "+sliceName);
				}
				slice.register(aid, topic);
			}
			catch(Throwable t) {
				// NOTE that slices are always retrieved from the main and not from the cache --> No need to retry in case of failure 
				myLogger.log(Logger.WARNING, "Error propagating topic registration to slice  " + sliceName, t);
			}
		}
	}
	
	private void broadcastDeregistration(AID aid, AID topic, Service.Slice[] slices) throws ServiceException {
		if (myLogger.isLoggable(Logger.CONFIG)) {
			myLogger.log(Logger.CONFIG, "Deregistering agent "+aid.getName()+" from topic "+topic.getLocalName());
		}
		for (int i = 0; i < slices.length; i++) {
			String sliceName = null;
			try {
				TopicManagementSlice slice = (TopicManagementSlice) slices[i];
				sliceName = slice.getNode().getName();
				if (myLogger.isLoggable(Logger.FINER)) {
					myLogger.log(Logger.FINER, "Propagating deregistration of agent "+aid.getName()+" to slice "+sliceName);
				}
				slice.deregister(aid, topic);
			}
			catch(Throwable t) {
				// NOTE that slices are always retrieved from the main and not from the cache --> No need to retry in case of failure 
				myLogger.log(Logger.WARNING, "Error propagating topic de-registration to slice  " + sliceName, t);
			}
		}
	}
}
