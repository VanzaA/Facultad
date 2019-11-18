package jade.imtp.leap.http;

//#J2ME_EXCLUDE_FILE

/**
 *
 * @author Eduard Drenth: Logica, 12-jul-2009
 * 
 */
public class HTTPSAddress extends HTTPAddress {

  /**
   * Constructor declaration
   * @param host
   * @param port
   * @param file
   * @param anchor
   */
  public HTTPSAddress(String host, String port, String file, String anchor) {
      super(host,(port != null ? port : String.valueOf(HTTPSProtocol.DEFAULT_PORT)),file,anchor);
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getProto() {
    return HTTPSProtocol.NAME;
  }

}
