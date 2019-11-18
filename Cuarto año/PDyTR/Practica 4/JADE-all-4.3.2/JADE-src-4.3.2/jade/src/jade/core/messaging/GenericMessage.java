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


package jade.core.messaging;

import jade.domain.FIPAAgentManagement.Envelope;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Serializable;

import jade.security.JADEPrincipal;
import jade.security.Credentials;

//#MIDP_EXCLUDE_BEGIN
import jade.lang.acl.LEAPACLCodec;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
//#MIDP_EXCLUDE_END

/**
 * Generic class to manage a unified representation of messages
 * (ACLMessage or Payload+Envelope)
 *
 * @author Jerome Picault - Motorola Labs
 * @version $Date: 2011-06-06 10:34:35 +0200(lun, 06 giu 2011) $ $Revision: 6416 $
 * 
 */
public class GenericMessage implements Serializable {

	private transient ACLMessage msg;
	private Envelope env;
	private byte[] payload;
	private transient AID sender;
	private transient JADEPrincipal senderPrincipal;
	private transient Credentials senderCredentials;
	private boolean isAMSFailure = false;
	private transient boolean foreignReceiver = false;
	private transient boolean modifiable = true;
	private String traceID = null;
	private long timeStamp = -1;

	public GenericMessage(){
	}

	public GenericMessage(ACLMessage msg){
		this.msg = msg;
	}

	public GenericMessage(Envelope env, byte[] payload){
		this.env = env;
		this.payload = payload;
	}

	public final byte[] getPayload(){
		return payload;
	}

	public final Envelope getEnvelope(){
		return env;
	}

	public final ACLMessage getACLMessage(){
		return msg;
	}

	public final void setACLMessage(ACLMessage msg){
		this.msg = msg;
	}

	public final void update(ACLMessage msg, Envelope env, byte[]payload){
		this.msg = msg;
		this.env = env;
		this.payload = payload;
	}

	final void setSenderPrincipal(JADEPrincipal senderPrincipal) {
		this.senderPrincipal = senderPrincipal;
	}

	final JADEPrincipal getSenderPrincipal() {
		return senderPrincipal;
	}

	final void setSenderCredentials(Credentials senderCredentials) {
		this.senderCredentials = senderCredentials;
	}

	final Credentials getSenderCredentials() {
		return senderCredentials;
	}

	public final boolean isAMSFailure() {
		return isAMSFailure;
	}

	public final void setAMSFailure(boolean b) {
		isAMSFailure=b;
	}

	final boolean hasForeignReceiver() {
		return foreignReceiver;
	}

	final void setForeignReceiver(boolean b) {
		foreignReceiver = b;
	}

	public final boolean isModifiable() {
		return modifiable;
	}

	/**
	 * Allow setting the value of the <code>modifiable</code> flag. If this flag is true (default)
	 * the MessageManager, when receiving a GenericMessage with the payload already set, nullifies the content slot
	 * of the embedded ACLMessage to save space. 
	 */
	public final void setModifiable(boolean b) {
		modifiable = b;
	}

	public final String getTraceID() {
		return traceID;
	}

	public final void setTraceID(String id) {
		traceID = id;
	}
	
	public final long getTimeStamp() {
		return timeStamp;
	}
	
	public final void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp; 
	}

	//#MIDP_EXCLUDE_BEGIN
	private void writeObject(ObjectOutputStream out) throws IOException {
		// Updates the payload if not present, before serialising
		if (payload==null){
			payload = (new LEAPACLCodec()).encode(msg, null);
		}
		out.defaultWriteObject();
	}
	//#MIDP_EXCLUDE_END

	public final void setSender(AID sender){
		this.sender = sender;
	}
	
	public final AID getSender(){
		if (sender != null) {
			return sender;
		}
		else if (msg!=null) {
			return msg.getSender();
		}
		else if (env!=null) {
			return env.getFrom();
		}
		else {
			return null;
		}
	}

	// DEBUG
	public String toString(){
		return "GenericMessage\n\t"+msg+"\n\t"+env+"\n\t"+((payload==null)?"null payload":payload.toString())+"\n";
	}

	public final int length() {
		int length = 0;
		if (payload != null) {
			length = payload.length;
		}
		else {
			if (msg != null) {
				byte[] content = msg.getByteSequenceContent();
				if (content != null) {
					length = content.length;
				}
			}
		}
		return length;
	}
}
