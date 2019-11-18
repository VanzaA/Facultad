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

/**
   @author Giovanni Caire - TILAB
 */
class LightResourceManager implements ResourceManager {
	private static final int USER_AGENTS_PRIORITY = Thread.NORM_PRIORITY;
	private static final int SYSTEM_AGENTS_PRIORITY = Thread.NORM_PRIORITY;
	private static final int TIME_CRITICAL_PRIORITY = Thread.MAX_PRIORITY;

	public LightResourceManager() {
	}

	public Thread getThread(int type, String name, Runnable r) {
		Thread t = new Thread(r);
		switch (type) {
		case USER_AGENTS:
			t.setPriority(USER_AGENTS_PRIORITY);
			break;
		case SYSTEM_AGENTS:
			t.setPriority(SYSTEM_AGENTS_PRIORITY);
			break;
		case TIME_CRITICAL:
			t.setPriority(TIME_CRITICAL_PRIORITY);
			break;
		}

		return t;
	}

	public void releaseResources() {
		// Do nothing
	}

	public void initialize(Profile p) {
		// Do nothing
	}
}





