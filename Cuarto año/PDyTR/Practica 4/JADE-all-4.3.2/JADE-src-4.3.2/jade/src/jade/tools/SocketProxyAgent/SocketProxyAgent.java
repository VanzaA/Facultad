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
import java.util.*;

import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.*;
import jade.core.behaviours.SimpleBehaviour;
import jade.util.Logger;

/**
 * Agent to bridge ACL messages between JADE and the outside using a socket.
 * The agent(s) to talk to and the port to be used may be specified:
 * <ol>
 * <li>as the two String arguments to the agent, or</li>
 * <li>if no agent argument, then in a configuration file 
 * <i>agentname</i><code>.inf</code>, or if no configuration file, then</li>
 * <li>default to {@link #DEFAULT_PORT} and all agents</li>
 * </ol>
 * The format for the arguments is:
 * <ol>
 * <li>agent arguments: argument 0 is port number, argument 1 legal addressee agents</li>
 * <li>configuration file: line 0 is port number, line 1 legal addressee agents</li>
 * </ol>
 * Specifying an asterisk (<code>*</code>) for the agents
 * will permit all agents to be legal addressees. 
 * @author Fabio Bellifemine - CSELT S.p.A
 * @author (HP modifications) Dick Cowan, David Bell, Sebastien Siva
 * @author David Bernstein - Caboodle Networks, Inc.
 */
public class SocketProxyAgent extends Agent {

	/** the default port to listen on */
	public final static int DEFAULT_PORT = 6789;

	/** my logger */
	private final static Logger logger = Logger.getMyLogger(SocketProxyAgent.class.getName());
	/** reader for parameters */
	private BufferedReader in;
	/** the thread doing connection on desired port */
	private Server proxyServer;
	/** port we're using */
	int portNumber  = DEFAULT_PORT;

	/**
	 * agent setup
	 */
	protected void setup() {
		try {

			String myName = getLocalName();

			logger.log( Logger.CONFIG, "My agent name:" + myName ); 


			Vector agentNames = new Vector();
			String fileName = myName + ".inf";
			String configSource = "*unset!*";
			try {

				Reader reader;
				Object arguments[] = getArguments();
				if ( ( null != arguments ) && 
						( null != arguments[0] ) &&
						( arguments[0] instanceof String ) ) {
					String arg0 = (String)arguments[0]; 
					String arg1 = "*";
					if ( arguments.length > 1 ) {
						arg1 = (String)arguments[1]; 
					}
					configSource = "agent arguments: \""+arg0+"\", \""+arg1+"\"";
					reader = new StringReader( arg0 + "\n" + arg1 + "\n" ); 
				}
				else {
					configSource = "file \""+fileName + "\"";
					reader = new FileReader( fileName ); 
				}
				logger.log( Logger.CONFIG, "reading configuration from " + configSource );

				in = new BufferedReader( reader );
				portNumber = Integer.parseInt(in.readLine());
				StringTokenizer st = new StringTokenizer(in.readLine());

				//verify if the name of the agents have the hap or not. 
				//If not add the local hap (of the dfproxy agent).
				while (st.hasMoreTokens()) {
					String name = st.nextToken();
					if (!name.equals("*")) {
						int atPos = name.lastIndexOf('@');

						if (atPos == -1) {
							name = name + "@" + getHap();
						}
					}
					if ( logger.isLoggable( Logger.FINE ) ) {
						logger.log( Logger.FINE, "Legal addressee:" + name);
					}
					agentNames.add(name);
				}
			}
			catch(Exception e) {
				logger.log( Logger.WARNING, "Unable to read configuration from "+configSource +
				", so will use default settings.");
				portNumber = DEFAULT_PORT; // Force Server to use default port
				agentNames.add("*");  // Allow messages to any agent.
			}
			if ( logger.isLoggable( Logger.FINE ) ) {
				logger.log( Logger.FINE, 
						"Attempting to open a server socket on port: " +portNumber);
				int agentNameCount = agentNames.size();
				if ( 0 == agentNameCount ) {
					logger.log( Logger.SEVERE, "no agent names!");
				}
				else {
					for ( int i = 0; i < agentNameCount; i++ ) {
						logger.log( Logger.FINE, "agent name "+i+": \""+
								agentNames.get(i)+"\"");
					}
				}
			}
			proxyServer = new Server(portNumber, this, agentNames);
		}
		catch (Exception e) {
			logger.log( Logger.SEVERE, "Failed to start server socket" + e, e );
			e.printStackTrace();
			doDelete();
		}
	}

	/**
	 * get port used
	 * @return port used
	 */
	public int getPort() {
		return portNumber;
	}

	private final static int ONE_SEC_AS_MS = 1000;

	/**
	 * agent takedown
	 */
	protected void takeDown() {
		try {
			if (in != null) {
				in.close();
				in = null;
			}
		}
		catch (Exception e) {
			// intentionally empty
		}

		try {
			if (proxyServer != null) {
				proxyServer.interrupt();
				proxyServer.closeDown();
				proxyServer.join(ONE_SEC_AS_MS);
				proxyServer = null;
			}
		}
		catch (Exception e) {
			// intentionally empty
		}
	}

}
