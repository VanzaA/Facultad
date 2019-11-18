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

//#MIDP_EXCLUDE_FILE

import jade.content.Concept;
import jade.util.leap.List;

/**
   This concept represents a parameter to be passed to a <code>Behaviour</code>
   in the dynamic loading procedure.
   @see LoadBehaviour
   @see jade.core.behaviours.LoaderBehaviour
   @author Giovanni Caire - TILAB
 */
public class Parameter implements Concept {
  // Parameter mode values
  public static final int IN_MODE = 0;
  public static final int OUT_MODE = 1;
  public static final int INOUT_MODE = 2;
  
	private String name;
	private Object value;
	private int mode = IN_MODE;
	
	public Parameter() {
	}
	
	public Parameter(String name, Object value) {
		this.name = name;
		this.value = value;
	}
	
	public Parameter(String name, Object value, int mode) {
		this.name = name;
		this.value = value;
		this.mode = mode;
	}
	
	/**
	   Sets the name of this parameter. This will be used as
	   the key in the dinamically loaded behaviour <code>DataStore</code>
	   for the parameter value
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	   @return the name of this parameter.
	 */
	public String getName() {
		return name;
	}
	
	/**
	   Sets the value of this parameter. The BehaviourLoading
	   ontology extends the SerializableOntology and therefore 
	   whatever <code>Serializable</code> object can be used. 
	 */
	public void setValue(Object value) {
		this.value = value;
	}
	
	/**
	   @return the value of this parameter.
	 */
	public Object getValue() {
		return value;
	}
	
	/**
	   Sets the IO mode of this parameter. 
	   This must be one of <code>IN_MODE</code>, <code>OUT_MODE</code>,
	   <code>INOUT_MODE</code>.
	 */
	public void setMode(int mode) {
		this.mode = mode;
	}
	
	/**
	   @return the IO mode of this parameter.
	 */
	public int getMode() {
		return mode;
	}
}