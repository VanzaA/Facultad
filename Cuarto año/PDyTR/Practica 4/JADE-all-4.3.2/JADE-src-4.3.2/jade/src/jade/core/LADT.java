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

import jade.util.leap.Map;
import jade.util.leap.HashMap;


// Class for the Local Agent Descriptor Table.

/**
   @author Giovanni Rimassa - Universita' di Parma
   @version $Date: 2008-01-07 15:49:34 +0100 (lun, 07 gen 2008) $ $Revision: 6013 $
 */
class LADT {

    // Rows of the LADT are protected by a recursive mutex lock
    private static class Row {
        private Agent value;
        // DEBUG private Agent bakValue;
        // DEBUG private String target = "ma";
        private Thread owner;
        private long depth;

        public Row(Agent a) {
            value = a;
            // DEBUG bakValue = value;
            depth = 0;
        }

        public synchronized Agent get() {
            return value;
        }

        public synchronized void clear() {
            value = null;
        }

        public synchronized void lock() {
        	if (value != null) {
            try {
                Thread me = Thread.currentThread();
                while((owner != null) && (owner != me)) {
                		// DEBUG boolean b = false;
              			// DEBUG if (bakValue.getLocalName().equals(target)) {
	                	// DEBUG 	System.out.println("Thread "+Thread.currentThread().getName()+" start waiting on "+target);
	                	// DEBUG 	b = true;
	                	// DEBUG }
                    wait();
                    // DEBUG if (b) {
                		// DEBUG 	System.out.println("Thread "+Thread.currentThread().getName()+" stop waiting on "+target);
                    // DEBUG }
                    if (value == null) {
                    	return;
                    }
                }

                owner = me;
                ++depth;
            }
            catch(InterruptedException ie) {
                return;
            }
        	}
        }

        public synchronized void unlock() {
            // Must be owner to unlock
            if(owner != Thread.currentThread())
                return;
            --depth;
            if(depth == 0 || value == null) {
                // Note that if the row has just been cleared we must wake up 
                // hanging threads even if depth is > 0, otherwise they will 
            		// hang forever
                owner = null;
                // DEBUG try {
	          		// DEBUG 	if (bakValue.getLocalName().equals(target)) {
	              // DEBUG 		System.out.println("Thread "+Thread.currentThread().getName()+" notifying all on "+target);
	              // DEBUG 	}
                // DEBUG }
                // DEBUG catch (Exception e) {}
                notifyAll();
            }
        }
        
        // For debugging purpose
        public String toString() {
        	if (value != null) {
				return "("+value.getName()+" :owner "+(owner != null ? owner.toString() : "null")+")";
        	}
        	else {
        		return "null";
        	}
        }

    } // End of Row class


    // Initial size of agent hash table
    //private static final int MAP_SIZE = 50;

    // Load factor of agent hash table
    //private static final float MAP_LOAD_FACTOR = 0.50f;


    //private Map agents = new HashMap(MAP_SIZE, MAP_LOAD_FACTOR);
    private Map agents;
    
    public LADT(int size) {
    	agents = new HashMap(size);
    }
    
    public Agent put(AID aid, Agent a) {
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
            
            Agent old = r.get();
          	// Re-putting an existing agent must have no effect
            if (a != old) {
	            agents.put(aid, new Row(a));
            }
            
            r.unlock();
            return old;
        }
    }

    public Agent remove(AID key) {
        Row r;
        synchronized(agents) {
            r = (Row)agents.get(key);
        }
        if(r == null)
            return null;
        else {
            r.lock();

            agents.remove(key);
            Agent a = r.get();
            // Clear the row value, to avoid pending acquire() using the
            // removed agent...
            r.clear();

            r.unlock();
            return a;
        }
    }

    // The caller must call release() after it has finished with the row
    public Agent acquire(AID key) {
        Row r;
        synchronized(agents) {
            r = (Row)agents.get(key);
        }
        if(r == null)
            return null;
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
            
            // Patch on 11/12/02 by NL to bypass J2ME bug with System.arraycopy
            for (int i=0;i<result.length;i++) {
                result[i]=(AID)objs[i];
            }            
            return result;
        }
    }
    
    public Agent[] values() {
        synchronized(agents) {
            Object[] objs = agents.values().toArray();
            Agent[] result = new Agent[objs.length];
            for(int i = 0; i < objs.length; i++) {
                Row r = (Row)objs[i];
                result[i] = r.get();
            }
            return result;
        }

    }

    synchronized boolean contains(AID key) {
    	return agents.containsKey(key);
    }
    
    // For debugging purpose
    public String[] getStatus() {
        synchronized(agents) {
            Object[] objs = agents.values().toArray();
            String[] status = new String[objs.length];
            for(int i = 0; i < objs.length; i++) {
                Row r = (Row)objs[i];
                status[i] = r.toString();
            }
            return status;
        }
    }
}
