package jade.imtp.leap.nio;

//#J2ME_EXCLUDE_FILE

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * A buffertransformer is responsible for the preparation of data read from a socket or data to be written to a socket. Implementers are
 * responsible for preparing ({@link ByteBuffer#flip() }) the data returned.
 * @author Eduard Drenth: Logica, 24-sep-2009
 *
 */
public interface BufferTransformer {

    /**
     * implementers can transform data available read from the socket. Any data left in the socketData argument will be used in subsequent
     * {@link NIOJICPConnection#readPacket() } calls.
     * @param socketData
     * @return a buffer containing the transformed data
     * @throws IOException will be handled by the {@link BEManagementService} and if applicable {@link NIOMediator}
     */
    public ByteBuffer postprocessBufferRead(ByteBuffer socketData) throws IOException;

    /**
     * implementers can transform the bytes before they are sent. When data in the dataToSend argument are left a IOException will be thrown by
     * {@link NIOJICPConnection#writePacket(jade.imtp.leap.JICP.JICPPacket) }.
     * @param dataToSend
     * @return the ByteBuffer to send
     * @throws IOException will be handled by the {@link BEManagementService} and if applicable {@link NIOMediator}
     */
    public ByteBuffer preprocessBufferToWrite(ByteBuffer dataToSend) throws IOException;


}
