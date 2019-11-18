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

//#MIDP_EXCLUDE_FILE

import jade.core.Agent;
import jade.core.NotFoundException;
import jade.util.Logger;

import java.lang.reflect.Method;
import java.util.Vector;
import java.util.Enumeration;

/**
 This class provides support for executing JADE Behaviours 
 in dedicated Java Threads. In order to do that it is sufficient 
 to add to an agent a normal JADE Behaviour "wrapped" into 
 a "threaded behaviour" as returned by the <code>wrap()</code> method
 of this class (see the example below).
 
 <pr><hr><blockquote><pre>
 ThreadedBehaviourFactory tbf = new ThreadedBehaviourFactory();
 Behaviour b = // create a JADE behaviour
 addBehaviour(tbf.wrap(b));
 </pre></blockquote><hr>
 
 This class also provides methods to control the termination of 
 the threads dedicated to the execution of wrapped behaviours
 
 <br>
 <b>NOT available in MIDP</b>
 <br>
 
 @author Giovanni Caire - TILAB
 */
public class ThreadedBehaviourFactory {
	// Thread states (only for debugging purpose)
	private static final String CREATED_STATE = "CREATED";
	private static final String RUNNING_STATE = "RUNNING";
	private static final String CHECKING_STATE = "CHECKING";
	private static final String BLOCKED_STATE = "BLOCKED";
	private static final String SUSPENDED_STATE = "SUSPENDED";
	private static final String TERMINATED_STATE = "TERMINATED";
	private static final String INTERRUPTED_STATE = "INTERRUPTED";
	private static final String ERROR_STATE = "ERROR";
	
	private Vector threadedBehaviours = new Vector();
	
	private Logger myLogger = Logger.getMyLogger(getClass().getName()); 
	
	/**
	 * Wraps a normal JADE Behaviour <code>b</code> into a "threaded behaviour". Adding the 
	 * wrapper behaviour to an agent results in executing <code>b</code> in a dedicated Java Therad.
	 */
	public Behaviour wrap(Behaviour b) {
		return new ThreadedBehaviourWrapper(b);
	}
	
	/**
	 * @return The number of active threads dedicated to the execution of 
	 * wrapped behaviours.
	 */
	public int size() {
		return threadedBehaviours.size();
	}
	
	/**
	 * Interrupt all threaded behaviours managed by this ThreadedBehaviourFactory
	 */
	public void interrupt() {
		ThreadedBehaviourWrapper[] tt = getWrappers();
		for (int i = 0; i < tt.length; ++i) {
			tt[i].interrupt();
		}
	}
	
	/**
	 * Blocks until all threads dedicated to the execution of threaded 
	 * behaviours complete.
	 * @param timeout The maximum timeout to wait for threaded behaviour
	 * termination.
	 * @return <code>true</code> if all threaded behaviour have actually
	 * completed, <code>false</code> otherwise.
	 */
	public synchronized boolean waitUntilEmpty(long timeout) {
		long time = System.currentTimeMillis();
		long deadline = time + timeout;
		try {
			while(!threadedBehaviours.isEmpty()) {
				if (timeout > 0 && time >= deadline) {
					// Timeout expired
					break;
				}
				wait(deadline - time);
				time = System.currentTimeMillis();
			}
		}
		catch (InterruptedException ie) {
			// Interrupted while waiting for threaded behaviour termination
		}
		return threadedBehaviours.isEmpty();  	
	}
	
	/**
	 * Interrupt a threaded behaviour. This method should be used to abort a threaded behaviour 
	 * instead of getThread().interrupt() because i) the latter may have no effect if called just after 
	 * the threaded behaviour suspended itself and ii) the threaded behaviour may be suspended
	 * and in this case its Thread is null.
	 * @return the Thread that was interrupted if any.
	 */
	public Thread interrupt(Behaviour b) throws NotFoundException {
		ThreadedBehaviourWrapper wrapper = getWrapper(b);
		if (wrapper != null) {
			return wrapper.interrupt();
		}
		else {
			throw new NotFoundException(b.getBehaviourName());
		}
	}

	/**
	 * Suspend a threaded behaviour. This method has only effect if called by the threaded behaviour
	 * itself and has the effect of releasing its dedicated Java Thread. This can later be restored 
	 * by means of the <code>resume()</code> method.
	 */
	public void suspend(Behaviour b) {
		ThreadedBehaviourWrapper wrapper = getWrapper(b);
		if (wrapper != null) {
			wrapper.suspend();
		}
	}
	
	/**
	 * Resume a threaded behaviour. Assign a new Java Thread to a threaded behaviour that is
	 * currently suspended.
	 */
	public void resume(Behaviour b) {
		ThreadedBehaviourWrapper wrapper = getWrapper(b);
		if (wrapper != null) {
			wrapper.resume();
		}
	}
	
	/**
	 @return the Thread dedicated to the execution of the Behaviour <code>b</code>
	 */
	public Thread getThread(Behaviour b) {
		ThreadedBehaviourWrapper tb = getWrapper(b);
		if (tb != null) {
			return tb.getThread();
		}
		return null;
	}
	
	//#APIDOC_EXCLUDE_BEGIN
	/**
	 * This method is declared public for debugging purpose only
	 * @return All the wrapper behaviours currently used by this ThreadedBehaviourFactory
	 */
	public ThreadedBehaviourWrapper[] getWrappers() {
		synchronized (threadedBehaviours) {
			ThreadedBehaviourWrapper[] wrappers = new ThreadedBehaviourWrapper[threadedBehaviours.size()];
			for (int i = 0; i < wrappers.length; ++i) {
				wrappers[i] = (ThreadedBehaviourWrapper) threadedBehaviours.elementAt(i);
			}
			return wrappers;
		}
	}
	
	private ThreadedBehaviourWrapper getWrapper(Behaviour b) {
		synchronized (threadedBehaviours) {
			Enumeration e = threadedBehaviours.elements();
			while (e.hasMoreElements()) {
				ThreadedBehaviourWrapper tb = (ThreadedBehaviourWrapper) e.nextElement();
				if (tb.getBehaviour().equals(b)) {
					return tb;
				}
			}
			return null;
		}
	}
	
	/**
	 * Inner class ThreadedBehaviourWrapper
	 * This class is declared public for debugging purpose only
	 */
	public class ThreadedBehaviourWrapper extends Behaviour implements Runnable {
		private Thread myThread;
		private Behaviour myBehaviour;
		private volatile boolean restarted = false;
		private boolean finished = false;
		private volatile boolean suspended = false;
		private int exitValue;
		// Only for debugging purpose
		private volatile String threadState = CREATED_STATE;
		
		private ThreadedBehaviourWrapper(Behaviour b) {
			super(b.myAgent);
			myBehaviour = b;
			myBehaviour.setParent(new DummyParentBehaviour(myAgent, this));
		}
		
		public void onStart() {
			// Be sure both the wrapped behaviour and its dummy parent are linked to the 
			// correct agent
			myBehaviour.setAgent(myAgent);
			myBehaviour.parent.setAgent(myAgent);
			
			start();
		}
		
		private void start() {
			// Start the dedicated thread
			myThread = new Thread(this);
			myThread.setName(myAgent.getLocalName()+"#"+myBehaviour.getBehaviourName());
			myThread.start();			
		}
		
		public void action() {
			if (!finished) {
				block();
			}
		}
		
		public boolean done() {
			return finished;
		}
		
		public int onEnd() {
			// This check only makes sense if the ThreadedBehaviourWrapper is a child
			// of a SerialBehaviour. In this case in fact the ThreadedBehaviourWrapper 
			// terminates, but the parent must remain blocked.
			if (!myBehaviour.isRunnable()) {
				block();
			}
			return exitValue;
		}
		
		/**
		 Propagate the parent to the wrapped behaviour. 
		 NOTE that the <code>parent</code> member variable of the wrapped behaviour
		 must point to the DummyParentBehaviour --> From the wrapped behaviour
		 accessing the actual parent must always be retrieved through the
		 getParent() method.
		 */
		protected void setParent(CompositeBehaviour parent) {
			super.setParent(parent);
			myBehaviour.setWrappedParent(parent);
		}
		
		public void setDataStore(DataStore ds) {
			myBehaviour.setDataStore(ds);
		}
		
		public DataStore getDataStore() {
			return myBehaviour.getDataStore();
		}
		
		public void reset() {
			restarted = false;
			finished = false;
			suspended = false;
			myBehaviour.reset();
			super.reset();
		}
		
		/**
		 Propagate a restart() call (typically this happens when this 
		 ThreadedBehaviourWrapped is directly added to the agent Scheduler
		 and a message is received) to the wrapped threaded behaviour.
		 */
		public void restart() {
			myBehaviour.restart();
		}
		
		/**
		 Propagate a DOWNWARDS event (typically this happens when this
		 ThreadedBehaviourWrapper is added as a child of a CompositeBehaviour
		 and the latter, or an ancestor, is blocked/restarted)
		 to the wrapped threaded behaviour.
		 If the event is a restart, also notify the dedicated thread.
		 */
		protected void handle(RunnableChangedEvent rce) {
			super.handle(rce);
			if (!rce.isUpwards()) {
				myBehaviour.handle(rce);
				if (rce.isRunnable()) {
					go();
				}
			}
		}
		
		private synchronized void go() {
			restarted = true;
			notifyAll();
		}
		
		// Only the dedicated thread can suspend a threaded behaviour
		private synchronized void suspend() {
			if (Thread.currentThread() == myThread) {
				suspended = true;
			}
		}
		
		private synchronized void resume() {
			if (suspended) {
				suspended = false;
				if (myThread == null) {
					start();
				}
			}
		}
		
		public void run() {
			if (threadState == CREATED_STATE) {
				threadedBehaviours.addElement(this);
			}
			else if (threadState == SUSPENDED_STATE) {
				invokeMethod(myBehaviour, "onResumed");
			}
			threadState = RUNNING_STATE;
			try {
				while (true) {
					restarted = false;
					myBehaviour.actionWrapper();
					
					synchronized (this) {
						// If the behaviour was restarted from outside during the action()
						// method, give it another chance
						if (restarted) {
							// We can't just set the runnable state of myBehaviour to true since, if myBehaviour
							// is a CompositeBehaviour, we may end up with myBehaviour runnable, but some of its children not runnable. 
							// However we can't call myBehaviour.restart() here because there could be a deadlock between a thread
							// posting a message and the current thread (monitors are this and the agent scheduler)
							myBehaviour.myEvent.init(true, Behaviour.NOTIFY_DOWN);
							myBehaviour.handle(myBehaviour.myEvent);
						}
						
						if (myBehaviour.done()) {
							break;
						}
						else {
							// If we were interrupted, avoid doing anything else and terminate
							if (Thread.currentThread().isInterrupted() || threadState == INTERRUPTED_STATE) {
								throw new InterruptedException();
							}
							// If the Behaviour suspended itself during the action() method --> Release the embedded Thread
							if (suspended) {
								threadState = SUSPENDED_STATE;
								myThread = null;
								// NOTE: We do not invoke the onSuspended() callback method here, but in the finally block 
								// to avoid giving users the possibility of putting application-specific code inside a section
								// where we already locked the ThreadedBehaviourFactory-Wrapper. This would open the door to
								// deadlock conditions.
								return;
							}
							if (!myBehaviour.isRunnable()) {
								threadState = BLOCKED_STATE;
								wait();
							}
						}
					}
					threadState = RUNNING_STATE;
				}
				exitValue = myBehaviour.onEnd();
				threadState = TERMINATED_STATE;
			}
			catch (InterruptedException ie) {
				threadState = INTERRUPTED_STATE;
				myLogger.log(Logger.WARNING, "Threaded behaviour "+myBehaviour.getBehaviourName()+" interrupted before termination");
			}
			catch (Agent.Interrupted ae) {
				threadState = INTERRUPTED_STATE;
				myLogger.log(Logger.WARNING, "Threaded behaviour "+myBehaviour.getBehaviourName()+" interrupted before termination");
			}
			catch (ThreadDeath td) {
				threadState = INTERRUPTED_STATE;
				myLogger.log(Logger.WARNING, "Threaded behaviour "+myBehaviour.getBehaviourName()+" stopped before termination");
				// ThreadDeath errors should always be propagated so that the top level handler can perform the necessary clean up
				throw td;
			}
			catch (Throwable t) {
				threadState = ERROR_STATE;
				t.printStackTrace();
			}
			finally {
				if (threadState == SUSPENDED_STATE) {
					// If we have just suspended and the Behaviour defined a handleSuspended() method 
					// invoke it from within the terminating thread 
					invokeMethod(myBehaviour, "onSuspended");
				}
				else {
					terminate();
				}
			}
		}
		
		/**
		 * Interrupt a threaded behaviour. This method should be used instead of 
		 * getThread().interrupt() because the latter may have no effect if called just after 
		 * the threaded behaviour suspended itself
		 */
		private synchronized Thread interrupt() {
			if (myThread != null) {
				threadState = INTERRUPTED_STATE;
				myThread.interrupt();
				return myThread;
			}
			else {
				if (threadState == SUSPENDED_STATE) {
					threadState = INTERRUPTED_STATE;
					terminate();
				}
				return null;
			}
		}

		private void terminate() {
			if (Thread.currentThread() == myThread) {
				if (threadState == INTERRUPTED_STATE || threadState == ERROR_STATE) {
					// If the Behaviour defined a handleAborted() method invoke it from within the terminating thread 
					// to give it a chance to clean up any allocated resources
					invokeMethod(myBehaviour, "onAborted");
				}
			}
			finished = true;
			// Restart the wrapper so that it terminates too and is removed from the Agent scheduler
			super.restart();
			threadedBehaviours.removeElement(this);
			synchronized(ThreadedBehaviourFactory.this) {
				ThreadedBehaviourFactory.this.notifyAll();
			}
		}
		
		public final Thread getThread() {
			return myThread;
		}
		
		public final Behaviour getBehaviour() {
			return myBehaviour;
		}
		
		public final String getThreadState() {
			return threadState;
		}
	} // END of inner class ThreadedBehaviourWrapper
	//#APIDOC_EXCLUDE_END
	
	private void invokeMethod(Object obj, String methodName) {
		try {
			Method m = obj.getClass().getMethod(methodName, new Class[0]);
			m.invoke(obj, new Object[0]);
		}
		catch (NoSuchMethodException nsme) {
			// Callback method not defined. Just do nothing 
		}
		catch (Exception e) {
			myLogger.log(Logger.WARNING, "Error invoking callback method "+methodName, e);
		}
	}
	
	/**
	 Inner class DummyParentBehaviour.
	 This class has the only purpose of propagating restart events
	 in the actual wrapped behaviour to the ThreadedBehaviourWrapper.
	 */
	private class DummyParentBehaviour extends CompositeBehaviour {
		private ThreadedBehaviourWrapper myChild;
		
		private DummyParentBehaviour(Agent a, ThreadedBehaviourWrapper b) {
			super(a);
			myChild = b;
		}
		
		public boolean isRunnable() {
			return false;
		}
		
		protected void handle(RunnableChangedEvent rce) {
			// This is always an UPWARDS event from the threaded behaviour, but
			// there is no need to propagate it to the wrapper since it will 
			// immediately block again. It would be just a waste of time.
			if (rce.isRunnable()) {
				myChild.go();
			}
		}
		
		/**
		 * Redefine the root() method so that both the DummyParentBehaviour
		 * and the ThreadedBehaviourWrapper are invisible in the behaviours hierarchy
		 */
		public Behaviour root() {
			Behaviour r = myChild.root();
			if (r == myChild) {
				return myChild.getBehaviour();
			}
			else {
				return r;
			}
		}
		
		protected void scheduleFirst() {
		}
		
		protected void scheduleNext(boolean currentDone, int currentResult) {
		}
		
		protected boolean checkTermination(boolean currentDone, int currentResult) {
			return false;
		}
		
		protected Behaviour getCurrent() {
			return null;
		}
		
		public jade.util.leap.Collection getChildren() {
			return null;
		}
	} // END of inner class DummyParentBehaviour
}
