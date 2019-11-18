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

package jade.domain.persistence;

import jade.domain.FIPAAgentManagement.ExceptionVocabulary;

/**
   This interface contains all the string constants for frame and slot
   names of exceptions defined in the <code>jade-persistence</code>
   ontology.

   @author Giovanni Rimassa - FRAMeTech
*/
public interface PersistenceVocabulary extends ExceptionVocabulary {
    /**
       A symbolic constant, containing the name of this ontology.
    */
    public static final String NAME = "JADE-Persistence";

    // Concepts
    public static final String AGENTGROUP = "agent-group";

    public static final String LOCATION = "location";
    public static final String LOCATION_NAME = "name";
    public static final String LOCATION_ADDRESS = "address";
    public static final String LOCATION_PROTOCOL = "protocol";

    public static final String CONTAINERID = "container-ID";

    // Actions supported by the ams
  
    public static final String SAVEAGENT = "save-agent";
    public static final String SAVEAGENT_AGENT = "agent";
    public static final String SAVEAGENT_REPOSITORY = "repository";

    public static final String LOADAGENT = "load-agent";
    public static final String LOADAGENT_AGENT = "agent";
    public static final String LOADAGENT_REPOSITORY = "repository";
    public static final String LOADAGENT_WHERE = "where";

    public static final String RELOADAGENT = "reload-agent";
    public static final String RELOADAGENT_AGENT = "agent";
    public static final String RELOADAGENT_REPOSITORY = "repository";

    public static final String DELETEAGENT = "delete-agent";
    public static final String DELETEAGENT_AGENT = "agent";
    public static final String DELETEAGENT_REPOSITORY = "repository";
    public static final String DELETEAGENT_WHERE = "where";

    public static final String FREEZEAGENT = "freeze-agent";
    public static final String FREEZEAGENT_AGENT = "agent";
    public static final String FREEZEAGENT_REPOSITORY = "repository";
    public static final String FREEZEAGENT_BUFFERCONTAINER = "buffer-container";

    public static final String THAWAGENT = "thaw-agent";
    public static final String THAWAGENT_AGENT = "agent";
    public static final String THAWAGENT_REPOSITORY = "repository";
    public static final String THAWAGENT_NEWCONTAINER = "new-container";

    public static final String SAVECONTAINER = "save-container";
    public static final String SAVECONTAINER_CONTAINER = "container";
    public static final String SAVECONTAINER_REPOSITORY = "repository";

    public static final String LOADCONTAINER = "load-container";
    public static final String LOADCONTAINER_CONTAINER = "container";
    public static final String LOADCONTAINER_REPOSITORY = "repository";

    public static final String DELETECONTAINER = "delete-container";
    public static final String DELETECONTAINER_CONTAINER = "container";
    public static final String DELETECONTAINER_REPOSITORY = "repository";
    public static final String DELETECONTAINER_WHERE = "where";

    public static final String SAVEAGENTGROUP = "save-agent-group";
    public static final String SAVEAGENTGROUP_GROUP = "group";
    public static final String SAVEAGENTGROUP_REPOSITORY = "repository";

    public static final String DELETEAGENTGROUP = "delete-agent-group";
    public static final String DELETEAGENTGROUP_GROUP = "group";
    public static final String DELETEAGENTGROUP_REPOSITORY = "repository";

    public static final String LOADAGENTGROUP = "load-agent-group";
    public static final String LOADAGENTGROUP_GROUP = "group";
    public static final String LOADAGENTGROUP_REPOSITORY = "repository";
    // FIXME: More slots are needed (deployment vector for the group)

    // Exception Predicates
    public static final String NOTREGISTERED = jade.domain.FIPAAgentManagement.FIPAManagementVocabulary.NOTREGISTERED;

}
