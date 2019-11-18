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

import java.io.DataOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Vector;
import java.util.Enumeration;
import jade.core.*;
import jade.core.messaging.GenericMessage;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.LEAPACLCodec;
import jade.domain.FIPAAgentManagement.Envelope;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ReceivedObject;
import jade.util.leap.Iterator;
import jade.util.leap.Properties;
import jade.util.leap.ArrayList;
import jade.mtp.MTPDescriptor;
import jade.mtp.TransportAddress;
import jade.imtp.leap.JICP.JICPAddress;
import jade.imtp.leap.http.HTTPAddress;

/**
 * This class implements a data output stream serializing
 * Deliverables, primitive types and J2SE class types that are considered as
 * primitive type to a given byte array according to the LEAP surrogate
 * serialization mechanism.
 * 
 * @author Michael Watzke
 * @author Giovanni Caire
 * @author Nicolas Lhuillier
 * @author Jerome Picault
 */
class DeliverableDataOutputStream extends DataOutputStream {

  private StubHelper myStubHelper;

  /**
   * Constructs a data output stream that is serializing Deliverables to a
   * given byte array according to the LEAP surrogate serialization
   * mechanism.
   */
  public DeliverableDataOutputStream(StubHelper sh) {
    super(new ByteArrayOutputStream());
    myStubHelper = sh;
  }

  /**
   * Get the byte array that contains the frozen, serialized Deliverables
   * written to this data output stream.
   * @return the byte array containing the serialized Deliverables written
   * to this data output stream
   */
  public byte[] getSerializedByteArray() {
    return ((ByteArrayOutputStream) out).toByteArray();
  } 

  /**
   * Writes an object whose class is not known from the context to
   * this data output stream.
   * @param o the object to be written.
   * @exception LEAPSerializationException if an error occurs during
   * serialization or the object is an instance of a class that cannot be
   * serialized.
   */
  public void writeObject(Object o) throws LEAPSerializationException {
    try {
      if (o != null) {

        // Presence flag true
        writeBoolean(true);

        // Directly handle serialization of classes that must be
        // serialized more frequently
        if (o instanceof HorizontalCommand) {
          writeByte(Serializer.HORIZONTALCOMMAND_ID);
          serializeHorizontalCommand((HorizontalCommand)o);
        }
        else if (o instanceof ACLMessage) {                   // ACLMessage
          writeByte(Serializer.ACL_ID);
          serializeACL((ACLMessage) o);
        } 
        else if (o instanceof AID) {                     // AID
          writeByte(Serializer.AID_ID);
          serializeAID((AID) o);
        } 
        else if (o instanceof AID[]) {                   // AID array
          writeByte(Serializer.AIDARRAY_ID);
          serializeAIDArray((AID[]) o);
        } 
        else if (o instanceof GenericMessage) {          // GenericMessage
          writeByte(Serializer.GENERICMESSAGE_ID);
          serializeGenericMessage((GenericMessage) o);
        } 
        else if (o instanceof String) {                  // String
          writeByte(Serializer.STRING_ID);
          writeUTF((String) o);
        } 
        else if (o instanceof NodeDescriptor) {    // NodeDescriptor
          writeByte(Serializer.NODEDESCRIPTOR_ID);
          serializeNodeDescriptor((NodeDescriptor) o);
        } 
        else if (o instanceof ContainerID) {             // ContainerID
          writeByte(Serializer.CONTAINERID_ID);
          serializeContainerID((ContainerID) o);
        } 
        else if (o instanceof ContainerID[]) {             // ContainerID[]
          writeByte(Serializer.CONTAINERIDARRAY_ID);
          serializeContainerIDArray((ContainerID[]) o);
        } 
        else if (o instanceof Boolean) {                 // Boolean
          writeByte(Serializer.BOOLEAN_ID);
          writeBoolean(((Boolean) o).booleanValue());
        } 
        else if (o instanceof Integer) {                 // Integer
          writeByte(Serializer.INTEGER_ID);
          writeInt(((Integer) o).intValue());
        } 
        else if (o instanceof Date) {                    // Date
          writeByte(Serializer.DATE_ID);
          serializeDate((Date) o);
        } 
        else if (o instanceof String[]) {                // Array of Strings
          writeByte(Serializer.STRINGARRAY_ID);
          serializeStringArray((String[]) o);
        } 
        else if (o instanceof Vector) {                  // Vector
          writeByte(Serializer.VECTOR_ID);
          serializeVector((Vector) o);
        } 
        else if (o instanceof MTPDescriptor) {           // MTPDescriptor
          writeByte(Serializer.MTPDESCRIPTOR_ID);
          serializeMTPDescriptor((MTPDescriptor) o);
        }
        else if (o instanceof Node) {                    // Node
          writeByte(Serializer.NODE_ID);
          serializeNode((Node) o);
        }
        else if (o instanceof PlatformManager) {         // PlatformManager
          writeByte(Serializer.PLATFORMMANAGER_ID);
          serializePlatformManager((PlatformManager) o);
        }
        else if (o instanceof Node[]) {                  // Array of Node
          writeByte(Serializer.NODEARRAY_ID);
          serializeNodeArray((Node[]) o);
        }
        else if (o instanceof ArrayList) {               // ArrayList
          writeByte(Serializer.ARRAYLIST_ID);
          serializeArrayList((ArrayList) o);
        }
        else if (o instanceof byte[]) {                  // Byte Array
          writeByte(Serializer.BYTEARRAY_ID);
          serializeByteArray((byte[]) o);
        }
        else if (o instanceof Envelope) {                // Envelope 
          writeByte(Serializer.ENVELOPE_ID);
          serializeEnvelope((Envelope) o);
        }
        else if (o instanceof JICPAddress) {             // JICPAddress 
          writeByte(Serializer.JICPADDRESS_ID);
          serializeTransportAddress((JICPAddress) o);
        }
        else if (o instanceof HTTPAddress) {             // HTTPAddress 
          writeByte(Serializer.HTTPADDRESS_ID);
          serializeTransportAddress((HTTPAddress) o);
        }
        else if (o instanceof Properties) {              // Properties 
          writeByte(Serializer.PROPERTIES_ID);
          serializeProperties((Properties) o);
        }
        else if (o instanceof ReceivedObject) {          // ReceivedObject
          writeByte(Serializer.RECEIVEDOBJECT_ID);
          serializeReceivedObject((ReceivedObject) o);
        }
        else if (o instanceof ServiceDescriptor) {       // ServiceDescriptor
          writeByte(Serializer.SERVICEDESCRIPTOR_ID);
          serializeServiceDescriptor((ServiceDescriptor) o);
        }
        else if (o instanceof SliceProxy) {      // SliceProxy
          writeByte(Serializer.SLICEPROXY_ID);
          serializeSliceProxy((SliceProxy) o);
        }
        //#DOTNET_EXCLUDE_BEGIN
        else if (o instanceof Service.SliceProxy) {      // Service.SliceProxy
          writeByte(Serializer.SERVICESLICEPROXY_ID);
          serializeServiceSliceProxy((Service.SliceProxy) o);
        }
        //#DOTNET_EXCLUDE_END
        else if(o instanceof Property) {                   // Property
          writeByte(Serializer.PROPERTY_ID);
          serializeProperty((Property) o);
        }
        /*#MIDP_INCLUDE_BEGIN
        // In J2SE and PJAVA we use Java serialization to transport 
        // Throwable objects so that we keep the correct message.        
        else if(o instanceof Throwable) {                   // Throwable
          writeByte(Serializer.THROWABLE_ID);
          serializeThrowable((Throwable) o);
        }
        #MIDP_INCLUDE_END*/
        //#MIDP_EXCLUDE_BEGIN
        else if (o instanceof java.io.Serializable) {       // Serializable 
          writeByte(Serializer.SERIALIZABLE_ID);
          ByteArrayOutputStream out = new ByteArrayOutputStream();
          java.io.ObjectOutputStream encoder = new java.io.ObjectOutputStream(out);
          encoder.writeObject(o);
          byte[] bytes = out.toByteArray();
          serializeByteArray(bytes);
        }
        //#MIDP_EXCLUDE_END
        else {
	        // Delegate serialization of other classes 
	        // to a proper Serializer object
          Serializer s = getSerializer(o);

          writeByte(Serializer.DEFAULT_ID);
          writeUTF(s.getClass().getName());
          s.serialize(o, this);
        } 
      } // END of if (o != null)
      else {

        // Presence flag false
        writeBoolean(false);
      } 
    }  // END of try
    catch (IOException ioe) {
      throw new LEAPSerializationException("Error Serializing object "+o);
    } 
  } 

  /**
   * Writes an AID object to this data output stream.
   * @param id the AID to be written.
   * @exception LEAPSerializationException if an error occurs during
   * serialization
   */
  public void writeAID(AID id) throws LEAPSerializationException {
    try {
      if (id != null) {
        writeBoolean(true);     // Presence flag true
        serializeAID(id);
      } 
      else {
        writeBoolean(false);    // Presence flag false
      } 
    } 
    catch (IOException ioe) {
      throw new LEAPSerializationException("Error serializing AID");
    } 
  } 

  /**
   * Writes a String object to this data output stream.
   * @param s the String to be written.
   * @exception LEAPSerializationException if an error occurs during
   * serialization
   */
  public void writeString(String s) throws LEAPSerializationException {
    try {
      if (s != null) {
        writeBoolean(true);     // Presence flag true
        writeUTF(s);
      } 
      else {
        writeBoolean(false);    // Presence flag false
      } 
    } 
    catch (IOException ioe) {
      throw new LEAPSerializationException("Error serializing String");
    } 
  } 

  /**
   * Writes a Date object to this data output stream.
   * @param d the Date to be written.
   * @exception LEAPSerializationException if an error occurs during
   * serialization
   */
  public void writeDate(Date d) throws LEAPSerializationException {
    try {
      if (d != null) {
        writeBoolean(true);     // Presence flag true
        serializeDate(d);
      } 
      else {
        writeBoolean(false);    // Presence flag false
      } 
    } 
    catch (IOException ioe) {
      throw new LEAPSerializationException("Error serializing Date");
    } 
  } 

  /**
   * Writes a StringBuffer object to this data output stream.
   * @param s the StringBuffer to be written.
   * @exception LEAPSerializationException if an error occurs during
   * serialization
   */
  public void writeStringBuffer(StringBuffer s) throws LEAPSerializationException {
    try {
      if (s != null) {
        writeBoolean(true);     // Presence flag true
        serializeStringBuffer(s);
      } 
      else {
        writeBoolean(false);    // Presence flag false
      } 
    } 
    catch (IOException ioe) {
      throw new LEAPSerializationException("Error serializing String");
    } 
  } 

  /**
   * Writes a Vector object to this data output stream.
   * @param v the Vector to be written.
   * @exception LEAPSerializationException if an error occurs during
   * serialization
   */
  public void writeVector(Vector v) throws LEAPSerializationException {
    try {
      if (v != null) {
        writeBoolean(true);     // Presence flag true
        serializeVector(v);
      } 
      else {
        writeBoolean(false);    // Presence flag false
      } 
    } 
    catch (IOException ioe) {
      throw new LEAPSerializationException("Error serializing Vector");
    } 
  } 

  /**
   * Writes an array of String to this data output stream.
   * @param sa the array of String to be written.
   * @exception LEAPSerializationException if an error occurs during
   * serialization
   */
  public void writeStringArray(String[] sa) throws LEAPSerializationException {
    try {
      if (sa != null) {
        writeBoolean(true);     // Presence flag true
        serializeStringArray(sa);
      } 
      else {
        writeBoolean(false);    // Presence flag false
      } 
    } 
    catch (IOException ioe) {
      throw new LEAPSerializationException("Error serializing String[]");
    } 
  } 


  // PRIVATE METHODS
  // All the following methods are used to actually serialize instances of
  // Java classes to this output stream. They are only used internally when
  // the context ensures that the Java object to be serialized is not null!

  /**
   */
  private void serializeDate(Date d) throws IOException {
    writeLong(d.getTime());
  } 

  /**
   */
  private void serializeStringBuffer(StringBuffer sb) throws IOException {
    writeUTF(sb.toString());
  } 

  /**
   */
  private void serializeVector(Vector v) throws IOException, LEAPSerializationException {
    writeInt(v.size());

    for (int i = 0; i < v.size(); i++) {
      writeObject(v.elementAt(i));
    } 
  } 

  /**
   */
  private void serializeStringArray(String[] sa) throws IOException, LEAPSerializationException {
    writeInt(sa.length);

    for (int i = 0; i < sa.length; i++) {
      writeString(sa[i]);
    } 
  } 

  private void serializeNodeDescriptor(NodeDescriptor desc) throws IOException, LEAPSerializationException {
    serializeNode(desc.getNode());
    writeContainerID(desc.getContainer());
    writeNode(desc.getParentNode());
		writeString(desc.getUsername());
    writeObject(desc.getPassword());
    writeObject(desc.getPrincipal());
    writeObject(desc.getOwnerPrincipal());
    writeObject(desc.getOwnerCredentials());
  }


  private void serializeHorizontalCommand(HorizontalCommand cmd) throws LEAPSerializationException {
    try {

	    // Write the mandatory command name and command service
	    writeUTF(cmd.getName());
	    writeUTF(cmd.getService());

	    // Write optional interaction ID
	    writeString(cmd.getInteraction());

	    // Write all parameters
	    Object[] params = cmd.getParams();
	    int sz = params.length;
	    writeInt(sz);
	    for(int i = 0; i < sz; i++) {
	    	writeObject(params[i]);
	    }
	    
	    // Write optional principal and credentials
	    writeObject(cmd.getPrincipal());
	    writeObject(cmd.getCredentials());
    }
    catch(IOException ioe) {
	    throw new LEAPSerializationException("Error serializing horizontal command");
    }
  }

  /**
   * Note that when delivering messages to agents we never deal with ACLMessage objects
   * since they are ALWAYS encoded/decoded by the Messaging filters. However there may 
   * be services that use ACLMessage objects as parameters in their HCommands  
   */
  private void serializeACL(ACLMessage msg) throws IOException, LEAPSerializationException {
	  LEAPACLCodec.serializeACL(msg, this);
	  //#CUSTOM_EXCLUDE_BEGIN
	  // NOTE that the above call does not serialize the envelope
	  Envelope env = msg.getEnvelope();
	  if (env != null) {
	      writeBoolean(true);
	      serializeEnvelope(env);
	  }
	  else {
		  writeBoolean(false);
	  }
	  //#CUSTOM_EXCLUDE_END
  } 

  /**
   * Package scoped as it is called by the EnvelopSerializer
   */
  void serializeAID(AID id) throws IOException, LEAPSerializationException {
	LEAPACLCodec.serializeAID(id, this);
  } 

  private void serializeAIDArray(AID[] aida) throws IOException, LEAPSerializationException {
    writeInt(aida.length);

    for (int i = 0; i < aida.length; i++) {
      writeAID(aida[i]);
    } 
  } 

  private void serializeGenericMessage(GenericMessage gm) throws IOException, LEAPSerializationException {
  	byte[] payload = gm.getPayload();
  	if (payload == null) {
      payload = (new LEAPACLCodec()).encode(gm.getACLMessage(), null);
    }
    serializeByteArray(payload);
    
	writeObject(gm.getEnvelope());
  	writeBoolean(gm.isAMSFailure());
  	writeObject(gm.getTraceID());
  }
  
  /**
   * Package scoped as it is called by the CommandDispatcher
   */
  void serializeCommand(Command cmd) throws LEAPSerializationException {
    try {
      writeInt(cmd.getCode());    // the code of the command has to
      // be at index 0 and it has to be
      // a 4 byte integer.
      writeInt(cmd.getObjectID());

      int paramCnt = cmd.getParamCnt();

      writeInt(paramCnt);

      for (int i = 0; i < paramCnt; ++i) {
        writeObject(cmd.getParamAt(i));
      } 
    } 
    catch (IOException ioe) {
      throw new LEAPSerializationException("Error serializing Command");
    } 
  } 

  /**
   */
  public void serializeContainerID(ContainerID cid) throws LEAPSerializationException {
    try {
      writeUTF(cid.getName());
      writeUTF(cid.getAddress());
    } 
    catch (IOException ioe) {
      throw new LEAPSerializationException("Error serializing ContainerID");
    } 
  } 

  public void writeContainerID(ContainerID id) throws LEAPSerializationException {
    try {
      if (id != null) {
        writeBoolean(true);     // Presence flag true
        serializeContainerID(id);
      } 
      else {
        writeBoolean(false);    // Presence flag false
      } 
    } 
    catch (IOException ioe) {
      throw new LEAPSerializationException("Error serializing ContainerID");
    } 
  } 

  private void serializeContainerIDArray(ContainerID[] cida) throws IOException, LEAPSerializationException {
    writeInt(cida.length);

    for (int i = 0; i < cida.length; i++) {
      writeContainerID(cida[i]);
    } 
  } 

  /**
   */
  public void serializeMTPDescriptor(MTPDescriptor dsc) throws LEAPSerializationException {
    try {
      writeUTF(dsc.getName());
	    writeUTF(dsc.getClassName());
      writeStringArray(dsc.getAddresses());
      writeStringArray(dsc.getSupportedProtocols());
    }
    catch (IOException ioe) {
      throw new LEAPSerializationException("Error serializing MTPDescriptor");
    }
  }

  /**
   */
  private void serializeServiceDescriptor(ServiceDescriptor dsc) throws LEAPSerializationException {
    try {
      writeUTF(dsc.getName());
      Service svc = dsc.getService();
      writeUTF(svc.getClass().getName());
    }
    catch (IOException ioe) {
      throw new LEAPSerializationException("Error serializing ServiceDescriptor");
    }
  }

  /**
   */
  private void serializeSliceProxy(SliceProxy proxy) throws LEAPSerializationException {
    try {
      writeUTF(proxy.getClass().getName());
      writeNode(proxy.getNode());
    }
    catch (Throwable t) {
      throw new LEAPSerializationException("Error serializing SliceProxy");
    }
  }

  //#DOTNET_EXCLUDE_BEGIN
  /**
   */
  private void serializeServiceSliceProxy(Service.SliceProxy proxy) throws LEAPSerializationException {
    try {
      writeUTF(proxy.getClass().getName());
      writeNode(proxy.getNode());
    }
    catch (Throwable t) {
      throw new LEAPSerializationException("Error serializing Service.SliceProxy");
    }
  }
  //#DOTNET_EXCLUDE_END

  private void serializeNode(Node n) throws LEAPSerializationException {
    try {
	    writeString(n.getName());
	    writeBoolean(n.hasPlatformManager());

	    NodeStub stub = null;
	    if (n instanceof NodeStub) {
	    	// This is already a stub --> serialize it directly
	    	stub = (NodeStub) n;
	    }
	    else {
		    // This is a real node --> get a stub and serialize it
	    	stub = (NodeStub) myStubHelper.buildLocalStub(n);
	    }
	    serializeStub(stub);
    }
    catch(IOException ioe) {
      ioe.printStackTrace();
	    throw new LEAPSerializationException("Error building a Node stub");
    }
    catch(IMTPException imtpe) {
      imtpe.printStackTrace();
	    throw new LEAPSerializationException("Error building a Node stub");
    }
  }

  private void writeNode(Node n) throws LEAPSerializationException {
    try {
      if (n != null) {
        writeBoolean(true);     // Presence flag true
        serializeNode(n);
      } 
      else {
        writeBoolean(false);    // Presence flag false
      } 
    } 
    catch (IOException ioe) {
      throw new LEAPSerializationException("Error serializing Node[]");
    } 
  }

  private void serializePlatformManager(PlatformManager pm) throws LEAPSerializationException {
    try {
	    writeString(pm.getLocalAddress());

	    PlatformManagerStub stub = null;
	    if (pm instanceof PlatformManagerStub) {
	    	// This is already a stub --> serialize it directly
	    	stub = (PlatformManagerStub) pm;
	    }
	    else {
		    // This is a real PlatformManager --> get a stub and serialize it
	    	stub = (PlatformManagerStub) myStubHelper.buildLocalStub(pm);
	    }
	    serializeStub(stub);
    }
    catch(IMTPException imtpe) {
      imtpe.printStackTrace();
	    throw new LEAPSerializationException("Error building a PlatformManager stub");
    }
  }

  private void serializeStub(Stub stub) throws LEAPSerializationException {
    try {
		writeUTF(stub.getClass().getName());
	    // Write the remote ID, uniquely identifying the remotized object this stub points to
	    writeInt(stub.remoteID);

	    // Write the name of the platform this stub belongs to
	    writeString(stub.platformName);
	    
	    // Write all the transport addresses
	    serializeArrayList((ArrayList) stub.remoteTAs);
    }
    catch(IOException ioe) {
	    throw new LEAPSerializationException("I/O Error during stub serialization");
    }
  }

  private void serializeNodeArray(Node[] nodes) 
    throws LEAPSerializationException {
    try {

	    writeInt(nodes.length);

	    for (int i = 0; i < nodes.length; i++) {
        writeNode(nodes[i]);
	    }
    } 
    catch (IOException ioe) {
      throw new LEAPSerializationException("IO error serializing node array");
    } 
  }

  public void writeNodeArray(Node[] nodes) throws LEAPSerializationException {
    try {
      if (nodes != null) {
        writeBoolean(true);     // Presence flag true
        serializeNodeArray(nodes);
      } 
      else {
        writeBoolean(false);    // Presence flag false
      } 
    } 
    catch (IOException ioe) {
      throw new LEAPSerializationException("Error serializing Node[]");
    } 
  } 



  /**
   */
  private void serializeArrayList(ArrayList l)
    throws LEAPSerializationException {
    try {
      int       size = l.size();
      writeInt(size);

      for (int i = 0; i < size; i++) {
        writeObject(l.get(i));
      } 
    } 
    catch (IOException ioe) {
      throw new LEAPSerializationException("I/O error serializing ArrayList "+l);
    } 
  }

  /**
   */  
  private void serializeByteArray(byte[] ba)
    throws LEAPSerializationException {
    try {
      writeInt(ba.length);
      write(ba, 0, ba.length);
    } 
    catch (IOException ioe) {
      throw new LEAPSerializationException("IO error serializing byte[] "+ba);
    } 
  }

  /**
   */  
  private void serializeEnvelope(Envelope e)
    throws LEAPSerializationException {
    try {
            
      // to
      Iterator it = e.getAllTo();
      while (it.hasNext()) {
        writeBoolean(true);
        serializeAID((AID) it.next());
      } 
      writeBoolean(false);
            
      writeAID(e.getFrom());
      writeString(e.getComments());
      writeString(e.getAclRepresentation());
      writeLong(e.getPayloadLength().longValue());
      writeString(e.getPayloadEncoding());
      writeDate(e.getDate());
            
      // intended receivers
      it = e.getAllIntendedReceiver();
      while (it.hasNext()) {
        writeBoolean(true);
        serializeAID((AID) it.next());
      } 
      writeBoolean(false);
            
      writeObject(e.getReceived());
            
      // properties
      it = e.getAllProperties();
      while (it.hasNext()) {
        writeBoolean(true);
        serializeProperty((Property) it.next());
      } 
      writeBoolean(false);
    } 
    catch (IOException ioe) {
      throw new LEAPSerializationException("IO error serializing Envelope "+e);
    } 
  }
    
  /**
   */  
  private void serializeTransportAddress(TransportAddress addr)
    throws LEAPSerializationException {
    writeString(addr.getProto());
    writeString(addr.getHost());
    writeString(addr.getPort());
    writeString(addr.getFile());
    writeString(addr.getAnchor());
  }

  /**
   */  
  private void serializeProperties(Properties p)
    throws LEAPSerializationException {
    try {     
      int        size = p.size();
      writeInt(size);  

      Enumeration e = p.propertyNames();
      while (e.hasMoreElements()) {
        Object key = e.nextElement();      
        writeObject(key);
        writeObject(p.getProperty((String) key));
      } 
    } 
    catch (IOException ioe) {
      throw new LEAPSerializationException("I/O error serializing Properties "+p);
    }    
  }

  /**
   */  
  private void serializeReceivedObject(ReceivedObject r)
    throws LEAPSerializationException {
    writeString(r.getBy());
    writeString(r.getFrom());
    writeDate(r.getDate());
    writeString(r.getId());
    writeString(r.getVia());
  }

  /**
   */  
  private void serializeProperty(Property p)
    throws LEAPSerializationException {
    writeString(p.getName());
    writeObject(p.getValue());    
  }
    
    
  private void serializeThrowable(Throwable t) throws LEAPSerializationException {
    writeString(t.getClass().getName());
    writeString(t.getMessage());
  }

    
  /**
   */
  private Serializer getSerializer(Object o) 
    throws LEAPSerializationException {
    String fullName = o.getClass().getName();
    int    index = fullName.lastIndexOf('.');
    String name = fullName.substring(index+1);
    String serName = new String("jade.imtp.leap."+name+"Serializer");
      
    // DEBUG
    // System.out.println(serName);
    try {
      Serializer s = (Serializer) Class.forName(serName).newInstance();

      return s;
    } 
    catch (Exception e) {
      throw new LEAPSerializationException("Error creating Serializer for object "+o);
    } 
  } 

}

