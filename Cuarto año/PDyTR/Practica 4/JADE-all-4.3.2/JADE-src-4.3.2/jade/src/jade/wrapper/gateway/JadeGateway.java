package jade.wrapper.gateway; 

//#J2ME_EXCLUDE_FILE
//#ANDROID_EXCLUDE_FILE

import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.util.Event;
import jade.util.leap.Properties;
import jade.util.Logger;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;

/**
 * This class provides a simple yet powerful gateway between some non-JADE code and a JADE based 
 * multi agent system. It is particularly suited to be used inside a Servlet or a JSP.
 * The class maintains an internal JADE agent (of class <code>GatewayAgent</code>
 * that acts as entry point in the JADE based system.
 * The activation/termination of this agent (and its underlying container) are completely managed
 * by the JadeGateway class and developers do not need to care about them.
 * The suggested way of using the JadeGateway class is creating proper behaviours that perform the commands 
 * that the external system must issue to the JADE based system and pass them as parameters to the execute() 
 * method. When the execute() method returns the internal agent of the JadeGateway as completely executed
 * the behaviour and outputs (if any) can be retrieved from the behaviour object using ad hoc methods 
 * as exemplified below.<br>
 <code>
 DoSomeActionBehaviour b = new DoSomeActionBehaviour(....);<br>
 JadeGateway.execute(b); // At this point b has been completely executed --> we can get results<br>
 result = b.getResult();<br>
 </code>
 * <br>
 * When using the JadeGateway class as described above <code>null</code> should be
 * passed as first parameter to the <code>init()</code> method. 
 * <p> Alternatively programmers can
 * <ul>
 * <li> create an application-specific class that extends <code>GatewayAgent</code>, that redefine its method <code>processCommand</code>
 * and that is the agent responsible for processing all command-requests
 * <li> initialize this JadeGateway by calling its method <code>init</code> with the
 * name of the class of the application-specific agent 
 * <li> finally, in order to request the processing of a Command, you must call the method <code>JadeGateway.execute(Object command)<code>.
 * This method will cause the callback of
 * the method <code>processCommand</code> of the application-specific agent.
 * The method <code>execute</code> will return only after the method <code>GatewayAgent.releaseCommand(command)</code> has been called
 * by your application-specific agent.
 * </ul>
 * <b>NOT available in MIDP</b>
 * @author Fabio Bellifemine, Telecom Italia LAB
 * @version $Date: 2013-10-08 11:45:49 +0200 (mar, 08 ott 2013) $ $Revision: 6688 $
 **/
public class JadeGateway {
	
	private static DynamicJadeGateway defaultGateway = new DynamicJadeGateway();
	
	/** Searches for the property with the specified key in the JADE Platform Profile. 
	 *	The method returns the default value argument if the property is not found. 
	 * @param key - the property key. 
	 * @param defaultValue - a default value
	 * @return the value with the specified key value
	 * @see java.util.Properties#getProperty(String, String)
	 **/
	public final static String getProfileProperty(String key, String defaultValue) {
		return defaultGateway.getProfileProperty(key, defaultValue);
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
	public final static void execute(Object command) throws StaleProxyException,ControllerException,InterruptedException {
		defaultGateway.execute(command);
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
	public final static void execute(Object command, long timeout) throws StaleProxyException,ControllerException,InterruptedException {
		defaultGateway.execute(command, timeout);
	}
	
	/**
	 * This method checks if both the container, and the agent, are up and running.
	 * If not, then the method is responsible for renewing myContainer.
	 * Normally programmers do not need to invoke this method explicitly.
	 **/
	public final static void checkJADE() throws StaleProxyException,ControllerException {
		defaultGateway.checkJADE();
	}
	
	/** Restart JADE.
	 * The method tries to kill both the agent and the container,
	 * then it puts to null the values of their controllers,
	 * and finally calls checkJADE
	 **/
	private final static void restartJADE() throws StaleProxyException,ControllerException {
		defaultGateway.restartJADE();
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
	public final static void init(String agentClassName, Object[] agentArgs, Properties jadeProfile) {
		defaultGateway.init(agentClassName, agentArgs, jadeProfile);
	}

	public final static void init(String agentClassName, Properties jadeProfile) {
		defaultGateway.init(agentClassName, jadeProfile);
	}
	
	/**
	 * Kill the JADE Container in case it is running.
	 */
	public final static void shutdown() {
		defaultGateway.shutdown();
	}
	
	/**
	 * Return the state of JadeGateway
	 * @return true if the container and the gateway agent are active, false otherwise
	 */
	public final static boolean isGatewayActive() {
		return defaultGateway.isGatewayActive();
	}
	
	//#DOTNET_EXCLUDE_BEGIN
	public static void addListener(GatewayListener l) {
		defaultGateway.addListener(l);
	}
	
	public void removeListener(GatewayListener l) {
		defaultGateway.removeListener(l);
	}
	//#DOTNET_EXCLUDE_END
	
	public static final DynamicJadeGateway getDefaultGateway() {
		return defaultGateway;
	}
	
	// Private constructor to avoid creating instances of the JadeGateway class directly
	private JadeGateway() {
	}
	
}
