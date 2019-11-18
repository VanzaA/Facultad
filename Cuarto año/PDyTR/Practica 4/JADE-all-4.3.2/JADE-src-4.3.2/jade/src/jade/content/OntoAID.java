/**
 * ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 * 
 * GNU Lesser General Public License
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */
package jade.content;

import jade.core.AID;
import jade.util.leap.Iterator;

/**
 * Utility class that allow using an <code>AID</code> object
 * as an ontological concept.
 * @author Giovanni Caire - TILAB
 */
public class OntoAID extends AID implements Concept {

  /**
   * Constructs an ontological Agent-Identifier whose slot name is 
   * set to an empty string
   * @see AID#AID()
   */
	public OntoAID() {
		super();
	}
	
  /** 
   * Constructor for an ontological Agent-identifier
   * @param name is the value for the slot name for the agent. 
   * @param isGUID indicates if the passed <code>name</code>
   * is already a globally unique identifier or not. Two
   * constants <code>ISGUID</code>, <code>ISLOCALNAME</code>
   * have also been defined for setting a value for this parameter.
   * If the name is a local name, then the HAP (Home Agent Platform)
   * is concatenated to the name, separated by  "@".
   * @see AID#AID(String, boolean)
   */
	public OntoAID(String name, boolean isGUID) {
		super(name, isGUID);
	}
	
	/** 
	 * Create an ontological Agent identifier that wraps an existing 
	 * <code>AID</code>.
	 * @param id the <code>AID</code>to be wrapped. If <code>id</code>
	 * is already an ontological agent identifier no new object is 
	 * created and <code>id</code> is returned with the resolvers 
	 * (if any) properly wrapped.
	 */
	public static OntoAID wrap(AID id) {
		OntoAID wrapper = null;
		if (id != null) {
			if (id instanceof OntoAID) {
				wrapper = (OntoAID) id;
			}
			else {
				wrapper = new OntoAID(id.getName(), ISGUID);
				Iterator it = id.getAllAddresses();
				while (it.hasNext()) {
					wrapper.addAddresses((String) it.next());
				}
				it = id.getAllResolvers();
				while (it.hasNext()) {
					// This automatically performs the wrapping
					wrapper.addResolvers((AID) it.next()); 
				}
			}
		}
		return wrapper; 
	}
	
	/**
	 * This method is redefined so that resolvers AID are automatically
	 * wrapped into OntoAIDs
	 */
	public void addResolvers(AID aid) {
		super.addResolvers(wrap(aid));
	}
	
	// FIXME: clone method should be redefined too	
}

