package jade.tools.logging.ontology;

//#J2ME_EXCLUDE_FILE

import jade.content.Concept;
import jade.util.leap.List;

public class LoggerInfo implements Concept {
	private String name;
	private int level;
	private List handlers;
	private String file;
	
	public LoggerInfo() {
		
	}

	public LoggerInfo(String name, int level) {
		setName(name);
		setLevel(level);
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setLevel(int level) {
		this.level = level;
	}
	
	public int getLevel() {
		return level;
	}
	
	public void setHandlers(List handlers) {
		this.handlers = handlers;
	}
	
	public List getHandlers() {
		return handlers;
	}
	
	public void setFile(String file) {
		this.file = file;
	}
	
	public String getFile() {
		return file;
	}	
	
	public String toString() {
		return name;
	}
}
