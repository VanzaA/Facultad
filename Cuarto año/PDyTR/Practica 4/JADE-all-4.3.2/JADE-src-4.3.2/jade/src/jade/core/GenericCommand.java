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


import jade.util.leap.List;
import jade.util.leap.LinkedList;


/**
 
 A generic implementation of the <code>Command</code> interface,
 operating at the meta-level to provide a generic transformation of
 method invocations.
 Generic commands can be used both as vertical and horizontal commands.
 Objects of this class can also be recycled to build <i>command
 pools</i>, since every information they hold can be rewritten.
 
 @author Giovanni Rimassa - FRAMeTech s.r.l.
 */
public class GenericCommand implements HorizontalCommand, VerticalCommand  {
	
	/**
	 Creates a new generic command, with the given name and
	 belonging to the given service and interaction.
	 
	 @param name The name of this command.
	 @param svc The name of the service this command belongs to.
	 @param interaction The identifier of the service interation this command belongs to.
	 */
	public GenericCommand(String name, String service, String interaction) {
		myName = name;
		myService = service;
		myInteraction = interaction;
		
		params = new LinkedList();
	}
	
	
	public String getName() {
		return myName;
	}
	
	public String getService() {
		return myService;
	}
	
	public String getInteraction() {
		return myInteraction;
	}
	
	/**
	 Add a new parameter to this command object.
	 @param obj The parameter to add. The actual type of
	 <code>obj</code> must be such that can be delivered over the
	 network by the concrete platform IMTP.
	 
	 @see jade.core.IMTPManager
	 */
	public void addParam(Object obj) {
		params.add(obj);
	}
	
	/**
	 Remove a parameter from this command object.
	 @param obj The parameter to remove.
	 */
	public void removeParam(Object obj) {
		params.remove(obj);
	}
	
	public void clear() {
		params.clear();
	}
	
	public void setReturnValue(Object rv) {
		returnValue = rv;
	}
	
	public final Object getParam(int index) {
		return params.get(index);
	}
	
	public Object[] getParams() {
		return params.toArray();
	}
	
	public Object getReturnValue() {
		return returnValue;
	}
	
	public jade.security.JADEPrincipal getPrincipal() {
		return principal;
	} // end getPrincipal
	
	public void setPrincipal( jade.security.JADEPrincipal p) {
		// principal can be set only one time
		// following setPrincipal are ignored
		if (principal==null) {
			this.principal = p;
		}
	} // end setPrincipal
	
	public jade.security.Credentials getCredentials() {
		return creds;
	} // end getCredentials
	
	public void setCredentials( jade.security.Credentials creds) {
		// credentials can be set only one time
		// following setCredentials are ignored
		if (this.creds==null) {
			this.creds = creds;
		}
	} // end setCredentials
	
	
	
	private jade.security.JADEPrincipal principal = null;
	private jade.security.Credentials creds;
	
	private final String myName;
	private final String myService;
	private final String myInteraction;
	
	private final List params;
	private Object returnValue;
	
}
