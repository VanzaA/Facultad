/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/


package examples.ontology.employment;

import jade.content.onto.*;
import jade.content.schema.*;
import java.util.*;

/**
   Javadoc documentation for the file EmploymentOntology
   @author Giovanni Caire - CSELT S.p.A.
   @version $Date: 2002-07-31 17:27:34 +0200 (mer, 31 lug 2002) $ $Revision: 3315 $
*/

public class EmploymentOntology extends Ontology {

  /**
    A symbolic constant, containing the name of this ontology.
   */
  public static final String NAME = "employment-ontology";

  // VOCABULARY
  // Concepts
  public static final String ADDRESS = "ADDRESS";
  public static final String ADDRESS_NAME = "street";
  public static final String ADDRESS_NUMBER = "number";
  public static final String ADDRESS_CITY = "city";
  
  public static final String PERSON = "PERSON";
  public static final String PERSON_NAME = "name";
  public static final String PERSON_AGE = "age";
  public static final String PERSON_ADDRESS = "address";
  
  public static final String COMPANY = "COMPANY";
  public static final String COMPANY_NAME = "name";
  public static final String COMPANY_ADDRESS = "address";
  
  // Actions
  public static final String ENGAGE = "ENGAGE";
  public static final String ENGAGE_PERSON = "person";
  public static final String ENGAGE_COMPANY = "company";
  // Predicates
  public static final String WORKS_FOR = "WORKS-FOR";
  public static final String WORKS_FOR_PERSON = "person";
  public static final String WORKS_FOR_COMPANY = "company";
  public static final String ENGAGEMENT_ERROR = "ENGAGEMENT-ERROR";
  public static final String PERSON_TOO_OLD = "PERSON-TOO-OLD";
  
  private static Ontology theInstance = new EmploymentOntology();
	
  /**
     This method grants access to the unique instance of the
     ontology.
     @return An <code>Ontology</code> object, containing the concepts
     of the ontology.
  */
   public static Ontology getInstance() {
		return theInstance;
   }
	
  /**
   * Constructor
   */
  private EmploymentOntology() {
    //__CLDC_UNSUPPORTED__BEGIN
  	super(NAME, BasicOntology.getInstance());


    try {
		add(new ConceptSchema(ADDRESS), Address.class);
		add(new ConceptSchema(PERSON), Person.class);
		add(new ConceptSchema(COMPANY), Company.class);
		add(new PredicateSchema(WORKS_FOR), WorksFor.class);
		add(new PredicateSchema(PERSON_TOO_OLD), PersonTooOld.class);
		add(new PredicateSchema(ENGAGEMENT_ERROR), EngagementError.class);
		add(new AgentActionSchema(ENGAGE), Engage.class);
		
    	ConceptSchema cs = (ConceptSchema)getSchema(ADDRESS);
		cs.add(ADDRESS_NAME, (PrimitiveSchema)getSchema(BasicOntology.STRING));
		cs.add(ADDRESS_NUMBER, (PrimitiveSchema)getSchema(BasicOntology.INTEGER), ObjectSchema.OPTIONAL);
		cs.add(ADDRESS_CITY, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
    	
    	cs = (ConceptSchema)getSchema(PERSON);
    	cs.add(PERSON_NAME, (PrimitiveSchema)getSchema(BasicOntology.STRING));
    	cs.add(PERSON_AGE, (PrimitiveSchema)getSchema(BasicOntology.INTEGER), ObjectSchema.OPTIONAL);
    	cs.add(PERSON_ADDRESS, (ConceptSchema)getSchema(ADDRESS), ObjectSchema.OPTIONAL);
    	
    	cs = (ConceptSchema)getSchema(COMPANY);
    	cs.add(COMPANY_NAME, (PrimitiveSchema)getSchema(BasicOntology.STRING));
    	cs.add(COMPANY_ADDRESS, (ConceptSchema)getSchema(ADDRESS), ObjectSchema.OPTIONAL);
    	
    	PredicateSchema ps = (PredicateSchema)getSchema(WORKS_FOR);
    	ps.add(WORKS_FOR_PERSON, (ConceptSchema)getSchema(PERSON));
    	ps.add(WORKS_FOR_COMPANY, (ConceptSchema)getSchema(COMPANY));
    	
		AgentActionSchema as = (AgentActionSchema)getSchema(ENGAGE);
		as.add(ENGAGE_PERSON, (ConceptSchema)getSchema(PERSON));
		as.add(ENGAGE_COMPANY, (ConceptSchema)getSchema(COMPANY)); 	
    }
    catch(OntologyException oe) {
      oe.printStackTrace();
    }
  } 
}

