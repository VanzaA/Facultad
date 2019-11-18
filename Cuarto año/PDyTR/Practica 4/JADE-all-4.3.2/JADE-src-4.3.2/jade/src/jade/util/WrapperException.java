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

package jade.util;

/**

  This class acts as a base class for all the exceptions that wrap
  another (nested) exception. The typical usage for descendents of
  this class is to throw them within the <code>catch</code> block for
  their nested exception.

  @author Giovanni Rimassa - Universita' di Parma
  @version $Date: 2012-11-29 09:31:49 +0100 (gio, 29 nov 2012) $ $Revision: 6607 $

 */
public abstract class WrapperException extends Exception {
	private Throwable nested = null;

	/**
       Create a wrapper exception with the given message and nested
       exception.
       @param m The exception message text.
       @param t The nested exception that caused this one.
	 */
	protected WrapperException(String m, Throwable t) {
		super(m);
		nested = t;
	}

	/**
       Create a wrapper exception with the given message.
       @param m The exception message text.
	 */
	protected WrapperException(String m) {
		super(m);
	}

	/**
       Retrieve the exception message text.
       @return The exception message, including the nested exception
       text if present.
	 */
	public String getMessage() {
		if((nested != null)) {
			return super.getMessage() + " - Caused by:  "+(nested.getMessage() != null ? nested.getMessage() : nested.toString());
		}
		return super.getMessage();
	}

	/**
       Prints the stack trace of this exception. If a nested exception is present, its stack trace is
       also printed.
	 */
	//#MIDP_EXCLUDE_BEGIN
	public void printStackTrace(java.io.PrintWriter pw) {
		super.printStackTrace(pw);
		if(nested != null) {
			pw.println("Nested Exception:");
			nested.printStackTrace(pw);
		}
	}
	public void printStackTrace(java.io.PrintStream ps) {
		super.printStackTrace(ps);
		if(nested != null) {
			ps.println("Nested Exception:");
			nested.printStackTrace(ps);
		}
	}
	//#MIDP_EXCLUDE_END
	/*#MIDP_INCLUDE_BEGIN
	public void printStackTrace() {
		super.printStackTrace();
		if(nested != null) {
			System.err.println("Nested Exception:");
			nested.printStackTrace();
		}
	}
	#MIDP_INCLUDE_END*/

	/**
       Reads the exception wrapped by this object.
       @return the <code>Throwable</code> object that is the exception 
       that was originally thrown.
	 */
	public Throwable getNested() {
		return nested;
	}

}
