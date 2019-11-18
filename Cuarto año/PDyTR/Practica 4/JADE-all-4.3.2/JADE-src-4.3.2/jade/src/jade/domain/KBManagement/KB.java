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

import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.proto.SubscriptionResponder;
import jade.util.leap.List;
import jade.util.Logger;

import java.util.Enumeration;


/**
 * @author Elisabetta Cortese - TILab
 *
 */


/** Base class for AMS and DF Knowledge Base*/
public abstract class KB {
	protected LeaseManager lm;
	protected SubscriptionResponder sr;
	protected Logger logger = Logger.getMyLogger(this.getClass().getName());
	private int maxResultLimit = -1;
	
	public KB(int maxResultLimit) {
		this.maxResultLimit = maxResultLimit;
	}
	
	public void setSubscriptionResponder(SubscriptionResponder sResp){
		sr = sResp;
	}
	
	public void setLeaseManager(LeaseManager leaseMng){
		lm = leaseMng;
	}
	
	public Object register(Object name, Object fact) {
		// We don't want to register a fact whose lease time has 
		// already expired
		if (lm.isExpired(lm.getLeaseTime(fact))) {
			if(logger.isLoggable(Logger.WARNING))
				logger.log(Logger.WARNING,"Fact with lease time already expired");
			return null;
		}
		
		// Now apply lease manager policy on requested lease time.
		lm.grantLeaseTime(fact);
		
		Object previous = insert(name, fact);
		if (previous != null && lm.isExpired(lm.getLeaseTime(previous))) {
			previous = null;
		}
		return previous;
	}
	
	public Object deregister(Object name) {
		Object obj = remove(name);
		if (obj != null && lm.isExpired(lm.getLeaseTime(obj))) {
			obj = null;
		}
		return obj;
	}
	
	protected abstract Object insert(Object name, Object fact);
	protected abstract Object remove(Object name);
	
	public List search(Object template) {
		return search(template, maxResultLimit);
	}
	
	public abstract List search(Object template, int maxResult);
	public abstract KBIterator iterator(Object template);
	
	public abstract void subscribe(Object template, SubscriptionResponder.Subscription s) throws NotUnderstoodException;
	public abstract Enumeration getSubscriptions();
	public abstract void unsubscribe(SubscriptionResponder.Subscription sub);
}