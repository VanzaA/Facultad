/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2002 TILAB S.p.A. 

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

package jade.security;

/**
	The <code>JADESecurityException</code> is the base class for
	exceptions thrown for unauthorized access or for authentication
	failures.
	
	@author Jerome Picault - Motorola Labs
	@version $Date: 2004-05-19 17:52:31 +0200 (mer, 19 mag 2004) $ $Revision: 5082 $
*/
public class JADESecurityException extends Exception {

	/**
		Creates a new JADESecurityException.
		@param msg The message of the exception.
	*/
	public JADESecurityException(String msg) {
		super(msg);
	}
}
