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

import jade.content.lang.sl.SL0Vocabulary;
import jade.content.lang.sl.SimpleSLTokenizer;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import jade.domain.FIPAAgentManagement.FIPAManagementVocabulary;
import jade.domain.FIPAAgentManagement.RefuseException;

import java.util.Date;

/**
 * This class provides a set of basic and static methods to perform the FIPA Agent Management actions.
 * However, developers should use <code>DFService</code> and <code>AMSService</code>
 * which provide specialized methods to communicate with the DF and the AMS.
 * @author Fabio Bellifemine - CSELT S.p.A.
 * @version $Date: 2010-05-18 14:40:49 +0200 (mar, 18 mag 2010) $ $Revision: 6340 $  
 **/
public class FIPAService {
	private static int cnt = 0;
	
	private synchronized static int getNextInt() {
		int ret = cnt;
		cnt = (cnt < 9999 ? (++cnt) : 0);
		return ret;
	}
	
	/**
	 * create a REQUEST message with the following slots:
	 * <code> (REQUEST :sender sender.getAID() :receiver receiver
	 * :protocol fipa-request :language FIPA-SL0 :ontology fipa-agent-management
	 * :reply-with xxx :conversation-id xxx) </code>
	 * where <code>xxx</code> are unique words generated on the basis of
	 * the sender's name and the current time.
	 * @param sender is the Agent sending the message
	 * @param receiver is the AID of the receiver agent
	 * @return an ACLMessage object 
	 */
	static ACLMessage createRequestMessage(Agent sender, AID receiver) {
		ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
		request.setSender(sender.getAID());
		request.addReceiver(receiver);
		request.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
		request.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);
		request.setOntology(FIPAManagementVocabulary.NAME);
		int n = getNextInt();
		request.setReplyWith("rw-"+sender.getName()+System.currentTimeMillis()+'-'+n);
		request.setConversationId("conv-"+sender.getName()+System.currentTimeMillis()+'-'+n);
		return request;
	}
	
	
	
	/**
	 * This method plays the initiator role in the Fipa-Request
	 * interaction protocol and performs all the steps of the
	 * protocol. The method uses the
	 * <code>:reply-with</code>/<code>:in-reply-to</code> ACL message
	 * slots as a mechanism to match the protocol replies.
	 * Take care because the method blocks until all the response messages are received.
	 * Under error conditions, or if the responder does not wish to respond, that
	 * might block for ever the execution of the agent.
	 * For this reason, the <code>FipaRequestInitiatorBehaviour</code> is the preferred
	 * way to play the protocol.
	 * @param a is the Agent playing the initiator role
	 * @param request is the ACLMessage to be sent. Notice that all the
	 * slots of the message must have already been filled by the
	 * caller. If the <code>:reply-with</code> message slot is not set,
	 * a default one will be generated automatically.
	 * @return the INFORM message received in the final state of the protocol, if
	 * the protocol succeeded, otherwise it throws an Exception
	 */
	public static ACLMessage doFipaRequestClient(Agent a, ACLMessage request) throws FIPAException {
		return doFipaRequestClient(a, request, 0);
	}
	
	/**
	 * This method plays the initiator role in the Fipa-Request
	 * interaction protocol and performs all the steps of the protocol,
	 * and additionally sets a conversation timeout. The method uses the
	 * <code>:reply-with</code>/<code>:in-reply-to</code> ACL message
	 * slots as a mechanism to match the protocol replies.  Under error
	 * conditions, or if the responder does not wish to respond, that
	 * might block the execution of the agent until the conversation
	 * timeout expires.  For this reason, the
	 * <code>FipaRequestInitiatorBehaviour</code> is the preferred way
	 * to play the protocol.
	 * @param a is the Agent playing the initiator role
	 * @param request is the ACLMessage to be sent. Notice that all the
	 * slots of the message must have already been filled by the
	 * caller. If the <code>:reply-with</code> message slot is not set,
	 * a default one will be generated automatically.
	 * @param timeout The maximum time to wait for the conversation to finish, in milliseconds.
	 * @return the INFORM message received in the final state of the protocol, if
	 * the protocol succeeded, otherwise it throws an Exception
	 */
	public static ACLMessage doFipaRequestClient(Agent a, ACLMessage request, long timeout) throws FIPAException {
		String key = null;
		// If the request message does not have a ':reply-with' and :conversation-id slots set them		
		if (request.getReplyWith() == null) {
			key = a.getLocalName()+System.currentTimeMillis()+'-'+getNextInt();
			request.setReplyWith("rw-"+key);
		}
		if (request.getConversationId() == null) {
			if (key == null) {
				key = a.getLocalName()+System.currentTimeMillis()+'-'+getNextInt();
			}
			request.setConversationId("cid-"+key);
		}
		
		long sendTime = System.currentTimeMillis();
		a.send(request);
		MessageTemplate mt = MessageTemplate.MatchInReplyTo(request.getReplyWith());
		
		ACLMessage reply = a.blockingReceive(mt, timeout);
		
		if(reply != null) {
			if (reply.getPerformative() == ACLMessage.INFORM) {
				return reply;
			}
			if (reply.getPerformative() == ACLMessage.AGREE){
				// We received an AGREE --> Go back waiting for the INFORM unless 
				// a timeout was set and it is expired.
				if (timeout > 0) {
					long agreeTime = System.currentTimeMillis();
					timeout -= (agreeTime - sendTime);
					if (timeout <= 0) {
						return null;
					}
				}
				reply = a.blockingReceive(mt, timeout);
			}
			if(reply != null) {
				if (reply.getPerformative() == ACLMessage.INFORM){
					return reply;
				}
				else {
					// We received a REFUSE, NOT_UNDERSTOOD, FAILURE or OUT_OF_SEQUENCE --> ERROR
					//#MIDP_EXCLIDE_BEGIN Avoid loading all exception classes in MIDP
					switch (reply.getPerformative()) {
					case ACLMessage.REFUSE:
						throw new jade.domain.FIPAAgentManagement.RefuseException(reply);
					case ACLMessage.FAILURE:
						throw new jade.domain.FIPAAgentManagement.FailureException(reply);
					case ACLMessage.NOT_UNDERSTOOD:
						throw new jade.domain.FIPAAgentManagement.NotUnderstoodException(reply);
					}
					//#MIDP_EXCLIDE_END
					throw new FIPAException(reply);
				}
			}
		}
		// The timeout has expired
		return null;
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
				if (next.equals(SL0Vocabulary.AID)) {
					l.add(parseAID(parser));
				}
			}
			next = parser.nextToken();
		}
		parser.consumeChar(')');
		return l;
	}

}
