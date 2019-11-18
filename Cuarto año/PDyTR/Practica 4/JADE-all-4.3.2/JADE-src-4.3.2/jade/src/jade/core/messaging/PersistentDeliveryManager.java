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

//#J2ME_EXCLUDE_FILE


import java.io.IOException;

import jade.core.AID;
import jade.core.Profile;


import jade.util.leap.List;
import jade.util.leap.LinkedList;
import jade.util.leap.Map;
import jade.util.leap.HashMap;
import jade.util.leap.Iterator;


/**
 * This class supports the ACL persistent delivery service, managing
 * actual ACL messages storage, scheduled message delivery and other
 * utility tasks related to the service.
 *
 * @author  Giovanni Rimassa - FRAMeTech s.r.l.
 * @author  Nicolas Lhuillier - Motorola
 *
 */
class PersistentDeliveryManager {

	public static synchronized PersistentDeliveryManager instance(Profile p, MessageManager.Channel ch) {
		if(theInstance == null) {
			theInstance = new PersistentDeliveryManager();
			theInstance.initialize(p, ch);
		}

		return theInstance;
	}

	// How often to check for expired deliveries
	private static final long DEFAULT_SENDFAILUREPERIOD = 60*1000; // One minute
	
	private static final String FILE_STORAGE_SHORTCUT = "file";

	// Default storage class
	private static final String DEFAULT_STORAGE = "jade.core.messaging.PersistentDeliveryManager$DummyStorage";
	private static final String FILE_STORAGE = "jade.core.messaging.FileMessageStorage";

	private static class DeliveryItem {

		public DeliveryItem(GenericMessage msg, AID id, MessageManager.Channel ch, String sid) {
			toDeliver = msg;
			receiver = id;
			channel = ch;
			storeName = sid;
		}

		public GenericMessage getMessage() {
			return toDeliver;
		}

		public AID getReceiver() {
			return receiver;
		}

		public MessageManager.Channel getChannel() {
			return channel;
		}

		public String getStoreName() {
			return storeName;
		}

		private GenericMessage toDeliver;
		private AID receiver;
		private MessageManager.Channel channel;
		private String storeName;


	} // End of DeliveryItem class



	private class ExpirationChecker implements Runnable {

		public ExpirationChecker(long t) {
			period = t;
			myThread = new Thread(this, "Persistent Delivery Service -- Expiration Checker Thread");
		}

		public void run() {
			while(active) {
				try {
					Thread.sleep(period);
					synchronized(pendingMessages) {
						// Try to send all stored messages...
						// If the receiver still not exists and the due date has elapsed
						// the sender will get back a FAILURE
						Object[] keys = pendingMessages.keySet().toArray();
						for(int i = 0; i < keys.length; i++) {
							flushMessages((AID) keys[i]);
						}
					}
				}
				catch (InterruptedException ie) {
					// Just do nothing
				}
			}
		}

		public void start() {
			active = true;
			myThread.start();
		}

		public void stop() {
			active = false;
			myThread.interrupt();
		}

		private boolean active = false;
		private long period;
		private Thread myThread;

	} // End of ExpirationChecker class


	public static class DummyStorage implements MessageStorage {

		public void init(Profile p) {
			// Do nothing
		}

		public String store(GenericMessage msg, AID receiver) throws IOException {
			// Do nothing
			return null;
		}

		public void delete(String storeName, AID receiver) throws IOException {
			// Do nothing
		}

		public void loadAll(LoadListener il) throws IOException {
			// Do nothing
		}

	} // End of DummyStorage class


	public void initialize(Profile p, MessageManager.Channel ch) {

		users = 0;
		myMessageManager = MessageManager.instance(p);
		deliveryChannel = ch;

		try {
			// Choose the persistent storage method
			String storageClass = p.getParameter(PersistentDeliveryService.PERSISTENT_DELIVERY_STORAGEMETHOD,DEFAULT_STORAGE);
			if (FILE_STORAGE_SHORTCUT.equalsIgnoreCase(storageClass)) {
				storageClass = FILE_STORAGE;
			}
			storage = (MessageStorage)Class.forName(storageClass).newInstance();
			storage.init(p);

			// Load all data persisted from previous sessions
			storage.loadAll(new MessageStorage.LoadListener() {
				public void loadStarted(String storeName) {
					System.out.println("--> Load BEGIN <--");
				}

				public void itemLoaded(String storeName, GenericMessage msg, AID receiver) {

					// Put the item into the pending messages table
					synchronized(pendingMessages) {
						List msgs = (List)pendingMessages.get(receiver);
						if(msgs == null) {
							msgs = new LinkedList();
							pendingMessages.put(receiver, msgs);
						}

						DeliveryItem item = new DeliveryItem(msg, receiver, deliveryChannel, storeName);
						msgs.add(item);
					}

					System.out.println("Message for <" + receiver.getLocalName() + ">");
				}

				public void loadEnded(String storeName) {
					System.out.println("--> Load END <--");
				}

			});
		}
		catch(IOException ioe) {
			ioe.printStackTrace();
		}
		catch(Exception e) {
			e.printStackTrace();
		}

		sendFailurePeriod = DEFAULT_SENDFAILUREPERIOD;
		String s = p.getParameter(PersistentDeliveryService.PERSISTENT_DELIVERY_SENDFAILUREPERIOD, null);
		if(s != null) {
			try {
				sendFailurePeriod = Long.parseLong(s);
			}
			catch(NumberFormatException nfe) {
				// Do nothing: the default value will be used...
			}
		}
	}

	public void storeMessage(String storeName, GenericMessage msg, AID receiver) throws IOException {

		// Store the ACL message and its receiver for later re-delivery...
		synchronized(pendingMessages) {
			List msgs = (List)pendingMessages.get(receiver);
			if(msgs == null) {
				msgs = new LinkedList();
				pendingMessages.put(receiver, msgs);
			}

			String tmpName = storage.store(msg, receiver);
			msgs.add(new DeliveryItem(msg, receiver, deliveryChannel, tmpName));
		}

	}

	public int flushMessages(AID receiver) {

		// Send messages for this agent, if any...
		int cnt = 0;
		List l = null;
		synchronized(pendingMessages) {
			l = (List)pendingMessages.remove(receiver);
		}

		if(l != null) {
			Iterator it = l.iterator();
			while(it.hasNext()) {
				DeliveryItem item = (DeliveryItem)it.next();
				retry(item);
				cnt++;
			}
		}
		return cnt;
	}

	public synchronized void start() {

		if(users == 0) {
			failureSender = new ExpirationChecker(sendFailurePeriod);
			failureSender.start();
		}

		users++;

	}

	public synchronized void stop() {
		users--;

		if(users == 0) {
			failureSender.stop();
		}
	}


	// A shared instance to have a single thread pool
	private static PersistentDeliveryManager theInstance; // FIXME: Maybe a table, indexed by a profile subset, would be better?


	private PersistentDeliveryManager() {
	}

	private void retry(DeliveryItem item) {
		// Remove the message from the storage
		try {
			storage.delete(item.getStoreName(), item.getReceiver());
		}
		catch(IOException ioe) {
			ioe.printStackTrace();
		}
		// Deliver it
		myMessageManager.deliver(item.getMessage(), item.getReceiver(), item.getChannel());
	}


	// The component managing asynchronous message delivery and retries
	private MessageManager myMessageManager;

	// The actual channel over which messages will be sent
	private MessageManager.Channel deliveryChannel;

	// How often pending messages due date will be checked (the
	// message will be sent out if expired)
	private long sendFailurePeriod;

	// How many containers are sharing this active component
	private long users;

	// The table of undelivered messages to send
	private Map pendingMessages = new HashMap();

	// The active object that periodically checks the due date of ACL
	// messages and sends them after it expired
	private ExpirationChecker failureSender;

	// The component performing the actual storage and retrieval from
	// a persistent support
	private MessageStorage storage;
}
