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

//#J2ME_EXCLUDE_FILE

class FullResourceManager implements ResourceManager {

	public static final String DISABLE_THREAD_GROUP_INTERRUPT = "jade_core_FullResourceManager_disablethreadgroupinterrupt";
	private static final boolean DEFAULT_DISABLE_THREAD_GROUP_INTERRUPT = false;
	public static final String THREAD_GROUP_INTERRUPT_TIMEOUT = "jade_core_FullResourceManager_threadgroupinterrupttimeout";
	private static final String DEFAULT_THREAD_GROUP_INTERRUPT_TIMEOUT = "5000";
	
	private static final String USER_AGENTS_GROUP_NAME = "JADE User Agents";
	private static final String SYSTEM_AGENTS_GROUP_NAME = "JADE System Agents";
	private static final String CRITICAL_THREADS_GROUP_NAME = "JADE Time-critical Threads";

	private ThreadGroup parent;
	private ThreadGroup agentThreads;
	private ThreadGroup systemAgentThreads;
	private ThreadGroup criticalThreads;
	
	private boolean terminating = false;

	private Profile myProfile;
	private boolean disableThreadGroupInterrupt;
	private int threadGroupInterruptTimeout;

	
	public FullResourceManager() {
		parent = new ThreadGroup("JADE") {
			public void uncaughtException(Thread t, Throwable e) {
				if (!terminating) {
					super.uncaughtException(t, e);
				}
			}
		};
		agentThreads = new ThreadGroup(parent, USER_AGENTS_GROUP_NAME);
		agentThreads.setMaxPriority(Thread.NORM_PRIORITY);

		systemAgentThreads = new ThreadGroup(parent, SYSTEM_AGENTS_GROUP_NAME);
		systemAgentThreads.setMaxPriority(Thread.NORM_PRIORITY+1);

		criticalThreads = new ThreadGroup(parent, CRITICAL_THREADS_GROUP_NAME);
		criticalThreads.setMaxPriority(Thread.MAX_PRIORITY);
	}

	public Thread getThread(int type, String name, Runnable r) {
		Thread t = null;
		switch (type) {
		case USER_AGENTS:
			t = new Thread(agentThreads, r);
			t.setPriority(agentThreads.getMaxPriority());
			break;
		case SYSTEM_AGENTS:
			t = new Thread(systemAgentThreads, r);
			t.setPriority(systemAgentThreads.getMaxPriority());
			break;
		case TIME_CRITICAL:
			t = new Thread(criticalThreads, r);
			t.setPriority(criticalThreads.getMaxPriority());
			break;
		}
		if (t != null) {
			t.setName(name);
		}

		return t;
	}

	public void releaseResources() {
		terminating = true;
		
		if (!disableThreadGroupInterrupt) {
			Thread t = new Thread() {
				public void run() {
					try {
						Thread.sleep(threadGroupInterruptTimeout);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					if (parent != null) {
						parent.interrupt();
					}
	
					agentThreads = null;
					systemAgentThreads = null;
					criticalThreads = null;
					parent = null;
				}
			};
			t.start();
		}
	}

	public void initialize(Profile p) {
		myProfile = p;
		
		disableThreadGroupInterrupt = myProfile.getBooleanProperty(DISABLE_THREAD_GROUP_INTERRUPT, DEFAULT_DISABLE_THREAD_GROUP_INTERRUPT);
		
		String tmp = myProfile.getParameter(THREAD_GROUP_INTERRUPT_TIMEOUT, DEFAULT_THREAD_GROUP_INTERRUPT_TIMEOUT);
		threadGroupInterruptTimeout = Integer.parseInt(tmp);
		
		if (!myProfile.getBooleanProperty(Profile.NO_DISPLAY, false)) {
			// Start the AWT-Toolkit outside the JADE Thread Group to avoid annoying InterruptedException-s on termination
			// when some agent with a Swing or AWT based GUI is used
			try {
				Class.forName("java.awt.Frame").newInstance();
			}
			catch (Throwable t) {
				// Ignore failure (e.g. in case we don't have the display)
			}
		}		
	}
}





