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

import java.io.Reader;
import java.io.Writer;
import java.io.IOException;
import jade.content.Predicate;

import jade.lang.acl.ACLMessage;

/**
This class represents a generic FIPAException, i.e. one of
NotUnderstood,Failure,Refuse, as defined in
<code>jade.domain.FIPAAgentManagement</code>.  It has two
constructors, one based on an ACLMessage, and the second based on its
content, i.e. the exception message.
@author Giovanni Rimassa - Universita' di Parma
@version $Date: 2009-03-03 15:02:51 +0100 (mar, 03 mar 2009) $ $Revision: 6097 $
 */

public class FIPAException extends Exception implements Predicate {

	protected ACLMessage msg; // can be accessed by subclasses
	
	private String content;

	/**
	 * Constructs a generic <code>FIPAException</code>. The ACL message
	 * performative is defaulted to <code>not-understood</code>.
	 * @param message is the content of the ACLMessage
	 **/
	public FIPAException(String message) {
		super();
		content = message;
	}

	/**
	 * Constructs a <code>FIPAException</code> from the given ACL
	 * message.
	 * @param message is the ACL message representing this exception
	 **/
	public FIPAException(ACLMessage message) {
		this(message.getContent());
		msg=(ACLMessage)message.clone();
	}

	/**
	 * Retrieve the ACL message whose content is represented by this
	 * exception.
	 * @return the ACLMessage representing this exception
	 **/
	public ACLMessage getACLMessage() {
		if (msg == null) {
			msg = new ACLMessage(ACLMessage.NOT_UNDERSTOOD);
			msg.setContent(getMessage());
		}
		return msg;
	}

	/**
	 * Set the content of the ACL message representing this exception
	 * @param message is the content
	 **/
	protected void setMessage(String message) {
		content=message;
		if (msg!=null)
			msg.setContent(message);
	}

	/**
	 * Get the content of the ACL message representing this exception
	 * @return A string representing the message content that describes
	 * this FIPA exception.
	 **/
	public String getMessage() {
		return content;
	}

}



