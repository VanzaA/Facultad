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
import jade.core.CaseInsensitiveString;
import jade.content.lang.sl.SL0Vocabulary;
import jade.content.lang.sl.SimpleSLTokenizer;
import jade.util.leap.Iterator;
import jade.lang.acl.ISO8601;

/**
   @author Giovanni Caire - TILAB
   @version $Date: 2010-04-08 15:54:18 +0200 (gio, 08 apr 2010) $ $Revision: 6298 $
 */
public class SLFrameCodec implements jade.util.leap.Serializable {
	public static final String NAME = "FIPA-SL";

	/**
     Transform a Frame into a String encoded according to the
     SL language
     @param content The Frame to be transformed
     @throws FrameException
	 */
	public String encode(Frame content) throws FrameException {
		if (content == null) {
			return null;
		}
		try {
			StringBuffer sb = new StringBuffer();
			write(sb, content);
			return sb.toString();
		} 
		catch (FrameException fe) {
			throw fe;
		}
		catch (Throwable t) {
			throw new FrameException("Error encoding content", t);
		}
	} 

	/**
     Transform a String encoded according to the SL 
     language into a Frame 
     @param content The String to be transformed.
     @throws FrameException
	 */
	public Frame decode(String content) throws FrameException {
		if (content == null || content.length() == 0) {
			return null;
		}
		try {
			return (Frame) read(new SimpleSLTokenizer(content));
		} 
		catch (FrameException fe) {
			throw fe;
		}
		catch (Throwable t) {
			throw new FrameException("Error decoding content", t);
		}
	} 

	/**
	 */
	private void write(StringBuffer sb, Object obj) throws Throwable {
		if (obj instanceof AID) {
			obj = aidToFrame((AID) obj);
		}

		if (obj instanceof QualifiedFrame) {
			writeQualified(sb, (QualifiedFrame) obj);
		}
		else if (obj instanceof OrderedFrame) {
			writeOrdered(sb, (OrderedFrame) obj);
		}
		else if (obj instanceof Date) {
			sb.append(ISO8601.toString((Date) obj));
		}
		else if (obj instanceof Integer || obj instanceof Long || obj instanceof Boolean) {
			sb.append(obj);
		}
		//#MIDP_EXCLUDE_BEGIN
		else if (obj instanceof Double) {
			sb.append(obj);
		}
		//#MIDP_EXCLUDE_END
		else if (obj instanceof byte[]) {
			byte[] b = (byte[]) obj;
			sb.append('#');
			sb.append(b.length);
			sb.append('"');
			// FIXME: Should we use Base64 encoding?
			sb.append(new String(b));
			sb.append('"');
		}
		else if (obj instanceof String) {
			String s = (String)obj;
			if (CaseInsensitiveString.equalsIgnoreCase("true",s) || CaseInsensitiveString.equalsIgnoreCase("false",s)) {
				s = '"'+s+'"'; // quote it to avoid confusion with the boolean primitives
			} else if (!SimpleSLTokenizer.isAWord(s)) {
				s = SimpleSLTokenizer.quoteString(s);
			}
			sb.append(s);
		}
		else {
			throw new FrameException("Can't encode "+obj+" of class "+obj.getClass().getName());
		}
	}

	private void writeQualified(StringBuffer sb, QualifiedFrame qf) throws Throwable {
		sb.append('(');
		sb.append(qf.getTypeName());
		Enumeration e = qf.keys();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			sb.append(" :");
			sb.append(key);
			sb.append(' ');
			write(sb, qf.get(key));
		}
		sb.append(')');
	}

	private void writeOrdered(StringBuffer sb, OrderedFrame of) throws Throwable {
		sb.append('(');
		sb.append(of.getTypeName());
		Enumeration e = of.elements();
		while (e.hasMoreElements()) {
			sb.append(' ');
			write(sb, e.nextElement());
		}
		sb.append(')');
	}


	/**
	 */
	private Object read(SimpleSLTokenizer st) throws Throwable {
		if (st.nextToken().equals("(")) {
			st.consumeChar('(');
			String typeName = st.getElement();
			if (st.nextToken().startsWith(":")) {
				QualifiedFrame qf = new QualifiedFrame(typeName);
				fillQualified(st, qf);
				// If this QualifiedFrame represents an AID, convert it
				if (qf.getTypeName().equals(SL0Vocabulary.AID)) {
					return frameToAid(qf);
				}
				else {
					return qf;
				}
			}
			else {
				OrderedFrame of = new OrderedFrame(typeName);
				fillOrdered(st, of);
				return of;
			}
		}
		else {
			String val = st.getElement();
			// Long
			try {
				return new Long(Long.parseLong(val));
			}
			catch (Exception e) {
			}
			//#MIDP_EXCLUDE_BEGIN
			// Float
			try {
				// Note that Double.parseDouble() does not exist in PJava
				return Double.valueOf(val);
			}
			catch (Exception e) {
			}
			//#MIDP_EXCLUDE_END
			// Date
			try {
				return ISO8601.toDate(val);
			}
			catch (Exception e) {
			}
			// Boolean
			if (val.equals("true")) {
				return new Boolean(true);
			}
			if (val.equals("false")) {
				return new Boolean(false);
			}
			// String
			return val;
		}		
	} 

	private void fillQualified(SimpleSLTokenizer st, QualifiedFrame qf) throws Throwable {
		while (!st.nextToken().equals(")")) {
			String key = st.getElement();
			Object val = read(st);
			qf.put(key, val);
		}
		st.consumeChar(')');
	}

	private void fillOrdered(SimpleSLTokenizer st, OrderedFrame of) throws Throwable {
		while (!st.nextToken().equals(")")) {
			of.addElement(read(st));
		}
		st.consumeChar(')');
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

