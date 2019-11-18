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

package jade.tools.introspector;
 
import java.util.Vector;
import jade.core.behaviours.Behaviour;
import jade.util.Event;

/**
 * @author Giovanni Caire - TILab 
 */
public class Sensor {
	private Vector eventQueue = new Vector();
	private Behaviour myManager;
	
	public synchronized void setManager(Behaviour b) {
		myManager = b;
		// If there were events already sensed --> restart the manager 
		// so that it can manages them
		if (myManager != null && eventQueue.size() > 0) {
			myManager.restart();
		}
	}
	
	public synchronized void post(Event ev) {
		// Insert the event in the queue
		eventQueue.addElement(ev);
		
		// Restart the sensor manager behaviour (if any) so that it can manage
		// the event
		if (myManager != null) {
			myManager.restart();
		}
	}	
	
	public synchronized Event get() {
		Event ev = null;
		if (eventQueue.size() > 0) {
			ev = (Event) eventQueue.elementAt(0);
			eventQueue.removeElementAt(0);
		}
		return ev;
	}			
}