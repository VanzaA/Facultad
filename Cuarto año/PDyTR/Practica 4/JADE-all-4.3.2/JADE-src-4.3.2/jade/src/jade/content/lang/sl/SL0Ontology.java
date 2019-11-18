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

import jade.content.*;
import jade.content.onto.*;
import jade.content.schema.*;
import jade.core.CaseInsensitiveString;

/**
 * Ontology containing schemas for the SL0 language operators.
 * see jade.content.Ontology
 * @author Giovanni Caire - TILAB
 */
class SL0Ontology extends Ontology implements SL0Vocabulary {
	// NAME
  public static final String ONTOLOGY_NAME = jade.domain.FIPANames.Ontology.SL0_ONTOLOGY;
	
  // The singleton instance of this ontology
	private static Ontology theInstance = new SL0Ontology(ONTOLOGY_NAME, BasicOntology.getInstance(), null);
	
	public static Ontology getInstance() {
		return theInstance;
	}
	
  /**
   * Constructor
   */
  protected SL0Ontology(String name, Ontology base, Introspector intro) {
  	super(name, base, intro);
	}

	boolean isUnaryLogicalOp(String symbol) {
		// There are no logical operators in SL0
		return false;
	}
	
	boolean isBinaryLogicalOp(String symbol) {
		// There are no logical operators in SL0
		return false;
	}
	
	boolean isQuantifier(String symbol) {
		// There are no quantifiers in SL0
		return false;
	}
	
	boolean isModalOp(String symbol) {
		// There are no modal operators in SL0
		return false;
	}
	
	boolean isActionOp(String symbol) {
		return CaseInsensitiveString.equalsIgnoreCase(DONE, symbol); 
	}
	
	boolean isBinaryTermOp(String symbol) {
		return (CaseInsensitiveString.equalsIgnoreCase(EQUALS, symbol) || 
			CaseInsensitiveString.equalsIgnoreCase(RESULT, symbol));
	}
	
	boolean isSLFunctionWithoutSlotNames(String symbol) {
		return CaseInsensitiveString.equalsIgnoreCase(ACTION, symbol); 
	}
}
