package examples.replication;

import jade.content.lang.sl.SLCodec;
import jade.core.AID;
import jade.core.Agent;
import jade.core.ContainerID;
import jade.core.Location;
import jade.core.ServiceException;
import jade.core.behaviours.OntologyServer;
import jade.core.replication.AgentReplicationHandle;
import jade.core.replication.AgentReplicationHelper;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

/**
 * Simple agent that shows how to exploit the features of the AgentReplicationService
 */
public class ValueProviderAgent extends Agent implements AgentReplicationHelper.Listener {
	
	private transient ValueProviderAgentGui myGui;
	private int myValue = 0;
	
	@Override
	protected void setup() {
		try {
			// Makes this agent become the master replica of a newly defined replicated agent
			AgentReplicationHelper helper = (AgentReplicationHelper) getHelper(AgentReplicationHelper.SERVICE_NAME);
			AID virtualAid = helper.makeVirtual(getLocalName()+"_V", AgentReplicationHelper.HOT_REPLICATION);

			// Register to the DF. 
			// NOTE: we use the virtual agent AID (not the concrete agent AID).
			// In this way requests from remote agents will be automatically spread across 
			// all replicas to achieve load balancing and fault tolerance.
			DFAgentDescription dfad = new DFAgentDescription();
			dfad.setName(virtualAid);
			ServiceDescription sd = new ServiceDescription();
			sd.setType("ValueProvider");
			sd.setName("VirtualValueProvider");
			dfad.addServices(sd);
			DFService.register(this, dfad);
			
			// Register required ontologies and language codecs
			getContentManager().registerLanguage(new SLCodec());
			getContentManager().registerOntology(ValueManagementOntology.getInstance());

			// Add the behaviour serving requests to read our current value
			addBehaviour(new OntologyServer(this, ValueManagementOntology.getInstance(), ACLMessage.REQUEST, this));
			
			// Show the GUI that allows the user to set the value and to create other replicas
			myGui = new ValueProviderAgentGui(this, myValue);
			myGui.setVisible(true);
		}
		catch (ServiceException se) {
			System.out.println("Agent "+getLocalName()+" - Error retrieving AgentReplicationHelper!!! Check that the AgentReplicationService is correctly installed in this container");
			se.printStackTrace();
			doDelete();
		}
		catch (FIPAException fe) {
			System.out.println("Agent "+getLocalName()+" - Error registering with the DF");
			fe.printStackTrace();
			doDelete();
		}
	}
	
	@Override
	protected void takeDown() {
		// Close the GUI (if present) when the agent terminates
		if (myGui != null) {
			myGui.dispose();
		}
	}
	
	@Override
	public void afterClone() {
		// New replicas are created cloning the master replica.
		// Just after cloning restore transient field such as registered ontologies and language codecs
		System.out.println("Agent "+getLocalName()+" - Alive");
		getContentManager().registerLanguage(new SLCodec());
		getContentManager().registerOntology(ValueManagementOntology.getInstance());
	}
	
	////////////////////////////////////////
	// Methods invoked by the GUI
	////////////////////////////////////////
	
	// This is invoked whenever the user selects a new value by moving the slider in the GUI
	public void setValue(int newValue) {
		// The call to setValue() will be invoked on other replicas too 
		AgentReplicationHandle.replicate(this, "setValue", new Object[]{newValue});
		
		myValue = newValue;
		System.out.println("Agent "+getLocalName()+": VALUE = "+myValue);
	}
	
	// This is invoked when the user clicks on the Create Replica button in the GUI
	void createReplica(String replicaName, String where) {
		if (replicaName == null || replicaName.trim().length() == 0) {
			System.out.println("Replica name not specified");
			return;
		}
		if (where == null || where.trim().length() == 0) {
			System.out.println("Replica location not specified");
			return;
		}
		try {
			AgentReplicationHelper helper = (AgentReplicationHelper) getHelper(AgentReplicationHelper.SERVICE_NAME);
			helper.createReplica(replicaName.trim(), new ContainerID(where.trim(), null));
		}
		catch (Exception e) {
			System.out.println("Agent "+getLocalName()+" - Error creating replica on container "+where);
			e.printStackTrace();
		}
	}
	
	
	/////////////////////////////////////////////////////////////////
	// Methods invoked when REQUESTS from other agents are received
	/////////////////////////////////////////////////////////////////
	
	public void serveGetValueRequest(GetValue gv, ACLMessage request) {
		int currentValue = myValue;
		System.out.println("Agent "+getLocalName()+" - Serving GetValue request. Current value is "+currentValue);
		ACLMessage reply = request.createReply();
		reply.setPerformative(ACLMessage.INFORM);
		reply.setContent(String.valueOf(currentValue));
		send(reply);
	}


	/////////////////////////////////////////////////////////////////
	// Methods of the AgentReplicationHelper.Listener interface
	/////////////////////////////////////////////////////////////////
	
	@Override
	public void replicaAdded(AID replicaAid, Location where) {
		System.out.println("Agent "+getLocalName()+" - New replica "+replicaAid.getLocalName()+" successfully added in "+where.getName());
	}

	@Override
	public void replicaRemoved(AID replicaAid, Location where) {
		System.out.println("Agent "+getLocalName()+" - Replica "+replicaAid.getLocalName()+" removed");
	}

	@Override
	public void replicaCreationFailed(AID replicaAid, Location where) {
		System.out.println("Agent "+getLocalName()+" - Creation of new replica "+replicaAid.getLocalName()+" in "+where.getName()+" failed");
	}

	@Override
	public void becomeMaster() {
		// The old master replica is dead. I'm the new master replica --> Show the GUI
		System.out.println("Agent "+getLocalName()+" - I'm the new master replica");
		myGui = new ValueProviderAgentGui(this, myValue);
		myGui.setVisible(true);
	}
}
