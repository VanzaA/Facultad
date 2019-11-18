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

package jade.core;

import jade.imtp.leap.JICP.JICPProtocol;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Iterator;
import jade.util.leap.Properties;
import jade.util.Logger;
import jade.security.JADESecurityException;
//#MIDP_EXCLUDE_BEGIN
import jade.core.behaviours.Behaviour;
import jade.core.event.ContainerEvent;
import jade.core.event.JADEEvent;
import jade.domain.FIPANames;
import jade.security.*;
//#MIDP_EXCLUDE_END

//#J2ME_EXCLUDE_BEGIN
import jade.util.ObjectManager;
//#J2ME_EXCLUDE_END

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;

/**
 @author Giovanni Caire - TILAB
 @author Jerome Picault - Motorola Labs
 */

class FrontEndContainer implements FrontEnd, AgentToolkit, Runnable {	
	Logger logger = Logger.getMyLogger(this.getClass().getName());
	
	// The table of local agents
	private Hashtable localAgents = new Hashtable(1);
	
	// The table of locally installed services
	private Hashtable localServices = new Hashtable(1);
	
	//#MIDP_EXCLUDE_BEGIN
	// The list of FELister
	private Vector feListeners = new Vector();
	//#MIDP_EXCLUDE_END
	
	// The ID of this container
	private ContainerID myId;
		
	// The addresses of the platform this container belongs to
	Vector platformAddresses;
	
	// The AID of the AMS
	private AID amsAID;
	
	// The AID of the default DF
	private AID dfAID;
	
	// The buffer of messages to be sent to the BackEnd
	private Vector pending;
	
	// The list of agents that have pending messages waiting to be delivered
	// This is used to delay the termination of an agent in case it has pending messages
	private Vector senderAgents;
	
	// The BackEnd this FrontEndContainer is connected to
	private BackEndWrapper myBackEnd;
	
	// The configuration properties for this FrontEndContainer
	private Properties configProperties;
	
	// Flag indicating that the shutdown procedure is in place
	private boolean exiting = false;
	// Flag indicating that the startup procedure is in place
	private boolean starting = true;
	
	/**
	 * Start this FrontEndContainer creating agents and services and connecting to the BackEnd.
	 */
	void start(Properties p) {
		configProperties = p;
		
		// Create all agents without starting them
		String agents = configProperties.getProperty(MicroRuntime.AGENTS_KEY);
		try {
			Vector specs = Specifier.parseSpecifierList(agents);
			Vector successfulAgents = new Vector();
			for (Enumeration en=specs.elements(); en.hasMoreElements(); ) {
				Specifier s = (Specifier) en.nextElement();
				try {
					localAgents.put(s.getName(), initAgentInstance(s.getName(), s.getClassName(), s.getArgs()));
					successfulAgents.addElement(s);
				}
				catch (Throwable t) {
					logger.log(Logger.SEVERE, "Exception creating agent "+t);
				}
			}
			configProperties.setProperty(MicroRuntime.AGENTS_KEY, Specifier.encodeSpecifierList(successfulAgents));
		}
		catch(Exception e1){
			configProperties.setProperty(MicroRuntime.AGENTS_KEY, null);
			logger.log(Logger.SEVERE, "Exception parsing agent specifiers "+e1);
			e1.printStackTrace();
		}
		
		// Install services if any
		String feServices = configProperties.getProperty(MicroRuntime.SERVICES_KEY);
		String beServices = null;
		Vector svcClasses = Specifier.parseList(feServices, ';');
		for (Enumeration en=svcClasses.elements(); en.hasMoreElements(); ) {
			String serviceClassName = (String) en.nextElement();
			try {
				FEService svc = (FEService) Class.forName(serviceClassName).newInstance();
				localServices.put(svc.getName(), svc);
				beServices = (beServices != null ? beServices+';'+svc.getBEServiceClassName() : svc.getBEServiceClassName());
			}
			catch (Throwable t) {
				logger.log(Logger.SEVERE, "Exception creating service "+t);
			}
		}
		// Store the list of services to be loaded on the back-end
		if (beServices != null) {
			configProperties.setProperty(MicroRuntime.BE_REQUIRED_SERVICES_KEY, beServices);
		}
		
		manageProtoOption(configProperties);
		
		// Connect to the BackEnd
		try {
			myBackEnd = new BackEndWrapper(this, configProperties);

			String startupTag = System.getProperty("startup-tag");
			if (startupTag != null) {
				// This line print in the standard output a tag used from the controller to check if container is started 
				System.out.println(startupTag + " " + myId);
			}

			logger.log(Logger.INFO, "--------------------------------------\nAgent container " + myId.getName() + " is ready.\n--------------------------------------------");
		}
		catch (IMTPException imtpe) {
			logger.log(Logger.SEVERE,"IMTP error "+imtpe);
			imtpe.printStackTrace();
			MicroRuntime.handleTermination(true);
			return;
		}
		catch (Exception e) {
			logger.log(Logger.SEVERE,"Unexpected error "+e);
			e.printStackTrace();
			MicroRuntime.handleTermination(true);
			return;
		}
		
		// Connect installed services with the BackEnd
		for (Enumeration en=localServices.elements(); en.hasMoreElements(); ) {
			FEService svc = (FEService) en.nextElement();
			svc.init(myBackEnd);
		}
			
		// Start all agents that have been successfully accepted by the main.
		// NOTE that after the BackEnd creation, each agent-specifier takes the form
		// <original-name>:<str1>[(str2)] 
		// where if str2 is NOT present --> the agent was accepted by the main
		// and str1 represents the actual agent name (with wild cards, if any, properly 
		// replaced), else there was an exception and str1 is the exception class name 
		// and str2 is the exception message.
		agents = configProperties.getProperty(MicroRuntime.AGENTS_KEY);
		try{
			Vector specs = Specifier.parseSpecifierList(agents);
			// Start all agents  
			Enumeration e = specs.elements();
			while (e.hasMoreElements()) {
				Specifier sp = (Specifier) e.nextElement();
				Agent a = (Agent) localAgents.remove(sp.getName());
				if(a != null){
					Object[] args = sp.getArgs();
					if((args != null) && args.length >0){
						//there was an exception notifying the main...
						logger.log(Logger.SEVERE, "Error starting agent " + sp.getName() + ". " + sp.getClassName() + " " + args[0]);
					}else{
						String actualName = sp.getClassName();
						activateAgent(actualName, a);
					}
				}else{
					logger.log(Logger.WARNING, "Agent " + sp.getName() + " not found locally.");
				}
			}
		}
		catch (Exception e1) {
			logger.log(Logger.SEVERE,"Exception parsing agent specifiers "+e1);
			e1.printStackTrace();
		}
		//clear the agent specifiers to avoid sending them again to the back end  
		//during BE re-creations. 
		configProperties.remove(MicroRuntime.AGENTS_KEY);
		
		notifyStarted();
	}
	
	private void manageProtoOption(Properties pp) {
		String proto = pp.getProperty(MicroRuntime.PROTO_KEY);
		if (proto != null) {
			// This option assumes the single-connection approach in case of SOCKET and SSL and usage of NIO back-end side 
			if (CaseInsensitiveString.equalsIgnoreCase(MicroRuntime.SOCKET_PROTOCOL, proto)) {
				pp.setProperty(MicroRuntime.CONN_MGR_CLASS_KEY, "jade.imtp.leap.JICP.FrontEndDispatcher");
			}
			else if (CaseInsensitiveString.equalsIgnoreCase(MicroRuntime.SSL_PROTOCOL, proto)) {
				pp.setProperty(MicroRuntime.CONN_MGR_CLASS_KEY, "jade.imtp.leap.JICP.FrontEndSDispatcher");
			}
			else if (CaseInsensitiveString.equalsIgnoreCase(MicroRuntime.HTTP_PROTOCOL, proto)) {
				pp.setProperty(MicroRuntime.CONN_MGR_CLASS_KEY, "jade.imtp.leap.http.HTTPFEDispatcher");
				pp.setProperty(JICPProtocol.MEDIATOR_CLASS_KEY, "jade.imtp.leap.nio.NIOHTTPBEDispatcher");
			}
			else if (CaseInsensitiveString.equalsIgnoreCase(MicroRuntime.HTTPS_PROTOCOL, proto)) {
				pp.setProperty(MicroRuntime.CONN_MGR_CLASS_KEY, "jade.imtp.leap.http.HTTPFESDispatcher");
				pp.setProperty(JICPProtocol.MEDIATOR_CLASS_KEY, "jade.imtp.leap.nio.NIOHTTPBEDispatcher");
			}
		}
	}
	
	void detach() {
		myBackEnd.detach();
	}
	
	//#MIDP_EXCLUDE_BEGIN
	void addListener(FEListener l) {
		if (!feListeners.contains(l)) {
			feListeners.add(l);
		}
	}
	
	void removeListener(FEListener l) {
		feListeners.remove(l);
	}
		
	private void notifyListeners(JADEEvent ev) {
		synchronized (feListeners) {
			for (int i = 0; i < feListeners.size(); ++i) {
				FEListener l = (FEListener) feListeners.get(i);
				l.handleEvent(ev);
			}
		}
	}
	//#MIDP_EXCLUDE_END
	
	/**
	 * Request the FrontEnd to return a local agent reference by his local name
	 */
	final Agent getLocalAgent(String localName) {
		return (Agent) localAgents.get(localName);
	}
	
	/////////////////////////////////////
	// FrontEnd interface implementation
	/////////////////////////////////////
	/**
	 Request the FrontEnd container to create a new agent.
	 @param name The name of the new agent.
	 @param className The class of the new agent.
	 @param args The arguments to be passed to the new agent.
	 */
	public final void createAgent(String name, String className, String[] args) throws IMTPException {
		try {
			Agent a = initAgentInstance(name, className, (Object[]) args);
			String newName = myBackEnd.bornAgent(name);
			activateAgent(newName, a);
		}
		catch (Exception e) {
			String msg = "Exception creating new agent. ";
			logger.log(Logger.SEVERE,msg+e);
			throw new IMTPException(msg, e);
		}
	}
	
	/**
	 Request the FrontEnd container to create a new agent.
	 @param name The name of the new agent.
	 @param className The class of the new agent.
	 @param args The arguments to be passed to the new agent.
	 */
	public final void createAgent(String name, String className, Object[] args) throws IMTPException {
		try {
			Agent a = initAgentInstance(name, className, args);
			String newName = myBackEnd.bornAgent(name);
			activateAgent(newName, a);
		}
		catch (Exception e) {
			String msg = "Exception creating new agent. ";
			logger.log(Logger.SEVERE,msg+e);
			throw new IMTPException(msg, e);
		}
	}

	/**
	 Request the FrontEnd container to kill an agent.
	 @param name The name of the agent to kill.
	 */
	public final void killAgent(String name) throws NotFoundException, IMTPException {
		waitUntilStarted();
		Agent agent = (Agent) localAgents.get(name);
		if(agent == null) {
			System.out.println("FrontEndContainer killing: " + name + " NOT FOUND");
			throw new NotFoundException("KillAgent failed to find " + name);
		}
		
		// Note that the agent will be removed from the local table in 
		// the handleEnd() method.
		agent.doDelete();
	}
	
	/**
	 Request the FrontEnd container to suspend an agent.
	 @param name The name of the agent to suspend.
	 */
	public final void suspendAgent(String name) throws NotFoundException, IMTPException {
		waitUntilStarted();
		Agent agent = (Agent) localAgents.get(name);
		if(agent == null) {
			throw new NotFoundException("SuspendAgent failed to find " + name);
		}
		agent.doSuspend();
	}
	
	/**
	 Request the FrontEnd container to resume an agent.
	 @param name The name of the agent to resume.
	 */
	public final void resumeAgent(String name) throws NotFoundException, IMTPException {
		waitUntilStarted();
		Agent agent = (Agent) localAgents.get(name);
		if(agent == null) {
			throw new NotFoundException("ResumeAgent failed to find " + name);
		}
		agent.doActivate();
	}
	
	/**
	 Pass an ACLMessage to the FrontEnd for posting.
	 @param msg The message to be posted.
	 @param sender The name of the receiver agent.
	 */
	public final void messageIn(ACLMessage msg, String receiver) throws NotFoundException, IMTPException {
		waitUntilStarted();
		if (receiver != null) {
			Agent agent = (Agent) localAgents.get(receiver);
			if(agent == null) {
				throw new NotFoundException("Receiver "+receiver+" not found");
			}
			agent.postMessage(msg);
		}
	}
	
	/**
	 Request the FrontEnd container to exit.
	 */
	public final void exit(boolean self) throws IMTPException {
		if (!exiting) {
			waitUntilStarted();
			
			exiting = true;
			logger.log(Logger.INFO,"Container shut down activated");
			
			// Kill all agents. We first get a snapshot of all agents and then scan it to kill them.
			// This is to avoid deadlock with handleEnd() that calls localAgents.remove()
			Vector toBeKilled = new Vector();
			synchronized (localAgents) {
				Enumeration e = localAgents.elements();
				while (e.hasMoreElements()) {
					toBeKilled.addElement(e.nextElement());
				}
			}
			Enumeration e = toBeKilled.elements();
			while (e.hasMoreElements()) {
				// Kill agent and wait for its termination
				Agent a = (Agent) e.nextElement();
				a.doDelete();
				a.join();
				a.resetToolkit();
			}
			localAgents.clear();
			logger.log(Logger.FINE,"Local agents terminated");
			
			// Shut down the connection with the BackEnd. The BackEnd will 
			// exit and deregister with the main
			myBackEnd.detach();
			logger.log(Logger.FINE,"Connection manager closed");
			
			// Notify the JADE Runtime that the container has terminated execution
			MicroRuntime.handleTermination(self);
			
			// Stop the TimerDispatcher if it was activated
			TimerDispatcher.getTimerDispatcher().stop();
		}
	}
	
	/**
	 Request the FrontEnd container to synch.
	 */
	public final void synch() throws IMTPException {
		synchronized (localAgents) {
			Enumeration e = localAgents.keys();
			while (e.hasMoreElements()) {
				String name = (String) e.nextElement();
				logger.log(Logger.INFO,"Resynching agent "+name);
				try {
					// Notify the BackEnd (note that the agent name will never change in this case)
					myBackEnd.bornAgent(name);
				}
				catch (IMTPException imtpe) {
					// The connection is likely down again. Rethrow the exception
					// to make the BE repeat the synchronization process
					logger.log(Logger.WARNING,"IMTPException resynching. "+imtpe);
					throw imtpe;					
				}
				catch (Exception ex) {
					logger.log(Logger.SEVERE,"Exception resynching agent "+name+". "+ex);
					ex.printStackTrace();
					// An agent with the same name has come up in the meanwhile.
					// FIXME: Kill the agent or notify a warning
				}
			}
		}
	}
	
	/**
	 * It may happen that a message for a bootstrap agent using wildcards in its name is received before 
	 * the actual agent name is assigned --> This method is called in messageIn() and other agent-related 
	 * methods to avoid that. 
	 */
	private synchronized void waitUntilStarted() {
		try {
			while (starting) {
				wait();
			}
		}
		catch (Exception e) {}
	}
	
	private synchronized void notifyStarted() {
		starting = false;
		notifyAll();
	}
	
	/////////////////////////////////////
	// AgentToolkit interface implementation
	/////////////////////////////////////
	//#MIDP_EXCLUDE_BEGIN
	public jade.wrapper.AgentContainer getContainerController(JADEPrincipal principal, Credentials credentials){
		return null;
	}
	//#MIDP_EXCLUDE_END
	
	public final Location here() {
		return myId;
	}
	
	public final void handleEnd(AID agentID) {
		String name = agentID.getLocalName();
		// Wait for messages (if any) sent by this agent to be transmitted
		if (pending != null) {
			synchronized (pending) {
				while (senderAgents.contains(name)) {
					try {
						pending.wait();
					}
					catch (Exception e) {}
				}
			}
		}
		
		//#MIDP_EXCLUDE_BEGIN
		// NOTIFY DEAD AGENT
		notifyListeners(new ContainerEvent(ContainerEvent.DEAD_AGENT, agentID, myId));
		//#MIDP_EXCLUDE_END
		
		if (!exiting) {
			// If this agent is ending because the container is exiting
			// just do nothing. The BackEnd will notify the main.
			try {
				localAgents.remove(name);
				myBackEnd.deadAgent(name);
				
				// If there are no more agents and the exitwhenempty option 
				// is set, activate shutdown
				if ("true".equals(configProperties.getProperty("exitwhenempty"))) {
					if (localAgents.isEmpty()) {
						exit(true);
					}
				}
			}
			catch(IMTPException re) {
				logger.log(Logger.SEVERE,re.toString());
			}
		}
	}
	
	public final void handleChangedAgentState(AID agentID, int from, int to) {
		// FIXME: This should call myBackEnd.suspendedAgent()/resumedAgent()
	}
	
	// Note that the needClone argument is ignored since the
	// FrontEnd must always clone
	public final void handleSend(ACLMessage msg, AID sender, boolean needClone) {
		Iterator it = msg.getAllIntendedReceiver();
		// If some receiver is local --> directly post the message
		boolean hasRemoteReceivers = false;
		while (it.hasNext()) {
			AID id = (AID) it.next();
			Agent a = (Agent) localAgents.get(id.getLocalName());
			if (a != null) {
				ACLMessage m = (ACLMessage) msg.clone();
				a.postMessage(m);
			}
			else {
				hasRemoteReceivers = true;
			}
		}
		// If some receiver is remote --> pass the message to the BackEnd
		// Do not change the list of receiver. If some receiver was local the BackEnd
		// will not deliver the message back (see BackEndContainer.postMessageMessageToLocalAgent())
		if (hasRemoteReceivers) {
			post(msg, sender.getLocalName());
		}
	}
	
	public final void setPlatformAddresses(AID id) {
		id.clearAllAddresses();
		for (int i = 0; i < platformAddresses.size(); ++i) {
			id.addAddresses((String)platformAddresses.elementAt(i));
		}
	}
	
	public final AID getAMS() {
		return amsAID;
	}
	
	public final AID getDefaultDF() {
		return dfAID;
	}
	
	public String getProperty(String key, String aDefault) {
		String ret = configProperties.getProperty(key);
		return (ret != null ? ret : aDefault); 
	}
	
	//#MIDP_EXCLUDE_BEGIN
	public Properties getBootProperties(){
		/* TODO To be completed */
		return null;
	}
	
	public void handleMove(AID agentID, Location where) throws JADESecurityException, IMTPException, NotFoundException {
	}
	
	public void handleClone(AID agentID, Location where, String newName) throws JADESecurityException, IMTPException, NotFoundException {
	}
	
	public void handleSave(AID agentID, String repository) throws ServiceException, NotFoundException, IMTPException {
	}
	
	public void handleReload(AID agentID, String repository) throws ServiceException, NotFoundException, IMTPException {
	}
	
	public void handleFreeze(AID agentID, String repository, ContainerID bufferContainer) throws ServiceException, NotFoundException, IMTPException {
	}
	
	public void handlePosted(AID agentID, ACLMessage msg) {
	}
	
	public void handleReceived(AID agentID, ACLMessage msg) {
	}
	
	public void handleBehaviourAdded(AID agentID, Behaviour b) {
	}
	
	public void handleBehaviourRemoved(AID agentID, Behaviour b) {
	}
	
	public void handleChangeBehaviourState(AID agentID, Behaviour b, String from, String to) {
	}
	//#MIDP_EXCLUDE_END
	
	public ServiceHelper getHelper(Agent a, String serviceName) throws ServiceException {
		FEService svc = (FEService) localServices.get(serviceName);
		if (svc != null) {
			return svc.getHelper(a);
		}
		else {
			throw new ServiceNotActiveException(serviceName);
		}
	}
	
	
	///////////////////////////////
	// Private methods
	///////////////////////////////
	final void initInfo(Properties pp) {
		myId = new ContainerID(pp.getProperty(MicroRuntime.CONTAINER_NAME_KEY), null);
		AID.setPlatformID(pp.getProperty(MicroRuntime.PLATFORM_KEY));
		platformAddresses = Specifier.parseList(pp.getProperty(MicroRuntime.PLATFORM_ADDRESSES_KEY), ';');
		amsAID = new AID("ams", AID.ISLOCALNAME);
		setPlatformAddresses(amsAID);
		dfAID = new AID("df", AID.ISLOCALNAME);
		setPlatformAddresses(dfAID);
	}
	
	
	private final Agent initAgentInstance(String name, String className, Object[] args) throws Exception {
		Agent agent = null;
		//#J2ME_EXCLUDE_BEGIN
		agent = (Agent) ObjectManager.load(className, ObjectManager.AGENT_TYPE);
		//#J2ME_EXCLUDE_END
		if (agent == null) {
			agent = (Agent) Class.forName(className).newInstance();
		}
		agent.setArguments(args);
		agent.setToolkit(this);
		//#MIDP_EXCLUDE_BEGIN
		agent.initMessageQueue();
		//#MIDP_EXCLUDE_END
		return agent;
	}
	
	private void activateAgent(String name, Agent a) {
		localAgents.put(name, a);
		AID id = new AID(name, AID.ISLOCALNAME);
		
		//#MIDP_EXCLUDE_BEGIN
		// NOTIFY BORN AGENT once the new agent has been created, inserted in the localTable and notified to the Main, but not yet started
		notifyListeners(new ContainerEvent(ContainerEvent.BORN_AGENT, id, myId));
		//#MIDP_EXCLUDE_END
		
		a.powerUp(id, new Thread(a));
	}
	
	
	private void post(ACLMessage msg, String sender) {
		if (pending == null) {
			// Lazily create the vector of pending messages, the Thread 
			// for asynchronous message delivery and the vector of senderAgents
			pending = new Vector(4);
			senderAgents = new Vector(1);
			Thread t = new Thread(this);
			t.start();
		}
		
		synchronized(pending) {
			if (!senderAgents.contains(sender)) {
				senderAgents.addElement(sender);
			}	
			pending.addElement(msg.clone());
			pending.addElement(sender);
			int size = pending.size();
			if (size > 100 && size < 110) {
				logger.log(Logger.INFO,size+" pending messages");
			}
			pending.notifyAll();
		}
	}
	
	public void run() {
		ACLMessage msg = null;
		String sender = null;
		
		while (true) {
			synchronized(pending) {
				while (pending.size() == 0) {
					try {
						pending.wait();
					}
					catch (InterruptedException ie) {
						// Should never happen
						logger.log(Logger.SEVERE,ie.toString());
					}
				}
				msg = (ACLMessage) pending.elementAt(0);
				sender = (String) pending.elementAt(1);
				pending.removeElementAt(1);
				pending.removeElementAt(0);
			}
			
			try {
				myBackEnd.messageOut(msg, sender);
			}
			catch (Exception e) {
				// This may only happen if the store-and-forward mechanism is disabled 
				// (note that "NotFound" here is referred to the sender).
				logger.log(Logger.SEVERE, "Error delivering message.", e);
				
				// Send back a failure message
				MicroRuntime.notifyFailureToSender(msg, sender, e.getMessage());
			}
			
			// Notify terminating agents (if any) waiting for their messages to be delivered
			synchronized (pending) {
				if (!pending.contains(sender)) {
					// No more pending messages from this agent
					senderAgents.removeElement(sender);
					pending.notifyAll();
				}
			}
		}
	}  
}

