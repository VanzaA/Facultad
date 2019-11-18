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

package jade.core.nodeMonitoring;

// Take care that the DOTNET build file (dotnet.xml) uses this file (it is copied just after the preprocessor excluded it)
//#J2ME_EXCLUDE_FILE
//#APIDOC_EXCLUDE_FILE

import jade.core.Node;
import jade.core.NodeFailureMonitor;
import jade.core.NodeEventListener;

import jade.util.Logger;
import jade.util.leap.Collection;


/**
 * The <code>UDPNodeFailureMonitor</code> class detects node failures and
 * notifies its registered listener using the UDP protocol.
 * 
 * @author Roland Mungenast - Profactor
 * @since JADE 3.3
 * @see jade.core.NodeFailureMonitor
 */
class UDPNodeFailureMonitor extends NodeFailureMonitor {

	/**
	 * The target is connected. That means that it is sending
	 * ping messages regulary.
	 */
	public static final int STATE_CONNECTED = 0;

	/**
	 * The target is unreachable. The target doesn't
	 * send ping messages any more.
	 */
	public static final int STATE_UNREACHABLE = 1;

	/**
	 * The final state. The target isn't
	 * monitored anymore.
	 */
	public static final int STATE_FINAL = 2;


	private long deadlineId = -1;
	private long lastPing = -1;
	private int state = -1;
	private long key = 0;
	private UDPMonitorServer server;
	private UDPNodeMonitoringService service;
	private Logger logger = Logger.getMyLogger(this.getClass().getName());


	/**
	 * Constructor
	 */
	UDPNodeFailureMonitor(UDPMonitorServer s, UDPNodeMonitoringService svc) {
		server = s;
		service = svc;
	}

	/**
	 * Start monitoring a given node
	 * @param n target node to monitor
	 * @param nel listener to inform about new events
	 */
	public void start(Node n, NodeEventListener nel) {
		super.start(n, nel);
		setState(STATE_CONNECTED);
		// Request the node to be monitored to start sending UDP packets
		key = System.currentTimeMillis();
		service.activateUDP(target, key);
		server.register(this);
	}

	public void stop() {
		server.deregister(this);
		// Request the node to be monitored to stop sending UDP packets
		service.deactivateUDP(target, key);
	}

	/**
	 * Returns all child nodes of the targeted node
	 * @return a <code>Collection</code> of <code>Node</code> instances
	 */
	public Collection getChildNodes() {
		return childNodes.values();
	}

	/**
	 * Returns the time when the last ping message has been received 
	 * from the targeted node
	 * 
	 * @return the difference, measured in milliseconds, 
	 * between the current time and midnight, January 1, 1970 UTC.
	 */
	public long getLastPing() {
		return lastPing;
	}

	public long getDeadlineID() {
		return deadlineId;
	}

	void setDeadlineID(long time) {
		deadlineId = time;
	}

	/**
	 * Returns the current state.
	 */
	public int getState() {
		return state;
	}

	/**
	 * Sets the current state.
	 */
	void setState(int newState) {

		if (logger.isLoggable(Logger.FINEST)) {
			logger.log(Logger.FINEST, "Transition to state " + newState + 
					" for node '" + target.getName() + "'");
		}

		// --> CONNECTED
		if (state == -1 && newState == STATE_CONNECTED) {
			fireNodeAdded();

			// CONNECTED --> UNREACHABLE
		} else if (state == STATE_CONNECTED && newState == STATE_UNREACHABLE) {
			fireNodeUnreachable();

			// UNREACHABLE --> CONNECTED
		} else if (state == STATE_UNREACHABLE && newState == STATE_CONNECTED) {
			fireNodeReachable();

			// REMOVED
		} else if (newState == STATE_FINAL) {
			fireNodeRemoved();
			server.deregister(this);
		}

		state = newState;
	}

	/**
	 * Sets the time when the last ping message has been received 
	 * from the targeted node
	 * 
	 * @param time the difference, measured in milliseconds, 
	 * between the current time and midnight, January 1, 1970 UTC.
	 */
	void setLastPing(long time) {
		lastPing = time;
	}
	
	public String requireService() {
		return UDPNodeMonitoringService.NAME;
	}
}
