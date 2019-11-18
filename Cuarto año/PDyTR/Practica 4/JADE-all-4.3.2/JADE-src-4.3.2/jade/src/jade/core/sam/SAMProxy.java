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

package jade.core.sam;

//#DOTNET_EXCLUDE_FILE

import jade.core.GenericCommand;
import jade.core.IMTPException;
import jade.core.Node;
import jade.core.ServiceException;
import jade.core.SliceProxy;


public class SAMProxy extends SliceProxy implements SAMSlice {
	private static final long serialVersionUID = 87469234984L;

	public SAMInfo getSAMInfo() throws IMTPException {
		try {
			GenericCommand cmd = new GenericCommand(H_GETSAMINFO, SAMHelper.SERVICE_NAME, null);
			
			Node n = getNode();
			Object result = n.accept(cmd);
			if (result instanceof IMTPException) {
				throw (IMTPException)result;
			}
			else if (result instanceof Throwable) {
				throw new IMTPException("An undeclared exception was thrown", (Throwable)result);
			}
			
			return (SAMInfo) result;
		}
		catch(ServiceException se) {
			throw new IMTPException("Unable to access remote node", se);
		}
	}
}
