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

package jade.core.management;

//#J2ME_EXCLUDE_FILE

import jade.core.AID;

/**
 * 
 * Class to subscribe to CodeLocator basic Events.
 * 
 * @author <a href="mailto:jcucurull@deic.uab.cat">Jordi Cucurull Juan</a>
 *
 */
public abstract class CodeLocatorListener {

	/**
	 * Event triggered when an agent is registered to the CodeLocator.
	 * @param name Agent Identification of the registered agent.
	 * @param cl ClassLoader associated to the agent.
	 */
	public void handleRegisterAgent(AID name, ClassLoader cl) {
		
	}
	
	/**
	 * Event triggered when the associated agent ClassLoader is updated in 
	 * the CodeLocator.
	 * @param name Agent Identification of the registered agent.
	 * @param clOld Old ClassLoader associated to the agent.
	 * @param clNew New ClassLoader associated to the agent.
	 */
	public void handleUpdateAgent(AID name, ClassLoader clOld, ClassLoader clNew) {
		
	}
	
	/**
	 * Event triggered when an agent is cloned.
	 * @param oldName Source agent identification.
	 * @param newName Target agent identification.
	 * @param cl ClassLoader associated to the source agent.
	 * @return New ClassLoader to the target agent or null to not influence it.
	 */
	public ClassLoader handleCloneAgent(AID oldName, AID newName, ClassLoader cl) {
		return null;
	}
	
	/**
	 * Event triggered when an agent is removed from the CodeLocator.
	 * @param name Agent Identification of the removed agent.
	 * @param cl ClassLoader associated to the agent.
	 */
	public void handleRemoveAgent(AID name, ClassLoader cl) {
		
	}
	
}
