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

import jade.content.AgentAction;
import jade.util.leap.List;

/**
   This action represents a request to load a <code>Behaviour</code> whose
   code is not included in the classpath of the JVM where the agent that is 
   going to execute the behaviour lives.
   @see jade.core.behaviours.LoaderBehaviour
   @author Giovanni Caire - TILAB
 */
public class LoadBehaviour implements AgentAction {
	private String className;
	private byte[] code;
	private byte[] zip;
	private List parameters;
	
	public LoadBehaviour() {
	}
	
	/**
	   Sets the name of the class of the behaviour to load
	 */
	public void setClassName(String className) {
		this.className = className;
	}
	
	/**
	   @return the name of the class of the behaviour to load
	 */
	public String getClassName() {
		return className;
	}
	
	/**
	   Sets the code of the class of the behaviour to load.
	   <code>code</code> must be filled with the content of the class 
	   file of the behaviour to load. 
	   If the behaviour requires other classes, the <code>setZip()</code>
	   method must be used instead.
	 */
	public void setCode(byte[] code) {
		this.code = code;
	}
	
	/**
	   @return the code of the class of the behaviour to load.
	 */
	public byte[] getCode() {
		return code;
	}
	
	/**
	   Sets the code of the behaviour to load as the content of a zip
	   file.
	 */
	public void setZip(byte[] zip) {
		this.zip = zip;
	}
	
	/**
	   @return the code of the behaviour to load as the content of a zip
	   file.
	 */
	public byte[] getZip() {
		return zip;
	}
	
	/**
	   Set the list of parameters to be passed to the behaviour.
	   These parameters will be inserted into the behaviour 
	   <code>DataStore</code>
	 */
	public void setParameters(List parameters) {
		this.parameters = parameters;
	}
	
	/**
	   @return the list of parameters to be passed to the behaviour.
	 */
	public List getParameters() {
		return parameters;
	}
}