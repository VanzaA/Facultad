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

//#MIDP_EXCLUDE_FILE

import jade.util.leap.Map;
import jade.util.leap.HashMap;


/**
   The table holding information about all agents known to the
   platform (both agents living in the platform and remote agents
   registered with the platform AMS).
   @author Giovanni Caire - TILAB
   @author Giovanni Rimassa - Universita` di Parma
 */
class GADT {
	private Map agents = new HashMap();

	public AgentDescriptor put(AID aid, AgentDescriptor a) {
		Row r;
		synchronized(agents) {
			r = (Row)agents.get(aid);
		}
		if(r == null) {
			agents.put(aid, new Row(a));
			return null;
		}
		else {
			r.lock();

			agents.put(aid, new Row(a));
			AgentDescriptor old = r.get();

			r.unlock();
			return old;
		}
	}

	public AgentDescriptor remove(AID key) {
		Row r;
		synchronized(agents) {
			r = (Row)agents.get(key);
		}
		if (r == null) {
			return null;
		}
		else {
			r.lock();

			agents.remove(key);
			AgentDescriptor a = r.get();
			// Clear the row value, to avoid pending acquire() using the
			// removed agent descriptor...
			r.clear();

			r.unlock();
			return a;
		}
	}

	// The caller must call release() after it has finished with the row
	public AgentDescriptor acquire(AID key) {
		Row r;
		synchronized(agents) {
			r = (Row)agents.get(key);
		}
		if(r == null) {
			return null;
		}
		else {
			r.lock();
			return r.get();
		}
	}

	public void release(AID key) {
		Row r;
		synchronized(agents) {
			r = (Row)agents.get(key);
		}
		if(r != null) {
			r.unlock();
		}
	}

	public AID[] keys() {
		synchronized(agents) {
			Object[] objs = agents.keySet().toArray();
			AID[] result = new AID[objs.length];
			System.arraycopy(objs, 0, result, 0, result.length);
			return result;
		}
	}

	public AgentDescriptor[] values() {
		synchronized(agents) {
			Object[] objs = agents.values().toArray();
			AgentDescriptor[] result = new AgentDescriptor[objs.length];
			for(int i = 0; i < objs.length; i++) {
				Row r = (Row)objs[i];
				result[i] = r.get();
			}
			return result;
		}
	}

	/**
     Inner class Row.
     Rows of the GADT are protected by a recursive mutex lock
	 */
	private static class Row {
		private AgentDescriptor value;
		private Thread owner;
		private long depth;

		public Row(AgentDescriptor a) {
			value = a;
			depth = 0;
		}

		public synchronized AgentDescriptor get() {
			return value;
		}

		public synchronized void clear() {
			value = null;
		}

		public synchronized void lock() {
			try {
				Thread me = Thread.currentThread();
				while((owner != null) && (owner != me)) {
					wait();
				}

				owner = me;
				++depth;
			}
			catch(InterruptedException ie) {
				return;
			}

		}

		public synchronized void unlock() {
			// Must be owner to unlock
			if(owner != Thread.currentThread()) {
				return;
			}
			--depth;
			if(depth == 0 || value == null) {
				// Note that if the row has just been cleared we must wake up 
				// hanging threads even if depth is > 0, otherwise they will 
				// hang forever
				owner = null;
				notifyAll();
			}
		}

	} // End of Row inner class
}
