package jade.tools.logging.ontology;

import jade.content.Concept;

public class LevelInfo implements Concept{
	
	private String name;
	private int value;
	
	
	public LevelInfo() {
	}
	
	public LevelInfo(String name, int value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
	
	

}
