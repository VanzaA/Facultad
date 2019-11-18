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

//#MIDP_EXCLUDE_BEGIN
import java.net.InetAddress;
//#DOTNET_EXCLUDE_BEGIN
import java.net.NetworkInterface;
import java.net.Inet4Address;
import java.net.Inet6Address;
//#DOTNET_EXCLUDE_END
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
//#MIDP_EXCLUDE_END

import jade.mtp.TransportAddress;
import jade.util.leap.List;
import jade.util.leap.Properties;

/**
 * This class allows retrieving configuration-dependent classes.
 * 
 * @author  Federico Bergenti
 * @author  Giovanni Caire - TILAB
 * @version 1.0, 22/11/00
 */
public abstract class Profile {
	
	/**
	 This constant is the name of the property whose value contains a
	 boolean indicating if this is the Main Container or a peripheral
	 container.
	 */
	public static final String MAIN = "main";
	
	/**
	 This constant is the name of the property whose value is a String
	 indicating the protocol to use to connect to the Main Container.
	 */
	public static final String MAIN_PROTO = "proto";
	
	/**
	 This constant is the name of the property whose value is the name
	 (or the IP address) of the network host where the JADE Main
	 Container is running.
	 */
	public static final String MAIN_HOST = "host";
	
	/**
	 This constant is the name of the property whose value contains an
	 integer representing the port number where the Main Container is
	 listening for container registrations.
	 */
	public static final String MAIN_PORT = "port";
	
	/**
	 This constant is the name of the property whose Boolean value
	 tells whether to activate the automatic main container detection mechanism.
	 By means of this mechanism a peripheral container is able to automatically detect the 
	 main container host and port at startup time. The mechanism is based on IP multicast communication
	 and must be activated on both the main container, that publishes its host and port on a given 
	 multicast address (by default 239.255.10.99, port 1199), and on peripheral containers.    
	 The default for this option is <code>true</code> on Main Containers and <code>false</code> 
	 on peripheral containers 
	 */
	public static final String DETECT_MAIN = "detect-main";
	
	/**
	 This constant is the name of the property whose value contains
	 the host name the container must bind on. The host name must
	 refer to the local machine, and is generally needed only when
	 multiple network interfaces are present or a non-default name is
	 desired.
	 */
	public static final String LOCAL_HOST = "local-host";
	
	/**
	 * This constant is the name of the property whose value (true or false) instructs 
	 * JADE to try to use host names instead of IP addresses in intra-platform communication.
	 * Default is false (IP addresses are used preferentially)
	 */
	public static final String PRIVILEDGE_LOGICAL_NAME = "priviledge-logical-name";
	
	/**
	 This constant is the name of the TCP port the container node must
	 listen to for incoming IMTP messages.
	 */
	public static final String LOCAL_PORT = "local-port";
	
	public static final String GUI = "gui";
	
	/**
	 This constant is the name of the property whose Boolean value
	 tells whether a local Service Manager is exported by this
	 container (only when using JADE support for fault-tolerant
	 platform configurations).
	 */
	public static final String LOCAL_SERVICE_MANAGER = "backupmain";
	
	/**
	 This constant is the name of the property whose Boolean value
	 tells whether startup options should be dumped. Default is false
	 */
	public static final String DUMP_OPTIONS = "dump-options";
	
	/**
	 * This constant, when set to <code>true</code> tells the JADE runtime that it will be executed 
	 * in an environment with no display available. 
	 */
	public static final String NO_DISPLAY = "no-display";
	
	//#APIDOC_EXCLUDE_BEGIN
	public static final String OWNER = "owner";
	
	// On J2SE and pJava, install mobility and notification services by default
	//#J2ME_EXCLUDE_BEGIN
	public static final String DEFAULT_SERVICES = "jade.core.mobility.AgentMobilityService;jade.core.event.NotificationService";
	public static final String DEFAULT_SERVICES_NOMOBILITY = "jade.core.event.NotificationService";
	//#J2ME_EXCLUDE_END
	
	// On PJAVA the Notification service is not supported
	//#DOTNET_EXCLUDE_BEGIN
	/*#PJAVA_INCLUDE_BEGIN
	 public static final String DEFAULT_SERVICES = "jade.core.mobility.AgentMobilityService";
	 public static final String DEFAULT_SERVICES_NOMOBILITY = "";
	 #PJAVA_INCLUDE_END*/
	//#DOTNET_EXCLUDE_END
	
	// On DOTNET the Notification service is  supported
	/*#DOTNET_INCLUDE_BEGIN
	 public static final String DEFAULT_SERVICES = "jade.core.mobility.AgentMobilityService;jade.core.event.NotificationService";
	 public static final String DEFAULT_SERVICES_NOMOBILITY = "jade.core.event.NotificationService";
	 #DOTNET_INCLUDE_END*/
	
	// On MIDP, no additional services are installed by default
	/*#MIDP_INCLUDE_BEGIN
	 public static final String DEFAULT_SERVICES = "";
	 #MIDP_INCLUDE_END*/
	
	//#APIDOC_EXCLUDE_END
	
	
	/**
	 This constant is the name of the property whose value contains
	 the unique platform ID of a JADE platform. Agent GUIDs in JADE
	 are made by a platform-unique nickname, the '@' character and the
	 platform ID.
	 */
	public static final String PLATFORM_ID = "platform-id";
	
	/**
	 This constant is the name of the property whose value contains
	 the user authentication type to be used to login to the JADE platform.
	 */
	public static final String USERAUTH_KEY = "userauth-key";
	
	/**
	 This constant is the name of the property whose value contains the
	 list of agents that have to be launched at bootstrap time
	 */
	public static final String AGENTS = "agents";
	
	/**
	 This constants is the name of the property whose value contains
	 the list of kernel-level services that have to be launched at
	 bootstrap time
	 */
	public static final String SERVICES = "services";
	
	/**
	 This constant is the name of the property whose value contains the
	 list of addresses through which the platform <i>Service
	 Manager</i> can be reached.
	 */
	public static final String REMOTE_SERVICE_MANAGER_ADDRESSES = "smaddrs";
	
	/**
	 * This constant is the key of the property whose value contains the
	 * list of MTPs that have to be launched at bootstrap time.
	 * This list must be retrieved via the <code>getSpecifiers(MTPS)<code>
	 * method.
	 */
	public static final String MTPS = "mtps";
	
	public static final String NO_MTP = "nomtp";
	
	/**
	 * This constant is the key of the property whose value
	 * identifies the IMTP Manager to be created by ProfileImpl
	 **/
	public static final String IMTP = "imtp";
	
	/**
	 * This constant is the key of the property whose value contains
	 * the desired name of the container. If this container name exists
	 * already, then a default name is assigned by the platform.
	 * The name of the main-container is always assigned by the platform
	 * and cannot be changed.
	 **/
	public static final String CONTAINER_NAME = "container-name";
	
	/**
	 * This constant is the key of the property whose value contains the
	 * list of ACLCODECSs that have to be launched at bootstrap time.
	 * This list must be retrieved via the <code>getSpecifiers(ACLCODECS)<code>
	 * method.
	 */
	public static final String ACLCODECS = "aclcodecs";
	
	/**
	 This constant is the key of the property whose value (true or false)
	 indicates whether or not this platform accepts foreign agents i.e.
	 agents whose names are not of the form <local-name>@<platform-name>.
	 */
	public static final String ACCEPT_FOREIGN_AGENTS = "accept-foreign-agents";
	
	public static final String STYLE_3_X = "style3-x";
		
	/**
	 * This constant is the key of the property whose value contains
	 * the name of the directory where all the files generated by JADE
	 * should be put. The default value is the current directory.
	 **/
	public static final String FILE_DIR = "file-dir";
	
	/**
	 * This constant is the key of the property whose value contains
	 * the version of IP address to use.
	 */
	public static final String IP_VERSION = "ip-version";
	
	public static final int IPV4 = 4;
	public static final int IPV6 = 6;
	
	public static final int DEFAULT_PORT = 1099;
	
	public static final String LOCALHOST_CONSTANT = "localhost";
	public static final String LOOPBACK_ADDRESS_CONSTANT = "127.0.0.1";

	public static final String LEAP_IMTP = "LEAP";
	public static final String RMI_IMTP = "RMI";
	
	//#APIDOC_EXCLUDE_BEGIN
	/**
	 * This constant is the key of the property whose value contains the
	 * indication about the type of JVM. 
	 */
	public static final String JVM = "jvm";
	public static final String J2SE = "j2se";
	public static final String PJAVA = "pjava";
	public static final String MIDP = "midp";
	public static final String ANDROID = "android";

	
	
	/**
	 Obtain a reference to the platform <i>Service Manager</i>, with
	 which kernel-level services can be added and removed.
	 @return A <code>ServiceManager</code> object, representing the
	 platform service manager.
	 */
	protected abstract ServiceManager getServiceManager() throws ProfileException;
	
	/**
	 Obtain a reference to the platform <i>Service Finder</i>, with
	 which kernel-level services can be looked up.
	 @return A <code>ServiceFinder</code> object, representing the
	 platform service manager.
	 */
	protected abstract ServiceFinder getServiceFinder() throws ProfileException;
	
	/**
	 Obtain a reference to the container <i>Command Processor</i>,
	 which manages kernel-level commands dispatching them to the
	 proper platform services.
	 @return A <code>ServiceManager</code> object, representing the
	 platform service manager.
	 */
	protected abstract CommandProcessor getCommandProcessor() throws ProfileException;
	
	//#MIDP_EXCLUDE_BEGIN
	protected abstract MainContainerImpl getMain() throws ProfileException;
	//#MIDP_EXCLUDE_END
	
	/**
	 */
	protected abstract IMTPManager getIMTPManager() throws ProfileException;
	
	/**
	 */
	public abstract ResourceManager getResourceManager() throws ProfileException;

	//#APIDOC_EXCLUDE_END
	
	
	//#MIDP_EXCLUDE_BEGIN
	/**
	 * Retrieve the configuration properties as they were passed to this Profile object, i.e. without 
	 * internal initializations automatically performed by the Profile class. 
	 */
	public abstract Properties getBootProperties();
	//#MIDP_EXCLUDE_END
	
	/**
	 * Retrieve a String value from the configuration properties.
	 * If no parameter corresponding to the specified key is found,
	 * return the provided default.
	 * @param key The key identifying the parameter to be retrieved
	 * among the configuration properties.
	 * @param aDefault The value to return when there is no property
	 * set for the given key.
	 */
	public abstract String getParameter(String key, String aDefault);
	
	/**
	 * Retrieve a boolean value for a configuration property.  If no
	 * corresponding property is found or if its string value cannot
	 * be converted to a boolean one, a default value is returned.
	 * @param key The key identifying the parameter to be retrieved
	 * among the configuration properties.
	 * @param aDefault The value to return when there is no property
	 * set for the given key, or its value cannot be converted to a
	 * boolean value.
	 */
	public abstract boolean getBooleanProperty(String key, boolean aDefault);
	
	/**
	 * Retrieve a list of Specifiers from the configuration properties.
	 * Agents, MTPs and other items are specified among the configuration
	 * properties in this way.
	 * If no list of Specifiers corresponding to the specified key is found,
	 * an empty list is returned.
	 * @param key The key identifying the list of Specifires to be retrieved
	 * among the configuration properties.
	 */
	public abstract List getSpecifiers(String key) throws ProfileException;
	
	/**
	 * Assign the given value to the given property name.
	 *
	 * @param key is the property name
	 * @param value is the property value
	 *
	 */
	public abstract void setParameter(String key, String value);
	
	/**
	 * Assign the given value to the given property name.
	 *
	 * @param key is the property name
	 * @param value is the property value
	 *
	 */
	public abstract void setSpecifiers(String key, List value);
	
	
	/**
	 * Provides a boolean indication about whether or not the container using this Profile
	 * is a Main Container. 
	 * @return <code>true</code> if the container using this Profile is a Main Container. <code>false</code> otherwise
	 */
	public boolean isMain() {
		return getBooleanProperty(MAIN, false);
	}
	
	/**
	 * Provides a boolean indication about whether or not the container using this Profile
	 * is the Master Main Container. 
	 * @return <code>true</code> if the container using this Profile is the Master Main Container. <code>false</code> otherwise
	 */
	public boolean isMasterMain() {
		return isMain() && !getBooleanProperty(LOCAL_SERVICE_MANAGER, false);
	}	
	
	/**
	 * Provides a boolean indication about whether or not the container using this Profile
	 * is a Backup Main Container. 
	 * @return <code>true</code> if the container using this Profile is a Backup Main Container. <code>false</code> otherwise
	 */
	public boolean isBackupMain() {
		return isMain() && getBooleanProperty(LOCAL_SERVICE_MANAGER, false);
	}
	
	public static String getDefaultNetworkName() {
		return getDefaultNetworkName(false);
	}
	
	public static String getDefaultNetworkName(boolean priviledgeLogicalName) {
		String host = LOCALHOST_CONSTANT;
		//#DOTNET_EXCLUDE_BEGIN
		//#MIDP_EXCLUDE_BEGIN
		try {
			if (priviledgeLogicalName) {
				try {
					host = InetAddress.getLocalHost().getCanonicalHostName();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (host.equals(LOCALHOST_CONSTANT) || host.equals(LOOPBACK_ADDRESS_CONSTANT)) {
				for (Enumeration en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
					NetworkInterface intf = (NetworkInterface)en.nextElement();
					for (Enumeration enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
						InetAddress inetAddress = (InetAddress)enumIpAddr.nextElement();
						if (!inetAddress.isLoopbackAddress()) {
							int useIPVersion = Integer.parseInt(System.getProperty(IP_VERSION, String.valueOf(IPV4)));
							if (!inetAddress.isLoopbackAddress()) {
								switch (useIPVersion) {
								case IPV4:
									if (inetAddress instanceof Inet4Address) {
										return  inetAddress.getHostAddress().toString();
									}
									break;
								case IPV6:
									if (inetAddress instanceof Inet6Address) {
										return  inetAddress.getHostAddress().toString();
									}
									break;
								default:
									return  inetAddress.getHostAddress().toString();
								}
							}
						}
					}
				}
			}
		}
		catch(Exception e) {
		}
		//#MIDP_EXCLUDE_END
		//#DOTNET_EXCLUDE_END
		return host;
	}
	
	//#MIDP_EXCLUDE_BEGIN
	/**
	 * Check whether or not a given host name or address corresponds to a local network interface
	 */
	public static boolean isLocalHost(String host) {
		// Fix by Felix Krull:
		// We cannot use the same approach used in compareHostNames(). This fails in case you set 
		// local-host in the container config to a local address which is not the address delivered by 
		// InetAddress.getLocalHost().getHostName(), but the address of another local interface
		if (LOCALHOST_CONSTANT.equalsIgnoreCase(host) || LOOPBACK_ADDRESS_CONSTANT.equals(host)) {
			return true;
		}
		
		// The trick here is to compare the IP Addresses the host name resolves to.
		try {
			InetAddress localHostAddr = InetAddress.getByName(host);
			
			//#DOTNET_EXCLUDE_BEGIN
			Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
			while(interfaces.hasMoreElements()) {
				NetworkInterface nextIface = (NetworkInterface)interfaces.nextElement();
				Enumeration addresses = nextIface.getInetAddresses();
				while(addresses.hasMoreElements()) {
					InetAddress ip = (InetAddress)addresses.nextElement();
					if(ip.equals(localHostAddr)) {
						return true;
					}
				}
			}
			//#DOTNET_EXCLUDE_END
			
			/*#DOTNET_INCLUDE_BEGIN
			return localHostAddr.equals(InetAddress.getLocalHost());
			#DOTNET_INCLUDE_END*/
		}
		//#DOTNET_EXCLUDE_BEGIN
		catch (SocketException e1) {
			// We cannot retrieve local network interfaces information --> print a warning and return false
			e1.printStackTrace();
		}
		//#DOTNET_EXCLUDE_END
		catch (UnknownHostException uhe) {
			// An unknown host is certainly not local		
		}
		return false;		
	}
	
	/**
	 * Compares two host names/addresses regardless of whether they include domain or not.
	 */
	public static boolean compareHostNames(String host1, String host2) {
		if (host1 == null || host2 == null) {
			return false;
		}
		
		if (host1.equalsIgnoreCase(host2)) {
			return true;
		}

		try {
			if (host1.equalsIgnoreCase(LOCALHOST_CONSTANT) || host1.equals(LOOPBACK_ADDRESS_CONSTANT)) {
				return isLocalHost(host2);
			}
			if (host2.equalsIgnoreCase(LOCALHOST_CONSTANT) || host2.equals(LOOPBACK_ADDRESS_CONSTANT)) {
				return isLocalHost(host1);
			}

			InetAddress host1Addrs[] = InetAddress.getAllByName(host1);
			InetAddress host2Addrs[] = InetAddress.getAllByName(host2);

			// The trick here is to compare the InetAddress
			// objects, not the strings since the one string might be a
			// fully qualified Internet domain name for the host and the
			// other might be a simple name.
			// Example: myHost.hpl.hp.com and myHost might
			// actually be the same host even though the hostname strings do
			// not match.  When the InetAddress objects are compared, the IP
			// addresses will be compared.
			int i = 0;
			boolean isEqual = false;

			while ((!isEqual) && (i < host1Addrs.length)) {
				int j = 0;

				while ((!isEqual) && (j < host2Addrs.length)) {
					isEqual = host1Addrs[i].equals(host2Addrs[j]);
					j++;
				}

				i++;
			}
			return isEqual;
		}
		catch (UnknownHostException uhe) {
			// If we can't retrieve the necessary information and the two strings are not the same,
			// we have no chance to make a correct comparison --> return false
			return false;
		}
	}
	
	/**
	 * Compares two stringified transport addresses.
	 * The provided IMTPManager must be able to deal with the protocols (e.g. http) the two transport address 
	 * refer to.
	 */
	public static boolean compareTransportAddresses(String addr1, String addr2, IMTPManager imtpManager) {
		try {
			TransportAddress ta1 = imtpManager.stringToAddr(addr1);
			TransportAddress ta2 = imtpManager.stringToAddr(addr2);
			return compareTransportAddresses(ta1, ta2);
		}
		catch (Exception e) {
			// If we can't parse the addresses, just compare them as strings
			return CaseInsensitiveString.equalsIgnoreCase(addr1, addr2);
		}
	}
	
	public static boolean compareTransportAddresses(TransportAddress ta1, TransportAddress ta2) {
		String proto1 = ta1.getProto();
		String proto2 = ta2.getProto();
		if (((proto1 == null) && (proto2 == null)) || CaseInsensitiveString.equalsIgnoreCase(proto1, proto2)) {
			// Both protocols not specified or equals --> OK
			String port1 = ta1.getPort();
			String port2 = ta2.getPort();
			if (((port1 == null) && (port2 == null)) || CaseInsensitiveString.equalsIgnoreCase(port1, port2)) {
				// Both ports not specified or equals --> OK
				if (Profile.compareHostNames(ta1.getHost(), ta2.getHost())) {
					return true;
				}
			}				
		}
		return false;
	}
	//#MIDP_EXCLUDE_END
	
}

