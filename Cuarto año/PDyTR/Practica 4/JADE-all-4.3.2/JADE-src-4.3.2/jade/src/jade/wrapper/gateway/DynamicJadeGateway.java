package jade.wrapper.gateway;

//#J2ME_EXCLUDE_FILE
//#ANDROID_EXCLUDE_FILE

import java.util.ArrayList;
import java.util.List;

import jade.core.AID;
import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.Event;
import jade.util.Logger;
import jade.util.leap.Properties;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;

public class DynamicJadeGateway {

	private static final int UNKNOWN = -1;
	private static final int ACTIVE = 1;
	private static final int NOT_ACTIVE = 2;
	
	ContainerController myContainer = null;
	AgentController myAgent = null;
	private String agentType = GatewayAgent.class.getName();
	private String agentName = null;
	// jade profile properties
	ProfileImpl profile;
	private Properties jadeProps;
	private Object[] agentArguments;
	
	private int gatewayAgentState = UNKNOWN;
	//#DOTNET_EXCLUDE_BEGIN
	private List<GatewayListener> listeners = new ArrayList<GatewayListener>();
	private volatile GatewayListener[] listenersArray = new GatewayListener[0];
	//#DOTNET_EXCLUDE_END
	
	private static Logger myLogger = Logger.getMyLogger(DynamicJadeGateway.class.getName());
	
	
	/** Searches for the property with the specified key in the JADE Platform Profile. 
	 *	The method returns the default value argument if the property is not found. 
	 * @param key - the property key. 
	 * @param defaultValue - a default value
	 * @return the value with the specified key value
	 * @see java.util.Properties#getProperty(String, String)
	 **/
	public final String getProfileProperty(String key, String defaultValue) {
		return profile.getParameter(key, defaultValue);
	}
	
	/**
	 * execute a command. 
	 * This method first check if the executor Agent is alive (if not it
	 * creates container and agent), then it forwards the execution
	 * request to the agent, finally it blocks waiting until the command
	 * has been executed (i.e. the method <code>releaseCommand</code> 
	 * is called by the executor agent)
	 * @throws StaleProxyException if the method was not able to execute the Command
	 * @see jade.wrapper.AgentController#putO2AObject(Object, boolean)
	 **/
	public final void execute(Object command) throws StaleProxyException,ControllerException,InterruptedException {
		execute(command, 0);
	}
	
	/**
	 * Execute a command specifying a timeout. 
	 * This method first check if the executor Agent is alive (if not it
	 * creates container and agent), then it forwards the execution
	 * request to the agent, finally it blocks waiting until the command
	 * has been executed. In case the command is a behaviour this method blocks 
	 * until the behaviour has been completely executed. 
	 * @throws InterruptedException if the timeout expires or the Thread
	 * executing this method is interrupted.
	 * @throws StaleProxyException if the method was not able to execute the Command
	 * @see jade.wrapper.AgentController#putO2AObject(Object, boolean)
	 **/
	public final void execute(Object command, long timeout) throws StaleProxyException,ControllerException,InterruptedException {
		Event e = null;
		synchronized (this) {
			checkJADE();
			// incapsulate the command into an Event
			e = new Event(-1, command);
			try {
				if (myLogger.isLoggable(Logger.INFO)) 
					myLogger.log(Logger.INFO, "Requesting execution of command "+command);
				myAgent.putO2AObject(e, AgentController.ASYNC);
			} catch (StaleProxyException exc) {
				exc.printStackTrace();
				// in case an exception was thrown, restart JADE
				// and then reexecute the command
				restartJADE();
				myAgent.putO2AObject(e, AgentController.ASYNC);
			}
		}
		// wait until the answer is ready
		e.waitUntilProcessed(timeout);
	}
	
	/**
	 * This method checks if both the container, and the agent, are up and running.
	 * If not, then the method is responsible for renewing myContainer.
	 * Normally programmers do not need to invoke this method explicitly.
	 **/
	public final void checkJADE() throws StaleProxyException,ControllerException {
		if (myContainer == null || !myContainer.isJoined()) {
			initProfile();
			
			myContainer = Runtime.instance().createAgentContainer(profile); 
			if (myContainer == null) {
				throw new ControllerException("JADE startup failed.");
			}
		}
		if (myAgent == null) {
			try {
				Agent a = (Agent) Class.forName(agentType).newInstance();
				if (a instanceof GatewayAgent) {
					//#DOTNET_EXCLUDE_BEGIN
					((GatewayAgent) a).setListener(new GatewayListenerImpl());
					//#DOTNET_EXCLUDE_END
					
					// We are able to detect the GatewayAgent state only if the internal agent is a GatewayAgent instance
					gatewayAgentState = NOT_ACTIVE;
				}
				a.setArguments(agentArguments);
				if (agentName == null) {
					agentName = "Control"+myContainer.getContainerName();
				}
				myAgent = myContainer.acceptNewAgent(agentName, a);
				
				if (gatewayAgentState == NOT_ACTIVE) {
					// Set the ACTIVE state synchronously so that when checkJADE() completes isGatewayActive() certainly returns true 
					gatewayAgentState = ACTIVE;
				}
				myAgent.start();
			}
			catch (StaleProxyException spe) {
				// Just let it through
				throw spe;
			}
			catch (Exception e) {
				throw new ControllerException("Error creating GatewayAgent [" + e + "]");
			}
		}
	}
	
	/** Restart JADE.
	 * The method tries to kill both the agent and the container,
	 * then it puts to null the values of their controllers,
	 * and finally calls checkJADE
	 **/
	final void restartJADE() throws StaleProxyException,ControllerException {
		shutdown();
		checkJADE();
	}
	
	/**
	 * Initialize this gateway by passing the proper configuration parameters
	 * @param agentName is the name of the JadeGateway internal agent. If null is passed
	 * the default name will be used.
	 * @param agentClassName is the fully-qualified class name of the JadeGateway internal agent. If null is passed
	 * the default class will be used.
	 * @param agentArgs is the list of agent arguments
	 * @param jadeProfile the properties that contain all parameters for running JADE (see jade.core.Profile).
	 * Typically these properties will have to be read from a JADE configuration file.
	 * If jadeProfile is null, then a JADE container attaching to a main on the local host is launched
	 **/
	public final void init(String agentName, String agentClassName, Object[] agentArgs, Properties jadeProfile) {
		this.agentName = agentName;
		
		if (agentClassName != null) {
			agentType = agentClassName;
		} else {
			agentType = GatewayAgent.class.getName();
		}
		
		jadeProps = jadeProfile;
		if (jadeProps != null) {
			// Since we will create a non-main container --> force the "main" property to be false
			jadeProps.setProperty(Profile.MAIN, "false");
		}
		
		agentArguments = agentArgs;
	}
	
	/**
	 * Initialize this gateway by passing the proper configuration parameters
	 * @param agentClassName is the fully-qualified class name of the JadeGateway internal agent. If null is passed
	 * the default class will be used.
	 * @param agentArgs is the list of agent arguments
	 * @param jadeProfile the properties that contain all parameters for running JADE (see jade.core.Profile).
	 * Typically these properties will have to be read from a JADE configuration file.
	 * If jadeProfile is null, then a JADE container attaching to a main on the local host is launched
	 **/
	public final void init(String agentClassName, Object[] agentArgs, Properties jadeProfile) {
		init(null, agentClassName, agentArgs, jadeProfile);
	}

	public final void init(String agentClassName, Properties jadeProfile) {
		init(agentClassName, null, jadeProfile);
	}
	
	final void initProfile() {
		// to initialize the profile every restart, otherwise an exception would be thrown by JADE
		profile = (jadeProps == null ? new ProfileImpl(false) : new ProfileImpl(jadeProps));
	}
	
	/**
	 * Kill the JADE Container in case it is running.
	 */
	public void shutdown() {
		try { // try to kill, but neglect any exception thrown
			if (myAgent != null)
				myAgent.kill();
		} catch (Exception e) {
		}
		try { // try to kill, but neglect any exception thrown
			if (myContainer != null)
				myContainer.kill();
		} catch (Exception e) {
		}
		myAgent = null;
		myContainer = null;
	}
	
	/**
	 * Return the state of JadeGateway
	 * @return true if the container and the gateway agent are active, false otherwise
	 */
	public final boolean isGatewayActive() {
		if (gatewayAgentState != UNKNOWN) {
			return gatewayAgentState == ACTIVE;
		}
		else {
			// If we are not able to monitor the actual gatewayAgentState, just check if myContainer and myAgent are not null
			return myContainer != null && myAgent != null;
		}
	}
	
	public AID createAID(String localName) {
		return new AID(localName+'@'+myContainer.getPlatformName(), AID.ISGUID);
	}
	
	//#DOTNET_EXCLUDE_BEGIN
	public void addListener(GatewayListener l) {
		listeners.add(l);
		listenersArray = listeners.toArray(new GatewayListener[0]);
	}
	
	public void removeListener(GatewayListener l) {
		if (listeners.remove(l)) {
			listenersArray = listeners.toArray(new GatewayListener[0]);
		}
	}
	
	/**
	 * Inner class GatewayListenerImpl
	 */
	private class GatewayListenerImpl implements GatewayListener {
		public void handleGatewayConnected() {
			// This is executed by the GatewayAgent Thread --> Notify listeners by means of an ad-hoc 
			// Thread to avoid deadlocks with other threads waiting for the execute() method to complete
			Thread t = new Thread() {
				public void run() {
					for (GatewayListener listener : listenersArray) {
						try {
							listener.handleGatewayConnected();
						}
						catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			};
			t.start();
		}
		
		public void handleGatewayDisconnected() {
			gatewayAgentState = NOT_ACTIVE;
			myAgent = null;
			
			// This is executed by the GatewayAgent Thread --> Notify listeners by means of an ad-hoc 
			// Thread to avoid deadlocks with other threads waiting for the execute() method to complete
			Thread t = new Thread() {
				public void run() {
					for (GatewayListener listener : listenersArray) {
						try {
							listener.handleGatewayDisconnected();
						}
						catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			};
			t.start();
		}
	} // END of inner class GatewayListenerImpl
	//#DOTNET_EXCLUDE_END
}
