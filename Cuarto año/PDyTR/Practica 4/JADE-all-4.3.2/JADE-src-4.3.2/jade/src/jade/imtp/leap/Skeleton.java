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

/**
 * @author Giovanni Caire - Telecom Italia LAB
 */
abstract class Skeleton {

	/**
	 * Processes a Command and returns another Command representing
	 * the response.
	 */
	Command processCommand(Command command) {
		Command resp = null;
		try {
			resp = executeCommand(command);
		} 
		catch (Throwable tr) {
			resp = new Command(Command.ERROR);

			resp.addParam(tr.getClass().getName());
			resp.addParam(tr.getMessage());
			// FIXME: Is it correct not to print the stack trace when a declared exception occurs? 
			if ((tr instanceof Error) || (tr instanceof RuntimeException)) { 
				tr.printStackTrace();
			}
		} 

		return resp;
	} 

	/**
	 * Method declaration
	 *
	 * @param command
	 *
	 * @return
	 *
	 * @throws Throwable
	 *
	 * @see
	 */
	abstract Command executeCommand(Command command) throws Throwable;

}

