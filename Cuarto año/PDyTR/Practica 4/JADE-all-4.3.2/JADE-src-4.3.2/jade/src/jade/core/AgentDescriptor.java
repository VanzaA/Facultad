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
//#APIDOC_EXCLUDE_FILE

import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.security.Credentials;
import jade.security.JADEPrincipal;



/**
   Hold all information about an agent
   @author Giovanni Rimassa - Universita' di Parma
   @author Giovanni Caire - TILAB
   @version $Date: 2007-02-07 14:31:27 +0100 (mer, 07 feb 2007) $ $Revision: 5929 $
 */

public class AgentDescriptor {

	public static final boolean NATIVE_AGENT = false;
	public static final boolean FOREIGN_AGENT = true;



	private AMSAgentDescription description;
	//  private AgentProxy proxy;
	private boolean foreign;
	private ContainerID containerID;
	private JADEPrincipal principal;
	private Credentials amsDelegation;


	public AgentDescriptor() {
		this(NATIVE_AGENT);
	}

	public AgentDescriptor(boolean isForeign) {
		foreign = isForeign;
	}

	// AMS description
	public void setDescription(AMSAgentDescription dsc) {
		description = dsc;
	}

	public AMSAgentDescription getDescription() {
		return description;
	}

	// Is this agent a foreign agent?
	public boolean isForeign() {
		return foreign;
	}


	// Is this agent a native agent?
	public boolean isNative() {
		return !foreign;
	}

	// Container ID
	public void setContainerID(ContainerID cid) {
		containerID = cid;
	}

	public ContainerID getContainerID() {
		return containerID;
	}

	// Agent principal
	public void setPrincipal(JADEPrincipal p) {
		principal = p;
	}

	public JADEPrincipal getPrincipal() {
		return principal;
	}

	// AMS delegation
	public void setAMSDelegation(Credentials cf) {
		amsDelegation = cf;
	}

	public Credentials getAMSDelegation() {
		return amsDelegation;
	}

}
