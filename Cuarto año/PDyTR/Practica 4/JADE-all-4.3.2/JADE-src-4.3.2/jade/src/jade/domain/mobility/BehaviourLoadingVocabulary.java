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

import jade.domain.FIPAAgentManagement.ExceptionVocabulary;

/**
   This interface contains all the string constants for concepts and slot
   names defined in the
   <code>Behaviour-Loading</code> ontology.
*/
public interface BehaviourLoadingVocabulary extends ExceptionVocabulary {
	
  /**
    A symbolic constant, containing the name of this ontology.
   */
  public static final String NAME = "Behaviour-Loading";

  // Concepts
  public static final String PARAMETER = "parameter";
  public static final String PARAMETER_NAME	= "name";
  public static final String PARAMETER_VALUE	= "value";
  public static final String PARAMETER_MODE	= "mode";
  
  // Actions
  public static final String LOAD_BEHAVIOUR = "load-behaviour";
  public static final String LOAD_BEHAVIOUR_CLASS_NAME = "class-name";
  public static final String LOAD_BEHAVIOUR_CODE = "code";
  public static final String LOAD_BEHAVIOUR_ZIP = "zip";
  public static final String LOAD_BEHAVIOUR_PARAMETERS = "parameters";
}
