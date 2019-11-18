package examples.service;

import jade.core.AID;
import jade.core.BaseService;
import jade.core.Filter;
import jade.core.VerticalCommand;
import jade.core.messaging.GenericMessage;
import jade.core.messaging.MessagingSlice;
import jade.lang.acl.ACLMessage;

/**
 * This example shows how to implement a JADE Kernel Service that provides 
 * a Filter to process OUTGOING commands. In particular in this case we 
 * intercept SEND_MESSAGE commands of the Messaging Service to log all messages 
 * exchanged by agents.<br>
 * In order to try it compile the JADE examples and then launch<br>
 * java -cp jade-and-examples-classes jade.Boot -gui -services examples.service.MessageTracingService 
 */
public class MessageTracingService extends BaseService {
	
	public static final String NAME = "MessageTracing";

	private Filter outgoingFilter  = new TracingFilter();
		
	public String getName() {
		return NAME;
	}

	@Override
	public Filter getCommandFilter(boolean direction) {
		if (direction == Filter.OUTGOING) {
			return outgoingFilter;
		}
		else {
			return null;
		}
	}
	
	
	/**
	 * Inner class TracingFilter.
	 * This is the Filter intercepting SEND_MESSAGE outgoing commands end tracing 
	 * included messages
	 */
	private class TracingFilter extends Filter {
		
		@Override
		protected boolean accept(VerticalCommand cmd) {
			if (cmd.getName().equals(MessagingSlice.SEND_MESSAGE)) {
				AID sender = (AID) cmd.getParam(0);
				GenericMessage gMsg = (GenericMessage) cmd.getParam(1);
				ACLMessage msg = gMsg.getACLMessage();
				AID receiver = (AID) cmd.getParam(2);
				
				System.out.println(sender.getLocalName()+" --> "+ACLMessage.getPerformative(msg.getPerformative())+" --> "+receiver.getLocalName());
			}
			// Never block any command
			return true;
		}
	}
}
