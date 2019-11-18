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

  This class represents the <code>mobile-agent-system</code> concept.

  @author Giovanni Rimassa -  Universita' di Parma
  @version $Date: 2003-11-19 17:04:37 +0100 (mer, 19 nov 2003) $ $Revision: 4567 $
*/
public class MobileAgentSystem implements Concept {


    private String name;
    private Long majorVersion;
    private Long minorVersion;
    private String dependencies;

    /**
       Default constructor. A default constructor is necessary for
       ontological classes.
    */
    public MobileAgentSystem() {
    }

    /**
       Set the <code>name</code> slot of this action.
       @param n The runtime system of the described agent.
    */
    public void setName(String n) {
      name = n;
    }

    /**
       Retrieve the value of the <code>name</code> slot of this
       action, containing the runtime system of the described agent.
       @return The value of the <code>name</code> slot, or
       <code>null</code> if no value was set.
    */
    public String getName() {
      return name;
    }

    /**
       Set the <code>major-version</code> slot of this action.
       @param v The major version number of the runtime system of the
       described agent.
    */
    public void setMajorVersion(Long v) {
      majorVersion = v;
    }

    /**
       Retrieve the value of the <code>major-version</code> slot of
       this event, containing the major version number of the runtime
       system of the described agent.
       @return The value of the <code>major-version</code> slot, or
       <code>null</code> if no value was set.
    */
    public Long getMajorVersion() {
      return majorVersion;
    }

    /**
       Set the <code>minor-version</code> slot of this action.
       @param v The minor version number of the runtime system of the
       described agent.
    */
    public void setMinorVersion(Long v) {
      minorVersion = v;
    }

    /**
       Retrieve the value of the <code>minor-version</code> slot of
       this event, containing the minor version number of the runtime
       system of the described agent.
       @return The value of the <code>minor-version</code> slot, or
       <code>null</code> if no value was set.
    */
    public Long getMinorVersion() {
      return minorVersion;
    }

    /**
       Set the <code>dependencies</code> slot of this action.
       @param d The runtime system dependencies of the described
       agent.
    */
    public void setDependencies(String d) {
      dependencies = d;
    }

    /**
       Retrieve the value of the <code>dependencies</code> slot of
       this event, containing the runtime system dependencies of the
       described agent.
       @return The value of the <code>dependencies</code> slot, or
       <code>null</code> if no value was set.
    */
    public String getDependencies() {
      return dependencies;
    }

}
