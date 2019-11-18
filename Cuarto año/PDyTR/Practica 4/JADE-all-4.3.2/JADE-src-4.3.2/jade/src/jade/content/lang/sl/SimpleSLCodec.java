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
package jade.content.lang.sl;

import jade.core.CaseInsensitiveString;
import jade.content.*;
import jade.content.abs.*;
import jade.content.schema.ObjectSchema;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.BasicOntology;
import jade.content.lang.StringCodec;
import jade.content.lang.sl.SLVocabulary;
import jade.lang.acl.ISO8601;
import jade.util.leap.Iterator;
import java.util.Date;
import jade.util.Logger;
/**
 * MIDP implementation of the SLCodec. Actually the MIDP version of the SLCodec just extends SimpleSLCodec
 * @version $Date: 2013-02-25 11:10:22 +0100 (lun, 25 feb 2013) $ $Revision: 6642 $
 **/
public class SimpleSLCodec extends StringCodec {
	private int indent = 0;

	private Logger logger = Logger.getMyLogger(this.getClass().getName());

	public SimpleSLCodec() {
		super(jade.domain.FIPANames.ContentLanguage.FIPA_SL);
	}

	/**
	 * Encodes a content into a string using a given ontology.
	 * @param ontology the ontology
	 * @param content the content as an abstract descriptor.
	 * @return the content as a string.
	 * @throws CodecException
	 */
	public String encode(Ontology ontology, AbsContentElement content) throws CodecException {
		StringBuffer str = new StringBuffer("(");
		if (content instanceof AbsContentElementList) {
			for (Iterator i=((AbsContentElementList)content).iterator(); i.hasNext(); ) {
				AbsObject abs = (AbsObject) i.next();
				stringify(abs, ontology, str);
				str.append(" ");
			}
		}
		else {
			stringify(content, ontology, str);
		}
		str.append(")");
		return str.toString();
	}

	private void stringify(AbsObject val, Ontology onto, StringBuffer str) throws CodecException {
		if (val instanceof AbsPrimitive)
			stringifyPrimitive((AbsPrimitive) val, str);
		else if (val instanceof AbsVariable)
			stringifyVariable((AbsVariable) val, str);
		else if (val instanceof AbsAggregate)
			stringifyAggregate((AbsAggregate)val, onto, str);
		else
			stringifyComplex(val, onto, str);
	}

	private void stringifyComplex(AbsObject val, Ontology onto, StringBuffer str) throws CodecException {
		str.append("(");
		str.append(val.getTypeName());
		ObjectSchema s = null;
		try {
			s = onto.getSchema(val.getTypeName());
		}
		catch (OntologyException oe) {
			throw new CodecException("Error getting the schema for element "+val, oe);
		}
		if (val instanceof AbsConcept && !s.getEncodingByOrder()) {
			encodeSlotsByName(val, val.getNames(), onto, str);
		}
		else {
			encodeSlotsByOrder(val, s.getNames(), onto, str);
		}
		str.append(")");
	}

	/**
	 * Encode the slots of an abstract descriptor by order, i.e.
	 * without writing the slot names. Also take into account that, in
	 * order to ensure a correct parsing, empty slots can only occur at
	 * the end.
	 */
	private void encodeSlotsByOrder(AbsObject val, String[] slotNames, Ontology onto, StringBuffer str) throws CodecException {
		boolean lastSlotEmpty = false;
		for (int i=0; i<slotNames.length; i++) {
			AbsObject s = val.getAbsObject(slotNames[i]);
			if (s != null) {
				if (lastSlotEmpty) {
					throw new CodecException("Non-empty slot "+slotNames[i]+" follows empty slot "+slotNames[i-1]);
				}
				str.append(" ");
				stringify(s, onto, str);
			}
			else {
				lastSlotEmpty = true;
			}
		}
	}

	/**
	 * Encode the slots of an abstract descriptor by name, i.e.
	 * writing for each non-empty slot the slot name followed by the
	 * slot value.
	 */
	private void encodeSlotsByName(AbsObject val, String[] slotNames, Ontology onto, StringBuffer str) throws CodecException {
		for (int i=0; i<slotNames.length; i++) {
			AbsObject s = val.getAbsObject(slotNames[i]);
			if (s != null) {
				str.append(" :");
				str.append(slotNames[i]);
				str.append(" ");
				stringify(s, onto, str);
			}
		}
	}

	private void stringifyAggregate(AbsAggregate val, Ontology onto, StringBuffer str) throws CodecException {
		str.append("(");
		str.append(val.getTypeName());
		for (Iterator i=val.iterator(); i.hasNext(); ) {
			str.append(" ");
			stringify((AbsObject)i.next(), onto, str);
		}
		str.append(")");
	}

	private void stringifyVariable(AbsVariable val, StringBuffer str) throws CodecException {
		str.append("?");
		str.append(val.getName());
	}

	private void stringifyPrimitive(AbsPrimitive val, StringBuffer str) throws CodecException {
		String type = val.getTypeName();
		if (type.equals(BasicOntology.STRING)) {
			String s = val.getString();
			if (CaseInsensitiveString.equalsIgnoreCase("true",s) || CaseInsensitiveString.equalsIgnoreCase("false",s)) {
				s = '"'+s+'"'; // quote it to avoid confusion with the boolean primitives
			} else if (!SimpleSLTokenizer.isAWord(s)) {
				s = SimpleSLTokenizer.quoteString(s);
			}
			str.append(s);
		}
		else if (type.equals(BasicOntology.DATE))
			str.append(ISO8601.toString(val.getDate()));
		else if (type.equals(BasicOntology.BYTE_SEQUENCE))
			throw new CodecException("SL_does_not_allow_encoding_sequencesOfBytes");
		else
			str.append(val.getObject().toString());
	}

	/**
	 * Decodes a content expression to an abstract description using a
	 * given ontology.
	 * @param ontology the ontology.
	 * @param content the content as a string.
	 * @return the content as an abstract description.
	 * @throws CodecException
	 */
	public AbsContentElement decode(Ontology ontology, String content) throws CodecException {
		SimpleSLTokenizer p = new SimpleSLTokenizer(content);
		try {
			p.consumeChar('(');
			AbsContentElement abs = (AbsContentElement) parse(p, ontology);
			if (!p.nextToken().equals(")")) {
				AbsContentElementList l = new AbsContentElementList();
				l.add(abs);
				do {
					AbsContentElement abs1 = (AbsContentElement) parse(p, ontology);
					l.add(abs1);
				} while (!p.nextToken().equals(")"));
				abs = l;
			}
			p.consumeChar(')');
			return abs;
		}
		catch (ClassCastException cce) {
			throw new CodecException("Error converting to AbsContentElement", cce);
		}
	}

	public AbsObject decodeObject(Ontology ontology, String content) throws CodecException {
		SimpleSLTokenizer p = new SimpleSLTokenizer(content);
		try {
			return parse(p, ontology);
		}
		catch (ClassCastException cce) {
			throw new CodecException("Error converting to AbsContentElement", cce);
		}
	}

	private AbsObject parse(SimpleSLTokenizer p, Ontology o) throws CodecException {
		AbsObject abs = null;
		if (p.isOpenBracket()) {
			abs = parseComplex(p, o);
		}
		else {
			abs = parseSimple(p);
		}
		return abs;
	}

	private AbsObject parseComplex(SimpleSLTokenizer p, Ontology o) throws CodecException {
		AbsObject abs = null;
		p.consumeChar('(');
		String name = p.getElement();
		if(logger.isLoggable(Logger.FINE))
			logger.log(Logger.FINE,"Parse complex descriptor: "+name);
		++indent;
		try {
			ObjectSchema s = o.getSchema(name);
			if (s != null) {
				abs = s.newInstance();
				if (abs instanceof AbsAggregate) {
					fillAggregate((AbsAggregate) abs, p, o);
				}
				else if (p.nextToken().startsWith(":")) {
					fillSlotsByName((AbsConcept) abs, p, o);
				}
				else {
					fillSlotsByOrder(abs, s, p, o);
				}
			}
			else {
				throw new CodecException("No schema found for element "+name);
			}
		}
		catch (CodecException ce) {
			throw ce;
		}
		catch (Throwable t) {
			throw new CodecException("Unexpeceted error parsing "+name, t);
		}
		indent--;
		p.consumeChar(')');
		if(logger.isLoggable(Logger.FINE))
			logger.log(Logger.FINE,abs.toString());
		return abs;
	}

	private void fillSlotsByOrder(AbsObject abs, ObjectSchema s, SimpleSLTokenizer p, Ontology o) throws CodecException {
		String[] slotNames = s.getNames();
		int i = 0;
		while (!p.isClosedBracket()) {
			AbsObject val = parse(p, o);
			try {
				AbsHelper.setAttribute(abs, slotNames[i], val);
				++i;
			}
			catch (OntologyException oe) {
				throw new CodecException("Can't assign "+val+" to slot "+slotNames[i]+" of "+abs);
			}
		}
	}

	private void fillSlotsByName(AbsConcept abs, SimpleSLTokenizer p, Ontology o) throws CodecException {
		while (!p.isClosedBracket()) {
			String slotName = p.getElement();
			try {
				AbsTerm val = (AbsTerm) parse(p, o);
				abs.set(slotName, val);
			}
			catch (ClassCastException cce) {
				throw new CodecException("Non Term value for slot "+slotName+" of Concept "+abs);
			}
		}
	}

	private void fillAggregate(AbsAggregate abs, SimpleSLTokenizer p, Ontology o) throws CodecException {
		int i = 0;
		while (!p.isClosedBracket()) {
			try {
				AbsTerm val = (AbsTerm) parse(p, o);
				abs.add(val);
				++i;
			}
			catch (ClassCastException cce) {
				throw new CodecException("Non Term value for element "+i+" of Aggregate "+abs);
			}
		}
	}

	private AbsObject parseSimple(SimpleSLTokenizer p) throws CodecException {
		String val = p.getElement();
		if(logger.isLoggable(Logger.FINE))
			logger.log(Logger.FINE,"Parse simple descriptor: "+val+". Next is "+p.nextToken());
		try {
			return AbsPrimitive.wrap(Long.parseLong(val));
		}
		catch (Exception e) {
		}
		//#MIDP_EXCLUDE_BEGIN
		// Float
		try {
			// Note that Double.parseDouble() does not exist in PJava
			return AbsPrimitive.wrap(Double.valueOf(val).doubleValue());
		}
		catch (Exception e) {
		}
		//#MIDP_EXCLUDE_END
		// Date
		try {
			return AbsPrimitive.wrap(ISO8601.toDate(val));
		}
		catch (Exception e) {
		}
		// Boolean
		if (val.equals("true")) {
			return AbsPrimitive.wrap(true);
		}
		if (val.equals("false")) {
			return AbsPrimitive.wrap(false);
		}
		// Variable
		if (val.startsWith("?")) {
			return new AbsVariable(val.substring(1, val.length()), null);
		}
		// String
		if (val.startsWith("\"")) {
			return AbsPrimitive.wrap(val.substring(1, val.length()-1));
		}
		else {
			return AbsPrimitive.wrap(val);
		}
	}

	/**
	 */
	public AbsContentElement decode(String content) throws CodecException {
		throw new CodecException("Unsupported operation");
	}

	/**
	 */
	public String encode(AbsContentElement content) throws CodecException {
		throw new CodecException("Unsupported operation");
	}



	public Ontology getInnerOntology() {
		return SLOntology.getInstance();
	}

}
