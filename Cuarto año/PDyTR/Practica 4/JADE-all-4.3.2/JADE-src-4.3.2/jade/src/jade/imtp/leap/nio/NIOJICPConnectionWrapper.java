package jade.imtp.leap.nio;

//#J2ME_EXCLUDE_FILE
import jade.imtp.leap.JICP.JICPPacket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * When using a single connection between FE and BE it is important to ensure that 
 * when FE writes JICP packet and waits for the Response, no IN Command is written 
 * by the BE in the meanwhile. This could be the case in the mediator startup phase
 * as exemplified below:
 * FE                                          BE                    BackEndContainer  
 *           CREATE_MEDIATOR
 *  ---------------------------------->          Start 
 *                                     ------------------------> Start Agent A1
 *                                                              |---------------->
 *                                                              |  Message for A1 
 *                                             messageIn()      <---------------------
 *               COMMAND               <------------------------|                 
 *  <----------------------------------                         | Other stuff (e.g. 
 *                                                              | start of other agents)                
 *                                           Start completed    |    
 *     CREATE_MEDIATOR_RESPONSE        <------------------------
 *  <----------------------------------
 * 
 * The situation is even worse when the GET_SERVER_TIME request is sent just after 
 * mediator startup.
 * This class simply wraps a NIOJICPConnection and forwards to it all calls to public 
 * methods. Furthermore it allows a Thread to lock the connection so that if another
 * Thread tries to write a packet it is enqueued until the connection is unlocked.
 */
class NIOJICPConnectionWrapper extends NIOJICPConnection {

	private NIOJICPConnection realConnection;
	private Thread ownerThread;
	
	NIOJICPConnectionWrapper(NIOJICPConnection c) {
		super(true);
		realConnection = c;
	}
	
	void lock() {
		ownerThread = Thread.currentThread();
	}
	
	boolean isLocked() {
		return ownerThread != null;
	}
	
	synchronized void unlock() {
		ownerThread = null;
		notifyAll();
	}
	
	public final int writePacket(JICPPacket pkt) throws IOException {
		synchronized (this) {
			while (ownerThread != null && ownerThread != Thread.currentThread()) {
				// Connection is locked and we are not the owner Thread --> wait until it is unlocked
				try {
					wait();
				}
				catch (InterruptedException ie) {
					throw new IOException("Interrupted while waiting on a locked connection");
				}
			}
		}
		return realConnection.writePacket(pkt);
	}
	
	/////////////////////////////////////////////////////////////////////
	// All other methods simply forward the call to the realConnection
	/////////////////////////////////////////////////////////////////////
	public final SocketChannel getChannel() {
		return realConnection.getChannel();
	}
	
	public final JICPPacket readPacket() throws IOException {
		return realConnection.readPacket();
	}
	
	public final boolean moreDataAvailable() {
		return realConnection.moreDataAvailable();
	}
	
	public final int writeToChannel(ByteBuffer bb) throws IOException {
		return realConnection.writeToChannel(bb);
	}
	
	public final void close() throws IOException {
		unlock(); // Be sure no one remains blocked waiting for a connection that no one will use anymore
		realConnection.close();
	}
	
	public final boolean isClosed() {
		return realConnection.isClosed();
	}

	public final String getRemoteHost() {
		return realConnection.getRemoteHost();
	}

	public final void addBufferTransformer(BufferTransformer transformer) {
		realConnection.addBufferTransformer(transformer);
	}
}
