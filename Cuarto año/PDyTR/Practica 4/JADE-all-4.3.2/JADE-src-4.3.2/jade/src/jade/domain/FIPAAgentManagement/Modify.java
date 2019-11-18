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


package jade.domain.FIPAAgentManagement;


import jade.content.*;

/** 
   This class implements the <code>modify</code> action of the 
   <code>fipa-agent-management ontology</code>.
   This action is supported by (and can be requested to) both the AMS and
   the DF.
   @see jade.domain.FIPAAgentManagement.FIPAManagementOntology
   @author Fabio Bellifemine - CSELT S.p.A.
   @version $Date: 2008-03-18 14:37:09 +0100 (mar, 18 mar 2008) $ $Revision: 6022 $
 */
public class Modify implements AgentAction {

	/**
       A string constant for the <code>description</code> slot name.
	 */
	public static final String MODIFY_DESCRIPTION = "description";

	Object description;

	/**
       Default constructor.
	 */
	public Modify() {
	}

	/**
       Set the <code>description</code> slot of this object.
       @param desc An AMS or DF description for this action.
	 */
	public void setDescription(Object desc) {
		description = desc;
	}

	/**
       Retrieve the <code>description</code> slot of this object.
       @return The value of the <code>description</code> slot of
       this action, or <code>null</code> if no value was set.
	 */
	public Object getDescription() {
		return description;
	}

}
