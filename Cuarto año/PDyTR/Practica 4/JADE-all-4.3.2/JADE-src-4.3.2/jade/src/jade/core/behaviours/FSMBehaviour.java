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

import java.util.Hashtable;
import jade.util.leap.*;
import jade.util.leap.Serializable;
import jade.util.Logger;

import jade.core.Agent;

/**
 Composite behaviour with Finite State Machine based children scheduling. 
 It is a <code>CompositeBehaviour</code> that executes its children 
 behaviours according to a FSM defined by the user. More specifically 
 each child represents a state in the FSM.
 The class provides methods to register states (sub-behaviours) and 
 transitions that defines how sub-behaviours will be scheduled.
 <p> At a minimum, the following steps are needed in order to properly
 define a <code>FSMBehaviour</code>:
 <ul>
 <li> register a single Behaviour as the initial state of the FSM by calling
 the method <code>registerFirstState</code>;
 <li> register one or more Behaviours as the final states of the FSM
 by calling the method <code>registerLastState</code>;
 <li> register one or more Behaviours as the intermediate states of the FSM
 by calling the method <code>registerState</code>;
 <li> for each state of the FSM, register the transitions to the other
 states by calling the method <code>registerTransition</code>;
 <li> the method <code>registerDefaultTransition</code> is also useful
 in order to register a default transition from a state to another state
 independently on the termination event of the source state.
 </ul>
 A number of other methods are available in this class for generic
 tasks, such as getting the current state or the name of a state, ...
 @see jade.core.behaviours.SequentialBehaviour
 @see jade.core.behaviours.ParallelBehaviour
 
 @author Giovanni Caire - CSELT
 @version $Date: 2008-04-15 11:09:57 +0200 (mar, 15 apr 2008) $ $Revision: 6029 $
 
 */
public class FSMBehaviour extends SerialBehaviour {
	
	private Map states = new HashMap();
	private Behaviour current = null;
	// Protected for debugging purposes only
	protected List lastStates = new ArrayList();
	protected String currentName = null;
	private String previousName = null;
	private String firstName = null;
	private int lastExitValue;
	
	// These variables are used to force a transition on a given state at runtime
	private boolean transitionForced = false;
	private String forcedTransitionDest = null;
	
	private TransitionTable theTransitionTable = new TransitionTable();
	
	//#J2ME_EXCLUDE_BEGIN
	private Logger myLogger = Logger.getMyLogger(FSMBehaviour.class.getName());
	//#J2ME_EXCLUDE_END
	
	/**
	 Default constructor, does not set the owner agent.
	 */
	public FSMBehaviour() {
		super();
	}
	
	/**
	 This constructor sets the owner agent.
	 @param a The agent this behaviour belongs to.
	 */
	public FSMBehaviour(Agent a) {
		super(a);
	} 
	
	/** 
	 Register a <code>Behaviour</code> as a state of this 
	 <code>FSMBehaviour</code>. When the FSM reaches this state
	 the registered <code>Behaviour</code> will be executed.
	 @param state The <code>Behaviour</code> representing the state
	 @param name The name identifying the state.
	 */
	public void registerState(Behaviour state, String name) {
		state.setBehaviourName(name);
		state.setParent(this);
		state.setAgent(myAgent);
		states.put(name, state);
		
		// Maybe we are over-writing the state that is currently in execution
		if (name.equals(currentName)) {
			current = state;
		}
	}
	
	/** 
	 Register a <code>Behaviour</code> as the initial state of this 
	 <code>FSMBehaviour</code>. 
	 @param state The <code>Behaviour</code> representing the state
	 @param name The name identifying the state.
	 */
	public void registerFirstState(Behaviour state, String name) {
		registerState(state, name);
		firstName = name;
	}
	
	/** 
	 Register a <code>Behaviour</code> as a final state of this 
	 <code>FSMBehaviour</code>. When the FSM reaches this state
	 the registered <code>Behaviour</code> will be executed and, 
	 when completed, the <code>FSMBehaviour</code> will terminate too. 
	 @param state The <code>Behaviour</code> representing the state
	 @param name The name identifying the state.
	 */
	public void registerLastState(Behaviour state, String name) {
		registerState(state, name);
		if (!lastStates.contains(name)) {
			lastStates.add(name);
		}
	}
	
	/** 
	 Deregister a state of this <code>FSMBehaviour</code>. 
	 @param name The name of the state to be deregistered.
	 @return the Behaviour if any that was registered as the 
	 deregistered state.
	 */
	public Behaviour deregisterState(String name) {
		Behaviour b = (Behaviour) states.remove(name);
		if (b != null) {
			b.setParent(null);
		}
		theTransitionTable.removeTransitionsFromState(name);
		if (name.equals(firstName)) {
			firstName = null;
		}
		lastStates.remove(name);
		return b;
	}
	
	/** 
	 Register a transition in the FSM defining the policy for
	 children scheduling of this <code>FSMBehaviour</code>.
	 @param s1 The name of the state this transition starts from
	 @param s2 The name of the state this transition leads to
	 @param event The termination event that fires this transition
	 as returned by the <code>onEnd()</code> method of the 
	 <code>Behaviour</code> representing state s1.
	 @see jade.core.behaviours.Behaviour#onEnd()
	 */
	public void registerTransition(String s1, String s2, int event) {
		registerTransition(s1, s2, event, null);
	}
	
	/** 
	 Register a transition in the FSM defining the policy for
	 children scheduling of this <code>FSMBehaviour</code>. 
	 When this transition is fired the states indicated in the
	 <code>toBeReset</code> parameter are reset. This is
	 particularly useful for transitions that lead to states that
	 have already been visited.
	 @param s1 The name of the state this transition starts from
	 @param s2 The name of the state this transition leads to
	 @param event The termination event that fires this transition
	 as returned by the <code>onEnd()</code> method of the 
	 <code>Behaviour</code> representing state s1.
	 @param toBeReset An array of strings including the names of 
	 the states to be reset.
	 @see jade.core.behaviours.Behaviour#onEnd()
	 */
	public void registerTransition(String s1, String s2, int event, String[] toBeReset) {
		Transition t = new Transition(this, s1, s2, event, toBeReset);
		theTransitionTable.addTransition(t);
	}
	
	/** 
	 Register a default transition in the FSM defining the policy for
	 children scheduling of this <code>FSMBehaviour</code>.
	 This transition will be fired when state s1 terminates with 
	 an event that is not explicitly associated to any transition. 
	 @param s1 The name of the state this transition starts from
	 @param s2 The name of the state this transition leads to
	 */
	public void registerDefaultTransition(String s1, String s2) {
		registerDefaultTransition(s1, s2, null);
	}
	
	/** 
	 Register a default transition in the FSM defining the policy for
	 children scheduling of this <code>FSMBehaviour</code>.
	 This transition will be fired when state s1 terminates with 
	 an event that is not explicitly associated to any transition. 
	 When this transition is fired the states indicated in the
	 <code>toBeReset</code> parameter are reset. This is
	 particularly useful for transitions that lead to states that
	 have already been visited.
	 @param s1 The name of the state this transition starts from
	 @param s2 The name of the state this transition leads to
	 @param toBeReset An array of strings including the names of 
	 the states to be reset.
	 */
	public void registerDefaultTransition(String s1, String s2, String[] toBeReset) {
		Transition t = new Transition(this, s1, s2, toBeReset);
		theTransitionTable.addTransition(t);
	}
	
	/**
	 * Deregister the transition from a given source state and identfied by a 
	 * given termination event. 
	 * @param source The name of the source state
	 * @param event The termination event that identifies the transition to be removed
	 */
	public void deregisterTransition(String source, int event) {
		theTransitionTable.removeTransition(source, event);
	}
	
	/**
	 * Deregister the default transition from a given source state.
	 * @param source The name of the source state
	 */
	public void deregisterDefaultTransition(String source) {
		theTransitionTable.removeTransition(source);
	}
	
	/**
	 * Check if a default transition exits from a given source state.
	 * @param source The name of the source state
	 * @return <code>true</code> if a default transition exits from the given source state. <code>false</code> otherwise.
	 */
	public boolean hasDefaultTransition(String source) {
		return (theTransitionTable.getTransition(source) != null);
	}
	
	/** 
	 Retrieve the child behaviour associated to the FSM state with
	 the given name.
	 @return the <code>Behaviour</code> representing the state whose
	 name is <code>name</code>, or <code>null</code> if no such
	 behaviour exists.
	 */
	public Behaviour getState(String name) {
		Behaviour b = null;
		if (name != null) {
			b = (Behaviour) states.get(name);
		}
		return b;
	}
	
	/** 
	 Retrieve the name of the FSM state associated to the given child
	 behaviour.
	 @return the name of the state represented by
	 <code>Behaviour</code> state, or <code>null</code> if the given
	 behaviour is not a child of this FSM behaviour.
	 */
	public String getName(Behaviour state) {
		Iterator it = states.keySet().iterator();
		while (it.hasNext()) {
			String name = (String) it.next();
			Behaviour s = (Behaviour) states.get(name);
			if (state == s) {
				return name;
			}
		}
		return null;
	}
	
	/** 
	 Retrieve the exit value of the most recently executed
	 child. This is also the trigger value that selects the next FSM
	 transition.
	 @return the exit value of the last executed state.
	 */
	public int getLastExitValue() {
		return lastExitValue;
	}
	
	/** 
	 Override the onEnd() method to return the exit value of the
	 last executed state.
	 */
	public int onEnd() {
		return getLastExitValue();
	}
	
	/**
	 Prepare the first child for execution. The first child is the 
	 <code>Behaviour</code> registered as the first state of this
	 <code>FSMBehaviour</code>
	 @see jade.core.behaviours.CompositeBehaviour#scheduleFirst
	 */
	protected void scheduleFirst() {
		if (transitionForced) {
			currentName = forcedTransitionDest;
			transitionForced = false;
		}
		else {
			// Normal case: go to the first state
			currentName = firstName;
		}
		current = getState(currentName);
		handleStateEntered(current);
		// DEBUG
		//System.out.println(myAgent.getLocalName()+" is Executing state "+currentName);
	}
	
	/**
	 This method schedules the next child to be executed. It checks 
	 whether the current child is completed and, in this case, fires
	 a suitable transition (according to the termination event of 
	 the current child) and schedules the child representing the 
	 new state.
	 @param currentDone a flag indicating whether the just executed
	 child has completed or not.
	 @param currentResult the termination value (as returned by
	 <code>onEnd()</code>) of the just executed child in the case this
	 child has completed (otherwise this parameter is meaningless)
	 @see jade.core.behaviours.CompositeBehaviour#scheduleNext(boolean, int)
	 */
	protected void scheduleNext(boolean currentDone, int currentResult) {
		if (currentDone) {
			try {
				previousName = currentName;
				if (transitionForced) {
					currentName = forcedTransitionDest;
					transitionForced = false;
				}
				else {
					// Normal case: use the TransitionTable to select the next state
					Transition t = theTransitionTable.getTransition(currentName, currentResult);
					resetStates(t.toBeReset);
					currentName = t.dest;
				}
				current = getState(currentName);
				if (current == null) {
					throw new NullPointerException();
				}
				else {
					handleStateEntered(current);
				}
			}
			catch (NullPointerException npe) {
				current = null;
				handleInconsistentFSM(previousName, currentResult);
			}
			// DEBUG
			//System.out.println(myAgent.getLocalName()+ " is Executing state "+currentName);
		}
	}
	
	protected void handleInconsistentFSM(String current, int event) {
		throw new RuntimeException("Inconsistent FSM. State: "+current+" event: "+event);
	}
	
	protected void handleStateEntered(Behaviour state) {
	}
	
	/**
	 Check whether this <code>FSMBehaviour</code> must terminate.
	 @return true when the last child has terminated and it 
	 represents a final state. false otherwise
	 @see jade.core.behaviours.CompositeBehaviour#checkTermination
	 */
	protected boolean checkTermination(boolean currentDone, int currentResult) {
		boolean ret = false;
		if (currentDone) {
			lastExitValue = currentResult;
			ret = lastStates.contains(currentName);
		}
		//#J2ME_EXCLUDE_BEGIN
		if (myLogger.isLoggable(Logger.FINE)) {
			myLogger.log(Logger.FINE, "FSM-Behaviour "+getBehaviourName()+": checkTermination() returning "+ret);
		}
		//#J2ME_EXCLUDE_END
		return ret;
	}  		
	
	/** 
	 Get the current child
	 @see jade.core.behaviours.CompositeBehaviour#getCurrent
	 */
	protected Behaviour getCurrent() {
		return current;
	}
	
	/**
	 Return a Collection view of the children of 
	 this <code>SequentialBehaviour</code> 
	 @see jade.core.behaviours.CompositeBehaviour#getChildren
	 */
	public Collection getChildren() {
		return states.values();
	}
	
	/**
	 Temporarily disregards the FSM structure, and jumps to the given
	 state. This method acts as a sort of <code>GOTO</code> statement
	 between states, and replaces the currently active state without
	 considering the trigger event or whether a transition was
	 registered. It should be used only to handle exceptional
	 conditions, if default transitions are not effective enough.
	 
	 @param next The name of the state to jump to at the next FSM
	 cheduling quantum. If the FSM has no state with the given name,
	 this method does nothing.
	 */
	protected void forceTransitionTo(String next) {
		// Just check that the forced transition leads into a valid state
		Behaviour b = getState(next);
		if (b != null) {
			transitionForced = true;
			forcedTransitionDest = next;
		}
	}
	
	/** 
	 Get the previously executed child
	 @see jade.core.behaviours.CompositeBehaviour#getCurrent
	 */
	protected Behaviour getPrevious() {
		return getState(previousName);
	}
	
	/**
	 Put this FSMBehaviour back in the initial condition.
	 */ 
	public void reset() {
		super.reset();
		transitionForced = false;
		forcedTransitionDest = null;
	}
	
	/**
	 Reset the children behaviours registered in the states indicated in
	 the <code>states</code> parameter.
	 @param states the names of the states that have to be reset
	 */
	public void resetStates(String[] states) {	
		if (states != null) {
			for(int i=0; i < states.length; i++){
				Behaviour b = getState(states[i]);
				b.reset();
			}
		}
	}
	
	
	/** 
	 * Inner class implementing the FSM transition table
	 */
	class TransitionTable implements Serializable {
		private Hashtable transitions = new Hashtable();
		private static final long serialVersionUID = 3487495895819003L;
		
		void clear() {
			transitions.clear();
		}
		
		void addTransition(Transition t) {
			String key1 = t.getFromState();
			
			TransitionsFromState tfs = (TransitionsFromState) transitions.get(key1);
			
			if (tfs == null) {
				tfs = new TransitionsFromState();
				transitions.put(key1, tfs);
			}
			
			if (t.isDefault()) {
				tfs.setDefaultTransition(t);
			}
			else {
				Integer key2 = new Integer(t.getTrigger());
				tfs.put(key2, t);
			}
		}
		
		void removeTransition(String s1, int event) {
			TransitionsFromState tfs = (TransitionsFromState)transitions.get(s1);
			if(tfs != null) {
				Transition t = (Transition)tfs.remove(new Integer(event));
				if(t != null) {
					
					if((tfs.isEmpty() && (tfs.getDefaultTransition() == null))) {
						transitions.remove(s1);
					}
				}
			}
		}
		
		void removeTransition(String s1) {
			TransitionsFromState tfs = (TransitionsFromState)transitions.get(s1);
			if(tfs != null) {
				tfs.setDefaultTransition(null);
				
				if (tfs.isEmpty()) {
					transitions.remove(s1);
				}
			}
		}
		
		Transition getTransition(String s, int event) {
			TransitionsFromState tfs = (TransitionsFromState) transitions.get(s);
			if(tfs != null) {
				Transition t = (Transition) tfs.get(new Integer(event));
				return t;
			}
			else {
				return null;
			}
		}
		
		Transition getTransition(String s) {
			TransitionsFromState tfs = (TransitionsFromState) transitions.get(s);
			if(tfs != null) {
				return tfs.getDefaultTransition();
			}
			else {
				return null;
			}
		}
		
		void removeTransitionsFromState(String stateName) {
			transitions.remove(stateName);
		}
	}
	
	
	/**
	 * Inner class Transition
	 */
	static class Transition implements Serializable {
		
		private FSMBehaviour fsm;
		private String src;
		private String dest;
		private int trigger;
		private boolean def;
		private String[] toBeReset;
		private static final long     serialVersionUID = 3487495895819004L;
		
		public Transition() {
		}
		
		public Transition(FSMBehaviour f, String s, String d, int t, String[] rs) {
			fsm = f;
			src = s;
			dest = d;
			trigger = t;
			def = false;
			toBeReset = rs;
		}
		
		public Transition(FSMBehaviour f, String s, String d, String[] rs) {
			fsm = f;
			src = s;
			dest = d;
			trigger = 0;
			def = true;
			toBeReset = rs;
		}
		
		public FSMBehaviour getFSM() {
			return fsm;
		}
		
		public void setFSM(FSMBehaviour f) {
			fsm = f;
		}
		
		public String getFromState() {
			return src;
		}
		
		public void setFromState(String f) {
			src = f;
		}
		
		public String getToState() {
			return dest;
		}
		
		public void setToState(String t) {
			dest = t;
		}
		
		public int getTrigger() {
			return trigger;
		}
		
		public void setTrigger(int t) {
			trigger = t;
		}
		
		public boolean isDefault() {
			return def;
		}
		
		public void setDefault(boolean d) {
			def = d;
		}
		
		public String[] getStatesToReset() {
			return toBeReset;
		}
		
		public void setStatesToReset(String[] ss) {
			toBeReset = ss;
		}	
		
		//#MIDP_EXCLUDE_BEGIN
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append("(TRANSITION trigger=").append(trigger).append(", source=").append(src).append(", dest=").append(dest).append(")");
			return sb.toString();
		}
		//#MIDP_EXCLUDE_END
	} // END of inner class Transition
	
	
	/**
	 * Inner class TransitionsFromState
	 */
	class TransitionsFromState extends Hashtable {
		private Transition defaultTransition = null;
		private static final long     serialVersionUID = 3487495895819005L;
		
		void setDefaultTransition(Transition dt) {
			defaultTransition = dt;
		}
		
		Transition getDefaultTransition() {
			return defaultTransition;
		}
		
		public Object get(Object key) {
			Transition t = (Transition) super.get(key);
			if (t == null) {
				t = defaultTransition;
			}
			return t;
		}
		
		//#MIDP_EXCLUDE_BEGIN
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append("Transitions: ");
			sb.append(super.toString());
			if (defaultTransition != null) {
				sb.append(" defaultTransition: "+defaultTransition);
			}
			return sb.toString();
		}
		//#MIDP_EXCLUDE_END
	} // END of inner class TransitionsFromState
	
	//#MIDP_EXCLUDE_BEGIN
	public String stringifyTransitionTable() {
		return theTransitionTable.transitions.toString();
	}
	//#MIDP_EXCLUDE_END
}
