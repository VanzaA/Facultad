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


package jade.domain.introspection;

//#APIDOC_EXCLUDE_FILE

import jade.core.AID;

import jade.domain.FIPAAgentManagement.APDescription;
/**
   This class represents the <code>platform-description</code>
   predicate, whose argument is a platform description object from the
   FIPA Agent Management ontology.
   
   @author Tiziana Trucco -  CSELT S.p.A.
   @version $Date: 2005-02-16 18:18:28 +0100 (mer, 16 feb 2005) $ $Revision: 5552 $
*/
public class PlatformDescription implements Event {

    /**
       A string constant for the name of this event.
    */
    public static final String NAME = IntrospectionVocabulary.PLATFORMDESCRIPTION;

    private APDescription platform;

    /**
       Default constructor. A default constructor is necessary for
       ontological classes.
    */
    public PlatformDescription() {
    }

    /**
       Retrieve the name of this event.
       @return A constant value for the event name.
    */
    public String getName() {
	return NAME;
    }

    /**
       Set the <code>platform</code> slot of this event.
       @param p The <code>ap-description</code> this notification
       refers to.
    */
    public void setPlatform(APDescription p) {
	platform = p;
    }

    /**
       Retrieve the value of the <code>platform</code> slot of this
       event, containing the <code>ap-description</code> this
       notification refers to.
       @return The value of the <code>platform</code> slot, or
       <code>null</code> if no value was set.
    */
    public APDescription getPlatform() {
	return platform;
    }


}
