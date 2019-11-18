/**
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A. 
 * Copyright (C) 2001,2002 TILab S.p.A. 
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
 */

package jade.util;

import java.util.Vector;
import jade.core.behaviours.Behaviour;

/**
 * This class implements a FIFO queue of objects that can be put and got
 * in a synchronized way. This is useful when an external thread, 
 * e.g. a GUI, has to communicate with an agent: The external thread
 * puts objects in the queue and the agent gets and processes them.
 * The queue can be associated to a <code>Behaviour</code>. This
 * Behaviour will be restarted each time an object is inserted in the 
 * queue. 
 * This class can be effectively used in combination with the 
 * <code>Event</code> class to support a synchronization between the 
 * external therad (posting the event in the <code>InputQueue</code>)
 * and the Agent thread (processing the event).
 * @see jade.util.Event
 * @author Giovanni Caire - TILab 
 */
public class InputQueue {
	private Vector queue = new Vector();
	private Behaviour myManager;

	/**
       Default constructor.
	 */
	public InputQueue() {
	}

	/**
	   Associate this <code>InputQueue</code> object with the indicated
	   <code>Behaviour</code> so that it will be restarted each time
	   a new object is inserted.
	   @param b The <code>Behaviour</code> to associate.
	 */
	public synchronized void associate(Behaviour b) {

		myManager = b;
		// If there were objects already inserted --> restart the manager 
		// so that it can manages them
		if (myManager != null && queue.size() > 0) {
			myManager.restart();
		}
	}

	/**
	   Insert an object into the queue. If there is a <code>Behaviour</code>
	   associated to this <code>InputQueue</code> it will be restarted.
	   @param obj The object to insert.
	 */
	public synchronized void put(Object obj) {
		queue.addElement(obj);

		// Restart the manager behaviour (if any) so that it can manage
		// the object
		if (myManager != null) {
			myManager.restart();
		}
	}	

	/**
	   Extract the first object in the queue (if any).
	   @return The first object in the queue or <code>null</code> if
	   the queue is empty.
	 */
	public synchronized Object get() {
		Object obj = null;
		if (queue.size() > 0) {
			obj = queue.elementAt(0);
			queue.removeElementAt(0);
		}
		return obj;
	}			

	/**
       Remove all elements from this queue.
	 */
	public synchronized void clear() {
		queue.removeAllElements();
	}

}
