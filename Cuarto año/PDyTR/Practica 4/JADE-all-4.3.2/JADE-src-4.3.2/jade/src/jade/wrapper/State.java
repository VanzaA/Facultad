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
package jade.wrapper;

/**
 * Defines the interface to all concrete implementations of state representation.
   <br>
   <b>NOT available in MIDP</b>
   <br>
 * @author Kevin A. Minder, David Bell, Dick Cowan : Hewlett-Packard
 */
public interface State {

    /**
     * Returns the name of the state.
     * This method will never return null.
     * @return The descriptive name of the state.
     */
    public String getName();

    /**
     * Return the integer code assigned to the state.
     * @return The integer code assigned to the state.
     */
    public int getCode();

    /**
     * Return string representation of state description and its code.
     * @return String representation of this state.
     */
    public String toString();

}
