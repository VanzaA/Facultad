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

import jade.util.leap.List;
import jade.util.leap.LinkedList;

/**
 * Implementation of a list of objects providing methods to 
 * synchronize threads acting on the list in such a way to prevent 
 * concurrent modifications (addition/remotion of elements) and 
 * concurrent scanning/modification. Concurrent scannings are allowed
 * instead.
 * @author Giovanni Caire - TILab 
 */
public class SynchList extends RWLock {
	// The actual list of objects
	private List innerList = null;


    /**
       Default constructor.
    */	
    public SynchList() {
    }

    /**
       This method grants writing privileges to the calling thread and
       grants access to the protected list.
       @return The inner, protected list.
    */
	public synchronized List startModifying() {
		writeLock();
		return innerList;
	}

    /**
       This method must be called when a writer thread has finished
       modifying the list, so that the associated readers-writer lock
       can be relinquished.
    */	
	public synchronized void stopModifying() {
		writeUnlock();
	}
	
    /**
       This method grants reading privileges to the calling thread and
       grants access to the protected list.
       @return The inner, protected list.
    */
	public synchronized List startScanning() {
		if (innerList != null) {
			readLock();
		}
		return innerList;
	}
	
    /**
       This method must be called when a reader thread has finished
       accessing the list, so that the associated readers-writer lock
       can be relinquished.
    */	
	public synchronized void stopScanning() {
		if (innerList != null) {
			readUnlock();
		}
	}

    //#APIDOC_EXCLUDE_BEGIN
	
	protected void onWriteStart() {
		if (innerList == null) {
			innerList = new LinkedList();
		}
	}
	
	protected void onWriteEnd() {
		if (innerList != null && innerList.size() == 0) {
			innerList = null;
		}
	}

    //#APIDOC_EXCLUDE_END

}
