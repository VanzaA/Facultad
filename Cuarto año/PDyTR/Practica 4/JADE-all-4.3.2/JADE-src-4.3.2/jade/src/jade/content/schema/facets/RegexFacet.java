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
package jade.content.schema.facets;

//#MIDP_EXCLUDE_FILE
//#DOTNET_EXCLUDE_FILE

import jade.content.abs.AbsObject;
import jade.content.abs.AbsPrimitive;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.schema.Facet;

/**
 * This facet forces an AbsPrimitive to contain a specific set of values
 * expressed as a regular expression.
 */
public class RegexFacet implements Facet {
	private String regex;

	/**
	   Construct a <code>PermittedValuesFacet</code> that 
	   forces an AbsPrimitive to contain a specific set of values
 	   expressed as a regular expression
	 */
	public RegexFacet(String regex) {
		this.regex = regex;
	}
	
	/**
	  Get the regex associated to this facet
	*/
	public String getRegex() {
		return regex;
	}
	
	/**
	   Check whether a given value for the slot this Facet applies
	   to is valid.
	   @param value The value to be checked
	   @throws OntologyException If the value is not valid
	 */
	public void validate(AbsObject value, Ontology onto) throws OntologyException {
		if (!(value instanceof AbsPrimitive)) {
			throw new OntologyException(value+" is not an AbsPrimitive");
		}
		
		AbsPrimitive absPrimitive = (AbsPrimitive) value;
		Object absValue = absPrimitive.getObject();
		if (absValue != null) {
			if (!absValue.toString().matches(regex)) {
				throw new OntologyException(value+" not match the regular expression "+regex);
			}
		}
	}
}
