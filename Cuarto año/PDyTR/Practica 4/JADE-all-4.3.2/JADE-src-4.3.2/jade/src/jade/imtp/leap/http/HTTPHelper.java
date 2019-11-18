package jade.imtp.leap.http;

import jade.imtp.leap.JICP.JICPPacket;
import jade.imtp.leap.JICP.JICPProtocol;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Eduard Drenth: Logica, 21-sep-2009
 * 
 */
public class HTTPHelper {
	public static final String RECIPIENT_ID_FIELD = "recipient-id";

	public HTTPHelper() {
	}

	//#MIDP_EXCLUDE_BEGIN
	/**
	 * @see HTTPHelper#readPacketFromHttp(java.io.InputStream)
	 */
	public static JICPPacket readPacketFromHttp(byte[] input) throws IOException {
		return readPacketFromHttp(new ByteArrayInputStream(input));
	}

	public static JICPPacket readPacketFromHttp(InputStream is) throws IOException {
		HTTPRequest request = new HTTPRequest();
		request.readFrom(is);
		if (request.getMethod().equals("GET")) {
			// This is a CONNECT_MEDIATOR
			String recipientID = request.getField(RECIPIENT_ID_FIELD);
			JICPPacket pkt = new JICPPacket(JICPProtocol.CONNECT_MEDIATOR_TYPE, JICPProtocol.DEFAULT_INFO, recipientID, null);
			return pkt;
		} else {
			// Read the JICPPacket from the HTTP request payload
			ByteArrayInputStream bis = new ByteArrayInputStream(request.getPayload());
			return JICPPacket.readFrom(bis);
		}
	}
	public static HTTPResponse wrapInHttp(byte[] jicpPacket) throws IOException {
		// Create an HTTPResponse and set the serialized JICPPacket as payload
		HTTPResponse response = new HTTPResponse();
		response.setCode("200");
		response.setMessage("OK");
		response.setHttpType("HTTP/1.1");
		response.setPayload(jicpPacket);
		return response;
	}

	public static HTTPResponse wrapInHttp(JICPPacket pkt) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream(pkt.getLength());
		pkt.writeTo(os);
		return wrapInHttp(os.toByteArray());
	}
	//#MIDP_EXCLUDE_END
}
