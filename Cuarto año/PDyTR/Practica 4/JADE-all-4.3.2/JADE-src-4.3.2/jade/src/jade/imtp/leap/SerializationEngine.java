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

package jade.imtp.leap;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.LEAPACLCodec;

import java.io.*;

/**
 * Transform commands to/from sequences of bytes
 * @author Giovanni Caire - TILAB
 * @author Jerome Picault - Motorola Labs
 */
class SerializationEngine {
	private static final byte NULL_ID = 0;
	private static final byte STRING_ID = 1;
	private static final byte ACL_ID = 2;
	private static final byte STRING_ARRAY_ID = 3;
	private static final byte BOOLEAN_ID = 4;
	private static final byte INTEGER_ID = 5;
	private static final byte AID_ID = 6;

	final static byte[] serialize(Command cmd) throws LEAPSerializationException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.writeByte(cmd.getCode());
			int paramCnt = cmd.getParamCnt();
			dos.writeByte(paramCnt);
			for (int i = 0; i < paramCnt; ++i) {
				serializeObject(cmd.getParamAt(i), dos);
			}
			byte[] bb = baos.toByteArray();
			//Logger.println("Serialized command. Type = "+cmd.getCode()+". Length = "+(bb != null ? bb.length : 0));
			return bb;
		} 
		catch (IOException ioe) {
			throw new LEAPSerializationException("Error serializing Command");
		}
	}

	final static Command deserialize(byte[] data) throws LEAPSerializationException {
		try { 
			DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
			int type = (int) dis.readByte();
			Command cmd = new Command(type);
			int paramCnt = (int) dis.readByte();
			for (int i = 0; i < paramCnt; ++i) {
				cmd.addParam(deserializeObject(dis, data));
			} 
			//Logger.println("De-serialized command. Type = "+cmd.getCode()+". Length = "+(data != null ? data.length : 0));
			return cmd;
		} 
		catch (Exception e) {
			throw new LEAPSerializationException("Error deserializing Command "+e);
		}
	}

	/**
	 * Writes an object whose class is not known from the context to
	 * a given DataOutputStream.
	 * @param o the object to be written.
	 * @param dos the DataOutputStream.
	 * @exception LEAPSerializationException if an error occurs during
	 * serialization or the object is an instance of a class that cannot be
	 * serialized.
	 */
	private final static void serializeObject(Object o, DataOutputStream dos) throws LEAPSerializationException {
		try {
			if (o != null) {
				if (o instanceof String) {            // String
					dos.writeByte(STRING_ID);
					dos.writeUTF((String) o);
				} 
				else if (o instanceof ACLMessage) {   // ACLMessage
					dos.writeByte(ACL_ID);
					LEAPACLCodec.serializeACL((ACLMessage) o, dos);
				} 
				else if (o instanceof AID) {   // AID
					dos.writeByte(AID_ID);
					LEAPACLCodec.serializeAID((AID) o, dos);
				} 
				else if (o instanceof String[]) {     // Array of Strings
					dos.writeByte(STRING_ARRAY_ID);
					serializeStringArray((String[]) o, dos);
				} 
				else if (o instanceof Boolean) {      // Boolean
					dos.writeByte(BOOLEAN_ID);
					dos.writeBoolean(((Boolean) o).booleanValue());
				} 
				else if (o instanceof Integer) {      // Integer
					dos.writeByte(INTEGER_ID);
					dos.writeInt(((Integer) o).intValue());
				} 
				else {
					throw new LEAPSerializationException("Unknown class "+o.getClass().getName());
				}
			}
			else {
				dos.writeByte(NULL_ID);
			}
		}  // END of try
		catch (IOException ioe) {
			throw new LEAPSerializationException("I/O Error Serializing object "+o+". "+ioe.getMessage());
		} 
	}

	/**
	 * Reads an object whose class is not known from the context from
	 * a given DataInputStream.
	 * @param dis The DataInputStream.
	 * @return the object that has been read.
	 * @exception LEAPSerializationException if an error occurs during
	 * deserialization or the object is an instance of a class that cannot be
	 * deserialized.
	 */
	private final static Object deserializeObject(DataInputStream dis, byte[] data) throws LEAPSerializationException {
		try {
			byte id = dis.readByte();
			switch (id) {
			case NULL_ID:
				return null;
			case STRING_ID:
				return dis.readUTF();
			case ACL_ID:
				return LEAPACLCodec.deserializeACL(dis);
			case AID_ID:
				return LEAPACLCodec.deserializeAID(dis);
			case STRING_ARRAY_ID:
				return deserializeStringArray(dis);
			case BOOLEAN_ID:
				return new Boolean(dis.readBoolean());
			case INTEGER_ID:
				return new Integer(dis.readInt());
			default:
				/*System.out.println("Packet was:");
      	jade.imtp.leap.JICP.JICPPacket pkt = jade.imtp.leap.JICP.BIFEDispatcher.lastResponseCazzo;
      	System.out.println("Type = "+pkt.getType());
      	System.out.println("Info = "+pkt.getInfo());
      	System.out.println("Sid = "+pkt.getSessionID());
      	System.out.println("recipientID = "+pkt.getRecipientID());
      	System.out.println("Data.length = "+pkt.getData().length);

				System.out.println("Data to deserialize is:");
				for (int i = 0; i < data.length; ++i) {
					System.out.print(data[i]+" ");
					if ((i % 16) == 0 && i != 0) {
						System.out.println("");
					}
				}*/
				throw new LEAPSerializationException("Unknown class ID: "+id);
			}
		}      // END of try
		catch (IOException e) {
			throw new LEAPSerializationException("I/O Error Deserializing a generic object");
		} 
	}


	private final static void serializeStringArray(String[] ss, DataOutputStream dos) throws IOException, LEAPSerializationException {
		dos.writeByte(ss.length);
		for (int i = 0; i < ss.length; ++i) {
			dos.writeUTF(ss[i]);
		}
	}

	private final static String[] deserializeStringArray(DataInputStream dis) throws IOException, LEAPSerializationException {
		String[] ss = new String[dis.readByte()];
		for (int i = 0; i < ss.length; ++i) {
			ss[i] = dis.readUTF();
		}
		return ss;
	}


}

