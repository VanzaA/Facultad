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
 
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.util.Event;

/**
 * @author Giovanni Caire - TILab 
 */
public abstract class SensorManager extends CyclicBehaviour {
	private Sensor mySensor;
	
	public SensorManager(Agent a, Sensor s) {
		super(a);
		mySensor = s;
		s.setManager(this);
	}
	
	public void action() {
		Event ev = mySensor.get();
		if (ev != null) {
			onEvent(ev);
		}
		else {
			block();
		}
	}
	
	protected abstract void onEvent(Event ev);
}