/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

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


import jade.domain.FIPAAgentManagement.Envelope;


/**
   This interface is the sender's view of an MTP.
 */
public interface OutChannel {

  /**
     Delivers to the specified address an ACL message, encoded in some
     concrete message representation, using the given envelope as a
     transmission header.
     @param ta The transport address to deliver the message to. It
     must be a valid address for this MTP.
     @param env The message envelope, containing various fields
     related to message recipients, encoding, and timestamping.
     @payload The byte sequence that contains the encoded ACL message.
     @exception MTPException Thrown if some MTP delivery error occurs.
   */
  void deliver(String addr, Envelope env, byte[] payload) throws MTPException;

}
