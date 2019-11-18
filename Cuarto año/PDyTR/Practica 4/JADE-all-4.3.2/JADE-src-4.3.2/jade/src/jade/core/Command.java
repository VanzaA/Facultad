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



/**
 The <code>Command</code> interface has to be implemented by all
 JADE kernel-level commands, used by the various platform services.
 
 @author Giovanni Rimassa - FRAMeTech s.r.l.
 
 */
public interface Command {
	
	/**
	 Query the name of this command object.
	 
	 @return A string containing the name of the given command.
	 */
	String getName();
	
	/**
	 Query the service this command object belongs to.
	 
	 @return The name of the service this command object belongs to.
	 */
	String getService();
	
	/**
	 Obtain the parameter list, as an array of Java objects.
	 @return The parameters associated with this command object.
	 */
	Object getParam(int index);
	
	/**
	 Obtain the parameter list, as an array of Java objects.
	 @return The parameters associated with this command object.
	 */
	Object[] getParams();
	
	/**
	 Assign a return value to this command, so that the original
	 command issuer can retrieve it.
	 @param rv The desired return value for this command object.
	 */
	void setReturnValue(Object rv);
	
	/**
	 Obtain the return value for this command.
	 @return The value that is to be returned back to the issuer of
	 this <code>Command</code> object.
	 */
	Object getReturnValue();
	
	/**
	 Get the JADEPrincipal of the actor, respnsible for this Command object.
	 @return
	 */
	jade.security.JADEPrincipal getPrincipal();
	void setPrincipal( jade.security.JADEPrincipal p );
	
	/**
	 Get the credentials that the actor of this command
	 wants to use while processing this command.
	 @return
	 */
	jade.security.Credentials getCredentials();
	void setCredentials( jade.security.Credentials creds );
	
}
