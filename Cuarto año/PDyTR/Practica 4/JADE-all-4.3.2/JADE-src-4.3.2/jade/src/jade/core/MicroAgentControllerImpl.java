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

//#MIDP_EXCLUDE_FILE

import jade.wrapper.*;

/**
   @see jade.core.MicroRuntime#getAgent(String)
   @author Marco Ughetti - TILAB
 */
class MicroAgentControllerImpl implements AgentController {

	private String agentName;
	private FrontEndContainer myFrontEnd;

	/**
	     This constructor should not be called by applications.
	     The method <code>MicroRuntime.startAgent()</code> should
	     be used instead.
	 */
	MicroAgentControllerImpl(String an, FrontEndContainer fec) {
		agentName = an;
		myFrontEnd = fec;
	}


	public String getName() throws StaleProxyException {
		Agent a = myFrontEnd.getLocalAgent(agentName);
		if (a != null) {
			return a.getName();
		}
		else {
			throw new StaleProxyException();
		}
	}

	public State getState() throws StaleProxyException {
		// FIXME: to be implemented 
		return null;
	}

	public void start() throws StaleProxyException {
		// Agents are automatically started when calling MicroRuntime.startAgent()
	}

	public void kill() throws StaleProxyException {
		try {
			myFrontEnd.killAgent(agentName);
		}
		catch (Throwable t) {
			throw new StaleProxyException(t);
		}
	}

	public void suspend() throws StaleProxyException {
		try{
			myFrontEnd.suspendAgent(agentName);
		}
		catch(Throwable t) {
			throw new StaleProxyException(t);
		}
	}

	public void activate() throws StaleProxyException {
		try {
			myFrontEnd.resumeAgent(agentName);
		}
		catch (Throwable t) {
			throw new StaleProxyException(t);
		}
	}

	public void move(Location where) throws StaleProxyException {
		// Mobility is not supported on a split container
	}

	public void clone(Location where, String newName) throws StaleProxyException {
		// Mobility is not supported on a split container
	}

	public void putO2AObject(Object o, boolean blocking) throws StaleProxyException {
		Agent adaptee = myFrontEnd.getLocalAgent(agentName);
		if (adaptee == null) {
			throw new StaleProxyException();
		}
		try {
			adaptee.putO2AObject(o, blocking);
		} catch (InterruptedException ace) {
			throw new StaleProxyException(ace);
		}
	}

	//#J2ME_EXCLUDE_BEGIN
	@SuppressWarnings("unchecked")
	public <T> T getO2AInterface(Class<T> theInterface) throws StaleProxyException {
		Agent adaptee = myFrontEnd.getLocalAgent(agentName);
		if (adaptee == null) {
			throw new StaleProxyException("Controlled agent does not exist");
		}

		T o2aInterfaceImpl = adaptee.getO2AInterface(theInterface);
		if (o2aInterfaceImpl == null)
			return null;

		ClassLoader classLoader = o2aInterfaceImpl.getClass().getClassLoader();

		return (T) java.lang.reflect.Proxy.newProxyInstance(classLoader,
				new Class[] { theInterface }, new O2AProxy(o2aInterfaceImpl) {
					protected void checkAgent() throws O2AException {
						if (myFrontEnd.getLocalAgent(agentName) == null) {
							throw new O2AException(
									"Controlled agent does not exist");
						}
					}
				});
	}
	//#J2ME_EXCLUDE_END

}
