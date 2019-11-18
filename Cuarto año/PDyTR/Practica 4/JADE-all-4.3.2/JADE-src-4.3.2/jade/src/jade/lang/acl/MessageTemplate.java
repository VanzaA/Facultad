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

package jade.lang.acl;

import java.io.Writer;
import java.io.IOException;
import java.util.Date;
import jade.util.leap.Serializable;

import jade.util.leap.Iterator;
import jade.util.leap.List;
import jade.util.leap.LinkedList;
import jade.util.leap.ArrayList;

import jade.core.AID;
import jade.core.CaseInsensitiveString;
import jade.core.messaging.TopicManagementHelper;
import jade.core.messaging.TopicUtility;

/**
 A pattern for matching incoming ACL messages. This class allows to
 build complex slot patterns to select ACL messages. These patterns
 can then be used in <code>receive()</code> operations.
 
 This class provide one method for each attribute of an ACLMessage, 
 that can be combined using the logic operators to create more complex 
 patterns.
 A user can also create an application-specific pattern. 
 In this case he has to implement the MatchExpression interface,
 writing the application specific <code>match()</code> method. 
 Then an instance of that class can be used as parameter of the MessageTemplate
 constructor to define the application specific MessageTemaplate.
 
 
 @see jade.core.Agent#receive(MessageTemplate mt)
 
 @author Giovanni Rimassa - Universita' di Parma
 @author Tiziana Trucco - Telecom Italia Lab S.p.A.
 @version $Date: 2008-10-09 14:07:18 +0200 (gio, 09 ott 2008) $ $Revision: 6052 $
 
 */
public class MessageTemplate implements Serializable {
	
	// Names of the various fields of an ACL messages.
	private static final int CONVERSATION_ID = 0;
	private static final int ENCODING = 1;
	private static final int IN_REPLY_TO = 2;
	private static final int LANGUAGE = 3;
	private static final int ONTOLOGY = 4;
	private static final int PROTOCOL = 5;
	private static final int REPLY_BY = 6;    
	private static final int REPLY_WITH = 7;
	private static final int RECEIVER = 9;
	private static final int REPLY_TO = 10;
	private static final int PERFORMATIVE = 11;
	private static final int CONTENT = 12;
	private static final int SENDER = 13;
	private static final int REPLY_BY_DATE = 14;
	
	/**
	 This interface must be overriden in order to define an application 
	 specific MessageTemplate.
	 In particular in the method <code> match()</code> the programmer
	 should realize the necessary checks on the ACLMessage in order 
	 to return <b>true</b> if the message match with the application 
	 specific requirements <b>false</b> otherwise.
	 */
	public static interface MatchExpression extends Serializable{
		/**
		 Check whether a given ACL message matches this
		 template. Concrete implementations of this interface will
		 have this method called to accept or refuse an ACL message.
		 @param msg The ACL message to match against this message
		 template.
		 @return A compliant implementation will return
		 <code>true</code> if the parameter ACL message matches the
		 template, and <code>false</code> otherwise.
		 */
		boolean match(ACLMessage msg);
	}
	
	private static class AndExpression implements MatchExpression {
		
		private MatchExpression op1;
		private MatchExpression op2;
		
		public AndExpression(MatchExpression e1, MatchExpression e2) {
			op1 = e1;
			op2 = e2;
		}
		
		public boolean match(ACLMessage msg) {
			return op1.match(msg) && op2.match(msg);
		}
		
		public String toString(){
			return "("+op1.toString()+  " AND " + op2.toString()+")";
		}
		
	} // End of AndExpression class
	
	private static class OrExpression implements MatchExpression{
		
		private MatchExpression op1;
		private MatchExpression op2;
		
		public OrExpression(MatchExpression e1, MatchExpression e2) {
			op1 = e1;
			op2 = e2;
		}
		
		public boolean match(ACLMessage msg) {
			return op1.match(msg) || op2.match(msg);
		}
		//only for debug
		public String toString(){
			return "("+op1.toString()+  " OR " + op2.toString()+")";
		}
		
	} // End of OrExpression class
	
	private static class NotExpression implements MatchExpression{
		private MatchExpression op;
		
		public NotExpression(MatchExpression e) {
			op = e;
		}
		
		public boolean match(ACLMessage msg) {
			return ! op.match(msg);
		}
		//only for debug
		public String toString(){
			return  "(NOT " + op.toString()+")";
		}
	} // End of NotExpression class
	
	private static class Literal implements MatchExpression{
		
		Object matchValue;
		int perfValue;
		int slotName;
		
		//Literal for all the string value to match: language, ontology,encoding...
		Literal(String matchValue, int slotName) {
			this.matchValue = matchValue;
			this.slotName = slotName;
		}
		
		//Literal for the sender value
		Literal(AID matchValue){
			this.matchValue = matchValue;
			this.slotName = SENDER;
		}
		//Literal for the receiver and replyTo slot.
		Literal(AID[] matchValue, int slotName){
			this.matchValue = matchValue;
			this.slotName = slotName;
		}
		
		//Literal for the performative slot
		Literal(int matchValue){
			this.perfValue = matchValue;
			this.slotName = PERFORMATIVE;
		}
		//literal for the reply_by_date slot.
		Literal(Date matchValue){
			this.matchValue=matchValue;
			this.slotName = REPLY_BY_DATE;
		}
		public boolean match(ACLMessage msg) {
			switch(slotName){
			
			case CONVERSATION_ID:
				return CaseInsensitiveString.equalsIgnoreCase((String)matchValue,msg.getConversationId());
			case REPLY_WITH:
				return CaseInsensitiveString.equalsIgnoreCase((String)matchValue,msg.getReplyWith());
			case PERFORMATIVE:
				return (perfValue == msg.getPerformative());
			case LANGUAGE:
				return CaseInsensitiveString.equalsIgnoreCase((String)matchValue,msg.getLanguage());
			case ONTOLOGY:
				return CaseInsensitiveString.equalsIgnoreCase((String)matchValue,msg.getOntology());
			case PROTOCOL:
				return CaseInsensitiveString.equalsIgnoreCase((String)matchValue,msg.getProtocol());
			case ENCODING:
				return CaseInsensitiveString.equalsIgnoreCase((String)matchValue,msg.getEncoding());
			case IN_REPLY_TO:
				return CaseInsensitiveString.equalsIgnoreCase((String)matchValue,msg.getInReplyTo());
				//  case(REPLY_BY):
				// return CaseInsensitiveString.equalsIgnoreCase((String)matchValue,msg.getReplyBy());
			case REPLY_BY_DATE:
				return ((Date)matchValue).equals(msg.getReplyByDate());
			case RECEIVER:
				if(matchValue != null){
					AID[] receivers = (AID[])matchValue;
					for(int i =0; i<receivers.length; i++){
						AID recToMatch = receivers[i];
						Iterator rec = msg.getAllReceiver();
						boolean found = false;
						while(rec.hasNext()){
							if(recToMatch.equals((AID)rec.next())){
								found = true;
								break; //out of the inner loop
							}
						}//end while
						if (found == false)
							return false;
					}//end for
					return true;
				}else
					return false;
				
			case REPLY_TO:
				if(matchValue != null){
					AID[] receivers = (AID[])matchValue;
					for(int i =0; i<receivers.length; i++){
						AID recToMatch = receivers[i];
						Iterator rec = msg.getAllReplyTo();
						boolean found = false;
						while(rec.hasNext()){
							if(recToMatch.equals((AID)rec.next())){
								found = true;
								break; //out of the inner loop
							}
						}//end while
						if (found == false)
							return false;
					}//end for
					return true;
				}else
					return false;
			case CONTENT://FIXME: verificare il caso in cui il contenuto e'in byte.
				return CaseInsensitiveString.equalsIgnoreCase((String)matchValue,msg.getContent());
			case SENDER:
				if(matchValue != null)
					return ((AID)matchValue).equals(msg.getSender());
				else
					return false;
				
			default:return false;
			}
		}
		
		//#MIDP_EXCLUDE_BEGIN
		//only for debug purpose.
		public String toString(){
			
			switch(slotName){
			
			case CONVERSATION_ID:
				return  "(ConversationId: " + (String)matchValue + ")";
			case ENCODING:
				return  "( Encoding: " +(String)matchValue + " )";
			case IN_REPLY_TO:
				return  "( InReplyTo: " + (String)matchValue + " )";
			case LANGUAGE:
				return  "( Language: " + (String)matchValue +  " )";
			case ONTOLOGY:
				return  "( Ontology: " + (String)matchValue + " )";
			case PROTOCOL:
				return  "( Protocol: " + (String)matchValue+  " )";
				// case(REPLY_BY):
				// return CaseInsensitiveString.equalsIgnoreCase((String)matchValue,msg.getReplyBy());
			case REPLY_BY_DATE:
				return  "( ReplyByDate: "+ (Date)matchValue + " )";
			case REPLY_WITH:
				return  "( ReplyWith: " + (String)matchValue + " )"; 
			case RECEIVER: 
				if(matchValue != null){
					AID[] receivers = (AID[])matchValue;
					String output =  "( Receivers: "; 
					for(int i =0; i<receivers.length; i++){
						AID recToMatch = receivers[i];
						output += recToMatch.toString();
					}
					return output +  ")" ;}
				else
					return  "(Receivers: null)"; 
			case REPLY_TO: //FIXME: da finire.
				if(matchValue != null){
					AID[] receivers = (AID[])matchValue;
					String output =  "( ReplyTo: "; 
					for(int i =0; i<receivers.length; i++){
						AID recToMatch = receivers[i];
						output += recToMatch.toString();
					}
					return output + " )" ;
				}else
					return  "(ReplyTo: null)" ;
			case PERFORMATIVE:
				return  "( Perfomative: " + ACLMessage.getPerformative(perfValue) + " )" ;
			case CONTENT:
				return  "( Content: " + (String)matchValue + ")";
			case SENDER:
				if(matchValue != null)
					return  "( Sender AID: " +  ((AID)matchValue).toString()+ ")" ;
				else
					return  "(Sender AID: null)" ;
				
			default:return  "No slot. This casa should never occur !!!" ;
			}
			
		}
		//#MIDP_EXCLUDE_END
	} // End of Literal class
	
	private static class MatchAllLiteral implements MatchExpression{
		//use this literal for a MessageTemplate who matches all ACLMessages
		MatchAllLiteral(){
		}
		
		public boolean match(ACLMessage msg){
			return true;
		}
		
		public String toString(){
			return  "Match ALL Template";     
		}
	}//end class MatchAllLiteral
	
	private static class CustomMsgLiteral implements MatchExpression{
		
		//use this literal for a messageTemplate matching the values of a given ACLMessage.
		ACLMessage messageToMatch;
		boolean matchPerformative;
		
		CustomMsgLiteral(ACLMessage msg,boolean matchPerformative){
			messageToMatch = msg;
			this.matchPerformative = matchPerformative;
		}

	    private static boolean compareByteArrays(byte[] a, byte[] a2) {
	        if (a==a2)
	            return true;
	        if (a==null || a2==null)
	            return false;

	        int length = a.length;
	        if (a2.length != length)
	            return false;

	        for (int i=0; i<length; i++)
	            if (a[i] != a2[i])
	                return false;

	        return true;
	    }

		public boolean match(ACLMessage msg){
			
			if(matchPerformative && (messageToMatch.getPerformative() != msg.getPerformative()))
				return false; 
			if(messageToMatch.hasByteSequenceContent()) {
				// we cannot use Array.equals() here because it is not available in MIDP 
				if (!compareByteArrays(messageToMatch.getByteSequenceContent(), msg.getByteSequenceContent()))
					return false;
				else if(!match(messageToMatch.getContent(),msg.getContent()))
					return false;
			}
			if(!match(messageToMatch.getConversationId(),msg.getConversationId()))
				return false;
			if(!match(messageToMatch.getEncoding(),msg.getEncoding()))
				return false;
			if(!match(messageToMatch.getInReplyTo(),msg.getInReplyTo()))
				return false;
			if(!match(messageToMatch.getLanguage(),msg.getLanguage()))
				return false;
			if(!match(messageToMatch.getOntology(),msg.getOntology()))
				return false;
			if(!match(messageToMatch.getProtocol(),msg.getProtocol()))
				return false;
			if(!match(messageToMatch.getReplyWith(),msg.getReplyWith()))
				return false;
			if(!match(messageToMatch.getReplyByDate(),msg.getReplyByDate()))
				return false;
			//receiver
			Iterator it1 = messageToMatch.getAllReceiver();
			while(it1.hasNext()){
				boolean found = false;
				AID rec = (AID)it1.next();
				Iterator it2 = msg.getAllReceiver();
				while(it2.hasNext()){
					if(rec.equals((AID)it2.next())){
						found = true;
						break;
					}    
				}//end while
				if(found == false)
					return false; //when a receiver of the template is not into the receivers of the ACLMessage.
			}//end while
			
			//replyTo
			Iterator it3 = messageToMatch.getAllReceiver();
			while(it3.hasNext()){
				boolean found = false;
				AID rec = (AID)it3.next();
				Iterator it2 = msg.getAllReceiver();
				while(it2.hasNext()){
					if(rec.equals((AID)it2.next())){
						found = true;
						break;
					}    
				}//end while
				if(found == false)
					return false; //when a receiver of the template is not into the receivers of the ACLMessage.
			}//end while
			
			//sender
			if((messageToMatch.getSender() != null) &&!(messageToMatch.getSender().equals(msg.getSender())))
				return false;
			
			return true;
		}
		
		private boolean match(String template, String actualValue){
			if(template == null)
				return true;
			else
				return CaseInsensitiveString.equalsIgnoreCase(template,actualValue);
		}
		
		private boolean match(Date template,Date actualValue){
			if(template == null)
				return true;
			else
				return template.equals(actualValue);
		}
		
		//only for debug purpose.
		public String toString(){
			String output = (matchPerformative ?  "match the performative "  : "no match on performative "   );
			return (output + messageToMatch.toString());
		}
		
	}//end class CustomMsgLiteral
	
	private static class MatchTopic implements MatchExpression {
		private String topicName;
		private boolean isTemplate;
		private boolean matchAll = false;
		
		MatchTopic(AID topic) {
			String tmp = topic.getLocalName();
			if (tmp.equals(TopicManagementHelper.TOPIC_TEMPLATE_WILDCARD)) {
				matchAll = true;
			}
			else {
				if (tmp.endsWith("."+TopicManagementHelper.TOPIC_TEMPLATE_WILDCARD)) {
					topicName = tmp.substring(0, tmp.length()-2);
					isTemplate = true;
				}
				else {
					topicName = tmp;
					isTemplate = false;
				}
			}
		}
		
		public boolean match(ACLMessage msg) {
			Iterator it = msg.getAllReceiver();
			while (it.hasNext()) {
				AID receiver = (AID) it.next();
				if (TopicUtility.isTopic(receiver)) {
					if (matchAll) {
						return true;
					}
					else {
						String name = receiver.getLocalName();
						if (name.equals(topicName)) {
							return true;
						}
						else if (isTemplate && name.startsWith(topicName+'.')) {
							return true;
						}
					}
				}
			}
			return false;
		}
		
		public String toString() {
			String name = (matchAll ? TopicManagementHelper.TOPIC_TEMPLATE_WILDCARD : (isTemplate ? topicName+'.'+TopicManagementHelper.TOPIC_TEMPLATE_WILDCARD : topicName));
			return "( Topic: "+name+" )";
		}
	}
	/**
	 @serial
	 */
	private MatchExpression toMatch;
	
	/** Public constructor to use when the user needs to define 
	 an application specific pattern.	 
	 */
	
	public MessageTemplate(MatchExpression e) {
		toMatch = e;
	}
	
	/**
	 This <em>Factory Method</em> returns a message template that
	 matches any message.
	 @return A new <code>MessageTemplate</code> matching any given
	 value.
	 */
	
	public static MessageTemplate MatchAll() {
		return new MessageTemplate(new MatchAllLiteral());
	}
	
	/**
	 This <em>Factory Method</em> returns a message template that
	 matches any message with a given <code>:sender</code> slot.
	 @param value The value the message slot will be matched against.
	 @return A new <code>MessageTemplate</code> matching the given
	 value.
	 */
	public static MessageTemplate MatchSender(AID value) {
		return new MessageTemplate(new Literal(value));
	}
	
	/**
	 This <em>Factory Method</em> returns a message template that
	 matches any message with a given <code>:receiver</code> slot.
	 @param values An array of Agent IDs against which the
	 value of the message slot will be matched.
	 @return A new <code>MessageTemplate</code> matching the given
	 value.
	 */
	public static MessageTemplate MatchReceiver(AID[] values) {
		return new MessageTemplate(new Literal(values,RECEIVER));
	}
	
	/**
	 This <em>Factory Method</em> returns a message template that
	 matches any message about a given topic.
	 @param topic An AID representing the topic to be matched
	 @return A new <code>MessageTemplate</code> matching messages about the given topic
	 */
	public static MessageTemplate MatchTopic(AID topic) {
		return new MessageTemplate(new MatchTopic(topic));
	}
	
	/**
	 This <em>Factory Method</em> returns a message template that
	 matches any message with a given <code>:content</code> slot.
	 @param value The value the message slot will be matched against.
	 @return A new <code>MessageTemplate</code> matching the given
	 value.
	 */
	public static MessageTemplate MatchContent(String value) {
		return new MessageTemplate(new Literal(value,CONTENT));
	}
	
	/**
	 This <em>Factory Method</em> returns a message template that
	 matches any message with a given <code>:reply-with</code> slot.
	 @param value The value the message slot will be matched against.
	 @return A new <code>MessageTemplate</code> matching the given
	 value.
	 */
	public static MessageTemplate MatchReplyWith(String value) {
		return new MessageTemplate(new Literal(value, REPLY_WITH));
	}
	
	/**
	 This <em>Factory Method</em> returns a message template that
	 matches any message with a given <code>:in-reply-to</code> slot.
	 @param value The value the message slot will be matched against.
	 @return A new <code>MessageTemplate</code> matching the given
	 value.
	 */
	public static MessageTemplate MatchInReplyTo(String value) {
		return new MessageTemplate(new Literal(value,IN_REPLY_TO));
	}
	
	/**
	 This <em>Factory Method</em> returns a message template that
	 matches any message with a given <code>:reply-to</code> slot.
	 @param values An array of Agent IDs against which the
	 value of the message slot will be matched.
	 @return A new <code>MessageTemplate</code> matching the given
	 value.
	 */
	public static MessageTemplate MatchReplyTo(AID[] values) {
		return new MessageTemplate(new Literal(values,REPLY_TO));
	}
	
	/**
	 This <em>Factory Method</em> returns a message template that
	 matches any message with a given <code>:language</code> slot.
	 @param value The value the message slot will be matched against.
	 @return A new <code>MessageTemplate</code> matching the given
	 value.
	 */
	public static MessageTemplate MatchLanguage(String value) {
		return new MessageTemplate(new Literal(value,LANGUAGE));
	}
	
	/**
	 This <em>Factory Method</em> returns a message template that
	 matches any message with a given <code>:encoding</code> slot.
	 @param value The value the message slot will be matched against.
	 @return A new <code>MessageTemplate</code> matching the given
	 value.
	 */
	public static MessageTemplate MatchEncoding(String value) {
		return new MessageTemplate(new Literal(value,ENCODING));
	}
	
	/**
	 This <em>Factory Method</em> returns a message template that
	 matches any message with a given <code>:ontology</code> slot.
	 @param value The value the message slot will be matched against.
	 @return A new <code>MessageTemplate</code> matching the given
	 value.
	 */
	public static MessageTemplate MatchOntology(String value) {
		return new MessageTemplate(new Literal(value,ONTOLOGY));
	}
	
	/**
	 This <em>Factory Method</em> returns a message template that
	 matches any message with a given <code>:reply-by</code> slot.
	 @param value The <code>Date</code> the message slot will be matched against.
	 @return A new <code>MessageTemplate</code> matching the given
	 value.
	 **/
	public static MessageTemplate MatchReplyByDate(Date value){
		return new MessageTemplate(new Literal(value));
	}
	
	/**
	 This <em>Factory Method</em> returns a message template that
	 matches any message with a given <code>:protocol</code> slot.
	 @param value The value the message slot will be matched against.
	 @return A new <code>MessageTemplate</code> matching the given
	 value.
	 */
	public static MessageTemplate MatchProtocol(String value) {
		return new MessageTemplate(new Literal(value, PROTOCOL));
	}
	
	/**
	 This <em>Factory Method</em> returns a message template that
	 matches any message with a given <code>:conversation-id</code> slot.
	 @param value The value the message slot will be matched against.
	 @return A new <code>MessageTemplate</code> matching the given
	 value.
	 */
	public static MessageTemplate MatchConversationId(String value) {
		return new MessageTemplate(new Literal(value,CONVERSATION_ID));
	}
	
	/**
	 This <em>Factory Method</em> returns a message template that
	 matches any message with a given performative.
	 @param value The value the message slot will be matched against.
	 @return A new <code>MessageTenplate</code>matching the given
	 value.
	 */
	public static MessageTemplate MatchPerformative(int value){
		return new MessageTemplate(new Literal(value));
	}
	
	/**
	 This <em>Factory Method</em> returns a message template that
	 matches ACL messages against a given one, passed as
	 parameter. The following algorithm is used:
	 When the given <code>ACLMessage</code> has a non
	 <code>null</code> slot, subsequent messages must have the same
	 slot value in that slot to have a match.
	 When the given <code>ACLMessage</code> has a <code>null</code>
	 slot, subsequent messages can have any value for that slot and
	 still match the template.
	 In short, a <code>null</code> value for a slot means <em>don't
	 care</em>.
	 @param msg The <code>ACLMessage</code> used to build a custom
	 message template.
	 @param matchPerformative a <code>bool</code> value. When
	 <code>true</code>, the performative of the <code>msg</code> will
	 be considered as a part of the template (i.e. the message
	 template will match only ACL messages with the same performativa
	 as <code>msg</code>).
	 When <false>, the performative of <code>msg</code> is ignored and
	 the resulting message template will not consider it when matching
	 messages.
	 @return A new <code>MessageTemplate</code>, matching the given
	 message according to the above algorithm.
	 */
	public static MessageTemplate MatchCustom(ACLMessage msg, boolean matchPerformative) {
		ACLMessage message = (ACLMessage)msg.clone();
		return new MessageTemplate(new CustomMsgLiteral(message, matchPerformative));
	}
	
	/**
	 Logical <b>and</b> between two <code>MessageTemplate</code>
	 objects. This method creates a new message template that is
	 matched by those ACL messages matching <b><em>both</b></em>
	 message templates given as operands.
	 @param op1 The first <em>and</em> operand.
	 @param op2 The second <em>and</em> operand.
	 @return A new <code>MessageTemplate</code> object.
	 @see jade.lang.acl.MessageTemplate#or(MessageTemplate op1, MessageTemplate op2)
	 */
	public static MessageTemplate and(MessageTemplate op1, MessageTemplate op2) {
		AndExpression e = new AndExpression(op1.toMatch, op2.toMatch);
		MessageTemplate result = new MessageTemplate(e);
		return result;
	}
	
	/**
	 Logical <b>or</b> between two <code>MessageTemplate</code>
	 objects. This method creates a new message template that is
	 matched by those ACL messages matching <b><em>any</b></em> of the
	 two message templates given as operands.
	 @param op1 The first <em>or</em> operand.
	 @param op2 The second <em>or</em> operand.
	 @return A new <code>MessageTemplate</code> object.
	 @see jade.lang.acl.MessageTemplate#and(MessageTemplate op1, MessageTemplate op2)
	 */
	public static MessageTemplate or(MessageTemplate op1, MessageTemplate op2) {
		OrExpression e = new OrExpression(op1.toMatch, op2.toMatch);
		MessageTemplate result = new MessageTemplate(e);
		return result;
	}
	
	/**
	 Logical <b>not</b> of a <code>MessageTemplate</code> object. This
	 method creates a new message template that is matched by those
	 ACL messages <b><em>not</em></b> matching the message template
	 given as operand.
	 @param op The <em>not</em> operand.
	 @return A new <code>MessageTemplate</code> object.
	 */
	public static MessageTemplate not(MessageTemplate op) {
		NotExpression e = new NotExpression(op.toMatch);
		MessageTemplate result = new MessageTemplate(e);
		return result;
	}
	
	/**
	 Matches an ACL message against this <code>MessageTemplate</code>
	 object.
	 @param msg The <code>ACLMessage</code> to check for matching.
	 @return <code>true</code> if the ACL message matches this
	 template, <code>false</code> otherwise.
	 */
	public boolean match(ACLMessage msg) {
		return toMatch.match(msg);
	}
	
	/**
	 Retrieve a string representation of this message template.
	 @return A string describing the syntactic structure of this
	 message template.
	 */
	public String toString(){
		return toMatch.toString();
	}
}
