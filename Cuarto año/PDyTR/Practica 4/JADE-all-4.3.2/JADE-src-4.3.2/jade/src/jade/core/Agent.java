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

import java.io.IOException;
import java.io.InterruptedIOException;

import jade.util.leap.Serializable;
import jade.util.leap.Iterator;
import java.util.Hashtable;
import java.util.Enumeration;

import jade.core.behaviours.Behaviour;

import jade.lang.acl.*;

import jade.security.JADESecurityException;

//#MIDP_EXCLUDE_BEGIN
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import jade.core.mobility.AgentMobilityHelper;
import jade.core.mobility.Movable;

import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Map;
import jade.util.leap.HashMap;
//#MIDP_EXCLUDE_END
import jade.util.leap.Properties;

/*#MIDP_INCLUDE_BEGIN
 import javax.microedition.midlet.*;
 #MIDP_INCLUDE_END*/

/**
 The <code>Agent</code> class is the common superclass for user
 defined software agents. It provides methods to perform basic agent
 tasks, such as:
 <ul>
 <li> <b> Message passing using <code>ACLMessage</code> objects,
 both unicast and multicast with optional pattern matching. </b></li>
 <li> <b> Complete Agent Platform life cycle support, including
 starting, suspending and killing an agent. </b></li>
 <li> <b> Scheduling and execution of multiple concurrent activities. </b></li>
 </ul>

 Application programmers must write their own agents as
 <code>Agent</code> subclasses, adding specific behaviours as needed
 and exploiting <code>Agent</code> class capabilities.

 @author Giovanni Rimassa - Universita' di Parma
 @author Giovanni Caire - TILAB
 @version $Date: 2014-02-18 14:37:13 +0100 (mar, 18 feb 2014) $ $Revision: 6702 $
 */
public class Agent implements Runnable, Serializable 
//#APIDOC_EXCLUDE_BEGIN
, TimerListener 
//#APIDOC_EXCLUDE_END
{
	private static final long     serialVersionUID = 3487495895819000L;

	/**
	 Inner class Interrupted.
	 This class is used to handle change state requests that occur
	 in particular situations such as when the agent thread is 
	 blocked in the doWait() method.
	 */
	public static class Interrupted extends Error {
		public Interrupted() {
			super();
		}
	}  // END of inner class Interrupted


	/**
	 Inner class AssociationTB.
	 This class manages bidirectional associations between Timer and
	 Behaviour objects, using hash tables. This class is 
	 synchronized with the operations
	 carried out by the TimerDispatcher. It allows also to avoid a deadlock when:
	 1) A behaviour blocks for a very short time --> A Timer is added
	 to the TimerDispatcher
	 2) The Timer immediately expires and the TimerDispatcher try to 
	 restart the behaviour before the pair (b, t) is added to the 
	 pendingTimers of this agent.
	 */
	private class AssociationTB {
		private Hashtable BtoT = new Hashtable();
		private Hashtable TtoB = new Hashtable();

		public void clear() {
			synchronized (theDispatcher) {
				Enumeration e = timers();
				while (e.hasMoreElements()) {
					Timer t = (Timer) e.nextElement();
					theDispatcher.remove(t);
				}

				BtoT.clear();
				TtoB.clear();

				//#J2ME_EXCLUDE_BEGIN

				// For persistence service
				persistentPendingTimers.clear();

				//#J2ME_EXCLUDE_END
			} //end synch
		}

		public void addPair(Behaviour b, Timer t) {
			TBPair pair = new TBPair(Agent.this, t, b);
			addPair(pair);
		}

		public void addPair(TBPair pair) {
			synchronized (theDispatcher) {
				if(pair.getOwner() == null) {
					pair.setOwner(Agent.this);
				}

				pair.setTimer(theDispatcher.add(pair.getTimer()));
				TBPair old = (TBPair)BtoT.put(pair.getBehaviour(), pair);
				if(old != null) {
					theDispatcher.remove(old.getTimer());
					//#J2ME_EXCLUDE_BEGIN
					persistentPendingTimers.remove(old);
					//#J2ME_EXCLUDE_END
					TtoB.remove(old.getTimer());
				}
				// Note that timers added to the TimerDispatcher are unique --> there
				// can't be an old value to handle
				TtoB.put(pair.getTimer(), pair);

				//#J2ME_EXCLUDE_BEGIN
				// For persistence service
				persistentPendingTimers.add(pair);
				//#J2ME_EXCLUDE_END
			} //end synch
		}

		public void removeMapping(Behaviour b) {
			synchronized (theDispatcher) {
				TBPair pair = (TBPair)BtoT.remove(b);
				if(pair != null) {
					TtoB.remove(pair.getTimer());

					//#J2ME_EXCLUDE_BEGIN
					// For persistence service
					persistentPendingTimers.remove(pair);
					//#J2ME_EXCLUDE_END

					theDispatcher.remove(pair.getTimer());
				}
			} //end synch
		}


		public Timer getPeer(Behaviour b) {
			// this is not synchronized because BtoT is an Hashtable (that is already synch!)
			TBPair pair = (TBPair)BtoT.get(b);
			if(pair != null) {
				return pair.getTimer();
			}
			else {
				return null;
			}
		}

		public Behaviour getPeer(Timer t) {
			// this is not synchronized because BtoT is an Hashtable (that is already synch!)
			TBPair pair = (TBPair)TtoB.get(t);
			if(pair != null) {
				return pair.getBehaviour();
			}
			else {
				return null;
			}
		}

		private Enumeration timers() {
			return TtoB.keys();
		}


	} // End of inner class AssociationTB 

	/** Inner class TBPair
	 *
	 */
	private static class TBPair {

		public TBPair() {
			expirationTime = -1;
		}

		public TBPair(Agent a, Timer t, Behaviour b) {
			owner = a;
			myTimer = t;
			expirationTime = t.expirationTime();
			myBehaviour = b;
		}

		public void setTimer(Timer t) {
			myTimer = t;
		}

		public Timer getTimer() {
			return myTimer;
		}

		public Behaviour getBehaviour() {
			return myBehaviour;
		}

		public void setBehaviour(Behaviour b) {
			myBehaviour = b;
		}


		public Agent getOwner() {
			return owner;
		}

		public void setOwner(Agent o) {
			owner = o;
			createTimerIfNeeded();
		}

		public long getExpirationTime() {
			return expirationTime;
		}

		public void setExpirationTime(long when) {
			expirationTime = when;
			createTimerIfNeeded();
		}

		// If both the owner and the expiration time have been set,
		// but the Timer object is still null, create one
		private void createTimerIfNeeded() {
			if(myTimer == null) {
				if((owner != null) && (expirationTime > 0)) {
					myTimer = new Timer(expirationTime, owner);
				}
			}
		}  

		private Timer myTimer;
		private long expirationTime;
		private Behaviour myBehaviour;
		private Agent owner;

	} // End of inner class TBPair 


	//#MIDP_EXCLUDE_BEGIN
	/**
	 Inner class CondVar
	 A simple class for a boolean condition variable
	 */
	private static class CondVar {
		private boolean value = false;

		public synchronized void waitOn() throws InterruptedException {
			while(!value) {
				wait();
			}
		}

		public synchronized void set() {
			value = true;
			notifyAll();
		}

	} // End of inner class CondVar 
	//#MIDP_EXCLUDE_END


	//#APIDOC_EXCLUDE_BEGIN

	/**
	 Schedules a restart for a behaviour, after a certain amount of
	 time has passed.
	 @param b The behaviour to restart later.
	 @param millis The amount of time to wait before restarting
	 <code>b</code>.
	 @see jade.core.behaviours.Behaviour#block(long millis)
	 */
	public void restartLater(Behaviour b, long millis) {
		if (millis <= 0) 
			return;
		Timer t = new Timer(System.currentTimeMillis() + millis, this);
		pendingTimers.addPair(b, t);
	}

	/**
	 Restarts the behaviour associated with t. 
	 This method runs within the time-critical Timer Dispatcher thread and
	 is not intended to be called by users. It is defined public only because
	 is part of the <code>TimerListener</code> interface.
	 */
	public void doTimeOut(Timer t) {
		Behaviour b = null;
		// This synchronized block avoids that if a behaviour is blocked 
		// again just after pendingTimers.getPeer(t) is called, a new mapping
		// is added before the old one is removed --> The new mapping is 
		// removed instead of the old one.
		// In any case b.restart() must be called outside the synchronized
		// block to avoid a deadlock between the TimerDispatcher and the Scheduler.
		synchronized (theDispatcher) {
			b = pendingTimers.getPeer(t);
			if(b != null) {
				pendingTimers.removeMapping(b);
			}
		}
		if(b != null) {
			b.restart();
		}
		else {
			System.out.println("Warning: No mapping found for expired timer "+t.expirationTime());
		}
	}

	/**
	 Notifies this agent that one of its behaviours has been restarted
	 for some reason. This method clears any timer associated with
	 behaviour object <code>b</code>, and it is unneeded by
	 application level code. To explicitly schedule behaviours, use
	 <code>block()</code> and <code>restart()</code> methods.
	 @param b The behaviour object which was restarted.
	 @see jade.core.behaviours.Behaviour#restart()
	 */
	public void notifyRestarted(Behaviour b) {
		// Did this restart() cause the root behaviour to become runnable ?
		// If so, put the root behaviour back into the ready queue.
		Behaviour root = b.root();
		if(root.isRunnable()) {
			myScheduler.restart(root);
		}
	}

	public void removeTimer(Behaviour b) {
		// The mapping for b in general has already been removed in doTimeOut().
		// There is however a case related to ParallelBehaviours where 
		// notifyRestarted() is not called as a consequence of a timer
		// expiration --> doTimeOut() is not called in this case -->
		// We remove the mapping in any case.
		Timer t = pendingTimers.getPeer(b);
		if(t != null) {
			pendingTimers.removeMapping(b);
		}
	}


	/**
	 Out of band value for Agent Platform Life Cycle states.
	 */
	public static final int AP_MIN = 0;   // Hand-made type checking

	/**
	 Represents the <em>initiated</em> agent state.
	 */
	public static final int AP_INITIATED = 1;

	/**
	 Represents the <em>active</em> agent state.
	 */
	public static final int AP_ACTIVE = 2;

	/**
	 Represents the <em>idle</em> agent state.
	 */
	public static final int AP_IDLE = 3;

	/**
	 Represents the <em>suspended</em> agent state.
	 */
	public static final int AP_SUSPENDED = 4;

	/**
	 Represents the <em>waiting</em> agent state.
	 */
	public static final int AP_WAITING = 5;

	/**
	 Represents the <em>deleted</em> agent state.
	 */
	public static final int AP_DELETED = 6;


	/**
	 Out of band value for Agent Platform Life Cycle states.
	 */
	public static final int AP_MAX = 13;    // Hand-made type checking

	//#MIDP_EXCLUDE_BEGIN  

	/**
	 These constants represent the various Domain Life Cycle states
	 */

	/**
	 Out of band value for Domain Life Cycle states.
	 */
	public static final int D_MIN = 9;     // Hand-made type checking

	/**
	 Represents the <em>active</em> agent state.
	 */
	public static final int D_ACTIVE = 10;

	/**
	 Represents the <em>suspended</em> agent state.
	 */
	public static final int D_SUSPENDED = 20;

	/**
	 Represents the <em>retired</em> agent state.
	 */
	public static final int D_RETIRED = 30;

	/**
	 Represents the <em>unknown</em> agent state.
	 */
	public static final int D_UNKNOWN = 40;

	/**
	 Out of band value for Domain Life Cycle states.
	 */
	public static final int D_MAX = 41;    // Hand-made type checking
	//#MIDP_EXCLUDE_END
	//#APIDOC_EXCLUDE_END


	public static final String MSG_QUEUE_CLASS = "jade_core_Agent_msgQueueClass";
	
	private transient AgentToolkit myToolkit;
	
	private transient MessageQueue msgQueue;
	private int msgQueueMaxSize = 0;
	//#MIDP_EXCLUDE_BEGIN
	private transient boolean temporaryMessageQueue;
	private transient List o2aQueue;
	private int o2aQueueSize = 0;
	private transient Map o2aLocks;
	private Behaviour o2aManager = null;
	private transient Object suspendLock;
	//#MIDP_EXCLUDE_END
	
	//#J2ME_EXCLUDE_BEGIN
	private java.util.Map<Class<?>, Object> o2aInterfaces;
	//#J2ME_EXCLUDE_END
	

	private String myName = null;  
	private AID myAID = null;
	private String myHap = null;

	private transient Object stateLock;

	private transient Thread myThread;
	private transient TimerDispatcher theDispatcher;

	private Scheduler myScheduler;

	private transient AssociationTB pendingTimers;

	private boolean restarting = false;

	private LifeCycle myLifeCycle;
	private LifeCycle myBufferedLifeCycle;
	private LifeCycle myActiveLifeCycle;
	private transient LifeCycle myDeletedLifeCycle;
	//#MIDP_EXCLUDE_BEGIN
	private transient LifeCycle mySuspendedLifeCycle;
	//#MIDP_EXCLUDE_END

	/**
	 This flag is used to distinguish the normal AP_ACTIVE state from
	 the particular case in which the agent state is set to AP_ACTIVE
	 during agent termination (takeDown()) to allow it to clean-up properly. 
	 In this case in fact a call to <code>doDelete()</code>, 
	 <code>doMove()</code>, <code>doClone()</code> and <code>doSuspend()</code>
	 should have no effect.
	 */
	private boolean terminating = false;

	//#MIDP_EXCLUDE_BEGIN
	/** 
	 When set to false (default) all behaviour-related events (such as ADDED_BEHAVIOUR
	 or CHANGED_BEHAVIOUR_STATE) are not generated in order to improve performances.
	 These events in facts are very frequent.
	 */
	private boolean generateBehaviourEvents = false;
	//#MIDP_EXCLUDE_END

	/*#MIDP_INCLUDE_BEGIN
	public static MIDlet midlet;

	// Flag for agent interruption (necessary as Thread.interrupt() is not available in MIDP)
	private boolean isInterrupted = false;
	#MIDP_INCLUDE_END*/

	/**
	 Default constructor.
	 */
	public Agent() {
		//#MIDP_EXCLUDE_BEGIN
		myToolkit = DummyToolkit.instance();
		o2aLocks = new HashMap();
		suspendLock = new Object();
		temporaryMessageQueue = true;
		//#MIDP_EXCLUDE_END
		msgQueue = new InternalMessageQueue(msgQueueMaxSize, this);
		stateLock = new Object(); 
		pendingTimers = new AssociationTB();
		myActiveLifeCycle = new ActiveLifeCycle();
		myLifeCycle = myActiveLifeCycle;
		myScheduler = new Scheduler(this);
		theDispatcher = TimerDispatcher.getTimerDispatcher();
		//#J2ME_EXCLUDE_BEGIN
		o2aInterfaces = new java.util.Hashtable<Class<?>, Object>();
		//#J2ME_EXCLUDE_END
	}
	
	//#MIDP_EXCLUDE_BEGIN
	/**
	 * Developer can override this method to provide an alternative message queue creation mechanism
	 * @return The MessageQueue to be used by this agent or null if the internal message queue must be used
	 */
	protected MessageQueue createMessageQueue() {
		String msgQueueClass = getProperty(MSG_QUEUE_CLASS, null);
		if (msgQueueClass != null) {
			try {
				return (MessageQueue) Class.forName(msgQueueClass).newInstance();
			}
			catch (Exception e) {
				System.out.println("Error loading MessageQueue of class "+msgQueueClass+" ["+e+"]");
			}
		}
		return null;
	}

	/**
	 * If the agent still has a temporary message queue, create the real one and copy messages if any
	 */
	void initMessageQueue() {
		if (temporaryMessageQueue) {
			temporaryMessageQueue = false;
			MessageQueue queue = createMessageQueue();
			if (queue != null) {
				queue.setMaxSize(msgQueueMaxSize);
				// Copy messages (if any) from the old message queue to the new one
				synchronized (msgQueue) {
					int size = msgQueue.size();
					if (size > 0) {
						List l = new ArrayList(size);
						msgQueue.copyTo(l);
						Iterator it = l.iterator();
						while (it.hasNext()) {
							queue.addLast((ACLMessage) it.next());
						}
					}
					msgQueue = queue;
				}
			}
		}
	}
	
	/**
	 This is only called by AgentContainerImpl
	 */
	MessageQueue getMessageQueue() {
		return msgQueue;
	}

	// For persistence service
	private void setMessageQueue(MessageQueue mq) {
		msgQueue = mq;
	}

	/**
	 Constructor to be used by special "agents" that will never powerUp.
	 */
	Agent(AID id) {
		setAID(id);
	}

	/** 
	 * Declared transient because the container changes in case
	 * of agent migration.
	 */
	private transient jade.wrapper.AgentContainer myContainer = null;

	/**
	 * Return a controller for the container this agent lives in. 
	 * <br>
	 * <b>NOT available in MIDP</b>
	 * <br>
	 * @return jade.wrapper.AgentContainer a controller for the container this agent lives in.
	 */
	public jade.wrapper.AgentContainer getContainerController() {
		if (myContainer == null) {  // first time called
			try {
				jade.security.JADEPrincipal principal = null;
				jade.security.Credentials credentials = null;
				try {
					jade.security.CredentialsHelper ch = (jade.security.CredentialsHelper) getHelper("jade.core.security.Security");
					principal = ch.getPrincipal();
					credentials = ch.getCredentials();
				}
				catch (ServiceException se) {
					// Security plug-in not present. Ignore it
				}
				myContainer = myToolkit.getContainerController(principal, credentials);
			} catch (Exception e) {
				throw new IllegalStateException("A ContainerController cannot be got for this agent. Probably the method has been called at an appropriate time before the complete initialization of the agent.");
			}
		}
		return myContainer;
	}
	//#MIDP_EXCLUDE_END


	private transient Object[] arguments = null;  // array of arguments
	//#APIDOC_EXCLUDE_BEGIN
	/**
	 * Called by AgentContainerImpl in order to pass arguments to a
	 * just created Agent. 
	 * <p>Usually, programmers do not need to call this method in their code.
	 * @see #getArguments() how to get the arguments passed to an agent
	 **/
	public final void setArguments(Object args[]) {
		// I have declared the method final otherwise getArguments would not work!
		arguments=args;
	}
	//#APIDOC_EXCLUDE_END

	/**
	 * Get the array of arguments passed to this agent.
	 * <p> Take care that the arguments are transient and they do not
	 * migrate with the agent neither are cloned with the agent!
	 * @return the array of arguments passed to this agent.
	 * @see <a href=../../../tutorials/ArgsAndPropsPassing.htm>How to use arguments or properties to configure your agent.</a>
	 **/
	public Object[] getArguments() {
		return arguments;
	}

	void setRestarting(boolean restarting) {
		this.restarting = restarting;
	}

	/**
	 * This method returns <code>true</code> when this agent is restarting after a crash.
	 * The restarting indication is automatically reset as soon as the <code>setup()</code> method of 
	 * this agent terminates.
	 * @return <code>true</code> when this agent is restarting after a crash. <code>false</code> otherwise.
	 */
	public final boolean isRestarting() {
		return restarting;
	}
	/**
	 Get the Agent ID for the platform AMS.
	 @return An <code>AID</code> object, that can be used to contact
	 the AMS of this platform.
	 */
	public final AID getAMS() {
		return myToolkit.getAMS();  
	}

	/**
	 Get the Agent ID for the platform default DF.
	 @return An <code>AID</code> object, that can be used to contact
	 the default DF of this platform.
	 */
	public AID getDefaultDF() {
		return myToolkit.getDefaultDF();
	}


	/**
	 Method to query the agent local name.
	 @return A <code>String</code> containing the local agent name
	 (e.g. <em>peter</em>).
	 */
	public final String getLocalName() {
		return myName;
	}

	/**
	 Method to query the agent complete name (<em><b>GUID</b></em>).
	 @return A <code>String</code> containing the complete agent name
	 (e.g. <em>peter@fipa.org:50</em>).
	 */
	public final String getName() { 
		if (myHap != null) {
			return myName + '@' + myHap;
		}
		else {
			return myName;
		}
	}

	/**
	 * Method to query the Home Agent Platform. This is the name of
	 * the platform where the agent has been created, therefore it will 
	 * never change during the entire lifetime of the agent.
	 * In JADE the name of an agent by default is composed by the 
	 * concatenation (using '@') of the agent local name and the Home 
	 * Agent Platform name 
	 *
	 * @return A <code>String</code> containing the name of the home agent platform
	 * (e.g. <em>myComputerName:1099/JADE</em>).
	 */
	public final String getHap() {
		return myHap;
	}

	/**
	 Method to query the private Agent ID. Note that this Agent ID is
	 <b>different</b> from the one that is registered with the
	 platform AMS.
	 @return An <code>Agent ID</code> object, containing the complete
	 agent GUID, addresses and resolvers.
	 */
	public final AID getAID() {
		return myAID;
	}

	void setAID(AID id) {
		myName = id.getLocalName();
		myHap = id.getHap();
		myAID = id;
	}

	/**
	 This method adds a new platform address to the AID of this Agent.
	 It is called by the container when a new MTP is activated
	 in the platform (in the local container - installMTP() -  
	 or in a remote container - updateRoutingTable()) to keep the 
	 Agent AID updated.
	 */
	synchronized void addPlatformAddress(String address) { // Mutual exclusion with Agent.powerUp()
		if (myAID != null) {
			// Cloning the AID is necessary as the agent may be using its AID.
			// If this is the case a ConcurrentModificationException would be thrown
			myAID = (AID)myAID.clone(); 
			myAID.addAddresses(address);
		}
	}

	/**
	 This method removes an old platform address from the AID of this Agent.
	 It is called by the container when a new MTP is deactivated
	 in the platform (in the local container - uninstallMTP() -  
	 or in a remote container - updateRoutingTable()) to keep the 
	 Agent AID updated.
	 */
	synchronized void removePlatformAddress(String address) { // Mutual exclusion with Agent.powerUp()
		if (myAID != null) {
			// Cloning the AID is necessary as the agent may be using its AID.
			// If this is the case a ConcurrentModificationException would be thrown
			myAID = (AID)myAID.clone(); 
			myAID.removeAddresses(address);
		}
	}

	/**
	 Method to retrieve the location this agent is currently at.
	 @return A <code>Location</code> object, describing the location
	 where this agent is currently running.
	 */
	public Location here() {
		return myToolkit.here();
	}

	//#APIDOC_EXCLUDE_BEGIN
	/**
	 * This method is used internally by the framework and should NOT be used by programmers.
	 * This is used by the agent container to wait for agent termination.
	 * We have already called doDelete on the thread which would have
	 * issued an interrupt on it. However, it still may decide not to exit.
	 * So we will wait no longer than 5 seconds for it to exit and we
	 * do not care of this zombie agent.
	 * FIXME: we must further isolate container and agents, for instance
	 * by using custom class loader and dynamic proxies and JDK 1.3.
	 * FIXME: the timeout value should be got by Profile
	 */
	public void join() {
		//#MIDP_EXCLUDE_BEGIN
		try {
			if(myThread == null) {
				return;
			}
			myThread.join(5000);
			if (myThread.isAlive()) {
				System.out.println("*** Warning: Agent " + myName + " did not terminate when requested to do so.");
				if(!myThread.equals(Thread.currentThread())) {
					myThread.interrupt();
					System.out.println("*** Second interrupt issued.");
				}
			}
		}
		catch(InterruptedException ie) {
			ie.printStackTrace();
		}
		//#MIDP_EXCLUDE_END
		/*#MIDP_INCLUDE_BEGIN
		 if (myThread != null && myThread.isAlive()) {
		 try {
		 myThread.join();
		 } 
		 catch (InterruptedException ie) {
		 ie.printStackTrace();
		 } 
		 } 
		 #MIDP_INCLUDE_END*/
	}
	//#APIDOC_EXCLUDE_END

	/**
	 Set message queue size. This method allows to change the number
	 of ACL messages that can be buffered before being actually read
	 by the agent or discarded.
	 @param newSize A non negative integer value to set message queue
	 size to. Passing 0 means unlimited message queue.  When the number of 
	 buffered
	 messages exceeds this value, older messages are discarded
	 according to a <b><em>FIFO</em></b> replacement policy.
	 @throws IllegalArgumentException If <code>newSize</code> is negative.
	 @see jade.core.Agent#getQueueSize()
	 */
	public void setQueueSize(int newSize) throws IllegalArgumentException {
		msgQueue.setMaxSize(newSize);
		msgQueueMaxSize = newSize;
	}

	/**
	 * This method retrieves the current length of the message queue
	 * of this agent.
	 * @return The number of messages that are currently stored into the
	 * message queue.
	 **/
	public int getCurQueueSize() {
		return msgQueue.size();
	}

	/**
	 Reads message queue size. A zero value means that the message
	 queue is unbounded (its size is limited only by amount of
	 available memory).
	 @return The actual size of the message queue (i.e. the max number
	 of messages that can be stored into the queue)
	 @see jade.core.Agent#setQueueSize(int newSize)
	 @see jade.core.Agent#getCurQueueSize()
	 */
	public int getQueueSize() {
		return msgQueue.getMaxSize();
	}


	/////////////////////////////////
	// Agent state management
	/////////////////////////////////
	public void changeStateTo(LifeCycle newLifeCycle) {
		boolean changed = false;
		newLifeCycle.setAgent(this);
		synchronized (stateLock) {
			if (!myLifeCycle.equals(newLifeCycle)) {
				// The new state is actually different from the current one
				if (myLifeCycle.transitionTo(newLifeCycle)) {
					myBufferedLifeCycle = myLifeCycle;
					myLifeCycle = newLifeCycle;
					changed = true;
					//#MIDP_EXCLUDE_BEGIN
					notifyChangedAgentState(myBufferedLifeCycle.getState(), myLifeCycle.getState());
					//#MIDP_EXCLUDE_END
				}
			}
		}
		if (changed) {
			myLifeCycle.transitionFrom(myBufferedLifeCycle);
			if (!Thread.currentThread().equals(myThread)) {
				// If the state-change is forced from the outside, interrupt 
				// the agent thread to allow the state change to take place
				interruptThread();
			}
		}
	}

	public void restoreBufferedState() {
		changeStateTo(myBufferedLifeCycle);
	}

	/**
	 The ActiveLifeCycle handles different internal states (INITIATED, 
	 ACTIVE, WAITING, IDLE). This method switches between them.
	 */
	private void setActiveState(int newState) {
		synchronized (stateLock) {
			if (myLifeCycle == myActiveLifeCycle) {
				int oldState = myLifeCycle.getState();
				if (newState != oldState) {
					((ActiveLifeCycle) myLifeCycle).setState(newState);
					//#MIDP_EXCLUDE_BEGIN
					notifyChangedAgentState(oldState, newState);
					//#MIDP_EXCLUDE_END
				}
			}
			else {
				// A change state request arrived in the meanwhile. 
				// Let it take place.
				throw new Interrupted();
			}
		}
	}

	//#APIDOC_EXCLUDE_BEGIN
	/**
	 Read current agent state. This method can be used to query an
	 agent for its state from the outside.
	 @return the Agent Platform Life Cycle state this agent is currently in.
	 */
	public int getState() {
		return myLifeCycle.getState();
	}
	//#APIDOC_EXCLUDE_END


	//#MIDP_EXCLUDE_BEGIN
	public AgentState getAgentState() {
		return AgentState.getInstance(getState());
	}

	/**
	 This is only called by the NotificationService to provide the Introspector
	 agent with a snapshot of the behaviours currently loaded in the agent
	 */
	Scheduler getScheduler() {
		return myScheduler;
	}

	/////////////////////////////
	// Mobility related code
	/////////////////////////////
	private transient AgentMobilityHelper mobHelper;

	private void initMobHelper() throws ServiceException {
		if (mobHelper == null) {
			mobHelper = (AgentMobilityHelper) getHelper(AgentMobilityHelper.NAME);
			mobHelper.registerMovable(new Movable() {
				public void beforeMove() {
					Agent.this.beforeMove();
				}

				public void afterMove() {
					Agent.this.afterMove();
				}

				public void beforeClone() {
					Agent.this.beforeClone();
				}

				public void afterClone() {
					Agent.this.afterClone();
				}
			} );
		}
	}

	/**
	 Make this agent move to a remote location. This method
	 is intended to support agent mobility and is called either by the
	 Agent Platform or by the agent itself to start a migration process.
	 It should be noted that this method just changes the agent 
	 state to <code>AP_TRANSIT</code>. The actual migration takes
	 place asynchronously.
	 <br>
	 <b>NOT available in MIDP</b>
	 <br>
	 @param destination The <code>Location</code> to migrate to.
	 */
	public void doMove(Location destination) {
		// Do nothing if the mobility service is not installed
		try {
			initMobHelper();
			mobHelper.move(destination);
		}
		catch(ServiceException se) {
			// FIXME: Log a proper warning
			return;
		}
	}


	/**
	 Make this agent be cloned on another location. This method
	 is intended to support agent mobility and is called either by the
	 Agent Platform or by the agent itself to start a clonation process.
	 It should be noted that this method just changes the agent 
	 state to <code>AP_COPY</code>. The actual clonation takes
	 place asynchronously.
	 <br>
	 <b>NOT available in MIDP</b>
	 <br>
	 @param destination The <code>Location</code> where the copy agent will start.
	 @param newName The name that will be given to the copy agent.
	 */
	public void doClone(Location destination, String newName) {
		// Do nothing if the mobility service is not installed
		try {
			initMobHelper();
			mobHelper.clone(destination, newName);
		}
		catch(ServiceException se) {
			// FIXME: Log a proper warning
			return;
		}
	}  
	//#MIDP_EXCLUDE_END

	/**
	 Make a state transition from <em>active</em> or <em>waiting</em>
	 to <em>suspended</em> within Agent Platform Life Cycle; the
	 original agent state is saved and will be restored by a
	 <code>doActivate()</code> call. This method can be called from
	 the Agent Platform or from the agent itself and stops all agent
	 activities. Incoming messages for a suspended agent are buffered
	 by the Agent Platform and are delivered as soon as the agent
	 resumes. Calling <code>doSuspend()</code> on a suspended agent
	 has no effect.
	 <br>
	 <b>NOT available in MIDP</b>
	 <br>
	 @see jade.core.Agent#doActivate()
	 */
	public void doSuspend() {
		//#MIDP_EXCLUDE_BEGIN
		if (mySuspendedLifeCycle == null) {
			mySuspendedLifeCycle = new SuspendedLifeCycle();
		}
		changeStateTo(mySuspendedLifeCycle);
		//#MIDP_EXCLUDE_END
	}

	/**
	 Make a state transition from <em>suspended</em> to
	 <em>active</em> or <em>waiting</em> (whichever state the agent
	 was in when <code>doSuspend()</code> was called) within Agent
	 Platform Life Cycle. This method is called from the Agent
	 Platform and resumes agent execution. Calling
	 <code>doActivate()</code> when the agent is not suspended has no
	 effect.
	 <br>
	 <b>NOT available in MIDP</b>
	 <br>
	 @see jade.core.Agent#doSuspend()
	 */
	public void doActivate() {
		//#MIDP_EXCLUDE_BEGIN
		//doExecute();
		restoreBufferedState();
		//#MIDP_EXCLUDE_END
	}

	/**
	 Make a state transition from <em>active</em> to <em>waiting</em>
	 within Agent Platform Life Cycle. This method has only effect 
	 when called by the agent thread and causes the agent to
	 block, stopping all its activities until  
	 a message arrives or 	the
	 <code>doWake()</code> method is called. 
	 @see jade.core.Agent#doWake()
	 */
	public void doWait() {
		doWait(0);
	}

	/**
	 Make a state transition from <em>active</em> to <em>waiting</em>
	 within Agent Platform Life Cycle. This method adds a timeout to
	 the other <code>doWait()</code> version.
	 @param millis The timeout value, in milliseconds.
	 @see jade.core.Agent#doWait()
	 */
	public void doWait(long millis) {
		if (Thread.currentThread().equals(myThread)) {
			setActiveState(AP_WAITING);

			synchronized(msgQueue) {
				try {
					// Blocks on msgQueue monitor for a while
					waitOn(msgQueue, millis);
				}
				catch (InterruptedException ie) {
					if (myLifeCycle != myActiveLifeCycle && !terminating) {
						// Change state request from the outside
						throw new Interrupted();
					}
					else {
						// Spurious wake up. Just print a warning
						System.out.println("Agent "+getName()+" interrupted while waiting");
					}    			
				}
				setActiveState(AP_ACTIVE);
			}
		}
	}

	/**
	 Make a state transition from <em>waiting</em> to <em>active</em>
	 within Agent Platform Life Cycle. This method is called from
	 Agent Platform and resumes agent execution. Calling
	 <code>doWake()</code> when an agent is not waiting has no effect.
	 @see jade.core.Agent#doWait()
	 */
	public void doWake() {
		synchronized(stateLock) {
			int previous = myLifeCycle.getState();
			if((previous == AP_WAITING) || (previous == AP_IDLE)) {
				setActiveState(AP_ACTIVE);
			}
		}
		if(myLifeCycle.isMessageAware()) {
			activateAllBehaviours();
			synchronized(msgQueue) {
				msgQueue.notifyAll(); // Wakes up the embedded thread
			}
		}
	}

	/**
	 Make a state transition from <em>active</em>, <em>suspended</em>
	 or <em>waiting</em> to <em>deleted</em> state within Agent
	 Platform Life Cycle, thereby destroying the agent. This method
	 can be called either from the Agent Platform or from the agent
	 itself. Calling <code>doDelete()</code> on an already deleted
	 agent has no effect.
	 */
	public void doDelete() {
		if (myDeletedLifeCycle == null) {
			myDeletedLifeCycle = new DeletedLifeCycle();
		}
		changeStateTo(myDeletedLifeCycle);
	}

	// This is called only by the scheduler
	void idle() throws InterruptedException {
		setActiveState(AP_IDLE);
		// No need for synchronized block since this is only called by the 
		// scheduler in the synchronized schedule() method
		waitOn(myScheduler, 0);
		setActiveState(AP_ACTIVE);
	}

	//#MIDP_EXCLUDE_BEGIN
	/**
	 Write this agent to an output stream; this method can be used to
	 record a snapshot of the agent state on a file or to send it
	 through a network connection. Of course, the whole agent must
	 be serializable in order to be written successfully.
	 <br>
	 <b>NOT available in MIDP</b>
	 <br>
	 @param s The stream this agent will be sent to. The stream is
	 <em>not</em> closed on exit.
	 @exception IOException Thrown if some I/O error occurs during
	 writing.
	 @see jade.core.Agent#read(InputStream s)
	 */
	public void write(OutputStream s) throws IOException {
		ObjectOutput out = new ObjectOutputStream(s);
		out.writeUTF(myName);
		out.writeObject(this);
	}

	/**
	 Read a previously saved agent from an input stream and restarts
	 it under its former name. This method can realize some sort of
	 mobility through time, where an agent is saved, then destroyed
	 and then restarted from the saved copy.
	 <br>
	 <b>NOT available in MIDP</b>
	 <br>
	 @param s The stream the agent will be read from. The stream is
	 <em>not</em> closed on exit.
	 @exception IOException Thrown if some I/O error occurs during
	 stream reading.
	 @see jade.core.Agent#write(OutputStream s)
	 *
	 public static void read(InputStream s) throws IOException {
	 try {
	 ObjectInput in = new ObjectInputStream(s);
	 String name = in.readUTF();
	 Agent a = (Agent)in.readObject();
	 a.doStart(name);
	 }
	 catch(ClassNotFoundException cnfe) {
	 cnfe.printStackTrace();
	 }
	 }*/

	/**
	 Read a previously saved agent from an input stream and restarts
	 it under a different name. This method can realize agent cloning
	 through streams, where an agent is saved, then an exact copy of
	 it is restarted as a completely separated agent, with the same
	 state but with different identity and address.
	 <br>
	 <b>NOT available in MIDP</b>
	 <br>
	 @param s The stream the agent will be read from. The stream is
	 <em>not</em> closed on exit.
	 @param agentName The name of the new agent, copy of the saved
	 original one.
	 @exception IOException Thrown if some I/O error occurs during
	 stream reading.
	 @see jade.core.Agent#write(OutputStream s)
	 *
	 public static void read(InputStream s, String agentName) throws IOException {
	 try {
	 ObjectInput in = new ObjectInputStream(s);
	 String oldName = in.readUTF();
	 Agent a = (Agent)in.readObject();
	 a.doStart(agentName);
	 }
	 catch(ClassNotFoundException cnfe) {
	 cnfe.printStackTrace();
	 }
	 }*/

	/**
	 This method reads a previously saved agent, replacing the current
	 state of this agent with the one previously saved. The stream
	 must contain the saved state of <b>the same agent</b> that it is
	 trying to restore itself; that is, <em>both</em> the Java object
	 <em>and</em> the agent name must be the same.
	 <br>
	 <b>NOT available in MIDP</b>
	 <br>
	 @param s The input stream the agent state will be read from.
	 @exception IOException Thrown if some I/O error occurs during
	 stream reading.
	 <em>Note: This method is currently not implemented</em>
	 */
	public void restore(InputStream s) throws IOException {
		// FIXME: Not implemented
	}

	/**
	 This method should not be used by application code. Use the
	 same-named method of <code>jade.wrapper.Agent</code> instead.
	 <br>
	 <b>NOT available in MIDP</b>
	 <br>
	 @see jade.wrapper.AgentController#putO2AObject(Object o, boolean blocking)
	 */
	public void putO2AObject(Object o, boolean blocking) throws InterruptedException {
		// Drop object on the floor if object-to-agent communication is
		// disabled.
		if(o2aQueue == null)
			return;

		CondVar cond = null;
		// the following block is synchronized because o2aQueue.add and o2aLocks.put must be
		// an atomic operation
		synchronized (o2aQueue) {
			// If the queue has a limited capacity and it is full, discard the
			// first element
			if((o2aQueueSize != 0) && (o2aQueue.size() == o2aQueueSize))
				o2aQueue.remove(0);

			o2aQueue.add(o);

			// If we are going to block, then activate behaviours after storing the CondVar object
			if(blocking) {
				cond = new CondVar();

				// Store lock for later, when getO2AObject will be called
				o2aLocks.put(o, cond);
			}
		} // end synchronization
		// Reactivate the O2AManager if any or the whole agent if no O2AManager is set. 
		if (o2aManager == null){
			// This method is synchronized on the scheduler
			activateAllBehaviours();
		} else {
			o2aManager.restart();
		}
		if (blocking)
			// Sleep on the condition. This method is synchronized on the condvar
			cond.waitOn();
	}

	/**
	 This method picks an object (if present) from the internal
	 object-to-agent communication queue. In order for this method to
	 work, the agent must have declared its will to accept objects
	 from other software components running within its JVM. This can
	 be achieved by calling the
	 <code>jade.core.Agent.setEnabledO2ACommunication()</code> method.
	 If the retrieved object was originally inserted by an external
	 component using a blocking call, that call will return during the
	 execution of this method.
	 <br>
	 <b>NOT available in MIDP</b>
	 <br>
	 @return the first object in the queue, or <code>null</code> if
	 the queue is empty.
	 @see jade.wrapper.AgentController#putO2AObject(Object o, boolean blocking)
	 @see jade.core.Agent#setEnabledO2ACommunication(boolean enabled, int queueSize)
	 */
	public Object getO2AObject() {

		// Return 'null' if object-to-agent communication is disabled
		if(o2aQueue == null)
			return null;

		CondVar cond = null;
		Object result = null;
		synchronized (o2aQueue) {
			if(o2aQueue.isEmpty())
				return null;

			// Retrieve the first object from the object-to-agent
			// communication queue
			result = o2aQueue.remove(0);

			// If some thread issued a blocking putO2AObject() call with this
			// object, wake it up. cond.set is synchronized on CondVar object
			cond = (CondVar)o2aLocks.remove(result);
		}

		if(cond != null) {
			cond.set();
		}

		return result;

	}


	/**
	 This method declares this agent attitude towards object-to-agent
	 communication, that is, whether the agent accepts to communicate
	 with other non-JADE components living within the same JVM.
	 <br>
	 <b>NOT available in MIDP</b>
	 <br>
	 @param enabled Tells whether Java objects inserted with
	 <code>putO2AObject()</code> will be accepted.
	 @param queueSize If the object-to-agent communication is enabled,
	 this parameter specifiies the maximum number of Java objects that
	 will be queued. If the passed value is 0, no maximum limit is set
	 up for the queue.

	 @see jade.wrapper.AgentController#putO2AObject(Object o, boolean blocking)
	 @see getO2AObject()

	 */
	public void setEnabledO2ACommunication(boolean enabled, int queueSize) {
		if(enabled) {
			if(o2aQueue == null)
				o2aQueue = new ArrayList(queueSize);

			// Ignore a negative value
			if(queueSize >= 0)
				o2aQueueSize = queueSize;
		}
		else {

			// Wake up all threads blocked in putO2AObject() calls
			Iterator it = o2aLocks.values().iterator();
			while(it.hasNext()) {
				CondVar cv = (CondVar)it.next();
				if (cv != null) cv.set();
			}

			o2aQueue = null;
		}

	}
	
	/**
	 * Sets the behaviour responsible for managing objects passed to the agent by
	 * means of the Object-To-Agent (O2A) communication mechanism.
	 * If the O2A manager behaviour is set, whenever an object is inserted in the
	 * O2A queue by means of the <code>putO2AObject()</code> method, only the manager 
	 * is waken up. This improves the efficiency since all behaviours not interested in 
	 * O2A communication remain sleeping. <br>
	 * NOTE that this method only declares a behaviour as being responsible for managing 
	 * objects received by the agent by means of the O2A communication channel; in order to 
	 * correctly run, the behaviour must be added to the agent by means of the 
	 * <code>addBehaviour()<code> method as usual.
	 <br>
	 <b>NOT available in MIDP</b>
	 <br>
	 * @param b The behaviour that will act as O2A manager.
	 * 
	 * @see jade.wrapper.AgentController#putO2AObject(Object o, boolean blocking)
	 * @see getO2AObject()
	 */
	public void setO2AManager(Behaviour b) {
		o2aManager = b;
	}
	//#MIDP_EXCLUDE_END
	
	//#J2ME_EXCLUDE_BEGIN
	/**
	 * Used internally by the framework
	 */
	@SuppressWarnings("unchecked")
	public <T> T getO2AInterface(Class<T> theInterface) {
		return (T)o2aInterfaces.get(theInterface);
	}

	/**
	 * Registers an implementation for a given O2A interface. All invocations
	 * to methods of the O2A interface will be redirected to the registered implementation object.
	 * @param theInterface The O2A interface the implementation is registered for.
	 * @param implementation The object providing an implementation for the given O2A interface.
	 */
	public <T> void registerO2AInterface(Class<T> theInterface, T implementation) {
		o2aInterfaces.put(theInterface, implementation);
	}
	//#J2ME_EXCLUDE_END


	//#APIDOC_EXCLUDE_BEGIN

	/**
	 This method is the main body of every agent. It 
	 provides startup and cleanup hooks for application 
	 programmers to put their specific code into.
	 @see jade.core.Agent#setup()
	 @see jade.core.Agent#takeDown()
	 */
	public final void run() {
		try {
			myLifeCycle.init();
			while (myLifeCycle.alive()) {
				try {
					myLifeCycle.execute();
					// Let other agents go on
					Thread.yield();
				}
				catch (JADESecurityException jse) {
					// FIXME: maybe we should send a message to the agent
					System.out.println("JADESecurityException: "+jse.getMessage());
				}
				catch (InterruptedException ie) {
					// Change LC state request from the outside. Just do nothing
					// and let the new LC state do its job
				}
				catch (InterruptedIOException ie) {
					// Change LC state request from the outside. Just do nothing
					// and let the new LC state do its job
				}
				catch (Interrupted i) {
					// Change LC state request from the outside. Just do nothing
					// and let the new LC state do its job
				}
			}
		}
		catch(Throwable t) {
			System.err.println("***  Uncaught Exception for agent " + myName + "  ***");
			t.printStackTrace();
		}
		terminating = true;
		myLifeCycle.end();
	}		
	//#APIDOC_EXCLUDE_END



	/**
	 Inner class ActiveLifeCycle
	 */
	private class ActiveLifeCycle extends LifeCycle {
		private static final long serialVersionUID = 11111;
		private ActiveLifeCycle() {
			super(AP_INITIATED);
		}

		public void setState(int s) {
			myState = s;
		}

		public void init() {
			setActiveState(AP_ACTIVE);
			//#MIDP_EXCLUDE_BEGIN
			notifyStarted();
			//#MIDP_EXCLUDE_END
			setup();
			restarting = false;
		}

		public void execute() throws JADESecurityException, InterruptedException, InterruptedIOException {
			// Select the next behaviour to execute
			Behaviour currentBehaviour = myScheduler.schedule();
			long oldRestartCounter = currentBehaviour.getRestartCounter();

			// Just do it!
			currentBehaviour.actionWrapper();

			// When it is needed no more, delete it from the behaviours queue
			if(currentBehaviour.done()) {
				currentBehaviour.onEnd();
				myScheduler.remove(currentBehaviour);
				currentBehaviour = null;
			}
			else {
				synchronized(myScheduler) {
					// If the current Behaviour has blocked and it was restarted in the meanwhile 
					// (e.g. because a message arrived), restart the behaviour to give it another chance.
					// Furthermore restart it even if it appears to be runnable since, due to the fact that block/restart 
					// events are managed in an un-synchronized way, we may end up in a situation where the root is runnable,
					// but some of its childern are not.
					if(oldRestartCounter != currentBehaviour.getRestartCounter()) {
						currentBehaviour.handleRestartEvent();
					}
					
					// Need synchronized block (Crais Sayers, HP): What if
					// 1) it checks to see if its runnable, sees its not,
					//    so it begins to enter the body of the if clause
					// 2) meanwhile, in another thread, a message arrives, so
					//    the behaviour is restarted and moved to the ready list.
					// 3) now back in the first thread, the agent executes the
					//    body of the if clause and, by calling block(), moves
					//   the behaviour back to the blocked list.
					if(!currentBehaviour.isRunnable()) {
						// Remove blocked behaviour from ready behaviours queue
						// and put it in blocked behaviours queue
						myScheduler.block(currentBehaviour);
						currentBehaviour = null;
					}
				}
			}
		}

		public void end() {
			clean(false);
		}

		public boolean transitionTo(LifeCycle to) {
			// We can go to whatever state unless we are terminating
			if (!terminating) {
				// The agent is going to leave this state. When 
				// the agent will enter this state again it must be 
				// in AP_ACTIVE
				myState = AP_ACTIVE;
				return true;
			}
			else {
				return false;
			}
		}

		public void transitionFrom(LifeCycle from) {
			activateAllBehaviours();
		}

		public boolean isMessageAware() {
			return true;
		}
	} // END of inner class ActiveLifeCycle


	/**
	 Inner class DeletedLifeCycle
	 */
	private class DeletedLifeCycle extends LifeCycle {
		private static final long serialVersionUID = 11112;
		private DeletedLifeCycle() {
			super(AP_DELETED);
		}

		public void end() {
			clean(true);
		}

		public boolean alive() {
			return false;
		}		
	} // END of inner class DeletedLifeCycle

	//#MIDP_EXCLUDE_BEGIN
	/**
	 Inner class SuspendedLifeCycle
	 */
	private class SuspendedLifeCycle extends LifeCycle {
		private static final long serialVersionUID = 11113;
		private SuspendedLifeCycle() {
			super(AP_SUSPENDED);
		}

		public void execute() throws JADESecurityException, InterruptedException, InterruptedIOException {
			waitUntilActivate();
		}

		public void end() {
			clean(false);
		}

		public boolean transitionTo(LifeCycle to) {
			// We can only die or resume
			return (to.getState() == AP_ACTIVE || to.getState() == AP_DELETED); 
		}		
	} // END of inner class SuspendedLifeCycle

	//#MIDP_EXCLUDE_END


	//#APIDOC_EXCLUDE_BEGIN
	public void clean(boolean ok) {
		if (!ok) {
			System.out.println("ERROR: Agent " + myName + " died without being properly terminated !!!");
			System.out.println("State was " + myLifeCycle.getState());
		}
		//#MIDP_EXCLUDE_BEGIN
		// Reset the interrupted state of the Agent Thread
		Thread.interrupted();
		//#MIDP_EXCLUDE_END

		myBufferedLifeCycle = myLifeCycle;
		myLifeCycle = myActiveLifeCycle;
		takeDown();
		pendingTimers.clear();
		myToolkit.handleEnd(myAID);
		myLifeCycle = myBufferedLifeCycle;
	}
	//#APIDOC_EXCLUDE_END

	/**
	 This protected method is an empty placeholder for application
	 specific startup code. Agent developers can override it to
	 provide necessary behaviour. When this method is called the agent
	 has been already registered with the Agent Platform <b>AMS</b>
	 and is able to send and receive messages. However, the agent
	 execution model is still sequential and no behaviour scheduling
	 is active yet.

	 This method can be used for ordinary startup tasks such as
	 <b>DF</b> registration, but is essential to add at least a
	 <code>Behaviour</code> object to the agent, in order for it to be
	 able to do anything.
	 @see jade.core.Agent#addBehaviour(Behaviour b)
	 @see jade.core.behaviours.Behaviour
	 */
	protected void setup() {}

	/**
	 This protected method is an empty placeholder for application
	 specific cleanup code. Agent developers can override it to
	 provide necessary behaviour. When this method is called the agent
	 has not deregistered itself with the Agent Platform <b>AMS</b>
	 and is still able to exchange messages with other
	 agents. However, no behaviour scheduling is active anymore and
	 the Agent Platform Life Cycle state is already set to
	 <em>deleted</em>.

	 This method can be used for ordinary cleanup tasks such as
	 <b>DF</b> deregistration, but explicit removal of all agent
	 behaviours is not needed.
	 */
	protected void takeDown() {}

	//#MIDP_EXCLUDE_BEGIN
	/**
	 * This empty placeholder shall be overridden by user defined agents 
	 * to execute some actions before the original agent instance on the 
	 * source container is stopped (e.g. releasing local resources such 
	 * as a GUI).<br>
	 * <b>IMPORTANT:</b> At this point, it is ensured that the move process
	 * is successful and that a moved agent instance has been created on the 
	 * destination container 
	 * Therefore setting the value of a class field in this method will have
	 * no impact on the moved agent instance. Such parameters must indeed be 
	 * set <b>before</b> the <code>doMove()</code> method is called.
	 <br>
	 <b>NOT available in MIDP</b>
	 <br>
	 */
	protected void beforeMove() {}

	/**
	 Actions to perform after moving. This empty placeholder method can be
	 overridden by user defined agents to execute some actions just after
	 arriving to the destination agent container for a migration.
	 <br>
	 <b>NOT available in MIDP</b>
	 <br>
	 */
	protected void afterMove() {}

	/**
	 * This empty placeholder method shall be overridden by user defined agents 
	 * to execute some actions before copying an agent to another agent container.
	 * <br>
	 * <b>NOT available in MIDP</b>
	 * <br>
	 * @see beforeMove()
	 * @see afterClone()
	 */
	protected void beforeClone() {}

	/**
	 Actions to perform after cloning. This empty placeholder method can be
	 overridden by user defined agents to execute some actions just after
	 creating an agent copy to the destination agent container.
	 <br>
	 <b>NOT available in MIDP</b>
	 <br>
	 */
	protected void afterClone() {}
	//#MIDP_EXCLUDE_END

	// This method is used by the Agent Container to fire up a new agent for the first time
	// Mutual exclusion with itself and Agent.addPlatformAddress()
	synchronized void powerUp(AID id, Thread t) {
		if (myThread == null) {
			// Set this agent's name and address and start its embedded thread
			myName = id.getLocalName();
			myHap = id.getHap();

			myAID = id;
			myToolkit.setPlatformAddresses(myAID);

			myThread = t;
			myThread.start();
		}
	}

	//#J2ME_EXCLUDE_BEGIN
	// Return agent thread
	// Package scooped as it is called by JadeMisc add-on for container monitor purpose
	Thread getThread() {
		return myThread;
	}
	//#J2ME_EXCLUDE_END

	//#MIDP_EXCLUDE_BEGIN
	private void writeObject(ObjectOutputStream out) throws IOException {
		// Updates the queue maximum size field, before serialising
		msgQueueMaxSize = msgQueue.getMaxSize();

		out.defaultWriteObject();
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();

		// Restore transient fields apart from myThread, that will be set when the agent will be powered up)
		stateLock = new Object();
		suspendLock = new Object();
		pendingTimers = new AssociationTB();
		theDispatcher = TimerDispatcher.getTimerDispatcher();
		// restore O2AQueue
		if (o2aQueueSize > 0) 
			o2aQueue = new ArrayList(o2aQueueSize);
		o2aLocks = new HashMap();
		myToolkit = DummyToolkit.instance();
		temporaryMessageQueue = true;
		msgQueue = new InternalMessageQueue(msgQueueMaxSize, this);

		//#PJAVA_EXCLUDE_BEGIN
		//For persistence service
		persistentPendingTimers = new java.util.HashSet();
		//#PJAVA_EXCLUDE_END
	}
	//#MIDP_EXCLUDE_END


	/**
	 This method is executed when blockingReceive() is called
	 from a separate Thread. 
	 It does not affect the agent state.
	 */
	private void waitUntilWake(long millis) {
		synchronized(msgQueue) {
			try {
				// Blocks on msgQueue monitor for a while
				waitOn(msgQueue, millis);
			}
			catch (InterruptedException ie) {
				throw new Interrupted();
			}
		}
	}

	//#MIDP_EXCLUDE_BEGIN
	private void waitUntilActivate() throws InterruptedException {
		synchronized(suspendLock) {
			waitOn(suspendLock, 0);
		}
	}
	//#MIDP_EXCLUDE_END

	/**
	 This method adds a new behaviour to the agent. This behaviour
	 will be executed concurrently with all the others, using a
	 cooperative round robin scheduling.  This method is typically
	 called from an agent <code>setup()</code> to fire off some
	 initial behaviour, but can also be used to spawn new behaviours
	 dynamically.
	 @param b The new behaviour to add to the agent.
	 @see jade.core.Agent#setup()
	 @see jade.core.behaviours.Behaviour
	 */
	public void addBehaviour(Behaviour b) {
		b.setAgent(this);
		myScheduler.add(b);
	}

	/**
	 This method removes a given behaviour from the agent. This method
	 is called automatically when a top level behaviour terminates,
	 but can also be called from a behaviour to terminate itself or
	 some other behaviour.
	 @param b The behaviour to remove.
	 @see jade.core.behaviours.Behaviour
	 */
	public void removeBehaviour(Behaviour b) {
		myScheduler.remove(b);
		b.setAgent(null);
	}

	/**
	 Send an <b>ACL</b> message to another agent. This methods sends
	 a message to the agent specified in <code>:receiver</code>
	 message field (more than one agent can be specified as message
	 receiver).
	 @param msg An ACL message object containing the actual message to
	 send.
	 @see jade.lang.acl.ACLMessage
	 */
	public final void send(ACLMessage msg) {
		// Set the sender of the message if not yet set
		// FIXME. Probably we should always set the sender of the message!
		try {
			msg.getSender().getName().charAt(0);
		}
		catch (Exception e) {
			msg.setSender(myAID);
		}
		boolean cloneMessage = !("true".equals(msg.clearUserDefinedParameter(ACLMessage.NO_CLONE)));
		myToolkit.handleSend(msg, myAID, cloneMessage);
	}

	/**
	 Receives an <b>ACL</b> message from the agent message
	 queue. This method is non-blocking and returns the first message
	 in the queue, if any. Therefore, polling and busy waiting is
	 required to wait for the next message sent using this method.
	 @return A new ACL message, or <code>null</code> if no message is
	 present.
	 @see jade.lang.acl.ACLMessage
	 */
	public final ACLMessage receive() {
		return receive(null);
	}

	/**
	 Receives an <b>ACL</b> message matching a given template. This
	 method is non-blocking and returns the first matching message in
	 the queue, if any. Therefore, polling and busy waiting is
	 required to wait for a specific kind of message using this method.
	 @param pattern A message template to match received messages
	 against.
	 @return A new ACL message matching the given template, or
	 <code>null</code> if no such message is present.
	 @see jade.lang.acl.ACLMessage
	 @see jade.lang.acl.MessageTemplate
	 */
	public final ACLMessage receive(MessageTemplate pattern) {
		ACLMessage msg = null;
		synchronized (msgQueue) {
			msg = msgQueue.receive(pattern);
			//#MIDP_EXCLUDE_BEGIN
			if (msg != null) {
				myToolkit.handleReceived(myAID, msg);
			 }
			//#MIDP_EXCLUDE_END
		}
		return msg;
	}

	/**
	 Receives an <b>ACL</b> message from the agent message
	 queue. This method is blocking and suspends the whole agent until
	 a message is available in the queue. 
	 @return A new ACL message, blocking the agent until one is
	 available.
	 @see jade.core.Agent#receive()
	 @see jade.lang.acl.ACLMessage
	 */
	public final ACLMessage blockingReceive() {
		return blockingReceive(null, 0);
	}

	/**
	 Receives an <b>ACL</b> message from the agent message queue,
	 waiting at most a specified amount of time.
	 @param millis The maximum amount of time to wait for the message.
	 @return A new ACL message, or <code>null</code> if the specified
	 amount of time passes without any message reception.
	 */
	public final ACLMessage blockingReceive(long millis) {
		return blockingReceive(null, millis);
	}

	/**
	 Receives an <b>ACL</b> message matching a given message
	 template. This method is blocking and suspends the whole agent
	 until a message is available in the queue. 
	 @param pattern A message template to match received messages
	 against.
	 @return A new ACL message matching the given template, blocking
	 until such a message is available.
	 @see jade.core.Agent#receive(MessageTemplate)
	 @see jade.lang.acl.ACLMessage
	 @see jade.lang.acl.MessageTemplate
	 */
	public final ACLMessage blockingReceive(MessageTemplate pattern) {
		return blockingReceive(pattern, 0);
	}


	/**
	 Receives an <b>ACL</b> message matching a given message template,
	 waiting at most a specified time.
	 @param pattern A message template to match received messages
	 against.
	 @param millis The amount of time to wait for the message, in
	 milliseconds.
	 @return A new ACL message matching the given template, or
	 <code>null</code> if no suitable message was received within
	 <code>millis</code> milliseconds.
	 @see jade.core.Agent#blockingReceive()
	 */
	public final ACLMessage blockingReceive(MessageTemplate pattern, long millis) {
		ACLMessage msg = null;
		synchronized(msgQueue) {
			msg = receive(pattern);
			long timeToWait = millis;
			while(msg == null) {
				long startTime = System.currentTimeMillis();
				if (Thread.currentThread().equals(myThread)) {
					doWait(timeToWait);
				}
				else {
					// blockingReceive() called from an external thread --> Do not change the agent state
					waitUntilWake(timeToWait);
				}
				long elapsedTime = System.currentTimeMillis() - startTime;

				msg = receive(pattern);

				if(millis != 0) {
					timeToWait -= elapsedTime;
					if(timeToWait <= 0)
						break;
				}
			}
		}
		return msg;
	}

	/**
	 Puts a received <b>ACL</b> message back into the message
	 queue. This method can be used from an agent behaviour when it
	 realizes it read a message of interest for some other
	 behaviour. The message is put in front of the message queue, so
	 it will be the first returned by a <code>receive()</code> call.
	 @see jade.core.Agent#receive()
	 */
	public final void putBack(ACLMessage msg) {
		synchronized(msgQueue) {
			msgQueue.addFirst(msg);
		}
	}

	final void setToolkit(AgentToolkit at) {
		myToolkit = at;
	}

	final void resetToolkit() {
		//#MIDP_EXCLUDE_BEGIN
		myToolkit = DummyToolkit.instance();
		//#MIDP_EXCLUDE_END
		/*#MIDP_INCLUDE_BEGIN
		 myToolkit = null;
		 #MIDP_INCLUDE_END*/
	}


	//#MIDP_EXCLUDE_BEGIN
	//#APIDOC_EXCLUDE_BEGIN
	/**
	 This method blocks until the agent has finished its start-up phase
	 (i.e. until just before its setup() method is called.
	 When this method returns, the target agent is registered with the
	 AMS and the JADE platform is aware of it.
	 */
	public synchronized void waitUntilStarted() {
		while(myLifeCycle.getState() == AP_INITIATED) {
			try {
				wait();
			}
			catch(InterruptedException ie) {
				// Do nothing...
			}
		}
	}
	//#APIDOC_EXCLUDE_END


	// Notify creator that the start-up phase has completed
	private synchronized void notifyStarted() {
		notifyAll();
	}


	// Notify toolkit of the added behaviour
	// Package scooped as it is called by the Scheduler
	void notifyAddBehaviour(Behaviour b) {
		if (generateBehaviourEvents) {
			myToolkit.handleBehaviourAdded(myAID, b);
		}
	}

	// Notify the toolkit of the removed behaviour
	// Package scooped as it is called by the Scheduler
	void notifyRemoveBehaviour(Behaviour b) {
		if (generateBehaviourEvents) {
			myToolkit.handleBehaviourRemoved(myAID, b);
		}
	}


	//#APIDOC_EXCLUDE_BEGIN

	// Notify the toolkit of the change in behaviour state
	// Public as it is called by the Scheduler and by the Behaviour class 
	public void notifyChangeBehaviourState(Behaviour b, String from, String to) {
		b.setExecutionState(to);
		if (generateBehaviourEvents) {
			myToolkit.handleChangeBehaviourState(myAID, b, from, to);
		}
	}

	public void setGenerateBehaviourEvents(boolean b) {
		generateBehaviourEvents = b;
	}
	//#APIDOC_EXCLUDE_END


	// For persistence service
	private boolean getGenerateBehaviourEvents() {
		return generateBehaviourEvents;
	}


	// Notify toolkit that the current agent has changed its state
	private void notifyChangedAgentState(int oldState, int newState) {
		myToolkit.handleChangedAgentState(myAID, oldState, newState);
	}

	//#MIDP_EXCLUDE_END

	private void activateAllBehaviours() {
		myScheduler.restartAll();
	}

	/**
	 Put a received message into the agent message queue. The message
	 is put at the back end of the queue. This method is called by
	 JADE runtime system when a message arrives, but can also be used
	 by an agent, and is just the same as sending a message to oneself
	 (though slightly faster).
	 @param msg The ACL message to put in the queue.
	 @see jade.core.Agent#send(ACLMessage msg)
	 */
	public final void postMessage(final ACLMessage msg) {
		msg.setPostTimeStamp();
		synchronized (msgQueue) {
			if (msg != null) {
				//#MIDP_EXCLUDE_BEGIN
				myToolkit.handlePosted(myAID, msg);
				//#MIDP_EXCLUDE_END
				msgQueue.addLast(msg);
				doWake();
			}
		}
	}

	//#CUSTOM_EXCLUDE_BEGIN
	private jade.content.ContentManager theContentManager = null;

	/**
	 * Retrieves the agent's content manager 
	 * @return The content manager.
	 */
	public jade.content.ContentManager getContentManager() {
		if (theContentManager == null) {
			theContentManager = new jade.content.ContentManager();
		}
		return theContentManager;
	}

	// All the agent's service helper
	private transient Hashtable helpersTable;

	/**
	 * Retrieves the agent's service helper
	 * @return The service helper.
	 */
	public ServiceHelper getHelper( String serviceName ) throws ServiceException {
		ServiceHelper se = null;
		if (helpersTable == null) {
			helpersTable = new Hashtable();
		}

		se = (ServiceHelper) helpersTable.get(serviceName);
		// is the helper already into the agent's helpersTable ?
		if (se == null) {
			// there isn't, request its creation
			se = myToolkit.getHelper(this, serviceName);
			if (se != null) {
				se.init(this);
				helpersTable.put(serviceName, se);
			}
			else {
				throw new ServiceException("Null helper");
			}
		}
		return se;
	}
	//#CUSTOM_EXCLUDE_END

	/**
	 Retrieve a configuration property set in the <code>Profile</code>
	 of the local container (first) or as a System property.
	 @param key the key that maps to the property that has to be 
	 retrieved.
	 @param aDefault a default value to be returned if there is no mapping
	 for <code>key</code>
	 */
	public String getProperty(String key, String aDefault) {
		String val = myToolkit.getProperty(key, aDefault);
		if (val == null || val.equals(aDefault)) {
			// Try among the System properties
			String sval = System.getProperty(key);
			if (sval != null) {
				val = sval;
			}
		}
		return val;
	}

	//#MIDP_EXCLUDE_BEGIN
	/**
	 * Return the configuration properties exactly as they were passed to the Profile before 
	 * starting the local JADE container.
	 */
	public Properties getBootProperties() {
		return myToolkit.getBootProperties();
	}
	//#MIDP_EXCLUDE_END


	/**
	 This method is used to interrupt the agent's thread.
	 In J2SE/PJAVA it just calls myThread.interrupt(). In MIDP, 
	 where interrupt() is not supported the thread interruption is 
	 simulated as described below.
	 The agent thread can be in one of the following three states:
	 1) Running a behaviour.
	 2) Sleeping on msgQueue due to a doWait()
	 3) Sleeping on myScheduler due to a schedule() with no active behaviours
	 Note that in MIDP the suspended state is not supported
	 The idea is: set the 'isInterrupted' flag, then wake up the
	 thread wherever it may be
	 */
	private void interruptThread() {
		//#MIDP_EXCLUDE_BEGIN
		myThread.interrupt();
		//#MIDP_EXCLUDE_END
		/*#MIDP_INCLUDE_BEGIN
		 synchronized (this) {
		 isInterrupted = true;

		 // case 1: Nothing to do.
		  // case 2: Signal on msgQueue.
		   synchronized (msgQueue) {msgQueue.notifyAll();} 
		   // case 3: Signal on the Scheduler
		    synchronized (myScheduler) {myScheduler.notifyAll();}
		    }
		    #MIDP_INCLUDE_END*/
	} 

	/**
	 Since in MIDP Thread.interrupt() does not exist and a simulated
	 interruption is used to "interrupt" the agent's thread, we must 
	 check whether the simulated interruption happened just before and
	 after going to sleep.
	 */
	void waitOn(Object lock, long millis) throws InterruptedException {
		/*#MIDP_INCLUDE_BEGIN
		 synchronized (this) {
		 if (isInterrupted) {
		 isInterrupted = false;
		 throw new InterruptedException();
		 }
		 } 
		 #MIDP_INCLUDE_END*/
		lock.wait(millis);
		/*#MIDP_INCLUDE_BEGIN
		 synchronized (this) {
		 if (isInterrupted) {
		 isInterrupted = false;
		 throw new InterruptedException();
		 }
		 } 
		 #MIDP_INCLUDE_END*/
	}

	//#J2ME_EXCLUDE_BEGIN
	// For persistence service -- Hibernate needs java.util collections
	private java.util.Set getBehaviours() {
		Behaviour[] behaviours = myScheduler.getBehaviours();
		java.util.Set result = new java.util.HashSet();
		result.addAll(java.util.Arrays.asList(behaviours));

		return result;
	}

	// For persistence service -- Hibernate needs java.util collections
	private void setBehaviours(java.util.Set behaviours) {
		Behaviour[] arr = new Behaviour[behaviours.size()];

		arr = (Behaviour[])behaviours.toArray(arr);

		// Reconnect all the behaviour -> agent pointers
		for(int i = 0; i < arr.length; i++) {
			arr[i].setAgent(this);
		}

		myScheduler.setBehaviours(arr);
	}


	// For persistence service -- Hibernate needs java.util collections
	private transient java.util.Set persistentPendingTimers = new java.util.HashSet();


	// For persistence service -- Hibernate needs java.util collections
	private java.util.Set getPendingTimers() {
		return persistentPendingTimers;
	}

	// For persistence service -- Hibernate needs java.util collections
	private void setPendingTimers(java.util.Set timers) {

		if(!persistentPendingTimers.equals(timers)) {
			// Clear the timers table, and install the new timers.
			pendingTimers.clear();

			java.util.Iterator it = timers.iterator();
			while(it.hasNext()) {
				TBPair pair = (TBPair)it.next();
				pendingTimers.addPair(pair);
			}
		}

		persistentPendingTimers = timers;

	}

	//#J2ME_EXCLUDE_END
}
