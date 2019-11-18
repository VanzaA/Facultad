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

//#J2ME_EXCLUDE_FILE
//#APIDOC_EXCLUDE_FILE

import jade.util.leap.Map;
import jade.util.leap.HashMap;
import jade.util.leap.Properties;
import jade.security.*;

import jade.util.Logger;

/**
   This class is an auxiliary JADE Node that act as parent node
   for all back-ends in the local JVM 
   @author Giovanni Caire - TILAB
 */
public class BackEndManager {
  // The singleton BackEndManager
  private static BackEndManager theInstance;

  
  // The IMTP manager, used to access IMTP-dependent functionalities
  protected IMTPManager myIMTPManager;

  // The node acting as parent
  private Node myNode;
  
  // The child nodes
  private Map children = new HashMap();
  
  private Logger myLogger = Logger.getMyLogger(getClass().getName());

  public static BackEndManager getInstance(Profile p) throws ProfileException {
  	if (theInstance == null) {
  		theInstance = new BackEndManager(p);
  	}
  	return theInstance;
  }
  
  private BackEndManager(Profile p) throws ProfileException {
  	if (p != null) {
	    myIMTPManager = p.getIMTPManager();
	    try {
		    myNode = myIMTPManager.getLocalNode();
	    }
	    catch (IMTPException imtpe) {
	    	throw new ProfileException("Cannot retrieve local node.", imtpe);
	    }
  	}
  	else {
  		throw new ProfileException("Cannot create BackEndManager: Null profile");
  	}
  }
	
  
  public Node getNode() {
  	return myNode;
  }
  
  public synchronized void register(NodeDescriptor child) {
  	children.put(child.getName(), child);
  	if (myLogger.isLoggable(Logger.CONFIG)) {
  		myLogger.log(Logger.CONFIG, "Child node "+child.getName()+" registered.");
  	}
  }
  
  public synchronized void deregister(NodeDescriptor child) {
  	try {
	  	NodeDescriptor dsc = (NodeDescriptor) children.remove(child.getName());
	  	// Notify the PlatformManager
	  	PlatformManager pm = myIMTPManager.getPlatformManagerProxy();
	  	pm.removeNode(dsc, false);
	  	if (myLogger.isLoggable(Logger.CONFIG)) {
	  		myLogger.log(Logger.CONFIG, "Child node "+child.getName()+" deregistered.");
	  	}
  	}
  	catch (Exception e) {
  		myLogger.log(Logger.WARNING, "Error deregistering child node "+child.getName()+". "+e);
  		e.printStackTrace();
  	}
  }
}  
  