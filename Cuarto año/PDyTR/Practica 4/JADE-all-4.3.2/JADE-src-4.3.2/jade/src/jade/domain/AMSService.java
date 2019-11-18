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

package jade.domain;

import jade.util.leap.*;

import jade.domain.FIPAAgentManagement.*;
import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

//import jade.content.lang.sl.SimpleSLTokenizer;
//import jade.content.lang.sl.SL0Vocabulary;

import jade.content.*;
import jade.content.lang.*;
import jade.content.lang.sl.*;
import jade.content.lang.Codec.*;
import jade.content.onto.Ontology;
import jade.content.onto.basic.Action;
import jade.content.onto.basic.Result;
import jade.content.onto.OntologyException;

/**
 * This class provides a set of static methods to communicate with
 * a AMS Service that complies with FIPA specifications.
 * Notice that JADE calls automatically the register and deregister methods 
 * with the default AMS respectively before calling <code>Agent.setup()</code> 
 * method and just 
 * after <code>Agent.takeDown()</code> method returns; so there is no need for a normal 
 * programmer to call them. 
 * However, under certain circumstances, a programmer might need to call its 
 * methods. To give some examples: when an agent wishes to register with the 
 * AMS of a remote agent platform, or when an agent wishes to modify its 
 * description by adding a private address to the set of its addresses, ...
 * <p>
 * It includes methods to register, deregister, modify and search with an AMS. 
 * Each of this method has version with all the needed parameters, or with a 
 * subset of them where, those parameters that can be omitted have been 
 * defaulted to the default AMS of the platform, the AID of the sending agent,
 *  the default Search Constraints.
 * Notice that all these methods blocks every activity of the agent until the
 * action (i.e. register/deregister/modify/search) has been successfully 
 * executed or a jade.domain.FIPAException exception has been thrown 
 * (e.g. because a FAILURE message has been received by the AMS). 
 * In some cases, instead, it is more convenient to execute this task in a 
 * non-blocking way. The method getNonBlockingBehaviour() returns a 
 * non-blocking behaviour of type RequestFIPAServiceBehaviour that can be 
 * added to the queue of the agent behaviours, as usual, by using 
 * <code>Agent.addBehaviour()</code>. 
 * <br>
 * <b>The MIDP version of this class only includes the 
 * <code>getFailedReceiver()</code> method.</b>
 * <br>
 *
 * @author Fabio Bellifemine - CSELT S.p.A.
 @version $Date: 2012-09-10 10:53:21 +0200 (lun, 10 set 2012) $ $Revision: 6588 $ 
 **/
public class AMSService extends FIPAService {
	
	//#MIDP_EXCLUDE_BEGIN
	private static Codec c = new SLCodec();
	private static Ontology o = FIPAManagementOntology.getInstance();
	private static ContentManager cm = new ContentManager();
	static {
		cm.registerLanguage(c, FIPANames.ContentLanguage.FIPA_SL0);
		cm.registerOntology(o);
	}
	
	/**
	 * check that the <code>AMSAgentDescription</code> contains the mandatory
	 * slots, i.e. the agent name and the agent state. 
	 * @throw a MissingParameter exception is it is not valid
	 */
	static void checkIsValid(AMSAgentDescription amsd) throws MissingParameter {
		// FIXME: use FIPAManagementOntology constants instead of Strings  
		if (amsd.getName()==null) 
			throw new MissingParameter(FIPAManagementOntology.AMSAGENTDESCRIPTION, "name");
		if (amsd.getState()==null) 
			throw new MissingParameter(FIPAManagementOntology.AMSAGENTDESCRIPTION, "state");
	}
	
	/**
	 Register a AMSAgentDescription with a <b>AMS</b> agent. 
	 However, 
	 since <b>AMS</b> registration and
	 deregistration are automatic in JADE, this method should not be
	 used by application programmers to register with the default AMS.
	 @param a is the Agent performing the registration 
	 @param AMSName The AID of the <b>AMS</b> agent to register with.
	 @param amsd A <code>AMSAgentDescriptor</code> object containing all
	 data necessary to the registration. If the Agent name is empty, than
	 it is set according to the <code>a</code> parameter. If the Agent state is
	 empty, than it is set to ACTIVE.
	 @exception FIPAException A suitable exception can be thrown when
	 a <code>refuse</code> or <code>failure</code> messages are
	 received from the AMS to indicate some error condition or when
	 the method locally discovers that the amsdescription is not valid.
	 */
	public static void register(Agent a, AID AMSName, AMSAgentDescription amsd) throws FIPAException {
		ACLMessage request = createRequestMessage(a, AMSName);
		
		if (amsd.getName() == null)
			amsd.setName(a.getAID());
		if (amsd.getState() == null)
			amsd.setState(AMSAgentDescription.ACTIVE);
		checkIsValid(amsd);
		
		// Build a AMS action object for the request
		Register r = new Register();
		r.setDescription(amsd);
		
		Action act = new Action();
		
		act.setActor(AMSName);
		act.setAction(r);
		
		synchronized (cm) {
			try{    
				cm.fillContent(request, act);
			}
			catch(Exception e){
				throw new FIPAException("Error encoding REQUEST content. "+e);
			}
		}
		
		// Send message and collect reply
		doFipaRequestClient(a,request);
	}
	
	
	/**
	 * registers a <code>AMSAgentDescription</code> with the default AMS
	 * @see #register(Agent,AID,AMSAgentDescription)
	 **/
	public static void register(Agent a, AMSAgentDescription amsd) throws FIPAException {
		register(a,a.getAMS(),amsd);
	}
	
	/**
	 Deregister a AMSAgentDescription from a <b>AMS</b> agent. However, since <b>AMS</b> registration and
	 deregistration are automatic in JADE, this method should not be
	 used by application programmers to deregister with the default AMS.
	 @param AMSName The AID of the <b>AMS</b> agent to deregister from.
	 @param amsd A <code>AMSAgentDescription</code> object containing all
	 data necessary to the deregistration.
	 @exception FIPAException A suitable exception can be thrown when
	 a <code>refuse</code> or <code>failure</code> messages are
	 received from the AMS to indicate some error condition or when
	 the method locally discovers that the amsdescription is not valid.
	 */
	public static void deregister(Agent a, AID AMSName, AMSAgentDescription amsd) throws FIPAException {
		
		ACLMessage request = createRequestMessage(a, AMSName);
		
		if (amsd.getName() == null)
			amsd.setName(a.getAID());
		if (amsd.getState() == null)
			amsd.setState(AMSAgentDescription.ACTIVE);
		// Build a AMS action object for the request
		Deregister d = new Deregister();
		d.setDescription(amsd);
		
		Action act = new Action();
		act.setActor(AMSName);
		act.setAction(d);
		
		synchronized (cm) {
			try{    
				cm.fillContent(request, act);
			}
			catch(Exception e){
				throw new FIPAException("Error encoding REQUEST content. "+e);
			}
		}
		
		// Send message and collect reply
		doFipaRequestClient(a,request);
	}
	
	/**
	 The AID of the AMS is defaulted to the AMS of this platform.
	 @see #deregister(Agent a, AID AMSName, AMSAgentDescription amsd)
	 **/
	public static void deregister(Agent a, AMSAgentDescription amsd) throws FIPAException {
		deregister(a,a.getAMS(),amsd);
	}
	
	/**
	 A default AMSAgentDescription is used for this agent, where only AID and state
	 are set (state is set to ACTIVE).
	 @see #deregister(Agent a, AID AMSName, AMSAgentDescription amsd)
	 **/
	public static void deregister(Agent a, AID AMSName) throws FIPAException {
		AMSAgentDescription amsd = new AMSAgentDescription();
		amsd.setName(a.getAID());
		deregister(a,AMSName,amsd);
	}
	
	/**
	 A default AMSAgentDescription is used for this agent, where only AID and state
	 are set.
	 The AID of the AMS is defaulted to the AMS of this platform.
	 @see #deregister(Agent a, AID AMSName, AMSAgentDescription amsd)
	 **/
	public static void deregister(Agent a) throws FIPAException {
		AMSAgentDescription amsd = new AMSAgentDescription();
		amsd.setName(a.getAID());
		deregister(a,amsd);
	}
	
	
	/**
	 Modifies data contained within a <b>AMS</b>
	 agent. 
	 @param AMSName The GUID of the <b>AMS</b> agent holding the data
	 to be changed.
	 @param amsd The new <code>AMSAgentDescriptor</code> object 
	 that should modify the existing one. 
	 @exception FIPAException A suitable exception can be thrown when
	 a <code>refuse</code> or <code>failure</code> messages are
	 received from the AMS to indicate some error condition or when
	 the method locally discovers that the amsdescription is not valid.
	 */
	public static void modify(Agent a, AID AMSName, AMSAgentDescription amsd) throws FIPAException {
		ACLMessage request = createRequestMessage(a, AMSName);
		
		if (amsd.getName() == null)
			amsd.setName(a.getAID());
		checkIsValid(amsd);
		// Build a AMS action object for the request
		Modify m = new Modify();
		m.setDescription(amsd);
		
		Action act = new Action();
		act.setActor(AMSName);
		act.setAction(m);
		
		synchronized (cm) {
			try{    
				cm.fillContent(request, act);
			}
			catch(Exception e){
				throw new FIPAException("Error encoding REQUEST content. "+e);
			}
		}
		
		// Send message and collect reply
		doFipaRequestClient(a,request);
	}
	
	/**
	 The AID of the AMS is defaulted to the AMS of this platform.
	 @see #modify(Agent a, AID AMSName, AMSAgentDescription amsd)
	 **/
	public static void modify(Agent a, AMSAgentDescription amsd) throws FIPAException {
		modify(a,a.getAMS(),amsd);
	}
	
	/**
	 Searches for data contained within a <b>AMS</b> agent. 
	 @param a is the Agent performing the search 
	 @param AMSName The GUID of the <b>AMS</b> agent to start search from.
	 @param amsd A <code>AMSAgentDescriptor</code> object containing
	 data to search for; this parameter is used as a template to match
	 data against.
	 @param constraints of the search 
	 @return An array of <code>AMSAgentDescription</code> 
	 containing all found
	 items matching the given
	 descriptor, subject to given search constraints for search depth
	 and result size.
	 @exception FIPAException A suitable exception can be thrown when
	 a <code>refuse</code> or <code>failure</code> messages are
	 received from the AMS to indicate some error condition.
	 */
	public static AMSAgentDescription[] search(Agent a, AID AMSName, AMSAgentDescription amsd, SearchConstraints constraints) throws FIPAException {
		ACLMessage request = createRequestMessage(a, AMSName);
		
		// Build a AMS action object for the request
		Search s = new Search();
		s.setDescription(amsd);
		s.setConstraints(constraints);
		
		Action act = new Action();
		act.setActor(AMSName);
		act.setAction(s);
		
		synchronized (cm) {
			try{    
				cm.fillContent(request, act);
			}
			catch(Exception e){
				throw new FIPAException("Error encoding REQUEST content. "+e);
			}
		}
		
		// Send message and collect reply
		ACLMessage inform = doFipaRequestClient(a,request);
		
		Result r = null;
		synchronized (cm) {
			try{
				r = (Result) cm.extractContent( inform );
			}
			catch(Exception e){
				throw new FIPAException("Error decoding INFORM content. "+e);
			}
		}
		
		return toArray(r.getItems());
	}
	
	private static AMSAgentDescription[] toArray(List l) throws FIPAException {
		try {
			AMSAgentDescription[] items = new AMSAgentDescription[l.size()];
			for(int i = 0; i < l.size(); i++){
				items[i] = (AMSAgentDescription)l.get(i);
			}
			return items;
		}
		catch (ClassCastException cce) {
			throw new FIPAException("Found items are not AMSAgentDescriptions. "+cce);
		}
	}
	
	/**
	 * searches with the default AMS
	 * @see #search(Agent,AID,AMSAgentDescription,SearchConstraints)
	 **/
	public static AMSAgentDescription[] search(Agent a, AMSAgentDescription amsd, SearchConstraints constraints) throws FIPAException {
		return search(a,a.getAMS(),amsd,constraints);
	}
	
	/**
	 * searches with the default AMS and the default SearchConstraints.
	 * The default constraints specified by FIPA are max_results and max_depth
	 * both unspecified and left to the choice of the responder AMS.
	 * @see #search(Agent,AID,AMSAgentDescription,SearchConstraints)
	 **/
	public static AMSAgentDescription[] search(Agent a, AMSAgentDescription amsd) throws FIPAException {
		SearchConstraints constraints = new SearchConstraints();
		return search(a,a.getAMS(),amsd,constraints);
	}
	
	/**
	 * searches with the passed AMS by using the default SearchConstraints.
	 * The default constraints specified by FIPA are max_results and max_depth
	 * both unspecified and left to the choice of the responder AMS.
	 * @see #search(Agent,AID,AMSAgentDescription,SearchConstraints)
	 **/
	public static AMSAgentDescription[] search(Agent a, AID AMSName, AMSAgentDescription amsd) throws FIPAException {
		SearchConstraints constraints = new SearchConstraints();
		return search(a,AMSName,amsd,constraints);
	}
	
	
	/**
	 In some cases it is more convenient to execute this tasks in a non-blocking way. 
	 This method returns a non-blocking behaviour that can be added to the queue of the agent behaviours, as usual, by using <code>Agent.addBehaviour()</code>.
	 <p>
	 Several ways are available to get the result of this behaviour and the programmer can select one according to his preferred programming style:
	 <ul>
	 <li>
	 call getLastMsg() and getSearchResults() where both throw a NotYetReadyException if the task has not yet finished;
	 <li>create a SequentialBehaviour composed of two sub-behaviours:  the first subbehaviour is the returned RequestFIPAServiceBehaviour, while the second one is application-dependent and is executed only when the first is terminated;
	 <li>use directly the class RequestFIPAServiceBehaviour by extending it and overriding all the handleXXX methods that handle the states of the fipa-request interaction protocol.
	 </ul>
	 * @param a is the agent performing the task
	 * @param AMSName is the AID that should perform the requested action
	 * @param actionName is the name of the action (one of the constants defined
	 * in FIPAManagementOntology: REGISTER / DEREGISTER / MODIFY / SEARCH).
	 * @param amsd is the agent description
	 * @param constraints are the search constraints (can be null if this is
	 * not a search operation)
	 * @return the behaviour to be added to the agent
	 @exception FIPAException A suitable exception can be thrown 
	 to indicate some error condition 
	 locally discovered (e.g.the amsdescription is not valid.)
	 @see jade.domain.FIPAAgentManagement.FIPAManagementOntology
	 **/
	public static RequestFIPAServiceBehaviour getNonBlockingBehaviour(Agent a, AID AMSName, String actionName, AMSAgentDescription amsd, SearchConstraints constraints) throws FIPAException {
		return new RequestFIPAServiceBehaviour(a,AMSName,actionName,amsd,constraints);
	}
	
	/**
	 the default AMS is used.
	 @see #getNonBlockingBehaviour(Agent a, AID AMSName, String actionName, AMSAgentDescription amsd, SearchConstraints constraints)
	 **/
	public static RequestFIPAServiceBehaviour getNonBlockingBehaviour(Agent a, String actionName, AMSAgentDescription amsd, SearchConstraints constraints) throws FIPAException {
		return getNonBlockingBehaviour(a,a.getAMS(),actionName,amsd,constraints);
	}
	
	/**
	 the default AMS is used.
	 the default SearchContraints are used.
	 a default AgentDescription is used, where only the agent AID is set.
	 @see #getNonBlockingBehaviour(Agent a, AID AMSName, String actionName, AMSAgentDescription amsd, SearchConstraints constraints)
	 * @see #search(Agent,AID,AMSAgentDescription)
	 **/
	public static RequestFIPAServiceBehaviour getNonBlockingBehaviour(Agent a, String actionName) throws FIPAException {
		AMSAgentDescription amsd = new AMSAgentDescription();
		amsd.setName(a.getAID());
		SearchConstraints constraints = new SearchConstraints();
		return getNonBlockingBehaviour(a,a.getAMS(),actionName,amsd,constraints);
	}
	
	/**
	 the default SearchContraints are used.
	 a default AgentDescription is used, where only the agent AID is set.
	 @see #getNonBlockingBehaviour(Agent a, AID AMSName, String actionName, AMSAgentDescription amsd, SearchConstraints constraints)
	 * @see #search(Agent,AID,AMSAgentDescription)
	 **/
	public static RequestFIPAServiceBehaviour getNonBlockingBehaviour(Agent a, AID amsName, String actionName) throws FIPAException {
		AMSAgentDescription amsd = new AMSAgentDescription();
		amsd.setName(a.getAID());
		SearchConstraints constraints = new SearchConstraints();
		return getNonBlockingBehaviour(a,amsName,actionName,amsd,constraints);
	}
	
	/**
	 the default AMS is used.
	 the default SearchContraints are used.
	 a default AgentDescription is used, where only the agent AID is set.
	 @see #getNonBlockingBehaviour(Agent a, AID AMSName, String actionName, AMSAgentDescription amsd, SearchConstraints constraints)
	 * @see #search(Agent,AID,AMSAgentDescription)
	 **/
	public static RequestFIPAServiceBehaviour getNonBlockingBehaviour(Agent a, String actionName, AMSAgentDescription amsd) throws FIPAException {
		SearchConstraints constraints = new SearchConstraints();
		return getNonBlockingBehaviour(a,a.getAMS(),actionName,amsd,constraints);
	}
	
	/**
	 the default AMS is used.
	 the default SearchContraints are used.
	 @see #getNonBlockingBehaviour(Agent a, AID AMSName, String actionName, AMSAgentDescription amsd, SearchConstraints constraints)
	 * @see #search(Agent,AID,AMSAgentDescription)
	 **/
	public static RequestFIPAServiceBehaviour getNonBlockingBehaviour(Agent a, AID amsName, String actionName, AMSAgentDescription amsd) throws FIPAException {
		SearchConstraints constraints = new SearchConstraints();
		return getNonBlockingBehaviour(a,amsName,actionName,amsd,constraints);
	}
	//#MIDP_EXCLUDE_END
	
	
	/**
	 Extracts the receiver a message could not be delivered to from
	 a FAILURE message received by the AMS.
	 @param a The agent that is calling this method.
	 @param failure The FAILURE message received by thye AMS.
	 @return the receiver a message could not be delivered to.
	 */
	public static AID getFailedReceiver(Agent a, ACLMessage failure) throws FIPAException {
		if (failure.getPerformative() != ACLMessage.FAILURE || !failure.getSender().equals(a.getAMS())) {
			throw new FIPAException("Invalid AMS FAILURE message");
		}
		try {
			String content = failure.getContent();
			int start = content.indexOf("MTS-error");
			start = content.indexOf(SL0Vocabulary.AID, start);
			SimpleSLTokenizer parser = new SimpleSLTokenizer(content.substring(start));
			return parseAID(parser);
		}
		catch (Exception e) {
			throw new FIPAException("Invalid content. "+e);
		}
	}

	public static String getFailureReason(Agent a, ACLMessage failure) throws FIPAException {
		if (failure.getPerformative() != ACLMessage.FAILURE || !failure.getSender().equals(a.getAMS())) {
			throw new FIPAException("Invalid AMS FAILURE message");
		}
		try {
			String content = failure.getContent();
			int start = content.indexOf("MTS-error");
			if (start < 0) {
				throw new FIPAException("Invalid AMS FAILURE message");
			}
			start = content.indexOf(ExceptionVocabulary.INTERNALERROR, start);
			start = content.indexOf('"', start);
			int end = content.indexOf('"', start+1);
			content = content.substring(start, end);

			if (content.startsWith(ACLMessage.AMS_FAILURE_AGENT_NOT_FOUND)) {
				content = ACLMessage.AMS_FAILURE_AGENT_NOT_FOUND;
			}
			else if (content.startsWith(ACLMessage.AMS_FAILURE_AGENT_UNREACHABLE)) {
				content = ACLMessage.AMS_FAILURE_AGENT_UNREACHABLE;
			}
			else if (content.startsWith(ACLMessage.AMS_FAILURE_SERVICE_ERROR)) {
				content = ACLMessage.AMS_FAILURE_SERVICE_ERROR;
			}
			else if (content.startsWith(ACLMessage.AMS_FAILURE_FOREIGN_AGENT_NO_ADDRESS)) {
				content = ACLMessage.AMS_FAILURE_FOREIGN_AGENT_NO_ADDRESS;
			}
			else if (content.startsWith(ACLMessage.AMS_FAILURE_AGENT_UNREACHABLE)) {
				content = ACLMessage.AMS_FAILURE_AGENT_UNREACHABLE;
			}
			else if (content.startsWith(ACLMessage.AMS_FAILURE_UNEXPECTED_ERROR)) {
				content = ACLMessage.AMS_FAILURE_UNEXPECTED_ERROR;
			}

			return content;
		} catch (Exception e) {
			throw new FIPAException("Invalid content. "+e);
		}
	}

	/**
	 Default constructor.
	 */
	public AMSService() {
	}
}

