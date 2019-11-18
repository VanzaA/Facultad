/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A.

The updating of this file to JADE 2.0 has been partially supported by the
IST-1999-10211 LEAP Project

This file refers to parts of the FIPA 99/00 Agent Message Transport
Implementation Copyright (C) 2000, Laboratoire d'Intelligence
Artificielle, Ecole Polytechnique Federale de Lausanne

GNU Lesser General Public License

This library is free software; you can redistribute it sand/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation,
version 2.1 of the License.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/

/**
 * HTTPAddress.java
 *
 * @author Jose Antonio Exposito
 * @author MARISM-A Development group ( marisma-info@ccd.uab.es )
 * @version 0.1
 * @author Nicolas Lhuillier (Motorola)
 * @version 1.0
 */

package jade.mtp.http;

import jade.mtp.TransportAddress;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.InetAddress;

public class HTTPAddress implements TransportAddress {
  
  private URL url;
  
  HTTPAddress(URL addr) throws MalformedURLException {
    url = addr;
  }  
  
  HTTPAddress(String addr) throws MalformedURLException {
    url = new URL(addr);
  }

  /*#DOTNET_INCLUDE_BEGIN
  HTTPAddress(String addr, int port) throws MalformedURLException
  {
 	this(addr, port, false);
  }
  #DOTNET_INCLUDE_END*/
  
  HTTPAddress(String addr, int port, boolean https) 
    throws MalformedURLException {
        if(https){
        url = new URL("https",addr,port,"/acc");
      }else{
        url = new URL("http",addr,port,"/acc");
      }
  }
  
  /** Check if HTTP addresses are equivalent (same port, same host, same proto) */
  public boolean equals(HTTPAddress a) {
    try {
      if ((getPort().equals(a.getPort())) &&
          (InetAddress.getByName(url.getHost()).equals(InetAddress.getByName(a.getHost()))) &&
          (getProto().equals(a.getProto()))) {
        return true;
      }
    }
    catch(Exception e) {
      // Probably unknown host exception
    }
    return false;
  }
  
  /** Get the value of protocol */    
  public String getProto() {
    return url.getProtocol();
  }
  
  /** Get the value of host */
  public String getHost() {
    return url.getHost();
  }
   
  /** Get the value of port */
  public String getPort() {
    return Integer.toString(url.getPort());
  }

  /** Get the value of port */
  public int getPortNo() {
    return url.getPort();
  }
   
  /** Get the value of file */
  public String getFile() {
    return url.getFile();
  }
   
  /** Get the value of anchor */
  public String getAnchor() {
    return url.getRef();
  }   
  
  /** convert to String */
  public String toString() {
    return url.toString();
  }
  
} //End of HTTPAddress class
