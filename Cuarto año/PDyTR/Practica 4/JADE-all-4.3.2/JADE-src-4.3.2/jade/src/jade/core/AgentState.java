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

/**

  This class represents the Life-Cycle state of an agent.

  @author Giovanni Rimassa - Universita' di Parma
  @version $Date: 2004-08-20 12:14:20 +0200 (ven, 20 ago 2004) $ $Revision: 5282 $
*/
public class AgentState {

    /**
       Static access method to retrieve a prototype object for an
       agent life-cycle state.

       @param value One of the <code>AP_XXX</code> constants defined
       in the <code>Agent</code> class.
       @return A prototype object for the requested state.
    */
    public static AgentState getInstance(int value) {
	for(int i = 0; i < STATES.length; i++) {
	    AgentState as = STATES[i];
	    if(as.getValue() == value) {
		return as;
	    }
	}

	return null;
    }

    /**
       Access the whole prototypes array.
       @return All the defined prototype objects for the life-cycle
       states.
    */
    public static AgentState[] getAllInstances() {
	return STATES;
    }

    /**
       Default constructor. A default constructor is necessary for
       this class because it is used in the
       <code>jade-introspection</code> ontology. Application code
       should use the static access method <code>getInstance()</code>,
       however.
    */
    public AgentState() {
    }


    private AgentState(String n, int v) {
	name = n;
	value = v;
    }

    /**
       Set the name of this state.
       @param n The name to give.
    */
    public void setName(String n) { 
	name = n;
    }

    /**
       Retrieve the name of this state.
       @return The state name.
    */
    public String getName() {
	return name;
    }

    /**
       Set the numeric value of this state, in agreement with the
       <code>AP_XXX</code> constants defined in the <code>Agent</code>
       class.
       @param v The numeric value for this state.
    */
    public void setValue(int v) {
	value = v;
    }

    /**
       Retrieve the numeric value of this state, in agreement with the
       <code>AP_XXX</code> constants defined in the <code>Agent</code>
       class.
       @return The numeric value set for this state.
    */
    public int getValue() {
	return value;
    }

    /**
       Equality operations between agent states. The equality
       operation is defined as equality by state name (case
       insensitive string comparison is used).
       @param o The right-hand side of the equality.
       @return If the current object and the parameter are equal
       (according to the criterion above), this method returns
       <code>true</code>, otherwise it returns <code>false</code>.
    */
    public boolean equals(Object o) {

	if(o instanceof String) {
	    return CaseInsensitiveString.equalsIgnoreCase(name, (String)o);
	}
	try {
	    AgentState as = (AgentState)o;
	    return CaseInsensitiveString.equalsIgnoreCase(name, as.name);
	}
	catch(ClassCastException cce) {
	    return false;
	}

    }

    /**
       Retrieve a string representation for this agent state.
       @return The state name.
    */
    public String toString() {
	return name;
    }

    /**
       Compares two agent states. A lexicographical, case insensitive
       comparison on state name is used.
       @param o The object to compare the current object to.
       @return An integer value, <code>-1</code>, <code>0</code> or
       <code>1</code> depending upon whether the current object is
       lesser, equal or greater than the given parameter.
       @throws java.lang.ClassCastException If the given parameter is
       not an instance of <code>AgentState</code>.
    */
    public int compareTo(Object o) {
	AgentState as = (AgentState)o;
	return name.toLowerCase().toUpperCase().compareTo(as.name.toLowerCase().toUpperCase());
    }

    /**
       Calculate an hash code for this agent state.
       @return An integer value, complying with the defined equality
       operation on <code>AgentState</code> instances.
    */
    public int hashCode() {
	return name.toLowerCase().hashCode();
    }

    private static final AgentState[] STATES = new AgentState[] { 
	new AgentState("Illegal MIN state", Agent.AP_MIN),
	new AgentState("Initiated", Agent.AP_INITIATED),
	new AgentState("Active", Agent.AP_ACTIVE),
	new AgentState("Idle", Agent.AP_IDLE),
	new AgentState("Suspended", Agent.AP_SUSPENDED),
	new AgentState("Waiting", Agent.AP_WAITING),
	new AgentState("Deleted", Agent.AP_DELETED),
	//#MIDP_EXCLUDE_BEGIN
	new AgentState("Transit", jade.core.mobility.AgentMobilityService.AP_TRANSIT),
	new AgentState("Copy", jade.core.mobility.AgentMobilityService.AP_COPY),
	new AgentState("Gone", jade.core.mobility.AgentMobilityService.AP_GONE),
	// FIXME: We can't use the constants since they are defined in the Persistence add-on
	new AgentState("Saving", 10),
	new AgentState("Loading", 11),
	new AgentState("Frozen", 12),
	//#MIDP_EXCLUDE_END
	new AgentState("Illegal MAX state", Agent.AP_MAX)
    };

    // For persistence service
    private Long persistentID;

    // For persistence service
    private Long getPersistentID() {
	return persistentID;
    }

    // For persistence service
    private void setPersistentID(Long l) {
	persistentID = l;
    }

    private String name;
    private int value;


}
