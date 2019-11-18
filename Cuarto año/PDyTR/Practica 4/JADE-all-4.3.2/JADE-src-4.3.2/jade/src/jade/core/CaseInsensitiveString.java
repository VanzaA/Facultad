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

import jade.util.leap.Serializable;

/**
 * A name string, with case insensitive comparison and equality operations.
 * This class holds a <code>String</code> inside, preserving the case; however,
 * all the equality and comparision operations are performed in a case
 * insensitive fashion.
 * @author Giovanni Rimassa - Universita' di Parma
 * @version $Date: 2005-10-28 14:20:41 +0200 (ven, 28 ott 2005) $ $Revision: 5806 $
 * 
 * Updated 1/06/2001 12:50 by Dmitri Toropov - Siemens AG
 */
public class CaseInsensitiveString implements Serializable {

    /**
     * @serial
     */
    private final String s;
    private final int hashCode;

    /**
     * Create a new <code>CaseInsensitiveString</code> object.
     * @param name The string that will be kept inside this object.
     */
    public CaseInsensitiveString(String name) {
        s = name;
				// the contract of String is to return 0 as the hash of an empty string
				// so I am assuming here than a null string is equal to an empty string
				// this trick avoid generating a null pointer exception
        hashCode = (s == null ? 0 : s.toLowerCase().hashCode());
    }

    /**
     * Converts the <code>CaseInsensitiveString</code> object into a
     * string.
     * @return The string stored inside by the constructor.
     */
    public final String toString() {
        return s;
    } 

    /**
     * Equality operation. This method compares a
     * <code>CaseInsensitiveString</code> object with another or with
     * a Java <code>String</code>. The comparison is case insensitive.
     * @param o The Java object to compare this
     * <code>CaseInsensitiveString</code> to.
     * @return <code>true</code> if the strings contained within the two objects
     * are equal, apart from case.
     */
    public final boolean equals(Object o) {
			if (o == null) {
				return false;
			}
			if (o instanceof String) {
			  return equalsIgnoreCase(s, (String) o);
			}
			if(o instanceof CaseInsensitiveString) {
			  return equalsIgnoreCase(s, ((CaseInsensitiveString)o).s);
			}
			return false;
    }

    /**
     * Hash code. This method returns an hash code in such a way that
     * two <code>CaseInsensitiveString</code> objects differing only
     * in case have the same hash code.
     * @return The hash code for this
     * <code>CaseInsensitiveString</code> object.
     */
    public final int hashCode() {
        return hashCode;
    } 

    /**
     * Static method for case insensitive string comparasion.
     * For comparasion used the regionMatches approach which
     * doesn't allocate any additional memory.
     * @param s1, s2 The <code>String</code> objects to compare
     * @return <code>true</code> if the strings are equal, apart from case.
     */
    public static final boolean equalsIgnoreCase(String s1, String s2) {
     	if (s1 == null || s2 == null) {
     		return false;
     	}
     	else {
    		return ((s1.length() == s2.length()) 
                && s1.regionMatches(true, 0, s2, 0, s1.length()));
     	}
    } 

}

