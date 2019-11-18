/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop multi-agent systems in compliance with the FIPA specifications.
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
   An abstract superclass for behaviours composed by many parts. This
   class holds inside a number of <b><em>children behaviours</em></b>.
   When a <code>CompositeBehaviour</code> receives it execution quantum
   from the agent scheduler, it executes one of its children according
   to some policy. This class must be extended to provide the actual
   scheduling policy to apply when running children behaviours.
   @see jade.core.behaviours.SequentialBehaviour
   @see jade.core.behaviours.ParallelBehaviour
   @see jade.core.behaviours.FSMBehaviour


   @author Giovanni Rimassa - Universita' di Parma
   @author Giovanni Caire - TILAB
   @version $Date: 2012-06-20 11:44:30 +0200 (mer, 20 giu 2012) $ $Revision: 6573 $

 */
public abstract class CompositeBehaviour extends Behaviour {

	/**
    This variable marks the state when no child-behaviour has been run yet.
	 */
	private boolean starting = true;
	/**
    This variable marks the state when all child-behaviours have been run.
	 */
	private boolean finished = false;

	private boolean currentDone;
	private int currentResult;

	//#APIDOC_EXCLUDE_BEGIN  
	protected boolean currentExecuted = false;
	//#APIDOC_EXCLUDE_END


	/**
     Default constructor, does not set the owner agent.
	 */
	protected CompositeBehaviour() {
		super();
	}

	/**
     This constructor sets the owner agent.
     @param a The agent this behaviour belongs to.
	 */
	protected CompositeBehaviour(Agent a) {
		super(a);
	} 

	/**
     Executes this <code>CompositeBehaviour</code>. This method 
     executes children according to the scheduling policy 
     defined by concrete subclasses that implements 
     the <code>scheduleFirst()</code> and <code>scheduleNext()</code>
     methods.
	 */
	public final void action() {
		if(starting) {
			scheduleFirst();
			starting = false;
		}
		else {
			if (currentExecuted) {
				scheduleNext(currentDone, currentResult);
			}
		}

		// Get the current child
		Behaviour current = getCurrent();
		currentExecuted = false;
		currentDone = false;
		currentResult = 0;

		if (current != null) {
			if (current.isRunnable()) {
				// Execute the current child
				current.actionWrapper();
				currentExecuted = true;

				// If it is done --> call its onEnd() method
				if (current.done()) {
					currentDone = true;
					currentResult = current.onEnd();
				}

				// Check if this CompositeBehaviour is finished
				finished = checkTermination(currentDone, currentResult);
			}
			else {
				// The currently scheduled child is not runnable --> This
				// Composite behaviour must block too and notify upwards
				myEvent.init(false, NOTIFY_UP);
				super.handle(myEvent);
			}
		}
		else {
			// There are no children to execute
			finished = true;
		}

	}

	/**
     Checks whether this behaviour has terminated.
     @return <code>true</code> if this <code>CompositeBehaviour</code>
     has finished executing, <code>false</code>otherwise.
	 */
	public final boolean done() {
		return (finished);
	}

	/**
	 * This method schedules the first child to be executed
	 */
	protected abstract void scheduleFirst();

	/**
	 * This method schedules the next child to be executed
	 * @param currentDone a flag indicating whether the just executed
	 * child has completed or not.
	 * @param currentResult the termination value (as returned by
	 * <code>onEnd()</code>) of the just executed child in the case this
	 * child has completed (otherwise this parameter is meaningless)
	 */
	protected abstract void scheduleNext(boolean currentDone, int currentResult);

	/**
	 * This methods is called after the execution of each child
	 * in order to check whether the <code>CompositeBehaviour</code>
	 * should terminate.
	 * @param currentDone a flag indicating whether the just executed
	 * child has completed or not.
	 * @param currentResult the termination value (as returned by
	 * <code>onEnd()</code>) of the just executed child in the case this
	 * child has completed (otherwise this parameter is meaningless)
	 * @return true if the <code>CompositeBehaviour</code>
	 * should terminate. false otherwise.
	 */
	protected abstract boolean checkTermination(boolean currentDone, int currentResult);

	/**
	 * This method returns the child behaviour currently 
	 * scheduled for execution
	 */
	protected abstract Behaviour getCurrent();

	/**
	 * This method returns a Collection view of the children of 
	 * this <code>CompositeBehaviour</code> 
	 */
	public abstract Collection getChildren();

	//#APIDOC_EXCLUDE_BEGIN
	/**
	 * This method is used internally by the framework. Developer should not call or redefine it.
	 */
	protected void handleBlockEvent() {
		// Notify upwards
		super.handleBlockEvent();

		// Then notify downwards
		myEvent.init(false, NOTIFY_DOWN);
		handle(myEvent);
	}

	/**
	 * This method is used internally by the framework. Developer should not call or redefine it.
	 */
	public void handleRestartEvent() {
		// Notify downwards
		myEvent.init(true, NOTIFY_DOWN);
		handle(myEvent);

		// Then notify upwards
		super.handleRestartEvent();
	}
	//#APIDOC_EXCLUDE_END

	/**
     Puts a <code>CompositeBehaviour</code> back in initial state. The
     internal state is cleaned up and <code>reset()</code> is
     recursively called for each child behaviour. 
	 */
	public void reset() {
		resetChildren();

		starting = true;
		finished = false;
		super.reset();
	}

	protected void resetChildren() {
		Collection c = getChildren();
		if (c != null) {
			Iterator it = c.iterator();
			while (it.hasNext()) {
				Behaviour b = (Behaviour) it.next();
				b.reset();
			}
		}
	}

	/**
     Associates this behaviour with the agent it belongs to.
     Overrides the method in the base class to propagate the
     setting to all children.
     @param a The agent this behaviour belongs to.
     @see jade.core.behaviours.Behaviour#setAgent(Agent a)
	 */
	public void setAgent(Agent a) {
		Collection c = getChildren();
		if (c != null) {
			Iterator it = c.iterator();
			while (it.hasNext()) {
				Behaviour b = (Behaviour) it.next();
				b.setAgent(a);
			}
		}

		super.setAgent(a);
	}

	//#APIDOC_EXCLUDE_BEGIN
	protected void registerAsChild(Behaviour b) {
		b.setParent(this);
	}
	//#APIDOC_EXCLUDE_END

}
