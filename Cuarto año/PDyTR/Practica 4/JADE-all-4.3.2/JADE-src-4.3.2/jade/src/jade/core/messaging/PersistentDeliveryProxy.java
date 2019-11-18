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

//#J2ME_EXCLUDE_FILE

import jade.core.Node;
import jade.core.Service;
import jade.core.SliceProxy;
import jade.core.GenericCommand;
import jade.core.AID;
import jade.core.ContainerID;
import jade.core.Location;
import jade.core.IMTPException;
import jade.core.ServiceException;
import jade.core.NotFoundException;
import jade.core.NameClashException;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.FIPAAgentManagement.Envelope;


/**
   The remote proxy for the JADE kernel-level service managing a
   persistent message delivery subsystem installed in the platform.
   A persistent message delivery is able to deliver an ACL message
   across an agent lifecycle, as long as the agent ID stays the same.

   @author Giovanni Rimassa - FRAMeTech s.r.l.
 */
public class PersistentDeliveryProxy extends SliceProxy implements PersistentDeliverySlice {

	public boolean storeMessage(String storeName, GenericMessage msg, AID receiver) throws IMTPException, NotFoundException {
		try {
			GenericCommand cmd = new GenericCommand(H_STOREMESSAGE, NAME, null);
			cmd.addParam(storeName);
			// NOTE that we can't send the GenericMessage directly as a parameter
			// since we would loose the embedded ACLMessage
			cmd.addParam(msg.getACLMessage());
			cmd.addParam(msg.getEnvelope());
			cmd.addParam(msg.getPayload());
			cmd.addParam(new Boolean(msg.hasForeignReceiver()));
			cmd.addParam(msg.getTraceID());
			cmd.addParam(receiver);

			Node n = getNode();
			Object result = n.accept(cmd);
			if((result != null) && (result instanceof Throwable)) {
				if(result instanceof IMTPException) {
					throw (IMTPException)result;
				}
				else if(result instanceof NotFoundException) {
					throw (NotFoundException)result;
				}
				else {
					throw new IMTPException("An undeclared exception was thrown", (Throwable)result);
				}
			}
			return ((Boolean)result).booleanValue();
		}
		catch(ServiceException se) {
			throw new IMTPException("Unable to access remote node", se);
		}
	}

	public void flushMessages(AID receiver) throws IMTPException {
		try {
			GenericCommand cmd = new GenericCommand(H_FLUSHMESSAGES, NAME, null);
			cmd.addParam(receiver);

			Node n = getNode();
			Object result = n.accept(cmd);
			if((result != null) && (result instanceof Throwable)) {
				if(result instanceof IMTPException) {
					throw (IMTPException)result;
				}
				else {
					throw new IMTPException("An undeclared exception was thrown", (Throwable)result);
				}
			}

		}
		catch(ServiceException se) {
			throw new IMTPException("Unable to access remote node", se);
		}
	}

}
