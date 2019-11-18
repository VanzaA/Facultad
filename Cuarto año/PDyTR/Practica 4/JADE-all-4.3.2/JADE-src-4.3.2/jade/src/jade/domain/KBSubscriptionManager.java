/*****************************************************************
 JADE - Java Agent DEvelopment Framework is a framework to develop 
 multi-agent systems in compliance with the FIPA specifications.
 Copyright (C) 2000 CSELT S.p.A. 
 
 The updating of this file to JADE 2.0 has been partially supported by the IST-1999-10211 LEAP Project
 
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

package jade.domain;

//#MIDP_EXCLUDE_FILE

import java.util.Enumeration;

import jade.util.leap.ArrayList;
import jade.util.leap.Collection;
import jade.util.leap.List;
import jade.util.leap.Iterator;
import jade.util.leap.Map;
import jade.util.leap.HashMap;
import jade.util.Logger;

import jade.domain.FIPAAgentManagement.*;
import jade.domain.KBManagement.*;
import jade.lang.acl.ACLMessage;

import jade.proto.SubscriptionResponder;

import jade.content.*;
import jade.content.lang.sl.*;
import jade.content.onto.*;
import jade.content.abs.*;

/**
 * @author Elisabetta Cortese - TILab
 *
 */
class KBSubscriptionManager implements SubscriptionResponder.SubscriptionManager {
	private Logger myLogger = Logger.getMyLogger(getClass().getName());
	
	private Map subscriptionsCache = new HashMap();
	private SubscriptionInfo[] subscriptions = null;
	
	KB kBase;
	ContentManager cm;
	
	public KBSubscriptionManager(KB k){
		super();
		kBase = k;
	}
	
	public void setContentManager(ContentManager c){
		cm = c;
	}
	
	public boolean register(SubscriptionResponder.Subscription sub) throws RefuseException, NotUnderstoodException{
		
		DFAgentDescription dfdTemplate = null;
		SearchConstraints constraints = null;
		AbsIRE absIota = null;
		
		try {
			// Get DFD template and search constraints from the subscription message 
			ACLMessage subMessage = sub.getMessage();
			if (myLogger.isLoggable(Logger.CONFIG)) {
				myLogger.log(Logger.CONFIG, "Registering subscription "+subMessage.getConversationId()+". Agent is "+subMessage.getSender().getName());
			}
			
			absIota = (AbsIRE) cm.extractAbsContent(subMessage);
			AbsPredicate absResult = absIota.getProposition();
			AbsAgentAction absAction = (AbsAgentAction) absResult.getAbsObject(BasicOntology.RESULT_ACTION);
			AbsAgentAction absSearch = (AbsAgentAction) absAction.getAbsObject(BasicOntology.ACTION_ACTION);
			Search search = (Search) FIPAManagementOntology.getInstance().toObject(absSearch);
			
			dfdTemplate = (DFAgentDescription) search.getDescription();
			constraints = search.getConstraints();
			
			// Register the Subscription
			kBase.subscribe(dfdTemplate, sub);
			
			// Update the cache
			synchronized (subscriptionsCache) {
				if (subscriptions != null) {
					subscriptionsCache.put(subMessage.getConversationId(), new SubscriptionInfo(sub, dfdTemplate, absIota));
					subscriptions = toArray(subscriptionsCache);
				}
			}
		}
		catch(Exception e) {
			throw new NotUnderstoodException(e.getMessage());
		}
		// Search for DFDs that already match the specified template.
		// Note that we ignore the SearchConstraint.maxResult here
		// FIXME: Getting all DFDs matching a template may cause out-of-memory.
		// We should use an iterated search and send back possibly more than one
		// notification.
		List results = kBase.search(dfdTemplate, -1); 
		
		// If some DFD matches the template, notify the subscribed agent 
		if(results.size() > 0){
			notify(sub, results, absIota);
			return true;
		}
		return false;
	}
	
	
	public boolean deregister( SubscriptionResponder.Subscription sub ) throws FailureException {
		if (myLogger.isLoggable(Logger.CONFIG)) {
			ACLMessage subMessage = sub.getMessage();
			myLogger.log(Logger.CONFIG, "Deregistering subscription "+subMessage.getConversationId()+". Agent is "+subMessage.getSender().getName());
		}
		kBase.unsubscribe(sub);
		
		// Update the cache
		synchronized (subscriptionsCache) {
			if (subscriptions != null) {
				subscriptionsCache.remove(sub.getMessage().getConversationId());
				subscriptions = toArray(subscriptionsCache);
			}
		}
		return false;
	}
	
	
	/**
	 Handle registrations/deregistrations/modifications by notifying 
	 subscribed agents if necessary. 
	 NOTE that if a df poolsize > 0 is specified this method is executed in a different Thread
	 --> This is the reason for the synchronized blocks
	 */
	void handleChange(DFAgentDescription dfd, DFAgentDescription oldDfd) {
		synchronized (subscriptionsCache) {
			if (subscriptions == null) {
				subscriptionsCache = loadSubscriptionsCache();
				subscriptions = toArray(subscriptionsCache);
			}
		}
		for (int i = 0; i < subscriptions.length; ++i) {
			SubscriptionInfo info = subscriptions[i];
			DFAgentDescription template = info.getTemplate();
			if ( DFMemKB.compare(template, dfd) || ((oldDfd!=null) && DFMemKB.compare(template, oldDfd))) {
				// This subscriber must be notified
				List results = new ArrayList();
				results.add(dfd);
				if (myLogger.isLoggable(Logger.FINE)) {
					ACLMessage subMessage = info.getSubscription().getMessage();
					myLogger.log(Logger.FINE, "Notifying subscribed agent "+subMessage.getSender().getName()+" ["+subMessage.getConversationId()+"] ");
				}
				notify(info.getSubscription(), results, info.getAbsIota());
			}
		}
	}
	
	private Map loadSubscriptionsCache() {
		Map m = new HashMap();
		Enumeration e = kBase.getSubscriptions();
		while (e.hasMoreElements()) {
			SubscriptionResponder.Subscription sub = (SubscriptionResponder.Subscription) e.nextElement();
			
			try {
				AbsIRE absIota = (AbsIRE) cm.extractAbsContent(sub.getMessage());
				AbsPredicate absResult = absIota.getProposition();
				AbsAgentAction absAction = (AbsAgentAction) absResult.getAbsObject(BasicOntology.RESULT_ACTION);
				AbsAgentAction absSearch = (AbsAgentAction) absAction.getAbsObject(BasicOntology.ACTION_ACTION);
				Search search = (Search) FIPAManagementOntology.getInstance().toObject(absSearch);		
				DFAgentDescription template = (DFAgentDescription) search.getDescription();
				
				m.put(sub.getMessage().getConversationId(), new SubscriptionInfo(sub, template, absIota));
			}
			catch (Exception ex) {
				// Should never happen since, this has already been decoded correctly once
				ex.printStackTrace();
			}
		}
		return m;
	}

	private void notify(SubscriptionResponder.Subscription sub, List results, AbsIRE absIota) {
		try {
			ACLMessage notification = sub.getMessage().createReply();
			notification.addUserDefinedParameter(ACLMessage.IGNORE_FAILURE, "true");
			notification.setPerformative(ACLMessage.INFORM);
			AbsPredicate absEquals = new AbsPredicate(SLVocabulary.EQUALS);
			absEquals.set(SLVocabulary.EQUALS_LEFT, absIota);
			absEquals.set(SLVocabulary.EQUALS_RIGHT, FIPAManagementOntology.getInstance().fromObject(results));
			
			cm.fillContent(notification, absEquals);
			//pass to Subscription the message to send
			sub.notify(notification);
		}
		catch (Exception e) {
			e.printStackTrace();
			//FIXME: Check whether a FAILURE message should be sent back.       
		}
	}
	
	private static final SubscriptionInfo[] toArray(Map m) {
		Collection c = m.values();
		SubscriptionInfo[] result = new SubscriptionInfo[c.size()];
		Iterator it = c.iterator();
		int i = 0;
		while (it.hasNext()) {
			result[i] = (SubscriptionInfo) it.next();
			++i;
		}
		return result;
	}
	
	/**
	 * Inner class SubscriptionInfo
	 * This class associates a Subscription object with the DFAgentDescription that acts as
	 * template for that Subscription object
	 */
	private class SubscriptionInfo {
		private SubscriptionResponder.Subscription subscription;
		private DFAgentDescription template;
		private AbsIRE absIota;
		
		private SubscriptionInfo(SubscriptionResponder.Subscription subscription, DFAgentDescription template, AbsIRE absIota) {
			this.subscription = subscription;
			this.template = template;
			this.absIota = absIota;
		}
		
		public SubscriptionResponder.Subscription getSubscription() {
			return subscription;
		}
		
		public DFAgentDescription getTemplate() {
			return template;
		}
		
		public AbsIRE getAbsIota() {
			return absIota;
		}
	}
}
