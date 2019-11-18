/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/**
 * ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 * GNU Lesser General Public License
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */
package jade.core;

//#APIDOC_EXCLUDE_FILE


import jade.util.WrapperException;

/**
 * This exception is thrown when an error occurs in the
 * communication with a remote object
 * @author Giovanni Caire - TILAB
 * @author Nicolas Lhuillier - Motorola
 */
 public class IMTPException extends WrapperException {

  /**
   * Constructs an <code>IMTPException</code> with the specified detail message.
   * @param msg The detail message.
   */
  public IMTPException(String msg) {
    super(msg);
  }

  /**
   * Constructs an <code>IMTPException</code> with the specified detail message,
   * wrapping the given <code>Throwable</code>object.
   * @param msg The detail message.
   * @param t The exception to wrap.
   */
  public IMTPException(String msg, Throwable t) {
    super(msg, t);
  }

}

