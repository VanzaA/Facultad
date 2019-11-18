package examples.content.sfo;

import jade.content.onto.BeanOntology;
import jade.content.onto.BeanOntologyException;

public class SoftwareFactoryHierarchicalOntology extends BeanOntology {
	private static final long serialVersionUID = 1L;

	public SoftwareFactoryHierarchicalOntology(String name) throws BeanOntologyException {
		super(name);
		add(ExpertProgrammer.class);
		// equivalent to add(ExpertProgrammer.class, true);
	}
}
