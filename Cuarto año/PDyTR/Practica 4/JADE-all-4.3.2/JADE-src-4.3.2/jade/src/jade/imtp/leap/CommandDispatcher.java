/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/**
 * ***************************************************************
 * The LEAP libraries, when combined with certain JADE platform components,
 * provide a run-time environment for enabling FIPA agents to execute on
 * lightweight devices running Java. LEAP and JADE teams have jointly
 * designed the API for ease of integration and hence to take advantage
 * of these dual developments and extensions so that users only see
 * one development platform and a
 * single homogeneous set of APIs. Enabling deployment to a wide range of
 * devices whilst still having access to the full development
 * environment and functionalities that JADE provides.
 * Copyright (C) 2001 Telecom Italia LAB S.p.A.
 * Copyright (C) 2001 Siemens AG.
 * 
 * GNU Lesser General Public License
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */


package jade.imtp.leap;


import jade.core.PlatformManager;
import jade.core.Node;
import jade.core.IMTPException;
import jade.core.Profile;
import jade.core.UnreachableException;
import jade.mtp.TransportAddress;
import jade.util.leap.Iterator;
import jade.util.leap.ArrayList;
import jade.util.leap.List;
import jade.util.leap.Map;
import jade.util.leap.HashMap;
import jade.util.Logger;

/**
 * This class provides the implementation of a command
 * dispatcher. The command dispatcher misses support for multiple remote
 * objects, multiple ICPs and command routing.
 * 
 * <p>The command dispatcher is based on an implementation written by
 * Michael Watzke and Giovanni Caire (TILAB), 09/11/2000.</p>
 * 
 * @author Tobias Schaefer
 * @version 1.0
 */
class CommandDispatcher implements StubHelper, ICP.Listener {
	private static final String MAIN_PROTO_CLASS = "main-proto-class";
	
	/**
	 * The default name for new instances of the class
	 * <tt>CommandDispatcher</tt> that have not get an unique name by
	 * their container yet.
	 */
	protected static final String      DEFAULT_NAME = "Default";
	
	private static boolean enableMultiplePlatforms;
	
	/**
	 * The default singleton instance of the command dispatcher.
	 */
	protected static CommandDispatcher defaultCommandDispatcher;
	
	/**
	 * The singleton map of command dispatchers in case more than one must be present in the same JVM.
	 */
	protected static Map dispatchers = new HashMap();
	
	private String platformName;
	
	/**
	 * The unique name of this command dispatcher used to avoid loops in
	 * the forwarding mechanism.
	 */
	protected String name;
	
	/**
	 * The transport address of the default router. Commands that cannot
	 * be dispatched directly will be sent to this address.
	 */
	protected TransportAddress         routerTA = null;
	
	
	/**
	 * This hashtable maps the IDs of the objects remotized by this
	 * command dispatcher to the skeletons for these objects. It is used
	 * when a command is received from a remote JVM.
	 */
	protected Map skeletons = new HashMap();
	
	/**
	 * This hashtable maps the objects remotized by this command
	 * dispatcher to their IDs. It is used when a stub of a remotized
	 * object must be built to be sent to a remote JVM.
	 */
	protected Map ids = new HashMap();
	
	/**
	 * A counter that is used for determining IDs for remotized objects.
	 * Everytime a new object is registered by the command dispatcher it
	 * gets the value of this field as ID and the field is increased.
	 */
	protected int       nextID;
	
	/**
	 * The pool of ICP objects used by this command dispatcher to
	 * actually send/receive data over the network. It is a table that
	 * associates a <tt>String</tt> representing a protocol (e.g. "http")
	 * to a list of ICPs supporting that protocol.
	 */
	protected Map icps = new HashMap();
	
	/**
	 * The transport addresses the ICPs managed by this command
	 * dispatcher are listening for commands on.
	 */
	protected List      addresses = new ArrayList();
	
	/**
	 * The URLs corresponding to the local transport addresses.
	 */
	protected List      urls = new ArrayList();
	
	/**
	 The stub for the platform service manager. This stub will be
	 shared by all nodes within this Java virtual Machine.
	 */
	private PlatformManager thePlatformManager = null;
	
	
	private Logger myLogger = Logger.getMyLogger(getClass().getName());;
	
	static {
		enableMultiplePlatforms = "true".equals(System.getProperty("jade.enable.multiple.platforms"));
	}
	
	/**
	 * Returns a reference to the singleton instance of the CommandDispatcher for the indicated platform.
	 * Such instance is created if necessary.
	 * If no platform name is specified or the enableMultiplePlatforms flag is not set, then use the default 
	 * CommandDispatcher
	 * @return the singleton instance of the CommandDispatcher for the indicated platform.
	 */
	public synchronized static final CommandDispatcher getDispatcher(String name) throws IMTPException {
		if (enableMultiplePlatforms) {
			if (name != null) {
				CommandDispatcher cd = (CommandDispatcher) dispatchers.get(name);
				if (cd == null) {
					cd = new CommandDispatcher();
					cd.setPlatformName(name);
					dispatchers.put(name, cd);
				}
				return cd;
			}
			else {
				throw new IMTPException("No platform name specified and enable-multiple-platforms mode activated");
			}
		}
		else {
			// Use the default CommandDispatcher anyway
			if (defaultCommandDispatcher == null) {
				defaultCommandDispatcher = new CommandDispatcher();
				defaultCommandDispatcher.setPlatformName(name);
			} 
			return defaultCommandDispatcher;
		}
	} 
	
	private synchronized static void removeDispatcher(String name) {
		if (enableMultiplePlatforms) {
			dispatchers.remove(name);
		}
	}
	
	private void setPlatformName(String name) {
		platformName = name;
	}
	
	/**
	 * A sole constructor. To get a command dispatcher the constructor
	 * should not be called directly but the static <tt>create</tt> and
	 * <tt>getDispatcher</tt> methods should be used. Thereby the
	 * existence of a singleton instance of the command dispatcher will
	 * be guaranteed.
	 */
	private CommandDispatcher() {
		// Set a temporary name. Will be substituted as soon as the first
		// container attached to this CommandDispatcher will receive a
		// unique name from the main.
		name = DEFAULT_NAME;
		nextID = 1;
	}
	
	synchronized PlatformManager getPlatformManagerProxy(Profile p) throws IMTPException {
		if(thePlatformManager == null) {
			
			PlatformManagerStub stub = new PlatformManagerStub(platformName);
			TransportAddress mainTA = initMainTA(p);
			stub.bind(this);
			stub.addTA(mainTA);
			setPlatformManager(stub);
		}
		
		return thePlatformManager;
	}

	// No need to synchronize it as it is always called within synchronized blocks
	private void setPlatformManager(PlatformManager pm) throws IMTPException {
		thePlatformManager = pm;
		String actualPlatformName  = thePlatformManager.getPlatformName();
		if (platformName != null && !platformName.equals("*") ) { // * is the wild-card for automatic main detection
			// PlatformName already set --> Check that it is consistent with the actual name of the platform
			if (!platformName.equals(actualPlatformName)) {
				throw new IMTPException("Wrong platform name "+platformName+". It should be "+actualPlatformName);
			}
		}
		else {
			platformName = actualPlatformName;
		}
	}
	
	// This is only called when reconnecting to a new Master Main Container --> No need to check again the platformName
	synchronized void setPlatformManagerProxy(PlatformManager pm) {
		thePlatformManager = pm;
	}
	
	public PlatformManager getPlatformManagerStub(String addr) throws IMTPException {
		
		// Try to translate the address into a TransportAddress
		// using a protocol supported by this CommandDispatcher
		try {
			PlatformManagerStub stub = new PlatformManagerStub(platformName);
			TransportAddress ta = stringToAddr(addr);
			stub.bind(this);
			stub.addTA(ta);
			return stub;
		}
		catch (DispatcherException de) {
			throw new IMTPException("Invalid address for a Platform Manager", de);
		}
		
	}
	
	public void addAddressToStub(Stub target, String toAdd) {
		try {
			TransportAddress ta = stringToAddr(toAdd);
			target.addTA(ta);
		}
		catch(DispatcherException de) {
			de.printStackTrace();
		}
	}
	
	public void removeAddressFromStub(Stub target, String toRemove) {
		try {
			TransportAddress ta = stringToAddr(toRemove);
			target.removeTA(ta);
		}
		catch(DispatcherException de) {
			de.printStackTrace();
		}
	}
	
	public void clearStubAddresses(Stub target) {
		target.clearTAs();
	}
	
	/**
	 * Sets the transport address of the default router used for the
	 * forwarding mechanism.
	 * 
	 * @param url the URL of the default router.
	 */
	void setRouterAddress(String url) {
		if (url != null) {
			// The default router must be directly reachable -->
			// Its URL can be converted into a TransportAddress by
			// the ICP registered to this CommandDispatcher
			try {
				TransportAddress ta = stringToAddr(url);
				if (routerTA != null && !routerTA.equals(ta)) {
					if(myLogger.isLoggable(Logger.WARNING))
						myLogger.log(Logger.WARNING,"Transport address of current router has been changed");
				} 
				routerTA = ta;
			}
			catch (Exception e) {
				// Just print a warning: default (i.e. main TA) will be used
				if(myLogger.isLoggable(Logger.WARNING))
					myLogger.log(Logger.WARNING,"Can't initialize router address");
			}
		}    		
	} 
	
	/**
	 * This method dispatches the specified command to the first address
	 * (among those specified) to which dispatching succeeds.
	 * 
	 * @param destTAs a list of transport addresses where the command
	 * dispatcher should try to dispatch the command.
	 * @param command the command that is to be dispatched.
	 * @return a response command from the receiving container.
	 * @throws DispatcherException if an error occurs during dispatching.
	 * @throws UnreachableException if none of the destination addresses
	 * is reachable.
	 */
	public Command dispatchCommand(List destTAs, 
			Command command) throws DispatcherException, UnreachableException {
		
		// DEBUG
		//TransportAddress ta = (TransportAddress) destTAs.get(0);
		//System.out.println("Dispatching command of type " + command.getCode() + " to "+ta.getHost()+":"+ta.getPort());
		Command response = null;
		//#J2ME_EXCLUDE_BEGIN
		if (isLocal(destTAs)) {
			Integer id = new Integer(command.getObjectID());
			Skeleton skel = (Skeleton) skeletons.get(id);
			if (skel != null) {
				response = skel.processCommand(command);
			}
		}
		//#J2ME_EXCLUDE_END
		if (response == null) {
			try {
				response = dispatchSerializedCommand(destTAs, serializeCommand(command), command.getRequireFreshConnection(), name);
			} 
			catch (LEAPSerializationException lse) {
				throw new DispatcherException("Error serializing command "+command+" ["+lse.getMessage()+"]");
			}
		}
		
		// If the dispatched command was an ADD_NODE --> get the
		// name from the response and use it as the name of the CommandDispatcher
		if (command.getCode() == Command.ADD_NODE && name.equals(DEFAULT_NAME)) {
			name = (String) response.getParamAt(0);
		}
		return response;
	} 
	
	private boolean isLocal(List destTAs) {
		try {
			TransportAddress ta1 = (TransportAddress) addresses.get(0);
			TransportAddress ta2 = (TransportAddress) destTAs.get(0);
			return (ta1.getHost().equals(ta2.getHost()) && ta1.getPort().equals(ta2.getPort()) && ta2.getFile() == null);
		}
		catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * Dispatches the specified serialized command to one of the
	 * specified transport addresses (the first where dispatching
	 * succeeds) directly or through the router.
	 * 
	 * @param destTAs a list of transport addresses where the command
	 * dispatcher should try to dispatch the command.
	 * @param commandPayload the serialized command that is to be
	 * dispatched.
	 * @param origin a <tt>String</tt> object describing the origin of
	 * the command to be dispatched.
	 * @return a response command from the receiving container.
	 * @throws DispatcherException if an error occurs during dispatching.
	 * @throws UnreachableException if none of the destination addresses
	 * is reachable.
	 */
	private Command dispatchSerializedCommand(List destTAs, byte[] commandPayload, boolean requireFreshConnection, String origin) 
	throws DispatcherException, UnreachableException {
		
		// Be sure that the destination addresses are correctly specified
		if (destTAs == null || destTAs.size() == 0) {
			throw new DispatcherException("no destination address specified.");		
		} 
		
		byte[] responsePayload = null;
		try {		
			// Try to dispatch the command directly
			responsePayload = dispatchDirectly(destTAs, commandPayload, requireFreshConnection);
		} 
		catch (UnreachableException ue) {
			// Direct dispatching failed --> Try through the router
			if (myLogger.isLoggable(Logger.FINE)) {
				myLogger.log(Logger.FINE, "Destination unreachable. Dispatch command through router.", ue);
			}
			
			if (routerTA != null) {
				responsePayload = dispatchThroughRouter(destTAs, commandPayload, requireFreshConnection, origin);
			}
			else {
				throw ue;
			}
		} 
		
		// Deserialize the response
		try {
			Command response = deserializeCommand(responsePayload);
			
			// Check whether some exceptions to be handled by the
			// CommandDispatcher occurred on the remote site
			checkRemoteExceptions(response);
			
			return response;
		} 
		catch (LEAPSerializationException lse) {
			throw new DispatcherException("error deserializing response ["+lse.getMessage()+"].");
		} 
	} 
	
	/**
	 * Dispatches the specified serialized command to one of the
	 * specified transport addresses (the first where dispatching
	 * succeeds) directly.
	 * 
	 * @param destTAs a list of transport addresses where the command
	 * dispatcher should try to dispatch the command.
	 * @param commandPayload the serialized command that is to be
	 * dispatched.
	 * @return a serialized response command from the receiving
	 * container.
	 * @throws UnreachableException if none of the destination addresses
	 * is reachable.
	 */
	private byte[] dispatchDirectly(List destTAs, byte[] commandPayload, boolean requireFreshConnection) throws UnreachableException {
		
		// Loop on destinaltion addresses (No need to check again
		// that the list of addresses is not-null and not-empty)
		UnreachableException lastException = null;
		for (int i = 0; i < destTAs.size(); i++) {
			try {
				return send((TransportAddress) destTAs.get(i), commandPayload, requireFreshConnection);
			} 
			catch (UnreachableException ue) {
				lastException = ue;
				// Can't send command to this address --> try the next one
				// DEBUG
				//TransportAddress ta = (TransportAddress)destTAs.get(i);
				//System.out.println("Sending command to " + ta.getProto() + "://" + ta.getHost() + ":" + ta.getPort() + " failed [" + ue.getMessage() + "]");
				//if (i < destTAs.size() - 1)
				//  System.out.println("Try next address");
			} 
		} 
		
		// Sending failed to all addresses --> Throw the last exception
		throw lastException;
	} 
	
	/**
	 * Dispatches the specified serialized command to one of the
	 * specified transport addresses (the first where dispatching
	 * succeeds) through the router.
	 * 
	 * @param destTAs a list of transport addresses where the command
	 * dispatcher should try to dispatch the command.
	 * @param commandPayload the serialized command that is to be
	 * dispatched.
	 * @param origin a <tt>String</tt> object describing the origin of
	 * the command to be dispatched.
	 * @return a serialized response command from the receiving
	 * container.
	 * @throws DispatcherException if an error occurs during dispatching.
	 * @throws UnreachableException if none of the destination addresses
	 * is reachable.
	 */
	private byte[] dispatchThroughRouter(List destTAs, byte[] commandPayload, boolean requireFreshConnection, String origin) throws DispatcherException, UnreachableException {
		// Build a FORWARD command
		Command forward = new Command(Command.FORWARD);
		forward.addParam(commandPayload);
		forward.addParam(destTAs);
		forward.addParam(origin);
		
		try {
			return send(routerTA, serializeCommand(forward), requireFreshConnection);
		} 
		catch (LEAPSerializationException lse) {
			throw new DispatcherException("error serializing FORWARD command ["+lse.getMessage()+"].");
		} 
	} 
	
	/**
	 * Checks whether some exceptions to be handled by the command
	 * dispatcher occurred on the remote site. If this is the case the
	 * command dispatcher throws the corresponding exception locally.
	 * 
	 * @param response the resonse comman from the receiving container.
	 * @throws DispatcherException if an error occurs on the remote site
	 * during dispatching.
	 * @throws UnreachableException if the destination address is not
	 * reachable.
	 */
	protected void checkRemoteExceptions(Command response) 
	throws DispatcherException, UnreachableException {
		if (response.getCode() == Command.ERROR) {
			String exception = (String) response.getParamAt(0);
			
			// DispatcherException (some error occurred in the remote
			// CommandDispatcher) --> throw a DispatcherException.
			if (exception.equals("jade.imtp.leap.DispatcherException")) {
				throw new DispatcherException("DispatcherException in remote site. "+response.getParamAt(1));
			} 
			else    // UnreachableException (the Command was sent to the router,
				// but the final destination was unreachable from there)
				// --> throw an UnreachableException
				if (exception.equals("jade.core.UnreachableException")) {
					throw new UnreachableException((String) response.getParamAt(1));
				} 
		} 
	} 
	
	/**
	 * Serializes a <tt>Command</tt> object into a <tt>byte</tt> array.
	 * 
	 * @param command the command to be serialized.
	 * @return the serialized command.
	 * @throws LEAPSerializationException if the command cannot be
	 * serialized.
	 */
	protected byte[] serializeCommand(Command command) throws LEAPSerializationException {
		DeliverableDataOutputStream ddout = new DeliverableDataOutputStream(this);
		ddout.serializeCommand(command);
		
		return ddout.getSerializedByteArray();
	} 
	
	/**
	 * Deserializes a <tt>Command</tt> object from a <tt>byte</tt> array.
	 * 
	 * @param data the <tt>byte</tt> array containing serialized command.
	 * @return the deserialized command.
	 * @throws LEAPSerializationException if the command cannot be
	 * deserialized.
	 */
	protected Command deserializeCommand(byte[] data) throws LEAPSerializationException {
		return new DeliverableDataInputStream(data, this).deserializeCommand();
	} 
	
	/**
	 * Builds a command that carries an exception.
	 * 
	 * @param exception the exception to be carried.
	 * @return the command carrying the exception.
	 */
	protected Command buildExceptionResponse(Exception exception) {
		Command response = new Command(Command.ERROR);
		response.addParam(exception.getClass().getName());
		response.addParam(exception.getMessage());
		
		return response;
	} 
	
	private TransportAddress initMainTA(Profile p) throws IMTPException {
		
		TransportAddress mainTA = null;
		
		try {
			String mainURL = p.getParameter(LEAPIMTPManager.MAIN_URL, null);
			// DEBUG
			//System.out.println("Main URL is "+mainURL);
			
			// Try to translate the mainURL into a TransportAddress
			// using a protocol supported by this CommandDispatcher
			try {
				mainTA = stringToAddr(mainURL);
			}
			catch (DispatcherException de) {
				// Failure --> A suitable protocol class may be explicitly
				// indicated in the profile (otherwise rethrow the exception)
				String mainTPClass = p.getParameter(MAIN_PROTO_CLASS, null);
				if (mainTPClass != null) {
					TransportProtocol tp = (TransportProtocol) Class.forName(mainTPClass).newInstance();
					mainTA = tp.stringToAddr(mainURL);
				}
				else {
					throw de;
				}
			}
			
			// If the router TA was not set --> use the mainTA as default
			//if (routerTA == null) {
			//	routerTA = mainTA;
			//}
			
			return mainTA;
			
		}
		catch (Exception e) {
			throw new IMTPException("Error getting Main Container address", e);
		}
		
	}
	
	/**
	 * Adds (and activates) an ICP to this command dispatcher.
	 * 
	 * @param peer the ICP to add.
	 * @param args the arguments required by the ICP for the activation.
	 * These arguments are ICP specific.
	 */
	public void addICP(ICP peer, String peerID, Profile p) {
		try {
			
			// Activate the peer.
			TransportAddress  ta = peer.activate(this, peerID, p);
			
			// Add the listening address to the list of local addresses.
			TransportProtocol tp = peer.getProtocol();
			String            url = tp.addrToString(ta);
			if (myLogger.isLoggable(Logger.FINE)) {
				myLogger.log(Logger.FINE, "ICP "+peerID+" of class "+peer.getClass().getName()+" activated. Address is: "+url);
			}
			addresses.add(ta);
			urls.add(url);
			
			// Put the peer in the table of local ICPs.
			String proto = tp.getName().toLowerCase();
			List                  list = (List) icps.get(proto);
			if (list == null) {
				icps.put(proto, (list = new ArrayList()));
			} 
			
			list.add(peer);
		} 
		catch (ICPException icpe) {
			// Print a warning.
			myLogger.log(Logger.WARNING, "Error adding ICP "+peer+"["+icpe.getMessage()+"].");
		} 
	} 
	
	TransportProtocol getProtocol(String protoName) {
		List list = (List) icps.get(protoName.toLowerCase());
		if (list != null && list.size() > 0) {
			ICP icp = (ICP) list.get(0);
			return icp.getProtocol();
		}
		return null;
	}
	
	/**
	 * Returns the ID of the specified remotized object.
	 * 
	 * @param remoteObject the object whose ID should be returned.
	 * @return the ID of the reomte object.
	 * @throws RuntimeException if the specified object is not
	 * remotized by this command dispatcher.
	 */
	public int getID(Object remoteObject) throws IMTPException {
		Integer id = (Integer) ids.get(remoteObject);
		if (id != null) {
			return id.intValue();
		} 
		
		throw new IMTPException("specified object is not remotized by this command dispatcher.");
	} 
	
	/**
	 * Returns the list of local addresses.
	 * 
	 * @return the list of local addresses.
	 */
	public List getLocalTAs() {
		return addresses;
	} 
	
	/**
	 * Returns the list of URLs corresponding to the local addresses.
	 * 
	 * @return the list of URLs corresponding to the local addresses.
	 */
	public List getLocalURLs() {
		return urls;
	} 
	
	/**
	 * Converts an URL into a transport address using the transport
	 * protocol supported by the ICPs currently installed in the command
	 * dispatcher. If there is no ICP installed to the command dispatcher
	 * or their transport protocols are not able to convert the specified
	 * URL a <tt>DispatcherException</tt> is thrown.
	 * 
	 * @param url a <tt>String</tt> object specifying the URL to convert.
	 * @return the converted URL.
	 * @throws DispatcherException if there is no ICP installed to the
	 * command dispatcher or the transport protocols of the ICPs
	 * are not able to convert the specified URL.
	 */
	protected TransportAddress stringToAddr(String url) throws DispatcherException {
		Iterator peers = icps.values().iterator();
		
		while (peers.hasNext()) {
			
			// Try to convert the url using the TransportProtocol
			// supported by this ICP.
			try {
				// There can be more than one peer supporting the same
				// protocol. Use the first one.
				return ((ICP) ((List) peers.next()).get(0)).getProtocol().stringToAddr(url);
			}
			catch (Throwable t) {
				// Do nothing and try the next one.
			} 
		} 
		
		// If we reach this point the url can't be converted.
		throw new DispatcherException("can't convert URL "+url+".");
	} 
	
	/**
	 * Registers the specified skeleton to the command dispatcher.
	 * 
	 * @param skeleton a skeleton to be managed by the command
	 * dispatcher.
	 * @param remoteObject the remote object related to the specified
	 * skeleton.
	 */
	public synchronized void registerSkeleton(Skeleton skeleton, Object remotizedObject) throws IMTPException {
		Integer id = null;
		if(remotizedObject instanceof PlatformManager) {
			id = new Integer(0);
			name = "Service-Manager";
			setPlatformManager((PlatformManager) remotizedObject);
		}
		else {
			id = new Integer((int) (System.currentTimeMillis() & 0xffffff));
		}
		if (myLogger.isLoggable(Logger.FINE)) {
			myLogger.log(Logger.FINE, "Registering skeleton "+skeleton+" for remotized object "+remotizedObject+". ID is "+id);
		}
		skeletons.put(id, skeleton);
		ids.put(remotizedObject, id);
	}
	
	/**
	 * Deregisters the specified remote object from the command dispatcher.
	 * 
	 * @param remoteObject the remote object related to the specified
	 * skeleton.
	 */
	public synchronized void deregisterSkeleton(final Object remoteObject) {
		if (myLogger.isLoggable(Logger.FINE)) {
			myLogger.log(Logger.FINE, "Deregistering skeleton for remotized object "+remoteObject);
		}
		if (skeletons.size() == 1) {
			// This is the only skeleton --> The problem described below (in the "else" clause) 
			// can't happen. Moreover the JVM is likely going to exit --> We can't delay the skeleton deregistration
			removeRemoteObject(remoteObject);
		}
		else {        
			// Hack: If the PlatformManager monitoring this node is in the same 
			// JVM it needs some time to broadcast the termination of this node
			// to its replicas (if any) --> asynchronously deregister the skeleton after 
			// a while
			Thread t = new Thread() {
				public void run() {
					try {
						Thread.sleep(1000);
					}
					catch (InterruptedException ie) {}
					removeRemoteObject(remoteObject);
				}
			};
			t.start();
		} 
	}
	
	private synchronized void removeRemoteObject(Object remoteObject) {
		Object id = ids.remove(remoteObject);
		if (id != null) {
			if (myLogger.isLoggable(Logger.FINE)) {
				myLogger.log(Logger.FINE, "Asynchronous deregisteration of skeleton for remotized object "+remoteObject+". ID is "+id);
			}
	 		skeletons.remove(id);
		}
		
		// When there are no more skeletons, shutdown the CommandDispatcher (this closes all ICPs)
		if (ids.isEmpty()) {
			if (myLogger.isLoggable(Logger.FINE)) {
				myLogger.log(Logger.FINE, "All skeletons deregistered. Shutting down.");
			}
			shutDown();
		} 
	}
	
	public Stub buildLocalStub(Object remotizedObject) throws IMTPException {
		Stub stub = null;
		
		if (remotizedObject instanceof Node) {
			stub = new NodeStub(getID(remotizedObject), platformName);
		}
		else if (remotizedObject instanceof PlatformManager) {
			stub = new PlatformManagerStub(platformName);
		}
		else {
			throw new IMTPException("can't create a stub for object "+remotizedObject+".");
		}
		
		stub.bind(this);
		
		// Add the local addresses.
		Iterator it = addresses.iterator();
		while (it.hasNext()) {
			stub.addTA((TransportAddress) it.next());
		}
		
		return stub;
	}
	
	
	/**
	 * Selects a suitable peer and sends the specified serialized command
	 * to the specified transport address.
	 * 
	 * @param ta the transport addresses where the command should be
	 * sent.
	 * @param commandPayload the serialized command that is to be
	 * sent.
	 * @return a serialized response command from the receiving
	 * container.
	 * @throws UnreachableException if the destination address is not
	 * reachable.
	 */
	private byte[] send(TransportAddress ta, byte[] commandPayload, boolean requireFreshConnection) throws UnreachableException {
		
		// Get the ICPs suitable for the given TransportAddress.
		List list = (List) icps.get(ta.getProto().toLowerCase());
		
		if (list == null) {
			throw new UnreachableException("no ICP suitable for protocol "+ta.getProto()+".");
			
		} 
		
		ICPException lastException = null;
		for (int i = 0; i < list.size(); i++) {
			try {
				return ((ICP) list.get(i)).deliverCommand(ta, commandPayload, requireFreshConnection);
			} 
			catch (ICPException icpe) {
				lastException = icpe;
				// DEBUG
				// Print a warning and try next address
				//System.out.println("Warning: can't deliver command to "+ta+". "+icpe.getMessage());
			} 
		} 
		
		throw new UnreachableException("ICPException delivering command to address "+ta+".", lastException);
	} 
	
	/**
	 * Shuts the command dispatcher down and deactivates the local ICPs.
	 */
	private void shutDown() {
		Iterator peersKeys = icps.keySet().iterator();
		
		while (peersKeys.hasNext()) {
			List list = (List) icps.get(peersKeys.next());
			
			for (int i = 0; i < list.size(); i++) {
				try {
					// This call interrupts the listening thread of this peer
					// and waits for its completion.
					((ICP) list.get(i)).deactivate();
				} 
				catch (ICPException icpe) {
					// Do nothing as this means that this peer had never been activated.
				} 
			}
			list.clear();
		} 
		icps.clear();
		urls.clear();
		addresses.clear();
		thePlatformManager = null;
		name = DEFAULT_NAME;
		nextID = 1;
		removeDispatcher(platformName);
		platformName = null;
	} 
	
	// /////////////////////////////////////////
	// ICP.Listener INTERFACE
	// /////////////////////////////////////////
	
	/**
	 * Handles a received (still serialized) command object, i.e.
	 * deserialize it and launch processing of the command.
	 * 
	 * @param commandPayload the command to be deserialized and
	 * processed.
	 * @return a <tt>byte</tt> array containing the serialized response
	 * command.
	 * @throws LEAPSerializationException if the command cannot be
	 * (de-)serialized.
	 */
	public byte[] handleCommand(byte[] commandPayload) throws LEAPSerializationException {
		try {
			
			// Deserialize the incoming command.
			Command command = deserializeCommand(commandPayload);
			Command response = null;
			
			// DEBUG
			//System.out.println("Received command of type " + command.getCode());
			if (command.getCode() == Command.FORWARD) {
				
				// DEBUG
				// System.out.println("Routing command");
				
				// If this is a FORWARD command then handle it directly.
				byte[] originalPayload = (byte[]) command.getParamAt(0);
				List   destTAs = (List) command.getParamAt(1);
				String origin = (String) command.getParamAt(2);
				
				if (origin.equals(name)) {
					// The forwarding mechanism is looping.
					response = buildExceptionResponse(new UnreachableException("destination unreachable (and forward loop)."));
				} 
				else {
					try {
						response = dispatchSerializedCommand(destTAs, originalPayload, false, origin);
					} 
					catch (UnreachableException ue) {
						response = buildExceptionResponse(ue);
					} 
				} 
			} 
			else {
				
				// If this is a normal Command, let the proper Skeleton
				// process it.
				Integer id = new Integer(command.getObjectID());
				Skeleton s = (Skeleton) skeletons.get(id);
				if (s != null) {
					response = s.processCommand(command);
				}
				else {
					response = buildExceptionResponse(new DispatcherException("No skeleton for object-id "+id));
				}
			} 
			
			return serializeCommand(response);
		} 
		catch (LEAPSerializationException lse) {
			lse.printStackTrace();
			// Note that if the call below throws an exception this is not handled by
			// the CommandDispatcher. However this should never happen as an exception
			// response should always be serializable.
			return serializeCommand(buildExceptionResponse(new DispatcherException(lse.toString())));
		} 
		catch (Exception e) {
			// Note that if the call below throws an exception this is not handled by
			// the CommandDispatcher. However this should never happen as an exception
			// response should always be serializable.
			return serializeCommand(buildExceptionResponse(new DispatcherException(e.toString())));
		} 
	} 
	
	
	
}

