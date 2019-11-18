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


/**
   This class is maintained for backward compatibility only. 
   It is not deprecated since, for backward compatibility 
   reasons, it is used internally by the framework.
   Use <code>jade.wrapper.ContainerController</code> instead.

   @author Giovanni Caire - TILAB
 */
public class AgentContainer extends ContainerController implements PlatformController {

	public AgentContainer(ContainerProxy cp, jade.core.AgentContainer impl, String platformName) {
		super(cp, impl, platformName);
	}

	public String getName() {
		return getPlatformName();
	}

	public void start() throws ControllerException {
	}

	public void suspend() throws ControllerException {
		initPlatformController();
		myPlatformController.suspend();
	}

	public void resume() throws ControllerException {
		initPlatformController();
		myPlatformController.resume();
	}

	public State getState() {
		try {
			initPlatformController();
			return myPlatformController.getState();
		}
		catch (Exception e) {
			return PlatformState.PLATFORM_STATE_KILLED;
		}
	}

	public void addPlatformListener(Listener aListener) throws ControllerException {
		initPlatformController();
		myPlatformController.addPlatformListener(aListener);
	}

	public void removePlatformListener(Listener aListener) throws ControllerException {
		initPlatformController();
		myPlatformController.removePlatformListener(aListener);
	}

}
