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


import jade.util.leap.Serializable;
import jade.util.leap.Comparable;
/*#MIDP_INCLUDE_BEGIN
 import jade.content.lang.sl.SimpleSLTokenizer;
 #MIDP_INCLUDE_END*/

import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import jade.util.leap.Properties;
import jade.util.leap.EnumIterator;

import java.util.Enumeration;
import java.util.Vector;

/**
 This class represents a JADE Agent Identifier. JADE internal agent
 tables use this class to record agent names and addresses.
 */
public class AID implements Comparable, Serializable {
	// Unique ID of the platform, used to build the GUID of resident agents.
	private static String platformID;
	
	private String name; 
	private int hashCode;
	
	private static final int EXPECTED_ADDRESSES_SIZE = 1;
	private static final int EXPECTED_RESOLVERS_SIZE = 1;
	
	//#MIDP_EXCLUDE_BEGIN
	private List addresses = new ArrayList(EXPECTED_ADDRESSES_SIZE);
	private List resolvers = new ArrayList(EXPECTED_RESOLVERS_SIZE);
	//#MIDP_EXCLUDE_END
	/*#MIDP_INCLUDE_BEGIN
	 private Vector addresses = new Vector(EXPECTED_ADDRESSES_SIZE,1);
	 private Vector resolvers = new Vector(EXPECTED_RESOLVERS_SIZE,1);
	 #MIDP_INCLUDE_END*/
	
	private Properties userDefSlots = new Properties();
	
	
	
	/**
	 * Constructs an Agent-Identifier whose slot name is set to an empty string
	 */
	public AID() {
		this("",ISGUID);
	}
	
	/** Constructor for an Agent-identifier
	 * This constructor (which is deprecated), examines the name
	 * to see if the "@" chararcter is present.  If so, it calls 
	 * <code> this(name, ISGUID)<code> 
	 * otherwise it calls <code>this(name, ISLOCALNAME)</code>
	 * This ensures better compatibility with JADE2.2 code.
	 * @param guid is the Globally Unique identifer for the agent. The slot name
	 * assumes that value in the constructed object. 
	 * @deprecated This constructor might generate a wrong AID, if
	 * the passed parameter is not a guid (globally unique identifier), but
	 * the local name of an agent (e.g. "da0"). 
	 * @see AID#AID(String boolean)
	 */
	public AID(String guid) {
		this(guid,ISGUID);
	}
	
	
	/** Constructor for an Agent-identifier
	 * @param name is the value for the slot name for the agent. 
	 * @param isGUID indicates if the passed <code>name</code>
	 * is already a globally unique identifier or not. Two
	 * constants <code>ISGUID</code>, <code>ISLOCALNAME</code>
	 * have also been defined for setting a value for this parameter.
	 * If the name is a local name, then the HAP (Home Agent Platform)
	 * is concatenated to the name, separated by  "@".
	 **/
	public AID(String name, boolean isGUID) {
		if (isGUID)
			setName(name);
		else
			setLocalName(name);
	}
	
	static final String getPlatformID() {
		return platformID;
	}
	
	static final void setPlatformID(String id) {
		platformID = id;
	}
	
	/** constant to be used in the constructor of the AID **/
	public static final boolean ISGUID = true;
	/** constant to be used in the constructor of the AID **/
	public static final boolean ISLOCALNAME = false;
	
	/** 
	 Key to retrieve the agent class name as a user defined slot of 
	 the AID included in the AMSAgentDescription registered with 
	 the AMS.
	 */
	public static final String AGENT_CLASSNAME = "JADE-agent-classname";
	
	/**
	 * This method permits to set the symbolic name of an agent.
	 * The passed parameter must be a GUID and not a local name. 
	 */
	public void setName(String n){
		name = n.trim();
		hashCode = name.toLowerCase().hashCode();
	}
	
	/**
	 * This method permits to set the symbolic name of an agent.
	 * The passed parameter must be a local name. 
	 */
	public void setLocalName(String n){
		String hap = getPlatformID();
		if (hap == null) {
			throw new RuntimeException("Unknown Platform Name");
		}
		name = n.trim(); 
		// Concatenates the HAP 
		/*if (! ( (name.length() > hap.length()) && 
				name.regionMatches(true, name.length() - hap.length(), hap, 0, hap.length()))) {
			name = name.concat("@"+hap);
		}*/
		name = name.concat("@"+hap);
		hashCode = name.toLowerCase().hashCode();			
	}
	
	/**
	 * This method returns the name of the agent.
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * This method permits to add a transport address where 
	 * the agent can be contacted.
	 * The address is added only if not yet present
	 */
	public void addAddresses(String url) {
		if (!addresses.contains(url)) {
			//#MIDP_EXCLUDE_BEGIN
			addresses.add(url);
			//#MIDP_EXCLUDE_END
			/*#MIDP_INCLUDE_BEGIN
			 addresses.addElement(url);
			 #MIDP_INCLUDE_END*/
		}
	}
	
	/**
	 * To remove a transport address.
	 * @param url the address to remove
	 * @return true if the addres has been found and removed, false otherwise.
	 */
	public boolean removeAddresses(String url) {
		//#MIDP_EXCLUDE_BEGIN
		return addresses.remove(url);
		//#MIDP_EXCLUDE_END
		/*#MIDP_INCLUDE_BEGIN
		 return addresses.removeElement(url);
		 #MIDP_INCLUDE_END*/
	}
	
	/**
	 * To remove all addresses of the agent
	 */
	public void clearAllAddresses(){
		//#MIDP_EXCLUDE_BEGIN
		addresses.clear();
		//#MIDP_EXCLUDE_END
		/*#MIDP_INCLUDE_BEGIN
		 addresses.removeAllElements();
		 #MIDP_INCLUDE_END*/
	}
	
	/**
	 * Returns an iterator of all the addresses of the agent.
	 * @see jade.util.leap.Iterator
	 */
	public Iterator getAllAddresses(){
		//#MIDP_EXCLUDE_BEGIN
		return addresses.iterator();
		//#MIDP_EXCLUDE_END
		/*#MIDP_INCLUDE_BEGIN
		 return new EnumIterator(addresses.elements());
		 #MIDP_INCLUDE_END*/
	}
	
	/**
	 * This method permits to add the AID of a resolver (an agent where name 
	 * resolution services for the agent can be contacted) 
	 */
	public void addResolvers(AID aid){
		if (!resolvers.contains(aid)) {
			//#MIDP_EXCLUDE_BEGIN
			resolvers.add(aid);
			//#MIDP_EXCLUDE_END
			/*#MIDP_INCLUDE_BEGIN
			 resolvers.addElement(aid);
			 #MIDP_INCLUDE_END*/
		}
	}
	
	/**
	 * To remove a resolver.
	 * @param aid the AID of the resolver to remove
	 * @return true if the resolver has been found and removed, false otherwise.
	 */
	public boolean removeResolvers(AID aid){
		//#MIDP_EXCLUDE_BEGIN
		return resolvers.remove(aid);
		//#MIDP_EXCLUDE_END
		/*#MIDP_INCLUDE_BEGIN
		 return resolvers.removeElement(aid);
		 #MIDP_INCLUDE_END*/
	}
	
	/**
	 * To remove all resolvers.
	 */
	public void clearAllResolvers(){
		//#MIDP_EXCLUDE_BEGIN
		resolvers.clear();
		//#MIDP_EXCLUDE_END
		/*#MIDP_INCLUDE_BEGIN
		 resolvers.removeAllElements();
		 #MIDP_INCLUDE_END*/
	}
	
	/**
	 * Returns an iterator of all the resolvers.
	 * @see jade.util.leap.Iterator
	 */
	public Iterator getAllResolvers() {
		//#MIDP_EXCLUDE_BEGIN
		return resolvers.iterator();
		//#MIDP_EXCLUDE_END
		/*#MIDP_INCLUDE_BEGIN
		 return new EnumIterator(resolvers.elements());
		 #MIDP_INCLUDE_END*/
	}
	
	/**
	 * To add a user defined slot (a pair key, value).
	 * @param key the name of the property
	 * @param value the corresponding value of the property
	 */
	public void addUserDefinedSlot(String key, String value){
		userDefSlots.setProperty(key, value);
	}
	
	/**
	 * To remove a user defined slot.
	 * @param key the name of the property
	 * @return true if the property has been found and removed, false otherwise
	 */
	public boolean removeUserDefinedSlot(String key){
		return (userDefSlots.remove(key) != null);
	}
	
	/**
	 * Returns an array of string containing all the addresses of the agent
	 */
	public String[] getAddressesArray() {
		//#MIDP_EXCLUDE_BEGIN
		Object[] objs = addresses.toArray();
		String[] result = new String[objs.length];
		System.arraycopy(objs, 0, result, 0, objs.length); 
		//#MIDP_EXCLUDE_END
		/*#MIDP_INCLUDE_BEGIN
		 String[] result = new String[addresses.size()];
		 addresses.copyInto(result);
		 #MIDP_INCLUDE_END*/
		return result;
	}
	
	// For persistence service
	private void setAddressesArray(String[] arr) {
		//#MIDP_EXCLUDE_BEGIN
		addresses.clear();
		//#MIDP_EXCLUDE_END
		
		/*#MIDP_INCLUDE_BEGIN
		 addresses.removeAllElements();
		 #MIDP_INCLUDE_END*/
		
		for(int i = 0; i < arr.length; i++) {
			addAddresses(arr[i]);
		}
	}
	
	/**
	 * Returns an array containing all the AIDs of the resolvers.
	 */
	public AID[] getResolversArray() {
		//#MIDP_EXCLUDE_BEGIN
		Object[] objs = resolvers.toArray();
		AID[] result = new AID[objs.length];
		System.arraycopy(objs, 0, result, 0, objs.length); 
		//#MIDP_EXCLUDE_END
		/*#MIDP_INCLUDE_BEGIN
		 AID[] result = new AID[resolvers.size()];
		 resolvers.copyInto(result);
		 #MIDP_INCLUDE_END*/
		return result;
	}
	
	// For persistence service
	private void setResolversArray(AID[] arr) {
		//#MIDP_EXCLUDE_BEGIN
		resolvers.clear();
		//#MIDP_EXCLUDE_END
		
		/*#MIDP_INCLUDE_BEGIN
		 resolvers.removeAllElements();
		 #MIDP_INCLUDE_END*/
		
		for(int i = 0; i < arr.length; i++) {
			addResolvers(arr[i]);
		}
	}
	
	/**
	 * Returns the user-defined slots as properties. 
	 * @return all the user-defined slots as a <code>jade.util.leap.Properties</code> java Object.
	 * @see jade.util.leap.Properties
	 */
	public Properties getAllUserDefinedSlot(){
		return userDefSlots;
	}
	
	/**
	 * Converts this agent identifier into a readable string.
	 * @return the String full representation of this AID
	 **/
	public String toString() {
		StringBuffer s = new StringBuffer("( agent-identifier ");
		//#MIDP_EXCLUDE_BEGIN
		jade.lang.acl.StringACLCodec.appendACLExpression(s,":name",name);
		//#MIDP_EXCLUDE_END
		/*#MIDP_INCLUDE_BEGIN
		 s.append(":name ");
		 s.append(SimpleSLTokenizer.isAWord(name) ? name : SimpleSLTokenizer.quoteString(name));
		 #MIDP_INCLUDE_END*/
		
		
		if (addresses.size()>0)
			s.append(" :addresses (sequence ");
		for (int i=0; i<addresses.size(); i++)
			try {
				//#MIDP_EXCLUDE_BEGIN
				s.append((String)addresses.get(i));
				//#MIDP_EXCLUDE_END
				/*#MIDP_INCLUDE_BEGIN
				 s.append((String)addresses.elementAt(i));
				 #MIDP_INCLUDE_END*/
				s.append(" ");
			} 
		catch (IndexOutOfBoundsException e) {e.printStackTrace();}
		if (addresses.size()>0)
			s.append(")");
		if (resolvers.size()>0)
			s.append(" :resolvers (sequence ");
		for (int i=0; i<resolvers.size(); i++) { 
			try {
				//#MIDP_EXCLUDE_BEGIN
				s.append(resolvers.get(i).toString());
				//#MIDP_EXCLUDE_END
				/*#MIDP_INCLUDE_BEGIN
				 s.append(resolvers.elementAt(i).toString());
				 #MIDP_INCLUDE_END*/
			} 
			catch (IndexOutOfBoundsException e) {e.printStackTrace();}
			s.append(" ");
		}
		if (resolvers.size()>0)
			s.append(")");
		Enumeration e = userDefSlots.propertyNames();
		String key, value;
		while (e.hasMoreElements()) {
			key = (String)e.nextElement();
			value = userDefSlots.getProperty(key);
			s.append(" :X-");
			//#MIDP_EXCLUDE_BEGIN
			jade.lang.acl.StringACLCodec.appendACLExpression(s,key,value); 
			//#MIDP_EXCLUDE_END
			/*#MIDP_INCLUDE_BEGIN
			 s.append(key); 
			 s.append(" ");
			 s.append(SimpleSLTokenizer.isAWord(value) ? value : SimpleSLTokenizer.quoteString(value));
			 #MIDP_INCLUDE_END*/
		}
		s.append(")");
		return s.toString();
	}
	
	/**
	 * Clone the AID object.
	 */
	public synchronized Object clone() {
		AID      result = new AID(this.name, ISGUID);
		result.persistentID = null;
		
		//#MIDP_EXCLUDE_BEGIN
		result.addresses = (ArrayList)((ArrayList)addresses).clone();
		result.resolvers = (ArrayList)((ArrayList)resolvers).clone();
		//#MIDP_EXCLUDE_END
		/*#MIDP_INCLUDE_BEGIN
		 result.addresses = new Vector(addresses.size());
		 for (int i=0; i<addresses.size(); i++)
		 result.addresses.addElement(addresses.elementAt(i));
		 result.resolvers = new Vector(resolvers.size());
		 for (int i=0; i<resolvers.size(); i++)
		 result.resolvers.addElement(resolvers.elementAt(i));
		 #MIDP_INCLUDE_END*/
		
		
		
		// Copying user defined slots
		//Enumeration enum = userDefSlots.propertyNames();
		//while (enum.hasMoreElements()) {
		//    String key = (String) enum.nextElement();
		//    result.addUserDefinedSlot(key, 
		//                              (String) userDefSlots.getProperty(key));
		//}
		result.userDefSlots = (Properties) userDefSlots.clone();
		
		return result;
	} 
	
	
	/**
	 Equality operation. This method compares an <code>AID</code> object with
	 another or with a Java <code>String</code>. The comparison is case
	 insensitive.
	 @param o The Java object to compare this <code>AID</code> to.
	 @return <code>true</code> if one of the following holds:
	 <ul>
	 <li> The argument <code>o</code> is an <code>AID</code> object
	 with the same <em>GUID</em> in its name slot (apart from
	 differences in case).
	 <li> The argument <code>o</code> is a <code>String</code> that is
	 equal to the <em>GUID</em> contained in the name slot of this
	 Agent ID (apart from differences in case).
	 </ul>
	 */
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (o instanceof AID) {
			return CaseInsensitiveString.equalsIgnoreCase(name, ((AID) o).name);
		}
		if(o instanceof String) {
			return CaseInsensitiveString.equalsIgnoreCase(name, (String)o);
		}
		return false;
	}
	
	
	/**
	 Comparison operation. This operation imposes a total order
	 relationship over Agent IDs.
	 @param o Another <code>AID</code> object, that will be compared
	 with the current <code>AID</code>.
	 @return -1, 0 or 1 according to the lexicographical order of the
	 <em>GUID</em> of the two agent IDs, apart from differences in
	 case.
	 */
	public int compareTo(Object o) {
		AID id = (AID)o;
		return name.toLowerCase().toUpperCase().compareTo(id.name.toLowerCase().toUpperCase());
	}
	
	
	/**
	 Hash code. This method returns an hash code in such a way that two
	 <code>AID</code> objects with equal names or with names differing
	 only in case have the same hash code.
	 @return The hash code for this <code>AID</code> object.
	 */
	public int hashCode() {
		return hashCode;
	}
	
	/**
	 * Returns the local name of the agent (without the HAP).
	 * If the agent is not local, then the method returns its GUID.
	 */
	public String getLocalName() {
		int atPos = name.lastIndexOf('@');
		if(atPos == -1)
			return name;
		else
			return name.substring(0, atPos);
	}
	
	/**
	 Returns the HAP of the agent or null if the GUID of this 
	 <code>AID</code> is not of the form <local-name>@<platform-name>
	 */
	public String getHap() {
		int atPos = name.lastIndexOf('@');
		if(atPos == -1)
			return null;
		else
			return name.substring(atPos + 1);
	}
	
	
	// For persistence service
	private transient Long persistentID;
	
	// For persistence service
	private Long getPersistentID() {
		return persistentID;
	}
	
	// For persistence service
	private void setPersistentID(Long l) {
		persistentID = l;
	}
	
}
