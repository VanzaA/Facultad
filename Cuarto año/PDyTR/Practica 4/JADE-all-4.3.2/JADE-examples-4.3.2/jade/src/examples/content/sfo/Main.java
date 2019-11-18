package examples.content.sfo;

import jade.content.onto.OntologyUtils;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;

public class Main {

	/**
	 * @param args
	 * @throws OntologyException 
	 */
	public static void main(String[] args) throws OntologyException {
		Ontology ontology = new SoftwareFactoryFlatOntology("flat-swf-ontology");
		OntologyUtils.exploreOntology(ontology);
		ontology = new SoftwareFactoryHierarchicalOntology("hierarchical-swf-ontology");
		OntologyUtils.exploreOntology(ontology);
	}

}
