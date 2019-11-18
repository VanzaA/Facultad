package jade.util;

//#MIDP_EXCLUDE_FILE

import jade.core.IMTPException;
import jade.core.IMTPManager;
import jade.core.Profile;
import jade.mtp.TransportAddress;

/**
 * This class wraps a Transport Address and implements the equals() and hashCode() methods
 * so that two Transport Addresses representing the same address, even if possibly expressed 
 * in different forms, are actually considered equals. 
 * @author 00917536
 *
 */
public class TransportAddressWrapper {
	private TransportAddress myTA;
	private int hashCode;
	
	public TransportAddressWrapper(TransportAddress t) {
		myTA = t;
		String host = myTA.getHost();
		if (host != null) {
			if (Profile.isLocalHost(host)) {
				hashCode = Profile.LOCALHOST_CONSTANT.hashCode();
			}
			else {
				// Since it is difficult to generate a suitable hashCode that ensures
				// that if two stringified hosts represent the same host the hashCode-s are equals, 
				// while if they represent different hosts very likely the hashCode-s are different,
				// we use a conservative approach: if the host represents a remote host, it 
				// does not contribute to the hashCode generation. This makes hash-based searches
				// un-efficient, but guarantees no search fails 
				hashCode = 0;
			}
		}
		hashCode += (myTA.getProto() != null ? myTA.getProto().hashCode() : 0);
		hashCode += (myTA.getPort() != null ? myTA.getPort().hashCode() : 0);
	}
	
	public static TransportAddressWrapper getWrapper(String stringifiedTransportAddress, IMTPManager imtpManager) throws IMTPException {
		return new TransportAddressWrapper(imtpManager.stringToAddr(stringifiedTransportAddress));
	}
	
	public TransportAddress getAddress() {
		return myTA;
	}
	
	public int hashCode() {
		return hashCode;
	}
	
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		
		TransportAddress otherTA = null;
		if (obj instanceof TransportAddressWrapper) {
			TransportAddressWrapper taw = (TransportAddressWrapper) obj;
			otherTA = taw.getAddress();
		}
		else if (obj instanceof TransportAddress) {
			otherTA = (TransportAddress) obj; 
		}
		else {
			return false;
		}
		return Profile.compareTransportAddresses(myTA, otherTA);
	}
	
	public String toString() {
		return "W["+myTA+"]";
	}
}
