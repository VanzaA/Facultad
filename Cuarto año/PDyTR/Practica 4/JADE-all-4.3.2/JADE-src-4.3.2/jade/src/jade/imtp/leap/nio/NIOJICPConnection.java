package jade.imtp.leap.nio;

//#J2ME_EXCLUDE_FILE
import jade.imtp.leap.ICPException;
import jade.imtp.leap.JICP.*;

import java.io.IOException;
import java.io.EOFException;
import java.io.ByteArrayOutputStream;
import java.nio.*;
import java.nio.channels.*;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
@author Giovanni Caire - TILAB
 */
public class NIOJICPConnection extends Connection {
	// type+info+session+recipient-length+recipient(255)+payload-length(4)
	public static final int MAX_HEADER_SIZE = 263;
	// TODO 1k, why? configurable?
	public static final int INITIAL_BUFFER_SIZE = 1024;
	
	private SocketChannel myChannel;
	private ByteBuffer socketData; 
	private ByteBuffer payloadBuf;
	private ByteBuffer unmanagedJicpData = null;

	private byte type;
	private byte info;
	private byte sessionID;
	private String recipientID;

	private boolean headerReceived = false;
	private boolean closed = false; 

	private List<BufferTransformerInfo> transformers;

	private static final Logger log = Logger.getLogger(NIOJICPConnection.class.getName());

	public NIOJICPConnection() {
		socketData = ByteBuffer.allocateDirect(INITIAL_BUFFER_SIZE); 
		payloadBuf = ByteBuffer.allocateDirect(INITIAL_BUFFER_SIZE);
		transformers = new LinkedList<BufferTransformerInfo>();
	}
	
	// This constructor is only used by the NIOJICPConnectionWrapper that,
	// being just a wrapper, does not need to allocate the internal buffers 
	protected NIOJICPConnection(boolean wrapper) {
	}
	
	public SocketChannel getChannel() {
		return myChannel;
	}
	
	/**
    Read a JICPPacket from the connection.
    The method is synchronized since we reuse the same Buffer object
    for reading the packet header.
    It should be noted that the packet data may not be completely
    available when the embedded channel is ready for a READ operation.
    In that case a PacketIncompleteException is thrown to indicate
    that successive calls to this method must occur in order to
    fully read the packet.
	 */
	public synchronized JICPPacket readPacket() throws IOException {
		ByteBuffer jicpData = null;
		if (unmanagedJicpData == null) { 
			// No JICP data to be processed from previous round --> Read new data from the network
			read();
			jicpData = transformAfterRead(socketData);
			// NOTE: Transformers are expected to transform ALL (not just the first one) units they
			// are able to manage --> At this point we have:
			// - socketData empty!
			// - unprocessedData inside transformers (see the BufferTransformerInfo inner class) 
			// possibly containing some bytes representing a portion of a transformation unit (more 
			// data from the network is necessary to transform them)
			// - jicpData containing one of the following:
			//   1) Nothing (no bytes were fully transformed)
			//   2) The first part of a JICPPacket (Header plus a portion of the payload)
			//   3) Exactly 1 JICPPacket or the remaining part of the payload in case at previous 
			//      round we fell in case 2 or 4.b 
			//   4) Case 3 plus 0 or more complete JICPPacket plus, optionally, the first part of a JICPPacket (case 4.b) 
			// In case 1 and 2 the method throws PacketIncompleteException.
			// In case 3 the method returns the reconstructed JICPPacket.
			// In case 4 the method returns the first reconstructed packet and store remaining JICP
			// data. When that occurs the moreDataAvailable() method returns true to indicate that 
			// the readPacket() method must be called again before going back to wait for network data.
		}
		else {
			// Some JICP data still need to be processed from previous round --> do it.
			jicpData = unmanagedJicpData;
		}
		
		try {
			if (jicpData.hasRemaining()) {
				// JICP data actually available after transformations
				if (!headerReceived) {
					// Note that, since we require that a JICP-Header is never split, we 
					// are sure that at least all header bytes are available
					//System.out.println("Read "+jicpData.remaining()+" bytes");
					headerReceived = true;
					type = jicpData.get();
					//System.out.println("type = "+type);
					info = jicpData.get();
					//System.out.println("info = "+info);
					sessionID = -1;
					if ((info & JICPProtocol.SESSION_ID_PRESENT_INFO) != 0) {
						sessionID = jicpData.get();
						//System.out.println("SessionID = "+sessionID);
					}
					if ((info & JICPProtocol.RECIPIENT_ID_PRESENT_INFO) != 0) {
						byte recipientIDLength = jicpData.get();
						byte[] bb = new byte[recipientIDLength];
						jicpData.get(bb);
						recipientID = new String(bb);
					}
					if ((info & JICPProtocol.DATA_PRESENT_INFO) != 0) {
						int b1 = (int) jicpData.get();
						int b2 = (int) jicpData.get();
						int payloadLength = ((b2 << 8) & 0x0000ff00) | (b1 & 0x000000ff);
						int b3 = (int) jicpData.get();
						int b4 = (int) jicpData.get();
						payloadLength |= ((b4 << 24) & 0xff000000) | ((b3 << 16) & 0x00ff0000);
	
						if (payloadLength > JICPPacket.MAX_SIZE) {
							throw new IOException("Packet size greater than maximum allowed size. " + payloadLength);
						}
	
						resizePayloadBuffer(payloadLength);
	
						// jicpData likely already contains some payload bytes --> copy them into the payload buffer
						NIOHelper.copyAsMuchAsFits(payloadBuf, jicpData);
	
						if (payloadBuf.hasRemaining()) {
							// Payload not completely received. Wait for next round 
							throw new PacketIncompleteException("Missing "+payloadBuf.remaining()+" payload bytes");
						} else {
							return buildPacket(jicpData);
						}
					} else {
						return buildPacket(jicpData);
					}
				}
				else {
					// We are in the middle of reading the payload of a packet (the previous call to readPacket() resulted in a PacketIncompleteException)
					NIOHelper.copyAsMuchAsFits(payloadBuf, jicpData);
					if (payloadBuf.hasRemaining()) {
						// Payload not completely received. Wait for next round 
						throw new PacketIncompleteException("Missing "+payloadBuf.remaining()+" payload bytes");
					} else {
						return buildPacket(jicpData);
					}
				}
			}
			else {
				// No JICP data available at this round. Wait for next one
				throw new PacketIncompleteException(socketData.limit()+" bytes read from the network. No JICP data transformed");
			}
		}
		finally {
			if (unmanagedJicpData != null && !unmanagedJicpData.hasRemaining()) {
				// If we just fully processed some previous-round-unmanaged data, reset the unmanagedJicpData buffer
				// so that we are sure to read bytes from the network at next round
				unmanagedJicpData  = null;
			}
		}
	}
	
	public boolean moreDataAvailable() {
		return unmanagedJicpData != null;
	}

	private void read() throws IOException {
		socketData.clear();
		readFromChannel(socketData);
		while (!socketData.hasRemaining()) {
			// We read exactly how many bytes how socketData can contain. VERY likely there are 
			// more bytes to read from the channel --> Enlarge socketData and read again
			socketData.flip();
			socketData = NIOHelper.enlargeAndFillBuffer(socketData, BEManagementService.getBufferIncreaseSize(), "socketData");
			try {
				readFromChannel(socketData);
			}
			catch (EOFException eofe) {
				// Return the bytes read so far
				break;
			}
		}
		socketData.flip();
		if (log.isLoggable(Level.FINE)) 
			log.fine("------- READ "+socketData.remaining()+" bytes from the network");
	}

	
	/**
	 * reads data from the socket into a buffer
	 * @param b
	 * @return number of bytes read
	 * @throws IOException
	 */
	private final int readFromChannel(ByteBuffer b) throws IOException {
		int n = myChannel.read(b);
		if (n == -1) {
			throw new EOFException("Channel closed");
		}
		return n;
	}

	private ByteBuffer transformAfterRead(ByteBuffer incomingData) throws IOException {
		// Let BufferTransformers process incoming data
		ByteBuffer transformationInput = incomingData;
		ByteBuffer transformationOutput = transformationInput;
		for (ListIterator<BufferTransformerInfo> it = transformers.listIterator(transformers.size()); it.hasPrevious();) {
			BufferTransformerInfo info = it.previous();
			BufferTransformer btf = info.getTransformer();

			// In case there were unprocessed data at previous round, append them before the data to be processed at this round 
			transformationInput = info.attachUnprocessedData(transformationInput);

			if (log.isLoggable(Level.FINER)) 
				log.finer("--------- Passing "+transformationInput.remaining()+" bytes to Transformer "+btf.getClass().getName());
			transformationOutput = btf.postprocessBufferRead(transformationInput);
			if (log.isLoggable(Level.FINER))
				log.finer("--------- Transformer "+btf.getClass().getName()+" did not transform " +transformationInput.remaining()+" bytes");

			// In case the transformer did not process all input data, store unprocessed data for next round
			info.storeUnprocessedData(transformationInput);

			// Output of transformer N becomes input of transformer N-1 (transformers are scanned in reverse order when managing incoming data)
			transformationInput = transformationOutput;

			if (!transformationInput.hasRemaining() && it.hasPrevious()) {
				// No bytes for next transformation --> no need to continue scanning transformers
				break;
			}
		}
		return transformationOutput;
	}


	private void resizePayloadBuffer(int payloadLength) {
		if (payloadLength > payloadBuf.capacity()) {
			payloadBuf = NIOHelper.enlargeBuffer(payloadBuf, payloadLength - payloadBuf.capacity(), "payLoad",true);
		} else {
			payloadBuf.limit(payloadLength);
		}
	}

	private JICPPacket buildPacket(ByteBuffer jicpData) {
		payloadBuf.flip();
		byte[] payload = new byte[payloadBuf.remaining()];
		payloadBuf.get(payload, 0, payload.length);
		JICPPacket pkt = new JICPPacket(type, info, recipientID, payload);
		pkt.setSessionID(sessionID);

		// Reset internal fields to properly manage next JICP packet
		headerReceived = false;
		recipientID = null;
		payloadBuf.clear();
		
		// Before returning the reconstructed packet check if some JICP data still need
		// to be processed and store it in that case
		if (jicpData.hasRemaining()) {
			unmanagedJicpData = jicpData;
		}
		
		return pkt;
	}


	/**
	 * Write a JICPPacket on the connection, first calls {@link #preprocessBufferToWrite(java.nio.ByteBuffer) }.
	 * When the buffer returned by {@link #preprocessBufferToWrite(java.nio.ByteBuffer) }, no write will be performed.
	 * @return number of application bytes written to the socket
	 */
	public synchronized int writePacket(JICPPacket pkt) throws IOException {
		ByteBuffer bb = null;
		int totalToWrite = 0;
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		int n = pkt.writeTo(os);
		if (log.isLoggable(Level.FINE)) {
			log.fine("writePacket: number of bytes before preprocessing: " + n);
		}
		ByteBuffer toSend = ByteBuffer.wrap(os.toByteArray());
		bb = transformBeforeWrite(toSend);
		if (toSend.hasRemaining() && transformers.size() > 0) {
			// for direct JICPConnections the data from the packet are used directly
			// for subclasses the subsequent transformers must transform all data from the packet before sending
			throw new IOException("still need to transform: " + toSend.remaining());
		}
		int totalWrited = 0;
		totalToWrite = bb.remaining();
		while (bb.hasRemaining()) {
			int toWrite = bb.remaining();
			int writed = writeToChannel(bb);
			totalWrited += writed;
			if (log.isLoggable(Level.FINE)) {
				log.fine("writePacket: bytes written " + writed + ", needed to write: " + toWrite);
			}
		}
		
		if (log.isLoggable(Level.FINE)) {
			log.fine("writePacket: total bytes written " + totalWrited + ", total needed to write: " + totalToWrite);
		}

		return totalWrited;
	}

	private ByteBuffer transformBeforeWrite(ByteBuffer data) throws IOException {
		for (BufferTransformerInfo info : transformers) {
			BufferTransformer btf = info.getTransformer();
			data = btf.preprocessBufferToWrite(data);
		}
		return data;
	}

	/**
	 * writes data to the channel
	 * @param bb
	 * @return the number of bytes written to the channel
	 * @throws IOException
	 */
	public int writeToChannel(ByteBuffer bb) throws IOException {
		return myChannel.write(bb);
	}


	/**
    Close the connection
	 */
	public void close() throws IOException {
		closed = true;
		myChannel.close();
	}

	// In some cases we may receive some data (often a socket closed by peer indication) while 
	// closing the channel locally. Trying to read such data results in an Exception. To avoid printing 
	// this Exception it is possible to check this method
	public boolean isClosed() {
		return closed;
	}

	public String getRemoteHost() {
		return myChannel.socket().getInetAddress().getHostAddress();
	}

	/**
	 * sets the channel for this connection
	 * @param channel
	 * @throws ICPException
	 */
	void init(SocketChannel channel) throws ICPException {
		this.myChannel = (SocketChannel) channel;
	}

	public void addBufferTransformer(BufferTransformer transformer) {
		transformers.add(new BufferTransformerInfo(transformer));
	}


	/**
	 * Inner class BufferTransformerInfo
	 * This class keeps together a BufferTransformer and a ByteBuffer holding data already received 
	 * but not yet processed by that transformer. Such data will be used (together with newly received data)
	 * at next transformation attempt
	 */
	private class BufferTransformerInfo {
		private BufferTransformer transformer;
		private ByteBuffer unprocessedData;

		BufferTransformerInfo(BufferTransformer transformer) {
			this.transformer = transformer;
		}

		BufferTransformer getTransformer() {
			return transformer;
		}

		public void storeUnprocessedData(ByteBuffer transformationInput) {
			if (transformationInput.hasRemaining()) {
				//System.out.println("######## Storing "+transformationInput.remaining()+" bytes for next round");
				unprocessedData = ByteBuffer.allocateDirect(transformationInput.remaining());
				NIOHelper.copyAsMuchAsFits(unprocessedData, transformationInput);
				unprocessedData.flip();
			}
			else {
				// No un-processed data to store
				unprocessedData = null;
			}
		}

		public ByteBuffer attachUnprocessedData(ByteBuffer transformationInput) {
			ByteBuffer actualTransformationInput = transformationInput;
			if (unprocessedData != null && unprocessedData.hasRemaining()) {
				//System.out.println("######## Attaching "+unprocessedData.remaining()+" unprocessed bytes");
				actualTransformationInput = NIOHelper.enlargeAndFillBuffer(unprocessedData, transformationInput.remaining(), "unprocessedData");
				NIOHelper.copyAsMuchAsFits(actualTransformationInput, transformationInput);
				actualTransformationInput.flip();
			}
			return actualTransformationInput;
		}
	} // END of inner class BufferTransformerInfo

}

