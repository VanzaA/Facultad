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

import java.util.Vector;

import jade.util.leap.List;
import jade.util.leap.LinkedList;
import jade.util.leap.Iterator;
import jade.util.leap.EnumIterator;
import jade.util.leap.Serializable;

import jade.core.behaviours.Behaviour;

/**
 @author Giovanni Rimassa - Universita' di Parma
 @version $Date: 2005-12-16 16:37:25 +0100 (ven, 16 dic 2005) $ $Revision: 5844 $
 */

/**************************************************************
 
 Name: Scheduler
 
 Responsibility and Collaborations:
 
 + Selects the behaviour to execute.
 (Behaviour)
 
 + Holds together all the behaviours of an agent.
 (Agent, Behaviour)
 
 + Manages the resources needed to synchronize and execute agent
 behaviours, such as thread pools, locks, etc.
 
 ****************************************************************/
class Scheduler implements Serializable {
	
	
	//#MIDP_EXCLUDE_BEGIN
	protected List readyBehaviours = new LinkedList();
	protected List blockedBehaviours = new LinkedList();
	//#MIDP_EXCLUDE_END
	/*#MIDP_INCLUDE_BEGIN
	 protected Vector readyBehaviours = new Vector();
	 protected Vector blockedBehaviours = new Vector();
	 #MIDP_INCLUDE_END*/
	
	
	
	/**
	 @serial
	 */
	private Agent owner;
	
	/**
	 @serial
	 */
	private int currentIndex;
	
	public Scheduler(Agent a) {
		owner = a;
		currentIndex = 0;
	}
	
	// Add a behaviour at the end of the behaviours queue. 
	// This can never change the index of the current behaviour.
	// If the behaviours queue was empty notifies the embedded thread of
	// the owner agent that a behaviour is now available.
	public synchronized void add(Behaviour b) {
		//#MIDP_EXCLUDE_BEGIN
		readyBehaviours.add(b);
		//#MIDP_EXCLUDE_END
		/*#MIDP_INCLUDE_BEGIN
		 readyBehaviours.addElement(b);
		 #MIDP_INCLUDE_END*/
		notify();
		//#MIDP_EXCLUDE_BEGIN
		owner.notifyAddBehaviour(b);
		//#MIDP_EXCLUDE_END
	}
	
	// Moves a behaviour from the ready queue to the sleeping queue.
	public synchronized void block(Behaviour b) {
		if (removeFromReady(b)) {
			//#MIDP_EXCLUDE_BEGIN
			blockedBehaviours.add(b);
			//#MIDP_EXCLUDE_END
			/*#MIDP_INCLUDE_BEGIN
			 blockedBehaviours.addElement(b);
			 #MIDP_INCLUDE_END*/
			//#MIDP_EXCLUDE_BEGIN
			owner.notifyChangeBehaviourState(b, Behaviour.STATE_READY, Behaviour.STATE_BLOCKED);
			//#MIDP_EXCLUDE_END
		}
	}
	
	// Moves a behaviour from the sleeping queue to the ready queue.
	public synchronized void restart(Behaviour b) {
		if (removeFromBlocked(b)) {
			//#MIDP_EXCLUDE_BEGIN
			readyBehaviours.add(b);
			//#MIDP_EXCLUDE_END
			/*#MIDP_INCLUDE_BEGIN
			 readyBehaviours.addElement(b);
			 #MIDP_INCLUDE_END*/
			notify();
			//#MIDP_EXCLUDE_BEGIN
			owner.notifyChangeBehaviourState(b, Behaviour.STATE_BLOCKED, Behaviour.STATE_READY);
			//#MIDP_EXCLUDE_END
		}
	}
	
	/**
	 Restarts all behaviours. This method simply calls
	 Behaviour.restart() on every behaviour. The
	 Behaviour.restart() method then notifies the agent (with the
	 Agent.notifyRestarted() method), causing Scheduler.restart() to
	 be called (this also moves behaviours from the blocked queue to 
	 the ready queue --> we must copy all behaviours into a temporary
	 buffer to avoid concurrent modification exceptions).
	 Why not restarting only blocked behaviours?
	 Some ready behaviour can be a ParallelBehaviour with some of its
	 children blocked. These children must be restarted too.
	 */
	public synchronized void restartAll() {
		
		Behaviour[] behaviours = new Behaviour[readyBehaviours.size()];
		int counter = 0;
		//#MIDP_EXCLUDE_BEGIN
		for(Iterator it = readyBehaviours.iterator(); it.hasNext();)
			//#MIDP_EXCLUDE_END
			/*#MIDP_INCLUDE_BEGIN
			 for(Iterator it = new EnumIterator(readyBehaviours.elements()); it.hasNext();)
			 #MIDP_INCLUDE_END*/
			behaviours[counter++] = (Behaviour)it.next();
		
		for(int i = 0; i < behaviours.length; i++) {
			Behaviour b = behaviours[i];
			b.restart();
		}
		
		behaviours = new Behaviour[blockedBehaviours.size()];
		counter = 0;
		//#MIDP_EXCLUDE_BEGIN
		for(Iterator it = blockedBehaviours.iterator(); it.hasNext();) {
			//#MIDP_EXCLUDE_END
			/*#MIDP_INCLUDE_BEGIN
			 for(Iterator it = new EnumIterator(blockedBehaviours.elements()); it.hasNext();) {
			 #MIDP_INCLUDE_END*/
			
			//#DOTNET_EXCLUDE_BEGIN
			behaviours[counter++] = (Behaviour)it.next();
			//#DOTNET_EXCLUDE_END
			/*#DOTNET_INCLUDE_BEGIN
			 Object tmpB = null;
			 try // Hack: sometimes .NET inserts into this array a non-Behaviour object
			 { 
			 tmpB = it.next();
			 behaviours[counter++] = (Behaviour)tmpB;
			 }
			 catch(ClassCastException cce) 
			 {
			 System.out.println("Found an object of type "+tmpB.getClass().getName()+" instead of Behaviour");
			 cce.printStackTrace();
			 }
			 #DOTNET_INCLUDE_END*/
		}
		
		for(int i = 0; i < behaviours.length; i++) {
			Behaviour b = behaviours[i];
			/*#DOTNET_INCLUDE_BEGIN
			 if (b != null)
			 #DOTNET_INCLUDE_END*/
			b.restart();
			
		}
	}
	
	/**
	 Removes a specified behaviour from the scheduler
	 */
	public synchronized void remove(Behaviour b) {
		boolean found = removeFromBlocked(b);
		if(!found) {
			found = removeFromReady(b);
		}
		if (found) {
			//#MIDP_EXCLUDE_BEGIN
			owner.notifyRemoveBehaviour(b);    
			//#MIDP_EXCLUDE_END
		}
	}
	
	/**
	 Selects the appropriate behaviour for execution, with a trivial
	 round-robin algorithm.
	 */
	public synchronized Behaviour schedule() throws InterruptedException {
		while(readyBehaviours.isEmpty()) {
			owner.idle();
		}
		
		//#MIDP_EXCLUDE_BEGIN
		Behaviour b = (Behaviour)readyBehaviours.get(currentIndex);
		//#MIDP_EXCLUDE_END
		/*#MIDP_INCLUDE_BEGIN
		 Behaviour b = (Behaviour)readyBehaviours.elementAt(currentIndex);
		 #MIDP_INCLUDE_END*/
		currentIndex = (currentIndex + 1) % readyBehaviours.size();
		return b;
	}
	
	
	//#MIDP_EXCLUDE_BEGIN
	
	// Helper method for persistence service
	public synchronized Behaviour[] getBehaviours() {
		
		Behaviour[] result = new Behaviour[blockedBehaviours.size() + readyBehaviours.size()];
		Iterator itReady = readyBehaviours.iterator();
		Iterator itBlocked = blockedBehaviours.iterator();
		for(int i = 0; i < result.length; i++) {
			Behaviour b = null;
			if(itReady.hasNext()) {
				b = (Behaviour)itReady.next();
			}
			else {
				b = (Behaviour)itBlocked.next();
			}
			
			result[i] = b;
			
		}
		
		return result;
	}
	
	// Helper method for persistence service
	public void setBehaviours(Behaviour[] behaviours) {
		
		readyBehaviours.clear();
		blockedBehaviours.clear();
		
		for(int i = 0; i < behaviours.length; i++) {
			Behaviour b = behaviours[i];
			if(b.isRunnable()) {
				readyBehaviours.add(b);
			}
			else {
				blockedBehaviours.add(b);
			}
		}
		
		// The current index is not saved when persisting an agent
		currentIndex = 0;
	}
	
	//#MIDP_EXCLUDE_END
	
	
	// Removes a specified behaviour from the blocked queue.
	private boolean removeFromBlocked(Behaviour b) {
		//#MIDP_EXCLUDE_BEGIN
		return blockedBehaviours.remove(b);
		//#MIDP_EXCLUDE_END
		/*#MIDP_INCLUDE_BEGIN
		 return blockedBehaviours.removeElement(b);
		 #MIDP_INCLUDE_END*/
	}
	
	// Removes a specified behaviour from the ready queue.
	// This can change the index of the current behaviour, so a check is
	// made: if the just removed behaviour has an index lesser than the
	// current one, then the current index must be decremented.
	private boolean removeFromReady(Behaviour b) {
		int index = readyBehaviours.indexOf(b);
		if(index != -1) {
			//#MIDP_EXCLUDE_BEGIN
			readyBehaviours.remove(b);
			//#MIDP_EXCLUDE_END
			/*#MIDP_INCLUDE_BEGIN
			 readyBehaviours.removeElement(b);
			 #MIDP_INCLUDE_END*/
			if(index < currentIndex)
				--currentIndex;
			//if(currentIndex < 0)
			//  currentIndex = 0;
			else if (index == currentIndex && currentIndex == readyBehaviours.size())
				currentIndex = 0;
		}
		return index != -1;
	}
	
}

