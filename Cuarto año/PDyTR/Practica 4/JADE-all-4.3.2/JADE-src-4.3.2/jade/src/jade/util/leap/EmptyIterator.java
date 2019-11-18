/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2003 TILAB 

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

package jade.util.leap;
import java.util.NoSuchElementException;

/**
 * Singleton Iterator for an empty collection. 
 * The usage of the singleton-pattern for this simple object allows just 
 * to save some memory. 
 * @version $Date: 2003-11-20 17:04:51 +0100 (gio, 20 nov 2003) $ $Revision: 4573 $
 * @author Fabio Bellifemine, TILAB
 */
public class EmptyIterator implements Iterator {

		private final static Iterator it = new EmptyIterator();

    /**
     * Constructor declaration
     * 
     */
    private EmptyIterator() {
    } 

		/**
		 * Returns the singleton EmptyIterator object.
		 * Most of the methods of this class are instance methods and must be 
		 * invoked with respect to the singleton object. 
		 **/
		public static Iterator getInstance() {
				return it;
		}

    /**
       Checks whether the iterator can scan further.
       @return This method always returns <code>false</code>.
     */
    public boolean hasNext() {
      return false;
    } 

    /**
       Retrieves the next element in the collection scanned by this
       iterator.
       @return This method always throws an exception.
       @throws NoSuchElementException Always, because the underlying
       collection is always empty.
     */
    public Object next() {
      throw new NoSuchElementException();
    } 

    /**
       Remove the element pointed to by this iterator from the
       collection.
       @throws RuntimeException Always, because the underlying
       collection is always empty.
     */
    public void remove() {
      throw new RuntimeException();
    } 

  }
