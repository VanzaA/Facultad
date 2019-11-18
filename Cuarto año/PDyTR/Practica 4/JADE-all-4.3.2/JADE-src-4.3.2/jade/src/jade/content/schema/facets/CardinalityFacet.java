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

import jade.content.onto.*;
import jade.content.schema.*;
import jade.content.abs.*;
import jade.util.leap.Iterator;

/**
 * This facet forces an AbsAggregate to contain a number of 
 * elements that is comprised between a given min and a given max.
 * @author Giovanni Caire - TILAB
 */
public class CardinalityFacet implements Facet {
	private int cardMin;
	private int cardMax;
	
	/**
	   Construct a <code>CardinalityFacet</code> that forces
	   the number of elements in an AbsAggregate to be within
	   a given range
	   @param cardMin The range lower bound
	   @param cardMax The range upper bound
	 */
	public CardinalityFacet(int cardMin, int cardMax) {
		this.cardMin = cardMin;
		this.cardMax = cardMax;
	}
	
	public int getCardMin(){
		return this.cardMin;	
	}
	
	public int getCardMax(){
		return this.cardMax;	
	}	
	
	/**
	   Check that
	   the number of elements in an AbsAggregate is within
	   a given range
	   @param value The value to be checked
	   @throws OntologyException If the value is not valid
	 */
	public void validate(AbsObject value, Ontology onto) throws OntologyException {
		if (value instanceof AbsVariable) {
			// A variable can always be used where whatever term is required
			return;
		}
		
  	if (!(value instanceof AbsAggregate)) {
  		throw new OntologyException(value+" is not an AbsAggregate");
  	}
  	
  	int size = ((AbsAggregate) value).size();
  	if (size < cardMin) {
  		throw new OntologyException(value+" includes less elements than required ("+cardMin+")");
  	}
  	if (cardMax != ObjectSchema.UNLIMITED && size > cardMax) {
  		throw new OntologyException(value+" includes more elements than allowed ("+cardMax+")");
  	}
	}
}