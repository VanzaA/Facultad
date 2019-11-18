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

package examples.thanksAgent;

import jade.core.Agent;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.*;


/**
 * This agent has the following functionality: 
 * <ul>
 * <li> registers with the DF
 * <li> creates a list of agents
 * <li> each of this new agents registers with the DF
 * <li> the father agent sends a message of greeting to each of them
 * <li> it waits for an answer to the greeting
 * <li> it thanks the agents that have answered 
 * </ul>
 * @author Fabio Bellifemine, TILab
 * @version $Date: 2009-07-01 15:43:58 +0200 (mer, 01 lug 2009) $ $Revision: 6151 $
 **/
public class ThanksAgent extends Agent {

	private static boolean IAmTheCreator = true;
	// number of answer messages received.
	private int answersCnt = 0;

	public final static String GREETINGS = "GREETINGS";
	public final static String ANSWER = "ANSWER";
	public final static String THANKS = "THANKS";
	private AgentContainer ac = null;
	private AgentController t1 = null;
	private AID initiator = null;

	protected void setup() {
		System.out.println(getLocalName()+" STARTED");
		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			initiator = new AID((String) args[0], AID.ISLOCALNAME);
		}

		try {
			// create the agent descrption of itself
			DFAgentDescription dfd = new DFAgentDescription();
			dfd.setName(getAID());
			// register the description with the DF
			DFService.register(this, dfd);
			System.out.println(getLocalName()+" REGISTERED WITH THE DF");
		} catch (FIPAException e) {
			e.printStackTrace();
		}

		if (IAmTheCreator) {
			IAmTheCreator = false;  // next agent in this JVM will not be a creator

			// create another two ThanksAgent
			String t1AgentName = getLocalName()+"t1";
			String t2AgentName = getLocalName()+"t2";

			try {
				// create agent t1 on the same container of the creator agent
				AgentContainer container = (AgentContainer)getContainerController(); // get a container controller for creating new agents
				t1 = container.createNewAgent(t1AgentName, "examples.thanksAgent.ThanksAgent", null);
				t1.start();
				System.out.println(getLocalName()+" CREATED AND STARTED NEW THANKSAGENT:"+t1AgentName + " ON CONTAINER "+container.getContainerName());
			} catch (Exception any) {
				any.printStackTrace();
			}


			// create agent t2 on a new container 
			// Get a hold on JADE runtime
			Runtime rt = Runtime.instance();
			// Create a default profile
			ProfileImpl p = new ProfileImpl(false);

			try {
				// Create a new non-main container, connecting to the default
				// main container (i.e. on this host, port 1099)
				ac = rt.createAgentContainer(p);
				// create a new agent
				AgentController t2 = ac.createNewAgent(t2AgentName,getClass().getName(),new Object[0]);
				// fire-up the agent
				t2.start();
				System.out.println(getLocalName()+" CREATED AND STARTED NEW THANKSAGENT:"+t2AgentName + " ON CONTAINER "+ac.getContainerName());
			} catch (Exception e2) {
				e2.printStackTrace();
			}

			// send them a GREETINGS message
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.setContent(GREETINGS);

			msg.addReceiver(new AID(t1AgentName, AID.ISLOCALNAME));
			msg.addReceiver(new AID(t2AgentName, AID.ISLOCALNAME));

			send(msg);
			System.out.println(getLocalName()+" SENT GREETINGS MESSAGE  TO "+t1AgentName+" AND "+t2AgentName); 
		}  /* IF YOU COMMENTED OUT THIS ELSE CLAUSE, THEN YOU WOULD GENERATE
	      AN INTERESTING INFINITE LOOP WITH INFINTE AGENTS AND AGENT 
	      CONTAINERS BEING CREATED 
	      else {
	      IAmTheCreator = true;
	      doWait(2000); // wait two seconds
	      }
		 */

		// add a Behaviour that listen if a greeting message arrives
		// and sends back an ANSWER.
		// if an ANSWER to a greetings message is arrived 
		// then send a THANKS message
		addBehaviour(new CyclicBehaviour(this) {
			public void action() {
				// listen if a greetings message arrives
				ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
				if (msg != null) {
					if (GREETINGS.equalsIgnoreCase(msg.getContent())) {
						// if a greetings message is arrived then send an ANSWER
						System.out.println(myAgent.getLocalName()+" RECEIVED GREETINGS MESSAGE FROM "+msg.getSender().getLocalName()); 
						ACLMessage reply = msg.createReply();
						reply.setContent(ANSWER);
						myAgent.send(reply);
						System.out.println(myAgent.getLocalName()+" SENT ANSWER MESSAGE");
					} 
					else if (ANSWER.equalsIgnoreCase(msg.getContent())) {
						// if an ANSWER to a greetings message is arrived 
						// then send a THANKS message
						System.out.println(myAgent.getLocalName()+" RECEIVED ANSWER MESSAGE FROM "+msg.getSender().getLocalName()); 
						ACLMessage replyT = msg.createReply();
						replyT.setContent(THANKS);
						myAgent.send(replyT);
						System.out.println(myAgent.getLocalName()+" SENT THANKS MESSAGE"); 
						answersCnt++;
						if (answersCnt == 2) {
							// All answers have been received. 
							// Wait a bit to be sure the other Thanks agents gets the Thank message,
							// then kill everybody
							try {
								Thread.sleep(1000);
							} 
							catch (InterruptedException ie) {}
							try {
								// Kill the created container (this will also kill ThanksAgent2)
								ac.kill();
								// Kill ThanksAgent2
								t1.kill();
								// Reset the creator indication
								IAmTheCreator = true;
								// Notify the initiator if any
								if (initiator != null) {
									ACLMessage notification = new ACLMessage(ACLMessage.INFORM);
									notification.addReceiver(initiator);
									send(notification);
								}	
							} 
							catch (StaleProxyException any) {
								any.printStackTrace();
							}
						}
					}
					else if (THANKS.equalsIgnoreCase(msg.getContent())) {
						System.out.println(myAgent.getLocalName()+" RECEIVED THANKS MESSAGE FROM "+msg.getSender().getLocalName()); 
					}
					else {
						System.out.println(myAgent.getLocalName()+" Unexpected message received from "+msg.getSender().getLocalName()); 
					}					
				}
				else {
					// if no message is arrived, block the behaviour
					block();
				}
			}
		});
	}

	protected void takeDown() {
		// Deregister with the DF
		try {
			DFService.deregister(this);
			System.out.println(getLocalName()+" DEREGISTERED WITH THE DF");
		} catch (FIPAException e) {
			e.printStackTrace();
		}
	}
}
