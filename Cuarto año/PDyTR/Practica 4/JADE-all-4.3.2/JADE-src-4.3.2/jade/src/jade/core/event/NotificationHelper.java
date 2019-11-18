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

package jade.core.event;

//#MIDP_EXCLUDE_FILE

import jade.core.ServiceHelper;
import jade.core.AID;
import jade.core.Location;
import jade.core.IMTPException;
import jade.core.ServiceException;
import jade.core.NotFoundException;
import jade.core.NameClashException;

import jade.security.JADESecurityException;

import jade.util.leap.List;

/** 
 The vertical interface for the JADE kernel-level service implementing the 
 JADE event dispatching mechanism.
 
 @author Giovanni Caire - Telecom Italia
 */
public interface NotificationHelper extends ServiceHelper {
	/**
	 * Register a listener of message events
	 */
	void registerMessageListener(MessageListener ml);
	
	/**
	 * Deregister a listener of message events
	 */
	void deregisterMessageListener(MessageListener ml);
	
	/**
	 * Register a listener of agent internal events
	 */
	void registerAgentListener(AgentListener al);
	
	/**
	 * Deregister a listener of agent internal events
	 */
	void deregisterAgentListener(AgentListener al);
	
	/**
	 * Register a listener of container related events
	 */
	void registerContainerListener(ContainerListener cl);
	
	/**
	 * Deregister a listener of container related events
	 */
	void deregisterContainerListener(ContainerListener cl);
}
