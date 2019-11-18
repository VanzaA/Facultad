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

import java.util.EventListener;
import java.util.EventObject;

/**
 * Defines those methods which are permitted on a platform.
   <br>
   <b>NOT available in MIDP</b>
   <br>
 * @author David Bell, Dick Cowan: Hewlett-Packard
 */
public interface PlatformController
{
    /**
       Inner callback interface to receive platform events.
    */
    public static interface Listener extends EventListener {
        /**
         * Called when an agent is born. EventObject source is AgentController.
         */
        public void bornAgent(PlatformEvent anEvent);

        /**
         * Called when an agent dies. PlatformEvent source is AgentController.
         */
        public void deadAgent(PlatformEvent anEvent);
        
        /**
         * Called when the platform is started. PlatformEvent source is PlatformController.
         */
        public void startedPlatform(PlatformEvent anEvent);
        
        /**
         * Called when the platform is suspended. PlatformEvent source is PlatformController.
         */
        public void suspendedPlatform(PlatformEvent anEvent);
        
        /**
         * Called when the platform is activated. PlatformEvent source is PlatformController.
         */
        public void resumedPlatform(PlatformEvent anEvent);

        /**
         * Called when the platform is killed (destroyed). PlatformEvent source is PlatformController.
         */
        public void killedPlatform(PlatformEvent anEvent);
    }

    /**
     * Get the name of the platform.
     * @return String The platform name.
     */
    public String getName();
    
    /**
     * Start the platform after its been initialized.
     * @throws ControllerException If any probelms other than illegal state occur.
     * @throws IllegalStateException If state is illegal for this activity.
     */
    public void start() throws ControllerException;

    /**
     * Suspend the agent platform. Next action may be resume or kill.
     * @throws ControllerException If any probelms other than illegal state occur.
     * @throws IllegalStateException If state is illegal for this activity.
     */
    public void suspend() throws ControllerException;

    /**
     * Activate the agent platform. Next action may be suspend or kill.
     * @throws ControllerException If any probelms other than illegal state occur.
     * @throws IllegalStateException If state is illegal for this activity.
     */
    public void resume() throws ControllerException;

    /**
     * Kill the agent platform. Kills all agents.
     * @throws ControllerException If any probelms other than illegal state occur.
     * @throws IllegalStateException If state is illegal for this activity.
     */
    public void kill() throws ControllerException;

    /**
     * Get agent proxy to local agent given its name.
     * @param localAgentName The short local name of the desired agent.
     * @throws ControllerException If any probelms occur obtaining this proxy.
     */
    public AgentController getAgent(String localAgentName) throws ControllerException;

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
    public AgentController createNewAgent(String nickName, String className,
                                       Object[] args) throws ControllerException;

    /**
     * Returns an instance of PlatformState.
     */
    public State getState();

    /**
     * Add a platform listener.
     * @param aListener The listener to be notified.
     */
    public void addPlatformListener(Listener aListener) throws ControllerException;

    /**
     * Remove a platform listener.
     * @param aListener The listener to be notified.
     */
    public void removePlatformListener(Listener aListener) throws ControllerException;
 
}

