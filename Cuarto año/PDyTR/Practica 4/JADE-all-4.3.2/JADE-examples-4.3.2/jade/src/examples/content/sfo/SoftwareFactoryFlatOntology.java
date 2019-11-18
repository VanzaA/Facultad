package examples.content.sfo;

import jade.content.onto.BeanOntology;
import jade.content.onto.BeanOntologyException;

public class SoftwareFactoryFlatOntology extends BeanOntology {
	private static final long serialVersionUID = 1L;

	public SoftwareFactoryFlatOntology(String name) throws BeanOntologyException {
		super(name);
		add(ExpertProgrammer.class, false);
	}
}
