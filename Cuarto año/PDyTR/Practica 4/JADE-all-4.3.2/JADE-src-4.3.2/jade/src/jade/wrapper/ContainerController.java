/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be usefubut
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
 *****************************************************************/

package jade.wrapper;

import jade.core.AID;
import jade.core.NotFoundException;
import jade.core.AgentManager;
import jade.core.AgentContainer;
import jade.core.MainContainer;

import jade.mtp.MTPException;

/**
   This class is a Proxy class, allowing access to a JADE agent
   container. Invoking methods on instances of this class, it is
   possible to request services from <it>in-process</it> agent
   containers.
   This class must not be instantiated by applications. Instead, use
   the <code>createContainer()</code> method in class
   <code>Runtime</code>.
   <br>
   <b>NOT available in MIDP</b>
   <br>
   @see jade.core.Runtime#createAgentContainer(Profile)

   @author Giovanni Rimassa - Universita' di Parma
   @author Giovanni Caire - TILAB
 */
public class ContainerController {

	private AgentContainer myImpl;
	private ContainerProxy myProxy;
	private String myPlatformName;

	protected PlatformController myPlatformController; 

	/**
     This constructor requires a concrete
     implementation of a JADE agent container, which cannot be
     instantiated by applications, so it cannot be meaningfully called
     from application code. The proper way to create an agent
     container from an application is to call the
     <code>Runtime.createContainer()</code> method.
     @see jade.core.Runtime#createAgentContainer(Profile)
     @param impl A concrete implementation of a JADE agent container.
     @param platformName the name of the platform
	 */
	public ContainerController(ContainerProxy cp, AgentContainer impl, String platformName) {
		myProxy = cp;
		myImpl = impl;
		myPlatformName = platformName;
	}


	/**
	 * Get a controller (i.e. a proxy) to a local agent given its local-name.
	 * @param localAgentName The local name of the desired agent.
	 * @throws ControllerException If any problems occur obtaining this proxy or if no such agent exists in the local container.
	 */
	public AgentController getAgent(String localAgentName) throws ControllerException {
		return getAgent(localAgentName, AID.ISLOCALNAME);
	}

	/**
	 * Get a controller (i.e. a proxy) to a local agent given its local-name or GUID.
	 * @param name The local name or the GUID of the desired agent.
	 * @param isGuid A flag indicating whether <code>name</code> represents the local-name (<code>AID.ISLOCALNAME</code>)
	 * or the GUID (<code>AID.ISGUID</code>) of the desired agent. 
	 * @throws ControllerException If any problems occur obtaining this proxy or if no such agent exists in the local container.
	 */
	public AgentController getAgent(String name, boolean isGuid) throws ControllerException {
		if(myImpl == null || myProxy == null) {
			throw new StaleProxyException();
		}

		AID agentID = new AID(name, isGuid);

		// Check that the agent exists
		jade.core.Agent instance = myImpl.acquireLocalAgent(agentID);
		if (instance == null) {
			throw new ControllerException("Agent " + agentID.getName() + " not found.");
		} 
		myImpl.releaseLocalAgent(agentID);
		return new AgentControllerImpl(agentID, myProxy, myImpl);
	}

	



	/**
     Creates a new JADE agent, running within this container, 
     @param nickname A platform-unique nickname for the newly created
     agent. The agent will be given a FIPA compliant agent identifier
     using the nickname and the ID of the platform it is running on.
     @param className The fully qualified name of the class that
     implements the agent.
     @param args An object array, containing initialization parameters
     to pass to the new agent. 
     @return A proxy object, allowing to call state-transition forcing
     methods on the real agent instance.
	 */
	public AgentController createNewAgent(String nickname, String className, Object[] args) throws StaleProxyException {
		if(myImpl == null || myProxy == null) {
			throw new StaleProxyException();
		}

		AID agentID = new AID(nickname, AID.ISLOCALNAME);

		try {
			myProxy.createAgent(agentID, className, args);
			return new AgentControllerImpl(agentID, myProxy, myImpl);
		}
		catch (Throwable t) {
			throw new StaleProxyException(t);
		}
	}

	// HP Patch begin ----------------------------------------------------------------------------------
	/**
	 * Add an Agent to this container. Typically Agent would be some class extending
	 * Agent which was instantiated and configured.
	 * @param nickname A platform-unique nickname for the newly created agent.
	 * The agent will be given a FIPA compliant agent identifier using the nickname and
	 * the ID of the platform it is running on.
	 * @param anAgent The agent to be added to this agent container.
	 * @return An AgentController, allowing to call state-transition forcing methods on the real agent instance.
	 */
	public AgentController acceptNewAgent(String nickname, jade.core.Agent anAgent) throws StaleProxyException {
		if (myImpl == null || myProxy == null) {
			throw new StaleProxyException();
		}

		AID agentID = new AID(nickname, AID.ISLOCALNAME);
		// FIXME: This call skips the security checks on the local container
		try {
			jade.core.NodeDescriptor nd = myImpl.getNodeDescriptor();
			// The owner of the new agent is the owner of the local container.
			// The new agent has NO initial credentials
			myImpl.initAgent(agentID, anAgent, nd.getOwnerPrincipal(), null);
		}
		catch(Exception e) {
			throw new StaleProxyException(e);
		}
		return new AgentControllerImpl(agentID, myProxy, myImpl);
	}
	// HP Patch end ------------------------------------------------------------------------------------


	/**
     Shuts down this container, terminating all the agents running within it.
	 */
	public void kill() throws StaleProxyException {
		if (myImpl == null || myProxy == null) {
			throw new StaleProxyException();
		}

		try {
			myProxy.killContainer();
			// release resources of this object
			myProxy = null;
			myImpl = null;
			myPlatformName = null;
		}
		catch (Throwable t) {
			throw new StaleProxyException(t);
		}
	}



	/**
     Installs a new message transport protocol, that will run within
     this container.

     @param address The transport address exported by the new MTP, in
     string format.
     @param className The fully qualified name of the Java class that
     implements the transport protocol.
     @exception MTPException If something goes wrong during transport
     protocol activation.
	 */
	public void installMTP(String address, String className) throws MTPException, StaleProxyException {
		if (myImpl == null || myProxy == null) {
			throw new StaleProxyException();
		}

		try {
			myProxy.installMTP(address, className);
		}
		catch (Throwable t) {
			throw new StaleProxyException(t);
		}
	}

	/**
     Removes a message transport protocol, previously running within this
     container.

     @param address The transport address exported by the new MTP, in
     string format.
     @exception MTPException If something goes wrong during transport
     protocol activation.
     @exception NotFoundException If no protocol with the given
     address is currently installed on this container.
	 */
	public void uninstallMTP(String address) throws MTPException, NotFoundException, StaleProxyException {
		if (myImpl == null || myProxy == null) {
			throw new StaleProxyException();
		}

		try {
			myProxy.uninstallMTP(address);
		}
		catch (Throwable t) {
			throw new StaleProxyException(t);
		}
	}

	/**
	 * Retrieve the name of the platform the container wrapped by this
	 * ContainerController belongs to.
	 * @return the name (i.e. the HAP) of this platform.
	 **/
	public String getPlatformName() {
		return myPlatformName;
	}

	/**
	 * Retrieve the name of the wrapped container.
	 * @return the name of this platform container.
	 **/
	public String getContainerName() throws ControllerException {
		if(myImpl == null) {
			throw new ControllerException("Stale proxy.");
		}
		return myImpl.here().getName();
	}

	/**
     Retrieve a controller for the platform the container 
     wrapped by this ContainerController belongs to  and acts 
     as the Main Container.
     @return a <code>PlatfromController</code> for the platform
     the container wrapped by this <code>ContainerController</code>
     belongs to.
     @exception ControllerException If the container wrapped by this
     <code>ContainerController</code> is not valid or is not the 
     platform Main Container.
	 */
	public PlatformController getPlatformController() throws ControllerException {
		initPlatformController();
		return myPlatformController;
	}

	protected void initPlatformController() throws ControllerException {
		if (myPlatformController == null) {
			if (myImpl == null) {
				throw new ControllerException("Stale proxy.");
			}
			MainContainer main = myImpl.getMain();
			if (main == null) {
				throw new ControllerException("Not a Main Container.");
			}
			if (main instanceof AgentManager) {
				myPlatformController = new PlatformControllerImpl(this, (AgentManager) main);
			}
			else {
				throw new ControllerException("Platform not accessible.");
			}
		}
	}  
	
	public boolean isJoined() {
		return myImpl != null && myImpl.isJoined();
	}
}
