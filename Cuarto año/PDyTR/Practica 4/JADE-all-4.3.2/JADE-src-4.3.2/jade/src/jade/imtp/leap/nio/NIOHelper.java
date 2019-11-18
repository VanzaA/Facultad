/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jade.imtp.leap.nio;

//#J2ME_EXCLUDE_FILE

import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * static helper for ssl/nio related handshaking/input/output
 * @author eduard
 */
public class NIOHelper {
	public static final ByteBuffer EMPTY_BUFFER = ByteBuffer.allocateDirect(0);

	private NIOHelper() {
	}

	private static final Logger log = Logger.getLogger(NIOHelper.class.getName());

	/**
	 * logs info on a bytebuffer at level FINE with name "unknown"
	 * @param b
	 */
	public static void logBuffer(ByteBuffer b) {
		logBuffer(b, "unknown");
	}

	/**
	 * logs info on a bytebuffer at level FINE with name &lt;name&gt;
	 * @param b
	 */
	public static void logBuffer(ByteBuffer b, String name) {
		logBuffer(b, name, Level.FINE);
	}

	/**
	 * logs info on a bytebuffer at level &lt;l&gt; with name &lt;name&gt;
	 * @param b
	 */
	public static void logBuffer(ByteBuffer b, String name, Level l) {
		if (log.isLoggable(l)) {
			log.log(l, "bufferinfo {0}: pos {1}, rem {2}, lim {3}, cap {4}", new Object[]{name, b.position(), b.remaining(), b.limit(), b.capacity()});
		}
	}

	/**
	 * copy data from src to dst, as much as fits in dst. src's position() will be moved
	 * when data are copied.
	 * @param src copy from
	 * @param dst copy to
	 * @return number of bytes copied
	 */
	public static int copyAsMuchAsFits( ByteBuffer dst, ByteBuffer src) {
		if (dst.hasRemaining() && src.hasRemaining()) {
			// current position in dst
			int pos = dst.position();

			// read from src as much as fits in dst
			int limit = src.limit();
			if (src.remaining() > dst.remaining()) {
				// data from src does not fit, set limit so that data will fit
				if (log.isLoggable(Level.FINE)) {
					log.fine("setting limit of src buffer to " + (src.position() + dst.remaining()));
				}
				src.limit(src.position() + dst.remaining());
			}

			dst.put(src);

			// reset limit, to make rest of data available to put in payload buffer
			src.limit(limit);

			if (log.isLoggable(Level.FINE)) {
				log.fine("bytes copied to dst " + (dst.position() - pos) + ", bytes left in src " + src.remaining());
				logBuffer(src, "src");
				logBuffer(dst, "dst");
			}
			// return number of data read into dst
			return dst.position() - pos;
		} else {
			return 0;
		}
	}

	/**
	 * calls {@link NIOHelper#enlargeBuffer(java.nio.ByteBuffer, int, java.lang.String, boolean) } with false for doLog
	 * @param b
	 * @param extraSpace
	 * @param name the name of the buffer
	 * @return the new enlarged buffer
	 */
	public static ByteBuffer enlargeBuffer(ByteBuffer b, int extraSpace, String name) {
		return enlargeBuffer(b, extraSpace, name, false);
	}

	/**
	 * returns an enlarged, empty buffer
	 * @param b
	 * @param extraSpace
	 * @param name the name of the buffer
	 * @param doLog log the changes or not
	 * @return the new enlarged buffer
	 */
	public static ByteBuffer enlargeBuffer(ByteBuffer b, int extraSpace, String name, boolean doLog) {
		if (!doLog) {
			return ByteBuffer.allocateDirect(b.capacity() + extraSpace);
		} else {
			ByteBuffer bigger = ByteBuffer.allocateDirect(b.capacity() + extraSpace);
			logBuffer(b,String.format("before resize %s",name),Level.WARNING);
			logBuffer(bigger,String.format("after resize %s",name),Level.WARNING);
			return bigger;
		}

	}

	/**
	 * returns an enlarged, filled with bytes from the buffer argument
	 * @param b
	 * @param extraSpace
	 * @return the new enlarged buffer
	 */
	public static ByteBuffer enlargeAndFillBuffer(ByteBuffer b, int extraSpace, String name) {
		ByteBuffer bigger = enlargeBuffer(b, extraSpace, name,false);
		bigger.put(b);
		logBuffer(b,String.format("before resize %s",name),Level.WARNING);
		logBuffer(bigger,String.format("after resize %s",name),Level.WARNING);
		return bigger;
	}
}
