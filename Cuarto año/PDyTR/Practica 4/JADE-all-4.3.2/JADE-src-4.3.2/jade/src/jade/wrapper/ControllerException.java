/*
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * Copyright (C) 2000 CSELT S.p.A. 
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

/**
 * This exception class is thrown when an operation fails on any
 * of the agent controller methods.
   <br>
   <b>NOT available in MIDP</b>
   <br>
 */
public class ControllerException extends Exception {

	/**
	 * Creates a new exception object, with a default detail message.
	 */
	public ControllerException() {
		super("Agent error.");
	}

	/**
	 * Creates a new exception object, with a given detail message.
	 * @param message The detail message for the new exception object.
	 */
	public ControllerException(String message) {
		super(message);
	}

	/**
	 * Creates a new exception object, extracting message from another throwable.
	 * @param aThrowable The original exception or error.
	 */
	public ControllerException(Throwable aThrowable) {
		super(aThrowable.getMessage());
	}

}

