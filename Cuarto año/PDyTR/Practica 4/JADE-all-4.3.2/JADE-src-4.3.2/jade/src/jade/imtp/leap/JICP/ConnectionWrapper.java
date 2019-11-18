/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/**
 * ***************************************************************
 * The LEAP libraries, when combined with certain JADE platform components,
 * provide a run-time environment for enabling FIPA agents to execute on
 * lightweight devices running Java. LEAP and JADE teams have jointly
 * designed the API for ease of integration and hence to take advantage
 * of these dual developments and extensions so that users only see
 * one development platform and a
 * single homogeneous set of APIs. Enabling deployment to a wide range of
 * devices whilst still having access to the full development
 * environment and functionalities that JADE provides.
 * Copyright (C) 2001 Telecom Italia LAB S.p.A.
 * Copyright (C) 2001 Motorola.
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
 * **************************************************************
 */

package jade.imtp.leap.JICP;

//#MIDP_EXCLUDE_FILE

import jade.mtp.TransportAddress;
import java.io.*;
import jade.util.leap.*;

/**
 * @author Giovanni Caire - TILAB
 */
class ConnectionWrapper {
	private Connection myConnection;
	private TransportAddress destAddr;
	private boolean oneShot;
	private boolean locked;
	private boolean closed;
	private boolean reused;
	private long lastUsage;
	private long nUsages;
	
	ConnectionWrapper(Connection c, TransportAddress ta) {
		myConnection = c;
		destAddr = ta;
		oneShot = false;
		reused = false;
		locked = true;
		closed = false;
		nUsages = 0;
	}
	
	final Connection getConnection() {
		return myConnection;
	}
	
	final TransportAddress getDestAddress() {
		return destAddr;
	}
	
	final void setOneShot() {
		oneShot = true;
	}
	
	final boolean isOneShot() {
		return oneShot;
	}
	
	final void setReused() {
		reused = true;
	}
	
	final boolean isReused() {
		return reused;
	}
	
	synchronized boolean lock() {
		if (locked) {
			return false;
		}
		else {
			locked = true;
			return true;
		}
	}
	
	synchronized void unlock() {
		locked = false;
		nUsages++;
		lastUsage = System.currentTimeMillis();
		if (closed) {
			close();
		}
	}
	
	synchronized void close() {
		if (!locked) {
			try {
				myConnection.close();
			}
			catch (Exception e) {
			}
		}
		else {
			// Delay the closure to when the connection will be unlocked
			closed = true;
		}
	}
	
	synchronized boolean isExpired(long currentTime) {
		// A locked connection must not be checked for expiration
		if (!locked) {
			if ((currentTime - lastUsage) > 120000) {
				// If the connection is expired also lock it so that it cannot be assigned just before its removal
				locked = true;
				return true;
			}
		}
		return false;
	}
	
	public String toString() {
		return "[CW: nUsages="+nUsages+", lastUsage="+lastUsage+"]";
	}
}

