package examples.replication;

import jade.content.onto.BeanOntology;
import jade.content.onto.Ontology;

public class ValueManagementOntology extends BeanOntology {
	public static final String NAME = "Value-Management-Ontology";
	
	// The singleton instance of the Time-Ontology
	private static ValueManagementOntology theInstance = new ValueManagementOntology();
	
	public static Ontology getInstance() {
		return theInstance;
	}
	
	private ValueManagementOntology() {
		super(NAME);
		
		try {
			// Add all Concepts, Predicates and AgentActions in the local package
			add(GetValue.class);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}


}
