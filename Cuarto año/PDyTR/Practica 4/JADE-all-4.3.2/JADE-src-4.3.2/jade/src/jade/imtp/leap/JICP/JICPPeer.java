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
 * Copyright (C) 2001 Broadcom Eireann Research.
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

import jade.core.Profile;
import jade.mtp.TransportAddress;
import jade.imtp.leap.*;
import java.io.*;
import java.net.*;

/**
 * Class declaration
 * @author Giovanni Caire - TILAB
 * @author Jamie Lawrence - Broadcom Eireann Research
 * @author Ronnie Taib - Motorola
 * @author Nicolas Lhuillier - Motorola
 */
public class JICPPeer implements ICP, ProtocolManager {
	private static final int POOL_SIZE = 50;

	private JICPClient   client;
	private JICPServer   server;
	private Ticker       ticker;

	private String myID;

	private int connectionTimeout = 0;

	public static final String CONNECTION_TIMEOUT = "jade_imtp_leap_JICP_JICPPeer_connectiontimeout";
	/**
	 * Start listening for internal platform messages on the specified port
	 */
	public TransportAddress activate(ICP.Listener l, String peerID, Profile p) throws ICPException {
		myID = peerID;

		connectionTimeout = Integer.parseInt(p.getParameter(CONNECTION_TIMEOUT, "0"));

		// Start the client
		client = new JICPClient(getProtocol(), getConnectionFactory(), POOL_SIZE, connectionTimeout);

		// Start the server listening for connections
		server = new JICPServer(p, this, l, getConnectionFactory(), POOL_SIZE);
		server.start();

		// Start the Ticker
		ticker = new Ticker(60000);
		ticker.start();

		// Creates the local transport address
		TransportAddress localTA = getProtocol().buildAddress(server.getLocalHost(), String.valueOf(server.getLocalPort()), null, null);

		return localTA;
	} 

	/**
	 * stop listening for internal platform messages
	 */
	public void deactivate() throws ICPException {
		if (server != null) {
			client.shutdown();
			server.shutdown();
			ticker.shutdown();
		} 
		else {
			throw new ICPException("No external listener was activated.");
		} 
	} 

	/**
	 * deliver a serialized command to a given transport address
	 */
	public byte[] deliverCommand(TransportAddress ta, byte[] payload, boolean requireFreshConnection) throws ICPException {
		byte[] respPayload = client.send(ta, JICPProtocol.COMMAND_TYPE, payload, requireFreshConnection);

		return (respPayload);
	} 

	final String getID() {
		return myID;
	}

	/**
	 * Pings the specified transport address in order to obtain
	 * the local hostname or IP address.
	 * @param pingURL The <code>URL</code> to ping (usually the
	 * main container).
	 * @return The local IP address of the local container as a
	 * <code>String</code>.
	 * 
	 * @throws ICPException
	 */
	String getAddress(String pingURL) throws ICPException {
		byte[] respPayload = null;

		try {
			TransportAddress pingAddr = getProtocol().stringToAddr(pingURL);

			respPayload = client.send(pingAddr, JICPProtocol.GET_ADDRESS_TYPE, new byte[0], false);
		} 
		catch (ICPException icpe) {
			throw new ICPException("JICP GET_ADDRESS error. Cannot retrieve local hostname: "
					+icpe.getMessage());
		} 

		return (new String(respPayload));
	} 

	/**
     Subclasses may re-define this method to return their own
     protocol
	 */
	public TransportProtocol getProtocol() {
		return JICPProtocol.getInstance();
	} 

	/**
     Subclasses may re-define this method to return their own
     ConnectionFactory
	 */
	public ConnectionFactory getConnectionFactory() {
		return new ConnectionFactory() {
			public Connection createConnection(Socket s) {
				return new JICPConnection(s);
			}
			public Connection createConnection(TransportAddress ta) throws IOException {
				JICPConnection con = new JICPConnection(ta, connectionTimeout);
				return con;
			}
		};
	}  

	protected ServerSocket getServerSocket(String host, int port, boolean changePortIfBusy) throws ICPException {
		try {
			return new ServerSocket(port, 50, (host != null ? InetAddress.getByName(host) : null));
		} 
		catch (SocketException be) {
			// HACK! We should do this only in case of a BindException. However some implementations 
			// of the JVM (particularly that for Windows 7 64 bits) seem to have a bug and throw a 
			// generic SocketException also in the case that the port is busy.
			if (changePortIfBusy) {
				// The specified port is busy. Let the system find a free one
				try {
					return new ServerSocket(0, 50, (host != null ? InetAddress.getByName(host) : null));
				} catch (IOException ioe) {
					throw new ICPException("Cannot create server socket on a free port. ", ioe);
				}
			} else {
				throw new ICPException("Cannot bind server socket to "+(host != null ? "host "+host : "localhost")+ " port " + port);
			}
		} catch (IOException ioe2) {
			throw new ICPException("Cannot create server socket. ", ioe2);
		}
	}

	/**
     Inner class Ticker
	 */
	private class Ticker extends Thread {
		private long period;
		private boolean active = false;

		private Ticker(long period) {
			super();
			this.period = period;
		}

		public void start() {
			active = true;
			super.start();
		}

		public void run() {
			while (active) {
				try {
					Thread.sleep(period);
					long currentTime = System.currentTimeMillis();
					client.tick(currentTime);
					server.tick(currentTime);
				}
				catch (InterruptedException ie) {
				}
			}
		}

		public void shutdown() {
			active = false;
			interrupt();
		}
	} // END of inner class Ticker
}

