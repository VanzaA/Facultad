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
   This example shows how to create a custom <code>MessageTemplate</code>.
   In this case we define a template matching only REQUEST messages where
   the ontology starts with "X".
   @author Giovanni Caire - TILAB
 */
public class CustomTemplateAgent extends Agent {
	
	/** 
	   Inner class MatchXOntology
	 */
  private class MatchXOntology implements MessageTemplate.MatchExpression {

    public boolean match(ACLMessage msg) {
    	String ontology = msg.getOntology();
    	return (ontology != null && ontology.startsWith("X"));
    }
  } // END of inner class MatchXOntology
  
	private MessageTemplate template = MessageTemplate.and(
		MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
		new MessageTemplate(new MatchXOntology()));
		
	protected void setup() {
		System.out.println("Agent "+getLocalName()+" is ready.");
		
		addBehaviour(new CyclicBehaviour(this) {
			public void action() {
				ACLMessage msg = myAgent.receive(template);
				if (msg != null) {
					System.out.println("Message matching custom template received:");
					System.out.println(msg);
				}
				else {
					block();
				}
			}
		} );
	}
}
