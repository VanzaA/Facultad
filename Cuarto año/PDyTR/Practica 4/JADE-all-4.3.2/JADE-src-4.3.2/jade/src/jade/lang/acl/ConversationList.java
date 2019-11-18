package jade.lang.acl;

import jade.core.Agent;
import jade.util.leap.Serializable;

import java.util.Vector;

/**
   This class represents a list of conversations that an agent is 
   currently carrying out and allows creating a <code>MessageTemplate</code> 
   that matches only messages that do not belong to any of these 
   conversations. 
 */
public class ConversationList implements Serializable{
	private Vector conversations = new Vector();
	protected Agent myAgent = null;
	protected int cnt = 0;
	
	private MessageTemplate myTemplate = new MessageTemplate(new MessageTemplate.MatchExpression() {
		public boolean match(ACLMessage msg) {
			String convId = msg.getConversationId();
			return (convId == null || (!conversations.contains(convId)));
		}
	} );
	
	/**
	   Construct a ConversationList to be used inside a given agent.
	 */
	public ConversationList(Agent a) {
		myAgent = a;
	}
	
	/**
	   Register a conversation creating a new unique ID.
	 */
	public String registerConversation() {
		String id = createConversationId();
		conversations.addElement(id);
		return id;
	}
	
	/**
	   Register a conversation with a given ID.
	 */
	public void registerConversation(String convId) {
		if (convId != null) {
			conversations.addElement(convId);
		}
	}
	
	/**
	   Deregister a conversation with a given ID.
	 */
	public void deregisterConversation(String convId) {
		if (convId != null) {
			conversations.removeElement(convId);
		}
	}

	/**
	   Deregister all conversations.
	 */
	public void clear() {
		conversations.removeAllElements();
	}
	
	/**
	   Return a template that matches only messages that do not belong to 
	   any of the conversations in this list.
	 */
	public MessageTemplate getMessageTemplate() {
		return myTemplate;
	}
	
	protected String createConversationId() {
		return myAgent.getName()+(cnt++);
	}
}
		