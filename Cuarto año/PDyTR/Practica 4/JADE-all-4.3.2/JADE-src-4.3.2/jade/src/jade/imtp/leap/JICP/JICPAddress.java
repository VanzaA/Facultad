/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/**
 * ***************************************************************
 * The LEAP libraries, when combined with certain JADE platform components,
 * provide a run-time environment for enabling FIPA agents to execute on
 * lightweight devices running Java. LEAP and JADE teams have jointly
 * designed the API for ease of integration and hence to take advantage
 * of these dual developments and extensions so that users only see
 * one development platform and a
 * single homogeneous set of APIs. Enabling deployment to a wide range of
 * devices whilst still having access to the full development
 * environment and functionalities that JADE provides.
 * Copyright (C) 2001 Telecom Italia LAB S.p.A.
 * 
 * GNU Lesser General Public License
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */

package jade.imtp.leap.JICP;

import jade.mtp.TransportAddress;

/**
 * Class declaration
 * @author Giovanni Caire - TILAB
 */
public class JICPAddress implements TransportAddress {
  protected String host;
  protected String port;
  protected String file;
  protected String anchor;

  /**
   * Constructor declaration
   */
  public JICPAddress() {
  } 

  /**
   * Constructor declaration
   * @param host
   * @param port
   * @param file
   * @param anchor
   */
  public JICPAddress(String host, String port, String file, String anchor) {
    this.host = host;
    this.port = (port != null ? port : String.valueOf(JICPProtocol.DEFAULT_PORT));
    this.file = file;
    this.anchor = anchor;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getProto() {
    return JICPProtocol.NAME;
  } 

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getHost() {
    return host;
  } 

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getPort() {
    return port;
  } 

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getFile() {
    return file;
  } 

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getAnchor() {
    return anchor;
  } 

  public boolean equals(Object rhs) {
      if(rhs instanceof JICPAddress) {
	  return toString().equals(rhs.toString());
      }
      else {
	  return false;
      }
  }
  
  public String toString() {
    StringBuffer address = new StringBuffer();

    address.append(JICPProtocol.NAME);
    address.append("://");
    address.append(host);

    if (port != null) {
      address.append(":");
      address.append(port);
    } 

    if (file != null) {
      address.append("/");
      address.append(file);
    } 

    if (anchor != null) {
      address.append("#");
      address.append(anchor);
    } 

    return address.toString();
  }
}

