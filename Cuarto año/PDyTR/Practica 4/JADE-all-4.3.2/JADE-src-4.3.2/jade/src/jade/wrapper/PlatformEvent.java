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

package jade.wrapper;

import java.util.EventObject;

/**
   <br>
   <b>NOT available in MIDP</b>
   <br>
*/
public interface PlatformEvent {

    //String agentGUID, platformName;
    //int eventType;

    /**
       String constant for the name of the <code>born-agent</code> event.
    */
    public static final int BORN_AGENT = 3;

    /**
       String constant for the name of the <code>dead-agent</code> event.
    */
    public static final int DEAD_AGENT = 4;

    /**
       String constant for the name of the <code>started-platform</code> event.
    */
    public static final int STARTED_PLATFORM = 100;

    /**
       String constant for the name of the <code>suspended-platform</code> event.
    */
    public static final int SUSPENDED_PLATFORM = 101;

    /**
       String constant for the name of the <code>resumed-platform</code> event.
    */
    public static final int RESUMED_PLATFORM = 102;

    /**
       String constant for the name of the <code>killed-platform</code> event.
    */
    public static final int KILLED_PLATFORM = 103;

    /*PlatformEvent(Object anObject, int eventType, String agentGUID, String platformName) {
        super(anObject);
				this.eventType = eventType;
				this.agentGUID = agentGUID;
				this.platformName = platformName;
    }*/

    //public String getAgentGUID() {return agentGUID;}
    //public String getPlatformName() {return platformName;}
    //public int getEventType() {return eventType;}

    /**
       Retrieve the global agent name (i.e. the local name and the
       platform ID).
       @return The global name of the agent this event refers to.
    */
    public String getAgentGUID();

    /**
       Retrieve the platform name.
       @return The name of the platform this event refers to.
    */
    public String getPlatformName();

    /**
       Retrieve the event type.
       @return The type of this event.
    */
    public int getEventType();
}
