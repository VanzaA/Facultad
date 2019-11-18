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

   An introspection event, recording the sending of an ACL message
   within the platform.

   @author Giovanni Rimassa -  Universita' di Parma
   @version $Date: 2005-02-16 18:18:28 +0100 (mer, 16 feb 2005) $ $Revision: 5552 $
*/

public class SentMessage implements Event {

    /**
       A string constant for the name of this event.
    */
    public static final String NAME = "Sent-Message";

    private AID sender;
    private AID receiver;
    private ACLMessage message;

    /**
       Default constructor. A default constructor is necessary for
       ontological classes.
    */
    public SentMessage() {
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
       Set the unicast <code>receiver</code> slot of this event.
       @param id The agent identifier of the unicast receiver of the
       sent message.
    */
    public void setReceiver(AID id) {
	receiver = id;
    }

    /**
       Retrieve the value of the unicast <code>receiver</code> slot of this
       event.
       @return The value of the unicast <code>receiver</code> slot, or
       <code>null</code> if no value was set.
    */
    public AID getReceiver() {
	return receiver;
    }
    
    /**
       Set the <code>message</code> slot of this event.
       @param msg The ACL message that was sent.
    */
    public void setMessage(ACLMessage msg) {
	message = msg;
    }

    /**
       Retrieve the value of the <code>message</code> slot of this
       event, containing the ACL message that was sent.
       @return The value of the <code>message</code> slot, or
       <code>null</code> if no value was set.
    */
    public ACLMessage getMessage() {
	return message;
    }

}
