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


import java.net.*;
import java.io.*;
import java.util.*;

import jade.core.Agent;
import jade.util.Logger;

/**
 * this class is a thread to listen for connections on the desired port.
 */
class Server extends Thread
{

	/** my logger */
	private final static Logger logger = 
		Logger.getMyLogger(Server.class.getName());
	private ServerSocket listen_socket;
	private Agent myAgent;
	private Vector myOnlyReceivers;
	private boolean done = false;
	private Socket client_socket;
	private Connection c;

	/**
	 * Constructor of the class.
	 * It creates a ServerSocket to listen for connections on.
	 * @param port is the port number to listen for. If 0, then it uses
	 * the default port number.
	 * @param a is the pointer to agent to be used to send messages.
	 * @param receivers vector with the names of all the agents that
	 * wish to receive messages through this proxy.
	 */
	Server(int port, Agent a, Vector receivers) {
		myAgent = a;
		setName (myAgent.getLocalName() + "-SocketListener");
		if (port == 0) {
			port = SocketProxyAgent.DEFAULT_PORT;
		}

		myOnlyReceivers = receivers;

		try {
			listen_socket = new ServerSocket(port);
		}
		catch (IOException e) {
			e.printStackTrace();
			myAgent.doDelete();

			return;
		}

		logger.log( Logger.CONFIG, getName() + ": Listening on port: " + port);
		start();
	}

	/**
	 * The body of the server thread. 
	 * It is executed when the start() method of the server object is called.
	 * Loops forever, listening for and accepting connections from clients.
	 * For each connection, creates a Connection object to handle communication
	 * through the new Socket. Each Connection object is a new thread.
	 * The maximum queue length for incoming connection indications
	 * (a request to connect) is set to 50 (that is the default for the
	 * ServerSocket constructor). If a connection indication
	 * arrives when the queue is full, the connection is refused.
	 */
	public void run() {
		try {
			done = false;
			while (!done) {
				client_socket = listen_socket.accept();

				if ( logger.isLoggable( Logger.FINE ) ) {
					logger.log( Logger.FINE, "New Connection with "
							+ client_socket.getInetAddress().toString()
							+ " on remote port " + client_socket.getPort());
				}

				c = new Connection(client_socket, myAgent, myOnlyReceivers);
			}
		}
		catch (IOException e) {
			// If the done flag is still false, then we had an unexpected
			// IOException.
			if (!done) {
				logger.log( Logger.WARNING, getName() + " IOException: " + e);
				myAgent.doDelete();
			}
		}
		finally {
			finalize();
		}
	}

	/**
	 * stop listening
	 */
	protected void closeDown() {
		done = true;
		try {
			if (listen_socket != null) {
				listen_socket.close();
				listen_socket = null;
			}
		}
		catch (Exception e) {
			// Do nothing
		}
	}

	/**
	 * try to clean up on GC
	 */
	protected void finalize() {
		closeDown();

		try	{
			if (client_socket != null) {
				client_socket.close();
				client_socket = null;
			}
		}
		catch (Exception e) {
			// Do nothing            
		}

		try	{
			if (c != null) {
				if (c.isAlive()) {
					c.close();
				}
				c.join(1000);
				c = null;
			}
		}
		catch (Exception e) {
			// Do nothing            
		}
	}

}