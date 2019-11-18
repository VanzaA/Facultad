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

package jade.wrapper;

/**
 This exception class is thrown when an attempt to use a stale
 (i.e. outdated) wrapper object is made.
 <br>
 <b>NOT available in MIDP</b>
 <br>
 */
public class StaleProxyException extends ControllerException {

	/**
     Creates a new exception object, with a default detail message.
	 */
	public StaleProxyException() {
		super("The proxy is not valid anymore.");
	}

	/**
     Creates a new exception object, with a given detail message.
     @param message The detail message for the new exception object.
	 */
	public StaleProxyException(String message) {
		super(message);
	}

	/**
	 * Creates a new exception object, extracting message from another throwable.
	 * @param aThrowable The original exception or error.
	 */
	public StaleProxyException(Throwable aThrowable) {
		super(aThrowable.getMessage());
	}

}

