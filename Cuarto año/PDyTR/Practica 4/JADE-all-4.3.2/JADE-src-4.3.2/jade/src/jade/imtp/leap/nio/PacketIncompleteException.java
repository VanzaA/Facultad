package jade.imtp.leap.nio;

//#J2ME_EXCLUDE_FILE

import java.io.IOException;

class PacketIncompleteException extends IOException {

	public PacketIncompleteException(String message) {
		super(message);
	}
}