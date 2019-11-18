package jade.core.messaging;

//#J2ME_EXCLUDE_FILE
//#APIDOC_EXCLUDE_FILE

import jade.core.Agent;
import jade.domain.introspection.*;

// TO BE REMOVED
public class MessagingMonitorAgent extends Agent {
	protected void setup() {
		addBehaviour(new IntrospectionServer(this));
	}
	
	public String[] getMessageManagerQueueStatus() {
		MessageManager mm = MessageManager.instance(null);
		return mm.getQueueStatus();
	}
	
	public String[] getMessageManagerThreadPoolStatus() {
		MessageManager mm = MessageManager.instance(null);
		return mm.getThreadPoolStatus();
	}
}
