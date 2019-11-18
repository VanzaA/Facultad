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
import java.util.Enumeration;
import jade.util.Logger;

/**
 * Implementation of an Iterator constructed by an
 * <code>Enumeration</code>.
 * @version $Date: 2004-12-02 15:39:36 +0100 (gio, 02 dic 2004) $ $Revision: 5473 $
 * @author Fabio Bellifemine, TILAB
 */
public class EnumIterator implements Iterator {

		private Enumeration e;
    /**
     * Constructor declaration
     * 
     */
    public EnumIterator(Enumeration enumeration) {
				e = enumeration;
    } 

    /**
       Checks whether the iterator can scan further by looking at the
       underlying <code>Enumeration</code>.
       @return The return value of the <code>hasMoreElements()</code>
       method of the underlying <code>Enumeration</code>..
     */
    public boolean hasNext() {
      return e.hasMoreElements();
    } 

    /**
       Retrieves the next element in the collection scanned by this
       iterator, forwarding the request to the underlying
       <code>Enumeration</code>.
       @return The return value of the <code>nextElement()</code> of
       the underlying <code>Enumeration</code>.
     */
    public Object next() {
      return e.nextElement();
    } 

    /**
       Remove the element pointed to by this iterator. <b>This
       operation is not supported and this method will always throw a
       runtime exception</b>
     */
    public void remove() {
	RuntimeException e = new RuntimeException("Unsupported Operation");
	Logger logger = Logger.getMyLogger(this.getClass().getName());
	if(logger.isLoggable(Logger.WARNING))
		logger.log(Logger.WARNING,e.getMessage());
	throw e;
    }

  }
