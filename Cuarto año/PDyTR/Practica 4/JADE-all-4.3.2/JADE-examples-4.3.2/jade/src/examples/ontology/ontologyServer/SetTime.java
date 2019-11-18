package examples.ontology.ontologyServer;

import java.util.Date;

import jade.content.AgentAction;

public class SetTime implements AgentAction{
	private Date time;

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}
}
