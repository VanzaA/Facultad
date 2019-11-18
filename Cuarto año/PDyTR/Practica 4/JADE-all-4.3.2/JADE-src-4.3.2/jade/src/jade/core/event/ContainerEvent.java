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

package jade.core.event;

import jade.core.AID;
import jade.core.ContainerID;

/**
   This class represents a container related event

   @author Giovanni Caire - Telecom Italia
 */
public class ContainerEvent extends JADEEvent {

	public static final int BORN_AGENT = 1;
	public static final int DEAD_AGENT = 2;
	public static final int REATTACHED = 3;
	public static final int RECONNECTED = 4;
	public static final int LEADERSHIP_ACQUIRED = 5;

	private AID agent;

	public ContainerEvent(int id, AID aid, ContainerID cid) {
		super(id, cid);
		agent = aid;
	}

	public AID getAgent() {
		return agent;
	}
}
