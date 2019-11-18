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

package jade.tools.SocketProxyAgent;

import java.io.*;
import java.net.*;
import java.util.*;

import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.*;
import jade.core.behaviours.SimpleBehaviour;
import jade.util.Logger;

/**
 * thread to listen on socket and copy to receivers
 */
class Connection extends Thread {
	/** my logger */
	private final static Logger logger = 
		Logger.getMyLogger(Server.class.getName());
	private Agent myAgent;
	private Socket client;
	private DataInputStream in;
	private PrintStream out;
	private boolean done = false;
	private boolean closed = false;

	/** Name of the agents who intend to receive any message from this agent */
	private Vector allowedNames;

	/**
	 * this class is a thread to listen on socket and copy to receivers
	 * @param client_socket socket to talk outside JADE with
	 * @param a proxy agent talking to
	 * @param receivers agents to forward messages to
	 */
	Connection(Socket client_socket, Agent a, Vector receivers)
	{
		myAgent = a;
		String threadName = 
			myAgent.getLocalName() + "-ClientConnection-" + getName();
		// The thread name must not contain any spaces because it gets used
		// as the value for the reply-with field in the ACL message.
		threadName = threadName.trim().replace(' ', '_');
		setName(threadName);
		myAgent = a;
		client = client_socket;
		allowedNames = receivers;

		try {
			in = new DataInputStream(client.getInputStream());
			out = new PrintStream(client.getOutputStream(), true);
		} 
		catch (IOException e) {
			try {
				client.close();
			}
			catch (IOException e2) {
				// intentionally empty
			}

			e.printStackTrace();

			return;
		}

		start();
	}

	/**
	 * Validate ALL the agent names of the recipients in the ACL message.
	 * @param msg The ACL message which we wish to send.
	 * @return True if they are all allowed, false otherwise.
	 */
	private boolean allValidReceivers(ACLMessage msg) {
		if (allowedNames == null) {
			return true;
		}
		jade.util.leap.Iterator itor = msg.getAllReceiver();
		while (itor.hasNext()) {
			if (!isValidReceiver(((AID)itor.next()).getName())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Validate a single agent name.
	 * @param aName The intended agent's name.
	 * @return True if its allowed, false otherwise.
	 */
	private boolean isValidReceiver(String aName) {
		for (int i = 0; i < allowedNames.size(); i++) {
			String allowName = (String)allowedNames.elementAt(i);
			if ( allowName.equals("*") ||
					allowName.equalsIgnoreCase(aName)) {
				if ( logger.isLoggable( Logger.FINER ) ) {
					logger.log( Logger.FINER, aName + " is allowed");
				}
				return true;
			}
		}
		if ( logger.isLoggable( Logger.FINER ) ) {
			logger.log( Logger.FINER, aName + " is NOT allowed");
		}
		return false;
	}

	/**
	 * Fix any receiver names which are missing the @ part.
	 * @param msg The ACL message which we wish to send.
	 */
	private void fixReceiverNames(ACLMessage msg) {
		jade.util.leap.Iterator itor = msg.getAllReceiver();
		while (itor.hasNext()) {
			AID receiverAID = (AID)itor.next();
			String receiverName = receiverAID.getName();
			if (receiverName.indexOf('@') < 0) {
				msg.removeReceiver(receiverAID);
				AID newAID = new AID(receiverName, AID.ISLOCALNAME);
				msg.addReceiver(newAID);
				if ( logger.isLoggable( Logger.FINE ) ) {
					logger.log( Logger.FINE, "Changed receiver " + receiverName +
							" to " + newAID.getName());
				}
			}
		}
	}

	/**
	 * Parses ACL messages from the input stream. It is written to
	 * take multiple messages from the stream. For each message it
	 * gets from the input stream it 1) sends it into the Jade world
	 * and 2) starts a new behavior to wait for the response. When that
	 * behavior gets the response (or times out) it will send a message
	 * back out the socket.
	 */
	public void run() {
		boolean gotOneMessage = false;
		ACLMessage msg = null;
		try {
			ACLParser parser = new ACLParser(in);

			while (true) {
				// This may throw a parsing exception due to
				// end of file (stream).
				msg = parser.Message(); 
				gotOneMessage = true;  // OK to ignore EOF now
				if ( logger.isLoggable( Logger.FINE ) ) {
					logger.log( Logger.FINE , "Received message:" + msg);
				}

				fixReceiverNames(msg);
				if (allValidReceivers(msg)) {
					msg.setSender(myAgent.getAID());

					if ((msg.getReplyWith() == null) || (msg.getReplyWith().length() < 1)) {
						msg.setReplyWith(myAgent.getLocalName() + "."
								+ getName() + "."
								+ java.lang.System
								.currentTimeMillis());
					}

					if ( msg.getInReplyTo() == null ) {
						msg.setInReplyTo( "noValue" );
					}

					if ( logger.isLoggable( Logger.FINE ) ) {
						jade.util.leap.Iterator itor = msg.getAllReceiver();
						StringBuffer sb = new StringBuffer();
						while (itor.hasNext()) {
							AID aid = (AID)itor.next();
							sb.append(aid.getName());
							if (itor.hasNext()) {
								sb.append(" ");
							}
						}
						logger.log( Logger.FINE, "Sending message to:" + sb.toString());
						logger.log( Logger.FINE, msg.toString());
					}
					myAgent.send(msg);

					myAgent.addBehaviour(new WaitAnswersBehaviour(myAgent, msg, out));
				} else {
					logger.log( Logger.WARNING, "Unauthorized recipient.");
					out.println("(refuse :content unauthorised)");
					out.flush();
					close();

					return;
				}
			}
		} catch (Throwable any) {

			// Jade puts "<EOF>" in the exception message
			if ( (gotOneMessage) && (any.getMessage() != null) && (any.getMessage().indexOf("<EOF>") >= 0) ) {
				// ignore it
			} else {
				msg = new ACLMessage(ACLMessage.FAILURE);
				msg.setContent("( \"Error: " + any + "\" )");
				logger.log( Logger.WARNING, "Writing error message to socket.");
				logger.log(Logger.WARNING, msg.toString());
				out.println(msg.toString());
				out.flush();
			}
			close();
			return;
		}
	}


	protected void finalize() {
		if (!closed) {
			close();
		}
	}

	/**
	 * Close all our stuff.
	 */
	void close() {

		try {
			if (client != null) {
				client.close();
				client = null;
			}
		} catch (Exception e) {}

		try {
			if (in != null) {
				in.close();
				in = null;
			}
		} catch (Exception e) {}

		try {
			if (out != null) {
				out.close();
				out = null;
			}
		} catch (Exception e) {}
		closed = true;
	}
}
