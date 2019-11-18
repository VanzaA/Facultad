package examples.inprocess;

import java.util.Vector;

import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;
import jade.util.leap.List;
import jade.util.leap.Properties;

import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import jade.content.onto.basic.Result;
import jade.core.AID;
import jade.core.AgentContainer;
import jade.core.ContainerID;
import jade.core.Profile;
import jade.domain.FIPANames;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.QueryAgentsOnLocation;
import jade.wrapper.gateway.JadeGateway;
/**
 * This class provides a simple example of a NON-jade program that connects to a running JADE-based system
 * using the JadeGateway class.
 * In order to try this example 
 * <ol>
 * <li>Be sure the JADE examples are compiled</li>
 * <li>Be sure the JADE libraries (jade/lib/jade.jar) and the JADE examples classes (jade/classes)
 * are in the classpath</li>
 * <li>Open a shell and start a JADE main container typing <code>java jade.Boot -gui</code>. This represents the 
 * running JADE-based system we have to connect to</li>
 * <li>Open another shell and start this example typing <code>java examples.inprocess.JadeGatewayExample</code>.</li>  
 * </ol>
 */
public class JadeGatewayExample {

	public static void main(String[] args) {
		// Initialize the JadeGateway to connect to the running JADE-based system.
		// In this example we assume the Main Container of the JADE-based system we want to connect 
		// to is running in the local host and is using the default port 1099 
		Properties pp = new Properties();
		pp.setProperty(Profile.MAIN_HOST, "localhost");
		pp.setProperty(Profile.MAIN_PORT, "1099");
		JadeGateway.init(null, pp);
		

		// Now retrieve all agents active in the Main Container by running (through the JadeGateway)
		// a behaviour that requests that information to the AMS. This behaviour will be executed 
		// by the Gateway Agent inside the JadeGateway. As soon as the execution completes the execute() method 
		// will return.
		try {
			MainContainerAgentsRetriever retriever = new MainContainerAgentsRetriever();
			JadeGateway.execute(retriever);
			// At this point the retriever behaviour has been fully executed --> the list of 
			// agents running in the Main Container is available: get it and print it
			List agents = retriever.getAgents();
			if (agents != null) {
				System.out.println("Agents living in the Main Container: ");
				for (int i = 0; i < agents.size(); ++i) {
					System.out.println("- "+ ((AID) agents.get(i)).getLocalName());
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		System.exit(0);
	}
	
	
	/**
	 * Inner class MainContainerAgentsRetriever.
	 * This behaviour requests the AMS the list of agents running in the Main Container
	 */
	private static class MainContainerAgentsRetriever extends AchieveREInitiator {

		private List agents;
		
		public MainContainerAgentsRetriever() {
			super(null, null);
		}
		
		public List getAgents() {
			return agents;
		}
		
		public void onStart() {
			super.onStart();
			
			// Be sure the JADEManagementOntology and the Codec for the SL language are 
			// registered in the Gateway Agent 
			myAgent.getContentManager().registerLanguage(new SLCodec());
			myAgent.getContentManager().registerOntology(JADEManagementOntology.getInstance());
		}
		
		@Override
		protected Vector prepareRequests(ACLMessage initialMsg) {
			Vector v = null;
			
			// Prepare the request to be sent to the AMS
			ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
			request.addReceiver(myAgent.getAMS());
			request.setOntology(JADEManagementOntology.getInstance().getName());
			request.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
			
			QueryAgentsOnLocation qaol = new QueryAgentsOnLocation();
			qaol.setLocation(new ContainerID(AgentContainer.MAIN_CONTAINER_NAME, null));
			Action actExpr = new Action(myAgent.getAMS(), qaol);
			try {
				myAgent.getContentManager().fillContent(request, actExpr);
				v = new Vector(1);
				v.add(request);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			return v;
		}
		
		@Override
		protected void handleInform(ACLMessage inform) {
			try {
				// Get the result from the AMS, parse it and store the list of agents 
				Result result = (Result) myAgent.getContentManager().extractContent(inform);
				agents = (List) result.getValue();
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
}
