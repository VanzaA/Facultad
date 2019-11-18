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

package jade.mtp;

import jade.util.WrapperException;

/**
  Generic exception class for MTP errors.
*/
public class MTPException extends WrapperException {

  /**
     Constructor for exception class.
     @param msg A message detailing the cause of the exception.
   */
  public MTPException(String msg) {
    super(msg);
  }

  /**
     Constructor for exception class.
     @param msg A message detailing the cause of the exception.
     @param t The exception wrapped by this object.
  */
  public MTPException(String msg, Throwable t) {
    super(msg, t);
  }

}
