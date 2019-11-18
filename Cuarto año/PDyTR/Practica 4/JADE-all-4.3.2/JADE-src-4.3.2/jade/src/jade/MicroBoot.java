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

package jade;

import jade.core.MicroRuntime;
import jade.util.leap.Properties;
import jade.util.Logger;
import java.io.IOException;

/**
   Main class to start JADE as a split-container.
   @author Giovanni Caire - TILAB
 */
public class MicroBoot {

	private static Logger logger = Logger.getMyLogger("jade.MicroBoot");

	/**
       Default constructor.
	 */
	public MicroBoot() {
	}

	/**
	 * Fires up the <b><em>JADE</em></b> runtime.
	 */
	public static void main(String args[]) {
		String propsFile = null;
		try {
			Properties props = parseCmdLineArgs(args);
			propsFile = props.getProperty("conf");
			if (propsFile != null) {
				props.load(propsFile);
			}
			Logger.initialize(props);
			if (props.getProperty(MicroRuntime.JVM_KEY) == null) {
				//#PJAVA_EXCLUDE_BEGIN
				props.setProperty(MicroRuntime.JVM_KEY, MicroRuntime.J2SE);
				//#PJAVA_EXCLUDE_END
				/*#PJAVA_INCLUDE_BEGIN
				props.setProperty(MicroRuntime.JVM_KEY, MicroRuntime.PJAVA);
				#PJAVA_INCLUDE_END*/
			}

			MicroRuntime.startJADE(props, new Runnable() {
				public void run() {
					// Wait a bit before killing the JVM
					try {
						Thread.sleep(1000);
					}
					catch (InterruptedException ie) {
					}
					logger.log(Logger.INFO,"Exiting now!");
					System.exit(0);
				} 
			});
		}
		catch (IllegalArgumentException iae) {
			logger.log(Logger.SEVERE,"Error reading command line configuration properties. "+iae.getMessage());
			iae.printStackTrace();
			printUsage();
			System.exit(-1);
		}
		catch (IOException ioe) {
			logger.log(Logger.SEVERE,"Error reading configuration properties from file "+propsFile+".", ioe);
			printUsage();
			System.exit(-1);
		}
	}

	public static Properties parseCmdLineArgs(String[] args) throws IllegalArgumentException {
		Properties props = new Properties();

		int i = 0;
		while (i < args.length) {
			if (args[i].startsWith("-")) {
				// Parse next option
				String name = args[i].substring(1);
				if (++i < args.length) {
					props.setProperty(name, args[i]);
				}
				else {
					throw new IllegalArgumentException("No value specified for property \""+name+"\"");
				}
				++i;
			}
			else {
				// Get agents at the end of command line
				if (props.getProperty(MicroRuntime.AGENTS_KEY) != null) {
					if(logger.isLoggable(Logger.WARNING))
						logger.log(Logger.WARNING,"WARNING: overriding agents specification set with the \"-agents\" option");
				}
				String agents = args[i];
				props.setProperty(MicroRuntime.AGENTS_KEY, args[i]);
				if (++i < args.length) {
					if(logger.isLoggable(Logger.WARNING))
						logger.log(Logger.WARNING,"WARNING: ignoring command line argument "+args[i]+" occurring after agents specification");
					if (agents != null && agents.indexOf('(') != -1 && !agents.endsWith(")")) {
						if(logger.isLoggable(Logger.WARNING))
							logger.log(Logger.WARNING,"Note that agent arguments specifications must not contain spaces");
					}
					if (args[i].indexOf(':') != -1) {
						if(logger.isLoggable(Logger.WARNING))
							logger.log(Logger.WARNING,"Note that agent specifications must be separated by a semicolon character \";\" without spaces");
					}
				}
				break;
			}
		}

		return props;
	}

	private static void printUsage() {
		logger.log(Logger.ALL,"Usage:");
		logger.log(Logger.ALL,"java -cp <classpath> jade.MicroBoot [options] [agents]");
		logger.log(Logger.ALL,"Options:");
		logger.log(Logger.ALL,"    -conf <file-name>. Read configuration properties from the specified file name");
		logger.log(Logger.ALL,"    -host <host-name>. The name/address of the host where the BackEnd has to be created");
		logger.log(Logger.ALL,"    -port <port-number>. The port of the J2SE container active on \"host\"");
		logger.log(Logger.ALL,"    -<key> <value>");
		logger.log(Logger.ALL,"Agents: [-agents] <semicolon-separated agent-specifiers>");
		logger.log(Logger.ALL,"     where agent-specifier = <agent-name>:<agent-class>[(comma separated args)]\n"); 
	}

}

