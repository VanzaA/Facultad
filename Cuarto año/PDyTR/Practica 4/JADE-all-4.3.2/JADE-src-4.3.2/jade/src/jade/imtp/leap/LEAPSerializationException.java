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
 * Copyright (C) 2001 Siemens AG.
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

/**
 * A <code>LEAPSerializationException</code> is used as generic exception to ease
 * the handling of all exceptions that may occur during the LEAP surrogate
 * serialization mechanism. The LEAP surrogate serialization mechanism only throws
 * this type of exception and encapsulates an occurred exception. This way, all
 * possible exceptions during LEAP serialization can be handled by handling
 * this <code>LEAPSerializationException</code>. If the detailed exception
 * is needed, it is available in field <code>detail</code>.
 * 
 * @author Michael Watzke
 * @version 1.0, 09/11/2000
 */
public class LEAPSerializationException extends Exception {

  /**
   * Nested Exception to hold wrapped exception.
   */
  public Throwable detail;

  /**
   * Constructs a <code>LEAPSerializationException</code> with no specified
   * detail message.
   */
  public LEAPSerializationException() {
  } 

  /**
   * Constructs a <code>LEAPSerializationException</code> with the specified
   * detail message.
   * @param s the detail message
   */
  public LEAPSerializationException(String s) {
    super(s);
  }

  /**
   * Constructs a <code>LEAPSerializationException</code> with the specified
   * detail message and nested exception.
   * @param s the detail message
   * @param ex the nested exception
   */
  public LEAPSerializationException(String s, Throwable ex) {
    super(s);

    detail = ex;
  }

  /**
   * Get the detail message, including the message from the nested
   * exception if there is one.
   * @return the detail message
   */
  public String getMessage() {
    if (detail == null) {
      return super.getMessage();
    } 
    else {
      return super.getMessage()+"; nested exception is: \n\t"+detail.toString();
    } 
  } 

}

