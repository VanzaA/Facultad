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

import jade.content.AgentAction;

/**
 This class represents the <code>get-keys</code> action of the Introspection Ontology.
 This action allows querying an agent that is running an IntrospectionServer behaviour 
 for the keys whose values can be retrieved by means of the <code>GetValue</code> action

 @author Giovanni Caire - TILAB
 */
public class GetKeys implements AgentAction {
	public GetKeys() {
	}
}
