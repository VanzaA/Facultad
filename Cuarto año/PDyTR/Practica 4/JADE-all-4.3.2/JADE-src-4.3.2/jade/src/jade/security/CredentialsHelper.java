/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2002 TILAB S.p.A. 

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

package jade.security;

import jade.lang.acl.ACLMessage;

/**
   This interface includes the subset of methods of the 
   <code>SecurityHelper</code> class required by the JADE runtime
	
	 @author Giovanni Caire - TILAB
*/
public interface CredentialsHelper {

	/**
	   Retrieve the principal of the agent this CredentialsHelper 
	   refers to.
	 */
	JADEPrincipal getPrincipal();
	
	/**
	   Retrieve the credentials of the agent this CredentialsHelper 
	   refers to.
	 */
	Credentials getCredentials();
	
	/**
	   Retrieve the principal of the sender of a signed ACLMessage.
	 */
	JADEPrincipal getPrincipal(ACLMessage msg);
	
	/**
	   Retrieve the credentials of the sender of a signed ACLMessage.
	 */
	Credentials getCredentials(ACLMessage msg) throws JADESecurityException;
}
