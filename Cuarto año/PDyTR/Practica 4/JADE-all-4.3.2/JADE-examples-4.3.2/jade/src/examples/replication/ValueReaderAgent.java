package examples.replication;

import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class ValueReaderAgent extends Agent {
	
	private AID provider;
	
	protected void setup() {
		// Register required language and ontologies
		getContentManager().registerLanguage(new SLCodec());
		getContentManager().registerOntology(ValueManagementOntology.getInstance());
		
		// Search the DF to find an agent providing a service of type "ValueProvider"
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("ValueProvider");
		template.addServices(sd);
		
		try {
			DFAgentDescription[] res = DFService.searchUntilFound(this, this.getDefaultDF(), template, null, 10000);
			if (res.length > 0) {
				provider = res[0].getName();
				System.out.println("Agent "+getLocalName()+" - ValueProvider agent "+provider.getLocalName()+" found");
				
				// Add the behaviour that periodically read the value from the value provider agent
				addBehaviour(new PeriodicValueReader());
			}
			else {
				System.out.println("Agent "+getLocalName()+" - No ValueProvider agent found.");
				doDelete();
			}
		}
		catch (FIPAException fe) {
			System.out.println("Agent "+getLocalName()+" - Error searching the DF for a ValueProvider agent");
			fe.printStackTrace();
			doDelete();
		}
	}

	
	/**
	 * Inner class PeriodicValueReader
	 * This is a behaviour that periodically retrieve the value from the value provider agent
	 * and prints it in the standard output
	 */
	private class PeriodicValueReader extends TickerBehaviour {

		public PeriodicValueReader() {
			super(null, 10000);
		}

		@Override
		protected void onTick() {
			GetValue gv = new GetValue();
			Action actExpr = new Action(provider, gv);
			ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
			request.addReceiver(provider);
			request.setOntology(ValueManagementOntology.getInstance().getName());
			request.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
			try {
				myAgent.getContentManager().fillContent(request, actExpr);
				ACLMessage inform = FIPAService.doFipaRequestClient(myAgent, request);
				System.out.println("Agent "+myAgent.getLocalName()+" - Retrieved value = "+inform.getContent());
			}
			catch (Exception e) {
				System.out.println("Agent "+myAgent.getLocalName()+" - Error retrieving value from value provider agent");
				e.printStackTrace();
			}
		}		
	}
}
