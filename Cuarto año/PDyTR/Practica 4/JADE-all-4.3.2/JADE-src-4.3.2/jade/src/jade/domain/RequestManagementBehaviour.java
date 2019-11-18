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

//#MIDP_EXCLUDE_FILE

//#APIDOC_EXCLUDE_FILE


import jade.core.Agent;
import jade.core.CaseInsensitiveString;

import jade.content.onto.OntologyException;
import jade.content.lang.Codec.CodecException;
import jade.content.onto.basic.Action;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import jade.proto.SimpleAchieveREResponder;

import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.UnsupportedValue;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.security.JADESecurityException;
import jade.domain.FIPAAgentManagement.ExceptionVocabulary;

import jade.util.Logger;

/**
   Base class for AMS and DF behaviours managing requests from agents.
   This class handles the FIPA-request protocol and in particular prepares
   the response taking into account all possible exceptions.
   The preparation of the result notification is delegated to subclasses as
   its form (RESULT or DONE) and sending time (i.e. whether it can be sent 
   immediately or must be delayed at a later time) depends on the specific 
   action.
   @author Giovanni Caire - Tilab
 */
public abstract class RequestManagementBehaviour extends SimpleAchieveREResponder {
	private ACLMessage notification;
	protected Logger myLogger;

	protected RequestManagementBehaviour(Agent a, MessageTemplate mt){
		super(a,mt);
		if (myAgent != null) {
			myLogger = Logger.getMyLogger(myAgent.getLocalName());
		}
	}

	public void onStart() {
		if (myLogger == null) {
			myLogger = Logger.getMyLogger(myAgent.getLocalName());
		}
		super.onStart();
	}
	
	protected abstract ACLMessage performAction(Action slAction, ACLMessage request) throws JADESecurityException, FIPAException; 

	/**
	 * @return null when the AGREE message can be skipper, the AGREE message
	 * otherwise.
	 */
	protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
		ACLMessage response = null;  
		Throwable t = null;
		try{	
			// Check the language is SL0, SL1, SL2 or SL. 
			isAnSLRequest(request);

			// Extract the content
			Action slAction = (Action) myAgent.getContentManager().extractContent(request);

			// Perform the action
			notification = performAction(slAction, request);

			// Action OK 
		} 
		catch (OntologyException oe) {
			// Error decoding request --> NOT_UNDERSTOOD
			response = request.createReply();
			response.setPerformative(ACLMessage.NOT_UNDERSTOOD);
			response.setContent("("+ExceptionVocabulary.UNRECOGNISEDVALUE+" content)");
			t = oe;
		}	
		catch (CodecException ce) {
			// Error decoding request --> NOT_UNDERSTOOD
			response = request.createReply();
			response.setPerformative(ACLMessage.NOT_UNDERSTOOD);
			response.setContent("("+ExceptionVocabulary.UNRECOGNISEDVALUE+" content)");
			t = ce;
		}	
		catch (RefuseException re) {
			// RefuseException thrown during action execution --> REFUSE
			response = request.createReply();
			response.setPerformative(ACLMessage.REFUSE);
			response.setContent(prepareErrorContent(request.getContent(), re.getMessage()));
			t = re;
		}	
		catch (FailureException fe) {
			// FailureException thrown during action execution --> FAILURE
			notification = request.createReply();
			notification.setPerformative(ACLMessage.FAILURE);
			notification.setContent(prepareErrorContent(request.getContent(), fe.getMessage()));
			t = fe;
		}	
		catch(FIPAException fe){
			// Malformed request --> NOT_UNDERSTOOD
			response = request.createReply();
			response.setPerformative(ACLMessage.NOT_UNDERSTOOD);
			response.setContent("("+fe.getMessage()+")");
			t = fe;
		}
		catch(Throwable tr){
			tr.printStackTrace();
			// Generic error --> FAILURE
			notification = request.createReply();
			notification.setPerformative(ACLMessage.FAILURE);

			notification.setContent(prepareErrorContent(request.getContent(), ExceptionVocabulary.INTERNALERROR+" \""+tr+"\""));
		}
		if (t != null) {
			myLogger.log(Logger.WARNING, "Agent "+myAgent.getLocalName()+" - Error handling request", t);
		}
		return response;
	}

	/**
     Just return the (already prepared) notification message (if any).
	 */
	protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException{	
		return notification;       
	}

	//to reset the action
	public void reset(){
		super.reset();
		notification = null;
	}

	private void isAnSLRequest(ACLMessage msg) throws FIPAException { 
		String language = msg.getLanguage();
		if ( (!CaseInsensitiveString.equalsIgnoreCase(FIPANames.ContentLanguage.FIPA_SL0, language)) &&
				(!CaseInsensitiveString.equalsIgnoreCase(FIPANames.ContentLanguage.FIPA_SL1, language)) &&
				(!CaseInsensitiveString.equalsIgnoreCase(FIPANames.ContentLanguage.FIPA_SL2, language)) &&
				(!CaseInsensitiveString.equalsIgnoreCase(FIPANames.ContentLanguage.FIPA_SL, language))) {
			throw new UnsupportedValue("language");
		}
	}

	private String prepareErrorContent(String content, String e) {
		String tmp = content.trim();
		tmp = tmp.substring(1, tmp.length()-1);
		return "("+tmp+" "+e+")";
	}
}
