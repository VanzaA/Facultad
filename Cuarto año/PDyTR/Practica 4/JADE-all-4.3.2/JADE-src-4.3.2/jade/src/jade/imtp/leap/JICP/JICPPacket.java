/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/**
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
 * Copyright (C) 2001 Motorola.
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

package jade.imtp.leap.JICP;

import java.io.*;
import jade.util.Logger;

/**
 * This class is the JICP data packet representation along
 * with methods for reading from and writing to dataXXputStreams.
 * @author Ronnie Taib - Motorola
 * @author Steffen Rusitschka - Siemens AG
 */
public class JICPPacket {
	public static final int MAX_SIZE = 10000000;

  /**
   * The type of data included in the packet
   */
  private byte   type;

  /**
   * Bit encoded information about the content of the packet:
   */
  private byte   info;

  /**
   * An optional identifier for the session this packet belongs to
   */
  private byte   sessionID = -1;
  
  /**
   * An optional field indicating the actual recipient for this JICPPacket. 
   * - A JICPServer receiving a JICPPacket from a remote container
   * interprets this field as the ID of a local Mediator.
   * - A Mediator receiving a JICPPacket from its mediated container
   * interprets this field as the serialized transport address of 
   * final destination to forward the packet.
   */
  private String recipientID;

  /**
   * The payload data itself, as a byte array
   */
  private byte[] data;

  /**
   * Empty constructor
   */
  private JICPPacket() {
  } 

  /**
   * Constructor.
   * @param type The type of data included in the packet
   * @param data The data itself, as a byte array.
   */
  public JICPPacket(byte type, byte info, byte[] data) {
    init(type, info, null, data);
  }

  /**
   * Constructor used to set the recipientID.
   * @param type The type of data included in the packet
   * @param data The data itself, as a byte array.
   */
  public JICPPacket(byte type, byte info, String recipientID, byte[] data) {
    init(type, info, recipientID, data);
  }

  /**
   * constructs a JICPPacket of type JICPProtocol.ERROR_TYPE and sets the
   * data to the string representation of the exception.
   */
  public JICPPacket(String explanation, Exception e) {
    if (e != null) {
      explanation = explanation+": "+e.getClass().getName()+"#"+e.getMessage();
    } 

    init(JICPProtocol.ERROR_TYPE, JICPProtocol.DEFAULT_INFO, null, explanation.getBytes());
  }

  /**
   */
  private void init(byte t, byte i, String id, byte[] d) {
    type = t;
  	info = i;
  	sessionID = -1;
    data = d;
    
    setRecipientID(id);

    if (data != null) {
    	info |= JICPProtocol.DATA_PRESENT_INFO;
    	//if ((info & JICPProtocol.COMPRESSED_INFO) != 0) {
      //	data = JICPCompressor.compress(data);
    	//}
    }
  } 

  /**
   * @return The type of data included in the packet.
   */
  public byte getType() {
    return type;
  } 

  /**
   * @return the info field of this packet
   */
  public byte getInfo() {
    return info;
  } 

  /**
   * @return The sessionID of this packet.
   */
  public byte getSessionID() {
    return sessionID;
  } 

  /**
   * Set the sessionID of this packet and adjust the info field
   * accordingly.
   */
  public void setSessionID(byte id) {
    sessionID = id;
    
    if (sessionID >= 0) {
    	info |= JICPProtocol.SESSION_ID_PRESENT_INFO;
    }
    else {
    	info &= (~JICPProtocol.SESSION_ID_PRESENT_INFO);
    }
  } 

  /**
   * @return The recipientID of this packet.
   */
  public String getRecipientID() {
    return recipientID;
  } 

  /**
   * Set the recipientID of this packet and adjust the info field
   * accordingly.
   */
  public void setRecipientID(String id) {
    recipientID = id;
    
    if (recipientID != null) {
    	info |= JICPProtocol.RECIPIENT_ID_PRESENT_INFO;
    }
    else {
    	info &= (~JICPProtocol.RECIPIENT_ID_PRESENT_INFO);
    }
  } 

  /**
   * Set the TERMINATED_INFO flag in the info field.
   */
  public void setTerminatedInfo(boolean set) {
    if (set) {
      info |= JICPProtocol.TERMINATED_INFO;
    }
    else {
      info &= (~JICPProtocol.TERMINATED_INFO);
    }
  } 

  /**
   * @return The actual data included in the packet, as a byte array.
   */
  public byte[] getData() {
    //if (data != null && data.length != 0) {
    //  return (info & JICPProtocol.COMPRESSED_INFO) != 0 ? JICPCompressor.decompress(data) : data;
    //} 
    //else {
      return data;
    //} 
  } 

  /**
   * Writes the packet into the provided <code>OutputStream</code>.
   * The packet is serialized in an internal representation, so the
   * data should be retrieved and deserialized with the
   * <code>readFrom()</code> static method below. The output stream is flushed
   * but not opened nor closed by this method.
   * 
   * @param out The  <code>OutputStream</code> to write the data in
   * @exception May send a large bunch of exceptions, mainly in the IO
   * package.
   */
  public int writeTo(OutputStream out) throws IOException {
  	int cnt = 2;
    // Write the packet type
    out.write(type);

    // Write the packet info
    out.write(info);

    // Write the session ID if present
    if ((info & JICPProtocol.SESSION_ID_PRESENT_INFO) != 0) {
      out.write(sessionID);
      cnt++;
    }

    // Write recipient ID only if != null
    if ((info & JICPProtocol.RECIPIENT_ID_PRESENT_INFO) != 0) {
    	out.write(recipientID.length());
    	out.write(recipientID.getBytes());
    	cnt += (1 + recipientID.length());
    } 

    // Write data only if != null
    if (data != null) {
      // Size
    	int size = data.length;
    	out.write(size);
    	out.write(size >> 8);      	
    	out.write(size >> 16);      	
    	out.write(size >> 24);      	
    	cnt += 4;
    	// Payload
    	if (size > 0) {
      	out.write(data, 0, size);
      	cnt += size;
    	}
    }
  	// DEBUG
  	//System.out.println(getLength()+" bytes written");
    return cnt;
  } 

  /**
   * This static method reads from a given
   * <code>DataInputStream</code> and returns the JICPPacket that
   * it reads. The input stream is not opened nor closed by this method.
   * 
   * @param in The <code>InputStream</code> to read from
   * @exception May send a large bunch of exceptions, mainly in the IO
   * package.
   */
  public static JICPPacket readFrom(InputStream in) throws IOException {
    JICPPacket p = new JICPPacket();

    // Read packet type
    p.type = read(in);

    // Read the packet info
    p.info = read(in);

    // Read session ID if present
    if ((p.info & JICPProtocol.SESSION_ID_PRESENT_INFO) != 0) {
    	p.sessionID = read(in);
    }
    	
    // Read recipient ID if present
    if ((p.info & JICPProtocol.RECIPIENT_ID_PRESENT_INFO) != 0) {
    	int size = (read(in) & 0x000000ff);
    	byte[] bb = new byte[size];
    	in.read(bb, 0, size);
      p.recipientID = new String(bb);
    } 

    // Read data if present
    if ((p.info & JICPProtocol.DATA_PRESENT_INFO) != 0) {
    	int b1 = read(in);
    	int b2 = read(in);
    	int size = ((b2 << 8) & 0x0000ff00) | (b1 & 0x000000ff);
    	int b3 = read(in);
    	int b4 = read(in);
    	size |= ((b4 << 24) & 0xff000000) | ((b3 << 16) & 0x00ff0000);
    	if (size == 0) {
      	p.data = new byte[0];
    	} 
    	else {
      	// Read the actual data
      	p.data = new byte[size];

      	int cnt = 0;
      	int n;
      	do {
        	n = in.read(p.data, cnt, size-cnt);
        	if (n == -1) {
          	throw new EOFException("EOF reading packet data");
        	} 
        	cnt += n;
      	} 
      	while (cnt < size);
    	}
      //Logger.println("JICPPacket read. Type:"+p.type+" Info:"+p.info+" RID:"+p.recipientID+" Data-length:"+(p.data != null ? p.data.length : 0));
    } 

    // DEBUG
    //System.out.println(p.getLength()+" bytes read");
    return p;
  } 

  private static final byte read(InputStream in) throws IOException {
  	int i = in.read();
  	if (i == -1) {
  		throw new EOFException("EOF reading packet header");
  	}
  	return (byte) i;
  }
  
  public int getLength() {
  	int cnt = 2;
    if ((info & JICPProtocol.SESSION_ID_PRESENT_INFO) != 0) {
    	cnt++;
    }
    if ((info & JICPProtocol.RECIPIENT_ID_PRESENT_INFO) != 0) {
    	cnt += (1 + recipientID.getBytes().length);
    }
    if ((info & JICPProtocol.DATA_PRESENT_INFO) != 0) {
    	cnt += (4 + data.length);
    }
    return cnt;
  }
}

