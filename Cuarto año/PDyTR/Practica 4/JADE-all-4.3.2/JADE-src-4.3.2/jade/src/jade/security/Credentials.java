/*****************************************************************
 JADE - Java Agent DEvelopment Framework is a framework to develop
 multi-agent systems in compliance with the FIPA specifications.
 Copyright (C) 2002 TILAB S.p.A.
 GNU Lesser General Public License
 This library is free software; you can redistribute it and/or
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

package jade.security;

import java.util.Enumeration;
import java.io.IOException;

/**
 * This is a collection of Objects that can be used in order 
 * to get privileges to perform an action or access a service.
 * 
   @author Giosue Vitaglione - TILAB
   @version $Date: 2004-05-26 09:35:55 +0200 (mer, 26 mag 2004) $ $Revision: 5086 $
 */
public interface Credentials extends jade.util.leap.Serializable {
  
  /**
   *  Used to retrieve all the contained credentials. 
   */
  Enumeration elements();
  
  /** 
   * Look into the various credentials, if there is one that
   * certify the ownership for this credentials, return the JADEPrincipal
   * of the owner. Otherwise return null.
   */
  JADEPrincipal getOwner();
   
  /**
   * Encode the Credentials into a byte array, so that it can be transported in an ACLMessage.
   * The encoding format is:
   * Credential class | Encoding size | Encoding 
   */
  byte[] encode() throws IOException;
  
  /**
   * Returns a new Credentials from the encoded information. 
   */
  Credentials decode(byte[] enc) throws IOException;
  

}
