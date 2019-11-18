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

package jade.domain.KBManagement;

//#MIDP_EXCLUDE_FILE

import java.util.Enumeration;
import java.util.Hashtable;

import jade.util.leap.Map;
import jade.util.leap.HashMap;
import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import jade.util.Logger;

import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;

import jade.lang.acl.ACLMessage;
import jade.proto.SubscriptionResponder;
import jade.core.AID;

/**
 * Common base class for AMS and DF Knowledge Base
 * 
 * @author Elisabetta Cortese - TILab
 */
public abstract class MemKB extends KB {

	protected Map facts = new HashMap();
	protected Hashtable subscriptions = new Hashtable();
	protected LeaseManager lm;
	protected int currentReg = 0;
	protected SubscriptionResponder sr;

	protected final static int MAX_REGISTER_WITHOUT_CLEAN = 100;

	private Logger logger = Logger.getMyLogger(this.getClass().getName());
	
	
	public MemKB(int maxResultLimit) {
	    super(maxResultLimit);
	}

	protected Object insert(Object name, Object fact) {
		currentReg ++;
		if(currentReg > MAX_REGISTER_WITHOUT_CLEAN){
			clean();
			currentReg = 0;
		}
     
	  return facts.put(name, fact);
	}

	protected Object remove(Object name) {
	    return facts.remove(name);
	}

	  // This abstract method has to perform pattern matching
	protected abstract boolean match(Object template, Object fact);


	protected abstract void clean();

	public List search(Object template, int maxResults) {
		List result = new ArrayList();
	    Iterator it = facts.values().iterator();
		int found = 0;
	    while(it.hasNext() && ((maxResults < 0) || (found < maxResults)) ) {
	        Object fact = it.next();
		    if(match(template, fact)){
				result.add(fact);
				found ++;
		    }
      	}
        return result;
	}
	
	/**
	   Iterated search is only supported in a DB-based KB.
	   Throw a RuntimeException. The requester will get back a FAILURE
	 */
	public KBIterator iterator(Object template) {
		throw new RuntimeException("Iterated search non supported");
	}

	//
	public void subscribe(Object dfd, SubscriptionResponder.Subscription s) throws NotUnderstoodException{
		try{
			DFAgentDescription dfdTemplate = (DFAgentDescription) dfd;
			ACLMessage aclSub = (ACLMessage) s.getMessage();
			subscriptions.put(dfdTemplate, s);
		}catch(Exception e){
                  if(logger.isLoggable(Logger.SEVERE))
                    logger.log(Logger.SEVERE,"Subscribe error: "+e.getMessage());
			throw new NotUnderstoodException(e.getMessage());
		}
	}


	public Enumeration getSubscriptionDfAgentDescriptions(){
		return subscriptions.keys();
	}


	private SubscriptionResponder.Subscription getSubscription(Object key){
		SubscriptionResponder.Subscription sub = (SubscriptionResponder.Subscription)subscriptions.get(key);
		return sub;
	}


	// RITORNA Lista delle sottoscrizioni
	public	Enumeration getSubscriptions(){
		return	subscriptions.elements();
	}

	int offSetForSubscriptionToReturn = 0;

	// RITORNA Lista delle sottoscrizioni
	public	Enumeration getSubscriptions(int offset){
		// FIXME to do if this operation return a lot of subscriptions
		return null;
	}


	public void unsubscribe(SubscriptionResponder.Subscription sub ){

		ACLMessage aclSub = sub.getMessage();
		String convID = aclSub.getConversationId();
		Enumeration e = getSubscriptionDfAgentDescriptions();

		if( e != null ){
			while(e.hasMoreElements()){
				DFAgentDescription dfd = (DFAgentDescription)e.nextElement();
				SubscriptionResponder.Subscription s = getSubscription((Object) dfd);
				if((s.getMessage().getConversationId()).equals(convID)){
					subscriptions.remove(dfd);
					break;
				}
			}
		}
	}


	// Helper method to match two Agent Identifiers
	public static final boolean matchAID(AID template, AID fact) {

	  // Match the GUID in the ':name' slot
	    String templateName = template.getName();
	    if(templateName != null) {
	        String factName = fact.getName();
	        if((factName == null) || (!templateName.equalsIgnoreCase(factName)))
		    	return false;
	    }

	    // Match the address sequence. See 'FIPA Agent Management Specification, Sect. 6.4.2.1'
	    Iterator itTemplate = template.getAllAddresses();
	    Iterator itFact = fact.getAllAddresses();

	    // All the elements in the template sequence must appear in the
	    // fact sequence, in the same order
	    while(itTemplate.hasNext()) {
	        String templateAddr = (String)itTemplate.next();

	        // Search 'templateAddr' into the remaining part of the fact sequence
	        boolean found = false;
	        while(!found && itFact.hasNext()) {
	  	        String factAddr = (String)itFact.next();
		        found = templateAddr.equalsIgnoreCase(factAddr);
	        }
	        if(!found) // An element of the template does not appear in the fact sequence
				return false;
	   	}

	    // Match the resolvers sequence. See 'FIPA Agent Management Specification, Sect. 6.4.2.1'
	    itTemplate = template.getAllResolvers();
	    itFact = fact.getAllResolvers();

	    while(itTemplate.hasNext()) {
	      AID templateRes = (AID)itTemplate.next();

	      // Search 'templateRes' into the remaining part of the fact sequence
	      boolean found = false;
	      while(!found && itFact.hasNext()) {
			AID factRes = (AID)itFact.next();
			found = matchAID(templateRes, factRes); // Recursive call
	      }
	      if(!found) // An element of the template does not appear in the fact sequence
			  return false;
	    }

	    return true;
	  }


}
