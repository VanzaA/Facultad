/**
 * ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
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
package jade.content.frame;

/**
   Generic interface representing all non-primitive elements 
   that can appear in a content expression.
   Each frame has a type-name accessible by means of the 
   <code>getTypeName()</code> method and a number of composing
   elements that can be primitive elements or frames themselves.
   The way composing elements are stored within and retrieved 
   from a Frame depends on the specific type of Frame.
   @see OrderedFrame, QualifiedFrame
   @author Giovanni Caire - TILAB
 */
public interface Frame {
	/**
	   @return the type-name of this Frame
	 */
	String getTypeName();
}

