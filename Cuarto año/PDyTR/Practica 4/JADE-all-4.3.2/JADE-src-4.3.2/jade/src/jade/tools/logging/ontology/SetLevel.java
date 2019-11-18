package jade.tools.logging.ontology;

//#J2ME_EXCLUDE_FILE

import jade.content.AgentAction;

public class SetLevel implements AgentAction {
	private int level = 1;
	private String logger;
	
	public SetLevel() {
	}
	
	public SetLevel(String logger, int level) {
		this.logger = logger;
		this.level = level;
	}
	
	public void setLevel(int level) {
		this.level = level;
	}
	
	public int getLevel() {
		return level;
	}

	public String getLogger() {
		return logger;
	}

	public void setLogger(String logger) {
		this.logger = logger;
	}
	
}
