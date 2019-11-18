package jade.core;

import jade.core.MainDetectionManager.MulticastParams;
import jade.mtp.TransportAddress;
import jade.util.Logger;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import jade.util.leap.List;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

//#J2ME_EXCLUDE_FILE

class MulticastMainDetectionListener implements Runnable {

	private final static Logger logger = Logger.getMyLogger(MulticastMainDetectionListener.class.getName());

	private final static int DGRAM_BUF_LEN = 1024;

	private ProfileImpl profile;
	private IMTPManager manager;
	private MulticastParams mcast;
	private InetAddress mcastGroupAddress;
	private MulticastSocket socket;
	private boolean active;
	

	public MulticastMainDetectionListener(ProfileImpl profile, IMTPManager manager) throws ProfileException {
		active = false;
		this.profile = profile;
		this.manager = manager;
		mcast = new MainDetectionManager.MulticastParams(profile);
		try {
			mcastGroupAddress = InetAddress.getByName(mcast.address);
		} catch (UnknownHostException e) {
			throw new ProfileException("Cannot resolve address "+mcast.address, e);
		}
		if (!mcastGroupAddress.isMulticastAddress()) {
			throw new ProfileException("Address "+mcast.address+" is not a multicast address");
		}
		try {
			socket = new MulticastSocket(mcast.port);
	
			socket.joinGroup(mcastGroupAddress);
			socket.setTimeToLive(mcast.ttl);
		} catch (IOException ioe) {
			throw new ProfileException("Error setting up multicast socket", ioe);
		}
	}

	private String errorResponse(String code, String msg) {
		return code+" "+msg;
	}

	void stop() {
		if (active) {
			active = false;
			if (socket != null) {
				try {
					socket.leaveGroup(mcastGroupAddress);
				} catch (IOException e) {
					logger.log(Logger.FINER, "Error leaving multicast group", e);
				}
				socket.close();
			}
		}
	}

	private String serveGetMain(String request) throws IMTPException {
		logger.log(Logger.FINER, "MulticastMainDetectionListener::serveGetMain(request=\""+request+"\")");
		String response = null;
		int i;
		String proto = null;
		String platformName = null;

		// request is in form:
		//     get-main[@platform_name][:protocol_name]

		// does request contain a desired protocol?
		i = request.indexOf(':');
		if (i > 0) {
			proto = request.substring(i+1);
			request = request.substring(0, i);
			logger.log(Logger.FINER, "MulticastMainDetectionListener::serveGetMain(): desired proto is \""+proto+"\"");
		}

		// does request contain a platform name?
		i = request.indexOf('@');
		if (i > 0) {
			platformName = request.substring(i+1);
			request = request.substring(0, i);
			logger.log(Logger.FINER, "MulticastMainDetectionListener::serveGetMain(): request is for platform \""+platformName+"\"");
		}

		String myPlatform = profile.getParameter(Profile.PLATFORM_ID, null);
		if (platformName != null && !platformName.equals(myPlatform)) {
			// not my platform, bail out with no response
			logger.log(Logger.FINER, "MulticastMainDetectionListener::serveGetMain(): my platform is \""+myPlatform+"\" while request is for platform \""+platformName+"\" --> Do not reply");
			return null;
		}

		List addresses = manager.getLocalAddresses();
		List responseAddresses = new ArrayList(addresses.size());

		Iterator iter = addresses.iterator();
		TransportAddress addr;
		while (iter.hasNext()) {
			addr = (TransportAddress)iter.next();
			if (proto != null) {
				if (proto.equals(addr.getProto())) {
					responseAddresses.add(addr);
					break;
				}
			} else {
				responseAddresses.add(addr);
			}
		}

		if (responseAddresses.size() < 1) {
			response = errorResponse(MainDetectionManager.PROTO_RESP_NOTFOUND, "Cannot manage protocol "+proto);
		} else {
			response = MainDetectionManager.PROTO_RESP_OK;
			iter = responseAddresses.iterator();
			while (iter.hasNext()) {
				addr = (TransportAddress)iter.next();
				// FIXME use toString()
				//response += addr.toString();
				response += addr.getProto()+MainDetectionManager.PROTO_ADDR_SEPARATOR+addr.getHost()+MainDetectionManager.PROTO_ADDR_SEPARATOR+addr.getPort();
				if (iter.hasNext()) {
					response += MainDetectionManager.PROTO_ADDRESSES_SEPARATOR;
				}
			}
		}
		return response;
	}

	private String executeRequest(String request) throws Exception {
		logger.log(Logger.FINER, "MulticastMainDetectionListener::executeRequest(request=\""+request+"\")");
		String result = null;
		if (request.indexOf(MainDetectionManager.PROTO_CMD_GETMAIN) == 0) {
			// command [get main]
			if (profile.isMasterMain()) {
				// only master main replies to [get main] commands
				result = serveGetMain(request);
			} else {
				logger.log(Logger.FINER, "MulticastMainDetectionListener::executeRequest(): I'm not master --> Do not reply");
			}
		} else if (request.indexOf(MainDetectionManager.PROTO_CMD_PING) == 0) {
			throw new Exception("Command not implemented yet");
		} else {
			throw new Exception("Command not implemented");
		}
		return result;
	}

	private String manageRequest(byte[] requestBuffer) {
		logger.log(Logger.FINER, "MulticastMainDetectionListener::manageRequest(...)");
		String result = null;
		String request;
		request = MainDetectionManager.decodeData(requestBuffer);
		if (request == null) {
			throw new RuntimeException("Error decoding request");
		}
		try {
			logger.log(Logger.FINER, "MulticastMainDetectionListener::manageRequest(): request.length()="+request.length());
			if (request.length() <= MainDetectionManager.PROTO_VERSION.length()) {
				throw new Exception("Bad request [request=\""+request+"\"");
			}

			int i = MainDetectionManager.checkProtocolVersion(request);
			request = request.substring(0, i);
			result = executeRequest(request);
		} catch (Exception e) {
			throw new RuntimeException("Error managing request \""+request+"\"", e);
		}
		return result;
	}

	public void run() {
		logger.log(Logger.FINE, "MulticastMainDetectionListener::run()");
		try {
			String response;
			InetAddress clientAddr;
			int responsePort;
			active = true;
			while (true) {
				try {
					byte[] buf = new byte[DGRAM_BUF_LEN];
					DatagramPacket packet = new DatagramPacket(buf, buf.length);
					try {
						socket.receive(packet);
					} catch (IOException ioe) {
						/*
						 * when stop() is called, we get here with a
						 * SocketException: Socket closed and active == false
						 */
						if (active) {
							/*
							 * if active is still true, then something went wrong
							 */
							logger.log(Logger.SEVERE, "Error in receive()", ioe);
						}
						// in both cases, it's better to get outta here
						break;
					}
					logger.log(Logger.FINER, "MulticastMainDetectionListener::run(): "+packet.getLength()+" bytes received");
					try {
						response = manageRequest(packet.getData());
					} catch (Exception e) {
						logger.log(Logger.WARNING, "MulticastMainDetectionListener::run(): error managing request", e);
						response = errorResponse(MainDetectionManager.PROTO_RESP_ERR, e.getMessage());
					}

					if (response != null) {
						response += MainDetectionManager.PROTO_VERSION;
						buf = response.getBytes(MainDetectionManager.PROTO_ENCODING);
		
						// get client info
						clientAddr = packet.getAddress();
						responsePort = packet.getPort();
		
						// prepare packet to be sent back return to client
						packet = new DatagramPacket(buf, buf.length, clientAddr, responsePort);

						logger.log(Logger.FINER, "MulticastMainDetectionListener::run(): sending response \""+response+"\" to "+clientAddr+":"+responsePort);
						socket.send(packet);
					} else {
						logger.log(Logger.FINE, "MulticastMainDetectionListener::run(): discarded request, sending no response");
					}
				} catch(IOException ioe) {
					logger.log(Logger.WARNING, "Input-output error", ioe);
				}
			}
		} catch (Exception e) {
			logger.log(Logger.SEVERE, "Error in listener thread, MulticastMainDetectionListener is no more active", e);
		}
	}

}
