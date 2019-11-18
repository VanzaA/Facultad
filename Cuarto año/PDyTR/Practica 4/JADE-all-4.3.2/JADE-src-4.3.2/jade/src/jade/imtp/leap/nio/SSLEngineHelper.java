/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jade.imtp.leap.nio;

//#J2ME_EXCLUDE_FILE
import jade.imtp.leap.ICPException;
import jade.imtp.leap.SSLHelper;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;

/**
 * helper class that holds the ByteBuffers, SSLEngine and other Objects that deal with the non static part of the
 * ssl/nio handshaking/input/output. The contained SSLEngine is hidden to apps, because this helper takes on the responsibility
 * of dealing with concurrency issues
 * 
 * @author eduard
 */
public final class SSLEngineHelper implements BufferTransformer {

	public static final ByteBuffer EMPTY_BUFFER = NIOHelper.EMPTY_BUFFER;

	private SSLEngine ssle = null;

	private ByteBuffer wrapData;
	private ByteBuffer unwrapData;

	private NIOJICPConnection connection = null;

	private static Logger log = Logger.getLogger(SSLEngineHelper.class.getName());

	/**
	 * Creates and initializes ByteBuffers and SSLEngine necessary for ssl/nio.
	 * @see NIOHelper
	 * @see SSLHelper
	 * @param host provides a hint for optimization
	 * @param port provides a hint for optimization
	 * @param connection the connection that will use this helper
	 * @throws ICPException
	 */
	public SSLEngineHelper(String host, int port, NIOJICPConnection connection) throws ICPException {
		SSLContext context = SSLHelper.createContext();
		// get a SSLEngine, use host and port for optimization
		ssle = context.createSSLEngine(host, port);
		ssle.setUseClientMode(false);
		ssle.setNeedClientAuth(SSLHelper.needAuth());
		if (!SSLHelper.needAuth()) {
			// if we don't do authentication we restrict our ssl connection to use specific cipher suites
			ssle.setEnabledCipherSuites(SSLHelper.getSupportedKeys());
		}
		setBufferSizes();

		this.connection = connection;
		
		log.log(Level.INFO, "Create SSLEngine " + ssle + " of connection " + connection);
	}

	private void setBufferSizes() {
		SSLSession session = ssle.getSession();
		unwrapData = ByteBuffer.allocateDirect(session.getApplicationBufferSize());
		wrapData = ByteBuffer.allocateDirect(session.getPacketBufferSize());
		if (log.isLoggable(Level.FINE)) {
			NIOHelper.logBuffer(wrapData, "wrapData", Level.FINE);
			NIOHelper.logBuffer(unwrapData, "unwrapData", Level.FINE);
		}
	}

	/**
	 * runs all background handshaking tasks, blocks until these are finished
	 * @return the new handshake status after finishing the background tasks
	 */
	private SSLEngineResult.HandshakeStatus runHandshakeTasks() {
		Runnable task = null;
		while ((task = ssle.getDelegatedTask()) != null) {
			task.run();
		}
		// re evaluate handshake status
		return ssle.getHandshakeStatus();
	}

	/**
	 * closes the SSLEngine, tries to send a ssl close message
	 * @throws IOException
	 */
	public synchronized void close() throws IOException {
		/*
		 * try to nicely terminate ssl connection
		 * 1 closeoutbound
		 * 2 send ssl close message
		 * 3 wait for client to also send close message
		 * 4 close inbound
		 * 5 don't let all this frustrate the channel closing
		 */
		if (ssle != null) {
			log.log(Level.INFO, "Close SSLEngine " + ssle + " of connection " + connection);
			
			ssle.closeOutbound();
	
			sendSSLClose();
	
			try {
				ssle.closeInbound();
			} catch(Exception e) {
				log.log(Level.WARNING, "Error in closeInbound, SSLEH="+this);
			}
			
			// give gc a chance to cleanup
			ssle = null;
		}
	}

	private void sendSSLClose() {
		try {
			// try to send close message
			while (!ssle.isOutboundDone()) {
				wrapAndSend();
			}
		} catch (IOException e) {
			log.log(Level.FINE, "unable to send ssl close packet", e);
		}
	}

	private final int writeToChannel(ByteBuffer b) throws IOException {
		int m = b.remaining();
		int n = connection.writeToChannel(b);
		if (n != m) {
			throw new IOException("should write " + m + ", written " + n);
		}
		return n;
	}

	private String getRemoteHost() {
		return connection.getRemoteHost();
	}

	private SSLEngineResult unwrapData(ByteBuffer socketData) throws SSLException {
		SSLEngineResult result = null;
		do {
			try {
				// Unwrap (decode) the next SSL block. Unwrapped application data (if any) are put into the unwrapData buffer
				result = ssle.unwrap(socketData, unwrapData);
				if (log.isLoggable(Level.FINE))
					log.fine("Decoded " + result.bytesConsumed() + " bytes; Produced "+result.bytesProduced()+" application-data bytes ["+getRemoteHost()+"]");
			} catch (SSLException e) {
				// send close message to the client and re-throw the exception
				log.log(Level.WARNING, "Unwrap failure ["+getRemoteHost()+"]", e);
				try {
					close();
				} catch(IOException ex) {}
				throw e;
			}
		} while (result.getStatus().equals(Status.OK) && result.getHandshakeStatus().equals(HandshakeStatus.NEED_UNWRAP));

		return result;
	}

	private void checkStatusAfterHandshakeTasks(SSLEngineResult.HandshakeStatus handshakeStatus) throws SSLException, IOException {
		if (handshakeStatus.equals(HandshakeStatus.FINISHED)) {
			log.warning("Unexpected FINISHED SSL handshake status after execution of handshake tasks ["+getRemoteHost()+"]");
		} else if (handshakeStatus.equals(HandshakeStatus.NEED_TASK)) {
			log.warning("Unexpected NEED_TASK SSL handshake status after execution of handshake tasks ["+getRemoteHost()+"]");
		} else if (handshakeStatus.equals(HandshakeStatus.NEED_UNWRAP)) {
			if (log.isLoggable(Level.FINE))
				log.fine("Need more data to proceed with Handshake ["+getRemoteHost()+"]");
		} else if (handshakeStatus.equals(HandshakeStatus.NEED_WRAP)) {
			if (log.isLoggable(Level.FINE))
				log.fine("Send back Handshake data after task execution ["+getRemoteHost()+"]");
			wrapAndSend();
		} else if (handshakeStatus.equals(HandshakeStatus.NOT_HANDSHAKING)) {
			log.warning("Unexpected NOT_HANDSHAKING SSL handshake status after execution of handshake tasks ["+getRemoteHost()+"]");
		}
	}

	/**
	 * @return the number of bytes available in the unwrapBuffer
	 * @throws IOException
	 */
	private synchronized int decrypt(ByteBuffer socketData) throws SSLException, IOException {
		if (log.isLoggable(Level.FINE)) {
			log.fine("Decrypt incoming data: remaining = " + socketData.remaining() + ", position = " + socketData.position() + ", limit = " + socketData.limit() + " [" + getRemoteHost() + "]");
		}
		SSLEngineResult result = unwrapData(socketData);
		if (log.isLoggable(Level.FINE)) {
			log.fine("Checking handshake result [" + getRemoteHost() + "]");
		}
		int n = 0;
		SSLEngineResult.Status status = result.getStatus();
		SSLEngineResult.HandshakeStatus handshakeStatus = result.getHandshakeStatus();
		boolean recurse = true;
		if (status.equals(Status.OK)) {
			if (handshakeStatus.equals(HandshakeStatus.NOT_HANDSHAKING)) {
				// Application data
				n = result.bytesProduced();
			} else if (handshakeStatus.equals(HandshakeStatus.FINISHED)) {
				// Handshake completed
				if (log.isLoggable(Level.FINE)) {
					log.fine("Handshake finished [" + getRemoteHost() + "]");
				}
			} else if (handshakeStatus.equals(HandshakeStatus.NEED_TASK)) {
				// The SSLEngine requires one or more "tasks" to be executed before the handshake can proceed -->
				// Run them and then go on taking into account the new handshake-status
				if (log.isLoggable(Level.FINE)) {
					log.fine("Activate Handshake task [" + getRemoteHost() + "]");
				}
				handshakeStatus = runHandshakeTasks();
				checkStatusAfterHandshakeTasks(handshakeStatus);
			} else if (handshakeStatus.equals(HandshakeStatus.NEED_UNWRAP)) {
				// This should never happen since we cannot exit the unwrapData() method with status == OK and handshakeStatus == NEED_UNWRAP
				throw new SSLException("Unexpected NEED_UNWRAP SSL handshake status! [" + getRemoteHost() + "]");
			} else if (handshakeStatus.equals(HandshakeStatus.NEED_WRAP)) {
				// We must send data to the remote side for the handshake to continue
				if (log.isLoggable(Level.FINE)) {
					log.fine("Send back Handshake data [" + getRemoteHost() + "]");
				}
				wrapAndSend();
			}
		} else if (status.equals(Status.CLOSED)) {
			if (log.isLoggable(Level.FINE)) {
				log.fine(" sslengine closed [" + getRemoteHost() + "]");
			}
			// send ssl close, don't recurse
			recurse = false;
			close();
		} else if (status.equals(Status.BUFFER_UNDERFLOW)) {
			// There is not enough data in the socketData buffer to unwrap a meaningful SSL block -->
			// Wait for next data from the socket
			if (log.isLoggable(Level.FINE)) {
				log.fine("Not enough data to decode a meaningful SSL block. " + socketData.remaining() + " unprocessed bytes. [" + getRemoteHost() + "]");
			}
			// Avoid entering an infinite recursion trying to continuously decode bytes still present in socketData (that are
			// not sufficient to unwrap a meaningful SSL block)
			return n;
		} else if (status.equals(Status.BUFFER_OVERFLOW)) {
			// The unwrapData buffer is too small to hold all unwrapped data --> Repeat with a larger buffer
			if (log.isLoggable(Level.FINE)) {
				NIOHelper.logBuffer(socketData, "socketData", Level.FINE);
			}
			log.info("Buffer overflow. Enlarge buffer and retry [" + getRemoteHost() + "]");
			unwrapData.flip();
			unwrapData = NIOHelper.enlargeAndFillBuffer(unwrapData, BEManagementService.getBufferIncreaseSize(), "unwrapData");
			return decrypt(socketData);
		}
		// If the socketData buffer contains unprocessed data, manage them
		if (socketData.hasRemaining() && recurse) {
			n += decrypt(socketData);
		}
		// Return the number of valid application data
		return n;
	}


	/**
	 * generates handshake data and calls {@link NIOJICPConnection#writeToChannel(java.nio.ByteBuffer) }, possibly
	 * recursive when required by handshaking
	 * @return the amount of bytes written to the connection
	 * @throws SSLException
	 * @throws IOException
	 */
	private int wrapAndSend() throws SSLException, IOException {
		wrapData.clear();
		int n = 0;
		SSLEngineResult result = ssle.wrap(EMPTY_BUFFER,wrapData);
		if (log.isLoggable(Level.FINE)) {
			log.fine("wrapped " + result);
		}
		if (result.bytesProduced() > 0) {
			wrapData.flip();
			n = writeToChannel(wrapData);

			if (result.getHandshakeStatus().equals(HandshakeStatus.NEED_WRAP)) {
				n += wrapAndSend();
			}
			return n;
		} else {
			log.warning("wrap produced no data " + getRemoteHost());
		}
		return n;
	}

	public synchronized ByteBuffer preprocessBufferToWrite(ByteBuffer dataToSend) throws IOException {
		wrapData.clear();
		
		if (ssle == null) {
			throw new IllegalStateException("SSLEngine previously closed. The connection must be reestablished.");
		}
		
		while (dataToSend.hasRemaining()) {
			SSLEngineResult res = ssle.wrap(dataToSend,wrapData);
			if (log.isLoggable(Level.FINE)) {
				log.fine("wrapped " + res);
			}
			if (res.getStatus().equals(Status.BUFFER_OVERFLOW)) {
				wrapData.flip();
				wrapData = NIOHelper.enlargeAndFillBuffer(wrapData,BEManagementService.getBufferIncreaseSize(),"wrapData");
			}
		}
		wrapData.flip();
		return wrapData;
	}

	public synchronized ByteBuffer postprocessBufferRead(ByteBuffer socketData) throws PacketIncompleteException, IOException {
		//needMoreSocketData = false;
		unwrapData.clear();
		int n = decrypt(socketData);
		if (n > 0) {
			unwrapData.flip();
			return unwrapData;
		} else {
			return EMPTY_BUFFER;
		}
	}

	public boolean needSocketData() {
		return false;
	}
	
	@Override
	public String toString() {
		return super.toString()+"SSLE="+ssle;
	}
}
