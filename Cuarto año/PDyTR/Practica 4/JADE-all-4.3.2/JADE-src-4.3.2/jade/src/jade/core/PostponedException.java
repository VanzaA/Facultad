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

package jade.core;

//#J2ME_EXCLUDE_FILE
//#APIDOC_EXCLUDE_FILE

/**
   This exception is thrown by the FrontEndStub to indicate that 
   a commmand has been postponed due to a temporary disconnection 
   and will be delivered as soon as the FrontEnd will reconnect.
   The BackEnd container uses this indication to "simulate" the 
   effects of the command thus hiding the delivery delay to the 
   rest of the platform.
   It is declared as a RuntimeException to avoid declaring it in
   the FrontEnd interface.
   
   @author Giovanni Caire - TILAB
 */
public class PostponedException extends RuntimeException {
	public PostponedException() {
		super();
	}
}
