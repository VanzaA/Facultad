
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

package jade.core.behaviours;

//#CUSTOM_EXCLUDE_FILE

import jade.util.leap.*;

import jade.core.Agent;

/**
   Base class for all composite behaviour whose children run serially,
   i.e. the composite behaviour is blocked if and only if its current child
   is blocked.
   @author Giovanni Caire - Telecom Italia Lab
 */
public abstract class SerialBehaviour extends CompositeBehaviour {

	/**
       Create a new <code>SerialBehaviour</code> object, without
       setting the owner agent.
	 */
	protected SerialBehaviour() {
		super();
	}

	/**
       Create a new <code>SerialBehaviour</code> object and set the
       owner agent.
       @param a The agent owning this behaviour.
	 */
	protected SerialBehaviour(Agent a) {
		super(a);
	}


	//#APIDOC_EXCLUDE_BEGIN

	/**
     Handle block/restart notifications. A
     <code>SerialBehaviour</code> is blocked <em>only</em> when
     its currently active child is blocked, and becomes ready again
     when its current child is ready. This method takes care of the
     various possibilities.

     @param rce The event to handle.
	 */
	protected void handle(RunnableChangedEvent rce) {
		if(rce.isUpwards()) {
			// Upwards notification
			if (rce.getSource() == this) {
				// If the event is from this behaviour, set the new 
				// runnable state and notify upwords.
				super.handle(rce);
			}
			else if (rce.getSource() == getCurrent()) {
				// If the event is from the currently executing child, 
				// create a new event, set the new runnable state and
				// notify upwords.
				myEvent.init(rce.isRunnable(), NOTIFY_UP);
				super.handle(myEvent);
			}
			else {
				// If the event is from another child, just ignore it
			}
		}
		else {
			// Downwards notifications 
			// Copy the state and pass it downwords only to the
			// current child
			setRunnable(rce.isRunnable());
			Behaviour b  = getCurrent();
			if (b != null) {
				b.handle(rce);
			}
		}  	
	}

	//#APIDOC_EXCLUDE_END

}



