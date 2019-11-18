/*
 * (c) Copyright Hewlett-Packard Company 2001
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE and no warranty
 * that the program does not infringe the Intellectual Property rights of
 * a third party.  See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 *
 */
package jade.wrapper;

import jade.util.leap.Serializable;
import jade.util.leap.Comparable;

/**
 * Provides an abstract base class for implementations of state.
   <br>
   <b>NOT available in MIDP</b>
   <br>
 * @author David Bell, Dick Cowan: Hewlett-Packard
 */
public abstract class StateBase implements State, Comparable, Serializable {

    /**
     * Code corresponding to first legal state name. Typically 0 or 1.
     */
    private int baseCode;

    /**
     * The integer code that identifies which state this object represents.
     */
    private int m_code;

    /**
     * An array of string names, one for each of the valid state this object
     * can represent.
     */
    private String stateNames[];

    private StateBase() {
        // empty
    }

    /**
     * Creates a state object for a given code.  To avoid coding errors,
     * the integer codes used to create instances of this class should
     * come from the concrete extensions of this class.
     * <p>
     * If the code is "out of range" (i.e. not a valid code), an
     * {@link java.lang.IllegalArgumentException IllegalArgumentException}
     * will be thrown.
     *
     * @param code The integer code identifier for a particular state.
     * @param aBaseCode Value corresponding to first entry in names. Typically 0 or 1.
     * @param names Names of the states.
     */
    StateBase(int code, int aBaseCode, String[] names) {
        baseCode = aBaseCode;

        if (names == null) {
            throw new IllegalArgumentException(
                "State names must not be null");
        }

        stateNames = names;

        if ((code >= baseCode)
                && (code <= (baseCode + stateNames.length - 1))) {
            m_code = code;
        } else {
            throw new IllegalArgumentException(
                "State code out of range(" + baseCode + "-"
                + (baseCode + stateNames.length - 1) + "): " + code);
        }
    }

    /**
     * Returns the descriptive name of the state.
     * @return The descriptive name of the state.  Will never return null.
     */
    public String getName() {
        String rtVal = getName(m_code);

        // Should never happen, but just in case...
        if (rtVal == null) {
            rtVal = "Unknown";
        }

        return rtVal;
    }

    /**
     * Return the integer code that identifies this state.
     * @return The integer code that identifies this state.
     */
    public int getCode() {
        return (m_code);
    }

    /**
     * Converts a descriptive state name into a state code.
     * The name comparison is not case sensitive.
     * If the name cannot be located for any reason a
     * {@link java.lang.IllegalArgumentException IllegalArgumentException}
     * will be thrown
     * @param name The descriptive name of a state to convert to a state code.
     * @return The integer state code corresponding to the provided state
     * name.
     */
    int getCode(String name) {
        if (name == null) {
            throw new IllegalArgumentException("null argument not allowed");
        }

        boolean found = false;
        int idx;

        for (idx = 0; (!found && (idx < stateNames.length)); idx++) {
            if (stateNames[idx].equalsIgnoreCase(name)) {
                found = true;
            }
        }

        if (found) {
            return baseCode + idx;
        } else {
            throw new IllegalArgumentException("No such state name: " + name);
        }
    }

    /**
     * Converts a state code to a descriptive state name.
     * @param code A state code to convert to a state name.
     * @return The descriptive name of the state or null if the state
     * code is "out of range" (i.e. invalid).
     */
    String getName(int code) {
        if ((code >= baseCode)
                && (code <= (baseCode + stateNames.length - 1))) {
            return stateNames[code - baseCode];
        } else {
            return null;
        }
    }

    /**
     * Determines if an object is equal to this object.  In order for the
     * the passed object to be equal to this one the passed object must
     * be an instance if State and contain the same state code.
     * @param object An object to test for equality.
     * @return True if the object represent the same state and False otherwise.
     */
    public boolean equals(Object object) {
        boolean equal = false;

        if (object != null) {
            equal = (m_code == ((State) object).getCode());
        }

        return (equal);
    }

    /**
     * Compares a given object to this state object for the purpose of
     * collating.  This method is used for the sorting of states.
     * A positive value means the receiver (this object) is "greater than"
     * the passed object.
     * A negative value means the receiver is "less than" the passed object.
     * A value of zero means the receiver and the passed object are equal
     * to each other.
     *
     * @param object An object to compare this object to.
     *
     * @return The result of the comparison.
     * <p>
     * The value 1 is returned if:
     * <ul>
     * <li> the passed object is null (the receiver is considered "greater than"
     * null objects)</li>
     * <li> the state code held by the receiver (this object) is
     * greater than
     * the code of the passed object</li>
     * </ul>
     * <p>
     * The value 0 (zero) is returned if:
     * <ul>
     * <li>the two state objects hold the same state code</li>
     * </ul>
     * <p>
     * The value -1 is returned if:
     * <ul>
     * <li> the passed object is not a AgentStateImpl object
     * (the receiver is considered "less than" objects of other types)</li>
     * <li> the state code held by the receiver (this object) is
     * less than
     * the code of the passed object</li>
     * <ul>
     */
    public int compareTo(Object object) {
        int result = 0;

        if (object == null) {
            return 1;
        }

        if (object.getClass().isAssignableFrom(State.class)) {
            if (m_code > ((State) object).getCode()) {
                result = 1;
            } else if (m_code < ((State) object).getCode()) {
                result = -1;
            } else {
                result = 0;
            }
        } else {
            result = -1;
        }

        return (result);
    }

    /**
     * Returns the hash code for this state.  The hash code equals the
     * state code.
     *
     * @return The state code for this state.
     */
    public int hashCode() {
        return (m_code);
    }

    /**
     * A string representation of this state.
     * The returned string is of the form StateName(StateCode).
     * @return A string containing the name and code for this state.
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();

        buf.append(getName()).append("(").append(m_code).append(")");

        return (buf.toString());
    }

    /**
     * Return a string containing all descriptions and their codes.
     * Usefull for debugging.
     * @return String Is of the form StateName(StateCode), StateName(StateCode), ....
     */
    String legalRange() {
        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < stateNames.length; i++) {
            int code = i + baseCode;

            buf.append(getName(code)).append("(").append(code).append(")");

            if (i < stateNames.length - 1) {
                buf.append(", ");
            }
        }

        return (buf.toString());
    }
}
