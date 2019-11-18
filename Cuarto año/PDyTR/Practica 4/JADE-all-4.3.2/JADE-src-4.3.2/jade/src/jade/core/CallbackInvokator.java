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

//#MIDP_EXCLUDE_FILE
//#APIDOC_EXCLUDE_FILE

import java.lang.reflect.*;

/**
   This class is used internally by the framework and is not accessible
   to users.
   @author Giovanni Caire - TILAB
 */
public class CallbackInvokator implements jade.util.leap.Serializable {
	// Package-scoped constructor to avoid creation outside the 
	// container
	CallbackInvokator() {
	}
	
	public void invokeCallbackMethod(Agent a, String name) {
		recursiveInvoke(a, a.getClass(), name);
	}
	
	private void recursiveInvoke(Agent a, Class agentClass, String name) {
		Method callbackMethod = null;
		try {
			callbackMethod = agentClass.getDeclaredMethod(name, (Class[]) null);
			//#J2ME_EXCLUDE_BEGIN
			//#DOTNET_EXCLUDE_BEGIN
			boolean accessibilityChanged = false;
			if (!callbackMethod.isAccessible()) {
				try {
					callbackMethod.setAccessible(true);
					accessibilityChanged = true;
				}
				catch (SecurityException se) {
					System.out.println("Callback method "+name+"() of agent "+a.getName()+" not accessible.");
				}
			}					
			//#DOTNET_EXCLUDE_END
			//#J2ME_EXCLUDE_END
			try { 			
				callbackMethod.invoke(a, (Object[]) null);
				//#J2ME_EXCLUDE_BEGIN
				//#DOTNET_EXCLUDE_BEGIN
				// Restore accessibility if changed
				if (accessibilityChanged) {
					callbackMethod.setAccessible(false);
				}
				//#DOTNET_EXCLUDE_END
				//#J2ME_EXCLUDE_END
			}
			catch (Exception e) {
				System.out.println("Error executing callback method "+name+"() of agent "+a.getName()+". "+e);
			}
		}
		catch (NoSuchMethodException e) {
			// Callback method not defined. Try in the superclass if any.	
			// Otherwise just ignore it
			Class superClass = agentClass.getSuperclass();
			if (superClass != null) {
				recursiveInvoke(a, superClass, name);
			}
		}
		catch (Exception e1) {
			e1.printStackTrace();
		}
	}
}
