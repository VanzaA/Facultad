/**
 * ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 *
 * GNU Lesser General Public License
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */
package jade.imtp.leap.sms;

//#J2ME_EXCLUDE_FILE

import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.ProfileException;

/**
   Utility class that boots the JADE runtime system and automatically
   starts the SMSManager.
   @author Giovanni Caire - TILAB
 */
public class Boot extends jade.Boot {
  public static void main(String args[]) {
    try {
    	// Create the Profile 
    	ProfileImpl p = null;
    	if (args.length > 0) {
    		if (args[0].startsWith("-")) {
    			// Settings specified as command line arguments
    			p = new ProfileImpl(parseCmdLineArgs(args));
    		}
    		else {
    			// Settings specified in a property file
    			p = new ProfileImpl(args[0]);
    		}
    	} 
    	else {
    		// Settings specified in the default property file
    		p = new ProfileImpl(DEFAULT_FILENAME);
    	} 

      // Start a new JADE runtime system
      Runtime.instance().setCloseVM(true);
      // Check whether this is the Main Container or a peripheral container
      if (p.getBooleanProperty(Profile.MAIN, true)) {
        Runtime.instance().createMainContainer(p);
      } else {
        Runtime.instance().createAgentContainer(p);
      }
      
      // Activate the proper SMSManager
      SMSManager.getInstance(p.getProperties());
    }
    catch (ProfileException pe) {
      System.err.println("Error creating the Profile ["+pe.getMessage()+"]");
      pe.printStackTrace();
      printUsage();
      System.exit(-1);
    }
    catch (IllegalArgumentException iae) {
      System.err.println("Command line arguments format error. "+iae.getMessage());
      iae.printStackTrace();
      printUsage();
      System.exit(-1);
    }
  }
}
    