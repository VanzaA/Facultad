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

package jade.core;

import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.util.Logger;
import jade.util.leap.Iterator;
import jade.util.leap.Properties;
//#MIDP_EXCLUDE_BEGIN
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
//#MIDP_EXCLUDE_END

/**
 This class is used to start up the JADE runtime as a split (front-end) 
 container. Though
 JADE supports split containers on all Java editions, the split
 container deployment is better suited for small, resource
 constrained devices (MIDP and PJava).
 @author Giovanni Caire - TILAB
 */
public class MicroRuntime {

	/**
	 The configuration property key that maps to the list of agents 
	 that have to be activated at boostrap.
	 */
	public static final String AGENTS_KEY = "agents";

	/**
	 The configuration property key that maps to the list of services 
	 that have to be installed.
	 */
	public static final String SERVICES_KEY = "services";

	/**
	   The key to retrieve the owner of the starting container.
	   This information is the base for using a split container in a JADE platform with some form of security
	   as it is automatically propagated to the back-end.
	   For instance to connect a split-container to a platform using JADE-S
	   Front-End: In the startup peroperties specify 
	   - owner = <username>:<password>
	   Back-End: In the leap property file of the JICPMediatorManager
	   that will host the BackEnd specify
	   - services = ....<security services>...
	   - jade_security_authentication_logincallback = cmdline
	 */
	public static final String OWNER_KEY = "owner";	
	
	/**
	 The configuration property key that maps to the host where to connect to
	 the JADE mediator.
	 */
	public static final String HOST_KEY = "host";	

	/**
	 The configuration property key that maps to the port where to connect to
	 the JADE mediator.
	 */
	public static final String PORT_KEY = "port";	

	/**
	 The configuration property key that maps to the protocol that must be used to connect to
	 the JADE mediator. Possible values are: <code>socket, ssl, http, https</code>
	 */
	public static final String PROTO_KEY = "proto";
		
	/**
	 * The configuration property key that specifies how much time by default a command that is 
	 * being delivered during a temporary disconnection must be stored waiting for the FE
	 * to reconnect. After that time the command fails (in case of a message delivery command,
	 * a FAILURE is sent back to the sender).<br> 
	 * 0 means store and forward disabled
	 * -1 means infinite timeout
	 */
	public static final String DEFAULT_SF_TIMEOUT_KEY = "default-sf-timeout";
		
	public static final String REMOTE_CONFIG_HOST_KEY = "remote-config-host";
	public static final String REMOTE_CONFIG_PORT_KEY = "remote-config-port";
	
    public static final String SOCKET_PROTOCOL = "socket";
    public static final String SSL_PROTOCOL = "ssl";
    public static final String HTTP_PROTOCOL = "http";
    public static final String HTTPS_PROTOCOL = "https";
    
	//#APIDOC_EXCLUDE_BEGIN
	public static final String CONN_MGR_CLASS_KEY = "connection-manager";	

	public static final String CONTAINER_NAME_KEY = "container-name";
	public static final String PLATFORM_KEY = "platform-id";
	public static final String PLATFORM_ADDRESSES_KEY = "addresses";
	public static final String BE_REQUIRED_SERVICES_KEY = "be-required-services";

	public static final String JVM_KEY = "jvm";

	public static final String J2SE = "j2se";
	public static final String PJAVA = "pjava";
	public static final String MIDP = "midp";
	//#APIDOC_EXCLUDE_END

	//#J2ME_EXCLUDE_BEGIN
	private static Logger logger = Logger.getJADELogger(MicroRuntime.class.getName());
	//#J2ME_EXCLUDE_END
	/*#J2ME_INCLUDE_BEGIN
	private static Logger logger = Logger.getJADELogger("jade.core.MicroRuntime");
	#J2ME_INCLUDE_END*/

	private static Runnable terminator;
	private static FrontEndContainer myFrontEnd;
	private static boolean terminated;
	
	/**
	 Start up the JADE runtime. This method launches a JADE
	 Front End container. Since JADE supports only one container
	 in the split-container deployment, if a Front End is
	 already running this method does nothing.

	 @param p A property bag, containing name-value pairs used
	 to configure the container during boot.
	 @param r A <code>Runnable</code> object, whose
	 <code>run()</code> method will be executed just after
	 container termination.
	 */
	public static void startJADE(Properties p, Runnable r) {
		if (myFrontEnd == null) {
			terminator = r;
			terminated = false;
			myFrontEnd = new FrontEndContainer();
			myFrontEnd.start(p);
			if (terminated) {
				myFrontEnd = null;
			}
		}
	}

	/**
	 Shut down the JADE runtime. This method stops the JADE
	 Front End container currently running in this JVM, if one
	 such container exists.
	 */
	public static void stopJADE() {
		if (myFrontEnd != null) {
			try {
				// Self-initiated shutdown
				myFrontEnd.exit(true);
			}
			catch (IMTPException imtpe) {
				// Should never happen as this is a local call
				imtpe.printStackTrace();
			}
		}
	}

	/**
	 Tells whether a JADE Front End container is currently
	 running within this JVM.

	 @return If the JADE runtime is currently running,
	 <code>true</code> is returned. Otherwise, the method
	 returns <code>false</code>.
	 */
	public static boolean isRunning() {
		return myFrontEnd != null;
	}
	
	public static String getContainerName() {
		if (myFrontEnd != null) {
			Location cid = myFrontEnd.here();
			if (cid != null) {
				return cid.getName();
			}
		}
		return null;
	}

	/**
	 Start a new agent. This method starts a new agent within
	 the active Front End container.

	 @param name The local name (i.e. without the platform ID)
	 of the agent to create.
	 @param className The fully qualified name of the class
	 implementing the agent to start.
	 @param args The creation arguments for the agent.

	 @throws Exception If the underlying agent creation process
	 fails.
	 */
	public static void startAgent(String name, String className, String[] args) throws Exception {
		if (myFrontEnd != null) {
			try {
				myFrontEnd.createAgent(name, className, args);
			}
			catch (IMTPException imtpe) {
				// This is a local call --> an IMTPxception always wrap some other exception
				throw (Exception) imtpe.getNested();
			}
		}
	}

	/**
	 Start a new agent. This method starts a new agent within
	 the active Front End container.

	 @param name The local name (i.e. without the platform ID)
	 of the agent to create.
	 @param className The fully qualified name of the class
	 implementing the agent to start.
	 @param args The creation arguments for the agent.

	 @throws Exception If the underlying agent creation process
	 fails.
	 */
	public static void startAgent(String name, String className, Object[] args) throws Exception {
		if (myFrontEnd != null) {
			try {
				myFrontEnd.createAgent(name, className, args);
			}
			catch (IMTPException imtpe) {
				// This is a local call --> an IMTPxception always wrap some other exception
				throw (Exception) imtpe.getNested();
			}
		}
	}

	/**
	 Kill an agent. This method terminates an agent running
	 within the active Front End container.

	 @param name The local name (i.e. without the platform ID)
	 of the agent to kill.
	 @throws NotFoundException If no agent with the given local
	 name are running within the active Front End.
	 */
	public static void killAgent(String name) throws NotFoundException {
		if (myFrontEnd != null) {
			try {
				myFrontEnd.killAgent(name);
			}
			catch (IMTPException imtpe) {
				// Should never happen as this is a local call
				imtpe.printStackTrace();
			}
		}
	}

	//#MIDP_EXCLUDE_BEGIN
	/**
	 * Get agent proxy to local agent given its name.
	 * <br>
	 * <b>NOT available in MIDP</b>
	 * <br>
	 * @param localAgentName The short local name of the desired agent.
	 * @throws ControllerException If any problems occur obtaining this proxy.
	 */
	public static AgentController getAgent(String localName) throws ControllerException {
		if(myFrontEnd == null) {
			throw new ControllerException("FrontEndContainer  not found");
		}
		// Check that the agent exists
		jade.core.Agent instance = myFrontEnd.getLocalAgent(localName);
		if (instance == null) {
			throw new ControllerException("Agent " + localName + " not found.");
		} 
		return new MicroAgentControllerImpl(localName, myFrontEnd);
	}
	
	/**
	 * Add a listener that will be notified about Front-End relevant events such as 
	 * BORN_AGENT and DEAD_AGENT
	 * <br>
	 * <b>NOT available in MIDP</b>
	 * <br>
	 * @param l The listener that will be notified about Front-End relevant events
	 */
	public static void addListener(FEListener l) {
		if (myFrontEnd != null) {
			myFrontEnd.addListener(l);
		}
	}

	/**
	 * Remove a listener to Front-End relevant events 
	 * BORN_AGENT and DEAD_AGENT
	 * <br>
	 * <b>NOT available in MIDP</b>
	 * <br>
	 * @param l The listener to be removed
	 */
	public static void removeListener(FEListener l) {
		if (myFrontEnd != null) {
			myFrontEnd.removeListener(l);
		}
	}
	//#MIDP_EXCLUDE_END

	/**
	 * Disconnect this front-end container from the platform. That is, though the local
	 * front-end container and its agent(s) remain active, the back-end is shut down and,
	 * of course, the FE-2-BE connection is closed --> Therefore the rest of the platform 
	 * does not see the local container and its agent(s) anymore.
	 * The local container automatically re-connects to the platform (and local agents are 
	 * registered with the AMS again) as soon as a message to a remote agent is sent.   
	 */
	public static void detach() {
		if (myFrontEnd != null) {
			myFrontEnd.detach();
		}
	}
	
	//#APIDOC_EXCLUDE_BEGIN
	public static void notifyFailureToSender(ACLMessage msg, String sender, String error) {
		if (myFrontEnd != null) {
			// Send back a failure message
			try {
				Iterator it = msg.getAllReceiver();
				while (it.hasNext()) {
					AID receiver = (AID) it.next();
					// If this receiver is local, the message has certainly been delivered 
					// successfully --> Do not send any FAILURE back for this receiver
					if (myFrontEnd.getLocalAgent(receiver.getLocalName()) == null) { 
						ACLMessage failure = msg.createReply();
						failure.setPerformative(ACLMessage.FAILURE);
						failure.setSender(myFrontEnd.getAMS());
						failure.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
						String content = "( (action " + sender;
				        content = content + " (ACLMessage) ) (MTS-error " + receiver + " \"" + error + "\") )";
						failure.setContent(content);
						myFrontEnd.messageIn(failure, sender);
					}
				}
			} catch (Exception e1) {
				logger.log(Logger.SEVERE, "Error delivering FAILURE message.", e1);
			}
		}		
	}
	//#APIDOC_EXCLUDE_END
	
	/**
	 Activate the Java environment terminator when the JADE runtime
	 has stopped.
	 */
	static void handleTermination(boolean self) {
		terminated = true;
		myFrontEnd = null;
		Thread t = null;
		if (self) {
			t = new Thread(terminator);
		}
		else {
			// If the termination was activated from remote, then let
			// the current thread complete before closing down everything
			final Thread current = Thread.currentThread();
			t = new Thread(new Runnable() {
				public void run() {
					try {
						current.join();
					}
					catch (InterruptedException ie) {
						logger.log(Logger.SEVERE,"Interrupted in join");
					}
					if (terminator != null) {
						terminator.run();
					}
				}
			} );
		}
		t.start();
	}
}

