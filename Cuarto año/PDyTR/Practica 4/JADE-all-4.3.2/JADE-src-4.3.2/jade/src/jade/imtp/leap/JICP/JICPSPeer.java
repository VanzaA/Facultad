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

//#J2ME_EXCLUDE_FILE

import jade.core.Profile;
import jade.mtp.TransportAddress;
import jade.imtp.leap.*;
import jade.imtp.leap.SSLHelper;
import jade.util.Logger;
import java.io.*;
import java.net.*;
import javax.net.ssl.*;



/**
 * JICPSPeer  -  JICP Secure Peer
 * 
 * This JICP peer uses secure connections 
 * with or without mutual authentication 
 * of the endpoints by using digital certificates.
 * It leverages SSL/TLS.
 *
 * @author Giovanni Caire - TILAB
 * @author Giosue Vitaglione - TILAB
 * @author Jamie Lawrence - Broadcom Eireann Research
 * @author Ronnie Taib - Motorola
 * @author Nicolas Lhuillier - Motorola
 */
public class JICPSPeer extends JICPPeer {


	protected static Logger myLogger = Logger.getMyLogger( JICPSPeer.class.getName() );
	private SSLContext ctx = null;

	public TransportAddress activate(ICP.Listener l, String peerID, Profile p) throws ICPException {
		if (myLogger.isLoggable(Logger.FINE)) {
			myLogger.log(Logger.FINE, 
					"About to activate JICP peer." );
		}
		ctx = SSLHelper.createContext(); // create context at activation time
                setUseSSLAuth(SSLHelper.needAuth());
		if (myLogger.isLoggable(Logger.FINE)) {
			myLogger.log(Logger.FINE, 
					"activate() context created ctx="+ctx );
		}
		TransportAddress ta = super.activate(l, peerID, p);
		if (myLogger.isLoggable(Logger.INFO)) {
			myLogger.log(Logger.INFO, 
					"JICP Secure Peer activated. (auth="+getUseSSLAuth()+", ta="+ta+")");
		}
		return ta;
	}

	/**
     Subclasses may re-define this method to return their own
     ConnectionFactory
	 */
	public ConnectionFactory getConnectionFactory() {
		return new ConnectionFactory() {
			public Connection createConnection(Socket s) {
				return new JICPSConnection(s);
			}
			public Connection createConnection(TransportAddress ta) throws IOException {
				return new JICPSConnection(ta, getUseSSLAuth());
			}
		};
	}

	protected ServerSocket getServerSocket(String host, int port, boolean changePortIfBusy) throws ICPException {
		// socket to be created
		ServerSocket sss = null;

		if ( getUseSSLAuth() ) {
			sss=getServerSocketWithAuth(host, port, changePortIfBusy);
			if (myLogger.isLoggable(Logger.FINE)) {
				myLogger.log(Logger.FINE, 
				"Creating JICPSPeer with mutual Authentication." );
			}
		} else {
			sss=getServerSocketNoAuth(host, port, changePortIfBusy);
			if (myLogger.isLoggable(Logger.WARNING)) {
				myLogger.log(Logger.WARNING, 
				"Creating JICPSPeer with NO-AUTHENTICATION (only confidentiality)." );
			}

		}
		return sss;
	}

	private ServerSocket getServerSocketWithAuth(String host, int port, boolean changePortIfBusy) throws ICPException {  
		// Create the SSLContext if necessary
		if (ctx == null) {
			ctx = SSLHelper.createContextWithAuth();
		}

		// socket to be created
		SSLServerSocket sss = null;

		try {
			// create factory
			SSLServerSocketFactory serverSocketFactory = ctx.getServerSocketFactory();
			try {
				// create server socket
				sss =(SSLServerSocket)serverSocketFactory.createServerSocket(port);
			} catch (IOException ioe) {
				if (changePortIfBusy) {
					// The specified port is busy. Let the system find a free one
					try {
						sss = (SSLServerSocket) serverSocketFactory.createServerSocket(0);
					} catch (IOException ioe2) {
						throw new ICPException("Problems initializing server socket. No free port found.", ioe2);
					}
				} else {
					throw new ICPException("I/O error opening server socket on port "+port, ioe);
				} // end if (changePortIfBusy)
			}
		} catch (Exception e) {
			throw new ICPException("Error creating SSLServerSocketFactory.", e);
		}

		if (myLogger.isLoggable(Logger.FINE)) {
			myLogger.log(Logger.FINE, getChiperDebugString(sss) );
		}

		return sss;
	} // end getServerSocketWithAuth

	private static String getChiperDebugString(SSLServerSocket sss){
		// debug
		StringBuffer sb = new StringBuffer();
		sb.append( "\n--EnabledProtocols:\n" );
		String prot[] = sss.getEnabledProtocols();
		for (int i=0; i<prot.length; i++) 
			sb.append( "  "+prot[i] );
		sb.append( "\n--EnabledCipherSuites:\n" );
		String suite[] = sss.getEnabledCipherSuites() ;
		for (int i=0; i<suite.length; i++) 
			sb.append(  "  "+suite[i] );
		sb.append( "\n--SupportedCipherSuites\n" );
		String supported_suite[] = sss.getSupportedCipherSuites() ;
		for (int i=0; i<supported_suite.length; i++) 
			sb.append( "  "+supported_suite[i] );
		sb.append( "\n--\n" );
		return sb.toString();
	}

	private ServerSocket getServerSocketNoAuth(String host, int port, boolean changePortIfBusy) throws ICPException {  
		// Create the SSLContext if necessary
		if (ctx == null) {
			ctx = SSLHelper.createContextNoAuth();
		}
		// Create the SSLServerSocket
		SSLServerSocket sss = null;

		try {
			SSLServerSocketFactory ssf = ctx.getServerSocketFactory();
			try {
				sss = (SSLServerSocket) ssf.createServerSocket(port); 
			} 
			catch (IOException ioe) {
				if (changePortIfBusy) {
					// The specified port is busy. Let the system find a free one
					try {
						sss = (SSLServerSocket) ssf.createServerSocket(0);
					}
					catch (IOException ioe2) {
						throw new ICPException("Problems initializing server socket. No free port found.", ioe2);
					}
				}
				else {
					throw new ICPException("I/O error opening server socket on port "+port, ioe);
				}
			}
		}
		catch (Exception e) {
			throw new ICPException("Error creating SSLServerSocketFactory.", e);
		}

		// Initialize the SSLServerSocket to disable authentication
		try {
			sss.setEnabledCipherSuites(new String[] {"SSL_DH_anon_WITH_RC4_128_MD5"});

			String[] ecs = sss.getEnabledCipherSuites();
			//DEBUG
			//for (int i=0; i<ecs.length; i++) { 
			//	System.out.println("--"+i+"-- "+ecs[i]);
			//}
		}
		catch (Exception e) {
			throw new ICPException("Error enabling cypher suites.", e);
		}

		return sss;
	} // end getServerSocketNoAuth(..)



	private boolean useSSLAuth=false;
	private boolean getUseSSLAuth(){ // if needed, may become public
		return useSSLAuth;
	}
	private void setUseSSLAuth(boolean b){
		useSSLAuth = b;
	}


} // end class

