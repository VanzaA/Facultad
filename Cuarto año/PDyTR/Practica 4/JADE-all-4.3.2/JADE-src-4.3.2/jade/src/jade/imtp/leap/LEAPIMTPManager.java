/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/*
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


import jade.core.*;
import jade.lang.acl.ACLMessage;
import jade.mtp.TransportAddress;
import jade.util.leap.*;
import jade.imtp.leap.JICP.JICPProtocol;
import jade.imtp.leap.JICP.Connection;
import jade.util.Logger;
import java.io.IOException;

/**
 * @author Giovanni Caire - Telecom Italia Lab
 */
public class LEAPIMTPManager implements IMTPManager {
	
	/**
	 * Profile keys for LEAP-IMTP specific parameters
	 */
	static final String MAIN_URL = "main-url";
	
	public static final String CHANGE_PORT_IF_BUSY = "jade_imtp_leap_LEAPIMTPManager_changeportifbusy";
	
	private static final String ICPS = "icps";
	private static final String ROUTER_URL = "router-url";
	
	/**
	 * Local pointer to the singleton command dispatcher in this JVM
	 */
	private CommandDispatcher theDispatcher = null;
	
	/**
	 * The Profile holding the configuration for this IMTPManager
	 */
	private Profile theProfile = null;
	
	private String masterPMAddr;
	private String localAddr;
	
	private NodeLEAP localNode;
	
	Logger logger = Logger.getMyLogger(getClass().getName());
	
	/**
	 * Default constructor used to dynamically instantiate a new
	 * LEAPIMTPManager object
	 */
	public LEAPIMTPManager() {
	}
	
	// ////////////////////////////////
	// IMTPManager Interface
	// ////////////////////////////////
	
	/**
	 * Initialize the support for intra-platform communication
	 */
	public void initialize(Profile p) throws IMTPException {
		theProfile = p;
		
		// Get the CommandDispatcher for the local platform
		String platformName = p.getParameter(Profile.PLATFORM_ID, null);
		theDispatcher = CommandDispatcher.getDispatcher(platformName);
		
		// Add to the CommandDispatcher the ICPs specified in the Profile
		try {
			// Set defaults if not explicitly set. 
			setDefaults();
			
			List     l = theProfile.getSpecifiers(ICPS);
			Iterator it = l.iterator();
			
			while (it.hasNext()) {
				Specifier s = (Specifier) it.next();
				
				try {
					ICP peer = (ICP) Class.forName(s.getClassName()).newInstance();
					String id = (s.getArgs() != null ? (String) (s.getArgs()[0]) : null);
					theDispatcher.addICP(peer, id, theProfile);
				} 
				catch (Exception e) {
					if(logger.isLoggable(Logger.SEVERE))
						logger.log(Logger.SEVERE,"Error adding ICP. "+e);
				} 
			}
			
			// Initialize the local node
			localNode = new NodeLEAP("No-Name", theProfile.getBooleanProperty(Profile.MAIN, true), theDispatcher);
			Skeleton skel = new NodeSkel(localNode);
			theDispatcher.registerSkeleton(skel, localNode);
		}
		catch (ProfileException pe) {
			// Just print a warning
			if(logger.isLoggable(Logger.SEVERE))
				logger.log(Logger.SEVERE,"Profile error. "+pe.getMessage());
		} 
		
		// Now check that some ICP is active. Note that, as a CommandDispatcher
		// can serve multiple IMTPManagers, some ICPs could be already active.
		List URLs = theDispatcher.getLocalURLs();
		
		if (URLs.size() == 0) {
			throw new IMTPException("No ICP active");
		} 
		else {
			localAddr = (String) URLs.get(0);
			Iterator it = URLs.iterator();
						
			StringBuffer sb = new StringBuffer("Listening for intra-platform commands on address:\n");
			
			while (it.hasNext()) {
				sb.append("- "+(String) it.next()+"\n");
			}
			logger.log(Logger.INFO,sb.toString());
		}
		
		// Be sure the mainURL uses the host address. Note that this must
		// be done after ICP installation
		adjustMainURL();
		
		// If this is a backup main get the address of the master PlatformManager
		if (theProfile.isBackupMain()) {
			// This node hosts a real PlatformManager that is NOT the master PlatformManager
			// --> MAIN_URL points to the master PlatformManager
			masterPMAddr = theProfile.getParameter(MAIN_URL, null);
		}
		
		// Finally, if a URL for the default router is specified in the
		// Profile, set it in the CommandDispatcher
		theDispatcher.setRouterAddress(theProfile.getParameter(ROUTER_URL, null));
	}
	
	private void adjustMainURL() {
		//#MIDP_EXCLUDE_BEGIN
		try {
			String mainURL = theProfile.getParameter(MAIN_URL, null);
			TransportAddress ta = theDispatcher.stringToAddr(mainURL);
			java.net.InetAddress ad = java.net.InetAddress.getByName(ta.getHost());
			TransportProtocol tp = theDispatcher.getProtocol(ta.getProto());
			String hostAddr = ad.getHostAddress();
			if (hostAddr.equals("127.0.0.1")) {
				hostAddr = ad.getHostName();
			}
			ta = tp.buildAddress(hostAddr, ta.getPort(), ta.getFile(), ta.getAnchor());
			// DEBUG
			//System.out.println("MAIN URL set to "+tp.addrToString(ta));
			theProfile.setParameter(MAIN_URL, tp.addrToString(ta));
		}
		catch (Exception e) {
			// Ignore it
		}
		//#MIDP_EXCLUDE_END
	}
	
	public Node getLocalNode() throws IMTPException {
		return localNode;
	}
	
	//#MIDP_EXCLUDE_BEGIN
	public void exportPlatformManager(PlatformManager mgr) throws IMTPException {
		Skeleton skel = new PlatformManagerSkel(mgr, this);
		mgr.setLocalAddress(localAddr);
		theDispatcher.registerSkeleton(skel, mgr);
		
		// Attach to the original Platform manager, if any
		if (masterPMAddr != null) {
			PlatformManager masterPM = theDispatcher.getPlatformManagerStub(masterPMAddr);
			
			try {
				((PlatformManagerImpl) mgr).setPlatformName(masterPM.getPlatformName());
				logger.log(Logger.INFO, "Connecting to master Main Container at address "+masterPMAddr);
				mgr.addReplica(masterPMAddr, true); // Do as if it was a propagated info
				masterPM.addReplica(localAddr, false);
			}
			catch (ServiceException se) {
				throw new IMTPException("Cannot attach to the original PlatformManager.", se);
			}
			catch (IMTPException imtpe) {
				Throwable t = imtpe.getNested();
				if ((t != null) && (t instanceof UnreachableException)) {
					// The master main container does not exist. Become the leader
					logger.log(Logger.INFO,"No master Main Container found at address "+masterPMAddr+". Take the leadership");
					masterPMAddr = null;
					theProfile.setParameter(Profile.LOCAL_SERVICE_MANAGER, "false");
				}
				else {
					throw imtpe;
				}
			}		  	
		}
	}
	
	public void unexportPlatformManager(PlatformManager pm) throws IMTPException {
		theDispatcher.deregisterSkeleton(pm);
	}
	//#MIDP_EXCLUDE_END
	
	public PlatformManager getPlatformManagerProxy() throws IMTPException {
		return theDispatcher.getPlatformManagerProxy(theProfile);
	}
	
	public PlatformManager getPlatformManagerProxy(String addr) throws IMTPException {
		return theDispatcher.getPlatformManagerStub(addr);
	}
	
	public void reconnected(PlatformManager pm) {
		theDispatcher.setPlatformManagerProxy(pm);
	}
	
	// FIXME: this is not IMTP-dependent --> Should be moved elsewhere
	public Service.Slice createSliceProxy(String serviceName, Class itf, Node where) throws IMTPException {
		try {
			Class proxyClass = Class.forName(serviceName + "Proxy");
			Service.Slice proxy = (Service.Slice) proxyClass.newInstance();
			if (proxy instanceof SliceProxy) {
				((SliceProxy) proxy).setNode(where);
			}
			//#DOTNET_EXCLUDE_BEGIN
			else if (proxy instanceof Service.SliceProxy) {
				((Service.SliceProxy) proxy).setNode(where);
			}
			//#DOTNET_EXCLUDE_END
			else {
				throw new IMTPException("Class "+proxyClass.getName()+" is not a slice proxy.");
			}
			return proxy;
		}
		catch(Exception e) {
			throw new IMTPException("Error creating a slice proxy", e);
		}
	}
	
	/**
	 * Release all resources of this LEAPIMTPManager
	 */
	public void shutDown() {
		try {
			if (localNode != null) {
				localNode.exit();
				theDispatcher.deregisterSkeleton(localNode);
			}
		}
		catch (IMTPException imtpe) {
			// Should never happen since this is a local call
			imtpe.printStackTrace();
		}
	} 
	
	/**
	 */
	public List getLocalAddresses() {
		return theDispatcher.getLocalTAs();
	} 
	
	// /////////////////////////
	// PRIVATE METHODS
	// /////////////////////////
	
	/**
	 In the Profile ICPS can be specified as follows.
	 1) If there is only one ICP -->
	 icps = <icp-class-name>
	 <param-key> = <param-value>
	 .......
	 2) In general (typically used when there are 2 or more ICPs)
	 icps = <icp1-class-name>(<icp1-id>);<icp2-class-name>(<icp2-id>)...
	 <icp1-id>-<param-key> = <param-value>
	 .......
	 <icp2-id>-<param-key> = <param-value>
	 .......
	 
	 If there is no ICP indication set it as follows. 
	 a) Peripheral container / J2SE or PJAVA
	 - There is at least 1 ICP already active --> Nothing
	 - There are no ICPs already active --> JICPPeer
	 b) Peripheral container / MIDP --> Throws a ProfileException
	 c) Main container / J2SE or PJAVA --> JICPPeer
	 */
	private void setDefaults() throws ProfileException {
		// - MAIN URL (Useful for peripheral containers and backup main. Ignored otherwise
		String mainURL = theProfile.getParameter(MAIN_URL, null);
		if (mainURL == null) {
			String mainHost = getMainHost();
			if (mainHost != null) {
				String mainProto = theProfile.getParameter(Profile.MAIN_PROTO, "jicp");
				String mainPort = theProfile.getParameter(Profile.MAIN_PORT, null);
				mainPort = (mainPort != null) ? (":" + mainPort) : (":" + JICPProtocol.DEFAULT_PORT);
				mainURL = new String(mainProto + "://" + mainHost + mainPort);
				theProfile.setParameter(MAIN_URL, mainURL);
			}
		}
		
		if (!theProfile.getBooleanProperty(Profile.MAIN, true)) {
			// ICPS for a "Peripheral Container"
			if (theProfile.getParameter(ICPS, null) == null) {
				String jvm = theProfile.getParameter(Profile.JVM, null);
				if (Profile.MIDP.equals(jvm)) {
					// Should never happen
					throw new ProfileException("Stand-alone execution mode not supported in MIDP");
				}
				else {
					// Set default ICPS for J2SE and PJAVA
					if (theDispatcher.getLocalURLs().size() == 0) {
						theProfile.setParameter(ICPS, "jade.imtp.leap.JICP.JICPPeer");
					}
				}
			}
		}
		else {
			// ICPS for a Main Container
			if (theProfile.getParameter(ICPS, null) == null) {
				theProfile.setParameter(ICPS, "jade.imtp.leap.JICP.JICPPeer");
			}
		}  
	}
	
	/**
	 */
	private String getMainHost() {
		String host = theProfile.getParameter(Profile.MAIN_HOST, null);
		//#MIDP_EXCLUDE_BEGIN
		if (host == null) {
			host = Profile.getDefaultNetworkName();
		}
		//#MIDP_EXCLUDE_END
		return host;
	}
	
	public TransportAddress stringToAddr(String addr) throws IMTPException {
		try {
			return theDispatcher.stringToAddr(addr);
		}
		catch (DispatcherException de) {
			throw new IMTPException(de.getMessage());
		}
	}
	
}

