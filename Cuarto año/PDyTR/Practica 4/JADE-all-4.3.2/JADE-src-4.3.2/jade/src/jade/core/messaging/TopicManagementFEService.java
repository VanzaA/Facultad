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
import jade.core.Agent;
import jade.core.FEService;
import jade.core.IMTPException;
import jade.core.NotFoundException;
import jade.core.ServiceException;
import jade.core.ServiceHelper;

/**
 * Front-end side service class for the TopicManagementService
 */
public class TopicManagementFEService extends FEService {
	
	public String getName() {
		return TopicManagementHelper.SERVICE_NAME;
	}

	public String getBEServiceClassName() {
		return "jade.core.messaging.TopicManagementService";
	}

	public ServiceHelper getHelper(Agent a) {
		ServiceHelper sh = new TopicManagementHelper() {
			private Agent myAgent;
			
			public void init(Agent a) {
				myAgent = a;
			}
			
			public AID createTopic(String topicName) {
				return TopicUtility.createTopic(topicName);
			}

			public boolean isTopic(AID id) {
				return TopicUtility.isTopic(id);
			}
			
			public void register(AID topic) throws ServiceException {
				try {
					invoke(myAgent.getLocalName(), "register", new Object[]{topic});
				}
				catch (NotFoundException nfe) {
					throw new ServiceException("Local agent "+myAgent.getLocalName()+" not found on the back-end");
				}
				catch (IMTPException imtpe) {
					throw new ServiceException("Communication error: "+imtpe.getMessage(), imtpe);
				}
			}
			
			public void register(AID id, AID topic) throws ServiceException {
				try {
					invoke(myAgent.getLocalName(), "register", new Object[]{id, topic});
				}
				catch (NotFoundException nfe) {
					throw new ServiceException("Local agent "+myAgent.getLocalName()+" not found on the back-end");
				}
				catch (IMTPException imtpe) {
					throw new ServiceException("Communication error: "+imtpe.getMessage(), imtpe);
				}
			}
			
			public void deregister(AID topic) throws ServiceException {
				try {
					invoke(myAgent.getLocalName(), "deregister", new Object[]{topic});
				}
				catch (NotFoundException nfe) {
					throw new ServiceException("Local agent "+myAgent.getLocalName()+" not found on the back-end");
				}
				catch (IMTPException imtpe) {
					throw new ServiceException("Communication error: "+imtpe.getMessage(), imtpe);
				}
			}
			
			public void deregister(AID id, AID topic) throws ServiceException {
				try {
					invoke(myAgent.getLocalName(), "deregister", new Object[]{id, topic});
				}
				catch (NotFoundException nfe) {
					throw new ServiceException("Local agent "+myAgent.getLocalName()+" not found on the back-end");
				}
				catch (IMTPException imtpe) {
					throw new ServiceException("Communication error: "+imtpe.getMessage(), imtpe);
				}
			}
		};
		sh.init(a);
		return sh;
	}

}
