/**
 * ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
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
package jade.content.frame;

import java.util.Vector;
import java.util.Enumeration;
import java.util.Date;
import java.io.*;

import jade.core.AID;
import jade.content.lang.sl.SL0Vocabulary;
import jade.util.leap.Iterator;

/**
   @author Giovanni Caire - TILAB
 */
public class LEAPFrameCodec implements jade.util.leap.Serializable {
	public static final String NAME = "LEAP";

	private transient ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
	private transient DataOutputStream      outStream = new DataOutputStream(outBuffer);
	private transient Vector             stringReferences = new Vector();
	//#MIDP_EXCLUDE_BEGIN
	private void readObject(java.io.ObjectInputStream oin) throws java.io.IOException, ClassNotFoundException {
		oin.defaultReadObject();
		outBuffer = new ByteArrayOutputStream();
		outStream = new DataOutputStream(outBuffer);
		stringReferences = new Vector();
	}
	//#MIDP_EXCLUDE_END

	// Primitive types
	private static final byte  STRING = 6;
	private static final byte  BOOLEAN = 7;
	private static final byte  INTEGER = 8;
	private static final byte  FLOAT = 9;
	private static final byte  DATE = 10;
	private static final byte  BYTE_SEQUENCE = 11;

	// Structured types
	private static final byte  AGGREGATE = 1;
	private static final byte  CONTENT_ELEMENT_LIST = 2;
	private static final byte  OBJECT = 3;

	// Markers for structured types
	private static final byte  ELEMENT = 4;
	private static final byte  END = 5;

	// Modifiers for string encoding
	private static final byte  MODIFIER = (byte) 0x10; // Only bit five set to 1
	private static final byte  UNMODIFIER = (byte) 0xEF; // Only bit five cleared to 1

	/**
     Transform a Frame into a sequence of bytes encoded according to the
     LEAP language
     @param content The Frame to be transformed
     @throws FrameException
	 */
	public synchronized byte[] encode(Frame content) throws FrameException {
		if (content == null) {
			return null;
		}
		try {
			write(outStream, content);
			return outBuffer.toByteArray();
		} 
		catch (FrameException fe) {
			throw fe;
		}
		catch (Throwable t) {
			throw new FrameException("Error encoding content", t);
		}
		finally {
			outBuffer.reset();
			stringReferences.removeAllElements();
		}
	} 

	/**
     Transform a sequence of bytes encoded according to the LEAP 
     language into a Frame 
     @param content The sequence of bytes to be transformed.
     @throws FrameException
	 */
	public synchronized Frame decode(byte[] content) throws FrameException {
		if (content == null || content.length == 0) {
			return null;
		}
		ByteArrayInputStream inpBuffer = null;
		DataInputStream      inpStream = null;
		try {
			inpBuffer = new ByteArrayInputStream(content);
			inpStream = new DataInputStream(inpBuffer);

			return (Frame) read(inpStream);
		} 
		catch (FrameException fe) {
			//fe.printStackTrace();
			throw fe;
		}
		catch (Throwable t) {
			//t.printStackTrace();
			throw new FrameException("Error decoding content", t);
		}
		finally {
			try {
				inpStream.close();
				inpBuffer.close();
			}
			catch (IOException ioe) {
				ioe.printStackTrace();
			}
			stringReferences.removeAllElements();
		}
	} 

	/**
	 */
	private void write(DataOutputStream stream, Object obj) throws Throwable {
		// PRIMITIVES
		if (obj instanceof String) {
			writeString(stream, STRING, (String) obj);
		}
		else if (obj instanceof Boolean) {
			stream.writeByte(BOOLEAN);
			stream.writeBoolean(((Boolean) obj).booleanValue());
		}
		else if (obj instanceof Long) {
			stream.writeByte(INTEGER);
			stream.writeLong(((Long) obj).longValue());
		}
		//#MIDP_EXCLUDE_BEGIN
		else if (obj instanceof Double) {
			stream.writeByte(FLOAT);
			stream.writeDouble(((Double) obj).doubleValue());
		} 
		//#MIDP_EXCLUDE_END
		else if (obj instanceof Date) {
			stream.writeByte(DATE);
			stream.writeLong(((Date) obj).getTime());
		}
		else if (obj instanceof byte[]) {
			stream.writeByte(BYTE_SEQUENCE);
			byte[] b = (byte[]) obj;
			stream.writeInt(b.length);
			stream.write(b, 0, b.length);
		}

		// ORDERED FRAME
		else if (obj instanceof OrderedFrame) {
			OrderedFrame f = (OrderedFrame) obj;
			int size = f.size();
			String typeName = f.getTypeName();
			if (typeName != null) {
				// AGGREGATE
				writeString(stream, AGGREGATE, typeName);
			}
			else {
				// CONTENT_ELEMENT_LIST
				stream.writeByte(CONTENT_ELEMENT_LIST);
			}
			for (int i = 0; i < size; i++) {
				stream.writeByte(ELEMENT);
				write(stream, f.elementAt(i));
			} 
			stream.writeByte(END);
		}

		// QUALIFIED_FRAME
		else if (obj instanceof QualifiedFrame) {
			QualifiedFrame f = (QualifiedFrame) obj;
			writeString(stream, OBJECT, f.getTypeName());

			Enumeration e = f.keys();
			while (e.hasMoreElements()) {
				String key = (String) e.nextElement();
				writeString(stream, ELEMENT, key);
				write(stream, f.get(key));
			} 
			stream.writeByte(END);
		}

		// AID
		else if (obj instanceof AID) {
			// Convert the AID into a qualified frame and call write() again
			write(stream, aidToFrame((AID) obj));
		}

		else {
			throw new FrameException("Object "+obj+" cannot be encoded");
		}
	} 

	/**
	 */
	private Object read(DataInputStream stream) throws Throwable {
		Object obj = null;  
		byte type = stream.readByte();

		// PRIMITIVES
		if ((type&UNMODIFIER) == STRING) {
			obj = readString(stream, type);
		}
		else if (type == BOOLEAN) {
			obj = new Boolean(stream.readBoolean());
		}
		else if (type == INTEGER) {
			obj = new Long(stream.readLong());
		}
		//#MIDP_EXCLUDE_BEGIN
		else if (type == FLOAT) {
			obj = new Double(stream.readDouble());
		} 
		//#MIDP_EXCLUDE_END
		else if (type == DATE) {
			obj = new Date(stream.readLong());
		}
		else if (type == BYTE_SEQUENCE) {
			int length = stream.readInt();
			obj = new byte[length];
			stream.read((byte[]) obj, 0, length);
		}

		// AGGREGATE
		else if ((type&UNMODIFIER) == AGGREGATE) {
			String typeName = readString(stream, type);
			OrderedFrame f = new OrderedFrame(typeName);
			fillOrderedFrame(stream, f);
			obj = f;
		} 

		// CONTENT_ELEMENT_LIST
		else if (type == CONTENT_ELEMENT_LIST) {
			OrderedFrame f = new OrderedFrame(null);
			fillOrderedFrame(stream, f);
			obj = f;
		}

		// QUALIFIED_FRAME
		else if ((type&UNMODIFIER) == OBJECT) {
			String typeName = readString(stream, type);
			QualifiedFrame f = new QualifiedFrame(typeName);

			byte marker = stream.readByte();
			do {
				if ((marker&UNMODIFIER) == ELEMENT) {
					String elementName = readString(stream, marker);
					Object elementVal = read(stream);
					f.put(elementName, elementVal);
					marker = stream.readByte();
				}
			} 
			while (marker != END);

			// If this QualifiedFrame represents an AID, convert it
			if (f.getTypeName().equals(SL0Vocabulary.AID)) {
				obj = frameToAid(f);
			}
			else {
				obj = f;
			}
		}
		else {
			throw new FrameException("Unexpected tag "+type);
		}

		return obj;
	} 

	private void fillOrderedFrame(DataInputStream stream, OrderedFrame f) throws Throwable {
		byte marker = stream.readByte();
		do {
			if (marker == ELEMENT) {
				Object elementVal = read(stream);
				f.addElement(elementVal);
				marker = stream.readByte();
			}
		} 
		while (marker != END);
	}

	private final void writeString(DataOutputStream stream, byte tag, String s) throws Throwable {
		int index = stringReferences.indexOf(s);
		if (index >= 0) {
			// Write the tag modified and just put the index
			stream.writeByte(tag|MODIFIER);
			stream.writeByte(index);

		}
		else {
			stream.writeByte(tag);
			stream.writeUTF(s);
			if ((s.length() > 1) && (stringReferences.size() < 256)) {
				stringReferences.addElement(s);
				//System.out.println("writeString: added:"+s);
			}
		}
	}

	private final String readString(DataInputStream stream, byte tag) throws Throwable {
		if ((tag&MODIFIER) != 0) {
			int index = stream.readUnsignedByte();
			String s = (String) stringReferences.elementAt(index);
			return s;
		}
		else {
			String s = stream.readUTF();
			if ((s.length() > 1) && (stringReferences.size() < 256)) {
				stringReferences.addElement(s);
			}
			return s;
		}
	}

	private final QualifiedFrame aidToFrame(AID id) {
		QualifiedFrame f = new QualifiedFrame(SL0Vocabulary.AID);
		// Name
		f.put(SL0Vocabulary.AID_NAME, id.getName());

		// Addresses
		Iterator i = id.getAllAddresses();
		if (i.hasNext()) {
			OrderedFrame addresses = new OrderedFrame(SL0Vocabulary.SEQUENCE);
			while (i.hasNext()) {
				addresses.addElement(i.next());
			}
			f.put(SL0Vocabulary.AID_ADDRESSES, addresses);
		}
		// Resolvers
		i = id.getAllResolvers();
		if (i.hasNext()) {
			OrderedFrame resolvers = new OrderedFrame(SL0Vocabulary.SEQUENCE);
			while (i.hasNext()) {
				AID res = (AID) i.next();
				resolvers.addElement(aidToFrame(res));
			}
			f.put(SL0Vocabulary.AID_RESOLVERS, resolvers);
		}
		return f;
	}

	private final AID frameToAid(QualifiedFrame f) {
		// Name
		AID id = new AID((String) f.get(SL0Vocabulary.AID_NAME), AID.ISGUID);

		// Addresses
		OrderedFrame addresses = (OrderedFrame) f.get(SL0Vocabulary.AID_ADDRESSES);
		if (addresses != null) {
			for (int i = 0; i < addresses.size(); ++i) {
				id.addAddresses((String) addresses.elementAt(i));
			}
		}
		// Resolvers
		OrderedFrame resolvers = (OrderedFrame) f.get(SL0Vocabulary.AID_RESOLVERS);
		if (resolvers != null) {
			for (int i = 0; i < resolvers.size(); ++i) {
				AID res = frameToAid((QualifiedFrame) resolvers.elementAt(i));
				id.addResolvers(res);
			}
		}
		return id;
	}
}

