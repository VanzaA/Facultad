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

package examples.messaging;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
   This example shows how to receive messages in blocking mode
   by means of the <code>blockingReceive()</code> method. Note that 
   this method blocks the whole agent. If you call it from within
   a behaviour you have to take into account that all other behaviours
   will not be able to run until the call to <code>blockingReceive()</code>
   returns.
   @author Giovanni Caire - TILAB
 */
public class BlockingReceiveAgent extends Agent {
		
	protected void setup() {
		System.out.println("Agent "+getLocalName()+": waiting for REQUEST message...");
		ACLMessage msg = blockingReceive(MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
		System.out.println("Agent "+getLocalName()+": REQUEST message received. Reply and exit.");
		ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
		reply.addReceiver(msg.getSender());
		reply.setContent("exiting");
		send(reply);
		doDelete();
	}
	
	protected void takeDown() {
		System.out.println("Agent "+getLocalName()+": terminating");
	}
}
