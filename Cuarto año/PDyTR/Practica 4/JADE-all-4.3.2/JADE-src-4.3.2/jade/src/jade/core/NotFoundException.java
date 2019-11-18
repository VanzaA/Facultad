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

/**
  This exception is thrown when some component (agent, agent container, etc.)
  cannot be found in the agent platform data structures.

  @author Giovanni Rimassa - Universita` di Parma
  @version $Date: 2003-05-29 15:27:01 +0200 (gio, 29 mag 2003) $ $Revision: 4046 $
*/
public class NotFoundException extends Exception {

  /**
     Construct a <code>NotFoundException</code> with no detail message
  */
  public NotFoundException() {
      super();
  }

  /**
    Construct a <code>NotFoundException</code> with the given message.
    @param msg The exception message.
  */
  public NotFoundException(String msg) {
    super(msg);
  }
}
