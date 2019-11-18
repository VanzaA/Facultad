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


package jade.domain.FIPAAgentManagement;

import jade.core.AID;
import jade.content.Concept;

/**
 * This class implements the concept of the fipa-agent-management ontology
 * representing the description of an Agent in the AMS catalogue.
 * @see jade.domain.FIPAAgentManagement.FIPAManagementOntology
 * @see jade.domain.AMSService
 * @author Fabio Bellifemine - CSELT S.p.A.
 * @version $Date: 2006-12-14 17:26:48 +0100 (gio, 14 dic 2006) $ $Revision: 5916 $
 */
public class AMSAgentDescription implements Concept {

	/**
       String constant for the <code>initiated</code> agent life-cycle
       state.
	 */
	public static final String INITIATED = "initiated";

	/**
       String constant for the <code>active</code> agent life-cycle
       state.
	 */
	public static final String ACTIVE = "active";

	/**
       String constant for the <code>suspended</code> agent life-cycle
       state.
	 */
	public static final String SUSPENDED = "suspended";

	/**
       String constant for the <code>waiting</code> agent life-cycle
       state.
	 */
	public static final String WAITING = "waiting";

	/**
       String constant for the <code>transit</code> agent life-cycle
       state.
	 */
	public static final String TRANSIT = "transit";

	/**
    String constant for the <code>latent</code> agent life-cycle
    state. JADE specific state indicating an agent waiting to be restored after a 
    crash of the main container
	 */
	public static final String LATENT = "latent";

	private AID name;
	private String ownership;
	private String state;

	/**
       Default constructor.
	 */
	public AMSAgentDescription() {
	}

	/**
       Set the <code>name</code> slot of this object.
       @param n The agent identifier for the name.
	 */
	public void setName(AID n){
		name = n;
	}

	/**
       Set the <code>ownership</code> slot of this object.
       @param n The string for the ownership.
	 */
	public void setOwnership(String n) {
		ownership = n;
	}

	/**
       Set the <code>state</code> slot of this object.
       @param n The string for the state.
	 */
	public void setState(String n) {
		state = n;
	}

	/**
       Retrieve the <code>name</code> slot of this object.
       @return The value of the <code>name</code> slot, or
       <code>null</code> if no value was set.
	 */
	public AID getName(){
		return name;
	}

	/**
       Retrieve the <code>ownership</code> slot of this object.
       @return The value of the <code>ownership</code> slot, or
       <code>null</code> if no value was set.
	 */
	public String getOwnership(){
		return ownership;
	}

	/**
       Retrieve the <code>state</code> slot of this object.
       @return The value of the <code>state</code> slot, or
       <code>null</code> if no value was set.
	 */
	public String getState(){
		return state;
	}

}
