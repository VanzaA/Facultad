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

package jade.imtp.leap;

import jade.core.IMTPException;
import jade.core.Timer;
import jade.core.TimerDispatcher;
import jade.core.TimerListener;

import java.util.Vector;
import jade.util.Logger;

/**
 * Class declaration
 * @author Giovanni Caire - TILAB
 */
public class MicroStub {
	public static final long MINIMUM_TIMEOUT = 3000; // 3 sec
	
	protected Dispatcher myDispatcher;
	protected Vector pendingCommands = new Vector();
	private boolean flushing = false;
	private Thread flushingThread;
	private Vector dispatchingThreads = new Vector();
	protected Logger logger;
	
	public MicroStub(Dispatcher d) {
		myDispatcher = d;
		logger = Logger.getMyLogger(getClass().getName());
	}
	
	protected Command executeRemotely(Command c, long timeout) throws IMTPException {
		return executeRemotely(c, timeout, -1);
	}
	
	private Command executeRemotely(Command c, long timeout, int sessionId) throws IMTPException {
		long start = System.currentTimeMillis();
		try {
			beginDispatch();
			byte[] cmd = SerializationEngine.serialize(c);
			logger.log(Logger.FINE, "Dispatching command "+c.getCode()+". SF-timeout="+timeout+", old-SID="+sessionId);
			byte[] rsp = myDispatcher.dispatch(cmd, flushing, sessionId);
			if (pendingCommands.size() > 0) {
				logger.log(Logger.FINE, "############# Dispatch succeeded with "+pendingCommands.size()+" pending commands.");
			}
			Command r = SerializationEngine.deserialize(rsp);
			if (r.getCode() == Command.ERROR) {
				if (!((Boolean) r.getParamAt(0)).booleanValue()) {
					// Unexpected exception thrown in the remote site
					String msg = new String("Exception "+(String) r.getParamAt(1)+" occurred in remote site processing command "+c.getCode()+". "+(String) r.getParamAt(2));
					logger.log(Logger.SEVERE,msg);
					throw new IMTPException(msg);
				}
				else if (((String) r.getParamAt(1)).equals("jade.core.IMTPException")) {
					throw new IMTPException((String) r.getParamAt(2));
				}
			}
			return r;
		}
		catch (ICPException icpe) {
			if (timeout == 0 && (icpe instanceof ConnectionDropped)) {
				// If the exception just depends on the fact that the connection was
				// dropped, use a non-null timeout waiting for the connection to be undropped 
				timeout = 30000;
			}
			
			// The destination is unreachable.
			// Postpone the command if store-and-forward is enabled (timeout > 0 or -1 i.e. INFNITE)
			if (timeout == 0) {
				// The command can't be postponed
				throw new IMTPException("Destination unreachable", icpe);
			}
			else {
				if (timeout > 0) {
					// If we must store the command for N sec, but M sec have already been spent 
					// trying to dispatch the command, wait for N-M sec only  
					long elapsedTime = System.currentTimeMillis() - start;
					long remainingTime = timeout - elapsedTime;
					timeout = (remainingTime > MINIMUM_TIMEOUT ? remainingTime : MINIMUM_TIMEOUT);
				}
				int dispatchSessionId = -1;
				if (icpe instanceof ICPDispatchException) {
					dispatchSessionId = ((ICPDispatchException) icpe).getSessionId();
				}
				postpone(c, timeout, dispatchSessionId, icpe);
				logger.log(Logger.WARNING, "Dispatch failed. Command postponed [SF-timeout="+timeout+", SID="+dispatchSessionId+"]. "+icpe.getMessage());
				return null;
			}
		}
		catch (FlushDeadlock fd) {
			throw new IMTPException("Flush deadlock detected. Try again later");
		}
		catch (LEAPSerializationException lse) {
			throw new IMTPException("Serialization error", lse);
		}
		finally {
			endDispatch();
		}
	}
	
	private void postpone(Command c, long timeout, int sessionId, ICPException icpe) {
		if (logger.isLoggable(Logger.FINE)) {
			logger.log(Logger.FINE, Thread.currentThread().toString()+": Command "+c.getCode()+" postponed");
		}
		final PostponedCommand pc = new PostponedCommand(c, sessionId, icpe);
		pendingCommands.addElement(pc);
		if (timeout > 0) {
			logger.log(Logger.INFO, Thread.currentThread().toString()+": Activating Timer for Command "+c.getCode());
			pc.timer = TimerDispatcher.getTimerDispatcher().add(new Timer(System.currentTimeMillis()+timeout, new TimerListener() {
				public void doTimeOut(Timer t) {
					// Timeout expired --> Remove the related postponed command and
					// let subclasses react properly 
					if (t == pc.timer) {
						logger.log(Logger.INFO, Thread.currentThread().toString()+": Timer for Command "+pc.command.getCode()+" expired!!!");
						manageTimerExpired(pc);
					}
				}
			}));
		}
		
		int size = pendingCommands.size();
		if (size > 100 && size < 110) {
			logger.log(Logger.WARNING,size+" postponed commands");
		}
	}
	
	
	public boolean flush() {
		Thread t = checkFlush();
		if (t != null) {
			t.start();
			return true;
		}
		else {
			return false;
		}
	}
	
	public Thread checkFlush() {
		// 1) Lock the buffer of pending commands to ensure mutual exclusion with executeRemotely() (see comment in beginDispatch())
		if (beginFlush()) {
			// This is called by the main thread of the underlying EndPoint
			// --> The actual flushing must be done asynchronously to avoid deadlock
			flushingThread = new Thread() {
				public void run() {
					// 2) Flush the buffer of pending commands
					logger.log(Logger.INFO, "Start flushing");					
					int flushedCnt = 0;
					PostponedCommand pc = null;
					while ((pc = removeFirst()) != null) {
						// Exceptions and return values of commands whose delivery
						// was delayed for disconnection problems can and must not
						// be handled!!!
						try {
							if (logger.isLoggable(Logger.FINE)) {
								logger.log(Logger.FINE,"Flushing command: code = "+pc.command.getCode());
							}
							Command r = executeRemotely(pc.command, 0, pc.sessionId);
							// Command delivered. Remove the Timer associated to it if any 
							if (pc.timer != null) {
								TimerDispatcher.getTimerDispatcher().remove(pc.timer);
							}
							flushedCnt++;
							if (r.getCode() == Command.ERROR) {
								logger.log(Logger.SEVERE,"Remote exception in command asynchronous delivery. "+r.getParamAt(2));
							}
						}
						catch (Exception ex) {
							logger.log(Logger.WARNING,"Exception in command asynchronous delivery. "+ex);
							// We are disconnected again --> put the command back in the queue of postponed commands
							// and stop flushing. If there was a Timer it is still there --> No need to do anything
							if (ex instanceof ICPDispatchException) {
								pc.sessionId = ((ICPDispatchException) ex).getSessionId();
							}
							pendingCommands.insertElementAt(pc, 0);
							break;
						}
					}
					
					// 3) Unlock the buffer of pending commands
					logger.log(Logger.FINE, "########## "+pendingCommands.size()+" pending commands after flush");
					endFlush();
					logger.log(Logger.INFO,"Flushing thread terminated ("+flushedCnt+")");
				}
			};
			return flushingThread;
		}
		else {
			// Flushing will not start --> no need to call endFlush()
			return null;
		}
	}
	
	public boolean isEmpty() {
		return ((pendingCommands.size() == 0) && (!flushing));
	}
	
	protected void handlePostponedCommandExpired(Command c, ICPException exception) {
		// Do nothing by default. Subclasses may redefine this to react properly
	}
	
	/**
	 Note that normal command-dispatching and postponed command
	 flushing can't occur at the same time, but different commands
	 can be dispatched in parallel. This is the reason for this
	 lock/unlock mechanism instead of a simple synchronization.
	 */
	private void beginDispatch() {
		// If this is the flushingThread (of course flush is in progress) do not block it 
		if (Thread.currentThread() != flushingThread) {
			synchronized (pendingCommands) {
				while (flushing) {
					try {
						pendingCommands.wait();
					}
					catch (InterruptedException ie) {
					}
				}
				dispatchingThreads.addElement(Thread.currentThread());
			}
		}
	}
	
	private void endDispatch() {
		if (Thread.currentThread() != flushingThread) {
			synchronized (pendingCommands) {
				dispatchingThreads.removeElement(Thread.currentThread());
				if (dispatchingThreads.isEmpty()) {
					pendingCommands.notifyAll();
				}
			}
		}
	}	
	
	private boolean beginFlush() {
		synchronized (pendingCommands) {
			if (dispatchingThreads.contains(Thread.currentThread())) {
				// If this is a dispatching Thread we will enter a deadlock. 
				// Throw a suitable exception to avoid that.
				throw new FlushDeadlock();
			}
			while (dispatchingThreads.size() > 0) {
				try {
					pendingCommands.wait();
				}
				catch (InterruptedException ie) {
				}
			}
			if (pendingCommands.isEmpty()) {
				// Nothing to flush. 
				return false;
			}
			else {
				flushing = true;
				return true;
			}
		}
	}
	
	public void endFlush() {
		synchronized (pendingCommands) {
			flushing = false;
			pendingCommands.notifyAll();
		}
	}
	
	private PostponedCommand removeFirst() {
		synchronized (pendingCommands) {
			PostponedCommand pc = null;
			if (pendingCommands.size() > 0) {
				pc = (PostponedCommand) pendingCommands.elementAt(0);
				pendingCommands.removeElementAt(0);
			}
			return pc;
		}
	}
	
	private void manageTimerExpired(final PostponedCommand pc) {
		// This is invoked by the TimerDispatcher Thread. Since the operation may be 
		// long, do it in a dedicated Thread
		Thread t = new Thread() {
			public void run() {
				// Removing expired pending commands cannot be done while flushing to 
				// avoid cases like:
				// - Flushing starts and postponed command dispatching begins
				// - Timer expires and FAILURE is sent back
				// - Delivering succeeds
				beginDispatch();
				boolean found = pendingCommands.removeElement(pc);
				endDispatch();
				// The command may have been processed while we were waiting to disable flush.
				// Do nothing in this case
				if (found) {
					handlePostponedCommandExpired(pc.command, pc.icpe);
				}
			}
		};
		t.start();
	}
	
	protected class PostponedCommand {
		private Command command;
		private int sessionId;
		private ICPException icpe;
		private Timer timer;
		
		public PostponedCommand(Command c, int sessionId, ICPException icpe) {
			this.command = c;
			this.sessionId = sessionId;
			this.icpe = icpe;
		}
		
		public Command getCommand() {
			return command;
		}
		
		public ICPException getException() {
			return icpe;
		}
	}
	
	private class FlushDeadlock extends RuntimeException {
		public FlushDeadlock() {
			super();
		}
	}
}

