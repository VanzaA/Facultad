package jade.content.onto;

public class UnknownSlotException extends OntologyException {

	public UnknownSlotException() {
		super(null);
	}
	
	public UnknownSlotException(String slotName) {
		super("Slot "+slotName+" does not exist");
	}
}
