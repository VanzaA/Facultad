/*****************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 *
 * The updating of this file to JADE 2.0 has been partially supported by the
 * IST-1999-10211 LEAP Project
 *
 * This file refers to parts of the FIPA 99/00 Agent Message Transport
 * Implementation Copyright (C) 2000, Laboratoire d'Intelligence
 * Artificielle, Ecole Polytechnique Federale de Lausanne
 *
 * GNU Lesser General Public License
 *
 * This library is free software; you can redistribute it sand/or
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
 *****************************************************************/

/**
 * MessageTransportProtocol.java
 *
 *
 * @author Jose Antonio Exposito
 * @author MARISM-A Development group ( marisma-info@ccd.uab.es )
 * @version 0.1
 * @author Nicolas Lhuillier (Motorola Labs)
 * @version 1.0
 */


package jade.mtp.http;

import java.net.*;
import java.io.*;
import java.util.Hashtable;
import java.util.Enumeration;

import jade.mtp.InChannel;
import jade.mtp.MTP;
import jade.mtp.MTPException;
import jade.mtp.TransportAddress;
import jade.domain.FIPAAgentManagement.Envelope;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.util.ExtendedProperties;

public class MessageTransportProtocol implements MTP {
	
	// DEFAULT VALUES
	private static final int    IN_PORT    = 7778;
	private static final String OUT_PORT   = "-1";
	private static final String PROXY_PORT = "1080";
	private static final String MAX_KA     = "10";
	private static final String POLICY     = "conservative"; //conservative or aggressive
	private static final String PREFIX     = "jade_mtp_http_";
	private static final String TIMEOUT    = "60000"; // 60 seconds
	
	private int    numKA;
	private String proxyHost;
	private int    proxyPort;
	private int    timeout;
	//private int    proxyKATimeout;
	private boolean policy;
	private boolean keepAlive  = false;
	private boolean useProxy   = false;
	
	private String[] protocols = {};
	private String FIPA_NAME = "fipa.mts.mtp.http.std";
	private Hashtable addr2srv = new Hashtable();
	
	//Object Keep-Alive connections
	private KeepAlive ka;
	
	
	/** MTP Interface Methods */
	public TransportAddress strToAddr(String rep) throws MTPException {
		try {
			return new HTTPAddress(rep);
		} catch(MalformedURLException mue) {
			throw new MTPException("Address mismatch: this is not a valid HTTP address.");
		}
	}
	
	public String addrToStr(TransportAddress ta) throws MTPException {
		try {
			return ((HTTPAddress) ta).toString();
		} catch(Exception e) {
			throw new MTPException(e.toString());
		}
	}
	
	public String getName() {
		return FIPA_NAME;
	}
	
	public String[] getSupportedProtocols() {
		return protocols;
	}
	
	/********************************
	 *   InChannel Interface Methods *
	 *********************************/
	
	/**
	 * Old method, only for compliance with former versions (prior 3.0)
	 */
	public TransportAddress activate(InChannel.Dispatcher disp)
	throws MTPException {
		try {
			return activate(disp,new ProfileImpl(new ExtendedProperties()));
		} catch(Exception e) {
			throw new MTPException(e.getMessage());
		}
	}
	
	public void activate(InChannel.Dispatcher disp, TransportAddress ta)
	throws MTPException {
		try {
			activate(disp,ta,new ProfileImpl(new ExtendedProperties()));
		} catch(Exception e) {
			throw new MTPException(e.getMessage());
		}
	}
	
	public TransportAddress activate(InChannel.Dispatcher disp, Profile p)
	throws MTPException {
		//Active the new HTTPAddress
		return activateServer(disp,null,p);
	}
	
	/**
	 * Actual method to activate the HTTP MTP.
	 *
	 * Customizable parameters read from profile:<UL>
	 * <LI><B>port</B>: the port this HTTP server listens to.</LI>
	 * <LI><B>numKeepAlive</B>: Maximum number of keep-alive connections.
	 * Default value is 10. Set to 0 to disable keep-alive coonections (possible performance impact).</LI>
	 * <LI><B>proxyHost</B>: Proxy host name or IP-address. No default value.</LI>
	 * <LI><B>proxyPort</B>: Default value is 1080.</LI>
	 * <LI><B>outPort</B>: Fix port to be used by HTTP client (for firewall configuration).
	 * Default value is freely chosen by Java</LI>
	 * <LI><B>parser</B>: XML SAX2 parser implementation to use.
	 * Default value is JDK 1.4 default parser.</LI>
	 * <LI><B>policy</B>: "conservative" (default value) or "aggressive".
	 * (see documentation for details).</LI>
	 * <LI><B>timeout</B>: Timeout for keep-alive connections. Default value is 1 min.
	 * 0 means infinite.</LI>
	 * </UL>
	 * Note that all these parameters must be prefixed with "jade_mtp_http_".
	 */
	public void activate(InChannel.Dispatcher disp, TransportAddress ta, Profile p) throws MTPException {
		activateServer(disp, ta, p);
	}
	
	private TransportAddress activateServer(InChannel.Dispatcher disp, TransportAddress ta, Profile p) throws MTPException {
		//Comprobation of correct HTTPAddress
		String interfaceAddress = null;
		int port = -1;
		boolean changePortIfBusy = false;
		String saxClass = null;
		HTTPAddress hta = null;
		try {
			if (ta != null) {
				hta = (HTTPAddress)ta;
			} else {
				try {
					// Create default HTTPAddress
					String tmp;
					if ((tmp = p.getParameter(PREFIX+"port",null)) != null) {
						port = Integer.parseInt(tmp);
					} else {
						// Use default port
						port = IN_PORT;
						changePortIfBusy = true;
					}
					//#DOTNET_EXCLUDE_BEGIN
					hta = new HTTPAddress(InetAddress.getLocalHost().getCanonicalHostName(),port, false);
					//#DOTNET_EXCLUDE_END
					/*#DOTNET_INCLUDE_BEGIN
					hta = new HTTPAddress(InetAddress.getLocalHost().getHostName(),port, false);
					#DOTNET_INCLUDE_END*/
				} catch( UnknownHostException ukhexc ) {
					throw new MTPException("Cannot activate MTP on default address: Unknown Host");
				} catch( MalformedURLException mexc ) {
					throw new MTPException("Cannot activate MTP on default address: Malformed URL");
				} catch( NumberFormatException nfexc ) {
					throw new MTPException("Cannot activate MTP on default address: Invalid port");
				}
			}
			interfaceAddress = hta.getHost();
			port = hta.getPortNo();
			if((port <= 0) || (port > 65535)) {
				throw new MTPException("Invalid port number "+ta.getPort());
			}
			
			// Parse other profile parameters
			numKA     = Integer.parseInt(p.getParameter(PREFIX+"numKeepAlive",MAX_KA));
			policy    = (p.getParameter(PREFIX+"policy",POLICY).equals("aggressive"))?true:false;
			int outPort   = Integer.parseInt(p.getParameter(PREFIX+"outPort",OUT_PORT));
			ka = new KeepAlive(numKA, outPort, policy);
			keepAlive = numKA > 0;
			proxyHost = p.getParameter(PREFIX+"proxyHost",null);
			if (proxyHost != null) {
				useProxy = true;
				proxyPort = Integer.parseInt(p.getParameter(PREFIX+"proxyPort",PROXY_PORT));
			}
			saxClass = p.getParameter(PREFIX+"parser",null);
			
			timeout = Integer.parseInt(p.getParameter(PREFIX+"timeout",TIMEOUT));
			
			//#PJAVA_EXCLUDE_BEGIN
			try{
				HTTPSocketFactory.getInstance().configure(p, hta);
			} catch(Exception e){
				throw new MTPException("Error configuring Socket Factory", e);
			}
			//#PJAVA_EXCLUDE_END
			// System.out.println("Parameters set:");
			// System.out.println("- KA "+numKA);
			// System.out.println("- Policy "+policy);
			// System.out.println("- Out port "+outPort);
			// System.out.println("- Proxy host "+proxyHost);
			// System.out.println("- Proxy port "+proxyPort);
			// System.out.println("- Parser "+saxClass);
			// System.out.println("- Timeout "+timeout);
		} catch (ClassCastException cce) {
			throw new MTPException("User supplied transport address not supported.");
		} catch( NumberFormatException nexc ) {
			throw new MTPException(nexc.getMessage());
		}
		
		//Creation of the Server
		try {
			//Create object server
			HTTPServer srv = new HTTPServer(interfaceAddress, port,disp,numKA,saxClass,timeout, changePortIfBusy);
			int actualPort = srv.getLocalPort();
			if (actualPort != port) {
				// The selected port is busy and a new one was selected --> Update the transport address
				boolean useHttps = "https".equalsIgnoreCase(hta.getProto());
				//#DOTNET_EXCLUDE_BEGIN
				hta = new HTTPAddress(InetAddress.getLocalHost().getCanonicalHostName(),actualPort, useHttps);
				//#DOTNET_EXCLUDE_END
				/*#DOTNET_INCLUDE_BEGIN
				hta = new HTTPAddress(InetAddress.getLocalHost().getHostName(),actualPort, useHttps);
				#DOTNET_INCLUDE_END*/
			}
			//Save the reference to HTTPServer
			addr2srv.put(hta.toString(),srv);
			//Execute server
			srv.start();
			protocols = new String[]{hta.getProto()};
			return hta;
		} catch( Exception e ) {
			throw new MTPException("While activating MTP got exception "+e);
		}
	}
	
	public void deactivate(TransportAddress ta) throws MTPException {
		// Shutdown HTTP Server
		HTTPServer srv = (HTTPServer)addr2srv.get(ta.toString());
		if( srv != null ) {
			addr2srv.remove(ta.toString());
			srv.desactivate();
			//srv.interrupt();
		} else
			throw new MTPException("No server on address "+ta);
	}
	
	public void deactivate() throws MTPException {
		for(Enumeration it=addr2srv.keys(); it.hasMoreElements() ; ) {
			TransportAddress ta=(TransportAddress)it.nextElement();
			deactivate(ta);
		}
		
	}
	
	
	/********************************
	 *  OutChannel Interface Methods *
	 *********************************/
	
	public void deliver(String addr, Envelope env, byte[] payload) throws MTPException {
		HTTPAddress url;
		byte[] request;
		
		try {
			
			HTTPAddress host = new HTTPAddress(addr);
			if (useProxy) {
				url = new HTTPAddress(proxyHost,proxyPort,false); //false=>do_not_use HTTPS with the proxy
			} else {
				url = host;
			}
			String connPol = (keepAlive)?HTTPIO.KA:HTTPIO.CLOSE;
			
			// Prepare the HTTP request
			// ------------------------
			//Calculate the value of boundary
			StringBuffer boundary = new StringBuffer();
			for( int i=0 ; i < 31 ; i++ ) {
				boundary.append(Integer.toString((int)Math.round(Math.random()*15),16));
			}
			
			//Request body
			//String body = HTTPIO.createHTTPBody(env,boundary.toString(),new String(payload));
			byte[] boundaryBytes = boundary.toString().getBytes("ISO-8859-1");
			byte[] body = HTTPIO.createHTTPBody(env,boundaryBytes,payload);
			
			//HTTP header
			//StringBuffer req = HTTPIO.createHTTPHeader(host,body.length(),connPol,boundary.toString(),useProxy);
			byte[] header = HTTPIO.createHTTPHeader(host,body.length,connPol,boundaryBytes,useProxy);
			// Concatenate header + body
			//req.append(body);
			ByteArrayOutputStream requestStream = new ByteArrayOutputStream(header.length + body.length);
			requestStream.write(header);
			requestStream.write(body);
			requestStream.flush();
			requestStream.close();
			request = requestStream.toByteArray();
			
		} catch (Exception e) {
			throw new MTPException(e.getMessage());
		}
		ka.send(url, request);
	}
	
	
} // End of MessageTransportProtocol class
