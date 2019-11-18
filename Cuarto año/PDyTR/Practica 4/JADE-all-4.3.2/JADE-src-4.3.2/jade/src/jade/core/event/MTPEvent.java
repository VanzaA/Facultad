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

package jade.core.event;

import jade.core.ContainerID;
import jade.core.Channel;

import jade.domain.FIPAAgentManagement.Envelope;

/**
   This class represents an event related to the MTP configuration.

   @author Giovanni Rimassa - Universita` di Parma
   @version $Date: 2002-08-28 17:14:13 +0200 (mer, 28 ago 2002) $ $Revision: 3354 $
 */
public class MTPEvent extends JADEEvent {

  public static final int ADDED_MTP = 1;
  public static final int REMOVED_MTP = 2;
  public static final int MESSAGE_IN = 3;
  public static final int MESSAGE_OUT = 4;

  //private int myID; // The actual type of the event
  private Channel chan;
  private Envelope env;
  private byte[] payload;

  public MTPEvent(int id, ContainerID cid, Channel ch) {
    super(id, cid);
    //myID = id;
    if(!isInstall()) {
      throw new InternalError("Bad event kind: it must be an MTP installation related kind.");
    }
    chan = ch;
    env = null;
    payload = null;
  }

  public MTPEvent(int id, ContainerID cid, Envelope e, byte[] pl) {
    super(id, cid);
    //myID = id;
    if(!isCommunication()) {
      throw new InternalError("Bad event kind: it must be a communication related kind.");
    }
    chan = null;
    env = e;
    payload = pl;
  }

  public Channel getChannel() {
    return chan;
  }

  public Envelope getEnvelope() {
    return env;
  }

  public byte[] getPayload() {
    return payload;
  }

  public boolean isInstall() {
    return (type == ADDED_MTP) || (type == REMOVED_MTP);
  }

  public boolean isCommunication() {
    return (type == MESSAGE_IN) || (type == MESSAGE_OUT);
  }

  // Nothing to add to superclass

}
