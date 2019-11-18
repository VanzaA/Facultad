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

//#APIDOC_EXCLUDE_FILE


import jade.util.leap.Comparable;

/**
This class is used by the JADE internal timing system to schedule 
actions after a given amount of time. It should not
be used by application developers. 
@author Giovanni Rimassa - Universita' di Parma
@version $Date: 2007-03-05 11:02:22 +0100 (lun, 05 mar 2007) $ $Revision: 5938 $
 */
public class Timer {

	private long expireTimeMillis;
	private boolean fired;
	private TimerListener owner;

	public Timer(long when, TimerListener tl) {
		expireTimeMillis = when;
		owner = tl;
		fired = false;
	}

	public boolean equals(Object o) {
		Timer t = (Timer)o;
		return (expireTimeMillis == t.expireTimeMillis);
	}


	// Called by the TimerDispatcher

	boolean isExpired() {
		return expireTimeMillis < System.currentTimeMillis();
	}

	void fire() {
		if (!fired) {
			fired = true;
			owner.doTimeOut(this);
		}
	}

	final long expirationTime() {
		return expireTimeMillis;
	}

	void setExpirationTime(long t) {
		expireTimeMillis = t;
	}

}
