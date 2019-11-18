package jade.imtp.leap.http;

//#J2ME_EXCLUDE_FILE

import jade.imtp.leap.ICPException;
import jade.mtp.TransportAddress;

/**
 *
 * @author Eduard Drenth: Logica, 12-jul-2009
 * 
 */
public class HTTPSProtocol extends HTTPProtocol {
  /**
   * The protocol's name, as used in a URL protocol field.
   */
  public static final String NAME = "https";
  public static final int    DEFAULT_PORT = 443;

  private static HTTPSProtocol theInstance = new HTTPSProtocol();

  public static HTTPSProtocol getInstance() {
  	return theInstance;
  }

  /**
   */
  public String addrToString(TransportAddress ta) throws ICPException {

    // Check that the specified ta is actually a JICP address
    HTTPSAddress hta = null;

    try {
      hta = (HTTPSAddress) ta;
    }
    catch (ClassCastException cce) {
      throw new ICPException("The TransportAddress "+ta.toString()+" is not an HTTPS Address");
    }

    return hta.toString();
  }
  /**
   */
  public TransportAddress buildAddress(String host, String port, String file, String anchor) {
    return new HTTPSAddress(host, port, file, anchor);
  }

}
