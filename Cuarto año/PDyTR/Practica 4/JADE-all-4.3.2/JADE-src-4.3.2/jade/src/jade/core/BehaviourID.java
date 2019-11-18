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

//#MIDP_EXCLUDE_FILE
//#APIDOC_EXCLUDE_FILE

import jade.core.behaviours.*;

import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import jade.util.leap.Collection;

import jade.content.Concept;

/**
 
 This class represents an unique identifier referring to a specific
 agent behaviour.
 
 @author Giovanni Rimassa - Universita' di Parma
 @version $Date: 2006-10-25 18:45:54 +0200 (mer, 25 ott 2006) $ $Revision: 5902 $
 
 */
public class BehaviourID implements Concept {
	
	private int code;
	private String name;
	private String className;
	private String kind; 
	private List children = new ArrayList();
	
	/**
	 Default constructor. Builds an unspecified behaviour ID.
	 */
	public BehaviourID () {
	}
	
	/**
	 This constructor builds a new behaviour ID, describing the
	 given behaviour object. The various attributes of the behaviour
	 ID (behaviour name, behaviour class, etc.) are set accordingly.
	 
	 @param b The <code>Behaviour</code> object that is to be
	 described with this ID.
	 */
	public BehaviourID (Behaviour b) {
		code = b.hashCode();
		name = b.getBehaviourName();
		className = b.getClass().getName();      
		kind = getClassKind(b.getClass());      
		
		// If we have a composite behaviour, add the
		// children to this behaviour id.
		if (b instanceof CompositeBehaviour) {
			CompositeBehaviour c = (CompositeBehaviour)b;
			Iterator iter = c.getChildren().iterator();
			while (iter.hasNext()) {
				addChildren(new BehaviourID((Behaviour)iter.next()));
			}
		}
	}
	
	private String getClassKind(Class c) {
		if (c == null) {
			return null;
		}
		
		String className = c.getName();
		// Remove the class name and the '$' characters from
		// the class name for readability.
		int dotIndex = className.lastIndexOf('.');
		int dollarIndex = className.lastIndexOf('$');
		int lastIndex = (dotIndex > dollarIndex ? dotIndex : dollarIndex);
		if (lastIndex == -1) {
			return className;
		}
		else if (lastIndex == dotIndex) {
			return className.substring(lastIndex+1);
		}
		else {
			// This is an anonymous inner class (the name is not meaningful) --> 
			// Use the extended class 
			return getClassKind(c.getSuperclass());
		}
	}
	
	/**
	 Set the name of this behaviour ID
	 @param n The name to give to this behaviour ID.
	 */
	public void setName(String n) {
		name = n;
	}
	
	/**
	 Retrieve the name of this behaviour ID.
	 @return The given name, or <code>null</code> if no name was set.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 Set the code of this behaviour ID
	 @param code The code to give to this behaviour ID.
	 */
	public void setCode(int code) {
		this.code = code;
	}
	
	/**
	 Retrieve the code of this behaviour ID.
	 */
	public int getCode() {
		return code;
	}
	
	/**
	 Set the class name for this behaviour ID. This is the name of
	 the Java class implementing the described agent behaviour.
	 @param n The class name of the described behaviour.
	 */
	public void setClassName(String n) {
		className = n;
	}
	
	/**
	 Retrieve the class name implementing the agent behaviour
	 described by this ID.
	 @return The class name, or <code>null</code> if no class name
	 was set.
	 */
	public String getClassName() {
		return className;
	}
	
	/**
	 Set the kind of behaviour described by this behaviour ID.
	 @param k A string specifying the kind of the described
	 behaviour.
	 */
	public void setKind(String k) {
		kind = k;
	}
	
	/**
	 Retrieve the kind of behaviour described by this behaviour ID.
	 @return A string describing the kind of behaviour, or
	 <code>null</code> if no kind was set.
	 */
	public String getKind() {
		return kind;
	}
	
	/**
	 Adds a new behaviour ID as a child of this one. The
	 parent-child relationship between behaviour IDs reflects the
	 one between their described behaviours.
	 
	 @param bid The behaviour ID object to add.
	 */
	public void addChildren(BehaviourID bid) {
		children.add(bid);
	}
	
	/**
	 Retrieve the list of all the children behaviour IDs, as an
	 iterator object.
	 @return An iterator over the children collection of this
	 behaviour ID.
	 */
	public Iterator getAllChildren() {
		return children.iterator();
	}
		
	/**
	 Tells whether this behaviour ID has children.
	 @return If the children collection is empty, <code>true</code>
	 is returned, and <code>false</code> otherwise.
	 */
	public boolean isSimple() {
		return (children.size() == 0);
	}
	
	/**
	 Equality test on two behaviour IDs. They are considered to be
	 equal if and only if their <i>name</i>, <i>className</i> and
	 <i>kind</i> attributed are the same.
	 
	 @param o The right hand side of the equality test (the left
	 hand one being the current object).
	 @return If the <code>o</code> parameter is a behaviour ID with
	 the same name, class name and kind of the current object,
	 <code>true</code>. Otherwise, <code>false</code> is returned.
	 */
	public boolean equals(Object o) {
		if (o != null && o instanceof BehaviourID) {
			BehaviourID b = (BehaviourID)o;
			return (checkEquals(name, b.name) && checkEquals(className, b.className) && checkEquals(kind, b.kind));
		}
		else {
			return false;
		}
	}
	
	
	
	private boolean checkEquals(String s1, String s2) {
		if (s1 != null) {
			return (s1.equals(s2));
		}
		else {
			return s2 == null;
		}
	}

	/**
	 Hash code operation, compliant with identity-by-name. This
	 method returns an hash code for a behaviour ID, so that two
	 behaviour IDs with the same name, class name and kind have the
	 same hash code.
	 */
	public int hashCode() {
		int result = 0;
		if(name != null) {
			result = name.hashCode();
		}
		if(className != null) {
			result = result*2 + className.hashCode();
		}
		if(kind != null) {
			result = result*2 + kind.hashCode();
		}
		
		return result;
	}
	
	/**
	 Create a string representation for this behaviour ID. The
	 actual representation is simply the name of the behaviour ID.
	 
	 @return The name of this behaviour ID.
	 */
	public String toString() {
		return name;
	}
	
}
