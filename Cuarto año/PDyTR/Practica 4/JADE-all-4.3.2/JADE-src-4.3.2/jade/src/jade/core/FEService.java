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

//#APIDOC_EXCLUDE_FILE

/**
 * JADE kernel services providing a service-helper and wishing this helper to be available in the 
 * split execution mode too, need to provide a FEService class.
 * When starting a split container the <code>services</code> option works exactly as when starting a  
 * normal container, but the indicated classes must be concrete implementations of the <code>FEService</code>
 * abstract class.
 * <br> 
 * It should be noticed that a Front-End service is only intended to provide access to the service helper
 * and not to actually implement full service functionality 
 * 
 * @author Giovanni Caire - Telecom Italia
 */
public abstract class FEService {
	private BackEnd myBackEnd;

	/**
	 * Subclasses must implement this method to return the name of this service. 
	 * Such name must be specified by an agent in the <code>getHelper()</code> 
	 * method when retrieving the helper for this service.
	 * @return The name of this service
	 */
	public abstract String getName();

	/**
	 * In many cases for the Front-End service to work properly it is necessary that the real service
	 * (or possibly a modified version of it) is active in the Back-End. 
	 * In such cases subclasses may redefine this method to return the fully qualified name of the class
	 * implementing the back-end side counterpart of this service.
	 * @return The fully qualified name of the class implementing the back-end side counterpart of this service
	 */
	public String getBEServiceClassName() {
		return null;
	}

	/**
	 * Subclasses must implement this method to return the helper for accessing this service.
	 * @param a The agent which the helper is requested for.
	 * @return The ServiceHelper to be used by the agent.
	 */
	public abstract ServiceHelper getHelper(Agent a);	

	/**
	 * Subclasses can use this method to forward a front-end side invocation of a service helper method to the back-end.
	 * Said A the agent invoking the method, the effect is that the same method will be invoked, with the passed parameters, 
	 * on the service helper object associated to the agent image representing A in the back-end.
	 * 
	 * In case the back-end side service helper method requires parameters different than those passed in the 
	 * methodParams argument, a suitable "BackEnd Codec" class must be provided to perform the necessary conversions. 
	 * Such class must implement the <code>BECodec</code> interface and must be called "<service-name>BECodec"
	 *   
	 * @param actor The name of the agent invoking the service helper method
	 * @param methodName The name of the invoked method
	 * @param methodParams The parameters to be passed to the back-end
	 * @return The result of the back-end side service helper method invocation possibly encoded by the BackEnd Codec (if any)
	 * 
	 * @throws NotFoundException If the invoking agent is not found in the back-end
	 * @throws ServiceException If an error occurs at the service level
	 * @throws IMTPException If a communication error occurs
	 */
	protected Object invoke(String actor, String methodName, Object[] methodParams) throws NotFoundException, ServiceException, IMTPException {
		return myBackEnd.serviceInvokation(actor, getName(), methodName, methodParams);
	}

	void init(BackEnd be) {
		myBackEnd = be;
	}
}
