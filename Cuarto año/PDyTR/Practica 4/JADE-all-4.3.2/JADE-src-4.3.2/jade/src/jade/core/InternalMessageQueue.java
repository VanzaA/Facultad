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

package jade.core;

import jade.util.Logger;
import jade.util.leap.Iterator;
import jade.util.leap.EnumIterator;
import jade.util.leap.List;
import jade.util.leap.LinkedList;

import java.util.Vector;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 @author Giovanni Rimassa - Universita` di Parma
 @version $Date: 2006-01-12 13:21:47 +0100 (gio, 12 gen 2006) $ $Revision: 5847 $
 */
class InternalMessageQueue implements MessageQueue {

	//#MIDP_EXCLUDE_BEGIN
	// In MIDP we use Vector instead of jade.util.leap.LinkedList as the latter has been implemented in terms of the first
	private LinkedList list;
	//#MIDP_EXCLUDE_END
	/*#MIDP_INCLUDE_BEGIN
	 private Vector list;
	 #MIDP_INCLUDE_END*/

	private int maxSize;
	private Agent myAgent;
	private Logger myLogger = Logger.getJADELogger(getClass().getName());

	public InternalMessageQueue(int size, Agent a) {
		maxSize = size;
		myAgent = a;
		//#MIDP_EXCLUDE_BEGIN
		list = new LinkedList();
		//#MIDP_EXCLUDE_END
		/*#MIDP_INCLUDE_BEGIN
		 list = new Vector();
		 #MIDP_INCLUDE_END*/
	}

	public InternalMessageQueue() {
		this(0, null);
	}

	public boolean isEmpty() {
		return list.isEmpty();
	}

	public void setMaxSize(int newSize) throws IllegalArgumentException {
		if(newSize < 0)
			throw new IllegalArgumentException("Invalid MsgQueue size");
		maxSize = newSize;
	}

	public int getMaxSize() {
		return maxSize;
	}

	/**
	 * @return the number of messages
	 * currently in the queue
	 **/
	public int size() {
		return list.size();
	}

	public void addFirst(ACLMessage msg) {
		if((maxSize != 0) && (list.size() >= maxSize)) {
			//#MIDP_EXCLUDE_BEGIN
			list.removeFirst(); // FIFO replacement policy
		}
		list.addFirst(msg);
		//#MIDP_EXCLUDE_END
		/*#MIDP_INCLUDE_BEGIN
		 list.setElementAt(msg,0);
		 } else
		 list.insertElementAt(msg,0);
		 #MIDP_INCLUDE_END*/
	}

	public void addLast(ACLMessage msg) {
		if((maxSize != 0) && (list.size() >= maxSize)){
			//#MIDP_EXCLUDE_BEGIN
			list.removeFirst(); // FIFO replacement policy
			myLogger.log(Logger.SEVERE, "Agent "+getAgentName()+" - Message queue size exceeded. Message discarded!!!!!");
		}
		list.addLast(msg);
		//#MIDP_EXCLUDE_END
		/*#MIDP_INCLUDE_BEGIN
		 list.removeElementAt(0);
		 } 
		 list.addElement(msg);
		 #MIDP_INCLUDE_END*/
	}

	private String getAgentName() {
		return myAgent != null ? myAgent.getLocalName() : "null";
	}
	
	public ACLMessage receive(MessageTemplate pattern) {
		ACLMessage result = null;
		// This is just for the MIDP implementation where iterator.remove() is not supported. 
		// We don't surround it with preprocessor directives to avoid making the code unreadable
		int cnt = 0;
		for (Iterator messages = iterator(); messages.hasNext(); cnt++) {
			ACLMessage msg = (ACLMessage)messages.next();
			if (pattern == null || pattern.match(msg)) {
				//#MIDP_EXCLUDE_BEGIN
				messages.remove();
				//#MIDP_EXCLUDE_END
				/*#MIDP_INCLUDE_BEGIN
				 list.removeElementAt(cnt);
				 #MIDP_INCLUDE_END*/
				result = msg;
				break;
			}
		}
		return result;
	}

	private Iterator iterator() {
		//#MIDP_EXCLUDE_BEGIN
		return list.iterator();
		//#MIDP_EXCLUDE_END
		/*#MIDP_INCLUDE_BEGIN
		 return new EnumIterator(list.elements());
		 #MIDP_INCLUDE_END*/
	}

	//#J2ME_EXCLUDE_BEGIN	
	// For persistence service
	private void setMessages(java.util.List l) {
		// FIXME: To be implemented
		System.out.println(">>> MessageQueue::setMessages() <<<");
	}

	// For persistence service
	private java.util.List getMessages() {
		// FIXME: To be implemented
		System.out.println(">>> MessageQueue::getMessages() <<<");
		return null;
	}
	//#J2ME_EXCLUDE_END


	// For persistence service
	private Long persistentID;

	// For persistence service
	private Long getPersistentID() {
		return persistentID;
	}

	// For persistence service
	private void setPersistentID(Long l) {
		persistentID = l;
	}

	//#MIDP_EXCLUDE_BEGIN
	public void copyTo(List messages) {
		for (Iterator i = iterator(); i.hasNext(); messages.add(i.next()));
	}
	
	// For debugging purpose
	Object[] getAllMessages() {
		return list.toArray();
	}
	
	void cleanOldMessages(long maxTime, MessageTemplate pattern) {
		long now = System.currentTimeMillis();
		int cnt = 0;
		for (Iterator messages = iterator(); messages.hasNext(); cnt++) {
			ACLMessage msg = (ACLMessage)messages.next();
			long postTime = msg.getPostTimeStamp();
			if (postTime > 0 && ((now - postTime) > maxTime)) {
				if (pattern == null || pattern.match(msg)) {
					messages.remove();
				}
			}
		}
	}
	//#MIDP_EXCLUDE_END

}
