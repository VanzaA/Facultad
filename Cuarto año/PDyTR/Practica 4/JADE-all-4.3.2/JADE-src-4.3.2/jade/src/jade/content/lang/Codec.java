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
package jade.content.lang;

import jade.content.ContentException;
import jade.content.onto.*;
import jade.util.leap.Serializable;

/**
 * Generic base class for al content language codecs
 * @author Federico Bergenti - Universita` di Parma
 */
public abstract class Codec implements Serializable {

	/**
	 * Class CodecException.
	 *
	 * @author Federico Bergenti
	 */
	public static class CodecException extends ContentException {

		/**
		 * Constructor
		 *
		 * @param message the message.
		 *
		 */
		public CodecException(String message) {
			super(message);
		}

		/**
	   			 Construct a new <code>CodecException</code>
	         @param msg The message for this exception.
	         @param t The exception wrapped by this object.
		 */
		public CodecException(String msg, Throwable t) {
			super(msg, t);
		}		

	}

	/** This string is the prefix of all the unnamed slots of a Frame **/
	public static String UNNAMEDPREFIX = "_JADE.UNNAMED"; 

	private String name = null;

	/**
	 * Construct a Codec object with the given name
	 */
	protected Codec(String name) {
		this.name = name;
	}

	/**
	 * Gets the name of this codec.
	 * @return the name of this codec.
	 */
	public String getName() {
		return name;
	} 

	/**
	 * @return the ontology containing the schemas of the operator
	 * defined in this language
	 */
	public Ontology getInnerOntology() {
		return null;
	}
}

