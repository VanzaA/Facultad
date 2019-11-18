package jade.wrapper.gateway;

//#J2ME_EXCLUDE_FILE

import jade.util.leap.Iterator;
import jade.util.leap.HashMap;

import jade.core.behaviours.CyclicBehaviour;
import jade.util.Event;
import jade.util.Logger;

/**
 * This is a cyclic behaviour that processes the commands received via JadeGateway. 
 * <code>JadeGateway</code> enables two alternative ways to implement a gateway
 * that allows non-JADE code to communicate with JADE agents.
 * <br> The first one is to extend the <code>GatewayAgent</code> (see its javadoc for reference).
 * <br> The second one is to extend this <code>GatewayBehaviour</code> and add an instance
 * of this Behaviour to your own agent that will have to function as a gateway.
 * @author Fabio Bellifemine, Telecom Italia Lab
 * @version $Date: $ $Revision: $
 */
public abstract class GatewayBehaviour extends CyclicBehaviour {

	/** 
	 * Queue of all pending commands that have not yet been released (see method releaseCommand)
	 * In this Hash, the key is the Object while the value is the Event
	 */
	private final HashMap commandQueue = new HashMap(2);

	private final Logger myLogger = Logger.getMyLogger(this.getClass().getName());

	public void action() {
		Event e = (Event) myAgent.getO2AObject();
		if (e == null) {
			block();
			return;
		}
		// put the event into the command Queue
		commandQueue.put(e.getSource(), e);
		if (myLogger.isLoggable(Logger.INFO)) {
			myLogger.log(Logger.INFO, myAgent.getLocalName() + " started execution of command " + e.getSource());
		}
		// call the processCommand method such as the command is executed
		processCommand(e.getSource());
	}

	/** subclasses must implement this method.
	 * The method is called each time a request to process a command
	 * is received from the JSP Gateway.
	 * <p> The recommended pattern is the following implementation:
	 <code>
	 if (c instanceof Command1)
	 execCommand1(c);
	 else if (c instanceof Command2)
	 execCommand2(c);
	 </code>
	 * </p>
	 * <b> REMIND THAT WHEN THE COMMAND HAS BEEN PROCESSED,
	 * YOU MUST CALL THE METHOD <code>releaseCommand</code>.
	 * Sometimes, you might prefer launching a new Behaviour that asynchronously processes
	 * this command and release the command just when the Behaviour terminates,
	 * i.e. in its <code>onEnd()</code> method.
	 **/
	abstract protected void processCommand(Object command);

	/**
	 * notify that the command has been processed and remove the command from the queue
	 * @param command is the same object that was passed in the processCommand method
	 **/
	final public void releaseCommand(Object command) {
		// remove the command from the queue
		Event e = (Event) commandQueue.remove(command);
		// notify that the command has been processed such as the JADEGateway is waken-up
		if (e != null) {
			if (myLogger.isLoggable(Logger.INFO)) {
				myLogger.log(Logger.INFO, myAgent.getLocalName() + " terminated execution of command " + command);
			}
			e.notifyProcessed(null);
		}
	}
	
	public int onEnd() {
		if (myLogger.isLoggable(Logger.INFO))
			myLogger.log(Logger.INFO, myAgent.getLocalName()
					+ " terminated GatewayBehaviour");
		for (Iterator i = commandQueue.values().iterator(); i.hasNext();)
			((Event) (i.next())).notifyProcessed(null);
		return super.onEnd();
	}

}
