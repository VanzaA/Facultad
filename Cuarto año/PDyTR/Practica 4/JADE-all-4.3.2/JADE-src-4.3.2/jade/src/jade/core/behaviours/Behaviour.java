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

import jade.util.leap.Serializable;

import jade.core.Agent;

/**
 Abstract base class for <b><em>JADE</em></b> behaviours.  Extending
 this class directly should only be needed for particular behaviours
 with special synchronization needs; this is because event based
 notification used for blocking and restarting behaviours is
 directly accessible at this level.
 
 
 @author Giovanni Rimassa - Universita' di Parma
 @version $Date: 2013-03-11 09:55:05 +0100 (lun, 11 mar 2013) $ $Revision: 6645 $
 
 */
public abstract class Behaviour implements Serializable {
	private static final long     serialVersionUID = 3487495895819001L;
	
	//#APIDOC_EXCLUDE_BEGIN
	
	/**
	 A constant for child-to-parent notifications.
	 @serial
	 */
	protected static final int NOTIFY_UP = -1;
	
	/**
	 A constant for parent-to-child notifications.
	 @serial
	 */
	protected static final int NOTIFY_DOWN = 1;
	
	/**
	 A constant identifying the runnable state.
	 @serial
	 */
	public static final String STATE_READY = "READY";
	
	/**
	 A constant identifying the blocked state.
	 @serial
	 */
	public static final String STATE_BLOCKED = "BLOCKED";
	
	/**
	 A constant identifying the running state.
	 @serial
	 */
	public static final String STATE_RUNNING = "RUNNING";
	
	
	/**
	 Event class for notifying blocked and restarted behaviours.
	 This class is used to notify interested behaviours when a
	 Behaviour changes its runnable state. It may be sent to
	 behaviour's parent (<em>upward notification</em> or to behaviour's
	 children (<em>downward notification</em>).
	 */
	protected class RunnableChangedEvent implements Serializable {
		private static final long     serialVersionUID = 3487495895819002L;
		/**
		 @serial
		 */
		private boolean runnable;
		
		/**
		 @serial
		 */
		private int direction;
		
		/**
		 Re-init event content. This method can be used to rewrite an
		 existing event with new data (much cheaper than making a new
		 object).
		 @param b A <code>boolean</code> flag; when <code>false</code>
		 it means that a behaviour passed from <em>Ready</em> to
		 <em>Blocked</em> state. When <code>true</code> it means that a
		 behaviour passed from <em>Blocked</em> to <em>Ready</em> (this
		 flag is the truth value of the predicate <em><b>'The behaviour
		 has now become runnable'</b></em>.
		 @param d A notification direction: when direction is
		 <code>NOTIFY_UP</code>, the event travels upwards the behaviour
		 containment hierarchy; when it is <code>NOTIFY_DOWN</code>, the
		 event travels downwards.
		 */
		public void init(boolean b, int d) {
			runnable = b;
			direction = d;
		}
		
		
		/**
		 Read event source.
		 @return The <code>Behaviour</code> object which generated this event.
		 */
		public Behaviour getSource() {
			return Behaviour.this;
		}
		
		/**
		 Check whether the event is runnable.
		 @return <code>true</code> when the behaviour generating this
		 event has become <em>Ready</em>, <code>false</code> when it has
		 become <em>Blocked</em>.  */
		public boolean isRunnable() {
			return runnable;
		}
		
		/**
		 Check which direction this event is travelling.
		 @return <code>true</code> when the event is a notification
		 going from a child behaviour to its parent; <code>false</code>
		 otherwise.
		 */
		public boolean isUpwards() {
			return direction == NOTIFY_UP;
		}
		
	} // End of RunnableChangedEvent class
	
	
	//#APIDOC_EXCLUDE_END
	
	
	private String myName;	
	private boolean startFlag = true;
	
	/**
	 The agent this behaviour belongs to.
	 
	 This is an instance variable that holds a reference to the Agent
	 object and allows the usage of its methods within the body of the
	 behaviour. As the class <code>Behaviour</code> is the superclass
	 of all the other behaviour classes, this variable is always
	 available. Of course, remind to use the appropriate constructor,
	 i.e. the one that accepts an agent object as argument; otherwise,
	 this variable is set to <code>null</code>.
	 */
	protected Agent myAgent;
	
	/**
	 Flag indicating whether this Behaviour is runnable or not
	 */
	private volatile boolean runnableState = true;
	private volatile long restartCounter = 0;
	private volatile String executionState = STATE_READY;
	
	//#APIDOC_EXCLUDE_BEGIN
	
	/**
	 This event object will be re-used for every state change
	 notification.
	 */
	protected RunnableChangedEvent myEvent = new RunnableChangedEvent();
	
	//#APIDOC_EXCLUDE_END
	
	//#CUSTOM_EXCLUDE_BEGIN
	/** 
	 The private data store of this Behaviour
	 */
	private DataStore myStore;
	
	void setParent(CompositeBehaviour cb) {
		parent = cb;
		if (parent != null) {
			myAgent = parent.myAgent;
		}
		wrappedParent = null;
	}
	
	void setWrappedParent(CompositeBehaviour cb) {
		wrappedParent = cb;
	}
	
	private CompositeBehaviour wrappedParent;
	//#APIDOC_EXCLUDE_BEGIN
	protected CompositeBehaviour parent;
	//#APIDOC_EXCLUDE_END
	
	/**
	 * Retrieve the enclosing CompositeBehaviour (if present). In order to access the parent behaviour 
	 * it is strongly suggested to use this method rather than the <core>parent</code> member variable
	 * directly. In case of threaded or wrapped behaviour in facts the latter may have unexpected values. 
	 @return The enclosing CompositeBehaviour (if present).
	 @see jade.core.behaviours.CompositeBehaviour
	 */
	protected CompositeBehaviour getParent() {
		if (wrappedParent != null) {
			return wrappedParent;
		}
		else {
			return parent;
		}
	}
	//#CUSTOM_EXCLUDE_END
	
	
	/**
	 Default constructor. It does not set the agent owning this
	 behaviour object.
	 */
	public Behaviour() {
		// Construct a default name
		myName = getClass().getName();
		// Remove the class name and the '$' characters from
		// the class name for readability.
		int dotIndex = myName.lastIndexOf('.');
		int dollarIndex = myName.lastIndexOf('$');
		int lastIndex = (dotIndex > dollarIndex ? dotIndex : dollarIndex);
		
		if (lastIndex != -1) {
			myName = myName.substring(lastIndex+1);
		}
	}
	
	/**
	 Constructor with owner agent.
	 @param a The agent owning this behaviour.
	 */
	public Behaviour(Agent a) {
		this();
		myAgent = a;
	}
	
	/**
	 Give a name to this behaviour object.
	 @param name The name to give to this behaviour.
	 */
	public final void setBehaviourName(String name) {
		myName = name;
	}
	
	/**
	 Retrieve the name of this behaviour object. If no explicit name
	 was set, a default one is given, based on the behaviour class
	 name.
	 @return The name of this behaviour.
	 */
	public final String getBehaviourName() {
		return myName;
	}
	
	/**
	 Runs the behaviour. This abstract method must be implemented by
	 <code>Behaviour</code>subclasses to perform ordinary behaviour
	 duty. An agent schedules its behaviours calling their
	 <code>action()</code> method; since all the behaviours belonging
	 to the same agent are scheduled cooperatively, this method
	 <b>must not</b> enter in an endless loop and should return as
	 soon as possible to preserve agent responsiveness. To split a
	 long and slow task into smaller section, recursive behaviour
	 aggregation may be used.
	 @see jade.core.behaviours.CompositeBehaviour
	 */
	public abstract void action();
	
	/**
	 Check if this behaviour is done. The agent scheduler calls this
	 method to see whether a <code>Behaviour</code> still need to be
	 run or it has completed its task. Concrete behaviours must
	 implement this method to return their completion state. Finished
	 behaviours are removed from the scheduling queue, while others
	 are kept within to be run again when their turn comes again.
	 @return <code>true</code> if the behaviour has completely executed.
	 */
	public abstract boolean done();
	
	/**
	 This method is just an empty placeholder for subclasses. It is
	 invoked just once after this behaviour has ended. Therefore,
	 it acts as an epilog for the task represented by this
	 <code>Behaviour</code>.
	 <br>
	 Note that <code>onEnd</code> is called after the behaviour has been
	 removed from the pool of behaviours to be executed by an agent. 
	 Therefore calling
	 <code>reset()</code> is not sufficient to cyclically repeat the task
	 represented by this <code>Behaviour</code>. In order to achieve that, 
	 this <code>Behaviour</code> must be added again to the agent 
	 (using <code>myAgent.addBehaviour(this)</code>). The same applies to
	 in the case of a <code>Behaviour</code> that is a child of a 
	 <code>ParallelBehaviour</code>.
	 @return an integer code representing the termination value of
	 the behaviour.
	 */
	public int onEnd() {
		return 0;
	}
	
	/**
	 This method is just an empty placeholders for subclasses. It is
	 executed just once before starting behaviour execution. 
	 Therefore, it acts as a prolog to the task
	 represented by this <code>Behaviour</code>.
	 */
	public void onStart() {
	}
	
	//#APIDOC_EXCLUDE_BEGIN
	
	/** 
	 This method is called internally by the JADE framework 
	 and should not be called by the user.
	 */
	public final void actionWrapper() {
		if (startFlag) {
			onStart();
			startFlag = false;
		}
		
		//#MIDP_EXCLUDE_BEGIN
		// Maybe the behaviour was removed from another thread
		if (myAgent != null) {
			myAgent.notifyChangeBehaviourState(this, Behaviour.STATE_READY, Behaviour.STATE_RUNNING);
		}
		//#MIDP_EXCLUDE_END
		action();
		//#MIDP_EXCLUDE_BEGIN
		if (myAgent != null) {
			myAgent.notifyChangeBehaviourState(this, Behaviour.STATE_RUNNING, Behaviour.STATE_READY);
		}
		//#MIDP_EXCLUDE_END
	}
	
	public final void setExecutionState(String s) {
		executionState = s;
	}
	public final String getExecutionState() {
		return executionState;
	}
	//#APIDOC_EXCLUDE_END
	
	
	/**
	 Restores behaviour initial state. This method must be implemented
	 by concrete subclasses in such a way that calling
	 <code>reset()</code> on a behaviour object is equivalent to
	 destroying it and recreating it back. The main purpose for this
	 method is to realize multistep cyclic behaviours without needing
	 expensive constructions an deletion of objects at each loop
	 iteration.
	 Remind to call super.reset() from the sub-classes.
	 */
	public void reset() {
		startFlag = true;
		restart();
	}
	
	//#APIDOC_EXCLUDE_BEGIN
	/**
	 Handler for block/restart events. This method handles
	 notification by copying its runnable state and then by simply
	 forwarding the event when it is traveling upwards and by doing
	 nothing when it is traveling downwards, since an ordinary
	 behaviour has no children.
	 @param rce The event to handle
	 */
	protected void handle(RunnableChangedEvent rce) {
		// Set the new runnable state
		setRunnable(rce.isRunnable());
		//#CUSTOM_EXCLUDE_BEGIN
		// If the notification is upwords and a parent exists -->
		// Notify the parent
		if( (parent != null) && (rce.isUpwards()) ) {
			parent.handle(rce);
		}
		//#CUSTOM_EXCLUDE_END
	}
	//#APIDOC_EXCLUDE_END
	
	/**
	 Returns the root for this <code>Behaviour</code> object. That is,
	 the top-level behaviour this one is a part of. Agents apply
	 scheduling only to top-level behaviour objects, so they just call
	 <code>restart()</code> on root behaviours.
	 @return The top-level behaviour this behaviour is a part of. If
	 this one is a top level behaviour itself, then simply
	 <code>this</code> is returned.
	 @see jade.core.behaviours.Behaviour#restart()
	 */
	public Behaviour root() {
		//#CUSTOM_EXCLUDE_BEGIN
		Behaviour p = getParent();
		if (p != null) {
			return p.root();
		}
		//#CUSTOM_EXCLUDE_END
		return this;
	}
	
	// Sets the runnable/not-runnable state
	void setRunnable(boolean runnable) {
		runnableState = runnable;
		if (runnableState) {
			restartCounter++;
		}
	}
	
	/**
	 Returns whether this <code>Behaviour</code> object is blocked or
	 not.
	 @return <code>true</code> when this behaviour is not blocked,
	 <code>false</code> when it is.
	 */
	public boolean isRunnable() {
		return runnableState;
	}
	
	//#APIDOC_EXCLUDE_BEGIN
	/**
	 * This method is used internally by the framework. Developer should not call or redefine it.
	 */
	public final long getRestartCounter() {
		return restartCounter;
	}
	//#APIDOC_EXCLUDE_END
	
	/**
	 Blocks this behaviour. It should be noticed that this method is NOT a 
	 blocking call: when it is invoked, the internal behaviour
	 state is set to <em>Blocked</em> so that, as soon as the <code>action()</code>
	 method returns, the behaviour is put into a blocked behaviours queue so that it will 
	 not be scheduled anymore.<br> 
	 The behaviour is moved back in the pool of active behaviours when either 
	 a message is received or the behaviour is explicitly restarted by means of its 
	 <code>restart()</code> method.<br> 
	 If this behaviour is a child of a <code>CompositeBehaviour</code> a suitable event is fired to
	 notify its parent behaviour up to the behaviour composition hierarchy root.
	 @see jade.core.behaviours.Behaviour#restart() 
	 */
	public void block() {
		handleBlockEvent();
	}
	
	//#APIDOC_EXCLUDE_BEGIN
	/**
	 * This method is used internally by the framework. Developer should not call or redefine it.
	 */
	protected void handleBlockEvent() {
		myEvent.init(false, NOTIFY_UP);
		handle(myEvent);
	}
	//#APIDOC_EXCLUDE_END
	
	
	/**
	 Blocks this behaviour for a specified amount of time. The
	 behaviour will be restarted when among the three following
	 events happens.
	 <ul>
	 <li> <em>A time of <code>millis</code> milliseconds has passed
	 since the call to <code>block()</code>.</em>
	 <li> <em>An ACL message is received by the agent this behaviour
	 belongs to.</em>
	 <li> <em>Method <code>restart()</code> is called explicitly on
	 this behaviour object.</em>
	 </ul>
	 @param millis The amount of time to block, in
	 milliseconds. <em><b>Notice:</b> a value of 0 for
	 <code>millis</code> is equivalent to a call to
	 <code>block()</code> without arguments.</em>
	 @see jade.core.behaviours.Behaviour#block()
	 */
	public void block(long millis) {
		// Note that it is important to block the behaviour before
		// adding a Timer to restart it in a millis time. In fact if
		// the two operations are cerried out the other way around, it 
		// could happen that the Timer expires before the block() 
		// operation is executed --> The TimerDispatcher thread restarts
		// the behaviour (that has not blocked yet) and just after the
		// behaviour blocks.
		block();
		if (myAgent != null) {
			myAgent.restartLater(this, millis);
		}
	}
	
	/**
	 Restarts a blocked behaviour. This method fires a suitable event
	 to notify this behaviour's parent. When the agent scheduler
	 inserts a blocked event back into the agent ready queue, it
	 restarts it automatically. When this method is called, any timer
	 associated with this behaviour object is cleared.
	 @see jade.core.behaviours.Behaviour#block()
	 */
	public void restart() {
		if(myAgent != null) {
			myAgent.removeTimer(this);
		}
		
		handleRestartEvent();
		
		if(myAgent != null) {
			myAgent.notifyRestarted(this);
		}
	}
	
	//#APIDOC_EXCLUDE_BEGIN
	/**
	 * This method is used internally by the framework. Developer should not call or redefine it.
	 */
	public void handleRestartEvent() {
		myEvent.init(true, NOTIFY_UP);		
		handle(myEvent);
	}
	//#APIDOC_EXCLUDE_END
	
	
	/**
	 Associates this behaviour with the agent it belongs to. There is
	 no need to call this method explicitly, since the
	 <code>addBehaviour()</code> call takes care of the association
	 transparently.
	 @param a The agent this behaviour belongs to.
	 @see jade.core.Agent#addBehaviour(Behaviour b)
	 */
	public void setAgent(Agent a) {
		myAgent = a;
	}
	
	public Agent getAgent() {
		return myAgent;
	}
	
	
	//#CUSTOM_EXCLUDE_BEGIN
	/**
	 Return the private data store of this <code>Behaviour</code>.
	 If it was null, a new DataStore is created and returned.
	 @return The private data store of this <code>Behaviour</code>
	 */
	public DataStore getDataStore() {
		if (myStore == null) {
			myStore =  new DataStore();
		}
		return myStore;
	}
	
	/**
	 Set the private data store of this <code>Behaviour</code>
	 @param ds the <code>DataStore</code> that this <code>Behaviour</code>
	 will use as its private data store
	 */
	public void setDataStore(DataStore ds) {
		myStore = ds;
	}  
	
	
	//#CUSTOM_EXCLUDE_END
}
