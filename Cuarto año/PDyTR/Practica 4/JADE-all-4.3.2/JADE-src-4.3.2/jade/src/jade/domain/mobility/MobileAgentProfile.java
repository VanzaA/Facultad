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

package jade.domain.mobility;

import jade.core.*;
import jade.content.*;

/**

  This class represents the <code>mobile-agent-profile</code> concept.

  @author Giovanni Rimassa -  Universita' di Parma
  @version $Date: 2003-11-19 17:04:37 +0100 (mer, 19 nov 2003) $ $Revision: 4567 $
*/
public class MobileAgentProfile implements Concept {

    private MobileAgentSystem system;
    private MobileAgentLanguage language;
    private MobileAgentOS os;


    /**
       Default constructor. A default constructor is necessary for
       ontological classes.
    */
    public MobileAgentProfile() {
    }

    /**
       Set the <code>system</code> slot of this action.
       @param s The runtime system of the described agent.
    */
    public void setSystem(MobileAgentSystem s) {
      system = s;
    }

    /**
       Retrieve the value of the <code>system</code> slot of this
       action, containing the runtime system of the described agent.
       @return The value of the <code>system</code> slot, or
       <code>null</code> if no value was set.
    */
    public MobileAgentSystem getSystem() {
      return system;
    }

    /**
       Set the <code>language</code> slot of this action.
       @param l The language of the described agent.
    */
    public void setLanguage(MobileAgentLanguage l) {
      language = l;
    }

    /**
       Retrieve the value of the <code>language</code> slot of this
       action, containing the language of the described agent.
       @return The value of the <code>language</code> slot, or
       <code>null</code> if no value was set.
    */
    public MobileAgentLanguage getLanguage() {
      return language;
    }

    /**
       Set the <code>os</code> slot of this action.
       @param o The OS of the described agent.
    */
    public void setOS(MobileAgentOS o) {
      os = o;
    }

    /**
       Retrieve the value of the <code>os</code> slot of this action,
       containing the OS of the described agent.
       @return The value of the <code>os</code> slot, or
       <code>null</code> if no value was set.
    */
    public MobileAgentOS getOS() {
      return os;
    }

  }
