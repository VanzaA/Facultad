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
import jade.core.behaviours.WakerBehaviour;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.ISO8601;

import jade.content.lang.sl.SimpleSLTokenizer;
import jade.content.lang.sl.SL0Vocabulary;

import java.util.Date;

/**
 * This class provides a set of static methods to communicate with
 * a DF Service that complies with FIPA specifications.
 * It includes methods to register, deregister, modify and search with a DF. 
 * Each of this method has version with all the needed parameters, or with a 
 * subset of them where, those parameters that can be omitted have been 
 * defaulted to the default DF of the platform, the AID of the sending agent,
 * the default Search Constraints.
 * <p>
 * Notice that all these methods block every activity of the agent until 
 * the action (i.e. register/deregister/modify/search) has been successfully 
 * executed or a jade.domain.FIPAException exception has been thrown 
 * (e.g. because a FAILURE message has been received from the DF). 
 * <p>
 * In some cases, instead, it is more convenient to execute these tasks in a 
 * non-blocking way. In these cases a <code>jade.proto.AchieveREInitiator</code>
 * or <code>jade.proto.SubscriptionInitiator</code> should be used in 
 * conjunction with the <code>createRequestMessage(), createSubscriptionMessage(), 
 * decodeDone(), decodeResult() and decodeNotification()</code> methods 
 * that facilitate the preparation and decoding of messages to be sent/received
 * to/from the DF. The following piece of code exemplifies that in the case 
 * of an agent subscribing to the default DF.
 
 <pr><hr><blockquote><pre>
 DFAgentDescription template = // fill the template
 Behaviour b = new SubscriptionInitiator(
 this, 
 DFService.createSubscriptionMessage(this, getDefaultDF(), template, null)) 
 {
 protected void handleInform(ACLMessage inform) {
 try {
 DFAgentDescription[] dfds = DFService.decodeNotification(inform.getContent());
 // do something
  }
  catch (FIPAException fe) {
  fe.printStackTrace();
  }
  }
  };
  addBehaviour(b);
  </pre></blockquote><hr>
  
  * @author Fabio Bellifemine (CSELT S.p.A.)
  * @author Elisabetta Cortese (TiLab S.p.A.)
  * @author Giovanni Caire (TiLab S.p.A.)
  @version $Date: 2012-03-02 14:18:37 +0100(ven, 02 mar 2012) $ $Revision: 6548 $ 
  * 
  **/
public class DFService extends FIPAService {
	public static final String DF_SEARCH_TIMEOUT_KEY = "jade_domain_dfservice_searchtimeout";
	public static final String DF_SEARCH_TIMEOUT_DEFAULT = "30000";
	
	private static final long OFFSET = 10000; // 10 sec
	private static final String SPACE_COLON = " :";
	private static final String SPACE_BRACKET = " (";
	
	/**
	 * check that the <code>DFAgentDescription</code> contains the mandatory
	 * slots, i.e. the agent name and, for each servicedescription, the
	 * service name and the service type
	 * @exception a MissingParameter exception is it is not valid
	 */
	static void checkIsValid(DFAgentDescription dfd, boolean checkServices) throws MissingParameter {
		try {
			if (dfd.getName().getName().length() == 0) {
				throw new MissingParameter(FIPAManagementVocabulary.DFAGENTDESCRIPTION, FIPAManagementVocabulary.DFAGENTDESCRIPTION_NAME);
			}
		}
		catch (NullPointerException npe) {
			throw new MissingParameter(FIPAManagementVocabulary.DFAGENTDESCRIPTION, FIPAManagementVocabulary.DFAGENTDESCRIPTION_NAME);
		}
		
		if (checkServices) {
			Iterator i = dfd.getAllServices();
			ServiceDescription sd;
			while (i.hasNext()) {
				sd = (ServiceDescription)i.next();
				if (sd.getName() == null)
					throw new MissingParameter(FIPAManagementVocabulary.SERVICEDESCRIPTION, FIPAManagementVocabulary.SERVICEDESCRIPTION_NAME);
				if (sd.getType() == null)
					throw new MissingParameter(FIPAManagementVocabulary.SERVICEDESCRIPTION, FIPAManagementVocabulary.SERVICEDESCRIPTION_TYPE);
			}
		}
	}
	
	
	/**
	 Register a new DF-Description with a <b>DF</b> agent.
	 <p> 
	 It should be noted that, depending on the policy adopted by 
	 the DF, the granted lease time for the registration can be 
	 shorter than the requested one. This can be checked by looking 
	 at the DF-Description actually registered by the DF that is made 
	 available as the return value of this method. 
	 The <code>keepRegistered()</code> method can be used to keep
	 the registration valid until a given time.
	 @param a is the Agent performing the registration (it is needed in order
	 to send/receive messages)
	 @param dfName The AID of the <b>DF</b> agent to register with.
	 @param dfd A <code>DFAgentDescription</code> object containing all
	 data necessary to the registration. If the Agent name is empty, than
	 it is set according to the <code>a</code> parameter.
	 @return the <code>DFAgentDescription</code> actually registered 
	 by the DF
	 @exception FIPAException If a <code>REFUSE</code>, 
	 <code>FAILURE</code> or <code>NOT_UNDERSTOOD</code>
	 message is received from the DF (to indicate some error condition) 
	 or if the supplied DF-Description is not valid.
	 @see #keepRegistered(Agent, AID, DFAgentDescription, Date)
	 */
	public static DFAgentDescription register(Agent a, AID dfName, DFAgentDescription dfd) throws FIPAException {
		if (dfd == null) {
			dfd = new DFAgentDescription();
		}
		if (dfd.getName() == null) {
			dfd.setName(a.getAID());
		}
		checkIsValid(dfd, true);
		
		ACLMessage request = createRequestMessage(a, dfName, FIPAManagementVocabulary.REGISTER, dfd, null);
		ACLMessage reply = doFipaRequestClient(a,request);
		if (reply == null) {
			throw new FIPAException("Missing reply");
		}
		return decodeDone(reply.getContent());
	}
	
	
	/**
	 * Registers a <code>DFAgentDescription</code> with the default DF
	 @exception FIPAException  
	 * @see #register(Agent,AID,DFAgentDescription)
	 **/
	public static DFAgentDescription register(Agent a, DFAgentDescription dfd) throws FIPAException {
		return register(a,a.getDefaultDF(),dfd);
	}
	
	/**
	 Deregister a DFAgentDescription from a <b>DF</b> agent. 
	 @param dfName The AID of the <b>DF</b> agent to deregister from.
	 @param dfd A <code>DFAgentDescription</code> object containing all
	 data necessary to the deregistration.
	 @exception FIPAException If a <code>REFUSE</code>, 
	 <code>FAILURE</code> or <code>NOT_UNDERSTOOD</code>
	 message is received from the DF (to indicate some error condition) 
	 or if the supplied DF-Description is not valid.
	 */
	public static void deregister(Agent a, AID dfName, DFAgentDescription dfd) throws FIPAException {
		if (dfd == null) {
			dfd = new DFAgentDescription();
		}
		if (dfd.getName() == null) {
			dfd.setName(a.getAID());
		}
		
		ACLMessage request = createRequestMessage(a, dfName, FIPAManagementVocabulary.DEREGISTER, dfd, null);
		ACLMessage reply = doFipaRequestClient(a,request);
		if (reply == null) {
			throw new FIPAException("Missing reply");
		}
	}
	
	/**
	 * Deregisters a <code>DFAgentDescription</code> from the default DF
	 @exception FIPAException  
	 @see #deregister(Agent a, AID dfName, DFAgentDescription dfd) 
	 **/
	public static void deregister(Agent a, DFAgentDescription dfd) throws FIPAException {
		deregister(a,a.getDefaultDF(),dfd);
	}
	
	/**
	 * A default Agent Description is used which contains only the AID
	 * of this agent.
	 @exception FIPAException  
	 @see #deregister(Agent a, AID dfName, DFAgentDescription dfd) 
	 **/
	public static void deregister(Agent a, AID dfName) throws FIPAException {
		deregister(a,dfName,null);
	}
	
	/**
	 * Deregisters a <code>DFAgentDescription</code> from the default DF.
	 * A default DF-Description is used which contains only the AID
	 * of this agent.
	 @exception FIPAException  
	 @see #deregister(Agent a, AID dfName, DFAgentDescription dfd) 
	 **/
	public static void deregister(Agent a) throws FIPAException {
		deregister(a,a.getDefaultDF());
	}
	
	
	/**
	 Modifies a previously registered DF-Description within a <b>DF</b>
	 agent. 
	 <p>
	 It should be noted that, depending on the policy adopted by 
	 the DF, the granted lease time for the modified registration can be 
	 shorter than the requested one. This can be checked by looking 
	 at the DF-Description actually registered by the DF that is made 
	 available as the return value of this method.
	 The <code>keepRegistered()</code> method can be used to keep
	 the registration valid until a given time.
	 @param a is the Agent performing the request of modification 
	 @param dfName The AID of the <b>DF</b> agent holding the data
	 to be changed.
	 @param dfd A <code>DFAgentDescription</code> object containing all
	 new data values; 
	 @return the <code>DFAgentDescription</code> actually registered 
	 (after the modification) by the DF
	 @exception FIPAException If a <code>REFUSE</code>, 
	 <code>FAILURE</code> or <code>NOT_UNDERSTOOD</code>
	 message is received from the DF (to indicate some error condition) 
	 or if the supplied DF-Description is not valid.
	 @see #keepRegistered(Agent, AID, DFAgentDescription, Date)
	 */
	public static DFAgentDescription modify(Agent a, AID dfName, DFAgentDescription dfd) throws FIPAException {
		if (dfd == null) {
			dfd = new DFAgentDescription();
		}
		if (dfd.getName() == null) {
			dfd.setName(a.getAID());
		}      
		checkIsValid(dfd, true);
		
		ACLMessage request = createRequestMessage(a, dfName, FIPAManagementVocabulary.MODIFY, dfd, null);
		ACLMessage reply = doFipaRequestClient(a,request);
		if (reply == null) {
			throw new FIPAException("Missing reply");
		}
		
		return decodeDone(reply.getContent());
	}
	
	/**
	 * Modify a <code>DFAgentDescription</code> from the default DF.
	 @exception FIPAException  
	 @see #modify(Agent a, AID dfName, DFAgentDescription dfd)
	 **/
	public static DFAgentDescription modify(Agent a, DFAgentDescription dfd) throws FIPAException {
		return modify(a,a.getDefaultDF(),dfd);
	}
	
	/**
	 Add a suitable behaviour that ensures that a DF-Description currently
	 registered with a DF is kept registered until a given deadline. 
	 This method is particularly useful when dealing with a DF agent 
	 that grants limited lease time for agent registrations.
	 The following piece of code exemplifies how to use it.
	 
	 <pr><hr><blockquote><pre>
	 DFAgentDescription dfd = // fill DF-Description
	 try {
	 DFAgentDescription actualDfd = DFService.register(this, dfd);
	 DFService.keepRegistered(this, actualDfd, dfd.getLeaseTime());
	 }
	 catch (FIPAException fe) {
	 fe.printStackTarce();
	 }
	 </pre></blockquote><hr>
	 
	 @param a The agent that is registerd with the DF.
	 @param df The DF agent
	 @param dfd The DF-Description that is currently registered with the DF
	 @param deadline The time until which the currenlty registered 
	 DF-Description must be kept valid. Use null to indicate an infinite 
	 time
	 */
	public static void keepRegistered(Agent a, AID df, final DFAgentDescription dfd, Date deadline) {
		Date lease = dfd.getLeaseTime();
		final AID theDF = (df != null ? df : a.getDefaultDF());
		if (lease != null) {
			if (deadline == null || lease.getTime() < deadline.getTime()) {
				dfd.setLeaseTime(deadline);
				a.addBehaviour(new WakerBehaviour(a, new Date(lease.getTime() - OFFSET)) {
					protected void onWake() {
						try {
							DFAgentDescription newDfd = modify(myAgent, theDF, dfd);
							keepRegistered(myAgent, theDF, newDfd, dfd.getLeaseTime());
						}
						catch (FIPAException fe) {
							// There is nothing we can do
							fe.printStackTrace();
						}
					}
				} );
			}
		}
	}
	
	// constant used to set max results of SearchConstraints
	private static Long MINUSONE = new Long(-1);
	/**
	 Searches for data contained within a <b>DF</b> agent. 
	 @param a is the Agent requesting the search 
	 @param dfName The AID of the <b>DF</b> agent to start search from.
	 @param dfd A <code>DFAgentDescription</code> object containing
	 data to search for; this parameter is used as a template to match
	 data against.
	 @param constraints of the search 
	 @return An array of <code>DFAgentDescription</code> 
	 containing all found
	 items matching the given
	 descriptor, subject to given search constraints for search depth
	 and result size.
	 @exception FIPAException If a <code>REFUSE</code>, 
	 <code>FAILURE</code> or <code>NOT_UNDERSTOOD</code>
	 message is received from the DF (to indicate some error condition) 
	 */
	public static DFAgentDescription[] search(Agent a, AID dfName, DFAgentDescription dfd, SearchConstraints constraints) throws FIPAException {
		if (dfName == null) {
			dfName = a.getDefaultDF();
		}
		if (dfd == null) {
			dfd = new DFAgentDescription();
		}
		if (constraints == null) {
			constraints = new SearchConstraints();
			constraints.setMaxResults(MINUSONE);
		}
		
		ACLMessage request = createRequestMessage(a, dfName, FIPAManagementVocabulary.SEARCH, dfd, constraints);
		
		int timeout = 0;
		try {
			timeout = Integer.parseInt(a.getProperty(DF_SEARCH_TIMEOUT_KEY, DF_SEARCH_TIMEOUT_DEFAULT));
		} catch (Exception e) {
			throw new FIPAException("Property "+DF_SEARCH_TIMEOUT_KEY+" is not a valid value for integer parameter");
		}

		ACLMessage inform = doFipaRequestClient(a, request, timeout);
		if (inform == null) {
			throw new FIPAException("Timeout searching for data into df");
		}
		
		return decodeResult(inform.getContent());
	}
	
	/**
	 * The default DF is used.
	 @exception FIPAException  
	 @see #search(Agent a, AID dfName, DFAgentDescription dfd, SearchConstraints constraints) 
	 **/
	public static DFAgentDescription[] search(Agent a, DFAgentDescription dfd, SearchConstraints constraints) throws FIPAException {
		return search(a,null,dfd,constraints);
	}
	
	/**
	 * The default DF is used.
	 * The default SearchConstraints are used. According to FIPA they are
	 * defaulted to null value for all slots.
	 @exception FIPAException  
	 @see #search(Agent a, AID dfName, DFAgentDescription dfd, SearchConstraints constraints) 
	 **/
	public static DFAgentDescription[] search(Agent a, DFAgentDescription dfd) throws FIPAException {
		return search(a,null,dfd,null);
	}
	
	/**
	 * The default SearchConstraints are used. According to FIPA they are
	 * defaulted to null value for all slots.
	 @exception FIPAException  
	 @see #search(Agent a, AID dfName, DFAgentDescription dfd, SearchConstraints constraints) 
	 **/
	public static DFAgentDescription[] search(Agent a, AID dfName, DFAgentDescription dfd) throws FIPAException {
		return search(a,dfName,dfd,null);
	}
	
	/** 
	 Searches the DF and remains blocked until a result is found or the
	 specified timeout has expired.
	 @param a The agent that is performing the search
	 @param dfName The AID of the <b>DF</b> agent where to search into.
	 @param template A <code>DFAgentDescription</code> object that is used 
	 as a template to identify the DF descriptions to search for.  
	 @param constraints The constraints to limit the number of results to be
	 sent back.
	 @param timeout The maximum amount of time that we want to remain blocked 
	 waiting for results.
	 @return The DF agent descriptions matching the specified template or 
	 <code>null</code> if the timeout expires.
	 @exception FIPAException If a <code>REFUSE</code>, 
	 <code>FAILURE</code> or <code>NOT_UNDERSTOOD</code>
	 message is received from the DF (to indicate some error condition) 
	 or if the supplied DF-Description template is not valid.
	 */
	public static DFAgentDescription[] searchUntilFound(Agent a, AID dfName, DFAgentDescription dfd, SearchConstraints constraints, long timeout) throws FIPAException {
		
		ACLMessage subscribe = createSubscriptionMessage(a, dfName, dfd, constraints);
		
		ACLMessage inform = doFipaRequestClient(a, subscribe, timeout);
		
		// Send the CANCEL message
		ACLMessage cancel = createCancelMessage(a, dfName, subscribe);
		a.send(cancel);
		
		DFAgentDescription[] result = null;
		if (inform != null) {
			result = decodeNotification(inform.getContent());
		}
		return result;
	}
	
	///////////////////////////////////
	// Message preparation methods
	///////////////////////////////////
	
	/** 
	 Utility method that creates a suitable message to be used 
	 to REQUEST a DF agent to perform a given action of the 
	 FIPA-Management-ontology.
	 <p>
	 This method can be fruitfully used in combination with 
	 the <code>jade.proto.AchieveREInitiator</code> protocol and with
	 the <code>decodeDone()</code> and <code>decodeResult()</code> methods
	 to interact with a DF in a non-blocking way.
	 @param a The agent that is requesting the DF
	 @param dfName The AID of the <b>DF</b> agent to send the request to.
	 @param action The name of the requested action. This must be one of
	 <ul>
	 <li><code>FIPAManagementVocabulary.REGISTER</code></li>
	 <li><code>FIPAManagementVocabulary.DEREGISTER</code></li>
	 <li><code>FIPAManagementVocabulary.MODIFY</code></li>
	 <li><code>FIPAManagementVocabulary.SEARCH</code></li>
	 </ul>
	 @param dfd A <code>DFAgentDescription</code> object. Depending on the
	 requested action, this is the description to register/deregister/modify  
	 or a template to match data against during a search.
	 @param constraints The constraints to limit the number of results to be
	 notified. This is meaningful only if the requested action is SEARCH.
	 @return the request message.
	 @see jade.proto.AchieveREInitiator
	 @see #decodeDone(String)
	 @see #decodeResult(String)
	 */
	public static ACLMessage createRequestMessage(Agent a, AID dfName, String action, DFAgentDescription dfd, SearchConstraints constraints) {
		ACLMessage request = createRequestMessage(a, dfName);
		request.setContent(encodeAction(dfName, action, dfd, constraints));
		return request;
	}
	
	/** 
	 Utility method that creates a suitable message to be used 
	 to SUBSCRIBE to a DF agent in order to receive notifications when a new 
	 DF-Description matching the indicated template is registererd
	 with that DF. 
	 <p>
	 This method can be fruitfully used in combination with 
	 the <code>jade.proto.SubscriptionInitiator</code> protocol and with
	 the <code>createCancelMessage()</code> and <code>decodeNotification()</code>
	 methods to interact with a DF in a non-blocking way.
	 @param a The agent that is subscribing to the DF
	 @param dfName The AID of the <b>DF</b> agent to subscribe to.
	 @param template A <code>DFAgentDescription</code> object that is used 
	 as a template to identify DF description that will be notified
	 @param constraints The constraints to limit the number of results to be
	 notified.
	 @return the subscription message.
	 @see jade.proto.SubscriptionInitiator
	 @see #createCancelMessage(Agent, AID, ACLMessage)
	 @see #decodeNotification(String)
	 */
	public static ACLMessage createSubscriptionMessage(Agent a, AID dfName, DFAgentDescription template, SearchConstraints constraints) {
		ACLMessage subscribe = createRequestMessage(a, dfName);
		subscribe.setPerformative(ACLMessage.SUBSCRIBE);
		subscribe.setProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE);
		// Note that iota is not included in SL0
		subscribe.setLanguage(FIPANames.ContentLanguage.FIPA_SL);	
		if (constraints == null) {
			constraints = new SearchConstraints();
			constraints.setMaxResults(MINUSONE);
		}		
		subscribe.setContent(encodeIota(dfName, template, constraints));
		return subscribe;
	}
	
	/**
	 @deprecated Use <code>createSubscriptionMessage()</code> instead
	 */  
	public static ACLMessage getSubscriptionMessage(Agent a, AID dfName, DFAgentDescription template, SearchConstraints constraints) throws FIPAException {
		return createSubscriptionMessage(a, dfName, template, constraints);
	}
	
	/** 
	 Utility method that creates a suitable message to be used 
	 to CANCEL a subscription to a DF agent. 
	 @param a The agent that wants to cancel its subscription to the DF
	 @param dfName The AID of the <b>DF</b> agent.
	 @param subscribe The subscription message previously sent to the DF 
	 @return the cancel message.
	 @see jade.proto.SubscriptionInitiator
	 @see #createSubscriptionMessage(Agent, AID, DFAgentDescription, SearchConstraints)
	 @see #decodeNotification(String)
	 */
	public static ACLMessage createCancelMessage(Agent a, AID dfName, ACLMessage subscribe) {
		ACLMessage cancel = new ACLMessage(ACLMessage.CANCEL);
		cancel.addReceiver(dfName);
		cancel.setLanguage(subscribe.getLanguage());
		cancel.setOntology(subscribe.getOntology());
		cancel.setProtocol(subscribe.getProtocol());
		cancel.setConversationId(subscribe.getConversationId());
		cancel.setContent(encodeCancel(dfName, subscribe));
		return cancel;
	}
	
	///////////////////////////////////
	// Decoding methods
	///////////////////////////////////
	
	/**
	 Process the content of the final <code>inform (Done)</code> message
	 resulting from a <code>register</code> or <code>deregister</code>
	 action requested to a DF agent, extracting the
	 <code>df-agent-description</code> contained within.
	 @return The <code>DFAgentDescription</code> object included
	 in the "done" expression used as the content of the INFORM message
	 send back by the DF in response to a REQUEST to perform a register,
	 deregister or modify action.
	 @exception FIPAException If some error occurs while decoding
	 */
	public static DFAgentDescription decodeDone(String s) throws FIPAException {
		// S has the form: 
		// ((done (action (AID...) (register (df-agent-description ....) ) ) ) )
		// We skip until we find "df-agent-description" and start decoding from there.
		try {
			int start = s.indexOf(FIPAManagementVocabulary.DFAGENTDESCRIPTION);
			return parseDfd(new SimpleSLTokenizer(s.substring(start)));
		}
		catch (Exception e) {
			throw new FIPAException("Error decoding INFORM Done. "+e.getMessage());
		}
	}
	
	/**
	 Process the content of the final <code>inform (result)</code> message resulting
	 from a <code>search</code> action requested to a DF agent, extracting the array of
	 <code>df-agent-description</code> contained within.
	 @return The <code>DFAgentDescription</code> objects (as an array) included
	 in the "result" expression used as the content of the INFORM message
	 send back by the DF in response to a REQUEST to perform a search action.
	 @exception FIPAException If some error occurs while decoding
	 */
	public static DFAgentDescription[] decodeResult(String s) throws FIPAException {
		// S has the form: 
		// ((result (action...)  (sequence (DFD...) (DFD...)) ) )
		// We skip until we find "action", skip until the end of (action...) and start decoding from there.
		try {
			int start = s.indexOf(SL0Vocabulary.ACTION);
			start += countUntilEnclosing(s, start);
			return decodeDfdSequence(s.substring(start));
		}
		catch (Exception e) {
			throw new FIPAException("Error decoding INFORM Result. "+e.getMessage());
		}	  	
	}
	
	/**
	 Process the content of the <code>inform</code> message resulting
	 from a subscription with a DF agent, extracting the array of
	 <code>df-agent-description</code> objects contained within.
	 @return The <code>DFAgentDescription</code> objects (as an array) included
	 in the "(= (iota...) ...)" expression used as the content of an INFORM message
	 sent back by the DF as a subscription notification.
	 @exception FIPAException If some error occurs while decoding
	 */
	public static DFAgentDescription[] decodeNotification(String s) throws FIPAException {
		// S has the form:
		// ((= (iota...)  (sequence (DFD...) (DFD...)) ) )
		// We skip until we find "iota", skip until the end of (iota...) and start decoding from there.
		try {
			int start = s.indexOf("iota");
			start += countUntilEnclosing(s, start);
			return decodeDfdSequence(s.substring(start));
		}
		catch (Exception e) {
			throw new FIPAException("Error decoding INFORM Equals. "+e.getMessage());
		}
	}
	
	/**
	 The parser content has the form:
	 df-agent-description ......) <possibly something else>
	 */
	private static DFAgentDescription parseDfd(SimpleSLTokenizer parser) throws Exception {
		DFAgentDescription dfd = new DFAgentDescription();
		// Skip "df-agent-description"
		parser.getElement();
		while (parser.nextToken().startsWith(":")) {
			String slotName = parser.getElement();
			// Name
			if (slotName.equals(FIPAManagementVocabulary.DFAGENTDESCRIPTION_NAME)) {
				parser.consumeChar('(');
				dfd.setName(parseAID(parser));
			}
			// Lease time
			else if (slotName.equals(FIPAManagementVocabulary.DFAGENTDESCRIPTION_LEASE_TIME)) {
				dfd.setLeaseTime(ISO8601.toDate(parser.getElement()));
			}
			// Protocols
			else if (slotName.equals(FIPAManagementVocabulary.DFAGENTDESCRIPTION_PROTOCOLS)) {
				Iterator it = parseAggregate(parser).iterator();
				while (it.hasNext()) {
					dfd.addProtocols((String) it.next());
				}
			}
			// Languages
			else if (slotName.equals(FIPAManagementVocabulary.DFAGENTDESCRIPTION_LANGUAGES)) {
				Iterator it = parseAggregate(parser).iterator();
				while (it.hasNext()) {
					dfd.addLanguages((String) it.next());
				}
			}
			// Ontologies
			else if (slotName.equals(FIPAManagementVocabulary.DFAGENTDESCRIPTION_ONTOLOGIES)) {
				Iterator it = parseAggregate(parser).iterator();
				while (it.hasNext()) {
					dfd.addOntologies((String) it.next());
				}
			}
			// Services
			else if (slotName.equals(FIPAManagementVocabulary.DFAGENTDESCRIPTION_SERVICES)) {
				Iterator it = parseAggregate(parser).iterator();
				while (it.hasNext()) {
					dfd.addServices((ServiceDescription) it.next());
				}
			}
		}
		parser.consumeChar(')');
		return dfd;
	}
	
	
	/**
	 The parser content has the form:
	 service-description ......) <possibly something else>
	 */
	private static ServiceDescription parseServiceDescription(SimpleSLTokenizer parser) throws Exception {
		ServiceDescription sd = new ServiceDescription();
		// Skip "service-description"
		parser.getElement();
		while (parser.nextToken().startsWith(":")) {
			String slotName = parser.getElement();
			// Name
			if (slotName.equals(FIPAManagementVocabulary.SERVICEDESCRIPTION_NAME)) {
				sd.setName(parser.getElement());
			}
			// Type
			else if (slotName.equals(FIPAManagementVocabulary.SERVICEDESCRIPTION_TYPE)) {
				sd.setType(parser.getElement());
			}
			// Ownership
			else if (slotName.equals(FIPAManagementVocabulary.SERVICEDESCRIPTION_OWNERSHIP)) {
				sd.setOwnership(parser.getElement());
			}
			// Protocols
			else if (slotName.equals(FIPAManagementVocabulary.SERVICEDESCRIPTION_PROTOCOLS)) {
				Iterator it = parseAggregate(parser).iterator();
				while (it.hasNext()) {
					sd.addProtocols((String) it.next());
				}
			}
			// Languages
			else if (slotName.equals(FIPAManagementVocabulary.SERVICEDESCRIPTION_LANGUAGES)) {
				Iterator it = parseAggregate(parser).iterator();
				while (it.hasNext()) {
					sd.addLanguages((String) it.next());
				}
			}
			// Ontologies
			else if (slotName.equals(FIPAManagementVocabulary.SERVICEDESCRIPTION_ONTOLOGIES)) {
				Iterator it = parseAggregate(parser).iterator();
				while (it.hasNext()) {
					sd.addOntologies((String) it.next());
				}
			}
			// Properties
			else if (slotName.equals(FIPAManagementVocabulary.SERVICEDESCRIPTION_PROPERTIES)) {
				Iterator it = parseAggregate(parser).iterator();
				while (it.hasNext()) {
					sd.addProperties((Property) it.next());
				}
			}
		}
		parser.consumeChar(')');
		return sd;
	}
	
	/**
	 The parser content has the form:
	 property ......) <possibly something else>
	 */
	private static Property parseProperty(SimpleSLTokenizer parser) throws Exception {
		Property p = new Property();
		// Skip "property"
		parser.getElement();
		while (parser.nextToken().startsWith(":")) {
			String slotName = parser.getElement();
			// Name
			if (slotName.equals(FIPAManagementVocabulary.PROPERTY_NAME)) {
				p.setName(parser.getElement());
			}
			// Name
			if (slotName.equals(FIPAManagementVocabulary.PROPERTY_VALUE)) {
				p.setValue(parser.getElement());
			}
		}
		parser.consumeChar(')');
		return p;
	}
	
	
	/**
	 The parser content has the form:
	 agent-identifier ......) <possibly something else>
	 */
	public static AID parseAID(SimpleSLTokenizer parser) throws Exception {
		AID id = new AID("", AID.ISGUID); // Dummy temporary name
		// Skip "agent-identifier"
		parser.getElement();
		while (parser.nextToken().startsWith(":")) {
			String slotName = parser.getElement();
			// Name
			if (slotName.equals(SL0Vocabulary.AID_NAME)) {
				id.setName(parser.getElement());
			}
			// Addresses
			else if (slotName.equals(SL0Vocabulary.AID_ADDRESSES)) {
				Iterator it = parseAggregate(parser).iterator();
				while (it.hasNext()) {
					id.addAddresses((String) it.next());
				}
			}
			// Resolvers
			else if (slotName.equals(SL0Vocabulary.AID_RESOLVERS)) {
				Iterator it = parseAggregate(parser).iterator();
				while (it.hasNext()) {
					id.addResolvers((AID) it.next());
				}
			}
		}
		parser.consumeChar(')');
		return id;
	}

	/**
	 The parser content has the form:
	 (sequence <val> <val> ......) <possibly something else>
	 or 
	 (set <val> <val> ......) <possibly something else>
	 */
	private static List parseAggregate(SimpleSLTokenizer parser) throws Exception {
		List l = new ArrayList();
		// Skip first (
		parser.consumeChar('(');
		// Skip "sequence" or "set" (no matter)
		parser.getElement();
		String next = parser.nextToken();
		while (!next.startsWith(")")) {
			if (!next.startsWith("(")) {
				l.add(parser.getElement());
			}
			else {
				parser.consumeChar('(');
				next = parser.nextToken();
				if (next.equals(FIPAManagementVocabulary.DFAGENTDESCRIPTION)) {
					l.add(parseDfd(parser));
				}
				if (next.equals(SL0Vocabulary.AID)) {
					l.add(parseAID(parser));
				}
				else if (next.equals(FIPAManagementVocabulary.SERVICEDESCRIPTION)) {
					l.add(parseServiceDescription(parser));
				}
				else if (next.equals(FIPAManagementVocabulary.PROPERTY)) {
					l.add(parseProperty(parser));
				}
			}
			next = parser.nextToken();
		}
		parser.consumeChar(')');
		return l;
	}
	
	/**
	 S has the form:
	 (sequence (DFD...) (DFD...)) <possibly something else>
	 */
	private static DFAgentDescription[] decodeDfdSequence(String s) throws Exception {
		List l = parseAggregate(new SimpleSLTokenizer(s));
		// Convert the list into an array
		DFAgentDescription[] items = new DFAgentDescription[l.size()];
		for(int i = 0; i < l.size(); i++){
			items[i] = (DFAgentDescription)l.get(i);
		}
		return items;
	}
	
	/**
	 Start indicates the index of the first char after the open parenthesis
	 */
	private static int countUntilEnclosing(String s, int start) {
		int openCnt = 1;
		boolean skipMode = false;
		int cnt = start;
		while (openCnt > 0) {
			char c = s.charAt(cnt++);
			if (!skipMode) {
				if (c == '(') {
					openCnt++;
				}
				else if (c == ')') {
					openCnt--;
				}
				else if (c == '"') {
					skipMode = true;
				}
			}
			else {
				if (c == '\\' && s.charAt(cnt) == '\"') {
					cnt++;
				}
				else if (c == '"') {
					skipMode = false;
				}
			}
		}
		return cnt-start;
	}
	
	///////////////////////////////////
	// Encoding methods
	///////////////////////////////////
	
	/**
	 This is package scoped as it is used by DFUpdateBehaviour and 
	 DFSearchBehaviour.
	 */
	static String encodeAction(AID df, String actionName, DFAgentDescription dfd, SearchConstraints sc) {
		StringBuffer sb = new StringBuffer("((");
		sb.append(SL0Vocabulary.ACTION);
		sb.append(' ');
		sb.append(df.toString());
		sb.append(SPACE_BRACKET);
		sb.append(actionName);
		sb.append(' ');
		encodeDfd(sb, dfd);
		if (actionName.equals(FIPAManagementVocabulary.SEARCH) && sc == null) {
			sc = new SearchConstraints();
			sc.setMaxResults(MINUSONE);
		}
		if (sc != null) {
			sb.append(SPACE_BRACKET);
			sb.append(FIPAManagementVocabulary.SEARCHCONSTRAINTS);
			encodeField(sb, sc.getMaxResults(), FIPAManagementVocabulary.SEARCHCONSTRAINTS_MAX_RESULTS);
			encodeField(sb, sc.getMaxDepth(), FIPAManagementVocabulary.SEARCHCONSTRAINTS_MAX_DEPTH);
			encodeField(sb, sc.getSearchId(), FIPAManagementVocabulary.SEARCHCONSTRAINTS_SEARCH_ID);
			sb.append(')');
		}
		sb.append(")))"); // Close <actionName>, action and content
		return sb.toString();
	}
	
	/**
	 This is package scoped as it is used by DFSearchBehaviour 
	 */
	static String encodeIota(AID df, DFAgentDescription dfd, SearchConstraints sc) {
		StringBuffer sb = new StringBuffer("((iota ?x (");
		sb.append(SL0Vocabulary.RESULT);
		sb.append(' ');
		String tmp = encodeAction(df, FIPAManagementVocabulary.SEARCH, dfd, sc);
		sb.append(tmp.substring(1, tmp.length()-1));
		sb.append(" ?x)))"); // Close Result, iota and content
		return sb.toString();
	}
	
	/**
	 This is package scoped as it is used by DFSearchBehaviour 
	 */
	static String encodeCancel(AID df, ACLMessage msg) {
		StringBuffer sb = new StringBuffer("((");
		sb.append(SL0Vocabulary.ACTION);
		sb.append(' ');
		sb.append(df.toString());
		sb.append(SPACE_BRACKET);
		sb.append(ACLMessage.getPerformative(msg.getPerformative()));
		encodeField(sb, msg.getSender(), SL0Vocabulary.ACLMSG_SENDER);
		encodeAggregate(sb, msg.getAllReceiver(), SL0Vocabulary.SEQUENCE, SL0Vocabulary.ACLMSG_RECEIVERS);
		encodeField(sb, msg.getProtocol(), SL0Vocabulary.ACLMSG_PROTOCOL);
		encodeField(sb, msg.getLanguage(), SL0Vocabulary.ACLMSG_LANGUAGE);
		encodeField(sb, msg.getOntology(), SL0Vocabulary.ACLMSG_ONTOLOGY);
		encodeField(sb, msg.getReplyWith(), SL0Vocabulary.ACLMSG_REPLY_WITH);
		encodeField(sb, msg.getConversationId(), SL0Vocabulary.ACLMSG_CONVERSATION_ID);
		encodeField(sb, msg.getContent(), SL0Vocabulary.ACLMSG_CONTENT);
		sb.append(")))"); // Close msg, action and content
		return sb.toString();
	}
	
	private static void encodeDfd(StringBuffer sb, DFAgentDescription dfd) {
		sb.append('(');
		sb.append(FIPAManagementVocabulary.DFAGENTDESCRIPTION);
		encodeField(sb, dfd.getName(), FIPAManagementVocabulary.DFAGENTDESCRIPTION_NAME);
		encodeAggregate(sb, dfd.getAllProtocols(), SL0Vocabulary.SET, FIPAManagementVocabulary.DFAGENTDESCRIPTION_PROTOCOLS);
		encodeAggregate(sb, dfd.getAllLanguages(), SL0Vocabulary.SET, FIPAManagementVocabulary.DFAGENTDESCRIPTION_LANGUAGES);
		encodeAggregate(sb, dfd.getAllOntologies(), SL0Vocabulary.SET, FIPAManagementVocabulary.DFAGENTDESCRIPTION_ONTOLOGIES);
		encodeAggregate(sb, dfd.getAllServices(), SL0Vocabulary.SET, FIPAManagementVocabulary.DFAGENTDESCRIPTION_SERVICES);
		Date lease = dfd.getLeaseTime();
		if (lease != null) {
			sb.append(SPACE_COLON);
			sb.append(FIPAManagementVocabulary.DFAGENTDESCRIPTION_LEASE_TIME);
			sb.append(' ');
			sb.append(ISO8601.toString(lease));
		}
		sb.append(')');
	}
	
	private static void encodeServiceDescription(StringBuffer sb, ServiceDescription sd) {
		sb.append('(');
		sb.append(FIPAManagementVocabulary.SERVICEDESCRIPTION);
		encodeField(sb, sd.getName(), FIPAManagementVocabulary.SERVICEDESCRIPTION_NAME);
		encodeField(sb, sd.getType(), FIPAManagementVocabulary.SERVICEDESCRIPTION_TYPE);
		encodeField(sb, sd.getOwnership(), FIPAManagementVocabulary.SERVICEDESCRIPTION_OWNERSHIP);
		encodeAggregate(sb, sd.getAllProtocols(), SL0Vocabulary.SET, FIPAManagementVocabulary.SERVICEDESCRIPTION_PROTOCOLS);
		encodeAggregate(sb, sd.getAllLanguages(), SL0Vocabulary.SET, FIPAManagementVocabulary.SERVICEDESCRIPTION_LANGUAGES);
		encodeAggregate(sb, sd.getAllOntologies(), SL0Vocabulary.SET, FIPAManagementVocabulary.SERVICEDESCRIPTION_ONTOLOGIES);
		encodeAggregate(sb, sd.getAllProperties(), SL0Vocabulary.SET, FIPAManagementVocabulary.SERVICEDESCRIPTION_PROPERTIES);
		sb.append(')');
	}
	
	private static void encodeProperty(StringBuffer sb, Property p) {
		sb.append('(');
		sb.append(FIPAManagementVocabulary.PROPERTY);
		encodeField(sb, p.getName(), FIPAManagementVocabulary.PROPERTY_NAME);
		encodeField(sb, p.getValue(), FIPAManagementVocabulary.PROPERTY_VALUE);
		sb.append(')');
	}
	
	private static void encodeField(StringBuffer sb, Object val, String name) {
		if (val != null) {
			sb.append(SPACE_COLON);
			sb.append(name);
			sb.append(' ');
			if (val instanceof String) {
				encodeString(sb, (String) val);
			}
			else {
				sb.append(val);
			}
		}
	}
	
	private static void encodeAggregate(StringBuffer sb, Iterator agg, String aggType, String name) {
		if (agg != null && agg.hasNext()) {
			sb.append(SPACE_COLON);
			sb.append(name);
			sb.append(SPACE_BRACKET);
			sb.append(aggType);
			while (agg.hasNext()) {
				sb.append(' ');
				Object val = agg.next();
				if (val instanceof ServiceDescription) {
					encodeServiceDescription(sb, (ServiceDescription) val);
				}
				else if (val instanceof Property) {
					encodeProperty(sb, (Property) val);
				}
				else if (val instanceof String) {
					encodeString(sb, (String) val);
				}
				else {
					sb.append(val);
				}
			}
			sb.append(')');
		}
	}
	
	private static void encodeString(StringBuffer sb, String s) {
		if (SimpleSLTokenizer.isAWord(s)) {
			sb.append(s);
		}
		else {
			sb.append(SimpleSLTokenizer.quoteString(s));
		}
	}
	
	
	//#MIDP_EXCLUDE_BEGIN 
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
	 * @param dfName is the AID of the DF that should perform the requested action
	 * @param actionName is the name of the action (one of the constants defined
	 * in FIPAManagementOntology: REGISTER / DEREGISTER / MODIFY / SEARCH).
	 * @param dfd is the agent description
	 * @param constraints are the search constraints (can be null if this is
	 * not a search operation)
	 * @return the behaviour to be added to the agent
	 @exception FIPAException A suitable exception can be thrown 
	 to indicate some error condition 
	 locally discovered (e.g.the agentdescription is not valid.)
	 @see jade.domain.FIPAAgentManagement.FIPAManagementOntology
	 @deprecated Use <code>AchieveREInitiator</code> instead
	 **/
	public static RequestFIPAServiceBehaviour getNonBlockingBehaviour(Agent a, AID dfName, String actionName, DFAgentDescription dfd, SearchConstraints constraints) throws FIPAException {
		return new RequestFIPAServiceBehaviour(a,dfName,actionName,dfd,constraints);
	}
	
	/**
	 * The default DF is used.
	 * @see #getNonBlockingBehaviour(Agent a, AID dfName, String actionName, DFAgentDescription dfd, SearchConstraints constraints) 
	 @deprecated Use <code>AchieveREInitiator</code> instead
	 **/
	public static RequestFIPAServiceBehaviour getNonBlockingBehaviour(Agent a, String actionName, DFAgentDescription dfd, SearchConstraints constraints) throws FIPAException {
		return getNonBlockingBehaviour(a,a.getDefaultDF(),actionName,dfd,constraints);
	}
	
	/**
	 * The default DF is used.
	 the default SearchContraints are used.
	 a default AgentDescription is used, where only the agent AID is set.
	 * @see #getNonBlockingBehaviour(Agent a, AID dfName, String actionName, DFAgentDescription dfd, SearchConstraints constraints) 
	 @deprecated Use <code>AchieveREInitiator</code> instead
	 **/
	public static RequestFIPAServiceBehaviour getNonBlockingBehaviour(Agent a, String actionName) throws FIPAException {
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(a.getAID());
		SearchConstraints constraints = new SearchConstraints();
		constraints.setMaxResults(MINUSONE);
		return getNonBlockingBehaviour(a,a.getDefaultDF(),actionName,dfd,constraints);
	}
	
	
	/**
	 the default SearchContraints are used.
	 a default AgentDescription is used, where only the agent AID is set.
	 * @see #getNonBlockingBehaviour(Agent a, AID dfName, String actionName, DFAgentDescription dfd, SearchConstraints constraints) 
	 @deprecated Use <code>AchieveREInitiator</code> instead
	 **/
	public static RequestFIPAServiceBehaviour getNonBlockingBehaviour(Agent a, AID dfName, String actionName) throws FIPAException {
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(a.getAID());
		SearchConstraints constraints = new SearchConstraints();
		constraints.setMaxResults(MINUSONE);
		return getNonBlockingBehaviour(a,dfName,actionName,dfd,constraints);
	}
	
	
	/**
	 * The defautl DF is used.
	 the default SearchContraints are used.
	 * @see #getNonBlockingBehaviour(Agent a, AID dfName, String actionName, DFAgentDescription dfd, SearchConstraints constraints) 
	 @deprecated Use <code>AchieveREInitiator</code> instead
	 **/
	public static RequestFIPAServiceBehaviour getNonBlockingBehaviour(Agent a, String actionName, DFAgentDescription dfd) throws FIPAException {
		SearchConstraints constraints = new SearchConstraints();
		constraints.setMaxResults(MINUSONE);
		return getNonBlockingBehaviour(a,a.getDefaultDF(),actionName,dfd,constraints);
	}
	
	/**
	 *   the default SearchContraints are used.
	 * @see #getNonBlockingBehaviour(Agent a, AID dfName, String actionName, DFAgentDescription dfd, SearchConstraints constraints) 
	 @deprecated Use <code>AchieveREInitiator</code> instead
	 **/
	public static RequestFIPAServiceBehaviour getNonBlockingBehaviour(Agent a, AID dfName, String actionName, DFAgentDescription dfd) throws FIPAException {
		SearchConstraints constraints = new SearchConstraints();
		constraints.setMaxResults(MINUSONE);
		return getNonBlockingBehaviour(a,dfName,actionName,dfd,constraints);
	}
	//#MIDP_EXCLUDE_END 
	
	/**
	 Default constructor.
	 */
	public DFService() {
	}
	
}

