/*****************************************************************
 JADE - Java Agent DEvelopment Framework is a framework to develop
 multi-agent systems in compliance with the FIPA specifications.
 Copyright (C) 2000 CSELT S.p.A.
 
 The updating of this file to JADE 2.0 has been partially supported by the
 IST-1999-10211 LEAP Project
 
 This file refers to parts of the FIPA 99/00 Agent Message Transport
 Implementation Copyright (C) 2000, Laboratoire d'Intelligence
 Artificielle, Ecole Polytechnique Federale de Lausanne
 
 GNU Lesser General Public License
 
 This library is free software; you can redistribute it sand/or
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

/**
 * HTTPServer.java
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
import java.util.*;
import jade.mtp.InChannel;
import jade.mtp.InChannel.Dispatcher;
import jade.mtp.MTPException;
import jade.domain.FIPAAgentManagement.Envelope;
import jade.util.Logger;

//#DOTNET_EXCLUDE_BEGIN
import javax.xml.parsers.SAXParserFactory;
//#DOTNET_EXCLUDE_END


public class HTTPServer extends Thread {
	// Codec class
	
	//static String CODEC = "org.apache.xerces.parsers.SAXParser";
	static String CODEC   = "org.apache.crimson.parser.XMLReaderImpl";
	
	private String address;
	private int port;
	private InChannel.Dispatcher dispatcher;
	private int maxKA;
	private int timeout;
	private ServerSocket server;
	
	//logging
	private static Logger logger = Logger.getMyLogger(HTTPServer.class.getName());
	
	private Vector threads; // for keep alive connections
	
	//attribute for synchronized
	private static Object lock = new Object();
	
	// the flag that shows if the server is active or not
	boolean active = true;
	
	/** Constructor: Store the information*/
	public HTTPServer(String interfaceAddress, int p, InChannel.Dispatcher d, int m, String s, int t, boolean changePortIfBusy) throws IOException {
		address    = interfaceAddress;
		port       = p;
		dispatcher = d;
		maxKA      = m;
		threads    = new Vector(maxKA);
		//#DOTNET_EXCLUDE_BEGIN
		CODEC = getSaxParserName(s);
		if (CODEC == null) {
			throw new IOException("NO XML Parser specified");
		}
		//#DOTNET_EXCLUDE_END
		logger.log(Logger.INFO, "HTTP-MTP Using XML parser "+CODEC);
		timeout = t;
		try {
			//#PJAVA_EXCLUDE_BEGIN 
			server = HTTPSocketFactory.getInstance().createServerSocket(address, port);
			//#PJAVA_EXCLUDE_END
			/*#PJAVA_INCLUDE_BEGIN
			 server = new ServerSocket(port);
			 #PJAVA_INCLUDE_END*/
		}
		catch (IOException ioe) {
			if (changePortIfBusy) {
				// The specified port is busy. Let the system find a free one
				//#PJAVA_EXCLUDE_BEGIN	
				server = HTTPSocketFactory.getInstance().createServerSocket(address, 0);
				//#PJAVA_EXCLUDE_END
				/*#PJAVA_INCLUDE_BEGIN
				 server = new ServerSocket(0);
				 #PJAVA_INCLUDE_END*/
				if(logger.isLoggable(Logger.WARNING))
					logger.log(Logger.WARNING,"Port "+p+" is already in used, selected another one");
			}
			else {
				throw ioe;
			}
		}
	}
	
	/** 
	 * Desactivate The HTTPServerThread and all other sub-threads
	 **/
	public synchronized void desactivate() {
		//Stop keep-alive Threads
		for(int i=0 ; i < threads.size(); i++) {
			((ServerThread) threads.elementAt(i)).shutdown();
		}
		// The non-keep-alive will close themselves after a while
		active = false;
		try { 
			server.close();
		}
		catch(Exception e) {
			// Does nothing as we asked to close
		}
	}
	
	//#DOTNET_EXCLUDE_BEGIN
	private String getSaxParserName(String s) {
		if (s != null) {
			// SAXParser specified by means of the jade_mtp_http_parser JADE option
			return s;
		}
		else {
			String saxFactory = System.getProperty( "org.xml.sax.driver" );
			if( saxFactory != null ) {
				// SAXParser specified by means of the org.xml.sax.driver Java option
				return saxFactory;
			}
			else {
				// Use the JVM default SAX Parser
				try {
					return SAXParserFactory.newInstance().newSAXParser().getXMLReader().getClass().getName();
				}
				catch (Throwable t) {
				}
			}
		}	
		return null;
	}
	//#DOTNET_EXCLUDE_END
	
	int getLocalPort() {
		return server.getLocalPort();
	}
	
	void addThread(ServerThread st) { 
		synchronized(lock) {
			threads.addElement(st);
			if(logger.isLoggable(Logger.CONFIG))
				logger.log(Logger.CONFIG," Added Ka threads: "+threads.size()+"/"+maxKA);
		}
	}
	
	void removeThread(ServerThread st) {  
		synchronized(lock) {
			threads.removeElement(st);
			if(logger.isLoggable(Logger.CONFIG))
				logger.log(Logger.CONFIG," Removed Ka threads: "+threads.size()+"/"+maxKA);
		}
	}
	
	boolean isSpaceLeft() {
		synchronized(lock) {
			return (threads.size() < maxKA);
		}
	}
	
	/** 
	 * Entry point for the master server thread
	 */
	public void run() {
		try {
			while(active) {  //Accept the input connections
				Socket client = server.accept();
				client.setSoTimeout(timeout);
				new ServerThread(this,client,dispatcher).start();
			}
		} 
		catch( Exception e ) {
			if (active) {
				if(logger.isLoggable(Logger.WARNING))
					logger.log(Logger.WARNING,"HTTP Server closed on port "+port);
			}
		} 
	}
	
	public static class ServerThread extends Thread {
		private HTTPServer           father;
		private Socket               client;    
		private InputStream          input;
		private OutputStream         output;
		private InChannel.Dispatcher dispatcher;
		private XMLCodec             codec;
		private boolean             keepAlive = false;
		private boolean             active    = false;
		
		/** Constructor: Store client port*/
		public ServerThread(HTTPServer f, Socket s, InChannel.Dispatcher d) { 
			father = f;
			client = s;
			dispatcher = d;
		}
		
		
		/** 
		 * Entry point for the slave server thread
		 */
		public void run () {
			try {
				//#DOTNET_EXCLUDE_BEGIN
				codec = new XMLCodec(HTTPServer.CODEC);
				//#DOTNET_EXCLUDE_END
				/*#DOTNET_INCLUDE_BEGIN
				 codec = new XMLCodec();
				 #DOTNET_INCLUDE_END*/
				input = new BufferedInputStream(client.getInputStream());
				output = new BufferedOutputStream(client.getOutputStream());
				do {
					//Read the request from client
					StringBuffer envelope   = new StringBuffer(40);
					ByteArrayOutputStream payload = new ByteArrayOutputStream(40);
					StringBuffer connection = new StringBuffer();
					String responseMsg      = HTTPIO.readAll(input,envelope,payload,connection);	
					String type = connection.toString();
					if (HTTPIO.OK.equals(responseMsg)) {
						// Extract the information from request
						//System.out.println("\n"+envelope.toString());
						//System.out.println("--------------------------");
						//System.out.println("--------------------------");
						//System.out.println(payload.toString()+"\n"); 
						//Execute parser to extract information from the Envelope
						//System.out.println("Envelope received:\n"+envelope.toString());
						
						//#DOTNET_EXCLUDE_BEGIN  
						StringReader sr = new StringReader(envelope.toString());
						//#DOTNET_EXCLUDE_END
						/*#DOTNET_INCLUDE_BEGIN
						 System.IO.StringReader sr = new System.IO.StringReader( envelope.toString() );
						 #DOTNET_INCLUDE_END*/
						Envelope env = codec.parse(sr);
						
						/*#DOTNET_INCLUDE_BEGIN
						 //There are problems if PayloadEncoding is set to US-ASCII
						  if (env.getPayloadEncoding() == null)
						  env.setPayloadEncoding(XMLCodec.CHARS_CODEC);
						  #DOTNET_INCLUDE_END*/
						
						//System.out.println("Envelope received:\n"+env);
						//Post the Message to Jade platform	
						synchronized (dispatcher) {
							
							if(logger.isLoggable(Logger.WARNING)) {
								// check payload size
								if ((env.getPayloadLength() != null) && (env.getPayloadLength().intValue() >= 0) && (env.getPayloadLength().intValue() != payload.size()))
									logger.log(Logger.WARNING,"Payload size does not match envelope information"); 
							}
							dispatcher.dispatchMessage(env,payload.toByteArray());
						}
						if (HTTPIO.KA.equalsIgnoreCase(type)) {
							if (! keepAlive) { 
								// This thread is not known yet
								if (father.isSpaceLeft()) { 
									// There is space left for a new KA
									active = true;
									keepAlive = true;
									father.addThread(this);
								}
								else {
									// This is a to-be-closed thread
									type = HTTPIO.CLOSE;
								}
							}
						}
						else {
							active = false;
						}
					}
					HTTPIO.writeAll(output,HTTPIO.createHTTPResponse(responseMsg,type));
				} while(active);
			} 
			catch(SocketException se) {
			} 
			catch(IOException ioe) {
			} 
			catch(Exception e ) {
				if(logger.isLoggable(Logger.WARNING))
					logger.log(Logger.WARNING,"HTTPServer error : "+e);
			}
			finally {
				//Close socket connection
				if (keepAlive) {
					father.removeThread(this);
					// shutdown(); 
				}
			}
			shutdown();
		}
		
		void shutdown() {
			active = false;
			keepAlive = false;
			try {
				client.close();
			}
			catch(Exception e) {
				// Nothing important can happen here
			} 
		}
		
	} //End of ServerThread class
	
}//End of HTTPServer class			


