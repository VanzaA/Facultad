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



package examples.mobile;

import jade.core.Agent;
import jade.core.Location;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.StringTokenizer;

/**
This behaviour of the Agent serves all the received messages. In particular,
the following expressions are accepted as content of "request" messages:
- (move <destination>)  to move the Agent to another container. Example (move Front-End) or
(move (:location (:name Container-1) (:transport-protocol JADE-IPMT) (:transport-address IOR:0000...) ))
- (exit) to request the agent to exit
- (stop) to stop the counter
- (continue) to continue counting
@author Giovanni Caire - CSELT S.p.A
@version $Date: 2008-10-09 14:04:02 +0200 (gio, 09 ott 2008) $ $Revision: 6051 $
*/
class ServeIncomingMessagesBehaviour extends SimpleBehaviour
{
	ServeIncomingMessagesBehaviour(Agent a)
	{
		super(a);
	}

	public boolean done()
	{
		return false;
	}

	public void action()
	{
		ACLMessage msg;
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);

		// Get a message from the queue or wait for a new one if queue is empty
		msg = myAgent.receive(mt);
		if (msg == null) {
			block();
		 	return;
		}
		else {
			String replySentence = "";

			// Get action to perform
			//String s = msg.getContent().
			StringTokenizer st = new StringTokenizer(msg.getContent(), " ()\t\n\r\f");
			String action = (st.nextToken()).toLowerCase();
			// EXIT
			if      (action.equals("exit"))
			{
				System.out.println("They requested me to exit (Sob!)");
				// Set reply sentence
				replySentence = "\"OK exiting\"";
				myAgent.doDelete();
			}
			// STOP COUNTING
			else if (action.equals("stop"))
			{
				System.out.println("They requested me to stop counting");
				((MobileAgent) myAgent).stopCounter();
				// Set reply sentence
				replySentence = "\"OK stopping\"";
			} 				
			// CONTINUE COUNTING
			else if (action.equals("continue"))
			{
				System.out.println("They requested me to continue counting");
				((MobileAgent) myAgent).continueCounter();
				// Set reply sentence
				replySentence = "\"OK continuing\"";
			} 
			// MOVE TO ANOTHER LOCATION				
			else if (action.equals("move"))
			{
			    String destination = st.nextToken();
			    System.out.println();
			    Location dest = new jade.core.ContainerID(destination, null);
			    System.out.println("They requested me to go to " + destination);
				// Set reply sentence
				replySentence = "\"OK moving to " + destination+" \"";
				// Prepare to move
				((MobileAgent)myAgent).nextSite = dest;
				myAgent.doMove(dest);
			}
			// CLONE TO ANOTHER LOCATION				
			else if (action.equals("clone"))
			{
			    String destination = st.nextToken();
			    System.out.println();
			    Location dest = new jade.core.ContainerID(destination, null);
			    System.out.println("They requested me to clone myself to " + destination);
				// Set reply sentence
				replySentence = "\"OK cloning to " + destination+" \"";
				// Prepare to move
				((MobileAgent)myAgent).nextSite = dest;
				myAgent.doClone(dest, "clone"+((MobileAgent)myAgent).cnt+"of"+myAgent.getName());
			}
			// SAY THE CURRENT LOCATION 
			else if (action.equals("where-are-you"))
			{
			    System.out.println();
			    Location current = myAgent.here();
			    System.out.println("Currently I am running on "+current.getName());
				// Set reply sentence
				replySentence = current.getName();
			}

			// Reply
			ACLMessage replyMsg = msg.createReply();
			replyMsg.setPerformative(ACLMessage.INFORM);
			replyMsg.setContent(replySentence);
			myAgent.send(replyMsg);
		}

		return;
	}
}

