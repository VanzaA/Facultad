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

package jade.imtp.leap;

/**
   This interface provides a callback method that is called
   by the JADE runtime (front-end of a split container) when connection 
   specific events happen on the device.
   Application developers wishing to handle these events may provide
   a class implementing this interface and set the 
   <code>connection-listener</code> property to the fully qualified name 
   of that class. ConnectionListener implementation classes must have 
   an accessible default constructor;<br>
   Alternatively an object implementing the ConnectionListener interface 
   may be put in the activation <code>Properties</code> specified at
   JADE runtime activation.
   @author Giovanni Caire - TILAB
 */
public interface ConnectionListener {
	/**
	   This event is rised just before each attempt to create 
	   a network connection. A common use case consists in reacting to
	   it to set up an appropriate PDP context just if not in place
	   already.
	 */
	public static final int BEFORE_CONNECTION = 1;
	
	/**
	   This event is raised whenever a temporary disconnection 
	   is detected.
	 */
	public static final int DISCONNECTED = 2;
	
	/**
	   This event is raised whenever a the device reconnects
	   after a temporary disconnection.
	 */
	public static final int RECONNECTED = 3;

	/**
	   This event is raised whenever a the FrontEnd drops down the 
	   connection with the BackEnd since no data has been transferred 
	   over the connection since a while. This can only happen if the
	   <code>drop-down-time</code> option is set to a value > 0.
	 */
	public static final int DROPPED = 4;

	/**
	   This event is raised when the device detects it is no longer
	   possible to reconnect (e.g. because the maximum disconnection 
	   timeout expired) 
	 */
	public static final int RECONNECTION_FAILURE = 5;

	/**
	   This event is raised when the mediator replies with a BE Not Found
	   to a CONNECT_MEDIATOR request.
	 */
	public static final int BE_NOT_FOUND = 6;
	
	/**
	   This event is raised when the mediator replies with an error
	   response of type Not Authorized to a CREATE_MEDIATOR or 
	   CONNECT_MEDIATOR request.
	 */
	public static final int NOT_AUTHORIZED = 7;

	/**
	   This callback method is called by the JADE runtime (front-end of 
	   a split container) when connection specific events happen on the 
	   device.
	   @param ev The event that happened
     @param info an object parameter of the event. 
	 */
	public void handleConnectionEvent(int ev, Object info);
}