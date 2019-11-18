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


package jade.tools.DummyAgent;

//Import required Java classes 
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.util.*;

//Import required Jade classes
import jade.core.*;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.*;
import jade.domain.FIPAAgentManagement.Envelope;

/**
@author Giovanni Caire - CSELT S.p.A
@version $Date: 2007-04-05 12:23:17 +0200 (gio, 05 apr 2007) $ $Revision: 5955 $
 */

class DummyBehaviour extends CyclicBehaviour {
	DummyBehaviour(Agent a) {
		super(a);
	}

	public void action() {
		ACLMessage msg = myAgent.receive();
		if (msg != null) {		
			// ATTENTION!! In order to insert the received message in the queued message list 
			// I cannot simply do it e.g. 
			// ((DummyAgent)myAgent).getGui().queuedMsgListModel.add(0, (Object) new MsgIndication(msg, MsgIndication.INCOMING, new Date()));
			// WHY?
			// Because this is not thread safe!!!
			// In fact, if this operation is executed from this thread while the AWT Event Dispatching 
			// Thread is updating the JList component that shows the queued message list in the DummyAgent
			// GUI (e.g. because the user has just sent a message), this can cause an inconsistency
			// between what is shown in the GUI and what the queued message list actually contains.
			// HOW TO SOLVE THE PROBLEM?
			// I need to request the AWT Event Dispatching Thread to insert the received message
			// in the queued message list!
			// This can be done by using the invokeLater static method of the SwingUtilities class 
			// as below

			SwingUtilities.invokeLater(new EDTRequester((DummyAgent)myAgent, msg));
		}
		else {
			block();
		}
	}

	class EDTRequester implements Runnable {
		DummyAgent agent;
		ACLMessage msg;

		EDTRequester(DummyAgent a, ACLMessage m) {
			agent = a;
			msg = m;
		}

		public void run() {
			agent.getGui().queuedMsgListModel.add(0, (Object) new MsgIndication(msg, MsgIndication.INCOMING, new Date()));
			StringACLCodec codec = new StringACLCodec();
			try {
				String charset;
				Envelope e;
				if (((e = msg.getEnvelope()) == null) || ((charset = e.getPayloadEncoding()) == null)) {
					charset = ACLCodec.DEFAULT_CHARSET;
				}
				codec.decode(codec.encode(msg,charset),charset);
			} catch (ACLCodec.CodecException ce) {
				ce.printStackTrace();
			}
		}
	}

}
