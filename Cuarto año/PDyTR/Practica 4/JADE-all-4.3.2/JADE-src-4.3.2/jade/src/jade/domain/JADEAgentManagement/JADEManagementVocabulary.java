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

package jade.domain.JADEAgentManagement;

import jade.domain.FIPAAgentManagement.ExceptionVocabulary;

/**
   This interface contains all the string constants for frame and slot
   names of exceptions defined in the
   <code>jade-agent-management</code> ontology.
*/
public interface JADEManagementVocabulary extends ExceptionVocabulary {	
	/**
    A symbolic constant, containing the name of this ontology.
   */
  public static final String NAME = "JADE-Agent-Management";

  // Concepts
  public static final String LOCATION = "location";
  public static final String LOCATION_NAME = "name";
  public static final String LOCATION_ADDRESS = "address";
  public static final String LOCATION_PROTOCOL = "protocol";

  public static final String CONTAINERID = "container-ID";
  public static final String CONTAINERID_MAIN = "main";
  public static final String CONTAINERID_PORT = "port";
  public static final String CONTAINERID_PROTOCOL = "protocol";

  
  
  public static final String PLATFORMID = "platform-ID";
  
  // Actions supported by the ams
  public static final String QUERYAGENTSONLOCATION = "query-agents-on-location";
  public static final String QUERYAGENTSONLOCATION_LOCATION = "location";
  
  public static final String SHUTDOWNPLATFORM = "shutdown-platform";
  public static final String KILLCONTAINER = "kill-container";
  public static final String KILLCONTAINER_CONTAINER = "container";
  public static final String KILLCONTAINER_PASSWORD = "password";
  
  public static final String CREATEAGENT = "create-agent";
  public static final String CREATEAGENT_AGENT_NAME = "agent-name";
  public static final String CREATEAGENT_CLASS_NAME = "class-name";
  public static final String CREATEAGENT_ARGUMENTS = "arguments";
  public static final String CREATEAGENT_CONTAINER = "container";
  //#MIDP_EXCLUDE_BEGIN
  public static final String CREATEAGENT_OWNER = "owner";
  public static final String CREATEAGENT_INITIAL_CREDENTIALS = "initial-credentials";
	//#MIDP_EXCLUDE_END
	
  public static final String KILLAGENT = "kill-agent";
  public static final String KILLAGENT_AGENT = "agent";
  public static final String KILLAGENT_PASSWORD = "password";

  public static final String INSTALLMTP = "install-mtp";
  public static final String INSTALLMTP_ADDRESS = "address";
  public static final String INSTALLMTP_CONTAINER = "container";
  public static final String INSTALLMTP_CLASS_NAME = "class-name";

  public static final String UNINSTALLMTP = "uninstall-mtp";
  public static final String UNINSTALLMTP_ADDRESS = "address";
  public static final String UNINSTALLMTP_CONTAINER = "container";

  public static final String SNIFFON = "sniff-on";
  public static final String SNIFFON_SNIFFER = "sniffer";
  public static final String SNIFFON_SNIFFED_AGENTS = "sniffed-agents";
  public static final String SNIFFON_PASSWORD = "password";

  public static final String SNIFFOFF = "sniff-off";
  public static final String SNIFFOFF_SNIFFER = "sniffer";
  public static final String SNIFFOFF_SNIFFED_AGENTS = "sniffed-agents";
  public static final String SNIFFOFF_PASSWORD = "password";

  public static final String DEBUGON = "debug-on";
  public static final String DEBUGON_DEBUGGER = "debugger";
  public static final String DEBUGON_DEBUGGED_AGENTS = "debugged-agents";
  public static final String DEBUGON_PASSWORD = "password";

  public static final String DEBUGOFF = "debug-off";
  public static final String DEBUGOFF_DEBUGGER = "debugger";
  public static final String DEBUGOFF_DEBUGGED_AGENTS = "debugged-agents";
  public static final String DEBUGOFF_PASSWORD = "password";

  public static final String WHEREISAGENT = "where-is-agent";
  public static final String WHEREISAGENT_AGENTIDENTIFIER = "agent-identifier";
  
  public static final String QUERY_PLATFORM_LOCATIONS = "query-platform-locations";

  // actions supported by the DF
  public static final String SHOWGUI = "showgui";

  // Exception Predicates
  public static final String NOTREGISTERED = jade.domain.FIPAAgentManagement.FIPAManagementVocabulary.NOTREGISTERED;
  public static final String ALREADYREGISTERED = jade.domain.FIPAAgentManagement.FIPAManagementVocabulary.ALREADYREGISTERED;
  
  // additional constants.
  public static final String CONTAINER_WILDCARD = "%C";
}
