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

import jade.security.JADESecurityException;
import jade.util.leap.Serializable;

import java.io.InterruptedIOException;

/**
   Common bas class for all agent life cycle states.
   Note that a single LifeCycle object may represent more than
   one agent state.
   @author Giovanni Caire - TILAB
 */
public abstract class LifeCycle implements Serializable {
	protected Agent myAgent;
	protected int myState;
	
	public LifeCycle(int s) {
		myState = s;
	}
	
	/**
	   This is only called by the Agent class.
	 */
	void setAgent(Agent a) {
		myAgent = a;
	}
	
	/**
	   This method is invoked when the agent thread starts
	 */	   
	public void init() {
	}
	

	/**
	   This method actually implements what the agent has to do in this 
	   LifeCycle state
	 */
	public void execute() throws JADESecurityException, InterruptedException, InterruptedIOException {
	}
	
	/**
	   This method is invoked when the agent thread termiantes
	 */	   
	public void end() {
	}
	
	/**
	   Specifies whether or not this is a termination state
	 */	   
	public boolean alive() {
		return true;
	}
	
	public int getState() {
		return myState;
	}
	
	/**
	   This method is invoked as soon as we enter this LifeCycle state.
	   When this method is executed this LifeCycle object is the current
	   agent LifeCycle.
	 */
	public void transitionFrom(LifeCycle from) {	
	}
	
	/**
	   This method is invoked just before leaving this LifeCycle state.
	   When this method is executed this LifeCycle object is still the 
	   current agent LifeCycle.
	   The boolean return value is used to prevent transitions 
	   to certain states. In that case the life cycle state does not 
	   change.
	 */
	public boolean transitionTo(LifeCycle to) {
		return false;
	}
	
	/**
	 * Specifies whether or not the agent should react to incoming messages when in this 
	 * LifeCycle state.  
	 */
	public boolean isMessageAware() {
		return false;
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof LifeCycle) {
			return myState == ((LifeCycle) obj).myState;
		}
		return false;
	}
}

