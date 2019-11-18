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

package jade.core;

//#APIDOC_EXCLUDE_FILE


/**
   This is the interface that must be implemented by a class 
   managing Thread resources on a <code>Container</code>
   @see FullResourceManager
   @author Giovanni Caire - TILAB
 */
public interface ResourceManager {
	// Constants identifying the types of Thread that can be 
	// requested to the ResourceManager 
	public static final int USER_AGENTS = 0;
	public static final int SYSTEM_AGENTS = 1;
	public static final int TIME_CRITICAL = 2;

	/** 
	   Return a Thread without starting it.
	   @param type The type of the Thread that will be returned: valid 
	   types are <code>USER_AGENTS</code>, <code>SYSTEM_AGENTS</code>,
	   <code>TIME_CRITICAL</code>.
	   @param r The <code>Runnable</code> object that will executed by the 
	   returned <code>Thread</code>.
	 */
  public Thread getThread(int type, String name, Runnable r);
  
  public void initialize(Profile p);
  
  public void releaseResources();
}
