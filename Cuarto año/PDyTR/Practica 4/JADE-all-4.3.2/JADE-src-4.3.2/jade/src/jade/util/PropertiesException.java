/*
 * (c) Copyright Hewlett-Packard Company 2001
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
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

package jade.util;

//#MIDP_EXCLUDE_FILE

/**
 * Property related exception.
 * @author Dick Cowan - HP Labs
 */
public class PropertiesException extends RuntimeException {

    /**
     * Constructs a ApplicationException with null as its error detail message.
     */
    public PropertiesException() {
        super();
    }

    /**
     * Construct a PropertiesException with the specified detail message.
     * @param aMessage The detail message.
     */
    public PropertiesException(String aMessage) {
        super(aMessage);
    }
}
