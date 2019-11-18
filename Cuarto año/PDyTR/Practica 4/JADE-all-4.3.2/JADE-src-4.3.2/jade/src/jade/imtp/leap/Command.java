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
 * Copyright (C) 2001 Siemens AG.
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
import java.io.IOException;
import java.io.DataOutputStream;
import java.io.ByteArrayOutputStream;
import java.util.Vector;

/**
 * The <code>Command</code> object is used to represent a platform command.
 * @author Michael Watzke
 * @author Steffen Rusitschka
 * @author Giovanni Rimassa - FRAMeTech s.r.l.
 */
public class Command {
	/**
	 * Unspecified object id
	 */
	public static final int        DUMMY_ID = -1;

	/**
	 * Command identifier code for response command.
	 */

	// Lower limit for service and node management related commands
	static final int SERVICE_BASE = 0;

	// Service and node management related command IDs
	public static final int GET_PLATFORM_NAME = 1;
	public static final int ADD_NODE = 2;
	public static final int REMOVE_NODE = 3;
	public static final int ACTIVATE_SERVICE = 4;
	public static final int DEACTIVATE_SERVICE = 5;
	public static final int FIND_SLICE_NODE = 6;
	public static final int FIND_ALL_NODES = 7;
	public static final int ACCEPT_COMMAND = 8;
	public static final int PING_NODE_BLOCKING = 9;
	public static final int PING_NODE_NONBLOCKING = 10;
	public static final int EXIT_NODE = 11;
	public static final int INTERRUPT_NODE = 12;
	public static final int SERVICE_MANAGER_ADOPT = 13;
	public static final int SERVICE_MANAGER_ADD_REPLICA = 14;
	public static final int SERVICE_MANAGER_UPDATE_COUNTERS = 15;
	public static final int PLATFORM_MANAGER_DEAD = 16;

	public static final int        OK = 1;
	public static final int        ERROR = 2;
	public static final int        FORWARD = 37;

	/**
	 * Code defining the type of command.
	 */
	private int             commandCode;

	/**
	 * Identifier of the remote object this Command is directed to.
	 */
	private int             objectID;

	/**
	 * This list represents the argument list of this platform command.
	 */
	private Vector            commandParameters;
	
	private boolean requireFreshConnection = false;

	/**
	 */
	Command(int code) {
		commandCode = code;
		objectID = DUMMY_ID;
	}

	/**
	 */
	Command(int code, int id) {
		commandCode = code;
		objectID = id;
	}

	/**
	 */
	Command(int code, int id, boolean requireFreshConnection) {
		commandCode = code;
		objectID = id;
		this.requireFreshConnection = requireFreshConnection;
	}

	/**
	   Allows reusing the same object to deal with another command.
	   Generally used to build a response to an incoming command.
	 */
	void reset(int code) {
		commandCode = code;
		objectID = DUMMY_ID;
		if (commandParameters != null) {
			commandParameters.removeAllElements();
		}
	}

	/**
	 * Return the command identifier code of this command.
	 * @return the command identifier code specifying the type of command
	 */
	int getCode() {
		return commandCode;
	} 

	/**
	 * Method declaration
	 * 
	 * @return
	 * 
	 * @see
	 */
	int getObjectID() {
		return objectID;
	} 

	/**
	 * Add a deliverable parameter, i.e., an object implementing the
	 * <code>Deliverable</code> interface or a <code>java.lang.String</code> or a
	 * <code>java.lang.StringBuffer</code> object to the end of the
	 * argument list of this command object.
	 * @param param the parameter object to be added at the end of the argument
	 * list
	 * @see Deliverable
	 * @see DeliverableDataInputStream#readObject()
	 * @see DeliverableDataOutputStream#writeObject( java.lang.Object )
	 */
	void addParam(Object param) {
		if (commandParameters == null) {
			commandParameters = new Vector();
		}
		commandParameters.addElement(param);
	} 

	/**
	 * Return the number of parameters in this command object.
	 */
	int getParamCnt() {
		if (commandParameters == null) {
			return 0;
		}
		else {
			return commandParameters.size();
		}
	} 

	/**
	 * Return the parameter at the specified index of this command object.
	 * @param index the parameter index
	 * @return the parameter at the specified index
	 */
	Object getParamAt(int index) {
		if (commandParameters == null) {
			throw new IndexOutOfBoundsException(String.valueOf(index));
		}
		else {
			return commandParameters.elementAt(index);
		}
	} 
	
	boolean getRequireFreshConnection() {
		return requireFreshConnection;
	}
}

