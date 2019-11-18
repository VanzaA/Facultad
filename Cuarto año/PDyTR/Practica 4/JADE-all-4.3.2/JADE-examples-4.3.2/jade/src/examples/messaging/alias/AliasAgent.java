package examples.messaging.alias;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.messaging.MessagingHelper;
import jade.core.messaging.MessagingService;
import jade.lang.acl.ACLMessage;

/**
 * This example shows how an agent can declare an ALIAS, i.e. an additional name.
 * Messages sent to that ALIAS will be delivered to the declaring agent as if they 
 * were explicitly directed to it. 
 * In particular in this example the Alias is constructed by adding the "-AAA" suffix 
 * to the agent name.<br> 
 * In order to try the example, start an AliasAgent agent and then using the DummyAgent
 * tool send it a message specifying its alias as receiver.  
 */
public class AliasAgent extends Agent {
	protected void setup() {
		String myName = getLocalName();
		String myAlias = myName+"-AAA";
		try {
			MessagingHelper helper = (MessagingHelper) getHelper(MessagingService.NAME);
			helper.createAlias(myAlias);
			System.out.println("Agent "+getLocalName()+" successfully registered Alias "+myAlias);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		addBehaviour(new CyclicBehaviour() {
			public void action() {
				ACLMessage msg = myAgent.receive();
				if (msg != null) {
					System.out.println("Received message from agent "+msg.getSender().getLocalName()+". Reply...");
					ACLMessage reply = msg.createReply();
					reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
					myAgent.send(reply);
				}
				else {
					block();
				}
			}
		});
	}
}
