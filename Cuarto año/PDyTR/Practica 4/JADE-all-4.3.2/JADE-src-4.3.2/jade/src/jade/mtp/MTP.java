/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

The updating of this file to JADE 2.0 has been partially supported by the IST-1999-10211 LEAP Project

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

package jade.mtp;


/**
   Abstract interface for Message Transport Protocols

   @author Giovanni Rimassa - Universita` di Parma
   @version $Date: 2001-10-05 15:36:54 +0200 (ven, 05 ott 2001) $ $Revision: 2757 $

*/
public interface MTP extends InChannel, OutChannel {

  /**
     Converts a string representing a valid address in this MTP to a
     <code>TransportAddress</code> object.
     @param rep The string representation of the address.
     @return A <code>TransportAddress</code> object, created from the
     given string.
     @exception MTPException If the given string is not a valid
     address according to this MTP.
   */
  TransportAddress strToAddr(String rep) throws MTPException;

  /**
     Converts a <code>TransportAddress</code> object into a string
     representation.
     @param ta The <code>TransportAddress</code> object.
     @return A string representing the given address.
     @exception MTPException If the given
     <code>TransportAddress</code> is not a valid address for this
     MTP.
   */
  String addrToStr(TransportAddress ta) throws MTPException;

  /**
     Reads the name of the message transport protocol managed by this
     MTP. The FIPA standard message transport protocols have a name
     starting with <code><b>"fipa.mts.mtp"</b></code>.
     @return A string, that is the name of this MTP.
   */
  String getName();

  String[] getSupportedProtocols();

}
