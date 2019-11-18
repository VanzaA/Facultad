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


import jade.core.behaviours.Behaviour;
import jade.util.Logger;
import jade.util.leap.HashMap;
import jade.util.leap.Iterator;
import jade.util.leap.Map;

/**
 
 The <code>BaseService</code> abstract class partially implements
 the <code>Service</code> interface, providing a simple and uniform
 mechanism for slice management and service discovery.
 Developers interested in creating JADE kernel level services should
 extend <code>BaseService</code> instead of directly implementing the 
 <code>Service</code> interface
 
 @author Giovanni Rimassa - FRAMeTech s.r.l.
 
 */
public abstract class BaseService implements Service {
	
	public static final String MAIN_SLICE = ServiceFinder.MAIN_SLICE;
	public static final String THIS_SLICE = ServiceFinder.THIS_SLICE;
	
	public static final String ALL_DUMP_KEY = "ALL";
	
	public void init(AgentContainer ac, Profile p) throws ProfileException {
		myFinder = p.getServiceFinder();
		myIMTPManager = p.getIMTPManager();
		
		slices = new HashMap(5);
		aliases = new HashMap(1);
		
		myLogger.log(jade.util.Logger.INFO, "Service "+getName()+" initialized");
	}
	
	// Package scoped method to receive the Command Processor from the
	// agent container at startup time. The rationale for avoiding
	// passing this to the constructor is to prevent concrete services
	// from accessing the Command Processor (they are less trusted than jade.core).
	// Notice that, for further measure, this method only works the first time it
	// is called.
	void setCommandProcessor(CommandProcessor cp) {
		if(myCommandProcessor == null) {
			myCommandProcessor = cp;
		}
	}
	
	
	/**
	 The <code>getSlice()</code> implementation of this class works
	 as follows:
	 <ol>
	 <li><i>First, the name alias table is used to convert the given
	 slice name into another name, if any</i></li>
	 
	 <li><i>Then, the new name (which may or may not be different
	 from the original one) is used to look up an internal table
	 keeping the service slices</i></li>
	 
	 <li><i>If no slice was found, the</i>
	 <code>ServiceFinder</code> <i>is asked to provide the slice,
	 which is then put into the local table.</i></li>
	 </ol>
	 */
	public Service.Slice getSlice(String name) throws ServiceException {
		
		// First look through the name alias table
		String realName = lookupAlias(name);
		
		// Then look up in the slice table
		Service.Slice s = (Service.Slice)slices.get(realName);
		
		// if there's not a suitable slice, ask the service finder,
		// then cache the result in the slices table.
	    if(s == null) {
			try {
				s = myFinder.findSlice(getName(), realName);
				slices.put(realName, s);
			}
			catch(IMTPException imtpe) {
				throw new ServiceException("IMTP Error while using the Service Finder", imtpe);
			}
		}
		
		return s;
		
	}
	
	/**
	 This method returns the current number of slices known to this
	 service <b>on this node</b>. Due to the distributed nature of
	 many JADE services, there is no guaranteed that calling this
	 method for the same service on different nodes will actually
	 result on the same number.
	 
	 @return The number of slices of this service that are known to
	 this node.
	 */
	public int getNumberOfSlices() {
		return slices.size();
	}
	
	public Node getLocalNode() throws IMTPException {
		return myIMTPManager.getLocalNode();
	}
	
	/**
	 The <code>getAllSlices()</code> implementation of this class
	 directly retrieves the current list of slices from the Service
	 Manager. Note that slices are retrieved directly from the Main and not 
	 from the cache.
	 */
	public Service.Slice[] getAllSlices() throws ServiceException {
		try {
			return myFinder.findAllSlices(getName());
		}
		catch(IMTPException imtpe) {
			throw new ServiceException("IMTP Error while using the Service Finder", imtpe);
		}
	}
	
	public void broadcast(HorizontalCommand cmd, boolean includeMyself) throws IMTPException, ServiceException {
		Service.Slice[] slices = myFinder.findAllSlices(getName());
		String localNodeName = getLocalNode().getName();
		for (int i = 0; i < slices.length; i++) {
			Service.Slice s = slices[i];
			String sliceName = s.getNode().getName();
			if (includeMyself || !sliceName.equals(localNodeName)) {
				s.serve(cmd);
				Object ret = cmd.getReturnValue();
				if (ret instanceof Throwable) {
					myLogger.log(Logger.WARNING, "Error propagating H-command " + cmd.getName() + " to slice " + sliceName, ((Throwable) ret));
				}
			}
		}
	}
	
	
	/**
	 This protected method allows subclasses to define their own
	 naming schemes, by adding aliases for existing slice names.
	 
	 @param alias The new alias name.
	 @param name The real name this alias must be mapped to.
	 */
	protected void addAlias(String alias, String name) {
		aliases.put(alias, name);
	}
	
	/**
	 This protected method is used by <code>getSlice()</code> to
	 dereference aliases for slice names. Subclasses can override
	 this method to build their own service-specific naming schema.
	 
	 @param alias The alias name to map to a real slice name.
	 @return A mapped name, or the original one if no mapping was
	 found.
	 */
	protected String lookupAlias(String alias) {
		String result = (String)aliases.get(alias);
		if(result != null) {
			return result;
		}
		else {
			return alias;
		}
	}
	
	/** 
	 This should be properly implemented
	 by the services that have filters.
	 Note that when called multiple times with the same value of the <code>direction</code> 
	 parameter this method MUST always return the same object!
	 */
	public Filter getCommandFilter(boolean direction) {
		return null;
	}
	
	/** 
	 This should be properly implemented
	 by the services that have sinks.
	 */
	public Sink getCommandSink(boolean direction) {
		return null;
	}
	
	/** 
	 This should be properly implemented
	 by the services that owns vertival commands.
	 */
	public String[] getOwnedCommands() {
		return null;
	}
	
	/** 
	 This should be properly implemented
	 by the services that have non-empty slices.
	 */
	public Class getHorizontalInterface() {
		return null;
	}
	
	/** 
	 This should be properly implemented
	 by the services that have non-empty slices.
	 */
	public Service.Slice getLocalSlice() {
		return null;
	}
	
	public boolean isLocal() {
		return false;
	}
	
	/** 
	 This should be properly implemented
	 by the services that have helpers.
	 */
	public ServiceHelper getHelper(Agent a) throws ServiceException {
		return null;
	}
	
	/** 
	 This should be properly implemented
	 by the services that require a service specific Behaviour 
	 running in the AMS.
	 */
	public Behaviour getAMSBehaviour() {
		return null;
	}
	
	public void boot(Profile p) throws ServiceException {
		// Empty placeholder method
	}
	
	public void shutdown() {
		// Empty placeholder method
	}
	
	public Object submit(VerticalCommand cmd) throws ServiceException {
		String cmdName = cmd.getName();
		String[] ownedCommands = getOwnedCommands();
		
		for(int i = 0; i < ownedCommands.length; i++) {
			if(cmdName.equals(ownedCommands[i])) {
				return myCommandProcessor.processOutgoing(cmd);
			}
		}
		
		throw new ServiceException("Command <" + cmdName + "> does not belong to service <" + getName() + ">");
		
	}
	
	protected Service.Slice getFreshSlice(String name) throws ServiceException {
		clearCachedSlice(name);
		// Get a newer slice and return it
		return getSlice(name);
	}
	
	protected IMTPManager getIMTPManager() {
		return myIMTPManager;
	}

	protected void clearCachedSlice(String name) {
		// slices may be null when calling this method on the Main Container, but on a service not active on the Main Container
		// --> In this case the service has not been initialized.
		if (slices != null) {
			// First look through the name alias table
			String realName = lookupAlias(name);
			// Invalidate the cache entry
			slices.remove(realName);
		}
	}
	
	/**
	 * This method can be redefined to support service internal data inspection by means of the ContainerMonitorAgent
	 * included in the misc add-on.
	 * The default implementation just dumps the map of cached slices
	 * @param key A hint indicating which service data should be dumped
	 * @return A string representation of the service internal data
	 */
	public String dump(String key) {
		if (key == null || key.equals(ALL_DUMP_KEY)) {
			StringBuffer sb = new StringBuffer("LOCAL: ").append(isLocal()).append('\n');
			sb.append("CACHED SLICES:\n");
			Iterator it = slices.keySet().iterator();
			while (it.hasNext()) {
				String name = (String) it.next();
				sb.append("- ").append(name).append(" --> "+stringifySlice((Slice) slices.get(name))).append("\n");
			}
			
			return sb.toString();
		}
		else {
			return "";
		}
	}

	public static final String stringifySlice(Slice s) {
		StringBuffer sb = new StringBuffer("SLICE ");
		try {
			sb.append(s.getClass().getName()).append(": node = "+s.getNode().getName());
		}
		catch (ServiceException se) {
			// Should never happen as this is a local call
			se.printStackTrace();
		}
		return sb.toString();
	}
	
	//#MIDP_EXCLUDE_BEGIN
	protected CallbackInvokator createInvokator() {
		return new CallbackInvokator();
	}
	//#MIDP_EXCLUDE_END
	
	private IMTPManager myIMTPManager;
	
	private CommandProcessor myCommandProcessor = null;
	
	private Map slices;
	private Map aliases;
	
	protected ServiceFinder myFinder;
	protected transient jade.util.Logger myLogger = jade.util.Logger.getMyLogger(getName());
}
