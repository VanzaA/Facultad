package jade.imtp.leap.nio;

//#J2ME_EXCLUDE_FILE
import jade.imtp.leap.JICP.JICPPacket;
import jade.imtp.leap.JICP.JICPProtocol;
import jade.imtp.leap.http.HTTPHelper;
import jade.imtp.leap.http.HTTPRequest;
import jade.imtp.leap.http.HTTPResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Eduard Drenth: Logica, 22-sep-2009
 * 
 */
public class NIOHTTPHelper extends HTTPHelper implements BufferTransformer {

	//private boolean needToRead = false;
	NIOJICPConnection connection;
	private static Logger log = Logger.getLogger(NIOHTTPHelper.class.getName());


	public static ByteBuffer readByteBufferFromHttp(InputStream is) throws IOException {
		HTTPRequest request = new HTTPRequest();
		request.readFrom(is);
		if (is.markSupported()) {
			is.mark(2);
			if (is.read() != -1) {
				is.reset();
				throw new IOException("bytes left in stream after constructing HTTPRequest");
			}
		}
		if (request.getMethod().equals("GET")) {
			String recipientID = request.getField(RECIPIENT_ID_FIELD);
			JICPPacket pkt = new JICPPacket(JICPProtocol.CONNECT_MEDIATOR_TYPE, JICPProtocol.DEFAULT_INFO, recipientID, null);
			ByteBuffer b = ByteBuffer.allocateDirect(pkt.getLength());
			MyOut out = new MyOut(b);
			pkt.writeTo(out);
			b.flip();
			return b;
		} else {
			// Read the JICPPacket from the HTTP request payload
			byte[] a = request.getPayload();
			ByteBuffer b = ByteBuffer.allocateDirect(a.length);
			MyOut out = new MyOut(b);
			out.write(a, 0, a.length);
			b.flip();
			return b;
		}
	}
	public NIOHTTPHelper(NIOJICPConnection connection) {
		this.connection = connection;
	}

	private static ByteBuffer wrapInHttpResponse(ByteBuffer pkt) throws IOException {
		byte[] b = new byte[pkt.remaining()];
		pkt.get(b, 0, pkt.remaining());
		HTTPResponse response = wrapInHttp(b);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		response.writeTo(out);
		return ByteBuffer.wrap(out.toByteArray());
	}

	private static ByteBuffer constructJICPPacket(ByteBuffer input) throws IOException {
		byte[] out = new byte[input.remaining()];
		input.get(out, 0, input.remaining());
		if (log.isLoggable(Level.FINE)) {
			log.fine("trying to read jicp packet from http: \n" + new String(out));
		}
		return readByteBufferFromHttp(new ByteArrayInputStream(out));
	}

	public synchronized ByteBuffer postprocessBufferRead(ByteBuffer data) throws IOException {
		//needToRead = false;
		data.mark();
		try {
			return constructJICPPacket(data);
		} catch (EOFException ex) {
			if (log.isLoggable(Level.FINE)) {
				log.log(Level.FINE, "not enough data available, wait for more", ex);
			}
			//needToRead = true;
			// incomplete, wait for more data
			data.reset();
		}
		return NIOHelper.EMPTY_BUFFER;
	}

	public synchronized ByteBuffer preprocessBufferToWrite(ByteBuffer dataToSend) throws IOException {
		return wrapInHttpResponse(dataToSend);
	}

	public boolean needSocketData() {
		//return needToRead;
		return false;
	}

	private static class MyOut extends ByteArrayOutputStream {

		private ByteBuffer buffer;

		public MyOut(ByteBuffer b) {
			this.buffer = b;
		}

		@Override
		public synchronized void write(int b) {
			buffer.put((byte) b);
		}

		@Override
		public synchronized void write(byte[] b, int off, int len) {
			buffer.put(b, off, len);
		}
	}
}
