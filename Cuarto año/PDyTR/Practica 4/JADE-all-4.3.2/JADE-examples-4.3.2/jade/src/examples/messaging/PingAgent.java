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
   This example shows an agent able to respond to other agents wishing
   to know if it is alive. More in details we use a 
   <code>CyclicBehaviour</code> that receives only messages of type 
   <code>QUERY_IF</code> and using the <code>presence</code> ontology.
   All other messages are ignored. This is achieved by specifying a 
   proper <code>MessageTemplate</code>. Whenever such a message is 
   received an <code>INFORM</code> message is sent back as reply.
   @author Giovanni Caire - TILAB
 */
public class PingAgent extends Agent {
	private MessageTemplate template = MessageTemplate.and(
		MessageTemplate.MatchPerformative(ACLMessage.QUERY_IF),
		MessageTemplate.MatchOntology("presence") );
		
	protected void setup() {
		addBehaviour(new CyclicBehaviour(this) {
			public void action() {
				ACLMessage msg = myAgent.receive(template);
				if (msg != null) {
					System.out.println("Received QUERY_IF message from agent "+msg.getSender().getName());
					ACLMessage reply = msg.createReply();
					if ("alive".equals(msg.getContent())) {
						reply.setPerformative(ACLMessage.INFORM);
						reply.setContent("alive");
					}
					else {
						reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
						reply.setContent("Unknown-content");
					}
					myAgent.send(reply);
				}
				else {
					block();
				}
			}
		} );
	}
}
