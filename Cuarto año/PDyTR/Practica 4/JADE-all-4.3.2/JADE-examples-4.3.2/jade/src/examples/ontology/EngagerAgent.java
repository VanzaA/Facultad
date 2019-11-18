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

package examples.ontology;

import jade.content.ContentElementList;
import jade.content.Predicate;
import jade.content.abs.AbsPredicate;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.lang.sl.SLVocabulary;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.content.onto.basic.Done;
import jade.content.onto.basic.TrueProposition;
import jade.core.Agent;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SimpleAchieveREResponder;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import jade.util.leap.List;
import examples.ontology.employment.Address;
import examples.ontology.employment.Company;
import examples.ontology.employment.EmploymentOntology;
import examples.ontology.employment.Engage;
import examples.ontology.employment.EngagementError;
import examples.ontology.employment.Person;
import examples.ontology.employment.PersonTooOld;
import examples.ontology.employment.WorksFor;

/**
	This agent is able to engage people on behalf of company 
	CSELT
	Via Reiss Romoli 274 - Turin
	
	@author Giovanni Caire - CSELT S.p.A
	@version $Date: 2008-10-09 14:04:02 +0200 (gio, 09 ott 2008) $ $Revision: 6051 $
	@see examples.ontology.RequesterAgent
*/
public class EngagerAgent extends Agent {
	
	// AGENT BEHAVIOURS
	/**
		This behaviour handles all queries about people working for a company
		following the FIPA-Query protocol
	*/
	class HandleEnganementQueriesBehaviour extends SimpleAchieveREResponder {
		/**
			Constructor for the <code>HandleEnganementQueriesBehaviour</code>
			class.
			
			@param myAgent The agent owning this behaviour
		*/
		public HandleEnganementQueriesBehaviour(Agent myAgent){
			super(myAgent, MessageTemplate.and(
												MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_QUERY),
												MessageTemplate.MatchOntology(EmploymentOntology.NAME)));
		}
		
		/**
			This method is called when a QUERY-IF or QUERY-REF message is received.
			
			@param msg The received query message
			@return The ACL message to be sent back as reply
			@see jade.proto.FipaQueryResponderBehaviour
		*/
		public ACLMessage prepareResponse(ACLMessage msg){
			
			ACLMessage reply = msg.createReply();
			
			// The QUERY message could be a QUERY-REF. In this case reply 
			// with NOT_UNDERSTOOD
			
			if (msg.getPerformative() != ACLMessage.QUERY_IF){
				reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
  				String content = "(" + msg.toString() + ")"; 
				reply.setContent(content);
				return(reply);
			}
			
			try {
				// Get the predicate for which the truth is queried	
				Predicate pred = (Predicate)myAgent.getContentManager().extractContent(msg);
				if (!(pred instanceof WorksFor)) {
				// If the predicate for which the truth is queried is not WORKS_FOR
				// reply with NOT_UNDERSTOOD
					reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
  					String content = "(" + msg.toString() + ")"; 
					reply.setContent(content);
					return(reply);
				}
			
				// Reply 
				reply.setPerformative(ACLMessage.INFORM);
				WorksFor wf = (WorksFor)pred;
				Person p = wf.getPerson();
				Company c = wf.getCompany();
				if (((EngagerAgent) myAgent).isWorking(p, c)) 
					reply.setContent(msg.getContent());
				 else {
					// Create an object representing the fact that the WORKS_FOR 
					// predicate is NOT true.
					Ontology o = getContentManager().lookupOntology(EmploymentOntology.NAME);
					AbsPredicate not = new AbsPredicate(SLVocabulary.NOT);
  					not.set(SLVocabulary.NOT_WHAT, o.fromObject(wf));
		    		myAgent.getContentManager().fillContent(reply, not);
				}
			}
			catch (Codec.CodecException fe) {
				System.err.println(myAgent.getLocalName()+" Fill/extract content unsucceeded. Reason:" + fe.getMessage());
			}
			catch (OntologyException oe){
				System.err.println(myAgent.getLocalName()+" getRoleName() unsucceeded. Reason:" + oe.getMessage());
			}
			
			return (reply);
			
		} // END of handleQueryMessage() method
		
	} // END of HandleEnganementQueriesBehaviour
				
			
	/**
		This behaviour handles a single engagement action that has been  
		requested following the FIPA-Request protocol
	*/
	class HandleEngageBehaviour extends SimpleAchieveREResponder {
  		
  	/**
  		Constructor for the <code>HandleEngageBehaviour</code> class.
  		
  		@param myAgent The agent owning this behaviour
  		@param requestMsg The ACL message by means of which the engagement
  		action has been requested
  	*/
  	public HandleEngageBehaviour(Agent myAgent){
			super(myAgent, MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST));
  	}
  	
  	/**
	  	This method implements the <code>FipaRequestResponderBehaviour.Factory</code>
  		interface.
  		It will be called within a <code>FipaRequestResponderBehaviour</code> 
  		when an engagement action is requested to instantiate a new 
  		<code>HandleEngageBehaviour</code> handling the requested action
   
  		@param msg The ACL message by means of which the engagement
  		action has been requested
  	*/
    /**
    	This method actually handles the engagement action
     */
     
    public ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
    		// Prepare a dummy ACLMessage used to create the content of all reply messages
   			ACLMessage msg = request.createReply();

			try{
				// Get the requested action
				Action a = (Action)myAgent.getContentManager().extractContent(request);
				Engage e = (Engage) a.getAction();
				Person p = e.getPerson();
				Company c = e.getCompany();
				
				// Check person's age. If < 35 --> AGREE, else REFUSE and exit
				// Perform the engagement action
				int result = ((EngagerAgent) myAgent).doEngage(p, c);
				
				// Reply according to the result
				if (result > 0){
					// OK --> INFORM action done
					Done d = new Done();
					d.setAction(a);
					myAgent.getContentManager().fillContent(msg, d);
					msg.setPerformative(ACLMessage.INFORM);
				}
				else{
					// NOT OK --> FAILURE
					ContentElementList l = new ContentElementList();
					l.add(a);
					l.add(new EngagementError());
					myAgent.getContentManager().fillContent(msg, l);
					msg.setPerformative(ACLMessage.FAILURE);
				}
				
			}
			catch (Exception fe){
				System.out.println(myAgent.getName() + ": Error handling the engagement action.");
				System.out.println(fe.getMessage());
			}
			
			// System.out.println(msg);
			return msg;
		}
		
	     
	public ACLMessage prepareResponse(ACLMessage request) {
			// Prepare a dummy ACLMessage used to create the content of all reply messages
			ACLMessage temp = request.createReply();
						
			try{
				// Get the requested action. 
				Action a = (Action)getContentManager().extractContent(request);
				Engage e = (Engage) a.getAction();
				Person p = e.getPerson();
				Company c = e.getCompany();
			
				// Check person's age. If < 35 --> AGREE, else REFUSE and exit
				if (p.getAge().intValue() < 35){
					// AGREE to accomplish the engagement action without any 
					// special condition.
					ContentElementList l = new ContentElementList();
					l.add(a);
					l.add(new TrueProposition());
					getContentManager().fillContent(temp, l);
					temp.setPerformative(ACLMessage.AGREE);
				}
				else {
					ContentElementList l = new ContentElementList();
					l.add(a);
					l.add(new PersonTooOld());
					getContentManager().fillContent(temp, l);
					temp.setPerformative(ACLMessage.REFUSE);
				}

			} catch (Exception fe){
				fe.printStackTrace();
				System.out.println(getName() + ": Error handling the engagement action.");
				System.out.println(fe.getMessage());
			}
			
			return temp;
		}
	}	
	
	// AGENT LOCAL VARIABLES
	private Company representedCompany; // The company on behalf of which this agent is able to engage people 
	private List employees;	// The people currently working for the company
	
	// AGENT CONSTRUCTOR
	public EngagerAgent(){
		super();
		
		representedCompany = new Company();
		representedCompany.setName("CSELT");
		Address a = new Address();
		a.setStreet("\"Via Reiss Romoli\"");
		a.setNumber(new Long(274));
		a.setCity("Turin");
		representedCompany.setAddress(a);
		
		employees = new ArrayList();
	}
	
	// AGENT SETUP
	protected void setup() {
		System.out.println("This is the EngagerAgent representing the company "+representedCompany.getName());
		
		// Register the codec for the SL0 language
		getContentManager().registerLanguage(new SLCodec(), FIPANames.ContentLanguage.FIPA_SL0);	
		
		// Register the ontology used by this application
		getContentManager().registerOntology(EmploymentOntology.getInstance());
			
		// Create and add the behaviour for handling QUERIES using the employment-ontology
  		addBehaviour(new HandleEnganementQueriesBehaviour(this));
  	
		// Create and add the behaviour for handling REQUESTS using the employment-ontology
		MessageTemplate mt = MessageTemplate.and(
											MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
											MessageTemplate.MatchOntology(EmploymentOntology.NAME));
  	HandleEnganementQueriesBehaviour b = new HandleEnganementQueriesBehaviour(this);
  	HandleEngageBehaviour c = new HandleEngageBehaviour(this);
  	addBehaviour(b);
  	addBehaviour(c);
	}
	
	// AGENT METHODS
	boolean isWorking(Person p, Company c){
		boolean isAnEmployee = false;	
		Iterator i = employees.iterator();
		while (i.hasNext()){
			Person empl = (Person) i.next();
			if (p.equals(empl))
				isAnEmployee = true;
		}
		
		if (c.equals(representedCompany))
			return isAnEmployee;
		else
			return !isAnEmployee;
	}
	
	int doEngage(Person p, Company c){
		if (!c.equals(representedCompany))
			return (-1); // Can engage people on behalf of representedCompany only
		else
			employees.add(p);
		return (1);
	}
}
