/**
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A. 
 * Copyright (C) 2001,2002 TILab S.p.A. 
 *
 * GNU Lesser General Public License
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 */

package jade.util;
 
//#APIDOC_EXCLUDE_FILE

/**
 * This class provides support for  
 * synchronizing threads acting on a generic resource in such a way that 
 * - If a thread is writing the resource no other thread can act on it 
 * in any way
 * - Several threads can read the resource at the same time
 * - If one or more threads are reading the resource no thread can write it
 * @author Giovanni Caire - TILab 
 */
public class RWLock {
	// The counter of threads currently reading the resource
	private int readersCnt = 0;
	
	// The Thread currently writing the resource (there can only be
	// one such a therad at a given time)
	private Thread currentWriter = null;
	
	// writeLock()/unlock() can be nested. This indicates the current 
	// depth
	private int writeLockDepth = 0;
	
	private Logger logger = Logger.getMyLogger(this.getClass().getName());

    /**
       Default constructor.
    */
    public RWLock() {
    }

    /**
       Acquire the protected resource with writing privileges. Only
       one writer at a time can access the protected resource, and no
       readers can access it at the same time. The locking is
       recursive (i.e. the same thread can acquire the lock multiple
       times, but must unlock it a matching number of times to
       actually free the protected resource).
    */
	public synchronized void writeLock() {
		Thread me = Thread.currentThread();
		while ((currentWriter != null && currentWriter != me) || readersCnt > 0) {
			// Someone (not me) is writing the resource OR
			// There are one or more Threads reading the resource
			// --> Go to sleep
			try {
				wait();
			}
			catch (InterruptedException ie) {
				if(logger.isLoggable(Logger.WARNING))
					logger.log(Logger.WARNING,"Unexpected interruption. "+ie.getMessage());
			}
		}
		writeLockDepth++;
		if (writeLockDepth == 1) {
			currentWriter = me;
			onWriteStart();
		}
	}
	
    /**
       Release the protected resource, previously acquired with
       writing privileges.
    */
	public synchronized void writeUnlock() {
		if (Thread.currentThread() == currentWriter) {
			writeLockDepth--;
			if (writeLockDepth == 0) {
				// I have finished writing the resource --> Wake up hanging threads
				currentWriter = null;
				notifyAll();
				onWriteEnd();
			}
		}
	}
	
    /**
       Acquire the protected resource with reading privileges. Many
       readers can access the protected resource at the same time, but
       no writer can access it while at least one reader is
       present. The locking is recursive (i.e. the same thread can
       acquire the lock multiple times, but must unlock it a matching
       number of times to actually free the protected resource).
    */
	public synchronized void readLock() {
		while (currentWriter != null) {
			// Someone is writing the resource --> Go to sleep
			try {
				wait();
			}
			catch (InterruptedException ie) {
				if(logger.isLoggable(Logger.WARNING))
					logger.log(Logger.WARNING,"Unexpected interruption. "+ie.getMessage());
			}
		}
		readersCnt++;
	}
	

    /**
       Release the protected resource, previously acquired with
       reading privileges.
    */
	public synchronized void readUnlock() {
		readersCnt--;
		if (readersCnt == 0) {
			// No one is reading the resource anymore --> Wake up threads
			// waiting to write it
			notifyAll();
		}
	}

    /**
       This placeholder method is called every time a thread actually
       acquires the protected resource with writing privileges (this
       means that, in case of multiple recursive locking by the same
       thread, this method is called only the first time). Subclasses
       can exploit this to transparently trigger a resource
       acquisition prolog.
    */	
	protected void onWriteStart() {
	}
	
    /**
       This placeholder method is called every time a thread actually
       releases the protected resource with writing privileges (this
       means that, in case of multiple recursive unlocking by the same
       thread, this method is called only the last time). Subclasses
       can exploit this to transparently trigger a resource release
       epilog.
    */	
	protected void onWriteEnd() {
	}
}
