package jade.content.schema;

import jade.content.abs.AbsConceptSlotFunction;
import jade.content.abs.AbsObject;
import jade.content.onto.OntologyException;

//#APIDOC_EXCLUDE_FILE

public class ConceptSlotFunctionSchema extends ConceptSchema {
	public static final String CONCEPT_SLOT_FUNCTION_CONCEPT = "CSF_CONCEPT";
	
	public ConceptSlotFunctionSchema(String slotName) {
		super(slotName);
		
		add(CONCEPT_SLOT_FUNCTION_CONCEPT, ConceptSchema.getBaseSchema());
		
		this.setEncodingByOrder(true);
	}
	
	public AbsObject newInstance() throws OntologyException {
		return new AbsConceptSlotFunction(getTypeName());
	} 
}
