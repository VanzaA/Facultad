/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
 *****************************************************************/

package jade.core.messaging;

import jade.util.Logger;
import jade.lang.acl.ACLMessage;
import jade.domain.FIPAAgentManagement.InternalError;

import jade.core.AID;
import jade.core.ResourceManager;
import jade.core.Profile;
import jade.core.ProfileException;
import jade.core.NotFoundException;
import jade.core.UnreachableException;

/**
 * This class manages the delivery of ACLMessages to remote destinations
 * in an asynchronous way.
 * If network problems prevent the delivery of a message, this class also 
 * embeds a mechanism to buffer the message and periodically retry to 
 * deliver it.
 * @author  Giovanni Caire - TILAB
 * @author  Elisabetta Cortese - TILAB
 * @author  Fabio Bellifemine - TILAB
 * @author  Jerome Picault - Motorola Labs
 * @version $Date: 2013-04-18 17:07:42 +0200 (gio, 18 apr 2013) $ $Revision: 6668 $
 */
class MessageManager {

	public interface Channel {
		void deliverNow(GenericMessage msg, AID receiverID) throws UnreachableException, NotFoundException;
		void notifyFailureToSender(GenericMessage msg, AID receiver, InternalError ie);
	}


	// A shared instance to have a single thread pool
	private static MessageManager theInstance; // FIXME: Maybe a table, indexed by a profile subset, would be better?

	private static final String DUMMY_RECEIVER_NAME = "___DUMMY_";
	
	private static final int  POOL_SIZE_DEFAULT = 5;
	private static final int  MAX_POOL_SIZE = 100;
	
	private static final int  DELIVERY_TIME_THRESHOLD_DEFAULT = 1000; // ms
	private static final int  WARNING_QUEUE_SIZE_DEFAULT = 10000000; // 10MBytes
	private static final int  MAX_QUEUE_SIZE_DEFAULT = 100000000; // 100MBytes
	private static final int  SLEEP_TIME_FACTOR_DEFAULT = -1; // ms/MByes, -1=no sleep time

	private OutBox outBox;
	private Thread[] delivererThreads;
	private Deliverer[] deliverers;
	private boolean active = true;
	private long deliveryTimeThreshold;
	
	private Logger myLogger = Logger.getMyLogger(getClass().getName());

	private MessageManager() {
	}

	public static synchronized MessageManager instance(Profile p) {
		if(theInstance == null) {
			theInstance = new MessageManager();
			theInstance.initialize(p);
		}

		return theInstance;
	}

	public void initialize(Profile p) {
		// POOL_SIZE
		int poolSize = POOL_SIZE_DEFAULT;
		try {
			String tmp = p.getParameter("jade_core_messaging_MessageManager_poolsize", null);
			poolSize = Integer.parseInt(tmp);
		}
		catch (Exception e) {
			// Do nothing and keep default value
		}

		// OUT_BOX_MAX_SIZE
		deliveryTimeThreshold = DELIVERY_TIME_THRESHOLD_DEFAULT;
		try {
			String tmp = p.getParameter("jade_core_messaging_MessageManager_deliverytimethreshold", null);
			deliveryTimeThreshold = Integer.parseInt(tmp);
		}
		catch (Exception e) {
			// Do nothing and keep default value
		}
		
		// OUT_BOX_WARNING_SIZE
		int warningQueueSize = WARNING_QUEUE_SIZE_DEFAULT;
		try {
			String tmp = p.getParameter("jade_core_messaging_MessageManager_warningqueuesize", null);
			warningQueueSize = Integer.parseInt(tmp);
		}
		catch (Exception e) {
			// Do nothing and keep default value
		}
		
		// OUT_BOX_MAX_SIZE
		int maxQueueSize = MAX_QUEUE_SIZE_DEFAULT;
		try {
			String tmp = p.getParameter("jade_core_messaging_MessageManager_maxqueuesize", null);
			maxQueueSize = Integer.parseInt(tmp);
		}
		catch (Exception e) {
			// Do nothing and keep default value
		}

		// OUT_BOX_SLEEP_TIME_FACTOR
		int sleepTimeFactor = SLEEP_TIME_FACTOR_DEFAULT;
		try {
			String tmp = p.getParameter("jade_core_messaging_MessageManager_sleeptimefactor", null);
			sleepTimeFactor = Integer.parseInt(tmp);
		}
		catch (Exception e) {
			// Do nothing and keep default value
		}
		
		outBox = new OutBox(warningQueueSize, maxQueueSize, sleepTimeFactor);

		try {
			ResourceManager rm = p.getResourceManager();
			delivererThreads = new Thread[poolSize];
			deliverers = new Deliverer[poolSize];
			for (int i = 0; i < poolSize; ++i) {
				String name = "Deliverer-"+i;
				deliverers[i] = new Deliverer(name);
				delivererThreads[i] = rm.getThread(ResourceManager.TIME_CRITICAL, name, deliverers[i]);
				if (myLogger.isLoggable(Logger.FINE)) {
					myLogger.log(Logger.FINE, "Starting deliverer "+name+". Thread="+delivererThreads[i]);
				}
				delivererThreads[i].start();
			}
			
			// When the JADE Runtime terminates stop all deliverers.
			jade.core.Runtime.instance().invokeOnTermination(new Runnable() {

				public void run() {
					shutdown();
				}
			});
		}
		catch (ProfileException pe) {
			throw new RuntimeException("Can't get ResourceManager. "+pe.getMessage());
		}
	}
	
	private void shutdown() {
		myLogger.log(Logger.INFO, "MessageManager shutting down ...");
		active = false;
		// Submit 1 dummy message for each deliverer. 
		for (int i = 0; i < deliverers.length; ++i) {
			outBox.addLast(new AID(DUMMY_RECEIVER_NAME+i, AID.ISGUID), new GenericMessage(), null);
		}
		// Reset the MessageManager singleton instance 
		theInstance = null;
	}

	/**
	   Activate the asynchronous delivery of a GenericMessage
	 */
	public void deliver(GenericMessage msg, AID receiverID, Channel ch) {
		if (active) {
			try {
				outBox.addLast(receiverID, msg, ch);
			} catch(Exception e) {
				ch.notifyFailureToSender(msg, receiverID, new InternalError(e.getMessage()));
			}
		}
		else {
			myLogger.log(Logger.WARNING, "MessageManager NOT active. Cannot deliver message "+stringify(msg));
		}
	}



	/**
 	   Inner class Deliverer
	 */
	class Deliverer implements Runnable {

		private String name;
		// For debugging purpose
		private long servedCnt = 0;

		Deliverer(String name) {
			this.name = name;
		}
		
		public void run() {
			while (active) {
				// Get a message from the OutBox (block until there is one)
				PendingMsg pm = outBox.get();
				long startTime = System.currentTimeMillis();
				GenericMessage msg = pm.getMessage();
				AID receiverID = pm.getReceiver();

				// Deliver the message
				Channel ch = pm.getChannel();
				if (ch != null) {
					// Ch is null only in the case of dummy messages used to make the deliverers terminate.
					// See shutdown() method
					try {
						ch.deliverNow(msg, receiverID);
					}
					catch (Throwable t) {
						// A MessageManager deliverer thread must never die
						myLogger.log(Logger.WARNING, "MessageManager cannot deliver message "+stringify(msg)+" to agent "+receiverID.getName(), t);
						ch.notifyFailureToSender(msg, receiverID, new InternalError(ACLMessage.AMS_FAILURE_UNEXPECTED_ERROR + ": "+t));
					}
					servedCnt++;
					outBox.handleServed(receiverID);
					
					long deliveryTime = System.currentTimeMillis() - startTime;
					try {
						if (deliveryTimeThreshold > 0 && deliveryTime > deliveryTimeThreshold) {
							myLogger.log(Logger.WARNING, "Deliverer Thread "+name+ " - Delivery-time over threshold ("+deliveryTime+"). Receiver = "+receiverID.getLocalName()+", message size = "+msg.length());
						}
					}
					catch (Exception e) {
						myLogger.log(Logger.WARNING, "Unexpected error computing message delivery time", e);
					}
				}
			}
			
			myLogger.log(Logger.CONFIG, "Deliverer Thread "+name+ " terminated");
		}
		
		long getServedCnt() {
			return servedCnt;
		}
	} // END of inner class Deliverer	


	/**
	   Inner class PendingMsg
	 */
	public static class PendingMsg {
		private final GenericMessage msg;
		private final AID receiverID;
		private final Channel channel;
		private long deadline;

		public PendingMsg(GenericMessage msg, AID receiverID, Channel channel, long deadline) {
			this.msg = msg;
			this.receiverID = receiverID;
			this.channel = channel;
			this.deadline = deadline;
		}

		public GenericMessage getMessage() {
			return msg;
		}

		public AID getReceiver() {
			return receiverID;
		}

		public Channel getChannel() {
			return channel;
		}

		public long getDeadline() {
			return deadline;
		}

		public void setDeadline(long deadline) {
			this.deadline = deadline;
		}
	} // END of inner class PendingMsg


	/**
	 */
	public static final String stringify(GenericMessage m) {
		ACLMessage msg = m.getACLMessage();
		if (msg != null) {
			StringBuffer sb = new StringBuffer("(");
			sb.append(ACLMessage.getPerformative(msg.getPerformative()));
			sb.append(" sender: ");
			sb.append(msg.getSender().getName());
			if (msg.getOntology() != null) {
				sb.append(" ontology: ");
				sb.append(msg.getOntology());
			}
			if (msg.getConversationId() != null) {
				sb.append(" conversation-id: ");
				sb.append(msg.getConversationId());
			}
			sb.append(')');
			return sb.toString();
		}
		else {
			return ("\"Unavailable\"");
		}
	}

	// For debugging purpose
	String[] getQueueStatus() {
		return outBox.getStatus();
	}
	
	// For debugging purpose
	String getGlobalInfo() {
		return "Submitted-messages = "+outBox.getSubmittedCnt()+", Served-messages = "+outBox.getServedCnt()+", Queue-size (byte) = "+outBox.getSize();
	}

	// For debugging purpose
	String[] getThreadPoolStatus() {
		String[] status = new String[delivererThreads.length];
		for (int i = 0; i < delivererThreads.length; ++i) {
			status[i] = "(Deliverer-"+i+" :alive "+delivererThreads[i].isAlive()+" :Served-messages "+deliverers[i].getServedCnt()+")";
		}
		return status;
	}
	
	// For debugging purpose
	Thread[] getThreadPool() {
		return delivererThreads;
	}
}

