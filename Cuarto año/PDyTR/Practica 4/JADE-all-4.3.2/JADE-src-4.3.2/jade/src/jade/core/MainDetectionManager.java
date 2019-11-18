package jade.core;

import jade.util.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

//#J2ME_EXCLUDE_FILE

class MainDetectionManager {

	private final static Logger logger = Logger.getMyLogger(MainDetectionManager.class.getName());

	/*
	 *  configuration options with defaults
	 */
	private final static String OPT_MCAST_ADDR = "jade_core_MainDetectionManager_mcastaddr";
	private final static String OPT_MCAST_ADDR_DEFAULT = "239.255.10.99";

	private final static String OPT_MCAST_PORT = "jade_core_MainDetectionManager_mcastport";
	private final static String OPT_MCAST_PORT_DEFAULT = "1199";

	private final static String OPT_MCAST_TTL = "jade_core_MainDetectionManager_mcastttl";
	private final static String OPT_MCAST_TTL_DEFAULT = "4";

	private final static String OPT_MCAST_FIRST_TIMEOUT = "jade_core_MainDetectionManager_mcastfirsttimeout";
	private final static String OPT_MCAST_FIRST_TIMEOUT_DEFAULT = "500";

	private final static String OPT_MCAST_TIMEOUT = "jade_core_MainDetectionManager_mcasttimeout";
	private final static String OPT_MCAST_TIMEOUT_DEFAULT = "2500";

	private final static String OPT_MCAST_RETRIES = "jade_core_MainDetectionManager_mcastretries";
	private final static String OPT_MCAST_RETRIES_DEFAULT = "3";

	/*
	 *  protocol data
	 */
	// version string
	public final static String PROTO_VERSION = " MJADE/1.0";

	// charset used to encode/decode packets
	public final static String PROTO_ENCODING = "ISO-8859-1";

	// protocol commands
	public final static String PROTO_CMD_GETMAIN = "get-main";
	public final static String PROTO_CMD_PING = "ping";

	// constants for get-main command
	public final static String PROTO_ADDRESSES_SEPARATOR = ";";
	public final static String PROTO_ADDR_SEPARATOR = ":";

	// protocol responses
	public final static String PROTO_RESP_OK = "200 OK ";
	public final static String PROTO_RESP_ERR = "500 Internal Server Error";
	public final static String PROTO_RESP_NOTFOUND = "404 Not Found";

	// buffer size
	private final static int DGRAM_BUF_LEN = 1024;

	// source port for datagram packets
	private final static int SRC_PORT = 1198;

	private static final String MATCH_ALL_PLATFORMS = "*";

	/*
	 * inner class used to parse and keep addresses from main
	 */
	private static class MainAddr {
		public String protocol;
		public String hostname;
		public int port;

		public MainAddr(String address) {
			StringTokenizer ast;
			/*
			 * Address must be in form "proto:host:port"
			 * If address has a bad format, the following code
			 * will throw an exception. The behaviour is by design.
			 */
			ast = new StringTokenizer(address, PROTO_ADDR_SEPARATOR);
			protocol = ast.nextToken();
			hostname = ast.nextToken();
			port = Integer.parseInt(ast.nextToken());
		}
	}

	/*
	 * inner class used to get multicast parameters from Profile
	 */
	static class MulticastParams {
		String address;
		int port;
		int firstTimeout;
		int timeout;
		int retries;
		int ttl;

		private void checkTrue(boolean condition, String paramName, String paramValue) throws ProfileException {
			if (!condition) {
				throw new ProfileException("Bad value \""+paramValue+"\" for option "+paramName);
			}
		}

		private int parseInt(String paramName, String paramValue) throws ProfileException {
			try {
				return Integer.parseInt(paramValue);
			} catch (NumberFormatException nfe) {
				throw new ProfileException("Bad value \""+paramValue+"\" for option "+paramName+": integer value required", nfe);
			}
		}

		public MulticastParams(Profile p) throws ProfileException {
			String s;

			address = p.getParameter(OPT_MCAST_ADDR, OPT_MCAST_ADDR_DEFAULT);
			checkTrue(address != null && address.length() > 0, OPT_MCAST_ADDR, address);

			s = p.getParameter(OPT_MCAST_PORT, OPT_MCAST_PORT_DEFAULT);
			port = parseInt(OPT_MCAST_PORT, s);
			checkTrue(port > 0, OPT_MCAST_PORT, s);

			s = p.getParameter(OPT_MCAST_FIRST_TIMEOUT, OPT_MCAST_FIRST_TIMEOUT_DEFAULT);
			firstTimeout = parseInt(OPT_MCAST_FIRST_TIMEOUT, s);
			checkTrue(firstTimeout > 0, OPT_MCAST_FIRST_TIMEOUT, s);

			s = p.getParameter(OPT_MCAST_TIMEOUT, OPT_MCAST_TIMEOUT_DEFAULT);
			timeout = parseInt(OPT_MCAST_TIMEOUT, s);
			checkTrue(timeout >= 0, OPT_MCAST_TIMEOUT, s);

			s = p.getParameter(OPT_MCAST_RETRIES, OPT_MCAST_RETRIES_DEFAULT);
			retries = parseInt(OPT_MCAST_RETRIES, s);
			checkTrue(retries >= 0, OPT_MCAST_RETRIES, s);

			s = p.getParameter(OPT_MCAST_TTL, OPT_MCAST_TTL_DEFAULT);
			ttl = parseInt(OPT_MCAST_TTL, s);
			checkTrue(ttl > 0, OPT_MCAST_TTL, s);
		}
	}

	/*
	 * decode data using the right protocol charset and throw away trailing '\0's
	 */
	public static String decodeData(byte[] data) {
		String result = null;

		try {
			// discard all null bytes at the end of the buffer
			int i;
			for (i = data.length-1; i >= 0; i--) {
				if (data[i] != 0) {
					break;
				}
			}
			if (i > 0) {
				result = new String(data, 0, i+1, PROTO_ENCODING);
			}
		} catch (UnsupportedEncodingException uee) {
			logger.log(Logger.SEVERE, "Cannot decode data with charset "+PROTO_ENCODING, uee);
		}
		return result;
	}

	/*
	 *  check protocol version at the end of request
	 */
	public static int checkProtocolVersion(String request) throws Exception {
		int i = request.lastIndexOf(PROTO_VERSION);
		if (i < 0) {
			throw new Exception("Bad message");
		}
		if (i+PROTO_VERSION.length() != request.length()) {
			throw new Exception("Wrong protocol version");
		}
		return i;
	}

	/*
	 * extract Main address from the list got in response
	 */
	private static MainAddr extractAddress(String response, String proto) {
		logger.log(Logger.FINER, "MainDetectionManager::extractAddress(response=\""+response+"\", proto=\""+proto+"\")");
		MainAddr result = null;

		// parse a list of addresses in form proto_1:hostname_1:port_1;...;proto_n:hostname_n:port_n
		StringTokenizer st = new StringTokenizer(response, PROTO_ADDRESSES_SEPARATOR);
		String address;
		MainAddr ma;
		while (st.hasMoreTokens()) {
			address = st.nextToken();
			try {
				ma = new MainAddr(address);
				result = ma;
				/*
				 * If either no protocol specified or main has an address with
				 * desired protocol, then we are done, otherwise, go on searching
				 * for the right protocol.
				 */
				if (proto == null || proto.equals(ma.protocol)) {
					break;
				}
			} catch (Exception e) {
				// skip malformed address...
				logger.log(Logger.WARNING, "Skipping malformed address", e);
			}
		}
		return result;
	}

	/*
	 * manage responses for get-main query
	 */
	private static MainAddr manageGetMainResponses(List responses, String proto) {
		logger.log(Logger.FINER, "MainDetectionManager::manageGetMainResponses(responses.size()="+responses.size()+", proto=\""+proto+"\")");
		MainAddr mainAddress = null;

		String response;
		String s;
		Iterator iter = responses.iterator();
		byte[] data;
		InetAddress senderHost;
		int senderPort;
		while (iter.hasNext()) {
			DatagramPacket p = (DatagramPacket)iter.next();
			data = p.getData();
			senderHost = p.getAddress();
			senderPort = p.getPort();
			response = decodeData(data);

			try {
				if (response == null) {
					throw new Exception("Response cannot be decoded");
				}
				s = response;

				// first of all, check protocol version in response
				int i = checkProtocolVersion(s);
				s = s.substring(0, i);

				// then, check for errors returned by Main
				i = s.indexOf(PROTO_RESP_OK);
				if (i != 0) {
					throw new Exception("Main container returned Error in response");
				}
				s = s.substring(PROTO_RESP_OK.length());
				mainAddress = extractAddress(s, proto);
				// bail out at first good answer
				break;
			} catch (Exception e) {
				logger.log(Logger.WARNING, "Error managing response \""+response+"\" from "+senderHost+":"+senderPort+"; response discarded", e);
			}
		}
		// at last, extract Main address
		return mainAddress;
	}

	/*
	 * send multicast request, managing timeout and retries, and return decoded response
	 */
	private static List multicAsk(String request, Profile p) throws ProfileException {
		logger.log(Logger.FINER, "MainDetectionManager::multicAsk(...)");
		List result = null;
		/*
		 * usually we expect only one Main, but when more are active,
		 * we collect all responses and choose the one we like more
		 */
		List responses = new ArrayList(3);

		// parse parameters from profile
		MulticastParams mcast = new MulticastParams(p);

		logger.log(Logger.FINER, "MainDetectionManager::multicAsk(): prepared msg=\""+request+"\"");

		InetAddress mcastGroupAddress;
		try {
			mcastGroupAddress = InetAddress.getByName(mcast.address);
		} catch (UnknownHostException e) {
			throw new ProfileException("Cannot resolve address "+mcast.address, e);
		}
		if (!mcastGroupAddress.isMulticastAddress()) {
			throw new ProfileException("Address "+mcast.address+" is not a multicast address");
		}

		MulticastSocket socket = null;
		try {
			try {
				socket = new MulticastSocket(SRC_PORT);
				socket.setTimeToLive(mcast.ttl);
				socket.setSoTimeout(mcast.firstTimeout);
			} catch (IOException ioe) {
				throw new ProfileException("Error setting up multicast socket", ioe);
			}
			DatagramPacket packet;
			int retries = mcast.retries+1;
			do {
				try {
					logger.log(Logger.FINER, "MainDetectionManager::multicAsk(): sending msg =\""+request+"\" to "+mcast.address+":"+mcast.port);
					packet = new DatagramPacket(request.getBytes(PROTO_ENCODING), request.length(), mcastGroupAddress, mcast.port);
					socket.send(packet);

					do {
						try {
							// get responses
							byte[] buf = new byte[DGRAM_BUF_LEN];
							DatagramPacket recv = new DatagramPacket(buf, buf.length);
							socket.receive(recv);
							logger.log(Logger.FINER, "MainDetectionManager::multicAsk(): received "+recv.getLength()+" bytes");
							responses.add(recv);
						} catch (SocketTimeoutException ste) {
							socket.setSoTimeout(mcast.timeout);
							if (responses.size() > 0) {
								// we received at least one answer, it's enough
								break;
							}
							throw ste;
						}
					} while(true);
					if (responses.size() > 0) {
						// bail out
						break;
					}
				} catch (SocketTimeoutException ste) {
					// timeout, go for retry
					--retries;
					logger.log(Logger.FINER, "MainDetectionManager::multicAsk(): timeout, "+retries+" retries left");
				}
			} while (retries > 0);
			if (responses.size() > 0) {
				// we got at least a response!
				result = responses;
			}
		} catch (Exception e) {
			throw new ProfileException("Error during multicast querying", e);
		} finally {
			if (socket != null) {
				socket.close();
			}
		}
		return result;
	}

	/*
	 * build request for get-main command
	 */
	private static String buildGetMainRequest(String platformName, String proto) {
		// build request
		StringBuffer msg = new StringBuffer(PROTO_CMD_GETMAIN);
		if (platformName != null) {
			msg.append('@');
			msg.append(platformName);
		}
		if (proto != null) {
			msg.append(':');
			msg.append(proto);
		}
		msg.append(PROTO_VERSION);
		return msg.toString();
	}

	/*
	 * search for a main container
	 */
	private static MainAddr getMainAddress(Profile profile) throws ProfileException {
		logger.log(Logger.FINER, "MainDetectionManager::getMainAddress(...)");
		MainAddr result = null;
		// get platform name and protocol
		String platformName = profile.getParameter(Profile.PLATFORM_ID, null);
		if (platformName == null) {
			throw new ProfileException("platform id is mandatory when using automatic main detection; use \""+MATCH_ALL_PLATFORMS+"\" to match all");
		}
		if (MATCH_ALL_PLATFORMS.equals(platformName)) {
			platformName = null;
		}
		String proto = profile.getParameter(Profile.MAIN_PROTO, null);

		// build request
		String msg = buildGetMainRequest(platformName, proto);

		// send multicast query to search for a main
		List responses = multicAsk(msg, profile);
		if (responses != null) {
			// parse the response
			result = manageGetMainResponses(responses, proto);
		}
		return result;
	}

	public static void detect(ProfileImpl profile) throws ProfileException {
		logger.log(Logger.FINER, "MainDetectionManager::detect(...)");
		if (!profile.isMasterMain()) {
			MainAddr mainAddress = getMainAddress(profile);
			if (mainAddress == null) {
				// FIXME is the right exception to throw?
				throw new ProfileException("Cannot detect Main Container");
			}
			logger.log(Logger.CONFIG, "setting "+Profile.MAIN_PROTO+"="+mainAddress.protocol);
			profile.setParameter(Profile.MAIN_PROTO, mainAddress.protocol);
			logger.log(Logger.CONFIG, "setting "+Profile.MAIN_HOST+"="+mainAddress.hostname);
			profile.setParameter(Profile.MAIN_HOST, mainAddress.hostname);
			logger.log(Logger.CONFIG, "setting "+Profile.MAIN_PORT+"="+mainAddress.port);
			profile.setParameter(Profile.MAIN_PORT, Integer.toString(mainAddress.port));
		}
	}

	public static MulticastMainDetectionListener createListener(ProfileImpl profile, IMTPManager m) throws ProfileException {
		logger.log(Logger.FINER, "MainDetectionManager::export(...)");
		MulticastMainDetectionListener listener = new MulticastMainDetectionListener(profile, m);
		Thread listenerThread = new Thread(listener);
		listenerThread.start();
		return listener;
	}
}
