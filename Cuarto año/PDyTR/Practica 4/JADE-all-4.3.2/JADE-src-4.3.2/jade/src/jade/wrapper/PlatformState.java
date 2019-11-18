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
 * Provides a concrete implementation of the State interface for agent platforms.
   <br>
   <b>NOT available in MIDP</b>
   <br>
 * @author David Bell, Dick Cowan: Hewlett-Packard
 */
public class PlatformState extends StateBase {
    static final int LOWEST_STATE = 0;

    /**
     * An array of string names, one for each of the valid state this object
     * can represent. This array MUST be initialized before the other static
     * constructors.
     */
    static final String stateNames[] = {
        "Void",         //  0
        "Initializing", //  1
        "Initialized",  //  2
        "Starting",     //  3
        "Ready",        //  4
        "Suspending",   //  5
        "Suspended",    //  6
        "Killing",      //  7
        "Killed",       //  8
        "Failed"        //  9
    };

    public static final int cPLATFORM_STATE_VOID         = 0;
    public static final int cPLATFORM_STATE_INITIALIZING = 1;
    public static final int cPLATFORM_STATE_INITIALIZED  = 2;
    public static final int cPLATFORM_STATE_STARTING     = 3;
    public static final int cPLATFORM_STATE_READY        = 4;
    public static final int cPLATFORM_STATE_SUSPENDING   = 5;
    public static final int cPLATFORM_STATE_SUSPENDED    = 6;
    public static final int cPLATFORM_STATE_KILLING      = 7;
    public static final int cPLATFORM_STATE_KILLED       = 8;
    public static final int cPLATFORM_STATE_FAILED       = 9;
    
    static final State PLATFORM_STATE_VOID = new PlatformState(cPLATFORM_STATE_VOID);
    static final State PLATFORM_STATE_INITIALIZING = new PlatformState(cPLATFORM_STATE_INITIALIZING);
    static final State PLATFORM_STATE_INITIALIZED = new PlatformState(cPLATFORM_STATE_INITIALIZED);
    static final State PLATFORM_STATE_STARTING = new PlatformState(cPLATFORM_STATE_STARTING);
    static final State PLATFORM_STATE_READY = new PlatformState(cPLATFORM_STATE_READY);
    static final State PLATFORM_STATE_SUSPENDING = new PlatformState(cPLATFORM_STATE_SUSPENDING);
    static final State PLATFORM_STATE_SUSPENDED = new PlatformState(cPLATFORM_STATE_SUSPENDED);
    static final State PLATFORM_STATE_KILLING = new PlatformState(cPLATFORM_STATE_KILLING);
    static final State PLATFORM_STATE_KILLED = new PlatformState(cPLATFORM_STATE_KILLED);

    /**
     * A constant that holds an object to represent the state FAILED. A component
     * will be placed in this state if a failure occurs during most of
     * the transition states. These states are INITIALIZING, STARTING, and SUSPENDING.
     * If a failure occurs during the processing associated with these transitional
     * states the component will be placed into the FAILED state. A component may then
     * only be transitioned into a KILLING and then KILLED state at some later time.
     * No other state transitions are allowed from the FAILED state.
     */
    static final State PLATFORM_STATE_FAILED = new PlatformState(cPLATFORM_STATE_FAILED);

    /**
     * Constructor PlatformState
     * @param code The desired state.
     *
     */
    PlatformState(int code) {
        super(code, LOWEST_STATE, stateNames);
    }

    /**
     * For testing, simply list the valid state descriptions and numbers.
     * @param args Command line arguments, currently not used.
     */
    public static void main(String[] args) {
        PlatformState aState = new PlatformState(LOWEST_STATE);
        System.out.println(aState.legalRange());
    }
}
