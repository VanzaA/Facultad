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

package jade.mtp.iiop;


import java.io.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Date;
import java.util.Calendar;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;

import org.omg.CORBA.*;
import org.omg.CosNaming.*;

import FIPA.*; // OMG IDL Stubs

import jade.core.AID;
import jade.core.Profile;

import jade.mtp.InChannel;
import jade.mtp.OutChannel;
import jade.mtp.MTP;
import jade.mtp.MTPException;
import jade.mtp.TransportAddress;
import jade.domain.FIPAAgentManagement.Envelope;
import jade.domain.FIPAAgentManagement.ReceivedObject;
import jade.domain.FIPAAgentManagement.Property;
//FIXME: if we add the method getSize() to all the slots whose value is a set
// (e.g. intendedReceiver, userdefProperties, ...) we can improve a lot
// performance in marshalling and unmarshalling.

/**
   Implementation of <code><b>fipa.mts.mtp.iiop.std</b></code>
   specification for delivering ACL messages over the OMG IIOP
   transport protocol.

   @author Giovanni Rimassa - Universita' di Parma
   @version $Date: 2008-10-09 14:04:02 +0200 (gio, 09 ott 2008) $ $Revision: 6051 $
 */
public class MessageTransportProtocol implements MTP {


  private static class MTSImpl extends FIPA._MTSImplBase {

    private final InChannel.Dispatcher dispatcher;

    public MTSImpl(InChannel.Dispatcher disp) {
      dispatcher = disp;
    }

    public void message(FipaMessage aFipaMessage) {
      FIPA.Envelope[] envelopes = aFipaMessage.messageEnvelopes;
      byte[] payload = aFipaMessage.messageBody;
      
      Envelope env = new Envelope();

      // Read all the envelopes sequentially, so that later slots
      // overwrite earlier ones.
      for(int e = 0; e < envelopes.length; e++) {
	FIPA.Envelope IDLenv = envelopes[e];

	// Read in the 'to' slot
	if(IDLenv.to.length > 0)
	  env.clearAllTo();
	for(int i = 0; i < IDLenv.to.length; i++) {
	  AID id = unmarshalAID(IDLenv.to[i]);
	  env.addTo(id);
	}

	// Read in the 'from' slot
	if(IDLenv.from.length > 0) {
	  AID id = unmarshalAID(IDLenv.from[0]);
	  env.setFrom(id);
	}

	// Read in the 'intended-receiver' slot
	if(IDLenv.intendedReceiver.length > 0)
	  env.clearAllIntendedReceiver();
	for(int i = 0; i < IDLenv.intendedReceiver.length; i++) {
	  AID id = unmarshalAID(IDLenv.intendedReceiver[i]);
	  env.addIntendedReceiver(id);
	}

	// Read in the 'encrypted' slot
	//if(IDLenv.encrypted.length > 0)
	//  env.clearAllEncrypted();
	//for(int i = 0; i < IDLenv.encrypted.length; i++) {
	//  String word = IDLenv.encrypted[i];
	//  env.addEncrypted(word);
	//}

	// Read in the other slots
	if(IDLenv.comments.length() > 0)
	  env.setComments(IDLenv.comments);
	if(IDLenv.aclRepresentation.length() > 0)
	  env.setAclRepresentation(IDLenv.aclRepresentation);
	if(IDLenv.payloadLength > 0)
	  env.setPayloadLength(new Long(IDLenv.payloadLength));
	if(IDLenv.payloadEncoding.length() > 0)
	  env.setPayloadEncoding(IDLenv.payloadEncoding);
	if(IDLenv.date.length > 0) {
	  Date d = unmarshalDateTime(IDLenv.date[0]);
	  env.setDate(d);
	}

	// Read in the 'received' stamp
	if(IDLenv.received.length > 0)
	  env.addStamp(unmarshalReceivedObj(IDLenv.received[0]));

	// Read in the 'user-defined properties' slot
	if(IDLenv.userDefinedProperties.length > 0)
	  env.clearAllProperties();
	for(int i = 0; i < IDLenv.userDefinedProperties.length; i++) {
	  env.addProperties(unmarshalProperty(IDLenv.userDefinedProperties[i]));
	}
      }
      
      //String tmp = "\n\n"+(new  java.util.Date()).toString()+"   RECEIVED IIOP MESSAGE"+ "\n" + env.toString() + "\n" + new String(payload); 
      //System.out.println(tmp);
   
       
      //MessageTransportProtocol.log(tmp); //Write in a log file for iiop incoming message
      
      // Dispatch the message
      dispatcher.dispatchMessage(env, payload);

    }


    private AID unmarshalAID(FIPA.AgentID id) {
      AID result = new AID();
      result.setName(id.name);
      for(int i = 0; i < id.addresses.length; i++)
	result.addAddresses(id.addresses[i]);
      for(int i = 0; i < id.resolvers.length; i++)
	result.addResolvers(unmarshalAID(id.resolvers[i]));
      return result;
    }

    private Date unmarshalDateTime(FIPA.DateTime d) {
      Date result = new Date();
      return result;
    }

    private Property unmarshalProperty(FIPA.Property p) {
      return new Property(p.keyword, p.value.extract_Value());
    }

    private ReceivedObject unmarshalReceivedObj(FIPA.ReceivedObject ro) {
      ReceivedObject result = new ReceivedObject();
      result.setBy(ro.by);
      result.setFrom(ro.from);
      result.setDate(unmarshalDateTime(ro.date));
      result.setId(ro.id);
      result.setVia(ro.via);
      return result;
    }

  } // End of MTSImpl class


  private static final String[] PROTOCOLS = new String[] { "IOR", "corbaloc", "corbaname" };

  private final ORB myORB;
  private MTSImpl server;
  private static PrintWriter logFile;
  
  public MessageTransportProtocol() {
    myORB = ORB.init(new String[0], null);
    
  }

  public TransportAddress activate(InChannel.Dispatcher disp, Profile p) throws MTPException {
    server = new MTSImpl(disp);
    myORB.connect(server);
    IIOPAddress iiop = new IIOPAddress(myORB, server);

    /* //Open log file
    String fileName = "iiop"+iiop.getHost()+iiop.getPort()+".log";
		try{
    	logFile = new PrintWriter(new FileWriter(fileName,true));
    }catch(java.io.IOException e){e .printStackTrace();}
    */
    return iiop;
    
  }

  public void activate(InChannel.Dispatcher disp, TransportAddress ta, Profile p) throws MTPException {
      //    throw new MTPException("User supplied transport address not supported.");

      // Do not throw, but modify the supplied address instead.
      IIOPAddress iia = (IIOPAddress)ta;
      IIOPAddress generated = (IIOPAddress)activate(disp, p);
      iia.initFromIOR(generated.getIOR());
  }

  public void deactivate(TransportAddress ta) throws MTPException {
    myORB.disconnect(server);
  }

  public void deactivate() throws MTPException {
    myORB.disconnect(server);
  }

  public void deliver(String addr, Envelope env, byte[] payload) throws MTPException {
    try {
      TransportAddress ta = strToAddr(addr);
      IIOPAddress iiopAddr = (IIOPAddress)ta;
      FIPA.MTS objRef = iiopAddr.getObject();

      // verifies if the server object really exists (useful if the IOR is
      // valid, i.e corresponds to a good object) (e.g. old IOR)
      // FIXME. To check if this call slows down performance
      if (objRef._non_existent()) 
        throw new MTPException("Bad IIOP server object reference:" + objRef.toString());

      // Fill in the 'to' field of the IDL envelope
      Iterator itTo = env.getAllTo();
      List to = new ArrayList();
      while(itTo.hasNext()) {
	AID id = (AID)itTo.next();
	to.add(marshalAID(id));
      }

      FIPA.AgentID[] IDLto = new FIPA.AgentID[to.size()];
      for(int i = 0; i < to.size(); i++)
	IDLto[i] = (FIPA.AgentID)to.get(i);


      // Fill in the 'from' field of the IDL envelope
      AID from = env.getFrom();
      FIPA.AgentID[] IDLfrom = new FIPA.AgentID[] { marshalAID(from) };


      // Fill in the 'intended-receiver' field of the IDL envelope
      Iterator itIntendedReceiver = env.getAllIntendedReceiver();
      List intendedReceiver = new ArrayList();
      while(itIntendedReceiver.hasNext()) {
	AID id = (AID)itIntendedReceiver.next();
	intendedReceiver.add(marshalAID(id));
      }

      FIPA.AgentID[] IDLintendedReceiver = new FIPA.AgentID[intendedReceiver.size()];
      for(int i = 0; i < intendedReceiver.size(); i++)
	IDLintendedReceiver[i] = (FIPA.AgentID)intendedReceiver.get(i);


      // Fill in the 'encrypted' field of the IDL envelope
      //Iterator itEncrypted = env.getAllEncrypted();
      //List encrypted = new ArrayList();
      //while(itEncrypted.hasNext()) {
	//String word = (String)itEncrypted.next();
	//encrypted.add(word);
      //}

      String[] IDLencrypted = new String[0];
      //String[] IDLencrypted = new String[encrypted.size()];
      //for(int i = 0; i < encrypted.size(); i++)
	//IDLencrypted[i] = (String)encrypted.get(i);


      // Fill in the other fields of the IDL envelope ...
      
      String IDLcomments = (env.getComments() != null)?env.getComments():"";
      String IDLaclRepresentation = env.getAclRepresentation();
      Long payloadLength = env.getPayloadLength();
      int IDLpayloadLength = payloadLength.intValue();
      String IDLpayloadEncoding = (env.getPayloadEncoding() != null)?env.getPayloadEncoding():"";
      FIPA.DateTime[] IDLdate = new FIPA.DateTime[] { marshalDateTime(env.getDate()) };
      FIPA.Property[][] IDLtransportBehaviour = new FIPA.Property[][] { };

      // Fill in the 'userdefined-properties' field of the IDL envelope
      Iterator itUserDefProps = env.getAllProperties();
      List userDefProps = new ArrayList();
      while(itUserDefProps.hasNext()) {
	Property p = (Property)itUserDefProps.next();
	userDefProps.add(marshalProperty(p));
      }
      FIPA.Property[] IDLuserDefinedProperties = new FIPA.Property[userDefProps.size()];
      for(int i = 0; i < userDefProps.size(); i++)
	IDLuserDefinedProperties[i] = (FIPA.Property)userDefProps.get(i);

      // Fill in the list of 'received' stamps
      /* FIXME: Maybe several IDL Envelopes should be generated, one for every 'received' stamp...
      ReceivedObject[] received = env.getStamps();
      FIPA.ReceivedObject[] IDLreceived = new FIPA.ReceivedObject[received.length];
      for(int i = 0; i < received.length; i++)
	IDLreceived[i] = marshalReceivedObj(received[i]);
      */

      // FIXME: For now, only the current 'received' object is considered...
      ReceivedObject received = env.getReceived();
      FIPA.ReceivedObject[] IDLreceived;
      if(received != null)
	IDLreceived = new FIPA.ReceivedObject[] { marshalReceivedObj(received) };
      else
	IDLreceived = new FIPA.ReceivedObject[] { };

      FIPA.Envelope IDLenv = new FIPA.Envelope(IDLto,
					       IDLfrom,
					       IDLcomments,
					       IDLaclRepresentation,
					       IDLpayloadLength,
					       IDLpayloadEncoding,
					       IDLdate,
					       IDLencrypted,
					       IDLintendedReceiver,
					       IDLreceived,
					       IDLtransportBehaviour,
					       IDLuserDefinedProperties);

      FipaMessage msg = new FipaMessage(new FIPA.Envelope[] { IDLenv }, payload);

      //String tmp = "\n\n"+(new  java.util.Date()).toString()+"   SENT IIOP MESSAGE"+ "\n" + env.toString() + "\n" + new String(payload); 
      //System.out.println(tmp);
	
  //MessageTransportProtocol.log(tmp); // write in a log file for sent iiop message
		
	objRef.message(msg);
    }
    catch(ClassCastException cce) {
      cce.printStackTrace();
      throw new MTPException("Address mismatch: this is not a valid IIOP address.");
    }
 catch(Exception cce2) {
      cce2.printStackTrace();
      throw new MTPException("Address mismatch: this is not a valid IIOP address.");
    }

  }

  public TransportAddress strToAddr(String rep) throws MTPException {
    return new IIOPAddress(myORB, rep); // FIXME: Should cache object references
  }

  public String addrToStr(TransportAddress ta) throws MTPException {
    try {
      IIOPAddress addr = (IIOPAddress)ta;
      return addr.getIOR();
    }
    catch(ClassCastException cce) {
      throw new MTPException("Address mismatch: this is not a valid IIOP address.");
    }
  }

  public String getName() {
    return jade.domain.FIPANames.MTP.IIOP; 
  }

  public String[] getSupportedProtocols() {
    return PROTOCOLS;
  }

  private FIPA.Property marshalProperty(jade.domain.FIPAAgentManagement.Property p) {
  	org.omg.CORBA.Any value = myORB.create_any();
  	java.lang.Object v = p.getValue();
  	if (v instanceof java.io.Serializable) {
    	value.insert_Value((Serializable) v);
  	}
  	else {
  		if (v != null) {
	  		value.insert_Value(v.toString());
  		}
  	}
    return new FIPA.Property(p.getName(), value);
  }

  private FIPA.AgentID marshalAID(AID id) {
    String name = id.getName();
    String[] addresses = id.getAddressesArray();
    AID[] resolvers = id.getResolversArray();
    FIPA.Property[] userDefinedProperties = new FIPA.Property[] { };
    int numOfResolvers = resolvers.length;
    FIPA.AgentID result = new FIPA.AgentID(name, addresses, new AgentID[numOfResolvers], userDefinedProperties);
    for(int i = 0; i < numOfResolvers; i++) {
      result.resolvers[i] = marshalAID(resolvers[i]); // Recursively marshal all resolvers, which are, in turn, AIDs.
    }

    return result;

  }

  private FIPA.DateTime marshalDateTime(Date d) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(d);
    short year = (short)cal.get(Calendar.YEAR);
    short month = (short)cal.get(Calendar.MONTH);
    short day = (short)cal.get(Calendar.DAY_OF_MONTH);
    short hour = (short)cal.get(Calendar.HOUR_OF_DAY);
    short minutes = (short)cal.get(Calendar.MINUTE);
    short seconds = (short)cal.get(Calendar.SECOND);
    short milliseconds = 0; // FIXME: This is truncated to the second
    char typeDesignator = ' '; // FIXME: Uses local timezone ?
    FIPA.DateTime result = new FIPA.DateTime(year,
					     month,
					     day,
					     hour,
					     minutes,
					     seconds,
					     milliseconds,
					     typeDesignator);
    return result;
  }

  private FIPA.ReceivedObject marshalReceivedObj(ReceivedObject ro) {
    FIPA.ReceivedObject result = new FIPA.ReceivedObject();
    result.by = ro.getBy();
    result.from = ro.getFrom();
    result.date = marshalDateTime(ro.getDate());
    result.id = ro.getId();
    result.via = ro.getVia();
    return result;
  }
 

//Method to write on a file the iiop message log file
/*public static synchronized void log(String str) {
  logFile.println(str);  
	logFile.flush();
		}*/

} // End of class MessageTransportProtocol

/**
This class represents an IIOP address.
Three syntaxes are allowed for an IIOP address (all case-insensitive):
<code>
IIOPAddress ::= "ior:" (HexDigit HexDigit+)
              | "corbaname://" NSHost ":" NSPort "/" NSObjectID "#" objectName
              | "corbaloc:" HostName ":" portNumber "/" objectID
</code>
Notice that, in the third case, BIG_ENDIAN is assumed by default. In the first and second case, instead, the endianess information is contained within the IOR definition.
**/
  class IIOPAddress implements TransportAddress {

    public static final byte BIG_ENDIAN = 0;
    public static final byte LITTLE_ENDIAN = 1;

    private static final String FIPA_2000_TYPE_ID = "IDL:FIPA/MTS:1.0";
    private static final String NS_TYPE_ID = "IDL:omg.org/CosNaming/NamingContext";
    private static final int TAG_INTERNET_IOP = 0;
    private static final byte IIOP_MAJOR = 1;
    private static final byte IIOP_MINOR = 0;

    private static final byte ASCII_PERCENT = getASCIIByte("%");
    private static final byte ASCII_UPPER_A = getASCIIByte("A");
    private static final byte ASCII_UPPER_Z = getASCIIByte("Z");
    private static final byte ASCII_LOWER_A = getASCIIByte("a");
    private static final byte ASCII_LOWER_Z = getASCIIByte("z");
    private static final byte ASCII_ZERO = getASCIIByte("0");
    private static final byte ASCII_NINE = getASCIIByte("9");

    private static final byte ASCII_MINUS = getASCIIByte("-");
    private static final byte ASCII_UNDERSCORE = getASCIIByte("_");
    private static final byte ASCII_DOT = getASCIIByte(".");
    private static final byte ASCII_BANG = getASCIIByte("!");
    private static final byte ASCII_TILDE = getASCIIByte("~");
    private static final byte ASCII_STAR = getASCIIByte("*");
    private static final byte ASCII_QUOTE = getASCIIByte("'");
    private static final byte ASCII_OPEN_BRACKET = getASCIIByte("(");
    private static final byte ASCII_CLOSED_BRACKET = getASCIIByte("$");

    private static final char[] HEX = {
      '0','1','2','3','4','5','6','7',
      '8','9','a','b','c','d','e','f'
    };

    private static final byte getASCIIByte(String ch) {
      try {
	return (ch.getBytes("US-ASCII"))[0];
      }
      catch(UnsupportedEncodingException uee) {
	return 0;
      }
    }


    private final ORB orb;

    private String ior;
    private String host;
    private short port;
    private String objectKey;
    private String anchor;

    private CDRCodec codecStrategy;

    public IIOPAddress(ORB anOrb, FIPA.MTS objRef) throws MTPException {
      this(anOrb, anOrb.object_to_string(objRef));
    }

    public IIOPAddress(ORB anOrb, String s) throws MTPException {
      orb = anOrb;
      if(s.toLowerCase().startsWith("ior:"))
	initFromIOR(s);
      else if(s.toLowerCase().startsWith("corbaloc:"))
	initFromURL(s, BIG_ENDIAN);
      else if(s.toLowerCase().startsWith("corbaname:"))
	initFromNS(s);
      else
	throw new MTPException("Invalid string prefix");
    }

    void initFromIOR(String s) throws MTPException {
      parseIOR(s, FIPA_2000_TYPE_ID);
      anchor = "";
    }

    private void initFromURL(String s, short endianness) throws MTPException {

      // Remove 'corbaloc:' prefix to get URL host, port and file
      s = s.substring(9);

      if(s.toLowerCase().startsWith("iiop:")) {
	// Remove an explicit IIOP specification
	s = s.substring(5);
      }
      else if(s.startsWith(":")) {
	// Remove implicit IIOP specification
	s = s.substring(1);
      }
      else
	throw new MTPException("Invalid 'corbaloc' URL: neither 'iiop:' nor ':' was specified.");

      buildIOR(s, FIPA_2000_TYPE_ID, endianness);

    }

    private void initFromNS(String s) throws MTPException {
      // First perform a 'corbaloc::' resolution to get the IOR of the Naming Service.
      // Replace 'corbaname:' with 'corbaloc::'
      StringBuffer buf = new StringBuffer(s);

      // Use 'corbaloc' support to build a reference on the NamingContext
      // where the real object reference will be looked up.
      buf.replace(0, 11, "corbaloc::");
      buildIOR(s.substring(11), NS_TYPE_ID, BIG_ENDIAN);
      org.omg.CORBA.Object o = orb.string_to_object(ior);
      NamingContext ctx = NamingContextHelper.narrow(o);

      try {

	// Transform the string after the '#' sign into a COSNaming::Name.
	StringTokenizer lexer = new StringTokenizer(anchor, "/.", true);
	List name = new ArrayList();
	while(lexer.hasMoreTokens()) {
	  String tok = lexer.nextToken();
	  NameComponent nc = new NameComponent();
	  nc.id = tok;
	  name.add(nc);
	  if(!lexer.hasMoreTokens())
	    break; // Out of the while loop

	  tok = lexer.nextToken();
	  if(tok.equals(".")) { // An (id, kind) pair
	    tok = lexer.nextToken();
	    nc.kind = tok;
	  }
	  else if(!tok.equals("/")) // No separator other than '.' or '/' is allowed
	    throw new MTPException("Ill-formed path into the Naming Service: Unknown separator.");
	}

	// Get the object reference stored into the naming service...
	NameComponent[] path = (NameComponent[])name.toArray(new NameComponent[name.size()]);
	o = ctx.resolve(path);

	// Stringify it and use the resulting IOR to initialize yourself
      	String realIOR = orb.object_to_string(o);
	initFromIOR(realIOR);

      }
      catch(NoSuchElementException nsee) {
	throw new MTPException("Ill-formed path into the Naming Service.", nsee);
      }
      catch(UserException ue) {
	throw new MTPException("CORBA Naming Service user exception.", ue);
      }
      catch(SystemException se) {
	throw new MTPException("CORBA Naming Service system exception.", se);
      }
      

    }

    private void parseIOR(String s, String typeName) throws MTPException {
      try {
	// Store stringified IOR
	ior = s.toUpperCase();

	// Remove 'IOR:' prefix to get Hex digits
	String hexString = ior.substring(4);

	short endianness = Short.parseShort(hexString.substring(0, 2), 16);

	switch(endianness) {
	case BIG_ENDIAN:
	  codecStrategy = new BigEndianCodec(hexString);
	  break;
	case LITTLE_ENDIAN:
	  codecStrategy = new LittleEndianCodec(hexString);
	  break;
	default:
	  throw new MTPException("Invalid endianness specifier");
	}

	try {
	  // Read 'string type_id' field
	  String typeID = codecStrategy.readString();
	  if(!typeID.equalsIgnoreCase(typeName))
	    throw new MTPException("Invalid type ID" + typeID);
	}
	catch (Exception e) { // all exceptions are converted into MTPException
	  throw new MTPException("Invalid type ID");
	}

	// Read 'sequence<TaggedProfile> profiles' field
	// Read sequence length
	int seqLen = codecStrategy.readLong();
	for(int i = 0; i < seqLen; i++) {
	  // Read 'ProfileId tag' field
	  int tag = codecStrategy.readLong();
	  byte[] profile = codecStrategy.readOctetSequence();
	  if(tag == TAG_INTERNET_IOP) {
	    // Process IIOP profile
	    CDRCodec profileBodyCodec;
	    switch(profile[0]) {
	    case BIG_ENDIAN:
	      profileBodyCodec = new BigEndianCodec(profile);
	      break;
	    case LITTLE_ENDIAN:
	      profileBodyCodec = new LittleEndianCodec(profile);
	      break;
	    default:
	      throw new MTPException("Invalid endianness specifier");
	    }

	    // Read IIOP version
	    byte versionMajor = profileBodyCodec.readOctet();
	    byte versionMinor = profileBodyCodec.readOctet();
	    if(versionMajor != 1)
	      throw new MTPException("IIOP version not supported");

	    try {
	      // Read 'string host' field
	      host = profileBodyCodec.readString();
	    }
	    catch (Exception e) {
	      throw new MTPException("Invalid host string");
	    }

	    // Read 'unsigned short port' field
	    port = profileBodyCodec.readShort();

	    // Read 'sequence<octet> object_key' field and convert it
	    // into a String object
	    byte[] keyBuffer = profileBodyCodec.readOctetSequence();
	    ByteArrayOutputStream buf = new ByteArrayOutputStream();

	    // Escape every forbidden character, as for RFC 2396 (URI: Generic Syntax)
	    for(int ii = 0; ii < keyBuffer.length; ii++) {
	      byte b = keyBuffer[ii];
	      if(isUnreservedURIChar(b)) {
		// Write the character 'as is'
		buf.write(b);
	      }
	      else {
		// Escape it using '%'
		buf.write(ASCII_PERCENT);
		buf.write(HEX[(b & 0xF0) >> 4]); // High nibble
		buf.write(HEX[b & 0x0F]); // Low nibble
	      }
	    }

	    objectKey = buf.toString("US-ASCII");
	    codecStrategy = null;

	  }
	}
      }
      catch (Exception e) { // all exceptions are converted into MTPException
	throw new MTPException(e.getMessage());
      }
    }

    private void buildIOR(String s, String typeName, short endianness) throws MTPException {
      int colonPos = s.indexOf(':');
      int slashPos = s.indexOf('/');
      int poundPos = s.indexOf('#');
      if((colonPos == -1) || (slashPos == -1))
	throw new MTPException("Invalid URL string");

      host = s.substring(0, colonPos);
      port = Short.parseShort(s.substring(colonPos + 1, slashPos));
      if(poundPos == -1) {
	objectKey = s.substring(slashPos + 1, s.length());
	anchor = "";
      }
      else {
	objectKey = s.substring(slashPos + 1, poundPos);
	anchor = s.substring(poundPos + 1, s.length());
      }

      switch(endianness) {
      case BIG_ENDIAN:
	codecStrategy = new BigEndianCodec(new byte[0]);
	break;
      case LITTLE_ENDIAN:
	codecStrategy = new LittleEndianCodec(new byte[0]);
	break;
      default:
	throw new MTPException("Invalid endianness specifier");
      }

      codecStrategy.writeString(typeName);

      // Write '1' as profiles sequence length
      codecStrategy.writeLong(1);

      codecStrategy.writeLong(TAG_INTERNET_IOP);
      CDRCodec profileBodyCodec;
      switch(endianness) {
      case BIG_ENDIAN:
	profileBodyCodec = new BigEndianCodec(new byte[0]);
	break;
      case LITTLE_ENDIAN:
	profileBodyCodec = new LittleEndianCodec(new byte[0]);
	break;
      default:
	throw new MTPException("Invalid endianness specifier");
      }

      // Write IIOP 1.0 profile to auxiliary CDR codec
      profileBodyCodec.writeOctet(IIOP_MAJOR);
      profileBodyCodec.writeOctet(IIOP_MINOR);
      profileBodyCodec.writeString(host);
      profileBodyCodec.writeShort(port);
      try {
	byte[] objKey = objectKey.getBytes("US-ASCII");

	// Remove all the RFC 2396 escape sequences...
	ByteArrayOutputStream buf = new ByteArrayOutputStream();
	for(int i = 0; i < objKey.length; i++) {
	  byte b = objKey[i];
	  if(b != ASCII_PERCENT)
	    buf.write(b);
	  else {
	    // Get the hex value represented by the two bytes after '%'
	    try {
	      String hexPair = new String(objKey, i + 1, 2, "US-ASCII");
	      short sh = Short.parseShort(hexPair, 16);
	      if(sh > Byte.MAX_VALUE)
		b = (byte)(sh + 2*Byte.MIN_VALUE); // Conversion from unsigned to signed
	      else
		b = (byte)sh;
	    }
	    catch(UnsupportedEncodingException uee) {
	      b = 0;
	    }
	    buf.write(b);
	    i += 2;
	  }
	}

	profileBodyCodec.writeOctetSequence(buf.toByteArray());

	byte[] encapsulatedProfile = profileBodyCodec.writtenBytes();

	// Write encapsulated profile to main IOR codec
	codecStrategy.writeOctetSequence(encapsulatedProfile);

	String hexString = codecStrategy.writtenString();
	ior = "IOR:" + hexString;

	codecStrategy = null;

      }
      catch(UnsupportedEncodingException uee) {
	// It should never happen
	uee.printStackTrace();
      }

    }

    // This method returns true if and only if the supplied byte,
    // interpreted as an US-ASCII encoded character, corresponds to an
    // unreserved URI character. See RFC 2396 for details.
    private boolean isUnreservedURIChar(byte b) {
      // An upper case letter?
      if((ASCII_UPPER_A <= b)&&(ASCII_UPPER_Z >= b))
	return true;
      // A lower case letter?
      if((ASCII_LOWER_A <= b)&&(ASCII_LOWER_Z >= b))
	return true;
      // A decimal digit?
      if((ASCII_ZERO <= b)&&(ASCII_NINE >= b))
	return true;
      // An unreserved, but not alphanumeric character?
      if((b == ASCII_MINUS)||(b == ASCII_UNDERSCORE)||(b == ASCII_DOT)||(b == ASCII_BANG)||(b == ASCII_TILDE)||
	 (b == ASCII_STAR)||(b == ASCII_QUOTE)||(b == ASCII_OPEN_BRACKET)||(b == ASCII_CLOSED_BRACKET))
	return true;

      // Anything else is not allowed
      return false;
    }

    public String getURL() {
      int portNum = port;
      if(portNum < 0)
	portNum += 65536;
      return "corbaloc::" + host + ":" + portNum + "/" + objectKey;
    }

    public String getIOR() {
      return ior;
    }

    public FIPA.MTS getObject() {
      return FIPA.MTSHelper.narrow(orb.string_to_object(ior));
    }

    private static abstract class CDRCodec {

      protected byte[] readBuffer;
      protected StringBuffer writeBuffer;
      protected int readIndex = 0;
      protected int writeIndex = 0;

      protected CDRCodec(String hexString) {
	// Put all Hex digits into a byte array
	readBuffer = bytesFromHexString(hexString);
	readIndex = 1;
	writeBuffer = new StringBuffer(255);
      }

      protected CDRCodec(byte[] hexDigits) {
	readBuffer = new byte[hexDigits.length];
	System.arraycopy(hexDigits, 0, readBuffer, 0, readBuffer.length);
	readIndex = 1;
	writeBuffer = new StringBuffer(255);
      }

      public String writtenString() {
	return new String(writeBuffer);
      }

      public byte[] writtenBytes() {
	return bytesFromHexString(new String(writeBuffer));
      }

      public byte readOctet() {
	return readBuffer[readIndex++];
      }

      public byte[] readOctetSequence() {
	int seqLen = readLong();
	byte[] result = new byte[seqLen];
	System.arraycopy(readBuffer, readIndex, result, 0, seqLen);
	readIndex += seqLen;
	return result;
      }

      public String readString() { 
	  int strLen = readLong(); // This includes '\0' terminator
	  String result = new String(readBuffer, readIndex, strLen - 1);
	  readIndex += strLen;
	  return result;
      }

      // These depend on endianness, so are deferred to subclasses
      public abstract short readShort();   // 16 bits
      public abstract int readLong();      // 32 bits
      public abstract long readLongLong(); // 64 bits

      // Writes a couple of hexadecimal digits representing the given byte.
      // All other marshalling operations ultimately use this method to modify
      // the write buffer
      public void writeOctet(byte b) {
	char[] digits = new char[2];
	digits[0] = HEX[(b & 0xF0) >> 4]; // High nibble
	digits[1] = HEX[b & 0x0F]; // Low nibble
	writeBuffer.append(digits);
	writeIndex++;
      }

      public void writeOctetSequence(byte[] seq) {
	int seqLen = seq.length;
	writeLong(seqLen);
	for(int i = 0; i < seqLen; i++)
	  writeOctet(seq[i]);
      }

      public void writeString(String s) {
	int strLen = s.length() + 1; // This includes '\0' terminator
	writeLong(strLen);
	byte[] bytes = s.getBytes();
	for(int i = 0; i < s.length(); i++)
	  writeOctet(bytes[i]);
	writeOctet((byte)0x00);
      }

      // These depend on endianness, so are deferred to subclasses
      public abstract void writeShort(short s);   // 16 bits
      public abstract void writeLong(int i);      // 32 bits
      public abstract void writeLongLong(long l); // 64 bits

      protected void setReadAlignment(int align) {
	while((readIndex % align) != 0)
	  readIndex++;
      }

      protected void setWriteAlignment(int align) {
	while(writeIndex % align != 0)
	  writeOctet((byte)0x00);
      }

      private byte[] bytesFromHexString(String hexString) {
	int hexLen = hexString.length() / 2;
	byte[] result = new byte[hexLen];

	for(int i = 0; i < hexLen; i ++) {
	  String currentDigit = hexString.substring(2*i, 2*(i + 1));
	  Short s = Short.valueOf(currentDigit, 16);
	  result[i] = s.byteValue();
	}

	return result;
      }

    } // End of CDRCodec class

    private static class BigEndianCodec extends CDRCodec {

      public BigEndianCodec(String ior) {
	super(ior);
	writeOctet((byte)0x00); // Writes 'Big Endian' magic number
      }

      public BigEndianCodec(byte[] hexDigits) {
	super(hexDigits);
	writeOctet((byte)0x00); // Writes 'Big Endian' magic number
      }

      public short readShort() {
	setReadAlignment(2);
	short result = (short)((readBuffer[readIndex++] << 8) + readBuffer[readIndex++]);
	return result;
      }

      public int readLong() {
	setReadAlignment(4);
	int result = (readBuffer[readIndex++] << 24) + (readBuffer[readIndex++] << 16);
	result += (readBuffer[readIndex++] << 8) + readBuffer[readIndex++];
	return result;
      }

      public long readLongLong() {
	setReadAlignment(8);
	long result = (readBuffer[readIndex++] << 56) + (readBuffer[readIndex++] << 48);
	result += (readBuffer[readIndex++] << 40) + (readBuffer[readIndex++] << 32);
	result += (readBuffer[readIndex++] << 24) + (readBuffer[readIndex++] << 16);
	result += (readBuffer[readIndex++] << 8) + readBuffer[readIndex++];
	return result;
      }

      public void writeShort(short s) {
	setWriteAlignment(2);
	writeOctet((byte)((s & 0xFF00) >> 8));
	writeOctet((byte)(s & 0x00FF));
      }

      public void writeLong(int i) {
	setWriteAlignment(4);
	writeOctet((byte)((i & 0xFF000000) >> 24));
	writeOctet((byte)((i & 0x00FF0000) >> 16));
	writeOctet((byte)((i & 0x0000FF00) >> 8));
	writeOctet((byte)(i & 0x000000FF));
      }

      public void writeLongLong(long l) {
	setWriteAlignment(8);
	writeOctet((byte)((l & 0xFF00000000000000L) >> 56));
	writeOctet((byte)((l & 0x00FF000000000000L) >> 48));
	writeOctet((byte)((l & 0x0000FF0000000000L) >> 40));
	writeOctet((byte)((l & 0x000000FF00000000L) >> 32));
	writeOctet((byte)((l & 0x00000000FF000000L) >> 24));
	writeOctet((byte)((l & 0x0000000000FF0000L) >> 16));
	writeOctet((byte)((l & 0x000000000000FF00L) >> 8));
	writeOctet((byte)(l & 0x00000000000000FFL));
      }

    } // End of BigEndianCodec class

    private static class LittleEndianCodec extends CDRCodec {

      public LittleEndianCodec(String ior) {
	super(ior);
	writeOctet((byte)0x01); // Writes 'Little Endian' magic number
      }

      public LittleEndianCodec(byte[] hexDigits) {
	super(hexDigits);
	writeOctet((byte)0x01); // Writes 'Little Endian' magic number
      }

      public short readShort() {
	setReadAlignment(2);
	short result = (short)(readBuffer[readIndex++] + (readBuffer[readIndex++] << 8));
	return result;
      }

      public int readLong() {
	setReadAlignment(4);
	int result = readBuffer[readIndex++] + (readBuffer[readIndex++] << 8) + (readBuffer[readIndex++] << 16) + (readBuffer[readIndex++] << 24);
	return result;
      }

      public long readLongLong() {
	setReadAlignment(8);
	long result = readBuffer[readIndex++] + (readBuffer[readIndex++] << 8);
	result += (readBuffer[readIndex++] << 16) + (readBuffer[readIndex++] << 24);
	result += (readBuffer[readIndex++] << 32) + (readBuffer[readIndex++] << 40);
	result += (readBuffer[readIndex++] << 48) + (readBuffer[readIndex++] << 56);
	return result;
      }

      public void writeShort(short s) {
	setWriteAlignment(2);
	writeOctet((byte)(s & 0x00FF));
	writeOctet((byte)((s & 0xFF00) >> 8));
      }

      public void writeLong(int i) {
	setWriteAlignment(4);
	writeOctet((byte)(i & 0x000000FF));
	writeOctet((byte)((i & 0x0000FF00) >> 8));
	writeOctet((byte)((i & 0x00FF0000) >> 16));
	writeOctet((byte)((i & 0xFF000000) >> 24));
      }

      public void writeLongLong(long l) {
	setWriteAlignment(8);
	writeOctet((byte)(l & 0x00000000000000FFL));
	writeOctet((byte)((l & 0x000000000000FF00L) >> 8));
	writeOctet((byte)((l & 0x0000000000FF0000L) >> 16));
	writeOctet((byte)((l & 0x00000000FF000000L) >> 24));
	writeOctet((byte)((l & 0x000000FF00000000L) >> 32));
	writeOctet((byte)((l & 0x0000FF0000000000L) >> 40));
	writeOctet((byte)((l & 0x00FF000000000000L) >> 48));
	writeOctet((byte)((l & 0xFF00000000000000L) >> 56));
      }

    }  // End of LittleEndianCodec class

    public String getProto() {
      return "iiop";
    }

    public String getHost() {
      return host;
    }

    public String getPort() {
      return Short.toString(port);
    }

    public String getFile() {
      return objectKey;
    }

    public String getAnchor() {
      return anchor;
    }
    
    

  } // End of IIOPAddress class

