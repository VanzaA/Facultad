
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
   Composite behaviour with sequential children scheduling. It is a
   <code>CompositeBehaviour</code> that executes its children behaviours
   in sequential order, and terminates when its last child has ended.


   @author Giovanni Rimassa - Universita` di Parma
   @author Giovanni Caire - Telecom Italia Lab
   @version $Date: 2011-04-19 17:10:42 +0200(mar, 19 apr 2011) $ $Revision: 6404 $

 */
public class SequentialBehaviour extends SerialBehaviour {

	private List subBehaviours = new ArrayList();
	int current = 0;

	/**
     Default constructor. It does not set the owner agent for this
     behaviour.
	 */
	public SequentialBehaviour() {
	}

	/**
     This constructor sets the owner agent for this behaviour.
     @param a The agent this behaviour belongs to.
	 */
	public SequentialBehaviour(Agent a) {
		super(a);
	}

	/**
     Prepare the first child for execution
     @see jade.core.behaviours.CompositeBehaviour#scheduleFirst
	 */
	protected void scheduleFirst() {
		// Schedule the first child
		current = 0;
	}

	/**
     Sequential policy for children scheduling. This method schedules
     children behaviours one at a time, in a FIFO fashion.
     @see jade.core.behaviours.CompositeBehaviour#scheduleNext(boolean, int)
	 */
	protected void scheduleNext(boolean currentDone, int currentResult) {
		if (currentDone) {
			// Schedule the next child only if the current one is terminated
			current++;
		}
	}

	/**
     Check whether this <code>SequentialBehaviour</code> must terminate.
     @return true when the last child has terminated. false otherwise
     @see jade.core.behaviours.CompositeBehaviour#checkTermination
	 */
	protected boolean checkTermination(boolean currentDone, int currentResult) {
		return (currentDone && current >= (subBehaviours.size()-1));
	}

	/** 
     Get the current child
     @see jade.core.behaviours.CompositeBehaviour#getCurrent
	 */
	protected Behaviour getCurrent() {
		Behaviour b = null;
		if (subBehaviours.size() > current) {
			b = (Behaviour) subBehaviours.get(current);
		}
		return b;
	}

	/**
     Return a Collection view of the children of 
     this <code>SequentialBehaviour</code> 
     @see jade.core.behaviours.CompositeBehaviour#getChildren
	 */
	public Collection getChildren() {
		return subBehaviours;
	}

	/** 
     Add a sub behaviour to this <code>SequentialBehaviour</code>
	 */
	public void addSubBehaviour(Behaviour b) {
		subBehaviours.add(b);
		b.setParent(this);
		b.setAgent(myAgent);
	}

	/** 
     Remove a sub behaviour from this <code>SequentialBehaviour</code>
	 */
	public void removeSubBehaviour(Behaviour b) {
		boolean rc = subBehaviours.remove(b);
		if(rc) {
			b.setParent(null);
		}
		else {
			// The specified behaviour was not found. Do nothing
		}
	}

	public void reset() {
		super.reset();
		current = 0;
	}

	//#APIDOC_EXCLUDE_BEGIN

	public void skipNext() {
		current = subBehaviours.size();
	}

	//#APIDOC_EXCLUDE_END


	//#MIDP_EXCLUDE_BEGIN

	// For persistence service
	private Behaviour[] getSubBehaviours() {
		Object[] objs = subBehaviours.toArray();
		Behaviour[] result = new Behaviour[objs.length];
		for(int i = 0; i < objs.length; i++) {
			result[i] = (Behaviour)objs[i];
		}

		return result;
	}

	// For persistence service
	private void setSubBehaviours(Behaviour[] behaviours) {
		subBehaviours.clear();
		for(int i = 0; i < behaviours.length; i++) {
			subBehaviours.add(behaviours[i]);
		}
	}

	// For persistence service
	private int getCurrentIndex() {
		return current;
	}

	// For persistence service
	private void setCurrentIndex(int idx) {
		current = idx;
	}


	//#MIDP_EXCLUDE_END


}



