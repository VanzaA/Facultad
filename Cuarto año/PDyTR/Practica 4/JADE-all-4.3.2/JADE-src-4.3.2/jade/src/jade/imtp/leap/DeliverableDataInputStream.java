/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/*
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
 * Copyright (C) 2001 Siemens AG.
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

package jade.imtp.leap;

import java.io.DataInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Vector;
import jade.core.*;
import jade.core.messaging.GenericMessage;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.LEAPACLCodec;
import jade.domain.FIPAAgentManagement.Envelope;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ReceivedObject;
import jade.util.leap.Properties;
import jade.util.leap.ArrayList;
import jade.mtp.MTPDescriptor;
import jade.security.*;
import jade.imtp.leap.JICP.JICPAddress;
import jade.imtp.leap.JICP.JICPProtocol;
import jade.imtp.leap.http.HTTPAddress;
import jade.imtp.leap.http.HTTPProtocol;

/**
 * This class implements a data input stream deserializing
 * Deliverables from a given byte array according to the LEAP surrogate
 * serialization mechanism. In order to function correctly, the byte array
 * must contain serialized Deliverables, primitive types or J2SE class types,
 * that are considered (by this stream class) as primitive types.
 *
 * @author Michael Watzke
 * @author Giovanni Caire
 * @author Nicolas Lhuillier
 * @author Jerome Picault
 */
class DeliverableDataInputStream extends DataInputStream {
	private StubHelper myStubHelper;

	/**
	 * Constructs a data input stream that is deserializing Deliverables from the
	 * given byte array according to the LEAP surrogate serialization mechanism.
	 * @param serialized_byte_array the byte array this data input stream
	 * deserializes Deliverables from
	 */
	public DeliverableDataInputStream(byte[] serialized_byte_array, StubHelper sh) {
		super(new ByteArrayInputStream(serialized_byte_array));
		myStubHelper = sh;
	}
	/**
	 * Reads an object whose class is not known from the context from
	 * this data input stream.
	 * @return the object that has been read.
	 * @exception LEAPSerializationException if an error occurs during
	 * deserialization or the object is an instance of a class that cannot be
	 * deserialized.
	 */
	public Object readObject() throws LEAPSerializationException {
		String className = null;    // This is defined here as it is used also inside the catch blocks

		try {
			boolean presenceFlag = readBoolean();

			if (presenceFlag) {
				byte id = readByte();

				switch (id) {
				// Directly handle deserialization of classes that must be
				// deserialized more frequently
				case Serializer.HORIZONTALCOMMAND_ID:
					return deserializeHorizontalCommand();
				case Serializer.ACL_ID:
					return deserializeACL();
				case Serializer.AID_ID:
					return deserializeAID();
				case Serializer.AIDARRAY_ID:
					return deserializeAIDArray();
				case Serializer.GENERICMESSAGE_ID:
					return deserializeGenericMessage();
				case Serializer.STRING_ID:
					return readUTF();
				case Serializer.CONTAINERID_ID:
					return deserializeContainerID();
				case Serializer.CONTAINERIDARRAY_ID:
					return deserializeContainerIDArray();
				case Serializer.BOOLEAN_ID:
					return new Boolean(readBoolean());
				case Serializer.INTEGER_ID:
					return new Integer(readInt());
				case Serializer.DATE_ID:
					return deserializeDate();
				case Serializer.STRINGARRAY_ID:
					return deserializeStringArray();
				case Serializer.VECTOR_ID:
					return deserializeVector();
				case Serializer.MTPDESCRIPTOR_ID:
					return deserializeMTPDescriptor();
				case Serializer.NODEDESCRIPTOR_ID:
					return deserializeNodeDescriptor();
				case Serializer.NODE_ID:
					return deserializeNode();
				case Serializer.PLATFORMMANAGER_ID:
					return deserializePlatformManager();
				case Serializer.NODEARRAY_ID:
					return deserializeNodeArray();
				case Serializer.ENVELOPE_ID:
					return deserializeEnvelope();
				case Serializer.ARRAYLIST_ID:
					return deserializeArrayList();
				case Serializer.BYTEARRAY_ID:
					return deserializeByteArray();
				case Serializer.PROPERTIES_ID:
					return deserializeProperties();
				case Serializer.RECEIVEDOBJECT_ID:
					return deserializeReceivedObject();
				case Serializer.SERVICEDESCRIPTOR_ID:
					return deserializeServiceDescriptor();
				case Serializer.SLICEPROXY_ID:
					return deserializeSliceProxy();
					//#DOTNET_EXCLUDE_BEGIN
				case Serializer.SERVICESLICEPROXY_ID:
					return deserializeServiceSliceProxy();
					//#DOTNET_EXCLUDE_END
				case Serializer.JICPADDRESS_ID:
					return deserializeJICPAddress();
				case Serializer.HTTPADDRESS_ID:
					return deserializeHTTPAddress();
				case Serializer.THROWABLE_ID:
					return deserializeThrowable();
				case Serializer.PROPERTY_ID:
					return deserializeProperty();
					//#MIDP_EXCLUDE_BEGIN
				case Serializer.SERIALIZABLE_ID:
					byte[] bytes = deserializeByteArray();
					ByteArrayInputStream inp = new ByteArrayInputStream(bytes);
					java.io.ObjectInputStream decoder = new java.io.ObjectInputStream(inp);
					Object obj = decoder.readObject();
					return obj;
					//#MIDP_EXCLUDE_END
				case Serializer.DEFAULT_ID:
					String     serName = readUTF();
					Serializer s = (Serializer) Class.forName(serName).newInstance();
					Object     o = s.deserialize(this);
					return o;
				default:
					throw new LEAPSerializationException("Unknown class ID: "+id);
				} 
			}    // END of if (presenceFlag)
			else {
				return null;
			} 
		}      // END of try
		catch (IOException e) {
			e.printStackTrace();
			throw new LEAPSerializationException("I/O Error Deserializing a generic object");
		} 
		catch(LEAPSerializationException leapse) {
			throw leapse; // Let this one through
		}
		catch (Exception e) {
			throw new LEAPSerializationException("Error creating (de)Serializer: "+e);
		} 
	} 

	/**
	 * Reads an object of type String from this data input stream.
	 * @return the String object that has been read.
	 * @exception LEAPSerializationException if an error occurs during
	 * deserialization.
	 */
	public String readString() throws LEAPSerializationException {
		try {
			boolean presenceFlag = readBoolean();

			if (presenceFlag) {
				return readUTF();
			} 
			else {
				return null;
			} 
		} 
		catch (IOException e) {
			throw new LEAPSerializationException("Error deserializing String");
		} 
	} 

	/**
	 * Reads an object of type Date from this data input stream.
	 * @return the Date object that has been read.
	 * @exception LEAPSerializationException if an error occurs during
	 * deserialization.
	 */
	public Date readDate() throws LEAPSerializationException {
		try {
			boolean presenceFlag = readBoolean();

			if (presenceFlag) {
				return deserializeDate();
			} 
			else {
				return null;
			} 
		} 
		catch (IOException e) {
			throw new LEAPSerializationException("Error deserializing Date");
		} 
	} 

	/**
	 * Reads an object of type StringBuffer from this data input stream.
	 * @return the StringBuffer object that has been read.
	 * @exception LEAPSerializationException if an error occurs during
	 * deserialization.
	 */
	public StringBuffer readStringBuffer() throws LEAPSerializationException {
		try {
			boolean presenceFlag = readBoolean();

			if (presenceFlag) {
				return deserializeStringBuffer();
			} 
			else {
				return null;
			} 
		} 
		catch (IOException e) {
			throw new LEAPSerializationException("Error deserializing StringBuffer");
		} 
	} 

	/**
	 * Reads an object of type Vector from this data input stream.
	 * @return the Vector object that has been read.
	 * @exception LEAPSerializationException if an error occurs during
	 * deserialization.
	 */
	public Vector readVector() throws LEAPSerializationException {
		try {
			boolean presenceFlag = readBoolean();

			if (presenceFlag) {
				return deserializeVector();
			} 
			else {
				return null;
			} 
		} 
		catch (IOException e) {
			throw new LEAPSerializationException("Error deserializing Vector");
		} 
	} 

	/**
	 * Reads an array of String from this data input stream.
	 * @return the array of String that has been read.
	 * @exception LEAPSerializationException if an error occurs during
	 * deserialization.
	 */
	public String[] readStringArray() throws LEAPSerializationException {
		try {
			boolean presenceFlag = readBoolean();

			if (presenceFlag) {
				return deserializeStringArray();
			} 
			else {
				return null;
			} 
		} 
		catch (IOException e) {
			throw new LEAPSerializationException("Error deserializing String[]");
		} 
	} 

	/**
	 * Reads an object of type Long from this data input stream.
	 * @return the Long object that has been read.
	 * @exception LEAPSerializationException if an error occurs during
	 * deserialization.
	 */
	/*
	 * public Long readLongObject() throws LEAPSerializationException {
	 * try {
	 * boolean presenceFlag = readBoolean();
	 * 
	 * if (presenceFlag) {
	 * return new Long(readLong());
	 * }
	 * else {
	 * return null;
	 * }
	 * }
	 * catch (IOException e) {
	 * throw new LEAPSerializationException("Error deserializing Long");
	 * }
	 * }
	 */

	// PRIVATE METHODS
	// All the following methods are used to actually deserialize instances of
	// Java classes from this input stream. They are only used internally when
	// the context ensures that the Java object to be serialized is not null!

	/**
	 */
	public Date deserializeDate() throws IOException {
		return new Date(readLong());
	} 

	/**
	 */
	public StringBuffer deserializeStringBuffer() throws IOException {
		return new StringBuffer(readUTF());
	} 

	/**
	 */
	public Vector deserializeVector() throws IOException, LEAPSerializationException {
		int    size = readInt();
		Vector v = new Vector(size);

		for (int i = 0; i < size; i++) {
			v.addElement(readObject());
		} 

		return v;
	} 

	/**
	 */
	public String[] deserializeStringArray() throws IOException, LEAPSerializationException {
		String[] sa = new String[readInt()];

		for (int i = 0; i < sa.length; i++) {
			sa[i] = readString();
		} 

		return sa;
	} 


	private HorizontalCommand deserializeHorizontalCommand() throws LEAPSerializationException {
		try {
			// Read the mandatory command name and command service
			String name = readUTF();
			String service = readUTF();

			// Read the optional command interaction
			String interaction = readString();

			// Create the command and add all the read parameters to it
			GenericCommand result = new GenericCommand(name, service, interaction);
			int sz = readInt();
			for(int i = 0; i < sz; i++) {
				result.addParam(readObject());
			}
			
			// Read optional principal and credentials
			result.setPrincipal((JADEPrincipal) readObject());
			result.setCredentials((Credentials) readObject());

			return result;
		}
		catch(IOException ioe) {
			throw new LEAPSerializationException("Error deserializing horizontal command");
		}
	}

	/**
	 * Note that when delivering messages to agents we never deal with ACLMessage objects
	 * since they are ALWAYS encoded/decoded by the Messaging filters. However there may 
	 * be services that use ACLMessage objects as parameters in their HCommands  
	 */
	private ACLMessage deserializeACL() throws IOException, LEAPSerializationException {
		ACLMessage msg = LEAPACLCodec.deserializeACL(this);
		//#CUSTOM_EXCLUDE_BEGIN
		if (readBoolean()) {
			Envelope env = deserializeEnvelope();
			msg.setEnvelope(env);
		}
		//#CUSTOM_EXCLUDE_END

		return msg;
	} 

	/**
	 */
	public AID readAID() throws LEAPSerializationException {
		try {
			boolean presenceFlag = readBoolean();

			if (presenceFlag) {
				return deserializeAID();
			} 
			else {
				return null;
			} 
		} 
		catch (IOException e) {
			throw new LEAPSerializationException("Error deserializing AID");
		} 
	} 

	/**
	 * Package scoped as it may be called by external serializers
	 */
	AID deserializeAID() throws IOException, LEAPSerializationException {
		return LEAPACLCodec.deserializeAID(this);
	} 

	public AID[] deserializeAIDArray() throws IOException, LEAPSerializationException {
		AID[] aida = new AID[readInt()];

		for (int i = 0; i < aida.length; i++) {
			aida[i] = readAID();
		} 

		return aida;
	} 

	private GenericMessage deserializeGenericMessage() throws IOException, LEAPSerializationException {
		byte[] payload = deserializeByteArray();    
		Envelope env = (Envelope) readObject();
		GenericMessage gm = new GenericMessage(env, payload);
		gm.setAMSFailure(readBoolean());
		gm.setTraceID((String) readObject());
		return gm;
	}

	/**
	 * Package scoped as it is called by the CommandDispatcher
	 */
	Command deserializeCommand() throws LEAPSerializationException {
		try {
			Command cmd = new Command(readInt(), readInt());
			int     paramCnt = readInt();

			for (int i = 0; i < paramCnt; ++i) {
				cmd.addParam(readObject());
			} 

			return cmd;
		} 
		catch (IOException ioe) {
			throw new LEAPSerializationException("Error deserializing Command");
		} 
	} 

	/**
	 */
	private ContainerID deserializeContainerID() throws LEAPSerializationException {
		try {
			ContainerID cid = new ContainerID();
			cid.setName(readUTF());
			cid.setAddress(readUTF());

			return cid;
		} 
		catch (IOException ioe) {
			throw new LEAPSerializationException("Error deserializing ContainerID");
		} 
	} 

	public ContainerID readContainerID() throws LEAPSerializationException {
		try {
			boolean presenceFlag = readBoolean();

			if (presenceFlag) {
				return deserializeContainerID();
			} 
			else {
				return null;
			} 
		} 
		catch (IOException e) {
			throw new LEAPSerializationException("Error deserializing AID");
		} 
	} 

	public ContainerID[] deserializeContainerIDArray() throws IOException, LEAPSerializationException {
		ContainerID[] cida = new ContainerID[readInt()];

		for (int i = 0; i < cida.length; i++) {
			cida[i] = readContainerID();
		} 

		return cida;
	} 

	/**
	 */
	private MTPDescriptor deserializeMTPDescriptor() throws LEAPSerializationException {
		try {
			String   name = readUTF();
			String   className = readUTF();
			String[] addresses = readStringArray();
			String[] protoNames = readStringArray();
			return new MTPDescriptor(name, className, addresses, protoNames);
		}
		catch (IOException ioe) {
			throw new LEAPSerializationException("Error deserializing MTPDescriptor");
		}
	}

	private ServiceDescriptor deserializeServiceDescriptor() throws LEAPSerializationException {
		String name = null;
		String className = null;
		try {
			name = readUTF();
			className = readUTF();
			Service svc = (Service) Class.forName(className).newInstance();
			return new ServiceDescriptor(name, svc);
		}
		catch (ClassNotFoundException cnfe) {
			throw new LEAPSerializationException("Cannot deserialize ServiceDescriptor: class "+className+" for service "+name+" not found");
		}
		catch (Throwable t) {
			throw new LEAPSerializationException("Error deserializing ServiceDescriptor");
		}
	}

	private SliceProxy deserializeSliceProxy() throws LEAPSerializationException {
		String className = null;
		try {
			className = readUTF();
			SliceProxy proxy = (SliceProxy) Class.forName(className).newInstance();
			proxy.setNode(readNode());
			return proxy;
		}
		catch (ClassNotFoundException cnfe) {
			throw new LEAPSerializationException("Cannot deserialize SliceProxy: class "+className+" not found");
		}
		catch (Throwable t) {
			throw new LEAPSerializationException("Error deserializing SliceProxy");
		}
	}

	//#DOTNET_EXCLUDE_BEGIN
	private Service.SliceProxy deserializeServiceSliceProxy() throws LEAPSerializationException {
		try {
			String   className = readUTF();
			Service.SliceProxy proxy = (Service.SliceProxy) Class.forName(className).newInstance();
			proxy.setNode(readNode());
			return proxy;
		}
		catch (Throwable t) {
			throw new LEAPSerializationException("Error deserializing Service.SliceProxy");
		}
	}
	//#DOTNET_EXCLUDE_END

	private NodeDescriptor deserializeNodeDescriptor() throws LEAPSerializationException {
		try {
			NodeDescriptor dsc = null;
			Node node = deserializeNode();
			if(readBoolean()) {
				ContainerID cid = deserializeContainerID();
				dsc = new NodeDescriptor(cid, node);
			}
			else {
				dsc = new NodeDescriptor(node);
			}

			dsc.setParentNode(readNode());
			dsc.setUsername(readString());
			dsc.setPassword((byte[]) readObject());
			dsc.setPrincipal((JADEPrincipal) readObject());
			dsc.setOwnerPrincipal((JADEPrincipal) readObject());
			dsc.setOwnerCredentials((Credentials) readObject());

			return dsc;
		}
		catch(IOException ioe) {
			throw new LEAPSerializationException("Error deserializing NodeDescriptor");
		}
	}

	// Package-scoped since it is used by the NodeSerializer
	Node deserializeNode() throws LEAPSerializationException {
		try {
			String name = readString();
			boolean hasPM = readBoolean();

			NodeStub stub = (NodeStub) deserializeStub();
			stub.setName(name);
			stub.setPlatformManager(hasPM);
			return stub;
		}
		catch (IOException ioe) {
			throw new LEAPSerializationException("Error deserializing node");
		}
	}

	private PlatformManager deserializePlatformManager() throws LEAPSerializationException {

		String address = readString();

		PlatformManagerStub stub = (PlatformManagerStub) deserializeStub();
		stub.setLocalAddress(address);
		return stub;
	}

	private Stub deserializeStub() throws LEAPSerializationException {
		try {
			String stubClassName = readUTF();
			Stub stub = (Stub) Class.forName(stubClassName).newInstance();

			stub.remoteID = readInt();
			stub.platformName = readString();
			stub.remoteTAs = deserializeArrayList();	    

			stub.bind(myStubHelper);
			return stub;
		}
		catch(Throwable t) {
			throw new LEAPSerializationException("Error deserializing stub");
		}
	}

	private Node readNode() throws LEAPSerializationException {
		try {
			boolean presenceFlag = readBoolean();

			if(presenceFlag) {
				return deserializeNode();
			}
			else {
				return null;
			}
		}
		catch(IOException ioe) {
			throw new LEAPSerializationException("Error deserializing Node");
		}
	}

	private Node[] deserializeNodeArray() throws LEAPSerializationException {

		try {
			Node[] nodes = new Node[readInt()];

			for(int i = 0; i < nodes.length; i++) {
				nodes[i] = readNode();
			}

			return nodes;
		}
		catch(IOException ioe) {
			throw new LEAPSerializationException("Error deserializing Node[]");
		}

	}

	public Node[] readNodeArray() throws LEAPSerializationException {
		try {
			boolean presenceFlag = readBoolean();

			if (presenceFlag) {
				return deserializeNodeArray();
			} 
			else {
				return null;
			} 
		} 
		catch (IOException e) {
			throw new LEAPSerializationException("Error deserializing Node[]");
		} 
	} 

	private Envelope deserializeEnvelope() throws LEAPSerializationException {
		try {
			Envelope e = new Envelope();
			while (readBoolean()) {
				e.addTo(deserializeAID());
			}

			e.setFrom(readAID());
			e.setComments(readString());
			e.setAclRepresentation(readString());
			e.setPayloadLength(new Long(readLong()));
			e.setPayloadEncoding(readString());
			e.setDate(readDate());

			while (readBoolean()) {
				e.addIntendedReceiver(deserializeAID());
			} 

			e.setReceived((ReceivedObject) readObject());

			while (readBoolean()) {
				e.addProperties(deserializeProperty());
			} 

			return e;
		} 
		catch (IOException ioe) {
			throw new LEAPSerializationException("Error deserializing Envelope");
		}    
	}

	private ArrayList deserializeArrayList() throws LEAPSerializationException {
		try {
			ArrayList l = new ArrayList();
			int size = readInt();
			for (int i = 0; i < size; ++i) {
				l.add(readObject());
			} 
			return l;
		} 
		catch (IOException ioe) {
			throw new LEAPSerializationException("Error deserializing ArrayList");
		}    
	}

	private byte[] deserializeByteArray() throws LEAPSerializationException { 
		try {
			byte[] ba = new byte[readInt()];
			read(ba, 0, ba.length);
			return ba;
		} 
		catch (IOException ioe) {
			throw new LEAPSerializationException("Error deserializing Byte Array");
		}    
	}

	private Properties deserializeProperties() throws LEAPSerializationException {
		try {
			Properties p = new Properties();
			int size = readInt();
			for (int i = 0; i < size; i++) {
				p.setProperty((String) readObject(), (String) readObject());
			} 
			return p;
		} 
		catch (IOException ioe) {
			throw new LEAPSerializationException("Error deserializing Properties");
		}    
	}

	private ReceivedObject deserializeReceivedObject() throws LEAPSerializationException {
		ReceivedObject r = new ReceivedObject();
		r.setBy(readString());
		r.setFrom(readString());
		r.setDate(readDate());
		r.setId(readString());
		r.setVia(readString());
		return r;
	}    

	/**
	 */  
	private Property deserializeProperty() throws LEAPSerializationException {
		Property p = new Property();
		p.setName(readString());
		p.setValue(readObject());
		return p;
	}

	private JICPAddress deserializeJICPAddress() throws LEAPSerializationException {

		String protocol = readString();

		if (!JICPProtocol.NAME.equals(protocol)) {
			throw new LEAPSerializationException("Unexpected protocol \""+protocol+"\" when \""
					+JICPProtocol.NAME+"\" was expected.");
		} 

		String host = readString();
		String port = readString();
		String file = readString();
		String anchor = readString();

		return new JICPAddress(host, port, file, anchor);
	} 

	private HTTPAddress deserializeHTTPAddress() throws LEAPSerializationException {

		String protocol = readString();

		if (!HTTPProtocol.NAME.equals(protocol)) {
			throw new LEAPSerializationException("Unexpected protocol \""+protocol+"\" when \""
					+HTTPProtocol.NAME+"\" was expected.");
		} 

		String host = readString();
		String port = readString();
		String file = readString();
		String anchor = readString();

		return new HTTPAddress(host, port, file, anchor);
	} 

	private Throwable deserializeThrowable() throws LEAPSerializationException {
		String className = readString();
		String message = readString();
		try {
			Throwable result = (Throwable)Class.forName(className).newInstance();
			// FIXME: How do we set the message?
			return result;
		}
		catch(Throwable t) {

			// Actual class not found: simply create a java.lang.Exception
			return new java.lang.Exception(message + " [Original exception was: " + className + "]");
		}
	}


} // End of DeliverableDataInputStream class

