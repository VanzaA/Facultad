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
package jade.content.onto;

import jade.content.*;
import jade.content.schema.*;
import jade.content.abs.*;
import jade.content.ContentElementList;
import jade.content.onto.basic.*;
import jade.content.lang.sl.SL0Vocabulary;
import jade.core.AID;
import jade.core.CaseInsensitiveString;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.ISO8601;
import jade.util.leap.*;

import java.util.Date;


/**
 * This class implements an ontology containing schemas for 
 * Primitive types and SL0 operators i.e. basic ontological elements 
 * required for minimal agent interaction.
 * Users should always extend this ontology when defining their 
 * ontologies.
 * @author Federico Bergenti - Universita` di Parma
 * @author Giovanni Caire - TILAB
 */
public class BasicOntology extends Ontology implements SL0Vocabulary {

	// The singleton instance of this ontology
	private static final BasicOntology theInstance = new BasicOntology();
	static {
		theInstance.initialize();
	}

	// Primitive types names
	public static final String         STRING = "BO_String";
	public static final String         FLOAT = "BO_Float";
	public static final String         INTEGER = "BO_Integer";
	public static final String         BOOLEAN = "BO_Boolean";
	public static final String         DATE = "BO_Date";
	public static final String         BYTE_SEQUENCE = "BO_Byte-sequence";

	// Content element list 
	public static final String         CONTENT_ELEMENT_LIST = ContentElementListSchema.BASE_NAME;

	//#MIDP_EXCLUDE_BEGIN
	private transient Map primitiveSchemas;
	//#MIDP_EXCLUDE_END

	/**
	 * Constructor
	 */
	private BasicOntology() {
		super("BASIC_ONTOLOGY", (Ontology) null, null);
	}

	private void initialize() {
		// Note that the association between schemas and classes is not 
		// necessary for the elements of the BasicOntology as the
		// BasicOntology does not use schemas to translate between 
		// Java objects and abstract descriptors, but performs a hardcoded
		// translation  
		try {
			// Schemas for primitives
			add(new PrimitiveSchema(FLOAT));
			add(new PrimitiveSchema(INTEGER));
			add(new PrimitiveSchema(BOOLEAN));

			// Schemas for aggregates
			add(new AggregateSchema(SEQUENCE));
			add(new AggregateSchema(SET));

			// Content element list Schema
			add(ContentElementListSchema.getBaseSchema()); 

			//#MIDP_EXCLUDE_BEGIN
			add(new PrimitiveSchema(STRING), String.class);
			add(new PrimitiveSchema(DATE), Date.class);
			add(new PrimitiveSchema(BYTE_SEQUENCE), byte[].class);
			add(new ConceptSchema(AID), AID.class);
			add(new AgentActionSchema(ACLMSG), ACLMessage.class); 
			add(new PredicateSchema(TRUE_PROPOSITION), TrueProposition.class);
			add(new PredicateSchema(FALSE_PROPOSITION), FalseProposition.class);
			add(new AgentActionSchema(ACTION), Action.class);
			add(new PredicateSchema(DONE), Done.class);
			add(new PredicateSchema(RESULT), Result.class);
			add(new PredicateSchema(EQUALS), Equals.class);
			//#MIDP_EXCLUDE_END
			/*#MIDP_INCLUDE_BEGIN
			add(new PrimitiveSchema(STRING));
			add(new PrimitiveSchema(DATE));
			add(new PrimitiveSchema(BYTE_SEQUENCE));
			add(new ConceptSchema(AID)); 
			add(new AgentActionSchema(ACLMSG)); 
			add(new PredicateSchema(TRUE_PROPOSITION));
			add(new PredicateSchema(FALSE_PROPOSITION));
			add(new AgentActionSchema(ACTION));
			add(new PredicateSchema(DONE));
			add(new PredicateSchema(RESULT));
			add(new PredicateSchema(EQUALS));
			#MIDP_INCLUDE_END*/

			// AID Schema
			ConceptSchema aidSchema = (ConceptSchema)getSchema(AID);
			aidSchema.add(AID_NAME, (TermSchema) getSchema(STRING));
			aidSchema.add(AID_ADDRESSES, (TermSchema) getSchema(STRING), 0, ObjectSchema.UNLIMITED);
			aidSchema.add(AID_RESOLVERS, aidSchema, 0, ObjectSchema.UNLIMITED);

			// ACLMessage Schema
			AgentActionSchema msgSchema = (AgentActionSchema)getSchema(ACLMSG);
			msgSchema.add(ACLMSG_SENDER, (ConceptSchema) getSchema(AID), ObjectSchema.OPTIONAL);
			msgSchema.add(ACLMSG_RECEIVERS, (ConceptSchema) getSchema(AID), 0, ObjectSchema.UNLIMITED);
			msgSchema.add(ACLMSG_REPLY_TO, (ConceptSchema) getSchema(AID), 0, ObjectSchema.UNLIMITED);
			msgSchema.add(ACLMSG_LANGUAGE, (PrimitiveSchema) getSchema(STRING), ObjectSchema.OPTIONAL);
			msgSchema.add(ACLMSG_ONTOLOGY, (PrimitiveSchema) getSchema(STRING), ObjectSchema.OPTIONAL);
			msgSchema.add(ACLMSG_PROTOCOL, (PrimitiveSchema) getSchema(STRING), ObjectSchema.OPTIONAL);
			msgSchema.add(ACLMSG_IN_REPLY_TO, (PrimitiveSchema) getSchema(STRING), ObjectSchema.OPTIONAL);
			msgSchema.add(ACLMSG_REPLY_WITH, (PrimitiveSchema) getSchema(STRING), ObjectSchema.OPTIONAL);
			msgSchema.add(ACLMSG_CONVERSATION_ID, (PrimitiveSchema) getSchema(STRING), ObjectSchema.OPTIONAL);
			msgSchema.add(ACLMSG_REPLY_BY, (PrimitiveSchema) getSchema(DATE), ObjectSchema.OPTIONAL);
			msgSchema.add(ACLMSG_CONTENT, (PrimitiveSchema) getSchema(STRING), ObjectSchema.OPTIONAL);
			msgSchema.add(ACLMSG_BYTE_SEQUENCE_CONTENT, (PrimitiveSchema) getSchema(BYTE_SEQUENCE), ObjectSchema.OPTIONAL);
			msgSchema.add(ACLMSG_ENCODING, (PrimitiveSchema) getSchema(STRING), ObjectSchema.OPTIONAL);

			// ACTION Schema
			AgentActionSchema actionSchema = (AgentActionSchema)getSchema(ACTION);
			actionSchema.add(ACTION_ACTOR, (TermSchema) getSchema(AID));
			actionSchema.add(ACTION_ACTION, (TermSchema) ConceptSchema.getBaseSchema());
			actionSchema.setEncodingByOrder(true);

			// DONE Schema
			PredicateSchema doneSchema = (PredicateSchema)getSchema(DONE);
			doneSchema.add(DONE_ACTION, (AgentActionSchema) AgentActionSchema.getBaseSchema());
			doneSchema.add(DONE_CONDITION, (PredicateSchema) PredicateSchema.getBaseSchema(), ObjectSchema.OPTIONAL);

			// RESULT Schema
			PredicateSchema resultSchema = (PredicateSchema)getSchema(RESULT);
			resultSchema.add(RESULT_ACTION, (AgentActionSchema) AgentActionSchema.getBaseSchema());
			resultSchema.add(RESULT_VALUE, (TermSchema) TermSchema.getBaseSchema());

			// EQUALS Schema
			PredicateSchema equalsSchema = (PredicateSchema)getSchema(EQUALS);
			equalsSchema.add(EQUALS_LEFT, TermSchema.getBaseSchema());
			equalsSchema.add(EQUALS_RIGHT, TermSchema.getBaseSchema());

			//#MIDP_EXCLUDE_BEGIN
			fillPrimitiveSchemas();
			//#MIDP_EXCLUDE_END
		} 
		catch (OntologyException oe) {
			oe.printStackTrace();
		} 
	}
	
	//#MIDP_EXCLUDE_BEGIN
	private void fillPrimitiveSchemas() throws OntologyException {
		// This map is only needed to make the getSchema(Class) method work properly also in the case of java primitives
		primitiveSchemas = new HashMap(10);
		primitiveSchemas.put(boolean.class, getSchema(BasicOntology.BOOLEAN));
		primitiveSchemas.put(java.lang.Boolean.class, getSchema(BasicOntology.BOOLEAN));
		primitiveSchemas.put(int.class, getSchema(BasicOntology.INTEGER));
		primitiveSchemas.put(long.class, getSchema(BasicOntology.INTEGER));
		primitiveSchemas.put(java.lang.Integer.class, getSchema(BasicOntology.INTEGER));
		primitiveSchemas.put(java.lang.Long.class, getSchema(BasicOntology.INTEGER));
		primitiveSchemas.put(float.class, getSchema(BasicOntology.FLOAT));
		primitiveSchemas.put(double.class, getSchema(BasicOntology.FLOAT));
		primitiveSchemas.put(java.lang.Float.class, getSchema(BasicOntology.FLOAT));
		primitiveSchemas.put(java.lang.Double.class, getSchema(BasicOntology.FLOAT));
	}

	private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
		in.defaultReadObject();
		
		try {
			fillPrimitiveSchemas();
		}
		catch (OntologyException oe) {
			// Should never happen 
			oe.printStackTrace();
		}
	}
	//#MIDP_EXCLUDE_END
	
	/**
	 * Returns the singleton instance of the <code>BasicOntology</code>.
	 * @return the singleton instance of the <code>BasicOntology</code>
	 */
	public static Ontology getInstance() {
		return theInstance;
	}

	//#APIDOC_EXCLUDE_BEGIN
	/**
	 * This method is redefined as BasicOntology does not use an
	 * Introspector for performance reason
	 * @see Ontology#toObject(AbsObject)
	 */
	protected Object toObject(AbsObject abs, String lcType, Ontology referenceOnto) throws UngroundedException, OntologyException {
		try {
			if (abs == null) {
				return null;
			} 

			// PRIMITIVE
			if (abs.getAbsType() == AbsObject.ABS_PRIMITIVE) {
				return ((AbsPrimitive) abs).getObject();
			} 
			// AGGREGATES
			if (abs.getAbsType() == AbsObject.ABS_AGGREGATE) {
				String absTypeName = abs.getTypeName();
				if (BasicOntology.SEQUENCE.equals(absTypeName)) {
					return AbsHelper.internaliseList((AbsAggregate) abs, referenceOnto);
				} else if (BasicOntology.SET.equals(absTypeName)) {
					return AbsHelper.internaliseSet((AbsAggregate) abs, referenceOnto);
			//#MIDP_EXCLUDE_BEGIN
				} else {
					return AbsHelper.internaliseJavaCollection((AbsAggregate) abs, referenceOnto);
			//#MIDP_EXCLUDE_END
				}
			} 
			// CONTENT ELEMENT LIST
			if (abs.getAbsType() == AbsObject.ABS_CONTENT_ELEMENT_LIST) {
				return AbsHelper.internaliseContentElementList((AbsContentElementList) abs, referenceOnto);
			}
			//#MIDP_EXCLUDE_BEGIN
			// CONCEPT_SLOT_FUNCTION
			if (abs.getAbsType() == AbsObject.ABS_CONCEPT_SLOT_FUNCTION) {
				AbsObject absConcept = abs.getAbsObject(ConceptSlotFunctionSchema.CONCEPT_SLOT_FUNCTION_CONCEPT);
				return referenceOnto.createConceptSlotFunction(abs.getTypeName(), (Concept) referenceOnto.toObject(absConcept));
			}
			//#MIDP_EXCLUDE_END
			// AID
			if (CaseInsensitiveString.equalsIgnoreCase(abs.getTypeName(), BasicOntology.AID)) { 
				return AbsHelper.internaliseAID((AbsConcept) abs);
			}
			// TRUE_PROPOSITION
			if (CaseInsensitiveString.equalsIgnoreCase(abs.getTypeName(), BasicOntology.TRUE_PROPOSITION)) { 
				TrueProposition t = new TrueProposition();
				return t;
			}
			// FALSE_PROPOSITION
			if (CaseInsensitiveString.equalsIgnoreCase(abs.getTypeName(), BasicOntology.FALSE_PROPOSITION)) { 
				FalseProposition t = new FalseProposition();
				return t;
			}
			// DONE
			if (CaseInsensitiveString.equalsIgnoreCase(abs.getTypeName(), BasicOntology.DONE)) { 
				Done d = new Done();
				d.setAction((AgentAction) referenceOnto.toObject(abs.getAbsObject(BasicOntology.DONE_ACTION))); 
				AbsPredicate condition = (AbsPredicate) abs.getAbsObject(BasicOntology.DONE_CONDITION);
				if (condition != null) {
					d.setCondition((Predicate) referenceOnto.toObject(abs.getAbsObject(BasicOntology.DONE_CONDITION)));
				}
				return d;
			}
			// RESULT
			if (CaseInsensitiveString.equalsIgnoreCase(abs.getTypeName(), BasicOntology.RESULT)) { 
				Result r = new Result();
				r.setAction((AgentAction) referenceOnto.toObject(abs.getAbsObject(BasicOntology.RESULT_ACTION)));
				r.setValue(referenceOnto.toObject(abs.getAbsObject(BasicOntology.RESULT_VALUE)));
				return r;
			}
			// EQUALS
			if (CaseInsensitiveString.equalsIgnoreCase(abs.getTypeName(), BasicOntology.EQUALS)) { 
				Equals e = new Equals();
				e.setLeft(referenceOnto.toObject(abs.getAbsObject(BasicOntology.EQUALS_LEFT))); 
				e.setRight(referenceOnto.toObject(abs.getAbsObject(BasicOntology.EQUALS_RIGHT))); 
				return e;
			}
			// ACTION
			if (CaseInsensitiveString.equalsIgnoreCase(abs.getTypeName(), BasicOntology.ACTION)) { 
				Action a = new Action();
				a.internalise(abs, referenceOnto);
				return a;
			}
			// ACLMESSAGE
			if (ACLMessage.getInteger(abs.getTypeName()) != -1) { 
				return AbsHelper.internaliseACLMessage((AbsAgentAction) abs, referenceOnto);
			}

			throw new UnknownSchemaException();
		} 
		catch (OntologyException oe) {
			// Forward the exception
			throw oe;
		} 
		catch (Throwable t) {
			throw new OntologyException("Unexpected error internalising "+abs+".", t);
		}
	}

	/**
	 * This method is redefined as BasicOntology does not use an
	 * Introspector for performance reason
	 * @see Ontology#toObject(AbsObject)
	 */
	protected AbsObject fromObject(Object obj, Ontology referenceOnto) throws OntologyException{
		try {
			if (obj == null) {
				return null;
			} 

			if (obj instanceof String) {
				return AbsPrimitive.wrap((String) obj);
			} 
			if (obj instanceof Boolean) {
				return AbsPrimitive.wrap(((Boolean) obj).booleanValue());
			} 
			if (obj instanceof Integer) {
				return AbsPrimitive.wrap(((Integer) obj).intValue());
			} 
			if (obj instanceof Long) {
				return AbsPrimitive.wrap(((Long) obj).longValue());
			} 
			//#MIDP_EXCLUDE_BEGIN
			if (obj instanceof Float) {
				return AbsPrimitive.wrap(((Float) obj).floatValue());
			} 
			if (obj instanceof Double) {
				return AbsPrimitive.wrap(((Double) obj).doubleValue());
			} 
			//#MIDP_EXCLUDE_END
			if (obj instanceof Date) {
				return AbsPrimitive.wrap((Date) obj);
			} 
			if (obj instanceof byte[]) {
				return AbsPrimitive.wrap((byte[]) obj);
			} 

			if (obj instanceof List) {
				return AbsHelper.externaliseList((List) obj, referenceOnto, SEQUENCE);
			}

			if (obj instanceof Set) {
				return AbsHelper.externaliseSet((Set) obj, referenceOnto, SET);
			}

			if (obj instanceof Iterator) {
				return AbsHelper.externaliseIterator((Iterator) obj, referenceOnto, SEQUENCE);
			}

			if(obj instanceof AID) {
				return AbsHelper.externaliseAID((AID)obj);
			}

			if (obj instanceof ContentElementList) {
				return AbsHelper.externaliseContentElementList((ContentElementList) obj, referenceOnto);
			} 

			if(obj instanceof TrueProposition) {
				AbsPredicate absTrueProp = new AbsPredicate(BasicOntology.TRUE_PROPOSITION);
				return absTrueProp;
			}

			if(obj instanceof FalseProposition) {
				AbsPredicate absTrueProp = new AbsPredicate(BasicOntology.FALSE_PROPOSITION);
				return absTrueProp;
			}

			if(obj instanceof Done) {
				AbsPredicate absDone = new AbsPredicate(BasicOntology.DONE);
				absDone.set(BasicOntology.DONE_ACTION, (AbsAgentAction) referenceOnto.fromObject(((Done) obj).getAction()));
				if (((Done) obj).getCondition() != null) {
					absDone.set(BasicOntology.DONE_CONDITION, (AbsPredicate) referenceOnto.fromObject(((Done) obj).getCondition()));
				}
				return absDone;
			}

			if(obj instanceof Result) {
				AbsPredicate absResult = new AbsPredicate(BasicOntology.RESULT);
				absResult.set(BasicOntology.RESULT_ACTION, (AbsAgentAction) referenceOnto.fromObject(((Result) obj).getAction()));
				absResult.set(BasicOntology.RESULT_VALUE, (AbsTerm) referenceOnto.fromObject(((Result) obj).getValue()));
				return absResult;
			}

			if(obj instanceof Equals) {
				AbsPredicate absEquals = new AbsPredicate(BasicOntology.EQUALS);
				absEquals.set(BasicOntology.EQUALS_LEFT, (AbsTerm) referenceOnto.fromObject(((Equals) obj).getLeft()));
				absEquals.set(BasicOntology.EQUALS_RIGHT, (AbsTerm) referenceOnto.fromObject(((Equals) obj).getRight()));
				return absEquals;
			}

			if (obj instanceof Action) {
				AbsAgentAction absAction = new AbsAgentAction(BasicOntology.ACTION);
				((Action) obj).externalise(absAction, referenceOnto);
				return absAction;
			}

			if (obj instanceof ACLMessage) {
				return AbsHelper.externaliseACLMessage((ACLMessage)obj, referenceOnto);
			}
			
			//#MIDP_EXCLUDE_BEGIN
			if (obj instanceof ConceptSlotFunction) {
				ConceptSlotFunction csf = (ConceptSlotFunction) obj;
				AbsObject absConcept = referenceOnto.fromObject(csf.getConcept());
				AbsConcept absCsf = new AbsConcept(csf.getSlotName());
				absCsf.set(ConceptSlotFunctionSchema.CONCEPT_SLOT_FUNCTION_CONCEPT, absConcept);
				return absCsf;
			}
			//#MIDP_EXCLUDE_END

			throw new UnknownSchemaException();
		} 
		catch (OntologyException oe) {
			// Forward the exception
			throw oe;
		} 
		catch (Throwable t) {
			throw new OntologyException("Unexpected error externalising "+obj+".", t);
		}
	}
	//#APIDOC_EXCLUDE_END

	/**
	 * Redefine the <code>getSchema()</code> method to take into 
	 * account ACL performatives. 
	 * @param name the name of the schema in the vocabulary.
	 * @return the schema or <code>null</code> if the schema is not found.
	 * @throws OntologyException 
	 */
	public ObjectSchema getSchema(String name) throws OntologyException {
		ObjectSchema ret = super.getSchema(name);
		if (ret == null) {
			int perf = ACLMessage.getInteger(name);
			if (perf != -1) {
				ret = createMsgSchema(name);
			}
		}
		return ret;
	}

	/**
	 * Redefine the <code>getSchema()</code> method to take into 
	 * account java primitives.
	 * @param clazz the class whose associated schema must be retrieved.
	 * @return the schema associated to the given class or <code>null</code> if the schema is not found.
	 * @throws OntologyException
	 */
	public ObjectSchema getSchema(Class clazz) throws OntologyException {
		//#MIDP_EXCLUDE_BEGIN
		ObjectSchema schema = (ObjectSchema)primitiveSchemas.get(clazz);
		if (schema == null) {
			schema = super.getSchema(clazz);
		}
		//#MIDP_EXCLUDE_END
		/*#MIDP_INCLUDE_BEGIN
		ObjectSchema schema = super.getSchema(clazz);
		#MIDP_INCLUDE_END*/
		return schema;
	}

	/**
     Note that we don't want to keep 22 different schemas for the 22 
     FIPA communicative acts --> We generate the schemas for communicative
     acts on the fly as needed.
	 */
	private ObjectSchema createMsgSchema(String performative) throws OntologyException {
		AgentActionSchema msgSchema = new AgentActionSchema(performative);
		msgSchema.addSuperSchema((AgentActionSchema) getSchema(ACLMSG));
		return msgSchema;
	}

	//#MIDP_EXCLUDE_BEGIN
	/**
	 Convert, if possible, the numeric value srcValue into an instance of destClass 
	 */
	public static Object adjustPrimitiveValue(Object srcValue, Class destClass) {
		Object destValue = srcValue;
		if (srcValue != null) {
			Class srcClass = srcValue.getClass();
			// Note that we deal with Integer, int, Long, long... classes only --> we can compare the classes using == and != instead of using instanceof
			if (srcClass != destClass) {
				if (destClass == Integer.class ||
					destClass == int.class) {
					if (srcClass == Long.class) {
						destValue = new Integer(((Long)srcValue).intValue());
					} 
					else if (srcClass == String.class) {
						destValue = new Integer(Integer.parseInt((String)srcValue));
					}
				}
				else if (destClass == Long.class ||
						 destClass == long.class) {
					if (srcClass == Integer.class) {
						destValue = new Long(((Integer)srcValue).longValue());
					} 
					else if (srcClass == String.class) {
						destValue = new Long(Long.parseLong((String)srcValue));
					}
				}
				else if (destClass == Float.class ||
						 destClass == float.class) {
					if (srcClass == Integer.class) {
						destValue = new Float(((Integer)srcValue).floatValue());
					}
					else if (srcClass == Long.class) {
						destValue = new Float(((Long)srcValue).floatValue());
					}
					else if (srcClass == Double.class) {
						destValue = new Float(((Double)srcValue).floatValue());
					}
					else if (srcClass == String.class) {
						destValue = new Float(Float.parseFloat((String)srcValue));
					}
				}
				else if (destClass == Double.class ||
						 destClass == double.class) {
					if (srcClass == Integer.class) {
						destValue = new Double(((Integer)srcValue).doubleValue());
					}
					else if (srcClass == Long.class) {
						destValue = new Double(((Long)srcValue).doubleValue());
					}
					else if (srcClass == Float.class) {
						destValue = new Double(((Float)srcValue).doubleValue());
					}
					else if (srcClass == String.class) {
						destValue = new Double(Double.parseDouble((String)srcValue));
					}
				}
				else if (destClass == String.class) {
					destValue = srcValue.toString();
				}
				else if (destClass == Boolean.class ||
						 destClass == boolean.class) {
					if (srcClass == String.class) {
						String s = (String) srcValue;
						if (s.equalsIgnoreCase("true")) {
							destValue = new Boolean(true);
						}
						else if (s.equalsIgnoreCase("false")) {
							destValue = new Boolean(false);
						}
					}
				}
				else if (destClass == Date.class) {
					try {
						// Try to convert string from FIPA-ISO8601 format
						destValue = ISO8601.toDate(srcValue.toString());
					} catch (Exception e) {
						try {
							// Try to convert string from W3C-ISO8601 format
							java.text.SimpleDateFormat W3CISO8601DateFormat = new java.text.SimpleDateFormat ("yyyy-MM-dd'T'HH:mm:ss.SSS");
							destValue = W3CISO8601DateFormat.parse(srcValue.toString());
						} catch (java.text.ParseException e1) {
							// Date format not correct
						}
					}
				}
			}
		}
		return destValue;
	}

	/**
	 * @deprecated Use adjustPrimitiveValue() instead
	 */
	public static Object resolveNumericValue(Object srcValue, Class destClass) {
		return adjustPrimitiveValue(srcValue, destClass);
	}
	//#MIDP_EXCLUDE_END
}
