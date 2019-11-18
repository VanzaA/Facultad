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

import jade.util.WrapperException;

/**
 * Generic exception of the Frame-based content support.
 *
 * @author Giovanni Caire - TILAB
 */
public class FrameException extends WrapperException {

    /**
       Construct a <code>FrameException</code> with a given message.
       @param message the message
     */
    public FrameException(String message) {
        super(message);
    }
    
    
    /**
       Construct a <code>FrameException</code> with a given message
       and a given nested Throwable.
       @param message The message
       @param t The nested Throwable
     */
    public FrameException(String message, Throwable t) {
        super(message, t);
    }
}

