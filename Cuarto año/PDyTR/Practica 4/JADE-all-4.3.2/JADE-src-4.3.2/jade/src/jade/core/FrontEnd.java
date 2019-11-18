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


import jade.lang.acl.ACLMessage;

/**
@author Giovanni Caire - TILAB
*/

public interface FrontEnd {

  /**
     This constant is the name of the property whose value contains
     the list of addresses where a contaner Front End can contact its
     Back End.
  */
  static final String REMOTE_BACK_END_ADDRESSES = "beaddrs";

	/**
	   Request the FrontEnd container to create a new agent.
	   @param name The name of the new agent.
	   @param className The class of the new agent.
	   @param args The arguments to be passed to the new agent.
	 */
  void createAgent(String name, String className, String[] args) throws IMTPException;

  /**
	   Request the FrontEnd container to kill an agent.
	   @param name The name of the agent to kill.
	 */
  void killAgent(String name) throws NotFoundException, IMTPException;
  
  /**
	   Request the FrontEnd container to suspend an agent.
	   @param name The name of the agent to suspend.
	 */
  void suspendAgent(String name) throws NotFoundException, IMTPException;
  
  /**
	   Request the FrontEnd container to resume an agent.
	   @param name The name of the agent to resume.
	 */
  void resumeAgent(String name) throws NotFoundException, IMTPException;
  
  /**
	   Pass an ACLMessage to the FrontEnd for posting.
	   @param msg The message to be posted.
	   @param sender The name of the receiver agent.
	 */
  void messageIn(ACLMessage msg, String receiver) throws NotFoundException, IMTPException;
  
  /**
	   Request the FrontEnd container to exit.
	 */
  void exit(boolean self) throws IMTPException;
  
  /**
     Request the FrontEnd to synchronize i.e. to notify all its agents
	 */
  void synch() throws IMTPException;
}

