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

package demo.MeetingScheduler.Ontology;

//import jade.onto.*;
//import jade.onto.basic.*;
import jade.content.onto.*;
import jade.content.schema.*;
import jade.content.abs.*;

public class MSOntology extends Ontology {
  /**
    A symbolic constant, containing the name of this ontology.
   */
  public static final String NAME = "Meeting-Scheduling-Ontology";

  public static final String PERSON = "Person";
  public static final String APPOINTMENT = "Appointment";

  private static Ontology theInstance = new MSOntology();

  public static Ontology getInstance(){
	return theInstance;
 }


  private  MSOntology() {
    		
    // Adds the roles of the basic ontology (ACTION, AID,...)
	super(NAME,BasicOntology.getInstance(),new BCReflectiveIntrospector());
    
    try{
		//Concepts.
		add(new ConceptSchema(PERSON),Person.class);
		add(new ConceptSchema(APPOINTMENT),Appointment.class);

		ConceptSchema cs = (ConceptSchema)getSchema(PERSON);
		cs.add("name",(PrimitiveSchema)getSchema(BasicOntology.STRING),ObjectSchema.MANDATORY);
		cs.add("AID",(ConceptSchema) getSchema(BasicOntology.AID),ObjectSchema.OPTIONAL);
		cs.add("DFName",(ConceptSchema) getSchema(BasicOntology.AID),ObjectSchema.OPTIONAL);
			
		
		cs = (ConceptSchema)getSchema(APPOINTMENT);
		cs.add("inviter",(ConceptSchema) getSchema(BasicOntology.AID),ObjectSchema.MANDATORY);
		cs.add("description",(PrimitiveSchema)getSchema(BasicOntology.STRING),ObjectSchema.OPTIONAL);
		cs.add("startingon",(PrimitiveSchema)getSchema(BasicOntology.DATE),ObjectSchema.OPTIONAL);
		cs.add("endingwith",(PrimitiveSchema)getSchema(BasicOntology.DATE),ObjectSchema.OPTIONAL);
	    cs.add("fixeddate",(PrimitiveSchema)getSchema(BasicOntology.DATE),ObjectSchema.OPTIONAL);
		cs.add("invitedpersons",(ConceptSchema)getSchema(PERSON),0,ObjectSchema.UNLIMITED,BasicOntology.SET);
		cs.add("possibledates",(PrimitiveSchema)getSchema(BasicOntology.DATE),0,ObjectSchema.UNLIMITED,BasicOntology.SET);
   
    } catch (OntologyException oe) {
      oe.printStackTrace();
    }
  }

}




