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

package jade.domain.introspection;

import jade.core.ContainerID;

/**
 * This class represents the <code>shutdown-platform-requested</code> concept in the
 * <code>jade-introspection</code> ontology.
 * @author Giovanni Caire - TILAB
 * @since JADE 3.5
 */
public class ShutdownPlatformRequested implements Event {
	/**
	 A string constant for the name of this event.
	 */
	public static final String NAME = "shutdown-platform-requested";

	/**
	 Default constructor. A default constructor is necessary for
	 ontological classes.
	 */
	public ShutdownPlatformRequested() {
	}

	/**
	 Retrieve the name of this event.
	 @return A constant value for the event name.
	 */
	public String getName() {
		return NAME;
	}
}
