package jade.tools.logging.ontology;

//#J2ME_EXCLUDE_FILE

import jade.content.AgentAction;

public class SetFile implements AgentAction {
	private String file;
	private String logger;
	
	public SetFile() {
	}
	
	public SetFile(String logger, String file) {
		this.logger = logger;
		this.file = file;
	}
	
	public SetFile(String file) {
		setFile(file);
	}
	
	public void setFile(String file) {
		this.file = file;
	}
	
	public String getFile() {
		return file;
	}
	
	public String getLogger() {
		return logger;
	}

	public void setLogger(String logger) {
		this.logger = logger;
	}
}
