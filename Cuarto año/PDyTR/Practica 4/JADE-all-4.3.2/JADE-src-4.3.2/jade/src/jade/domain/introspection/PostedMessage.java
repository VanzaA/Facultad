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


package jade.domain.introspection;

//#APIDOC_EXCLUDE_FILE

import jade.core.AID;

/**
   An introspection event, recording the posting of an ACL message
   within the platform.

   @author Giovanni Rimassa -  Universita' di Parma
   @version $Date: 2010-06-11 15:32:31 +0200 (ven, 11 giu 2010) $ $Revision: 6352 $
 */
public class PostedMessage implements Event {

	/**
       A string constant for the name of this event.
	 */
	public static final String NAME = "Posted-Message";

	private ACLMessage message;
	private AID sender;
	private AID receiver;


	/**
       Default constructor. A default constructor is necessary for
       ontological classes.
	 */
	public PostedMessage() {
	}

	/**
       Retrieve the name of this event.
       @return A constant value for the event name.
	 */
	public String getName() {
		return NAME;
	}

	/**
       Set the <code>sender</code> slot of this event.
       @param id The agent identifier of the agent that sent the
       message.
	 */
	public void setSender(AID id) {
		sender = id;
	}

	/**
       Retrieve the value of the <code>sender</code> slot of this
       event, containing the agent identifier of the agent that sent
       the message.
       @return The value of the <code>sender</code> slot, or
       <code>null</code> if no value was set.
	 */
	public AID getSender() {
		return sender;
	}

	/**
       Set the <code>message</code> slot of this event.
       @param msg The ACL message that was posted to the receiver
       agent message queue.
	 */
	public void setMessage(ACLMessage msg) {
		message = msg;
	}

	/**
       Retrieve the value of the <code>message</code> slot of this
       event, containing the ACL message that was posted to the
       receiver agent message queue.
       @return The value of the <code>message</code> slot, or
       <code>null</code> if no value was set.
	 */
	public ACLMessage getMessage() {
		return message;
	}

	/**
       Set the <code>receiver</code> slot of this event.
       @param id The agent identifier of the agent owning the message
       queue the message was posted to.
	 */
	public void setReceiver(AID id) {
		receiver = id;
	}

	/**
       Retrieve the value of the <code>receiver</code> slot of this
       event, containing the agent identifier of the agent owning the
       message queue the message was posted to.
       @return The value of the <code>receiver</code> slot, or
       <code>null</code> if no value was set.
	 */
	public AID getReceiver() {
		return receiver;
	}

}
