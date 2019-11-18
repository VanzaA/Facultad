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

//#APIDOC_EXCLUDE_FILE

import jade.util.leap.Iterator;
import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Properties;

import jade.util.Logger;

import jade.lang.acl.ACLMessage;
import jade.core.behaviours.Behaviour;

import jade.core.messaging.GenericMessage;
import jade.core.management.AgentManagementSlice;

//#MIDP_EXCLUDE_BEGIN
import jade.domain.AMSEventQueueFeeder;
//#MIDP_EXCLUDE_END
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.InternalError;
import jade.domain.JADEAgentManagement.JADEManagementOntology;

import jade.mtp.MTPDescriptor;
import jade.mtp.TransportAddress;

import jade.security.JADESecurityException;
import jade.security.CredentialsHelper;
import jade.security.JADEPrincipal;
import jade.security.Credentials;


/**
 This class is a concrete implementation of the JADE agent
 container, providing runtime support to JADE agents.

 This class cannot be instantiated from applications. Instead, the
 <code>Runtime.createAgentContainer(Profile p)</code> method must be called.

 @see Runtime#createAgentContainer(Profile)

 @author Giovanni Rimassa - Universita' di Parma
 @author Jerome Picault - Motorola Labs
 @author Giovanni Caire - TILAB
 @version $Date: 2013-05-22 12:12:53 +0200 (mer, 22 mag 2013) $ $Revision: 6674 $

 */
class AgentContainerImpl implements AgentContainer, AgentToolkit {

	public static final String ENABLE_MONITOR = "jade_core_AgentContainerImpl_enablemonitor";
	public static final String MONITOR_AGENT_NAME = "monitor-%C";
	public static final String MONITOR_AGENT_CLASS = "jade.core.ContainerMonitorAgent";

	private Logger myLogger = Logger.getMyLogger(this.getClass().getName());

	// Local agents, indexed by agent name
	protected LADT localAgents;

	// The Profile defining the configuration of this Container
	protected Profile myProfile;

	// The Command Processor through which all the vertical commands in this container will pass
	protected CommandProcessor myCommandProcessor;

	//#MIDP_EXCLUDE_BEGIN
	// The agent platform this container belongs to
	protected MainContainerImpl myMainContainer; // FIXME: It should go away
	//#MIDP_EXCLUDE_END

	//#J2ME_EXCLUDE_BEGIN
	// The listener for multicast main detecton
	private MulticastMainDetectionListener mainDetectionListener;
	//#J2ME_EXCLUDE_END

	// The IMTP manager, used to access IMTP-dependent functionalities
	protected IMTPManager myIMTPManager;

	// The platform Service Manager
	private ServiceManager myServiceManager;

	// The platform Service Finder
	private ServiceFinder myServiceFinder;

	// The Object managing Thread resources in this container
	private ResourceManager myResourceManager;

	protected ContainerID myID;
	
	protected NodeDescriptor myNodeDescriptor;

	// These are only used at bootstrap-time to initialize the local
	// NodeDescriptor. Further modifications take no effect
	protected JADEPrincipal ownerPrincipal;
	protected Credentials ownerCredentials;

	private AID theAMS;
	private AID theDefaultDF;

	// This is used to avoid killing this container just after its creation and 
	// possibly before the monitoring PING is received. In that case in fact the Main Container does 
	// not deregister it.
	private long creationTime = -1;

	private boolean joined;

	// Default constructor
	AgentContainerImpl() {
	}

	// Package scoped constructor, so that only the Runtime
	// class can actually create a new Agent Container.
	AgentContainerImpl(Profile p) {
		myProfile = p;
		((ProfileImpl) myProfile).init();
		localAgents = new LADT(16);
	}

	//#MIDP_EXCLUDE_BEGIN
	/////////////////////////////////////////////////
	// Support for the in-process interface section
	/////////////////////////////////////////////////
	jade.wrapper.AgentContainer getContainerController() {
		return getContainerController(myNodeDescriptor.getOwnerPrincipal(), myNodeDescriptor.getOwnerCredentials());
	}

	public jade.wrapper.AgentContainer getContainerController(JADEPrincipal principal, Credentials credentials) {
		return new jade.wrapper.AgentContainer(getContainerProxy(principal, credentials), this, getPlatformID());
	}

	/**
	 Return a proxy that allows making requests to the local container
	 as if they were received from the main. This allows dealing
	 uniformly with local and remote requests.
	 Local requests occurs at bootstrap and following calls to the
	 in-process interface.
	 */
	private jade.wrapper.ContainerProxy getContainerProxy(final JADEPrincipal principal, final Credentials credentials) {
		return new jade.wrapper.ContainerProxy() {
			GenericCommand dummyCmd = new GenericCommand(null, null, null);

			{
				dummyCmd.setPrincipal(principal);
				dummyCmd.setCredentials(credentials);
			}

			public void createAgent(AID id, String className, Object[] args) throws Throwable {
				// Do as if it was a remote call from the main to allow
				// security checks to take place if needed
				AgentManagementSlice target = (AgentManagementSlice) getProxyToLocalSlice(AgentManagementSlice.NAME);
				target.createAgent(id, className, args, principal, null, AgentManagementSlice.CREATE_ONLY, dummyCmd);
			}

			public void killContainer() throws Throwable {
				// Do as if it was a remote call from the main to allow
				// security checks to take place if needed
				AgentManagementSlice target = (AgentManagementSlice) getProxyToLocalSlice(AgentManagementSlice.NAME);
				// FIXME: set Principal and Credentials
				target.exitContainer();
			}

			public MTPDescriptor installMTP(String address, String className) throws Throwable {
				// Do as if it was a remote call from the main to allow
				// security checks to take place if needed
				jade.core.messaging.MessagingSlice target = (jade.core.messaging.MessagingSlice) getProxyToLocalSlice(jade.core.messaging.MessagingSlice.NAME);
				// FIXME: set Principal and Credentials
				return target.installMTP(address, className);
			}

			public void uninstallMTP(String address) throws Throwable {
				// Do as if it was a remote call from the main to allow
				// security checks to take place if needed
				jade.core.messaging.MessagingSlice target = (jade.core.messaging.MessagingSlice) getProxyToLocalSlice(jade.core.messaging.MessagingSlice.NAME);
				// FIXME: set Principal and Credentials
				target.uninstallMTP(address);
			}

			public void suspendAgent(AID id) throws Throwable {
				// Do as if it was a remote call from the main to allow
				// security checks to take place if needed
				jade.core.management.AgentManagementSlice target = (jade.core.management.AgentManagementSlice) getProxyToLocalSlice(jade.core.management.AgentManagementSlice.NAME);
				// FIXME: set Principal and Credentials
				target.changeAgentState(id, Agent.AP_SUSPENDED);
			}

			public void activateAgent(AID id) throws Throwable {
				// Do as if it was a remote call from the main to allow
				// security checks to take place if needed
				jade.core.management.AgentManagementSlice target = (jade.core.management.AgentManagementSlice) getProxyToLocalSlice(jade.core.management.AgentManagementSlice.NAME);
				// FIXME: set Principal and Credentials
				target.changeAgentState(id, Agent.AP_ACTIVE);
			}

			public void killAgent(AID id) throws Throwable {
				// Do as if it was a remote call from the main to allow
				// security checks to take place if needed
				jade.core.management.AgentManagementSlice target = (jade.core.management.AgentManagementSlice) getProxyToLocalSlice(jade.core.management.AgentManagementSlice.NAME);
				target.killAgent(id, dummyCmd);
			}

			public void moveAgent(AID id, Location where) throws Throwable {
				// Do as if it was a remote call from the main to allow
				// security checks to take place if needed
				jade.core.mobility.AgentMobilitySlice target = (jade.core.mobility.AgentMobilitySlice) getProxyToLocalSlice(jade.core.mobility.AgentMobilitySlice.NAME);
				// FIXME: set Principal and Credentials
				target.moveAgent(id, where);
			}

			public void cloneAgent(AID id, Location where, String newName) throws Throwable {
				// Do as if it was a remote call from the main to allow
				// security checks to take place if needed
				jade.core.mobility.AgentMobilitySlice target = (jade.core.mobility.AgentMobilitySlice) getProxyToLocalSlice(jade.core.mobility.AgentMobilitySlice.NAME);
				// FIXME: set Principal and Credentials
				target.copyAgent(id, where, newName);
			}

			private SliceProxy getProxyToLocalSlice(String serviceName) throws Throwable {
				Service svc = myServiceFinder.findService(serviceName);
				return (SliceProxy) myIMTPManager.createSliceProxy(serviceName, svc.getHorizontalInterface(), myIMTPManager.getLocalNode());
			}
		};
	}
	////////////////////////////////////////////////////////
	// END of support for the in-process interface section
	////////////////////////////////////////////////////////
	//#MIDP_EXCLUDE_END

	/**
	 Issue an INFORM_CREATED vertical command.
	 Note that the Principal, if any, is that of the
	 owner of the newly born agent, while the Credentials, if any, are a set
	 additional initial credentials to be attached to the newly born agent. 
	 The SecurityService, if active,
	 will create a new Principal for the newly born agent and will initialize its 
	 credentials as the union of the initial credential and the ownership 
	 certificate.
	 */
	public void initAgent(AID agentID, Agent instance,
			JADEPrincipal ownerPrincipal, Credentials initialCredentials)
	throws NameClashException, IMTPException, NotFoundException, JADESecurityException {

		// Replaces wildcards
		agentID.setName(JADEManagementOntology.adjustAgentName(agentID.getName(), new String[] {myID.getName()}));

		// Setting the AID and toolkit here is redundant, but
		// allows services to retrieve their agent helper correctly
		// when processing the INFORM_CREATED command.
		instance.setAID(agentID);
		instance.setToolkit(this);

		GenericCommand cmd = new GenericCommand(jade.core.management.AgentManagementSlice.INFORM_CREATED, jade.core.management.AgentManagementSlice.NAME, null);
		cmd.addParam(agentID);
		cmd.addParam(instance);
		cmd.addParam( ownerPrincipal );
		cmd.addParam( initialCredentials );

		Object ret = myCommandProcessor.processOutgoing(cmd);
		if (ret != null) {
			if (ret instanceof NameClashException) {
				throw ((NameClashException) ret);
			}
			else if (ret instanceof IMTPException) {
				throw ((IMTPException) ret);
			}
			else if (ret instanceof NotFoundException) {
				throw ((NotFoundException) ret);
			}
			else if (ret instanceof JADESecurityException) {
				throw ((JADESecurityException) ret);
			}
			else if (ret instanceof Throwable) {
				((Throwable) ret).printStackTrace();
				throw new IMTPException("Unexpected error initializing agent "+agentID.getName(), (Throwable) ret);
			}
		}
	}

	public NodeDescriptor getNodeDescriptor() {
		return myNodeDescriptor;
	}

	protected void init() throws IMTPException, ProfileException {
		myCommandProcessor = myProfile.getCommandProcessor();

		//#J2ME_EXCLUDE_BEGIN
		// main-host option takes precendence over dectect-main
		if (myProfile.getBooleanProperty(Profile.DETECT_MAIN, false) && !myProfile.getBooleanProperty(Profile.MAIN_HOST, false)) {
			// FIXME check correctness of cast to ProfileImpl
			MainDetectionManager.detect((ProfileImpl)myProfile);
		}
		//#J2ME_EXCLUDE_END

		try {
			// Create and initialize the IMTPManager
			myIMTPManager = myProfile.getIMTPManager();
			myIMTPManager.initialize(myProfile);
			//#J2ME_EXCLUDE_BEGIN
			mainDetectionListener = null;
			if (myProfile.getBooleanProperty(Profile.DETECT_MAIN, true) && ((ProfileImpl)myProfile).isMain()) {
				try {
					mainDetectionListener = MainDetectionManager.createListener((ProfileImpl)myProfile, myIMTPManager);
				}
				catch (ProfileException pe) {
					if ("true".equalsIgnoreCase(myProfile.getBootProperties().getProperty(Profile.DETECT_MAIN))) {
						// The detect-main option was explicitly set to true in the boot properties --> let the exception through
						throw pe;
					}
					else {
						// The detect-main option was NOT explicitly specified in the boot properties --> just print a warning
						myLogger.log(Logger.WARNING, "Automatic main-detection mechanism initialization failed ("+pe.getMessage()+"). Mechanism disabled!");
					}
				}
			}
			//#J2ME_EXCLUDE_END
		}
		finally {
			if (myProfile.getBooleanProperty(Profile.DUMP_OPTIONS, false)) {
				myLogger.log(Logger.INFO, "Startup options dump:\n"+myProfile);
			}
		}

		// Get the Service Manager and the Service Finder
		myServiceManager = myProfile.getServiceManager();
		myServiceFinder = myProfile.getServiceFinder();

		// Attach CommandProcessor and ServiceManager to the local node
		BaseNode localNode = (BaseNode) myIMTPManager.getLocalNode();
		localNode.setCommandProcessor(myCommandProcessor);
		localNode.setServiceManager(myServiceManager);

		//#MIDP_EXCLUDE_BEGIN
		myMainContainer = myProfile.getMain();
		//#MIDP_EXCLUDE_END

		// This string will be used to build the GUID for every agent on
		// this platform.
		AID.setPlatformID(myServiceManager.getPlatformName());

		// Build the Agent IDs for the AMS and for the Default DF.
		theAMS = new AID(FIPANames.AMS, AID.ISLOCALNAME);
		theDefaultDF = new AID(FIPANames.DEFAULT_DF, AID.ISLOCALNAME);

		// Create the ResourceManager
		myResourceManager = myProfile.getResourceManager();
		myResourceManager.initialize(myProfile);

		// Initialize the Container ID
		TransportAddress addr = (TransportAddress) myIMTPManager.getLocalAddresses().get(0);
		myID = new ContainerID(myProfile.getParameter(Profile.CONTAINER_NAME, PlatformManager.NO_NAME), addr);
		myNodeDescriptor = new NodeDescriptor(myID, myIMTPManager.getLocalNode());
	}

	/**
	 Add the node to the platform with the basic services
	 */
	protected void startNode() throws IMTPException, ProfileException, ServiceException, JADESecurityException, NotFoundException {
		// Initialize all services (without activating them)
		List services = new ArrayList();
		
		initMandatoryServices(services);

		List l = myProfile.getSpecifiers(Profile.SERVICES);
		myProfile.setSpecifiers(Profile.SERVICES, l); // Avoid parsing services twice
		initAdditionalServices(l.iterator(), services);

		// Register with the platform (pass only global services to the Main)
		ServiceDescriptor[] descriptors = new ServiceDescriptor[services.size()];
		for (int i = 0; i < descriptors.length; ++i) {
			descriptors[i] = (ServiceDescriptor) services.get(i);
		}
		// This call performs the real connection to the platform and can modify the 
		// name of this container
		myServiceManager.addNode(myNodeDescriptor, descriptors);
		creationTime = System.currentTimeMillis();

		//#MIDP_EXCLUDE_BEGIN
		// If we are the master main container --> initialize the AMS and DF. Do that before booting all services 
		// since during service boot some messages may be directed to the AMS or DF
		if(myProfile.isMasterMain()) {
			myMainContainer.initSystemAgents(this, false);
		}
		//#MIDP_EXCLUDE_END

		// Once we are connected, boot all services
		bootAllServices(services);

		//#MIDP_EXCLUDE_BEGIN
		// If we are the master main container --> start the AMS and DF.
		if(myProfile.isMasterMain()) {
			myMainContainer.startSystemAgents(this, null);
		}
		//#MIDP_EXCLUDE_END
	}

	void initMandatoryServices(List services) throws ServiceException {
		ServiceDescriptor dsc = startService("jade.core.management.AgentManagementService", false);
		dsc.setMandatory(true);
		services.add(dsc);
		
		//#MIDP_EXCLUDE_BEGIN
		dsc = startService("jade.core.messaging.MessagingService", false);
		//#MIDP_EXCLUDE_END
		/*#MIDP_INCLUDE_BEGIN
		 dsc = startService("jade.core.messaging.LightMessagingService", false);
		 #MIDP_INCLUDE_END*/
		dsc.setMandatory(true);
		services.add(dsc);
		
		//#J2ME_EXCLUDE_BEGIN
		dsc = startService("jade.core.resource.ResourceManagementService", false);
		dsc.setMandatory(true);
		services.add(dsc);
		//#J2ME_EXCLUDE_END
	}
	
	void initAdditionalServices(Iterator serviceSpecifiers, List services) throws ServiceException {
		while(serviceSpecifiers.hasNext()) {
			Specifier s = (Specifier) serviceSpecifiers.next();
			String serviceClass = s.getClassName();
			boolean isMandatory = false;
			if ( s.getArgs() != null ) {
				isMandatory = CaseInsensitiveString.equalsIgnoreCase( (String) s.getArgs()[0], "true" );
			}
			try {
				ServiceDescriptor dsc = startService(serviceClass, false);
				dsc.setMandatory(isMandatory);
				services.add(dsc);
			} 
			catch (ServiceException se) {
				if (isMandatory) {
					throw se;
				}
				else {
					myLogger.log(Logger.WARNING, "Exception initializing service " + serviceClass , se);
				}
			}
		}
	}

	void bootAllServices(List services) throws ServiceException {
		Iterator it = services.iterator();
		while (it.hasNext()) {
			ServiceDescriptor dsc = (ServiceDescriptor) it.next();
			try {
				dsc.getService().boot(myProfile);
			} 
			catch(Throwable t) {
				if ( dsc.isMandatory() ) {
					throw new ServiceException("Error while booting mandatory service " + dsc.getName(), t);
				}
				else {
					myLogger.log(Logger.WARNING, "Exception booting service " + dsc.getName(), t);
				}
			}
		}
	}
	
	
	boolean joinPlatform() {
		//#J2ME_EXCLUDE_BEGIN
		Properties bootProps = myProfile.getBootProperties();
		if (bootProps != null && bootProps.getProperty(Profile.LOCAL_HOST) == null) {
			checkLocalHostAddress();
		}
		//#J2ME_EXCLUDE_END

		try {
			// Perform the initial setup from the profile
			init();

			// Connect the local node to the platform and activate all the services
			startNode();
		}
		catch (IMTPException imtpe) {
			myLogger.log(Logger.SEVERE,"Communication failure while joining agent platform: " + imtpe.getMessage());
			imtpe.printStackTrace();
			endContainer();
			cleanIMTPManager();
			return false;
		}
		catch (JADESecurityException ae) {
			myLogger.log(Logger.SEVERE,"Authentication or authorization failure while joining agent platform.");
			ae.printStackTrace();
			endContainer();
			cleanIMTPManager();
			return false;
		}
		catch (Exception e) {
			myLogger.log(Logger.SEVERE,"Some problem occurred while joining agent platform.");
			e.printStackTrace();
			endContainer();
			cleanIMTPManager();
			return false;
		}

		// Create and activate agents that must be launched at bootstrap
		startBootstrapAgents();

		joined = true;
		
		String startupTag = System.getProperty("startup-tag");
		if (startupTag != null) {
			// This line print in the standard output a tag used from the controller to check if container is started 
			System.out.println(startupTag + " " + myID);
		}

		myLogger.log(Logger.INFO, "--------------------------------------\nAgent container " + myID + " is ready.\n--------------------------------------------");
		
		return true;
	}
	
	private void checkLocalHostAddress() {
		String address = Profile.getDefaultNetworkName();
		if (address.equals(Profile.LOCALHOST_CONSTANT) || address.equals(Profile.LOOPBACK_ADDRESS_CONSTANT)) {
			myLogger.log(Logger.WARNING, "\n***************************************************************\nJAVA is not able to detect the local host address.\nIf this container is part of a distributed platform, use the\n-local-host option to explicitly specify it\n***************************************************************\n");
		}
	}

	private void cleanIMTPManager() {
		// In case container startup failed, we clean IMTPManager resources. 
		// This is important when the JVM is not killed on JADE termination.
		if (myIMTPManager != null) {
			myIMTPManager.shutDown();
		}
	}

	private void startBootstrapAgents() {
		try {
			List l = myProfile.getSpecifiers(Profile.AGENTS);
			Iterator agentSpecifiers = l.iterator();
			while(agentSpecifiers.hasNext()) {
				Specifier s = (Specifier) agentSpecifiers.next();
				if (s.getName() != null) {
					AID agentID = new AID(s.getName(), AID.ISLOCALNAME);

					try {
						//#MIDP_EXCLUDE_BEGING
						getContainerProxy(myNodeDescriptor.getOwnerPrincipal(), myNodeDescriptor.getOwnerCredentials()).createAgent(agentID, s.getClassName(), s.getArgs());
						//#MIDP_EXCLUDE_END
						/*#MIDP_INCLUDE_BEGIN
						 String serviceName = jade.core.management.AgentManagementSlice.NAME;
						 Service svc = myServiceFinder.findService(serviceName);
						 jade.core.management.AgentManagementSlice target = (jade.core.management.AgentManagementSlice) myIMTPManager.createSliceProxy(serviceName, svc.getHorizontalInterface(), myIMTPManager.getLocalNode());
						 GenericCommand dummyCmd = new GenericCommand(null, null, null);
						 dummyCmd.setPrincipal(myNodeDescriptor.getOwnerPrincipal());
						 dummyCmd.setCredentials(myNodeDescriptor.getOwnerCredentials());
						 target.createAgent(agentID, s.getClassName(), s.getArgs(), myNodeDescriptor.getOwnerPrincipal(), null, target.CREATE_ONLY, dummyCmd);
						 #MIDP_INCLUDE_END*/
					}
					catch (Throwable t) {
						myLogger.log(Logger.SEVERE,"Cannot create agent "+s.getName()+": "+t.getMessage());
					}
				}
				else {
					myLogger.log(Logger.WARNING,"Cannot create an agent with no name. Class was "+s.getClassName());
				}              	
			}

			// Now activate all agents (this call starts their embedded threads)
			AID[] allLocalNames = localAgents.keys();
			for (int i = 0; i < allLocalNames.length; i++) {
				AID id = allLocalNames[i];

				if(!id.equals(theAMS) && !id.equals(theDefaultDF)) {
					try {
						powerUpLocalAgent(id);
					}
					catch (NotFoundException nfe) {
						// Should never happen
						nfe.printStackTrace();
					}
				}
			}

			//#J2ME_EXCLUDE_BEGIN
			// If the Misc add-on is in the classpath and the -jade_core_AgentContainerImpl_enablemonitor option is not explicitly set to false, activate a ContainerMonitorAgent
			if (myProfile.getBooleanProperty(ENABLE_MONITOR, true)) {
				AID monitorId = new AID(MONITOR_AGENT_NAME, AID.ISLOCALNAME);
				try {
					getContainerProxy(myNodeDescriptor.getOwnerPrincipal(), myNodeDescriptor.getOwnerCredentials()).createAgent(monitorId, MONITOR_AGENT_CLASS, new Object[]{this, localAgents});
					powerUpLocalAgent(monitorId);
					myLogger.log(Logger.INFO, "Container-Monitor agent activated");
				}
				catch (Throwable t) {
					// The Misc add-on is not in the classpath --> Just do nothing
				}
			}
			//#J2ME_EXCLUDE_END
		}
		catch (ProfileException pe) {
			myLogger.log(Logger.WARNING, "Error reading initial agents. "+pe);
			pe.printStackTrace();
		}
	}

	public void shutDown() {
		checkCreationTime();

		// Remove all non-system agents
		Agent[] allLocalAgents = localAgents.values();

		for(int i = 0; i < allLocalAgents.length; i++) {
			// Kill agent and wait for its termination
			Agent a = allLocalAgents[i];

			// Skip the Default DF and the AMS
			AID id = a.getAID();
			if(id.equals(getAMS()) || id.equals(getDefaultDF()))
				continue;

			//System.out.println("Killing agent "+a.getLocalName());
			//System.out.flush();
			a.doDelete();
			//System.out.println("Done. Waiting for its termination...");
			//System.out.flush();
			a.join();
			//System.out.println("Agent "+a.getLocalName()+" terminated");
			//System.out.flush();
			a.resetToolkit();
		}

		try {
			myServiceManager.removeNode(myNodeDescriptor);
			//#J2ME_EXCLUDE_BEGIN
			if (mainDetectionListener != null) {
				mainDetectionListener.stop();
			}
			//#J2ME_EXCLUDE_END
			myIMTPManager.shutDown();
		}
		catch(IMTPException imtpe) {
			imtpe.printStackTrace();
		}
		catch(ServiceException se) {
			se.printStackTrace();
		}

		// Release Thread resources
		myResourceManager.releaseResources();

		// Notify the JADE Runtime that the container has terminated execution
		endContainer();
		
		joined = false;
	}

	private void checkCreationTime() {
		long time = System.currentTimeMillis();
		if ((time - creationTime) < 3000) {
			try {Thread.sleep(3000 - (time - creationTime));} catch (Exception e) {}
		}
	}

	// Call Runtime.instance().endContainer()
	// with the security priviledges of AgentContainerImpl
	// no matter priviledges of who originaltely triggered this action
	private void endContainer() {
		try {
			Runtime.instance().endContainer();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}


	////////////////////////////////////////////
	// AgentToolkit interface implementation
	////////////////////////////////////////////

	public Location here() {
		return myID;
	}

	/**
	 Issue a SEND_MESSAGE VerticalCommand for each receiver
	 */
	public void handleSend(ACLMessage msg, AID sender, boolean needClone) {
		Iterator it = msg.getAllIntendedReceiver();
		// If there are multiple receivers the message must always be cloned
		// since the MessageManager will modify it. If there is a single 
		// receiver we clone it or not depending on the needClone parameter
		boolean isFirst = true;
		while (it.hasNext()) {
			AID receiver = (AID)it.next();
			if (isFirst) {
				needClone = needClone || it.hasNext();
				isFirst = false;
			}
			GenericCommand cmd = new GenericCommand(jade.core.messaging.MessagingSlice.SEND_MESSAGE, jade.core.messaging.MessagingSlice.NAME, null);
			cmd.addParam(sender);
			ACLMessage toBeSent = null;
			if (needClone) {
				toBeSent = (ACLMessage) msg.clone();
			}
			else {
				toBeSent = msg;
			}
			GenericMessage gmsg = new GenericMessage(toBeSent);
			cmd.addParam(gmsg);
			cmd.addParam(receiver);
			// Set the credentials of the sender
			initCredentials(cmd, sender);
			Object ret = myCommandProcessor.processOutgoing(cmd);
			if (ret != null) {
				if (ret instanceof Throwable) {
					// The SEND_MESSAGE VerticalCommand was blocked by some Filter 
					// before reaching the Messaging Souce Sink --> Issue
					// a NOTIFY_FAILURE VerticalCommand to notify the sender
					cmd = new GenericCommand(jade.core.messaging.MessagingSlice.NOTIFY_FAILURE, jade.core.messaging.MessagingSlice.NAME, null);
					cmd.addParam(gmsg);
					cmd.addParam(receiver);
					cmd.addParam(new InternalError("Message blocked: "+ret));
					ret = myCommandProcessor.processOutgoing(cmd);
					if (ret != null) {
						if (ret instanceof Throwable) {
							((Throwable) ret).printStackTrace();
						}
					}
				}
			}
		}

	}

	//#MIDP_EXCLUDE_BEGIN
	// FIXME: to be removed
	public void handlePosted(AID agentID, ACLMessage msg) {
		GenericCommand cmd = new GenericCommand(jade.core.event.NotificationSlice.NOTIFY_POSTED, jade.core.event.NotificationSlice.NAME, null);
		cmd.addParam(msg);
		cmd.addParam(agentID);

		Object ret = myCommandProcessor.processOutgoing(cmd);
		if (ret != null) {
			if (ret instanceof Throwable) {
				((Throwable) ret).printStackTrace();
			}
		}
	}
	//#MIDP_EXCLUDE_END

	//#MIDP_EXCLUDE_BEGIN
	/**
	 Issue a NOTIFY_RECEIVED VerticalCommand
	 */
	public void handleReceived(AID agentID, ACLMessage msg) {
		GenericCommand cmd = new GenericCommand(jade.core.event.NotificationSlice.NOTIFY_RECEIVED, jade.core.event.NotificationSlice.NAME, null);
		cmd.addParam(msg);
		cmd.addParam(agentID);
		// No security check is meaningful on this action --> don't even set the Credentials

		Object ret = myCommandProcessor.processOutgoing(cmd);
		if (ret != null) {
			if (ret instanceof Throwable) {
				((Throwable) ret).printStackTrace();
			}
		}

	}
	//#MIDP_EXCLUDE_END

	//#MIDP_EXCLUDE_BEGIN
	public void handleBehaviourAdded(AID agentID, Behaviour b) {
		GenericCommand cmd = new GenericCommand(jade.core.event.NotificationSlice.NOTIFY_BEHAVIOUR_ADDED, jade.core.event.NotificationSlice.NAME, null);
		cmd.addParam(agentID);
		cmd.addParam(b);
		// No security check is meaningful on this action --> don't even set the Credentials

		Object ret = myCommandProcessor.processOutgoing(cmd);
		if (ret != null) {
			if (ret instanceof Throwable) {
				((Throwable) ret).printStackTrace();
			}
		}
	}
	//#MIDP_EXCLUDE_END

	//#MIDP_EXCLUDE_BEGIN
	public void handleBehaviourRemoved(AID agentID, Behaviour b) {
		GenericCommand cmd = new GenericCommand(jade.core.event.NotificationSlice.NOTIFY_BEHAVIOUR_REMOVED, jade.core.event.NotificationSlice.NAME, null);
		cmd.addParam(agentID);
		cmd.addParam(b);
		// No security check is meaningful on this action --> don't even set the Credentials

		Object ret = myCommandProcessor.processOutgoing(cmd);
		if (ret != null) {
			if (ret instanceof Throwable) {
				((Throwable) ret).printStackTrace();
			}
		}
	}
	//#MIDP_EXCLUDE_END

	//#MIDP_EXCLUDE_BEGIN
	public void handleChangeBehaviourState(AID agentID, Behaviour b, String from, String to) {
		GenericCommand cmd = new GenericCommand(jade.core.event.NotificationSlice.NOTIFY_CHANGED_BEHAVIOUR_STATE, jade.core.event.NotificationSlice.NAME, null);
		cmd.addParam(agentID);
		cmd.addParam(b);
		cmd.addParam(from);
		cmd.addParam(to);
		// No security check is meaningful on this action --> don't even set the Credentials

		Object ret = myCommandProcessor.processOutgoing(cmd);
		if (ret != null) {
			if (ret instanceof Throwable) {
				((Throwable) ret).printStackTrace();
			}
		}
	}
	//#MIDP_EXCLUDE_END

	//#MIDP_EXCLUDE_BEGIN
	// FIXME: to be removed
	public void handleChangedAgentPrincipal(AID agentID, JADEPrincipal oldPrincipal, Credentials creds) {

		/***

		 myNotificationManager.fireEvent(NotificationManager.CHANGED_AGENT_PRINCIPAL,
		 new Object[]{agentID, oldPrincipal, (AgentPrincipal)certs.getIdentityCertificate().getSubject()});
		 try {
		 myPlatform.changedAgentPrincipal(agentID, certs);
		 }
		 catch (IMTPException re) {
		 re.printStackTrace();
		 }
		 catch (NotFoundException nfe) {
		 nfe.printStackTrace();
		 }

		 ***/
	}
	//#MIDP_EXCLUDE_END


	public void handleChangedAgentState(AID agentID, int oldState, int newState) {
		AgentState from = AgentState.getInstance(oldState);
		AgentState to = AgentState.getInstance(newState);

		GenericCommand cmd = new GenericCommand(jade.core.management.AgentManagementSlice.INFORM_STATE_CHANGED, jade.core.management.AgentManagementSlice.NAME, null);
		cmd.addParam(agentID);
		cmd.addParam(from);
		cmd.addParam(to);
		// No security check is meaningful on this action --> don't even set the Credentials

		Object ret = myCommandProcessor.processOutgoing(cmd);
		if (ret != null) {
			if (ret instanceof Throwable) {
				((Throwable) ret).printStackTrace();
			}
		}
	}

	public void handleEnd(AID agentID) {
		GenericCommand cmd = new GenericCommand(jade.core.management.AgentManagementSlice.INFORM_KILLED, jade.core.management.AgentManagementSlice.NAME, null);
		cmd.addParam(agentID);
		// Set the credentials of the terminating agent
		initCredentials(cmd, agentID);

		Object ret = myCommandProcessor.processOutgoing(cmd);
		if (ret != null) {
			if (ret instanceof Throwable) {
				((Throwable) ret).printStackTrace();
			}
		}
	}

	public void setPlatformAddresses(AID id) {
		GenericCommand cmd = new GenericCommand(jade.core.messaging.MessagingSlice.SET_PLATFORM_ADDRESSES, jade.core.messaging.MessagingSlice.NAME, null);
		cmd.addParam(id);
		// No security check is meaningful on this action --> don't even set the Credentials

		Object ret = myCommandProcessor.processOutgoing(cmd);
		if (ret != null) {
			if (ret instanceof Throwable) {
				((Throwable) ret).printStackTrace();
			}
		}
	}

	public AID getAMS() {
		return (AID)theAMS.clone();
	}

	public AID getDefaultDF() {
		return (AID)theDefaultDF.clone();
	}

	public String getProperty(String key, String aDefault) {
		return myProfile.getParameter(key, aDefault);
	}

	//#MIDP_EXCLUDE_BEGIN
	public Properties getBootProperties(){
		return myProfile.getBootProperties();
	}
	//#MIDP_EXCLUDE_END

	public ServiceHelper getHelper(Agent a, String serviceName) throws ServiceException {
		try {

			// Retrieve the service
			Service s = myServiceFinder.findService(serviceName);
			if(s == null) {
				throw new ServiceNotActiveException(serviceName);
			}

			return s.getHelper( a );
		}
		catch (IMTPException imtpe) {
			throw new ServiceException(" ServiceHelper could not be created for: " + serviceName, imtpe);
		}
	}


	// Private and package scoped methods



	/**
	 */
	public String getPlatformID() {
		return AID.getPlatformID();
	}

	public Agent addLocalAgent(AID id, Agent a) {
		a.setToolkit(this);
		//#MIDP_EXCLUDE_BEGIN
		// Initialize the agent message queue after the toolkit is set and before the agent is inserted in the LADT
		a.initMessageQueue();
		//#MIDP_EXCLUDE_END
		return localAgents.put(id, a);
	}

	public void powerUpLocalAgent(AID agentID) throws NotFoundException {
		Agent instance = localAgents.acquire(agentID);
		if (instance == null) {
			throw new NotFoundException("powerUpLocalAgent() failed to find agent "+agentID.getName());
		}
		int type = (agentID.equals(theAMS) || agentID.equals(theDefaultDF) ? ResourceManager.SYSTEM_AGENTS : ResourceManager.USER_AGENTS);
		Thread t = myResourceManager.getThread(type, agentID.getLocalName(), instance);
		instance.powerUp(agentID, t);
		localAgents.release(agentID);
	}

	public void removeLocalAgent(AID id) {
		localAgents.remove(id);
	}

	public Agent acquireLocalAgent(AID id) {
		return localAgents.acquire(id);
	}

	public void releaseLocalAgent(AID id) {
		localAgents.release(id);
	}

	public boolean isLocalAgent(AID id) {
		return localAgents.contains(id);
	}
	
	public AID[] agentNames() {
		return localAgents.keys();
	}

	//#MIDP_EXCLUDE_BEGIN
	public void fillListFromMessageQueue(List messages, Agent a) {
		MessageQueue mq = a.getMessageQueue();
		synchronized(mq) {
			mq.copyTo(messages);
		}
	}
	//#MIDP_EXCLUDE_END

	//#MIDP_EXCLUDE_BEGIN
	public void fillListFromReadyBehaviours(List behaviours, Agent a) {

		Scheduler s = a.getScheduler();

		// (Mutual exclusion with Scheduler.add(), remove()...)
		synchronized (s) {
			Iterator it = s.readyBehaviours.iterator();
			while (it.hasNext()) {
				Behaviour b = (Behaviour) it.next();
				behaviours.add(new BehaviourID(b));
			}

		}
	}
	//#MIDP_EXCLUDE_END

	//#MIDP_EXCLUDE_BEGIN
	public void fillListFromBlockedBehaviours(List behaviours, Agent a) {

		Scheduler s = a.getScheduler();

		// (Mutual exclusion with Scheduler.add(), remove()...)
		synchronized (s) {
			Iterator it = s.blockedBehaviours.iterator();
			while (it.hasNext()) {
				Behaviour b = (Behaviour) it.next();
				behaviours.add(new BehaviourID(b));
			}
		}
	}
	//#MIDP_EXCLUDE_END

	//#MIDP_EXCLUDE_BEGIN
	/*public void commitMigration(Agent instance) {
	 instance.doGone();
	 localAgents.remove(instance.getAID());
	 }*/
	//#MIDP_EXCLUDE_END

	//#MIDP_EXCLUDE_BEGIN
	/*public void abortMigration(Agent instance) {
	 instance.doExecute();
	 }*/
	//#MIDP_EXCLUDE_END

	public void addAddressToLocalAgents(String address) {
		Agent[] allLocalAgents = localAgents.values();

		// Add the address to the AIDs of all local agents
		for(int j = 0; j < allLocalAgents.length; j++) {
			allLocalAgents[j].addPlatformAddress(address);
		}

		// Add the new addresses to the AMS and Default DF AIDs
		theAMS.addAddresses(address);
		theDefaultDF.addAddresses(address);
	}

	public void removeAddressFromLocalAgents(String address) {
		Agent[] allLocalAgents = localAgents.values();

		// Remove the address from the AIDs of all local agents
		for(int j = 0; j < allLocalAgents.length; j++) {
			allLocalAgents[j].removePlatformAddress(address);
		}

		// Remove the address from the AIDs of the AMS and the Default DF
		theAMS.removeAddresses(address);
		theDefaultDF.removeAddresses(address);
	}

	public boolean postMessageToLocalAgent(ACLMessage msg, AID receiverID) {
		Agent receiver = localAgents.acquire(receiverID);
		if(receiver == null) {
			return false;
		}
		receiver.postMessage(msg);
		localAgents.release(receiverID);

		return true;
	}

	public ContainerID getID() {
		return myID;
	}

	public MainContainer getMain() {
		//#MIDP_EXCLUDE_BEGIN
		return myMainContainer;
		//#MIDP_EXCLUDE_END
		/*#MIDP_INCLUDE_BEGIN
		 return null;
		 #MIDP_INCLUDE_END*/
	}

	public ServiceManager getServiceManager() {
		return myServiceManager;
	}

	public ServiceFinder getServiceFinder() {
		return myServiceFinder;
	}

	// Utility method to start a kernel service
	protected ServiceDescriptor startService(String name, boolean activateIt) throws ServiceException {

		try {
			Class svcClass = Class.forName(name);
			Service svc = (Service)svcClass.newInstance();
			svc.init(this, myProfile);
			ServiceDescriptor dsc = new ServiceDescriptor(svc.getName(), svc);

			if (activateIt) {
				myServiceManager.activateService(dsc);
				svc.boot(myProfile);
			}
			return dsc;
		}
		catch(ServiceException se) {
			// Let it through
			throw se;
		}
		catch(Throwable t) {
			throw new ServiceException("An error occurred during service activation", t);
		}
	}

	protected void stopService(String name) throws ServiceException {
		try {
			myServiceManager.deactivateService(name);
		}
		catch(ServiceException se) {
			// Let it through
			throw se;
		}
		catch(Throwable t) {
			throw new ServiceException("An error occurred during service deactivation", t);
		}
	}

	//#MIDP_EXCLUDE_BEGIN
	public void becomeLeader(AMSEventQueueFeeder feeder) {
		try {
			myMainContainer.initSystemAgents(this, true);
			myMainContainer.startSystemAgents(this, feeder);
			myMainContainer.restartReplicatedAgents(this);
			myProfile.setParameter(Profile.LOCAL_SERVICE_MANAGER, "false");
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	//#MIDP_EXCLUDE_END


	//#ALL_EXCLUDE_BEGIN
	//FIXME: These methods have been added to support
	// PlatformListener registration from the In-process-interface
	// with minimum effort. They will possibly be removed in a
	// future (more general) implementation
	public void addPlatformListener(AgentManager.Listener l) throws ClassCastException {
		AgentManager m = (AgentManager) myMainContainer;
		m.addListener(l);
	}

	public void removePlatformListener(AgentManager.Listener l) throws ClassCastException {
		AgentManager m = (AgentManager) myMainContainer;
		m.removeListener(l);
	}
	//#ALL_EXCLUDE_END

	private void initCredentials(Command cmd, AID id) {
		//#MIDP_EXCLUDE_BEGIN
		Agent agent = localAgents.acquire(id);
		if (agent != null) {
			try {
				CredentialsHelper ch = (CredentialsHelper) agent.getHelper("jade.core.security.Security");
				cmd.setPrincipal(ch.getPrincipal());
				cmd.setCredentials(ch.getCredentials());
			}
			catch (ServiceException se) {
				// The security plug-in is not there. Just ignore it
			}
		}
		localAgents.release(id);
		//#MIDP_EXCLUDE_END
	}

	public boolean isJoined() {
		return joined;
	}

}
