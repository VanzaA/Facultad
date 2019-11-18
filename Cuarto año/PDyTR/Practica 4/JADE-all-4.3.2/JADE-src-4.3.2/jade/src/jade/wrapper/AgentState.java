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

import java.io.Serializable;

/**
 * Provides a concrete implementation of the State interface for agents.
   <br>
   <b>NOT available in MIDP</b>
   <br>
 * @author David Bell, Dick Cowan: Hewlett-Packard
 */
public class AgentState extends StateBase {
    static final int LOWEST_STATE = 1;

    /**
     * An array of string names, one for each of the valid state this object
     * can represent. This array MUST be initialized before the other static
     * constructors.
     */
    static final String[] stateNames = {
        "Initiated",    // 1
        "Active",       // 2
        "Idle",         // 3
        "Suspended",    // 4
        "Waiting",      // 5
        "Deleted",      // 6
        "Transit"       // 7
    };

    public static final int cAGENT_STATE_INITIATED = 1;
    public static final int cAGENT_STATE_ACTIVE    = 2;
    public static final int cAGENT_STATE_IDLE      = 3;
    public static final int cAGENT_STATE_SUSPENDED = 4;
    public static final int cAGENT_STATE_WAITING   = 5;
    public static final int cAGENT_STATE_DELETED   = 6;
    public static final int cAGENT_STATE_TRANSIT   = 7;

    static final State AGENT_STATE_INITIATED = new AgentState(cAGENT_STATE_INITIATED);
    static final State AGENT_STATE_ACTIVE = new AgentState(cAGENT_STATE_ACTIVE);
    static final State AGENT_STATE_IDLE = new AgentState(cAGENT_STATE_IDLE);
    static final State AGENT_STATE_SUSPENDED = new AgentState(cAGENT_STATE_SUSPENDED);
    static final State AGENT_STATE_WAITING = new AgentState(cAGENT_STATE_WAITING);
    static final State AGENT_STATE_DELETED = new AgentState(cAGENT_STATE_DELETED);
    static final State AGENT_STATE_INTRANSIT = new AgentState(cAGENT_STATE_TRANSIT);

    /**
     * Creates a state object for a given code.  To avoid coding errors,
     * the integer codes used to create instances of this class should
     * come from the {@link StateConstants StateConstants} class.
     * <p>
     * If the code is "out of range" (i.e. not a valid code), an
     * {@link java.lang.IllegalArgumentException IllegalArgumentException}
     * will be thrown.
     *
     * @param code The integer code identifier for a particular state.
     */
    AgentState(int code) {
        super(code, LOWEST_STATE, stateNames);
    }

    /**
     * For testing, simply list the valid state descriptions and numbers.
     * @param args Command line arguments, currently not used.
     */
    public static void main(String[] args) {
        AgentState aState = new AgentState(LOWEST_STATE);

        System.out.println(aState.legalRange());
    }
}
