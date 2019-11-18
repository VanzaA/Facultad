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

package jade.wrapper;

import jade.core.AgentManager;
import jade.core.ContainerID;
import jade.security.JADESecurityException;

import java.util.Hashtable;

/**
 @author Giovanni Caire - TILAB
 */
class PlatformControllerImpl implements PlatformController {
	private ContainerController myContainer;
	private AgentManager myMain;
	private Hashtable listeners = new Hashtable();
	private State platformState = PlatformState.PLATFORM_STATE_VOID;
	
	PlatformControllerImpl(ContainerController c, AgentManager m) {
		myContainer = c;
		myMain = m;
		platformState = PlatformState.PLATFORM_STATE_READY;
		myMain.addListener(new AgentManagerListenerAdapter() {
			public void removedContainer(jade.core.event.PlatformEvent ev) {
				ContainerID cid = ev.getContainer();
				try {
					if (cid.getName().equals(myContainer.getContainerName())) {
						// The local main container is terminating -->
						// The whole platform is terminating
						platformState = PlatformState.PLATFORM_STATE_KILLED;
					}
				}
				catch (ControllerException ce) {
					// Should never happen
					ce.printStackTrace();
				}
			}
		} );    	
	}
	
	/**
	 * Get the name of the platform.
	 * @return String The platform name.
	 */
	public String getName() {
		return myContainer.getPlatformName();
	}
	
	/**
	 * Start the platform after its been initialized.
	 * @throws ControllerException If any probelms other than illegal state occur.
	 * @throws IllegalStateException If state is illegal for this activity.
	 */
	public void start() throws ControllerException {
		// Just do nothing
	}
	
	/**
	 * Suspend the agent platform. Next action may be resume or kill.
	 * @throws ControllerException If any probelms other than illegal state occur.
	 * @throws IllegalStateException If state is illegal for this activity.
	 */
	public void suspend() throws ControllerException {
		throw new ControllerException("Not_Yet_Implemented");
	}
	
	/**
	 * Activate the agent platform. Next action may be suspend or kill.
	 * @throws ControllerException If any probelms other than illegal state occur.
	 * @throws IllegalStateException If state is illegal for this activity.
	 */
	public void resume() throws ControllerException {
		throw new ControllerException("Not_Yet_Implemented");
	}
	
	/**
	 * Kill the agent platform with all its agents.
	 * <br>
	 * <b>NOTE</b> that this method must not be executed within an agent Thread. 
	 * This in fact would cause a deadlock condition. If an agent needs to invoke it
	 * it should spawn a dedicated Thread.
	 * @throws ControllerException If any probelms other than illegal state occur.
	 * @throws IllegalStateException If state is illegal for this activity.
	 */
	public void kill() throws ControllerException {
		try {
			platformState = PlatformState.PLATFORM_STATE_KILLING;
			// FIXME: Where do we get the principal and credentials
			myMain.shutdownPlatform(null, null);
		}
		catch (JADESecurityException jse) {
			throw new ControllerException("Security error. "+jse.getMessage());
		}
	}
	
	/**
	 * Get agent proxy to local agent given its name.
	 * @param localAgentName The short local name of the desired agent.
	 * @throws ControllerException If any probelms occur obtaining this proxy.
	 */
	public AgentController getAgent(String localAgentName) throws ControllerException {
		return myContainer.getAgent(localAgentName);
	}
	
	/**
	 * Create a new agent.
	 * @param nickName The name of the agent.
	 * @param className The class implementing the agent.
	 * @param args The agents parameters - typically String[] from a configuration file.
	 * @return AgentController to enable control of the agent.
	 * @throws IllegalStateException If state is illegal for this activity.
	 * @throws ControllerException If any else goes wrong. All other exceptions are caught
	 * and rethrown as a ControllerException.
	 */
	public AgentController createNewAgent(String nickname, String className,
			Object[] args) throws ControllerException {
		return myContainer.createNewAgent(nickname, className, args);
	}
	
	/**
	 * Returns an instance of PlatformState.
	 */
	public State getState() {
		return platformState;
	}
	
	/**
	 * Add a platform listener.
	 * @param aListener The listener to be notified.
	 */
	public synchronized void addPlatformListener(PlatformController.Listener aListener) throws ControllerException {
		ListenerWrapper wrapper = new ListenerWrapper(aListener);
		myMain.addListener(wrapper);
		listeners.put(aListener, wrapper);
	}
	
	/**
	 * Remove a platform listener.
	 * @param aListener The listener to be notified.
	 */
	public synchronized void removePlatformListener(PlatformController.Listener aListener) throws ControllerException {
		ListenerWrapper wrapper = (ListenerWrapper) listeners.get(aListener);
		if (wrapper != null) {
			myMain.removeListener(wrapper);
		}
	}
	
	/**
	 Inner class AgentManagerListenerAdapter
	 This utility class provides a dummy implementation of
	 all the methods of the jade.core.AgentManager.Listener interface
	 */
	class AgentManagerListenerAdapter implements AgentManager.Listener {
		
		public void addedContainer(jade.core.event.PlatformEvent ev) {
		}
		public void removedContainer(jade.core.event.PlatformEvent ev) {
		}
		public void bornAgent(jade.core.event.PlatformEvent ev) {
		}
		public void deadAgent(jade.core.event.PlatformEvent ev) {
		}
		public void movedAgent(jade.core.event.PlatformEvent ev) {
		}
		public void suspendedAgent(jade.core.event.PlatformEvent ev) {
		}
		public void resumedAgent(jade.core.event.PlatformEvent ev) {
		}
		public void frozenAgent(jade.core.event.PlatformEvent ev) {
		}
		public void thawedAgent(jade.core.event.PlatformEvent ev) {
		}
		
		public void addedMTP(jade.core.event.MTPEvent ev) {
		}
		public void removedMTP(jade.core.event.MTPEvent ev) {
		}
		public void messageIn(jade.core.event.MTPEvent ev) {
		}
		public void messageOut(jade.core.event.MTPEvent ev) {
		}
	} // END of inner class AgentManagerListenerAdapter	  
	
	/**
	 Inner class ListenerWrapper
	 This utility class wraps a jade.wrapper.PlatformController.Listener
	 into a jade.core.AgentManager.Listener
	 */
	class ListenerWrapper extends AgentManagerListenerAdapter {
		private PlatformController.Listener myListener;
		
		ListenerWrapper(PlatformController.Listener l) {
			myListener = l;
		}
		
		public void removedContainer(jade.core.event.PlatformEvent ev) {
			ContainerID cid = ev.getContainer();
			try {
				if (cid.getName().equals(myContainer.getContainerName())) {
					// The local main container is terminating -->
					// The whole platform is terminating
					myListener.killedPlatform(new jade.wrapper.PlatformEvent() {
						public String getAgentGUID() {
							return null;
						}
						
						public String getPlatformName() {
							return myContainer.getPlatformName();
						}
						
						public int getEventType() {
							return PlatformEvent.KILLED_PLATFORM;
						}
					} );
				}
			}
			catch (ControllerException ce) {
				// Should never happen 
				ce.printStackTrace();
			}
		}
		public void bornAgent(jade.core.event.PlatformEvent ev) {
			myListener.bornAgent(ev);
		}
		public void deadAgent(jade.core.event.PlatformEvent ev) {
			myListener.deadAgent(ev);
		}
	} // END of inner class ListenerWrapper
}

