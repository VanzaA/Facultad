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

import jade.core.AID;
import jade.core.ContainerID;
import jade.core.Channel;
import jade.lang.acl.ACLMessage;

/**
   This class represents the events related to ACL message passing.

   @author Giovanni Rimassa - Universita` di Parma
   @version $Date: 2004-10-05 10:35:16 +0200 (mar, 05 ott 2004) $ $Revision: 5412 $

 */
public class MessageEvent extends JADEEvent {

  public static final int SENT_MESSAGE = 1;
  public static final int POSTED_MESSAGE = 2;
  public static final int RECEIVED_MESSAGE = 3;
  public static final int ROUTED_MESSAGE = 4;

  private ACLMessage message = null;
  private AID sender = null;
  private AID receiver = null;
  private Channel from = null;
  private Channel to = null;

  public MessageEvent(int id, ACLMessage msg, AID s, AID r, ContainerID cid) {
    super(id, cid);
    if(isRouting()) {
      throw new InternalError("Bad event kind: it must not be a 'message-routed' event.");
    }
    message = msg;
    sender = s;
    receiver = r;
  }

  public MessageEvent(int id, ACLMessage msg, Channel f, Channel t, ContainerID cid) {
    super(id, cid);
    if(!isRouting()) {
      throw new InternalError("Bad event kind: it must be a 'message-routed' event.");
    }
    message = msg;
    from = f;
    to = t;
  }

  public ACLMessage getMessage() {
    return message;
  }

  public AID getSender() {
  	if (sender != null) {
	    return sender;
  	}
  	else {
  		return message.getSender();
  	}
  }

  public AID getReceiver() {
    return receiver;
  }
  
  public AID getAgent() {
  	if (type == SENT_MESSAGE) {
  		return getSender();
  	}
  	else {
  		return getReceiver();
  	}
  }

  public Channel getFrom() {
    return from;
  }

  public Channel getTo() {
    return to;
  }

  public boolean isRouting() {
    return type == ROUTED_MESSAGE;
  }

}
