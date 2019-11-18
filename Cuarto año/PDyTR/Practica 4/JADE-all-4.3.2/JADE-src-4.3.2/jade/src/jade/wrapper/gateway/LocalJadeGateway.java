package jade.wrapper.gateway;

//#J2ME_EXCLUDE_FILE
//#ANDROID_EXCLUDE_FILE

import jade.core.Agent;
import jade.core.ProfileImpl;

public class LocalJadeGateway extends DynamicJadeGateway {

	public LocalJadeGateway(Agent a) {
		myContainer = a.getContainerController();
		profile = new ProfileImpl(a.getBootProperties()); 
	}

	public final void init(String agentName, String agentClassName, Object[] agentArgs) {
		super.init(agentName, agentClassName, agentArgs, null);
	}
	public final void init(String agentClassName, Object[] agentArgs) {
		super.init(agentClassName, agentArgs, null);
	}

	public final void init(String agentClassName) {
		super.init(agentClassName, null);
	}
	
	public final void shutdown() {
		try {
			if (myAgent != null)
				myAgent.kill();
		} catch (Exception e) {
		}
		myAgent = null;
	}
}
