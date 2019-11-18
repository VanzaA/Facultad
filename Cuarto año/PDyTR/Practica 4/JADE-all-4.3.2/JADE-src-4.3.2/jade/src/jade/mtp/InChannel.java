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
import jade.core.Profile;

/**
   This interface is the receiver's view of an MTP.
 */
public interface InChannel {

  /**
     Callback interface to be notified of message arrivals over this
     Message Transport Protocol.
   */
  public static interface Dispatcher {
    void dispatchMessage(Envelope env, byte[] payload);
  }

  /**
     Activates an MTP handler for incoming messages on a default
     address.
		 @parameter p is the Profile from which the configuration parameters
		 for this instance of JADE container can be retrieved
     @return A <code>TransportAddress</code>, corresponding to the
     chosen default address.
     @exception MTPException Thrown if some MTP initialization error
     occurs.
  */
  TransportAddress activate(Dispatcher disp, Profile p) throws MTPException;

  /**
     Activates an MTP handler for incoming messages on a specific
     address.
     @param ta A <code>TransportAddress</code> object, representing
     the transport address to listen to.
		 @parameter p is the Profile from which the configuration parameters
		 for this instance of JADE container can be retrieved
     @exception MTPException Thrown if some MTP initialization error
     occurs.
   */
  void activate(Dispatcher disp, TransportAddress ta, Profile p) throws MTPException;

  /**
     Deactivates the MTP handler listening at a given transport
     address.
     @param ta The <code>TransportAddress</code> object the handle to
     close is listening to.
     @exception MTPException Thrown if some MTP cleanup error occurs.
   */
  void deactivate(TransportAddress ta) throws MTPException;

  /**
     Deactivates all the MTP handlers.
     @exception MTPException Thrown if some MTP cleanup error occurs.
   */
  void deactivate() throws MTPException;

}
