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
 * KeepAlive.java
 *
 * @author Jose Antonio Exposito
 * @author MARISM-A Development group ( marisma-info@ccd.uab.es )
 * @version 0.1
 * @author Nicolas Lhuillier (Motorola Labs)
 * @version 1.0
 */


package jade.mtp.http;

import jade.mtp.MTPException;
import jade.util.leap.HashMap;
import jade.util.Logger;

import java.io.*;
import java.util.Vector;
import java.net.Socket;
import java.net.InetAddress;
import java.net.UnknownHostException;


/**
 * This class represents a connection to a remote server
 */
public class KeepAlive {
	
	private static Logger logger = Logger.getMyLogger(KeepAlive.class.getName());
	
  /*
   * Inner structure to contain all connection information
   */
	public static class KAConnection {
		private OutputStream   out;
		private InputStream    in;
		private HTTPAddress    address;
		private Vector         connections;
		private int outPort;
		
		KAConnection(HTTPAddress a, int outPort) {
			address = a;
			this.outPort = outPort;
		}
		
		public void open() throws IOException {
			// The address is new or the KA connection was closed
			//HTTPAddress is new or cached missed;
			// Open a new connection
			Socket client;
			//#PJAVA_EXCLUDE_BEGIN
			HTTPSocketFactory sfac = HTTPSocketFactory.getInstance();
			if (outPort > 0) {
				client = sfac.createSocket(address.getHost(),address.getPortNo(),InetAddress.getLocalHost(),outPort);
			} else {
				client = sfac.createSocket(address.getHost(),address.getPortNo());
			}
			
			//#PJAVA_EXCLUDE_END
			/*#PJAVA_INCLUDE_BEGIN
			if (outPort > 0)
			{
				client = new Socket(address.getHost(),address.getPortNo(),InetAddress.getLocalHost(),outPort);
			}
			else {
				client = new Socket(address.getHost(),address.getPortNo());
			}
			#PJAVA_INCLUDE_END*/
			out = new BufferedOutputStream(client.getOutputStream());
			in = new BufferedInputStream(client.getInputStream());
		}
		
		OutputStream getOut() {
			return out;
		}
		
		InputStream getIn() {
			return in;
		}
		
		public HTTPAddress getAddress() {
			return address;
		}
		
		public boolean equals(HTTPAddress a) {
			return address.equals(a);
		}
		
		
		void close() {
			if (isOpen()) {
				try {
					in.close();
					out.close();
				} catch(IOException ioe) {
					if(logger.isLoggable(Logger.WARNING))
						logger.log(Logger.WARNING,"Exception while closing KA connection: "+ioe);
				}
				in = null;
				out = null;
			}
		}
		
		boolean isOpen() {
			return in != null;
		}
		
		void send(byte[] req) throws MTPException {
			try {
				if(logger.isLoggable(Logger.FINER))
					logger.log(Logger.FINER,"Sending HTTP message to: "+ address);
				HTTPIO.writeAll(out,req);
				//Capture the HTTPresponse
				StringBuffer typeConnection = new StringBuffer();
				int code = HTTPIO.getResponseCode(in, typeConnection);
				if (!HTTPIO.KA.equals(typeConnection.toString())) {
					// Close the connection
					if(logger.isLoggable(Logger.FINER))
						logger.log(Logger.FINER,"Closing " + typeConnection +" connection to " + address);
					close();
				}
				if (code != 200) {
					if(logger.isLoggable(Logger.FINER))
						logger.log(Logger.FINER,"Not OK: " + code +", Closing connection to " + address);
					close();
					throw new MTPException("Description: ResponseMessage is not OK");
				}
			} catch (IOException e) {
				close();
				throw new MTPException(e.getMessage(), e);
			}
		}
		
		
		
	} // End of KAConnection inner class
	
	private final Vector   connections;
	private final int      dim;
	private final int      outPort;
	private final boolean  agressive;
	private final HashMap locks = new HashMap();
	
	/** Constructor */
	public KeepAlive(int dim, int outPort, boolean agressive) {
		connections = new Vector(dim);
		this.dim = dim;
		this.outPort = outPort;
		this.agressive = agressive;
	}
	
	/** add a new connection */
	public synchronized void add(KAConnection c) {
		try {
			//The vectors are full.
			if (connections.size() == dim) {
				remove(0); //Remove the first element of vectors, is the older element
			}
			connections.addElement(c);
			//System.out.println("DEBUG: Added Ka conn: "+connections.size()+"/"+dim+" with "+c.getAddress().getPortNo());
		} catch(Exception ioe) {
			if(logger.isLoggable(Logger.WARNING))
				logger.log(Logger.WARNING,ioe.getMessage());
		}
	}
	
	/** delete an exisiting connection, based on position */
	private void remove(int pos) {
		try {
			KAConnection old = getConnection(pos);
			connections.removeElementAt(pos);
			old.close();
		} catch(Exception ioe) {
			if(logger.isLoggable(Logger.WARNING))
				logger.log(Logger.WARNING,ioe.getMessage());
		}
	}
	
	/** delete an exisiting connection, based on its address */
	public synchronized void remove(HTTPAddress addr) {
		connections.removeElement(search(addr));
	}
	
	/** delete an exisiting connection*/
	public synchronized void remove(KAConnection ka) {
		connections.removeElement(ka);
	}
	
	
	
	/** get the socket of the connection when addr make matching */
	private KAConnection getConnection(int pos) {
		return (KAConnection)connections.elementAt(pos);
	}
	
	private KAConnection search(HTTPAddress addr) {
		if (addr != null) {
			KAConnection c;
			for(int i=(connections.size()-1); i >= 0; i--) {
				if ((c=(KAConnection)getConnection(i)).equals(addr)) {
					return c;
				}
			}
		}
		return null;
	}
	
	/** get the socket of the connection when addr make matching */
	public KAConnection getConnection(HTTPAddress addr) {
		return search(addr);
	}
	
	private KAConnection createConnection(final HTTPAddress url) throws MTPException {
		KeepAlive.KAConnection kac = null;
		try {
			kac = new KAConnection(url, outPort);
			kac.open();
			return kac;
		} catch (IOException e) {
			//Remove the inputs of KA object for the current address
			if (kac != null) {
				kac.close();
			}
			throw new MTPException(e.getMessage(), e);
		}
	}
	
	/** get the dimension of Vectors */
	public  int getDim(){
		return dim;
	}
	
	/** get the capacity of Vectors */
	public int capacity() {
		//System.out.println("DIMENSION: "+dim+"  "+"TAMVECT: "+addresses.size());
		return (dim - connections.size());
	}
	
	public synchronized void swap(KAConnection c) {
		try {
			//if only have 1 socket isn't necessary make swap function
			if ((dim > 1)&&(!(connections.indexOf(c)==(connections.size()-1)))) {
				//remove the elements at former position
				connections.removeElement(c);
				//put the elements at the end
				connections.addElement(c);
			}
		} catch(Exception ioe) {
			if(logger.isLoggable(Logger.WARNING))
				logger.log(Logger.WARNING,ioe.getMessage());
		}
	}
	
	
	public void send(HTTPAddress url, byte[] request) throws MTPException {
		Object lock = getLock(url);
		synchronized(lock) {
			KeepAlive.KAConnection kac = null;
			// Try to re-use an existing socket
			if (dim > 0) {
				//Search the address in Keep-Alive object
				kac = getConnection(url);
				if (kac != null) {
					try {
						if(logger.isLoggable(Logger.FINER))
							logger.log(Logger.FINER,"Reusing keepAlive for " + url);
						kac.send(request);
						if (kac.isOpen()) {
							//change the priority of socket & another components of keep-alive object
							//Only the policy == AGGRESSIVE
							//HTTPAddress is cached;
							if (agressive) {
								swap(kac);
							}
						} else {
							if(logger.isLoggable(Logger.FINER))
								logger.log(Logger.FINER,"Removing keepAlive for " + url);
							remove(kac);
						}
					} catch (MTPException e) {
						if(logger.isLoggable(Logger.FINER))
							logger.log(Logger.FINER,"Removing keepAlive for " + url);
						remove(kac);
						// retry with new connection
						kac = null;
					}
				}
			}
			if (kac == null) {
				if(logger.isLoggable(Logger.FINER))
					logger.log(Logger.FINER,"Creating connection to " + url);
				kac = createConnection(url);
				// Send out and check response code
				kac.send(request);
				if (kac.isOpen()) {
					if (dim > 0) {
						// Store the new connection
						if(logger.isLoggable(Logger.FINER))
							logger.log(Logger.FINER,"Adding keepAlive for " + url);
						add(kac);
					} else {
						if(logger.isLoggable(Logger.FINER))
							logger.log(Logger.FINER,"Closing open connection for " + url);
						kac.close();
					}
				}
			}
		}
	}
	
	private Object getLock(final HTTPAddress url) {
		Object lock = locks.get(url.getHost());
		if (lock == null) {
			synchronized(this) {
				lock = locks.get(url.getHost());
				if (lock == null) {
					lock = new Object();
					locks.put(url.getHost(), lock);
				}
			}
		}
		return lock;
	}
	
} //End of class KeepAlive
