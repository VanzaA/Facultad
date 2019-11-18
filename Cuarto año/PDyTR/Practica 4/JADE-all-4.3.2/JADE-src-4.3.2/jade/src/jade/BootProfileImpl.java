/**
 * 
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 *
 * GNU Lesser General Public License
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 *
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
 * 
 */
package jade;

//#APIDOC_EXCLUDE_FILE
//#J2ME_EXCLUDE_FILE

import java.net.InetAddress;

import java.util.Enumeration;    // J2ME CLDC OK
import java.util.Vector;         // J2ME CLDC OK
import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import jade.util.leap.Properties;
import jade.util.ExtendedProperties;
import jade.util.PropertiesException;

import jade.core.ProfileImpl;
import jade.core.Profile;
import jade.core.Specifier;

/**
 * A profile implementation enhanced to support boot's
 * argument parsing. This class serves as the bridge between
 * boot properties and profile properties. It defines a
 * collection of property keys which correspond to boot
 * argument names. These are used to access the boot properties
 * from the argument properties. The class Profile defines
 * a similar collection of keys which are used to access profile
 * properties.
 * @author Dick Cowan - HP Labs
 * @version $Date: 2011-03-21 14:10:30 +0100(lun, 21 mar 2011) $ $Revision: 6395 $
 */
public class BootProfileImpl extends ProfileImpl {

	public static final String ACLCODEC_KEY = "aclcodec";
	public static final String CONF_KEY = "conf";
	public static final String CONTAINER_KEY = "container";
	public static final String DUMP_KEY = "dump";
	public static final String GUI_KEY = "gui";
	public static final String HELP_KEY = "help";
	public static final String MTP_KEY = "mtp";
	public static final String NOMTP_KEY = "nomtp";
	public static final String NAME_KEY = "name";
	public static final String LOGIN_KEY = "auth";
	public static final String SMHOST_KEY = "smhost";
	public static final String SMPORT_KEY = "smport";
	public static final String VERSION_KEY = "version";
	public static final String NOMOBILITY_KEY = "nomobility";



	ExtendedProperties argProp = null;
	BootHelper helper = new BootHelper();

	/**
	 * Construct default profile with empty argument properties
	 */
	public BootProfileImpl() {
		// creates the default profile
		super(true); 
		argProp = new ExtendedProperties();
	}

	/**
	 * Construct profile with specified arguments
	 * @param args Boot arguments
	 */
	public BootProfileImpl(String[] args) throws PropertiesException {
		this();
		ExtendedProperties properties = new ExtendedProperties(args);
		bootProps = (Properties)properties.clone();
		setArgProperties(properties);
	}        

	/**
	 * Return the properties collection which resulted from the arguments.
	 * This collection is used to create/modify the underlying profile's
	 * properties.
	 * @return ExtendedProperties The argument property collection.
	 */
	public ExtendedProperties getArgProperties() {
		return argProp;
	}

	/**
	 * Copy a collection of argument properties into the existing
	 * argument properties and then into the profile properties.
	 * When moving between the argument properties and profile
	 * properties different keys are required.
	 * @param source A collection of argument properties. The
	 * keys to this collection are from the XXX_KEY strings
	 * defined in this class.
	 */
	public void setArgProperties(ExtendedProperties source) {
		argProp.copyProperties(source);
		String value = null;
		boolean flag = false;


		// Transfer argument properties into profile properties

		ExtendedProperties profileProp = (ExtendedProperties)getProperties();

		boolean isMain = true;
		if (argProp.getProperty(Profile.MAIN) != null) {
			isMain = fetchAndVerifyBoolean(Profile.MAIN);
		}
		else {
			isMain = !fetchAndVerifyBoolean(CONTAINER_KEY);
		}
		if (isMain) {
			profileProp.setProperty(Profile.MAIN, "true");
		}
		else {
			profileProp.setProperty(Profile.MAIN, "false");
			// Since the value is false, we cancel the default done in ProfileImpl's constructor
			setSpecifiers(Profile.MTPS, new ArrayList(0)); // remove default MTP
		}

		String sm = argProp.getProperty(LOCAL_SERVICE_MANAGER);
		if(sm != null) {
			profileProp.setProperty(Profile.LOCAL_SERVICE_MANAGER, sm);
		}

		/* PER IL DF
        value = argProp.getProperty(df.KBDF_MAX_RESULTS);
        if (value != null) {
            profileProp.setProperty(df.KBDF_MAX_RESULTS, value);
        }
        value = argProp.getProperty(df.DBDF_DRIVER);
        if (value != null) {
            profileProp.setProperty(df.DBDF_DRIVER, value);
        }
        value = argProp.getProperty(df.DBDF_URL);
        if (value != null) {
            profileProp.setProperty(df.DBDF_URL, value);
        }*/

		/*
        value = argProp.getProperty(PASSWD_KEY);
        if (value != null) {
            //profileProp.setProperty(Profile.PASSWD_FILE, value);
        }
		 */

		value = argProp.getProperty(IMTP);
		if (value != null) {
			profileProp.setProperty(Profile.IMTP, value);
		}

		String host = argProp.getProperty(MAIN_HOST);
		if (host != null) {
			profileProp.setProperty(Profile.MAIN_HOST, host);
		} else {
			host = profileProp.getProperty(Profile.MAIN_HOST);
			if (host == null) {
				host = getDefaultNetworkName();
				profileProp.setProperty(Profile.MAIN_HOST, host);
			}
		}

		String port = argProp.getProperty(MAIN_PORT);
		if (port == null) {
			// Default for a sole main container: use the local port, or
			// the default port if also the local port is null.
			if(isMasterMain()) {
				port = argProp.getProperty(LOCAL_PORT);
			}
			if(port == null) {
				// All other cases: use the default port.
				port = Integer.toString(DEFAULT_PORT);
			}
		}
		profileProp.setProperty(Profile.MAIN_PORT, port);

		String localHost = argProp.getProperty(LOCAL_HOST);
		if(localHost == null) {
			// Default for a sole main container: use the MAIN_HOST property
			if(isMasterMain()) {
				localHost = host;
			}
			else {
				// Default for a peripheral container or an added main container: use the local host
				localHost = getDefaultNetworkName();
			}
		}
		profileProp.setProperty(Profile.LOCAL_HOST, localHost);

		String localPort = argProp.getProperty(LOCAL_PORT);
		if(localPort == null) {
			// Default for a sole main container: use the MAIN_PORT property
			if(isMasterMain()) {
				localPort = port;
			}
			else {
				// Default for a peripheral container or an added main container: use the default port
				localPort = Integer.toString(DEFAULT_PORT);
			}
		}
		profileProp.setProperty(Profile.LOCAL_PORT, localPort);

		value = argProp.getProperty(REMOTE_SERVICE_MANAGER_ADDRESSES);
		if (value != null) {
			try {
				Vector v = Specifier.parseSpecifierList(value);
				// Convert the Vector into a List
				List l = new ArrayList(v.size());
				Enumeration e = v.elements();
				while (e.hasMoreElements()) {
					l.add(e.nextElement());
				}
				setSpecifiers(Profile.REMOTE_SERVICE_MANAGER_ADDRESSES, l);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}

		value = argProp.getProperty(NAME_KEY);
		if (value != null) {
			profileProp.setProperty(Profile.PLATFORM_ID, value);
		}  

		value = argProp.getProperty(LOGIN_KEY);
		if (value != null) {
			profileProp.setProperty(Profile.USERAUTH_KEY, value);
		}  

		value = argProp.getProperty(MTP_KEY);
		if (value != null) {
			setSpecifiers(Profile.MTPS, parseSpecifiers(value));
		}

		//NOMTP
		flag = fetchAndVerifyBoolean(NOMTP_KEY);
		if (flag) {
			// Since the value was set to true, cancel the MTP settings
			setSpecifiers(Profile.MTPS, new ArrayList(0));
		}

		value = argProp.getProperty(ACLCODEC_KEY);
		if (value != null) {
			setSpecifiers(Profile.ACLCODECS, parseSpecifiers(value));
		}

		// Get agent list (if any)
		value = argProp.getProperty(AGENTS);


		flag = fetchAndVerifyBoolean(GUI_KEY);
		if (flag) {
			// need to run RMA agent
			if (value != null) {
				value = "RMA:jade.tools.rma.rma " + value;  // put before other agents
			} else {
				value = "RMA:jade.tools.rma.rma";  // only one
			}
		}

		if (value != null) {
			Vector agentVector = helper.T2(value, false);
			List agents = new ArrayList();

			for (Enumeration e = helper.getCommandLineAgentSpecifiers(agentVector);
			e.hasMoreElements(); ) {
				agents.add((Specifier) e.nextElement());
			}

			setSpecifiers(Profile.AGENTS, agents);
		}

		// Get service list (if any)
		value = argProp.getProperty(SERVICES);
		if(value == null) {
			// Remove mobility service from the list if '-nomobility' was specified
			flag = fetchAndVerifyBoolean(NOMOBILITY_KEY);
			if (flag) {
				value = DEFAULT_SERVICES_NOMOBILITY;
			}
			else {
				value = DEFAULT_SERVICES;
			}
		}

		setSpecifiers(Profile.SERVICES, parseSpecifiers(value));


		// finally, copy into profileProp all the properties that
		// were present in argProp AND were not already present in profileProp.
		// The fact that we do not copy those properties that were already
		// present in profileProp is a sanity check to avoid modification
		// of properties that were set in the code above
		for (Enumeration e=argProp.keys(); e.hasMoreElements(); ) {
			String key = (String)e.nextElement();
			if (getParameter(key,null) == null) 
				setParameter(key, argProp.get(key).toString());
		}

		// The following is for debugging only. Probably should not document the "dumpProfile" attribute.
		// Note All the jade.util.leap.ArrayList structures will only print their type unless a
		// toString() method were added to them. 
		if (argProp.getBooleanProperty("dumpProfile", false)) {
			ArrayList aList = new ArrayList();
			System.out.println("---------- Jade Boot profile property values ----------");
			for (Enumeration e = profileProp.sortedKeys(); e.hasMoreElements(); ) {
				String key = (String) e.nextElement();
				Object o = profileProp.get(key);
				if (o.getClass().isAssignableFrom(aList.getClass())) {
					System.out.print(key + "=");
					ArrayList al = (ArrayList)o;
					Iterator itor = al.iterator();
					if (!itor.hasNext()) {
						System.out.println("<empty>");
					} else {
						StringBuffer sb = new StringBuffer();
						while (itor.hasNext()) {
							sb.append(itor.next());
							if (itor.hasNext()) {
								sb.append(" ");
							}
						}
						System.out.println(sb.toString());
					}
				} else {
					System.out.println(key + "=" + profileProp.getProperty(key));
				}
			}
			System.out.println("-------------------------------------------------------");
		}
	}

	/**
	 * Fetch and verify a boolean attribute.
	 * @param aKey The property key to check.
	 * @return True or false depending on the attributes setting. False if attribute doesn't exist.
	 * @throws PropertiesException if there is a value but its not either "true" or "false".
	 */
	protected boolean fetchAndVerifyBoolean(String aKey) throws PropertiesException {
		String value = argProp.getProperty(aKey);
		if (value != null) {
			if (value.equalsIgnoreCase("true")) {
				return true;
			}
			if (value.equalsIgnoreCase("false")) {
				return false;
			}
			throw new PropertiesException("The value of the attribute " + aKey + " must be either true or false.");
		}
		return false;
	}

	private static final String ARGUMENT_SEPARATOR = ";";

	/**
	 * Parse a String reading for a set of
	 * <code>parameter(arg)</code>
	 * each delimited by a <code>;</code> and no space in between.
	 * <p>
	 * For instance
	 * <code>jade.mtp.iiop(50);http.mtp.http(8080)</code> is a valid
	 * string, while  <code>jade.mtp.iiop(50 80);http.mtp.http(8080)</code>
	 * is not valid
	 * For each object specifier, a new java object <code>Specifier</code>
	 * is added to the passed <code>out</code> List parameter.
	 */
	public List parseSpecifiers(String str) throws PropertiesException {

		List result = new ArrayList();

		// Cursor on the given string: marks the parser position
		int cursor = 0;

		while (cursor < str.length()) {
			int commaPos = str.indexOf(ARGUMENT_SEPARATOR, cursor);

			if (commaPos == -1) {
				commaPos = str.length();
			}

			String arg = str.substring(cursor, commaPos);
			int openBracketPos = arg.indexOf('(');
			int closedBracketPos = arg.indexOf(')');
			Specifier s = new Specifier();

			if ((openBracketPos == -1) && (closedBracketPos == -1)) {

				// No brackets: no argument
				s.setClassName(arg);
			} else {

				// An open bracket, then something, then a closed bracket:
				// the class name is before the open bracket, and the
				// argument is between brackets.
				if ((openBracketPos != -1) && (closedBracketPos != -1)
						&& (openBracketPos < closedBracketPos)) {
					s.setClassName(arg.substring(0, openBracketPos));

					Object a[] = new Object[1];

					a[0] = arg.substring(openBracketPos + 1,
							closedBracketPos);

					s.setArgs(a);
				} else {
					throw new PropertiesException(
							"Ill-formed specifier: mismatched parentheses.");
				}
			}

			cursor = commaPos + 1;

			result.add(s);
		}    // while (cursor)
		return result;
	}
}

