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
//#MIDP_EXCLUDE_FILE

import jade.content.abs.AbsObject;
import jade.content.abs.AbsPrimitive;
import jade.content.abs.AbsConcept;
import jade.content.schema.PrimitiveSchema;
import jade.content.schema.ConceptSchema;

import org.apache.commons.codec.binary.Base64;
import java.io.*;

/**
 This ontology allows dealing with Java Serializable objects as
 if they were instances of ontological elements.
 <br>
 <b>NOT available in MIDP</b>
 <br>
 @author Giovanni Caire - TILAB
 */
public class SerializableOntology extends Ontology {
	// The singleton instance of this ontology
	private static final SerializableOntology theInstance = new SerializableOntology();
	
	public static final String SERIALIZABLE = "serializable";
	public static final String SERIALIZABLE_VALUE = "value";
	
	
	private ClassLoader myClassLoader;
	
	/**
	 * Returns the singleton instance of the <code>SerializableOntology</code>.
	 * @return the singleton instance of the <code>SerializableOntology</code>
	 */
	public static Ontology getInstance() {
		return theInstance;
	}
	
	/**
	 Construct a SerializableOntology object
	 */
	private SerializableOntology() {
		super("Serializable-ontology", (Ontology) null, null);
		try {
			// Add the primitive schema for binary data
			PrimitiveSchema stringSchema = (PrimitiveSchema) BasicOntology.getInstance().getSchema(BasicOntology.STRING);
			add(stringSchema);
			// Add the schema for a generic Serializable object
			ConceptSchema serializableSchema = new ConceptSchema(SERIALIZABLE);
			serializableSchema.add(SERIALIZABLE_VALUE, stringSchema);
			add(serializableSchema);
		}
		catch (Exception e) {
			// Should never happen
			e.printStackTrace();
		}
	}
	
	public void setClassLoader(ClassLoader cl) {
		myClassLoader = cl;
	}
	
	//#APIDOC_EXCLUDE_BEGIN
	/**
	 */
	protected Object toObject(AbsObject abs, String lcType, Ontology globalOnto) throws UnknownSchemaException, UngroundedException, OntologyException {
		if (SERIALIZABLE.equals(abs.getTypeName())) {
			try {
				AbsPrimitive absValue = (AbsPrimitive) abs.getAbsObject(SERIALIZABLE_VALUE);
				String stringValue = absValue.getString();
				byte[] value = Base64.decodeBase64(stringValue.getBytes("US-ASCII"));
				//#J2ME_EXCLUDE_BEGIN
				ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(value)) {
					protected Class resolveClass(ObjectStreamClass v) throws IOException, ClassNotFoundException {
						if (myClassLoader != null) {
							// FIXME: Manage primitive class fields. Refactor with AgentMobilityService
							return Class.forName(v.getName(), true, myClassLoader);
						}
						else {
							return super.resolveClass(v);
						}
					}
				};
				//#J2ME_EXCLUDE_END
				/*#J2ME_INCLUDE_BEGIN
				ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(value));
				#J2ME_INCLUDE_END*/
				return in.readObject();
			}
			catch (Throwable t) {
				throw new OntologyException("Error in object deserialization.", t);
			}
		}
		else {
			throw new OntologyException("Abs-object "+abs+" is not serializable");
		}
	}
	
	/**
	 */
	protected AbsObject fromObject(Object obj, Ontology globalOnto) throws UnknownSchemaException, OntologyException {
		// If obj is already an abstract descriptor --> just return it
		if (obj instanceof AbsObject) {
			return (AbsObject) obj;
		}
		
		if (obj instanceof Serializable) {
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream out = new ObjectOutputStream(baos);
				out.writeObject(obj);
				AbsConcept absSerializable = new AbsConcept(SERIALIZABLE);
				String stringValue = new String(Base64.encodeBase64(baos.toByteArray()), "US-ASCII");
				absSerializable.set(SERIALIZABLE_VALUE, stringValue);
				return absSerializable;
			}
			catch (Throwable t) {
				throw new OntologyException("Error in object serialization.", t);
			}
		}
		else {
			throw new OntologyException("Object "+obj+" is not serializable");
		}
	} 
	//#APIDOC_EXCLUDE_END
	
	//#J2ME_EXCLUDE_BEGIN
	private Object writeReplace() throws ObjectStreamException {
		return new DummySerializableOntology();
	}

	private static class DummySerializableOntology implements Serializable {

		private Object readResolve() throws ObjectStreamException {
			return SerializableOntology.getInstance();
		}
	}
	//#J2ME_EXCLUDE_END
}
