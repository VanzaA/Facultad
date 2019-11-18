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

import jade.core.AID;
import jade.core.ServiceHelper;
import jade.core.ServiceException;

/**
 * The TopicManagementHelper provides methods that allows creating topic objects and registering/deregistering
 * to topics.
 * Topics are represented by means of <code>AID</code> objects so that they can be used as receivers
 * of ACLMessages. In this way sending a message to an agent or sending a message about a topic is 
 * completely uniform.   
 * @author Giovanni Caire - TILAB
 */
public interface TopicManagementHelper extends ServiceHelper {
	/**
	 * This constant represents the name of the Topic Management Service and must be specified 
	 * in the <code>getHelper()</code> method of the <code>Agent</code> class to retrieve the helper
	 * of the local TopicManagementService.
	 */
	public static final String SERVICE_NAME = "jade.core.messaging.TopicManagement";
	public static final String TOPIC_SUFFIX = "TOPIC_";
	public static final String TOPIC_TEMPLATE_WILDCARD = "*";
	 
	/**
	 * Create a topic with a given name.
	 * @param topicName The name of the topic to be created
	 * @return The <code>AID</code> object representing the created topic
	 */
	AID createTopic(String topicName);

	/**
	 * Checks if an <code>AID</code> represents a topic
	 * @param id The <code>AID</code> to be checked
	 * @return <code>true</code> if the given <code>AID</code> represents a topic. <code>false</code> otherwise
	 */
	boolean isTopic(AID id);
	
	/**
	 * Register the agent associated to this helper to a given topic
	 * @param topic The topic to register to
	 * @throws ServiceException If some error occurs during the registration
	 */
	void register(AID topic) throws ServiceException;
	
	/**
	 * Register a given AID to a given topic. Registering a specific AID 
	 * instead of the agent AID, allows registering an Alias or a Virtual agent AID 
	 * @param aid The AID that is going to be registered
	 * @param topic The topic to register to
	 * @throws ServiceException If some error occurs during the registration
	 */
	void register(AID aid, AID topic) throws ServiceException;
	
	/**
	 * De-register the agent associated to this helper from a given topic
	 * @param topic The topic to de-register from
	 * @throws ServiceException If some error occurs during the de-registration
	 */
	void deregister(AID topic) throws ServiceException;
	
	/**
	 * De-register a given AID from a given topic
	 * @param aid The AID that is going to be de-registered
	 * @param topic The topic to de-register from
	 * @throws ServiceException If some error occurs during the de-registration
	 */
	void deregister(AID aid, AID topic) throws ServiceException;
}
