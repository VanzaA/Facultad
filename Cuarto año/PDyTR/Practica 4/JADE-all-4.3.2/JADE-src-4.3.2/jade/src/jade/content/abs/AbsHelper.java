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
package jade.content.abs;

//#APIDOC_EXCLUDE_FILE

import jade.content.onto.*;
import jade.content.*;
import jade.content.schema.*;
import jade.util.leap.HashSet;
import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import jade.util.leap.Set;
import jade.core.AID;
import jade.core.CaseInsensitiveString;
import jade.lang.acl.ACLMessage;


/**
 * @author Federico Bergenti - Universita` di Parma
 * @author Giovanni Caire - TILAB
 */
public class AbsHelper {
	/**
	 * Converts a <code>List</code> into a <code>AbsAggregate</code> using
	 * the specified ontology.
	 * @param obj the <code>List</code>
	 * @param onto the ontology.
	 * @return the abstract descriptor.
	 * @throws OntologyException
	 */
	public static AbsAggregate externaliseList(List obj, Ontology onto, String AggregateType) throws OntologyException {
		AbsAggregate ret = new AbsAggregate(AggregateType);

		try {
			for (int i = 0; i < obj.size(); i++) {
				ret.add((AbsTerm) (onto.fromObject(obj.get(i))));
			}
		}
		catch (ClassCastException cce) {
			throw new OntologyException("Non term object in aggregate");
		}

		return ret;
	} 

	/**
	 * Converts a <code>Set</code> into a <code>AbsAggregate</code> using
	 * the specified ontology.
	 * @param obj the <code>Set</code>
	 * @param onto the ontology.
	 * @return the abstract descriptor.
	 * @throws OntologyException
	 */
	public static AbsAggregate externaliseSet(Set obj, Ontology onto, String AggregateType) throws OntologyException {
		AbsAggregate ret = new AbsAggregate(AggregateType);

		try {
			Iterator it = obj.iterator();
			while(it.hasNext()) {
				ret.add((AbsTerm) (onto.fromObject(it.next())));
			}
		}
		catch (ClassCastException cce) {
			throw new OntologyException("Non term object in aggregate");
		}

		return ret;
	} 

	/**
	 * Converts an <code>Iterator</code> into a <code>AbsAggregate</code> using
	 * the specified ontology.
	 * @param obj the <code>Iterator</code>
	 * @param onto the ontology.
	 * @return the abstract descriptor.
	 * @throws OntologyException
	 */
	//#J2ME_EXCLUDE_BEGIN
	public static AbsAggregate externaliseIterator(java.util.Iterator obj, Ontology onto, String AggregateType) throws OntologyException {
		//#J2ME_EXCLUDE_END
		/*#J2ME_INCLUDE_BEGIN
        public static AbsAggregate externaliseIterator(Iterator obj, Ontology onto, String AggregateType) throws OntologyException {
        #J2ME_INCLUDE_END*/
		AbsAggregate ret = new AbsAggregate(AggregateType);

		try {
			while(obj.hasNext())
				ret.add((AbsTerm) (onto.fromObject(obj.next())));
		}
		catch (ClassCastException cce) {
			throw new OntologyException("Non term object in aggregate");
		}
		return ret;
	}

	/**
	 * Converts an <code>AID</code> into an <code>AbsConcept</code> 
	 * representing an AID 
	 * @param obj the <code>AID</code>
	 * @return the abstract descriptor.
	 */
	public static AbsConcept externaliseAID(AID obj) {
		AbsConcept aid = new AbsConcept(BasicOntology.AID);
		// Name
		aid.set(BasicOntology.AID_NAME, obj.getName());

		// Addresses
		Iterator i = obj.getAllAddresses();
		if (i.hasNext()) {
			AbsAggregate addresses = new AbsAggregate(BasicOntology.SEQUENCE);
			while (i.hasNext()) {
				String addr = (String) i.next();
				addresses.add(AbsPrimitive.wrap(addr));
			}
			aid.set(BasicOntology.AID_ADDRESSES, addresses);
		}
		// Resolvers
		i = obj.getAllResolvers();
		if (i.hasNext()) {
			AbsAggregate resolvers = new AbsAggregate(BasicOntology.SEQUENCE);
			while (i.hasNext()) {
				AID res = (AID) i.next();
				resolvers.add(externaliseAID(res));
			}
			aid.set(BasicOntology.AID_RESOLVERS, resolvers);
		}
		return aid;
	} 

	/**
	 * Converts a <code>ContentElementList</code> into an
	 * <code>AbsContentElementList</code> using
	 * the specified ontology.
	 * @param obj the <code>ContentElementList</code>
	 * @param onto the ontology.
	 * @return the abstract descriptor.
	 * @throws OntologyException
	 */
	public static AbsContentElementList externaliseContentElementList(ContentElementList obj, Ontology onto) throws OntologyException {
		AbsContentElementList ret = new AbsContentElementList();

		try {
			for (int i = 0; i < obj.size(); i++) {
				ret.add((AbsContentElement) (onto.fromObject(obj.get(i))));
			}
		}
		catch (ClassCastException cce) {
			throw new OntologyException("Non content element object in content element list");
		}

		return ret;
	} 

	/**
	 * Converts an <code>ACLMessage</code> into an
	 * <code>AbsAgentAction</code> using
	 * the specified ontology.
	 * @param obj the <code>ACLMessage</code>
	 * @param onto the ontology.
	 * @return the abstract descriptor.
	 * @throws OntologyException
	 */
	public static AbsAgentAction externaliseACLMessage(ACLMessage obj, Ontology onto) throws OntologyException {
		try {
			AbsAgentAction absMsg =  new AbsAgentAction(ACLMessage.getPerformative(obj.getPerformative()));

			absMsg.set(BasicOntology.ACLMSG_SENDER, (AbsTerm) onto.fromObject(obj.getSender()));
			// Receivers
			AbsAggregate recvs = new AbsAggregate(BasicOntology.SEQUENCE);
			Iterator it = obj.getAllReceiver();
			while (it.hasNext()) {
				recvs.add((AbsTerm) onto.fromObject(it.next()));
			}
			if (recvs.size() > 0) {
				absMsg.set(BasicOntology.ACLMSG_RECEIVERS, recvs);
			}
			// Reply_to
			AbsAggregate repls = new AbsAggregate(BasicOntology.SEQUENCE);
			it = obj.getAllReplyTo();
			while (it.hasNext()) {
				repls.add((AbsTerm) onto.fromObject(it.next()));
			}
			if (repls.size() > 0) {
				absMsg.set(BasicOntology.ACLMSG_REPLY_TO, repls);
			}

			absMsg.set(BasicOntology.ACLMSG_LANGUAGE, obj.getLanguage());
			absMsg.set(BasicOntology.ACLMSG_ONTOLOGY, obj.getOntology());
			absMsg.set(BasicOntology.ACLMSG_PROTOCOL, obj.getProtocol());
			absMsg.set(BasicOntology.ACLMSG_IN_REPLY_TO, obj.getInReplyTo());
			absMsg.set(BasicOntology.ACLMSG_REPLY_WITH, obj.getReplyWith());
			absMsg.set(BasicOntology.ACLMSG_CONVERSATION_ID, obj.getConversationId());
			absMsg.set(BasicOntology.ACLMSG_REPLY_BY, obj.getReplyByDate());
			// Content
			if (obj.hasByteSequenceContent()) {
				absMsg.set(BasicOntology.ACLMSG_BYTE_SEQUENCE_CONTENT, obj.getByteSequenceContent());
			}
			else {
				absMsg.set(BasicOntology.ACLMSG_CONTENT, obj.getContent());
			}
			absMsg.set(BasicOntology.ACLMSG_ENCODING, obj.getEncoding());

			return absMsg;
		}
		catch (Exception e) {
			throw new OntologyException("Error externalising ACLMessage", e);
		}
	}


	/**
	 * Converts an <code>AbsAggregate</code> into a List using the 
	 * specified ontology.
	 * @param onto the ontology
	 * @return the List
	 * @throws OntologyException
	 */
	public static List internaliseList(AbsAggregate aggregate, Ontology onto) throws OntologyException {
		List ret = new ArrayList();

		for (int i = 0; i < aggregate.size(); i++) {
			Object element = onto.toObject(aggregate.get(i));
			// Check if the element is a Term, a primitive an AID or a List
			Ontology.checkIsTerm(element);
			ret.add(element);
		}

		return ret;
	} 

	/**
	 * Converts an <code>AbsAggregate</code> into a Set using the 
	 * specified ontology.
	 * @param onto the ontology
	 * @return the Set
	 * @throws OntologyException
	 */
	public static Set internaliseSet(AbsAggregate aggregate, Ontology onto) throws OntologyException {
		Set ret = new HashSet();

		for (int i = 0; i < aggregate.size(); i++) {
			Object element = onto.toObject(aggregate.get(i));
			// Check if the element is a Term, a primitive an AID or a List
			Ontology.checkIsTerm(element);
			ret.add(element);
		}

		return ret;
	} 

	//#MIDP_EXCLUDE_BEGIN
	/**
	 * Converts an <code>AbsAggregate</code> into a List using the 
	 * specified ontology.
	 * @param onto the ontology
	 * @return the List
	 * @throws OntologyException
	 */
	public static java.util.Collection internaliseJavaCollection(AbsAggregate aggregate, Ontology onto) throws OntologyException {
		java.util.Collection collection;

		try {
			collection = (java.util.Collection) Class.forName(aggregate.getTypeName()).newInstance();
		} catch (Exception e) {
			throw new OntologyException("Cannot instantiate java collection of class "+aggregate.getTypeName(), e);
		}
		for (int i = 0; i < aggregate.size(); i++) {
			Object element = onto.toObject(aggregate.get(i));
			// Check if the element is a Term, a primitive an AID or a List
			Ontology.checkIsTerm(element);
			collection.add(element);
		}

		return collection;
	} 
	//#MIDP_EXCLUDE_END

	/**
	 * Converts an <code>AbsConcept</code> representing an AID 
	 * into an OntoAID 
	 * @return the OntoAID
	 * @throws OntologyException if <code>aid</code> does not 
	 * represent a valid AID
	 */
	public static OntoAID internaliseAID(AbsConcept aid) throws OntologyException {
		OntoAID ret = new OntoAID();

		try {
			// Name
			ret.setName(aid.getString(BasicOntology.AID_NAME));

			// Addresses
			AbsAggregate addresses = (AbsAggregate) aid.getAbsObject(BasicOntology.AID_ADDRESSES);
			if (addresses != null) {
				for (int i = 0; i < addresses.size(); ++i) {
					String addr = ((AbsPrimitive) addresses.get(i)).getString();
					ret.addAddresses(addr);
				}
			}
			// Resolvers
			AbsAggregate resolvers = (AbsAggregate) aid.getAbsObject(BasicOntology.AID_RESOLVERS);
			if (resolvers != null) {
				for (int i = 0; i < resolvers.size(); ++i) {
					OntoAID res = internaliseAID((AbsConcept) resolvers.get(i));
					ret.addResolvers(res);
				}
			}
			return ret;
		}
		catch (Exception e) {
			throw new OntologyException(aid+" is not a valid AID");
		}
	} 

	/**
	 * Converts to an <code>AbsContentElementList</code> into a 
	 * ContentElementList using the 
	 * specified ontology.
	 * @param onto the ontology
	 * @return the ContentElementList
	 * @throws OntologyException
	 */
	public static ContentElementList internaliseContentElementList(AbsContentElementList l, Ontology onto) throws OntologyException {
		ContentElementList ret = new ContentElementList();

		try {
			for (int i = 0; i < l.size(); i++) {
				ContentElement element = (ContentElement) onto.toObject(l.get(i));
				ret.add(element);
			}
		}
		catch (ClassCastException cce) {
			throw new OntologyException("Non content element object in content element list");
		}

		return ret;
	} 

	/**
	 * Converts to an <code>AbsAgentAction</code> representing an ACLMessage
	 * into an OntoACLMessage using the specified ontology.
	 * @param onto the ontology
	 * @return the OntoACLMessage
	 * @throws OntologyException
	 */
	public static OntoACLMessage internaliseACLMessage(AbsAgentAction absMsg, Ontology onto) throws OntologyException {
		OntoACLMessage ret = new OntoACLMessage(ACLMessage.getInteger(absMsg.getTypeName()));

		try {
			ret.setSender((AID) onto.toObject(absMsg.getAbsObject(BasicOntology.ACLMSG_SENDER))); 
			// Receivers
			ret.clearAllReceiver();
			List l = (List) onto.toObject(absMsg.getAbsObject(BasicOntology.ACLMSG_RECEIVERS));
			if (l != null) {
				Iterator it = l.iterator();
				while (it.hasNext()) {
					ret.addReceiver((AID) it.next());
				}
			}
			// ReplyTo
			ret.clearAllReplyTo();
			l = (List) onto.toObject(absMsg.getAbsObject(BasicOntology.ACLMSG_REPLY_TO));
			if (l != null) {
				Iterator it = l.iterator();
				while (it.hasNext()) {
					ret.addReplyTo((AID) it.next());
				}
			}
			ret.setLanguage(absMsg.getString(BasicOntology.ACLMSG_LANGUAGE)); 
			ret.setOntology(absMsg.getString(BasicOntology.ACLMSG_ONTOLOGY)); 
			ret.setProtocol(absMsg.getString(BasicOntology.ACLMSG_PROTOCOL)); 
			ret.setInReplyTo(absMsg.getString(BasicOntology.ACLMSG_IN_REPLY_TO)); 
			ret.setReplyWith(absMsg.getString(BasicOntology.ACLMSG_REPLY_WITH)); 
			ret.setConversationId(absMsg.getString(BasicOntology.ACLMSG_CONVERSATION_ID)); 
			ret.setReplyByDate(absMsg.getDate(BasicOntology.ACLMSG_REPLY_BY)); 
			String c = absMsg.getString(BasicOntology.ACLMSG_CONTENT);
			if (c != null) {
				ret.setContent(c);
			}
			else {
				byte[] bsc = absMsg.getByteSequence(BasicOntology.ACLMSG_BYTE_SEQUENCE_CONTENT);
				if (bsc != null) {
					ret.setByteSequenceContent(bsc);
				}
			}
			ret.setEncoding(absMsg.getString(BasicOntology.ACLMSG_ENCODING)); 

			return ret;
		}
		catch (Exception e) {
			throw new OntologyException("Error internalising OntoACLMessage", e);
		}
	}

	/**
	 * Set an attribute in an abstract descriptor performing all 
	 * necessary type checks.
	 * @throws OntologyException if a type mismatch is detected
	 */
	public static void setAttribute(AbsObject abs, String attrName, AbsObject attrValue) throws OntologyException { 
		if (abs.getAbsType() == AbsObject.ABS_AGENT_ACTION) {
			if (attrValue instanceof AbsTerm || attrValue == null) {
				((AbsAgentAction) abs).set(attrName, (AbsTerm) attrValue);
				return;
			}
			if (attrValue instanceof AbsPredicate || attrValue == null) {
				((AbsAgentAction) abs).set(attrName, (AbsPredicate) attrValue);
				return;
			}
		}
		if (abs.getAbsType() == AbsObject.ABS_CONCEPT) {
			if (attrValue instanceof AbsTerm || attrValue == null) {
				((AbsConcept) abs).set(attrName, (AbsTerm) attrValue);
				return;
			}
		}
		else if (abs.getAbsType() == AbsObject.ABS_PREDICATE) {
			((AbsPredicate) abs).set(attrName, attrValue);
			return;
		}
		else if (abs.getAbsType() == AbsObject.ABS_IRE) {
			if (attrValue instanceof AbsVariable && CaseInsensitiveString.equalsIgnoreCase(attrName, IRESchema.VARIABLE)) {
				((AbsIRE) abs).setVariable((AbsVariable) attrValue);
				return;
			}
			else if (attrValue instanceof AbsPredicate && CaseInsensitiveString.equalsIgnoreCase(attrName, IRESchema.PROPOSITION)) {
				((AbsIRE) abs).setProposition((AbsPredicate) attrValue);
				return;
			}
		}
		else if (abs.getAbsType() == AbsObject.ABS_VARIABLE) {
			if (attrValue instanceof AbsPrimitive && CaseInsensitiveString.equalsIgnoreCase(attrName, VariableSchema.NAME)) {
				((AbsVariable) abs).setName(((AbsPrimitive) attrValue).getString());
				return;
			}
			else if (attrValue instanceof AbsPrimitive && CaseInsensitiveString.equalsIgnoreCase(attrName, VariableSchema.VALUE_TYPE)) {
				((AbsVariable) abs).setType(((AbsPrimitive) attrValue).getString());
				return;
			}
		}

		// If we reach this point there is a type incompatibility
		throw new OntologyException("Type incompatibility: value of attribute "+attrName+" of "+abs+" is "+attrValue); 
	}

	
	//#J2ME_EXCLUDE_BEGIN
	/**
	 * Remove all variables and empty aggregates (only if specified)
	 * @param abs to nullify
	 * @param removeEmptyAggregate if true remove all empty aggregates  
	 * @return abs without variables and empty aggregates
	 */
	public static AbsObject nullifyVariables(AbsObject abs, boolean removeEmptyAggregate) {
		// Remove AbsVariable
		if (abs instanceof AbsVariable) {
			return null;
		}
		// Remove empty AbsAggregate
		if (removeEmptyAggregate && abs instanceof AbsAggregate) {
			AbsAggregate absAggregate = (AbsAggregate)abs;
			if (absAggregate.size() == 0) {
				return null;
			}
		}
		// If not grounded -> check all slots
		if (!abs.isGrounded()) {
			
			// Aggregate
			if (abs instanceof AbsAggregate) {
				AbsAggregate absAggregate = (AbsAggregate)abs;
				
				Iterator it = absAggregate.iterator();
				while (it.hasNext()) {
					AbsObject slotValue = (AbsObject)it.next();
					
					AbsObject nullifiedSlotValue = nullifyVariables(slotValue, removeEmptyAggregate);
					if (nullifiedSlotValue == null) {
						// Remove null slot
						it.remove();
					}
				}
				if (removeEmptyAggregate && abs.getCount() == 0) {
					return null;
				}
			} 
			// Concept
			else {
				for (String slotName : abs.getNames()) {
					AbsObject slotValue = abs.getAbsObject(slotName);
					AbsObject nullifiedSlotValue = nullifyVariables(slotValue, removeEmptyAggregate);
					
					// Replace nullified value into the slot
					((AbsConcept)abs).set(slotName, nullifiedSlotValue);
				}
			}
		}
		return abs;
	}
	
	/**
	 * Recursively removes prefix (if present) from abs-type-name, except for BasicOntology types
	 * Prefix is identified from last separator string
	 * Eg. A.B.C -> C (if separator is .)
	 * @param abs abs to un-prefix
	 * @param separator separator string
	 * @return un-prefixde abs
	 * @throws OntologyException
	 */
	public static AbsObject removePrefix(AbsObject abs, String separator) throws OntologyException {
		if (abs instanceof AbsConcept || abs instanceof AbsAggregate) {
			String typeName = abs.getTypeName();
			
			// If the abs schema is a BasicOntology schema don't try to remove prefix
			// ex. SEQUENCE
			if (BasicOntology.getInstance().getSchema(typeName) == null) {
				
				// Check if separator is present
				int separatorPos = typeName.lastIndexOf(separator);
				if (separatorPos != -1 && separatorPos < typeName.length()) {
					// Remove prefix 
					typeName = typeName.substring(separatorPos+separator.length());
	
					// Set un-prefixed typeName  
					((AbsConcept)abs).setTypeName(typeName);
				}
			}
			
			for (String slotName : abs.getNames()) {
				removePrefix(abs.getAbsObject(slotName), separator);
			}
		}
		return abs;
	}
	
	/**
	 * Recursively add prefix to abs-type-name, except for BasicOntology types
	 * Eg. C -> A.B.C (if prefix is A.B.)
	 * @param abs abs to prefix
	 * @param prefix prefix to add
	 * @return prefix abs
	 * @throws OntologyException
	 */
	public static AbsObject addPrefix(AbsObject abs, String prefix) throws OntologyException {
		if (abs instanceof AbsConcept || abs instanceof AbsAggregate) {
			String typeName = abs.getTypeName();
			
			// If the abs schema is a BasicOntology schema don't add prefix
			// ex. SEQUENCE
			if (BasicOntology.getInstance().getSchema(typeName) == null) {
				((AbsConcept)abs).setTypeName(prefix+typeName);
			}
			
			for (String slotName : abs.getNames()) {
				addPrefix(abs.getAbsObject(slotName), prefix);
			}
		}
		return abs;
	}
	
	/**
	 * Return true if the abs-object is a template.
	 * A template is an abs with all slots AbsVariable or AbsAggregate empty
	 * @param abs abs-object to check
	 * @return true if is a template
	 */
	public static boolean isAbsTemplate(AbsObject abs) {
		if (abs instanceof AbsPrimitive) {
			return false;
		}
		
		if (abs instanceof AbsAggregate) {
			Iterator it = ((AbsAggregate)abs).iterator();
			while (it.hasNext()) {
				if (!isAbsTemplate((AbsObject)it.next())) {
					return false;
				}
			}
		}

		if (abs instanceof AbsConcept) {
			for (String slotName : abs.getNames()) {
				if (!isAbsTemplate(abs.getAbsObject(slotName))) {
					return false;
				}
			}
		}

		return true;
	}
	
	/**
	 * Apply default value to template
	 * Replace node with default value only if the value in template is null or AbsVariable or 
	 * default value is null and template is a true template 
	 * @param absTemplate abs template value 
	 * @param absDefault abs value with default value
	 * @return
	 * @throws OntologyException 
	 */
	public static AbsObject applyDefaultValues(AbsObject absTemplate, AbsObject absDefault) throws OntologyException {

		// Aggregate not empty but all filled -> ignore default
		if (absTemplate instanceof AbsAggregate && absTemplate.getCount() != 0 && absTemplate.isGrounded()) {
			return absTemplate;
		}
		
		// Template is already all filled -> ignore default
		if (!(absTemplate instanceof AbsAggregate) && absTemplate.isGrounded()) {
			return absTemplate; 
		}
		
		// Template slot null or variable -> replace with default 
		if (absTemplate == null || absTemplate instanceof AbsVariable) {
			return absDefault; 
		}

		// Manage default value null  
		if (absDefault == null) {
			if (isAbsTemplate(absTemplate)) {
				return null;
			} else {
				return absTemplate;
			}
		}

		// Here the template is a concept -> check compatibility 
		if (!(absDefault instanceof AbsConcept)) {
			throw new OntologyException("Default abs structure ("+absDefault.getAbsType()+") not compatible with template ("+absTemplate.getAbsType()+")");
		}
		
		// Loop all slots
		for (String slotName : absTemplate.getNames()) {
			AbsObject slotTemplateValue = absTemplate.getAbsObject(slotName);
			AbsObject absDefaultValue = absDefault.getAbsObject(slotName);
			if (absDefaultValue != null) {
				AbsHelper.setAttribute(absTemplate, slotName, applyDefaultValues(slotTemplateValue, absDefaultValue));
			}
		}
		return absTemplate;
	}
	
	/**
	 * Generate an AbsObject consistently with class. 
	 * @param clazz class to convert
	 * @param onto reference ontology
	 * @return abs-object
	 * @throws OntologyException
	 */
	public static AbsObject createAbsTemplate(Class clazz, Ontology onto) throws OntologyException {
		// Convert class into schema
		// Try to get associated schema from ontology
		ObjectSchema schema = onto.getSchema(clazz);
		
		// If no schema found, try as an aggregate 
		if (schema == null) {
			schema = AggregateHelper.getSchema(clazz, null);
		}
		
		return createAbsTemplate(schema, null, VarIndexWrapper.ZERO);
	}
	
	/**
	 * Generate an AbsObject consistently with schema. 
	 * @param schema to convert
	 * @return abs-object
	 * @throws Exception
	 */
	public static AbsObject createAbsTemplate(ObjectSchema schema) throws OntologyException {
		return createAbsTemplate(schema, null, VarIndexWrapper.ZERO);
	}
	
	/**
	 * Generate an AbsObject consistently with schema.
	 * All variables are prefixed 
	 * @param schema to convert
	 * @param prefix for variable
	 * @return abs-object
	 * @throws Exception
	 */
	public static AbsObject createAbsTemplate(ObjectSchema schema, String prefix) throws OntologyException {
		return createAbsTemplate(schema, prefix, VarIndexWrapper.ZERO);
	}

	private static AbsObject createAbsTemplate(ObjectSchema schema, String prefix, VarIndexWrapper viw) throws OntologyException {
		
		// For primitive schemas and TermSchema (bean object type) 
		if (schema instanceof PrimitiveSchema || schema.getClass() == TermSchema.class) {
			return new AbsVariable(createVariableName(prefix, viw), schema.getTypeName());
		}

		if (schema instanceof AggregateSchema) {
			AggregateSchema aggregateSchema = (AggregateSchema)schema; 
			AbsAggregate aggregate = new AbsAggregate(aggregateSchema.getTypeName());
			
			// If is present the element schema add this information in aggregate
			ObjectSchema elementsSchema = aggregateSchema.getElementsSchema();
			if (elementsSchema != null) {
				aggregate.setElementTemplate((AbsTerm)createAbsTemplate(elementsSchema, prefix, viw));
			}
			return aggregate;
		}
		
		AbsObject abs = schema.newInstance();
		for (String slotName : schema.getNames()) {
			ObjectSchema slotSchema = schema.getSchema(slotName);
			setAttribute(abs, slotName, createAbsTemplate(slotSchema, prefix, viw));
		}
		return abs;
	}

	private static String createVariableName(final String prefix, VarIndexWrapper viw) {
		String varName = (prefix!=null ? prefix : "") + "v#" + viw.index;
		viw.index++;
		return varName;
	}

	
	/**
	 * Inner class used to manage variable name generation 
	 */
	static class VarIndexWrapper {
		static VarIndexWrapper ZERO = new VarIndexWrapper(0); 
		int index;
		public VarIndexWrapper(int index) {
			this.index = index;
		}
	}
	//#J2ME_EXCLUDE_END
}


