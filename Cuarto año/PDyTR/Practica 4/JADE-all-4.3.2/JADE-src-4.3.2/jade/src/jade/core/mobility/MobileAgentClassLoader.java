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

package jade.core.mobility;


import jade.core.ServiceFinder;
import jade.core.IMTPException;
import jade.core.ServiceException;
import jade.util.Logger;


//#MIDP_EXCLUDE_FILE

/**
   @author Giovanni Rimassa - FRAMeTech s.r.l.
 */
class MobileAgentClassLoader extends ClassLoader {

    private AgentMobilitySlice classServer;
    private String agentName;
    private String sliceName;
    private ServiceFinder finder;
    private Logger myLogger = Logger.getMyLogger(AgentMobilityService.NAME);

    public MobileAgentClassLoader(String an, String sn, ServiceFinder sf, ClassLoader parent) throws IMTPException, ServiceException {
    	//#PJAVA_EXCLUDE_BEGIN
    	super(parent);
		//#PJAVA_EXCLUDE_END

		agentName = an;
	    sliceName = sn;
	    finder = sf;
	    classServer = (AgentMobilitySlice)finder.findSlice(AgentMobilitySlice.NAME, sliceName);
	    if (classServer == null) {
			throw new ServiceException("Code source container "+sliceName+" does not exist or does not support mobility");
	    }
    }

    protected Class findClass(String name) throws ClassNotFoundException {
    	byte[] classFile;

    	try {
    		if(myLogger.isLoggable(Logger.FINE)) {
    			myLogger.log(Logger.FINE,"Remote retrieval of code for class " + name);
    		}
    		try {
    			classFile = classServer.fetchClassFile(name, agentName);
    		}
    		catch(IMTPException imtpe) {
    			// Get a fresh slice and retry
    			classServer = (AgentMobilitySlice)finder.findSlice(AgentMobilitySlice.NAME, sliceName);
    			classFile = classServer.fetchClassFile(name, agentName);
    		}
    	}
    	catch (IMTPException imtpe) {
    		imtpe.printStackTrace();
    		throw new ClassNotFoundException(name);
    	}
    	catch (ServiceException se) {
    		throw new ClassNotFoundException(name);
    	}

    	if (classFile != null) {
    		if(myLogger.isLoggable(Logger.FINE)) {
    			myLogger.log(Logger.FINE,"Code of class " + name + " retrieved. Length is " + classFile.length);
    		}
    		return defineClass(name, classFile, 0, classFile.length);
    	}
    	else {
    		throw new ClassNotFoundException(name);
    	}
    }
    
    protected Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
    	Class c = null;
    	if(myLogger.isLoggable(Logger.FINER)) {
    		myLogger.log(Logger.FINER,"Loading class " + name);
    	}
		//#PJAVA_EXCLUDE_BEGIN
		c = super.loadClass(name, resolve);
		//#PJAVA_EXCLUDE_END
		
		//#DOTNET_EXCLUDE_BEGIN
		/*#PJAVA_INCLUDE_BEGIN In PersonalJava loadClass(String, boolean) is abstract --> we must implement it
	  	// 1) Try to see if the class has already been loaded
	  	c = findLoadedClass(name);
	
		// 2) Try to load the class using the system class loader
	  	if(c == null) {
		    try {
		        c = findSystemClass(name);
		    }
		    catch (ClassNotFoundException cnfe) {
		    }
		}
	
	  	// 3) If still not found, try to load the class from the proper site
	  	if(c == null) {
	  	    c = findClass(name);
	  	}
	
	  	if(resolve) {
	  	    resolveClass(c);
	  	}
		#PJAVA_INCLUDE_END*/
		//#DOTNET_EXCLUDE_END
	
		/*#DOTNET_INCLUDE_BEGIN
		System.Type myType = System.Type.GetType(name);
		if (myType == null)
		{
			//resolveClass(c);
			boolean found = false;
			int i = 0;
			System.Reflection.Assembly[] assemblies =  System.AppDomain.get_CurrentDomain().GetAssemblies();
	
			while (!found && i<assemblies.length)
			{
				myType = assemblies[i].GetType(name);
				found = (myType != null);
				i++;
			}
	
			c = Class.FromType( myType );
		}
		#DOTNET_INCLUDE_END*/
	
		if(myLogger.isLoggable(Logger.FINER)) {
			myLogger.log(Logger.FINER,"Class " + name + " loaded" );
		}
	
	  	return c;
    }
}
