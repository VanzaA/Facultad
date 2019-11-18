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


package jade.domain.DFGUIManagement;

import jade.core.AID;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.content.AgentAction;

/**
   This class implements the Federate action.
   @author Elisabetta Cortese -  TiLab S.p.A.
   @version $Date: 2003-08-26 11:15:34 +0200 (mar, 26 ago 2003) $
*/

public class Federate implements AgentAction {
	
	//private Object parentDF;
	//private Object childrenDF;
	private AID df;
	private DFAgentDescription description;

	public void setDf(AID df) {
		this.df = df;
	}
	 
	public AID getDf() {
		return df;
	} 
	
	public void setDescription(DFAgentDescription description) {
		this.description = description;
	}
  
	public DFAgentDescription getDescription() {
		return description;
	}
	
	/**
	   @deprecated Use setDf() instead.
	 */
	public void setParentDF(Object parent)
	{
		setDf((AID) parent);
	}
	 
	/**
	   @deprecated Use getDf() instead.
	 */
	public Object getParentDF()
	{
		return getDf();
	} 
	
	/**
	   @deprecated Use setDescription() instead.
	 */
	public void setChildrenDF(Object desc)
	{
		setDescription((DFAgentDescription) desc);
	}
  
	/**
	   @deprecated Use getDescription() instead.
	 */
	public Object getChildrenDF()
	{
		return getDescription();
	}
}